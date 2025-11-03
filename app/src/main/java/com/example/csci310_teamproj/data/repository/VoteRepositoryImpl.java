package com.example.csci310_teamproj.data.repository;

import android.util.Log;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Firebase implementation of VoteRepository.
 * Vote types: 1 = upvote, -1 = downvote, 0 = no vote
 */
public class VoteRepositoryImpl implements VoteRepository {
    private static final String TAG = "VoteRepositoryImpl";
    public static final int VOTE_UPVOTE = 1;
    public static final int VOTE_DOWNVOTE = -1;
    public static final int VOTE_NONE = 0;

    @Override
    public void voteOnPost(String postId, String userId, int voteType, RepositoryCallback<Void> callback) {
        if (postId == null || userId == null) {
            callback.onError("Post ID or User ID is null");
            return;
        }

        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId).child("posts").child(postId);
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);

        // First get current vote and post counts
        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer currentVote = snapshot.exists() ? snapshot.getValue(Integer.class) : VOTE_NONE;
                int voteChange = voteType - currentVote;

                if (voteChange == 0) {
                    // Same vote clicked - remove vote
                    removeVoteFromPost(postId, userId, callback);
                    return;
                }

                // Update user's vote
                voteRef.setValue(voteType)
                        .addOnSuccessListener(aVoid -> {
                            // Update post vote counts
                            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot postSnapshot) {
                                    int upvotes = 0;
                                    int downvotes = 0;
                                    if (postSnapshot.hasChild("upvotes")) {
                                        Long up = postSnapshot.child("upvotes").getValue(Long.class);
                                        upvotes = up != null ? up.intValue() : 0;
                                    }
                                    if (postSnapshot.hasChild("downvotes")) {
                                        Long down = postSnapshot.child("downvotes").getValue(Long.class);
                                        downvotes = down != null ? down.intValue() : 0;
                                    }

                                    // Adjust counts based on vote change
                                    if (currentVote == VOTE_UPVOTE) upvotes--;
                                    if (currentVote == VOTE_DOWNVOTE) downvotes--;
                                    if (voteType == VOTE_UPVOTE) upvotes++;
                                    if (voteType == VOTE_DOWNVOTE) downvotes++;

                                    postRef.child("upvotes").setValue(upvotes);
                                    postRef.child("downvotes").setValue(downvotes);

                                    Log.d(TAG, "Vote updated for post: " + postId);
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.e(TAG, "Error reading post votes", error.toException());
                                    callback.onError(error.getMessage());
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating vote", e);
                            callback.onError(e.getMessage());
                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error reading current vote", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void voteOnComment(String postId, String commentId, String userId, int voteType, RepositoryCallback<Void> callback) {
        if (postId == null || commentId == null || userId == null) {
            callback.onError("Post ID, Comment ID or User ID is null");
            return;
        }

        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId)
                .child("comments").child(postId).child(commentId);
        DatabaseReference commentRef = FirebaseHelper.getPostCommentsRef(postId).child(commentId);

        // Get current vote
        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer currentVote = snapshot.exists() ? snapshot.getValue(Integer.class) : VOTE_NONE;
                int voteChange = voteType - currentVote;

                if (voteChange == 0) {
                    removeVoteFromComment(postId, commentId, userId, callback);
                    return;
                }

                voteRef.setValue(voteType)
                        .addOnSuccessListener(aVoid -> {
                            commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot commentSnapshot) {
                                    int upvotes = 0;
                                    int downvotes = 0;
                                    if (commentSnapshot.hasChild("upvotes")) {
                                        Long up = commentSnapshot.child("upvotes").getValue(Long.class);
                                        upvotes = up != null ? up.intValue() : 0;
                                    }
                                    if (commentSnapshot.hasChild("downvotes")) {
                                        Long down = commentSnapshot.child("downvotes").getValue(Long.class);
                                        downvotes = down != null ? down.intValue() : 0;
                                    }

                                    if (currentVote == VOTE_UPVOTE) upvotes--;
                                    if (currentVote == VOTE_DOWNVOTE) downvotes--;
                                    if (voteType == VOTE_UPVOTE) upvotes++;
                                    if (voteType == VOTE_DOWNVOTE) downvotes++;

                                    commentRef.child("upvotes").setValue(upvotes);
                                    commentRef.child("downvotes").setValue(downvotes);

                                    Log.d(TAG, "Vote updated for comment: " + commentId);
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    callback.onError(error.getMessage());
                                }
                            });
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void removeVoteFromPost(String postId, String userId, RepositoryCallback<Void> callback) {
        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId).child("posts").child(postId);
        DatabaseReference postRef = FirebaseHelper.getPostRef(postId);

        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(null);
                    return;
                }

                Integer currentVote = snapshot.getValue(Integer.class);
                if (currentVote == null) {
                    callback.onSuccess(null);
                    return;
                }

                voteRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot postSnapshot) {
                                    int upvotes = 0;
                                    int downvotes = 0;
                                    if (postSnapshot.hasChild("upvotes")) {
                                        Long up = postSnapshot.child("upvotes").getValue(Long.class);
                                        upvotes = up != null ? up.intValue() : 0;
                                    }
                                    if (postSnapshot.hasChild("downvotes")) {
                                        Long down = postSnapshot.child("downvotes").getValue(Long.class);
                                        downvotes = down != null ? down.intValue() : 0;
                                    }

                                    if (currentVote == VOTE_UPVOTE && upvotes > 0) upvotes--;
                                    if (currentVote == VOTE_DOWNVOTE && downvotes > 0) downvotes--;

                                    postRef.child("upvotes").setValue(upvotes);
                                    postRef.child("downvotes").setValue(downvotes);

                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    callback.onError(error.getMessage());
                                }
                            });
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void removeVoteFromComment(String postId, String commentId, String userId, RepositoryCallback<Void> callback) {
        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId)
                .child("comments").child(postId).child(commentId);
        DatabaseReference commentRef = FirebaseHelper.getPostCommentsRef(postId).child(commentId);

        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(null);
                    return;
                }

                Integer currentVote = snapshot.getValue(Integer.class);
                if (currentVote == null) {
                    callback.onSuccess(null);
                    return;
                }

                voteRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot commentSnapshot) {
                                    int upvotes = 0;
                                    int downvotes = 0;
                                    if (commentSnapshot.hasChild("upvotes")) {
                                        Long up = commentSnapshot.child("upvotes").getValue(Long.class);
                                        upvotes = up != null ? up.intValue() : 0;
                                    }
                                    if (commentSnapshot.hasChild("downvotes")) {
                                        Long down = commentSnapshot.child("downvotes").getValue(Long.class);
                                        downvotes = down != null ? down.intValue() : 0;
                                    }

                                    if (currentVote == VOTE_UPVOTE && upvotes > 0) upvotes--;
                                    if (currentVote == VOTE_DOWNVOTE && downvotes > 0) downvotes--;

                                    commentRef.child("upvotes").setValue(upvotes);
                                    commentRef.child("downvotes").setValue(downvotes);

                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    callback.onError(error.getMessage());
                                }
                            });
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getUserVoteOnPost(String postId, String userId, RepositoryCallback<Integer> callback) {
        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId).child("posts").child(postId);
        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer vote = snapshot.getValue(Integer.class);
                    callback.onSuccess(vote != null ? vote : VOTE_NONE);
                } else {
                    callback.onSuccess(VOTE_NONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void getUserVoteOnComment(String postId, String commentId, String userId, RepositoryCallback<Integer> callback) {
        DatabaseReference voteRef = FirebaseHelper.getUserVotesRef(userId)
                .child("comments").child(postId).child(commentId);
        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer vote = snapshot.getValue(Integer.class);
                    callback.onSuccess(vote != null ? vote : VOTE_NONE);
                } else {
                    callback.onSuccess(VOTE_NONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}

