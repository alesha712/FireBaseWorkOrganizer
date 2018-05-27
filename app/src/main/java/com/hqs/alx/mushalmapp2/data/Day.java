package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alex on 30/01/2018.
 */

public class Day implements Parcelable{

    String name;
    String date;
    ArrayList<Shift> shifts;

    public Day() {
    }

    public Day(String name, String date, ArrayList<Shift> shifts) {
        this.name = name;
        this.shifts = shifts;
        this.date = date;
    }

    protected Day(Parcel in) {
        name = in.readString();
        date = in.readString();
        shifts = in.createTypedArrayList(Shift.CREATOR);
    }

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(ArrayList<Shift> shifts) {
        this.shifts = shifts;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(date);
        dest.writeTypedList(shifts);
    }
}
