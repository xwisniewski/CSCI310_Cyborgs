package com.example.csci310_teamproj.data.repository;

import android.util.Log;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.domain.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
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

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", post.getId());
        postMap.put("title", post.getTitle());
        postMap.put("body", post.getBody());
        postMap.put("authorId", post.getAuthorId());
        postMap.put("authorName", post.getAuthorName());
        postMap.put("llmTag", post.getLlmTag());
        postMap.put("timestamp", post.getTimestamp());
        postMap.put("upvotes", post.getUpvotes());
        postMap.put("downvotes", post.getDownvotes());
        postMap.put("isDeleted", post.isDeleted());

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
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", post.getTitle());
        updates.put("body", post.getBody());
        updates.put("llmTag", post.getLlmTag());

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
            public void onDataChange(DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Explicitly check isDeleted from snapshot to handle Firebase deserialization issues
                    Boolean isDeleted = postSnapshot.child("isDeleted").getValue(Boolean.class);
                    if (isDeleted != null && isDeleted) {
                        // Skip deleted posts
                        continue;
                    }
                    
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Ensure isDeleted is set correctly
                        if (isDeleted != null) {
                            post.setDeleted(isDeleted);
                        }
                        posts.add(post);
                    }
                }
                // Sort by timestamp descending (newest first)
                posts.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                callback.onSuccess(posts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError("Post not found");
                    return;
                }
                
                // Explicitly check isDeleted from snapshot
                Boolean isDeleted = snapshot.child("isDeleted").getValue(Boolean.class);
                if (isDeleted != null && isDeleted) {
                    callback.onError("Post not found or deleted");
                    return;
                }
                
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    // Ensure isDeleted is set correctly
                    if (isDeleted != null) {
                        post.setDeleted(isDeleted);
                    }
                    callback.onSuccess(post);
                } else {
                    callback.onError("Post not found or deleted");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
            public void onDataChange(DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();
                long currentTime = System.currentTimeMillis();
                
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Explicitly check isDeleted from snapshot
                    Boolean isDeleted = postSnapshot.child("isDeleted").getValue(Boolean.class);
                    if (isDeleted != null && isDeleted) {
                        // Skip deleted posts
                        continue;
                    }
                    
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Ensure isDeleted is set correctly
                        if (isDeleted != null) {
                            post.setDeleted(isDeleted);
                        }
                        posts.add(post);
                    }
                }
                
                // Sort by trending score (upvotes * time decay) - computed on the fly
                posts.sort((p1, p2) -> {
                    double score1 = calculateTrendingScore(p1, currentTime);
                    double score2 = calculateTrendingScore(p2, currentTime);
                    return Double.compare(score2, score1); // Descending order
                });
                
                // Limit to top K posts
                if (limit > 0 && posts.size() > limit) {
                    posts = posts.subList(0, limit);
                }
                
                callback.onSuccess(posts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching trending posts", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Calculate trending score based on upvotes and time decay.
     * Recent posts get higher scores. Older posts decay over time.
     * 
     * Formula: score = (upvotes - downvotes) * timeDecayFactor
     * Time decay: exponential decay based on age in hours
     * 
     * @param post The post to score
     * @param currentTime Current timestamp in milliseconds
     * @return Trending score (higher = more trending)
     */
    private double calculateTrendingScore(Post post, long currentTime) {
        // Calculate net votes (upvotes - downvotes)
        int netVotes = post.getUpvotes() - post.getDownvotes();
        
        // Calculate age in hours
        long ageInMillis = currentTime - post.getTimestamp();
        double ageInHours = ageInMillis / (1000.0 * 60.0 * 60.0);
        
        // Exponential time decay: 0.95^hours means 5% decay per hour
        // Older posts get lower scores even with same vote count
        double timeDecayFactor = Math.pow(0.95, ageInHours);
        
        // Ensure score doesn't go negative for very old posts
        timeDecayFactor = Math.max(0.1, timeDecayFactor);
        
        // Trending score = net votes * time decay
        return netVotes * timeDecayFactor;
    }
}

