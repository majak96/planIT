package model;

public class HabitDayConnection {
    private Long id;
    private String day;
    private Integer local_id;
    private Long habitId;
    private Integer habitDayId;

    private boolean deleted;

    public HabitDayConnection() {
    }

    public Integer getLocal_id() {
        return local_id;
    }

    public void setLocal_id(Integer local_id) {
        this.local_id = local_id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    public Integer getHabitDayId() {
        return habitDayId;
    }

    public void setHabitDayId(Integer habitDayId) {
        this.habitDayId = habitDayId;
    }

    public Long getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
