package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;

import java.util.ArrayList;

/**
 * Created by Alex on 29/05/2018.
 */

public class MySalaryShiftsAdapter extends RecyclerView.Adapter<MySalaryShiftsAdapter.MyViewHolder> {

    private ArrayList<ShiftInfoForUsers> allShifts;
    private Context context;
    private SharedPreferences preferences;

    public MySalaryShiftsAdapter (ArrayList<ShiftInfoForUsers> allShifts, Context context){
        this.allShifts = allShifts;
        this.context = context;
    }

    @Override
    public MySalaryShiftsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        View singleView = LayoutInflater.from(context).inflate(R.layout.single_shift_info_for_salary, parent, false);
        MyViewHolder singleHolder = new MyViewHolder(singleView);
        return singleHolder;
    }

    @Override
    public void onBindViewHolder(MySalaryShiftsAdapter.MyViewHolder holder, int position) {
        ShiftInfoForUsers currentShift = allShifts.get(position);
        holder.bindShiftData(currentShift);
    }

    @Override
    public int getItemCount() {
        return allShifts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView shiftDateTV,shiftTimeTV, shiftEarningsTV;
        EditText shiftComments, shiftDuration;
        LoadingButton updateBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindShiftData(final ShiftInfoForUsers currentShift){

            final double[] duration = new double[1];
            double salaryPerHour = Double.parseDouble(preferences.getString("salaryPerHour", "0"));
            double shiftSalary = salaryPerHour * currentShift.getDuration();

            shiftDateTV = (TextView) itemView.findViewById(R.id.shiftSalaryDateTV);
            shiftTimeTV = (TextView) itemView.findViewById(R.id.shiftSalaryTimeTV);
            shiftEarningsTV = (TextView) itemView.findViewById(R.id.shiftSalaryEarningsTV);
            shiftComments = (EditText) itemView.findViewById(R.id.shiftCommentsET);
            shiftDuration = (EditText) itemView.findViewById(R.id.durationET);
            updateBtn = (LoadingButton) itemView.findViewById(R.id.updateSavedShiftBtn);

            if(currentShift.getNotice() != null){
                shiftComments.setText(currentShift.getNotice());
            }

            String shiftDurationString = String.valueOf(currentShift.getDuration());
            shiftDuration.setText(shiftDurationString);

            String shiftSalaryString = String.valueOf(shiftSalary);
            shiftEarningsTV.setText(shiftSalaryString);

            shiftDateTV.setText(currentShift.getDate());
            shiftTimeTV.setText(currentShift.getStart() + " - " + currentShift.getEnd());
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBtn.startLoading();
                    if(shiftDuration.getText().toString().isEmpty()){
                        duration[0] = 0;
                        updateBtn.loadingFailed();
                    }else{
                        duration[0] = Double.parseDouble(shiftDuration.getText().toString().trim());
                        currentShift.setDuration(duration[0]);
                        currentShift.setNotice(shiftComments.getText().toString().trim());
                        long n = currentShift.save();
                        Log.d("dddd", "asdasd " + n);
                        //hide keyBoard
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        updateBtn.loadingSuccessful();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 1 second
                                updateBtn.reset();
                                notifyDataSetChanged();
                            }
                        }, 1000);

                    }

                    Toast.makeText(context, "" + currentShift.getDate(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
