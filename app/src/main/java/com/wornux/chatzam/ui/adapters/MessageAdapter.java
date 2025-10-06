package com.wornux.chatzam.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.wornux.chatzam.databinding.ItemMessageReceivedBinding;
import com.wornux.chatzam.databinding.ItemMessageSentBinding;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.data.enums.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OnMessageClickListener clickListener;
    @Setter
    private String currentUserId;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);

        void onMessageLongClick(Message message);
    }

    public MessageAdapter(String currentUserId) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        return message.getSenderId().equals(currentUserId) ? MessageViewType.SENT.getViewType() : MessageViewType.RECEIVED.getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == MessageViewType.SENT.getViewType()) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(inflater, parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(inflater, parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);

        if (holder instanceof SentMessageViewHolder sent)
            sent.bind(message, clickListener, timeFormat);
        else if (holder instanceof ReceivedMessageViewHolder received)
            received.bind(message, clickListener, timeFormat);
    }

    static class SentMessageViewHolder extends BaseMessageViewHolder {

        private final ItemMessageSentBinding binding;
        public SentMessageViewHolder(@NonNull ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected ImageView getMessageImageView() {
            return binding.messageImage;
        }

        @Override
        protected View getMessageTextView() {
            return binding.messageText;
        }

        @Override
        protected View getTimestampTextView() {
            return binding.timestampText;
        }

    }
    static class ReceivedMessageViewHolder extends BaseMessageViewHolder {

        private final ItemMessageReceivedBinding binding;
        public ReceivedMessageViewHolder(@NonNull ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected ImageView getMessageImageView() {
            return binding.messageImage;
        }

        @Override
        protected View getMessageTextView() {
            return binding.messageText;
        }

        @Override
        protected View getTimestampTextView() {
            return binding.timestampText;
        }

    }

    abstract static class BaseMessageViewHolder extends RecyclerView.ViewHolder {
        protected BaseMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract ImageView getMessageImageView();
        protected abstract View getMessageTextView();
        protected abstract View getTimestampTextView();

        public void bind(Message message, OnMessageClickListener listener, SimpleDateFormat timeFormat) {
            if (message.getMessageType() == MessageType.IMAGE && message.hasMedia()) {
                getMessageImageView().setVisibility(View.VISIBLE);
                getMessageTextView().setVisibility(View.GONE);

                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .centerCrop()
                        .into(getMessageImageView());
            } else {
                getMessageImageView().setVisibility(View.GONE);
                getMessageTextView().setVisibility(View.VISIBLE);
                ((android.widget.TextView) getMessageTextView()).setText(message.getContent());
            }

            if (message.getTimestamp() != null) {
                ((android.widget.TextView) getTimestampTextView()).setText(timeFormat.format(message.getTimestamp()));
            }

            setupClickListeners(message, listener);
        }

        private void setupClickListeners(Message message, OnMessageClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            if (listener != null) {
                itemView.setOnLongClickListener(v -> {
                    listener.onMessageLongClick(message);
                    return true;
                });
            } else {
                itemView.setOnLongClickListener(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Message>() {
                @Override
                public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    return oldItem.getMessageId().equals(newItem.getMessageId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    return Objects.equals(oldItem.getContent(), newItem.getContent())
                            && Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp())
                            && oldItem.isRead() == newItem.isRead()
                            && oldItem.isDelivered() == newItem.isDelivered();
                }
            };

    @Getter
    @RequiredArgsConstructor
    private enum MessageViewType {
        SENT(1), RECEIVED(2);
        private final int viewType;

    }
}
