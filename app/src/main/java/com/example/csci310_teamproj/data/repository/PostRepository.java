package com.example.csci310_teamproj.data.repository;

import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.domain.model.PostVersion;
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

    // 📌 Bookmark operations
    void addBookmark(String userId, String postId, RepositoryCallback<Void> callback);
    void removeBookmark(String userId, String postId, RepositoryCallback<Void> callback);
    void getUserBookmarks(String userId, RepositoryCallback<List<String>> callback);
    void getBookmarkedPosts(String userId, RepositoryCallback<List<Post>> callback);
    void isBookmarked(String userId, String postId, RepositoryCallback<Boolean> callback);

    // 📜 Version history operations
    void getPostHistory(String postId, RepositoryCallback<List<PostVersion>> callback);
}

