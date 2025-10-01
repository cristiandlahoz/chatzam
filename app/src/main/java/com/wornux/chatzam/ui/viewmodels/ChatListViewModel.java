package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ChatListViewModel extends BaseViewModel {
    
    private final ChatRepository chatRepository;
    private final AuthenticationManager authManager;
    @Getter
    private final LiveData<List<Chat>> chats;
    
    @Inject
    public ChatListViewModel(ChatRepository chatRepository, 
                           AuthenticationManager authManager) {
        this.chatRepository = chatRepository;
        this.authManager = authManager;
        
        // Get real-time chats from Firebase for current user
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            this.chats = chatRepository.getChats(currentUserId);
        } else {
            this.chats = new MutableLiveData<>();
        }
    }

    public LiveData<Boolean> isEmpty() {
        return Transformations.map(chats, chatList -> 
            chatList == null || chatList.isEmpty());
    }
    
    public void deleteChat(String chatId) {
        setLoading(true);
        chatRepository.deleteChat(chatId)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to delete chat: " + exception.getMessage());
                });
    }
    
    public void refreshChats() {
        // Real-time listeners automatically refresh,
        // but we can trigger a manual refresh if needed
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            setError("User not logged in");
        }
    }
    
    private String getCurrentUserId() {
        return authManager.getCurrentUser() != null ? 
               authManager.getCurrentUser().getUid() : null;
    }
}