package com.joe.assignment2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.joe.assignment2.R;
import com.joe.assignment2.model.ChatMessageModel;
import com.joe.assignment2.utils.FirebaseUtil;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;

    // Constructor initializes the adapter with Firestore options and application context.
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        // Determine if the current user is the sender to align messages left or right
        if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
            // Hide the left chat layout and show the right chat layout for the current user's messages
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);

            // Check if the message is text or image and display appropriately
            if (model.getMessageType() == ChatMessageModel.TYPE_TEXT) {
                // If the message is text, display it in the right chat text view
                holder.rightChatTextview.setVisibility(View.VISIBLE);
                holder.rightChatImageview.setVisibility(View.GONE);
                holder.rightChatTextview.setText(model.getMessage());
            } else if (model.getMessageType() == ChatMessageModel.TYPE_IMAGE) {
                // If the message is an image, display it in the right chat image view
                holder.rightChatTextview.setVisibility(View.GONE);
                holder.rightChatImageview.setVisibility(View.VISIBLE);
                // Use Glide to load and display image messages
                Glide.with(context).load(model.getImageUrl()).into(holder.rightChatImageview);
            }
        } else {
            // Align messages from other users to the left
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);

            // If the message is text, display it in the left chat text view
            if (model.getMessageType() == ChatMessageModel.TYPE_TEXT) {
                holder.leftChatTextview.setVisibility(View.VISIBLE);
                holder.leftChatImageview.setVisibility(View.GONE);
                holder.leftChatTextview.setText(model.getMessage());
            } else if (model.getMessageType() == ChatMessageModel.TYPE_IMAGE) {
                // If the message is an image, display it in the left chat image view
                holder.leftChatTextview.setVisibility(View.GONE);
                holder.leftChatImageview.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getImageUrl()).into(holder.leftChatImageview);
            }
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the type of view needed based on the message type (text or image)
        ChatMessageModel model = getItem(position);
        return model.getMessageType();
    }

    // View holder class to hold references to the views for each item in the RecyclerView
    class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;
        ImageView rightChatImageview, leftChatImageview;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the layout and views for message display
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatImageview = itemView.findViewById(R.id.left_chat_imageview);
            rightChatImageview = itemView.findViewById(R.id.right_chat_imageview);
        }
    }
}
