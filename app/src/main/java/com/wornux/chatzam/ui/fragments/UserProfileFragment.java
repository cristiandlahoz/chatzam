package com.wornux.chatzam.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wornux.chatzam.databinding.FragmentUserProfileBinding;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.enums.UserStatus;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.UserProfileViewModel;
import dagger.hilt.android.AndroidEntryPoint;

import java.text.SimpleDateFormat;
import java.util.Locale;

@AndroidEntryPoint
public class UserProfileFragment extends BaseFragment<UserProfileViewModel> {
    
    private FragmentUserProfileBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        viewModel.uploadProfileImage(imageUri);
                    }
                }
            });
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getCurrentProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                populateProfileData(profile);
            }
        });
        
        viewModel.isProfileUpdated().observe(getViewLifecycleOwner(), isUpdated -> {
            if (isUpdated != null && isUpdated) {
                showSnackbar("Profile updated successfully!");
            }
        });
        
        viewModel.getProfileImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            if (imageUrl != null) {
                showSnackbar("Profile image updated successfully!");
            }
        });

    viewModel
        .getLoading()
        .observe(
            getViewLifecycleOwner(),
            isLoading -> {
              binding.loadingProgressBar.setVisibility(
                  Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
              binding.saveButton.setEnabled(!isLoading);
            });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }
    
    @Override
    protected void setupClickListeners() {
        binding.profileImageView.setOnClickListener(v -> openImagePicker());
        
        binding.saveButton.setOnClickListener(v -> saveProfile());
    }

    @Override
    protected Class<UserProfileViewModel> getViewModelClass() {
        return UserProfileViewModel.class;
    }

    private void populateProfileData(User profile) {
        if (binding.displayNameEditText.getText() == null || 
            binding.displayNameEditText.getText().toString().isEmpty()) {
            binding.displayNameEditText.setText(profile.getDisplayName());
        }
    }
    

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void saveProfile() {
        String displayName = binding.displayNameEditText.getText() != null ? 
                            binding.displayNameEditText.getText().toString().trim() : "";

        viewModel.updateProfile(displayName, UserStatus.ONLINE);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}