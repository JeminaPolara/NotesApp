

package com.note.remindernote;

import static com.note.remindernote.utils.ConstantsBase.DATE_FORMAT_ASSIGN;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.note.remindernote.MyApp;
import com.note.remindernote.R;
import com.note.remindernote.helpers.date.DateHelper;
import com.note.remindernote.models.Note;
import com.note.remindernote.utils.date.ReminderPickers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SublimePickerFragment extends DialogFragment {

    DateFormat mDateFormatter, mTimeFormatter;

    Callback mCallback;

    private int mYear, mMonth, mHour, mMinute, mDay;

    private String mTime;
    private String mDate;

    private String mRepeatNo;
    private String mRepeatType;
    private String mActive;

    private TextView mDateText, mTimeText, mRepeatText, mRepeatNoText, mRepeatTypeText;
    private Switch mRepeatSwitch;

    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;

    private CardView idCVRepeatSub;
    private Button idBtnAddRepeateType;
    private LinearLayout repeat_ll;
    Note note;
    Activity mActivity;

    public SublimePickerFragment(Activity mActivity, Note note) {
        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        this.note = note;
        this.mActivity = mActivity;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.activity_add_reminder, container, false);
        mDateText = (TextView) view.findViewById(R.id.set_date);
        mTimeText = (TextView) view.findViewById(R.id.set_time);
        mRepeatText = (TextView) view.findViewById(R.id.set_repeat);
        mRepeatNoText = (TextView) view.findViewById(R.id.set_repeat_no);
        mRepeatTypeText = (TextView) view.findViewById(R.id.set_repeat_type);
        mRepeatSwitch = (Switch) view.findViewById(R.id.repeat_switch);
        mFAB1 = (FloatingActionButton) view.findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) view.findViewById(R.id.starred2);
        idCVRepeatSub = (CardView) view.findViewById(R.id.idCVRepeatSub);
        idBtnAddRepeateType = (Button) view.findViewById(R.id.idBtnAddRepeateType);
        repeat_ll = (LinearLayout) view.findViewById(R.id.repeat_ll);


        Calendar mCalendar = ReminderPickers.calendar;
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);
        mDate = mDay + " " + mMonth + "," + mYear;
        mTime = mHour + ":" + mMinute;

        mRepeatNo = Integer.toString(1);
        mRepeatType = "Hour";

        mDateText.setText(mDate);
        mTimeText.setText(mTime);


        if (note.isReminderFired()) {
            mRepeatSwitch.setChecked(true);
            mRepeatText.setText("Every " + note.getRecurrenceRule() + "(s)");
            mRepeatNoText.setText(note.getRecurrenceRule().substring(0, 1));
            mRepeatNo=note.getRecurrenceRule().substring(0, 1);
            mRepeatType = note.getRecurrenceRule().substring(1);
            mRepeatTypeText.setText(mRepeatType);
        } else {
            mRepeatSwitch.setChecked(false);
        }
        mRepeatSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = ((Switch) v).isChecked();
                if (on) {
                    mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                    note.setReminderFired(true);
                } else {
                    mRepeatText.setText("Repeat Off");
                    note.setReminderFired(false);
                }
            }
        });
        repeat_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRepeatSwitch.isChecked())
                    idCVRepeatSub.setVisibility(View.VISIBLE);
            }
        });
        idBtnAddRepeateType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idCVRepeatSub.setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.RepeatNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Enter number of every event");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                if (input.getText().toString().length() == 0) {
                                    mRepeatNo = Integer.toString(1);
                                    mRepeatNoText.setText(mRepeatNo);
                                    mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                                } else {
                                    mRepeatNo = input.getText().toString().trim();
                                    mRepeatNoText.setText(mRepeatNo);
                                    mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                                }
                            }
                        });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        });
        view.findViewById(R.id.date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dialogDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        mDay = dayOfMonth;
                        mMonth = month;
                        mYear = year;
                        mDate = dayOfMonth + " " + month + "," + year;
                        mDateText.setText(mDate);
                    }
                },/*
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH);
                ,*/mYear, mMonth, mDay);

                dialogDatePicker.getDatePicker().setMinDate(new Date().getTime());
                dialogDatePicker.show();
            }
        });
        view.findViewById(R.id.idBtnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onDateCustom(mDay, mMonth, mYear,
                        mHour, mMinute, Integer.parseInt(mRepeatNo), mRepeatType);

                dismiss();
            }
        });
        view.findViewById(R.id.idBtnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mHour = hourOfDay;
                                mMinute = minute;
                                if (minute < 10) {
                                    mTime = hourOfDay + ":" + "0" + minute;
                                } else {
                                    mTime = hourOfDay + ":" + minute;
                                }
                                mTimeText.setText(mTime);
                            }
                        },
                        mHour,
                        mMinute,/*
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),*/
                        false
                );
                tpd.show();
            }
        });
        view.findViewById(R.id.RepeatType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[5];

                items[0] = "Minute";
                items[1] = "Hour";
                items[2] = "Day";
                items[3] = "Week";
                items[4] = "Month";

                // Create List Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select Type");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        mRepeatType = items[item];
                        mRepeatTypeText.setText(mRepeatType);
                        mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view;
    }


    public interface Callback {

        void onDateCustom(int mDay, int mMonth, int mYear,
                          int mHour, int mMinute, int noOfEvent, String recRule);
    }
}
