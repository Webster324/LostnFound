package com.joe.assignment2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditInfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText editUserName, editContactNo;
    private Database database;
    private StorageReference storageRef;
    private Button submitEditInfoButton, uploadImageButton;
    private ImageView updateProfileImage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Uri imageUri;
    private String userName, contactNo, userId;


    public EditInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditInfoFragment newInstance(String param1, String param2) {
        EditInfoFragment fragment = new EditInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_info, container, false);
        editUserName = view.findViewById(R.id.editUserNameText);
        editContactNo = view.findViewById(R.id.editContactNoText);
        submitEditInfoButton = view.findViewById(R.id.submitEditInfoButton);
        updateProfileImage = view.findViewById(R.id.updateProfileImageView);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        database = new Database();

        sharedPreferences = getActivity().getSharedPreferences("user info", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user id", "");
        userName = sharedPreferences.getString("username", "");
        contactNo = sharedPreferences.getString("contact number", "");

        storageRef = FirebaseStorage.getInstance().getReference();
        editUserName.setText(userName);
        editContactNo.setText(contactNo);


        // Find the user profile image that stored in the FirebaseStorage by user Id and fetch it to updateProfileImage
        storageRef.child("profile_images").child(userId).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(getActivity())
                .load(uri)
                .transform(new CircleCrop())
                .into(updateProfileImage));

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery for user to select image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 200);
            }
        });
        submitEditInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editInfo();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Set selected image to updateProfileImage
            updateProfileImage.setImageURI(imageUri);
        }
    }

    private void editInfo() {
        String userName = editUserName.getText().toString();
        String contactNumber = editContactNo.getText().toString();

        if (userName.isEmpty()) {
            editUserName.setError("Please Enter UserName");
        } else if (contactNumber.isEmpty()) {
            editContactNo.setError("Please Enter Contact Number");
        } else {
            // If image is selected, upload it to firebase storage and update user info; otherwise, directly update user info
            if (imageUri != null) {
                uploadImageAndUpdateUserInfo(userName, contactNumber);
            } else {
                updateUserInfo(userName, contactNumber);
            }
        }
    }

    private void uploadImageAndUpdateUserInfo(final String userName, final String contactNumber) {
        storageRef.child("profile_images").child(userId).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Update user info after image upload successfully
                updateUserInfo(userName, contactNumber);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Display a failure message if image upload failed
                Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(String userName, String contactNumber) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", userName);
        user.put("contact number", contactNumber);

        // Update user information in the firestore database
        database.updateDocument("Users", userId, user, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> user_chatroom = new HashMap<>();
                user_chatroom.put("username", userName);
                database.updateDocument("users", userId, user_chatroom,docRef -> {}, e -> {});
                // Update shared preferences
                editor = sharedPreferences.edit();
                editor.putString("username", userName);
                editor.putString("contact number", contactNumber);
                editor.commit();
                Toast.makeText(getActivity(), "Information updated successfully.", Toast.LENGTH_SHORT).show();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Display a failure message if update user info failed
                Toast.makeText(getActivity(), "Failed to update information. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}