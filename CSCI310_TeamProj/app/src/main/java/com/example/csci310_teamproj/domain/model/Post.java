package com.example.csci310_teamproj.domain.model;

/**
 * Represents a post in the BestLLM application.
 * Each post contains information about a user's experience with an LLM.
 */
public class Post {
    private String id;
    private String title;
    private String body;
    private String authorId;
    private String authorName;
    private String llmTag;
    private long timestamp;
    private int upvotes;
    private int downvotes;
    private boolean isDeleted;

    public Post() {
        // Default constructor for Firebase
    }

    public Post(String id, String title, String body, String authorId, String authorName, 
                String llmTag, long timestamp) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.authorName = authorName;
        this.llmTag = llmTag;
        this.timestamp = timestamp;
        this.upvotes = 0;
        this.downvotes = 0;
        this.isDeleted = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getLlmTag() { return llmTag; }
    public void setLlmTag(String llmTag) { this.llmTag = llmTag; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
}

