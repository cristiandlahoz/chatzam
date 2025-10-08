package com.wornux.chatzam.services;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FCMTokenService {

  private static final String TAG = "FCMTokenService";
  private static final String USERS_COLLECTION = "users";
  private static final String FCM_TOKENS_FIELD = "fcm_tokens";

  private final FirebaseFirestore db;
  private final FirebaseAuth auth;
  private final FirebaseMessaging messaging;

  @Inject
  public FCMTokenService(FirebaseManager firebaseManager) {
    this.db = firebaseManager.getFirestore();
    this.auth = firebaseManager.getFirebaseAuth();
    this.messaging = firebaseManager.getMessaging();
  }

  public void registerToken() {
    messaging
        .getToken()
        .addOnSuccessListener(
            token -> {
              this.registerToken(token)
                  .addOnSuccessListener(
                      v -> Log.d(TAG, "FCM token registered successfully"))
                  .addOnFailureListener(
                      e -> Log.d(TAG, "FCM token registration failed"));
            })
        .addOnFailureListener(e -> Log.w(TAG, "Failed to obtain token from firebase messaging", e));
  }

  public Task<Void> registerToken(String token) {
    String userId = getCurrentUserId();
    if (userId == null) {
      Log.e(TAG, "Cannot register token: User not authenticated");
      return Tasks.forException(new IllegalStateException("User not authenticated"));
    }

    Log.d(TAG, "Registering FCM token for user: " + userId);

    return db.collection(USERS_COLLECTION)
        .document(userId)
        .update(FCM_TOKENS_FIELD, FieldValue.arrayUnion(token))
        .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token registered successfully"))
        .addOnFailureListener(e -> Log.e(TAG, "Failed to register FCM token", e));
  }

  public Task<Void> unregisterToken(String token) {
    String userId = getCurrentUserId();
    if (userId == null) {
      Log.e(TAG, "Cannot unregister token: User not authenticated");
      return com.google.android.gms.tasks.Tasks.forException(
          new IllegalStateException("User not authenticated"));
    }

    Log.d(TAG, "Unregistering FCM token for user: " + userId);

    return db.collection(USERS_COLLECTION)
        .document(userId)
        .update(FCM_TOKENS_FIELD, FieldValue.arrayRemove(token))
        .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token unregistered successfully"))
        .addOnFailureListener(e -> Log.e(TAG, "Failed to unregister FCM token", e));
  }

  public void updateToken(String oldToken, String newToken) {
    String userId = getCurrentUserId();
    if (userId == null) {
      Log.e(TAG, "Cannot update token: User not authenticated");
      return;
    }

    Log.d(TAG, "Updating FCM token for user: " + userId);

    if (oldToken != null && !oldToken.isEmpty()) {
      db.collection(USERS_COLLECTION)
          .document(userId)
          .update(FCM_TOKENS_FIELD, FieldValue.arrayRemove(oldToken))
          .addOnSuccessListener(aVoid -> Log.d(TAG, "Old FCM token removed"))
          .addOnFailureListener(e -> Log.e(TAG, "Failed to remove old FCM token", e));
    }

    db.collection(USERS_COLLECTION)
        .document(userId)
        .update(FCM_TOKENS_FIELD, FieldValue.arrayUnion(newToken))
        .addOnSuccessListener(aVoid -> Log.d(TAG, "New FCM token registered"))
        .addOnFailureListener(e -> Log.e(TAG, "Failed to register new FCM token", e));
  }

  private String getCurrentUserId() {
    return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
  }
}
