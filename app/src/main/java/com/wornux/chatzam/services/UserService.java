package com.wornux.chatzam.services;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.data.repositories.StorageRepository;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.data.dto.UserDto;
import com.wornux.chatzam.data.entities.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

  private static final String TAG = "UserService";

  private final UserRepository userRepository;
  private final StorageRepository storageRepository;
  private final ChatRepository chatRepository;

  @Inject
  public UserService(
      UserRepository userRepository,
      StorageRepository storageRepository,
      ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.storageRepository = storageRepository;
    this.chatRepository = chatRepository;
  }

  public Task<Void> updateFmcTokens(String userId, String token) {
    return userRepository
        .updateFmcTOkens(userId, token)
        .continueWithTask(task -> {
          if (task.isSuccessful()) {
            return getUserProfile(userId)
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
    return userRepository
        .updateUser(user)
        .continueWithTask(
            task -> {
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

  private Task<Void> syncParticipantDetailsInChats(User user) {
    if (user == null || user.getUserId() == null) {
      Log.w(TAG, "Cannot sync participant details: user is null");
      return Tasks.forResult(null);
    }

    String userId = user.getUserId();

    UserDto userDTO =
        UserDto.builder()
            .userId(userId)
            .displayName(user.getDisplayName())
            .profileImageUrl(user.getProfileImageUrl())
            .lastSeen(user.getLastSeen())
            .isOnline(user.isOnline())
            .fcmTokens(user.getFcmTokens())
            .build();

    return chatRepository
        .getChatsByParticipantTask(userId)
        .continueWithTask(
            task -> {
              if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                Log.i(
                    TAG,
                    String.format(
                        "canonical-log-line sync_participant_details status=skipped user_id=%s chat_count=0  reason=no_chats",
                        userId));
                return Tasks.forResult(null);
              }

              List<Task<Void>> updateTasks = new ArrayList<>();
              int chatCount = task.getResult().size();

              for (Chat chat : task.getResult()) {
                updateTasks.add(
                    chatRepository.updateSingleParticipantDetail(
                        chat.getChatId(), userId, userDTO));
              }

              return Tasks.whenAllSuccess(updateTasks)
                  .continueWith(
                      completedTask -> {
                        boolean success = completedTask.isSuccessful();
                        String status = success ? "success" : "failed";

                        String errorMsg = "";
                        if (!success && completedTask.getException() != null) {
                          errorMsg = " error=\"" + completedTask.getException().getMessage() + "\"";
                        }

                        Log.i(
                            TAG,
                            String.format(
                                "canonical-log-line sync_participant_details status=%s user_id=%s chat_count=%d%s",
                                status, userId, chatCount, errorMsg));

                        return null;
                      });
            });
  }
}
