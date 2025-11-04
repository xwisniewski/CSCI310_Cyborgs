package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.data.repository.PromptRepository;

/**
 * Use case for updating an existing prompt.
 */
public class UpdatePromptUseCase {

    private final PromptRepository promptRepository;

    public UpdatePromptUseCase(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public void execute(Prompt prompt, PromptRepository.Callback<Void> callback) {
        promptRepository.updatePrompt(prompt, callback);
    }
}

