package com.example.chatapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendRecyclerAdapter;
import com.example.chatapp.adapter.FriendRequestRecyclerAdapter;
import com.example.chatapp.model.FriendRequest;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserRelationship;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class FriendFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendRecyclerAdapter adapter;

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        Query query = FirebaseUtility.getAllUserRelationship()
                .whereArrayContains("userIds", FirebaseUtility.getCurrentUserId())
                .whereEqualTo("type", "friend");

        FirestoreRecyclerOptions<UserRelationship> options = new FirestoreRecyclerOptions.Builder<UserRelationship>()
                .setQuery(query, UserRelationship.class).build();

        adapter = new FriendRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.stopListening();
        }

        super.onDestroy();
    }
}