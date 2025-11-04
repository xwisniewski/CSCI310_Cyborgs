package com.example.csci310_teamproj.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.csci310_teamproj.R;
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText nameField, emailField, studentIdField, affiliationField, birthDateField, bioField;
    private Button saveButton, logoutButton, resetButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind UI elements
        nameField        = view.findViewById(R.id.editTextName);
        emailField       = view.findViewById(R.id.editTextEmail);
        studentIdField   = view.findViewById(R.id.editTextStudentIdProfile);
        affiliationField = view.findViewById(R.id.editTextAffiliation);
        birthDateField   = view.findViewById(R.id.editTextBirthDate);
        bioField         = view.findViewById(R.id.editTextBio);
        saveButton       = view.findViewById(R.id.buttonSave);
        logoutButton     = view.findViewById(R.id.buttonLogout);
        resetButton      = view.findViewById(R.id.buttonResetPassword);

        // Disable immutable fields
        nameField.setEnabled(false);
        emailField.setEnabled(false);
        studentIdField.setEnabled(false);
        // Leave affiliationField editable

        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        DatabaseReference userRef = FirebaseHelper.getUserRef(currentUser.getUid());
        loadUserProfile(userRef);

        // Save button updates bio, birthDate, and affiliation
        saveButton.setOnClickListener(v -> saveProfileUpdates(userRef));

        // Reset password
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

    /** Loads user profile from Firebase */
    private void loadUserProfile(DatabaseReference userRef) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "Profile not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                nameField.setText(snapshot.child("name").getValue(String.class));
                emailField.setText(snapshot.child("email").getValue(String.class));
                studentIdField.setText(snapshot.child("studentId").getValue(String.class));
                affiliationField.setText(snapshot.child("affiliation").getValue(String.class));
                birthDateField.setText(snapshot.child("birthDate").getValue(String.class));
                bioField.setText(snapshot.child("bio").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Saves editable fields (affiliation, bio, birthDate) back to Firebase */
    private void saveProfileUpdates(DatabaseReference userRef) {
        String updatedBirthDate = birthDateField.getText().toString().trim();
        String updatedBio = bioField.getText().toString().trim();
        String updatedAffiliation = affiliationField.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("birthDate", updatedBirthDate);
        updates.put("bio", updatedBio);
        updates.put("affiliation", updatedAffiliation);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
