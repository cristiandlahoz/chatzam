package com.wornux.chatzam.domain.repositories;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.wornux.chatzam.domain.entities.Chat;
import com.wornux.chatzam.domain.entities.Message;

import java.util.List;

public interface ChatRepository {
    LiveData<List<Chat>> getChats(String userId);
    Task<String> createChat(List<String> participants, boolean isGroup);
    Task<Void> updateLastMessage(String chatId, Message message);
    Task<Chat> getChatById(String chatId);
    Task<Void> updateChatInfo(Chat chat);
    Task<Void> deleteChat(String chatId);
}