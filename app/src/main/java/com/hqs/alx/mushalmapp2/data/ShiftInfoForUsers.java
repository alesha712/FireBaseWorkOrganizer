package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Alex on 15/04/2018.
 */

public class ShiftInfoForUsers extends SugarRecord implements Comparable<ShiftInfoForUsers>, Parcelable {

    private String date;
    private String day;
    private String start;
    private String end;
    private boolean done;
    private String notice;
    private ArrayList<EmployeeForShift> employees;
    private String startSchedule;
    private String endSchedule;
    private double duration;

    public ShiftInfoForUsers(){}

    public ShiftInfoForUsers(String date, String day, String start, String end, boolean done,
                             String notice, ArrayList<EmployeeForShift> employees, String startSchedule, String endSchedule, double duration) {
        this.date = date;
        this.day = day;
        this.start = start;
        this.end = end;
        this.done = done;
        this.notice = notice;
        this.employees = employees;
        this.startSchedule = startSchedule;
        this.endSchedule = endSchedule;
        this.duration = duration;
    }

    protected ShiftInfoForUsers(Parcel in) {
        date = in.readString();
        day = in.readString();
        start = in.readString();
        end = in.readString();
        done = in.readByte() != 0;
        notice = in.readString();
        employees = in.createTypedArrayList(EmployeeForShift.CREATOR);
        startSchedule = in.readString();
        endSchedule = in.readString();
        duration = in.readDouble();
    }

    public static final Creator<ShiftInfoForUsers> CREATOR = new Creator<ShiftInfoForUsers>() {
        @Override
        public ShiftInfoForUsers createFromParcel(Parcel in) {
            return new ShiftInfoForUsers(in);
        }

        @Override
        public ShiftInfoForUsers[] newArray(int size) {
            return new ShiftInfoForUsers[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public ArrayList<EmployeeForShift> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<EmployeeForShift> employees) {
        this.employees = employees;
    }

    public String getStartSchedule() {
        return startSchedule;
    }

    public void setStartSchedule(String startSchedule) {
        this.startSchedule = startSchedule;
    }

    public String getEndSchedule() {
        return endSchedule;
    }

    public void setEndSchedule(String endSchedule) {
        this.endSchedule = endSchedule;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(@NonNull ShiftInfoForUsers o) {

        Date date01 = null;
        Date date02 = null;
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            date01 = format.parse(getDate());
            date02 = format.parse(o.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date01 == null || date02 == null)
            return 0;

        return date01.compareTo(date02);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(day);
        dest.writeString(start);
        dest.writeString(end);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeString(notice);
        dest.writeTypedList(employees);
        dest.writeString(startSchedule);
        dest.writeString(endSchedule);
        dest.writeDouble(duration);
    }
}
