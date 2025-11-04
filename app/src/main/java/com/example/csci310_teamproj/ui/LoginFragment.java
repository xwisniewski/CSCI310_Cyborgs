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
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                            // 🔹 Make absolutely sure we’re in AuthActivity before navigating
                            if (getActivity() instanceof AuthActivity) {
                                // Delay slightly to avoid fragment transition overlap
                                getActivity().runOnUiThread(() -> {
                                    ((AuthActivity) getActivity()).openMainApp();
                                });
                            } else {
                                // 🔹 Fallback in case context mismatch occurs
                                Intent intent = new Intent(requireContext(), com.example.csci310_teamproj.MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }

                        } else {
                            String errorMessage = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Unknown error occurred";
                            Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔹 Go to Register screen
        goToRegisterButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
