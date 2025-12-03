package com.example.csci310_teamproj.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.repository.PostRepository;
import com.example.csci310_teamproj.data.repository.PostRepositoryImpl;
import com.example.csci310_teamproj.data.repository.RepositoryCallback;
import com.example.csci310_teamproj.data.repository.VoteRepository;
import com.example.csci310_teamproj.data.repository.VoteRepositoryImpl;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.domain.model.PostVersion;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;
import com.example.csci310_teamproj.ui.history.PostHistoryDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TrendingFragment displays posts ranked by upvotes.
 */
public class TrendingFragment extends Fragment {

    private RecyclerView trendingRecyclerView;
    private PostAdapter postAdapter;
    private PostRepository postRepository;
    private VoteRepository voteRepository;
    private String currentUserId;
    private List<Post> trendingPosts;
    private Set<String> bookmarkedPostIds = new HashSet<>();
    private static final int TRENDING_LIMIT = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = new PostRepositoryImpl();
        voteRepository = new VoteRepositoryImpl();
        trendingPosts = new ArrayList<>();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trending, container, false);

        trendingRecyclerView = view.findViewById(R.id.trendingRecyclerView);
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        trendingRecyclerView.setNestedScrollingEnabled(true);

        postAdapter = new PostAdapter(trendingPosts, currentUserId,
                new PostAdapter.OnPostClickListener() {

                    @Override
                    public void onPostClick(Post post) {
                        if (post.getId() != null) {
                            Bundle args = new Bundle();
                            args.putString("postId", post.getId());
                            Navigation.findNavController(view).navigate(R.id.postDetailFragment, args);
                        }
                    }

                    @Override
                    public void onEditPost(Post post) { }

                    @Override
                    public void onDeletePost(Post post) { }

                    @Override
                    public void onUpvotePost(Post post) {
                        if (currentUserId != null && post.getId() != null) {
                            voteRepository.voteOnPost(post.getId(), currentUserId,
                                    VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result) {
                                            loadTrendingPosts();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(),
                                                        "Error voting: " + error, Toast.LENGTH_SHORT).show();
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
                                            loadTrendingPosts();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(),
                                                        "Error voting: " + error, Toast.LENGTH_SHORT).show();
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

        trendingRecyclerView.setAdapter(postAdapter);
        loadBookmarks();
        loadTrendingPosts();
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
                Log.e("TrendingFragment", "Error loading bookmarks: " + error);
            }
        });
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

    private void loadTrendingPosts() {
        postRepository.getTrendingPosts(TRENDING_LIMIT,
                new RepositoryCallback<List<Post>>() {

                    @Override
                    public void onSuccess(List<Post> result) {
                        trendingPosts = result;
                        if (postAdapter != null) {
                            postAdapter.updatePosts(trendingPosts);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Error loading trending posts: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
        loadTrendingPosts();
    }
}
