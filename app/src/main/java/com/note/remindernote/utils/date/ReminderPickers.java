

package com.note.remindernote.utils.date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.note.remindernote.AlarmReceiver;
import com.note.remindernote.SublimePickerFragment;
import com.note.remindernote.models.Note;
import com.note.remindernote.listeners.OnReminderPickedListener;

import java.util.Calendar;


public class ReminderPickers {

    private FragmentActivity mActivity;
    private OnReminderPickedListener mOnReminderPickedListener;
    public static Calendar calendar;

    public ReminderPickers(FragmentActivity mActivity,
                           OnReminderPickedListener mOnReminderPickedListener) {
        this.mActivity = mActivity;
        this.mOnReminderPickedListener = mOnReminderPickedListener;
    }

    public void pick(Long presetDateTime, String recurrenceRule, Note note) {
        showDateTimeSelectors(DateUtils.getCalendar(presetDateTime), recurrenceRule, note);
//    mOnReminderPickedListener.onReminderPicked(presetDateTime);
    }


    /**
     * Show date and time pickers
     */
    private void showDateTimeSelectors(Calendar reminder, String recurrenceRule, Note note) {

        SublimePickerFragment pickerFrag = new SublimePickerFragment(mActivity, note);

        calendar = reminder;
        pickerFrag.setCallback(new SublimePickerFragment.Callback() {

            @Override
            public void onDateCustom(int mDay, int mMonth, int mYear, int mHour, int mMinute, int noOfEvent, String recRule) {
                long mRepeatTime = 0;
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.set(Calendar.MONTH, --mMonth);
                mCalendar.set(Calendar.YEAR, mYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
                if (note.isReminderFired()) {
                    // Constant values in milliseconds
                    long milMinute = 60000L;
                    long milHour = 3600000L;
                    long milDay = 86400000L;
                    long milWeek = 604800000L;
                    long milMonth = 2592000000L;

                    mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
                    mCalendar.set(Calendar.MINUTE, mMinute);
                    mCalendar.set(Calendar.SECOND, 0);

                    switch (recRule) {
                        case "Minute":
                            mRepeatTime = noOfEvent * milMinute;
                            break;
                        case "Hour":
                            mRepeatTime = noOfEvent * milHour;
                            break;
                        case "Day":
                            mRepeatTime = noOfEvent * milDay;
                            break;
                        case "Week":
                            mRepeatTime = noOfEvent * milWeek;
                            break;
                        case "Month":
                            mRepeatTime = noOfEvent * milMonth;
                            break;
                    }
                    note.setFromReminder(mCalendar.getTimeInMillis());
                    note.setAlarm(mRepeatTime);
                    note.setRepeatTime(mCalendar.getTimeInMillis() + mRepeatTime);
                    note.setLastModification(System.currentTimeMillis());


                    Intent notificationIntent = new Intent(mActivity, AlarmReceiver.class);
                    notificationIntent.setAction("android.intent.action.EVENT_REMINDER");
                    notificationIntent.putExtra("noteId", note.get_id());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity, note.get_id().intValue(), notificationIntent, 0);
                    AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, note.getRepeatTime(), pendingIntent);
                }
                mOnReminderPickedListener.onReminderPicked(mCalendar.getTimeInMillis(), mRepeatTime);
                mOnReminderPickedListener.onRecurrenceReminderPicked(noOfEvent + recRule);
            }
        });


        pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(mActivity.getSupportFragmentManager(), "SUBLIME_PICKER");
    }

}
