package com.hqs.alx.mushalmapp2.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alex on 30/01/2018.
 */

public class Shift implements Parcelable{

    private String day;
    private String name;
    private String date;
    private String weekDay;
    private String start;
    private String end;
    private ArrayList<EmployeeForShift> employee;

    public Shift(){

    }

    public Shift(String name, String day, String date, String start, String end, ArrayList<EmployeeForShift> employee) {
        this.name = name;
        this.day = day;
        this.date = date;
        this.start = start;
        this.end = end;
        this.employee = employee;
    }

   /* public Shift(String day, String date, String weekDay, int start, int end, ArrayList<String> employeeNameOrUID) {
        this.day = day;
        this.date = date;
        this.weekDay = weekDay;
        this.start = start;
        this.end = end;
        this.employeeNameOrUID = employeeNameOrUID;
    }*/

    public Shift(String name, String start, String end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    protected Shift(Parcel in) {
        day = in.readString();
        name = in.readString();
        date = in.readString();
        weekDay = in.readString();
        start = in.readString();
        end = in.readString();
        employee = in.createTypedArrayList(EmployeeForShift.CREATOR);
    }

    public static final Creator<Shift> CREATOR = new Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel in) {
            return new Shift(in);
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList<EmployeeForShift> getEmployee() {
        return employee;
    }

    public void setEmployee(ArrayList<EmployeeForShift> employee) {
        this.employee = employee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(day);
        dest.writeString(name);
        dest.writeString(date);
        dest.writeString(weekDay);
        dest.writeString(start);
        dest.writeString(end);
        dest.writeTypedList(employee);
    }
}
