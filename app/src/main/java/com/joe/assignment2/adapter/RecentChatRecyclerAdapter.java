package com.joe.assignment2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.joe.assignment2.ChatActivity;
import com.joe.assignment2.R;
import com.joe.assignment2.model.ChatroomModel;
import com.joe.assignment2.model.UserModel;
import com.joe.assignment2.utils.AndroidUtil;
import com.joe.assignment2.utils.FirebaseUtil;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    // Constructor initializes the adapter with Firestore options and application context.
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    // Binds data from Firestore to the RecyclerView, triggers UI updates
    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        // Fetch other user details asynchronously from Firestore
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        // Get UserModel object from Firestore result
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        // Set username text
                        holder.usernameText.setText(otherUserModel.getUsername() != null ? otherUserModel.getUsername() : "Unknown User");

                        // Set last message text, add "You:" text if the last message was sent by the current user
                        if (lastMessageSentByMe) {
                            if (model.getLastMessage() != null) {
                                holder.lastMessageText.setText("You : " + model.getLastMessage());
                            }
                        } else {
                            if (model.getLastMessage() != null) {
                                holder.lastMessageText.setText(model.getLastMessage());
                            }
                        }


                        // Set last message time text
                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                        // Handles navigation to the chat activity when a chat is clicked
                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            // Pass UserModel as Intent extra
                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            // Start ChatActivity
                            context.startActivity(intent);
                        });

                    } else {
                        // Set default values when user data is not available
                        holder.usernameText.setText("Unknown User");
                        holder.lastMessageText.setText("");
                        holder.lastMessageTime.setText("");
                    }
                });
    }


    // Inflates the layout for each item in the RecyclerView
    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    // ViewHolder class to hold references to the views for each item in the RecyclerView
    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
        }
    }

}
