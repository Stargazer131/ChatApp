package com.example.chatapp.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatapp.R;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;

public class ResetPasswordFragment extends DialogFragment {
    private EditText emailInput;
    private Button sendBtn;
    private FirebaseAuth firebaseAuth;

    // Interface to communicate button click events to the calling activity/fragment
    public interface OnSendClickListener {
        void onSendClicked(String email);
    }

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reset_password, container, false);
        emailInput = rootView.findViewById(R.id.edit_text_email);
        sendBtn = rootView.findViewById(R.id.send_button);
        firebaseAuth = FirebaseAuth.getInstance();

        sendBtn.setOnClickListener(v -> {
            try {
                sendBtn.setEnabled(false);
                String email = emailInput.getText().toString().trim();
                if (email.isEmpty()) {
                    emailInput.setError("Blank email");
                    sendBtn.setEnabled(true);
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Invalid Email");
                    sendBtn.setEnabled(true);
                    return;
                }

                findUserWithEmail(email);
            } catch (Exception ignored) {

            }
        });

        return rootView;
    }

    private void sendResetPasswordEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AndroidUtility.showToast(getActivity(),
                        "Sent successfully, please check your email!");
            } else {
                AndroidUtility.showToast(getActivity(),
                        "Error sending email, please try again later");
            }
        });
    }

    private void findUserWithEmail(String email) {
        FirebaseUtility.getAllUser().get()
                .addOnCompleteListener(task -> {
                    boolean findUser = false;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userEmail = (String) document.get("email");
                            if (userEmail.equals(email)) {
                                findUser = true;
                            }
                        }
                    }

                    if (findUser) {
                        sendResetPasswordEmail(email);
                    } else {
                        AndroidUtility.showToast(getActivity(), "Can't find the user with this email");
                        sendBtn.setEnabled(true);
                    }
                });
    }
}