package com.example.csci310_teamproj.domain.usecase;

import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.data.repository.PromptRepository;

import java.util.Date;

public class CreatePromptUseCase {

    private final PromptRepository promptRepository;

    public CreatePromptUseCase(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    /**
     * @param postedUserId      "anonymous" OR real UID (public identity)
     * @param originalAuthorId  always real UID
     */
    public void execute(String title,
                        String promptText,
                        String description,
                        String llmTag,
                        String experience,
                        String postedUserId,        // "anonymous" OR real UID
                        String originalAuthorId,    // ALWAYS real UID
                        boolean isDraft,
                        PromptRepository.Callback<Void> callback) {

        Prompt prompt = new Prompt(
                null,               // id assigned later
                title,
                promptText,
                description,
                llmTag,
                experience,
                new Date(),         // publish date
                postedUserId,       // 🔥 public identity
                originalAuthorId,   // 🔥 real owner
                isDraft,
                false               // hasHistory
        );

        promptRepository.createPrompt(prompt, callback);
    }
}
