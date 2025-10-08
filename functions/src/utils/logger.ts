import * as functions from "firebase-functions";

export function logInfo(message: string, data?: Record<string, unknown>): void {
  functions.logger.info(message, data);
}

export function logError(message: string, error?: unknown, data?: Record<string, unknown>): void {
  const errorData = {
    ...data,
    error: error instanceof Error ? {
      message: error.message,
      stack: error.stack,
    } : String(error),
  };
  functions.logger.error(message, errorData);
}

export function logWarning(message: string, data?: Record<string, unknown>): void {
  functions.logger.warn(message, data);
}

export function logDebug(message: string, data?: Record<string, unknown>): void {
  functions.logger.debug(message, data);
}
