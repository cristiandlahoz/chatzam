package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;
import com.wornux.chatzam.services.MessageService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

@HiltViewModel
public class ChatViewModel extends BaseViewModel {

  private final MessageService messageService;
  private final AuthenticationManager authManager;
  private final MutableLiveData<String> currentChatId = new MutableLiveData<>();
  @Getter private final LiveData<List<Message>> messages;

  @Inject
  public ChatViewModel(MessageService messageService, AuthenticationManager authManager) {
    this.messageService = messageService;
    this.authManager = authManager;

    LiveData<List<Message>> emptyMessages = new MutableLiveData<>();
    this.messages =
        Transformations.switchMap(
            currentChatId,
            chatId -> (chatId != null) ? messageService.getMessages(chatId) : emptyMessages);
  }

  public void setChatId(String chatId) {
    currentChatId.setValue(chatId);
  }

  public LiveData<Boolean> isEmpty() {
    return Transformations.map(
        messages, messageList -> messageList == null || messageList.isEmpty());
  }

  public void sendMessage(String content) {
    String currentUserId = getCurrentUserId();
    String chatId = currentChatId.getValue();

    if (currentUserId == null) {
      setError("User not logged in");
      return;
    }

    if (chatId == null) {
      setError("No chat selected");
      return;
    }

    if (content == null || content.trim().isEmpty()) {
      setError("Message cannot be empty");
      return;
    }

    Message message =
        Message.builder()
            .messageId(UUID.randomUUID().toString())
            .senderId(currentUserId)
            .chatId(chatId)
            .content(content.trim())
            .messageType(MessageType.TEXT)
            .timestamp(new Date())
            .isDelivered(false)
            .isRead(false)
            .build();

    messageService
        .sendMessage(message)
        .addOnSuccessListener(
            documentReference -> {
                setLoading(false);})
        .addOnFailureListener(
            exception -> setError("Failed to send message: " + exception.getMessage()));
  }

  public void markMessageAsRead(String messageId) {
    messageService
        .markMessageAsRead(messageId)
        .addOnFailureListener(
            exception -> setError("Failed to mark message as read: " + exception.getMessage()));
  }

  public String getCurrentUserId() {
    return authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUid() : null;
  }

  public boolean isMessageFromCurrentUser(Message message) {
    String currentUserId = getCurrentUserId();
    return currentUserId != null && currentUserId.equals(message.getSenderId());
  }
}
