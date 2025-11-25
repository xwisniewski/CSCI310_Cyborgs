package com.example.csci310_teamproj.data.model;

/**
 * Firebase representation of a prompt history entry.
 */
public class PromptVersionEntity {
    public String versionId;
    public String promptId;
    public String title;
    public String description;
    public String promptText;
    public String llmTag;
    public String experience;
    public Boolean draft;
    public String updatedBy;
    public Long updatedAt;

    public PromptVersionEntity() {
    }

    public PromptVersionEntity(String versionId,
                               String promptId,
                               String title,
                               String description,
                               String promptText,
                               String llmTag,
                               String experience,
                               Boolean draft,
                               String updatedBy,
                               Long updatedAt) {
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
}

