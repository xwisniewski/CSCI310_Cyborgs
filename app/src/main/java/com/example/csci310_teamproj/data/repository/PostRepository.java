package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Post;
import java.util.List;

/**
 * Defines post-related data operations.
 */
public interface PostRepository {
    void createPost(Post post, RepositoryCallback<Void> callback);
    void updatePost(String postId, Post post, RepositoryCallback<Void> callback);
    void deletePost(String postId, RepositoryCallback<Void> callback);
    void getAllPosts(RepositoryCallback<List<Post>> callback);
    void getPost(String postId, RepositoryCallback<Post> callback);
    void getTrendingPosts(int limit, RepositoryCallback<List<Post>> callback);
}

