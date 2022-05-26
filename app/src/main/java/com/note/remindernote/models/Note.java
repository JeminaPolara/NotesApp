
package com.note.remindernote.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Note extends RealmObject implements Parcelable {

    @PrimaryKey
    private Long creation;
    private String title;
    private String content;
    private Long lastModification;

    private Long assignDate;
    private Long maxDate;


    private Boolean archived;
    private Boolean trashed;
    private String alarm;
    private Boolean reminderFired;
    private String recurrenceRule;
    private Double latitude;
    private Double longitude;
    private String address;
    private Boolean locked;
    private Boolean checklist;
    private long repeatTime;
    private long fromReminder;

    private Boolean isDone;
    private Boolean isChanged;
    private long completedTime;

    public Note(Parcel in) {

        setCreation(in.readString());
        setLastModification(in.readString());
        setTitle(in.readString());
        setContent(in.readString());
        setArchived(in.readInt());
        setTrashed(in.readInt());
        setAlarm(in.readString() != null ? Long.parseLong(in.readString()) : 0);

        setReminderFired(in.readInt());
        setRecurrenceRule(in.readString());
        setLatitude(in.readString());
        setLongitude(in.readString());
        setAddress(in.readString());
        setLocked(in.readInt());
        setChecklist(in.readInt());
        setRepeatTime(in.readInt());
        setFromReminder(Long.valueOf(in.readInt()));
        setAssignDate(Long.parseLong(in.readString()));
        setMaxDate(Long.parseLong(in.readString()));
        setCompletedTime(Long.parseLong(in.readString()));
    }


    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    public Boolean getChanged() {
        return isChanged;
    }

    public void setChanged(Boolean changed) {
        isChanged = changed;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(long completedTime) {
        this.completedTime = completedTime;
    }

    public long getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(long repeatTime) {
        this.repeatTime = repeatTime;
    }

    public long getFromReminder() {
        return fromReminder;
    }

    public void setFromReminder(long fromReminder) {
        this.fromReminder = fromReminder;
    }

    public Long getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Long assignDate) {
        this.assignDate = assignDate;
    }

    public Long getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Long maxDate) {
        this.maxDate = maxDate;
    }


    public void set_id(Long _id) {
        this.creation = _id;
    }


    public Long get_id() {
        return creation;
    }


    public String getTitle() {
        if (title == null) return "";
        return title;
    }


    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }


    public String getContent() {
        if (content == null) return "";
        return content;
    }


    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }


    public Long getCreation() {
        return creation;
    }


    public void setCreation(Long creation) {
        this.creation = creation;
    }


    public void setCreation(String creation) {
        Long creationLong;
        try {
            creationLong = Long.parseLong(creation);
        } catch (NumberFormatException e) {
            creationLong = null;
        }
        this.creation = creationLong;
    }


    public Long getLastModification() {
        return lastModification;
    }


    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }


    public void setLastModification(String lastModification) {
        Long lastModificationLong;
        try {
            lastModificationLong = Long.parseLong(lastModification);
        } catch (NumberFormatException e) {
            lastModificationLong = null;
        }
        this.lastModification = lastModificationLong;
    }


    public Boolean isArchived() {
        return !(archived == null || !archived);
    }


    public void setArchived(Boolean archived) {
        this.archived = archived;
    }


    public void setArchived(int archived) {
        this.archived = archived == 1;
    }


    public Boolean isTrashed() {
        return !(trashed == null || !trashed);
    }


    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }


    public void setTrashed(int trashed) {
        this.trashed = trashed == 1;
    }


    public String getAlarm() {
        return alarm;
    }


  /*public void setAlarm(String alarm) {
    this.alarm = alarm;
  }
*/

    public void setAlarm(long alarm) {
        this.alarm = String.valueOf(alarm);
    }


    public Boolean isReminderFired() {
        return !(reminderFired == null || !reminderFired);
    }


    public void setReminderFired(Boolean reminderFired) {
        this.reminderFired = reminderFired;
    }


    public void setReminderFired(int reminderFired) {
        this.reminderFired = reminderFired == 1;
    }


    public String getRecurrenceRule() {
        return recurrenceRule;
    }


    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }


    public Double getLatitude() {
        return latitude;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public void setLatitude(String latitude) {
        try {
            setLatitude(Double.parseDouble(latitude));
        } catch (NumberFormatException | NullPointerException e) {
            this.latitude = null;
        }
    }


    public Double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public void setLongitude(String longitude) {
        try {
            setLongitude(Double.parseDouble(longitude));
        } catch (NumberFormatException e) {
            this.longitude = null;
        } catch (NullPointerException e) {
            this.longitude = null;
        }
    }


    public Boolean isLocked() {
        return !(locked == null || !locked);
    }


    public void setLocked(Boolean locked) {
        this.locked = locked;
    }


    public void setLocked(int locked) {
        this.locked = locked == 1;
    }


    public Boolean isChecklist() {
        return !(checklist == null || !checklist);
    }


    public void setChecklist(Boolean checklist) {
        this.checklist = checklist;
    }


    public void setChecklist(int checklist) {
        this.checklist = checklist == 1;
    }


    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public boolean isChanged(Note baseNote) {
        return !equals(baseNote);
    }

    /**
     * after Base note code
     */

    public static final Creator<Note> CREATOR = new Creator<Note>() {

        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }


        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public Note() {
        this.title = "";
        this.content = "";
        this.archived = false;
        this.trashed = false;
        this.locked = false;
        this.checklist = false;
    }


    public Note(Long creation, Long lastModification, String title, String content, Integer archived,
                Integer trashed, String alarm, String recurrenceRule, Integer reminderFired, String latitude,
                String longitude,
                Integer locked, Integer checklist) {
        this.title = title;
        this.content = content;
        this.creation = creation;
        this.lastModification = lastModification;
        this.archived = archived == 1;
        this.trashed = trashed == 1;
        this.alarm = alarm;
        this.reminderFired = reminderFired == 1;
        this.recurrenceRule = recurrenceRule;
        setLatitude(latitude);
        setLongitude(longitude);
        this.locked = locked == 1;
        this.checklist = checklist == 1;
    }


    public Note(Note baseNote) {
        setFromReminder(baseNote.getFromReminder());
        setRepeatTime(baseNote.getRepeatTime());
        setAssignDate(baseNote.getAssignDate());
        setMaxDate(baseNote.getMaxDate());
        setTitle(baseNote.getTitle());
        setContent(baseNote.getContent());
        setCreation(baseNote.getCreation());
        setLastModification(baseNote.getLastModification());
        setArchived(baseNote.isArchived());
        setTrashed(baseNote.isTrashed());
        setAlarm(baseNote.getAlarm() != null ? Long.parseLong(baseNote.getAlarm()) : 0);
        setRecurrenceRule(baseNote.getRecurrenceRule());
        setReminderFired(baseNote.isReminderFired());
        setLatitude(baseNote.getLatitude());
        setLongitude(baseNote.getLongitude());
        setAddress(baseNote.getAddress());
        setLocked(baseNote.isLocked());
        setChecklist(baseNote.isChecklist());
        setCompletedTime(baseNote.getCompletedTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(String.valueOf(getCreation()));
        parcel.writeString(String.valueOf(getLastModification()));
        parcel.writeString(getTitle());
        parcel.writeString(getContent());
        parcel.writeInt(isArchived() ? 1 : 0);
        parcel.writeInt(isTrashed() ? 1 : 0);
        parcel.writeString(getAlarm());
        parcel.writeInt(isReminderFired() ? 1 : 0);
        parcel.writeString(getRecurrenceRule());
        parcel.writeString(String.valueOf(getLatitude()));
        parcel.writeString(String.valueOf(getLongitude()));
        parcel.writeString(getAddress());
        parcel.writeInt(isLocked() ? 1 : 0);
        parcel.writeInt(isChecklist() ? 1 : 0);
        parcel.writeInt((int) getCompletedTime());
    }

}
