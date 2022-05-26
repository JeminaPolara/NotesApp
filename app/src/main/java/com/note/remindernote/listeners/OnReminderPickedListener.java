
package com.note.remindernote.listeners;


public interface OnReminderPickedListener {

  void onReminderPicked(long timeInMillis, long reminder);

  void onRecurrenceReminderPicked(String recurrenceRule);
}
