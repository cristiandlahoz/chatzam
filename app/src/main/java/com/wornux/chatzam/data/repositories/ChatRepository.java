package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.enums.ChatType;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;

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
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        List<Chat> chats = new ArrayList<>();
                        
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Chat chat = documentToChat(document);
                            chats.add(chat);
                        }
                        
                        chatsLiveData.setValue(chats);
                    }
                });
        
        return chatsLiveData;
    }
    
    public Task<String> createChat(Chat chat) {
        Map<String, Object> chatData = chatToMap(chat);
        return addDocument(chatData).continueWith(task -> chat.getChatId());
    }
    
    public Task<Chat> getChatById(String chatId) {
        return getDocument(chatId)
                .continueWith(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        return documentToChat(document);
                    }
                    return null;
                });
    }
    
    public Task<Void> updateChat(Chat chat) {
        Map<String, Object> chatData = chatToMap(chat);
        return updateDocument(chat.getChatId(), chatData);
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
            updates.put("groupName", groupName);
        }
        if (groupImageUrl != null) {
            updates.put("groupImageUrl", groupImageUrl);
        }
        return updateDocument(chatId, updates);
    }
    
    private Map<String, Object> chatToMap(Chat chat) {
        Map<String, Object> data = new HashMap<>();
        data.put("chatId", chat.getChatId());
        data.put("participants", chat.getParticipants());
        data.put("chatType", chat.getChatType() != null ? chat.getChatType().name() : null);
        data.put("lastMessageTimestamp", chat.getLastMessageTimestamp());
        data.put("unreadCount", chat.getUnreadCount());
        data.put("groupName", chat.getGroupName());
        data.put("groupImageUrl", chat.getGroupImageUrl());
        data.put("createdBy", chat.getCreatedBy());
        data.put("createdAt", chat.getCreatedAt());
        return data;
    }
    
    private Chat documentToChat(DocumentSnapshot document) {
        Set<String> participants = new HashSet<>();
        if (document.get("participants") instanceof List values)
            for (Object value : values) {
                participants.add(value.toString());
            }


        return Chat.builder()
                .chatId(document.getId())
                .participants(participants)
                .chatType(ChatType.valueOf(document.getString("chatType")))
                .lastMessageTimestamp(document.getDate("lastMessageTimestamp"))
                .unreadCount(document.getLong("unreadCount") != null ? document.getLong("unreadCount").intValue() : 0)
                .groupName(document.getString("groupName"))
                .groupImageUrl(document.getString("groupImageUrl"))
                .createdBy(document.getString("createdBy"))
                .createdAt(document.getDate("createdAt"))
                .build();
    }
}
