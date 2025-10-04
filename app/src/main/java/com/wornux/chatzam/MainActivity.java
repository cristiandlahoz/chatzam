package com.wornux.chatzam;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.databinding.ActivityMainBinding;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import java.util.Objects;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration mAppBarConfiguration;
  private ActivityMainBinding binding;
  private NavController navController;

  @Inject AuthenticationManager authManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupBackPressHandler();
    setupNavigation();
    checkAuthenticationState();
  }

  private void setupBackPressHandler() {
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (!authManager.isUserLoggedIn()) {
                  finishAffinity();
                } else {
                  setEnabled(false);
                  getOnBackPressedDispatcher().onBackPressed();
                  setEnabled(true);
                }
              }
            });
  }

  private void checkAuthenticationState() {
    boolean isLoggedIn = authManager.isUserLoggedIn();

    if (!isLoggedIn) {
      NavOptions navOptions =
          new NavOptions.Builder().setPopUpTo(R.id.mobile_navigation, true).build();
      navController.navigate(R.id.authenticationFragment, null, navOptions);
    }
  }

  private void setupNavigation() {
    setSupportActionBar(binding.appBarMain.toolbar);

    NavigationView navigationView = binding.navView;

    mAppBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_home)
            .setOpenableLayout(binding.drawerLayout)
            .build();

    NavHostFragment navHostFragment =
        (NavHostFragment)
            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);

    if (navHostFragment != null) {
      navController = navHostFragment.getNavController();
      NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
      NavigationUI.setupWithNavController(navigationView, navController);

      navController.addOnDestinationChangedListener(
          (controller, destination, arguments) -> {
            boolean isAuthScreen = destination.getId() == R.id.authenticationFragment;
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!isAuthScreen);

            if (isAuthScreen) {
              binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
              binding.navView.setVisibility(View.GONE);
            } else if (authManager.isUserLoggedIn()) {
              binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
              binding.navView.setVisibility(View.VISIBLE);
            }
          });
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }
}
