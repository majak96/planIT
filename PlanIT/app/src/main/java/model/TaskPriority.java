package model;

public enum TaskPriority {
    LOW("!", "Low"),
    MEDIUM("!!", "Medium"),
    HIGH("!!!", "High");

    private String label;
    private String symbol;

    private TaskPriority(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public String getLabel(){
        return this.label;
    }

}
