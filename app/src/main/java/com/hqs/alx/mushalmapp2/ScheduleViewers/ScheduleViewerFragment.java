package com.hqs.alx.mushalmapp2.ScheduleViewers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.AutoSuggestedAdapter.EmployeeAutoSuggestedAdapter;
import com.hqs.alx.mushalmapp2.MainActivity;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.Day;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.Shift;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.data.WorkSchedule;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyWorkScheduleRecyclerAdapter;
import com.spark.submitbutton.SubmitButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleViewerFragment extends Fragment {

    private MyScheduleBroadCastReciever myBroadCastReceiver = new MyScheduleBroadCastReciever();
    private IntentFilter intentFilter = new IntentFilter();

    private DatabaseReference scheduleRef;
    public WorkSchedule currentWorkSchedule;
    ArrayList<Day> scheduleDays;
    public ArrayList<EmployeeForShift> allEmployeesFromWorkDB;
    private RecyclerView currentScheduleRecycler;
    MyWorkScheduleRecyclerAdapter currentAdapter;
    HorizontalCalendar horizontalCalendar;
    int numOfEmployees = 0;
    float pixels = 0;

    public static ImageView publishScheduleBtn;

    public ScheduleViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            currentWorkSchedule = savedInstanceState.getParcelable("currentWorkSchedule");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_viewer, container, false);

        scheduleRef = FirebaseDatabase.getInstance().getReference();

        publishScheduleBtn = (ImageView) view.findViewById(R.id.publishScheduleBtn);
        scheduleDays = currentWorkSchedule.getSchedule();
        allEmployeesFromWorkDB = new ArrayList<EmployeeForShift>();
        currentScheduleRecycler = (RecyclerView) view.findViewById(R.id.chosenScheduleViewer);


        ArrayList<Day> allDays = currentWorkSchedule.getSchedule();
        ArrayList<EmployeeForShift> e;
        ArrayList<Shift> dayShifts;
        if(numOfEmployees == 0){
            for (int i = 0; i < allDays.size(); i++) {
                dayShifts = allDays.get(i).getShifts();
                for (int j = 0; j < dayShifts.size(); j++) {
                    e = dayShifts.get(j).getEmployee();
                    if(e.size() > numOfEmployees)
                        numOfEmployees = e.size();
                }
            }
        }

        int newRawHeight = numOfEmployees*37;
        pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newRawHeight, getResources().getDisplayMetrics());

        //only if the user is admin - he'll be able to update shift info
        if(WorkPlaceFragment.currentWorkPlace.isAdmin()){
            publishScheduleBtn.setVisibility(View.VISIBLE);
        }
        if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT){
            startPortrait();
        }else if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
            startLandscape();
        }

        Toast.makeText(getActivity(), "" + currentWorkSchedule.getStartDate(), Toast.LENGTH_SHORT).show();

        return view;
    }

    public int getScreenOrientation() {
        DisplayMetrics display = this.getResources().getDisplayMetrics();

        int width = display.widthPixels;
        int height = display.heightPixels;

        //Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        if(width==height){
            orientation = Configuration.ORIENTATION_SQUARE;
            Log.d("orientation", "square");
        } else{
            if(width < height){
                orientation = Configuration.ORIENTATION_PORTRAIT;
                Log.d("orientation", "portrait");
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
                Log.d("orientation", "landscape");
            }
        }
        return orientation;
    }

    public void startPortrait(){

        //creating an array of users that are assigned to the current work place.
        //getting all the users that the current workPlace has
        //Create an array of String and an array of EmployeeForShift:
        // String array is for autocomplete
        //EmployeeForShift array is for getting the names - later, assign EmployeeForShift for a shift
        scheduleRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(allEmployeesFromWorkDB.size() != 0) {
                            allEmployeesFromWorkDB.clear();
                        }
                        ArrayList<String> userNamesFromDB = new ArrayList<String>();

                        for(DataSnapshot snap: dataSnapshot.getChildren()){
                            EmployeeForShift employee = snap.getValue(EmployeeForShift.class);
                            allEmployeesFromWorkDB.add(employee);
                            userNamesFromDB.add(employee.getName());
                        }
                        currentScheduleRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                        currentAdapter = new MyWorkScheduleRecyclerAdapter(getActivity(), currentWorkSchedule, allEmployeesFromWorkDB, userNamesFromDB, pixels);
                        currentScheduleRecycler.setAdapter(currentAdapter);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


    }

    public void startLandscape(){

        scheduleRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(allEmployeesFromWorkDB.size() != 0) {
                            allEmployeesFromWorkDB.clear();
                        }
                        ArrayList<String> userNamesFromDB = new ArrayList<String>();

                        for(DataSnapshot snap: dataSnapshot.getChildren()){
                            EmployeeForShift employee = snap.getValue(EmployeeForShift.class);
                            allEmployeesFromWorkDB.add(employee);
                            userNamesFromDB.add(employee.getName());
                        }

                        ArrayList<Day> totalDays = currentWorkSchedule.getSchedule();
                        int spanCount = 0;
                        if(totalDays.size() < 7)
                            spanCount = totalDays.size();
                        else
                            spanCount = 7;

                        currentScheduleRecycler.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
                        currentAdapter = new MyWorkScheduleRecyclerAdapter(getActivity(), currentWorkSchedule, allEmployeesFromWorkDB, userNamesFromDB, pixels);
                        currentScheduleRecycler.setAdapter(currentAdapter);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        intentFilter.addAction("com.hqs.alx.mushalmapp2.FOCUSEDDAY");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.UPDATED_SCHEDULE");

        getActivity().registerReceiver(myBroadCastReceiver, intentFilter);

        // Set title bar
        ((MainActivity) getActivity()).setActionBarTitle(WorkPlaceFragment.currentWorkPlace.getWorkName());
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(myBroadCastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentWorkSchedule", currentWorkSchedule);
    }

    class MyScheduleBroadCastReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //action that is recieved when clicking on single item in the schedule list
            if(action.equals("com.hqs.alx.mushalmapp2.FOCUSEDDAY")){
                String focusedDate = intent.getStringExtra("datePosition");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Calendar date = Calendar.getInstance();
                try {
                    date.setTime(dateFormat.parse(focusedDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                horizontalCalendar.centerCalendarToPosition(horizontalCalendar.positionOfDate(date));
            }else if(action.equals("com.hqs.alx.mushalmapp2.UPDATED_SCHEDULE")){

                final String scheduleID = intent.getStringExtra("schedule_ID");

                scheduleRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                        .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_USERS)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(allEmployeesFromWorkDB.size() != 0) {
                                    allEmployeesFromWorkDB.clear();
                                }
                                final ArrayList<String> userNamesFromDB = new ArrayList<String>();

                                for(DataSnapshot snap: dataSnapshot.getChildren()){
                                    EmployeeForShift employee = snap.getValue(EmployeeForShift.class);
                                    allEmployeesFromWorkDB.add(employee);
                                    userNamesFromDB.add(employee.getName());
                                }

                                scheduleRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                                        .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_WORKSCHEDULE).child(scheduleID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                currentWorkSchedule = dataSnapshot.getValue(WorkSchedule.class);

                                                currentAdapter = new MyWorkScheduleRecyclerAdapter(getActivity(), currentWorkSchedule, allEmployeesFromWorkDB, userNamesFromDB, pixels);
                                                currentScheduleRecycler.setAdapter(currentAdapter);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d("DatabaseError: ", "action:'com.hqs.alx.mushalmapp2.UPDATED_SCHEDULE' " + databaseError.getMessage());
                                            }
                                        });


                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });


            }


        }
    }

}
