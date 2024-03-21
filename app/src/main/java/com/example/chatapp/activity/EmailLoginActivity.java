package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatapp.R;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    MaterialButton btnLogin;
    Button btnRegister;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        editTextEmail = findViewById(R.id.edit_text_login_email);
        editTextPassword = findViewById(R.id.edit_text_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_email_register);
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                login(email, password);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
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
                .addOnCompleteListener(EmailLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getUser().getUid();
                            FirebaseUtil.updateUserStatus(userId, "online");

                            Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Exception exception = task.getException();
                            String errorMessage = exception.getMessage();
                            AndroidUtil.showToast(EmailLoginActivity.this, "Login failed: " + errorMessage);

                        }
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(true);
                    }
                });
    }

    private void register() {
        Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
        startActivity(intent);
    }
}