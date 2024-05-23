package com.example.cardiacrecorder.classes;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cardiacrecorder.others.StringListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MyTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private final StringListener listener;

    public MyTimePicker(StringListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        LocalTime localTime = LocalTime.now();

        int hour = localTime.getHour();
        int minute = localTime.getMinute();

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));

    }

    /**
     * sets time
     * @param timePicker The view associated with this listener.
     * @param hour The hour of the day (0-23).
     * @param min The minute of the hour (0-59).
     */

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int min) {

        LocalTime localTime = LocalTime.of(hour,min);
        String pattern = "hh:mma";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.US);

        listener.onStringSelected(formatter.format(localTime));

    }

}
