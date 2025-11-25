package com.example.csci310_teamproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csci310_teamproj.MainActivity;
import com.example.csci310_teamproj.R;

/**
 * Simple branded welcome experience shown immediately after login/registration.
 */
public class WelcomeLoadingActivity extends AppCompatActivity {

    private static final long LOADING_DURATION_MS = 3_000L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_loading);

        TextView title = findViewById(R.id.textWelcomeTitle);
        TextView subtitle = findViewById(R.id.textWelcomeSubtitle);

        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_pulse);
        title.startAnimation(pulseAnimation);
        subtitle.startAnimation(pulseAnimation);

        // Transition to the main experience after the short welcome animation finishes.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(WelcomeLoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, LOADING_DURATION_MS);
    }
}

