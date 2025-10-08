package com.wornux.chatzam.data.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.wornux.chatzam.data.dto.UserDto;
import com.wornux.chatzam.data.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
  private Map<String, UserDto> participantDetails;

  @Getter(onMethod_ = {@PropertyName("chat_type")})
  @Setter(onMethod_ = {@PropertyName("chat_type")})
  private ChatType chatType;

  @Getter(onMethod_ = {@PropertyName("last_message")})
  @Setter(onMethod_ = {@PropertyName("last_message")})
  private Message lastMessage;

  @Getter(onMethod_ = {@PropertyName("last_message_timestamp")})
  @Setter(onMethod_ = {@PropertyName("last_message_timestamp")})
  private Timestamp lastMessageTimestamp;

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
  private Timestamp createdAt = Timestamp.now();

  @Getter(onMethod_ = {@PropertyName("encryption_key")})
  @Setter(onMethod_ = {@PropertyName("encryption_key")})
  private String encryptionKey;

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
      UserDto otherUser = participantDetails.get(otherUserId);
      if (otherUser != null && otherUser.getDisplayName() != null) {
        return otherUser.getDisplayName();
      }
    }
    return "Direct Chat";
  }

  @Exclude
  public String getOtherParticipant(String currentUserId) {
    if (chatType == ChatType.INDIVIDUAL && participants != null) {
      return participants.stream().filter(id -> !id.equals(currentUserId)).findFirst().orElse(null);
    }
    return null;
  }

  @Exclude
  public boolean equals(Chat other) {
    if (!Objects.equals(this.getChatType(), other.getChatType())) {
      return false;
    }

    if (!Objects.equals(this.getLastMessageContent(), other.getLastMessageContent())
        || !Objects.equals(this.getLastMessageTimestamp(), other.getLastMessageTimestamp())
        || this.getUnreadCount() != other.getUnreadCount()) {
      return false;
    }

    if (this.isGroup()) {
      return Objects.equals(this.getGroupName(), other.getGroupName())
          && Objects.equals(this.getGroupImageUrl(), other.getGroupImageUrl());
    } else {
      return compareParticipantDetails(this.getParticipantDetails(), other.getParticipantDetails());
    }
  }

  @Exclude
  private boolean compareParticipantDetails(
      Map<String, UserDto> oldMap, Map<String, UserDto> newMap) {
    if (oldMap == null && newMap == null) return true;
    if (oldMap == null || newMap == null) return false;
    if (oldMap.size() != newMap.size()) return false;

    for (String userId : oldMap.keySet()) {
      UserDto oldUser = oldMap.get(userId);
      UserDto newUser = newMap.get(userId);

      if (newUser == null) return false;

      if (!Objects.equals(oldUser.getDisplayName(), newUser.getDisplayName())
          || !Objects.equals(oldUser.getProfileImageUrl(), newUser.getProfileImageUrl())) {
        return false;
      }
    }

    return true;
  }
}
