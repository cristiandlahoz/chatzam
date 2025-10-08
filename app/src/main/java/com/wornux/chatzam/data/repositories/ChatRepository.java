package com.wornux.chatzam.data.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.dto.UserDto;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;
import com.wornux.chatzam.utils.CryptoUtils;

import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatRepository extends BaseRepository<Chat> {

  @Inject
  public ChatRepository(FirebaseManager firebaseManager) {
    super(firebaseManager.getFirestore(), Chat.class);
  }

  public LiveData<List<Chat>> getChatsByParticipant(String userId) {
    MutableLiveData<List<Chat>> chatsLiveData = new MutableLiveData<>();

    db.collection(collectionName)
        .whereArrayContains("participants", userId)
        .orderBy("last_message_timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener(
            (value, error) -> {
              if (error != null) Log.e("ChatRepository", "Error getting chats", error);

              if (error == null && value != null) {
                List<Chat> chats = value.toObjects(Chat.class);
                chatsLiveData.setValue(chats);
              }
            });

    return chatsLiveData;
  }

  public Task<List<Chat>> getChatsByParticipantTask(String userId) {
    return db.collection(collectionName)
        .whereArrayContains("participants", userId)
        .get()
        .continueWith(
            task -> {
              if (task.isSuccessful() && task.getResult() != null) {
                return task.getResult().toObjects(Chat.class);
              }
              return new ArrayList<>();
            });
  }

  public Task<String> createChat(Chat chat) {
    return db.collection(collectionName)
        .document(chat.getChatId())
        .set(chat)
        .continueWith(task -> chat.getChatId());
  }

  public Task<Chat> getChatById(String chatId) {
    return getDocument(chatId)
        .continueWith(
            task -> {
              if (task.getResult().exists()) {
                return task.getResult().toObject(Chat.class);
              }
              return null;
            });
  }

  public Task<Void> updateChat(Chat chat) {
    return db.collection(collectionName).document(chat.getChatId()).set(chat);
  }

  public Task<Void> updateLastMessage(String chatId, Map<String, Object> lastMessageData) {
    return updateDocument(chatId, lastMessageData);
  }

  public Task<Void> updateParticipants(String chatId, List<String> participants) {
    Map<String, Object> updates = new HashMap<>();
    updates.put("participants", participants);
    return updateDocument(chatId, updates);
  }

  public Task<Void> updateGroupInfo(String chatId, String groupName, String groupImageUrl) {
    Map<String, Object> updates = new HashMap<>();
    if (groupName != null) {
      updates.put("group_name", groupName);
    }
    if (groupImageUrl != null) {
      updates.put("group_image_url", groupImageUrl);
    }
    return updateDocument(chatId, updates);
  }

  public Task<Void> updateSingleParticipantDetail(String chatId, String userId, UserDto userDTO) {
    Map<String, Object> updates = new HashMap<>();
    updates.put("participant_details." + userId, userDTO);
    return updateDocument(chatId, updates);
  }

  public Task<String> getOrCreateEncryptionKey(String chatId) {
    return getChatById(chatId)
        .continueWithTask(
            task -> {
              Chat chat = task.getResult();

              if (chat != null
                  && chat.getEncryptionKey() != null
                  && !chat.getEncryptionKey().isEmpty()) {
                return com.google.android.gms.tasks.Tasks.forResult(chat.getEncryptionKey());
              }

              String newKey = CryptoUtils.generateEncryptionKey();
              Map<String, Object> updates = new HashMap<>();
              updates.put("encryption_key", newKey);

              return updateDocument(chatId, updates).continueWith(updateTask -> newKey);
            });
  }

  public Task<String> getEncryptionKey(String chatId) {
    return getChatById(chatId)
        .continueWith(
            task -> {
              Chat chat = task.getResult();
              return (chat != null) ? chat.getEncryptionKey() : null;
            });
  }
}
