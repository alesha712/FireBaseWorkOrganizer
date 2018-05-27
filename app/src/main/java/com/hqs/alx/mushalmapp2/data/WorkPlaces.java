package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alex on 08/02/2018.
 */

public class WorkPlaces implements Parcelable {

    String workCode;
    String workName;
    String password;
    boolean isAdmin;


    public WorkPlaces() {
    }

    public WorkPlaces(String workCode, String workName, String password) {
        this.workCode = workCode;
        this.workName = workName;
        this.password = password;
    }

    public WorkPlaces(String workCode, String workName, boolean isAdmin) {
        this.workCode = workCode;
        this.workName = workName;
        this.isAdmin = isAdmin;
    }

    protected WorkPlaces(Parcel in) {
        workCode = in.readString();
        workName = in.readString();
        password = in.readString();
        isAdmin = in.readByte() != 0;
    }

    public static final Creator<WorkPlaces> CREATOR = new Creator<WorkPlaces>() {
        @Override
        public WorkPlaces createFromParcel(Parcel in) {
            return new WorkPlaces(in);
        }

        @Override
        public WorkPlaces[] newArray(int size) {
            return new WorkPlaces[size];
        }
    };

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(workCode);
        dest.writeString(workName);
        dest.writeString(password);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
    }
}
