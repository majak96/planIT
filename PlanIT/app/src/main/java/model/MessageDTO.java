package model;

public class MessageDTO {
    private Long serverTeamId;
    private String message;
    private String sender;
    private Long createdAt;
    private Long messageId;

    public MessageDTO(Long serverTeamId, String message, String sender, Long createdAt, Long messageId) {
        this.serverTeamId = serverTeamId;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
        this.messageId = messageId;
    }

    public MessageDTO(Long serverTeamId, String message, String sender, Long createdAt) {
        this.serverTeamId = serverTeamId;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getServerTeamId() {
        return serverTeamId;
    }

    public void setServerTeamId(Long serverTeamId) {
        this.serverTeamId = serverTeamId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "serverTeamId=" + serverTeamId +
                ", message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
