package com.example.planit.mokaps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Label;
import model.Task;
import model.TaskPriority;
import model.Team;
import model.User;
import model.Message;

public class Mokap {

    private static final String TAG = "Mokap";

    public static List<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<Message>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        Message m1 = new Message("Konacno smo polozile PMA", u1, 1320917972);
        Message m2 = new Message("I JSD", u2, 11122211);
        Message m3 = new Message("JEEEJ", u3, 11122211);

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);

        return messages;
    }

    public static List<User> getUsers() {
        ArrayList<User> users = new ArrayList<User>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
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

    public static List<Task> getTasks() {
        List<Task> tasks = new ArrayList<Task>();

        Date date = new Date();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 16);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 20);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        Task task1 = new Task(1L, "buy milk", "", date, null, true);
        Task task2 = new Task(2L, "jogging", "", date, null, false);
        Task task3 = new Task(3L, "meeting", "", date, cal1.getTime(), false);
        Task task4 = new Task(4L, "dinner with Vesna", "", date, cal2.getTime(), false);

        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        tasks.add(task4);

        return tasks;
    }

    public static Task getTask(Long id) {
        Date date = new Date();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 16);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 20);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        Task task;
        switch (id.intValue()) {
            case 1:
                task = new Task(1L, "buy milk", null, date, null, true);
                task.getLabels().add(new Label("shop", "#99ff66"));
                task.setPriority(TaskPriority.MEDIUM);
                break;
            case 2:
                task = new Task(2L, "jogging", "5km", date, null, false);
                task.getLabels().add(new Label("exercising", "#6699ff"));
                task.setPriority(TaskPriority.LOW);
                break;
            case 3:
                task = new Task(3L, "meeting", null, date, cal1.getTime(), false);
                task.setPriority(TaskPriority.HIGH);
                break;
            case 4:
                task = new Task(4L, "dinner with Vesna", null, date, cal2.getTime(), false);
                task.setPriority(TaskPriority.HIGH);
                task.getLabels().add(new Label("label 1", "#99b4f4"));
                task.getLabels().add(new Label("label 2", "#f3b1e9"));
                task.setAddress("Bulevar Despota Stefana 5a, Novi Sad");
                break;
            default:
                task = new Task(0L, "Example task", "This is an example description.", date, null, true);
                task.getLabels().add(new Label("label 1", "#99b4f4"));
                task.getLabels().add(new Label("label 2", "#f3b1e9"));
                task.setPriority(TaskPriority.HIGH);
                break;
        }

        return task;
    }

    public static List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        Team team1 = new Team(1L, "DSL Tim 3", "Jezici specifični za domen", "http://nekiLink.com", u3);
        Team team2 = new Team(2L, "PMA Tim 3", "Programiranje mobilnih aplikacija", "http://nekiLink2.com", u1);

        teams.add(team1);
        teams.add(team2);

        return teams;
    }

    public static Team getTeam(Long id) {

        Team team;
        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        switch (id.intValue()) {
            case 1:
                team = new Team(1L, "DSL Tim 3", "Jezici specifični za domen", "http://nekiLink.com", u3);
                List<User>users=new ArrayList<>();
                users.add(u1);
                users.add(u2);
                team.setUsers(users);
                break;
            case 2:
                team = new Team(2L, "PMA Tim 3", "Programiranje mobilnih aplikacija", "http://nekiLink2.com", u1);
                break;
            default:
                team = new Team(3L, "UKS Tim 3", "Upravljanje konfiguracijom softvera!", "http://nekiLink.com", u3);
                break;
        }

        return team;
    }

}
