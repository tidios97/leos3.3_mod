package eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo;

import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataFieldType;

public class MetadataFieldInfo {
    protected final MetadataFieldType fieldType;

    public MetadataFieldInfo(final MetadataFieldType fieldType){
        this.fieldType = fieldType;
    }

    public MetadataFieldType getFieldType() { return this.fieldType; }

    @Override
    public String toString(){
        return String.format("MetadataFieldInfo(fieldType: %s)", this.fieldType.toString());
    }
}