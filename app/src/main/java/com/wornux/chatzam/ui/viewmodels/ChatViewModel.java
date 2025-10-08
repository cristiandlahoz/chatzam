package com.wornux.chatzam.ui.viewmodels;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.services.ChatService;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;
import com.wornux.chatzam.services.MessageService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ChatViewModel extends BaseViewModel {

  private final MessageService messageService;
  private final AuthenticationManager authManager;
  private final MutableLiveData<String> currentChatId = new MutableLiveData<>();
  private final MediatorLiveData<List<Message>> messagesMediator = new MediatorLiveData<>();

  @Inject
  public ChatViewModel(MessageService messageService, AuthenticationManager authManager) {
    this.messageService = messageService;
    this.authManager = authManager;

    LiveData<List<Message>> emptyMessages = new MutableLiveData<>(new ArrayList<>());
    LiveData<List<Message>> firestoreMessages =
        Transformations.switchMap(
            currentChatId,
            chatId -> (chatId != null) ? messageService.getMessages(chatId) : emptyMessages);

    messagesMediator.addSource(firestoreMessages, messagesMediator::setValue);
  }

  public LiveData<List<Message>> getMessages() {
    return messagesMediator;
  }

  public void setChatId(String chatId) {
    currentChatId.setValue(chatId);
  }

  public String getCurrentUserId() {
    return authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUid() : null;
  }

  public void sendMessage(String content) {
    SendContext context = validateSendContext();
    if (context == null) return;

    if (content == null || content.trim().isEmpty()) {
      setError("Message cannot be empty");
      return;
    }

    Message message =
        createBaseMessage(context).content(content.trim()).messageType(MessageType.TEXT).build();

    sendMessage(
        message,
        messageId -> {},
        exception -> setError("Failed to send message: " + exception.getMessage()));
  }

  public void sendImageMessage(Uri imageUri) {
    SendContext context = validateSendContext();
    if (context == null) return;

    if (imageUri == null) {
      setError("Invalid image");
      return;
    }

    setLoading(true);

    messageService
        .uploadMedia(imageUri, MessageType.IMAGE)
        .addOnSuccessListener(
            downloadUrl -> {
              Message message =
                  createBaseMessage(context)
                      .content("")
                      .mediaUrl(downloadUrl)
                      .messageType(MessageType.IMAGE)
                      .build();

              sendMessage(
                  message,
                  messageId -> setLoading(false),
                  exception -> {
                    setError("Failed to send image: " + exception.getMessage());
                    setLoading(false);
                  });
            })
        .addOnFailureListener(
            exception -> {
              setError("Failed to upload image: " + exception.getMessage());
              setLoading(false);
            });
  }

  private SendContext validateSendContext() {
    String currentUserId = getCurrentUserId();
    String chatId = currentChatId.getValue();

    if (currentUserId == null) {
      setError("User not logged in");
      return null;
    }

    if (chatId == null) {
      setError("No chat selected");
      return null;
    }

    return new SendContext(currentUserId, chatId);
  }

  private Message.MessageBuilder createBaseMessage(SendContext context) {
    return Message.builder()
        .messageId("temp_" + System.currentTimeMillis())
        .senderId(context.userId)
        .chatId(context.chatId)
        .timestamp(Timestamp.now());
  }

  private void sendMessage(
      Message message, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {

    List<Message> currentMessages = messagesMediator.getValue();
    if (currentMessages != null) {
      List<Message> updatedMessages = new ArrayList<>(currentMessages);
      updatedMessages.add(message);
      messagesMediator.setValue(updatedMessages);
    }

    messageService
        .sendMessage(message)
        .addOnSuccessListener(onSuccess)
        .addOnFailureListener(
            exception -> {
              if (currentMessages != null) {
                messagesMediator.setValue(currentMessages);
              }
              onFailure.onFailure(exception);
            });
  }

  private record SendContext(String userId, String chatId) {}
}
