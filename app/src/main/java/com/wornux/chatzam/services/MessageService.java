package com.wornux.chatzam.services;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.wornux.chatzam.data.repositories.MessageRepository;
import com.wornux.chatzam.data.repositories.StorageRepository;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class MessageService {

    private final MessageRepository messageRepository;
    private final StorageRepository storageRepository;
    private final ChatService chatService;

    @Inject
    public MessageService(MessageRepository messageRepository,
                          StorageRepository storageRepository, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.storageRepository = storageRepository;
        this.chatService = chatService;
    }

    public Task<String> sendMessage(Message message) {
        if ((message.getContent() == null || message.getContent().trim().isEmpty()) && (message.getMediaUrl() == null || message.getMediaUrl().trim().isEmpty()))
            throw new IllegalArgumentException("Message must have content or media");

        return messageRepository.createMessage(message.getChatId(), message).addOnSuccessListener(v -> {
            chatService.updateLastMessage(message.getChatId(), message);
        });
    }

    public LiveData<List<Message>> getMessages(String chatId) {
        return messageRepository.getMessagesByChatId(chatId);
    }

    public Task<String> uploadMedia(Uri uri, MessageType messageType) {
        if (uri == null) {
            throw new IllegalArgumentException("Media URI cannot be null");
        }

        String fileName = "media_" + System.currentTimeMillis();

        if (Objects.requireNonNull(messageType) != MessageType.IMAGE)
            Tasks.forException(new IllegalArgumentException("Unsupported media type: " + messageType));

        return storageRepository.uploadImage(uri, fileName).continueWith(task -> {
            Uri downloadUrl = task.getResult();
            return downloadUrl.toString();
        });
    }
}
