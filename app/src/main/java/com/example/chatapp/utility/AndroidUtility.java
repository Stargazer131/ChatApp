package com.example.chatapp.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.activity.MainActivity;
import com.example.chatapp.model.User;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AndroidUtility {

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
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView);

        } catch (Exception ignored) {
            Log.d("ERROR", "GLIDE CAN'T SET IMAGE");
        }
    }

    public static void changeAvatarProfileColor(String status, ImageView imageView, Context context) {
        int color = status.equals("online") ?
                ContextCompat.getColor(context, R.color.green) :
                ContextCompat.getColor(context, R.color.gray);

        ColorStateList colorStateList = ColorStateList.valueOf(color);
        imageView.setBackgroundTintList(colorStateList);
    }

    public static ArrayList<UrlString> findUrlPatterns(String text) {
        Pattern pattern = Patterns.WEB_URL;
        Matcher matcher = pattern.matcher(text);

        ArrayList<UrlString> urls = new ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group();
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            urls.add(new UrlString(url, startIndex, endIndex));
        }
        return urls;
    }
}
