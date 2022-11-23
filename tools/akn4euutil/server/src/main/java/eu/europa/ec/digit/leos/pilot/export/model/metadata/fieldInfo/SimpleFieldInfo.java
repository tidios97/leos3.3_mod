package eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo;

import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataFieldType;

public class SimpleFieldInfo extends MetadataFieldInfo {
    private final String value;

    public SimpleFieldInfo(final String value,
                           final MetadataFieldType fieldType) {
        super(fieldType);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.format("SimpleFieldInfo(value: %s / fieldType: %s)", this.value, this.fieldType.toString());
    }
}