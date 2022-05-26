

package com.note.remindernote.helpers.date;

import static com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker.RecurrenceOption.DOES_NOT_REPEAT;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.appeaser.sublimepickerlibrary.recurrencepicker.EventRecurrence;
import com.appeaser.sublimepickerlibrary.recurrencepicker.EventRecurrenceFormatter;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker.RecurrenceOption;
import com.note.remindernote.MyApp;
import com.note.remindernote.R;
import com.note.remindernote.models.Note;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.Recur.Frequency;
import net.fortuna.ical4j.model.property.RRule;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class RecurrenceHelper {

  private RecurrenceHelper() {
    // hides public constructor
  }

  public static String formatRecurrence(Context mContext, String recurrenceRule) {
    if (StringUtils.isEmpty(recurrenceRule)) {
      return "";
    }

    EventRecurrence recurrenceEvent = new EventRecurrence();
    recurrenceEvent.setStartDate(new Time("" + new Date().getTime()));
    recurrenceEvent.parse(recurrenceRule);
    return EventRecurrenceFormatter.getRepeatString(mContext.getApplicationContext(),
        mContext.getResources(), recurrenceEvent, true);
  }

  public static Long nextReminderFromRecurrenceRule(Note note) {
    if (!TextUtils.isEmpty(note.getRecurrenceRule()) && note.getAlarm() != null) {
      return nextReminderFromRecurrenceRule(Long.parseLong(note.getAlarm()),
          Calendar.getInstance().getTimeInMillis(), note.getRecurrenceRule());
    }
    return 0L;
  }

  public static Long nextReminderFromRecurrenceRule(long reminder, long currentTime,
      String recurrenceRule) {
    try {
      RRule rule = new RRule();
      rule.setValue(recurrenceRule);
      long startTimestamp = reminder + 60 * 1000;
      if (startTimestamp < currentTime) {
        startTimestamp = currentTime;
      }
      Date nextDate = rule.getRecur()
          .getNextDate(new DateTime(reminder), new DateTime(startTimestamp));
      return nextDate == null ? 0L : nextDate.getTime();
    } catch (ParseException e) {
      return 0L;
    }
  }

  public static String getNoteReminderText(long reminder) {
    return MyApp.getAppContext().getString(R.string.alarm_set_on) + " " + DateHelper
        .getDateTimeShort(MyApp
            .getAppContext(), reminder);
  }

  public static String getNoteRecurrentReminderText(long reminder, String rrule) {
    return "Every " + rrule + " " + MyApp.getAppContext()
        .getString
            (R.string.starting_from) + " " + DateHelper
        .getDateTimeShort(MyApp.getAppContext(), reminder);
  }

  public static String buildRecurrenceRuleByRecurrenceOptionAndRule(
      RecurrenceOption recurrenceOption,
      String recurrenceRule) {
    if (recurrenceRule == null && recurrenceOption != DOES_NOT_REPEAT) {
      Frequency freq = Frequency.valueOf(recurrenceOption.toString());
      Recur recur = new Recur(freq, new DateTime(32519731800000L));
      return new RRule(recur).toString().replace("RRULE:", "");
    }
    return recurrenceRule;
  }

}
