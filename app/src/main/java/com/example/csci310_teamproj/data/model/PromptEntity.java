package com.example.csci310_teamproj.data.model;

/**
 * Firebase storage representation of a Prompt.
 */
public class PromptEntity {
    public String id;
    public String title;
    public String promptText;
    public String description;
    public String llmTag;
    public String experience;
    public Long publishDate;

    // "anonymous" OR real uid (public identity)
    public String userId;

    // real UID of owner (never "anonymous")
    public String originalAuthorId;

    public Boolean isDraft;
    public Boolean hasHistory;

    public PromptEntity() {}

    public PromptEntity(
            String id,
            String title,
            String promptText,
            String description,
            String llmTag,
            String experience,
            Long publishDate,
            String userId,
            String originalAuthorId,
            Boolean isDraft,
            Boolean hasHistory
    ) {
        this.id = id;
        this.title = title;
        this.promptText = promptText;
        this.description = description;
        this.llmTag = llmTag;
        this.experience = experience;
        this.publishDate = publishDate;
        this.userId = userId;
        this.originalAuthorId = originalAuthorId;
        this.isDraft = isDraft;
        this.hasHistory = hasHistory;
    }
}
