package eu.europa.ec.digit.leos.pilot.export.model.metadata;

public enum MetadataActionName {
    INSERT_DATA("InsertData");

    private String value;

    private MetadataActionName(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}