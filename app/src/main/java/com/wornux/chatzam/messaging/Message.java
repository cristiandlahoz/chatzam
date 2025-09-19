package com.wornux.chatzam.messaging;

import com.wornux.chatzam.enums.MessageType;

import java.util.Date;

public class Message {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String chatId;
    private String content;
    private String encryptedContent;
    private MessageType messageType;
    private Date timestamp;
    private boolean isDelivered;
    private boolean isRead;
    private String mediaUrl;
}
