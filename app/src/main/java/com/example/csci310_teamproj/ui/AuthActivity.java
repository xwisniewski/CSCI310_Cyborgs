package com.example.csci310_teamproj.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csci310_teamproj.MainActivity;
import com.example.csci310_teamproj.R;

/**
 * Handles authentication flow — hosts the RegisterFragment (and later LoginFragment).
 */
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Load LoginFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }

    }

    /**
     * Navigate to main app after registration/login success.
     */
    public void openMainApp() {

        boolean isTest = getIntent().getBooleanExtra("TESTING_MODE", false);

        Intent intent;

        if (isTest) {
            // Skip loading screen entirely in tests
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("TESTING_MODE", true);
        } else {
            // Normal user flow
            intent = new Intent(this, WelcomeLoadingActivity.class);
        }

        startActivity(intent);

        // Only finish AuthActivity during real app use
        if (!isTest) {
            finish();
        }

        overridePendingTransition(0, 0);
    }

}
