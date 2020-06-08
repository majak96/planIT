package model;

import com.google.gson.annotations.SerializedName;

public class ChangeProfileDTO {
    @SerializedName("email")
    private String email;

    @SerializedName("firstName")
    private String firstName;

    public ChangeProfileDTO(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @SerializedName("lastName")
    private String lastName;

    ChangeProfileDTO() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
