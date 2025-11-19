package com.example.csci310_teamproj.data.repository;

import android.util.Log;
import com.example.csci310_teamproj.domain.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporary in-memory implementation of UserRepository.
 * Replace with database or network calls later.
 */
public class UserRepositoryImpl implements UserRepository {

    private static final String TAG = "UserRepositoryImpl";
    private final List<User> users = new ArrayList<>();

    @Override
    public void registerUser(User user) {
        users.add(user);
        // Prevent crash during JVM unit tests (Log.d is not available)
        try {
            android.util.Log.d(TAG, "User registered: " + user.getName() + " (" + user.getEmail() + ")");
        } catch (Exception ignored) {}

    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}
