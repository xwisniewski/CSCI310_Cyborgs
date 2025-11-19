package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Prompt;
import java.util.List;

/**
 * Defines prompt-related data operations.
 */
public interface PromptRepository {
    List<Prompt> getPrompts(); // Legacy method for backward compatibility
    void getPrompts(Callback<List<Prompt>> callback); // Async method with callback
    void createPrompt(Prompt prompt, Callback<Void> callback);
    void updatePrompt(Prompt prompt, Callback<Void> callback);
    void deletePrompt(String promptId, Callback<Void> callback);
    void getPromptById(String promptId, Callback<Prompt> callback);

    interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
