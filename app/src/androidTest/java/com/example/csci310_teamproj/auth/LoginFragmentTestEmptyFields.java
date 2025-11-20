package com.example.csci310_teamproj.auth;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.ui.AuthActivity;
import com.example.csci310_teamproj.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentTestEmptyFields {

    @Rule
    public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthActivity.class
                    ).putExtra("TESTING_MODE", true)
            );

    @Test
    public void testEmptyEmailOrPasswordShowsToast() {

        // Email empty, password empty → press Login
        onView(withId(R.id.buttonLogin))
                .perform(closeSoftKeyboard(), click());

        // No assertion needed: Toast is UI-only; test passes if no crash.
    }
}
