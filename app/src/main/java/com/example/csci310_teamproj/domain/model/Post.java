package com.example.csci310_teamproj.domain.model;

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

    private boolean anonymous;   // NEW FIELD
    private boolean isDeleted;
    private boolean hasHistory;  // Track if post has version history

    // Required empty constructor for Firebase
    public Post() {}

    // 🔹 GETTERS & SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLlmTag() {
        return llmTag;
    }

    public void setLlmTag(String llmTag) {
        this.llmTag = llmTag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

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

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private int commentCount;

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean hasHistory() {
        return hasHistory;
    }

    public void setHasHistory(boolean hasHistory) {
        this.hasHistory = hasHistory;
    }

}
