package eu.europa.ec.digit.leos.pilot.export.model.metadata;

public class MetadataXmlValidationError {
    public final String node;
    public final String taskId;
    public final String reason;
    public final MetadataFieldType fieldType;

    public MetadataXmlValidationError(String node,
                                      String taskId,
                                      String reason,
                                      MetadataFieldType fieldType) {
        this.node = node;
        this.taskId = taskId;
        this.reason = reason;
        this.fieldType = fieldType;
    }

    public Boolean isFielNodeError() {
        return (this.node != null) && !this.node.equals("field");
    }

    public String toString() {
        return String.format("MetadataXmlValidationError(node: %s / taskId: %s / fieldType: %s / reason: %s )",
                this.node, this.taskId, this.fieldType, this.reason);
    }

    public static MetadataXmlValidationError newNodeError(String node, String taskId, String reason) {
        return new MetadataXmlValidationError(node, taskId, reason, null);
    }

    public static MetadataXmlValidationError newFieldNodeError(MetadataFieldType fieldType, String taskId, String reason) {
        return new MetadataXmlValidationError("field", taskId, reason, fieldType);
    }

    public static MetadataXmlValidationError getNodeIsMissingError(String node) {
        return newNodeError(node, null, String.format("Node '%s' is missing", node));
    }

    public static MetadataXmlValidationError getNodeIsMissingInError(String node, String missingIn, String taskId) {
        return newNodeError(node, taskId, String.format("Node '%s' is missing in %s", node, missingIn));
    }
}