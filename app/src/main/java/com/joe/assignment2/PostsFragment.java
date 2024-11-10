package com.joe.assignment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostsFragment extends Fragment implements PostClickListenerInterface.PostsClickListener, ContactClickListenerInterface.ContactClickListener {

    RecyclerView recyclerView;
    PostAdapter adapter;
    ArrayList<PostModel> postModels = new ArrayList<>();
    int[] postImage;

    private FirebaseFirestore fStore;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String userID;

    // Empty constructor
    public PostsFragment() {

    }

    // As the Post and My Post will sharing same fragment, the My Post will pass userID to this newInstance
    public static PostsFragment newInstance(String userID) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // userID != null means user accessing My Post
        if (userID != null) {
            Database db = new Database();
            PostModel.setMode(PostModel.MODE.MyPosts);

            // Get current user's Posts from firebase
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userID);
            db.getDocumentsWithCondition("Posts", "userId", userRef, new Database.OnDocumentsLoadedListener() {
                @Override
                public void onDocumentLoaded(List<Map<String, Object>> documents) {
                    for (Map<String, Object> document : documents) {
                        String postId = (String) document.get("docId");
                        String title = (String) document.get("title");
                        String description = (String) document.get("description");
                        String location = (String) document.get("Location");
                        String type = (String) document.get("type");
                        DocumentReference userRef = (DocumentReference) document.get("userId");
                        String imagePath = (String) document.get("src_path");
                        assert userRef != null;
                        String userProfileImagePath = "profile_images/" + userRef.getId();

                        db.getDocument("Users", userRef.getId(), new Database.OnDocumentLoadedListener() {
                            @Override
                            public void onDocumentLoaded(Map<String, Object> data) {
                                String username = (String) data.get("username");
                                postModels.add(new PostModel(postId, title, description, location, type, username, userRef.getId(), imagePath, userProfileImagePath));
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDocumentNotFound() {
                                // Document does not exist
                            }

                            @Override
                            public void onDocumentLoadFailed(String errorMessage) {
                                // Failed to load document, handle the error
                            }
                        });
                    }
                }

                @Override
                public void onDocumentNotFound() {

                }

                @Override
                public void onDocumentLoadFailed(String errorMessage) {

                }
            });
        } else {
            fStore = FirebaseFirestore.getInstance();
            PostModel.setMode(PostModel.MODE.Posts);

            fStore.collection("Posts").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Access document fields and do something with them
                        String postId = document.getId();
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String location = document.getString("Location");
                        String type = document.getString("type");
                        DocumentReference userRef = document.getDocumentReference("userId");
                        String imagePath = document.getString("src_path");
                        assert userRef != null;
                        String userProfileImagePath = "profile_images/" + userRef.getId();


                        fStore.collection("Users").document(userRef.getId()).get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                DocumentSnapshot userDocument = userTask.getResult();
                                if (userDocument.exists()) {
                                    String username = userDocument.getString("username");
                                    // Populate your postModels ArrayList
                                    postModels.add(new PostModel(postId, title, description, location, type, username, userRef.getId(), imagePath, userProfileImagePath));

                                    // Notify the adapter of the data change
                                    adapter.notifyDataSetChanged();

                                } else {
                                    Log.d("FirestoreData", "User document not found for post");
                                }
                            } else {
                                Log.d("FirestoreData", "Error getting user document for post", userTask.getException());
                            }
                        });

                        // Do something with the retrieved data
                        Log.d("FirestoreData", "Title: " + title + ", Description: " + description + ", Locations:" + location + ", Ref:" + userRef.getPath());
                    }

                } else {
                    Log.d("FirestoreData", "Error getting documents: ", task.getException());
                }
            });
        }


        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        recyclerView = view.findViewById(R.id.postRecycleView);
        recyclerView.setHasFixedSize(true);

        adapter = new PostAdapter(getContext(), postModels, this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when the user submits the query (e.g., presses "Enter" on the keyboard)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the text in the search view changes
                searchPosts(newText);
                return true;
            }
        });

        return view;


    }


    // Handle the Edit Post button click event
    @Override
    public void onMyPostsClicked(String postId) {
        CreatePostFragment myPostsFragment = CreatePostFragment.newInstance(postId);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayout, myPostsFragment);
        fragmentTransaction.addToBackStack("myPosts");
        fragmentTransaction.commit();
        TextView navbarTitle = getActivity().findViewById(R.id.title_textview);
        Button actionButton = getActivity().findViewById(R.id.action_button);
        navbarTitle.setText("Edit Posts");
        actionButton.setText("Update Post");
        actionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // After user update post, user will be navigate back to the My Post Fragment
        actionButton.setOnClickListener(v -> {
            myPostsFragment.onPostButtonClickListener();
            PostsFragment myPostFragment = PostsFragment.newInstance(userID);
            FragmentManager fragManager = getChildFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.fragmentLayout, myPostFragment);
            fragTransaction.addToBackStack("myPosts");
            fragTransaction.commit();
            navbarTitle.setText("My Posts");
            actionButton.setText("");
            actionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        });


    }

    // Handle the Delete Post button click event
    @Override
    public void onPostDeleteButtonClicked(String postId, String userID) {
        Database db = new Database();
        // Delete the image that been deleted
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("post_image/" + userID + "/" + postId + "/");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    item.delete().addOnSuccessListener(Void -> {
                    }).addOnFailureListener(exception -> {
                    });
                });
            }
        }).addOnFailureListener(exception -> {
        });


        db.deleteDocument("Posts", postId, docRef -> {}, e->{});
    }

    @Override
    public void goToChat(String username){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                            Toast.makeText(getActivity(), "You cannot send message to your own", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create UserModel object with user ID and username
                            UserModel model = new UserModel();
                            model.setUserId(userId);
                            model.setUsername(username);

                            // Start ChatActivity and pass UserModel as intent extra
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            this.startActivity(intent);
                        }


                    }else {
                        // Log an error message if the task is not successful
                        Log.e("TAG", "Error getting user document: ", task.getException());
                    }
                });
    }

    public void searchPosts(String query) {
        ArrayList<PostModel> filteredList = new ArrayList<>();

        // Iterate through the original list of posts
        for (PostModel post : postModels) {
            // Convert the title to lowercase for case-insensitive search
            String title = post.getPostTitle().toLowerCase();

            // Check if the title contains the search query
            if (title.contains(query.toLowerCase())) {
                // If the title matches the query, add it to the filtered list
                filteredList.add(post);
            }
        }

        // Update the RecyclerView adapter with the filtered list
        adapter = new PostAdapter(getContext(), filteredList, this, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}