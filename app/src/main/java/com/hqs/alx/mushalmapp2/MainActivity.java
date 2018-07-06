package com.hqs.alx.mushalmapp2;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hqs.alx.mushalmapp2.AutoSuggestedAdapter.EmployeeAutoSuggestedAdapter;
import com.hqs.alx.mushalmapp2.ScheduleViewers.ScheduleViewerFragment;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyEmployeesListRecyclerAdapter;
import com.orm.SugarContext;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

public class MainActivity extends AppCompatActivity implements FragmentChanger {

    public final static int PERMISSION_EXTERNAL = 154;

    private MyBroadCastReceiver myBroadCastReceiver = new MyBroadCastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    MyUser myUser;
    WorkPlaces chosenWorkPlace;

    DatabaseReference current_user_db, work_array_db;
    private FirebaseAuth myFirebaseAuth;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    FirebaseUser myFirebaseUser;

    WorkPlaceFragment workPlaceFragment;
    ChatFragment chatFragment;
    WorkScheduleFragment workScheduleFragment;
    WelcomeFragment welcomeFragment;
    ScheduleViewerFragment scheduleViewerFragment;
    EmployeesListFragment employeeListFragment;
    ToDOFragment toDOFragment;
    SalaryFragment salaryFragment;

    boolean isWelcomeFragVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SugarContext.init(this);

        if(savedInstanceState != null){
            isWelcomeFragVisible = savedInstanceState.getBoolean("isWelcomeFragVisible");
        }

        workPlaceFragment = new WorkPlaceFragment();
        welcomeFragment = new WelcomeFragment();
        chatFragment = new ChatFragment();
        workScheduleFragment  = new WorkScheduleFragment();
        scheduleViewerFragment = new ScheduleViewerFragment();
        toDOFragment = new ToDOFragment();
        salaryFragment = new SalaryFragment();

        current_user_db = FirebaseDatabase.getInstance().getReference();
        myFirebaseAuth = FirebaseAuth.getInstance();
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //user is signed in
                    if(isWelcomeFragVisible)
                        getFragmentManager().beginTransaction().add(R.id.MymainLayout, welcomeFragment).commit();

                }else if (user == null){
                    //user is signed out
                    LoginFragment loginFragment = new LoginFragment();
                    getFragmentManager().beginTransaction().add(R.id.MymainLayout, loginFragment).commit();
                }
            }
        };

        myFirebaseAuth.addAuthStateListener(myAuthStateListener);

        //startService(new Intent(this, MyFirebaseMessagingService.class));
/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1545);
        }
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                isWelcomeFragVisible = false;
                changeToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!welcomeFragment.isAdded())
            outState.putBoolean("isWelcomeFragVisible", false);
        else{
            isWelcomeFragVisible = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        intentFilter.addAction("com.hqs.alx.mushalmapp2.USER_WORK_CODE");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.WORK_PLACE");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.GO_TO_CHAT");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.GO_TO_SCHEDULE");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.NEW_USER_INFO");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.SCHEDULEVIEWER");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.GO_TO_TODOLIST");
        intentFilter.addAction("com.hqs.alx.mushalmapp2.SAVED_SHIFTS");

        MainActivity.this.registerReceiver(myBroadCastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(myAuthStateListener != null)
            myFirebaseAuth.removeAuthStateListener(myAuthStateListener);

        unregisterReceiver(myBroadCastReceiver);
    }

    @Override
    public void changeFragments() {

        getFragmentManager().beginTransaction().replace(R.id.MymainLayout, welcomeFragment).commit();
    }

    @Override
    public void changeToChatFragment() {
        getFragmentManager().beginTransaction().addToBackStack("movingToChat").replace(R.id.MymainLayout, chatFragment).commit();
    }

    @Override
    public void changeToSettings() {

        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().addToBackStack("movingToSettings").replace(R.id.MymainLayout, settingsFragment).commit();
    }

    @Override
    public void changeToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        getFragmentManager().beginTransaction().replace(R.id.MymainLayout, loginFragment).commit();
    }

    @Override
    public void changeToSignUp() {
        SignUpFragment signUpFragment = new SignUpFragment();
        getFragmentManager().beginTransaction().addToBackStack("movingToSignUp").replace(R.id.MymainLayout, signUpFragment).commit();
    }

    @Override
    public void changeToemployeeList() {
        employeeListFragment = new EmployeesListFragment();
        getFragmentManager().beginTransaction().addToBackStack("MovingToEmployeeList").replace(R.id.MymainLayout, employeeListFragment).commit();
    }

    @Override
    public void changeToWorkSchedule() {

        getFragmentManager().beginTransaction().addToBackStack("movingToSchedule").replace(R.id.MymainLayout, workScheduleFragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    WelcomeFragment.circularImageView.setClickable(true);
                    Log.d("ExternalPermission: ", "Granted!!!");

                } else {
                    WelcomeFragment.circularImageView.setClickable(false);
                    Toast.makeText(this, getResources().getString(R.string.permissionRequiredForImages), Toast.LENGTH_LONG).show();
                    Log.d("ExternalPermission: ", "Denied!!!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.hqs.alx.mushalmapp2.NEW_USER_INFO")){
                    myUser = intent.getParcelableExtra("user_constructor");
                myFirebaseUser = myFirebaseAuth.getCurrentUser();
                if(myFirebaseUser != null) {
                    final String user_UID = myFirebaseUser.getUid();
                    myUser.setFirebaseUID(user_UID);
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_UID).setValue(myUser);
                    current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_UID)
                                            .child(FireBaseConstants.USER_DEVICE_TOKEN).setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeFragments();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Failure", "" + e.getMessage().toString());
                        }
                    });

                }else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.session_error), Toast.LENGTH_SHORT).show();
                }
            }else if(action.equals("com.hqs.alx.mushalmapp2.WORK_PLACE")){
                chosenWorkPlace = intent.getParcelableExtra("chosen_wor_place");
                workPlaceFragment.currentWorkPlace = chosenWorkPlace;
                //getFragmentManager().beginTransaction().remove(welcomeFragment);
                getFragmentManager().beginTransaction().addToBackStack("MovingToWorkFrag").replace(R.id.MymainLayout, workPlaceFragment).setTransition(TRANSIT_FRAGMENT_OPEN).commit();
                isWelcomeFragVisible = false;

            }else if(action.equals("com.hqs.alx.mushalmapp2.GO_TO_CHAT")){
                chatFragment.currentUser = intent.getParcelableExtra("currentUser");
                chatFragment.workUsers = intent.getParcelableArrayListExtra("workUsersArray");
                chatFragment.currentWorkPlace = intent.getParcelableExtra("work_place_to_chat");
                getFragmentManager().beginTransaction().addToBackStack("movingToChat").replace(R.id.MymainLayout, chatFragment).commit();
            }else if(action.equals("com.hqs.alx.mushalmapp2.GO_TO_SCHEDULE")){
                workScheduleFragment.currentWorkPlace = intent.getParcelableExtra("work_place_to_schedule");
                getFragmentManager().beginTransaction().addToBackStack("movingToSchedule").replace(R.id.MymainLayout, workScheduleFragment).commit();
            }else if(action.equals("com.hqs.alx.mushalmapp2.SCHEDULEVIEWER")){
                scheduleViewerFragment.currentWorkSchedule = intent.getParcelableExtra("scheduleToView");
                getFragmentManager().beginTransaction().addToBackStack("viewSchedule").replace(R.id.MymainLayout, scheduleViewerFragment).commit();
            }else if(action.equals("com.hqs.alx.mushalmapp2.GO_TO_TODOLIST")){
                toDOFragment.currentUser = intent.getParcelableExtra("currentUser");
                getFragmentManager().beginTransaction().addToBackStack("goToTODOList").replace(R.id.MymainLayout, toDOFragment).commit();
            }else if(action.equals("com.hqs.alx.mushalmapp2.SAVED_SHIFTS")){
                getFragmentManager().beginTransaction().addToBackStack("goToSalary").replace(R.id.MymainLayout, salaryFragment).commit();
            }

        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


}
