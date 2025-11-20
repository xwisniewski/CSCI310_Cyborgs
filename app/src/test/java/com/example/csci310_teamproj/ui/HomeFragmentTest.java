package com.example.csci310_teamproj.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;

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
public class HomeFragmentTest {

    private HomeFragment fragment;
    private List<Post> allPosts;
    private List<Post> posts;
    private PostAdapter adapter;

    @Before
    public void setUp() throws Exception {
        fragment = new HomeFragment();

        Context context = ApplicationProvider.getApplicationContext();
        RecyclerView recyclerView = new RecyclerView(context);

        adapter = Mockito.mock(PostAdapter.class);

        setField("postsRecyclerView", recyclerView);
        setField("postAdapter", adapter);

        allPosts = new ArrayList<>();
        posts = new ArrayList<>();
        setField("allPosts", allPosts);
        setField("posts", posts);
        setField("selectedLlms", new HashSet<>(Arrays.asList("All")));
        setField("searchQuery", "");
        setField("searchMode", "Title");
    }

    @Test
    public void applyFilter_keepsAllPostsWhenNoCriteria() throws Exception {
        seedPosts(
            post("1", "Post 1", "GPT-4", "Author1", "Content 1"),
            post("2", "Post 2", "Claude-3", "Author2", "Content 2")
        );

        invokeApplyFilter();

        assertEquals(2, posts.size());
        Mockito.verify(adapter).updatePosts(posts);
    }

    @Test
    public void applyFilter_filtersByLlmTag() throws Exception {
        seedPosts(
            post("1", "Post 1", "GPT-4", "Author1", "Content 1"),
            post("2", "Post 2", "Claude-3", "Author2", "Content 2")
        );

        setField("selectedLlms", new HashSet<>(Arrays.asList("Claude-3")));

        invokeApplyFilter();

        assertEquals(1, posts.size());
        assertEquals("Post 2", posts.get(0).getTitle());
    }

    @Test
    public void applyFilter_filtersBySearchQueryTitleMode() throws Exception {
        seedPosts(
            post("1", "Budget Planner", "GPT-4", "Author1", "Content 1"),
            post("2", "Daily Journal", "Claude-3", "Author2", "Content 2")
        );

        setField("searchQuery", "budget");
        setField("searchMode", "Title");

        invokeApplyFilter();

        assertEquals(1, posts.size());
        assertEquals("Budget Planner", posts.get(0).getTitle());
    }

    @Test
    public void applyFilter_filtersBySearchQueryAuthorMode() throws Exception {
        seedPosts(
            post("1", "Post 1", "GPT-4", "John Doe", "Content 1"),
            post("2", "Post 2", "Claude-3", "Jane Smith", "Content 2")
        );

        setField("searchQuery", "john");
        setField("searchMode", "Author");

        invokeApplyFilter();

        assertEquals(1, posts.size());
        assertEquals("John Doe", posts.get(0).getAuthorName());
    }

    @Test
    public void applyFilter_combinesLlmAndSearchFilters() throws Exception {
        seedPosts(
            post("1", "Budget Planner", "GPT-4", "Author1", "Content 1"),
            post("2", "Daily Journal", "Claude-3", "Author2", "Content 2"),
            post("3", "Budget Strategy", "Claude-3", "Author3", "Content 3")
        );

        setField("selectedLlms", new HashSet<>(Arrays.asList("Claude-3")));
        setField("searchQuery", "budget");
        setField("searchMode", "Title");

        invokeApplyFilter();

        assertEquals(1, posts.size());
        assertEquals("Budget Strategy", posts.get(0).getTitle());
        assertEquals("Claude-3", posts.get(0).getLlmTag());
    }

    @Test
    public void isValidLlmTagFormat_validFormats() throws Exception {
        assertTrue(invokeIsValidLlmTagFormat("GPT-4"));
        assertTrue(invokeIsValidLlmTagFormat("Claude-4.1"));
        assertTrue(invokeIsValidLlmTagFormat("Gemini-1.5"));
        assertTrue(invokeIsValidLlmTagFormat("LLaMA-2"));
    }

    @Test
    public void isValidLlmTagFormat_invalidFormats() throws Exception {
        assertFalse(invokeIsValidLlmTagFormat("GPT4"));
        assertFalse(invokeIsValidLlmTagFormat("GPT-"));
        assertFalse(invokeIsValidLlmTagFormat("-4"));
        assertFalse(invokeIsValidLlmTagFormat(""));
        assertFalse(invokeIsValidLlmTagFormat(null));
    }

    @Test
    public void applyFilter_caseInsensitiveSearch() throws Exception {
        seedPosts(
            post("1", "BUDGET PLANNER", "GPT-4", "Author1", "Content 1"),
            post("2", "Daily Journal", "Claude-3", "Author2", "Content 2")
        );

        setField("searchQuery", "budget");
        setField("searchMode", "Title");

        invokeApplyFilter();

        assertEquals(1, posts.size());
    }

    @Test
    public void applyFilter_emptySearchQueryShowsAll() throws Exception {
        seedPosts(
            post("1", "Post 1", "GPT-4", "Author1", "Content 1"),
            post("2", "Post 2", "Claude-3", "Author2", "Content 2")
        );

        setField("searchQuery", "");
        setField("searchMode", "Title");

        invokeApplyFilter();

        assertEquals(2, posts.size());
    }

    @Test
    public void applyFilter_filtersBySearchQueryContentMode() throws Exception {
        seedPosts(
            post("1", "Post 1", "GPT-4", "Author1", "This is about machine learning"),
            post("2", "Post 2", "Claude-3", "Author2", "This is about cooking")
        );

        setField("searchQuery", "machine");
        setField("searchMode", "Content");

        invokeApplyFilter();

        assertEquals(1, posts.size());
        assertTrue(posts.get(0).getBody().contains("machine learning"));
    }

    private Post post(String id, String title, String llmTag, String authorName, String body) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setLlmTag(llmTag);
        post.setAuthorName(authorName);
        post.setBody(body);
        post.setTimestamp(System.currentTimeMillis());
        return post;
    }

    private void seedPosts(Post... posts) {
        allPosts.clear();
        this.posts.clear();
        allPosts.addAll(Arrays.asList(posts));
    }

    private void invokeApplyFilter() throws Exception {
        Method method = HomeFragment.class.getDeclaredMethod("applyFilter");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    private boolean invokeIsValidLlmTagFormat(String llmTag) throws Exception {
        Method method = HomeFragment.class.getDeclaredMethod("isValidLlmTagFormat", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(fragment, llmTag);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = HomeFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }
}

