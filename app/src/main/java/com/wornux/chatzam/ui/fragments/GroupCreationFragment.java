package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.databinding.FragmentGroupCreationBinding;
import com.wornux.chatzam.data.entities.UserProfile;
import com.wornux.chatzam.ui.adapters.SelectedUsersAdapter;
import com.wornux.chatzam.ui.adapters.UserSelectionAdapter;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.GroupChatViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GroupCreationFragment extends BaseFragment {
    
    private FragmentGroupCreationBinding binding;
    private GroupChatViewModel viewModel;
    private UserSelectionAdapter userSelectionAdapter;
    private SelectedUsersAdapter selectedUsersAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GroupChatViewModel.class);
        setupRecyclerViews();
        setupSearchListener();
        super.onViewCreated(view, savedInstanceState);
    }
    
    private void setupRecyclerViews() {
        userSelectionAdapter = new UserSelectionAdapter();
        binding.availableUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.availableUsersRecyclerView.setAdapter(userSelectionAdapter);
        
        selectedUsersAdapter = new SelectedUsersAdapter();
        binding.selectedMembersRecyclerView.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.selectedMembersRecyclerView.setAdapter(selectedUsersAdapter);
        
        userSelectionAdapter.setOnUserSelectionListener(new UserSelectionAdapter.OnUserSelectionListener() {
            @Override
            public void onUserSelected(UserProfile user) {
                viewModel.addUserToSelection(user);
                selectedUsersAdapter.addUser(user);
            }
            
            @Override
            public void onUserDeselected(UserProfile user) {
                viewModel.removeUserFromSelection(user);
                selectedUsersAdapter.removeUser(user);
            }
        });
        
        selectedUsersAdapter.setOnUserRemoveListener(user -> {
            viewModel.removeUserFromSelection(user);
            userSelectionAdapter.notifyDataSetChanged();
        });
    }
    
    private void setupSearchListener() {
        binding.searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.searchUsers(s.toString());
            }
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
            if (users != null) {
                selectedUsersAdapter.updateSelectedUsers(users);
                binding.selectedMembersLabel.setText("Selected Members (" + users.size() + ")");
            }
        });
        
        viewModel.canCreateGroup().observe(getViewLifecycleOwner(), canCreate -> {
            binding.createGroupButton.setEnabled(canCreate != null && canCreate);
        });
        
        viewModel.isGroupCreated().observe(getViewLifecycleOwner(), isCreated -> {
            if (isCreated != null && isCreated) {
                showSnackbar("Group created successfully!");
                getNavController().popBackStack();
            }
        });
        
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.createGroupButton.setEnabled(!isLoading);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }
    
    @Override
    protected void setupClickListeners() {
        binding.createGroupButton.setOnClickListener(v -> createGroup());
        
        binding.groupImageView.setOnClickListener(v -> {
            showSnackbar("Image selection coming soon!");
        });
    }
    
    private void createGroup() {
        String groupName = binding.groupNameEditText.getText() != null ? 
                          binding.groupNameEditText.getText().toString().trim() : "";
        
        if (groupName.isEmpty()) {
            binding.groupNameEditText.setError("Group name is required");
            return;
        }
        
        viewModel.createGroup(groupName);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}