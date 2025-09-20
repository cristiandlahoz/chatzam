package com.wornux.chatzam.domain.entities;

import com.wornux.chatzam.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String chatId;
    private String content;
    private String encryptedContent;
    private MessageType messageType;
    @Builder.Default
    private Date timestamp = new Date();
    private boolean isDelivered;
    private boolean isRead;
    private String mediaUrl;

    public void markAsDelivered() {
        this.isDelivered = true;
    }

    public void markAsRead() {
        this.isRead = true;
        this.isDelivered = true;
    }

    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.trim().isEmpty();
    }
}
