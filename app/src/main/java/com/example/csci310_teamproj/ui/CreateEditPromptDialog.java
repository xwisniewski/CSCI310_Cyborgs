package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.domain.model.Prompt;
import com.google.android.material.textfield.TextInputEditText;

public class CreateEditPromptDialog extends DialogFragment {

    public interface OnPromptSavedListener {
        void onPromptSaved(Prompt prompt);
    }

    private static final String ARG_PROMPT = "prompt";
    private static final String ARG_IS_EDIT = "is_edit";

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextPromptText;
    private TextInputEditText editTextLlmTag;
    private TextInputEditText editTextExperience;
    private Button buttonSave;
    private Button buttonCancel;

    private Prompt existingPrompt;
    private boolean isEditMode;
    private OnPromptSavedListener listener;

    public static CreateEditPromptDialog newInstance(Prompt prompt, boolean isEdit) {
        CreateEditPromptDialog dialog = new CreateEditPromptDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROMPT, prompt);
        args.putBoolean(ARG_IS_EDIT, isEdit);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingPrompt = (Prompt) getArguments().getSerializable(ARG_PROMPT);
            isEditMode = getArguments().getBoolean(ARG_IS_EDIT, false);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_edit_prompt, null);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextPromptText = view.findViewById(R.id.editTextPromptText);
        editTextLlmTag = view.findViewById(R.id.editTextLlmTag);
        editTextExperience = view.findViewById(R.id.editTextExperience);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        // Set title in dialog
        android.widget.TextView titleView = view.findViewById(R.id.textViewDialogTitle);
        if (titleView != null) {
            titleView.setText(isEditMode ? "Edit Prompt" : "Create Prompt");
        }

        // Populate fields if editing
        if (isEditMode && existingPrompt != null) {
            editTextTitle.setText(existingPrompt.getTitle());
            editTextDescription.setText(existingPrompt.getDescription());
            editTextPromptText.setText(existingPrompt.getPromptText());
            editTextLlmTag.setText(existingPrompt.getLlmTag());
            editTextExperience.setText(existingPrompt.getExperience());
        }

        buttonSave.setOnClickListener(v -> savePrompt());
        buttonCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        if (isEditMode) {
            builder.setTitle("Edit Prompt");
        } else {
            builder.setTitle("Create Prompt");
        }

        return builder.create();
    }

    private void savePrompt() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String promptText = editTextPromptText.getText().toString().trim();
        String llmTag = editTextLlmTag.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();

        // Validate required fields
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (promptText.isEmpty()) {
            Toast.makeText(getContext(), "Prompt text is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (llmTag.isEmpty()) {
            Toast.makeText(getContext(), "LLM tag is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create or update prompt
        Prompt prompt;
        if (isEditMode && existingPrompt != null) {
            prompt = existingPrompt;
            prompt.setTitle(title);
            prompt.setDescription(description);
            prompt.setPromptText(promptText);
            prompt.setLlmTag(llmTag);
            prompt.setExperience(experience);
        } else {
            // This will be set by the use case
            prompt = new Prompt(null, title, promptText, description, llmTag, experience, null, null);
        }

        if (listener != null) {
            listener.onPromptSaved(prompt);
        }
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Don't require context to implement listener - use setOnPromptSavedListener instead
    }

    public void setOnPromptSavedListener(OnPromptSavedListener listener) {
        this.listener = listener;
    }
}

