package model;

import java.io.Serializable;
import java.sql.Time;

public class Habit implements Serializable {

    private Integer id;
    private String title;
    private String description;
    private Integer goal;
    private Integer numberOfDays;
    private Time reminder;
    private Integer totalNumberOfDays;

    public Habit() {

    }

    public Habit(Integer id, String title, String description, Integer goal, Integer numberOfDays) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.numberOfDays = numberOfDays;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Time getReminder() { return reminder; }

    public void setReminder(Time reminder) { this.reminder = reminder; }

    public Integer getTotalNumberOfDays() { return totalNumberOfDays; }

    public void setTotalNumberOfDays(Integer totalNumberOfDays) { this.totalNumberOfDays = totalNumberOfDays; }
}
