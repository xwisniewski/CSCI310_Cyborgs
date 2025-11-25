package com.example.csci310_teamproj.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.PromptVersion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PromptHistoryAdapter extends RecyclerView.Adapter<PromptHistoryAdapter.VersionViewHolder> {

    private final List<PromptVersion> versions;
    private final Map<String, String> userNameMap;
    private final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
    private final boolean hasCurrentEntry;

    public PromptHistoryAdapter(List<PromptVersion> versions, Map<String, String> userNameMap, boolean hasCurrentEntry) {
        this.versions = versions;
        this.userNameMap = userNameMap;
        this.hasCurrentEntry = hasCurrentEntry;
    }

    @NonNull
    @Override
    public VersionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prompt_version, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VersionViewHolder holder, int position) {
        boolean isCurrent = hasCurrentEntry && position == 0;
        holder.bind(versions.get(position), isCurrent);
    }

    @Override
    public int getItemCount() {
        return versions != null ? versions.size() : 0;
    }

    class VersionViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewVersionTimestamp;
        private final TextView textViewVersionMeta;
        private final TextView textViewVersionTitle;
        private final TextView textViewVersionDescription;
        private final TextView textViewVersionPrompt;
        private final TextView textViewVersionExperience;
        private final TextView textViewVersionLlmTag;

        VersionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewVersionTimestamp = itemView.findViewById(R.id.textViewVersionTimestamp);
            textViewVersionMeta = itemView.findViewById(R.id.textViewVersionMeta);
            textViewVersionTitle = itemView.findViewById(R.id.textViewVersionTitle);
            textViewVersionDescription = itemView.findViewById(R.id.textViewVersionDescription);
            textViewVersionPrompt = itemView.findViewById(R.id.textViewVersionPrompt);
            textViewVersionExperience = itemView.findViewById(R.id.textViewVersionExperience);
            textViewVersionLlmTag = itemView.findViewById(R.id.textViewVersionLlmTag);
        }

        void bind(PromptVersion version, boolean isCurrentEntry) {
            if (version.getUpdatedAt() != null) {
                textViewVersionTimestamp.setText(formatter.format(version.getUpdatedAt()));
            } else {
                textViewVersionTimestamp.setText("Unknown time");
            }

            StringBuilder metaBuilder = new StringBuilder();
            if (isCurrentEntry) {
                metaBuilder.append("Current version");
                if (version.getUpdatedBy() != null) {
                    metaBuilder.append(" • ").append(resolveName(version.getUpdatedBy()));
                }
            } else {
                metaBuilder.append("Edited by ").append(resolveName(version.getUpdatedBy()));
            }
            if (version.wasDraft()) {
                metaBuilder.append(" • Draft snapshot");
            }
            textViewVersionMeta.setText(metaBuilder.toString());

            textViewVersionTitle.setText(!TextUtils.isEmpty(version.getTitle()) ? version.getTitle() : "Untitled");

            if (!TextUtils.isEmpty(version.getDescription())) {
                textViewVersionDescription.setVisibility(View.VISIBLE);
                textViewVersionDescription.setText(version.getDescription());
            } else {
                textViewVersionDescription.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(version.getPromptText())) {
                textViewVersionPrompt.setVisibility(View.VISIBLE);
                textViewVersionPrompt.setText("“" + version.getPromptText() + "”");
            } else {
                textViewVersionPrompt.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(version.getExperience())) {
                textViewVersionExperience.setVisibility(View.VISIBLE);
                textViewVersionExperience.setText("Experience: " + version.getExperience());
            } else {
                textViewVersionExperience.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(version.getLlmTag())) {
                textViewVersionLlmTag.setVisibility(View.VISIBLE);
                textViewVersionLlmTag.setText(version.getLlmTag());
            } else {
                textViewVersionLlmTag.setVisibility(View.GONE);
            }
        }
    }

    private String resolveName(String userId) {
        if (userId == null) {
            return "Unknown editor";
        }
        String name = userNameMap != null ? userNameMap.get(userId) : null;
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        return userId.length() > 10 ? userId.substring(0, 10) + "..." : userId;
    }
}

