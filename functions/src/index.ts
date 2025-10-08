import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {Chat, Message} from "./types/firestore-types";
import {sendNotificationsToRecipients} from "./services/notification-service";
import {storeFailedNotification, processRetries} from "./services/retry-service";
import {logInfo, logError, logWarning} from "./utils/logger";

admin.initializeApp();

export const onMessageCreated = functions
  .region("us-east1")
  .firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const messageId = context.params.messageId;
    const chatId = context.params.chatId;

    try {
      const message = snapshot.data() as Message;

      logInfo("Processing new message", {
        messageId,
        chatId,
        senderId: message.sender_id,
        messageType: message.message_type,
      });

      const chatDoc = await admin.firestore()
        .collection("chats")
        .doc(chatId)
        .get();

      if (!chatDoc.exists) {
        logError("Chat document not found", undefined, {chatId, messageId});
        return;
      }

      const chat = chatDoc.data() as Chat;

      if (!chat.participant_details) {
        logWarning("Chat has no participant_details", {chatId, messageId});
        return;
      }

      const participantEntries = Object.entries(chat.participant_details);

      if (participantEntries.length === 0) {
        logWarning("Chat participant_details is empty", {chatId, messageId});
        return;
      }

      const recipientTokens: string[] = [];
      const recipientIds: string[] = [];
      let senderName = message.sender_name;

      for (const [participantId, participantData] of participantEntries) {
        if (participantId === message.sender_id) {
          senderName = participantData.display_name || message.sender_name;
          continue;
        }

        if (participantData.fcm_tokens && participantData.fcm_tokens.length > 0) {
          recipientIds.push(participantId);
          recipientTokens.push(...participantData.fcm_tokens);
        }
      }

      if (recipientTokens.length === 0) {
        logInfo("No recipients with FCM tokens", {chatId, messageId});
        return;
      }

      logInfo("Extracted FCM tokens from participant_details", {
        chatId,
        messageId,
        recipientCount: recipientIds.length,
        tokenCount: recipientTokens.length,
      });

      const result = await sendNotificationsToRecipients(message, chat, recipientTokens, senderName);

      if (result.success.length > 0) {
        await snapshot.ref.update({
          is_delivered: true,
          delivered_at: admin.firestore.FieldValue.serverTimestamp(),
        });

        logInfo("Message marked as delivered", {
          messageId,
          chatId,
          successCount: result.success.length,
        });
      }

      if (result.failed.length > 0) {
        const failedByUser: {[userId: string]: string[]} = {};
        for (const [participantId, participantData] of participantEntries) {
          if (participantId === message.sender_id) continue;
          const userFailedTokens = participantData.fcm_tokens?.filter((token) =>
            result.failed.includes(token)
          ) || [];
          if (userFailedTokens.length > 0) {
            failedByUser[participantId] = userFailedTokens;
          }
        }

        await storeFailedNotification(
          messageId,
          chatId,
          recipientIds,
          failedByUser,
          "Initial send partially failed"
        );

        logWarning("Some notifications failed, stored for retry", {
          messageId,
          chatId,
          failedTokenCount: result.failed.length,
        });
      }

      logInfo("Message notification processing complete", {
        messageId,
        chatId,
        successCount: result.success.length,
        failedCount: result.failed.length,
      });
    } catch (error) {
      logError("Failed to process message notification", error, {
        messageId,
        chatId,
      });
    }
  });

export const processNotificationRetries = functions
  .region("us-east1")
  .pubsub
  .schedule("every 1 minutes")
  .onRun(async () => {
    logInfo("Starting notification retry processing");

    try {
      await processRetries();
      logInfo("Notification retry processing complete");
    } catch (error) {
      logError("Failed to process notification retries", error);
    }

    return null;
  });
