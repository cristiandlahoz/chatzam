import * as admin from "firebase-admin";
import {Message, MessageType, UserDTO} from "../types/firestore-types";
import {decryptMessageContent} from "./encryption-service";
import {truncateText} from "../utils/crypto-utils";
import {logInfo, logError, logWarning} from "../utils/logger";

export async function sendNotificationsToRecipients(
  message: Message,
  recipients: UserDTO[],
  encryptionKey: string
): Promise<{success: string[], failed: {[userId: string]: string[]}}> {
  const successfulTokens: string[] = [];
  const failedTokens: {[userId: string]: string[]} = {};

  const notificationBody = await buildNotificationBody(message, encryptionKey);

  const sendPromises = recipients.map(async (recipient) => {
    if (!recipient.fcm_tokens || recipient.fcm_tokens.length === 0) {
      logWarning("Recipient has no FCM tokens", {userId: recipient.user_id});
      return;
    }

    for (const token of recipient.fcm_tokens) {
      try {
        const payload = buildFCMPayload(message, notificationBody);
        await admin.messaging().send({
          token: token,
          ...payload,
        });

        successfulTokens.push(token);
        logInfo("Notification sent successfully", {
          userId: recipient.user_id,
          messageId: message.message_id,
        });
      } catch (error) {
        if (!failedTokens[recipient.user_id]) {
          failedTokens[recipient.user_id] = [];
        }
        failedTokens[recipient.user_id].push(token);

        if (isInvalidToken(error)) {
          await removeInvalidToken(recipient.user_id, token);
        }

        logError("Failed to send notification", error, {
          userId: recipient.user_id,
          messageId: message.message_id,
          token: token.substring(0, 20) + "...",
        });
      }
    }
  });

  await Promise.allSettled(sendPromises);

  return {
    success: successfulTokens,
    failed: failedTokens,
  };
}

async function buildNotificationBody(message: Message, encryptionKey: string): Promise<string> {
  if (message.message_type === MessageType.TEXT) {
    if (message.encrypted_content) {
      const decrypted = await decryptMessageContent(message.encrypted_content, encryptionKey);
      if (decrypted) {
        return truncateText(decrypted, 100);
      }
      return "🔒 Sent an encrypted message";
    }
    return truncateText(message.content, 100);
  }

  switch (message.message_type) {
  case MessageType.IMAGE:
    return "📷 Sent an image";
  case MessageType.VIDEO:
    return "🎥 Sent a video";
  case MessageType.AUDIO:
    return "🎵 Sent an audio message";
  case MessageType.DOCUMENT:
    return "📄 Sent a document";
  default:
    return "Sent a message";
  }
}

function buildFCMPayload(message: Message, notificationBody: string): Omit<admin.messaging.Message, "token"> {
  return {
    notification: {
      title: message.sender_name,
      body: notificationBody,
    },
    data: {
      chatId: message.chat_id,
      messageId: message.message_id,
      senderId: message.sender_id,
      senderName: message.sender_name,
      messageType: message.message_type,
      timestamp: message.timestamp.toMillis().toString(),
      clickAction: "OPEN_CHAT",
      ...(message.media_url && {mediaUrl: message.media_url}),
    },
    android: {
      priority: "high",
      notification: {
        channelId: "chat_messages",
        sound: "default",
        defaultVibrateTimings: true,
      },
    },
  };
}

function isInvalidToken(error: unknown): boolean {
  if (error instanceof Error) {
    const errorCode = (error as {code?: string}).code;
    return errorCode === "messaging/invalid-registration-token" ||
           errorCode === "messaging/registration-token-not-registered";
  }
  return false;
}

async function removeInvalidToken(userId: string, invalidToken: string): Promise<void> {
  try {
    const userRef = admin.firestore().collection("users").doc(userId);
    await userRef.update({
      fcm_tokens: admin.firestore.FieldValue.arrayRemove(invalidToken),
    });
    logInfo("Removed invalid FCM token", {userId, token: invalidToken.substring(0, 20) + "..."});
  } catch (error) {
    logError("Failed to remove invalid token", error, {userId});
  }
}
