package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.data.repository.PromptRepository;

import java.util.List;

/**
 * Retrieves a list of prompts for display in the UI.
 */
public class GetPromptsUseCase {

    private final PromptRepository promptRepository;

    public GetPromptsUseCase(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public List<Prompt> execute() {
        return promptRepository.getPrompts();
    }
}
