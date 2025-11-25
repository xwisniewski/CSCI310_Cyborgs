package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.domain.model.PromptVersion;
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
    void getPromptHistory(String promptId, Callback<java.util.List<PromptVersion>> callback);

    interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
