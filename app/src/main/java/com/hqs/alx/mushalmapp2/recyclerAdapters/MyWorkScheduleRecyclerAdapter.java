package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.AutoSuggestedAdapter.EmployeeAutoSuggestedAdapter;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.ScheduleViewers.ScheduleViewerFragment;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.WorkScheduleFragment;
import com.hqs.alx.mushalmapp2.data.Day;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.Shift;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.data.WorkSchedule;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alex on 30/01/2018.
 */

/*
       ------------------- This is the recycler for presenting the schedule days----------------------
       ------------------- Each day will show its shifts and each shift will show the employee names-----
 */

public class MyWorkScheduleRecyclerAdapter extends RecyclerView.Adapter<MyWorkScheduleRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Day> allDays;
    private ArrayList<EmployeeForShift> allEmployeesFromWorkDB;
    private ArrayList<String> userNamesFromDB;
    private EmployeeAutoSuggestedAdapter autoCompleteAdapter;
    private View v;
    private WorkSchedule workSchedule;
    private WorkPlaces w;
    private DatabaseReference databaseReference;
    //A string message for the toast in the end - whether "Updated successfully" or " published successfuly
    private String message ="";
    int numOfEmployees = 0;
    private int rawHeight = 0;
    private float pixels;


    public MyWorkScheduleRecyclerAdapter (Context c, WorkSchedule workSchedule, ArrayList<EmployeeForShift> allEmployeesFromWorkDB, ArrayList<String> userNamesFromDB, float pixels){
        this.context = c;
        this.workSchedule = workSchedule;
        this.allEmployeesFromWorkDB = allEmployeesFromWorkDB;
        this.userNamesFromDB = userNamesFromDB;
        this.pixels = pixels;
    }

    @Override
    public MyWorkScheduleRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        v = parent;
        w = WorkPlaceFragment.currentWorkPlace;

        allDays = workSchedule.getSchedule();


        View singleDayView = LayoutInflater.from(context).inflate(R.layout.single_day_schedule, parent, false);
        MyViewHolder singleDay = new MyViewHolder(singleDayView);
        return singleDay;
    }

    @Override
    public void onBindViewHolder(MyWorkScheduleRecyclerAdapter.MyViewHolder holder, int position) {

        Day currentDay = allDays.get(position);
        holder.bindMyData(currentDay, position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return workSchedule.getSchedule().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View itemView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindMyData(final Day d, final int dayPosition){

            autoCompleteAdapter = new EmployeeAutoSuggestedAdapter(context, android.R.layout.simple_list_item_1, userNamesFromDB);

            TextView dayHeaderTV = (TextView) itemView.findViewById(R.id.dayTV);
            final ArrayList<Shift> dayShifts = d.getShifts();

            String dayName = d.getName();
            String dayDate = d.getDate();

            if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT){
                StringBuilder bldr = new StringBuilder(dayName);
                bldr.append(" ");
                bldr.append(dayDate);
                String textToShow = bldr.toString();
                dayHeaderTV.setText(textToShow);
            }else if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                StringBuilder dateBldr = new StringBuilder(dayDate);
                dateBldr.delete(5,10);
                String date = dateBldr.toString();

                StringBuilder nameBldr = new StringBuilder(dayName);
                nameBldr.delete(3,dayName.length());
                String name = nameBldr.toString();

                StringBuilder bldr = new StringBuilder(name);
                bldr.append("\n");
                bldr.append(date);
                String textToShow = bldr.toString();

                dayHeaderTV.setText(textToShow);
            }

            TableLayout tableLayout = new TableLayout(context);
            tableLayout = (TableLayout) itemView.findViewById(R.id.shiftsTable);
            tableLayout.setWeightSum(dayShifts.size());
            ArrayList<EmployeeForShift> ShiftEmployees;

            if(tableLayout.getChildCount() == 0){
                for(int i = 0; i < dayShifts.size() ; i++) {
                    final Shift shiftToPopUp = dayShifts.get(i);
                    final int shiftPosition = i;

                    View newShift = LayoutInflater.from(context).inflate(R.layout.single_row, tableLayout, false);
                    final TextView shiftHours = (TextView) newShift.findViewById(R.id.shiftNameTV);
                    String shiftHoursString = dayShifts.get(i).getStart() + " - " + dayShifts.get(i).getEnd();
                    shiftHours.setText(shiftHoursString);
                    LinearLayout singleShiftLayout = (LinearLayout) newShift.findViewById(R.id.singleShiftLayout);

                    TextView employeeNamesTV = (TextView) newShift.findViewById(R.id.ShiftEmployeeNameTV);
                    ShiftEmployees = dayShifts.get(i).getEmployee();

                    employeeNamesTV.setText("");

                    for (int j = 0; j < ShiftEmployees.size(); j++) {
                        String name = ShiftEmployees.get(j).getName();
                        String shortName = name;
                        if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                            if(name.length() >= 12){
                                StringBuilder nameBldr = new StringBuilder(name);
                                nameBldr.delete(12,name.length());
                                shortName = nameBldr.toString();
                            }
                        }
                        employeeNamesTV.append(shortName + "\n\n");
                    }
                    employeeNamesTV.setTag(i);
                    if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE)
                        employeeNamesTV.setHeight((int)pixels);

                    tableLayout.addView(singleShiftLayout);

                    //only if the user is admin - he'll be able to update shift info
                    if(w.isAdmin()){
                        if(!workSchedule.isPublished()){
                            ScheduleViewerFragment.publishScheduleBtn.setImageResource(R.drawable.ic_publish_black_24dp);
                        }else{
                            ScheduleViewerFragment.publishScheduleBtn.setImageResource(R.drawable.ic_check_black_24dp);
                        }
                        ScheduleViewerFragment.publishScheduleBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkIfReadyToPublish();
                            }
                        });

                        //adapter for auto complete - the List is all the names from the current work place
                        //AutoCompleteAdapter = new EmployeeAutoSuggestedAdapter(context, android.R.layout.simple_list_item_1, WorkPlaceFragment.userNamesFromDB);

                        newShift.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //start the popup with shift information and the ability to update it if the user is admin
                                popUpShiftInfo(shiftToPopUp, dayPosition , shiftPosition, autoCompleteAdapter);
                            }
                        });
                    }else{
                        ScheduleViewerFragment.publishScheduleBtn.setVisibility(View.GONE);
                    }
                }
            }
        }

        private void checkIfReadyToPublish(){

            AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);

            alertDialogBuider.setTitle(context.getResources().getString(R.string.areYouSure))
                    .setMessage(context.getResources().getString(R.string.areYouSureToPublish))
                    .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            publishSchedule();
                        }
                    })
                    .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuider.create();
            alertDialog.show();

        }

        private void publishSchedule(){

            final WorkPlaces w = WorkPlaceFragment.currentWorkPlace;
            final ArrayList<Shift> currentScheduleShifts = new ArrayList<Shift>();
            if(!workSchedule.isReady()){
                workSchedule.setReady(true);
                message = context.getResources().getString(R.string.schedulePublishedSuccess);
            }else{
                message = context.getResources().getString(R.string.scheduleUpdatedSuccess);
            }

            //getting the shift assigned for each user and updating it in DB
            databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName()).child(FireBaseConstants.CHILD_WORKSCHEDULE)
                    .child(workSchedule.getId()).child("schedule")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //creating an array of shifts from the current schedule
                            for(DataSnapshot snap: dataSnapshot.getChildren()){
                                Day day = snap.getValue(Day.class);
                                if (day != null) {
                                    currentScheduleShifts.addAll(day.getShifts());
                                }
                            }

                            String startSchedule = workSchedule.getStartDate().replace("/", "-");
                            String endSchedule = workSchedule.getEndDate().replace("/", "-");
                            String schedulePeriod = startSchedule + "--" + endSchedule;

                            //checking if a single user (from saved users in the workPlace) has his UID assigned to a single shift
                            //if does, it will add this shift to an array of shifts to be saved in DB
                            for (int i = 0; i < allEmployeesFromWorkDB.size(); i++) {
                                ArrayList<ShiftInfoForUsers> userShifts = new ArrayList<ShiftInfoForUsers>();
                                for (int j = 0; j < currentScheduleShifts.size(); j++) {
                                    for (int k = 0; k < currentScheduleShifts.get(j).getEmployee().size(); k++) {
                                        EmployeeForShift e =currentScheduleShifts.get(j).getEmployee().get(k);
                                        if(e.getUid().equals(allEmployeesFromWorkDB.get(i).getUid())){
                                            String fireBaseDateString = currentScheduleShifts.get(j).getDate().replace("/", "-");


                                            ShiftInfoForUsers shiftInfoForUsers = new ShiftInfoForUsers(fireBaseDateString,
                                                    currentScheduleShifts.get(j).getDay(),
                                                    currentScheduleShifts.get(j).getStart(),
                                                    currentScheduleShifts.get(j).getEnd(),
                                                    false, null, currentScheduleShifts.get(j).getEmployee(), startSchedule, endSchedule, 0);
                                            userShifts.add(shiftInfoForUsers);
                                        }
                                    }
                                }

                                databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName())
                                        .child(FireBaseConstants.CHILD_USERS_SHIFTS).child(allEmployeesFromWorkDB.get(i).getUid())
                                        .child(schedulePeriod).setValue(userShifts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d("ShiftPerUser", "Pushing Shift per user was successful");
                                        }else{
                                            Log.e("ShiftPerUser", "Error submiting shift for a single user");
                                        }
                                    }
                                });
                            }
                            Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(context, context.getResources().getString(R.string.schedulePublishError), Toast.LENGTH_SHORT).show();
                        }
                    });


            databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName()).child(FireBaseConstants.CHILD_WORKSCHEDULE)
                    .child(workSchedule.getId()).child(FireBaseConstants.CHILD_READY).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ScheduleViewerFragment.publishScheduleBtn.setImageResource(R.drawable.ic_check_black_24dp);
                        workSchedule.setPublished(true);
                    }
                }
            });
            databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName()).child(FireBaseConstants.CHILD_WORKSCHEDULE)
                    .child(workSchedule.getId()).child(FireBaseConstants.CHILD_PUBLISHED).setValue(true);


        }

        public void popUpShiftInfo(final Shift shift, final int dayPosition, final int shiftPosition, final EmployeeAutoSuggestedAdapter autoCompleteAdapter){

            View popUpCreatorView = LayoutInflater.from(context).inflate(R.layout.shift_editor, null);
            boolean focusable = true;
            final PopupWindow shiftEditorCreatorPopUp = new PopupWindow(popUpCreatorView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, focusable);

            final EditText shiftNameET = (EditText)  popUpCreatorView.findViewById(R.id.EditorShiftNameET);
            final TextView shiftStartTV = (TextView) popUpCreatorView.findViewById(R.id.EditorStartShiftTV);
            final TextView shiftEndTV = (TextView) popUpCreatorView.findViewById(R.id.EditorEndShiftTV);
            final ImageView deleteEmployeeIV  = (ImageView) popUpCreatorView.findViewById(R.id.editorDeleteEmployee);

            shiftNameET.setText(shift.getName());
            shiftStartTV.setText(shift.getStart());
            shiftEndTV.setText(shift.getEnd());

            //set underLine for each time textView
            shiftStartTV.setPaintFlags(shiftStartTV.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            shiftEndTV.setPaintFlags(shiftEndTV.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

            //set timePicker for each textView
            SetTime firstStart = new SetTime(shiftStartTV, context);
            SetTime firstEnd = new SetTime(shiftEndTV, context);

            final WorkPlaces w = WorkPlaceFragment.currentWorkPlace;

            ArrayList<EmployeeForShift> employeeNames = shift.getEmployee();
            final ArrayList<EmployeeForShift> updatedEmployeeNames = new ArrayList<EmployeeForShift>();

            TableLayout tableLayout = new TableLayout(context);
            tableLayout = (TableLayout) popUpCreatorView.findViewById(R.id.EditorEmployeeTable);
            tableLayout.setWeightSum(employeeNames.size());

            //Creating new layout for each employee
            if(tableLayout.getChildCount() == 0){
                for (int i = 0; i < employeeNames.size(); i++) {
                    View singleEmployeeView = LayoutInflater.from(context).inflate(R.layout.shift_editor_single_employee, tableLayout, false);

                    final RelativeLayout singleEmployeeLayout = (RelativeLayout) singleEmployeeView.findViewById(R.id.EditorSingleEmployeeLayout);
                    AutoCompleteTextView FirstEmployeeNameET = (AutoCompleteTextView) singleEmployeeView.findViewById(R.id.EditorEmployeeNameET);
                    FirstEmployeeNameET.setText(employeeNames.get(i).getName());

                    //autoCompleteAdapter to use in scheduleViewer - it is static because of issues in showing the list of names
                    //autoCompleteAdapter = new EmployeeAutoSuggestedAdapter(context, android.R.layout.simple_list_item_1, WorkPlaceFragment.userNamesFromDB);
                    //adapter for auto complete - the List is all the names from the current work place
                    FirstEmployeeNameET.setAdapter(autoCompleteAdapter);

                    tableLayout.addView(singleEmployeeLayout);
                }
            }

            final TableLayout finalTableLayout = tableLayout;

            popUpCreatorView.findViewById(R.id.editorSaveShiftBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    ArrayList<String> newEmployeeNames = new ArrayList<String>();
                    String newShiftName = shiftNameET.getText().toString().trim();
                    String newStartTime = shiftStartTV.getText().toString();
                    String newEndTime = shiftEndTV.getText().toString();

                    if(workSchedule.isPublished()){
                        workSchedule.setPublished(false);
                        databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName()).child(FireBaseConstants.CHILD_WORKSCHEDULE)
                                .child(workSchedule.getId()).child(FireBaseConstants.CHILD_PUBLISHED).setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    ScheduleViewerFragment.publishScheduleBtn.setImageResource(R.drawable.ic_publish_black_24dp);
                                }
                            }
                        });
                    }

                    for (int i = 0; i < finalTableLayout.getChildCount() ; i++) {
                        RelativeLayout relativeLayout = (RelativeLayout) finalTableLayout.getChildAt(i);
                        AutoCompleteTextView nameET = (AutoCompleteTextView) relativeLayout.getChildAt(0);

                        String name = nameET.getText().toString().trim();
                        newEmployeeNames.add(name);
                    }

                    for (int i = 0; i < newEmployeeNames.size(); i++) {
                        boolean exists = false;
                        for (int j = 0; j < allEmployeesFromWorkDB.size(); j++) {
                            if(newEmployeeNames.get(i).equals(allEmployeesFromWorkDB.get(j).getName())){
                                updatedEmployeeNames.add(allEmployeesFromWorkDB.get(j));
                                exists = true;
                            }
                        }
                        if(!exists){
                            EmployeeForShift employeeNotInDb = new EmployeeForShift(newEmployeeNames.get(i), "0", false);
                            updatedEmployeeNames.add(employeeNotInDb);
                        }
                    }

                    Shift updatedShift = new Shift(newShiftName, shift.getDay(), shift.getDate(), newStartTime, newEndTime, updatedEmployeeNames);

                    String dayPositionString = String.valueOf(dayPosition);
                    String shiftPositionString = String.valueOf(shiftPosition);

                    //Updating the shift saved in DB with the new shift
                    databaseReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(w.getWorkCode()).child(w.getWorkName())
                                        .child(FireBaseConstants.CHILD_WORKSCHEDULE).child(workSchedule.getId()).child("schedule").child(dayPositionString)
                                        .child("shifts").child(shiftPositionString).setValue(updatedShift)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(context, context.getResources().getString(R.string.shiftUpdated), Toast.LENGTH_SHORT).show();
                                shiftEditorCreatorPopUp.dismiss();
                                Intent intent = new Intent("com.hqs.alx.mushalmapp2.UPDATED_SCHEDULE");
                                intent.putExtra("schedule_ID", workSchedule.getId());
                                context.sendBroadcast(intent);
                            }else{
                                Toast.makeText(context, context.getResources().getString(R.string.errorUpdatingShift), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            popUpCreatorView.findViewById(R.id.editorCancelBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shiftEditorCreatorPopUp.dismiss();
                }
            });

            //Adding another employee to the shift
            popUpCreatorView.findViewById(R.id.editorAddEmployee).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View addEmployeeView = LayoutInflater.from(context).inflate(R.layout.shift_editor_single_employee, finalTableLayout, false);
                    RelativeLayout singleEmployeeLayout = (RelativeLayout) addEmployeeView.findViewById(R.id.EditorSingleEmployeeLayout);
                    AutoCompleteTextView newEmployeeNameET = (AutoCompleteTextView) addEmployeeView.findViewById(R.id.EditorEmployeeNameET);
                    newEmployeeNameET.setText(context.getResources().getString(R.string.newEmployee));

                    newEmployeeNameET.setAdapter(autoCompleteAdapter);
                    finalTableLayout.addView(singleEmployeeLayout);

                    if(deleteEmployeeIV.getVisibility() == View.GONE)
                        deleteEmployeeIV.setVisibility(View.VISIBLE);
                }
            });

            if(finalTableLayout.getChildCount() == 1 ){
                deleteEmployeeIV.setVisibility(View.GONE);
            }

            //deleting the last employee from the shift
            deleteEmployeeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positionToDelete = finalTableLayout.getChildCount() - 1;
                    if(positionToDelete != 0){
                        finalTableLayout.removeViewAt(positionToDelete);
                        if(positionToDelete == 1 ){
                            deleteEmployeeIV.setVisibility(View.GONE);
                        }
                    }
                }
            });


            shiftEditorCreatorPopUp.showAtLocation(v, Gravity.CENTER, 0,0);
        }
    }

    public int getScreenOrientation() {
        DisplayMetrics display = context.getResources().getDisplayMetrics();

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
            new TimePickerDialog(context, R.style.DatePickerTheme, this, hour, minute, true).show();
        }
    }
}
