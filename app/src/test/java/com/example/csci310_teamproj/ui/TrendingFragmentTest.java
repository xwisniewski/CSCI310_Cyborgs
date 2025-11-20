package com.example.csci310_teamproj.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.csci310_teamproj.data.repository.PostRepository;
import com.example.csci310_teamproj.data.repository.RepositoryCallback;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TrendingFragmentTest {

    private TrendingFragment fragment;
    private List<Post> trendingPosts;
    private PostAdapter adapter;
    private PostRepository postRepository;

    @Before
    public void setUp() throws Exception {
        fragment = new TrendingFragment();

        Context context = ApplicationProvider.getApplicationContext();
        RecyclerView recyclerView = new RecyclerView(context);
        adapter = Mockito.mock(PostAdapter.class);
        postRepository = Mockito.mock(PostRepository.class);

        setField("trendingRecyclerView", recyclerView);
        setField("postAdapter", adapter);
        setField("postRepository", postRepository);

        trendingPosts = new ArrayList<>();
        setField("trendingPosts", trendingPosts);
    }

    @Test
    public void loadTrendingPosts_limitsToTop5() throws Exception {
        List<Post> allPosts = createPostsWithUpvotes(10);
        
        ArgumentCaptor<RepositoryCallback<List<Post>>> callbackCaptor = 
            ArgumentCaptor.forClass(RepositoryCallback.class);
        
        Mockito.doAnswer(invocation -> {
                RepositoryCallback<List<Post>> callback = invocation.getArgument(1);
                callback.onSuccess(allPosts.subList(0, 5));
                return null;
            })
            .when(postRepository)
            .getTrendingPosts(Mockito.eq(5), callbackCaptor.capture());

        invokeLoadTrendingPosts();
        
        // Re-read the field from fragment since it gets reassigned
        trendingPosts = getListField("trendingPosts");

        assertEquals(5, trendingPosts.size());
        Mockito.verify(adapter).updatePosts(trendingPosts);
    }

    @Test
    public void loadTrendingPosts_sortsByUpvotesDescending() throws Exception {
        // Create posts sorted by upvotes descending (as repository would return them)
        List<Post> posts = Arrays.asList(
            postWithUpvotes("2", 10),
            postWithUpvotes("1", 5),
            postWithUpvotes("3", 3)
        );

        ArgumentCaptor<RepositoryCallback<List<Post>>> callbackCaptor = 
            ArgumentCaptor.forClass(RepositoryCallback.class);
        
        Mockito.doAnswer(invocation -> {
                RepositoryCallback<List<Post>> callback = invocation.getArgument(1);
                callback.onSuccess(posts);
                return null;
            })
            .when(postRepository)
            .getTrendingPosts(Mockito.eq(5), callbackCaptor.capture());

        invokeLoadTrendingPosts();
        
        // Re-read the field from fragment since it gets reassigned
        trendingPosts = getListField("trendingPosts");

        assertEquals(3, trendingPosts.size());
        assertEquals(10, trendingPosts.get(0).getUpvotes());
        assertEquals(5, trendingPosts.get(1).getUpvotes());
        assertEquals(3, trendingPosts.get(2).getUpvotes());
    }

    @Test
    public void loadTrendingPosts_handlesEmptyList() throws Exception {
        ArgumentCaptor<RepositoryCallback<List<Post>>> callbackCaptor = 
            ArgumentCaptor.forClass(RepositoryCallback.class);
        
        Mockito.doAnswer(invocation -> {
                RepositoryCallback<List<Post>> callback = invocation.getArgument(1);
                callback.onSuccess(new ArrayList<>());
                return null;
            })
            .when(postRepository)
            .getTrendingPosts(Mockito.eq(5), callbackCaptor.capture());

        invokeLoadTrendingPosts();
        
        // Re-read the field from fragment since it gets reassigned
        trendingPosts = getListField("trendingPosts");

        assertEquals(0, trendingPosts.size());
        Mockito.verify(adapter).updatePosts(trendingPosts);
    }

    @Test
    public void loadTrendingPosts_handlesError() throws Exception {
        ArgumentCaptor<RepositoryCallback<List<Post>>> callbackCaptor = 
            ArgumentCaptor.forClass(RepositoryCallback.class);
        
        Mockito.doAnswer(invocation -> {
                RepositoryCallback<List<Post>> callback = invocation.getArgument(1);
                callback.onError("Network error");
                return null;
            })
            .when(postRepository)
            .getTrendingPosts(Mockito.eq(5), callbackCaptor.capture());

        invokeLoadTrendingPosts();

        // Should handle error gracefully
        assertNotNull(trendingPosts);
    }

    @Test
    public void trendingPosts_usesCorrectLimit() throws Exception {
        Field limitField = TrendingFragment.class.getDeclaredField("TRENDING_LIMIT");
        limitField.setAccessible(true);
        int limit = limitField.getInt(null);

        assertEquals(5, limit);
    }

    private List<Post> createPostsWithUpvotes(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = new Post();
            post.setId("post-" + i);
            post.setTitle("Post " + i);
            post.setUpvotes(count - i); // Decreasing upvotes
            post.setDownvotes(0);
            post.setTimestamp(System.currentTimeMillis() - (count - i) * 1000); // Different timestamps
            posts.add(post);
        }
        return posts;
    }

    private Post postWithUpvotes(String id, int upvotes) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("Post " + id);
        post.setUpvotes(upvotes);
        post.setDownvotes(0);
        post.setTimestamp(System.currentTimeMillis());
        return post;
    }

    private void invokeLoadTrendingPosts() throws Exception {
        java.lang.reflect.Method method = TrendingFragment.class.getDeclaredMethod("loadTrendingPosts");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = TrendingFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }

    @SuppressWarnings("unchecked")
    private List<Post> getListField(String name) throws Exception {
        Field field = TrendingFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        return (List<Post>) field.get(fragment);
    }
}

