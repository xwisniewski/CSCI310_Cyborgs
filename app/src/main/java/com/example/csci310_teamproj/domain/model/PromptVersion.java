package com.example.csci310_teamproj.domain.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single historical snapshot of a prompt.
 */
public class PromptVersion implements Serializable {
    private String versionId;
    private String promptId;
    private String title;
    private String description;
    private String promptText;
    private String llmTag;
    private String experience;
    private boolean draft;
    private String updatedBy;
    private Date updatedAt;

    public PromptVersion() {
    }

    public PromptVersion(String versionId,
                         String promptId,
                         String title,
                         String description,
                         String promptText,
                         String llmTag,
                         String experience,
                         boolean draft,
                         String updatedBy,
                         Date updatedAt) {
        this.versionId = versionId;
        this.promptId = promptId;
        this.title = title;
        this.description = description;
        this.promptText = promptText;
        this.llmTag = llmTag;
        this.experience = experience;
        this.draft = draft;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getPromptId() {
        return promptId;
    }

    public void setPromptId(String promptId) {
        this.promptId = promptId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPromptText() {
        return promptText;
    }

    public String getLlmTag() {
        return llmTag;
    }

    public String getExperience() {
        return experience;
    }

    public boolean wasDraft() {
        return draft;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}

