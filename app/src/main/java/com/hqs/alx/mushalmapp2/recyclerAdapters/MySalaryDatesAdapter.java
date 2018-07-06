package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hqs.alx.mushalmapp2.R;

import java.util.ArrayList;

/**
 * Created by Alex on 29/05/2018.
 */

public class MySalaryDatesAdapter extends RecyclerView.Adapter<MySalaryDatesAdapter.MyViewHolder> {

    private ArrayList<String> allDates;
    private Context context;

    public MySalaryDatesAdapter(ArrayList<String> allDates, Context context){
        this.allDates = allDates;
        this.context = context;
    }


    @Override
    public MySalaryDatesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View singleDateView = LayoutInflater.from(context).inflate(R.layout.single_date_horizontal, parent, false);
        MyViewHolder singleDay = new MyViewHolder(singleDateView);

        return singleDay;
    }

    @Override
    public void onBindViewHolder(MySalaryDatesAdapter.MyViewHolder holder, int position) {
        String currentDate = allDates.get(position);
        holder.bindMyData(currentDate);
    }

    @Override
    public int getItemCount() {
        return allDates.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView dateTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindMyData (final String currentDate){
            dateTV = (TextView) itemView.findViewById(R.id.scheduleDateTV);
            dateTV.setText(currentDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.hqs.alx.mushalmapp2.CHOSEN_MONTH");
                    intent.putExtra("chosenDate", currentDate);
                    context.sendBroadcast(intent);
                }
            });
        }
    }
}
