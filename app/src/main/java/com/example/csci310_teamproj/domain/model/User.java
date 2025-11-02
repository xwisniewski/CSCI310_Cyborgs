package com.example.csci310_teamproj.domain.model;

/**
 * Core domain model representing an application user.
 * This model should remain independent of data-layer structures.
 */
public class User {
    private String id;
    private String name;
    private String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
