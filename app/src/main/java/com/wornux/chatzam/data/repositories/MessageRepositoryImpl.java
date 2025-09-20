package com.wornux.chatzam.data.repositories;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.wornux.chatzam.data.services.FirebaseStorageService;
import com.wornux.chatzam.data.services.FirestoreService;
import com.wornux.chatzam.domain.entities.Message;
import com.wornux.chatzam.domain.enums.MessageType;
import com.wornux.chatzam.domain.repositories.MessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessageRepositoryImpl implements MessageRepository {
    
    private static final String MESSAGES_COLLECTION = "messages";
    
    private final FirestoreService firestoreService;
    private final FirebaseStorageService storageService;
    
    @Inject
    public MessageRepositoryImpl(FirestoreService firestoreService, 
                                FirebaseStorageService storageService) {
        this.firestoreService = firestoreService;
        this.storageService = storageService;
    }
    
    @Override
    public Task<DocumentReference> sendMessage(Message message) {
        Map<String, Object> messageData = messageToMap(message);
        return firestoreService.addDocument(MESSAGES_COLLECTION, messageData);
    }
    
    @Override
    public LiveData<List<Message>> getMessages(String chatId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        
        firestoreService.getFirestore().collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        List<Message> messages = new ArrayList<>();
                        
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Message message = documentToMessage(document);
                            messages.add(message);
                        }
                        
                        messagesLiveData.setValue(messages);
                    }
                });
        
        return messagesLiveData;
    }
    
    @Override
    public Task<Void> markMessageAsRead(String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isRead", true);
        updates.put("isDelivered", true);
        
        return firestoreService.updateDocument(MESSAGES_COLLECTION, messageId, updates);
    }
    
    @Override
    public Task<Void> markMessageAsDelivered(String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isDelivered", true);
        
        return firestoreService.updateDocument(MESSAGES_COLLECTION, messageId, updates);
    }
    
    @Override
    public Task<Void> deleteMessage(String messageId) {
        return firestoreService.deleteDocument(MESSAGES_COLLECTION, messageId);
    }
    
    @Override
    public Task<String> uploadMedia(Uri uri, MessageType messageType) {
        String fileName = "media_" + System.currentTimeMillis();
        
        Task<Uri> uploadTask;
        switch (messageType) {
            case IMAGE:
                uploadTask = storageService.uploadImage(uri, fileName);
                break;
            case VIDEO:
                uploadTask = storageService.uploadVideo(uri, fileName);
                break;
            case DOCUMENT:
                uploadTask = storageService.uploadDocument(uri, fileName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported media type: " + messageType);
        }
        
        return uploadTask.continueWith(task -> {
            Uri downloadUrl = task.getResult();
            return downloadUrl.toString();
        });
    }
    
    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", message.getMessageId());
        data.put("senderId", message.getSenderId());
        data.put("receiverId", message.getReceiverId());
        data.put("chatId", message.getChatId());
        data.put("content", message.getContent());
        data.put("encryptedContent", message.getEncryptedContent());
        data.put("messageType", message.getMessageType() != null ? message.getMessageType().name() : null);
        data.put("timestamp", message.getTimestamp());
        data.put("isDelivered", message.isDelivered());
        data.put("isRead", message.isRead());
        data.put("mediaUrl", message.getMediaUrl());
        return data;
    }
    
    private Message documentToMessage(DocumentSnapshot document) {
        return Message.builder()
                .messageId(document.getId())
                .senderId(document.getString("senderId"))
                .receiverId(document.getString("receiverId"))
                .chatId(document.getString("chatId"))
                .content(document.getString("content"))
                .encryptedContent(document.getString("encryptedContent"))
                .messageType(MessageType.valueOf(document.getString("messageType")))
                .timestamp(document.getDate("timestamp"))
                .isDelivered(document.getBoolean("isDelivered") != null ? document.getBoolean("isDelivered") : false)
                .isRead(document.getBoolean("isRead") != null ? document.getBoolean("isRead") : false)
                .mediaUrl(document.getString("mediaUrl"))
                .build();
    }
}