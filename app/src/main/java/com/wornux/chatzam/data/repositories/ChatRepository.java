package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.enums.ChatType;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatRepository extends BaseRepository<Chat> {
    
    private final MutableLiveData<List<Chat>> chatsLiveData = new MutableLiveData<>();
    private List<Chat> currentChats = new ArrayList<>();
    @Inject
    public ChatRepository(FirebaseManager firebaseManager) {
        super(firebaseManager.getFirestore(), Chat.class);
    }
    
    public LiveData<List<Chat>> getChatsByParticipant(String userId) {
        firestore.collection(collectionName)
                .whereArrayContains("participants", userId)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        List<Chat> chats = new ArrayList<>();
                        
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Chat chat = documentToChat(document);
                            chats.add(chat);
                        }

                        currentChats = chats;
                        chatsLiveData.setValue(chats);
                    }
                });
        
        return chatsLiveData;
    }
    
    public Task<String> createChat(Chat chat) {
        return firestore.collection(collectionName)
                .document(chat.getChatId())
                .set(chat)
                .continueWith(task -> chat.getChatId());
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
    
     public Task<Boolean> checkIfIndividualChatExists(String canonicalChatId){
        return firestore.collection(collectionName).document(canonicalChatId)
                .get()
                .continueWith(task -> {
                    if(!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return task.getResult().exists();
                });
    }

    public boolean doesChatExistLocally(String canonicalChatId) {
        if (currentChats == null || canonicalChatId == null) {
            return false;
        }
        for (Chat chat : currentChats) {
            if (canonicalChatId.equals(chat.getChatId())) return true;
        }
        return false;
    }

    private Map<String, Object> chatToMap(Chat chat) {
        Map<String, Object> data = new HashMap<>();
        data.put("chatId", chat.getChatId());
        data.put("participants", chat.getParticipants());
        data.put("chatType", chat.getChatType() != null ? chat.getChatType().name() : null);
        data.put("lastMessageTimestamp", chat.getLastMessageTimestamp());
        data.put("unreadCount", chat.getUnreadCount());
        data.put("isGroup", chat.isGroup());
        data.put("groupName", chat.getGroupName());
        data.put("groupImageUrl", chat.getGroupImageUrl());
        data.put("createdBy", chat.getCreatedBy());
        data.put("createdAt", chat.getCreatedAt());
        return data;
    }
    
    private Chat documentToChat(DocumentSnapshot document) {
        List<String> participants = (List<String>) document.get("participants");
        
        return Chat.builder()
                .chatId(document.getId())
                .participants(participants != null ? participants : new ArrayList<>())
                .chatType(ChatType.valueOf(document.getString("chatType")))
                .lastMessageTimestamp(document.getDate("lastMessageTimestamp"))
                .unreadCount(document.getLong("unreadCount") != null ? document.getLong("unreadCount").intValue() : 0)
                .isGroup(document.getBoolean("isGroup") != null ? document.getBoolean("isGroup") : false)
                .groupName(document.getString("groupName"))
                .groupImageUrl(document.getString("groupImageUrl"))
                .createdBy(document.getString("createdBy"))
                .createdAt(document.getDate("createdAt"))
                .build();
    }
}
