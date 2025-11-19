package com.example.csci310_teamproj.data.model;

import java.util.Date;

/**
 * Data-layer representation of a Prompt.
 * Separate from domain model to allow flexibility in storage/serialization.
 */
public class PromptEntity {
    public String id;
    public String title;
    public String promptText;
    public String description;
    public String llmTag;
    public String experience;
    public Long publishDate; // Stored as timestamp for Firebase
    public String userId;

    public PromptEntity() {
        // Default constructor for Firebase
    }

    public PromptEntity(String id, String title, String promptText, String description, String llmTag, String experience, Long publishDate, String userId) {
        this.id = id;
        this.title = title;
        this.promptText = promptText;
        this.description = description;
        this.llmTag = llmTag;
        this.experience = experience;
        this.publishDate = publishDate;
        this.userId = userId;
    }
}
