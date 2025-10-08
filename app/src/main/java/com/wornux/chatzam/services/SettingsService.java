package com.wornux.chatzam.services;

import android.content.SharedPreferences;
import com.wornux.chatzam.utils.PreferenceConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsService {

  private final SharedPreferences preferences;

  @Inject
  public SettingsService(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  public boolean getPushNotificationsPreference() {
    return getBoolean(
        PreferenceConstants.KEY_PUSH_NOTIFICATIONS, PreferenceConstants.DEFAULT_PUSH_NOTIFICATIONS);
  }

  public void savePushNotificationsPreference(boolean enabled) {
    saveBoolean(PreferenceConstants.KEY_PUSH_NOTIFICATIONS, enabled);
  }

  public boolean getMessageSoundsPreference() {
    return getBoolean(
        PreferenceConstants.KEY_MESSAGE_SOUNDS, PreferenceConstants.DEFAULT_MESSAGE_SOUNDS);
  }

  public void saveMessageSoundsPreference(boolean enabled) {
    saveBoolean(PreferenceConstants.KEY_MESSAGE_SOUNDS, enabled);
  }

  private void saveBoolean(String key, boolean value) {
    preferences.edit().putBoolean(key, value).apply();
  }

  private boolean getBoolean(String key, boolean defaultValue) {
    return preferences.getBoolean(key, defaultValue);
  }
}
