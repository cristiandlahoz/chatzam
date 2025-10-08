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

  private final FirebaseAuth auth;
  private final FirebaseMessaging messaging;
  private final UserService userService;

  @Inject
  public FCMTokenService(FirebaseManager firebaseManager, UserService userService) {
    this.userService = userService;
    this.auth = firebaseManager.getFirebaseAuth();
    this.messaging = firebaseManager.getMessaging();
  }

  public void registerToken() {
    messaging
        .getToken()
        .addOnSuccessListener(
            token -> this.registerToken(token)
                .addOnSuccessListener(v -> Log.d(TAG, "FCM token registered successfully"))
                .addOnFailureListener(e -> Log.d(TAG, "FCM token registration failed")))
        .addOnFailureListener(e -> Log.w(TAG, "Failed to obtain token from firebase messaging", e));
  }

  public Task<Void> registerToken(String token) {
    String userId = getCurrentUserId();
    if (userId == null) {
      Log.e(TAG, "Cannot register token: User not authenticated");
      return Tasks.forException(new IllegalStateException("User not authenticated"));
    }

    Log.d(TAG, "Registering FCM token for user: " + userId);

    return userService.updateFmcTokens(userId, token)
        .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token registered successfully"))
        .addOnFailureListener(e -> Log.e(TAG, "Failed to register FCM token", e));
  }

  private String getCurrentUserId() {
    return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
  }
}
