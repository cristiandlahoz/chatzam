import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {Chat, Message, UserDTO} from "./types/firestore-types";
import {sendNotificationsToRecipients} from "./services/notification-service";
import {storeFailedNotification, processRetries} from "./services/retry-service";
import {logInfo, logError, logWarning} from "./utils/logger";

admin.initializeApp();

export const onMessageCreated = functions
  .region("us-central1")
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

      if (!chat.participants || chat.participants.length === 0) {
        logWarning("Chat has no participants", {chatId, messageId});
        return;
      }

      const recipientIds = chat.participants.filter((id) => id !== message.sender_id);

      if (recipientIds.length === 0) {
        logInfo("No recipients to notify (sender only)", {chatId, messageId});
        return;
      }

      if (recipientIds.length > 9) {
        logWarning("Chat has more than 9 recipients, limiting notifications", {
          chatId,
          recipientCount: recipientIds.length,
        });
      }

      const userDocs = await admin.firestore()
        .collection("users")
        .where(admin.firestore.FieldPath.documentId(), "in", recipientIds.slice(0, 10))
        .get();

      const recipients: UserDTO[] = userDocs.docs
        .map((doc) => doc.data() as UserDTO)
        .filter((user) => user.fcm_tokens && user.fcm_tokens.length > 0);

      if (recipients.length === 0) {
        logWarning("No recipients with FCM tokens", {chatId, messageId});
        return;
      }

      const encryptionKey = chat.encryption_key || "";
      if (!encryptionKey && message.encrypted_content) {
        logWarning("Message is encrypted but chat has no encryption key", {chatId, messageId});
      }

      const result = await sendNotificationsToRecipients(message, recipients, encryptionKey);

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

      if (Object.keys(result.failed).length > 0) {
        await storeFailedNotification(
          messageId,
          chatId,
          recipientIds,
          result.failed,
          "Initial send partially failed"
        );

        logWarning("Some notifications failed, stored for retry", {
          messageId,
          chatId,
          failedUserCount: Object.keys(result.failed).length,
        });
      }

      logInfo("Message notification processing complete", {
        messageId,
        chatId,
        successCount: result.success.length,
        failedCount: Object.keys(result.failed).length,
      });
    } catch (error) {
      logError("Failed to process message notification", error, {
        messageId,
        chatId,
      });
    }
  });

export const processNotificationRetries = functions
  .region("us-central1")
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
