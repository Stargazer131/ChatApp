package com.example.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.fragment.ResetPasswordFragment;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button btnLogin;
    Button btnRegister;
    TextView forgetPasswordTxt;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        editTextEmail = findViewById(R.id.edit_text_login_email);
        editTextPassword = findViewById(R.id.edit_text_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_email_register);
        forgetPasswordTxt = findViewById(R.id.forget_password_text);
        firebaseAuth = FirebaseAuth.getInstance();

        /// data from register
        Bundle extras = getIntent().getExtras();
        String email = (extras != null) ? extras.getString("email") : null;
        String password = (extras != null) ? extras.getString("password") : null;
        if(email != null) {
            editTextEmail.setText(email);
        }
        if(password != null) {
            editTextPassword.setText(password);
        }

        btnLogin.setOnClickListener(v -> {
            String email1 = editTextEmail.getText().toString().trim();
            String password1 = editTextPassword.getText().toString().trim();
            login(email1, password1);
        });

        forgetPasswordTxt.setOnClickListener(v -> {
            showResetPasswordDialog();
        });

        btnRegister.setOnClickListener(v -> register());
    }

    private void login(String email, String password) {
        if(email.isEmpty()) {
            editTextEmail.setError("Can't leave blank");
            return;
        }

        if(password.isEmpty()) {
            editTextEmail.setError("Can't leave blank");
            return;
        }

        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(EmailLoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        FirebaseUtility.updateUserStatus(userId, "online");

                        Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        Exception exception = task.getException();
                        String errorMessage = exception.getMessage();
                        AndroidUtility.showToast(EmailLoginActivity.this, "Login failed: " + errorMessage);

                    }
                    btnLogin.setEnabled(true);
                    btnRegister.setEnabled(true);
                });
    }

    private void register() {
        Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
        startActivity(intent);
    }

    private void showResetPasswordDialog() {
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        resetPasswordFragment.show(getSupportFragmentManager(), "Reset Password");
    }
}