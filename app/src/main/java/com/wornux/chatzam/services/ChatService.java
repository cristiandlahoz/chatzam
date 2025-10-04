package com.wornux.chatzam.services;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.wornux.chatzam.data.repositories.ChatRepository;
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
public class ChatService {
    
    private final ChatRepository repository;

    @Inject
    public ChatService(ChatRepository repository) {
        this.repository = repository;
    }
    
    public LiveData<List<Chat>> getChats(String userId) {
        return repository.getChatsByParticipant(userId);
    }
    
    public Task<String> createIndividualChat(List<String> participants, String chatDisplayName) {
        if (participants == null || participants.size() != 2) {
            throw new IllegalArgumentException("Individual chat must have exactly 2 participants");
        }
        
        String chatId = UUID.randomUUID().toString();

        Chat chat = Chat.builder()
                .chatId(chatId)
                .participants(participants)
                .chatType(ChatType.INDIVIDUAL)
                .isGroup(false)
                .groupName(chatDisplayName)
                .unreadCount(0)
                .build();
        
        return repository.createChat(chat);
    }
    
    public Task<Void> updateLastMessage(String chatId, Message message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageId", message.getMessageId());
        updates.put("lastMessageContent", message.getContent());
        updates.put("lastMessageTimestamp", message.getTimestamp());
        updates.put("lastMessageSenderId", message.getSenderId());
        
        return repository.updateLastMessage(chatId, updates);
    }
    
    public Task<Chat> getChatById(String chatId) {
        return repository.getChatById(chatId);
    }
    
    public Task<Void> updateChatInfo(Chat chat) {
        return repository.updateChat(chat);
    }
    
    public Task<Void> deleteChat(String chatId) {
        return repository.deleteDocument(chatId);
    }
    
    public Task<String> createGroupChat(String groupName, List<String> participants, String createdBy) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        
        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("Group must have at least 2 participants");
        }
        
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
        
        return repository.createChat(groupChat);
    }
    
    public Task<Void> addMembersToGroup(String chatId, List<String> newMembers) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            if (chat == null) {
                throw new IllegalArgumentException("Chat not found");
            }
            if (!chat.isGroup()) {
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
            for (String member : newMembers) {
                if (!updatedParticipants.contains(member)) {
                    updatedParticipants.add(member);
                }
            }
            
            return repository.updateParticipants(chatId, updatedParticipants);
        });
    }
    
    public Task<Void> removeMemberFromGroup(String chatId, String memberId) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            if (chat == null) {
                throw new IllegalArgumentException("Chat not found");
            }
            if (!chat.isGroup()) {
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
            updatedParticipants.remove(memberId);
            
            return repository.updateParticipants(chatId, updatedParticipants);
        });
    }
    
    public Task<Void> updateGroupInfo(String chatId, String groupName, String groupImageUrl) {
        return repository.updateGroupInfo(chatId, groupName, groupImageUrl);
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
}
