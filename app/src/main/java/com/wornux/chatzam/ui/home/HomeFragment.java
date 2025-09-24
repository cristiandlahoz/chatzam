package com.wornux.chatzam.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentHomeBinding;
import com.wornux.chatzam.domain.entities.Chat;
import com.wornux.chatzam.presentation.adapters.ChatListAdapter;
import com.wornux.chatzam.presentation.base.BaseFragment;
import com.wornux.chatzam.presentation.viewmodels.ChatListViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends BaseFragment implements ChatListAdapter.OnChatClickListener {

    private FragmentHomeBinding binding;
    private ChatListViewModel viewModel;
    private ChatListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
        setupRecyclerView();
        super.onViewCreated(view, savedInstanceState);
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
        binding.createGroupFab.setOnClickListener(v -> {
            getNavController().navigate(com.wornux.chatzam.R.id.action_nav_home_to_nav_group_creation);
        });
    }

    @Override
    public void onChatClick(Chat chat) {
        Bundle args = new Bundle();
        args.putString("chat_id", chat.getChatId());
        getNavController().navigate(com.wornux.chatzam.R.id.action_nav_home_to_nav_chat, args);
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