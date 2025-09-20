package com.wornux.chatzam.domain.entities;

import com.wornux.chatzam.domain.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private String chatId;
    @Builder.Default
    private List<String> participants = new ArrayList<>();
    private ChatType chatType;
    private Message lastMessage;
    private Date lastMessageTimestamp;
    private int unreadCount;
    private boolean isGroup;
    private String groupName;
    private String groupImageUrl;
    private String createdBy;
    @Builder.Default
    private Date createdAt = new Date();

    public void addParticipant(String userId) {
        if (!participants.contains(userId)) {
            participants.add(userId);
        }
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
}
