package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.wornux.chatzam.databinding.ItemUserChatSelectionBinding;
import com.wornux.chatzam.data.entities.User;

import java.util.*;

public class SingleUserSelectionAdapter extends RecyclerView.Adapter<SingleUserSelectionAdapter.SingleUserSelectionViewHolder> {
    
    private final List<User> users = new ArrayList<>();
    private final Set<String> selectedUserIds = new HashSet<>();
    private OnUserSelectionListener selectionListener;
    
    public interface OnUserSelectionListener {
        void onUserSelected(User user);
        void onUserDeselected(User user);
    }
    
    public void setOnUserSelectionListener(OnUserSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    @NonNull
    @Override
    public SingleUserSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserChatSelectionBinding binding = ItemUserChatSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SingleUserSelectionViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SingleUserSelectionViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void updateUsers(List<User> allUsers, String currentUserId, List<String> existingChatIds) {
        List<User> availableUsers = filterAvailableUsers(allUsers, currentUserId, existingChatIds);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(this.users, availableUsers));
        users.clear();
        users.addAll(availableUsers);
        diffResult.dispatchUpdatesTo(this);
    }
    
    private List<User> filterAvailableUsers(List<User> allUsers, String currentUserId, List<String> existingChatIds) {
        List<User> availableUsers = new ArrayList<>();
        Set<String> existingChatsSet = new HashSet<>(existingChatIds);

        for (User user : allUsers) {
            // No mostrar el usuario actual en la lista de selección
            if (user.getUserId().equals(currentUserId)) {
                continue;
            }

            String potentialChatId = generateCanonicalId(currentUserId, user.getUserId());
            if (!existingChatsSet.contains(potentialChatId)) {
                availableUsers.add(user);
            }
        }
        return availableUsers;
    }

    private String generateCanonicalId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        // Esta lógica debe ser idéntica a la de ChatService para crear el ID de un chat individual.
        return ids.get(0) + ids.get(1);
    }

    public void setSelectedUser(User user) {
        selectedUserIds.clear();
        if (user != null) {
            selectedUserIds.add(user.getUserId());
        }
        notifyDataSetChanged();
    }
    
    public void clearSelections() {
        selectedUserIds.clear();
        notifyDataSetChanged();
    }
    
    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }
    
    public List<User> getSelectedUsers() {
        List<User> selectedUsers = new ArrayList<>();
        for (User user : users) {
            if (selectedUserIds.contains(user.getUserId())) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }
    
    public class SingleUserSelectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserChatSelectionBinding binding;
        
        public SingleUserSelectionViewHolder(@NonNull ItemUserChatSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    User user = users.get(getAdapterPosition());
                    if (isChecked) {
                        if (!selectedUserIds.isEmpty()) {
                            User previouslySelectedUser = null;
                            for (User u : users) {
                                if (selectedUserIds.contains(u.getUserId())) {
                                    previouslySelectedUser = u;
                                    break;
                                }
                            }
                            selectedUserIds.clear();
                            if (previouslySelectedUser != null && selectionListener != null) {
                                selectionListener.onUserDeselected(previouslySelectedUser);
                            }
                        }
                        selectedUserIds.add(user.getUserId());
                        if (selectionListener != null) {
                            selectionListener.onUserSelected(user);
                        }
                        notifyDataSetChanged();
                    } else {
                        selectedUserIds.remove(user.getUserId());
                        if (selectionListener != null) {
                            selectionListener.onUserDeselected(user);
                        }
                    }
                }
            });
            
            binding.getRoot().setOnClickListener(v -> {
                binding.radioButton.toggle();
            });
        }
        
        public void bind(User user) {
            binding.userNameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");
            binding.userEmailText.setText(user.getEmail() != null ? user.getEmail() : "");
            
            boolean isSelected = selectedUserIds.contains(user.getUserId());
            binding.radioButton.setOnCheckedChangeListener(null);
            binding.radioButton.setChecked(isSelected);
            binding.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedUserIds.isEmpty()) {
                        User previouslySelectedUser = null;
                        for (User u : users) {
                            if (selectedUserIds.contains(u.getUserId())) {
                                previouslySelectedUser = u;
                                break;
                            }
                        }
                        selectedUserIds.clear();
                        if (previouslySelectedUser != null && selectionListener != null) {
                            selectionListener.onUserDeselected(previouslySelectedUser);
                        }
                    }
                    selectedUserIds.add(user.getUserId());
                    if (selectionListener != null) {
                        selectionListener.onUserSelected(user);
                    }
                    notifyDataSetChanged();
                } else {
                    selectedUserIds.remove(user.getUserId());
                    if (selectionListener != null) {
                        selectionListener.onUserDeselected(user);
                    }
                }
            });
        }
    }
    
    private static class UserDiffCallback extends DiffUtil.Callback {
        private final List<User> oldList;
        private final List<User> newList;
        
        public UserDiffCallback(List<User> oldList, List<User> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getUserId()
                    .equals(newList.get(newItemPosition).getUserId());
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            User oldUser = oldList.get(oldItemPosition);
            User newUser = newList.get(newItemPosition);
            
            return oldUser.equals(newUser);
        }
    }
}