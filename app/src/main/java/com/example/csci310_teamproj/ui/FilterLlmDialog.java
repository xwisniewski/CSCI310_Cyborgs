package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.csci310_teamproj.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FilterLlmDialog extends DialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(Set<String> selectedLlms);
    }

    private CheckBox checkBoxAll;
    private LinearLayout llmCheckboxesContainer;
    private Button buttonClearFilter;
    private Button buttonApplyFilter;

    private Set<String> currentlySelected;
    private List<String> availableLlms;
    private List<String> sortedLlms;
    private List<CheckBox> llmCheckboxes;
    private OnFilterAppliedListener listener;

    public static FilterLlmDialog newInstance(Set<String> currentSelection, List<String> availableLlms) {
        FilterLlmDialog dialog = new FilterLlmDialog();
        Bundle args = new Bundle();
        if (currentSelection != null) {
            args.putStringArrayList("currentSelection", new java.util.ArrayList<>(currentSelection));
        }
        if (availableLlms != null) {
            args.putStringArrayList("availableLlms", new java.util.ArrayList<>(availableLlms));
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            java.util.ArrayList<String> list = getArguments().getStringArrayList("currentSelection");
            currentlySelected = list != null ? new HashSet<>(list) : new HashSet<>();
            
            java.util.ArrayList<String> llmsList = getArguments().getStringArrayList("availableLlms");
            availableLlms = llmsList != null ? new ArrayList<>(llmsList) : new ArrayList<>();
        } else {
            currentlySelected = new HashSet<>();
            availableLlms = new ArrayList<>();
        }
        llmCheckboxes = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_filter_llm, null);

        checkBoxAll = view.findViewById(R.id.checkBoxAll);
        llmCheckboxesContainer = view.findViewById(R.id.llmCheckboxesContainer);
        buttonClearFilter = view.findViewById(R.id.buttonClearFilter);
        buttonApplyFilter = view.findViewById(R.id.buttonApplyFilter);

        // Create checkboxes dynamically for each available LLM
        createLlmCheckboxes();

        // Restore previous selection or default to "All"
        if (currentlySelected.isEmpty() || currentlySelected.contains("All")) {
            checkBoxAll.setChecked(true);
        } else {
            updateCheckboxesFromSelection(currentlySelected);
        }

        // Handle "All" checkbox
        checkBoxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all others when "All" is checked
                for (CheckBox cb : llmCheckboxes) {
                    cb.setChecked(false);
                }
            }
        });

        // When any specific LLM is checked, uncheck "All"
        View.OnClickListener uncheckAllListener = v -> {
            if (((CheckBox) v).isChecked()) {
                checkBoxAll.setChecked(false);
            }
        };

        for (CheckBox cb : llmCheckboxes) {
            cb.setOnClickListener(uncheckAllListener);
        }

        buttonClearFilter.setOnClickListener(v -> {
            checkBoxAll.setChecked(true);
            for (CheckBox cb : llmCheckboxes) {
                cb.setChecked(false);
            }
        });

        buttonApplyFilter.setOnClickListener(v -> applyFilter());

        builder.setView(view);
        builder.setTitle("Filter by LLM");
        return builder.create();
    }

    private void createLlmCheckboxes() {
        llmCheckboxesContainer.removeAllViews();
        llmCheckboxes.clear();

        // Sort LLMs alphabetically for consistent display
        sortedLlms = new ArrayList<>(new TreeSet<>(availableLlms));

        for (String llmTag : sortedLlms) {
            if (llmTag == null || llmTag.trim().isEmpty()) {
                continue;
            }
            
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(llmTag);
            checkBox.setTextSize(16);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 12; // 12dp margin
            checkBox.setLayoutParams(params);
            
            llmCheckboxes.add(checkBox);
            llmCheckboxesContainer.addView(checkBox);
        }
    }

    private void updateCheckboxesFromSelection(Set<String> selection) {
        for (int i = 0; i < llmCheckboxes.size() && i < sortedLlms.size(); i++) {
            CheckBox cb = llmCheckboxes.get(i);
            String llmTag = sortedLlms.get(i);
            
            // Check if this LLM tag is in the selection (case-insensitive)
            boolean isSelected = false;
            for (String selected : selection) {
                if (selected != null && llmTag != null && 
                    selected.equalsIgnoreCase(llmTag)) {
                    isSelected = true;
                    break;
                }
            }
            cb.setChecked(isSelected);
        }
    }

    private void applyFilter() {
        Set<String> selectedLlms = new HashSet<>();

        if (checkBoxAll.isChecked()) {
            selectedLlms.add("All");
        } else {
            // Add all checked LLM tags
            for (int i = 0; i < llmCheckboxes.size() && i < sortedLlms.size(); i++) {
                CheckBox cb = llmCheckboxes.get(i);
                if (cb.isChecked()) {
                    selectedLlms.add(sortedLlms.get(i));
                }
            }
        }

        if (listener != null) {
            listener.onFilterApplied(selectedLlms);
        }
        dismiss();
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }
}

