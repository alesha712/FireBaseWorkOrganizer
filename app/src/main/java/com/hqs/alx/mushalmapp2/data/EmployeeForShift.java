package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alex on 08/04/2018.
 */

public class EmployeeForShift implements Parcelable{

    String name;
    String uid;
    boolean isOnline;

    public EmployeeForShift(){
    }

    public EmployeeForShift(String name, String uid, boolean isOnline) {
        this.name = name;
        this.uid = uid;
        this.isOnline = isOnline;
    }

    protected EmployeeForShift(Parcel in) {
        name = in.readString();
        uid = in.readString();
        isOnline = in.readByte() != 0;
    }

    public static final Creator<EmployeeForShift> CREATOR = new Creator<EmployeeForShift>() {
        @Override
        public EmployeeForShift createFromParcel(Parcel in) {
            return new EmployeeForShift(in);
        }

        @Override
        public EmployeeForShift[] newArray(int size) {
            return new EmployeeForShift[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uid);
        dest.writeByte((byte) (isOnline ? 1 : 0));
    }
}
