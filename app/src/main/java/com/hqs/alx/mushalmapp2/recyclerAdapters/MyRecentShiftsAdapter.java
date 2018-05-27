package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WelcomeFragment;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;

import java.util.ArrayList;

/**
 * Created by Alex on 22/04/2018.
 */

public class MyRecentShiftsAdapter extends RecyclerView.Adapter<MyRecentShiftsAdapter.MyViewHolder> {

    private ArrayList<ShiftInfoForUsers> recentShifts;
    private Context context;
    private DatabaseReference shiftRef;

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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, " " + singleShiftToShow.getDate(), Toast.LENGTH_SHORT).show();
                    ArrayList<ShiftInfoForUsers> allShifts = (ArrayList<ShiftInfoForUsers>) ShiftInfoForUsers.listAll(ShiftInfoForUsers.class);
                    ShiftInfoForUsers.deleteAll(ShiftInfoForUsers.class);
                    Toast.makeText(context, "" + allShifts.size(), Toast.LENGTH_SHORT).show();
                }
            });

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
