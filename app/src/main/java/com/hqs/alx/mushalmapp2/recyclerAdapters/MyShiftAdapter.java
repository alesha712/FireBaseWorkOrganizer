package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.data.Shift;

import java.util.ArrayList;

/**
 * Created by Alex on 01/03/2018.
 */

public class MyShiftAdapter extends RecyclerView.Adapter<MyShiftAdapter.MyViewHolder> {

    Context context;
    ArrayList<Shift> shifts;

    public MyShiftAdapter(Context context, ArrayList<Shift> shifts) {
        this.context = context;
        this.shifts = shifts;
    }


    @Override
    public MyShiftAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View singleShiftView = LayoutInflater.from(context).inflate(R.layout.single_shift, null);
        MyViewHolder singleShift = new MyViewHolder(singleShiftView);
        return singleShift;
    }

    @Override
    public void onBindViewHolder(MyShiftAdapter.MyViewHolder holder, int position) {
        Shift singleShift = shifts.get(position);
        holder.bindMyData(singleShift);
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }

        public void bindMyData(Shift s){

            TextView shiftNameTV = (TextView) itemView.findViewById(R.id.shiftHeaderTV);
            shiftNameTV.setText(s.getStart() + " - " + s.getEnd());


        }
    }
}
