package com.example.csci310_teamproj.domain.model;

/**
 * Represents a prompt entity used in the app (e.g., AI prompt or user-submitted idea).
 */
public class Prompt {
    private String id;
    private String title;
    private String content;

    public Prompt(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
