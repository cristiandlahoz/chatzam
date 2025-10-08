import * as admin from "firebase-admin";
import {Message, MessageType, Chat} from "../types/firestore-types";
import {truncateText} from "../utils/string-utils";
import {logInfo, logError} from "../utils/logger";

export async function sendNotificationsToRecipients(
  message: Message,
  chat: Chat,
  tokens: string[],
  senderName: string
): Promise<{success: string[], failed: string[]}> {
  const successfulTokens: string[] = [];
  const failedTokens: string[] = [];

  const notificationBody = buildNotificationBody(message);

  const sendPromises = tokens.map(async (token) => {
    try {
      const payload = buildFCMPayload(message, chat, notificationBody, senderName);
      await admin.messaging().send({
        token: token,
        ...payload,
      });

      successfulTokens.push(token);
      logInfo("Notification sent successfully", {
        messageId: message.message_id,
        token: token.substring(0, 20) + "...",
      });
    } catch (error) {
      failedTokens.push(token);

      if (isInvalidToken(error)) {
        await removeInvalidTokenFromParticipants(chat.chat_id, token);
      }

      logError("Failed to send notification", error, {
        messageId: message.message_id,
        token: token.substring(0, 20) + "...",
      });
    }
  });

  await Promise.allSettled(sendPromises);

  return {
    success: successfulTokens,
    failed: failedTokens,
  };
}

function buildNotificationBody(message: Message): string {
  if (message.message_type === MessageType.TEXT) {
    return truncateText(message.content || "", 100);
  }

  switch (message.message_type) {
  case MessageType.IMAGE:
    return "📷 Sent an image";
  default:
    return "Sent a message";
  }
}

function buildFCMPayload(
  message: Message,
  chat: Chat,
  notificationBody: string,
  senderName: string
): Omit<admin.messaging.Message, "token"> {
  return {
    notification: {
      title: senderName,
      body: notificationBody,
    },
    data: {
      chatId: message.chat_id,
      messageId: message.message_id,
      senderId: message.sender_id,
      senderName: senderName,
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

async function removeInvalidTokenFromParticipants(chatId: string, invalidToken: string): Promise<void> {
  try {
    const chatRef = admin.firestore().collection("chats").doc(chatId);
    const chatDoc = await chatRef.get();

    if (!chatDoc.exists) {
      return;
    }

    const chat = chatDoc.data() as Chat;
    if (!chat.participant_details) {
      return;
    }

    for (const [participantId, participantData] of Object.entries(chat.participant_details)) {
      if (participantData.fcm_tokens?.includes(invalidToken)) {
        const updatedTokens = participantData.fcm_tokens.filter((t) => t !== invalidToken);
        await chatRef.update({
          [`participant_details.${participantId}.fcm_tokens`]: updatedTokens,
        });
        logInfo("Removed invalid FCM token from participant_details", {
          chatId,
          participantId,
          token: invalidToken.substring(0, 20) + "...",
        });
        break;
      }
    }
  } catch (error) {
    logError("Failed to remove invalid token from participant_details", error, {chatId});
  }
}
