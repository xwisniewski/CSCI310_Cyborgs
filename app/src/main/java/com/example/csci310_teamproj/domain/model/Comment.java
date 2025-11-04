package com.example.csci310_teamproj.domain.model;

/**
 * Represents a comment on a post in the BestLLM application.
 * Comments can have an optional title and body text.
 */
public class Comment {
    private String id;
    private String postId;
    private String title; // Optional
    private String body;
    private String authorId;
    private String authorName;
    private long timestamp;
    private int upvotes;
    private int downvotes;
    private boolean isDeleted;

    public Comment() {
        // Default constructor for Firebase
    }

    public Comment(String id, String postId, String title, String body, 
                   String authorId, String authorName, long timestamp) {
        this.id = id;
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.authorName = authorName;
        this.timestamp = timestamp;
        this.upvotes = 0;
        this.downvotes = 0;
        this.isDeleted = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
}

