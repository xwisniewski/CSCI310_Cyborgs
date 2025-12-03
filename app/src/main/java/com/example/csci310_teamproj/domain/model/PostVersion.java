package com.example.csci310_teamproj.domain.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single historical snapshot of a post.
 */
public class PostVersion implements Serializable {
    private String versionId;
    private String postId;
    private String title;
    private String body;
    private String llmTag;
    private boolean anonymous;
    private String updatedBy;
    private Date updatedAt;

    public PostVersion() {
    }

    public PostVersion(String versionId,
                       String postId,
                       String title,
                       String body,
                       String llmTag,
                       boolean anonymous,
                       String updatedBy,
                       Date updatedAt) {
        this.versionId = versionId;
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.llmTag = llmTag;
        this.anonymous = anonymous;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLlmTag() {
        return llmTag;
    }

    public void setLlmTag(String llmTag) {
        this.llmTag = llmTag;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}

