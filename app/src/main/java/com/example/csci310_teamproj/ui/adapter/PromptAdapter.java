package com.example.csci310_teamproj.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Prompt;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PromptAdapter extends RecyclerView.Adapter<PromptAdapter.PromptViewHolder> {

    private List<Prompt> prompts;
    private String currentUserId;
    private java.util.Map<String, String> userIdToNameMap;
    private OnPromptClickListener listener;
    private java.util.Set<String> favoriteIds = new java.util.HashSet<>();

    public interface OnPromptClickListener {
        void onPromptClick(Prompt prompt);
        void onEditClick(Prompt prompt);
        void onDeleteClick(Prompt prompt);
        void onFavoriteToggle(Prompt prompt, boolean newFavorite);
    }

    public PromptAdapter(List<Prompt> prompts, String currentUserId, OnPromptClickListener listener) {
        this.prompts = prompts;
        this.currentUserId = currentUserId;
        this.userIdToNameMap = new java.util.HashMap<>();
        this.listener = listener;
    }

    public void setUserIdToNameMap(java.util.Map<String, String> userIdToNameMap) {
        this.userIdToNameMap = userIdToNameMap != null ? userIdToNameMap : new java.util.HashMap<>();
        notifyDataSetChanged();
    }

    public void updatePrompts(List<Prompt> newPrompts) {
        this.prompts = newPrompts;
        notifyDataSetChanged();
    }

    public void setFavorites(java.util.Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds != null ? favoriteIds : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prompt, parent, false);
        return new PromptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromptViewHolder holder, int position) {
        Prompt prompt = prompts.get(position);
        holder.bind(prompt);
    }

    @Override
    public int getItemCount() {
        return prompts != null ? prompts.size() : 0;
    }

    class PromptViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewLlmTag;
        private TextView textViewDescription;
        private TextView textViewPromptText;
        private TextView textViewExperience;
        private TextView textViewPublishDate;
        private LinearLayout layoutActions;
        private Button buttonEdit;
        private Button buttonDelete;
        private ImageButton buttonFavorite;
        private View buttonCopy;
        private TextView buttonCopyText;

        public PromptViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewLlmTag = itemView.findViewById(R.id.textViewLlmTag);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPromptText = itemView.findViewById(R.id.textViewPromptText);
            textViewExperience = itemView.findViewById(R.id.textViewExperience);
            textViewPublishDate = itemView.findViewById(R.id.textViewPublishDate);
            buttonFavorite = itemView.findViewById(R.id.buttonFavorite);
            buttonCopy = itemView.findViewById(R.id.buttonCopy);
            buttonCopyText = itemView.findViewById(R.id.buttonCopyText);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(Prompt prompt) {
            textViewTitle.setText(prompt.getTitle());
            textViewLlmTag.setText(prompt.getLlmTag() != null ? prompt.getLlmTag() : "Unknown");
            textViewDescription.setText(prompt.getDescription() != null ? prompt.getDescription() : "");
            textViewPromptText.setText(prompt.getPromptText() != null ? "\"" + prompt.getPromptText() + "\"" : "");
            
            if (prompt.getExperience() != null && !prompt.getExperience().isEmpty()) {
                textViewExperience.setText("Experience: " + prompt.getExperience());
                textViewExperience.setVisibility(View.VISIBLE);
            } else {
                textViewExperience.setVisibility(View.GONE);
            }

            // Format publish date with author name
            StringBuilder dateText = new StringBuilder();
            if (prompt.getPublishDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                dateText.append(sdf.format(prompt.getPublishDate()));
            }
            
            // Add author name in brackets if available
            if (prompt.getUserId() != null && userIdToNameMap.containsKey(prompt.getUserId())) {
                String authorName = userIdToNameMap.get(prompt.getUserId());
                if (authorName != null && !authorName.isEmpty()) {
                    if (dateText.length() > 0) {
                        dateText.append(" ");
                    }
                    dateText.append("(").append(authorName).append(")");
                }
            }
            
            textViewPublishDate.setText(dateText.toString());

            // Favorite state
            boolean isFav = favoriteIds != null && prompt.getId() != null && favoriteIds.contains(prompt.getId());
            if (buttonFavorite != null) {
                buttonFavorite.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                buttonFavorite.setOnClickListener(v -> {
                    boolean newState = !(favoriteIds != null && favoriteIds.contains(prompt.getId()));
                    if (listener != null) listener.onFavoriteToggle(prompt, newState);
                });
            }

            // Copy description to clipboard
            View.OnClickListener copyListener = v -> {
                    String description = prompt.getDescription() != null ? prompt.getDescription() : "";
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("Prompt Description", description);
                        clipboard.setPrimaryClip(clip);
                        android.widget.Toast.makeText(v.getContext(), "Description copied", android.widget.Toast.LENGTH_SHORT).show();
                    }
                };
            if (buttonCopy != null) buttonCopy.setOnClickListener(copyListener);
            if (buttonCopyText != null) buttonCopyText.setOnClickListener(copyListener);

            // Show edit/delete buttons only for prompts created by current user
            boolean isOwnPrompt = currentUserId != null && 
                                  prompt.getUserId() != null && 
                                  currentUserId.equals(prompt.getUserId());
            
            if (isOwnPrompt) {
                layoutActions.setVisibility(View.VISIBLE);
                buttonEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClick(prompt);
                    }
                });
                buttonDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(prompt);
                    }
                });
            } else {
                layoutActions.setVisibility(View.GONE);
            }

            // Click on item to view full prompt
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPromptClick(prompt);
                }
            });
        }
    }
}

