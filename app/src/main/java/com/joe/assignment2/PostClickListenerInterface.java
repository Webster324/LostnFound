package com.joe.assignment2;

// The interface to handle click event for edit and delete button in my posts
public interface PostClickListenerInterface {
    public interface PostsClickListener {
        void onMyPostsClicked(String postId);
        void onPostDeleteButtonClicked(String postId, String userID);
    }
}
