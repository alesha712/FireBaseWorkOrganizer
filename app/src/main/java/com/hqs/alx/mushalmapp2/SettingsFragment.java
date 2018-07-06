package com.hqs.alx.mushalmapp2;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    String userWorkName;
    String user_uid;

    Button signOutBtn;
    TextView userNameHeadTV;
    EditText userFullNameET, userEmailET, userPhoneET, userPassForAuth, birthET;

    DatePickerDialog datePickerDialog;

    EditText userCurrentPassET, userNewPassET, userRepeatNewPassET;

    FirebaseAuth myFirebaseAuth;
    FirebaseDatabase myFirebaseDatabase;
    DatabaseReference current_user_db, work_place_db, all_users_db;
    FirebaseUser myFirebaseUser;
    ValueEventListener currentUserListener;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userNameHeadTV = (TextView) view.findViewById(R.id.settingUserNameHeadTV);
        userFullNameET = (EditText) view.findViewById(R.id.settingsFullNameET);
        userEmailET = (EditText) view.findViewById(R.id.settingsEmailET);
        userPhoneET = (EditText) view.findViewById(R.id.settingsPhoneET);
        userPassForAuth = (EditText) view.findViewById(R.id.settingsPassAuthET);
        birthET = (EditText) view.findViewById(R.id.settingsBirthET);
        signOutBtn = (Button) view.findViewById(R.id.signOutBtn);

        userCurrentPassET = (EditText) view.findViewById(R.id.settingsCurrentPassET);
        userNewPassET = (EditText) view.findViewById(R.id.settingsNewPassET);
        userRepeatNewPassET = (EditText) view.findViewById(R.id.settingsRepeatPassET);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), getResources().getString(R.string.user_signed_out), Toast.LENGTH_SHORT).show();
                FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                fragmentChanger.changeToLogin();
            }
        });

        birthET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.hasFocus()){
                    final Calendar c = Calendar.getInstance();
                    c.set(1990, 2, 23);
                    int myYear = c.get(Calendar.YEAR); // current year
                    int myMonth = c.get(Calendar.MONTH); // current month
                    int myDay = c.get(Calendar.DAY_OF_MONTH); // current Day

                    datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            birthET.setText(dayOfMonth + "/"
                                    + (month + 1) + "/" + year);
                        }
                    }, myYear, myMonth, myDay);
                    datePickerDialog.show();
                }
            }
        });

        myFirebaseDatabase = FirebaseDatabase.getInstance();
        myFirebaseAuth = FirebaseAuth.getInstance();
        current_user_db = myFirebaseDatabase.getReference();
        myFirebaseUser = myFirebaseAuth.getCurrentUser();


        currentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUser user = dataSnapshot.getValue(MyUser.class);
                userWorkName = user.getWorkName();
                userNameHeadTV.setText(user.getFullName());
                userFullNameET.setText(user.getFullName());
                userEmailET.setText(user.getEmail());
                userPhoneET.setText(user.getPhone());
                birthET.setText(user.getDateOfBirth());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("database error: ", databaseError.getDetails().toString());
            }
        };

        if(myFirebaseUser != null){
            //get user UID from firebase
            user_uid = myFirebaseUser.getUid();
            //get referance to current user database or create a new one if not exciting
            all_users_db = current_user_db.child(FireBaseConstants.ALL_APP_USERS).child(user_uid);
            userNameHeadTV.setText(myFirebaseUser.getDisplayName());
            userFullNameET.setText(myFirebaseUser.getDisplayName());
            userEmailET.setText(myFirebaseUser.getEmail());
            all_users_db.addValueEventListener(currentUserListener);
        }

        ((view).findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myFirebaseUser != null){
                    final String newUserName = userFullNameET.getText().toString().trim();
                    final String newEmail = userEmailET.getText().toString().trim();
                    final String newPhone = userPhoneET.getText().toString().trim();
                    final String newDateOfBirth = birthET.getText().toString().trim();
                    final String uid_userUpdate = myFirebaseUser.getUid();

                    if(newUserName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newDateOfBirth.isEmpty()){
                        Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        final String passAuth = userPassForAuth.getText().toString();
                        //authotication is required before changing email or password
                        if (!passAuth.isEmpty()) {
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(myFirebaseUser.getEmail(), passAuth);

                            // Prompt the user to re-provide their sign-in credentials
                            myFirebaseUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Log Message: ", "User re-authenticated.");
                                                Map<String, Object> userUpdates = new HashMap<>();
                                                userUpdates.put(FireBaseConstants.USER_FULL_NAME, newUserName);
                                                userUpdates.put(FireBaseConstants.USER_PHONE, newPhone);
                                                userUpdates.put(FireBaseConstants.USER_EMAIL, newEmail);
                                                userUpdates.put(FireBaseConstants.USER_BITHDAY, newDateOfBirth);
                                                all_users_db.updateChildren(userUpdates);

                                                myFirebaseUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), getResources().getString(R.string.user_updated), Toast.LENGTH_SHORT).show();
                                                            userEmailET.setText(myFirebaseUser.getEmail());
                                                        } else if (!task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), getResources().getString(R.string.eror_updating_user), Toast.LENGTH_SHORT).show();
                                                            Log.d("Update Eror : ", task.getException().getMessage().toString());
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(getActivity(), getResources().getString(R.string.eror_updating_user), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(getActivity(), getResources().getString(R.string.password_required), Toast.LENGTH_SHORT).show();
                        }

                    }

                }

            }
        });

        (view.findViewById(R.id.updatePasswordBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userCurrentPassET.getText().toString().trim().isEmpty() || userNewPassET.getText().toString().trim().isEmpty()
                                                                            || userRepeatNewPassET.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                }else{
                    final String newPass = userNewPassET.getText().toString().trim();
                    String repeatNew = userRepeatNewPassET.getText().toString().trim();

                    String pass = userCurrentPassET.getText().toString().trim();
                    String email = myFirebaseUser.getEmail();

                    if(email != null){
                        if(newPass.equals(repeatNew)){
                            AuthCredential credential = EmailAuthProvider.getCredential(email,pass);

                            myFirebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        myFirebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getActivity(), getResources().getString(R.string.passwordUpdated), Toast.LENGTH_SHORT).show();
                                                    userCurrentPassET.setText("");
                                                    userNewPassET.setText("");
                                                    userRepeatNewPassET.setText("");
                                                }else{
                                                    Toast.makeText(getActivity(), getResources().getString(R.string.errorUpdatingInfo), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(getActivity(), getResources().getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else
                            Toast.makeText(getActivity(), getResources().getString(R.string.checkPassword), Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(currentUserListener!= null){
            all_users_db.removeEventListener(currentUserListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        all_users_db.addValueEventListener(currentUserListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.setting_menu, menu);
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signeOutMenu:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), getResources().getString(R.string.user_signed_out), Toast.LENGTH_SHORT).show();
                FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                fragmentChanger.changeToLogin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
