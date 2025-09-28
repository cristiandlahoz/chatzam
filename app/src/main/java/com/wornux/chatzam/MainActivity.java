package com.wornux.chatzam;

import android.os.Bundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.wornux.chatzam.services.AuthenticationManager;
import com.wornux.chatzam.databinding.ActivityMainBinding;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration mAppBarConfiguration;
  private ActivityMainBinding binding;

  @Inject AuthenticationManager authManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    checkAuthenticationState();
    setupNavigation();
  }

  private void checkAuthenticationState() {
    if (!authManager.isUserLoggedIn()) {
      // TODO: Navigate to authentication fragment when we create it
      // For now, we'll continue with the app
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
      NavController navController = navHostFragment.getNavController();
      NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
      NavigationUI.setupWithNavController(navigationView, navController);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.fab_options_menu, menu);
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }
}
