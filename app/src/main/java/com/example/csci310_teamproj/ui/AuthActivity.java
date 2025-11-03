package com.example.csci310_teamproj.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csci310_teamproj.R;

/**
 * Handles authentication flow — hosts the RegisterFragment (and later LoginFragment).
 */
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Load RegisterFragment by default
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
        Intent intent = new Intent(this, com.example.csci310_teamproj.MainActivity.class);
        startActivity(intent);
        finish();
    }
}
