package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();
                setupSearchRecyclerView(searchTerm);
            }
        });
    }

    private void setupSearchRecyclerView(String searchTerm) {
        String searchLowerCase = searchTerm.toLowerCase();
        Query query = FirebaseUtil.getAllUser()
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
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}