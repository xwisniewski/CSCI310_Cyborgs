package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.repository.CommentRepository;
import com.example.csci310_teamproj.data.repository.CommentRepositoryImpl;
import com.example.csci310_teamproj.data.repository.PostRepository;
import com.example.csci310_teamproj.data.repository.PostRepositoryImpl;
import com.example.csci310_teamproj.data.repository.RepositoryCallback;
import com.example.csci310_teamproj.data.repository.VoteRepository;
import com.example.csci310_teamproj.data.repository.VoteRepositoryImpl;
import com.example.csci310_teamproj.domain.model.Comment;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.ui.adapter.CommentAdapter;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HomeFragment displays all posts and allows users to create, edit, and delete posts,
 * as well as view and manage comments on posts.
 */
public class HomeFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private VoteRepository voteRepository;
    private String currentUserId;
    private String currentUserName;
    private List<Post> posts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = new PostRepositoryImpl();
        commentRepository = new CommentRepositoryImpl();
        voteRepository = new VoteRepositoryImpl();
        posts = new ArrayList<>();
        
        // Get current user info
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // Set default username from email (will be updated if name exists in database)
            currentUserName = currentUser.getEmail();
            // Get user name from Firebase Realtime Database
            DatabaseReference userRef = FirebaseHelper.getUserRef(currentUserId);
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        if (name != null && !name.trim().isEmpty()) {
                            currentUserName = name;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Keep default email as username
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        postAdapter = new PostAdapter(posts, currentUserId, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
                if (post.getId() != null) {
                    // Navigate to post detail fragment
                    Bundle args = new Bundle();
                    args.putString("postId", post.getId());
                    Navigation.findNavController(view).navigate(R.id.postDetailFragment, args);
                }
            }

            @Override
            public void onEditPost(Post post) {
                showCreateEditPostDialog(post);
            }

            @Override
            public void onDeletePost(Post post) {
                showDeletePostConfirmation(post);
            }

            @Override
            public void onUpvotePost(Post post) {
                if (currentUserId != null && post.getId() != null) {
                    voteRepository.voteOnPost(post.getId(), currentUserId, 
                            VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadPosts(); // Refresh to show updated vote counts
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDownvotePost(Post post) {
                if (currentUserId != null && post.getId() != null) {
                    voteRepository.voteOnPost(post.getId(), currentUserId, 
                            VoteRepositoryImpl.VOTE_DOWNVOTE, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadPosts(); // Refresh to show updated vote counts
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        
        postsRecyclerView.setAdapter(postAdapter);

        FloatingActionButton fabCreatePost = view.findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v -> showCreateEditPostDialog(null));

        loadPosts();

        return view;
    }

    private void loadPosts() {
        postRepository.getAllPosts(new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                posts = result;
                postAdapter.updatePosts(posts);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading posts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateEditPostDialog(Post postToEdit) {
        boolean isEditing = postToEdit != null;
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_create_post, null);

        EditText titleEdit = dialogView.findViewById(R.id.editPostTitle);
        EditText tagEdit = dialogView.findViewById(R.id.editPostTag);
        EditText bodyEdit = dialogView.findViewById(R.id.editPostBody);
        Button saveButton = dialogView.findViewById(R.id.btnSavePost);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelPost);

        if (isEditing) {
            titleEdit.setText(postToEdit.getTitle());
            tagEdit.setText(postToEdit.getLlmTag());
            bodyEdit.setText(postToEdit.getBody());
            saveButton.setText("Update");
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String tag = tagEdit.getText().toString().trim();
            String body = bodyEdit.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(tag)) {
                Toast.makeText(getContext(), "LLM Tag is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(body)) {
                Toast.makeText(getContext(), "Body is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                postToEdit.setTitle(title);
                postToEdit.setLlmTag(tag);
                postToEdit.setBody(body);
                postRepository.updatePost(postToEdit.getId(), postToEdit, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "Post updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadPosts();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error updating post: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Post newPost = new Post();
                newPost.setTitle(title);
                newPost.setLlmTag(tag);
                newPost.setBody(body);
                newPost.setAuthorId(currentUserId);
                newPost.setAuthorName(currentUserName != null ? currentUserName : "Unknown User");
                
                postRepository.createPost(newPost, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadPosts();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error creating post: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeletePostConfirmation(Post post) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    postRepository.deletePost(post.getId(), new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPosts();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error deleting post: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPostDetailDialog(Post post) {
        if (post == null || getContext() == null) {
            Toast.makeText(getContext(), "Error: Post data unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_post_detail, null);

        // Set post details
        android.widget.TextView titleView = dialogView.findViewById(R.id.postDetailTitle);
        android.widget.TextView authorView = dialogView.findViewById(R.id.postDetailAuthor);
        android.widget.TextView tagView = dialogView.findViewById(R.id.postDetailTag);
        android.widget.TextView bodyView = dialogView.findViewById(R.id.postDetailBody);

        if (titleView != null) titleView.setText(post.getTitle() != null ? post.getTitle() : "");
        if (authorView != null) authorView.setText("By " + (post.getAuthorName() != null ? post.getAuthorName() : "Unknown"));
        if (tagView != null) tagView.setText(post.getLlmTag() != null ? post.getLlmTag() : "");
        if (bodyView != null) bodyView.setText(post.getBody() != null ? post.getBody() : "");

        // Setup comments RecyclerView
        RecyclerView commentsRecyclerView = dialogView.findViewById(R.id.commentsRecyclerView);
        
        // Declare these outside the if block to ensure they're accessible in callbacks
        final List<Comment> comments = new ArrayList<>();
        final CommentAdapter[] commentAdapterRef = new CommentAdapter[1];
        
        if (commentsRecyclerView != null && getContext() != null) {
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            
            CommentAdapter commentAdapter = new CommentAdapter(comments, currentUserId, 
                new CommentAdapter.OnCommentClickListener() {
                    @Override
                    public void onEditComment(Comment comment) {
                        showCreateEditCommentDialog(post, comment);
                    }

                    @Override
                    public void onDeleteComment(Comment comment) {
                        showDeleteCommentConfirmation(post, comment);
                    }

                    @Override
                    public void onUpvoteComment(Comment comment) {
                        if (currentUserId != null && post.getId() != null && comment.getId() != null) {
                            voteRepository.voteOnComment(post.getId(), comment.getId(), currentUserId,
                                    VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    // Refresh comments to show updated vote counts
                                    commentRepository.getCommentsForPost(post.getId(), new RepositoryCallback<List<Comment>>() {
                                        @Override
                                        public void onSuccess(List<Comment> result) {
                                            comments.clear();
                                            comments.addAll(result);
                                            if (commentAdapterRef[0] != null) {
                                                commentAdapterRef[0].updateComments(comments);
                                            }
                                        }

                                        @Override
                                        public void onError(String error) {
                                            // Silent fail
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onDownvoteComment(Comment comment) {
                        if (currentUserId != null && post.getId() != null && comment.getId() != null) {
                            voteRepository.voteOnComment(post.getId(), comment.getId(), currentUserId,
                                    VoteRepositoryImpl.VOTE_DOWNVOTE, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    // Refresh comments to show updated vote counts
                                    commentRepository.getCommentsForPost(post.getId(), new RepositoryCallback<List<Comment>>() {
                                        @Override
                                        public void onSuccess(List<Comment> result) {
                                            comments.clear();
                                            comments.addAll(result);
                                            if (commentAdapterRef[0] != null) {
                                                commentAdapterRef[0].updateComments(comments);
                                            }
                                        }

                                        @Override
                                        public void onError(String error) {
                                            // Silent fail
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            commentAdapterRef[0] = commentAdapter;
            commentsRecyclerView.setAdapter(commentAdapter);

            // Load comments
            if (post.getId() != null) {
                commentRepository.getCommentsForPost(post.getId(), new RepositoryCallback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> result) {
                        comments.clear();
                        comments.addAll(result);
                        if (commentAdapterRef[0] != null) {
                            commentAdapterRef[0].updateComments(comments);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading comments: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .create();

        Button addCommentButton = dialogView.findViewById(R.id.btnAddComment);
        if (addCommentButton != null) {
            addCommentButton.setOnClickListener(v -> {
                dialog.dismiss();
                showCreateEditCommentDialog(post, null);
            });
        }
        
        dialog.show();
    }

    private void showCreateEditCommentDialog(Post post, Comment commentToEdit) {
        if (post == null || getContext() == null) {
            Toast.makeText(getContext(), "Error: Post data unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isEditing = commentToEdit != null;
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_create_comment, null);

        EditText titleEdit = dialogView.findViewById(R.id.editCommentTitle);
        EditText bodyEdit = dialogView.findViewById(R.id.editCommentBody);
        Button saveButton = dialogView.findViewById(R.id.btnSaveComment);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelComment);

        if (titleEdit == null || bodyEdit == null || saveButton == null || cancelButton == null) {
            Toast.makeText(getContext(), "Error: Dialog view components not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditing) {
            if (commentToEdit.getTitle() != null) {
                titleEdit.setText(commentToEdit.getTitle());
            }
            bodyEdit.setText(commentToEdit.getBody());
            saveButton.setText("Update");
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String body = bodyEdit.getText().toString().trim();

            if (TextUtils.isEmpty(body)) {
                Toast.makeText(getContext(), "Comment body is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                commentToEdit.setTitle(title.isEmpty() ? null : title);
                commentToEdit.setBody(body);
                commentRepository.updateComment(commentToEdit.getId(), commentToEdit, 
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(getContext(), "Comment updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                showPostDetailDialog(post); // Refresh to show updated comments
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Error updating comment: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                if (post.getId() == null || currentUserId == null) {
                    Toast.makeText(getContext(), "Error: Missing required data", Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment newComment = new Comment();
                newComment.setPostId(post.getId());
                newComment.setTitle(title.isEmpty() ? null : title);
                newComment.setBody(body);
                newComment.setAuthorId(currentUserId);
                newComment.setAuthorName(currentUserName != null ? currentUserName : "Unknown User");

                commentRepository.createComment(newComment, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "Comment posted successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        showPostDetailDialog(post); // Refresh to show new comment
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error creating comment: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteCommentConfirmation(Post post, Comment comment) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    commentRepository.deleteComment(post.getId(), comment.getId(), 
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Toast.makeText(getContext(), "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                                    showPostDetailDialog(post); // Refresh to remove deleted comment
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Error deleting comment: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts(); // Refresh posts when fragment is resumed
    }
}
