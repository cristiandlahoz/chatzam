package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.services.ChatService;
import com.wornux.chatzam.services.UserService;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class GroupChatViewModel extends BaseViewModel {
    
    private final ChatService chatService;
    private final UserService userService;
    private final AuthenticationManager authManager;
    
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<List<User>> availableUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> selectedUsers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> groupCreated = new MutableLiveData<>();
    
    @Inject
    public GroupChatViewModel(ChatService chatService, 
                             UserService userService,
                             AuthenticationManager authManager) {
        this.chatService = chatService;
        this.userService = userService;
        this.authManager = authManager;
        
        loadAvailableUsers();
    }
    
    public LiveData<List<User>> getAvailableUsers() {
        return availableUsers;
    }
    
    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }
    
    public LiveData<Boolean> isGroupCreated() {
        return groupCreated;
    }
    
    public LiveData<Boolean> canCreateGroup() {
        return Transformations.map(selectedUsers, users -> 
            users != null && users.size() >= 2);
    }
    
    public void searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAvailableUsers();
            return;
        }
        
        setLoading(true);
        userService.searchUsers(query.trim())
                .addOnSuccessListener(users -> {
                    setLoading(false);
                    if (users != null) {
                        availableUsers.setValue(users);
                    }
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to search users: " + exception.getMessage());
                });
    }
    
    public void loadAvailableUsers() {
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
    
    public void addUserToSelection(User user) {
        List<User> current = selectedUsers.getValue();
        if (current != null) {
            List<User> updated = new ArrayList<>(current);
            if (!updated.contains(user)) {
                updated.add(user);
                selectedUsers.setValue(updated);
            }
        }
    }
    
    public void removeUserFromSelection(User user) {
        List<User> current = selectedUsers.getValue();
        if (current != null) {
            List<User> updated = new ArrayList<>(current);
            updated.remove(user);
            selectedUsers.setValue(updated);
        }
    }
    
    public void createGroup(String groupName) {
        String currentUserId = getCurrentUserId();
        List<User> selected = selectedUsers.getValue();
        
        if (currentUserId == null) {
            setError("User not logged in");
            return;
        }
        
        if (groupName == null || groupName.trim().isEmpty()) {
            setError("Group name cannot be empty");
            return;
        }
        
        if (selected == null || selected.size() < 2) {
            setError("Select at least 2 members to create a group");
            return;
        }
        
        List<String> participantIds = new ArrayList<>();
        participantIds.add(currentUserId);
        
        for (User user : selected) {
            participantIds.add(user.getUserId());
        }
        
        setLoading(true);
        chatService.createGroupChat(groupName.trim(), participantIds, currentUserId)
                .addOnSuccessListener(chatId -> {
                    setLoading(false);
                    groupCreated.setValue(true);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to create group: " + exception.getMessage());
                });
    }
    
    public void addMembersToGroup(String chatId, List<String> newMemberIds) {
        setLoading(true);
        chatService.addMembersToGroup(chatId, newMemberIds)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to add members: " + exception.getMessage());
                });
    }
    
    public void removeMemberFromGroup(String chatId, String memberId) {
        setLoading(true);
        chatService.removeMemberFromGroup(chatId, memberId)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to remove member: " + exception.getMessage());
                });
    }
    
    public void leaveGroup(String chatId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            setLoading(true);
            chatService.leaveGroup(chatId, currentUserId)
                    .addOnSuccessListener(aVoid -> {
                        setLoading(false);
                    })
                    .addOnFailureListener(exception -> {
                        setLoading(false);
                        setError("Failed to leave group: " + exception.getMessage());
                    });
        }
    }
    
    public void updateGroupInfo(String chatId, String groupName, String groupImageUrl) {
        setLoading(true);
        chatService.updateGroupInfo(chatId, groupName, groupImageUrl)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to update group info: " + exception.getMessage());
                });
    }
    
    private String getCurrentUserId() {
        return authManager.getCurrentUser() != null ? 
               authManager.getCurrentUser().getUid() : null;
    }
}