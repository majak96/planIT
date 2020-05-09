package model;

import java.sql.Time;
import java.util.Date;

public class Task {
    private String title;
    private String description;
    private Date startDateAndTime;
    private Boolean done;

    public Task(String title, String description, Date startDateAndTime, Boolean done) {
        this.title = title;
        this.description = description;
        this.startDateAndTime = startDateAndTime;
        this.done = done;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDateAndTime() {
        return startDateAndTime;
    }

    public void setStartDateAndTime(Date startDateAndTime) {
        this.startDateAndTime = startDateAndTime;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
