package com.wornux.chatzam.services;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.data.repositories.StorageRepository;
import com.wornux.chatzam.data.entities.User;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

  private final UserRepository userRepository;
  private final StorageRepository storageRepository;
  private final AuthenticationManager authManager;

  @Inject
  public UserService(
      UserRepository userRepository,
      StorageRepository storageRepository,
      AuthenticationManager authManager) {
    this.userRepository = userRepository;
    this.storageRepository = storageRepository;
    this.authManager = authManager;
  }

  public Task<DocumentReference> createUserProfile(User user) {
    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
      throw new IllegalArgumentException("User email is required");
    }
    if (user.getDisplayName() == null || user.getDisplayName().trim().isEmpty()) {
      throw new IllegalArgumentException("User display name is required");
    }
    
    return userRepository.createUser(user);
  }

  public Task<User> getUserProfile(String userId) {
    return userRepository.getUserById(userId);
  }

  public Task<Void> updateUserProfile(User user) {
    return userRepository.updateUser(user);
  }

  public Task<List<User>> searchUsers(String query) {
    return userRepository.searchUsers(query);
  }

  public LiveData<List<User>> getFriends(String userId) {
    return userRepository.getFriends(userId);
  }

  public Task<Void> addFriend(String userId, String friendId) {
    if (userId.equals(friendId)) {
      throw new IllegalArgumentException("Cannot add yourself as a friend");
    }
    
    return userRepository.addFriend(userId, friendId);
  }

  public Task<Void> removeFriend(String userId, String friendId) {
    return userRepository.removeFriend(userId, friendId);
  }

  public Task<String> uploadProfileImage(String userId, Uri imageUri) {
    if (imageUri == null) {
      throw new IllegalArgumentException("Image URI cannot be null");
    }
    
    String fileName = "profile_" + userId + "_" + System.currentTimeMillis();
    return storageRepository
        .uploadImage(imageUri, fileName)
        .continueWith(
            task -> {
              Uri downloadUrl = task.getResult();
              return downloadUrl.toString();
            });
  }

  public Task<Void> updateProfileImage(String userId, String imageUrl) {
    return userRepository.updateProfileImage(userId, imageUrl);
  }

  public LiveData<User> getCurrentUserProfile() {
    String currentUserId = getCurrentUserId();
    if (currentUserId != null) {
      return userRepository.getUserRealtime(currentUserId);
    }
    return null;
  }

  private String getCurrentUserId() {
    return authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUid() : null;
  }
}
