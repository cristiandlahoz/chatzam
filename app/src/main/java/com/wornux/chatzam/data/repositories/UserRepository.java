package com.wornux.chatzam.data.repositories;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.services.FirebaseStorageService;
import com.wornux.chatzam.services.FirestoreService;
import com.wornux.chatzam.data.entities.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {

  private static final String USERS_COLLECTION = "users";
  private static final String FRIENDS_COLLECTION = "friends";

  private final FirestoreService firestoreService;
  private final FirebaseStorageService storageService;
  private final AuthenticationManager authManager;

  @Inject
  public UserRepository(
      FirestoreService firestoreService,
      FirebaseStorageService storageService,
      AuthenticationManager authManager) {
    this.firestoreService = firestoreService;
    this.storageService = storageService;
    this.authManager = authManager;
  }

  public Task<DocumentReference> createUserProfile(UserProfile userProfile) {
    Map<String, Object> userData = userProfileToMap(userProfile);
    return firestoreService.addDocument(USERS_COLLECTION, userData);
  }

  public Task<UserProfile> getUserProfile(String userId) {
    return firestoreService
        .getDocument(USERS_COLLECTION, userId)
        .continueWith(
            task -> {
              DocumentSnapshot document = task.getResult();
              if (document.exists()) {
                return documentToUserProfile(document);
              }
              return null;
            });
  }

  public Task<Void> updateUserProfile(UserProfile userProfile) {
    Map<String, Object> userData = userProfileToMap(userProfile);
    return firestoreService.updateDocument(USERS_COLLECTION, userProfile.getUserId(), userData);
  }

  public Task<List<UserProfile>> searchUsers(String query) {
    return firestoreService
        .getCollection(USERS_COLLECTION)
        .continueWith(
            task -> {
              QuerySnapshot querySnapshot = task.getResult();
              List<UserProfile> users = new ArrayList<>();

              for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                UserProfile user = documentToUserProfile(document);
                if (user.getDisplayName().toLowerCase().contains(query.toLowerCase())
                    || user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                  users.add(user);
                }
              }
              return users;
            });
  }

  public LiveData<List<UserProfile>> getFriends(String userId) {
    MutableLiveData<List<UserProfile>> friendsLiveData = new MutableLiveData<>();

    firestoreService.addSnapshotListener(
        FRIENDS_COLLECTION + "/" + userId + "/friends",
        (value, error) -> {
          if (error == null && value != null) {
            List<UserProfile> friends = new ArrayList<>();

            for (DocumentSnapshot doc : value.getDocuments()) {
              String friendId = doc.getId();
              getUserProfile(friendId)
                  .addOnSuccessListener(
                      friendProfile -> {
                        if (friendProfile != null) {
                          friends.add(friendProfile);
                          friendsLiveData.setValue(friends);
                        }
                      });
            }
          }
        });

    return friendsLiveData;
  }

  public Task<Void> addFriend(String userId, String friendId) {
    Map<String, Object> friendData = new HashMap<>();
    friendData.put("friendId", friendId);
    friendData.put("timestamp", System.currentTimeMillis());

    return firestoreService
        .addDocument(FRIENDS_COLLECTION + "/" + userId + "/friends", friendData)
        .continueWith(task -> null);
  }

  public Task<Void> removeFriend(String userId, String friendId) {
    return firestoreService.deleteDocument(
        FRIENDS_COLLECTION + "/" + userId + "/friends", friendId);
  }

  public Task<String> uploadProfileImage(String userId, Uri imageUri) {
    String fileName = "profile_" + userId + "_" + System.currentTimeMillis();
    return storageService
        .uploadImage(imageUri, fileName)
        .continueWith(
            task -> {
              Uri downloadUrl = task.getResult();
              return downloadUrl.toString();
            });
  }

  public Task<Void> updateProfileImage(String userId, String imageUrl) {
    Map<String, Object> updates = new HashMap<>();
    updates.put("profileImageUrl", imageUrl);
    return firestoreService.updateDocument(USERS_COLLECTION, userId, updates);
  }

  public LiveData<UserProfile> getCurrentUserProfile() {
    MutableLiveData<UserProfile> profileLiveData = new MutableLiveData<>();

    String currentUserId = getCurrentUserId();
    if (currentUserId != null) {
      firestoreService
          .getFirestore()
          .collection(USERS_COLLECTION)
          .document(currentUserId)
          .addSnapshotListener(
              (documentSnapshot, error) -> {
                if (error == null && documentSnapshot != null && documentSnapshot.exists()) {
                  UserProfile profile = documentToUserProfile(documentSnapshot);
                  profileLiveData.setValue(profile);
                }
              });
    }

    return profileLiveData;
  }

  private String getCurrentUserId() {
    return authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUid() : null;
  }

  private Map<String, Object> userProfileToMap(UserProfile userProfile) {
    Map<String, Object> data = new HashMap<>();
    data.put("userId", userProfile.getUserId());
    data.put("email", userProfile.getEmail());
    data.put("displayName", userProfile.getDisplayName());
    data.put("profileImageUrl", userProfile.getProfileImageUrl());
    data.put("isOnline", userProfile.isOnline());
    data.put("lastSeen", userProfile.getLastSeen());
    data.put("status", userProfile.getStatus() != null ? userProfile.getStatus().name() : null);
    return data;
  }

  private UserProfile documentToUserProfile(DocumentSnapshot document) {
    return UserProfile.builder()
        .userId(document.getId())
        .email(document.getString("email"))
        .displayName(document.getString("displayName"))
        .profileImageUrl(document.getString("profileImageUrl"))
        .isOnline(
            document.getBoolean("isOnline") != null
                && Boolean.TRUE.equals(document.getBoolean("isOnline")))
        .lastSeen(document.getDate("lastSeen"))
        .build();
  }
}
