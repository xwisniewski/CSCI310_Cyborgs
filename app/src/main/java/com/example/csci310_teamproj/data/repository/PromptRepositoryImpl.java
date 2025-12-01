package com.example.csci310_teamproj.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.model.PromptEntity;
import com.example.csci310_teamproj.data.model.PromptVersionEntity;
import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.domain.model.PromptVersion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromptRepositoryImpl implements PromptRepository {

    private static final String TAG = "PromptRepositoryImpl";

    @Override
    public List<Prompt> getPrompts() {
        return new ArrayList<>(); // unused sync version
    }

    @Override
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
                callback.onError(error.getMessage());
            }
        });
    }

    // ----------------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------------
    @Override
    public void createPrompt(Prompt prompt, Callback<Void> callback) {
        String promptId = FirebaseHelper.getPromptsRef().push().getKey();
        if (promptId == null) {
            callback.onError("Failed to generate prompt ID");
            return;
        }

        prompt.setId(promptId);

        if (prompt.getPublishDate() == null) {
            prompt.setPublishDate(new Date());
        }

        // Always set originalAuthorId on create
        if (prompt.getOriginalAuthorId() == null) {
            prompt.setOriginalAuthorId(prompt.getUserId().equals("anonymous")
                    ? FirebaseHelper.getCurrentUser().getUid()
                    : prompt.getUserId());
        }

        PromptEntity entity = convertDomainToEntity(prompt);
        Map<String, Object> map = entityToMap(entity);

        FirebaseHelper.getPromptsRef().child(promptId)
                .setValue(map)
                .addOnSuccessListener(a -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ----------------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------------
    @Override
    public void updatePrompt(Prompt prompt, Callback<Void> callback) {
        if (prompt.getId() == null) {
            callback.onError("Prompt ID is required");
            return;
        }

        FirebaseHelper.getPromptRef(prompt.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        PromptEntity oldEntity = snapshot.getValue(PromptEntity.class);
                        if (oldEntity != null) {
                            oldEntity.id = snapshot.getKey();
                        }

                        saveVersionThenUpdate(prompt, oldEntity, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        performPromptUpdate(prompt, callback);
                    }
                });
    }

    // ----------------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------------
    @Override
    public void deletePrompt(String promptId, Callback<Void> callback) {
        if (promptId == null) {
            callback.onError("Prompt ID is required");
            return;
        }

        FirebaseHelper.getPromptsRef().child(promptId)
                .removeValue()
                .addOnSuccessListener(a -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ----------------------------------------------------------------------
    // SINGLE PROMPT
    // ----------------------------------------------------------------------
    @Override
    public void getPromptById(String promptId, Callback<Prompt> callback) {
        FirebaseHelper.getPromptRef(promptId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PromptEntity entity = snapshot.getValue(PromptEntity.class);
                        if (entity == null) {
                            callback.onError("Prompt not found");
                            return;
                        }
                        entity.id = snapshot.getKey();
                        callback.onSuccess(convertEntityToDomain(entity));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ----------------------------------------------------------------------
    // HISTORY
    // ----------------------------------------------------------------------
    @Override
    public void getPromptHistory(String promptId, Callback<List<PromptVersion>> callback) {
        FirebaseHelper.getPromptHistoryRef(promptId)
                .orderByChild("updatedAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PromptVersion> list = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PromptVersionEntity entity = ds.getValue(PromptVersionEntity.class);
                            if (entity != null) {
                                entity.versionId = ds.getKey();
                                list.add(convertHistoryEntityToDomain(entity));
                            }
                        }

                        Collections.reverse(list);
                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============ CONVERSION HELPERS ============

    private Prompt convertEntityToDomain(PromptEntity e) {
        return new Prompt(
                e.id,
                e.title,
                e.promptText,
                e.description,
                e.llmTag,
                e.experience,
                e.publishDate != null ? new Date(e.publishDate) : new Date(),
                e.userId,
                e.originalAuthorId != null ? e.originalAuthorId : e.userId,
                e.isDraft != null && e.isDraft,
                e.hasHistory != null && e.hasHistory
        );
    }

    private PromptEntity convertDomainToEntity(Prompt p) {

        long ts = p.getPublishDate() != null
                ? p.getPublishDate().getTime()
                : System.currentTimeMillis();

        return new PromptEntity(
                p.getId(),
                p.getTitle(),
                p.getPromptText(),
                p.getDescription(),
                p.getLlmTag(),
                p.getExperience(),
                ts,
                p.getUserId(),                // ALWAYS WRITTEN
                p.getOriginalAuthorId(),      // ALWAYS REAL UID
                p.isDraft(),
                p.hasHistory()
        );
    }

    // 🔥 FIX HERE: ALWAYS write fields, no conditional skipping
    private Map<String, Object> entityToMap(PromptEntity e) {
        Map<String, Object> m = new HashMap<>();

        m.put("id", e.id);
        m.put("title", e.title);
        m.put("promptText", e.promptText);
        m.put("description", e.description);
        m.put("llmTag", e.llmTag);
        m.put("experience", e.experience);
        m.put("publishDate", e.publishDate);

        // REQUIRED FIELDS — DO NOT SKIP
        m.put("userId", e.userId);                    // "anonymous" OR real UID
        m.put("originalAuthorId", e.originalAuthorId); // real UID only

        m.put("isDraft", e.isDraft);
        m.put("hasHistory", e.hasHistory != null ? e.hasHistory : false);

        return m;
    }

    private void saveVersionThenUpdate(
            Prompt prompt, PromptEntity previous, Callback<Void> callback) {

        if (previous == null) {
            performPromptUpdate(prompt, callback);
            return;
        }

        PromptVersionEntity hist = convertPromptToHistoryEntity(
                prompt.getId(), previous);

        if (hist == null) {
            performPromptUpdate(prompt, callback);
            return;
        }

        DatabaseReference ref = FirebaseHelper.getPromptHistoryRef(prompt.getId()).push();
        hist.versionId = ref.getKey();

        ref.setValue(historyEntityToMap(hist))
                .addOnSuccessListener(a -> {
                    prompt.setHasHistory(true);
                    performPromptUpdate(prompt, callback);
                })
                .addOnFailureListener(e -> performPromptUpdate(prompt, callback));
    }

    private void performPromptUpdate(Prompt prompt, Callback<Void> callback) {
        PromptEntity entity = convertDomainToEntity(prompt);
        Map<String, Object> map = entityToMap(entity);

        FirebaseHelper.getPromptRef(prompt.getId())
                .updateChildren(map)
                .addOnSuccessListener(a -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private PromptVersionEntity convertPromptToHistoryEntity(String promptId, PromptEntity e) {
        if (e == null) return null;

        String editor = FirebaseHelper.getCurrentUser() != null
                ? FirebaseHelper.getCurrentUser().getUid()
                : e.userId;

        return new PromptVersionEntity(
                null,
                promptId,
                e.title,
                e.description,
                e.promptText,
                e.llmTag,
                e.experience,
                e.isDraft,
                editor,
                System.currentTimeMillis()
        );
    }

    private PromptVersion convertHistoryEntityToDomain(PromptVersionEntity entity) {
        Date updatedAt = entity.updatedAt != null ? new Date(entity.updatedAt) : new Date();
        boolean wasDraft = entity.draft != null && entity.draft;

        return new PromptVersion(
                entity.versionId,
                entity.promptId,
                entity.title,
                entity.description,
                entity.promptText,
                entity.llmTag,
                entity.experience,
                wasDraft,
                entity.updatedBy,
                updatedAt
        );
    }


    private Map<String, Object> historyEntityToMap(PromptVersionEntity e) {
        Map<String, Object> m = new HashMap<>();

        m.put("versionId", e.versionId);
        m.put("promptId", e.promptId);
        m.put("title", e.title);
        m.put("description", e.description);
        m.put("promptText", e.promptText);
        m.put("llmTag", e.llmTag);
        m.put("experience", e.experience);
        m.put("draft", e.draft);
        m.put("updatedBy", e.updatedBy);
        m.put("updatedAt", e.updatedAt);

        return m;
    }
}
