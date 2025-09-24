package com.wornux.chatzam.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.data.services.AuthenticationManager;
import com.wornux.chatzam.domain.entities.Message;
import com.wornux.chatzam.domain.enums.MessageType;
import com.wornux.chatzam.domain.repositories.MessageRepository;
import com.wornux.chatzam.presentation.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

@HiltViewModel
public class ChatViewModel extends BaseViewModel {
    
    private final MessageRepository messageRepository;
    private final AuthenticationManager authManager;
    private final MutableLiveData<String> currentChatId = new MutableLiveData<>();
    private LiveData<List<Message>> messages;
    
    @Inject
    public ChatViewModel(MessageRepository messageRepository, 
                        AuthenticationManager authManager) {
        this.messageRepository = messageRepository;
        this.authManager = authManager;
        
        this.messages = Transformations.switchMap(currentChatId, chatId -> {
            if (chatId != null) {
                return messageRepository.getMessages(chatId);
            } else {
                return new MutableLiveData<>();
            }
        });
    }
    
    public void setChatId(String chatId) {
        currentChatId.setValue(chatId);
    }
    
    public LiveData<List<Message>> getMessages() {
        return messages;
    }
    
    public LiveData<Boolean> isEmpty() {
        return Transformations.map(messages, messageList -> 
            messageList == null || messageList.isEmpty());
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
        
        Message message = Message.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(currentUserId)
                .chatId(chatId)
                .content(content.trim())
                .messageType(MessageType.TEXT)
                .timestamp(new Date())
                .isDelivered(false)
                .isRead(false)
                .build();
        
        setLoading(true);
        messageRepository.sendMessage(message)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to send message: " + exception.getMessage());
                });
    }
    
    public void markMessageAsRead(String messageId) {
        messageRepository.markMessageAsRead(messageId)
                .addOnFailureListener(exception -> 
                    setError("Failed to mark message as read: " + exception.getMessage()));
    }
    
    public String getCurrentUserId() {
        return authManager.getCurrentUser() != null ? 
               authManager.getCurrentUser().getUid() : null;
    }
    
    public boolean isMessageFromCurrentUser(Message message) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(message.getSenderId());
    }
}