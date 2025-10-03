package com.wornux.chatzam.ui.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wornux.chatzam.data.repositories.SettingsRepository;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.ui.base.BaseViewModel;
import com.wornux.chatzam.utils.PreferenceConstants;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {
    
    private final SettingsRepository repository;

    private final AuthenticationManager authManager;
    private final MutableLiveData<Boolean> pushNotifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageSounds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showOnlineStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> readReceipts = new MutableLiveData<>();
    
    @Inject
    public SettingsViewModel(SettingsRepository repository, AuthenticationManager authManager) {
        this.repository = repository;
        this.authManager = authManager;
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
    
    public LiveData<Boolean> getReadReceipts() {
        return readReceipts;
    }
    
    private void loadSettings() {
        pushNotifications.setValue(repository.getBoolean(
            PreferenceConstants.KEY_PUSH_NOTIFICATIONS, 
            PreferenceConstants.DEFAULT_PUSH_NOTIFICATIONS));
        
        messageSounds.setValue(repository.getBoolean(
            PreferenceConstants.KEY_MESSAGE_SOUNDS, 
            PreferenceConstants.DEFAULT_MESSAGE_SOUNDS));
        
        showOnlineStatus.setValue(repository.getBoolean(
            PreferenceConstants.KEY_SHOW_ONLINE_STATUS, 
            PreferenceConstants.DEFAULT_SHOW_ONLINE_STATUS));
        
        readReceipts.setValue(repository.getBoolean(
            PreferenceConstants.KEY_READ_RECEIPTS, 
            PreferenceConstants.DEFAULT_READ_RECEIPTS));
    }
    
    public void updatePushNotifications(boolean enabled) {
        pushNotifications.setValue(enabled);
        repository.saveBoolean(PreferenceConstants.KEY_PUSH_NOTIFICATIONS, enabled);
    }
    
    public void updateMessageSounds(boolean enabled) {
        messageSounds.setValue(enabled);
        repository.saveBoolean(PreferenceConstants.KEY_MESSAGE_SOUNDS, enabled);
    }
    
    public void updateShowOnlineStatus(boolean enabled) {
        showOnlineStatus.setValue(enabled);
        repository.saveBoolean(PreferenceConstants.KEY_SHOW_ONLINE_STATUS, enabled);
    }
    
    public void updateReadReceipts(boolean enabled) {
        readReceipts.setValue(enabled);
        repository.saveBoolean(PreferenceConstants.KEY_READ_RECEIPTS, enabled);
    }

    public void logout(){

    }
    
}