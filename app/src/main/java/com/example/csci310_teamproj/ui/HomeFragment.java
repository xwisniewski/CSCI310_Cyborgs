package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.csci310_teamproj.domain.model.PostVersion;
import com.example.csci310_teamproj.ui.adapter.CommentAdapter;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;
import com.example.csci310_teamproj.ui.history.PostHistoryDialogFragment;
import com.example.csci310_teamproj.util.TestUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private List<Post> allPosts; // Store all posts before filtering
    private String searchQuery = ""; // Current search text
    private Set<String> selectedLlms; // Currently selected LLM filters
    private String searchMode = "Title"; // Current search mode: "Title", "Author", or "Content"
    private TextInputEditText editTextSearch;
    private TextInputLayout searchLayout;
    private android.widget.RadioGroup radioSearchMode;
    private android.widget.RadioButton radioSearchTitle;
    private android.widget.RadioButton radioSearchAuthor;
    private android.widget.RadioButton radioSearchContent;
    private Set<String> bookmarkedPostIds = new HashSet<>();
    private boolean showBookmarksOnly = false;
    private CheckBox checkboxBookmarksOnly;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = new PostRepositoryImpl();
        commentRepository = new CommentRepositoryImpl();
        voteRepository = new VoteRepositoryImpl();
        posts = new ArrayList<>();
        allPosts = new ArrayList<>();
        selectedLlms = new HashSet<>();
        selectedLlms.add("All"); // Default: show all

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
        editTextSearch = view.findViewById(R.id.editTextSearch);
        searchLayout = view.findViewById(R.id.searchLayout);
        radioSearchMode = view.findViewById(R.id.radioSearchMode);
        radioSearchTitle = view.findViewById(R.id.radioSearchTitle);
        radioSearchAuthor = view.findViewById(R.id.radioSearchAuthor);
        radioSearchContent = view.findViewById(R.id.radioSearchContent);
        checkboxBookmarksOnly = view.findViewById(R.id.checkboxBookmarksOnly);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsRecyclerView.setLayoutManager(layoutManager);
        postsRecyclerView.setNestedScrollingEnabled(true);
        postsRecyclerView.setClipToPadding(false);
        postsRecyclerView.setClipChildren(false);

        // Setup bookmarks filter checkbox
        if (checkboxBookmarksOnly != null) {
            checkboxBookmarksOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
                showBookmarksOnly = isChecked;
                applyFilter();
            });
        }

        // Setup search mode radio button listener
        if (radioSearchMode != null) {
            radioSearchMode.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioSearchTitle) {
                    searchMode = "Title";
                } else if (checkedId == R.id.radioSearchAuthor) {
                    searchMode = "Author";
                } else if (checkedId == R.id.radioSearchContent) {
                    searchMode = "Content";
                }
                applyFilter();
            });
        }

        // Setup search listener
        if (editTextSearch != null) {
            editTextSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    searchQuery = s != null ? s.toString().trim() : "";
                    applyFilter();
                }
            });
        }

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
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                }
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
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onBookmarkToggle(Post post, boolean isBookmarked) {
                if (currentUserId == null || post.getId() == null) {
                    Toast.makeText(getContext(), "Please log in to bookmark posts", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isBookmarked) {
                    postRepository.addBookmark(currentUserId, post.getId(), new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            bookmarkedPostIds.add(post.getId());
                            postAdapter.updateBookmarkState(post.getId(), true);
                            Toast.makeText(getContext(), "Post bookmarked", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error bookmarking: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    postRepository.removeBookmark(currentUserId, post.getId(), new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            bookmarkedPostIds.remove(post.getId());
                            postAdapter.updateBookmarkState(post.getId(), false);
                            Toast.makeText(getContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
                            // If showing bookmarks only, re-apply filter to hide unbookmarked post
                            if (showBookmarksOnly) {
                                applyFilter();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error removing bookmark: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onViewHistory(Post post) {
                showPostHistoryDialog(post);
            }
        });

        postsRecyclerView.setAdapter(postAdapter);

        // Setup Filter FAB
        FloatingActionButton fabFilter = view.findViewById(R.id.fabFilter);
        if (fabFilter != null) {
            fabFilter.setOnClickListener(v -> showFilterDialog());
            fabFilter.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fabCreatePost = view.findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v -> showCreateEditPostDialog(null));

        loadBookmarks();
        loadPosts();

        return view;
    }

    private void loadBookmarks() {
        if (currentUserId == null) return;

        postRepository.getUserBookmarks(currentUserId, new RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                bookmarkedPostIds.clear();
                bookmarkedPostIds.addAll(result);
                postAdapter.setBookmarkedPostIds(bookmarkedPostIds);
            }

            @Override
            public void onError(String error) {
                Log.e("HomeFragment", "Error loading bookmarks: " + error);
            }
        });
    }

    private void loadPosts() {
        postRepository.getAllPosts(new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                allPosts.clear();
                allPosts.addAll(result);
                // Apply current filter
                applyFilter();
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error loading posts: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showFilterDialog() {
        // Extract unique LLM tags from all posts
        Set<String> uniqueLlms = new HashSet<>();
        for (Post post : allPosts) {
            String llmTag = post.getLlmTag();
            if (llmTag != null && !llmTag.trim().isEmpty()) {
                uniqueLlms.add(llmTag.trim());
            }
        }
        List<String> availableLlms = new ArrayList<>(uniqueLlms);

        FilterLlmDialog dialog = FilterLlmDialog.newInstance(selectedLlms, availableLlms);
        dialog.setOnFilterAppliedListener(new FilterLlmDialog.OnFilterAppliedListener() {
            @Override
            public void onFilterApplied(Set<String> selectedLlms) {
                HomeFragment.this.selectedLlms = selectedLlms;
                applyFilter();
            }
        });
        dialog.show(getParentFragmentManager(), "FilterLlmDialog");
    }

    private void applyFilter() {
        posts.clear();
        for (Post post : allPosts) {
            // Bookmarks filter
            if (showBookmarksOnly && (post.getId() == null || !bookmarkedPostIds.contains(post.getId()))) {
                continue;
            }

            // LLM filter
            boolean llmOk;
            if (selectedLlms == null || selectedLlms.isEmpty() || selectedLlms.contains("All")) {
                llmOk = true;
            } else {
                String llmTag = post.getLlmTag();
                llmOk = false;
                if (llmTag != null) {
                    for (String selectedLlm : selectedLlms) {
                        // Case-insensitive matching
                        if (llmTag.equalsIgnoreCase(selectedLlm) ||
                            llmTag.toLowerCase().contains(selectedLlm.toLowerCase()) ||
                            selectedLlm.toLowerCase().contains(llmTag.toLowerCase())) {
                            llmOk = true;
                            break;
                        }
                    }
                }
            }

            // Search filter - check based on selected mode
            boolean searchOk;
            if (searchQuery == null || searchQuery.isEmpty()) {
                searchOk = true;
            } else {
                String query = searchQuery.toLowerCase();
                searchOk = false;

                if ("Title".equals(searchMode)) {
                    String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                    searchOk = title.contains(query);
                } else if ("Author".equals(searchMode)) {
                    String author = post.getAuthorName() != null ? post.getAuthorName().toLowerCase() : "";
                    searchOk = author.contains(query);
                } else if ("Content".equals(searchMode)) {
                    String body = post.getBody() != null ? post.getBody().toLowerCase() : "";
                    searchOk = body.contains(query);
                }
            }

            if (llmOk && searchOk) {
                posts.add(post);
            }
        }

        // Update adapter
        postAdapter.updatePosts(posts);
    }

    private void showPostHistoryDialog(Post post) {
        if (post == null || post.getId() == null) {
            Toast.makeText(getContext(), "Error: Post data unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        postRepository.getPostHistory(post.getId(), new RepositoryCallback<List<PostVersion>>() {
            @Override
            public void onSuccess(List<PostVersion> versions) {
                // Collect user IDs for name resolution
                Set<String> userIds = new HashSet<>();
                for (PostVersion v : versions) {
                    if (v.getUpdatedBy() != null) {
                        userIds.add(v.getUpdatedBy());
                    }
                }
                if (post.getAuthorId() != null) {
                    userIds.add(post.getAuthorId());
                }

                // Load user names
                loadUserNamesAndShowHistory(post, versions, userIds);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading history: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserNamesAndShowHistory(Post post, List<PostVersion> versions, Set<String> userIds) {
        HashMap<String, String> userNames = new HashMap<>();
        final int[] remaining = {userIds.size()};

        if (userIds.isEmpty()) {
            showHistoryDialog(post, versions, userNames);
            return;
        }

        for (String userId : userIds) {
            FirebaseHelper.getUserRef(userId).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                userNames.put(userId, name);
                            }
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                showHistoryDialog(post, versions, userNames);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                showHistoryDialog(post, versions, userNames);
                            }
                        }
                    });
        }
    }

    private void showHistoryDialog(Post post, List<PostVersion> versions, HashMap<String, String> userNames) {
        if (!isAdded()) return;
        
        PostHistoryDialogFragment dialog = PostHistoryDialogFragment.newInstance(
                post,
                new ArrayList<>(versions),
                userNames
        );
        dialog.show(getParentFragmentManager(), "PostHistoryDialog");
    }


    private void showCreateEditPostDialog(Post postToEdit) {

        // 🔒 PREVENT WINDOW LEAKS DURING ANDROID TESTS
//        if (TestUtils.isRunningTest()) {
//            Log.d("HomeFragment", "[TEST MODE] Skipping Create/Edit Post dialog.");
//            return;
//        }

        boolean isEditing = postToEdit != null;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_create_post, null);

        EditText titleEdit = dialogView.findViewById(R.id.editPostTitle);
        EditText tagEdit = dialogView.findViewById(R.id.editPostTag);
        EditText bodyEdit = dialogView.findViewById(R.id.editPostBody);
        Button saveButton = dialogView.findViewById(R.id.btnSavePost);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelPost);
        CheckBox anonCheck = dialogView.findViewById(R.id.checkboxAnonymous);

        if (isEditing) {
            titleEdit.setText(postToEdit.getTitle());
            tagEdit.setText(postToEdit.getLlmTag());
            bodyEdit.setText(postToEdit.getBody());
            anonCheck.setChecked(postToEdit.isAnonymous());
            saveButton.setText("Update");
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String tag = tagEdit.getText().toString().trim();
            String body = bodyEdit.getText().toString().trim();
            boolean anonymous = anonCheck.isChecked();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(tag)) {
                Toast.makeText(getContext(), "LLM Tag is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidLlmTagFormat(tag)) {
                Toast.makeText(getContext(),
                        "LLM Tag must be in format: ModelName-Version (e.g., GPT-4, Claude-4.1)",
                        Toast.LENGTH_LONG).show();
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
                postToEdit.setAnonymous(anonymous);

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
                newPost.setAnonymous(anonymous);
                newPost.setTimestamp(System.currentTimeMillis());

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
        if (post == null || post.getId() == null || post.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Cannot delete post - invalid post data", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String postId = post.getId();
                    postRepository.deletePost(postId, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPosts();
                        }

                        @Override
                        public void onError(String error) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Error deleting post: " + error, Toast.LENGTH_SHORT).show();
                            }
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
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                            }
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
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                                            }
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
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error loading comments: " + error, Toast.LENGTH_SHORT).show();
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
        CheckBox anonCheck = dialogView.findViewById(R.id.checkAnonymousComment);
        Button saveButton = dialogView.findViewById(R.id.btnSaveComment);
        Button cancelButton = dialogView.findViewById(R.id.btnCancelComment);

        // DEBUG: verify the checkbox is actually loaded
        Log.e("ANON_DEBUG", "anonCheck exists? " + (anonCheck != null));

        if (titleEdit == null || bodyEdit == null || saveButton == null || cancelButton == null) {
            Toast.makeText(getContext(), "Error: Dialog view components not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditing) {
            if (commentToEdit.getTitle() != null) {
                titleEdit.setText(commentToEdit.getTitle());
            }
            bodyEdit.setText(commentToEdit.getBody());

            // lock anonymous state
            anonCheck.setChecked(commentToEdit.isAnonymous());
            anonCheck.setEnabled(false);

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
                                showPostDetailDialog(post);
                            }

                            @Override
                            public void onError(String error) {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error updating comment: " + error, Toast.LENGTH_SHORT).show();
                                }
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
                newComment.setTimestamp(System.currentTimeMillis());

                // DEBUG: log checkbox state
                Log.e("ANON_DEBUG", "User checked anonymous? " + anonCheck.isChecked());

                // ANONYMOUS LOGIC
                if (anonCheck.isChecked()) {
                    newComment.setAnonymous(true);
                    newComment.setAuthorId("anonymous");
                    newComment.setAuthorName("Anonymous");
                } else {
                    newComment.setAnonymous(false);
                    newComment.setAuthorId(currentUserId);
                    newComment.setAuthorName(currentUserName != null ? currentUserName : "Unknown User");
                }

                commentRepository.createComment(newComment, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "Comment posted successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        showPostDetailDialog(post);
                    }

                    @Override
                    public void onError(String error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error creating comment: " + error, Toast.LENGTH_SHORT).show();
                        }
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
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Error deleting comment: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Validates LLM tag format: ModelName-Version (e.g., GPT-4, Claude-4.1)
     * Format: Starts with letter(s), dash, then version number (optionally with decimal)
     */
    private boolean isValidLlmTagFormat(String llmTag) {
        if (llmTag == null || llmTag.trim().isEmpty()) {
            return false;
        }
        // Pattern: ModelName-Version where ModelName starts with letter, Version is number (optionally with decimal)
        // Examples: GPT-4, Claude-4.1, Gemini-1.5
        String pattern = "^[A-Za-z][A-Za-z0-9]*-\\d+(\\.\\d+)?$";
        return llmTag.matches(pattern);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
        loadPosts(); // Refresh posts when fragment is resumed
    }
}
