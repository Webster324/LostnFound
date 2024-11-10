package com.joe.assignment2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.joe.assignment2.adapter.RecentChatRecyclerAdapter;
import com.joe.assignment2.model.ChatroomModel;
import com.joe.assignment2.utils.FirebaseUtil;

// Fragment class for displaying recent chat conversations in a RecyclerView.
public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;

    // Default constructor for the fragment.
    public ChatFragment() {
    }

    // Inflates the layout for the fragment and initializes the RecyclerView.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        setupRecyclerView();// Initialize RecyclerView

        return view;
    }

    // Sets up the RecyclerView with a Firestore query adapter.
    void setupRecyclerView() {
        // Construct Firestore query to retrieve recent chatrooms for the current user.
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        // Configure options for FirestoreRecyclerAdapter
        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();

        // Initialize RecyclerView adapter
        adapter = new RecentChatRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));// Set layout manager
        recyclerView.setAdapter(adapter);// Set adapter to RecyclerView
        adapter.startListening();// Start listening for Firestore updates
    }

    // Resumes listening to Firestore updates when the fragment starts.
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();// Resume listening for updates
    }

    // Stops listening to Firestore updates when the fragment is no longer visible.
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();// Stop listening for updates
    }

    // Refreshes data in adapter upon resuming the fragment.
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();// Notify adapter of data changes
    }
}