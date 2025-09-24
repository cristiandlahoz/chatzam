package com.wornux.chatzam.domain.repositories;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.wornux.chatzam.domain.entities.UserProfile;

import java.util.List;

public interface UserRepository {
    Task<Void> createUserProfile(UserProfile userProfile);
    Task<UserProfile> getUserProfile(String userId);
    Task<Void> updateUserProfile(UserProfile userProfile);
    Task<List<UserProfile>> searchUsers(String query);
    LiveData<List<UserProfile>> getFriends(String userId);
    Task<Void> addFriend(String userId, String friendId);
    Task<Void> removeFriend(String userId, String friendId);
    
    Task<String> uploadProfileImage(String userId, Uri imageUri);
    Task<Void> updateProfileImage(String userId, String imageUrl);
    LiveData<UserProfile> getCurrentUserProfile();
}