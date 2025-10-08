import * as crypto from "crypto";

const ALGORITHM = "aes-256-cbc";

export function decryptContent(encryptedContent: string, encryptionKey: string): string {
  try {
    const keyBuffer = Buffer.from(encryptionKey, "base64");
    const parts = encryptedContent.split(":");

    if (parts.length !== 2) {
      throw new Error("Invalid encrypted content format");
    }

    const iv = Buffer.from(parts[0], "hex");
    const encryptedText = Buffer.from(parts[1], "hex");

    const decipher = crypto.createDecipheriv(ALGORITHM, keyBuffer, iv);
    let decrypted = decipher.update(encryptedText);
    decrypted = Buffer.concat([decrypted, decipher.final()]);

    return decrypted.toString("utf8");
  } catch (error) {
    throw new Error(`Decryption failed: ${error instanceof Error ? error.message : "Unknown error"}`);
  }
}

export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
    return text;
  }
  return text.substring(0, maxLength) + "...";
}
