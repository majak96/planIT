package com.example.planit.mokaps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Label;
import model.Message;
import model.Task;
import model.TaskPriority;
import model.Team;
import model.User;

public class Mokap {

    private static final String TAG = "Mokap";

    public static List<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<Message>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "vesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        Message m1 = new Message("Let's have a team meeting tomorrow?", u2, 1320917972);
        Message m2 = new Message("I can't make it tomorrow. How about friday?", u1, 11122211);
        Message m3 = new Message("Friday works for me!", u3, 11122211);
        Message m4 = new Message("Great! See you on friday!", u2, 11122211);

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);
        messages.add(m4);

        return messages;
    }

    public static List<User> getUsers() {
        ArrayList<User> users = new ArrayList<User>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "vesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");
        User u4 = new User("Marijana", "Matkovski", "majam", "matkovskim@gmail.com");
        User u5 = new User("IsaProjekat", "Mail", "isa.22", "timisaprojekat@gmail.com");

        users.add(u1);
        users.add(u2);
        users.add(u3);
        users.add(u4);
        users.add(u5);

        return users;
    }

    public static List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "vesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        //Team team1 = new Team(1L, "DSL Tim 3", "Jezici specifični za domen", u3);
     //   Team team2 = new Team(2L, "PMA Tim 3", "Programiranje mobilnih aplikacija",  u1);

       // teams.add(team1);
       // teams.add(team2);

        return teams;
    }

    public static Team getTeam(Long id) {

        Team team;
        User u1 = new User("Vesna", "Milic", "vesna.22", "vesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        switch (id.intValue()) {
            case 1:
             //   team = new Team(1L, "DSL Tim 3", "Jezici specifični za domen", u3);
                List<User>users=new ArrayList<>();
                users.add(u1);
                users.add(u2);
             //   team.setUsers(users);
                break;
            case 2:
              //  team = new Team(2L, "PMA Tim 3", "Programiranje mobilnih aplikacija", u1);
                break;
            default:
              //  team = new Team(3L, "UKS Tim 3", "Upravljanje konfiguracijom softvera!", u3);
                break;
        }

        //return team;
        return null;
    }

}
