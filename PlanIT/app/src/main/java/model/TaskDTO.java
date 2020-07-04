package model;

import java.util.ArrayList;

public class TaskDTO {

    private String title;
    private String startDate;
    private Long id;
    private String description;
    private String address;
    private Double longitude;
    private Double latitude;
    private String startTime;
    private Boolean done;
    private TaskPriority priority;
    private Long teamId;
    private String reminderTime;
    private String userEmail;

    private ArrayList<LabelDTO> labels;

    public ArrayList<LabelDTO> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<LabelDTO> labels) {
        this.labels = labels;
    }

    public TaskDTO() {
        labels = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "title='" + title + '\'' +
                ", startDate=" + startDate +
                ", id=" + id +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", startTime=" + startTime +
                ", done=" + done +
                ", priority=" + priority +
                ", teamId=" + teamId +
                ", reminderTime='" + reminderTime + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
