package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Team implements Serializable {

    private Long id;
    private String name;
    private String description;
    private User teamCreator;
    private List<User> users;

    public Team() {
        this.users = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", teamCreator=" + teamCreator +
                ", users=" + users +
                '}';
    }

    public Team(Long id, String name) {
        this.id = id;
        this.name = name;
        this.users = new ArrayList<>();
    }

    public Team(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.users = new ArrayList<>();
    }

    public Team(Long id, String name, String description, User user) {
        this.id = id;
        this.users = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.teamCreator = user;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
