package com.example.chatapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatapp.activity.SplashActivity;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;

import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private EditText usernameInput, emailInput;
    private Button updateProfileBtn;
    private Button logoutBtn;

    private User currentUser;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectedImageUri;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                AndroidUtil.setProfilePicture(getContext(), selectedImageUri, profilePic);
                            }
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
        getUserData();

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBtnClick();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileBtn.setEnabled(false);
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUtil.updateUserStatus(currentUser.getUserId(), "offline");
                            FirebaseUtil.logout();

                            Intent intent = new Intent(getContext(), SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            AndroidUtil.showToast(getContext(), "Can't delete token");
                        }
                    }
                });
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(ProfileFragment.this).cropSquare()
                        .compress(512).maxResultSize(512, 512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                return null;
                            }
                        });
            }
        });

        return view;
    }

    private void updateBtnClick() {
        String newUsername = usernameInput.getText().toString().trim();
        if (newUsername.isEmpty()) {
            usernameInput.setError("Can't leave blank");
            return;
        }

        currentUser.setUsername(newUsername);
        updateProfileBtn.setEnabled(false);
        logoutBtn.setEnabled(false);
        if (selectedImageUri != null) {
            updateUserDataAndProfileImageToFirebase();
        } else {
            updateUserDataToFirebase();
        }
    }

    private void updateUserDataAndProfileImageToFirebase() {
        FirebaseUtil.getCurrentProfilePicture().putFile(selectedImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        updateUserDataToFirebase();
                    }
                });
    }

    private void updateUserDataToFirebase() {
        FirebaseUtil.getCurrentUser().set(currentUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(getContext(), "Updated successfully");
                        } else {
                            AndroidUtil.showToast(getContext(), "Updated failed");
                        }

                        try {
                            updateProfileBtn.setEnabled(true);
                            logoutBtn.setEnabled(true);
                        } catch (Exception ignored) {

                        }
                    }
                });
    }


    private void getUserData() {
        FirebaseUtil.getCurrentProfilePicture().getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            AndroidUtil.setProfilePicture(getContext(), uri, profilePic);
                        }
                    }
                });

        FirebaseUtil.getCurrentUser().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUser = task.getResult().toObject(User.class);
                usernameInput.setText(currentUser.getUsername());
                emailInput.setText(currentUser.getEmail());
            }
        });
    }
}