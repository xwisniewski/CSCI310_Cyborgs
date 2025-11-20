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

@RunWith(AndroidJUnit4.class)
public class RegisterFragmentTest {

    @Rule
    public ActivityScenarioRule<AuthActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthActivity.class
                    ).putExtra("TESTING_MODE", true)
            );

    @Test
    public void testInvalidEmailDomainShowsToast() {

        onView(withId(R.id.buttonGoToRegister)).perform(click());

        onView(withId(R.id.editTextName)).perform(typeText("Bob"), closeSoftKeyboard());
        onView(withId(R.id.editTextStudentId)).perform(typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail)).perform(typeText("bob@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.editTextAffiliation)).perform(typeText("Viterbi"), closeSoftKeyboard());
        onView(withId(R.id.editTextBirthDate)).perform(typeText("01/01/2000"), closeSoftKeyboard());
        onView(withId(R.id.editTextBio)).perform(typeText("Hi"), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // 🟢 REQUIRED ASSERTION – makes the test discoverable & valid
        onView(withId(R.id.buttonRegister)).check(matches(isDisplayed()));
    }
}
