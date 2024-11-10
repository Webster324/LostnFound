package com.joe.assignment2;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class CreatePostFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private EditText titleTextField;
    private EditText descriptionTextField;
    private EditText locationTextField;
    private Spinner postTypeSelection;
    String[] TYPE_OF_POST = new String[]{
            "Lost",
            "Found"
    };
    private String selectedPostType;
    private LinearLayout addImageButtonContainer;

    private ImageView previewImageView;

    private FirebaseAuth fAuth;
    private String userId;

    private String postId;

    private ArrayList<Uri> image = new ArrayList<>();



    // Empty Constructor
    public CreatePostFragment() {
    }

    /**
     * The Edit Post function will reuse the same fragment by postID needed to pass in
     * @param postID - The post's ID of the post to be edited
     */
    public static CreatePostFragment newInstance(String postID) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, postID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Get selected image
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            RelativeLayout imageButton = generateAddImageButton(imageUri,v->{
                deleteImage(imageUri);
                RelativeLayout parentLayout = (RelativeLayout) v.getTag();
                addImageButtonContainer.removeView(parentLayout);
                previewImageView.setImageResource(R.drawable.ic_launcher_background);
            });
            if (addImageButtonContainer.getChildCount() == 1){
                previewImageView.setImageURI(imageUri);
            }
            addImageButtonContainer.addView(imageButton, addImageButtonContainer.getChildCount() - 1);
            image.add(imageUri);
        }
    }


    /**
     * Upload Image to Firebase Storage
     * @param filePath - The path file of the Image
     * @param postId - The id of the post
     */
    private void uploadImage(Uri filePath, String postId) {
        if (filePath != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            String imageName = "post_image/" + userId + "/" + postId + "/" + UUID.randomUUID().toString(); // Generate a unique image name
            StorageReference imageRef = storageRef.child(imageName);

            imageRef.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {

                    })
                    .addOnFailureListener(e -> {

                    });
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        titleTextField = view.findViewById(R.id.title_text_field);
        descriptionTextField = view.findViewById(R.id.description_text_field);
        locationTextField = view.findViewById(R.id.location_text_field);
        postTypeSelection = view.findViewById(R.id.post_type_selection);
        addImageButtonContainer = view.findViewById(R.id.add_image_button_container);
        previewImageView = view.findViewById(R.id.preview_image_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, TYPE_OF_POST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postTypeSelection.setAdapter(adapter);
        selectedPostType = TYPE_OF_POST[0];

        fAuth = FirebaseAuth.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        postTypeSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedPostType = (String) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        RelativeLayout imageButton = generateAddImageButton(null, null);
        addImageButtonContainer.addView(imageButton);

        FirebaseApp.initializeApp(getContext());

        // If it is Edit Post mode, fill the post information into the form
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_PARAM1);

            Database db = new Database();

            // Get Post information from the Firestore
            db.getDocument("Posts", postId, new Database.OnDocumentLoadedListener() {
                @Override
                public void onDocumentLoaded(Map<String, Object> data) {
                    String title = (String) data.get("title");
                    String description = (String) data.get("description");
                    String location = (String) data.get("Location");
                    String type = (String) data.get("type");
                    String imagePath = (String) data.get("src_path");

                    titleTextField.setText(title);
                    descriptionTextField.setText(description);
                    locationTextField.setText(location);

                    for (int i = 0; i < TYPE_OF_POST.length; i++) {
                        if (Objects.equals(type, TYPE_OF_POST[i])){
                            selectedPostType = TYPE_OF_POST[i];
                            postTypeSelection.setSelection(i);
                        }
                    }

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

                    storageRef.listAll().addOnSuccessListener(listResult -> {
                        for (StorageReference item : listResult.getItems()) {
                            item.getDownloadUrl().addOnSuccessListener(uri -> {
                                RelativeLayout imageButton = generateAddImageButton(uri, v->{
                                    deleteImage(uri);
                                    RelativeLayout parentLayout = (RelativeLayout) v.getTag();
                                    addImageButtonContainer.removeView(parentLayout);
                                    previewImageView.setImageResource(R.drawable.ic_launcher_background);
                                });



                                if (addImageButtonContainer.getChildCount() == 1){
                                    Glide.with(getContext())
                                            .load(uri)
                                            .into(previewImageView);
                                }
                                addImageButtonContainer.addView(imageButton, addImageButtonContainer.getChildCount() - 1);
                                image.add(uri);
                            });
                        }
                    });
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

        return view;
    }


    // Post / Update the post to Firestore when user clicked Post / Update Post button on the top right corner of the page
    public void onPostButtonClickListener() {
        if(!formCheck()){
            return;
        }


        Database database = new Database();
        String collection = "Posts";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userIdRef = db.collection("users").document(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("Location", locationTextField.getText().toString());
        data.put("creation_time", FieldValue.serverTimestamp());
        data.put("description", descriptionTextField.getText().toString());
        data.put("last_update_time", FieldValue.serverTimestamp());
        data.put("status", 1);
        data.put("title", titleTextField.getText().toString());
        data.put("type", selectedPostType);
        data.put("userId", userIdRef);

        // Create Post
        if (postId == null) {
            CollectionReference collectionReference = db.collection(collection);
            DocumentReference documentReference = collectionReference.document();

            database.createDocument(collection, data, documentReference.getId(), docRef -> {}, e -> {});

            Database updateDatabase = new Database();
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("src_path", "post_image/" + userId + "/" + documentReference.getId() + "/");
            updateDatabase.updateDocument(collection, documentReference.getId(), updateData, docRef -> {}, e -> {});

            for (int i = 0; i < image.size(); i++) {
                uploadImage(image.get(i), documentReference.getId());
            }

            image.clear();
            Toast.makeText(getContext(), "Post Successfully", Toast.LENGTH_SHORT);
        }// Update Post
        else{
            database.updateDocument(
                    collection,
                    postId,
                    data,
                    docRef -> {},
                    e -> {}
            );
            // Delete the image that been deleted
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("post_image/" + userId + "/" + postId + "/");

            storageRef.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.i("URI Firebase", String.valueOf(uri));
                        Log.i("URI Local", String.valueOf(image.size()));
                        if (!image.contains(uri)) {
                            item.delete().addOnSuccessListener(Void -> {
                            }).addOnFailureListener(exception -> {
                            });
                        }
                    });
                }
            }).addOnFailureListener(exception -> {
            });


            for (int i = 0; i < image.size(); i++) {
                if ("content".equals(image.get(i).getScheme())) {
                    uploadImage(image.get(i), postId);
                }
            }

        }
        clearForm();
    }

    // Clear the form after user posted the post
    public void clearForm() {
        titleTextField.setText("");
        descriptionTextField.setText("");
        locationTextField.setText("");
        postTypeSelection.setSelection(0);
        selectedPostType = TYPE_OF_POST[0];
        RelativeLayout imageButton = generateAddImageButton(null, null);
        previewImageView.setImageResource(R.drawable.ic_launcher_background);
        addImageButtonContainer.removeAllViews();
        addImageButtonContainer.addView(imageButton);
    }

    /**
     * Generate Image Button
     * @param imageUri - File path of the image
     * @param deleteButtonListener - Listener for delete button in the image button
     * @return Image Button
     */
    private RelativeLayout generateAddImageButton(Uri imageUri, View.OnClickListener deleteButtonListener) {
        Context context = getContext();

        RelativeLayout buttonContainer = new RelativeLayout(context);

        ImageButton button = new ImageButton(context);
        Button deleteButton = new Button(context);

        deleteButton.setTag(buttonContainer);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                dpToPx(requireContext(), 100),
                dpToPx(requireContext(), 100)
        );
        int margin = dpToPx(context, 5); // Convert dp to pixels for margin
        layoutParams.setMargins(margin, margin, margin, margin); // Set margin
        buttonContainer.setLayoutParams(layoutParams);

        button.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        deleteButton.setLayoutParams(new RelativeLayout.LayoutParams(
                dpToPx(requireContext(), 24),
                dpToPx(requireContext(), 24)
        ));

        RelativeLayout.LayoutParams deleteParams = (RelativeLayout.LayoutParams) deleteButton.getLayoutParams();
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
        deleteButton.setBackgroundResource(R.drawable.round_shape);
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setText("-");
        deleteButton.setTextSize(dpToPx(requireContext(), 5));
        deleteButton.setPadding(0, 0, 0, 0);

        buttonContainer.addView(button);
        buttonContainer.addView(deleteButton);


        if (imageUri != null){
            Glide.with(getContext())
                    .load(imageUri)
                    .into(button);
            button.setBackgroundResource(android.R.color.transparent);
            button.setOnClickListener(v -> Glide.with(getContext())
                    .load(imageUri)
                    .into(previewImageView));
            deleteButton.setOnClickListener(deleteButtonListener);
        }else{
            deleteButton.setVisibility(View.GONE);
            button.setBackgroundResource(R.drawable.drawable_image_picker);
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_grey));
            button.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            int padding = dpToPx(context, 5);
            button.setPadding(padding, padding, padding, padding);
            button.setImageResource(R.drawable.add_24);
            button.setColorFilter(ContextCompat.getColor(context, R.color.light_grey), PorterDuff.Mode.SRC_IN);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            });
        }

        return buttonContainer;
    }

    /**
     * Convert DP to PX
     * @param context
     * @param dp - The Value in dp
     */
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Delete Image from the ArrayList
     * @param uri - File path*/
    public void deleteImage(Uri uri){
        image.remove(uri);
    }

    /**
     * Author: Eii Chee Hieng
     * Check the form and show WARNing message to user if there is empty field
     */
    public boolean formCheck(){
        String message = null;

        if (image.isEmpty()){
            message = "Please upload at least 1 image";
        }else if (titleTextField.getText().toString().isEmpty()){
            message = "Please fill in Title.";
        }else if (descriptionTextField.getText().toString().isEmpty()){
            message = "Please fill in Description.";
        } else if (locationTextField.getText().toString().isEmpty()){
            message = "Please fill in location.";
        }

        if (message != null){
            Toast toast= Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
        return message == null;
    }
}