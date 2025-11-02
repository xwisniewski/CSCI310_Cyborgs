package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Prompt;
import java.util.List;

/**
 * Defines prompt-related data operations.
 */
public interface PromptRepository {
    List<Prompt> getPrompts();
}
