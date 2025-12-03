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

        Log.d(TAG, "Updating prompt: " + prompt.getId());

        // Try to save version history first, but don't block the update if it fails
        FirebaseHelper.getPromptRef(prompt.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PromptEntity oldEntity = snapshot.getValue(PromptEntity.class);
                        if (oldEntity != null) {
                            oldEntity.id = snapshot.getKey();
                            Log.d(TAG, "Found existing prompt, attempting to save version history");
                            // Try to save history, but proceed with update regardless
                            trySaveHistoryThenUpdate(prompt, oldEntity, callback);
                        } else {
                            Log.d(TAG, "No existing prompt found, performing direct update");
                            performPromptUpdate(prompt, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching existing prompt: " + error.getMessage());
                        // Just do the update directly
                        performPromptUpdate(prompt, callback);
                    }
                });
    }

    private void trySaveHistoryThenUpdate(Prompt prompt, PromptEntity previous, Callback<Void> callback) {
        if (previous == null) {
            performPromptUpdate(prompt, callback);
            return;
        }

        PromptVersionEntity hist = convertPromptToHistoryEntity(prompt.getId(), previous);
        if (hist == null) {
            performPromptUpdate(prompt, callback);
            return;
        }

        try {
            DatabaseReference ref = FirebaseHelper.getPromptHistoryRef(prompt.getId()).push();
            hist.versionId = ref.getKey();
            Log.d(TAG, "Saving prompt version history to: " + ref.toString());

            ref.setValue(historyEntityToMap(hist))
                    .addOnSuccessListener(a -> {
                        Log.d(TAG, "✓ Prompt version history saved successfully");
                        prompt.setHasHistory(true);
                        
                        // Use setValue with complete entity instead of updateChildren
                        // This reconstructs the full object preserving ownership fields
                        performFullPromptReplace(prompt, previous, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "⚠ Could not save history, proceeding with update anyway: " + e.getMessage());
                        // Don't set hasHistory flag, just proceed with update
                        performPromptUpdate(prompt, callback);
                    });
        } catch (Exception e) {
            Log.w(TAG, "⚠ Exception saving history, proceeding with update: " + e.getMessage());
            performPromptUpdate(prompt, callback);
        }
    }

    private void performFullPromptReplace(Prompt prompt, PromptEntity previous, Callback<Void> callback) {
        // Verify ownership before attempting write
        String currentUserId = FirebaseHelper.getCurrentUser() != null 
                ? FirebaseHelper.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            Log.e(TAG, "User not authenticated for full replace");
            callback.onError("You must be logged in to update prompts");
            return;
        }

        // Determine the actual owner ID
        String ownerId = previous.originalAuthorId != null ? previous.originalAuthorId : previous.userId;
        if ("anonymous".equals(ownerId)) {
            // If userId is anonymous, we need to check if there's a real owner
            ownerId = previous.originalAuthorId;
        }
        
        Log.d(TAG, "=== FULL REPLACE DEBUG ===");
        Log.d(TAG, "Current User: " + currentUserId);
        Log.d(TAG, "Previous userId: " + previous.userId);
        Log.d(TAG, "Previous originalAuthorId: " + previous.originalAuthorId);
        Log.d(TAG, "Determined ownerId: " + ownerId);
        
        if (ownerId != null && !currentUserId.equals(ownerId)) {
            Log.e(TAG, "PERMISSION DENIED: User " + currentUserId + " does not own prompt (owner: " + ownerId + ")");
            callback.onError("You can only update your own prompts");
            return;
        }

        // Preserve ownership and identity fields from the previous version
        // Only update content fields from the new prompt
        PromptEntity updatedEntity = new PromptEntity(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getLlmTag(),
                prompt.getExperience(),
                prompt.getPublishDate() != null ? prompt.getPublishDate().getTime() : previous.publishDate,
                previous.userId,              // PRESERVE original userId
                previous.originalAuthorId != null ? previous.originalAuthorId : currentUserId,  // PRESERVE or set to current user
                prompt.isDraft(),
                prompt.hasHistory()
        );

        Map<String, Object> completeMap = entityToMap(updatedEntity);
        
        Log.d(TAG, "Performing full replace with preserved ownership");
        Log.d(TAG, "Writing userId: " + updatedEntity.userId);
        Log.d(TAG, "Writing originalAuthorId: " + updatedEntity.originalAuthorId);
        Log.d(TAG, "Complete map keys: " + completeMap.keySet());

        FirebaseHelper.getPromptRef(prompt.getId())
                .setValue(completeMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Prompt replaced successfully: " + prompt.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ FIREBASE FULL REPLACE FAILED ✗");
                    Log.e(TAG, "Error message: " + e.getMessage());
                    Log.e(TAG, "Error type: " + e.getClass().getName());
                    Log.e(TAG, "Error cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
                    Log.e(TAG, "Attempted to write userId: " + updatedEntity.userId);
                    Log.e(TAG, "Attempted to write originalAuthorId: " + updatedEntity.originalAuthorId);
                    Log.e(TAG, "Current authenticated user: " + currentUserId);
                    
                    if (e instanceof com.google.firebase.database.DatabaseException) {
                        Log.e(TAG, "DatabaseException details: " + e.toString());
                    }
                    
                    // Fallback: Try updateChildren with only content fields (no ownership fields)
                    Log.w(TAG, "Attempting fallback: updateChildren with content fields only");
                    performContentOnlyUpdate(prompt, callback);
                });
    }

    private void performContentOnlyUpdate(Prompt prompt, Callback<Void> callback) {
        // Fallback: Only update content fields, never touch ownership
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", prompt.getTitle());
        updates.put("promptText", prompt.getPromptText());
        updates.put("description", prompt.getDescription());
        updates.put("llmTag", prompt.getLlmTag());
        updates.put("experience", prompt.getExperience());
        updates.put("isDraft", prompt.isDraft());
        updates.put("hasHistory", prompt.hasHistory());
        
        if (prompt.getPublishDate() != null) {
            updates.put("publishDate", prompt.getPublishDate().getTime());
        }

        Log.d(TAG, "Fallback: Updating only content fields: " + updates.keySet());

        FirebaseHelper.getPromptRef(prompt.getId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Fallback update succeeded: " + prompt.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ FALLBACK UPDATE ALSO FAILED ✗");
                    Log.e(TAG, "Error: " + e.getMessage());
                    Log.e(TAG, "This indicates a Firebase Security Rules issue.");
                    Log.e(TAG, "Please check your Firebase Console → Realtime Database → Rules");
                    callback.onError("Permission denied. Please check Firebase Security Rules: " + e.getMessage());
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
        DatabaseReference historyRef = FirebaseHelper.getPromptHistoryRef(promptId);
        Log.d(TAG, "Loading prompt history from: " + historyRef.toString());
        
        historyRef.orderByChild("updatedAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "History snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());
                        List<PromptVersion> list = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Log.d(TAG, "Found history entry: " + ds.getKey());
                            PromptVersionEntity entity = ds.getValue(PromptVersionEntity.class);
                            if (entity != null) {
                                entity.versionId = ds.getKey();
                                list.add(convertHistoryEntityToDomain(entity));
                            }
                        }

                        Log.d(TAG, "Loaded " + list.size() + " history entries");
                        Collections.reverse(list);
                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading history: " + error.getMessage());
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

    private void performPromptUpdate(Prompt prompt, Callback<Void> callback) {
        // Get the current user
        String currentUserId = FirebaseHelper.getCurrentUser() != null 
                ? FirebaseHelper.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            Log.e(TAG, "User not authenticated");
            callback.onError("You must be logged in to update prompts");
            return;
        }

        Log.d(TAG, "=== PROMPT UPDATE DEBUG ===");
        Log.d(TAG, "Prompt ID: " + prompt.getId());
        Log.d(TAG, "Current User: " + currentUserId);
        Log.d(TAG, "Prompt.userId: " + prompt.getUserId());
        Log.d(TAG, "Prompt.originalAuthorId: " + prompt.getOriginalAuthorId());

        // First, verify ownership by reading the existing prompt
        FirebaseHelper.getPromptRef(prompt.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.e(TAG, "Prompt not found in Firebase: " + prompt.getId());
                            callback.onError("Prompt not found");
                            return;
                        }

                        // Check ownership - use originalAuthorId if available, otherwise userId
                        String ownerId = snapshot.child("originalAuthorId").getValue(String.class);
                        if (ownerId == null || ownerId.equals("anonymous")) {
                            ownerId = snapshot.child("userId").getValue(String.class);
                        }
                        
                        // Handle case where userId is "anonymous" - shouldn't block owner
                        if ("anonymous".equals(ownerId)) {
                            Log.w(TAG, "Owner is 'anonymous', trying to infer real owner");
                            // If we can't determine owner, allow update (might be old data)
                            ownerId = currentUserId;
                        }
                        
                        Log.d(TAG, "Firebase ownerId: " + ownerId);
                        Log.d(TAG, "Ownership check: " + currentUserId + " vs " + ownerId);
                        
                        if (ownerId != null && !currentUserId.equals(ownerId)) {
                            Log.e(TAG, "PERMISSION DENIED: User " + currentUserId + " does not own prompt (owner: " + ownerId + ")");
                            callback.onError("You can only update your own prompts");
                            return;
                        }

                        // Ownership verified, proceed with update
                        // CRITICAL: Only update content fields, never touch userId or originalAuthorId
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("title", prompt.getTitle());
                        updates.put("promptText", prompt.getPromptText());
                        updates.put("description", prompt.getDescription());
                        updates.put("llmTag", prompt.getLlmTag());
                        updates.put("experience", prompt.getExperience());
                        updates.put("isDraft", prompt.isDraft());
                        updates.put("hasHistory", prompt.hasHistory());
                        
                        if (prompt.getPublishDate() != null) {
                            updates.put("publishDate", prompt.getPublishDate().getTime());
                        }

                        Log.d(TAG, "Sending update with fields: " + updates.keySet());
                        Log.d(TAG, "Update values: " + updates);

                        FirebaseHelper.getPromptRef(prompt.getId())
                                .updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✓ Prompt updated successfully: " + prompt.getId());
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "✗ FIREBASE UPDATE FAILED ✗");
                                    Log.e(TAG, "Error message: " + e.getMessage());
                                    Log.e(TAG, "Error type: " + e.getClass().getName());
                                    Log.e(TAG, "Error cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
                                    
                                    if (e instanceof com.google.firebase.database.DatabaseException) {
                                        Log.e(TAG, "DatabaseException: " + e.toString());
                                    }
                                    
                                    callback.onError("Update failed: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "✗ DATABASE READ CANCELLED ✗");
                        Log.e(TAG, "Error code: " + error.getCode());
                        Log.e(TAG, "Error message: " + error.getMessage());
                        Log.e(TAG, "Error details: " + error.getDetails());
                        callback.onError("Database error: " + error.getMessage());
                    }
                });
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
