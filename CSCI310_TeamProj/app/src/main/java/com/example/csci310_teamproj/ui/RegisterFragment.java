package com.example.csci310_teamproj.ui;

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
import com.example.csci310_teamproj.data.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Input fields
        EditText nameInput = view.findViewById(R.id.editTextName);
        EditText studentIdInput = view.findViewById(R.id.editTextStudentId);
        EditText emailInput = view.findViewById(R.id.editTextEmail);
        EditText passwordInput = view.findViewById(R.id.editTextPassword);
        EditText affiliationInput = view.findViewById(R.id.editTextAffiliation);
        EditText birthDateInput = view.findViewById(R.id.editTextBirthDate);
        EditText bioInput = view.findViewById(R.id.editTextBio);
        Button registerButton = view.findViewById(R.id.buttonRegister);
        Button goToLoginButton = view.findViewById(R.id.buttonGoToLogin);

        // 🔹 Go back to Login screen
        goToLoginButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String studentId = studentIdInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String affiliation = affiliationInput.getText().toString().trim();
            String birthDate = birthDateInput.getText().toString().trim();
            String bio = bioInput.getText().toString().trim();

            // === Validation ===
            if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty() ||
                    affiliation.isEmpty() || birthDate.isEmpty() || bio.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.endsWith("@usc.edu")) {
                Toast.makeText(getContext(), "Email must end with @usc.edu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (studentId.length() != 10 || !studentId.matches("\\d{10}")) {
                Toast.makeText(getContext(), "Student ID must be exactly 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // === Firebase Registration ===
            FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseHelper.getCurrentUser();
                            if (firebaseUser == null) return;

                            String uid = firebaseUser.getUid();
                            DatabaseReference userRef = FirebaseHelper.getUserRef(uid);

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("name", name);
                            userMap.put("studentId", studentId);
                            userMap.put("email", email);
                            userMap.put("affiliation", affiliation);
                            userMap.put("birthDate", birthDate);
                            userMap.put("bio", bio);

                            userRef.setValue(userMap)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(getContext(),
                                                    "Account created successfully!",
                                                    Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(),
                                                    "Failed to save user data: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show());

                            if (getActivity() instanceof AuthActivity) {
                                ((AuthActivity) getActivity()).openMainApp();
                            }

                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Registration failed";
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}
