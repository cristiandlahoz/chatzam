package com.wornux.chatzam.ui.viewmodels;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.UserProfile;
import com.wornux.chatzam.data.enums.UserStatus;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class UserProfileViewModel extends BaseViewModel {
    
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    
    private final MutableLiveData<UserProfile> currentProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileUpdated = new MutableLiveData<>();
    private final MutableLiveData<String> profileImageUrl = new MutableLiveData<>();
    
    @Inject
    public UserProfileViewModel(UserRepository userRepository, 
                               AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        
        loadCurrentUserProfile();
    }
    
    public LiveData<UserProfile> getCurrentProfile() {
        return currentProfile;
    }
    
    public LiveData<Boolean> isProfileUpdated() {
        return profileUpdated;
    }
    
    public LiveData<String> getProfileImageUrl() {
        return profileImageUrl;
    }
    
    private void loadCurrentUserProfile() {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            setLoading(true);
            userRepository.getUserProfile(currentUserId)
                    .addOnSuccessListener(profile -> {
                        setLoading(false);
                        if (profile != null) {
                            currentProfile.setValue(profile);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        setLoading(false);
                        setError("Failed to load profile: " + exception.getMessage());
                    });
        }
    }
    
    public void updateProfile(String displayName, UserStatus status) {
        UserProfile current = currentProfile.getValue();
        String currentUserId = getCurrentUserId();
        
        if (current == null || currentUserId == null) {
            setError("Unable to update profile");
            return;
        }
        
        if (displayName == null || displayName.trim().isEmpty()) {
            setError("Display name cannot be empty");
            return;
        }
        
        UserProfile updatedProfile = UserProfile.builder()
                .userId(currentUserId)
                .email(current.getEmail())
                .displayName(displayName.trim())
                .profileImageUrl(current.getProfileImageUrl())
                .isOnline(current.isOnline())
                .lastSeen(current.getLastSeen())
                .status(status)
                .build();
        
        setLoading(true);
        userRepository.updateUserProfile(updatedProfile)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    currentProfile.setValue(updatedProfile);
                    profileUpdated.setValue(true);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to update profile: " + exception.getMessage());
                });
    }
    
    public void uploadProfileImage(Uri imageUri) {
        String currentUserId = getCurrentUserId();
        
        if (currentUserId == null) {
            setError("User not logged in");
            return;
        }
        
        if (imageUri == null) {
            setError("No image selected");
            return;
        }
        
        setLoading(true);
        userRepository.uploadProfileImage(currentUserId, imageUri)
                .addOnSuccessListener(imageUrl -> {
                    profileImageUrl.setValue(imageUrl);
                    updateProfileImageUrl(imageUrl);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to upload image: " + exception.getMessage());
                });
    }
    
    private void updateProfileImageUrl(String imageUrl) {
        String currentUserId = getCurrentUserId();
        
        if (currentUserId != null) {
            userRepository.updateProfileImage(currentUserId, imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        setLoading(false);
                        UserProfile current = currentProfile.getValue();
                        if (current != null) {
                            UserProfile updated = UserProfile.builder()
                                    .userId(current.getUserId())
                                    .email(current.getEmail())
                                    .displayName(current.getDisplayName())
                                    .profileImageUrl(imageUrl)
                                    .isOnline(current.isOnline())
                                    .lastSeen(current.getLastSeen())
                                    .status(current.getStatus())
                                    .build();
                            currentProfile.setValue(updated);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        setLoading(false);
                        setError("Failed to update profile image: " + exception.getMessage());
                    });
        }
    }
    
    public void refreshProfile() {
        loadCurrentUserProfile();
    }
    
    private String getCurrentUserId() {
        return authManager.getCurrentUser() != null ? 
               authManager.getCurrentUser().getUid() : null;
    }
}