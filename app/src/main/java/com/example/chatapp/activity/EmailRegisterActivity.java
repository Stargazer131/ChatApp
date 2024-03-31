package com.example.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailRegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, emailEditText, passwordEditText, reenteredPasswordEditText;
    private Button registerButton;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);

        usernameEditText = findViewById(R.id.edit_text_register_username);
        emailEditText = findViewById(R.id.edit_text_register_email);
        passwordEditText = findViewById(R.id.edit_text_register_password);
        reenteredPasswordEditText = findViewById(R.id.edit_text_register_reentered_password);
        registerButton = findViewById(R.id.btn_email_register);
        firebaseAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String reenteredPassword = reenteredPasswordEditText.getText().toString().trim();
            register(username, email, password, reenteredPassword);
        });
    }

    private void register(String username, String email, String password, String reenteredPassword) {
        if (!checkValidInformation(username, email, password, reenteredPassword)) {
            return;
        }

        // register account with firebase
        registerButton.setEnabled(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        User user = new User(username, uid, email);
                        user.setStatus("offline");
                        user.setLastActive(Timestamp.now());
                        addUserToFirebase(uid, user, password);

                    } else {
                        Exception exception = task.getException();
                        String errorMessage = exception.getMessage();
                        AndroidUtility.showToast(EmailRegisterActivity.this, "Registration failed: " + errorMessage);
                    }

                    registerButton.setEnabled(true);
                });
    }

    private void addUserToFirebase(String uid, User user, String password) {
        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtility.showToast(EmailRegisterActivity.this, "Registration Completed");
                        AndroidUtility.showOptionPanel(EmailRegisterActivity.this, "Confirmation", "Go back to Login Screen with your new account?",
                                "Yes", "No",
                                (dialog, which) -> {
                                    // yes
                                    Intent intent = new Intent(EmailRegisterActivity.this, EmailLoginActivity.class);
                                    intent.putExtra("email", user.getEmail());
                                    intent.putExtra("password", password);
                                    startActivity(intent);
                                },
                                (dialog, which) -> {
                                    // no
                                });
                    } else {
                        Exception exception = task.getException();
                        String errorMessage = exception.getMessage();
                        AndroidUtility.showToast(EmailRegisterActivity.this, "Registration failed: " + errorMessage);
                    }
                });
    }

    private boolean checkValidInformation(String username, String email, String password, String reenteredPassword) {
        if (username.isEmpty()) {
            usernameEditText.setError("Empty Field");
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Empty Field");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid Email");
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Empty Field");
            return false;
        }

        if (!reenteredPassword.equals(password)) {
            reenteredPasswordEditText.setError("Password isn't the same");
            return false;
        }

        return true;
    }
}