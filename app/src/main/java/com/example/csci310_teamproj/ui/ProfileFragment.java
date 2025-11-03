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
import com.example.csci310_teamproj.ui.AuthActivity;
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

    private EditText nameField, emailField, studentIdField, affiliationField, birthDateField, bioField;
    private Button saveButton, logoutButton, resetButton;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind UI
        nameField       = view.findViewById(R.id.editTextNameProfile);
        emailField      = view.findViewById(R.id.editTextEmailProfile);
        studentIdField  = view.findViewById(R.id.editTextStudentIdProfile);
        affiliationField= view.findViewById(R.id.editTextAffiliationProfile);
        birthDateField  = view.findViewById(R.id.editTextBirthDateProfile);
        bioField        = view.findViewById(R.id.editTextBioProfile);
        saveButton      = view.findViewById(R.id.buttonSaveProfile);
        logoutButton    = view.findViewById(R.id.buttonLogout);
        resetButton     = view.findViewById(R.id.buttonResetPassword);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        // Load profile data
        loadUserProfile();

        // Save updates for editable fields
        saveButton.setOnClickListener(v -> saveProfileUpdates());

        // Reset password (sends email to current user's email)
        resetButton.setOnClickListener(v -> {
            String email = currentUser.getEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(getContext(), "No email found on account.", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Password reset email sent to " + email, Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        // Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "Profile not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name        = snapshot.child("name").getValue(String.class);
                String email       = snapshot.child("email").getValue(String.class);
                String studentId   = snapshot.child("studentId").getValue(String.class);
                String affiliation = snapshot.child("affiliation").getValue(String.class);
                String birthDate   = snapshot.child("birthDate").getValue(String.class);
                String bio         = snapshot.child("bio").getValue(String.class);

                nameField.setText(name);
                emailField.setText(email);
                studentIdField.setText(studentId);
                affiliationField.setText(affiliation);
                birthDateField.setText(birthDate);
                bioField.setText(bio);

                // Disable immutable fields
                nameField.setEnabled(false);
                emailField.setEnabled(false);
                studentIdField.setEnabled(false);
                affiliationField.setEnabled(false);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileUpdates() {
        String updatedBirthDate = birthDateField.getText().toString().trim();
        String updatedBio       = bioField.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("birthDate", updatedBirthDate);
        updates.put("bio", updatedBio);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
