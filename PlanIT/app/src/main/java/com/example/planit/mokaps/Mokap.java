package com.example.planit.mokaps;

import java.util.ArrayList;
import java.util.List;

import model.User;
import model.Message;

public class Mokap {

    public static List<Message> getMessages(){
        ArrayList<Message> messages = new ArrayList<Message>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        Message m1 = new Message("Konacno smo polozile PMA", u1, 1320917972 );
        Message m2 = new Message("I JSD", u2, 11122211);
        Message m3 = new Message("JEEEJ", u3, 11122211);

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);

        return messages;
    }

    public static List<User> getUsers(){
        ArrayList<User> users = new ArrayList<User>();

        User u1 = new User("Vesna", "Milic", "vesna.22", "wesna@gmail.com");
        User u2 = new User("Marijana", "Kolosnjaji", "majak", "majak@gmail.com");
        User u3 = new User("Marijana", "Matkovski", "majam", "majam@gmail.com");

        users.add(u1);
        users.add(u2);
        users.add(u3);

        return users;
    }

}
