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

    // Public-facing identity (anonymous OR real uid)
    private String userId;

    // Real owner uid (used for security rules)
    private String originalAuthorId;

    private boolean isDraft;
    private boolean hasHistory;

    // ---------------------------------------------------------------------------------------------
    // Default constructor (Firebase required)
    // ---------------------------------------------------------------------------------------------
    public Prompt() {}

    // ---------------------------------------------------------------------------------------------
    // FULL constructor (recommended)
    // ---------------------------------------------------------------------------------------------
    public Prompt(
            String id,
            String title,
            String promptText,
            String description,
            String llmTag,
            String experience,
            Date publishDate,
            String userId,
            String originalAuthorId,
            boolean isDraft,
            boolean hasHistory
    ) {
        this.id = id;
        this.title = title;
        this.promptText = promptText;
        this.description = description;
        this.llmTag = llmTag;
        this.experience = experience;
        this.publishDate = publishDate != null ? publishDate : new Date();
        this.userId = userId;
        this.originalAuthorId = originalAuthorId;
        this.isDraft = isDraft;
        this.hasHistory = hasHistory;
    }

    // ---------------------------------------------------------------------------------------------
    // BACKWARD COMPATIBLE CONSTRUCTOR – CreatePromptUseCase uses this
    // ---------------------------------------------------------------------------------------------
    public Prompt(
            String id,
            String title,
            String promptText,
            String description,
            String llmTag,
            String experience,
            Date publishDate,
            String userId,
            boolean isDraft
    ) {
        this(
                id,
                title,
                promptText,
                description,
                llmTag,
                experience,
                publishDate,
                userId,
                userId,   // originalAuthorId defaults to real uid
                isDraft,
                false
        );
    }

    // ---------------------------------------------------------------------------------------------
    // **FIXED** 8-arg constructor — DO NOT FORCE originalAuthorId = userId
    // This was the cause of the anonymous-posting bug.
    // ---------------------------------------------------------------------------------------------
    public Prompt(
            String id,
            String title,
            String promptText,
            String description,
            String llmTag,
            String experience,
            Date publishDate,
            String userId
    ) {
        this(
                id,
                title,
                promptText,
                description,
                llmTag,
                experience,
                publishDate,
                userId,     // may be "anonymous"
                null,       // ❗ FIX: do NOT force originalAuthorId = userId
                false,
                false
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Getters / setters
    // ---------------------------------------------------------------------------------------------
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

    public String getOriginalAuthorId() { return originalAuthorId; }
    public void setOriginalAuthorId(String originalAuthorId) { this.originalAuthorId = originalAuthorId; }

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    public boolean hasHistory() { return hasHistory; }
    public void setHasHistory(boolean hasHistory) { this.hasHistory = hasHistory; }

    // Legacy compatibility
    public String getContent() { return promptText; }
}
