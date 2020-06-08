package model;

public class CreateTeamDTO {

    String title;
    String description;
    String creator;

    public CreateTeamDTO(String title, String description, String creator) {
        this.title = title;
        this.description = description;
        this.creator = creator;
    }

    public CreateTeamDTO() {

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
