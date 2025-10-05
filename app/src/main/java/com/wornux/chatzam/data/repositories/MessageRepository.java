package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessageRepository extends BaseRepository<Message> {
    
    @Inject
    public MessageRepository(FirebaseManager firebaseManager) {
        super(firebaseManager.getFirestore(), Message.class);
    }
    
    public Task<DocumentReference> createMessage(Message message) {
        Map<String, Object> messageData = messageToMap(message);
        return addDocument(messageData);
    }
    
    public LiveData<List<Message>> getMessagesByChatId(String chatId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        
        firestore.collection(collectionName)
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
    
    public Task<Void> markAsRead(String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isRead", true);
        updates.put("isDelivered", true);
        return updateDocument(messageId, updates);
    }
    
    public Task<Void> markAsDelivered(String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isDelivered", true);
        return updateDocument(messageId, updates);
    }
    
    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> data = new HashMap<>();
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
                .isDelivered(document.getBoolean("isDelivered") != null && Boolean.TRUE.equals(
                        document.getBoolean("isDelivered")))
                .isRead(document.getBoolean("isRead") != null && Boolean.TRUE.equals(document.getBoolean("isRead")))
                .mediaUrl(document.getString("mediaUrl"))
                .build();
    }
}
