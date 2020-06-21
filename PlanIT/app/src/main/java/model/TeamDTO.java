package model;

import java.util.ArrayList;
import java.util.List;

public class TeamDTO {

    private String title;
    private String description;
    private String creator;
    private List<String> members;

    public TeamDTO() {
        this.members = new ArrayList<>();
    }

    public TeamDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public TeamDTO(String title, String description, String creator, List<String> members) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.members = members;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
