package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository extends BaseRepository<User> {

    private static final String FRIENDS_COLLECTION = "friends";

    @Inject
    public UserRepository(FirebaseManager firebaseManager) {
        super(firebaseManager.getFirestore(), User.class);
    }

    public Task<Void> createUser(User user) {
        Map<String, Object> userData = userToMap(user);
        return db.collection(collectionName).document(user.getUserId()).set(userData);
    }

    public Task<User> getUserById(String userId) {
        return getDocument(userId)
                .continueWith(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        return documentToUser(document);
                    }
                    return null;
                });
    }

    public Task<Void> updateUser(User user) {
        Map<String, Object> userData = userToMap(user);
        return updateDocument(user.getUserId(), userData);
    }

    public Task<List<User>> searchUsers(String query) {
        return getCollection()
                .continueWith(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<User> users = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        User user = documentToUser(document);
                        if (query.isEmpty() ||
                                user.getDisplayName().toLowerCase().contains(query.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            users.add(user);
                        }
                    }
                    return users;
                });
    }

    public LiveData<User> getUserRealtime(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        db.collection(collectionName)
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error == null && documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentToUser(documentSnapshot);
                        userLiveData.setValue(user);
                    }
                });

        return userLiveData;
    }

    public Task<Void> updateProfileImage(String userId, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl);
        return updateDocument(userId, updates);
    }

    public LiveData<List<User>> getFriends(String userId) {
        MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();

        db.collection(FRIENDS_COLLECTION)
                .document(userId)
                .collection("friends")
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        List<User> friends = new ArrayList<>();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String friendId = doc.getId();
                            getUserById(friendId)
                                    .addOnSuccessListener(friendUser -> {
                                        if (friendUser != null) {
                                            friends.add(friendUser);
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

        return db.collection(FRIENDS_COLLECTION)
                .document(userId)
                .collection("friends")
                .add(friendData)
                .continueWith(task -> null);
    }

    public Task<Void> removeFriend(String userId, String friendId) {
        return db.collection(FRIENDS_COLLECTION)
                .document(userId)
                .collection("friends")
                .document(friendId)
                .delete();
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("profileImageUrl", user.getProfileImageUrl());
        data.put("isOnline", user.isOnline());
        data.put("lastSeen", user.getLastSeen());
        data.put("status", user.getStatus() != null ? user.getStatus().name() : null);
        return data;
    }

    private User documentToUser(DocumentSnapshot document) {
        return User.builder()
                .userId(document.getString("userId"))
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
