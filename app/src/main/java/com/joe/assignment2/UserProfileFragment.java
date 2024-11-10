package com.joe.assignment2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView email, name;
    private ImageView profileImage;
    private Button generateQRButton, editInfoButton, changePasswordButton, deteleAccButton, logOutButton, myPostsButton;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private SharedPreferences sharedPreferences;
    private StorageReference storageRef;
    private String userId, userEmail, userName;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        email = view.findViewById(R.id.emailTextView);
        name = view.findViewById(R.id.userNameTextView);
        profileImage = view.findViewById(R.id.userProfileImageView);
        generateQRButton = view.findViewById(R.id.generateQRButton);
        editInfoButton = view.findViewById(R.id.editInfoButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        deteleAccButton =  view.findViewById(R.id.deleteButton);
        logOutButton = view.findViewById(R.id.logOuButton);
        myPostsButton = view.findViewById(R.id.myPostButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        sharedPreferences = getActivity().getSharedPreferences("user info", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user id", "");
        userEmail = sharedPreferences.getString("email", "");
        userName = sharedPreferences.getString("username", "");
        email.setText(userEmail);
        name.setText(userName);


        // Find the user profile image that stored in the FirebaseStorage by user Id and fetch it to profileImage
        storageRef.child("profile_images").child(userId).getDownloadUrl().addOnSuccessListener(uri -> {
            if (isAdded() && getActivity() != null) {
                Glide.with(getActivity())
                        .load(uri)
                        .transform(new CircleCrop())
                        .into(profileImage);
            }
        });

        generateQRButton.setOnClickListener(v -> {
            // Navigate to GenerateQR Fragment
            Fragment generateQRFragment = new GenerateQRFragment();
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, generateQRFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        myPostsButton.setOnClickListener(v -> {
            // Navigate to myPosts fragment
            Fragment myPostsFragment = PostsFragment.newInstance(userId);
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, myPostsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            TextView navbarTitle = getActivity().findViewById(R.id.title_textview);
            navbarTitle.setText("My Posts");
        });
        editInfoButton.setOnClickListener(v -> {
            // Navigate to EditInfo fragment
            Fragment editInfoFragment = new EditInfoFragment();
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editInfoFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        changePasswordButton.setOnClickListener(v -> {
            // Navigate to ChangePasswordFragment
            Fragment changePasswordFragment = new ChangePasswordFragment();
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, changePasswordFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        deteleAccButton.setOnClickListener(v -> {
            // Navigate to website page to perform delete account
            Uri uriUrl = Uri.parse("https://17hieng.com/lostnfound/delete?uid=xxxxxx");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        });

        logOutButton.setOnClickListener(v -> {
            //Log out the user and redirect to login page
            fAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
        return view;
    }
}