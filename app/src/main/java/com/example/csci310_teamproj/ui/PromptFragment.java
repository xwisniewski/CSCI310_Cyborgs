package com.example.csci310_teamproj.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.example.csci310_teamproj.data.repository.PromptRepository;
import com.example.csci310_teamproj.data.repository.PromptRepositoryImpl;
import com.example.csci310_teamproj.domain.model.Prompt;
import com.example.csci310_teamproj.domain.usecase.CreatePromptUseCase;
import com.example.csci310_teamproj.domain.usecase.DeletePromptUseCase;
import com.example.csci310_teamproj.domain.usecase.UpdatePromptUseCase;
import com.example.csci310_teamproj.ui.adapter.PromptAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PromptFragment extends Fragment implements PromptAdapter.OnPromptClickListener, CreateEditPromptDialog.OnPromptSavedListener {

    private RecyclerView recyclerViewPrompts;
    private PromptAdapter promptAdapter;
    private LinearLayout layoutEmptyState;
    private FloatingActionButton fabCreatePrompt;

    private PromptRepository promptRepository;
    private CreatePromptUseCase createPromptUseCase;
    private UpdatePromptUseCase updatePromptUseCase;
    private DeletePromptUseCase deletePromptUseCase;

    private String currentUserId;
    private List<Prompt> prompts;
    private Map<String, String> userIdToNameMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize repository and use cases
        promptRepository = new PromptRepositoryImpl();
        createPromptUseCase = new CreatePromptUseCase(promptRepository);
        updatePromptUseCase = new UpdatePromptUseCase(promptRepository);
        deletePromptUseCase = new DeletePromptUseCase(promptRepository);

        // Get current user ID
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : null;

        prompts = new ArrayList<>();
        userIdToNameMap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prompt, container, false);

        recyclerViewPrompts = view.findViewById(R.id.recyclerViewPrompts);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        fabCreatePrompt = view.findViewById(R.id.fabCreatePrompt);

        // Setup RecyclerView
        recyclerViewPrompts.setLayoutManager(new LinearLayoutManager(getContext()));
        promptAdapter = new PromptAdapter(prompts, currentUserId, this);
        recyclerViewPrompts.setAdapter(promptAdapter);

        // Setup FAB - Always visible for creating prompts
        if (fabCreatePrompt != null) {
            fabCreatePrompt.setOnClickListener(v -> showCreatePromptDialog());
            fabCreatePrompt.setVisibility(View.VISIBLE);
            fabCreatePrompt.bringToFront(); // Ensure it's on top
        } else {
            android.util.Log.e("PromptFragment", "FAB not found!");
        }

        // Load prompts
        loadPrompts();

        return view;
    }

    private void loadPrompts() {
        promptRepository.getPrompts(new PromptRepository.Callback<List<Prompt>>() {
            @Override
            public void onSuccess(List<Prompt> result) {
                prompts.clear();
                prompts.addAll(result);
                
                // Extract unique user IDs
                Set<String> userIds = new HashSet<>();
                for (Prompt prompt : prompts) {
                    if (prompt.getUserId() != null) {
                        userIds.add(prompt.getUserId());
                    }
                }
                
                // Load user names for all unique user IDs
                loadUserNames(userIds);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load prompts: " + error, Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void loadUserNames(Set<String> userIds) {
        if (userIds.isEmpty()) {
            promptAdapter.updatePrompts(prompts);
            promptAdapter.setUserIdToNameMap(userIdToNameMap);
            updateEmptyState();
            return;
        }

        userIdToNameMap.clear();
        final int[] remaining = {userIds.size()};

        for (String userId : userIds) {
            FirebaseHelper.getUserRef(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue(String.class);
                                if (name != null && !name.isEmpty()) {
                                    userIdToNameMap.put(userId, name);
                                } else {
                                    // Fallback to email if name is not available
                                    String email = snapshot.child("email").getValue(String.class);
                                    if (email != null && !email.isEmpty()) {
                                        userIdToNameMap.put(userId, email.split("@")[0]);
                                    }
                                }
                            }
                            
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                // All user names loaded, update adapter
                                promptAdapter.updatePrompts(prompts);
                                promptAdapter.setUserIdToNameMap(userIdToNameMap);
                                updateEmptyState();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                // Even if some failed, update with what we have
                                promptAdapter.updatePrompts(prompts);
                                promptAdapter.setUserIdToNameMap(userIdToNameMap);
                                updateEmptyState();
                            }
                        }
                    });
        }
    }

    private void updateEmptyState() {
        if (prompts.isEmpty()) {
            recyclerViewPrompts.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPrompts.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showCreatePromptDialog() {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to create prompts", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateEditPromptDialog dialog = CreateEditPromptDialog.newInstance(null, false);
        dialog.setOnPromptSavedListener(this);
        dialog.show(getParentFragmentManager(), "CreatePromptDialog");
    }

    private void showEditPromptDialog(Prompt prompt) {
        CreateEditPromptDialog dialog = CreateEditPromptDialog.newInstance(prompt, true);
        dialog.setOnPromptSavedListener(this);
        dialog.show(getParentFragmentManager(), "EditPromptDialog");
    }

    @Override
    public void onPromptClick(Prompt prompt) {
        // Show full prompt details (could be expanded in the future)
        // For now, just show a simple message
        Toast.makeText(getContext(), prompt.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Prompt prompt) {
        showEditPromptDialog(prompt);
    }

    @Override
    public void onDeleteClick(Prompt prompt) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Prompt")
                .setMessage("Are you sure you want to delete this prompt?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePromptUseCase.execute(prompt.getId(), new PromptRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Prompt deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPrompts();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Failed to delete prompt: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPromptSaved(Prompt prompt) {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to save prompts", Toast.LENGTH_SHORT).show();
            return;
        }

        if (prompt.getId() == null) {
            // Creating new prompt
            createPromptUseCase.execute(
                    prompt.getTitle(),
                    prompt.getPromptText(),
                    prompt.getDescription(),
                    prompt.getLlmTag(),
                    prompt.getExperience(),
                    currentUser.getUid(),
                    new PromptRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Prompt created successfully", Toast.LENGTH_SHORT).show();
                            loadPrompts();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Failed to create prompt: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            // Updating existing prompt
            updatePromptUseCase.execute(prompt, new PromptRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(getContext(), "Prompt updated successfully", Toast.LENGTH_SHORT).show();
                    loadPrompts();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Failed to update prompt: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
