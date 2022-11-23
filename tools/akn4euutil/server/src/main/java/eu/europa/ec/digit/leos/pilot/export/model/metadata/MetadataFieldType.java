package eu.europa.ec.digit.leos.pilot.export.model.metadata;

import java.lang.IllegalArgumentException;

public enum MetadataFieldType {
    ADOPTION_LOCATION("adoptionLocation"),
    EMISSION_DATE("emissionDate"),
    INTERINSTITUTIONAL_COTE("interinstitutionalCote"),
    INSERT_COTE("insertCote"),
    LINKED_DOCUMENTS("linkedDocuments");

    private final String typeName;

    private MetadataFieldType(String typeName){
        this.typeName = typeName;
    }

    private String getTypeName(){
        return this.typeName;
    }

    @Override
    public String toString(){
        return this.typeName;
    }

    public static MetadataFieldType valueOfTypeName(String typeName) throws IllegalArgumentException {
        if (MetadataFieldType.ADOPTION_LOCATION.getTypeName().equals(typeName)){
            return MetadataFieldType.ADOPTION_LOCATION;
        }
        if (MetadataFieldType.EMISSION_DATE.getTypeName().equals(typeName)){
            return MetadataFieldType.EMISSION_DATE;
        }
        if (MetadataFieldType.INTERINSTITUTIONAL_COTE.getTypeName().equals(typeName)){
            return MetadataFieldType.INTERINSTITUTIONAL_COTE;
        }
        if (MetadataFieldType.INSERT_COTE.getTypeName().equals(typeName)){
            return MetadataFieldType.INSERT_COTE;
        }
        if (MetadataFieldType.LINKED_DOCUMENTS.getTypeName().equals(typeName)){
            return MetadataFieldType.LINKED_DOCUMENTS;
        }
        throw new IllegalArgumentException("Invalid metadata type name");
    }
}