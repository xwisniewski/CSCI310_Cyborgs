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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference();
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
}
