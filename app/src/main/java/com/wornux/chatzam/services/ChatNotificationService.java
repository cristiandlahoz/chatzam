package com.wornux.chatzam.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wornux.chatzam.MainActivity;
import com.wornux.chatzam.R;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@AndroidEntryPoint
public class ChatNotificationService extends FirebaseMessagingService {

  private static final String TAG = "ChatNotificationService";
  private static final String CHANNEL_ID = "chat_messages";
  private static final int NOTIFICATION_ID = 100;

  @Inject FCMTokenService fcmTokenService;

  @Inject SettingsService settingsService;

  @Override
  public void onNewToken(@NotNull String token) {
    super.onNewToken(token);
    Log.d(TAG, "New FCM token received");

    fcmTokenService
        .registerToken(token)
        .addOnSuccessListener(task -> Log.d(TAG, "New FCM token registered"))
        .addOnFailureListener(e -> Log.d(TAG, "New FCM token registration failed"));
  }

  @Override
  public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);

    Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

    if (remoteMessage.getData().isEmpty()) {
      Log.d(TAG, "Message data payload is empty");
      return;
    }

    String chatId = remoteMessage.getData().get("chatId");
    String messageId = remoteMessage.getData().get("messageId");
    String senderName = remoteMessage.getData().get("senderName");
    String senderId = remoteMessage.getData().get("senderId");

    boolean notificationsEnabled = settingsService.getPushNotificationsPreference();

    if (!notificationsEnabled) {
      Log.d(TAG, "Notifications are disabled globally");
      return;
    }

    if (remoteMessage.getNotification() != null) {
      String title = remoteMessage.getNotification().getTitle();
      String body = remoteMessage.getNotification().getBody();

      showNotification(title, body, chatId, messageId);
    }
  }

  private void showNotification(String title, String body, String chatId, String messageId) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("chatId", chatId);
    intent.putExtra("messageId", messageId);
    intent.putExtra("openChat", true);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.message_multiple_02_stroke_rounded)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE);

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    NotificationChannel channel =
        new NotificationChannel(CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription("Notifications for new chat messages");
    channel.enableVibration(true);
    channel.enableLights(true);
    channel.setSound(defaultSoundUri, null);
    notificationManager.createNotificationChannel(channel);

    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    Log.d(TAG, "Notification displayed for chat: " + chatId);
  }
}
