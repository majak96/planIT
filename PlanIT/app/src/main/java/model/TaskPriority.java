package model;

public enum TaskPriority {
    LOW("!"),
    MEDIUM("!!"),
    HIGH("!!!");

    private String label;

    private TaskPriority(String label) {
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
