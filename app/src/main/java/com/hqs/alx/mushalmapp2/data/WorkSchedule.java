package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alex on 05/03/2018.
 */

public class WorkSchedule implements Parcelable{

    String StartDate;
    String EndDate;
    String id;
    boolean isPublished;
    boolean isReady;
    ArrayList<Day> schedule;

    public WorkSchedule() {
    }

    public WorkSchedule(String id, String StartDate, String EndDate, boolean isReady, ArrayList<Day> schedule, boolean isPublished) {
        this.id = id;
        this.StartDate = StartDate;
        this.EndDate = EndDate;
        this.isReady = isReady;
        this.schedule = schedule;
        this.isPublished = isPublished;
    }

    protected WorkSchedule(Parcel in) {
        id = in.readString();
        StartDate = in.readString();
        EndDate = in.readString();
        isReady = in.readByte() != 0;
        schedule = in.createTypedArrayList(Day.CREATOR);
        isPublished = in.readByte() != 0;
    }

    public static final Creator<WorkSchedule> CREATOR = new Creator<WorkSchedule>() {
        @Override
        public WorkSchedule createFromParcel(Parcel in) {
            return new WorkSchedule(in);
        }

        @Override
        public WorkSchedule[] newArray(int size) {
            return new WorkSchedule[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String StartDate) {
        this.StartDate = StartDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String EndDate) {
        this.EndDate = EndDate;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public ArrayList<Day> getSchedule() {
        return schedule;
    }

    public void setSchedule(ArrayList<Day> schedule) {
        this.schedule = schedule;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(StartDate);
        dest.writeString(EndDate);
        dest.writeByte((byte) (isReady ? 1 : 0));
        dest.writeTypedList(schedule);
        dest.writeByte((byte) (isPublished ? 1 : 0));
    }
}
