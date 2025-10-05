package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;
import com.wornux.chatzam.services.MessageService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ChatViewModel extends BaseViewModel {

    private final MessageService messageService;
    private final AuthenticationManager authManager;
    private final MutableLiveData<String> currentChatId = new MutableLiveData<>();
    private final MediatorLiveData<List<Message>> messagesMediator = new MediatorLiveData<>();
    @Getter
    private final LiveData<List<Message>> messages = messagesMediator;

    @Inject
    public ChatViewModel(MessageService messageService, AuthenticationManager authManager) {
        this.messageService = messageService;
        this.authManager = authManager;

        LiveData<List<Message>> emptyMessages = new MutableLiveData<>(new ArrayList<>());
        LiveData<List<Message>> firestoreMessages = Transformations.switchMap(
                currentChatId,
                chatId -> (chatId != null) ? messageService.getMessages(chatId) : emptyMessages);

        messagesMediator.addSource(firestoreMessages, messagesMediator::setValue);
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
                        .messageId("temp_" + System.currentTimeMillis())
                        .senderId(currentUserId)
                        .chatId(chatId)
                        .content(content.trim())
                        .messageType(MessageType.TEXT)
                        .timestamp(new Date())
                        .isDelivered(false)
                        .isRead(false)
                        .build();


        List<Message> currentMessages = messagesMediator.getValue();
        if (currentMessages != null) {
            List<Message> updatedMessages = new ArrayList<>(currentMessages);
            updatedMessages.add(message);
            messagesMediator.setValue(updatedMessages);
        }
        messageService
                .sendMessage(message)
                .addOnSuccessListener(
                        documentReference -> {
                        })
                .addOnFailureListener(
                        exception -> {
                            setError("Failed to send message: " + exception.getMessage());
                            if (currentMessages != null) {
                                messagesMediator.setValue(currentMessages);
                            }
                        });
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
