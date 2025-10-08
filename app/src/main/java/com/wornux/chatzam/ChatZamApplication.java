package com.wornux.chatzam;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ChatZamApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    createNotificationChannel();
  }

  private void createNotificationChannel() {
    String channelId = "chat_messages";
    String channelName = "Chat Messages";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    boolean vibrationEnabled = true;
    boolean lightsEnabled = true;
    boolean soundEnabled = true;

    NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
    channel.setDescription("Notifications for new chat messages");
    channel.enableVibration(vibrationEnabled);
    channel.enableLights(lightsEnabled);
    channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);

    Log.i(
        "ChatZamApplication",
        String.format(
            "canonical-log-line operation=create_notification_channel "
                + "channel_id=%s channel_name=\"%s\" "
                + "importance=%d vibration_enabled=%b lights_enabled=%b "
                + "sound_enabled=%b sdk_version=%d status=success",
            channelId,
            channelName,
            importance,
            vibrationEnabled,
            lightsEnabled,
            soundEnabled,
            Build.VERSION.SDK_INT));
  }
}
