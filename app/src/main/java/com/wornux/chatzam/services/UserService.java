package com.wornux.chatzam.services;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.data.repositories.StorageRepository;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.entities.UserDTO;
import com.wornux.chatzam.data.entities.Chat;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final ChatRepository chatRepository;
    private final AuthenticationManager authManager;

    @Inject
    public UserService(
            UserRepository userRepository,
            StorageRepository storageRepository,
            ChatRepository chatRepository,
            AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
        this.chatRepository = chatRepository;
        this.authManager = authManager;
    }

    public Task<Void> createUserProfile(User user) {
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
        return userRepository.updateUser(user)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return syncParticipantDetailsInChats(user);
                    }
                    return task;
                });
    }

    public Task<List<User>> searchUsers(String query) {
        return userRepository.searchUsers(query);
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
        return userRepository.updateProfileImage(userId, imageUrl)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return userRepository.getUserById(userId)
                                .continueWithTask(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                                        return syncParticipantDetailsInChats(userTask.getResult());
                                    }
                                    return Tasks.forResult(null);
                                });
                    }
                    return task;
                });
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

    private Task<Void> syncParticipantDetailsInChats(User user) {
        String userId = user.getUserId();

        UserDTO userDTO = new UserDTO(
                userId,
                user.getDisplayName(),
                user.getProfileImageUrl(),
                user.getLastSeen(),
                user.isOnline()
        );

        return chatRepository.getChatsByParticipantTask(userId)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return Tasks.forResult(null);
                    }

                    List<Chat> chats = task.getResult();
                    if (chats.isEmpty()) {
                        return Tasks.forResult(null);
                    }

                    List<Task<Void>> updateTasks = new ArrayList<>();
                    for (Chat chat : chats) {
                        Task<Void> updateTask = chatRepository.updateSingleParticipantDetails(
                                chat.getChatId(),
                                userId,
                                userDTO
                        );
                        updateTasks.add(updateTask);
                    }

                    return Tasks.whenAll(updateTasks);
                });
    }
}
