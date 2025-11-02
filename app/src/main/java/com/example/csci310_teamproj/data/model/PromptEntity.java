package com.example.csci310_teamproj.data.model;

/**
 * Data-layer representation of a Prompt.
 * Separate from domain model to allow flexibility in storage/serialization.
 */
public class PromptEntity {
    public String id;
    public String title;
    public String content;
}
