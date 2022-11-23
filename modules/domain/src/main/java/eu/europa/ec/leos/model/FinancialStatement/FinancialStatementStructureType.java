package eu.europa.ec.leos.model.FinancialStatement;

public enum FinancialStatementStructureType {

    LEVEL("level");

    private String type;

    FinancialStatementStructureType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
