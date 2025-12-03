package com.example.csci310_teamproj.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.model.PostVersionEntity;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.domain.model.PostVersion;
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
 */
public class PostRepositoryImpl implements PostRepository {
    private static final String TAG = "PostRepositoryImpl";

    @Override
    public void createPost(Post post, RepositoryCallback<Void> callback) {
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

        Map<String, Object> postMap = postToMap(post);

        postsRef.child(postId).setValue(postMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post created successfully: " + postId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void updatePost(String postId, Post post, RepositoryCallback<Void> callback) {
        Log.d(TAG, "Updating post: " + postId);
        
        // First, get the current post to save as version history
        FirebaseHelper.getPostRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post oldPost = snapshotToPost(snapshot);
                        if (oldPost != null) {
                            Log.d(TAG, "Found existing post, attempting to save version history");
                            trySaveHistoryThenUpdate(postId, post, oldPost, callback);
                        } else {
                            Log.d(TAG, "No existing post found, performing direct update");
                            performPostUpdate(postId, post, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching existing post: " + error.getMessage());
                        // Just do the update directly
                        performPostUpdate(postId, post, callback);
                    }
                });
    }

    private void trySaveHistoryThenUpdate(String postId, Post newPost, Post oldPost, RepositoryCallback<Void> callback) {
        try {
            PostVersionEntity versionEntity = postToVersionEntity(postId, oldPost);
            DatabaseReference historyRef = FirebaseHelper.getPostHistoryRef(postId).push();
            versionEntity.versionId = historyRef.getKey();

            Log.d(TAG, "Saving version history to: " + historyRef.toString());
            
            Map<String, Object> versionMap = versionEntityToMap(versionEntity);

            historyRef.setValue(versionMap)
                    .addOnSuccessListener(a -> {
                        Log.d(TAG, "Version history saved successfully for post: " + postId);
                        newPost.setHasHistory(true);
                        performPostUpdate(postId, newPost, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Could not save history (permission issue?), proceeding with update: " + e.getMessage());
                        // Don't set hasHistory, just proceed with update
                        performPostUpdate(postId, newPost, callback);
                    });
        } catch (Exception e) {
            Log.w(TAG, "Exception saving history, proceeding with update: " + e.getMessage());
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
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post updated successfully: " + postId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating post", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void deletePost(String postId, RepositoryCallback<Void> callback) {
        if (postId == null || postId.isEmpty()) {
            callback.onError("Post ID is null or empty");
            return;
        }

        // Soft delete: set isDeleted to true
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);
        postRef.child("isDeleted").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post soft deleted: " + postId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllPosts(RepositoryCallback<List<Post>> callback) {
        DatabaseReference postsRef = FirebaseHelper.getPostsRef();
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.e(TAG, "Error fetching posts", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getPost(String postId, RepositoryCallback<Post> callback) {
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError("Post not found");
                    return;
                }

                Post post = snapshotToPost(snapshot);
                if (post != null && !post.isDeleted()) {
                    callback.onSuccess(post);
                } else {
                    callback.onError("Post not found or deleted");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching post", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getTrendingPosts(int limit, RepositoryCallback<List<Post>> callback) {
        DatabaseReference postsRef = FirebaseHelper.getPostsRef();
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = snapshotToPost(postSnapshot);
                    if (post != null && !post.isDeleted()) {
                        posts.add(post);
                    }
                }

                posts.sort((p1, p2) -> {
                    double score1 = calculateTrendingScore(p1, currentTime);
                    double score2 = calculateTrendingScore(p2, currentTime);
                    return Double.compare(score2, score1);
                });

                if (limit > 0 && posts.size() > limit) {
                    posts = posts.subList(0, limit);
                }

                callback.onSuccess(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching trending posts", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    // ============================================================================
    // BOOKMARK OPERATIONS
    // ============================================================================

    @Override
    public void addBookmark(String userId, String postId, RepositoryCallback<Void> callback) {
        if (userId == null || postId == null) {
            callback.onError("User ID and Post ID are required");
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Bookmark added: " + postId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding bookmark", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void removeBookmark(String userId, String postId, RepositoryCallback<Void> callback) {
        if (userId == null || postId == null) {
            callback.onError("User ID and Post ID are required");
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Bookmark removed: " + postId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing bookmark", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getUserBookmarks(String userId, RepositoryCallback<List<String>> callback) {
        if (userId == null) {
            callback.onError("User ID is required");
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> bookmarkedPostIds = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Boolean isBookmarked = child.getValue(Boolean.class);
                            if (isBookmarked != null && isBookmarked) {
                                bookmarkedPostIds.add(child.getKey());
                            }
                        }
                        callback.onSuccess(bookmarkedPostIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching bookmarks", error.toException());
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void getBookmarkedPosts(String userId, RepositoryCallback<List<Post>> callback) {
        getUserBookmarks(userId, new RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> bookmarkedPostIds) {
                if (bookmarkedPostIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Fetch all posts and filter by bookmarked IDs
                getAllPosts(new RepositoryCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> allPosts) {
                        List<Post> bookmarkedPosts = new ArrayList<>();
                        for (Post post : allPosts) {
                            if (bookmarkedPostIds.contains(post.getId())) {
                                bookmarkedPosts.add(post);
                            }
                        }
                        callback.onSuccess(bookmarkedPosts);
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
        if (userId == null || postId == null) {
            callback.onError("User ID and Post ID are required");
            return;
        }

        FirebaseHelper.getUserBookmarksRef(userId)
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isBookmarked = snapshot.getValue(Boolean.class);
                        callback.onSuccess(isBookmarked != null && isBookmarked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking bookmark status", error.toException());
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // VERSION HISTORY OPERATIONS
    // ============================================================================

    @Override
    public void getPostHistory(String postId, RepositoryCallback<List<PostVersion>> callback) {
        if (postId == null) {
            callback.onError("Post ID is required");
            return;
        }

        DatabaseReference historyRef = FirebaseHelper.getPostHistoryRef(postId);
        Log.d(TAG, "Loading post history from: " + historyRef.toString());

        historyRef.orderByChild("updatedAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Post history snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());
                        List<PostVersion> versions = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Log.d(TAG, "Found post history entry: " + ds.getKey());
                            PostVersionEntity entity = ds.getValue(PostVersionEntity.class);
                            if (entity != null) {
                                entity.versionId = ds.getKey();
                                versions.add(versionEntityToDomain(entity));
                            }
                        }

                        Log.d(TAG, "Loaded " + versions.size() + " post history entries");
                        // Most recent first
                        Collections.reverse(versions);
                        callback.onSuccess(versions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching post history", error.toException());
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private double calculateTrendingScore(Post post, long currentTime) {
        int netVotes = post.getUpvotes() - post.getDownvotes();
        long ageInMillis = currentTime - post.getTimestamp();
        double ageInHours = ageInMillis / (1000.0 * 60.0 * 60.0);

        double timeDecayFactor = Math.pow(0.95, ageInHours);
        timeDecayFactor = Math.max(0.1, timeDecayFactor);

        return netVotes * timeDecayFactor;
    }

    private Post snapshotToPost(DataSnapshot snapshot) {
        if (!snapshot.exists()) return null;

        Post post = snapshot.getValue(Post.class);
        if (post != null) {
            // Handle fields that might not be auto-mapped
            Boolean isDeleted = snapshot.child("isDeleted").getValue(Boolean.class);
            if (isDeleted != null) post.setDeleted(isDeleted);

            Boolean anonymous = snapshot.child("anonymous").getValue(Boolean.class);
            if (anonymous != null) post.setAnonymous(anonymous);

            Boolean hasHistory = snapshot.child("hasHistory").getValue(Boolean.class);
            if (hasHistory != null) post.setHasHistory(hasHistory);
        }
        return post;
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

    private Map<String, Object> versionEntityToMap(PostVersionEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("versionId", entity.versionId);
        map.put("postId", entity.postId);
        map.put("title", entity.title);
        map.put("body", entity.body);
        map.put("llmTag", entity.llmTag);
        map.put("anonymous", entity.anonymous);
        map.put("updatedBy", entity.updatedBy);
        map.put("updatedAt", entity.updatedAt);
        return map;
    }

    private PostVersion versionEntityToDomain(PostVersionEntity entity) {
        Date updatedAt = entity.updatedAt != null ? new Date(entity.updatedAt) : new Date();
        boolean wasAnonymous = entity.anonymous != null && entity.anonymous;

        return new PostVersion(
                entity.versionId,
                entity.postId,
                entity.title,
                entity.body,
                entity.llmTag,
                wasAnonymous,
                entity.updatedBy,
                updatedAt
        );
    }
}
