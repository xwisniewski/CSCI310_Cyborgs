package com.example.csci310_teamproj.domain.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a prompt entity used in the app (e.g., AI prompt or user-submitted idea).
 */
public class Prompt implements Serializable {
    private String id;
    private String title;
    private String promptText;
    private String description;
    private String llmTag;
    private String experience;
    private Date publishDate;
    private String userId; // ID of the user who created this prompt

    // Default constructor for Firebase
    public Prompt() {
    }

    public Prompt(String id, String title, String promptText, String description, String llmTag, String experience, Date publishDate, String userId) {
        this.id = id;
        this.title = title;
        this.promptText = promptText;
        this.description = description;
        this.llmTag = llmTag;
        this.experience = experience;
        this.publishDate = publishDate;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLlmTag() { return llmTag; }
    public void setLlmTag(String llmTag) { this.llmTag = llmTag; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Legacy method for backward compatibility
    public String getContent() { return promptText; }
}
