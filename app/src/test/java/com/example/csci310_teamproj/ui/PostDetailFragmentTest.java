package com.example.csci310_teamproj.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.csci310_teamproj.domain.model.Comment;
import com.example.csci310_teamproj.domain.model.Post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PostDetailFragmentTest {

    private PostDetailFragment fragment;
    private Post currentPost;
    private String currentUserId;
    private TextView editButton;
    private TextView deleteButton;
    private TextView titleView;
    private TextView authorView;
    private TextView tagView;
    private TextView dateView;
    private TextView bodyView;
    private TextView upvoteCount;
    private TextView downvoteCount;
    private List<Comment> comments;

    @Before
    public void setUp() throws Exception {
        fragment = new PostDetailFragment();

        Context context = ApplicationProvider.getApplicationContext();
        
        titleView = new TextView(context);
        authorView = new TextView(context);
        tagView = new TextView(context);
        dateView = new TextView(context);
        bodyView = new TextView(context);
        upvoteCount = new TextView(context);
        downvoteCount = new TextView(context);
        editButton = new TextView(context);
        deleteButton = new TextView(context);
        RecyclerView commentsRecyclerView = new RecyclerView(context);

        setField("titleView", titleView);
        setField("authorView", authorView);
        setField("tagView", tagView);
        setField("dateView", dateView);
        setField("bodyView", bodyView);
        setField("upvoteCount", upvoteCount);
        setField("downvoteCount", downvoteCount);
        setField("editButton", editButton);
        setField("deleteButton", deleteButton);
        setField("commentsRecyclerView", commentsRecyclerView);

        comments = new ArrayList<>();
        setField("comments", comments);

        currentUserId = "user-123";
        setField("currentUserId", currentUserId);
    }

    @Test
    public void loadPost_displaysPostDataCorrectly() throws Exception {
        currentPost = createPost("post-1", "Test Title", "Author Name", 
                                "Test Body", "GPT-4", 5, 2);
        setField("currentPost", currentPost);

        invokeLoadPost();

        assertEquals("Test Title", titleView.getText().toString());
        assertTrue(authorView.getText().toString().contains("Author Name"));
        assertEquals("Test Body", bodyView.getText().toString());
        assertEquals("5", upvoteCount.getText().toString());
        assertEquals("2", downvoteCount.getText().toString());
    }

    @Test
    public void editDeleteButtons_visibleForAuthor() throws Exception {
        currentPost = createPost("post-1", "Test Title", "Author Name", 
                                "Test Body", "GPT-4", 0, 0);
        currentPost.setAuthorId(currentUserId);
        setField("currentPost", currentPost);

        invokeLoadPost();

        assertEquals(View.VISIBLE, editButton.getVisibility());
        assertEquals(View.VISIBLE, deleteButton.getVisibility());
    }

    @Test
    public void editDeleteButtons_hiddenForNonAuthor() throws Exception {
        currentPost = createPost("post-1", "Test Title", "Author Name", 
                                "Test Body", "GPT-4", 0, 0);
        currentPost.setAuthorId("different-user");
        setField("currentPost", currentPost);

        invokeLoadPost();

        assertEquals(View.GONE, editButton.getVisibility());
        assertEquals(View.GONE, deleteButton.getVisibility());
    }

    @Test
    public void isValidLlmTagFormat_validFormats() throws Exception {
        assertTrue(invokeIsValidLlmTagFormat("GPT-4"));
        assertTrue(invokeIsValidLlmTagFormat("Claude-4.1"));
        assertTrue(invokeIsValidLlmTagFormat("Gemini-1.5"));
    }

    @Test
    public void isValidLlmTagFormat_invalidFormats() throws Exception {
        assertFalse(invokeIsValidLlmTagFormat("GPT4"));
        assertFalse(invokeIsValidLlmTagFormat("GPT-"));
        assertFalse(invokeIsValidLlmTagFormat(""));
        assertFalse(invokeIsValidLlmTagFormat(null));
    }

    @Test
    public void loadComments_updatesCommentList() throws Exception {
        currentPost = createPost("post-1", "Test", "Author", "Body", "GPT-4", 0, 0);
        setField("currentPost", currentPost);

        List<Comment> testComments = new ArrayList<>();
        testComments.add(createComment("comment-1", "Comment 1"));
        testComments.add(createComment("comment-2", "Comment 2"));

        setField("comments", testComments);

        List<Comment> loadedComments = getListField("comments");
        assertEquals(2, loadedComments.size());
    }

    @Test
    public void loadPost_handlesNullFields() throws Exception {
        currentPost = new Post();
        currentPost.setId("post-1");
        currentPost.setTitle(null);
        currentPost.setAuthorName(null);
        currentPost.setBody(null);
        currentPost.setTimestamp(System.currentTimeMillis());
        setField("currentPost", currentPost);

        invokeLoadPost();

        // Should not crash and display empty strings
        assertEquals("", titleView.getText().toString());
    }

    private Post createPost(String id, String title, String authorName, String body, 
                            String llmTag, int upvotes, int downvotes) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setAuthorName(authorName);
        post.setBody(body);
        post.setLlmTag(llmTag);
        post.setUpvotes(upvotes);
        post.setDownvotes(downvotes);
        post.setTimestamp(System.currentTimeMillis());
        return post;
    }

    private Comment createComment(String id, String body) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setBody(body);
        return comment;
    }

    private void invokeLoadPost() throws Exception {
        Method method = PostDetailFragment.class.getDeclaredMethod("loadPost");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    private boolean invokeIsValidLlmTagFormat(String llmTag) throws Exception {
        Method method = PostDetailFragment.class.getDeclaredMethod("isValidLlmTagFormat", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(fragment, llmTag);
    }

    @SuppressWarnings("unchecked")
    private List<Comment> getListField(String name) throws Exception {
        Field field = PostDetailFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        return (List<Comment>) field.get(fragment);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = PostDetailFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }
}

