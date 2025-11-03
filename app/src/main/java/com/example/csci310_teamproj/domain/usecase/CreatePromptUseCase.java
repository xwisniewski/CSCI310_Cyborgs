package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.data.repository.PromptRepository;

import java.util.Date;

/**
 * Use case for creating a new prompt.
 */
public class CreatePromptUseCase {

    private final PromptRepository promptRepository;

    public CreatePromptUseCase(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public void execute(String title, String promptText, String description, String llmTag, String experience, String userId, PromptRepository.Callback<Void> callback) {
        // Create date is automatically set by repository if null
        Prompt prompt = new Prompt(null, title, promptText, description, llmTag, experience, new Date(), userId);
        promptRepository.createPrompt(prompt, callback);
    }
}

