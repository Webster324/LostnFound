package com.joe.assignment2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;

public class SearchUserActivity extends AppCompatActivity {
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        backBtn = findViewById(R.id.back_btn);
        EditText usernameInput = findViewById(R.id.usernameInput);
        Button searchUserButton = findViewById(R.id.searchUserButton);

        searchUserButton.setOnClickListener(view -> {
            // Get username from input field
            String username = usernameInput.getText().toString().trim();
            // Call method to search for user
            searchUser(username);
        });

        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });
    }

    // Method to search for user in Firestore
    private void searchUser(String username) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Find username from database based on user given username
        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {

                        // Get the document ID of the user, which serves as the user's UID
                        String userId = task.getResult().getDocuments().get(0).getId();

                        // Check if the searched username is the same as the current user's username
                        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            // Display message that user cannot send message to their own account
                            Toast.makeText(this, "You cannot send message to your", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create UserModel object with user ID and username
                            UserModel model = new UserModel();
                            model.setUserId(userId);
                            model.setUsername(username);

                            // Start ChatActivity and pass UserModel as intent extra
                            Intent intent = new Intent(this, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            this.startActivity(intent);
                        }
                    } else {
                        // User does not exist
                        Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
