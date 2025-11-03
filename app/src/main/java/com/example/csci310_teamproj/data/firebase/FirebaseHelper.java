package com.example.csci310_teamproj.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Helper class for Firebase Realtime Database references and common Firebase operations.
 */
public class FirebaseHelper {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    
    /**
     * Get the current authenticated user.
     * @return FirebaseUser if logged in, null otherwise
     */
    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
    
    /**
     * Get reference to the posts node in Firebase.
     * @return DatabaseReference to /posts
     */
    public static DatabaseReference getPostsRef() {
        return database.getReference("posts");
    }
    
    /**
     * Get reference to a specific post.
     * @param postId The ID of the post
     * @return DatabaseReference to /posts/{postId}
     */
    public static DatabaseReference getPostRef(String postId) {
        return database.getReference("posts").child(postId);
    }
    
    /**
     * Get reference to a user's data.
     * @param userId The ID of the user
     * @return DatabaseReference to /users/{userId}
     */
    public static DatabaseReference getUserRef(String userId) {
        return database.getReference("users").child(userId);
    }
    
    /**
     * Get reference to a user's votes.
     * @param userId The ID of the user
     * @return DatabaseReference to /userVotes/{userId}
     */
    public static DatabaseReference getUserVotesRef(String userId) {
        return database.getReference("userVotes").child(userId);
    }
    
    /**
     * Get reference to comments for a specific post.
     * @param postId The ID of the post
     * @return DatabaseReference to /postComments/{postId}
     */
    public static DatabaseReference getPostCommentsRef(String postId) {
        return database.getReference("postComments").child(postId);
    }
}

