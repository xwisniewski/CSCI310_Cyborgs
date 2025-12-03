package com.example.csci310_teamproj.data.model;

/**
 * Firebase representation of a post history entry.
 */
public class PostVersionEntity {
    public String versionId;
    public String postId;
    public String title;
    public String body;
    public String llmTag;
    public Boolean anonymous;
    public String updatedBy;
    public Long updatedAt;

    public PostVersionEntity() {
    }

    public PostVersionEntity(String versionId,
                             String postId,
                             String title,
                             String body,
                             String llmTag,
                             Boolean anonymous,
                             String updatedBy,
                             Long updatedAt) {
        this.versionId = versionId;
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.llmTag = llmTag;
        this.anonymous = anonymous;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}

