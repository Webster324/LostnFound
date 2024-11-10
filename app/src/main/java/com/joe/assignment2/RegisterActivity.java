package com.joe.assignment2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText registerUserName, registerEmail, registerPassword, registerContactNo;
    private Button registerButton;
    private ImageView registerUserProfileImage;
    private FirebaseAuth fAuth;
    private StorageReference storageRef;
    private Database database;
    private String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.grey_white, getTheme()));
        window.setNavigationBarColor(getResources().getColor(R.color.grey_white, getTheme()));

        registerUserProfileImage = findViewById(R.id.registerProfileImageView);
        registerUserName = findViewById(R.id.registerUserName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerContactNo = findViewById(R.id.registerContactNumber);
        registerButton = findViewById(R.id.registerButton);
        fAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        database = new Database();

        registerUserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery for user to select image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 200);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registration();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Set selected image to registerUserProfileImage
            registerUserProfileImage.setImageURI(imageUri);
            registerUserProfileImage.setBackground(null);
        }
    }

    private void registration() {
        String userName = registerUserName.getText().toString();
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();
        String contactNo = registerContactNo.getText().toString();
        if (userName.isEmpty()) {
            registerUserName.setError("Please Enter Username");
        } else if (!email.matches(emailPattern)) {
            registerEmail.setError("Please Enter Valid Email");
        } else if (password.isEmpty() || password.length() < 6) {
            registerPassword.setError("Please Enter Valid Password");
        } else if (contactNo.isEmpty()) {
            registerContactNo.setError("Please Enter Contact Number");
        } else if (imageUri == null) {
            Toast.makeText(RegisterActivity.this, "Please Upload Profile Image", Toast.LENGTH_LONG).show();
        } else {
            // Register user based on the email and password
            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = fAuth.getCurrentUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", userName);
                    user.put("email", email);
                    user.put("contact number", contactNo);
                    user.put("status", 1);

                    // Upload profile image with the filename named with userId to Firebase Storage
                    storageRef.child(userId).putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Store user information in Cloud FirebaseStore
                    database.createDocument("Users", user, userId, docRef->{},
                            e -> Toast.makeText(RegisterActivity.this, "Failed to store user information", Toast.LENGTH_SHORT).show());
                    Toast.makeText(RegisterActivity.this, "Registration Successfully. Please login in", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}