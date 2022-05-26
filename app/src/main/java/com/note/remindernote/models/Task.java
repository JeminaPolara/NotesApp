package com.note.remindernote.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject {
    @PrimaryKey
    private Long taskid;
    private Long creation;
    private int flag = 0;
    private String taskDetail="";


    public Task() {

    }

    public Task(int taskid,Long creation, int flag,String taskDetail) {
        this.taskid = Long.valueOf(taskid);
        this.creation = creation;
        this.flag = flag;
        this.taskDetail = taskDetail;
    }
    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }

    public Long getTaskid() {
        return taskid;
    }

    public void setTaskid(Long taskid) {
        this.taskid = taskid;
    }

    public Long getCreation() {
        return creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
