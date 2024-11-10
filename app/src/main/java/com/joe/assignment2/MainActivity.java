package com.joe.assignment2;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    CreatePostFragment createPostFragment = new CreatePostFragment();
    PostsFragment postsFragment = new PostsFragment();
    UserProfileFragment userProfileFragment = new UserProfileFragment();
    TextView navigationBarTitleTextView;
    Button actionButton;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.grey_white, getTheme()));
        window.setNavigationBarColor(getResources().getColor(R.color.grey_white, getTheme()));

        setContentView(R.layout.activity_main);

        bottomNavigationView
                = findViewById(R.id.bottomNavigationView);
        actionButton = findViewById(R.id.action_button);
        navigationBarTitleTextView = findViewById(R.id.title_textview);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.menu_posts);

        MenuItem menuProfile = bottomNavigationView.getMenu().findItem(R.id.menu_profile);


    }



    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item)
    {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_chat_24, getTheme());

        if (item.getItemId() == R.id.menu_posts) {
            postsFragment = new PostsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentLayout, postsFragment)
                    .commit();
            navigationBarTitleTextView.setText("Posts");
            actionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            actionButton.setText("");
            actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatMain.class);
                startActivity(intent);
            });
            return true;
        } else if (item.getItemId() == R.id.menu_create_post) {
            if (getCurrentFragment() instanceof CreatePostFragment) {
                createPostFragment = new CreatePostFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentLayout, createPostFragment)
                    .commit();
            navigationBarTitleTextView.setText("Create Post");
            actionButton.setText("Post");
            actionButton.setOnClickListener(v -> createPostFragment.onPostButtonClickListener());
            actionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            return true;
        } else if (item.getItemId() == R.id.menu_profile) {
            if (getCurrentFragment() instanceof UserProfileFragment) {
                userProfileFragment = new UserProfileFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentLayout, userProfileFragment)
                    .commit();
            navigationBarTitleTextView.setText("Profile");
            actionButton.setText("");
            actionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatMain.class);
                startActivity(intent);
            });
            return true;
        }
        return false;
    }


    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragmentLayout);
    }



}