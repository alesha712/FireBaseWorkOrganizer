package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.ChatMessage;
import com.hqs.alx.mushalmapp2.MainActivity;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;

import java.util.ArrayList;

/**
 * Created by Alex on 10/02/2018.
 */

public class MyWorkPlaceRecyclerView extends RecyclerView.Adapter<MyWorkPlaceRecyclerView.MyViewHolder>{

    Context context;
    ArrayList<WorkPlaces> allUserWorkPlaces;

    private FirebaseUser myFirebaseUser;
    private DatabaseReference chosen_work_ref, chosen_user_ref;

    public MyWorkPlaceRecyclerView(Context context,ArrayList<WorkPlaces> allUserWorkPlaces) {
        this.context = context;
        this.allUserWorkPlaces = allUserWorkPlaces;
    }

    @Override
    public MyWorkPlaceRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View singleWorkPlaceView = LayoutInflater.from(context).inflate(R.layout.single_work_place, null);
        MyViewHolder singleWorkPlace = new MyViewHolder(singleWorkPlaceView);

        return singleWorkPlace;
    }

    @Override
    public void onBindViewHolder(MyWorkPlaceRecyclerView.MyViewHolder holder, int position) {

        WorkPlaces userSingleWorkPlace = allUserWorkPlaces.get(position);
        String singleWorkName = allUserWorkPlaces.get(position).getWorkName();
        holder.bindWorkData(singleWorkName, userSingleWorkPlace);

    }

    @Override
    public int getItemCount() {
        return allUserWorkPlaces.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        LoadingButton submitBtn;
        AlertDialog alertDialog;
        int newMessages = 0;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindWorkData (final String s, final WorkPlaces userWorkPlace){
            final ImageView workImage = (ImageView) itemView.findViewById(R.id.workPlaceImage);
            final ProgressBar workImageProgressBar = (ProgressBar) itemView.findViewById(R.id.workImageProgressBar);
            final ImageView workPlaceOptionsIV = (ImageView) itemView.findViewById(R.id.workPlaceOptionsIV);
            final ImageView newMessageIV = (ImageView) itemView.findViewById(R.id.newMessageIV);
            final TextView workNameTV = (TextView) itemView.findViewById(R.id.workPlaceNameTV);
            TextView roleTV = (TextView) itemView.findViewById(R.id.roleTV);
            TextView workCodeTV = (TextView) itemView.findViewById(R.id.workCodeTV);

            final long currentTimeStamp = System.currentTimeMillis();

            chosen_work_ref = FirebaseDatabase.getInstance().getReference();
            chosen_user_ref = FirebaseDatabase.getInstance().getReference();
            myFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            workNameTV.setText(s);
            workCodeTV.setText(workCodeTV.getText().toString() + " " + userWorkPlace.getWorkCode());

            newMessageIV.setVisibility(View.INVISIBLE);
            //TODO: CHECK FOR NEW MESSAGES

            chosen_user_ref.child(FireBaseConstants.ALL_APP_USERS).child(myFirebaseUser.getUid()).child(FireBaseConstants.CHILD_USER_WORK_PLACES)
                    .child(userWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_UNREAD_MESSAGES).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                                newMessageIV.setVisibility(View.VISIBLE);
                            else
                                newMessageIV.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            chosen_work_ref.child(FireBaseConstants.ALL_APP_WORKPLACES).child(userWorkPlace.getWorkCode()).child(userWorkPlace.getWorkName())
                    .child(FireBaseConstants.WORK_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        workImageProgressBar.setVisibility(View.VISIBLE);
                        String downloadImageUrl = dataSnapshot.getValue(String.class);
                        RequestOptions options = new RequestOptions();
                        options.centerCrop();
                        Glide.with(workImage.getContext()).load(downloadImageUrl).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                workImageProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                workImageProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).apply(options).into(workImage);
                    }else{
                        workImageProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(userWorkPlace.isAdmin()){
                StringBuilder bldr = new StringBuilder(roleTV.getText().toString());
                bldr.append(" ");
                bldr.append(context.getResources().getString(R.string.admin));
                String textToShow = bldr.toString();

                roleTV.setText(textToShow);
            }else {
                StringBuilder bldr = new StringBuilder(roleTV.getText().toString());
                bldr.append(" ");
                bldr.append(context.getResources().getString(R.string.employee));
                String textToShow = bldr.toString();

                roleTV.setText(textToShow);
            }

            workPlaceOptionsIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, workPlaceOptionsIV);
                    popupMenu.inflate(R.menu.work_place_options_menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.leaveWorkPlace:
                                    leaveWorkPlace(userWorkPlace);
                                    break;
                                case R.id.profileImage:
                                    if(userWorkPlace.isAdmin()){
                                        //This action is received in welcomeFragment  and changes the work image
                                        Intent intent = new Intent("com.hqs.alx.mushalmapp2.CHANGE_WORK_IMAGE");
                                        intent.putExtra("choseWorkPlaceToChangeImage", userWorkPlace);
                                        context.sendBroadcast(intent);
                                    }else{
                                        Toast.makeText(context, context.getResources().getString(R.string.onlyAdminCanUseThisOption), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case R.id.becomeAdmin:
                                    if(!userWorkPlace.isAdmin()){
                                        becomeAdmin(userWorkPlace);
                                    }else
                                        Toast.makeText(context, context.getResources().getString(R.string.alreadyAdmin), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.WORK_PLACE");
                    intent.putExtra("chosen_wor_place", userWorkPlace);
                    context.sendBroadcast(intent);
                }
            });
        }

        private void leaveWorkPlace(final WorkPlaces chosenWorkPlace){

            final String user_uid = myFirebaseUser.getUid();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(context.getResources().getString(R.string.leavePlaceQuestion))
                    .setTitle(context.getResources().getString(R.string.areYouSure))
                    .setIcon(R.drawable.ic_warning_24dp)
                    .setPositiveButton(context.getResources().getString(R.string.leaveBtn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            chosen_work_ref.child(FireBaseConstants.ALL_APP_WORKPLACES).child(chosenWorkPlace.getWorkCode())
                                    .child(chosenWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_USERS).child(user_uid).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        chosen_user_ref.child(FireBaseConstants.ALL_APP_USERS).child(user_uid).child(FireBaseConstants.CHILD_USER_WORK_PLACES)
                                                .child(chosenWorkPlace.getWorkName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(context, context.getResources().getString(R.string.workPlaceLeft), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(context, context.getResources().getString(R.string.eror_updating_user), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(context.getResources().getString(R.string.stayBtn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private void becomeAdmin(final WorkPlaces workPlace){

            AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
            View popUpCreatorView = LayoutInflater.from(context).inflate(R.layout.become_admin, null);

            final EditText becomAdminPass = (EditText) popUpCreatorView.findViewById(R.id.becomeAdminPass);
            submitBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.loadingSubmitBtn);

            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitBtn.startLoading();
                    String enteredPass = becomAdminPass.getText().toString().trim();
                    checkPass(enteredPass, workPlace);
                }
            });

            alertDialogBuider.setView(popUpCreatorView);
            alertDialog = alertDialogBuider.create();
            alertDialog.show();

        }

        private void checkPass(final String enteredPass, final WorkPlaces workPlace){
            chosen_work_ref.child(FireBaseConstants.ALL_APP_WORKPLACES).child(workPlace.getWorkCode()).child(workPlace.getWorkName())
                    .child(FireBaseConstants.USER_PASSWORD).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String user_uid = myFirebaseUser.getUid();
                    String existingPassword = dataSnapshot.getValue(String.class);
                    if(existingPassword.equals(enteredPass)){
                        chosen_user_ref.child(FireBaseConstants.ALL_APP_USERS).child(user_uid).child(FireBaseConstants.CHILD_USER_WORK_PLACES)
                                .child(workPlace.getWorkName()).child(FireBaseConstants.CHILD_ADMIN).setValue(true);
                        submitBtn.loadingSuccessful();
                        //wait 0.8 second before closing the alerDialog - without this, the user wont see the "succesfful button".
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 0.8 second
                                alertDialog.cancel();
                            }
                        }, 800);

                    }else{
                        submitBtn.loadingFailed();
                        Toast.makeText(context, context.getResources().getString(R.string.wrong_login_info), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
