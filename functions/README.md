# ChatZam Cloud Functions - Deployment Guide

Firebase Cloud Functions for push notifications with message encryption support.

## Features

- **Automatic Push Notifications**: Triggered when new messages are created
- **Message Encryption**: AES-256 decryption for notification previews
- **Retry Mechanism**: Exponential backoff (1min, 5min, 15min)
- **Multi-Device Support**: Send to all user devices via FCM tokens
- **Delivery Confirmation**: Updates `is_delivered` field on successful send
- **Invalid Token Cleanup**: Automatically removes invalid FCM tokens

---

## Prerequisites

1. **Node.js 18+** installed
2. **Firebase CLI** installed globally:
   ```bash
   npm install -g firebase-tools
   ```
3. **Firebase Project** with:
   - Firestore enabled
   - Firebase Cloud Messaging enabled
   - Billing account linked (required for Cloud Functions)

---

## Installation Steps

### 1. Clone and Navigate
```bash
cd /path/to/chatzam
cd functions
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Firebase Project
Update `.firebaserc` in the root directory:
```json
{
  "projects": {
    "default": "your-actual-firebase-project-id"
  }
}
```

### 4. Login to Firebase
```bash
firebase login
```

### 5. Build TypeScript
```bash
npm run build
```

### 6. Deploy Functions
```bash
firebase deploy --only functions
```

Or deploy specific functions:
```bash
firebase deploy --only functions:onMessageCreated
firebase deploy --only functions:processNotificationRetries
```

---

## Cloud Functions Overview

### 1. `onMessageCreated`
**Trigger:** Firestore document creation at `chats/{chatId}/messages/{messageId}`

**Flow:**
1. Fetches Chat document to get participants and encryption key
2. Filters out sender from recipients
3. Fetches User documents for FCM tokens (batch read)
4. Decrypts message content if encrypted
5. Sends FCM notifications to all recipients
6. Updates `is_delivered = true` on success
7. Stores failed sends in `notification_retries` collection

**Execution Time:** ~2-3 seconds for typical chats

### 2. `processNotificationRetries`
**Trigger:** Cloud Scheduler (every 1 minute)

**Flow:**
1. Queries `notification_retries` collection for due retries
2. Attempts to resend failed notifications
3. Updates retry count and next retry time
4. Moves to `notification_failures` after 3 attempts

**Max Attempts:** 3 (1min → 5min → 15min delays)

---

## Local Testing with Emulator

### 1. Start Firebase Emulators
```bash
cd functions
npm run serve
```

This starts:
- Functions Emulator: http://localhost:5001
- Firestore Emulator: http://localhost:8080

### 2. Test onMessageCreated
Create a test message in Firestore Emulator UI:
```json
{
  "message_id": "test123",
  "sender_id": "user1",
  "sender_name": "John Doe",
  "chat_id": "chat1",
  "content": "",
  "encrypted_content": "abc123...",
  "message_type": "TEXT",
  "timestamp": {"_seconds": 1704067200, "_nanoseconds": 0},
  "is_delivered": false,
  "is_read": false,
  "read_by": []
}
```

Watch function logs in terminal.

### 3. Test Encryption/Decryption
Use the Node.js REPL:
```bash
node
```
```javascript
const crypto = require('crypto');

// Simulate Android encryption
const key = Buffer.from('your-base64-key', 'base64');
const iv = crypto.randomBytes(16);
const cipher = crypto.createCipheriv('aes-256-cbc', key, iv);
let encrypted = cipher.update('Hello World', 'utf8', 'hex');
encrypted += cipher.final('hex');
const result = iv.toString('hex') + ':' + encrypted;
console.log(result); // Use this as encrypted_content
```

---

## Monitoring & Logs

### View Live Logs
```bash
firebase functions:log
```

### View Logs in Firebase Console
1. Go to Firebase Console → Functions
2. Click on function name
3. View logs, metrics, and errors

### Check Retry Queue
Query Firestore collection `notification_retries`:
```javascript
db.collection('notification_retries')
  .where('next_retry_at', '<=', new Date())
  .get()
```

### Check Failed Notifications
Query Firestore collection `notification_failures`:
```javascript
db.collection('notification_failures')
  .where('requires_manual_investigation', '==', true)
  .get()
```

---

## Troubleshooting

### Error: "Firebase project not found"
**Solution:** Update `.firebaserc` with correct project ID

### Error: "Billing account required"
**Solution:** Enable billing in Firebase Console (required for Cloud Functions)

### Error: "Permission denied"
**Solution:** Ensure Firebase service account has necessary permissions:
- Cloud Functions Admin
- Firestore Admin
- Firebase Cloud Messaging Admin

### Notifications Not Sending
1. Check Cloud Function logs: `firebase functions:log`
2. Verify FCM tokens exist in user documents
3. Test FCM token validity in Firebase Console
4. Check Android app has `google-services.json` configured

### Decryption Failing
1. Verify `encryption_key` exists in Chat document
2. Ensure Android encryption format matches: `iv_hex:encrypted_hex`
3. Check key is base64-encoded 256-bit AES key

### Retry Function Not Running
1. Verify Cloud Scheduler is enabled in GCP Console
2. Check function logs for errors
3. Ensure `notification_retries` collection has documents with `next_retry_at <= now()`

---

## Cost Optimization Tips

1. **Batch Firestore Reads**: Already implemented with `getAll()`
2. **Limit Recipients**: Max 10 participants enforced
3. **Clean Up Retries**: Automatically deleted after 3 attempts
4. **Function Memory**: Set to 256MB (sufficient for most cases)

### Adjust Function Memory (if needed)
Edit `functions/src/index.ts`:
```typescript
export const onMessageCreated = functions
  .region("us-central1")
  .runWith({ memory: "256MB" })  // or "512MB", "1GB"
  .firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    // ...
  });
```

---

## Environment Variables (Optional)

Set custom environment variables:
```bash
firebase functions:config:set encryption.max_preview_length="100"
```

Access in code:
```typescript
import * as functions from "firebase-functions";
const maxLength = functions.config().encryption.max_preview_length || 100;
```

---

## Updating Functions

### After Code Changes
```bash
cd functions
npm run build
firebase deploy --only functions
```

### Update Dependencies
```bash
cd functions
npm update
npm audit fix
npm run build
firebase deploy --only functions
```

---

## Production Checklist

- [ ] Updated `.firebaserc` with production project ID
- [ ] Enabled billing in Firebase Console
- [ ] Deployed functions: `firebase deploy --only functions`
- [ ] Verified `onMessageCreated` triggers on new messages
- [ ] Tested notification delivery on Android devices
- [ ] Confirmed encryption/decryption works correctly
- [ ] Set up alerting for function errors in Firebase Console
- [ ] Documented project-specific encryption key management
- [ ] Tested retry mechanism with failed sends
- [ ] Verified invalid token cleanup

---

## Security Notes

1. **Encryption Keys**: Stored in Firestore, encrypted at rest by Firebase
2. **Service Account**: Managed automatically by Firebase Admin SDK
3. **FCM Tokens**: Not sensitive, safe to store in Firestore
4. **Logs**: Never log encryption keys or full tokens (only log first 20 chars)
5. **Decrypted Content**: Held in memory only, never persisted

---

## Support

For issues or questions:
1. Check Firebase Functions logs
2. Review Firestore security rules
3. Verify Android app configuration
4. Test with Firebase Emulator locally

---

## Version History

- **v1.0.0** (2025-01-07): Initial implementation
  - Message notifications with encryption
  - Retry mechanism
  - Multi-device support
  - Delivery confirmation
