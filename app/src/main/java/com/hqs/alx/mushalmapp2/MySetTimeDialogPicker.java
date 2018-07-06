package com.hqs.alx.mushalmapp2;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Alex on 7/6/2018.
 */

public class MySetTimeDialogPicker implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

        private TextView textView;
        private Calendar myCalendar;
        private Context ctx;

        public MySetTimeDialogPicker(TextView textView, Context ctx){
            this.textView = textView;
            this.textView.setOnClickListener(this);
            this.myCalendar = Calendar.getInstance();
            this.ctx = ctx;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            this.textView.setText( hourOfDay + ":" + minute);
        }

        @Override
        public void onClick(View v) {
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(ctx, R.style.DatePickerTheme, this, hour, minute, true).show();
        }
}
