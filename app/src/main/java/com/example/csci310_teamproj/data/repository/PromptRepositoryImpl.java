package com.example.csci310_teamproj.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.model.PromptEntity;
import com.example.csci310_teamproj.domain.model.Prompt;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase implementation of PromptRepository.
 */
public class PromptRepositoryImpl implements PromptRepository {

    private static final String TAG = "PromptRepositoryImpl";

    @Override
    public List<Prompt> getPrompts() {
        // This method is kept for backward compatibility but won't work asynchronously
        // The UI should use getPrompts with callback instead
        return new ArrayList<>();
    }

    public void getPrompts(Callback<List<Prompt>> callback) {
        FirebaseHelper.getPromptsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Prompt> prompts = new ArrayList<>();
                for (DataSnapshot promptSnapshot : snapshot.getChildren()) {
                    PromptEntity entity = promptSnapshot.getValue(PromptEntity.class);
                    if (entity != null) {
                        entity.id = promptSnapshot.getKey();
                        prompts.add(convertEntityToDomain(entity));
                    }
                }
                callback.onSuccess(prompts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching prompts: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void createPrompt(Prompt prompt, Callback<Void> callback) {
        // Generate ID and set publish date
        String promptId = FirebaseHelper.getPromptsRef().push().getKey();
        if (promptId == null) {
            callback.onError("Failed to generate prompt ID");
            return;
        }

        prompt.setId(promptId);
        if (prompt.getPublishDate() == null) {
            prompt.setPublishDate(new Date());
        }

        PromptEntity entity = convertDomainToEntity(prompt);
        Map<String, Object> promptValues = entityToMap(entity);

        FirebaseHelper.getPromptsRef().child(promptId).setValue(promptValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Prompt created successfully: " + promptId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating prompt: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void updatePrompt(Prompt prompt, Callback<Void> callback) {
        if (prompt.getId() == null || prompt.getId().isEmpty()) {
            callback.onError("Prompt ID is required for update");
            return;
        }

        PromptEntity entity = convertDomainToEntity(prompt);
        Map<String, Object> promptValues = entityToMap(entity);

        FirebaseHelper.getPromptsRef().child(prompt.getId()).updateChildren(promptValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Prompt updated successfully: " + prompt.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating prompt: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void deletePrompt(String promptId, Callback<Void> callback) {
        if (promptId == null || promptId.isEmpty()) {
            callback.onError("Prompt ID is required for deletion");
            return;
        }

        FirebaseHelper.getPromptsRef().child(promptId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Prompt deleted successfully: " + promptId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting prompt: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getPromptById(String promptId, Callback<Prompt> callback) {
        FirebaseHelper.getPromptsRef().child(promptId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError("Prompt not found");
                    return;
                }

                PromptEntity entity = snapshot.getValue(PromptEntity.class);
                if (entity != null) {
                    entity.id = snapshot.getKey();
                    callback.onSuccess(convertEntityToDomain(entity));
                } else {
                    callback.onError("Failed to parse prompt data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching prompt: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    // Helper methods to convert between domain and entity models
    private Prompt convertEntityToDomain(PromptEntity entity) {
        Date publishDate = entity.publishDate != null ? new Date(entity.publishDate) : new Date();
        return new Prompt(
                entity.id,
                entity.title,
                entity.promptText,
                entity.description,
                entity.llmTag,
                entity.experience,
                publishDate,
                entity.userId
        );
    }

    private PromptEntity convertDomainToEntity(Prompt prompt) {
        Long publishDateTimestamp = prompt.getPublishDate() != null ? prompt.getPublishDate().getTime() : new Date().getTime();
        return new PromptEntity(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getLlmTag(),
                prompt.getExperience(),
                publishDateTimestamp,
                prompt.getUserId()
        );
    }

    private Map<String, Object> entityToMap(PromptEntity entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity.id != null) map.put("id", entity.id);
        if (entity.title != null) map.put("title", entity.title);
        if (entity.promptText != null) map.put("promptText", entity.promptText);
        if (entity.description != null) map.put("description", entity.description);
        if (entity.llmTag != null) map.put("llmTag", entity.llmTag);
        if (entity.experience != null) map.put("experience", entity.experience);
        if (entity.publishDate != null) map.put("publishDate", entity.publishDate);
        if (entity.userId != null) map.put("userId", entity.userId);
        return map;
    }
}
