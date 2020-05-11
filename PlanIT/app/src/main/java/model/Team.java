package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Team implements Serializable {

    private String name;
    private String description;
    private String URLShare;
    private User teamCreator;
    private List<User> users;

    public Team() {
        this.users = new ArrayList<>();
    }

    public Team(String name, String description, String URLShare, User user) {
        this.users = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.URLShare = URLShare;
        this.teamCreator= user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getURLShare() {
        return URLShare;
    }

    public void setURLShare(String URLShare) {
        this.URLShare = URLShare;
    }

    public User getTeamCreator() {
        return teamCreator;
    }

    public void setTeamCreator(User teamCreator) {
        this.teamCreator = teamCreator;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
