package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Prompt;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation returning fake prompts.
 */
public class PromptRepositoryImpl implements PromptRepository {

    @Override
    public List<Prompt> getPrompts() {
        List<Prompt> prompts = new ArrayList<>();
        prompts.add(new Prompt("1", "Welcome Prompt", "Describe your favorite programming project!"));
        prompts.add(new Prompt("2", "Creative Prompt", "If AI could do one thing for you, what would it be?"));
        return prompts;
    }
}
