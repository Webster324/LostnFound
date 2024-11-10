package com.joe.assignment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.joe.assignment2.adapter.ChatRecyclerAdapter;
import com.joe.assignment2.model.ChatMessageModel;
import com.joe.assignment2.model.ChatroomModel;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;
import com.joe.assignment2.utils.FirebaseUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    // Constants for identifying the request for image picking.
    private static final int PICK_IMAGE_REQUEST = 1;

    // Fields to hold user details and context.
    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    // UI components.
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn, addBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;

    // Initialization and UI setup.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve user details from intent and initialize chat room ID.
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        addBtn = findViewById(R.id.add_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);

        // Load other user's profile picture.
        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(this, uri, imageView);
                    }
                });

        // Set click listeners for buttons, back to previous page when click
        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });
        otherUsername.setText(otherUser.getUsername());

        // Open image picker when add button is clicked.
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Send message when send button is clicked.
        sendMessageBtn.setOnClickListener((v -> {
            //get user input message
            String message = messageInput.getText().toString().trim();
            //if user input message is empty, no message is send
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        // Get or create chatroom model and setup RecyclerView.
        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    // Setups up the chat messages RecyclerView with Firebase query.
    void setupChatRecyclerView() {
        // Get chat messages for the specified chat room, latest message will display at top
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Configure options for the FirestoreRecyclerAdapter
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        // Create a new instance of ChatRecyclerAdapter with the provided options
        adapter = new ChatRecyclerAdapter(options, getApplicationContext());
        // Create a LinearLayoutManager for the RecyclerView
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // Set the layout manager to reverse layout for displaying messages from bottom to top
        manager.setReverseLayout(true);
        // Set the layout manager for the RecyclerView
        recyclerView.setLayoutManager(manager);
        // Set the adapter for the RecyclerView
        recyclerView.setAdapter(adapter);
        // Start listening for changes in the Firestore data and update the RecyclerView accordingly
        adapter.startListening();
        // Register an observer to listen for when message are inserted into the adapter
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // When new message are inserted, scroll the RecyclerView to the top (latest message)
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    // Handles sending text messages to Firebase and updates UI accordingly.
    void sendMessageToUser(String message) {

        // Update chatroom model with last message details.
        // Set the timestamp of the last message to the current time.
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        // Set the sender ID of the last message to the current user's ID.
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        // Set the content of the last message.
        chatroomModel.setLastMessage(message);
        // Update the chatroom details in the database.
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        // Create chat message model and send it to Firebase.
        // Create a new chat message object with the message content, sender ID, timestamp, and type (text).
        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now(), ChatMessageModel.TYPE_TEXT);
        // Add the new message to the chatroom messages collection in the database.
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {// Add a completion listener to handle the result of adding the message.
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            // Clear the message input field after sending the message.
                            messageInput.setText("");
                            // Send push notification.
                            sendNotification(message);
                        }
                    }
                });
    }

    // Checks if chatroom exists or creates a new one if it doesn't.
    void getOrCreateChatroomModel() {
        // Retrieve the chatroom details Firebase Database
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Extract the chatroom model from the task result
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    // If the chatroom model does not exist (i.e., it's the first time the users are chatting):
                    // Create a new chatroom model with default values
                    chatroomModel = new ChatroomModel(
                            // Set the chatroom ID
                            chatroomId,
                            // Set the sender and receiver user IDs
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                            // Set the timestamp to the current time
                            Timestamp.now(),
                            // Set the initial last message content to an empty string
                            ""
                    );
                    // Set the newly created chatroom model in the database
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    // Sends a push notification using Firebase Cloud Messaging (FCM).
    void sendNotification(String message) {

        // Get current user details.
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try {
                    // Create JSON object for notification.
                    JSONObject jsonObject = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", currentUser.getUsername());
                    notificationObj.put("body", message);

                    // Create JSON object for data.
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());

                    // Construct final JSON object.
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);
                    jsonObject.put("to", otherUser.getFcmToken());

                    callApi(jsonObject); // Call FCM API to send notification.


                } catch (Exception e) {
                    // Handle exception.
                    Log.e("NotificationError", "Error creating JSON for notification: " + e.getMessage());
                }

            }else {
                // Handle task failure.
                Log.e("NotificationError", "Failed to get current user details: " + task.getException());
            }
        });

    }

    // Helper method to call FCM API to send notifications.
    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "key=")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle API call failure.
                Log.e("NotificationError", "Failed to send notification: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle API call response.
                if (!response.isSuccessful()) {
                    Log.e("NotificationError", "Failed to send notification. Response: " + response.body().string());
                }
            }
        });

    }

    // Handles the result from image picker activity and uploads selected image to Firebase Storage.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebaseStorage(imageUri);// Upload selected image.
        }
    }

    // Uploads an image to Firebase Storage and sends a message with the image URL.
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("chat_images/" + UUID.randomUUID().toString());
        storageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        sendMessageWithImage(downloadUri.toString());
                    } else {
                        // Handle errors here, e.g., show a Toast to the user
                        Toast.makeText(ChatActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Sends a chat message with an image URL, updating Firebase with the image message type.
    private void sendMessageWithImage(String imageUrl) {
        ChatMessageModel chatMessageModel = new ChatMessageModel(imageUrl, FirebaseUtil.currentUserId(), Timestamp.now(), ChatMessageModel.TYPE_IMAGE);
        chatMessageModel.setMessageType(ChatMessageModel.TYPE_IMAGE); // Setting the message type as image
        //if last message is image, set the last message to [image] text
        chatroomModel.setLastMessage("[image]");
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel);
    }


}