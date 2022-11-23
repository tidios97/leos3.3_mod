package eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo;

import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataFieldType;

import java.util.List;

public class MultipleReferencesFieldInfo extends MetadataFieldInfo {
    private final List<ReferenceFieldInfo> references;

    public MultipleReferencesFieldInfo(final List<ReferenceFieldInfo> references,
                                       final MetadataFieldType fieldType){
        super(fieldType);
        this.references = references;
    }

    public List<ReferenceFieldInfo> getReferences() {
        return this.references;
    }
}