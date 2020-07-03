package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {

    private Integer id;
    private Long serverTeamId;
    private String name;
    private String description;
    private User teamCreator;
    private List<User> users;
    private boolean deleted;
    private String creatorEmail;
    private Long creatorId;

    public Team() {
        this.users = new ArrayList<>();
    }

    public Team(Integer id, String name, String description, User user, Long serverTeamId) {
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

    public Long getServerTeamId() {
        return serverTeamId;
    }

    public void setServerTeamId(Long serverTeamId) {
        this.serverTeamId = serverTeamId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}
