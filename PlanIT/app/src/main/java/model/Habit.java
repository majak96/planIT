package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Habit implements Serializable {

    private Long id;
    private Integer localId;
    private String title;
    private String description;
    private Integer goal;
    private Integer numberOfDays;
    private Integer totalNumberOfDays;
    private Boolean deleted;
    private List<HabitDayConnection> habitDays;

    public Habit() {
        this.habitDays = new ArrayList<>();
    }

    public Integer getLocalId() {
        return localId;
    }

    public void setLocalId(Integer localId) {
        this.localId = localId;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
