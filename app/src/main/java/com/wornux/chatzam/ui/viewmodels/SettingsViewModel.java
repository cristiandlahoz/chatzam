package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wornux.chatzam.services.SettingsService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import com.wornux.chatzam.utils.PreferenceConstants;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {
    
    private final SettingsService settingsService;

    private final MutableLiveData<Boolean> pushNotifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageSounds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showOnlineStatus = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(SettingsService settingsService) {
        this.settingsService = settingsService;
        loadSettings();
    }
    
    public LiveData<Boolean> getPushNotifications() {
        return pushNotifications;
    }
    
    public LiveData<Boolean> getMessageSounds() {
        return messageSounds;
    }
    
    public LiveData<Boolean> getShowOnlineStatus() {
        return showOnlineStatus;
    }
    
    private void loadSettings() {
        pushNotifications.setValue(settingsService.getPushNotificationsPreference());
        messageSounds.setValue(settingsService.getMessageSoundsPreference());
        showOnlineStatus.setValue(settingsService.getShowOnlineStatusPreference());
    }
    
    public void updatePushNotifications(boolean enabled) {
        pushNotifications.setValue(enabled);
        settingsService.savePushNotificationsPreference(enabled);
    }
    
    public void updateMessageSounds(boolean enabled) {
        messageSounds.setValue(enabled);
        settingsService.saveMessageSoundsPreference(enabled);
    }
    
    public void updateShowOnlineStatus(boolean enabled) {
        showOnlineStatus.setValue(enabled);
        settingsService.saveShowOnlineStatusPreference(enabled);
    }
    
}