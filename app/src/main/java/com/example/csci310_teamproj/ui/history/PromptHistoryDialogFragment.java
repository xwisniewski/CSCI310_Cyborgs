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
import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.domain.model.PromptVersion;
import com.example.csci310_teamproj.ui.adapter.PromptHistoryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PromptHistoryDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_PROMPT_TITLE = "promptTitle";
    private static final String ARG_VERSIONS = "versions";
    private static final String ARG_USER_NAMES = "userNames";
    private static final String ARG_CURRENT_PROMPT = "currentPrompt";

    private String promptTitle;
    private ArrayList<PromptVersion> versions;
    private HashMap<String, String> userNames;
    private Prompt currentPrompt;

    public static PromptHistoryDialogFragment newInstance(Prompt currentPrompt,
                                                          ArrayList<PromptVersion> versions,
                                                          HashMap<String, String> userNames) {
        PromptHistoryDialogFragment fragment = new PromptHistoryDialogFragment();
        Bundle args = new Bundle();
        if (currentPrompt != null) {
            args.putString(ARG_PROMPT_TITLE, currentPrompt.getTitle());
            args.putSerializable(ARG_CURRENT_PROMPT, currentPrompt);
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
            promptTitle = getArguments().getString(ARG_PROMPT_TITLE);
            Object versionsObj = getArguments().getSerializable(ARG_VERSIONS);
            if (versionsObj instanceof ArrayList<?>) {
                versions = new ArrayList<>();
                for (Object item : (ArrayList<?>) versionsObj) {
                    if (item instanceof PromptVersion) {
                        versions.add((PromptVersion) item);
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
            Object currentPromptObj = getArguments().getSerializable(ARG_CURRENT_PROMPT);
            if (currentPromptObj instanceof Prompt) {
                currentPrompt = (Prompt) currentPromptObj;
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
        View view = inflater.inflate(R.layout.dialog_prompt_history, container, false);

        TextView titleView = view.findViewById(R.id.textViewHistoryTitle);
        TextView subtitleView = view.findViewById(R.id.textViewHistorySubtitle);
        TextView emptyStateView = view.findViewById(R.id.textViewHistoryEmpty);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);

        String displayTitle = promptTitle != null ? promptTitle : "Prompt history";
        titleView.setText(displayTitle + " • History");
        subtitleView.setText("Every saved edit stays here.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<PromptVersion> displayVersions = new ArrayList<>();
        boolean hasCurrent = false;
        if (currentPrompt != null) {
            displayVersions.add(convertPromptToVersion(currentPrompt));
            hasCurrent = true;
        }
        displayVersions.addAll(versions);

        PromptHistoryAdapter adapter = new PromptHistoryAdapter(displayVersions, userNames, hasCurrent);
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

    private PromptVersion convertPromptToVersion(Prompt prompt) {
        Date timestamp = prompt.getPublishDate() != null ? prompt.getPublishDate() : new Date();
        return new PromptVersion(
                "current",
                prompt.getId(),
                prompt.getTitle(),
                prompt.getDescription(),
                prompt.getPromptText(),
                prompt.getLlmTag(),
                prompt.getExperience(),
                prompt.isDraft(),
                prompt.getUserId(),
                timestamp
        );
    }
}

