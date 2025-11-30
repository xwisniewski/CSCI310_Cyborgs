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

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText emailInput = view.findViewById(R.id.editTextEmailLogin);
        EditText passwordInput = view.findViewById(R.id.editTextPasswordLogin);
        Button loginButton = view.findViewById(R.id.buttonLogin);
        Button goToRegisterButton = view.findViewById(R.id.buttonGoToRegister);

        auth = FirebaseAuth.getInstance();

        // 🔹 Handle login
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Please enter email and password",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            // Safe toast (fragment might be detached in tests)
                            if (isAdded()) {
                                Toast.makeText(requireContext(),
                                        "Login successful!",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // 🔹 Navigate only if still hosted by AuthActivity
                            if (getActivity() instanceof AuthActivity) {
                                requireActivity().runOnUiThread(() -> {
                                    ((AuthActivity) getActivity()).openMainApp();
                                });
                            } else {
                                // Fallback if somehow not attached to AuthActivity
                                Intent intent = new Intent(requireContext(),
                                        WelcomeLoadingActivity.class);
                                startActivity(intent);

                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }

                        } else {
                            String errorMessage = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Unknown error occurred";

                            // Safe error toast
                            if (isAdded()) {
                                Toast.makeText(requireContext(),
                                        "Login failed: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // 🔹 Go to Register screen
        goToRegisterButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container,
                            new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
