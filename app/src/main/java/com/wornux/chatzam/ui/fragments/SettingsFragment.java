package com.wornux.chatzam.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wornux.chatzam.databinding.FragmentSettingsBinding;
import com.wornux.chatzam.ui.base.BaseFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment {
    
    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        loadSettings();
    }
    
    private void loadSettings() {
        binding.pushNotificationsSwitch.setChecked(
            preferences.getBoolean("push_notifications", true));
        binding.messageSoundsSwitch.setChecked(
            preferences.getBoolean("message_sounds", true));
        binding.onlineStatusSwitch.setChecked(
            preferences.getBoolean("show_online_status", true));
        binding.readReceiptsSwitch.setChecked(
            preferences.getBoolean("read_receipts", true));
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("push_notifications", binding.pushNotificationsSwitch.isChecked());
        editor.putBoolean("message_sounds", binding.messageSoundsSwitch.isChecked());
        editor.putBoolean("show_online_status", binding.onlineStatusSwitch.isChecked());
        editor.putBoolean("read_receipts", binding.readReceiptsSwitch.isChecked());
        editor.apply();
    }
    
    @Override
    protected void setupObservers() {
    }
    
    @Override
    protected void setupClickListeners() {
        binding.pushNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            showSnackbar("Notification settings updated");
        });
        
        binding.messageSoundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            showSnackbar("Sound settings updated");
        });
        
        binding.onlineStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            showSnackbar("Privacy settings updated");
        });
        
        binding.readReceiptsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            showSnackbar("Privacy settings updated");
        });
        
        binding.logoutButton.setOnClickListener(v -> {
            showSnackbar("Logout functionality coming soon!");
        });
        
        binding.deleteAccountButton.setOnClickListener(v -> {
            showSnackbar("Account deletion functionality coming soon!");
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}