package com.hqs.alx.mushalmapp2;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hqs.alx.mushalmapp2.data.Shift;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    EditText userNameET, userPhoneET, userBirthDayET, userEmailET,
             repeatEmailET, userPasswordET, repeatPasswordET;

    private FirebaseAuth mAuth;
    DatabaseReference current_user_db;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        final LoadingButton signUpBtn = (LoadingButton) view.findViewById(R.id.signUpBtn);
        userNameET = (EditText) view.findViewById(R.id.userFullNameET);
        userPhoneET = (EditText) view.findViewById(R.id.userPhoneNumberET);
        userBirthDayET = (EditText) view.findViewById(R.id.enterBirthDateET);
        userEmailET = (EditText) view.findViewById(R.id.userEmailET);
        repeatEmailET = (EditText) view.findViewById(R.id.userEmailAgainET);
        userPasswordET = (EditText) view.findViewById(R.id.userPassET);
        repeatPasswordET = (EditText) view.findViewById(R.id.userPassRepeatET);

        mAuth = FirebaseAuth.getInstance();
        current_user_db = FirebaseDatabase.getInstance().getReference();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signUpBtn.startLoading();
                final String userName = userNameET.getText().toString().trim();
                final String userPhone = userPhoneET.getText().toString().trim();
                final String userBirthDay = userBirthDayET.getText().toString().trim();
                final String userEmail = userEmailET.getText().toString().trim();
                final String repeatEmail = repeatEmailET.getText().toString().trim();
                final String userPassword = userPasswordET.getText().toString().trim();
                final String repeatPassword = repeatPasswordET.getText().toString().trim();

                if(userName.isEmpty() || userPhone.isEmpty() || userBirthDay.isEmpty() || userEmail.isEmpty() || repeatEmail.isEmpty()
                        || userPassword.isEmpty() || repeatPassword.isEmpty()){
                    signUpBtn.loadingFailed();
                    signUpBtn.reset();
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                }else{
                    if(userEmail.equals(repeatEmail)){
                        if(userPassword.length() < 6 ){
                            Toast.makeText(getActivity(), getResources().getString(R.string.passwordAtleast6), Toast.LENGTH_SHORT).show();
                        }else if(userPassword.equals(repeatPassword)){
                            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        ArrayList<WorkPlaces> userPlaces = new ArrayList<WorkPlaces>();
                                        String userProfileImageEmpty ="";
                                        String firebaseUID = "";
                                        final MyUser newUser = new MyUser(firebaseUID, userName, userPhone, userEmail, userBirthDay, userPassword, userPlaces, userProfileImageEmpty);

                                        signUpBtn.loadingSuccessful();
                                        // wait 1 second than go to next fragment - without this user wont see the "successful Button"
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                // Actions to do after 1 seconds
                                                Intent intent = new Intent("com.hqs.alx.mushalmapp2.NEW_USER_INFO");
                                                intent.putExtra("user_constructor", newUser);
                                                getActivity().sendBroadcast(intent);
                                            }
                                        }, 1000);

                                    }else{
                                        signUpBtn.loadingFailed();
                                        signUpBtn.reset();
                                        String errorString = task.getException().toString();
                                        Log.d("MyEror: ", " " + errorString);
                                        Toast.makeText(getActivity(), "EROOORRRR" + errorString, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else{
                            signUpBtn.loadingFailed();
                            signUpBtn.reset();
                            Toast.makeText(getActivity(), getResources().getString(R.string.checkPassword), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        signUpBtn.loadingFailed();
                        signUpBtn.reset();
                        Toast.makeText(getActivity(), getResources().getString(R.string.checkEmail), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_settings);
        item.setVisible(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
