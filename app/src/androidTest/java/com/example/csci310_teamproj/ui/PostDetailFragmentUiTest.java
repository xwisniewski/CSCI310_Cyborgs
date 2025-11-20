package com.example.csci310_teamproj.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
public class PostDetailFragmentUiTest {

    private FragmentScenario<PostDetailFragment> scenario;

    @Before
    public void setUp() {
        AnimationDisabler.disableAnimations();
        scenario = launchScenarioWithData(buildPost("post-1", "Test Post", "GPT-4"));
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        AnimationDisabler.enableAnimations();
    }

    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void postTitle_isDisplayed() {
        // Verify post title view is displayed
        onView(withId(R.id.postDetailTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void postAuthor_isDisplayed() {
        // Verify post author view is displayed
        onView(withId(R.id.postDetailAuthor)).check(matches(isDisplayed()));
    }

    @Test
    public void postBody_isDisplayed() {
        // Verify post body view is displayed
        onView(withId(R.id.postDetailBody)).check(matches(isDisplayed()));
    }

    @Test
    public void addCommentButton_isDisplayed() {
        // Verify add comment button is displayed
        onView(withId(R.id.btnAddComment)).check(matches(isDisplayed()));
    }

    private FragmentScenario<PostDetailFragment> launchScenarioWithData(Post post) {
        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        
        // Use a custom FragmentFactory to set the post before onCreate completes
        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
                if (PostDetailFragment.class.getName().equals(className)) {
                    PostDetailFragment fragment = new PostDetailFragment();
                    // Set post immediately after fragment creation, before onCreate
                    try {
                        Field postField = PostDetailFragment.class.getDeclaredField("currentPost");
                        postField.setAccessible(true);
                        postField.set(fragment, post);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to set post in fragment factory", e);
                    }
                    return fragment;
                }
                return super.instantiate(classLoader, className);
            }
        };
        
        FragmentScenario<PostDetailFragment> scenario =
                FragmentScenario.launchInContainer(
                    PostDetailFragment.class, 
                    args, 
                    R.style.Theme_CSCI310_TeamProj, 
                    factory);
        
        // Verify that post is set and views are loaded after fragment is created
        scenario.onFragment(fragment -> {
            try {
                Field postField = PostDetailFragment.class.getDeclaredField("currentPost");
                postField.setAccessible(true);
                Post currentPost = (Post) postField.get(fragment);
                
                // Ensure post is still set (in case it was cleared)
                if (currentPost == null) {
                    postField.set(fragment, post);
                }
                
                // Ensure views are populated
                Field titleViewField = PostDetailFragment.class.getDeclaredField("titleView");
                titleViewField.setAccessible(true);
                Object titleView = titleViewField.get(fragment);
                
                if (titleView != null) {
                    android.widget.TextView tv = (android.widget.TextView) titleView;
                    // If title is empty, call loadPost() to populate views
                    if (tv.getText() == null || tv.getText().toString().isEmpty()) {
                        java.lang.reflect.Method loadPostMethod = PostDetailFragment.class.getDeclaredMethod("loadPost");
                        loadPostMethod.setAccessible(true);
                        loadPostMethod.invoke(fragment);
                    }
                }
            } catch (Exception e) {
                // If we can't set it up, that's OK - the fragment should still work
                throw new RuntimeException("Failed to verify post in fragment", e);
            }
        });
        
        return scenario;
    }

    private Post buildPost(String id, String title, String llm) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setLlmTag(llm);
        post.setAuthorName("Test Author");
        post.setAuthorId("author-1");
        post.setBody("Test post body content");
        post.setUpvotes(5);
        post.setDownvotes(1);
        post.setTimestamp(System.currentTimeMillis());
        return post;
    }
}
