package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.services.FirestoreService;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.ChatType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatRepository {
    
    private static final String CHATS_COLLECTION = "chats";
    
    private final FirestoreService firestoreService;
    private final AuthenticationManager authManager;
    
    @Inject
    public ChatRepository(FirestoreService firestoreService,
                             AuthenticationManager authManager) {
        this.firestoreService = firestoreService;
        this.authManager = authManager;
    }
    
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
    
    public Task<Void> updateLastMessage(String chatId, Message message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageId", message.getMessageId());
        updates.put("lastMessageContent", message.getContent());
        updates.put("lastMessageTimestamp", message.getTimestamp());
        updates.put("lastMessageSenderId", message.getSenderId());
        
        return firestoreService.updateDocument(CHATS_COLLECTION, chatId, updates);
    }
    
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
    
    public Task<Void> updateChatInfo(Chat chat) {
        Map<String, Object> chatData = chatToMap(chat);
        return firestoreService.updateDocument(CHATS_COLLECTION, chat.getChatId(), chatData);
    }
    
    public Task<Void> deleteChat(String chatId) {
        return firestoreService.deleteDocument(CHATS_COLLECTION, chatId);
    }
    
    public Task<String> createGroupChat(String groupName, List<String> participants, String createdBy) {
        String chatId = UUID.randomUUID().toString();
        
        Chat groupChat = Chat.builder()
                .chatId(chatId)
                .participants(participants)
                .chatType(ChatType.GROUP)
                .isGroup(true)
                .groupName(groupName)
                .createdBy(createdBy)
                .createdAt(new java.util.Date())
                .unreadCount(0)
                .build();
        
        Map<String, Object> chatData = chatToMap(groupChat);
        
        return firestoreService.addDocument(CHATS_COLLECTION, chatData)
                .continueWith(task -> chatId);
    }
    
    public Task<Void> addMembersToGroup(String chatId, List<String> newMembers) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            if (chat != null && chat.isGroup()) {
                List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
                for (String member : newMembers) {
                    if (!updatedParticipants.contains(member)) {
                        updatedParticipants.add(member);
                    }
                }
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("participants", updatedParticipants);
                
                return firestoreService.updateDocument(CHATS_COLLECTION, chatId, updates);
            }
            throw new IllegalArgumentException("Chat not found or not a group chat");
        });
    }
    
    public Task<Void> removeMemberFromGroup(String chatId, String memberId) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            if (chat != null && chat.isGroup()) {
                List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
                updatedParticipants.remove(memberId);
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("participants", updatedParticipants);
                
                return firestoreService.updateDocument(CHATS_COLLECTION, chatId, updates);
            }
            throw new IllegalArgumentException("Chat not found or not a group chat");
        });
    }
    
    public Task<Void> updateGroupInfo(String chatId, String groupName, String groupImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        if (groupName != null) {
            updates.put("groupName", groupName);
        }
        if (groupImageUrl != null) {
            updates.put("groupImageUrl", groupImageUrl);
        }
        
        return firestoreService.updateDocument(CHATS_COLLECTION, chatId, updates);
    }
    
    public Task<Void> leaveGroup(String chatId, String userId) {
        return removeMemberFromGroup(chatId, userId);
    }
    
    public Task<List<String>> getGroupMembers(String chatId) {
        return getChatById(chatId).continueWith(task -> {
            Chat chat = task.getResult();
            if (chat != null && chat.isGroup()) {
                return chat.getParticipants();
            }
            return new ArrayList<>();
        });
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