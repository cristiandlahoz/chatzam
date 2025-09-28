package com.wornux.chatzam.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.data.entities.UserProfile;
import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.repositories.UserRepository;
import com.wornux.chatzam.ui.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class GroupChatViewModel extends BaseViewModel {
    
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<List<UserProfile>> availableUsers = new MutableLiveData<>();
    private final MutableLiveData<List<UserProfile>> selectedUsers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> groupCreated = new MutableLiveData<>();
    
    @Inject
    public GroupChatViewModel(ChatRepository chatRepository, 
                             UserRepository userRepository,
                             AuthenticationManager authManager) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.authManager = authManager;
        
        loadAvailableUsers();
    }
    
    public LiveData<List<UserProfile>> getAvailableUsers() {
        return availableUsers;
    }
    
    public LiveData<List<UserProfile>> getSelectedUsers() {
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
        userRepository.searchUsers(query.trim())
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
    
    public void addUserToSelection(UserProfile user) {
        List<UserProfile> current = selectedUsers.getValue();
        if (current != null) {
            List<UserProfile> updated = new ArrayList<>(current);
            if (!updated.contains(user)) {
                updated.add(user);
                selectedUsers.setValue(updated);
            }
        }
    }
    
    public void removeUserFromSelection(UserProfile user) {
        List<UserProfile> current = selectedUsers.getValue();
        if (current != null) {
            List<UserProfile> updated = new ArrayList<>(current);
            updated.remove(user);
            selectedUsers.setValue(updated);
        }
    }
    
    public void createGroup(String groupName) {
        String currentUserId = getCurrentUserId();
        List<UserProfile> selected = selectedUsers.getValue();
        
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
        
        for (UserProfile user : selected) {
            participantIds.add(user.getUserId());
        }
        
        setLoading(true);
        chatRepository.createGroupChat(groupName.trim(), participantIds, currentUserId)
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
        chatRepository.addMembersToGroup(chatId, newMemberIds)
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
        chatRepository.removeMemberFromGroup(chatId, memberId)
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
            chatRepository.leaveGroup(chatId, currentUserId)
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
        chatRepository.updateGroupInfo(chatId, groupName, groupImageUrl)
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