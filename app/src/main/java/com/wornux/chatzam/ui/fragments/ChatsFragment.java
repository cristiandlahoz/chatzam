package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentChatsBinding;
import com.wornux.chatzam.data.entities.Chat;
import com.wornux.chatzam.ui.adapters.ChatListAdapter;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.ChatListViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatsFragment extends BaseFragment<ChatListViewModel> implements ChatListAdapter.OnChatClickListener {

  private FragmentChatsBinding binding;
  private ChatListAdapter adapter;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentChatsBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
    viewModel
        .getChats()
        .observe(
            getViewLifecycleOwner(),
            chats -> {
              if (chats != null) {
                adapter.submitList(chats);
              }
            });

    viewModel
        .isEmpty()
        .observe(
            getViewLifecycleOwner(),
            isEmpty -> {
              binding.emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
              binding.chatsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            });

    viewModel
        .getLoading()
        .observe(
            getViewLifecycleOwner(),
            isLoading -> {
              binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });

    viewModel
        .getError()
        .observe(
            getViewLifecycleOwner(),
            error -> {
              if (error != null) {
                showError(error);
              }
            });
  }

  @Override
  protected void setupClickListeners() {
    binding.createGroupFab.setOnClickListener(
        v ->
            getNavController()
                .navigate(com.wornux.chatzam.R.id.action_nav_home_to_nav_group_creation));
  }

    @Override
    protected Class<ChatListViewModel> getViewModelClass() {
      return ChatListViewModel.class;
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
