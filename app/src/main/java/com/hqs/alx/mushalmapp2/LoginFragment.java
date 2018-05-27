package com.hqs.alx.mushalmapp2;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;

import java.util.Arrays;

public class LoginFragment extends Fragment {

    EditText userEmail;
    EditText userPassword;
    LoadingButton loadingButton;
    View mainView;
    View animateView;

    private FirebaseAuth myFirebaseAuth;
    private FirebaseUser current_user;
    private DatabaseReference current_user_ref;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_login, container, false);

        userEmail = (EditText)mainView.findViewById(R.id.userNameET);
        userPassword = (EditText) mainView.findViewById(R.id.userPassET);
        loadingButton = (LoadingButton) mainView.findViewById(R.id.loading_btn);
        animateView = (View) mainView.findViewById(R.id.animate_view);

        (mainView.findViewById(R.id.signUpTV)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                fragmentChanger.changeToSignUp();
            }
        });

        myFirebaseAuth = FirebaseAuth.getInstance();
        loadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingButton.startLoading();
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.wrong_login_info), Toast.LENGTH_SHORT).show();
                    loadingButton.loadingFailed();
                    loadingButton.reset();
                    return;
                }else {
                    myFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                loadingButton.loadingFailed();
                                loadingButton.reset();
                                Toast.makeText(getActivity(), "Check your login information", Toast.LENGTH_SHORT).show();
                            }
                            if (task.isSuccessful()) {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                current_user = myFirebaseAuth.getCurrentUser();
                                String user_uid = current_user.getUid();
                                current_user_ref = FirebaseDatabase.getInstance().getReference();
                                current_user_ref.child(FireBaseConstants.ALL_APP_USERS).child(user_uid)
                                                        .child(FireBaseConstants.USER_DEVICE_TOKEN).setValue(deviceToken)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        loadingButton.loadingSuccessful();
                                        startAnimation();
                                        // wait 1 second than go to next fragment - without this user wont see the "successful Button"
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                // Actions to do after 1 seconds
                                                FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                                                fragmentChanger.changeFragments();
                                            }
                                        }, 1000);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Failure", ""+ e.getMessage().toString());
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });



        return mainView;
    }

    private void startAnimation() {

        int cx = (loadingButton.getLeft() + loadingButton.getRight()) / 2;
        int cy = (loadingButton.getTop() + loadingButton.getBottom()) / 2;

        Animator animator = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(animateView,cx,cy,0,getResources().getDisplayMetrics().heightPixels * 1.2f);
        }

        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateInterpolator());
        animateView.setVisibility(View.VISIBLE);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                loadingButton.reset();

                animateView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
