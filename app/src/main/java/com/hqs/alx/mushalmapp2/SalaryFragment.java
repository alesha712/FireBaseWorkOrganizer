package com.hqs.alx.mushalmapp2;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.hqs.alx.mushalmapp2.data.ShiftInfoForUsers;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MySalaryDatesAdapter;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MySalaryShiftsAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class SalaryFragment extends Fragment {

    private MyBroadCastReceiver myBroadCastReceiver = new MyBroadCastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    RecyclerView datesRecyclerView, shiftsRecyclerView;
    MySalaryDatesAdapter salaryDatesAdapter;
    MySalaryShiftsAdapter shiftsAdapter;
    TextView shiftsNumTV, salaryPerHourTV, salaryPerMonthTV, noSavedShiftsTV, totalHoursNumTV;
    AlertDialog salaryPerHourDialog;

    public SalaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_salary, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        totalHoursNumTV = (TextView) view.findViewById(R.id.totalHoursNumTV);
        shiftsNumTV = (TextView) view.findViewById(R.id.totalShiftsNumTV);
        salaryPerHourTV = (TextView) view.findViewById(R.id.salaryNumTV);
        salaryPerMonthTV = (TextView) view.findViewById(R.id.totalSalaryNumTV);
        noSavedShiftsTV = (TextView) view.findViewById(R.id.noSavedShiftsTV);
        noSavedShiftsTV.setVisibility(View.GONE);

        salaryPerHourTV.setText(preferences.getString("salaryPerHour", "50") + " " + preferences.getString("currencyString", "USD"));

        datesRecyclerView = (RecyclerView) view.findViewById(R.id.monthScheduleRV);
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        shiftsRecyclerView = (RecyclerView) view.findViewById(R.id.shiftByMonthRV);
        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<ShiftInfoForUsers> savedShifts = (ArrayList<ShiftInfoForUsers>) ShiftInfoForUsers.listAll(ShiftInfoForUsers.class);
        ArrayList<String> datesArray = new ArrayList<String>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        int month = 0;
        int year = 0;

        for (int i = 0; i <  savedShifts.size(); i++) {
            String dateString = savedShifts.get(i).getDate();

            Date date = null;
            try {
                date = formatter.parse(dateString);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                month = cal.get(Calendar.MONTH) +1 ;
                year = cal.get(Calendar.YEAR);

                String s = Integer.toString(month) + "/" + Integer.toString(year);
                if(datesArray.size() == 0){
                    datesArray.add(s);
                }else{
                    boolean exists = true;
                    for (int j = 0; j < datesArray.size(); j++) {
                        String check = datesArray.get(j);
                        if(check.equals(s)){
                            exists = true;
                            j = datesArray.size() +1 ;
                        }else {
                            exists = false;
                        }
                        if(j == datesArray.size() - 1 && !exists){
                            datesArray.add(s);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(datesArray.size()!=0){
            salaryDatesAdapter = new MySalaryDatesAdapter(datesArray, getActivity());
            datesRecyclerView.setAdapter(salaryDatesAdapter);
        }else{
            noSavedShiftsTV.setVisibility(View.VISIBLE);
        }

        salaryPerHourTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpSalaryPerHour();
            }
        });



        return view;
    }

    private void popUpSalaryPerHour(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        View popUpView = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_salary_per_hour, null);

        final EditText salaryET = (EditText) popUpView.findViewById(R.id.salaryPerHourET);
        Spinner currencySpinner = (Spinner) popUpView.findViewById(R.id.currencySpinner);
        final LoadingButton submitBtn = (LoadingButton) popUpView.findViewById(R.id.salaryPerHourBtn);
        final String[] currencyString = new String[1];

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyString[0] =  parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currencyString[0] = parent.getItemAtPosition(0).toString();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBtn.startLoading();
                if(salaryET.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                    submitBtn.loadingFailed();
                }
                else {
                    salaryPerHourTV.setText(salaryET.getText().toString().trim() + " " + currencyString[0]);
                    editor.putString("salaryPerHour", salaryET.getText().toString().trim());
                    editor.putString("currencyString", currencyString[0]);
                    editor.apply();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // Actions to do after 1 second
                            salaryPerHourDialog.dismiss();
                        }
                    }, 850);
                }
            }
        });

        alertBuilder.setView(popUpView);
        salaryPerHourDialog = alertBuilder.create();
        salaryPerHourDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        intentFilter.addAction("com.hqs.alx.mushalmapp2.CHOSEN_MONTH");

        getActivity().registerReceiver(myBroadCastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myBroadCastReceiver);
    }

    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            double totalHoursPerMonth = 0 ;

            if(action.equals("com.hqs.alx.mushalmapp2.CHOSEN_MONTH")){
                String chosenDate = intent.getStringExtra("chosenDate");
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                ArrayList<ShiftInfoForUsers> savedShifts = (ArrayList<ShiftInfoForUsers>) ShiftInfoForUsers.listAll(ShiftInfoForUsers.class);
                ArrayList<ShiftInfoForUsers> shiftsToShow = new ArrayList<ShiftInfoForUsers>();
                for (int i = 0; i < savedShifts.size(); i++) {
                    String shiftDateString = savedShifts.get(i).getDate();
                    Date date = null;
                    try {
                        date = formatter.parse(shiftDateString);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int month = cal.get(Calendar.MONTH) + 1;
                        int year = cal.get(Calendar.YEAR);

                        String s = Integer.toString(month) + "/" + Integer.toString(year);
                        if (s.equals(chosenDate)) {
                            shiftsToShow.add(savedShifts.get(i));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(i==savedShifts.size()-1){
                        shiftsNumTV.setText("" + shiftsToShow.size());
                    }
                }

                shiftsAdapter = new MySalaryShiftsAdapter(shiftsToShow, getActivity());
                shiftsRecyclerView.setAdapter(shiftsAdapter);

                for (int i = 0; i <  shiftsToShow.size(); i++) {
                    totalHoursPerMonth = shiftsToShow.get(i).getDuration() + totalHoursPerMonth;
                }
                double totatl = (Double.parseDouble(preferences.getString("salaryPerHour", "0"))) * totalHoursPerMonth;
                String totatlSalaryString = String.valueOf(totatl);
                salaryPerMonthTV.setText(totatlSalaryString);
                totalHoursNumTV.setText(String.valueOf(totalHoursPerMonth));

            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.salary_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.hourSalary:
                Toast.makeText(getActivity(), "SalaryNow", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
