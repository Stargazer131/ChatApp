package com.example.chatapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;

public class SearchUserActivity extends AppCompatActivity {

    private AutoCompleteTextView searchInput;
    private ImageButton searchButton;
    private RecyclerView recyclerView;
    private SearchUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.edit_text_search_username);
        searchButton = findViewById(R.id.btn_search_user);
        recyclerView = findViewById(R.id.search_user_recycler_view);
        searchInput.requestFocus();
        setUpAutoCompleteText();

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            setupSearchRecyclerView(searchTerm);
        });
    }

    private void setUpAutoCompleteText() {
        HashSet<String> usernameData = new HashSet<>();

        FirebaseUtility.getAllUser().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = (String) document.get("username");
                            usernameData.add(username);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                SearchUserActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                new ArrayList<>(usernameData)
                        );
                        searchInput.setAdapter(adapter);
                    }
                });
    }

    private void setupSearchRecyclerView(String searchTerm) {
        String searchLowerCase = searchTerm.toLowerCase();
        Query query = FirebaseUtility.getAllUser()
                .whereGreaterThanOrEqualTo("usernameLowercase", searchLowerCase)
                .whereLessThan("usernameLowercase", searchLowerCase + "\uf8ff")
                .orderBy("usernameLowercase");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchUserActivity.this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            Log.d("FRIEND_FRAGMENT", "START LISTENING");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            Log.d("FIND_USER_ACTIVITY", "STOP LISTENING");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}