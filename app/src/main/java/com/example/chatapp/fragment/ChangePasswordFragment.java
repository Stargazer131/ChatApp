package com.example.chatapp.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatapp.R;
import com.example.chatapp.utility.AndroidUtility;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordFragment extends DialogFragment {
    private TextInputEditText oldPasswordInput, newPasswordInput, reenterNewPasswordInput;
    private Button changeBtn;
    private FirebaseAuth firebaseAuth;
    private String email;

    // Interface to communicate button click events to the calling activity/fragment
    public interface OnSendClickListener {
        void onSendClicked(String email);
    }

    public ChangePasswordFragment(String email) {
        // Required empty public constructor
        this.email = email;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        oldPasswordInput = rootView.findViewById(R.id.edit_old_password);
        newPasswordInput = rootView.findViewById(R.id.edit_new_password);
        reenterNewPasswordInput = rootView.findViewById(R.id.edit_reenter_new_password);
        changeBtn = rootView.findViewById(R.id.change_btn);
        firebaseAuth = FirebaseAuth.getInstance();

        changeBtn.setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String reenterNewPassword = reenterNewPasswordInput.getText().toString().trim();
            if(checkValid(oldPassword, newPassword, reenterNewPassword)) {
                changePassword(oldPassword, newPassword);
            }

        });

        return rootView;
    }

    private void changePassword(String oldPassword, String newPassword) {
        changeBtn.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email, oldPassword)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        firebaseAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        AndroidUtility.showToast(getActivity(),
                                                "Change password successfully");
                                    } else {
                                        AndroidUtility.showToast(getActivity(),
                                                "Can't change password, something gone wrong");
                                    }
                                });
                    } else {
                        changeBtn.setEnabled(true);
                        AndroidUtility.showToast(getActivity(), "Old password don't match");
                    }
                });
    }



    private boolean checkValid(String oldPassword, String newPassword, String reenterNewPassword) {
        if(oldPassword.isEmpty()) {
            oldPasswordInput.setError("Blank field");
            return false;
        }

        if(newPassword.isEmpty()) {
            newPasswordInput.setError("Blank field");
            return false;
        }

        if(reenterNewPassword.isEmpty()) {
            reenterNewPasswordInput.setError("Blank field");
            return false;
        }

        if(newPassword.equals(oldPassword)) {
            newPasswordInput.setError("New password can't be the same as old password");
            return false;
        }

        if(!reenterNewPassword.equals(newPassword)) {
            reenterNewPasswordInput.setError("Reenter password don't match");
            return false;
        }

        return true;
    }
}