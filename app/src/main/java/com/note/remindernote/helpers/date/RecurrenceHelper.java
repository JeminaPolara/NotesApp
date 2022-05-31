

package com.note.remindernote.helpers.date;

import com.note.remindernote.MyApp;
import com.note.remindernote.R;

public class RecurrenceHelper {
    public static String getNoteRecurrentReminderText(long reminder, String rrule) {
        return "Every " + rrule + " " + MyApp.getAppContext()
                .getString
                        (R.string.starting_from) + " " + DateHelper
                .getDateTimeShort(MyApp.getAppContext(), reminder);
    }

}
