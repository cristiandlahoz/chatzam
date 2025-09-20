package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wornux.chatzam.data.services.AuthenticationManager;
import com.wornux.chatzam.data.services.FirestoreService;
import com.wornux.chatzam.domain.entities.Chat;
import com.wornux.chatzam.domain.entities.Message;
import com.wornux.chatzam.domain.enums.ChatType;
import com.wornux.chatzam.domain.repositories.ChatRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatRepositoryImpl implements ChatRepository {
    
    private static final String CHATS_COLLECTION = "chats";
    
    private final FirestoreService firestoreService;
    private final AuthenticationManager authManager;
    
    @Inject
    public ChatRepositoryImpl(FirestoreService firestoreService, 
                             AuthenticationManager authManager) {
        this.firestoreService = firestoreService;
        this.authManager = authManager;
    }
    
    @Override
    public LiveData<List<Chat>> getChats(String userId) {
        MutableLiveData<List<Chat>> chatsLiveData = new MutableLiveData<>();
        
        firestoreService.getFirestore().collection(CHATS_COLLECTION)
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
    
    @Override
    public Task<String> createChat(List<String> participants, boolean isGroup) {
        String chatId = UUID.randomUUID().toString();
        
        Chat chat = Chat.builder()
                .chatId(chatId)
                .participants(participants)
                .chatType(isGroup ? ChatType.GROUP : ChatType.INDIVIDUAL)
                .isGroup(isGroup)
                .unreadCount(0)
                .build();
        
        Map<String, Object> chatData = chatToMap(chat);
        
        return firestoreService.addDocument(CHATS_COLLECTION, chatData)
                .continueWith(task -> chatId);
    }
    
    @Override
    public Task<Void> updateLastMessage(String chatId, Message message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageId", message.getMessageId());
        updates.put("lastMessageContent", message.getContent());
        updates.put("lastMessageTimestamp", message.getTimestamp());
        updates.put("lastMessageSenderId", message.getSenderId());
        
        return firestoreService.updateDocument(CHATS_COLLECTION, chatId, updates);
    }
    
    @Override
    public Task<Chat> getChatById(String chatId) {
        return firestoreService.getDocument(CHATS_COLLECTION, chatId)
                .continueWith(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        return documentToChat(document);
                    }
                    return null;
                });
    }
    
    @Override
    public Task<Void> updateChatInfo(Chat chat) {
        Map<String, Object> chatData = chatToMap(chat);
        return firestoreService.updateDocument(CHATS_COLLECTION, chat.getChatId(), chatData);
    }
    
    @Override
    public Task<Void> deleteChat(String chatId) {
        return firestoreService.deleteDocument(CHATS_COLLECTION, chatId);
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