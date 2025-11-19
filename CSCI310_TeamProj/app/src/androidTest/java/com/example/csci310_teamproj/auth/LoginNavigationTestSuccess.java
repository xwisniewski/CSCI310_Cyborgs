package com.example.csci310_teamproj.auth;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.ui.AuthActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;


// 🚀 IMPORTANT:
// Your app navigates using Navigation Component, NOT startActivity()
// → Espresso Intents CANNOT detect that.
// So we detect login success by verifying MainActivity UI appears.

@RunWith(AndroidJUnit4.class)
public class LoginNavigationTestSuccess {

    @Rule
    public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthActivity.class
                    ).putExtra("TESTING_MODE", true)   // bypass redirects
            );

    @Test
    public void testSuccessfulLoginNavigatesToMain() throws InterruptedException {

        // Fill login fields
        onView(withId(R.id.editTextEmailLogin))
                .perform(typeText("xwisniew@usc.edu"), closeSoftKeyboard());

        onView(withId(R.id.editTextPasswordLogin))
                .perform(typeText("password"), closeSoftKeyboard());

        // Press Login
        onView(withId(R.id.buttonLogin)).perform(click());

        // WAIT for MainActivity to load
        Thread.sleep(900);

        // ✔ Instead of checking Intents, check MainActivity UI actually loaded
        // Replace this with an ID from MainActivity (root container, toolbar, etc.)
        onView(withId(R.id.bottomNav))
                .check(matches(isDisplayed()));
    }
}
