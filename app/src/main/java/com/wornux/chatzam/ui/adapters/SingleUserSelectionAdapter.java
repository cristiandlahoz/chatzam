package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.wornux.chatzam.databinding.ItemUserChatSelectionBinding;
import com.wornux.chatzam.data.entities.UserProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SingleUserSelectionAdapter extends RecyclerView.Adapter<SingleUserSelectionAdapter.SingleUserSelectionViewHolder> {
    
    private List<UserProfile> users = new ArrayList<>();
    private Set<String> selectedUserIds = new HashSet<>();
    private OnUserSelectionListener selectionListener;
    
    public interface OnUserSelectionListener {
        void onUserSelected(UserProfile user);
        void onUserDeselected(UserProfile user);
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
        UserProfile user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void updateUsers(List<UserProfile> newUsers) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(users, newUsers));
        users.clear();
        users.addAll(newUsers);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectedUser(UserProfile user) {
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
    
    public List<UserProfile> getSelectedUsers() {
        List<UserProfile> selectedUsers = new ArrayList<>();
        for (UserProfile user : users) {
            if (selectedUserIds.contains(user.getUserId())) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }
    
    class SingleUserSelectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserChatSelectionBinding binding;
        
        public SingleUserSelectionViewHolder(@NonNull ItemUserChatSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    UserProfile user = users.get(getAdapterPosition());
                    if (isChecked) {
                        if (!selectedUserIds.isEmpty()) {
                            UserProfile previouslySelectedUser = null;
                            for (UserProfile u : users) {
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
        
        public void bind(UserProfile user) {
            binding.userNameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");
            binding.userEmailText.setText(user.getEmail() != null ? user.getEmail() : "");
            
            boolean isSelected = selectedUserIds.contains(user.getUserId());
            binding.radioButton.setOnCheckedChangeListener(null);
            binding.radioButton.setChecked(isSelected);
            binding.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedUserIds.isEmpty()) {
                        UserProfile previouslySelectedUser = null;
                        for (UserProfile u : users) {
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
        private final List<UserProfile> oldList;
        private final List<UserProfile> newList;
        
        public UserDiffCallback(List<UserProfile> oldList, List<UserProfile> newList) {
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
            UserProfile oldUser = oldList.get(oldItemPosition);
            UserProfile newUser = newList.get(newItemPosition);
            
            return oldUser.equals(newUser);
        }
    }
}