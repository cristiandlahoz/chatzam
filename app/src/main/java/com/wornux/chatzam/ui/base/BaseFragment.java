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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BaseFragment<VM extends ViewModel> extends Fragment {

  protected VM viewModel;

  protected abstract void setupObservers();

  protected abstract void setupClickListeners();

  protected abstract Class<VM> getViewModelClass();

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(getViewModelClass());
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
    if (view != null) Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_SHORT).show();
  }

  protected void showError(String error) {
    showSnackbar("Error: " + error);
  }
}
