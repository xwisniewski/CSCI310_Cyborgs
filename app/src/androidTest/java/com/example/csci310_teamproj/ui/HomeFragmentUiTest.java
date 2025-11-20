package com.example.csci310_teamproj.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.util.AnimationDisabler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeFragmentUiTest {

    private FragmentScenario<HomeFragment> scenario;

    @Before
    public void setUp() {
        AnimationDisabler.disableAnimations();
        scenario = launchScenarioWithData(null);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        AnimationDisabler.enableAnimations();
    }

    @Test
    public void searchField_isDisplayedAndEditable() {
        // Verify search field is displayed
        onView(withId(R.id.editTextSearch)).check(matches(isDisplayed()));
        
        // Verify search field can receive text input
        onView(withId(R.id.editTextSearch))
                .perform(replaceText("test query"), closeSoftKeyboard());
        
        // Verify RecyclerView is still displayed after text input
        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void searchModeRadioButtons_areClickable() {
        // Verify all radio buttons are displayed and clickable
        onView(withId(R.id.radioSearchTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.radioSearchAuthor)).check(matches(isDisplayed()));
        onView(withId(R.id.radioSearchContent)).check(matches(isDisplayed()));
        
        // Test that radio buttons can be clicked
        onView(withId(R.id.radioSearchAuthor)).perform(click());
        onView(withId(R.id.radioSearchContent)).perform(click());
        onView(withId(R.id.radioSearchTitle)).perform(click());
        
        // Verify RecyclerView is still displayed after clicking
        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void filterFab_opensFilterDialog() {
        // Verify filter FAB is displayed
        onView(withId(R.id.fabFilter)).check(matches(isDisplayed()));
        
        // Click filter FAB to open dialog
        onView(withId(R.id.fabFilter)).perform(click());
        
        // Wait a bit for dialog to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify apply filter button exists (confirms dialog opened)
        onView(withId(R.id.buttonApplyFilter)).check(matches(isDisplayed()));
    }

    @Test
    public void createPostFab_opensCreatePostDialog() {
        onView(withId(R.id.fabCreatePost)).perform(click());

        onView(withId(R.id.editPostTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.editPostTag)).check(matches(isDisplayed()));
        onView(withId(R.id.editPostBody)).check(matches(isDisplayed()));
    }

    @Test
    public void recyclerView_isDisplayed() {
        // Verify RecyclerView is displayed
        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()));
    }

    private FragmentScenario<HomeFragment> launchScenarioWithData(Object unused) {
        // Simply launch the fragment - no complex setup needed for simple UI tests
        return FragmentScenario.launchInContainer(
                HomeFragment.class, 
                new Bundle(), 
                R.style.Theme_CSCI310_TeamProj, 
                (FragmentFactory) null);
    }

}

