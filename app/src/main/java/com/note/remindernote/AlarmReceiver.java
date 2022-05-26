package com.note.remindernote;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.note.remindernote.models.Note;
import com.note.remindernote.models.Task;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class AlarmReceiver extends BroadcastReceiver {

    private static int NOTIFICATION_ID;

    @Override
    public void onReceive(Context context, Intent intent) {

        Realm realm = Realm.getDefaultInstance();

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            RealmResults<Note> remindersNote = realm.where(Note.class).equalTo("reminderFired", true).findAll();
            for (Note note : remindersNote) {
                setNotificationReminder(note, realm, context);
            }
            return;
        }
        String noteId = intent.getSerializableExtra("noteId").toString();
        Note note = realm.where(Note.class).equalTo(Utils.NOTEID, Long.parseLong(noteId)).findFirst();

        setNotificationReminder(note, realm, context);
    }


    public void setNotificationReminder(Note note, Realm realm, Context context) {
        if (note != null && System.currentTimeMillis() <= note.getMaxDate()) {
            if (!note.getAlarm().equals("0")) {
                realm.beginTransaction();
                note.setRepeatTime(System.currentTimeMillis() + Long.parseLong(note.getAlarm()));
                realm.commitTransaction();
            }
            fireNotification(realm, note, context);
            if (!note.getAlarm().equals("0")) {
                Intent notificationIntent = new Intent(context, AlarmReceiver.class);
                notificationIntent.setAction("android.intent.action.EVENT_REMINDER");
                notificationIntent.putExtra("noteId", note.get_id());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, note.get_id().intValue(), notificationIntent, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, note.getRepeatTime(), pendingIntent);
                realm.beginTransaction();

                realm.insertOrUpdate(note);
                realm.commitTransaction();
            }

        }

    }

    public void fireNotification(Realm realm, Note note, Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NOTIFICATION_ID = note.get_id().intValue();
        Intent action = new Intent(context, MainActivity.class);
        action.setData(Uri.parse(note.getContent()));
        PendingIntent operation = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_ONE_SHOT);

        StringBuilder contentText = null;

        List<Task> taskList = realm.where(Task.class)
                .equalTo(Utils.NOTEID, note.get_id())
                .findAll();


        contentText = new StringBuilder();
        for (Task task : taskList) {
            if (task.getFlag() == 0) {
                if (note.isChecklist())
                    contentText.append("\u2022" + " ");
                contentText.append(task.getTaskDetail());
                contentText.append("\n");
            }
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(NOTIFICATION_ID));
        mBuilder.setContentTitle(note.getTitle());
        mBuilder.setContentIntent(operation);
        mBuilder.setContentText(contentText.toString().equals("") ? "All task done..." : contentText.toString());
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        mBuilder.setAutoCancel(true);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new
                    NotificationChannel(String.valueOf(NOTIFICATION_ID), "NOTES", importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mBuilder.setChannelId(String.valueOf(NOTIFICATION_ID));
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}