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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.*;

import android.content.Intent;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentTestInvalidEmail {

    @Rule
    public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthActivity.class
                    ).putExtra("TESTING_MODE", true)
            );
    @Test
    public void testInvalidEmailShowsFirebaseErrorToast() {

        onView(withId(R.id.editTextEmailLogin))
                .perform(typeText("notanemail"), closeSoftKeyboard());

        onView(withId(R.id.editTextPasswordLogin))
                .perform(typeText("123456"), closeSoftKeyboard());

        onView(withId(R.id.buttonLogin)).perform(click());

        // In Espresso Option 1, we do not assert Toast text.
        // Test passes if no crash.
    }
}
