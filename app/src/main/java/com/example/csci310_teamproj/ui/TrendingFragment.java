package com.example.csci310_teamproj.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.repository.PostRepository;
import com.example.csci310_teamproj.data.repository.PostRepositoryImpl;
import com.example.csci310_teamproj.data.repository.RepositoryCallback;
import com.example.csci310_teamproj.data.repository.VoteRepository;
import com.example.csci310_teamproj.data.repository.VoteRepositoryImpl;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.ui.adapter.PostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

/**
 * TrendingFragment displays posts ranked by upvotes (trending posts).
 * Posts are sorted by upvotes in descending order and limited to top K.
 */
public class TrendingFragment extends Fragment {

    private RecyclerView trendingRecyclerView;
    private PostAdapter postAdapter;
    private PostRepository postRepository;
    private VoteRepository voteRepository;
    private String currentUserId;
    private List<Post> trendingPosts;
    private static final int TRENDING_LIMIT = 5; // Top 5 trending posts

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = new PostRepositoryImpl();
        voteRepository = new VoteRepositoryImpl();
        trendingPosts = new ArrayList<>();
        
        // Get current user ID
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
        
        postAdapter = new PostAdapter(trendingPosts, currentUserId, new PostAdapter.OnPostClickListener() {
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
                // Can be implemented if needed, or disabled for trending view
            }

            @Override
            public void onDeletePost(Post post) {
                // Can be implemented if needed, or disabled for trending view
            }

            @Override
            public void onUpvotePost(Post post) {
                if (currentUserId != null && post.getId() != null) {
                    voteRepository.voteOnPost(post.getId(), currentUserId, 
                            VoteRepositoryImpl.VOTE_UPVOTE, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadTrendingPosts(); // Refresh to show updated vote counts
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
                            loadTrendingPosts(); // Refresh to show updated vote counts
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error voting: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        
        trendingRecyclerView.setAdapter(postAdapter);

        loadTrendingPosts();

        return view;
    }

    private void loadTrendingPosts() {
        postRepository.getTrendingPosts(TRENDING_LIMIT, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                trendingPosts = result;
                postAdapter.updatePosts(trendingPosts);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading trending posts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTrendingPosts(); // Refresh trending posts when fragment is resumed
    }
}

