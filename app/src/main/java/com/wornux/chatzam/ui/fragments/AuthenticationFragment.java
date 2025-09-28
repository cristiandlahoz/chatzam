package com.wornux.chatzam.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.wornux.chatzam.databinding.FragmentAuthenticationBinding;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.AuthenticationViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthenticationFragment extends BaseFragment {
    
    private FragmentAuthenticationBinding binding;
    private AuthenticationViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthenticationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    protected void setupObservers() {
    viewModel
        .getLoading()
        .observe(
            getViewLifecycleOwner(),
            isLoading -> {
              binding.progressBar.setVisibility(
                  Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
              binding.loginButton.setEnabled(!isLoading);
              binding.registerButton.setEnabled(!isLoading);
            });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
        
        viewModel.getIsLoginMode().observe(getViewLifecycleOwner(), this::updateUIMode);
        
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                showSnackbar("Login successful!");
                navigateToMain();
            }
        });
        
        viewModel.getRegistrationResult().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                showSnackbar("Registration successful!");
                navigateToMain();
            }
        });
    }
    
    @Override
    protected void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> performLogin());
        binding.registerButton.setOnClickListener(v -> performRegistration());
        binding.switchModeText.setOnClickListener(v -> viewModel.switchMode());
    }
    
    private void performLogin() {
        String email = getTextFromEditText(binding.emailEditText);
        String password = getTextFromEditText(binding.passwordEditText);
        
        viewModel.login(email, password);
    }
    
    private void performRegistration() {
        String email = getTextFromEditText(binding.emailEditText);
        String password = getTextFromEditText(binding.passwordEditText);
        String displayName = getTextFromEditText(binding.displayNameEditText);
        
        viewModel.register(email, password, displayName);
    }
    
    private void updateUIMode(boolean isLoginMode) {
        if (isLoginMode) {
            binding.displayNameInputLayout.setVisibility(View.GONE);
            binding.loginButton.setVisibility(View.VISIBLE);
            binding.registerButton.setVisibility(View.GONE);
            binding.switchModeText.setText("Don't have an account? Register here");
        } else {
            binding.displayNameInputLayout.setVisibility(View.VISIBLE);
            binding.loginButton.setVisibility(View.GONE);
            binding.registerButton.setVisibility(View.VISIBLE);
            binding.switchModeText.setText("Already have an account? Login here");
        }
    }
    
    private void navigateToMain() {
        // TODO: Navigate to chat list when we implement it
        // For now, just finish the activity
        requireActivity().finish();
    }
    
    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}