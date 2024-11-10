package com.joe.assignment2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.joe.assignment2.adapter.RecentChatRecyclerAdapter;
import com.joe.assignment2.model.ChatroomModel;
import com.joe.assignment2.utils.FirebaseUtil;

// Main activity for chat application that handles UI and functionality for displaying chat rooms.
public class ChatMain extends AppCompatActivity {

    ImageButton searchButton;
    ImageButton backBtn;

    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;

    // Sets up the activity layout, initializes UI components, and retrieves FCM token on creation.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        recyclerView = findViewById(R.id.recycler_view);

        searchButton = findViewById(R.id.main_search_btn);
        backBtn = findViewById(R.id.back_btn);

        // Sets up a click listener to navigate to the search user activity.
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatMain.this, SearchUserActivity.class);
                startActivity(intent);
            }
        });

        // Setup RecyclerView to display recent chatrooms
        setupRecyclerView();
        // Retrieves the FCM token of the current user and updates it in Firestore.
        getFCMToken();

        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });
    }


    // Configures the RecyclerView with a Firestore query to display recent chatrooms.
    void setupRecyclerView() {
        // Construct a Firestore query to retrieve chatrooms where the current user is a participant,
        // ordered by the timestamp of the last message in descending order.
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        // Configure FirestoreRecyclerOptions to define how the Firestore query results should be mapped to ChatroomModel objects.
        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();

        // Initialize the RecyclerView adapter with the FirestoreRecyclerOptions and the context of the ChatMain activity.
        adapter = new RecentChatRecyclerAdapter(options, ChatMain.this);
        // Set the layout manager for the RecyclerView to display items in a linear vertical list.
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatMain.this));
        // Set the adapter for the RecyclerView to the initialized adapter.
        recyclerView.setAdapter(adapter);
        // Start listening for Firestore updates to update the RecyclerView when data changes.
        adapter.startListening();

    }


    // Retrieves the current user's Firebase Cloud Messaging token and updates it in Firestore.
    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken", token);

            }
        });
    }
}

