package com.hqs.alx.mushalmapp2;

/**
 * Created by Alex on 20/01/2018.
 */

public class ChatMessage {

    private String text;
    private String userUID;
    private String name;
    private String userPhotoUrl;
    private String photoUrl;
    private boolean isCurrentUser;

    public ChatMessage() {
    }

    public ChatMessage(String text, String userUID, String name, String userPhotoUrl, String photoUrl, boolean isCurrentUser) {
        this.text = text;
        this.userUID = userUID;
        this.name = name;
        this.userPhotoUrl = userPhotoUrl;
        this.photoUrl = photoUrl;
        this.isCurrentUser = isCurrentUser;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

}
