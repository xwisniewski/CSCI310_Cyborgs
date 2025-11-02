package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.User;

/**
 * Defines user-related operations to be implemented by a concrete repository.
 */
public interface UserRepository {
    void registerUser(User user);
}
