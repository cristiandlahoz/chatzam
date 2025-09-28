package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.wornux.chatzam.databinding.ItemUserSelectionBinding;
import com.wornux.chatzam.data.entities.UserProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserSelectionViewHolder> {
    
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
    public UserSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserSelectionBinding binding = ItemUserSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserSelectionViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserSelectionViewHolder holder, int position) {
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
    
    class UserSelectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserSelectionBinding binding;
        
        public UserSelectionViewHolder(@NonNull ItemUserSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.userSelectionCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    UserProfile user = users.get(getAdapterPosition());
                    if (isChecked) {
                        selectedUserIds.add(user.getUserId());
                        if (selectionListener != null) {
                            selectionListener.onUserSelected(user);
                        }
                    } else {
                        selectedUserIds.remove(user.getUserId());
                        if (selectionListener != null) {
                            selectionListener.onUserDeselected(user);
                        }
                    }
                }
            });
            
            binding.getRoot().setOnClickListener(v -> {
                binding.userSelectionCheckBox.toggle();
            });
        }
        
        public void bind(UserProfile user) {
            binding.userNameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");
            binding.userEmailText.setText(user.getEmail() != null ? user.getEmail() : "");
            
            boolean isSelected = selectedUserIds.contains(user.getUserId());
            binding.userSelectionCheckBox.setOnCheckedChangeListener(null);
            binding.userSelectionCheckBox.setChecked(isSelected);
            binding.userSelectionCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUserIds.add(user.getUserId());
                    if (selectionListener != null) {
                        selectionListener.onUserSelected(user);
                    }
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