package model;

import com.google.gson.annotations.SerializedName;

public class UserInfoDTO {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("name")
    private String name;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("colour")
    private String colour;

    @SerializedName("firebaseId")
    private String firebaseId;

    public UserInfoDTO() {

    }

    public UserInfoDTO(String email, String password, String name, String lastName, String colour, String firebaseId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.colour = colour;
        this.firebaseId = firebaseId;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
