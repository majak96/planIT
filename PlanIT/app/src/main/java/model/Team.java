package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {

    private Integer id;
    private Integer serverTeamId;
    private String name;
    private String description;
    private User teamCreator;
    private List<User> users;

    public Team() {
        this.users = new ArrayList<>();
    }

    public Team(Integer id, String name, String description, User user, Integer serverTeamId) {
        this.id = id;
        this.users = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.teamCreator = user;
        this.serverTeamId = serverTeamId;
    }

    public Team(Integer id, String name, String description) {
        this.id = id;
        this.users = new ArrayList<>();
        this.name = name;
        this.description = description;
    }

    public Team(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.users = new ArrayList<>();
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getServerTeamId() {
        return serverTeamId;
    }

    public void setServerTeamId(Integer serverTeamId) {
        this.serverTeamId = serverTeamId;
    }
}
