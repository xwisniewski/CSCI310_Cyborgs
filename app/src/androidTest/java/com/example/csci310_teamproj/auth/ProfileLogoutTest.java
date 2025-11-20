package com.example.csci310_teamproj.auth;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.ui.AuthActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileLogoutTest {

    @Rule
    public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthActivity.class
                    ).putExtra("TESTING_MODE", true)
            );

    @Test
    public void testLogoutNavigatesBackToLoginFragment() throws InterruptedException {

        // --- LOGIN ---
        onView(withId(R.id.editTextEmailLogin))
                .perform(typeText("xwisniew@usc.edu"));
        closeSoftKeyboard();

        onView(withId(R.id.editTextPasswordLogin))
                .perform(typeText("password"));
        closeSoftKeyboard();

        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for MainActivity to load
        Thread.sleep(900);

        // Confirm bottomNav exists (meaning we are in MainActivity)
        onView(withId(R.id.bottomNav)).check(matches(isDisplayed()));

        // --- STEP 1: Navigate to Profile tab ---
        onView(withContentDescription("Profile")).perform(click());

        // --- STEP 2: Ensure logout button shows ---
        onView(withId(R.id.buttonLogout))
                .check(matches(isDisplayed()));

        // --- STEP 3: Click logout ---
        onView(withId(R.id.buttonLogout)).perform(click());

        // Give AuthActivity time to relaunch LoginFragment
        Thread.sleep(700);

        // --- STEP 4: Verify we are back on LoginFragment ---
        onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()));
    }
}
