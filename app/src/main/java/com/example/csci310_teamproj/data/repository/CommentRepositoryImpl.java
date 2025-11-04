package com.example.csci310_teamproj.data.repository;

import android.util.Log;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.domain.model.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase implementation of CommentRepository.
 */
public class CommentRepositoryImpl implements CommentRepository {
    private static final String TAG = "CommentRepositoryImpl";

    @Override
    public void createComment(Comment comment, RepositoryCallback<Void> callback) {
        DatabaseReference commentsRef = FirebaseHelper.getPostCommentsRef(comment.getPostId());
        String commentId = commentsRef.push().getKey();
        if (commentId == null) {
            callback.onError("Failed to generate comment ID");
            return;
        }

        comment.setId(commentId);
        comment.setTimestamp(System.currentTimeMillis());
        comment.setDeleted(false);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", comment.getId());
        commentMap.put("postId", comment.getPostId());
        commentMap.put("title", comment.getTitle());
        commentMap.put("body", comment.getBody());
        commentMap.put("authorId", comment.getAuthorId());
        commentMap.put("authorName", comment.getAuthorName());
        commentMap.put("timestamp", comment.getTimestamp());
        commentMap.put("upvotes", comment.getUpvotes());
        commentMap.put("downvotes", comment.getDownvotes());
        commentMap.put("isDeleted", comment.isDeleted());

        commentsRef.child(commentId).setValue(commentMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment created successfully: " + commentId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating comment", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateComment(String commentId, Comment comment, RepositoryCallback<Void> callback) {
        DatabaseReference commentRef = FirebaseHelper.getPostCommentsRef(comment.getPostId())
                .child(commentId);
        
        Map<String, Object> updates = new HashMap<>();
        if (comment.getTitle() != null) {
            updates.put("title", comment.getTitle());
        }
        updates.put("body", comment.getBody());

        commentRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment updated successfully: " + commentId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating comment", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void deleteComment(String postId, String commentId, RepositoryCallback<Void> callback) {
        // Soft delete: set isDeleted to true
        DatabaseReference commentRef = FirebaseHelper.getPostCommentsRef(postId).child(commentId);
        commentRef.child("isDeleted").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment soft deleted: " + commentId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting comment", e);
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getCommentsForPost(String postId, RepositoryCallback<List<Comment>> callback) {
        DatabaseReference commentsRef = FirebaseHelper.getPostCommentsRef(postId);
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null && !comment.isDeleted()) {
                        comments.add(comment);
                    }
                }
                // Sort by timestamp ascending (oldest first)
                comments.sort((c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()));
                callback.onSuccess(comments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching comments", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getComment(String postId, String commentId, RepositoryCallback<Comment> callback) {
        DatabaseReference commentRef = FirebaseHelper.getPostCommentsRef(postId).child(commentId);
        commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Comment comment = snapshot.getValue(Comment.class);
                if (comment != null && !comment.isDeleted()) {
                    callback.onSuccess(comment);
                } else {
                    callback.onError("Comment not found or deleted");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching comment", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }
}

