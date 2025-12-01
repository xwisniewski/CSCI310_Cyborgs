package com.example.csci310_teamproj.domain.model;

public class Comment {

    private String id;
    private String postId;
    private String authorId;
    private String authorName;
    private String title;
    private String body;
    private long timestamp;
    private int upvotes;
    private int downvotes;

    // NEW: anonymous support
    private boolean anonymous;

    // OLD FIELD YOU HAD BEFORE — required to fix build
    private boolean isDeleted;

    // Required empty constructor for Firebase
    public Comment() {}

    // ----- ID -----
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // ----- Post ID -----
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }

    // ----- Author -----
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    // ----- Title -----
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // ----- Body -----
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    // ----- Timestamp -----
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ----- Voting -----
    public int getUpvotes() {
        return upvotes;
    }
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }
    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    // ----- Anonymous -----
    public boolean isAnonymous() {
        return anonymous;
    }
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    // ----- Deleted (soft delete) -----
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }
}
