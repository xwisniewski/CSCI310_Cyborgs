package com.example.csci310_teamproj;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 TESTING-MODE OVERRIDE
        // If the test injected TESTING_MODE = true, skip login check
        boolean testing = getIntent().getBooleanExtra("TESTING_MODE", false);

        // 🔹 STEP 1 — Redirect to AuthActivity ONLY if not testing
        if (!testing && FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, com.example.csci310_teamproj.ui.AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 🔹 STEP 2 — Load Main UI
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
            NavigationUI.setupWithNavController(bottomNav, navController);
        } else {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml layout ID.");
        }
    }
}
