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

import fcu.app.appclassfinalproject.main_fragments.AddIssueFragment;
import fcu.app.appclassfinalproject.main_fragments.HomeFragment;
import fcu.app.appclassfinalproject.main_fragments.ProjectInfoFragment;
import fcu.app.appclassfinalproject.main_fragments.SettingsFragment;

public class ProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_project);
        Fragment homeFragment = HomeFragment.newInstance("", "");
        Fragment projectInfoFragment = ProjectInfoFragment.newInstance("", "");
        Fragment settingsFragment = SettingsFragment.newInstance("", "");
        Fragment addIssueFragment = AddIssueFragment.newInstance("","");
        setCurrentFragment(projectInfoFragment);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_project_issues) {
                    setCurrentFragment(projectInfoFragment);
                } else if (item.getItemId() == R.id.menu_projct_back) {
                    setCurrentFragment(homeFragment);
                    finish();
                } else if (item.getItemId() == R.id.menu_project_add) {
                    setCurrentFragment(addIssueFragment);
                } else if (item.getItemId() == R.id.menu_project_setting) {
                    setCurrentFragment(settingsFragment);
                }
                return true;
            }
        });

    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_main_project, fragment)
                .commit();
    }
}