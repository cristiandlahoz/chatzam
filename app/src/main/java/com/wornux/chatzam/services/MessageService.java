package com.wornux.chatzam.services;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.wornux.chatzam.data.repositories.MessageRepository;
import com.wornux.chatzam.data.repositories.StorageRepository;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final StorageRepository storageRepository;
    
    @Inject
    public MessageService(MessageRepository messageRepository, 
                                StorageRepository storageRepository) {
        this.messageRepository = messageRepository;
        this.storageRepository = storageRepository;
    }
    
    public Task<DocumentReference> sendMessage(Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            if (message.getMediaUrl() == null || message.getMediaUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Message must have content or media");
            }
        }
        
        return messageRepository.createMessage(message);
    }
    
    public LiveData<List<Message>> getMessages(String chatId) {
        return messageRepository.getMessagesByChatId(chatId);
    }
    
    public Task<Void> markMessageAsRead(String messageId) {
        return messageRepository.markAsRead(messageId);
    }
    
    public Task<Void> markMessageAsDelivered(String messageId) {
        return messageRepository.markAsDelivered(messageId);
    }
    
    public Task<Void> deleteMessage(String messageId) {
        return messageRepository.deleteDocument(messageId);
    }
    
    public Task<String> uploadMedia(Uri uri, MessageType messageType) {
        if (uri == null) {
            throw new IllegalArgumentException("Media URI cannot be null");
        }
        
        String fileName = "media_" + System.currentTimeMillis();
        
        Task<Uri> uploadTask;
        switch (messageType) {
            case IMAGE:
                uploadTask = storageRepository.uploadImage(uri, fileName);
                break;
            case VIDEO:
                uploadTask = storageRepository.uploadVideo(uri, fileName);
                break;
            case DOCUMENT:
                uploadTask = storageRepository.uploadDocument(uri, fileName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported media type: " + messageType);
        }
        
        return uploadTask.continueWith(task -> {
            Uri downloadUrl = task.getResult();
            return downloadUrl.toString();
        });
    }
}
