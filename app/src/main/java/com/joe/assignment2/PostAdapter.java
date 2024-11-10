package com.joe.assignment2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    Context context;
    ArrayList<PostModel> postModels;
    private PostClickListenerInterface.PostsClickListener listener;
    private ContactClickListenerInterface.ContactClickListener listener1;

    public PostAdapter(Context context, ArrayList<PostModel> postModels, PostClickListenerInterface.PostsClickListener listener, ContactClickListenerInterface.ContactClickListener listener1){
        this.context = context;
        this.postModels = postModels;
        this.listener = listener;
        this.listener1 = listener1;
    }

    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_post_item, parent, false);
        return new PostAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, int position) {
        try {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(postModels.get(position).getImage());

            StorageReference userProfileImageStorageReference = FirebaseStorage.getInstance().getReference().child(postModels.get(position).getUserProfileImageURL());

            storageRef.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        ImageView imageView = new ImageView(context);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setPadding(10, 0, 10, 0);
                        imageView.setAdjustViewBounds(true);
                        Glide.with(context)
                                .load(uri)
                                .into(imageView);

                        holder.imageContainer.addView(imageView);
                    });
                }
            });

            userProfileImageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context)
                        .load(uri)
                        .transform(new CircleCrop())
                        .into(holder.userProfileImageView);
            });


            holder.tvTitle.setText(postModels.get(position).getPostTitle());
            holder.tvDes.setText(postModels.get(position).getPostDescription());
            holder.tvLocation.setText(postModels.get(position).getPostLocation());
            holder.tvType.setText(postModels.get(position).getPostType());
            holder.tvUserName.setText(postModels.get(position).getPostUserName());


            //PostModel.getMode() == PostModel.MODE.Posts mean user browsing general Post
            if (PostModel.getMode() == PostModel.MODE.Posts) {
                if (postModels.get(position).getPostUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.contactButton.setVisibility(View.GONE);
                }
                holder.deleteButton.setVisibility(View.GONE);
                holder.actionButton.setOnClickListener(v -> {
                    listener1.goToChat(postModels.get(position).getPostUserName());
                });
            } else {
                holder.actionButton.setText("Edit");
                holder.actionButton.setOnClickListener(v -> {
                    listener.onMyPostsClicked(postModels.get(position).getPostID());
                });
                holder.deleteButton.setOnClickListener(v -> {
                    listener.onPostDeleteButtonClicked(postModels.get(position).getPostID(), postModels.get(position).postUserId);
                    if (position != RecyclerView.NO_POSITION) {
                        postModels.remove(position);
                        notifyItemRemoved(position);
                    }
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();

                });
            }
        }catch (Error error){
            Toast.makeText(context, "Connection Loss", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView userProfileImageView;
        TextView tvTitle, tvDes, tvLocation, tvType, tvUserName;
        Button actionButton, deleteButton, contactButton;
        LinearLayout imageContainer;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageContainer = itemView.findViewById(R.id.image_container);
            tvTitle = itemView.findViewById(R.id.postTitle);
            tvDes = itemView.findViewById(R.id.postDescription);
            tvLocation = itemView.findViewById(R.id.postLocation);
            tvType = itemView.findViewById(R.id.postType);
            tvUserName = itemView.findViewById(R.id.postUserName);
            userProfileImageView = itemView.findViewById(R.id.profile_picture_image_view);
            actionButton = itemView.findViewById(R.id.contactButton);
            deleteButton = itemView.findViewById(R.id.delete_button);
            contactButton = itemView.findViewById(R.id.contactButton);
        }
    }
}
