package model;

import java.util.List;
import java.util.Set;

public class TeamSyncDTO {

	private List<Team> teams;

	private List<TeamUserConnection> teamUserConnections;

	private List<Message> messages;

	private Set<User> users;

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public List<TeamUserConnection> getTeamUserConnections() {
		return teamUserConnections;
	}

	public void setTeamUserConnections(List<TeamUserConnection> teamUserConnections) {
		this.teamUserConnections = teamUserConnections;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
}
