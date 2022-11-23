package eu.europa.ec.digit.leos.pilot.export.model.metadata;

public enum MetadataValidationResultKey {
    XML_VALIDATION_CHECK("XMLValidationCheck");

    private String key;

    private MetadataValidationResultKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}