package com.hqs.alx.mushalmapp2;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.AutoSuggestedAdapter.EmployeeAutoSuggestedAdapter;
import com.hqs.alx.mushalmapp2.data.Day;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.Shift;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.data.WorkSchedule;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyAllPlaceSchedulesRecyclerAdapter;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyRecentShiftsAdapter;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyWorkScheduleRecyclerAdapter;
import com.tt.whorlviewlibrary.WhorlView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkScheduleFragment extends Fragment {

    private MyScheduleBroadCastReciever myReciever = new MyScheduleBroadCastReciever();
    private IntentFilter intentFilter = new IntentFilter();

    public WorkPlaces currentWorkPlace;

    private TextView noSchedulesTV, noUpcomingTV, noLastTv, noSavedTV;

    String user_uid;
    FloatingActionButton addNewScheduleBtn;
    private RecyclerView allWorkSchedulesRecycler, userAllShifts, userUpcomingShifts, completedShifts;
    MyRecentShiftsAdapter userCompleteShiftsAdapter;
    ArrayList<ShiftInfoForUsers> savedShifts;
    PopupWindow newSchedukeCreatorPopUp;
    //Custom Progress Bar
    private WhorlView whorlView;

    Calendar c;
    Date chosenEndDate, chosenStartDate;
    DatePickerDialog datePickerDialogEnd, datePickerDialogStart;

    String workName, workCode;
    int workDays, numOfChildren, numOfShiftsPerDay;
    int numOfColumns = 0;
    ArrayList<WorkSchedule> allFireBaseSchedules;

    ValueEventListener currentWorkEventListener;
    DatabaseReference current_work_reference, current_user_reference;
    FirebaseUser user;

    public WorkScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState != null){
            currentWorkPlace = savedInstanceState.getParcelable("currentWorkPlace");
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_work_schedule, container, false);

        noLastTv = (TextView) view.findViewById(R.id.noLastShiftsTV);
        noSavedTV = (TextView) view.findViewById(R.id.noSavedShiftsTV);
        noSchedulesTV = (TextView) view.findViewById(R.id.noSchedulesTV);
        noUpcomingTV = (TextView) view.findViewById(R.id.noUpcomingShiftsTV);

        addNewScheduleBtn = (FloatingActionButton) view.findViewById(R.id.newScheduleBtn);
        allWorkSchedulesRecycler = (RecyclerView) view.findViewById(R.id.allWorkSchedules);
        userAllShifts = (RecyclerView) view.findViewById(R.id.userAllShifts);
        userUpcomingShifts = (RecyclerView) view.findViewById(R.id.upComingShifts);
        completedShifts = (RecyclerView) view.findViewById(R.id.userSavedShifts);
        allFireBaseSchedules = new ArrayList<WorkSchedule>();

        //Custom progress bar
        whorlView = (WhorlView) view.findViewById(R.id.scheduleWhorlProgress);
        if(whorlView.getVisibility() == View.GONE)
            whorlView.setVisibility(View.VISIBLE);
        whorlView.start();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            //get user UID from firebase
            user_uid = user.getUid();
        }

        if(currentWorkPlace != null){
            workName = currentWorkPlace.getWorkName();
            workCode = currentWorkPlace.getWorkCode();
            if(currentWorkPlace.isAdmin()){
                addNewScheduleBtn.setVisibility(View.VISIBLE);
            }else{
                addNewScheduleBtn.setVisibility(View.GONE);
            }
            addNewScheduleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNewSchedulePopUp();
                }
            });

            //get referance to current user database or create a new one if not exciting
            current_user_reference = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_USERS).child(user_uid);

            current_work_reference = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_WORKPLACES)
                    .child(workCode).child(workName);

            savedShifts = (ArrayList<ShiftInfoForUsers>) ShiftInfoForUsers.listAll(ShiftInfoForUsers.class);
            if(savedShifts.size() == 0)
                noSavedTV.setVisibility(View.VISIBLE);
            else
                noSavedTV.setVisibility(View.GONE);

            //RecyclerView for complete Shifts
            LinearLayoutManager mLayoutManagerComplete = new LinearLayoutManager(getActivity());
            mLayoutManagerComplete.setReverseLayout(false);
            mLayoutManagerComplete.setStackFromEnd(false);
            userCompleteShiftsAdapter = new MyRecentShiftsAdapter(getActivity(), savedShifts);
            completedShifts.setLayoutManager(mLayoutManagerComplete);
            DividerItemDecoration dividerItemDecorationUpcoming = new DividerItemDecoration(completedShifts.getContext(),
                    DividerItemDecoration.VERTICAL);
            completedShifts.addItemDecoration(dividerItemDecorationUpcoming);
            completedShifts.setAdapter(userCompleteShiftsAdapter);

            currentWorkEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(allFireBaseSchedules.size() != 0){
                            allFireBaseSchedules.clear();
                        }
                        for (DataSnapshot scheduleSnapShot: dataSnapshot.getChildren()) {
                            WorkSchedule workSchedule = scheduleSnapShot.getValue(WorkSchedule.class);
                            allFireBaseSchedules.add(workSchedule);
                        }
                        if(numOfColumns == 0){
                            numOfColumns = workDays;
                        }

                        if(allFireBaseSchedules.size() != 0 ){
                            noSchedulesTV.setVisibility(View.GONE);
                            getShiftsFromLast2Schedules(allFireBaseSchedules);
                            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                            mLayoutManager.setReverseLayout(true);
                            mLayoutManager.setStackFromEnd(true);
                            allWorkSchedulesRecycler.setLayoutManager(mLayoutManager);
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(allWorkSchedulesRecycler.getContext(),
                                    DividerItemDecoration.VERTICAL);
                            allWorkSchedulesRecycler.addItemDecoration(dividerItemDecoration);
                            MyAllPlaceSchedulesRecyclerAdapter allPlaceSchedulesRecyclerAdapter = new MyAllPlaceSchedulesRecyclerAdapter(getActivity(),
                                    allFireBaseSchedules);
                            allWorkSchedulesRecycler.setAdapter(allPlaceSchedulesRecyclerAdapter);
                        }else{
                            noSchedulesTV.setVisibility(View.VISIBLE);
                        }
                    }else{
                        whorlView.stop();
                        whorlView.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("database error: ", databaseError.getDetails().toString());
                }
            };

            current_work_reference.child(FireBaseConstants.CHILD_WORKSCHEDULE).addValueEventListener(currentWorkEventListener);

            (view.findViewById(R.id.savedShiftsTV)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.SAVED_SHIFTS");
                    getActivity().sendBroadcast(intent);
                }
            });

        }

        return view;
    }

    private void getShiftsFromLast2Schedules(final ArrayList<WorkSchedule> allFireBaseSchedules){
        final ArrayList<ShiftInfoForUsers> lastThirtyUserShifts = new ArrayList<ShiftInfoForUsers>();
        final ArrayList<ShiftInfoForUsers> upComingShifts = new ArrayList<ShiftInfoForUsers>();

        current_work_reference.child(FireBaseConstants.CHILD_USERS_SHIFTS).child(user_uid).orderByKey().limitToLast(4).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    for (int i = 0; i < snap.getChildrenCount(); i++) {
                        String child = String.valueOf(i);
                        ShiftInfoForUsers shift = snap.child(child).getValue(ShiftInfoForUsers.class);
                        lastThirtyUserShifts.add(shift);
                        if(lastThirtyUserShifts.size() == 30){
                            Log.d("LASTSHIFTS", " LIMIT OF 30 IS REACHED");
                            break;
                        }
                    }
                }

                for (int i = 0; i < lastThirtyUserShifts.size(); i++) {
                    String dateString = lastThirtyUserShifts.get(i).getDate();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar calendarShiftDate = Calendar.getInstance();
                    try {
                        Date currentDate = Calendar.getInstance().getTime();

                        calendarShiftDate.setTime(dateFormat.parse(dateString));
                        Date shiftDate = dateFormat.parse(dateString);
                        if(currentDate.after(shiftDate)){
                            Log.d("CHECKDATE", "Current Date is AFTER shiftDate");
                        }else{
                            upComingShifts.add(lastThirtyUserShifts.get(i));
                            Log.d("CHECKDATE", "Current Date is BEFORE shiftDate");
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                String numOfUpcomingShiftsString = String.valueOf(upComingShifts.size());
                current_user_reference.child(FireBaseConstants.CHILD_USER_WORK_PLACES).child(workName).child(FireBaseConstants.CHILD_UPCOMING_SHIFTS).setValue(numOfUpcomingShiftsString);

                Log.d("upComingShifts", " upComingShifts SIZE: " + upComingShifts.size());

                if(lastThirtyUserShifts.size() ==0)
                    noLastTv.setVisibility(View.VISIBLE);
                else
                    noLastTv.setVisibility(View.GONE);

                if(upComingShifts.size() == 0)
                    noUpcomingTV.setVisibility(View.VISIBLE);
                else
                    noUpcomingTV.setVisibility(View.GONE);

                //sort the upcomoming shifts by date - the closest one will be the first
                Collections.sort(upComingShifts);

                //RecyclerView for upcoming Shifts
                LinearLayoutManager mLayoutManagerUpcoming = new LinearLayoutManager(getActivity());
                mLayoutManagerUpcoming.setReverseLayout(false);
                mLayoutManagerUpcoming.setStackFromEnd(false);
                MyRecentShiftsAdapter userComingShiftsAdapter = new MyRecentShiftsAdapter(getActivity(), upComingShifts);
                userUpcomingShifts.setLayoutManager(mLayoutManagerUpcoming);
                DividerItemDecoration dividerItemDecorationUpcoming = new DividerItemDecoration(userUpcomingShifts.getContext(),
                        DividerItemDecoration.VERTICAL);
                userUpcomingShifts.addItemDecoration(dividerItemDecorationUpcoming);
                userUpcomingShifts.setAdapter(userComingShiftsAdapter);


                //sort the last shifts by date - the last shift will be the first
                Collections.sort(lastThirtyUserShifts);

                //RecyclerView For All Shifts
                LinearLayoutManager mLayoutManagerAllShifts = new LinearLayoutManager(getActivity());
                mLayoutManagerAllShifts.setReverseLayout(true);
                mLayoutManagerAllShifts.setStackFromEnd(true);
                MyRecentShiftsAdapter myRecentShiftsAdapter = new MyRecentShiftsAdapter(getActivity(), lastThirtyUserShifts);
                userAllShifts.setLayoutManager(mLayoutManagerAllShifts);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(userAllShifts.getContext(),
                        DividerItemDecoration.VERTICAL);
                userAllShifts.addItemDecoration(dividerItemDecoration);
                userAllShifts.setAdapter(myRecentShiftsAdapter);

                whorlView.stop();
                whorlView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void createNewSchedulePopUp(){
        View popUpCreatorView = LayoutInflater.from(getActivity()).inflate(R.layout.create_new_schedule, null);

        boolean focusable = true;
        newSchedukeCreatorPopUp = new PopupWindow(popUpCreatorView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, focusable);

        //numOfShiftsPerDay = 0 - in case it was changed somehow
        numOfShiftsPerDay = 0;

        /*
        -------------------- Find all views in popUp ---------------------
         */
        final LinearLayout shiftCreatorContainer = (LinearLayout) popUpCreatorView.findViewById(R.id.shiftCreatorContainer);
        final LoadingButton creatScheduleBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.creatScheduleBtn);

        // imageViews to add or delete shifts
        ImageView addShiftIV = (ImageView) popUpCreatorView.findViewById(R.id.addShiftIV);
        final ImageView deleteShiftIV = (ImageView) popUpCreatorView.findViewById(R.id.deleteShiftIV);

        final TextView startDate = (TextView) popUpCreatorView.findViewById(R.id.startDateTV);
        final TextView endDate = (TextView) popUpCreatorView.findViewById(R.id.endDateTV);
        final EditText firstShiftET = (EditText) popUpCreatorView.findViewById(R.id.firstShiftET);
        final TextView startFirst = (TextView) popUpCreatorView.findViewById(R.id.startFirstTV);
        final TextView endFirst = (TextView) popUpCreatorView.findViewById(R.id.endFirstTV);

        //set underLine for each time textView
        startFirst.setPaintFlags(startFirst.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        endFirst.setPaintFlags(endFirst.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        //set timePicker for each textView
        SetTime firstStart = new SetTime(startFirst, getActivity());
        SetTime firstEnd = new SetTime(endFirst, getActivity());

        //getting the calendar instance
        c = Calendar.getInstance();
        //deciding how would be the Date shown to the user
        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        /*
        -----------------  Date picker for Start and End TextViews
         */
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int myYear = c.get(Calendar.YEAR); // current year
                int myMonth = c.get(Calendar.MONTH); // current month
                int myDay = c.get(Calendar.DAY_OF_MONTH); // current Day

                datePickerDialogStart = new DatePickerDialog(getActivity(), R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        chosenStartDate = newDate.getTime();
                        String dateString = df.format(chosenStartDate);
                        startDate.setTextColor(getResources().getColor(R.color.simple_white));
                        startDate.setText(dateString);
                    }
                }, myYear, myMonth, myDay);
                datePickerDialogStart.show();
            }
        });


        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int myYeare = c.get(Calendar.YEAR); // current year
                int myMonthe = c.get(Calendar.MONTH); // current month
                int myDaye = c.get(Calendar.DAY_OF_MONTH); // current Day

                datePickerDialogEnd = new DatePickerDialog(getActivity(), R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        chosenEndDate = newDate.getTime();
                        String dateString = df.format(chosenEndDate);
                        endDate.setTextColor(getResources().getColor(R.color.simple_white));
                        endDate.setText(dateString);
                    }
                }, myYeare, myMonthe, myDaye);
                datePickerDialogEnd.show();
            }
        });

        addShiftIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Number of child will provide the number of shifts per day
                int numOfShifts = shiftCreatorContainer.getChildCount() + 1;

                //Creating a new View of a single shift information
                View newShiftView = LayoutInflater.from(getActivity()).inflate(R.layout.single_shift_creator, shiftCreatorContainer, false);
                final EditText shiftNameET = (EditText) newShiftView.findViewById(R.id.shiftNameET);
                shiftNameET.setText(getResources().getString(R.string.shift) + " " + numOfShifts + ":");
                final TextView startShiftTV = (TextView) newShiftView.findViewById(R.id.startShiftTV);
                final TextView endShiftTV = (TextView) newShiftView.findViewById(R.id.endShiftTV);

                //Make the textView of time underLined
                startShiftTV.setPaintFlags(startShiftTV.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                endShiftTV.setPaintFlags(endShiftTV.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                //Connecting a timePicker for the textView
                SetTime startShiftTime = new SetTime(startShiftTV, getActivity());
                SetTime endShiftTime = new SetTime(endShiftTV, getActivity());

                // Getting the new shift we want to add and adding it to the container layout
                LinearLayout singleShiftCont = (LinearLayout) newShiftView.findViewById(R.id.singleShiftCont);
                shiftCreatorContainer.addView(singleShiftCont);

                if(deleteShiftIV.getVisibility() == View.GONE)
                    deleteShiftIV.setVisibility(View.VISIBLE);
            }
        });

        if(shiftCreatorContainer.getChildCount() == 1 ){
            deleteShiftIV.setVisibility(View.GONE);
        }

        deleteShiftIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionToDelete = shiftCreatorContainer.getChildCount() - 1 ;
                if(positionToDelete != 0 ){
                    shiftCreatorContainer.removeViewAt(positionToDelete);
                    if(positionToDelete == 1){
                        deleteShiftIV.setVisibility(View.GONE);
                    }
                }

            }
        });


        //numOfChildren is the number of schedules for a single workPlace - The ID of a schedule is (numOfChildren+1)
        numOfChildren = 0;
        current_work_reference.child(FireBaseConstants.CHILD_WORKSCHEDULE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    String key = snap.getKey();
                    int num = Integer.parseInt(key);
                    if(num > numOfChildren){
                        numOfChildren = num;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ListnerCanceled", "" + databaseError.getMessage());
            }
        });

        creatScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatScheduleBtn.startLoading();
                // startDate & endDate Textviews must be different from their initial value - if not, its not possible to continue
                if(startDate.getText().toString().trim().equals(getResources().getString(R.string.startDate)) ||
                        endDate.getText().toString().trim().equals(getResources().getString(R.string.endDate))){
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                    creatScheduleBtn.loadingFailed();
                    creatScheduleBtn.reset();
                    return;
                }else if((int)( (chosenEndDate.getTime() - chosenStartDate.getTime())
                        / (1000 * 60 * 60 * 24) ) + 1 > 7){

                    //TODO: Check if user has payed or not (a boolean value should be added to user constructor)
                    //TODO: make sure to create a seperate parent for payed users in database
                    Toast.makeText(getActivity(), "You are not a premium user - please pay some money!", Toast.LENGTH_SHORT).show();

                } else {
                    int scheduleDays = (int)( (chosenEndDate.getTime() - chosenStartDate.getTime())
                            / (1000 * 60 * 60 * 24) ) + 1;

                    ArrayList<Day> allDays = new ArrayList<Day>();

                    if(allDays.size() != 0){
                        allDays.clear();
                    }

                    Calendar c = Calendar.getInstance();
                    c.setTime(chosenStartDate);
                    Date date = chosenStartDate;
                    //the format of day and date Strings
                    DateFormat dayFormat = new SimpleDateFormat("EEEE");
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    String dayString = dayFormat.format(date);
                    String dateString = dateFormat.format(date);

                    ArrayList<Shift> shiftsPerDayArray;
                    ArrayList<EmployeeForShift> employeeNameForAShift = new ArrayList<EmployeeForShift>();
                    employeeNameForAShift.add(new EmployeeForShift(getResources().getString(R.string.noNameYet), "0", false));

                    for (int i = 0; i < scheduleDays; i++) {

                        shiftsPerDayArray = new ArrayList<Shift>();

                        for (int j = 0; j < shiftCreatorContainer.getChildCount(); j++) {
                            LinearLayout linearLayout = (LinearLayout) shiftCreatorContainer.getChildAt(j);
                            EditText shiftNameET = (EditText) linearLayout.getChildAt(0);
                            TextView startTime = (TextView) linearLayout.getChildAt(1);
                            TextView endTime = (TextView) linearLayout.getChildAt(2);

                            String shiftName = shiftNameET.getText().toString().trim();
                            String shiftStart = startTime.getText().toString();
                            String shiftEnd = endTime.getText().toString();

                            if(shiftName.length() < 1){
                                Toast.makeText(getActivity(), getResources().getString(R.string.shiftNameIsRequired), Toast.LENGTH_SHORT).show();
                                creatScheduleBtn.loadingFailed();
                                creatScheduleBtn.reset();
                                return;
                            }
                            DateFormat sdf = new SimpleDateFormat("hh:mm");
                            try {
                                // To get the date object from the string just called the
                                // parse method and pass the time string to it. This method
                                // throws ParseException if the time string is invalid.
                                // But remember as we don't pass the date information this
                                // date object will represent the 1st of january 1970.
                                Date dStart = sdf.parse(shiftStart);
                                Date dEnd = sdf.parse(shiftEnd);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Shift shift = new Shift(shiftName, dayString, dateString, shiftStart, shiftEnd, employeeNameForAShift);
                            shiftsPerDayArray.add(shift);
                        }

                        allDays.add(new Day(dayString, dateString, shiftsPerDayArray));

                        c.add(Calendar.DATE, 1);
                        date = c.getTime();
                        dayString = dayFormat.format(date);
                        dateString = dateFormat.format(date);
                    }

                    //strings for schedule object
                    String startDate = dateFormat.format(chosenStartDate);
                    String endDate = dateFormat.format(chosenEndDate);
                    String id = String.valueOf(numOfChildren + 1 );

                    WorkSchedule newWorkSchedule = new WorkSchedule(id, startDate, endDate, false, allDays, false);

                    current_work_reference.child(FireBaseConstants.CHILD_WORKSCHEDULE).child(id).setValue(newWorkSchedule);

                    creatScheduleBtn.loadingSuccessful();
                    //wait 1 second before closing the popUp - without this, the user wont show the "succesfful button".
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // Actions to do after 1 second
                            newSchedukeCreatorPopUp.dismiss();
                        }
                    }, 1000);
                }
            }
        });

        (popUpCreatorView.findViewById(R.id.closePopUpCreatorIV)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSchedukeCreatorPopUp.dismiss();
            }
        });

        newSchedukeCreatorPopUp.showAtLocation(getView(), Gravity.CENTER, 0,0);
    }

    class MyScheduleBroadCastReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.hqs.alx.mushalmapp2.SHIFT_SAVED")){
                noSavedTV.setVisibility(View.GONE);
                ShiftInfoForUsers shift = intent.getParcelableExtra("shiftSaved");
                String startTime = shift.getStart();
                String endTime = shift.getEnd();

                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = format.parse(startTime);
                    date2 = format.parse(endTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long duration = (date2.getTime() - date1.getTime()) / (60 * 60 * 1000) % 24;
                if(duration < 0)
                    duration = duration + 24;

                shift.setDuration(duration);
                shift.setDone(true);
                shift.save();

                Toast.makeText(context, context.getResources().getString(R.string.shiftSaved), Toast.LENGTH_SHORT).show();
                savedShifts = (ArrayList<ShiftInfoForUsers>) ShiftInfoForUsers.listAll(ShiftInfoForUsers.class);
                Collections.sort(savedShifts);
                //RecyclerView for complete Shifts
                MyRecentShiftsAdapter userCompleteShiftsAdaptesr = new MyRecentShiftsAdapter(getActivity(), savedShifts);
                completedShifts.setAdapter(userCompleteShiftsAdaptesr);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(currentWorkPlace.getWorkName());
        //autoComplete Adapter
        // = new EmployeeAutoSuggestedAdapter(getActivity(), android.R.layout.simple_list_item_1, WorkPlaceFragment.userNamesFromDB);
        //current_work_reference.child(FireBaseConstants.CHILD_WORKSCHEDULE).addValueEventListener(currentWorkEventListener);
        intentFilter.addAction("com.hqs.alx.mushalmapp2.SHIFT_SAVED");
        getActivity().registerReceiver(myReciever, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myReciever);
        if(currentWorkEventListener != null)
            current_work_reference.child(FireBaseConstants.CHILD_WORKSCHEDULE).removeEventListener(currentWorkEventListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentWorkPlace", currentWorkPlace);
    }

    class SetTime implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

        private TextView textView;
        private Calendar myCalendar;

        public SetTime(TextView textView, Context ctx){
            this.textView = textView;
            this.textView.setOnClickListener(this);
            this.myCalendar = Calendar.getInstance();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            this.textView.setText( hourOfDay + ":" + minute);
        }

        @Override
        public void onClick(View v) {
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(getActivity(), R.style.DatePickerTheme, this, hour, minute, true).show();
        }
    }
}

