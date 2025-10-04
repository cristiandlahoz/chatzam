package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wornux.chatzam.data.entities.UserProfile;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.ui.base.BaseViewModel;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import com.google.android.gms.tasks.OnCompleteListener;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatCreationViewModel extends BaseViewModel {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MutableLiveData<List<UserProfile>> availableUsers = new MutableLiveData<>();
    private final MutableLiveData<List<UserProfile>> selectedUsers = new MutableLiveData<>();
    private final AuthenticationManager authManager;


    @Inject
    public ChatCreationViewModel(UserRepository userRepository,
                                 ChatRepository chatRepository,
                                 AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.authManager = authManager;
        loadUsers();
    }


    public LiveData<List<UserProfile>> getAvailableUsers() {
        return availableUsers;
    }

    public LiveData<List<UserProfile>> getSelectedUsers() {
        return selectedUsers;
    }

    public void searchUsers(String query) {
        userRepository.searchUsers(query).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                availableUsers.setValue(task.getResult());
            }
        });
    }

    public void addUserToSelection(UserProfile user) {
        List<UserProfile> currentSelection = new ArrayList<>();
        currentSelection.add(user);
        selectedUsers.setValue(currentSelection);
    }

    public void removeUserFromSelection(UserProfile user) {
        selectedUsers.setValue(new ArrayList<>());
    }

    private void loadUsers() {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            setLoading(true);
            userRepository.searchUsers("")
                    .addOnSuccessListener(users -> {
                        setLoading(false);
                        if (users != null) {
                            List<UserProfile> filteredUsers = new ArrayList<>();
                            for (UserProfile user : users) {
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
        List<UserProfile> selected = selectedUsers.getValue();
        if (selected == null || selected.isEmpty()) {
            setError("No user selected");
            return;
        }

        UserProfile selectedUser = selected.get(0);
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(selectedUser.getUserId());

        setLoading(true);
        chatRepository.createChat(participants, false)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        setError("Failed to create chat: " + task.getException().getMessage());
                    }
                    onCompleteListener.onComplete(task);
                });
    }
}