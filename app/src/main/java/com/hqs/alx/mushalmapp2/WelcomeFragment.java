package com.hqs.alx.mushalmapp2;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyAppConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyWorkPlaceRecyclerView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tt.whorlviewlibrary.WhorlView;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    public static MyUser myUser;

    private MyBroadCastReceiver myBroadCastReceiver = new MyBroadCastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    //<editor-fold desc="Declaring Veriables">
    private final int REQUEST_CODE_USER_IMAGE = 1257;
    private final int REQUEST_CODE_WORK_IMAGE = 7485;

    private WorkPlaces chosenWorkPlace;

    private AlertDialog alertDialogNewWorkPlace , alertDialogJoin;
    private DatabaseReference current_user_db, work_place_db, user_single_work_place_db, user_work_places_ref;
    private FirebaseUser myFirebaseUser;
    private FirebaseAuth myFirebaseAuth;
    private ValueEventListener userWorkPlacesListener;
    StorageReference myUserStorageReference, chosenWorkStorageRef;

    SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private TextView userNameHeader, userTotalWorkPlacesTV, userTotalFriendsTV, userUpComingShiftsTV, noWorkPlacesTV;
    private ProgressBar imageProgressBar, workImageProgressBar;
    //Custom Progress Bar
    private WhorlView whorlView;

    private boolean isSuccededJoin;
    private int generatedWorkCode;
    private String generatedWorkCodeString;
    String userHeaderNameString, userNumOfWorkPlacesString, userNumOfTotalFriends;
    private  String userUpComingShiftsString;
    private long totalFriends;
    private ArrayList <WorkPlaces> allUserWorkPlaces;
    public static CircularImageView circularImageView;

    private RecyclerView workRecycler;
    private MyWorkPlaceRecyclerView adapter;

    private View view;
    FloatingActionButton myFloatingButton;

    private PopupWindow newWorkCreatorPopUp;
    private LoadingButton createBtn;
    //</editor-fold>

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState != null){
            myUser = savedInstanceState.getParcelable("currentUserInfo");
            String userImagePath = myUser.getProfileImageString();
            if(!userImagePath.isEmpty()){
                Uri imageDownloadUrl = Uri.parse(userImagePath);
                try{
                    Glide.with(circularImageView.getContext()).load(imageDownloadUrl).into(circularImageView);
                }catch (Exception e){
                    Log.d("GlideError", "Couldnt Get Image");
                }
            }else{
                circularImageView.setBackgroundResource(R.mipmap.ic_launcher_round);
            }
        }

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_welcome, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        //check permission for storage - onRequestPermissionsResult will be called from MainActivity.
        //if permission wont be granted - the user wont be able to upload images
        //<editor-fold desc="Asking For Permission External Storage">
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.PERMISSION_EXTERNAL);
        }
        //</editor-fold>

        // Get user information from sharedPreferances
        userHeaderNameString = preferences.getString(MyAppConstants.USER_NAME_FROM_SHAREDPREFERANCES, MyAppConstants.DEFAULT_USER_NAME);
        userNumOfWorkPlacesString = preferences.getString(MyAppConstants.USER_NUM_OF_WORK_PLACES, MyAppConstants.DEFAULT_NUM_OF_WORK_PLACES);
        userNumOfTotalFriends = preferences.getString(MyAppConstants.USER_TOTAL_FRIENDS, MyAppConstants.DEFAULT_NUM_OF_TOTAL_FRIENDS);
        userUpComingShiftsString = preferences.getString("numTotalString", "0");

        allUserWorkPlaces = new ArrayList<WorkPlaces>();
        myFloatingButton = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        userTotalFriendsTV = (TextView) view.findViewById(R.id.userTotalFriendsTV);
        userUpComingShiftsTV = (TextView) view.findViewById(R.id.userUpComingShiftsTV);
        userTotalWorkPlacesTV = (TextView) view.findViewById(R.id.userTotalWorkPlacesTV);
        userNameHeader = (TextView) view.findViewById(R.id.userWelcomeHeaderName);
        noWorkPlacesTV = (TextView) view.findViewById(R.id.noWorkPlaceTV);

        userNameHeader.setText(userHeaderNameString);
        userTotalWorkPlacesTV.setText(userNumOfWorkPlacesString);
        userTotalFriendsTV.setText(userNumOfTotalFriends);
        userUpComingShiftsTV.setText("" + userUpComingShiftsString);

        workRecycler = (RecyclerView) view.findViewById(R.id.workPlaceRecyclerView);
        workRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        //Custom progress bar
        whorlView = (WhorlView) view.findViewById(R.id.welcomeWhorlProgress);
        if(whorlView.getVisibility() == View.GONE)
            whorlView.setVisibility(View.VISIBLE);
        whorlView.start();

        imageProgressBar = (ProgressBar) view.findViewById(R.id.imageProgressBar);
        circularImageView = (CircularImageView)view.findViewById(R.id.userRoundImage);
        //assign onClick for the floating button
        myFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopUp();
            }
        });

        current_user_db = FirebaseDatabase.getInstance().getReference();
        //work_place_db is used in creating or joining a new workplace
        work_place_db = current_user_db.child(FireBaseConstants.ALL_APP_WORKPLACES);
        //user_single_work_place_db is used for getting total shifts and totatl employees for each workplace
        user_single_work_place_db = current_user_db.child(FireBaseConstants.ALL_APP_WORKPLACES);
        myFirebaseAuth = FirebaseAuth.getInstance();
        myFirebaseUser = myFirebaseAuth.getCurrentUser();

        //Getting the current user UID and then add value event listener and getting the whole user info (name, email, phone etc.)
        if(myFirebaseUser != null){
            getActivity().startService(new Intent(getActivity(), MyFirebaseInstanceIDService.class));
            final String user_firebase_UID = myFirebaseUser.getUid();
            //getting the device TokenId for future use
            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_firebase_UID).child(FireBaseConstants.USER_DEVICE_TOKEN).setValue(deviceToken);

            //Listener for users information - In case it changes, it will notify the app immediately and change the info shown to the user
            current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_firebase_UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    myUser = dataSnapshot.getValue(MyUser.class);
                    String userName = myUser.getFullName();
                    String userImagePath = myUser.getProfileImageString();
                    if(!userImagePath.isEmpty()){
                        Uri imageDownloadUrl = Uri.parse(userImagePath);
                        try{
                            Glide.with(circularImageView.getContext()).load(imageDownloadUrl).into(circularImageView);
                        }catch (Exception e){
                            Log.d("GlideError", "Couldnt Get Image");
                        }
                    }else{
                        circularImageView.setBackgroundResource(R.mipmap.ic_launcher_round);
                    }

                    userNameHeader.setText(userName);
                    editor.putString(MyAppConstants.USER_NAME_FROM_SHAREDPREFERANCES, userName);
                    editor.apply();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            //Getting all the WorkPlaces the user is connected to
            userWorkPlacesListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(totalFriends != 0 ){
                        totalFriends = 0;
                    }

                    if(allUserWorkPlaces.size() != 0){
                        allUserWorkPlaces.clear();
                    }
                    for (DataSnapshot workPlaceSnapShot: dataSnapshot.getChildren()) {
                        WorkPlaces singWorkPlace = workPlaceSnapShot.getValue(WorkPlaces.class);
                        allUserWorkPlaces.add(singWorkPlace);
                        Log.d("WorkPlaceUpdate", " A new work place added");
                    }
                    if(allUserWorkPlaces.size() == 0){
                        whorlView.stop();
                        whorlView.setVisibility(View.GONE);
                        workRecycler.setVisibility(View.GONE);
                        noWorkPlacesTV.setVisibility(View.VISIBLE);

                    }else{
                        noWorkPlacesTV.setVisibility(View.GONE);
                        workRecycler.setVisibility(View.VISIBLE);
                        adapter = new MyWorkPlaceRecyclerView(getActivity(), allUserWorkPlaces);
                        workRecycler.setAdapter(adapter);
                    }

                    getNumOfUpcoming(user_firebase_UID, allUserWorkPlaces);

                    for (int i = 0; i < allUserWorkPlaces.size(); i++) {
                        final int finalI = i;
                        user_single_work_place_db.child(allUserWorkPlaces.get(i).getWorkCode())
                                .child(allUserWorkPlaces.get(i).getWorkName()).child(FireBaseConstants.CHILD_USERS)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long num = dataSnapshot.getChildrenCount();
                                        totalFriends = totalFriends - 1 + num;

                                        //a way to make sure the UI updates only once (userTotalFriendsTV)
                                        if(finalI == allUserWorkPlaces.size() -1){
                                            String totalFriendsString = String.valueOf(totalFriends);
                                            userTotalFriendsTV.setText(totalFriendsString);
                                            editor.putString(MyAppConstants.USER_TOTAL_FRIENDS, totalFriendsString);
                                            editor.apply();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    int numOfWorkplaces = allUserWorkPlaces.size();
                    String numOfWorkplacesString = String.valueOf(numOfWorkplaces);
                    userTotalWorkPlacesTV.setText(numOfWorkplacesString);
                    editor.putString(MyAppConstants.USER_NUM_OF_WORK_PLACES, numOfWorkplacesString);
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            user_work_places_ref = current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_firebase_UID).child(FireBaseConstants.CHILD_USER_WORK_PLACES);
            user_work_places_ref.addValueEventListener(userWorkPlacesListener);

        }else {
            Toast.makeText(getActivity(), getResources().getString(R.string.session_error), Toast.LENGTH_SHORT).show();
            Log.d("Error Getting User", "myFirebaseUser returned null");
        }

        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), REQUEST_CODE_USER_IMAGE);
            }
        });


        return view;
    }

    class MyBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.hqs.alx.mushalmapp2.CHANGE_WORK_IMAGE")){
                chosenWorkPlace = intent.getParcelableExtra("choseWorkPlaceToChangeImage");
                Intent imageChangerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageChangerIntent.setType("image/jpeg");
                imageChangerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(imageChangerIntent, "Complete action using"), REQUEST_CODE_WORK_IMAGE);
            }
        }
    }

    public void getNumOfUpcoming(String user_uid, final ArrayList<WorkPlaces> allUserWorkPlaces){
        final ArrayList<ShiftInfoForUsers> lastUserShifts = new ArrayList<ShiftInfoForUsers>();

        final int[] total = {0};

        for (int i = 0; i < allUserWorkPlaces.size(); i++) {

            final int finalI = i;
            work_place_db.child(allUserWorkPlaces.get(i).getWorkCode()).child(allUserWorkPlaces.get(i).getWorkName())
                    .child(FireBaseConstants.CHILD_USERS_SHIFTS).child(user_uid).orderByKey().limitToLast(4)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(lastUserShifts.size() != 0)
                            lastUserShifts.clear();

                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            for (int i = 0; i < snap.getChildrenCount(); i++) {
                                String child = String.valueOf(i);
                                ShiftInfoForUsers shift = snap.child(child).getValue(ShiftInfoForUsers.class);
                                lastUserShifts.add(shift);
                                if(lastUserShifts.size() == 30){
                                    Log.d("LASTSHIFTS", " LIMIT OF 30 IS REACHED");
                                    break;
                                }
                            }
                        }

                        for (int i = 0; i < lastUserShifts.size(); i++) {
                            String dateString = lastUserShifts.get(i).getDate();
                            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            Calendar calendarShiftDate = Calendar.getInstance();
                            try {
                                Date currentDate = Calendar.getInstance().getTime();

                                calendarShiftDate.setTime(dateFormat.parse(dateString));
                                Date shiftDate = dateFormat.parse(dateString);
                                if(currentDate.after(shiftDate)){
                                    Log.d("CHECKDATE", "Current Date is AFTER shiftDate");
                                }else{
                                    total[0] = total[0] + 1;
                                    String numTotalString = String.valueOf(total[0]);
                                    userUpComingShiftsTV.setText(numTotalString);
                                    editor.putString("numTotalString", numTotalString);
                                    editor.apply();

                                    //upComingShifts.add(lastUserShifts.get(i));
                                    Log.d("CHECKDATE", "Current Date is BEFORE shiftDate");
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(total[0] == 0){
                        String numTotalString = String.valueOf(total[0]);
                        userUpComingShiftsTV.setText(numTotalString);
                        editor.putString("numTotalString", numTotalString);
                        editor.apply();
                    }
                    //stopping the custom progress bar
                    whorlView.stop();
                    whorlView.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void initiatePopUp(){

        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(getActivity());

        alertDialogBuider.setMessage(getActivity().getResources().getString(R.string.popUpHeader))
                .setPositiveButton(getActivity().getResources().getString(R.string.joinBtn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinWorkPlaceCreator();
                    }
                })
                .setNegativeButton(getActivity().getResources().getString(R.string.createBtn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newWorkPlaceCreator();
                    }
                }).setNeutralButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuider.create();
        alertDialog.show();
    }

    public void joinWorkPlaceCreator(){
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(getActivity());
        View popUpCreatorView = LayoutInflater.from(getActivity()).inflate(R.layout.join_work_pop_up, null);

        final TextInputEditText joinWorkName = (TextInputEditText) popUpCreatorView.findViewById(R.id.joinWorkName);
        final TextInputEditText joinWorkCode = (TextInputEditText) popUpCreatorView.findViewById(R.id.joinWorkCode);
        final LoadingButton joinWorkBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.loadingJoinBtn);

        joinWorkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                joinWorkBtn.startLoading();
                final String workPlaceName = joinWorkName.getText().toString().trim();
                final String workPlaceCode = joinWorkCode.getText().toString().trim();

                if (workPlaceName.isEmpty() || workPlaceCode.isEmpty()) {
                    joinWorkBtn.startLoading();
                    joinWorkBtn.loadingFailed();
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    myFirebaseUser = myFirebaseAuth.getCurrentUser();
                    if (myFirebaseUser != null) {
                        final String user_UID = myFirebaseUser.getUid();

                        current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_UID).child(FireBaseConstants.CHILD_USER_WORK_PLACES).child(workPlaceName)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    joinWorkBtn.startLoading();
                                    joinWorkBtn.loadingFailed();
                                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.workPlaceAlreadyExists), Toast.LENGTH_SHORT).show();
                                }else{
                                    current_user_db.child(FireBaseConstants.ALL_APP_WORKPLACES).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                                                String s = singleSnapShot.getKey();
                                                if (s.equals(workPlaceCode)) {
                                                    for (DataSnapshot child : singleSnapShot.getChildren()) {
                                                        String workNameFromCode = child.getKey();
                                                        if (workPlaceName.equals(workNameFromCode)) {
                                                            isSuccededJoin = true;

                                                            //TODO: change the device token place
                                                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                                            EmployeeForShift newUserInfo = new EmployeeForShift(myUser.getFullName(), user_UID, false);
                                                            work_place_db.child(workPlaceCode).child(workPlaceName).child(FireBaseConstants.CHILD_USERS)
                                                                    .child(user_UID).setValue(newUserInfo);
                                                            work_place_db.child(workPlaceCode).child(workPlaceName)
                                                                    .child(FireBaseConstants.USER_DEVICE_TOKEN).push().setValue(deviceToken);

                                                            //create workPlace reference (as child for the user)
                                                            WorkPlaces userWorkPlace = new WorkPlaces(workPlaceCode, workPlaceName, false);
                                                            current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_UID)
                                                                    .child(FireBaseConstants.CHILD_USER_WORK_PLACES).child(workPlaceName).setValue(userWorkPlace);
                                                        }
                                                    }
                                                }
                                            }
                                            if(isSuccededJoin){
                                                joinWorkBtn.loadingSuccessful();
                                                Toast.makeText(getActivity(), getResources().getString(R.string.joinedSuccessfully), Toast.LENGTH_SHORT).show();
                                                alertDialogJoin.cancel();
                                            }else{
                                                joinWorkBtn.loadingFailed();
                                                Toast.makeText(getActivity(), getResources().getString(R.string.error_joining_place), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            joinWorkBtn.loadingFailed();
                                            Toast.makeText(getActivity(), getResources().getString(R.string.error_joining_place), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        joinWorkBtn.startLoading();
                        joinWorkBtn.loadingFailed();
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_joining_place), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        alertDialogBuider.setView(popUpCreatorView);
        alertDialogJoin = alertDialogBuider.create();
        alertDialogJoin.show();
    }

    public void newWorkPlaceCreator(){
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(getActivity());
        View popUpCreatorView = LayoutInflater.from(getActivity()).inflate(R.layout.create_work_place_info, null);

        final EditText workName = (EditText) popUpCreatorView.findViewById(R.id.newWorkPlaceNameET);
        final EditText adminPass = (EditText) popUpCreatorView.findViewById(R.id.adminPassET);
        final EditText repeatAdminPass = (EditText) popUpCreatorView.findViewById(R.id.repeateAdminPassET);
        createBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.loadingCreateBtn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBtn.startLoading();
                String workNameString = workName.getText().toString().trim();
                String adminPassString = adminPass.getText().toString();
                String repeateAdminPassString = repeatAdminPass.getText().toString();

                if(workNameString.isEmpty() || adminPassString.isEmpty() || repeateAdminPassString.isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                    createBtn.loadingFailed();
                    return;
                }else if(adminPassString.equals(repeateAdminPassString)){
                    newWorkPlaceCreatorFireBase(workNameString, adminPassString);
                }else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.checkPassword), Toast.LENGTH_SHORT).show();
                    createBtn.loadingFailed();
                }
            }
        });

        alertDialogBuider.setView(popUpCreatorView);
        alertDialogNewWorkPlace = alertDialogBuider.create();
        alertDialogNewWorkPlace.show();
    }

    public void newWorkPlaceCreatorFireBase(final String workPlaceName, final String adminPass){

        // generate a random code for the new work place
        final Random r = new Random();
        final int Low = 123;
        final int High = 987;
        generatedWorkCode = r.nextInt(High-Low) + Low;

        generatedWorkCodeString = String.valueOf(generatedWorkCode);

        myFirebaseUser = myFirebaseAuth.getCurrentUser();
        if(myFirebaseUser != null){
            final String user_UID = myFirebaseUser.getUid();

            //check if the random code hasnt been used before
            current_user_db.child(FireBaseConstants.ALL_APP_WORKPLACES).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapShot: dataSnapshot.getChildren()){
                        String s = singleSnapShot.getKey();
                        if(s.equals(generatedWorkCodeString)){
                            generatedWorkCode = r.nextInt(High - Low) + Low;
                            generatedWorkCodeString = String.valueOf(generatedWorkCode);
                            onDataChange(dataSnapshot);
                        }
                    }
                    //Create a new work Place
                    WorkPlaces newWorkPlace = new WorkPlaces(generatedWorkCodeString, workPlaceName, adminPass);
                    EmployeeForShift employeeForShift = new EmployeeForShift(myUser.getFullName(), myUser.getFirebaseUID(), false);

                    work_place_db.child(generatedWorkCodeString).child(workPlaceName).setValue(newWorkPlace);
                    work_place_db.child(generatedWorkCodeString).child(workPlaceName)
                            .child(FireBaseConstants.CHILD_ADMIN).child(user_UID).setValue(myUser);
                    work_place_db.child(generatedWorkCodeString).child(workPlaceName)
                            .child(FireBaseConstants.CHILD_USERS).child(user_UID).setValue(employeeForShift);

                    //create workPlace reference (as child for the user)
                    WorkPlaces userWorkPlace = new WorkPlaces(generatedWorkCodeString, workPlaceName, true);
                    current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_UID).child(FireBaseConstants.CHILD_USER_WORK_PLACES)
                            .child(workPlaceName).setValue(userWorkPlace);

                    createBtn.loadingSuccessful();
                    Toast.makeText(getActivity(), getResources().getString(R.string.workPlaceCreated), Toast.LENGTH_SHORT).show();
                    alertDialogNewWorkPlace.cancel();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    createBtn.loadingFailed();
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_creating_workPlace), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            createBtn.loadingFailed();
            Toast.makeText(getActivity(), getResources().getString(R.string.error_creating_workPlace), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        intentFilter.addAction("com.hqs.alx.mushalmapp2.CHANGE_WORK_IMAGE");
        getActivity().registerReceiver(myBroadCastReceiver, intentFilter);

        if(myUser != null){
            String userImagePath = myUser.getProfileImageString();
            if(!userImagePath.isEmpty()){
                Uri imageDownloadUrl = Uri.parse(userImagePath);
                try{
                    Glide.with(circularImageView.getContext()).load(imageDownloadUrl).into(circularImageView);
                }catch (Exception e){
                    Log.d("GlideError", "Couldnt Get Image");
                }
            }else{
                circularImageView.setBackgroundResource(R.mipmap.ic_launcher_round);
            }
        }

        // Set title bar
        ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.app_name));
    }

    @Override
    public void onPause() {
        super.onPause();
        user_work_places_ref.removeEventListener(userWorkPlacesListener);
        getActivity().unregisterReceiver(myBroadCastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentUserInfo",myUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final String userUID = myFirebaseUser.getUid();
        if(requestCode == REQUEST_CODE_USER_IMAGE && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();

            Bitmap resizedBitmap = resizeBitmap(getPath(getActivity(), selectedImageUri), 1000, 1000);

                myUserStorageReference = FirebaseStorage.getInstance().getReference().child(FireBaseConstants.CHILD_USER_PHOTO).child(userUID);
            //StorageReference photoReference = myUserStorageReference.child(selectedImageUri.getLastPathSegment());
                StorageReference photoReference = myUserStorageReference.child("userProfileImage");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapData = baos.toByteArray();

                UploadTask uploadTask = photoReference.putBytes(bitmapData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(final UploadTask.TaskSnapshot taskSnapshot) {
                        imageProgressBar.setVisibility(View.VISIBLE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        RequestOptions options = new RequestOptions();
                        options.centerCrop();
                        options.fitCenter();

                        Uri downloadImageUrl = taskSnapshot.getDownloadUrl();
                        String userProfileImagString = downloadImageUrl.toString();
                        current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(userUID).child("profileImageString").setValue(userProfileImagString);

                        Glide.with(circularImageView.getContext()).load(downloadImageUrl).apply(options).into(circularImageView);
                        imageProgressBar.setVisibility(View.GONE);
                    }
                });
        }else if(requestCode == REQUEST_CODE_WORK_IMAGE && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();

            MyResizeBitmap myResizeBitmap = new MyResizeBitmap();

            Bitmap resizedBitmap = myResizeBitmap.resizeBitmap(getPath(getActivity(), selectedImageUri), 1000, 1000);

            chosenWorkStorageRef = FirebaseStorage.getInstance().getReference().child(FireBaseConstants.WORK_IMAGE).child(chosenWorkPlace.getWorkCode())
                    .child(chosenWorkPlace.getWorkName()).child("workImage");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bitmapData = baos.toByteArray();

            UploadTask uploadTask = chosenWorkStorageRef.putBytes(bitmapData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadImageUrl = taskSnapshot.getDownloadUrl();
                    String workImagString = downloadImageUrl.toString();
                    work_place_db.child(chosenWorkPlace.getWorkCode()).child(chosenWorkPlace.getWorkName())
                            .child(FireBaseConstants.WORK_IMAGE).setValue(workImagString).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            });
        }
    }

    public Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
