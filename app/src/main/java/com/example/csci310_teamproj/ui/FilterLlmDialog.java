package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.csci310_teamproj.R;

import java.util.HashSet;
import java.util.Set;

public class FilterLlmDialog extends DialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(Set<String> selectedLlms);
    }

    private CheckBox checkBoxAll;
    private CheckBox checkBoxGPT4;
    private CheckBox checkBoxGPT3;
    private CheckBox checkBoxClaude;
    private CheckBox checkBoxGemini;
    private CheckBox checkBoxLlama;
    private CheckBox checkBoxOther;
    private Button buttonClearFilter;
    private Button buttonApplyFilter;

    private Set<String> currentlySelected;
    private OnFilterAppliedListener listener;

    public static FilterLlmDialog newInstance(Set<String> currentSelection) {
        FilterLlmDialog dialog = new FilterLlmDialog();
        Bundle args = new Bundle();
        if (currentSelection != null) {
            args.putStringArrayList("currentSelection", new java.util.ArrayList<>(currentSelection));
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
        } else {
            currentlySelected = new HashSet<>();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_filter_llm, null);

        checkBoxAll = view.findViewById(R.id.checkBoxAll);
        checkBoxGPT4 = view.findViewById(R.id.checkBoxGPT4);
        checkBoxGPT3 = view.findViewById(R.id.checkBoxGPT3);
        checkBoxClaude = view.findViewById(R.id.checkBoxClaude);
        checkBoxGemini = view.findViewById(R.id.checkBoxGemini);
        checkBoxLlama = view.findViewById(R.id.checkBoxLlama);
        checkBoxOther = view.findViewById(R.id.checkBoxOther);
        buttonClearFilter = view.findViewById(R.id.buttonClearFilter);
        buttonApplyFilter = view.findViewById(R.id.buttonApplyFilter);

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
                checkBoxGPT4.setChecked(false);
                checkBoxGPT3.setChecked(false);
                checkBoxClaude.setChecked(false);
                checkBoxGemini.setChecked(false);
                checkBoxLlama.setChecked(false);
                checkBoxOther.setChecked(false);
            }
        });

        // When any specific LLM is checked, uncheck "All"
        View.OnClickListener uncheckAllListener = v -> {
            if (((CheckBox) v).isChecked()) {
                checkBoxAll.setChecked(false);
            }
        };

        checkBoxGPT4.setOnClickListener(uncheckAllListener);
        checkBoxGPT3.setOnClickListener(uncheckAllListener);
        checkBoxClaude.setOnClickListener(uncheckAllListener);
        checkBoxGemini.setOnClickListener(uncheckAllListener);
        checkBoxLlama.setOnClickListener(uncheckAllListener);
        checkBoxOther.setOnClickListener(uncheckAllListener);

        buttonClearFilter.setOnClickListener(v -> {
            checkBoxAll.setChecked(true);
            checkBoxGPT4.setChecked(false);
            checkBoxGPT3.setChecked(false);
            checkBoxClaude.setChecked(false);
            checkBoxGemini.setChecked(false);
            checkBoxLlama.setChecked(false);
            checkBoxOther.setChecked(false);
        });

        buttonApplyFilter.setOnClickListener(v -> applyFilter());

        builder.setView(view);
        builder.setTitle("Filter by LLM");
        return builder.create();
    }

    private void updateCheckboxesFromSelection(Set<String> selection) {
        checkBoxGPT4.setChecked(containsIgnoreCase(selection, "GPT-4") || containsIgnoreCase(selection, "gpt4"));
        checkBoxGPT3.setChecked(containsIgnoreCase(selection, "GPT-3.5") || containsIgnoreCase(selection, "GPT-3") || containsIgnoreCase(selection, "gpt3"));
        checkBoxClaude.setChecked(containsIgnoreCase(selection, "Claude"));
        checkBoxGemini.setChecked(containsIgnoreCase(selection, "Gemini"));
        checkBoxLlama.setChecked(containsIgnoreCase(selection, "Llama"));
        checkBoxOther.setChecked(isOtherSelected(selection));
    }

    private boolean containsIgnoreCase(Set<String> set, String value) {
        for (String s : set) {
            if (s != null && s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOtherSelected(Set<String> selection) {
        Set<String> knownLlms = new HashSet<>();
        knownLlms.add("GPT-4");
        knownLlms.add("GPT-3.5");
        knownLlms.add("GPT-3");
        knownLlms.add("GPT4");
        knownLlms.add("GPT3");
        knownLlms.add("Claude");
        knownLlms.add("Gemini");
        knownLlms.add("Llama");
        knownLlms.add("All");

        for (String selected : selection) {
            boolean isKnown = false;
            for (String known : knownLlms) {
                if (selected != null && selected.equalsIgnoreCase(known)) {
                    isKnown = true;
                    break;
                }
            }
            if (!isKnown) {
                return true;
            }
        }
        return false;
    }

    private void applyFilter() {
        Set<String> selectedLlms = new HashSet<>();

        if (checkBoxAll.isChecked()) {
            selectedLlms.add("All");
        } else {
            if (checkBoxGPT4.isChecked()) {
                selectedLlms.add("GPT-4");
            }
            if (checkBoxGPT3.isChecked()) {
                selectedLlms.add("GPT-3.5");
            }
            if (checkBoxClaude.isChecked()) {
                selectedLlms.add("Claude");
            }
            if (checkBoxGemini.isChecked()) {
                selectedLlms.add("Gemini");
            }
            if (checkBoxLlama.isChecked()) {
                selectedLlms.add("Llama");
            }
            if (checkBoxOther.isChecked()) {
                selectedLlms.add("Other");
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

