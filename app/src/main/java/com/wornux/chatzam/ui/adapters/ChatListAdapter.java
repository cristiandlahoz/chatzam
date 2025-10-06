package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wornux.chatzam.R;
import com.wornux.chatzam.data.entities.UserDTO;
import com.wornux.chatzam.databinding.ItemChatBinding;
import com.wornux.chatzam.data.entities.Chat;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ChatListAdapter extends ListAdapter<Chat, ChatListAdapter.ChatViewHolder> {
  private OnChatClickListener clickListener;
  @Setter
  private String currentUserId;

  public interface OnChatClickListener {
    void onChatClick(Chat chat);

    void onChatLongClick(Chat chat);
  }

    public void setOnChatClickListener(OnChatClickListener listener) {
    this.clickListener = listener;
  }

  public ChatListAdapter() {
    super(DIFF_CALLBACK);
  }

  @NonNull
  @Override
  public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemChatBinding binding =
        ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ChatViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
    Chat chat = getItem(position);
    holder.bind(chat, clickListener, currentUserId);
  }

  public static class ChatViewHolder extends RecyclerView.ViewHolder {
    private final ItemChatBinding binding;

    public ChatViewHolder(@NonNull ItemChatBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(Chat chat, OnChatClickListener listener, String currentUserId) {
      setupChatName(chat, currentUserId);
      setupChatImage(chat, currentUserId);
      setupLastMessage(chat);
      setupTimestamp(chat);
      setupUnreadBadge(chat);
      setupClickListeners(chat, listener);
    }

    private void setupChatName(Chat chat, String currentUserId) {
      String chatName = chat.getDisplayName(currentUserId);
      binding.chatNameText.setText(chatName);
    }
    
    private void setupChatImage(Chat chat, String currentUserId) {
      if (chat.isGroup()) {
        if (chat.getGroupImageUrl() != null && !chat.getGroupImageUrl().isEmpty()) {
          Glide.with(binding.getRoot().getContext())
              .load(chat.getGroupImageUrl())
              .placeholder(R.mipmap.ic_launcher_round)
              .into(binding.chatImageView);
        } else {
          binding.chatImageView.setImageResource(R.mipmap.ic_launcher_round);
        }
      } else {
        String otherUserId = chat.getOtherParticipant(currentUserId);
        if (otherUserId != null && chat.getParticipantDetails() != null) {
          UserDTO otherUser =
              chat.getParticipantDetails().get(otherUserId);
          if (otherUser != null && otherUser.getProfileImageUrl() != null 
              && !otherUser.getProfileImageUrl().isEmpty()) {
            Glide.with(binding.getRoot().getContext())
                .load(otherUser.getProfileImageUrl())
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.chatImageView);
          } else {
            binding.chatImageView.setImageResource(R.mipmap.ic_launcher_round);
          }
        } else {
          binding.chatImageView.setImageResource(R.mipmap.ic_launcher_round);
        }
      }
    }

    private void setupLastMessage(Chat chat) {
      if (chat.getLastMessageContent() != null && !chat.getLastMessageContent().isEmpty()) {
        binding.lastMessageText.setText(chat.getLastMessageContent());
      } else {
        binding.lastMessageText.setText(R.string.no_messages_yet);
      }
      binding.lastMessageText.setVisibility(View.VISIBLE);
    }

    private void setupTimestamp(Chat chat) {
      if (chat.getLastMessageTimestamp() != null) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        binding.timestampText.setText(timeFormat.format(chat.getLastMessageTimestamp()));
        binding.timestampText.setVisibility(View.VISIBLE);
      } else {
        binding.timestampText.setVisibility(View.GONE);
      }
    }

    private void setupUnreadBadge(Chat chat) {
      if (chat.getUnreadCount() > 0) {
        binding.unreadCountBadge.setText(String.valueOf(chat.getUnreadCount()));
        binding.unreadCountBadge.setVisibility(View.VISIBLE);
      } else {
        binding.unreadCountBadge.setVisibility(View.GONE);
      }
    }

    private void setupClickListeners(Chat chat, OnChatClickListener listener) {
      itemView.setOnClickListener(
          v -> {
            if (listener != null) {
              listener.onChatClick(chat);
            }
          });

      if (listener != null) {
        itemView.setOnLongClickListener(
            v -> {
              listener.onChatLongClick(chat);
              return true;
            });
      } else {
        itemView.setOnLongClickListener(null);
      }
    }
  }

  private static final DiffUtil.ItemCallback<Chat> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<Chat>() {
        @Override
        public boolean areItemsTheSame(@NonNull Chat oldItem, @NonNull Chat newItem) {
          return oldItem.getChatId().equals(newItem.getChatId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Chat oldItem, @NonNull Chat newItem) {
          return Objects.equals(oldItem.getGroupName(), newItem.getGroupName())
              && Objects.equals(oldItem.getLastMessageContent(), newItem.getLastMessageContent())
//                  && Objects.equals(oldItem.getDisplayName(currentUserId), newItem.getDisplayName(currentUserId))
              && Objects.equals(
                  oldItem.getLastMessageTimestamp(), newItem.getLastMessageTimestamp())
              && oldItem.getUnreadCount() == newItem.getUnreadCount();
        }
      };
}
