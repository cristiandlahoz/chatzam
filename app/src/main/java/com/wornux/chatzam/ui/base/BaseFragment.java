package com.wornux.chatzam.ui.base;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BaseFragment extends Fragment {
    
    protected abstract void setupObservers();
    protected abstract void setupClickListeners();
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        setupClickListeners();
    }
    
    protected NavController getNavController() {
        return Navigation.findNavController(requireView());
    }
    
    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        return new ViewModelProvider(this).get(viewModelClass);
    }
    
    protected void showSnackbar(String message) {
        View view = getView();
        if (view != null) {
            com.google.android.material.snackbar.Snackbar.make(view, message, 
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        }
    }
    
    protected void showError(String error) {
        showSnackbar("Error: " + error);
    }
}