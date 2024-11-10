package com.joe.assignment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;

public class OwnerDetailActivity extends AppCompatActivity {
    private TextView ownerName, ownerEmail, ownerContactNo;
    private ImageView ownerImage;
    private Button contactButton;
    private FirebaseFirestore fStore;
    private StorageReference storageRef;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_detail);

        ownerName = findViewById(R.id.ownerNameTextView);
        ownerEmail = findViewById(R.id.ownerEmailTextView);
        ownerContactNo = findViewById(R.id.ownerContactNumberTextView);
        ownerImage = findViewById(R.id.ownerProfileImage);
        contactButton = findViewById(R.id.contactOwnerButton);
        fStore = FirebaseFirestore.getInstance();

        Uri uri = getIntent().getData();

        if (uri != null) {
            String uriString = uri.toString();
            int startIndex = uriString.indexOf("=") + 1;
            String userId = uriString.substring(startIndex);
            if (userId != null) {
                // Retrieve owner details from firestore database
                fStore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");
                            String contactNo = documentSnapshot.getString("contact number");

                            ownerName.setText(username);
                            ownerEmail.setText(email);
                            ownerContactNo.setText(contactNo);
                        }else{
                            Toast.makeText(OwnerDetailActivity.this, "Owner does not exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OwnerDetailActivity.this, "Failed to retrieve owner data", Toast.LENGTH_SHORT).show();
                    }
                });

                storageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(OwnerDetailActivity.this)
                                .load(uri)
                                .transform(new CircleCrop())
                                .into(ownerImage);
                    }
                });
            } else {
                // Display a failure message if the user id is null (invalid)
                Toast.makeText(OwnerDetailActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = fAuth.getCurrentUser();
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();

                // Redirect user to login page if user is not authenticated
                if (currentUser == null) {
                    Toast.makeText(OwnerDetailActivity.this, "Please register an account.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(OwnerDetailActivity.this, LoginActivity.class));
                    finish();
                } else {
                    fStore.collection("Users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {

                                    // Get the document ID of the user, which serves as the user's UID
                                    String userId = task.getResult().getDocuments().get(0).getId();

                                    // Create UserModel object with user ID and username
                                    UserModel model = new UserModel();
                                    model.setUserId(userId);
                                    model.setUsername(username);

                                    // Start ChatActivity and pass UserModel as intent extra
                                    Intent intent = new Intent(OwnerDetailActivity.this, ChatActivity.class);
                                    AndroidUtil.passUserModelAsIntent(intent, model);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(OwnerDetailActivity.this, "Cannot find users.", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                }
            });
    }
}