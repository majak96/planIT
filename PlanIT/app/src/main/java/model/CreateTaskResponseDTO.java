package model;

import java.util.ArrayList;

public class CreateTaskResponseDTO {

    private Long globalId;
    private Long reminderId;
    private ArrayList<LabelDTO> labels;

    public CreateTaskResponseDTO() {
        // TODO Auto-generated constructor stub
    }

    public Long getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Long globalId) {
        this.globalId = globalId;
    }

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public ArrayList<LabelDTO> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<LabelDTO> labels) {
        this.labels = labels;
    }
}
