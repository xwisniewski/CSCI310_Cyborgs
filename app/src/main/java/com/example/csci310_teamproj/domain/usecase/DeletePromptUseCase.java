package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.data.repository.PromptRepository;

/**
 * Use case for deleting a prompt.
 */
public class DeletePromptUseCase {

    private final PromptRepository promptRepository;

    public DeletePromptUseCase(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public void execute(String promptId, PromptRepository.Callback<Void> callback) {
        promptRepository.deletePrompt(promptId, callback);
    }
}

