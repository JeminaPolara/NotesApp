
package com.note.remindernote.helpers.date;

import static com.note.remindernote.utils.ConstantsBase.DATE_FORMAT_SORTABLE;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.note.remindernote.MyApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Helper per la generazione di date nel formato specificato nelle costanti
 */
public class DateHelper {

  private DateHelper() {
    // hides public constructor
  }

  public static String getSortableDate() {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SORTABLE);
    return sdf.format(Calendar.getInstance().getTime());
  }


  /**
   * Build a formatted date string starting from values obtained by a DatePicker
   */
  public static String onDateSet(int year, int month, int day, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, day);
    return sdf.format(cal.getTime());
  }


  /**
   * Build a formatted time string starting from values obtained by a TimePicker
   */
  public static String onTimeSet(int hour, int minute, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    return sdf.format(cal.getTime());
  }

  /**
   *
   */
  public static String getDateTimeShort(Context mContext, Long date) {
    int flags = DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY
        | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE;
    return (date == null) ? "" : DateUtils.formatDateTime(mContext, date, flags)
        + " " + DateUtils.formatDateTime(mContext, date, DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   *
   */
  public static String getTimeShort(Context mContext, Long time) {
    if (time == null) {
      return "";
    }
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    return DateUtils.formatDateTime(mContext, time, DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   *
   */
  public static String getTimeShort(Context mContext, int hourOfDay, int minute) {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
    c.set(Calendar.MINUTE, minute);
    return DateUtils.formatDateTime(mContext, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   * Formats a short time period (minutes)
   */
  public static String formatShortTime(Context mContext, long time) {
    String m = String.valueOf(time / 1000 / 60);
    String s = String.format("%02d", (time / 1000) % 60);
    return m + ":" + s;
  }


  public static String getFormattedDate(Long timestamp, boolean prettified) {
    if (prettified) {
      return com.note.remindernote.utils.date.DateUtils.prettyTime(timestamp);
    } else {
      return DateHelper.getDateTimeShort(MyApp.getAppContext(), timestamp);
    }
  }
  public String getFormattedDate(long smsTimeInMilis) {
    Calendar smsTime = Calendar.getInstance();
    smsTime.setTimeInMillis(smsTimeInMilis);

    Calendar now = Calendar.getInstance();

    final String timeFormatString = "h:mm aa";
    final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
    final long HOURS = 60 * 60 * 60;
    if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
      return "Today " + DateFormat.format(timeFormatString, smsTime);
    } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
      return "Yesterday " + DateFormat.format(timeFormatString, smsTime);
    } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
      return DateFormat.format(dateTimeFormatString, smsTime).toString();
    } else {
      return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
    }
  }
}
