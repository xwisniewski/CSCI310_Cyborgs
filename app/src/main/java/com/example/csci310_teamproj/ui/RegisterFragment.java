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
import com.example.csci310_teamproj.data.repository.UserRepositoryImpl;
import com.example.csci310_teamproj.domain.model.User;
import com.example.csci310_teamproj.domain.usecase.RegisterUserUseCase;

/**
 * Handles user registration UI and interaction.
 * Uses the RegisterUserUseCase to perform business logic.
 */
public class RegisterFragment extends Fragment {

    private RegisterUserUseCase registerUserUseCase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize the use case with repository implementation
        registerUserUseCase = new RegisterUserUseCase(new UserRepositoryImpl());

        // Get references to UI elements
        EditText nameInput = view.findViewById(R.id.editTextName);
        EditText emailInput = view.findViewById(R.id.editTextEmail);
        Button registerButton = view.findViewById(R.id.buttonRegister);

        // Handle register button click
        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                User user = new User("", name, email);
                registerUserUseCase.execute(user);
                Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();

                // Optionally clear inputs
                nameInput.setText("");
                emailInput.setText("");
            }
        });

        return view;
    }
}
