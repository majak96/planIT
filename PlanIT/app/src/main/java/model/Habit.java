package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Habit implements Serializable {

    private Integer id;
    private String title;
    private String description;
    private Integer goal;
    private Integer numberOfDays;
    private Integer totalNumberOfDays;
    private List<HabitDayConnection> habitDays;

    public Habit() {
        this.habitDays = new ArrayList<>();
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

    public Integer getTotalNumberOfDays() {
        return totalNumberOfDays;
    }

    public void setTotalNumberOfDays(Integer totalNumberOfDays) {this.totalNumberOfDays = totalNumberOfDays;}

    public List<HabitDayConnection> getHabitDays() {
        return habitDays;
    }

    public void setHabitDays(List<HabitDayConnection> habitDays) {
        this.habitDays = habitDays;
    }
}
