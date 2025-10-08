package com.wornux.chatzam.ui.viewmodels;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.enums.UserStatus;
import com.wornux.chatzam.services.UserService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class UserProfileViewModel extends BaseViewModel {

  private final UserService userService;
  private final AuthenticationManager authManager;

  private final MutableLiveData<User> currentProfile = new MutableLiveData<>();
  private final MutableLiveData<Boolean> profileUpdated = new MutableLiveData<>();
  private final MutableLiveData<String> profileImageUrl = new MutableLiveData<>();
  private final MutableLiveData<Uri> profileImageUri = new MutableLiveData<>();

  @Inject
  public UserProfileViewModel(UserService userService, AuthenticationManager authManager) {
    this.userService = userService;
    this.authManager = authManager;

    loadCurrentUser();
  }

  public LiveData<User> getCurrentProfile() {
    return currentProfile;
  }

  public LiveData<Boolean> isProfileUpdated() {
    return profileUpdated;
  }

  public LiveData<String> getProfileImageUrl() {
    return profileImageUrl;
  }

  public LiveData<Uri> getProfileImageUri() {
    return profileImageUri;
  }

  public void setProfileImageUri(Uri uri) {
    profileImageUri.setValue(uri);
  }

  private void loadCurrentUser() {
    String currentUserId = getCurrentUserId();
    if (currentUserId != null) {
      setLoading(true);
      userService
          .getUserProfile(currentUserId)
          .addOnSuccessListener(
              profile -> {
                setLoading(false);
                if (profile != null) {
                  currentProfile.setValue(profile);
                }
              })
          .addOnFailureListener(
              exception -> {
                setLoading(false);
                setError("Failed to load profile: " + exception.getMessage());
              });
    }
  }

  public void updateProfile(String displayName, UserStatus status) {
    User current = currentProfile.getValue();
    Uri imageUri = profileImageUri.getValue();
    String currentUserId = getCurrentUserId();

    if (current == null || currentUserId == null) {
      setError("Unable to update profile");
      return;
    }

    if (displayName == null || displayName.trim().isEmpty()) {
      setError("Display name cannot be empty");
      return;
    }
    if (imageUri != null)
      uploadProfileImage()
          .addOnSuccessListener(
              v -> {
                update(currentUserId, current, displayName, status);
              });
    else update(currentUserId, current, displayName, status);
  }

  private void update(String currentUserId, User current, String displayName, UserStatus status) {
    String profileImageUrl =
        this.profileImageUrl.getValue() == null
            ? current.getProfileImageUrl()
            : this.profileImageUrl.getValue();
    User updatedProfile =
        createUser(currentUserId, current, displayName.trim(), profileImageUrl, status);

    setLoading(true);
    userService
        .updateUserProfile(updatedProfile)
        .addOnSuccessListener(
            aVoid -> {
              setLoading(false);
              currentProfile.setValue(updatedProfile);
              profileUpdated.setValue(true);
            })
        .addOnFailureListener(
            exception -> {
              setLoading(false);
              setError("Failed to update profile: " + exception.getMessage());
            });
  }

  private User createUser(
      String currentUserId,
      User current,
      String displayName,
      String profileImageUrl,
      UserStatus status) {
    return User.builder()
        .userId(currentUserId)
        .email(current.getEmail())
        .displayName(displayName)
        .profileImageUrl(profileImageUrl)
        .isOnline(current.isOnline())
        .lastSeen(current.getLastSeen())
        .status(status)
        .build();
  }

  public Task<String> uploadProfileImage() {
    String currentUserId = getCurrentUserId();
    Uri imageUri = profileImageUri.getValue();

    if (currentUserId == null) {
      setError("User not logged in");
      return null;
    }

    if (imageUri == null) {
      setError("No image selected");
      return null;
    }

    setLoading(true);
    return userService
        .uploadProfileImage(currentUserId, imageUri)
        .addOnSuccessListener(profileImageUrl::setValue)
        .addOnFailureListener(
            exception -> {
              setLoading(false);
              setError("Failed to upload image: " + exception.getMessage());
            });
  }

  private void updateProfileImageUrl(String imageUrl) {
    String currentUserId = getCurrentUserId();

    if (currentUserId != null) {
      userService
          .updateProfileImage(currentUserId, imageUrl)
          .addOnSuccessListener(
              aVoid -> {
                setLoading(false);
                User current = currentProfile.getValue();
                if (current != null) {
                  User updated =
                      User.builder()
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
          .addOnFailureListener(
              exception -> {
                setLoading(false);
                setError("Failed to update profile image: " + exception.getMessage());
              });
    }
  }

  public void refreshProfile() {
    loadCurrentUser();
  }

  private String getCurrentUserId() {
    return authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUid() : null;
  }
}
