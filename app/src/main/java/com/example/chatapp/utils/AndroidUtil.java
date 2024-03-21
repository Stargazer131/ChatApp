package com.example.chatapp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.activity.MainActivity;
import com.example.chatapp.model.User;


public class AndroidUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showOptionPanel(Context context, String title, String message, String positiveButtonText,
                                       String negativeButtonText, DialogInterface.OnClickListener positiveClickListener,
                                       DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonText, positiveClickListener);
        builder.setNegativeButton(negativeButtonText, negativeClickListener);
        builder.setCancelable(false); // Prevent dialog from being dismissed by tapping outside

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static Intent getBackHomeIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    //
    public static void setProfilePicture(Context context, Uri imageUri, ImageView imageView) {
        try {
            Glide.with(context)
                    .load(imageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        } catch (Exception ignored) {
            Log.d("ERROR", "GLIDE CAN'T SET IMAGE");
        }
    }

    public static void setImagePicture(Context context, Uri imageUri, ImageView imageView) {
        try {
            Glide.with(context)
                    .load(imageUri)
                    .into(imageView);

        } catch (Exception ignored) {
            Log.d("ERROR", "GLIDE CAN'T SET IMAGE");
        }
    }
}
