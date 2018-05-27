package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hqs.alx.mushalmapp2.AutoSuggestedAdapter.EmployeeAutoSuggestedAdapter;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.WorkSchedule;

import java.util.ArrayList;

/**
 * Created by Alex on 12/03/2018.
 */

public class MyAllPlaceSchedulesRecyclerAdapter extends RecyclerView.Adapter<MyAllPlaceSchedulesRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<WorkSchedule> allSchedules;

    public MyAllPlaceSchedulesRecyclerAdapter(Context context, ArrayList<WorkSchedule> allSchedules) {
        this.context = context;
        this.allSchedules = allSchedules;
    }

    @Override
    public MyAllPlaceSchedulesRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View singleView = LayoutInflater.from(context).inflate(R.layout.single_schedule, null);
        MyViewHolder singelSchedule = new MyViewHolder(singleView);
        return singelSchedule;
    }

    @Override
    public void onBindViewHolder(MyAllPlaceSchedulesRecyclerAdapter.MyViewHolder holder, int position) {
        WorkSchedule workSchedule = allSchedules.get(position);
        holder.bindData(workSchedule);
    }

    @Override
    public int getItemCount() {
        return allSchedules.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View itemView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindData(final WorkSchedule w){

            TextView startDate = (TextView) itemView.findViewById(R.id.startDateTV);
            TextView endDate = (TextView) itemView.findViewById(R.id.endDateTV);
            ImageView readyOrNotIV = (ImageView) itemView.findViewById(R.id.readyIV);

            startDate.setText(w.getStartDate());
            endDate.setText(w.getEndDate());

            if(WorkPlaceFragment.currentWorkPlace.isAdmin()){

                if(!w.isReady()){
                    readyOrNotIV.setImageResource(R.drawable.ic_not_ready_24dp);
                }

            }else{
                readyOrNotIV.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.SCHEDULEVIEWER");
                    intent.putExtra("scheduleToView", w);
                    context.sendBroadcast(intent);
                }
            });


        }
    }
}
