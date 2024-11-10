package com.joe.assignment2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joe.assignment2.model.UserModel;
import com.google.firebase.Timestamp;
import com.joe.assignment2.utils.FirebaseUtil;

public class LoginActivity extends AppCompatActivity {
    UserModel userModel;
    private TextView registerTextView;
    private Button loginButton;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.grey_white, getTheme()));
        window.setNavigationBarColor(getResources().getColor(R.color.grey_white, getTheme()));

        registerTextView = findViewById(R.id.register);
        loginButton = findViewById(R.id.loginButton);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        sharedPreferences = getSharedPreferences("user info", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userModel = new UserModel(null, null, null, null);

        //If current user is log in previously, then direct to main page
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (!email.matches(emailPattern)) {
            inputEmail.setError("Please Enter Valid Email");
        } else if (password.isEmpty()) {
            inputPassword.setError("Please Enter Valid Password");
        } else {
            // User authentication by using email and password
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                // TODO: the code skipped in unknown circumstance
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = fAuth.getCurrentUser().getUid();
                        String localUserId = sharedPreferences.getString("user id", "");
                        if (!userId.equals(localUserId)) {
                            /* If local userId stored in shared preferences not equal to the current userId,
                            save the user information into shared preferences.
                             */
                            fStore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String userName = documentSnapshot.getString("username");
                                    String contactNo = documentSnapshot.getString("contact number");
                                    editor.putString("username", userName);
                                    editor.putString("email", email);
                                    editor.putString("contact number", contactNo);
                                    editor.putString("user id", userId);
                                    editor.commit();
                                }
                            });
                        }

                        // Redirect user to main page
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        fetchUserDetailsByUid(FirebaseUtil.currentUserId());
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Display failure message if the authentication is fail
                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void fetchUserDetailsByUid(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userModel.setUserId(uid);

        db.collection("Users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String username = document.getString("username");
                    String contactNumber = document.getString("contact number");

                    if (username != null && contactNumber != null) {
                        userModel.setUsername(username);
                        userModel.setPhone(contactNumber);
                        userModel.setCreatedTimestamp(Timestamp.now());
                        FirebaseUtil.currentUserDetails().set(userModel);
                    }
                }
            } else {
                Toast.makeText(this, "?", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
