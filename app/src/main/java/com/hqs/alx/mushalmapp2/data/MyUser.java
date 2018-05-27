package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alex on 26/01/2018.
 */

public class MyUser implements Parcelable{

    String firebaseUID;
    String fullName;
    String phone;
    String email;
    String dateOfBirth;
    String password;
    String workName;
    ArrayList<WorkPlaces> userWorkPlaces;
    String profileImageString;

    public MyUser() {
    }

    public MyUser(String firebaseUID, String fullName, String phone, String email, String dateOfBirth, String password, ArrayList<WorkPlaces> userWorkPlaces, String profileImageString) {
        this.firebaseUID = firebaseUID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.userWorkPlaces = userWorkPlaces;
        this.profileImageString = profileImageString;
    }

    protected MyUser(Parcel in) {
        firebaseUID = in.readString();
        fullName = in.readString();
        phone = in.readString();
        email = in.readString();
        dateOfBirth = in.readString();
        password = in.readString();
        workName = in.readString();
        userWorkPlaces = in.createTypedArrayList(WorkPlaces.CREATOR);
        profileImageString = in.readString();

    }

    public static final Creator<MyUser> CREATOR = new Creator<MyUser>() {
        @Override
        public MyUser createFromParcel(Parcel in) {
            return new MyUser(in);
        }

        @Override
        public MyUser[] newArray(int size) {
            return new MyUser[size];
        }
    };

    public ArrayList<WorkPlaces> getUserWorkPlaces() {
        return userWorkPlaces;
    }

    public void setUserWorkPlaces(ArrayList<WorkPlaces> userWorkPlaces) {
        this.userWorkPlaces = userWorkPlaces;
    }

    public String getFirebaseUID() {
        return firebaseUID;
    }

    public void setFirebaseUID(String firebaseUID) {
        this.firebaseUID = firebaseUID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getProfileImageString() {
        return profileImageString;
    }

    public void setProfileImageString(String profileImageString) {
        this.profileImageString = profileImageString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firebaseUID);
        dest.writeString(fullName);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(dateOfBirth);
        dest.writeString(password);
        dest.writeString(workName);
        dest.writeTypedList(userWorkPlaces);
        dest.writeString(profileImageString);
    }
}
