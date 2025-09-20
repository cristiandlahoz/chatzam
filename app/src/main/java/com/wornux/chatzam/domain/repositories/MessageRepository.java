package com.wornux.chatzam.domain.repositories;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.wornux.chatzam.domain.entities.Message;
import com.wornux.chatzam.domain.enums.MessageType;

import java.util.List;

public interface MessageRepository {
    Task<DocumentReference> sendMessage(Message message);
    LiveData<List<Message>> getMessages(String chatId);
    Task<Void> markMessageAsRead(String messageId);
    Task<Void> markMessageAsDelivered(String messageId);
    Task<Void> deleteMessage(String messageId);
    Task<String> uploadMedia(Uri uri, MessageType messageType);
}