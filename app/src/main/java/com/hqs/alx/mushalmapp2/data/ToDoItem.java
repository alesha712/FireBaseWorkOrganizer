package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alex on 20/05/2018.
 */

public class ToDoItem implements Parcelable{

    private String publisherName;
    private String publisherUID;
    private String subject;
    private String text;
    private String date;
    private String imageURL;
    private String color;
    private String completeBy;
    private String keyRef;
    private boolean isComplete;
    private boolean isImportant;

    public ToDoItem(){}

    public ToDoItem(String publisherName, String publisherUID, String subject, String text, String date, String imageURL, String color, String completeBy, String keyRef, boolean isComplete, boolean isImportant) {
        this.publisherName = publisherName;
        this.publisherUID = publisherUID;
        this.subject = subject;
        this.text = text;
        this.date = date;
        this.imageURL = imageURL;
        this.color = color;
        this.completeBy = completeBy;
        this.keyRef = keyRef;
        this.isComplete = isComplete;
        this.isImportant = isImportant;
    }

    protected ToDoItem(Parcel in) {
        publisherName = in.readString();
        publisherUID = in.readString();
        subject = in.readString();
        text = in.readString();
        date = in.readString();
        imageURL = in.readString();
        color = in.readString();
        completeBy = in.readString();
        keyRef = in.readString();
        isComplete = in.readByte() != 0;
        isImportant = in.readByte() != 0;
    }

    public static final Creator<ToDoItem> CREATOR = new Creator<ToDoItem>() {
        @Override
        public ToDoItem createFromParcel(Parcel in) {
            return new ToDoItem(in);
        }

        @Override
        public ToDoItem[] newArray(int size) {
            return new ToDoItem[size];
        }
    };

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherUID() {
        return publisherUID;
    }

    public void setPublisherUID(String publisherUID) {
        this.publisherUID = publisherUID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCompleteBy() {
        return completeBy;
    }

    public void setCompleteBy(String completeBy) {
        this.completeBy = completeBy;
    }

    public String getKeyRef() {
        return keyRef;
    }

    public void setKeyRef(String keyRef) {
        this.keyRef = keyRef;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(publisherName);
        dest.writeString(publisherUID);
        dest.writeString(subject);
        dest.writeString(text);
        dest.writeString(date);
        dest.writeString(imageURL);
        dest.writeString(color);
        dest.writeString(completeBy);
        dest.writeString(keyRef);
        dest.writeByte((byte) (isComplete ? 1 : 0));
        dest.writeByte((byte) (isImportant ? 1 : 0));
    }
}
