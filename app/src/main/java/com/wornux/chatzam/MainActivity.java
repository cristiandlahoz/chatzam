package com.wornux.chatzam;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.wornux.chatzam.databinding.ActivityMainBinding;
import com.wornux.chatzam.data.services.AuthenticationManager;
import com.wornux.chatzam.presentation.fragments.GroupCreationFragment;
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
    binding.appBarMain.fab.setOnClickListener( view -> {
              android.widget.PopupMenu popup = new android.widget.PopupMenu(MainActivity.this, view);
              popup.getMenuInflater().inflate(R.menu.fab_options_menu, popup.getMenu());
              popup.setOnMenuItemClickListener(item -> {
                  int id = item.getItemId();
                  if (id == R.id.action_new_group_chat) {
                      GroupCreationFragment groupCreationFragment = new GroupCreationFragment();
                      getSupportFragmentManager()
                              .beginTransaction()
                              .replace(R.id.nav_host_fragment_content_main, groupCreationFragment)
                              .addToBackStack(null)
                              .commit();
                      return true;
                  } else if (id == R.id.action_new_one_to_one_chat) {
                      // Handle new one-to-one chat
                      return true;
                  } else if (id == R.id.action_add_contact) {
                      // Handle add contact
                      return true;
                  }
                  return false;
              });
              popup.show();
        });

    DrawerLayout drawer = binding.drawerLayout;
    NavigationView navigationView = binding.navView;

    mAppBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
            .setOpenableLayout(drawer)
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
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
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
