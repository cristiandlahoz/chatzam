package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.wornux.chatzam.R;
import com.wornux.chatzam.databinding.FragmentSettingsBinding;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.SettingsViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment<SettingsViewModel> {
    
    private FragmentSettingsBinding binding;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    protected Class<SettingsViewModel> getViewModelClass() {
        return SettingsViewModel.class;
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getPushNotifications().observe(getViewLifecycleOwner(), enabled -> 
            binding.pushNotificationsSwitch.setChecked(enabled));
        
        viewModel.getMessageSounds().observe(getViewLifecycleOwner(), enabled -> 
            binding.messageSoundsSwitch.setChecked(enabled));
        
        viewModel.getShowOnlineStatus().observe(getViewLifecycleOwner(), enabled -> 
            binding.onlineStatusSwitch.setChecked(enabled));
        
        viewModel.getReadReceipts().observe(getViewLifecycleOwner(), enabled -> 
            binding.readReceiptsSwitch.setChecked(enabled));
    }
    
    @Override
    protected void setupClickListeners() {
        binding.pushNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updatePushNotifications(isChecked);
            showSnackbar(getString(R.string.notification_settings_updated));
        });
        
        binding.messageSoundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateMessageSounds(isChecked);
            showSnackbar(getString(R.string.sound_settings_updated));
        });
        
        binding.onlineStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateShowOnlineStatus(isChecked);
            showSnackbar(getString(R.string.privacy_settings_updated));
        });
        
        binding.readReceiptsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateReadReceipts(isChecked);
            showSnackbar(getString(R.string.privacy_settings_updated));
        });
        
        binding.logoutButton.setOnClickListener(v -> showSnackbar(getString(R.string.logout_functionality_coming_soon)));
        
        binding.deleteAccountButton.setOnClickListener(v -> showSnackbar(getString(R.string.account_deletion_functionality_coming_soon)));
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}