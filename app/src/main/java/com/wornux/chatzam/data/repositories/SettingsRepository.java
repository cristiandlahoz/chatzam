package com.wornux.chatzam.data.repositories;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsRepository {

  private final SharedPreferences preferences;

  @Inject
  public SettingsRepository(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  public void saveBoolean(String key, boolean value) {
    preferences.edit().putBoolean(key, value).apply();
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    return preferences.getBoolean(key, defaultValue);
  }

}
