package model;

public class Message {
    Integer id;
    String message;
    User sender;
    Long createdAt;

    public Message(Integer id, String message, User sender, Long createdAt) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public Message(String message, User sender, Long createdAt) {
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
