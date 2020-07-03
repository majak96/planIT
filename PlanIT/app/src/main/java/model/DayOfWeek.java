package model;

public enum DayOfWeek {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int value;

    DayOfWeek(int value) {
        this.value = value;
    }

    public static DayOfWeek valueOf(int value) {

        for (int i = 0; i < values().length; i++) {
            if (values()[i].value == value)
                return values()[i];
        }

        return null;
    }

    }
