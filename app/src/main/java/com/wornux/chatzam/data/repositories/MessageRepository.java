package com.wornux.chatzam.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.repositories.base.BaseRepository;
import com.wornux.chatzam.services.FirebaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessageRepository extends BaseRepository<Message> {
    
    private static final String CHATS_COLLECTION = "chats";
    private static final String MESSAGES_SUBCOLLECTION = "messages";
    
    @Inject
    public MessageRepository(FirebaseManager firebaseManager) {
        super(firebaseManager.getFirestore(), Message.class);
    }
    
    public Task<String> createMessage(String chatId, Message message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        
        return db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
                .set(message)
                .continueWith(task -> messageId);
    }
    
    public LiveData<List<Message>> getMessagesByChatId(String chatId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        
        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        List<Message> messages = value.toObjects(Message.class);
                        messagesLiveData.setValue(messages);
                    }
                });
        
        return messagesLiveData;
    }
    
    public Task<Void> markAsRead(String chatId, String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);
        updates.put("is_delivered", true);
        
        return db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
                .update(updates);
    }
    
    public Task<Void> markAsDelivered(String chatId, String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_delivered", true);
        
        return db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
                .update(updates);
    }
}
