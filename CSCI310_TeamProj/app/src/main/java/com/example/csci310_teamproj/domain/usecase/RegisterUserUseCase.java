package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.domain.model.User;
import com.example.csci310_teamproj.data.repository.UserRepository; // ✅ add this

/**
 * Handles business logic for registering a new user.
 * Depends only on abstractions (UserRepository interface).
 */
public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(User user) {
        userRepository.registerUser(user);
    }
}
