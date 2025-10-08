package com.wornux.chatzam.services;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.dto.UserDto;
import com.wornux.chatzam.data.enums.ChatType;

import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Inject
    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public LiveData<List<Chat>> getChats(String userId) {
        return chatRepository.getChatsByParticipant(userId);
    }

    public Task<String> createIndividualChat(Set<String> participants) {
        if (participants == null || participants.size() != 2) {
            throw new IllegalArgumentException("Individual chat must have exactly 2 participants");
        }

        String chatId = UUID.randomUUID().toString();
        List<String> participantList = new ArrayList<>(participants);

        return createParticipantDetailsMap(participantList)
                .continueWithTask(task -> {
                    Chat chat = Chat.builder()
                            .chatId(chatId)
                            .participants(participantList)
                            .chatType(ChatType.INDIVIDUAL)
                            .participantDetails(task.getResult())
                            .unreadCount(0)
                            .build();

                    return chatRepository.createChat(chat);
                });
    }

    public Task<Void> updateLastMessage(String chatId, Message message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("last_message", message);
        updates.put("last_message_timestamp", message.getTimestamp());

        return chatRepository.updateLastMessage(chatId, updates);
    }

    public Task<Chat> getChatById(String chatId) {
        return chatRepository.getChatById(chatId);
    }

    public Task<Void> updateChatInfo(Chat chat) {
        return chatRepository.updateChat(chat);
    }

    public Task<Void> deleteChat(String chatId) {
        return chatRepository.deleteDocument(chatId);
    }

    public Task<String> createGroupChat(String groupName, Set<String> participants, String createdBy) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("Group must have at least 2 participants");
        }

        String chatId = UUID.randomUUID().toString();
        List<String> participantList = new ArrayList<>(participants);

        return createParticipantDetailsMap(participantList)
                .continueWithTask(task -> {
                    Chat groupChat = Chat.builder()
                            .chatId(chatId)
                            .participants(participantList)
                            .chatType(ChatType.GROUP)
                            .groupName(groupName)
                            .createdBy(createdBy)
                            .participantDetails(task.getResult())
                            .createdAt(Timestamp.now())
                            .unreadCount(0)
                            .build();

                    return chatRepository.createChat(groupChat).continueWith(t -> chatId);
                });
    }

    public Task<Void> addMembersToGroup(String chatId, List<String> newMembers) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            validChatGroup(chat);

            List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
            for (String member : newMembers) {
                if (!updatedParticipants.contains(member)) {
                    updatedParticipants.add(member);
                }
            }

            return chatRepository.updateParticipants(chatId, updatedParticipants);
        });
    }

    public Task<Void> removeMemberFromGroup(String chatId, String memberId) {
        return getChatById(chatId).continueWithTask(task -> {
            Chat chat = task.getResult();
            validChatGroup(chat);

            List<String> updatedParticipants = new ArrayList<>(chat.getParticipants());
            updatedParticipants.remove(memberId);

            return chatRepository.updateParticipants(chatId, updatedParticipants);
        });
    }

    private void validChatGroup(Chat chat) {
        if (chat == null) {
            throw new IllegalArgumentException("Chat not found");
        }
        if (chat.getChatType() != ChatType.GROUP) {
            throw new IllegalArgumentException("Chat is not a group");
        }
    }

    public Task<Void> updateGroupInfo(String chatId, String groupName, String groupImageUrl) {
        return chatRepository.updateGroupInfo(chatId, groupName, groupImageUrl);
    }

    public Task<Void> leaveGroup(String chatId, String userId) {
        return removeMemberFromGroup(chatId, userId);
    }

    private Task<Map<String, UserDto>> createParticipantDetailsMap(List<String> participantIds) {
        return userRepository.getUserDTOsByIds(participantIds)
                .continueWith(task -> {
                    Map<String, UserDto> participantDetails = new HashMap<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (UserDto user : task.getResult()) {
                            participantDetails.put(user.getUserId(), user);
                        }
                    }
                    return participantDetails;
                });
    }
}
