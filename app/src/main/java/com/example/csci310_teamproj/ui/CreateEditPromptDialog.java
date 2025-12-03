package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.domain.model.Prompt;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

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
    private CheckBox checkboxAnonymous;
    private Button buttonSave;
    private Button buttonSaveDraft;
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
        checkboxAnonymous = view.findViewById(R.id.checkboxPromptAnonymous);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonSaveDraft = view.findViewById(R.id.buttonSaveDraft);
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

            // If currently anonymous, show the box as checked but allow changing if you want
            if ("anonymous".equals(existingPrompt.getUserId())) {
                checkboxAnonymous.setChecked(true);
            } else {
                checkboxAnonymous.setChecked(false);
            }
        }

        if (buttonSave != null) {
            String primaryLabel = "Publish";
            if (isEditMode) {
                primaryLabel = (existingPrompt != null && existingPrompt.isDraft()) ? "Publish" : "Update";
            }
            buttonSave.setText(primaryLabel);
            buttonSave.setOnClickListener(v -> savePrompt(false));
        }
        if (buttonSaveDraft != null) {
            buttonSaveDraft.setText(isEditMode ? "Save as Draft" : "Save Draft");
            buttonSaveDraft.setOnClickListener(v -> savePrompt(true));
        }
        buttonCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        if (isEditMode) {
            builder.setTitle("Edit Prompt");
        } else {
            builder.setTitle("Create Prompt");
        }

        return builder.create();
    }

    private void savePrompt(boolean saveAsDraft) {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String promptText = editTextPromptText.getText().toString().trim();
        String llmTag = editTextLlmTag.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();
        boolean anonymous = checkboxAnonymous != null && checkboxAnonymous.isChecked();

        // === VALIDATION ===
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!saveAsDraft) {
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
            if (!isValidLlmTagFormat(llmTag)) {
                Toast.makeText(
                        getContext(),
                        "LLM Tag must be ModelName-Version (e.g., GPT-4, Claude-4.1)",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }

        // === CREATE OR EDIT PROMPT OBJECT ===
        Prompt prompt;
        if (isEditMode && existingPrompt != null) {
            prompt = existingPrompt;
        } else {
            prompt = new Prompt();
            prompt.setOriginalAuthorId(uid);   // real owner only set once
            prompt.setPublishDate(null);
        }

        // === CONTENT ===
        prompt.setTitle(title);
        prompt.setDescription(description);
        prompt.setPromptText(promptText);
        prompt.setLlmTag(llmTag);
        prompt.setExperience(experience.isEmpty() ? null : experience);
        prompt.setDraft(saveAsDraft);

        if (!saveAsDraft && prompt.getPublishDate() == null) {
            prompt.setPublishDate(new Date());
        }

        // === ENSURE ORIGINAL AUTHOR ALWAYS EXISTS ===
        if (prompt.getOriginalAuthorId() == null) {
            prompt.setOriginalAuthorId(uid);
        }

        // === ★ PUBLIC USER ID: ANON OR REAL ★ ===
        // IMPORTANT: Only set userId when creating, NOT when editing
        // Editing should not change the author identity
        if (!isEditMode) {
            String publicUserId = anonymous ? "anonymous" : uid;
            prompt.setUserId(publicUserId);
        }
        // When editing, keep the original userId unchanged

        // === RETURN TO CALLER ===
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

    /**
     * Validates LLM tag format: ModelName-Version (e.g., GPT-4, Claude-4.1)
     * Format: Starts with letter(s), dash, then version number (optionally with decimal)
     */
    private boolean isValidLlmTagFormat(String llmTag) {
        if (llmTag == null || llmTag.trim().isEmpty()) {
            return false;
        }
        String pattern = "^[A-Za-z][A-Za-z0-9]*-\\d+(\\.\\d+)?$";
        return llmTag.matches(pattern);
    }
}
