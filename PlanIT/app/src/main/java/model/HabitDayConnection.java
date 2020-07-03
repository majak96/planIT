package model;

public class HabitDayConnection {
    private Integer id;
    private Integer habitId;
    private Integer habitDayId;

    public HabitDayConnection() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHabitId() {
        return habitId;
    }

    public void setHabitId(Integer habitId) {
        this.habitId = habitId;
    }

    public Integer getHabitDayId() {
        return habitDayId;
    }

    public void setHabitDayId(Integer habitDayId) {
        this.habitDayId = habitDayId;
    }
}
