package com.wornux.chatzam.data.entities;

import com.wornux.chatzam.data.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private String chatId;
    @Builder.Default
    private Set<String> participants = new HashSet<>();
    private ChatType chatType;
    private Message lastMessage;
    private Date lastMessageTimestamp;
    private int unreadCount;
    private String groupName;
    private String groupImageUrl;
    private String createdBy;
    @Builder.Default
    private Date createdAt = new Date();

    public boolean containParticipant(String userId){
        return participants.contains(userId);
    }
    public void addParticipant(String userId) {
        participants.add(userId);
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public void setLastMessage(Message message) {
        this.lastMessage = message;
        this.lastMessageTimestamp = message.getTimestamp();
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    public String getLastMessageContent() {
        return lastMessage != null ? lastMessage.getContent() : null;
    }
}
