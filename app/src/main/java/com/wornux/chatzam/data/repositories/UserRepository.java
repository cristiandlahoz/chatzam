package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.entities.UserDTO;
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

    @Inject
    public UserRepository(FirebaseManager firebaseManager) {
        super(firebaseManager.getFirestore(), User.class);
    }

    public Task<Void> createUser(User user) {
        return db.collection(collectionName)
                .document(user.getUserId())
                .set(user);
    }

    public Task<User> getUserById(String userId) {
        return getDocument(userId)
                .continueWith(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        return document.toObject(User.class);
                    }
                    return null;
                });
    }

    public Task<Void> updateUser(User user) {
        return db.collection(collectionName)
                .document(user.getUserId())
                .set(user);
    }

    public Task<List<User>> searchUsers(String query) {
        return getCollection()
                .continueWith(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<User> users = querySnapshot.toObjects(User.class);

                    if (query.isEmpty()) {
                        return users;
                    }

                    List<User> filteredUsers = new ArrayList<>();
                    for (User user : users) {
                        if (user.getDisplayName().toLowerCase().contains(query.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            filteredUsers.add(user);
                        }
                    }
                    return filteredUsers;
                });
    }

    public LiveData<User> getUserRealtime(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        db.collection(collectionName)
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error == null && documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                    }
                });

        return userLiveData;
    }

    public Task<List<UserDTO>> getUserDTOsByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<String> limitedUserIds = userIds.size() > 10 ? userIds.subList(0, 10) : userIds;

        return db.collection(collectionName)
                .whereIn(FieldPath.documentId(), limitedUserIds)
                .get()
                .continueWith(task -> {
                    List<UserDTO> userDTOs = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                userDTOs.add(UserDTO.builder()
                                        .userId(user.getUserId())
                                        .displayName(user.getDisplayName())
                                        .profileImageUrl(user.getProfileImageUrl())
                                        .lastSeen(user.getLastSeen())
                                        .isOnline(user.isOnline())
                                        .build());
                            }
                        }
                    }
                    return userDTOs;
                });
    }

    public Task<Void> updateProfileImage(String userId, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profile_image_url", imageUrl);
        return updateDocument(userId, updates);
    }
}
