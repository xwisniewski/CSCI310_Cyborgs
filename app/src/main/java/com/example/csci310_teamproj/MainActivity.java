package com.example.csci310_teamproj;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ make sure this matches res/layout/activity_main.xml

        // 🚀 TEMP: Seed Firebase with sample data ONCE for testing
        // (You can remove or comment this out after confirming the database looks correct)
        //InitFirebaseData.seedTestData();

        // 🔹 Find the NavHostFragment from the layout
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            // 🔹 Get the NavController from the NavHostFragment
            NavController navController = navHostFragment.getNavController();

            // 🔹 Connect the BottomNavigationView to the NavController
            BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
            NavigationUI.setupWithNavController(bottomNav, navController);
        } else {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml layout ID.");
        }
    }
}
