package com.hqs.alx.mushalmapp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hqs.alx.mushalmapp2.ScheduleViewers.ScheduleViewerFragment;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyAppConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.data.WorkSchedule;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyRecentShiftsAdapter;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyWorkScheduleRecyclerAdapter;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class WorkPlaceFragment extends Fragment {

    public static WorkPlaces currentWorkPlace;
    public static final ArrayList<String> userNamesFromDB = new ArrayList<String>();
    private ArrayList<EmployeeForShift> allEmployeesFromWorkDB;

    private RecyclerView chatPreviewRecycler, schedulePreviewRecycler;
    private MyChatRecyclerAdapter myChatRecyclerAdapter;
    private LinearLayout workPlaceImage;
    private TextView numOfEmployees, userUpComingShifts, noChatsToShow, noUpcomingShiftsTV;
    String workName;
    String workCode;
    private String user_uid, workPlaceImageURL;
    private int numOfEmployeesINT;

    public MyUser currentUser;

    SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private DatabaseReference workPlaceRef, userRef, workUsersRef;
    private FirebaseUser user;

    public WorkPlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_work_place, container, false);
        workPlaceImage = (LinearLayout) view.findViewById(R.id.topView);

        if(savedInstanceState != null){
            currentWorkPlace = savedInstanceState.getParcelable("currentWorkPlace");
            workPlaceImageURL = savedInstanceState.getString("workPlaceImageURL");
            if(!workPlaceImageURL.equals("") && workPlaceImageURL != null){
                RequestOptions options = new RequestOptions();
                options.centerCrop();
                try{
                    Glide.with(getActivity()).load(workPlaceImageURL).into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            workPlaceImage.setBackground(resource);
                        }
                    });
                }catch (Exception e){
                    Log.d("GlideError", "Couldnt Get Image");
                }
            }else{
                workPlaceImage.setBackground(getResources().getDrawable(R.drawable.work_place_example));
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        numOfEmployees = (TextView) view.findViewById(R.id.numOfEmployees);
        userUpComingShifts = (TextView) view.findViewById(R.id.numUpComingShiftsPreview);
        chatPreviewRecycler = (RecyclerView) view.findViewById(R.id.chatPreviewRV);
        schedulePreviewRecycler = (RecyclerView) view.findViewById(R.id.schedulePreviewRV);
        noChatsToShow = (TextView) view.findViewById(R.id.noChatMessageTV);
        noUpcomingShiftsTV = (TextView) view.findViewById(R.id.noUpcomingShiftTV);


        numOfEmployees.setText(preferences.getString("numOfEmployees", "0"));
        userUpComingShifts.setText(preferences.getString("userUpComingShifts", "0"));

        workUsersRef = FirebaseDatabase.getInstance().getReference();
        workPlaceRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference();
        allEmployeesFromWorkDB = new ArrayList<EmployeeForShift>();

        if(currentWorkPlace != null){
            workName = currentWorkPlace.getWorkName();
            workCode = currentWorkPlace.getWorkCode();
            user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_WORKPLACES).child(currentWorkPlace.getWorkCode())
                    .child(currentWorkPlace.getWorkName()).child(FireBaseConstants.WORK_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        workPlaceImageURL = dataSnapshot.getValue(String.class);
                        RequestOptions options = new RequestOptions();
                        options.centerCrop();
                        try{
                            Glide.with(getActivity()).load(workPlaceImageURL).apply(options).into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    workPlaceImage.setBackground(resource);
                                }
                            });
                        }catch (Exception e){
                            Log.d("GlideError", "Couldnt Get Image");
                        }
                    }else{
                        workPlaceImage.setBackground(getResources().getDrawable(R.drawable.work_place_example));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(user != null){
                user_uid = user.getUid();
                //Get full info of currentUser
                userRef.child(FireBaseConstants.ALL_APP_USERS).child(user_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(MyUser.class);
                        //get all upComing Shifts
                        getUpcomingShifts();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            if(userNamesFromDB.size() != 0){
                userNamesFromDB.clear();
            }
            //get all the user at current work place
            workPlaceRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(workCode).child(workName).child(FireBaseConstants.CHILD_USERS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot snap: dataSnapshot.getChildren()){
                                EmployeeForShift employee = snap.getValue(EmployeeForShift.class);
                                allEmployeesFromWorkDB.add(employee);
                                userNamesFromDB.add(employee.getName());
                            }

                            numOfEmployeesINT = allEmployeesFromWorkDB.size();
                            String numOfEmployeesString = String.valueOf(numOfEmployeesINT);
                            numOfEmployees.setText(numOfEmployeesString);
                            editor.putString("numOfEmployees", numOfEmployeesString);
                            editor.apply();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setStackFromEnd(true);
            chatPreviewRecycler.setLayoutManager(linearLayoutManager);
            //get last 6 messages to show in preview chat
            workPlaceRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(workCode).child(workName).child(FireBaseConstants.CHILD_MESSAGES).limitToLast(6)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<ChatMessage> allMessages = new ArrayList<ChatMessage>();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                ChatMessage message = snapshot.getValue(ChatMessage.class);
                                Log.d("vvv","vvv");
                                allMessages.add(message);
                            }

                            if(allMessages.size()==0){
                                noChatsToShow.setVisibility(View.VISIBLE);
                            }else{
                                if(noChatsToShow.getVisibility()==View.VISIBLE){
                                    noChatsToShow.setVisibility(View.GONE);
                                }
                                myChatRecyclerAdapter = new MyChatRecyclerAdapter(allMessages, getActivity(), currentUser, MyAppConstants.CHAT_PREVIEW_TYPE);
                                chatPreviewRecycler.setAdapter(myChatRecyclerAdapter);
                                chatPreviewRecycler.scrollToPosition(allMessages.size()-1);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            (view.findViewById(R.id.goToChatCardView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.GO_TO_CHAT");
                    intent.putExtra("work_place_to_chat", currentWorkPlace);
                    intent.putExtra("currentUser", currentUser);
                    intent.putExtra("workUsersArray", allEmployeesFromWorkDB);
                    getActivity().sendBroadcast(intent);
                }
            });

            (view.findViewById(R.id.goToScheduleCardView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.GO_TO_SCHEDULE");
                    intent.putExtra("work_place_to_schedule", currentWorkPlace);
                    getActivity().sendBroadcast(intent);
                }
            });

            (view.findViewById(R.id.goToToDoListCardView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.GO_TO_TODOLIST");
                    intent.putExtra("work_place_to_schedule", currentWorkPlace);
                    intent.putExtra("currentUser", currentUser);
                    getActivity().sendBroadcast(intent);
                }
            });


            (view.findViewById(R.id.allWorkEmployees)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                    fragmentChanger.changeToemployeeList();
                }
            });
        }

        return view;
    }

    private void getUpcomingShifts(){
        final ArrayList<ShiftInfoForUsers> lastThirtyUserShifts = new ArrayList<ShiftInfoForUsers>();
        final ArrayList<ShiftInfoForUsers> upComingShifts = new ArrayList<ShiftInfoForUsers>();

        workPlaceRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(workCode).child(workName).child(FireBaseConstants.CHILD_USERS_SHIFTS)
                .child(user_uid).orderByKey().limitToLast(4).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    if(upComingShifts.size()==0){
                        noUpcomingShiftsTV.setVisibility(View.VISIBLE);
                        userUpComingShifts.setText("0");
                        editor.putString("userUpComingShifts", "0");
                        editor.apply();
                    }else{
                        if(noUpcomingShiftsTV.getVisibility()==View.VISIBLE){
                            noUpcomingShiftsTV.setVisibility(View.GONE);
                        }

                        String numOfUpcomingShiftsString = String.valueOf(upComingShifts.size());
                        userUpComingShifts.setText(numOfUpcomingShiftsString);
                        Log.d("userUpComingShiftsNum", numOfUpcomingShiftsString );
                        editor.putString("userUpComingShifts", numOfUpcomingShiftsString);
                        editor.apply();

                        Log.d("upComingShifts", " upComingShifts SIZE: " + upComingShifts.size());

                        //Sorting the list by date
                        Collections.sort(upComingShifts);

                        //RecyclerView for upcoming Shifts
                        LinearLayoutManager mLayoutManagerUpcoming = new LinearLayoutManager(getActivity());
                        mLayoutManagerUpcoming.setReverseLayout(false);
                        mLayoutManagerUpcoming.setStackFromEnd(false);
                        MyRecentShiftsAdapter userComingShiftsAdapter = new MyRecentShiftsAdapter(getActivity(), upComingShifts);
                        schedulePreviewRecycler.setLayoutManager(mLayoutManagerUpcoming);
                        DividerItemDecoration dividerItemDecorationUpcoming = new DividerItemDecoration(schedulePreviewRecycler.getContext(),
                                DividerItemDecoration.VERTICAL);
                        schedulePreviewRecycler.addItemDecoration(dividerItemDecorationUpcoming);
                        schedulePreviewRecycler.setAdapter(userComingShiftsAdapter);
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title bar
        ((MainActivity) getActivity()).setActionBarTitle(currentWorkPlace.getWorkName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        boolean isFragWelcomeVisible = true;
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentWorkPlace", currentWorkPlace);
        if(workPlaceImageURL == null){
            workPlaceImageURL = "";
        }
        outState.putString("workPlaceImageURL", workPlaceImageURL);
    }
}
