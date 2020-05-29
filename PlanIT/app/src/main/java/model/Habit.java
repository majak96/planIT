package model;

import java.io.Serializable;

public class Habit implements Serializable {

    private String title;
    private String description;
    private Integer goal;
    private Integer numberOfDays;

    public Habit() {

    }

    public Habit(String title, String description, Integer goal, Integer numberOfDays) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.numberOfDays = numberOfDays;
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
}
