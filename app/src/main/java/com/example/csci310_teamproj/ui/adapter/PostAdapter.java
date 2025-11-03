package com.example.csci310_teamproj.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Post;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying posts.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    
    private List<Post> posts;
    private OnPostClickListener listener;
    private String currentUserId;

    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onEditPost(Post post);
        void onDeletePost(Post post);
        void onUpvotePost(Post post);
        void onDownvotePost(Post post);
    }

    public PostAdapter(List<Post> posts, String currentUserId, OnPostClickListener listener) {
        this.posts = posts;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, currentUserId, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView tagText;
        private TextView authorText;
        private TextView dateText;
        private TextView bodyText;
        private TextView commentCountText;
        private TextView editButton;
        private TextView deleteButton;
        private TextView upvoteButton;
        private TextView downvoteButton;
        private TextView upvoteCount;
        private TextView downvoteCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.postTitle);
            tagText = itemView.findViewById(R.id.postTag);
            authorText = itemView.findViewById(R.id.postAuthor);
            dateText = itemView.findViewById(R.id.postDate);
            bodyText = itemView.findViewById(R.id.postBody);
            commentCountText = itemView.findViewById(R.id.commentCount);
            editButton = itemView.findViewById(R.id.editPostButton);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            downvoteButton = itemView.findViewById(R.id.downvoteButton);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            downvoteCount = itemView.findViewById(R.id.downvoteCount);
        }

        public void bind(Post post, String currentUserId, OnPostClickListener listener) {
            titleText.setText(post.getTitle());
            tagText.setText(post.getLlmTag());
            authorText.setText("By " + post.getAuthorName());
            
            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText(sdf.format(new Date(post.getTimestamp())));
            
            bodyText.setText(post.getBody());
            
            // Initialize comment count (will be updated when comments are loaded)
            commentCountText.setText("💬 View Comments");
            
            // Set vote counts
            upvoteCount.setText(String.valueOf(post.getUpvotes()));
            downvoteCount.setText(String.valueOf(post.getDownvotes()));
            
            // Show edit/delete buttons only if user is the author
            boolean isAuthor = currentUserId != null && currentUserId.equals(post.getAuthorId());
            editButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

            // Set click listeners
            // Handle clicks on the card body/text areas (not buttons)
            bodyText.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
            
            titleText.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditPost(post);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePost(post);
                }
            });

            upvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpvotePost(post);
                }
            });

            downvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownvotePost(post);
                }
            });
            
            commentCountText.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
    }
}

