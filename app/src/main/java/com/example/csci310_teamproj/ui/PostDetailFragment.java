package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailFragment extends Fragment {

    private static final String ARG_POST_ID = "postId";

    private TextView titleView;
    private TextView authorView;
    private TextView tagView;
    private TextView dateView;
    private TextView bodyView;
    private TextView upvoteButton;
    private TextView downvoteButton;
    private TextView upvoteCount;
    private TextView downvoteCount;
    private TextView editButton;
    private TextView deleteButton;
    private RecyclerView commentsRecyclerView;
    private Button addCommentButton;
    private FloatingActionButton backButton;

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private VoteRepository voteRepository;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;

    private String currentUserId;
    private String currentUserName;
    private Post currentPost;

    // -----------------------------
    // SAFE TOAST helper (prevents crashes in UI tests)
    // -----------------------------
    private void safeToast(String msg) {
        if (!isAdded() || getActivity() == null) return;
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = new PostRepositoryImpl();
        commentRepository = new CommentRepositoryImpl();
        voteRepository = new VoteRepositoryImpl();
        comments = new ArrayList<>();

        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserName = currentUser.getEmail();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleView = view.findViewById(R.id.postDetailTitle);
        authorView = view.findViewById(R.id.postDetailAuthor);
        tagView = view.findViewById(R.id.postDetailTag);
        dateView = view.findViewById(R.id.postDetailDate);
        bodyView = view.findViewById(R.id.postDetailBody);
        upvoteButton = view.findViewById(R.id.upvoteButton);
        downvoteButton = view.findViewById(R.id.downvoteButton);
        upvoteCount = view.findViewById(R.id.upvoteCount);
        downvoteCount = view.findViewById(R.id.downvoteCount);
        editButton = view.findViewById(R.id.editPostButton);
        deleteButton = view.findViewById(R.id.deletePostButton);
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        addCommentButton = view.findViewById(R.id.btnAddComment);
        backButton = view.findViewById(R.id.btnBack);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(comments, currentUserId,
                new CommentAdapter.OnCommentClickListener() {
                    @Override
                    public void onEditComment(Comment comment) {
                        showCreateEditCommentDialog(comment);
                    }

                    @Override
                    public void onDeleteComment(Comment comment) {
                        showDeleteCommentConfirmation(comment);
                    }

                    @Override
                    public void onUpvoteComment(Comment comment) {
                        if (currentUserId != null && currentPost != null && currentPost.getId() != null && comment.getId() != null) {
                            voteRepository.voteOnComment(currentPost.getId(), comment.getId(), currentUserId,
                                    VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            loadComments();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            safeToast("Error voting: " + error);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onDownvoteComment(Comment comment) {
                        if (currentUserId != null && currentPost != null && currentPost.getId() != null && comment.getId() != null) {
                            voteRepository.voteOnComment(currentPost.getId(), comment.getId(), currentUserId,
                                    VoteRepositoryImpl.VOTE_DOWNVOTE, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            loadComments();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            safeToast("Error voting: " + error);
                                        }
                                    });
                        }
                    }
                });

        commentsRecyclerView.setAdapter(commentAdapter);

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                Navigation.findNavController(v).navigateUp();
            }
        });

        addCommentButton.setOnClickListener(v -> showCreateEditCommentDialog(null));

        upvoteButton.setOnClickListener(v -> {
            if (currentPost != null && currentUserId != null && currentPost.getId() != null) {
                String postId = currentPost.getId();
                voteRepository.voteOnPost(postId, currentUserId,
                        VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadPostById(postId);
                            }

                            @Override
                            public void onError(String error) {
                                safeToast("Error voting: " + error);
                            }
                        });
            }
        });

        downvoteButton.setOnClickListener(v -> {
            if (currentPost != null && currentUserId != null && currentPost.getId() != null) {
                String postId = currentPost.getId();
                voteRepository.voteOnPost(postId, currentUserId,
                        VoteRepositoryImpl.VOTE_DOWNVOTE, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadPostById(postId);
                            }

                            @Override
                            public void onError(String error) {
                                safeToast("Error voting: " + error);
                            }
                        });
            }
        });

        editButton.setOnClickListener(v -> {
            if (currentPost != null) showCreateEditPostDialog();
        });

        deleteButton.setOnClickListener(v -> {
            if (currentPost != null) showDeletePostConfirmation();
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_POST_ID)) {
            String postId = args.getString(ARG_POST_ID);
            if (currentPost != null && currentPost.getId() != null && currentPost.getId().equals(postId)) {
                loadPost();
                loadComments();
            } else {
                loadPostById(postId);
            }
        } else {
            safeToast("Error: Post ID not found");
            if (getActivity() != null)
                Navigation.findNavController(view).navigateUp();
        }
    }

    private void loadPostById(String postId) {
        postRepository.getPost(postId, new RepositoryCallback<Post>() {
            @Override
            public void onSuccess(Post post) {
                currentPost = post;
                loadPost();
                loadComments();
            }

            @Override
            public void onError(String error) {
                safeToast("Error loading post: " + error);
                if (getView() != null && getActivity() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            }
        });
    }

    private void loadPost() {
        if (currentPost == null) return;

        titleView.setText(currentPost.getTitle() != null ? currentPost.getTitle() : "");
        authorView.setText("By " + (currentPost.getAuthorName() != null ? currentPost.getAuthorName() : "Unknown"));
        tagView.setText(currentPost.getLlmTag() != null ? currentPost.getLlmTag() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateView.setText(sdf.format(new Date(currentPost.getTimestamp())));

        bodyView.setText(currentPost.getBody() != null ? currentPost.getBody() : "");
        upvoteCount.setText(String.valueOf(currentPost.getUpvotes()));
        downvoteCount.setText(String.valueOf(currentPost.getDownvotes()));

        boolean isAuthor = currentUserId != null && currentUserId.equals(currentPost.getAuthorId());
        editButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
    }

    private void loadComments() {
        if (currentPost == null || currentPost.getId() == null) return;

        commentRepository.getCommentsForPost(currentPost.getId(), new RepositoryCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                comments.clear();
                comments.addAll(result);
                commentAdapter.updateComments(comments);
            }

            @Override
            public void onError(String error) {
                safeToast("Error loading comments: " + error);
            }
        });
    }

    private void showCreateEditPostDialog() {
        if (currentPost == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_post, null);

        EditText titleEdit = dialogView.findViewById(R.id.editPostTitle);
        EditText tagEdit = dialogView.findViewById(R.id.editPostTag);
        EditText bodyEdit = dialogView.findViewById(R.id.editPostBody);
        Button saveButton = dialogView.findViewById(R.id.btnSavePost);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelPost);

        titleEdit.setText(currentPost.getTitle());
        tagEdit.setText(currentPost.getLlmTag());
        bodyEdit.setText(currentPost.getBody());
        saveButton.setText("Update");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String tag = tagEdit.getText().toString().trim();
            String body = bodyEdit.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                safeToast("Title is required");
                return;
            }
            if (TextUtils.isEmpty(tag)) {
                safeToast("LLM Tag is required");
                return;
            }
            if (!isValidLlmTagFormat(tag)) {
                safeToast("LLM Tag must be in format: ModelName-Version (e.g., GPT-4, Claude-4.1)");
                return;
            }
            if (TextUtils.isEmpty(body)) {
                safeToast("Body is required");
                return;
            }

            currentPost.setTitle(title);
            currentPost.setLlmTag(tag);
            currentPost.setBody(body);

            postRepository.updatePost(currentPost.getId(), currentPost, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    safeToast("Post updated successfully");
                    dialog.dismiss();
                    loadPost();
                }

                @Override
                public void onError(String error) {
                    safeToast("Error updating post: " + error);
                }
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeletePostConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    postRepository.deletePost(currentPost.getId(), new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            safeToast("Post deleted successfully");
                            if (getView() != null && getActivity() != null)
                                Navigation.findNavController(getView()).navigateUp();
                        }

                        @Override
                        public void onError(String error) {
                            safeToast("Error deleting post: " + error);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateEditCommentDialog(Comment commentToEdit) {
        if (currentPost == null) {
            safeToast("Error: Post data unavailable");
            return;
        }

        boolean isEditing = commentToEdit != null;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_create_comment, null);

        EditText titleEdit = dialogView.findViewById(R.id.editCommentTitle);
        EditText bodyEdit = dialogView.findViewById(R.id.editCommentBody);
        Button saveButton = dialogView.findViewById(R.id.btnSaveComment);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelComment);

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
                safeToast("Comment body is required");
                return;
            }

            if (isEditing) {
                commentToEdit.setTitle(title.isEmpty() ? null : title);
                commentToEdit.setBody(body);

                commentRepository.updateComment(commentToEdit.getId(), commentToEdit, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        safeToast("Comment updated successfully");
                        dialog.dismiss();
                        loadComments();
                    }

                    @Override
                    public void onError(String error) {
                        safeToast("Error updating comment: " + error);
                    }
                });
            } else {
                if (currentPost.getId() == null || currentUserId == null) {
                    safeToast("Error: Missing required data");
                    return;
                }

                Comment newComment = new Comment();
                newComment.setPostId(currentPost.getId());
                newComment.setTitle(title.isEmpty() ? null : title);
                newComment.setBody(body);
                newComment.setAuthorId(currentUserId);
                newComment.setAuthorName(currentUserName != null ? currentUserName : "Unknown User");

                commentRepository.createComment(newComment, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        safeToast("Comment posted successfully");
                        dialog.dismiss();
                        loadComments();
                    }

                    @Override
                    public void onError(String error) {
                        safeToast("Error creating comment: " + error);
                    }
                });
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteCommentConfirmation(Comment comment) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    commentRepository.deleteComment(currentPost.getId(), comment.getId(),
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    safeToast("Comment deleted successfully");
                                    loadComments();
                                }

                                @Override
                                public void onError(String error) {
                                    safeToast("Error deleting comment: " + error);
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isValidLlmTagFormat(String llmTag) {
        if (llmTag == null || llmTag.trim().isEmpty()) return false;
        String pattern = "^[A-Za-z][A-Za-z0-9]*-\\d+(\\.\\d+)?$";
        return llmTag.matches(pattern);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentPost != null) {
            loadPost();
            loadComments();
        }
    }
}
