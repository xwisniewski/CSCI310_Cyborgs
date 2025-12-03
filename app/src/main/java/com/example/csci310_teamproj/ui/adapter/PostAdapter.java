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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RecyclerView adapter for displaying posts.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private OnPostClickListener listener;
    private String currentUserId;
    private Set<String> bookmarkedPostIds = new HashSet<>();

    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onEditPost(Post post);
        void onDeletePost(Post post);
        void onUpvotePost(Post post);
        void onDownvotePost(Post post);
        void onBookmarkToggle(Post post, boolean isBookmarked);
        void onViewHistory(Post post);
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
        boolean isBookmarked = bookmarkedPostIds.contains(post.getId());
        holder.bind(post, currentUserId, isBookmarked, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    public void setBookmarkedPostIds(Set<String> bookmarkedIds) {
        this.bookmarkedPostIds = bookmarkedIds != null ? bookmarkedIds : new HashSet<>();
        notifyDataSetChanged();
    }

    public void updateBookmarkState(String postId, boolean isBookmarked) {
        if (isBookmarked) {
            bookmarkedPostIds.add(postId);
        } else {
            bookmarkedPostIds.remove(postId);
        }
        // Find the position and notify
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() != null && posts.get(i).getId().equals(postId)) {
                notifyItemChanged(i);
                break;
            }
        }
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
        private TextView bookmarkButton;
        private TextView historyButton;

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
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            historyButton = itemView.findViewById(R.id.historyButton);
        }

        public void bind(Post post, String currentUserId, boolean isBookmarked, OnPostClickListener listener) {

            // Title, tag, body
            titleText.setText(post.getTitle());
            tagText.setText(post.getLlmTag());
            bodyText.setText(post.getBody());

            // Timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText(sdf.format(new Date(post.getTimestamp())));

            // 🔥 ANONYMOUS POST LOGIC
            if (post.isAnonymous()) {
                authorText.setText("By Anonymous");
            } else {
                authorText.setText("By " + post.getAuthorName());
            }

            // Votes
            upvoteCount.setText(String.valueOf(post.getUpvotes()));
            downvoteCount.setText(String.valueOf(post.getDownvotes()));

            // ⭐ COMMENT COUNT
            commentCountText.setText("💬 " + post.getCommentCount());

            // 📌 BOOKMARK BUTTON
            bookmarkButton.setText(isBookmarked ? "🔖" : "🏷️");
            bookmarkButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkToggle(post, !isBookmarked);
                }
            });

            // 📜 HISTORY BUTTON - always visible, shows history or "no edits" message
            historyButton.setVisibility(View.VISIBLE);
            historyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewHistory(post);
                }
            });

            // Show edit/delete if user is actual author (even if anonymous)
            boolean isAuthor = currentUserId != null && currentUserId.equals(post.getAuthorId());
            editButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

            // Clicking → open post
            titleText.setOnClickListener(v -> {
                if (listener != null) listener.onPostClick(post);
            });
            bodyText.setOnClickListener(v -> {
                if (listener != null) listener.onPostClick(post);
            });
            commentCountText.setOnClickListener(v -> {
                if (listener != null) listener.onPostClick(post);
            });

            // Action buttons
            editButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditPost(post);
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeletePost(post);
            });

            upvoteButton.setOnClickListener(v -> {
                if (listener != null) listener.onUpvotePost(post);
            });

            downvoteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDownvotePost(post);
            });
        }
    }
}
