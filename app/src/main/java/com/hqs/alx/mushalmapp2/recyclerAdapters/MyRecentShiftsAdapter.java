package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hqs.alx.mushalmapp2.MySetTimeDialogPicker;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WelcomeFragment;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alex on 22/04/2018.
 */

public class MyRecentShiftsAdapter extends RecyclerView.Adapter<MyRecentShiftsAdapter.MyViewHolder> {

    private ArrayList<ShiftInfoForUsers> recentShifts;
    private Context context;
    private DatabaseReference shiftRef;
    private AlertDialog shiftInfoDialog;

    public MyRecentShiftsAdapter(Context context, ArrayList<ShiftInfoForUsers> recentShifts){
        this.context = context;
        this.recentShifts = recentShifts;
    }

    @Override
    public MyRecentShiftsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        shiftRef = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_WORKPLACES)
                .child(WorkPlaceFragment.currentWorkPlace.getWorkCode()).child(WorkPlaceFragment.currentWorkPlace.getWorkName())
                .child(FireBaseConstants.CHILD_USERS_SHIFTS);
        View singleView = LayoutInflater.from(context).inflate(R.layout.single_shift_info_for_user, null);
        MyViewHolder singleShiftInfo = new MyViewHolder(singleView);

        return singleShiftInfo;
    }

    @Override
    public void onBindViewHolder(MyRecentShiftsAdapter.MyViewHolder holder, int position) {

        ShiftInfoForUsers singleShiftToShow = recentShifts.get(position);
        holder.bindData(singleShiftToShow, position);

    }

    @Override
    public int getItemCount() {
        return recentShifts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View itemView;
        TextView date;
        TextView startTime;
        TextView endTime;
        ImageView doneIV;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindData(final ShiftInfoForUsers singleShiftToShow, final int position){

            date = (TextView) itemView.findViewById(R.id.shiftInfoDateTV);
            startTime = (TextView) itemView.findViewById(R.id.shiftInfoStartTV);
            endTime = (TextView) itemView.findViewById(R.id.shiftInfoEndTV);
            doneIV = (ImageView) itemView.findViewById(R.id.shiftDoneIV);

            date.setText(singleShiftToShow.getDate());
            startTime.setText(singleShiftToShow.getStart());
            endTime.setText(singleShiftToShow.getEnd());

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(context, "LONGGGG", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });


            if(singleShiftToShow.isDone()){
                doneIV.setImageResource(R.drawable.ic_more_vert_24dp);
                doneIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        View popUpView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_saved_shift_info, null);

                        TextView shiftDateTV = (TextView) popUpView.findViewById(R.id.shiftDateInfoDialogTV);
                        final TextView shiftStartTV = (TextView) popUpView.findViewById(R.id.shiftStartInfoDialogTV);
                        final TextView shiftEndTV = (TextView) popUpView.findViewById(R.id.shiftEndInfoDialogTV);
                        final EditText shiftComments = (EditText) popUpView.findViewById(R.id.shiftCommentsInfoDialog);

                        shiftDateTV.setText(singleShiftToShow.getDate());
                        shiftStartTV.setText(singleShiftToShow.getStart());
                        shiftEndTV.setText(singleShiftToShow.getEnd());

                        MySetTimeDialogPicker firstStart = new MySetTimeDialogPicker(shiftStartTV, context);
                        MySetTimeDialogPicker firstEnd = new MySetTimeDialogPicker(shiftEndTV, context);

                        if(singleShiftToShow.getNotice() != null)
                            shiftComments.setText(singleShiftToShow.getNotice());

                        Log.d("OMGGGGGGGGGGG", "ERRR");
                        alertBuilder.setPositiveButton(context.getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String startTime = shiftStartTV.getText().toString();
                                String endTime = shiftEndTV.getText().toString();

                                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                Date date1 = null;
                                Date date2 = null;
                                try {
                                    date1 = format.parse(startTime);
                                    date2 = format.parse(endTime);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                long duration = (date2.getTime() - date1.getTime()) / (60 * 60 * 1000) % 24;
                                if(duration < 0)
                                    duration = duration + 24;

                                singleShiftToShow.setDuration(duration);
                                singleShiftToShow.setStart(shiftStartTV.getText().toString());
                                singleShiftToShow.setEnd(shiftEndTV.getText().toString());
                                singleShiftToShow.setNotice(shiftComments.getText().toString());
                                long d = singleShiftToShow.save();
                                Toast.makeText(context, context.getResources().getString(R.string.shiftUpdated), Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shiftInfoDialog.dismiss();
                            }
                        }).setNeutralButton(context.getResources().getString(R.string.deleteItem), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                singleShiftToShow.delete();
                                Toast.makeText(context, context.getResources().getString(R.string.itemDeleted), Toast.LENGTH_SHORT).show();
                                recentShifts.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, recentShifts.size());
                            }
                        });

                        alertBuilder.setView(popUpView);
                        shiftInfoDialog = alertBuilder.create();
                        shiftInfoDialog.show();
                    }
                });
            }else{
                doneIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.hqs.alx.mushalmapp2.SHIFT_SAVED");
                        intent.putExtra("shiftSaved", singleShiftToShow);
                        context.sendBroadcast(intent);
                    }
                });
            }

        }
    }
}
