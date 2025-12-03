package com.example.csci310_teamproj.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Post;
import com.example.csci310_teamproj.domain.model.PostVersion;
import com.example.csci310_teamproj.ui.adapter.PostHistoryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PostHistoryDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_POST_TITLE = "postTitle";
    private static final String ARG_VERSIONS = "versions";
    private static final String ARG_USER_NAMES = "userNames";
    private static final String ARG_CURRENT_POST = "currentPost";

    private String postTitle;
    private ArrayList<PostVersion> versions;
    private HashMap<String, String> userNames;
    private Post currentPost;

    public static PostHistoryDialogFragment newInstance(Post currentPost,
                                                        ArrayList<PostVersion> versions,
                                                        HashMap<String, String> userNames) {
        PostHistoryDialogFragment fragment = new PostHistoryDialogFragment();
        Bundle args = new Bundle();
        if (currentPost != null) {
            args.putString(ARG_POST_TITLE, currentPost.getTitle());
            // Note: Post is not Serializable, so we pass fields individually
            args.putString("postId", currentPost.getId());
            args.putString("postBody", currentPost.getBody());
            args.putString("postLlmTag", currentPost.getLlmTag());
            args.putBoolean("postAnonymous", currentPost.isAnonymous());
            args.putString("postAuthorId", currentPost.getAuthorId());
            args.putLong("postTimestamp", currentPost.getTimestamp());
        }
        args.putSerializable(ARG_VERSIONS, versions);
        args.putSerializable(ARG_USER_NAMES, userNames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postTitle = getArguments().getString(ARG_POST_TITLE);
            
            // Reconstruct current post from individual fields
            String postId = getArguments().getString("postId");
            if (postId != null) {
                currentPost = new Post();
                currentPost.setId(postId);
                currentPost.setTitle(postTitle);
                currentPost.setBody(getArguments().getString("postBody"));
                currentPost.setLlmTag(getArguments().getString("postLlmTag"));
                currentPost.setAnonymous(getArguments().getBoolean("postAnonymous", false));
                currentPost.setAuthorId(getArguments().getString("postAuthorId"));
                currentPost.setTimestamp(getArguments().getLong("postTimestamp", System.currentTimeMillis()));
            }

            Object versionsObj = getArguments().getSerializable(ARG_VERSIONS);
            if (versionsObj instanceof ArrayList<?>) {
                versions = new ArrayList<>();
                for (Object item : (ArrayList<?>) versionsObj) {
                    if (item instanceof PostVersion) {
                        versions.add((PostVersion) item);
                    }
                }
            }
            Object userNamesObj = getArguments().getSerializable(ARG_USER_NAMES);
            if (userNamesObj instanceof HashMap<?, ?>) {
                userNames = new HashMap<>();
                HashMap<?, ?> rawMap = (HashMap<?, ?>) userNamesObj;
                for (Object key : rawMap.keySet()) {
                    Object value = rawMap.get(key);
                    if (key instanceof String && value instanceof String) {
                        userNames.put((String) key, (String) value);
                    }
                }
            }
        }
        if (versions == null) {
            versions = new ArrayList<>();
        }
        if (userNames == null) {
            userNames = new HashMap<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post_history, container, false);

        TextView titleView = view.findViewById(R.id.textViewHistoryTitle);
        TextView subtitleView = view.findViewById(R.id.textViewHistorySubtitle);
        TextView emptyStateView = view.findViewById(R.id.textViewHistoryEmpty);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);

        String displayTitle = postTitle != null ? postTitle : "Post history";
        titleView.setText(displayTitle + " • History");
        subtitleView.setText("Every saved edit stays here.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<PostVersion> displayVersions = new ArrayList<>();
        boolean hasCurrent = false;
        if (currentPost != null) {
            displayVersions.add(convertPostToVersion(currentPost));
            hasCurrent = true;
        }
        displayVersions.addAll(versions);

        PostHistoryAdapter adapter = new PostHistoryAdapter(displayVersions, userNames, hasCurrent);
        recyclerView.setAdapter(adapter);

        if (displayVersions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }

        return view;
    }

    private PostVersion convertPostToVersion(Post post) {
        Date timestamp = new Date(post.getTimestamp());
        return new PostVersion(
                "current",
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getLlmTag(),
                post.isAnonymous(),
                post.getAuthorId(),
                timestamp
        );
    }
}

