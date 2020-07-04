package model;

import java.util.Objects;

public class User {

    private Integer id;
    private String name;
    private String lastName;
    private String password;
    private String email;
    private String colour;
    private String firebaseId;
    private Long globalId;

    public User(String email) {
        this.email = email;
    }

    public User(Integer id, String email, String name, String lastName, String colour, String firebaseId) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.colour = colour;
        this.email = email;
        this.firebaseId = firebaseId;
        this.colour = colour;
    }

    public User(String name, String lastName, String password, String username, String firebaseId) {
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.email = username;
        this.firebaseId = firebaseId;
    }

    public User(String name, String lastName, String email, String firebaseId) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.firebaseId = firebaseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public Long getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Long globalId) {
        this.globalId = globalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastName, password, email, colour);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", colour='" + colour + '\'' +
                '}';
    }

}
