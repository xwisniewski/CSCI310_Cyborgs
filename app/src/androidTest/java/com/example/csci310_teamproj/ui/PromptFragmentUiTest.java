package com.example.csci310_teamproj.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Root;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Prompt;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class PromptFragmentUiTest {

    private FragmentScenario<PromptFragment> scenario;

    @Before
    public void setUp() {
        scenario = launchScenarioWithData(defaultPrompts());
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void searchField_filtersPromptsByTitle() {
        onView(withId(R.id.editTextSearch))
                .perform(replaceText("budget"), closeSoftKeyboard());

        onView(withText("Budget Planner")).check(matches(isDisplayed()));
        onView(withText("Daily Journal")).check(doesNotExist());
    }

    @Test
    public void favoritesRadio_showsOnlyFavoritePrompts() {
        scenario.onFragment(fragment -> {
            try {
                Set<String> favorites = new HashSet<>();
                favorites.add("prompt-1");
                setField(fragment, "favoriteIds", favorites);

                com.example.csci310_teamproj.ui.adapter.PromptAdapter adapter =
                        (com.example.csci310_teamproj.ui.adapter.PromptAdapter) getField(fragment, "promptAdapter");
                adapter.setFavorites(favorites);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.radioFavorites)).perform(click());

        onView(withText("Budget Planner")).check(matches(isDisplayed()));
        onView(withText("Daily Journal")).check(doesNotExist());
    }

    @Test
    public void filterDialog_filtersBySelectedLlm() {
        onView(withId(R.id.fabFilter)).perform(click());

        onView(withText("Claude-3")).perform(click());
        onView(withId(R.id.buttonApplyFilter)).perform(click());

        onView(withText("Claude Strategy")).check(matches(isDisplayed()));
        onView(withText("Budget Planner")).check(doesNotExist());
    }

    @Test
    public void emptyState_visibleWhenNoPromptsRemain() {
        scenario.onFragment(fragment -> {
            try {
                List<Prompt> all = getListField(fragment, "allPrompts");
                all.clear();
                List<Prompt> visible = getListField(fragment, "prompts");
                visible.clear();
                invokeApplyFilter(fragment);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.layoutEmptyState))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.recyclerViewPrompts))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void tappingPrompt_showsTitleToast() {
        onView(withId(R.id.recyclerViewPrompts))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withText("Budget Planner"))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    private FragmentScenario<PromptFragment> launchScenarioWithData(List<Prompt> prompts) {
        FragmentScenario<PromptFragment> scenario =
                FragmentScenario.launchInContainer(PromptFragment.class, new Bundle(), R.style.Theme_CSCI310_TeamProj, null);
        scenario.onFragment(fragment -> {
            try {
                List<Prompt> all = getListField(fragment, "allPrompts");
                all.clear();
                all.addAll(prompts);
                List<Prompt> visible = getListField(fragment, "prompts");
                visible.clear();
                invokeApplyFilter(fragment);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return scenario;
    }

    private List<Prompt> defaultPrompts() {
        List<Prompt> prompts = new ArrayList<>();
        prompts.add(buildPrompt("prompt-1", "Budget Planner", "GPT-4"));
        prompts.add(buildPrompt("prompt-2", "Daily Journal", "GPT-4"));
        prompts.add(buildPrompt("prompt-3", "Claude Strategy", "Claude-3"));
        return prompts;
    }

    private Prompt buildPrompt(String id, String title, String llm) {
        Prompt prompt = new Prompt();
        prompt.setId(id);
        prompt.setTitle(title);
        prompt.setDescription("desc");
        prompt.setPromptText("body");
        prompt.setLlmTag(llm);
        prompt.setUserId("user-" + id);
        return prompt;
    }

    @SuppressWarnings("unchecked")
    private List<Prompt> getListField(PromptFragment fragment, String name) throws Exception {
        Field field = PromptFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        return (List<Prompt>) field.get(fragment);
    }

    private Object getField(PromptFragment fragment, String name) throws Exception {
        Field field = PromptFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(fragment);
    }

    private void setField(PromptFragment fragment, String name, Object value) throws Exception {
        Field field = PromptFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }

    private void invokeApplyFilter(PromptFragment fragment) throws Exception {
        Method method = PromptFragment.class.getDeclaredMethod("applyFilter");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    /** Matches Toast windows so we can assert prompt titles are surfaced. */
    private static class ToastMatcher extends TypeSafeMatcher<Root> {

        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if (type == android.view.WindowManager.LayoutParams.TYPE_TOAST) {
                View decorView = root.getDecorView();
                return decorView.getWindowToken() == decorView.getApplicationWindowToken();
            }
            return false;
        }
    }
}

