import {decryptContent} from "../utils/crypto-utils";
import {logError, logDebug} from "../utils/logger";

export async function decryptMessageContent(
  encryptedContent: string,
  encryptionKey: string
): Promise<string | null> {
  try {
    if (!encryptedContent || !encryptionKey) {
      logDebug("Missing encrypted content or encryption key");
      return null;
    }

    const decrypted = decryptContent(encryptedContent, encryptionKey);
    logDebug("Message content decrypted successfully");
    return decrypted;
  } catch (error) {
    logError("Failed to decrypt message content", error, {
      hasEncryptedContent: !!encryptedContent,
      hasKey: !!encryptionKey,
    });
    return null;
  }
}
