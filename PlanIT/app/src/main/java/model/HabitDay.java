package model;

public class HabitDay {
    private Integer id;
    private DayOfWeek day;

    public HabitDay() {
    }

    public HabitDay(Integer id, DayOfWeek day) {
        this.id = id;
        this.day = day;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }
}
