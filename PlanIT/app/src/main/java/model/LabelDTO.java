package model;

public class LabelDTO {

    private Integer id;
    private String name;
    private String color;
    private Integer globalId;
    private Long connectionId;

    public LabelDTO(Integer id, Integer globalId, String name, String color) {
        this.globalId = globalId;
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public Integer getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Integer globalId) {
        this.globalId = globalId;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
