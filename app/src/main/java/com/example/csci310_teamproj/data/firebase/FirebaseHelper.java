package com.example.csci310_teamproj.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Centralized Firebase access point for the app.
 * Prevents duplicate Firebase initialization and keeps data paths consistent.
 */
public class FirebaseHelper {

    // 🔐 Authentication
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    // 🌲 Root reference to Realtime Database
    public static DatabaseReference getRootRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    // 🧩 Node references for each entity
    public static DatabaseReference getUsersRef() {
        return getRootRef().child("users");
    }

    public static DatabaseReference getPromptsRef() {
        return getRootRef().child("prompts");
    }

    // You can later add:
    // public static DatabaseReference getPostsRef();
    // public static DatabaseReference getCommentsRef();

    // 👤 Get the currently signed-in Firebase user
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    // 📂 Get the database reference for a specific user by UID
    public static DatabaseReference getUserRef(String userId) {
        return getUsersRef().child(userId);
    }


    public static DatabaseReference getPostsRef() {
        return getRootRef().child("posts");
    }

    public static DatabaseReference getCommentsRef() {
        return getRootRef().child("comments");
    }

    public static DatabaseReference getVotesRef() {
        return getRootRef().child("votes");
    }


    public static DatabaseReference getPostRef(String postId) {
        return getPostsRef().child(postId);
    }

    public static DatabaseReference getPostCommentsRef(String postId) {
        return getCommentsRef().child(postId);
    }

    public static DatabaseReference getUserVotesRef(String userId) {
        return getVotesRef().child(userId);
    }

    public static DatabaseReference getPromptRef(String promptId) {
        return getPromptsRef().child(promptId);
    }

}
