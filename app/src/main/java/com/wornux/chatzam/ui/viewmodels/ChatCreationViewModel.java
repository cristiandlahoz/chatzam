package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.services.ChatService;
import com.wornux.chatzam.services.UserService;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.ui.base.BaseViewModel;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import com.google.android.gms.tasks.OnCompleteListener;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatCreationViewModel extends BaseViewModel {

    private final UserService userService;
    private final ChatService chatService;
    private final MutableLiveData<List<User>> availableUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> selectedUsers = new MutableLiveData<>();
    private final AuthenticationManager authManager;


    @Inject
    public ChatCreationViewModel(UserService userService,
                                 ChatService chatService,
                                 AuthenticationManager authManager) {
        this.userService = userService;
        this.chatService = chatService;
        this.authManager = authManager;
        loadUsers();
    }


    public LiveData<List<User>> getAvailableUsers() {
        return availableUsers;
    }

    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }

    public void searchUsers(String query) {
        userService.searchUsers(query).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                availableUsers.setValue(task.getResult());
            }
        });
    }

    public void addUserToSelection(User user) {
        List<User> currentSelection = new ArrayList<>();
        currentSelection.add(user);
        selectedUsers.setValue(currentSelection);
    }

    public void removeUserFromSelection(User user) {
        selectedUsers.setValue(new ArrayList<>());
    }

    private void loadUsers() {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            setLoading(true);
            userService.searchUsers("")
                    .addOnSuccessListener(users -> {
                        setLoading(false);
                        if (users != null) {
                            List<User> filteredUsers = new ArrayList<>();
                            for (User user : users) {
                                if (!user.getUserId().equals(currentUserId)) {
                                    filteredUsers.add(user);
                                }
                            }
                            availableUsers.setValue(filteredUsers);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        setLoading(false);
                        setError("Failed to load users: " + exception.getMessage());
                    });
        }
    }

    private String getCurrentUserId() {
        return authManager.getCurrentUser() != null ?
                authManager.getCurrentUser().getUid() : null;
    }

    public void createChat(String currentUserId, OnCompleteListener<String> onCompleteListener) {
        List<User> selected = selectedUsers.getValue();
        if (selected == null || selected.isEmpty()) {
            setError("No user selected");
            return;
        }

        User selectedUser = selected.get(0);
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(selectedUser.getUserId());

        setLoading(true);
        chatService.createIndividualChat(participants, selectedUser.getDisplayName())
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        setError("Failed to create chat: " + task.getException().getMessage());
                    }
                    onCompleteListener.onComplete(task);
                });
    }
}