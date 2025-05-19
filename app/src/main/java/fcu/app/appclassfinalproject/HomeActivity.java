package fcu.app.appclassfinalproject;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import fcu.app.appclassfinalproject.main_fragments.AddFragment;
import fcu.app.appclassfinalproject.main_fragments.HomeFragment;
import fcu.app.appclassfinalproject.main_fragments.SettingsFragment;

public class HomeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_home);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
    Fragment homeFrag = HomeFragment.newInstance("", "");
    Fragment addFrag = AddFragment.newInstance("", "");
    Fragment settingsFrag = SettingsFragment.newInstance("", "");

    setCurrentFragment(homeFrag);

    bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_Home) {
          setCurrentFragment(homeFrag);
        } else if (item.getItemId() == R.id.menu_Add) {
          setCurrentFragment(addFrag);
        } else {
          setCurrentFragment(settingsFrag);
        }
        return true;
      }


    });

  }

  private void setCurrentFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_main, fragment)
        .commit();
  }
}