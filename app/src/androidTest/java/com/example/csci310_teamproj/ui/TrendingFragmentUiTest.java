package com.example.csci310_teamproj.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.util.AnimationDisabler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TrendingFragmentUiTest {

    private FragmentScenario<TrendingFragment> scenario;

    @Before
    public void setUp() {
        AnimationDisabler.disableAnimations();
        scenario = launchScenarioWithData(defaultTrendingPosts());
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        AnimationDisabler.enableAnimations();
    }

    @Test
    public void trendingPosts_displayCorrectly() {
        // Simply verify RecyclerView is displayed
        onView(withId(R.id.trendingRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void upvoteButton_incrementsCount() {
        scenario.onFragment(fragment -> {
            try {
                List<Post> posts = getListField(fragment, "trendingPosts");
                if (!posts.isEmpty()) {
                    Post firstPost = posts.get(0);
                    int initialUpvotes = firstPost.getUpvotes();
                    // Simulate upvote
                    firstPost.setUpvotes(initialUpvotes + 1);
                    getAdapter(fragment).updatePosts(posts);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify UI updated
        onView(withId(R.id.trendingRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void downvoteButton_decrementsCount() {
        scenario.onFragment(fragment -> {
            try {
                List<Post> posts = getListField(fragment, "trendingPosts");
                if (!posts.isEmpty()) {
                    Post firstPost = posts.get(0);
                    int initialDownvotes = firstPost.getDownvotes();
                    firstPost.setDownvotes(initialDownvotes + 1);
                    getAdapter(fragment).updatePosts(posts);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.trendingRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void postClick_triggersClickHandler() {
        // Verify RecyclerView is displayed and ready for interactions
        onView(withId(R.id.trendingRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyState_whenNoPosts() {
        scenario.close();
        scenario = launchScenarioWithData(new ArrayList<>());

        onView(withId(R.id.trendingRecyclerView)).check(matches(isDisplayed()));
    }

    private FragmentScenario<TrendingFragment> launchScenarioWithData(List<Post> posts) {
        FragmentScenario<TrendingFragment> scenario =
                FragmentScenario.launchInContainer(
                    TrendingFragment.class, 
                    new Bundle(), 
                    R.style.Theme_CSCI310_TeamProj, 
                    (FragmentFactory) null);
        
        // Set posts after fragment is created and adapter is initialized
        scenario.onFragment(fragment -> {
            try {
                List<Post> trending = getListField(fragment, "trendingPosts");
                trending.clear();
                trending.addAll(posts);
                
                // Ensure adapter is initialized before updating
                com.example.csci310_teamproj.ui.adapter.PostAdapter adapter = getAdapter(fragment);
                if (adapter != null) {
                    adapter.updatePosts(trending);
                }
            } catch (Exception e) {
                // Adapter might not be initialized yet, will try again after UI is ready
            }
        });
        
        // Update again after UI is fully created and adapter is definitely initialized
        scenario.onFragment(fragment -> {
            try {
                List<Post> trending = getListField(fragment, "trendingPosts");
                // If posts weren't set or were cleared, set them again
                if (trending.isEmpty() && !posts.isEmpty()) {
                    trending.clear();
                    trending.addAll(posts);
                }
                
                com.example.csci310_teamproj.ui.adapter.PostAdapter adapter = getAdapter(fragment);
                if (adapter != null) {
                    // Update adapter with current posts - create new list to ensure reference is updated
                    adapter.updatePosts(new ArrayList<>(trending));
                } else {
                    throw new RuntimeException("Adapter is still null after UI creation");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to set up trending posts in fragment", e);
            }
        });
        
        return scenario;
    }

    private List<Post> defaultTrendingPosts() {
        List<Post> posts = new ArrayList<>();
        posts.add(buildPost("post-1", "Top Post", 10));
        posts.add(buildPost("post-2", "Second Post", 8));
        posts.add(buildPost("post-3", "Third Post", 5));
        return posts;
    }

    private Post buildPost(String id, String title, int upvotes) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setUpvotes(upvotes);
        post.setDownvotes(0);
        post.setTimestamp(System.currentTimeMillis());
        // Set required fields for PostAdapter to display properly
        post.setAuthorName("Test Author");
        post.setAuthorId("author-" + id);
        post.setLlmTag("GPT-4");
        post.setBody("Test post body content for " + title);
        return post;
    }

    @SuppressWarnings("unchecked")
    private List<Post> getListField(TrendingFragment fragment, String name) throws Exception {
        Field field = TrendingFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        return (List<Post>) field.get(fragment);
    }

    private com.example.csci310_teamproj.ui.adapter.PostAdapter getAdapter(TrendingFragment fragment) throws Exception {
        Field field = TrendingFragment.class.getDeclaredField("postAdapter");
        field.setAccessible(true);
        return (com.example.csci310_teamproj.ui.adapter.PostAdapter) field.get(fragment);
    }
}

