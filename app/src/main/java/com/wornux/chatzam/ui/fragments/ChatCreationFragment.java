package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentChatCreationBinding;
import com.wornux.chatzam.data.entities.User;
import com.wornux.chatzam.ui.adapters.SingleUserSelectionAdapter;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.ChatCreationViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import com.wornux.chatzam.services.AuthenticationManager;
import javax.inject.Inject;

@AndroidEntryPoint
public class ChatCreationFragment extends BaseFragment<ChatCreationViewModel> {

    @Inject
    AuthenticationManager authenticationManager;

    private FragmentChatCreationBinding binding;
    private SingleUserSelectionAdapter userSelectionAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchView();
        setupObservers();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        userSelectionAdapter = new SingleUserSelectionAdapter();
        binding.availableUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.availableUsersRecyclerView.setAdapter(userSelectionAdapter);

        userSelectionAdapter.setOnUserSelectionListener(new SingleUserSelectionAdapter.OnUserSelectionListener() {
            @Override
            public void onUserSelected(User user) {
                viewModel.addUserToSelection(user);
            }

            @Override
            public void onUserDeselected(User user) {
                viewModel.removeUserFromSelection(user);
            }
        });
    }

    private void setupSearchView() {
        binding.searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }


    @Override
    protected void setupObservers() {
        viewModel.getAvailableUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userSelectionAdapter.updateUsers(users);
            }
        });

        viewModel.getSelectedUsers().observe(getViewLifecycleOwner(), users -> {
            binding.createChatButton.setEnabled(users != null && !users.isEmpty());
            if (users != null && !users.isEmpty()) {
                userSelectionAdapter.setSelectedUser(users.get(0));
            } else {
                userSelectionAdapter.setSelectedUser(null);
            }
        });
    }

    @Override
    protected void setupClickListeners() {
        binding.createChatButton.setOnClickListener(v -> {
            String currentUserId = authenticationManager.getCurrentUser().getUid();
            viewModel.createChat(currentUserId, task -> {
                if (task.isSuccessful()) {
                    getNavController().popBackStack();
                }
            });
        });
    }

    @Override
    protected Class<ChatCreationViewModel> getViewModelClass() {
        return ChatCreationViewModel.class;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}