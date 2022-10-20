package eu.europa.ec.leos.model.explanatory;

public enum ExplanatoryStructureType {

    LEVEL("level");

    private String type;

    ExplanatoryStructureType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
