package com.wornux.chatzam.data.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.wornux.chatzam.data.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Getter(onMethod_ = {@PropertyName("message_id")})
    @Setter(onMethod_ = {@PropertyName("message_id")})
    private String messageId;
    
    @Getter(onMethod_ = {@PropertyName("sender_id")})
    @Setter(onMethod_ = {@PropertyName("sender_id")})
    private String senderId;
    
    @Getter(onMethod_ = {@PropertyName("sender_name")})
    @Setter(onMethod_ = {@PropertyName("sender_name")})
    private String senderName;
    
    @Getter(onMethod_ = {@PropertyName("receiver_id")})
    @Setter(onMethod_ = {@PropertyName("receiver_id")})
    private String receiverId;
    
    @Getter(onMethod_ = {@PropertyName("chat_id")})
    @Setter(onMethod_ = {@PropertyName("chat_id")})
    private String chatId;
    
    @Getter(onMethod_ = {@PropertyName("content")})
    @Setter(onMethod_ = {@PropertyName("content")})
    private String content;
    
    @Getter(onMethod_ = {@PropertyName("encrypted_content")})
    @Setter(onMethod_ = {@PropertyName("encrypted_content")})
    private String encryptedContent;
    
    @Getter(onMethod_ = {@PropertyName("message_type")})
    @Setter(onMethod_ = {@PropertyName("message_type")})
    private MessageType messageType;
    
    @Getter(onMethod_ = {@PropertyName("timestamp")})
    @Setter(onMethod_ = {@PropertyName("timestamp")})
    @Builder.Default
    private Timestamp timestamp = Timestamp.now();
    
    @Getter(onMethod_ = {@PropertyName("read_by")})
    @Setter(onMethod_ = {@PropertyName("read_by")})
    @Builder.Default
    private List<String> readBy = new ArrayList<>();
    
    @Getter(onMethod_ = {@PropertyName("media_url")})
    @Setter(onMethod_ = {@PropertyName("media_url")})
    private String mediaUrl;
    
    @Exclude
    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.trim().isEmpty();
    }
}
