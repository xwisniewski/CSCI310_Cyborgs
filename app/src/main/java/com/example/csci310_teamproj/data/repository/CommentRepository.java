package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Comment;
import java.util.List;

/**
 * Defines comment-related data operations.
 */
public interface CommentRepository {
    void createComment(Comment comment, RepositoryCallback<Void> callback);
    void updateComment(String commentId, Comment comment, RepositoryCallback<Void> callback);
    void deleteComment(String postId, String commentId, RepositoryCallback<Void> callback);
    void getCommentsForPost(String postId, RepositoryCallback<List<Comment>> callback);
    void getComment(String postId, String commentId, RepositoryCallback<Comment> callback);
}

