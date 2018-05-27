package com.hqs.alx.mushalmapp2;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
    private DatabaseReference notificationRef;
    FirebaseUser myFirebaseUser;
    FirebaseAuth myFirebaseAuth;

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    public void sendRegistrationToServer(String token) {

        myFirebaseAuth = FirebaseAuth.getInstance();
        myFirebaseUser = myFirebaseAuth.getCurrentUser();
        notificationRef = FirebaseDatabase.getInstance().getReference();

        if(myFirebaseUser != null) {
            String user_firebase_UID = myFirebaseUser.getUid();
            notificationRef.child(FireBaseConstants.ALL_APP_USERS).child(user_firebase_UID)
                                .child(FireBaseConstants.USER_DEVICE_TOKEN).setValue(token);
        }
    }
}
