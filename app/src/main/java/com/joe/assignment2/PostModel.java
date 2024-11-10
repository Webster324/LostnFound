package com.joe.assignment2;

public class PostModel {
    String postTitle;
    String postDescription;
    String postLocation;
    String postType;
    String postUserName;
    String postUserId;
    String postImage;
    private String userProfileImageURL;
    private String postID;

    private static MODE mode;
    public enum MODE {
            Posts,
            MyPosts
    }

    public PostModel(String postID, String postTitle, String postDescription, String postLocation, String postType, String postUserName, String postUserId, String postImage, String userProfileImageURL) {
        this.postID = postID;
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postLocation = postLocation;
        this.postType = postType;
        this.postImage = postImage;
        this.postUserName = postUserName;
        this.postUserId = postUserId;
        this.userProfileImageURL = userProfileImageURL;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public String getPostLocation() {
        return postLocation;
    }

    public String getPostType() {
        return postType;
    }

    public String getPostUserName(){
        return postUserName;
    }

    public String getPostUserId(){
        return postUserId;
    }

    public String getImage() {
        return postImage;
    }

    public String getUserProfileImageURL() {return userProfileImageURL;}

    public String getPostID(){ return postID;}

    public static MODE getMode() {return mode;}

    public static void setMode(MODE mode) { PostModel.mode = mode;}

}
