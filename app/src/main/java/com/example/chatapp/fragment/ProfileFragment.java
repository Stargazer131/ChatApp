package com.example.chatapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.activity.SplashActivity;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.messaging.FirebaseMessaging;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private EditText usernameInput, emailInput;
    private Button updateProfileBtn;
    private Button logoutBtn;
    private Button changePasswordBtn;

    private User currentUser;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectedImageUri;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            AndroidUtility.setProfilePicture(getContext(), selectedImageUri, profilePic);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.image_view_profile_picture);
        usernameInput = view.findViewById(R.id.edit_text_profile_username);
        emailInput = view.findViewById(R.id.edit_text_profile_email);
        updateProfileBtn = view.findViewById(R.id.btn_profile_update);
        logoutBtn = view.findViewById(R.id.btn_profile_logout);
        changePasswordBtn = view.findViewById(R.id.btn_password_update);
        getUserData();

        updateProfileBtn.setOnClickListener(v -> updateBtnClick());

        logoutBtn.setOnClickListener(v -> {
            updateProfileBtn.setEnabled(false);
            changePasswordBtn.setEnabled(false);
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtility.updateUserStatusAndLastActive(currentUser.getUserId());
                    FirebaseUtility.logout();

                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AndroidUtility.showToast(getContext(), "Can't delete token");
                }
            });
        });

        profilePic.setOnClickListener(v -> ImagePicker.with(ProfileFragment.this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .createIntent(intent -> {
                    imagePickLauncher.launch(intent);
                    return null;
                }));

        changePasswordBtn.setOnClickListener(v -> showChangePasswordDialog(currentUser.getEmail()));

        return view;
    }

    private void showChangePasswordDialog(String email) {
        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(email);
        changePasswordFragment.show(getActivity().getSupportFragmentManager(), "Change Password");
    }

    private void updateBtnClick() {
        String newUsername = usernameInput.getText().toString().trim();
        if (newUsername.isEmpty()) {
            usernameInput.setError("Can't leave blank");
            return;
        }

        currentUser.setUsername(newUsername);
        updateProfileBtn.setEnabled(false);
        changePasswordBtn.setEnabled(false);
        logoutBtn.setEnabled(false);
        if (selectedImageUri != null) {
            updateUserDataAndProfileImageToFirebase();
        } else {
            updateUserDataToFirebase();
        }
    }

    private void updateUserDataAndProfileImageToFirebase() {
        FirebaseUtility.getCurrentProfilePicture().putFile(selectedImageUri)
                .addOnCompleteListener(task -> updateUserDataToFirebase());
    }

    private void updateUserDataToFirebase() {
        FirebaseUtility.getCurrentUser().set(currentUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtility.showToast(getContext(), "Updated successfully");
                    } else {
                        AndroidUtility.showToast(getContext(), "Updated failed");
                    }

                    try {
                        updateProfileBtn.setEnabled(true);
                        logoutBtn.setEnabled(true);
                        changePasswordBtn.setEnabled(true);
                    } catch (Exception ignored) {

                    }
                });
    }


    private void getUserData() {
        FirebaseUtility.getCurrentProfilePicture().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setProfilePicture(getContext(), uri, profilePic);
                    }
                });

        FirebaseUtility.getCurrentUser().get().addOnCompleteListener(task -> {
            currentUser = task.getResult().toObject(User.class);
            usernameInput.setText(currentUser.getUsername());
            emailInput.setText(currentUser.getEmail());
        });
    }
}