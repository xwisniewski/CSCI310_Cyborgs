package com.example.csci310_teamproj.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.model.PostVersionEntity;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.domain.model.PostVersion;
import com.example.csci310_teamproj.util.TestUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase implementation of PostRepository.
 * Now includes instrumentation-test-safe guards to avoid Firebase access during androidTest runs.
 */
public class PostRepositoryImpl implements PostRepository {

    private static final String TAG = "PostRepositoryImpl";

    // ============================================================================
    // CREATE POST
    // ============================================================================

    @Override
    public void createPost(Post post, RepositoryCallback<Void> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping Firebase createPost()");
            callback.onSuccess(null);
            return;
        }

        DatabaseReference postsRef = FirebaseHelper.getPostsRef();
        String postId = postsRef.push().getKey();
        if (postId == null) {
            callback.onError("Failed to generate post ID");
            return;
        }

        post.setId(postId);
        post.setTimestamp(System.currentTimeMillis());
        post.setDeleted(false);
        post.setHasHistory(false);

        postsRef.child(postId).setValue(postToMap(post))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ============================================================================
    // UPDATE POST
    // ============================================================================

    @Override
    public void updatePost(String postId, Post post, RepositoryCallback<Void> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping Firebase updatePost()");
            callback.onSuccess(null);
            return;
        }

        FirebaseHelper.getPostRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post oldPost = snapshotToPost(snapshot);
                        if (oldPost != null) {
                            trySaveHistoryThenUpdate(postId, post, oldPost, callback);
                        } else {
                            performPostUpdate(postId, post, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        performPostUpdate(postId, post, callback);
                    }
                });
    }

    private void trySaveHistoryThenUpdate(String postId, Post newPost, Post oldPost, RepositoryCallback<Void> callback) {
        try {
            PostVersionEntity versionEntity = postToVersionEntity(postId, oldPost);
            DatabaseReference historyRef = FirebaseHelper.getPostHistoryRef(postId).push();
            versionEntity.versionId = historyRef.getKey();

            historyRef.setValue(versionEntityToMap(versionEntity))
                    .addOnSuccessListener(a -> {
                        newPost.setHasHistory(true);
                        performPostUpdate(postId, newPost, callback);
                    })
                    .addOnFailureListener(e -> {
                        performPostUpdate(postId, newPost, callback);
                    });
        } catch (Exception e) {
            performPostUpdate(postId, newPost, callback);
        }
    }

    private void performPostUpdate(String postId, Post post, RepositoryCallback<Void> callback) {
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", post.getTitle());
        updates.put("body", post.getBody());
        updates.put("llmTag", post.getLlmTag());
        updates.put("anonymous", post.isAnonymous());
        updates.put("hasHistory", post.hasHistory());

        postRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ============================================================================
    // DELETE POST
    // ============================================================================

    @Override
    public void deletePost(String postId, RepositoryCallback<Void> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping Firebase deletePost()");
            callback.onSuccess(null);
            return;
        }

        if (postId == null || postId.isEmpty()) {
            callback.onError("Post ID is null or empty");
            return;
        }

        FirebaseHelper.getPostRef(postId)
                .child("isDeleted")
                .setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ============================================================================
    // GET ALL POSTS
    // ============================================================================

    @Override
    public void getAllPosts(RepositoryCallback<List<Post>> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Returning empty getAllPosts()");
            callback.onSuccess(Collections.emptyList());
            return;
        }

        FirebaseHelper.getPostsRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Post> posts = new ArrayList<>();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Post post = snapshotToPost(postSnapshot);
                            if (post != null && !post.isDeleted()) {
                                posts.add(post);
                            }
                        }

                        posts.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                        callback.onSuccess(posts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // GET SINGLE POST
    // ============================================================================

    @Override
    public void getPost(String postId, RepositoryCallback<Post> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping getPost()");
            callback.onError("Post unavailable in test mode");
            return;
        }

        FirebaseHelper.getPostRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshotToPost(snapshot);
                        if (post != null && !post.isDeleted()) {
                            callback.onSuccess(post);
                        } else {
                            callback.onError("Post not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // TRENDING POSTS
    // ============================================================================

    @Override
    public void getTrendingPosts(int limit, RepositoryCallback<List<Post>> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Returning empty trending posts");
            callback.onSuccess(Collections.emptyList());
            return;
        }

        FirebaseHelper.getPostsRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Post> posts = new ArrayList<>();
                        long currentTime = System.currentTimeMillis();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Post post = snapshotToPost(child);
                            if (post != null && !post.isDeleted()) {
                                posts.add(post);
                            }
                        }

                        posts.sort((a, b) -> Double.compare(
                                calculateTrendingScore(b, currentTime),
                                calculateTrendingScore(a, currentTime)
                        ));

                        if (limit > 0 && posts.size() > limit) {
                            posts = posts.subList(0, limit);
                        }

                        callback.onSuccess(posts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // BOOKMARKS
    // ============================================================================

    @Override
    public void addBookmark(String userId, String postId, RepositoryCallback<Void> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping addBookmark()");
            callback.onSuccess(null);
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void removeBookmark(String userId, String postId, RepositoryCallback<Void> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping removeBookmark()");
            callback.onSuccess(null);
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getUserBookmarks(String userId, RepositoryCallback<List<String>> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Returning empty bookmark list");
            callback.onSuccess(Collections.emptyList());
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> ids = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Boolean v = child.getValue(Boolean.class);
                            if (v != null && v) ids.add(child.getKey());
                        }
                        callback.onSuccess(ids);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void getBookmarkedPosts(String userId, RepositoryCallback<List<Post>> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Returning empty bookmarked posts");
            callback.onSuccess(Collections.emptyList());
            return;
        }

        getUserBookmarks(userId, new RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> bookmarkedIds) {
                if (bookmarkedIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                getAllPosts(new RepositoryCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> allPosts) {
                        List<Post> result = new ArrayList<>();
                        for (Post p : allPosts) {
                            if (bookmarkedIds.contains(p.getId())) {
                                result.add(p);
                            }
                        }
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void isBookmarked(String userId, String postId, RepositoryCallback<Boolean> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Skipping isBookmarked()");
            callback.onSuccess(false);
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean value = snapshot.getValue(Boolean.class);
                        callback.onSuccess(value != null && value);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // VERSION HISTORY
    // ============================================================================

    @Override
    public void getPostHistory(String postId, RepositoryCallback<List<PostVersion>> callback) {
        if (TestUtils.isRunningTest()) {
            Log.d(TAG, "[TEST MODE] Returning empty post history");
            callback.onSuccess(Collections.emptyList());
            return;
        }

        DatabaseReference ref = FirebaseHelper.getPostHistoryRef(postId);
        ref.orderByChild("updatedAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PostVersion> versions = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PostVersionEntity e = ds.getValue(PostVersionEntity.class);
                            if (e != null) {
                                e.versionId = ds.getKey();
                                versions.add(versionEntityToDomain(e));
                            }
                        }
                        Collections.reverse(versions);
                        callback.onSuccess(versions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private double calculateTrendingScore(Post post, long currentTime) {
        int netVotes = post.getUpvotes() - post.getDownvotes();
        long ageMs = currentTime - post.getTimestamp();
        double ageH = ageMs / (1000.0 * 60.0 * 60.0);
        double decay = Math.max(0.1, Math.pow(0.95, ageH));
        return netVotes * decay;
    }

    private Post snapshotToPost(DataSnapshot snapshot) {
        if (!snapshot.exists()) return null;

        Post p = snapshot.getValue(Post.class);
        if (p != null) {
            Boolean deleted = snapshot.child("isDeleted").getValue(Boolean.class);
            Boolean anon = snapshot.child("anonymous").getValue(Boolean.class);
            Boolean hist = snapshot.child("hasHistory").getValue(Boolean.class);

            if (deleted != null) p.setDeleted(deleted);
            if (anon != null) p.setAnonymous(anon);
            if (hist != null) p.setHasHistory(hist);
        }
        return p;
    }

    private Map<String, Object> postToMap(Post post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("body", post.getBody());
        map.put("authorId", post.getAuthorId());
        map.put("authorName", post.getAuthorName());
        map.put("llmTag", post.getLlmTag());
        map.put("timestamp", post.getTimestamp());
        map.put("upvotes", post.getUpvotes());
        map.put("downvotes", post.getDownvotes());
        map.put("isDeleted", post.isDeleted());
        map.put("anonymous", post.isAnonymous());
        map.put("hasHistory", post.hasHistory());
        return map;
    }

    private PostVersionEntity postToVersionEntity(String postId, Post post) {
        String editorId = FirebaseHelper.getCurrentUser() != null
                ? FirebaseHelper.getCurrentUser().getUid()
                : post.getAuthorId();

        return new PostVersionEntity(
                null,
                postId,
                post.getTitle(),
                post.getBody(),
                post.getLlmTag(),
                post.isAnonymous(),
                editorId,
                System.currentTimeMillis()
        );
    }

    private Map<String, Object> versionEntityToMap(PostVersionEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("versionId", e.versionId);
        map.put("postId", e.postId);
        map.put("title", e.title);
        map.put("body", e.body);
        map.put("llmTag", e.llmTag);
        map.put("anonymous", e.anonymous);
        map.put("updatedBy", e.updatedBy);
        map.put("updatedAt", e.updatedAt);
        return map;
    }

    private PostVersion versionEntityToDomain(PostVersionEntity e) {
        return new PostVersion(
                e.versionId,
                e.postId,
                e.title,
                e.body,
                e.llmTag,
                e.anonymous != null && e.anonymous,
                e.updatedBy,
                new Date(e.updatedAt != null ? e.updatedAt : System.currentTimeMillis())
        );
    }
}
