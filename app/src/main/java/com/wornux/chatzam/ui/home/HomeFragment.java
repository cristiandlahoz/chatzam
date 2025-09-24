package com.wornux.chatzam.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentChatListBinding;
import com.wornux.chatzam.domain.entities.Chat;
import com.wornux.chatzam.presentation.adapters.ChatListAdapter;
import com.wornux.chatzam.presentation.base.BaseFragment;
import com.wornux.chatzam.presentation.viewmodels.ChatListViewModel;

public class HomeFragment extends BaseFragment implements ChatListAdapter.OnChatClickListener {

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;
    private ChatListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter();
        adapter.setOnChatClickListener(this);
        
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatsRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void setupObservers() {
        viewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            if (chats != null) {
                adapter.updateChats(chats);
            }
        });

        viewModel.isEmpty().observe(getViewLifecycleOwner(), isEmpty -> {
            binding.emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.chatsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    @Override
    protected void setupClickListeners() {
        // Click listeners are handled by the adapter
    }

    @Override
    public void onChatClick(Chat chat) {
        // TODO: Navigate to chat fragment when we implement it
        showSnackbar("Opening chat: " + (chat.isGroup() ? chat.getGroupName() : "Private Chat"));
    }

    @Override
    public void onChatLongClick(Chat chat) {
        // TODO: Show chat options (delete, mute, etc.)
        showSnackbar("Long clicked chat: " + chat.getChatId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}