package model;

import java.util.List;

public class HabitSyncDTO {
	
	private List<Habit> habits;
	
	private List<HabitFulfillment> habitFulfillments;
	
	private List<HabitDayConnection> habitDayConnection;
	
	private List<HabitReminderConnection> habitReminderConnections;
	
	private List<Reminder> reminderConn;

	public HabitSyncDTO() {
		// TODO Auto-generated constructor stub
	}

	public List<Habit> getHabits() {
		return habits;
	}

	public void setHabits(List<Habit> habits) {
		this.habits = habits;
	}

	public List<HabitFulfillment> getHabitFulfillment() {
		return habitFulfillments;
	}

	public void setHabitFulfillment(List<HabitFulfillment> habitFulfillment) {
		this.habitFulfillments = habitFulfillment;
	}

	public List<HabitDayConnection> getHabitDayConnection() {
		return habitDayConnection;
	}

	public void setHabitDayConnection(List<HabitDayConnection> habitDayConnection) {
		this.habitDayConnection = habitDayConnection;
	}

	public List<HabitReminderConnection> getHabitReminderConnections() {
		return habitReminderConnections;
	}

	public void setHabitReminderConnections(List<HabitReminderConnection> habitReminderConnections) {
		this.habitReminderConnections = habitReminderConnections;
	}

	public List<Reminder> getReminderConn() {
		return reminderConn;
	}

	public void setReminderConn(List<Reminder> reminderConn) {
		this.reminderConn = reminderConn;
	}

}
