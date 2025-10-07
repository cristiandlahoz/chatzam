package com.wornux.chatzam.data.entities;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.wornux.chatzam.data.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.*;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public sealed class Chat permits GroupChat {
    
    @Getter(onMethod_ = {@PropertyName("chat_id")})
    @Setter(onMethod_ = {@PropertyName("chat_id")})
    private String chatId;
    
    @Getter(onMethod_ = {@PropertyName("participants")})
    @Setter(onMethod_ = {@PropertyName("participants")})
    @Builder.Default
    private List<String> participants = new ArrayList<>();
    
    @Getter(onMethod_ = {@PropertyName("participant_details")})
    @Setter(onMethod_ = {@PropertyName("participant_details")})
    private Map<String, UserDTO> participantDetails;
    
    @Getter(onMethod_ = {@PropertyName("chat_type")})
    @Setter(onMethod_ = {@PropertyName("chat_type")})
    private ChatType chatType;
    
    @Getter(onMethod_ = {@PropertyName("last_message")})
    @Setter(onMethod_ = {@PropertyName("last_message")})
    private Message lastMessage;
    
    @Getter(onMethod_ = {@PropertyName("last_message_timestamp")})
    @Setter(onMethod_ = {@PropertyName("last_message_timestamp")})
    private Instant lastMessageTimestamp;
    
    @Getter(onMethod_ = {@PropertyName("unread_count")})
    @Setter(onMethod_ = {@PropertyName("unread_count")})
    private int unreadCount;
    
    @Getter(onMethod_ = {@PropertyName("group_name")})
    @Setter(onMethod_ = {@PropertyName("group_name")})
    private String groupName;
    
    @Getter(onMethod_ = {@PropertyName("group_image_url")})
    @Setter(onMethod_ = {@PropertyName("group_image_url")})
    private String groupImageUrl;
    
    @Getter(onMethod_ = {@PropertyName("created_by")})
    @Setter(onMethod_ = {@PropertyName("created_by")})
    private String createdBy;
    
    @Getter(onMethod_ = {@PropertyName("created_at")})
    @Setter(onMethod_ = {@PropertyName("created_at")})
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Exclude
    public String getLastMessageContent() {
        return lastMessage != null ? lastMessage.getContent() : null;
    }
    
    @Exclude
    public boolean isGroup() {
        return chatType == ChatType.GROUP;
    }
    
    @Exclude
    public String getDisplayName(String currentUserId) {
        if (chatType == ChatType.GROUP) {
            return groupName != null && !groupName.isEmpty() ? groupName : "Group Chat";
        }
        
        String otherUserId = getOtherParticipant(currentUserId);
        if (otherUserId != null && participantDetails != null) {
            UserDTO otherUser = participantDetails.get(otherUserId);
            if (otherUser != null && otherUser.getDisplayName() != null) {
                return otherUser.getDisplayName();
            }
        }
        return "Direct Chat";
    }
    
    @Exclude
    public String getOtherParticipant(String currentUserId) {
        if (chatType == ChatType.INDIVIDUAL && participants != null) {
            return participants.stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
