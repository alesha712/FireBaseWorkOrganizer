package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.Manifest;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 05/04/2018.
 */

public class MyEmployeesListRecyclerAdapter extends RecyclerView.Adapter<MyEmployeesListRecyclerAdapter.MyViewHolder>{

    private ArrayList<String> keyValues;
    private Context context;
    private DatabaseReference userReference;

    public MyEmployeesListRecyclerAdapter (Context context, ArrayList<String> keyValues){
        this.context = context;
        this.keyValues = keyValues;
    }

    @Override
    public MyEmployeesListRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View singleEmployee = LayoutInflater.from(context).inflate(R.layout.single_employee_list, null);
        MyViewHolder holder = new MyViewHolder(singleEmployee);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyEmployeesListRecyclerAdapter.MyViewHolder holder, int position) {
        String user_uid = keyValues.get(position);
        holder.bindMyData(user_uid);
    }

    @Override
    public int getItemCount() {
        return keyValues.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View itemView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindMyData( final String user_uid){

            final TextView employeeName = (TextView) itemView.findViewById(R.id.singleEmployeeName);
            final CircularImageView employeeProfileImage = (CircularImageView) itemView.findViewById(R.id.singleEmployeeImage);

            final MyUser[] user = new MyUser[1];

            userReference = FirebaseDatabase.getInstance().getReference();
            userReference.child(FireBaseConstants.ALL_APP_USERS).child(user_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        user[0] = dataSnapshot.getValue(MyUser.class);
                    }

                    String imageUriString = user[0].getProfileImageString();
                    if(!imageUriString.equals("") && imageUriString != null){
                        Uri imageDownloadUrl = Uri.parse(imageUriString);
                        try{
                            Glide.with(employeeProfileImage.getContext()).load(imageDownloadUrl).into(employeeProfileImage);
                        }catch (Exception e){
                            Log.e("GlideError", "Couldnt Get Image");
                        }
                    }

                    employeeName.setText(user[0].getFullName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, itemView.findViewById(R.id.singleEmployeeImage));
                    //inflating menu from xml resource
                    popup.inflate(R.menu.employee_options_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.callEmployee:
                                    callEmployee(user[0].getPhone());
                                    break;
                                case R.id.send_SMS:
                                    sendSMS(user[0].getPhone());
                                    break;
                                case R.id.say_hi:
                                    //TODO: Implement sanding notification to a single user
                                    Toast.makeText(context, "Saying hello by sanding notifications", Toast.LENGTH_SHORT).show();
                                    sayHiWithNotification(user[0].getFullName(), WorkPlaceFragment.currentWorkPlace.getWorkName(), user_uid);
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });
        }

        public void callEmployee(String phoneString){

            Intent callIntent = new Intent(Intent.ACTION_CALL); //use ACTION_CALL class
            callIntent.setData(Uri.parse("tel:" + phoneString));    //this is the phone number calling
            //check permission
            //If the device is running Android 6.0 (API level 23) and the app's targetSdkVersion is 23 or higher,
            //the system asks the user to grant approval.
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //request permission from user if the app hasn't got the required permission
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.CALL_PHONE},   //request specific permission from user
                        10);
                Toast.makeText(context,context.getResources().getString(R.string.callPermissionRequired),Toast.LENGTH_SHORT).show();
                return;
            }else {     //have got permission
                try{
                    context.startActivity(callIntent);  //call activity and make phone call
                }
                catch (android.content.ActivityNotFoundException ex){
                    Toast.makeText(context,"yourActivity is not founded",Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void sendSMS (String phoneString){
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
                    + phoneString)));
        }

        public void sayHiWithNotification(String user_Uid, String workName, String user_uid){

            //reference for the private notifications location
            DatabaseReference myHiNotificationDataRef = FirebaseDatabase.getInstance().getReference();
            //getting the current user - the one who sends the notifications
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserID = currentUser.getUid();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", currentUserID);
            notificationData.put("type", "hi_notification");

            //user_uid is the user who recieves the hi notification
            myHiNotificationDataRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode()).child(workName)
                    .child(FireBaseConstants.CHILD_PRIVATE_NOTIFICATION).child(user_uid).push().setValue(notificationData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("Private Notification ", " adding to DB is Successful" );
                            }else{
                                Log.d("Private Notification ", " adding to DB is Not Successful" );
                            }
                        }
                    });
        }
    }
}
