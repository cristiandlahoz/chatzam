package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentChatBinding;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.ui.adapters.MessageAdapter;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.ChatViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends BaseFragment<ChatViewModel> {
    
    private FragmentChatBinding binding;
    private MessageAdapter messageAdapter;
    
    private static final String ARG_CHAT_ID = "chat_id";
    
    public static ChatFragment newInstance(String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setHasOptionsMenu(true);
        
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        String chatId = getArguments() != null ? getArguments().getString(ARG_CHAT_ID) : null;
        if (chatId != null) {
            viewModel.setChatId(chatId);
        }
        
        setupRecyclerView();
    }
    
    private void setupRecyclerView() {
        String currentUserId = viewModel.getCurrentUserId();
        messageAdapter = new MessageAdapter(currentUserId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        
        binding.messagesRecyclerView.setLayoutManager(layoutManager);
        binding.messagesRecyclerView.setAdapter(messageAdapter);
        
        messageAdapter.setOnMessageClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message message) {
                if (!viewModel.isMessageFromCurrentUser(message) && !message.isRead()) {
                    viewModel.markMessageAsRead(message.getMessageId());
                }
            }
            
            @Override
            public void onMessageLongClick(Message message) {
            }
        });
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                messageAdapter.updateMessages(messages);
                scrollToBottom();
            }
        });
        
        viewModel.isEmpty().observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
            }
        });
        
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }
    
    @Override
    protected void setupClickListeners() {
        binding.sendButton.setOnClickListener(v -> sendMessage());
        
        binding.messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    @Override
    protected Class<ChatViewModel> getViewModelClass() {
        return ChatViewModel.class;
    }

    private void sendMessage() {
        String message = binding.messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            viewModel.sendMessage(message);
            binding.messageEditText.setText("");
            scrollToBottom();
        }
    }
    
    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            binding.messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}