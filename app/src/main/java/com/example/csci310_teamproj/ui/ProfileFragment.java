package com.example.csci310_teamproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.csci310_teamproj.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText nameField, emailField, affiliationField, birthDateField, bioField;
    private Button saveButton, logoutButton;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind UI
        nameField = view.findViewById(R.id.editTextNameProfile);
        emailField = view.findViewById(R.id.editTextEmailProfile);
        affiliationField = view.findViewById(R.id.editTextAffiliationProfile);
        birthDateField = view.findViewById(R.id.editTextBirthDateProfile);
        bioField = view.findViewById(R.id.editTextBioProfile);
        saveButton = view.findViewById(R.id.buttonSaveProfile);
        logoutButton = view.findViewById(R.id.buttonLogout);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Load profile data
        loadUserProfile();

        // Save updates for editable fields
        saveButton.setOnClickListener(v -> saveProfileUpdates());

        // Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String affiliation = snapshot.child("affiliation").getValue(String.class);
                    String birthDate = snapshot.child("birthDate").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);

                    nameField.setText(name);
                    emailField.setText(email);
                    affiliationField.setText(affiliation);
                    birthDateField.setText(birthDate);
                    bioField.setText(bio);

                    // Disable immutable fields
                    nameField.setEnabled(false);
                    emailField.setEnabled(false);
                    affiliationField.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Profile not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileUpdates() {
        String updatedBirthDate = birthDateField.getText().toString().trim();
        String updatedBio = bioField.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("birthDate", updatedBirthDate);
        updates.put("bio", updatedBio);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
