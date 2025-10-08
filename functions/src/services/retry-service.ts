import * as admin from "firebase-admin";
import {NotificationRetry, NotificationFailure} from "../types/firestore-types";
import {logInfo, logError, logWarning} from "../utils/logger";

const RETRY_DELAYS_MINUTES = [1, 5, 15];
const MAX_RETRY_ATTEMPTS = 3;

export async function storeFailedNotification(
  messageId: string,
  chatId: string,
  recipientIds: string[],
  failedTokens: {[userId: string]: string[]},
  error: string
): Promise<void> {
  try {
    const retryDoc: Omit<NotificationRetry, "retry_id"> = {
      message_id: messageId,
      chat_id: chatId,
      recipient_ids: recipientIds,
      failed_tokens: failedTokens,
      attempt_count: 1,
      next_retry_at: admin.firestore.Timestamp.fromMillis(
        Date.now() + RETRY_DELAYS_MINUTES[0] * 60 * 1000
      ),
      last_error: error,
      created_at: admin.firestore.Timestamp.now(),
    };

    const docRef = await admin.firestore()
      .collection("notification_retries")
      .add(retryDoc);

    logInfo("Stored failed notification for retry", {
      retryId: docRef.id,
      messageId,
      recipientCount: recipientIds.length,
    });
  } catch (err) {
    logError("Failed to store retry document", err, {messageId, chatId});
  }
}

export async function processRetries(): Promise<void> {
  const now = admin.firestore.Timestamp.now();
  const db = admin.firestore();

  try {
    const retrySnapshot = await db
      .collection("notification_retries")
      .where("next_retry_at", "<=", now)
      .limit(50)
      .get();

    if (retrySnapshot.empty) {
      logInfo("No retries to process");
      return;
    }

    logInfo(`Processing ${retrySnapshot.size} retry attempts`);

    const retryPromises = retrySnapshot.docs.map(async (doc) => {
      const retry = doc.data() as NotificationRetry;
      retry.retry_id = doc.id;

      try {
        await retryNotification(retry, doc.ref);
      } catch (error) {
        logError("Error processing retry", error, {retryId: doc.id});
      }
    });

    await Promise.allSettled(retryPromises);
  } catch (error) {
    logError("Failed to query retries", error);
  }
}

async function retryNotification(
  retry: NotificationRetry,
  retryRef: admin.firestore.DocumentReference
): Promise<void> {
  try {
    const messageDoc = await admin.firestore()
      .collection("chats")
      .doc(retry.chat_id)
      .collection("messages")
      .doc(retry.message_id)
      .get();

    if (!messageDoc.exists) {
      logWarning("Message no longer exists, deleting retry", {
        messageId: retry.message_id,
        retryId: retry.retry_id,
      });
      await retryRef.delete();
      return;
    }

    const failedAgain: {[userId: string]: string[]} = {};

    for (const [userId, tokens] of Object.entries(retry.failed_tokens)) {
      for (const token of tokens) {
        try {
          await admin.messaging().send({token});
          logInfo("Retry notification sent successfully", {
            userId,
            messageId: retry.message_id,
            attemptCount: retry.attempt_count,
          });
        } catch (error) {
          if (!failedAgain[userId]) {
            failedAgain[userId] = [];
          }
          failedAgain[userId].push(token);
          logWarning("Retry notification failed", {
            userId,
            messageId: retry.message_id,
            attemptCount: retry.attempt_count,
          });
        }
      }
    }

    if (Object.keys(failedAgain).length === 0) {
      await retryRef.delete();
      logInfo("All retry notifications sent successfully", {retryId: retry.retry_id});
      return;
    }

    if (retry.attempt_count >= MAX_RETRY_ATTEMPTS) {
      await moveToFailures(retry, failedAgain);
      await retryRef.delete();
      logWarning("Max retry attempts reached, moved to failures", {
        retryId: retry.retry_id,
        messageId: retry.message_id,
      });
      return;
    }

    const nextAttempt = retry.attempt_count + 1;
    const delayMinutes = RETRY_DELAYS_MINUTES[nextAttempt - 1];

    await retryRef.update({
      attempt_count: nextAttempt,
      failed_tokens: failedAgain,
      next_retry_at: admin.firestore.Timestamp.fromMillis(
        Date.now() + delayMinutes * 60 * 1000
      ),
      last_error: `Retry attempt ${retry.attempt_count} partially failed`,
    });

    logInfo("Updated retry for next attempt", {
      retryId: retry.retry_id,
      nextAttempt,
      delayMinutes,
    });
  } catch (error) {
    logError("Failed to process retry", error, {
      retryId: retry.retry_id,
      messageId: retry.message_id,
    });
  }
}

async function moveToFailures(
  retry: NotificationRetry,
  finalFailedTokens: {[userId: string]: string[]}
): Promise<void> {
  try {
    const failure: Omit<NotificationFailure, "failure_id"> = {
      message_id: retry.message_id,
      chat_id: retry.chat_id,
      recipient_ids: retry.recipient_ids,
      failed_tokens: finalFailedTokens,
      total_attempts: MAX_RETRY_ATTEMPTS,
      final_error: retry.last_error,
      created_at: admin.firestore.Timestamp.now(),
      requires_manual_investigation: true,
    };

    await admin.firestore()
      .collection("notification_failures")
      .add(failure);

    logInfo("Moved failed notification to failures collection", {
      messageId: retry.message_id,
    });
  } catch (error) {
    logError("Failed to move retry to failures", error, {
      retryId: retry.retry_id,
    });
  }
}
