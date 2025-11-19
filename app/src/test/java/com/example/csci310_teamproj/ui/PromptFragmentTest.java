package com.example.csci310_teamproj.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.ui.adapter.PromptAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class PromptFragmentTest {

    private PromptFragment fragment;
    private List<Prompt> allPrompts;
    private List<Prompt> visiblePrompts;
    private PromptAdapter adapter;

    @Before
    public void setUp() throws Exception {
        fragment = new PromptFragment();

        Context context = ApplicationProvider.getApplicationContext();
        RecyclerView recyclerView = new RecyclerView(context);
        LinearLayout emptyState = new LinearLayout(context);

        adapter = Mockito.mock(PromptAdapter.class);

        setField("recyclerViewPrompts", recyclerView);
        setField("layoutEmptyState", emptyState);
        setField("promptAdapter", adapter);

        allPrompts = new ArrayList<>();
        visiblePrompts = new ArrayList<>();
        setField("allPrompts", allPrompts);
        setField("prompts", visiblePrompts);
        setField("selectedLlms", new HashSet<>(Arrays.asList("All")));
        setField("favoriteIds", new HashSet<>());
        setField("searchQuery", "");
        setField("showFavoritesOnly", false);
    }

    @Test
    public void applyFilter_keepsAllPromptsWhenNoCriteria() throws Exception {
        seedPrompts(prompt("1", "Budget Planner", "GPT-4"),
                prompt("2", "Daily Journal", "Claude-3"));

        invokeApplyFilter();

        assertEquals(2, visiblePrompts.size());
        Mockito.verify(adapter).updatePrompts(visiblePrompts);
    }

    @Test
    public void applyFilter_filtersByLlmTag() throws Exception {
        seedPrompts(prompt("1", "Budget Planner", "GPT-4"),
                prompt("2", "Daily Journal", "Claude-3"));

        setField("selectedLlms", new HashSet<>(Arrays.asList("Claude-3")));

        invokeApplyFilter();

        assertEquals(1, visiblePrompts.size());
        assertEquals("Daily Journal", visiblePrompts.get(0).getTitle());
    }

    @Test
    public void applyFilter_filtersBySearchQuery() throws Exception {
        seedPrompts(prompt("1", "Budget Planner", "GPT-4"),
                prompt("2", "Daily Journal", "Claude-3"));

        setField("searchQuery", "budget");

        invokeApplyFilter();

        assertEquals(1, visiblePrompts.size());
        assertEquals("Budget Planner", visiblePrompts.get(0).getTitle());
    }

    @Test
    public void applyFilter_filtersByFavoritesFlag() throws Exception {
        seedPrompts(prompt("1", "Budget Planner", "GPT-4"),
                prompt("2", "Daily Journal", "Claude-3"));

        Set<String> favorites = new HashSet<>();
        favorites.add("2");
        setField("favoriteIds", favorites);
        setField("showFavoritesOnly", true);

        invokeApplyFilter();

        assertEquals(1, visiblePrompts.size());
        assertEquals("Daily Journal", visiblePrompts.get(0).getTitle());
    }

    @Test
    public void applyFilter_combinesAllCriteria() throws Exception {
        seedPrompts(prompt("1", "Budget Planner", "GPT-4"),
                prompt("2", "Daily Journal", "Claude-3"),
                prompt("3", "Budget Claude Strategy", "Claude-3"));

        Set<String> favorites = new HashSet<>();
        favorites.add("3");

        setField("favoriteIds", favorites);
        setField("showFavoritesOnly", true);
        setField("selectedLlms", new HashSet<>(Arrays.asList("Claude-3")));
        setField("searchQuery", "budget");

        invokeApplyFilter();

        assertEquals(1, visiblePrompts.size());
        assertEquals("Budget Claude Strategy", visiblePrompts.get(0).getTitle());
        assertTrue(visiblePrompts.get(0).getLlmTag().equals("Claude-3"));
    }

    private Prompt prompt(String id, String title, String llmTag) {
        Prompt prompt = new Prompt();
        prompt.setId(id);
        prompt.setTitle(title);
        prompt.setLlmTag(llmTag);
        prompt.setDescription("desc");
        prompt.setPromptText("text");
        prompt.setUserId("user-" + id);
        return prompt;
    }

    private void seedPrompts(Prompt... prompts) {
        allPrompts.clear();
        visiblePrompts.clear();
        allPrompts.addAll(Arrays.asList(prompts));
    }

    private void invokeApplyFilter() throws Exception {
        Method method = PromptFragment.class.getDeclaredMethod("applyFilter");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = PromptFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }
}

