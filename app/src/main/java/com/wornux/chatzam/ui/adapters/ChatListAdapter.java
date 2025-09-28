package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.wornux.chatzam.databinding.ItemChatBinding;
import com.wornux.chatzam.data.entities.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    
    private List<Chat> chats = new ArrayList<>();
    private OnChatClickListener clickListener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
        void onChatLongClick(Chat chat);
    }
    
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.clickListener = listener;
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding binding = ItemChatBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }
    
    @Override
    public int getItemCount() {
        return chats.size();
    }
    
    public void updateChats(List<Chat> newChats) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatDiffCallback(chats, newChats));
        chats.clear();
        chats.addAll(newChats);
        diffResult.dispatchUpdatesTo(this);
    }
    
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBinding binding;
        
        public ChatViewHolder(@NonNull ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onChatClick(chats.get(getAdapterPosition()));
                }
            });
            
            binding.getRoot().setOnLongClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onChatLongClick(chats.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Chat chat) {
            // Set chat name based on type
            if (chat.isGroup()) {
                binding.chatNameText.setText(chat.getGroupName() != null ? 
                    chat.getGroupName() : "Group Chat");
            } else {
                // For individual chats, we'd need to get the other participant's name
                // For now, show participant count
                binding.chatNameText.setText("Private Chat");
            }
            
            // Set last message
            if (chat.getLastMessage() != null) {
                binding.lastMessageText.setText(chat.getLastMessage().getContent());
                binding.lastMessageText.setVisibility(View.VISIBLE);
            } else {
                binding.lastMessageText.setText("No messages yet");
                binding.lastMessageText.setVisibility(View.VISIBLE);
            }
            
            // Set timestamp
            if (chat.getLastMessageTimestamp() != null) {
                binding.timestampText.setText(timeFormat.format(chat.getLastMessageTimestamp()));
                binding.timestampText.setVisibility(View.VISIBLE);
            } else {
                binding.timestampText.setVisibility(View.GONE);
            }
            
            // Set unread count
            if (chat.getUnreadCount() > 0) {
                binding.unreadCountBadge.setText(String.valueOf(chat.getUnreadCount()));
                binding.unreadCountBadge.setVisibility(View.VISIBLE);
            } else {
                binding.unreadCountBadge.setVisibility(View.GONE);
            }
            
            // TODO: Load chat image using Glide or similar
            // For now, use default image
        }
    }
    
    private static class ChatDiffCallback extends DiffUtil.Callback {
        private final List<Chat> oldList;
        private final List<Chat> newList;
        
        public ChatDiffCallback(List<Chat> oldList, List<Chat> newList) {
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
            return oldList.get(oldItemPosition).getChatId()
                   .equals(newList.get(newItemPosition).getChatId());
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Chat oldChat = oldList.get(oldItemPosition);
            Chat newChat = newList.get(newItemPosition);
            
            return oldChat.equals(newChat) &&
                   oldChat.getLastMessageTimestamp() != null && 
                   oldChat.getLastMessageTimestamp().equals(newChat.getLastMessageTimestamp()) &&
                   oldChat.getUnreadCount() == newChat.getUnreadCount();
        }
    }
}