package com.example.csci310_teamproj.data.repository;

/**
 * Defines vote-related data operations for posts and comments.
 */
public interface VoteRepository {
    void voteOnPost(String postId, String userId, int voteType, RepositoryCallback<Void> callback);
    void voteOnComment(String postId, String commentId, String userId, int voteType, RepositoryCallback<Void> callback);
    void removeVoteFromPost(String postId, String userId, RepositoryCallback<Void> callback);
    void removeVoteFromComment(String postId, String commentId, String userId, RepositoryCallback<Void> callback);
    void getUserVoteOnPost(String postId, String userId, RepositoryCallback<Integer> callback);
    void getUserVoteOnComment(String postId, String commentId, String userId, RepositoryCallback<Integer> callback);
}

