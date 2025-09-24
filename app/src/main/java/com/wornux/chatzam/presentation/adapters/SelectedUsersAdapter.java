package com.wornux.chatzam.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.wornux.chatzam.databinding.ItemSelectedUserBinding;
import com.wornux.chatzam.domain.entities.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.SelectedUserViewHolder> {
    
    private List<UserProfile> selectedUsers = new ArrayList<>();
    private OnUserRemoveListener removeListener;
    
    public interface OnUserRemoveListener {
        void onUserRemove(UserProfile user);
    }
    
    public void setOnUserRemoveListener(OnUserRemoveListener listener) {
        this.removeListener = listener;
    }
    
    @NonNull
    @Override
    public SelectedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectedUserBinding binding = ItemSelectedUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SelectedUserViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SelectedUserViewHolder holder, int position) {
        UserProfile user = selectedUsers.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return selectedUsers.size();
    }
    
    public void updateSelectedUsers(List<UserProfile> newUsers) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            new SelectedUserDiffCallback(selectedUsers, newUsers));
        selectedUsers.clear();
        selectedUsers.addAll(newUsers);
        diffResult.dispatchUpdatesTo(this);
    }
    
    public void addUser(UserProfile user) {
        if (!selectedUsers.contains(user)) {
            selectedUsers.add(user);
            notifyItemInserted(selectedUsers.size() - 1);
        }
    }
    
    public void removeUser(UserProfile user) {
        int index = selectedUsers.indexOf(user);
        if (index != -1) {
            selectedUsers.remove(index);
            notifyItemRemoved(index);
        }
    }
    
    class SelectedUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemSelectedUserBinding binding;
        
        public SelectedUserViewHolder(@NonNull ItemSelectedUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.removeUserButton.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION && removeListener != null) {
                    UserProfile user = selectedUsers.get(getAdapterPosition());
                    removeListener.onUserRemove(user);
                }
            });
        }
        
        public void bind(UserProfile user) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                String firstName = displayName.split(" ")[0];
                binding.selectedUserName.setText(firstName);
            } else {
                binding.selectedUserName.setText("User");
            }
        }
    }
    
    private static class SelectedUserDiffCallback extends DiffUtil.Callback {
        private final List<UserProfile> oldList;
        private final List<UserProfile> newList;
        
        public SelectedUserDiffCallback(List<UserProfile> oldList, List<UserProfile> newList) {
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
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}