package com.example.csci310_teamproj.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Comment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying comments.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    
    private List<Comment> comments;
    private OnCommentClickListener listener;
    private String currentUserId;

    public interface OnCommentClickListener {
        void onEditComment(Comment comment);
        void onDeleteComment(Comment comment);
        void onUpvoteComment(Comment comment);
        void onDownvoteComment(Comment comment);
    }

    public CommentAdapter(List<Comment> comments, String currentUserId, OnCommentClickListener listener) {
        this.comments = comments;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, currentUserId, listener);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView bodyText;
        private TextView authorText;
        private TextView dateText;
        private TextView editButton;
        private TextView deleteButton;
        private TextView upvoteButton;
        private TextView downvoteButton;
        private TextView upvoteCount;
        private TextView downvoteCount;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.commentTitle);
            bodyText = itemView.findViewById(R.id.commentBody);
            authorText = itemView.findViewById(R.id.commentAuthor);
            dateText = itemView.findViewById(R.id.commentDate);
            editButton = itemView.findViewById(R.id.editCommentButton);
            deleteButton = itemView.findViewById(R.id.deleteCommentButton);
            upvoteButton = itemView.findViewById(R.id.commentUpvoteButton);
            downvoteButton = itemView.findViewById(R.id.commentDownvoteButton);
            upvoteCount = itemView.findViewById(R.id.commentUpvoteCount);
            downvoteCount = itemView.findViewById(R.id.commentDownvoteCount);
        }

        public void bind(Comment comment, String currentUserId, OnCommentClickListener listener) {
            // Show title only if it exists
            if (comment.getTitle() != null && !comment.getTitle().trim().isEmpty()) {
                titleText.setText(comment.getTitle());
                titleText.setVisibility(View.VISIBLE);
            } else {
                titleText.setVisibility(View.GONE);
            }

            bodyText.setText(comment.getBody());
            authorText.setText("By " + comment.getAuthorName());
            
            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            dateText.setText(sdf.format(new Date(comment.getTimestamp())));
            
            // Set vote counts
            upvoteCount.setText(String.valueOf(comment.getUpvotes()));
            downvoteCount.setText(String.valueOf(comment.getDownvotes()));
            
            // Show edit/delete buttons only if user is the author
            boolean isAuthor = currentUserId != null && currentUserId.equals(comment.getAuthorId());
            editButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

            // Set click listeners
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditComment(comment);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteComment(comment);
                }
            });

            upvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpvoteComment(comment);
                }
            });

            downvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownvoteComment(comment);
                }
            });
        }
    }
}

