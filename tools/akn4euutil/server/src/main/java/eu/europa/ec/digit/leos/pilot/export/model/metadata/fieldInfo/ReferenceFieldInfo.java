package eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo;

import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataFieldType;

public class ReferenceFieldInfo extends MetadataFieldInfo {
    private final String id;
    private final String href;
    private final String displayValue;
    private final String shortValue;

    public ReferenceFieldInfo(final String id,
                              final String href,
                              final String displayValue,
                              final String shortValue,
                              final MetadataFieldType fieldType){
        super(fieldType);
        this.id = id;
        this.href = href;
        this.displayValue = displayValue;
        this.shortValue = shortValue;
    }

    public String getId(){ return this.id; }

    public String getHref(){ return this.href; }

    public String getDisplayValue(){ return this.displayValue; }

    public String getShortValue(){ return this.shortValue; }

    @Override
    public String toString(){
        return String.format("ReferenceFieldInfo(id: %s / href: %s / displayValue: %s / shortValue: %s / fieldType: %s)",
                this.id, this.href, this.displayValue, this.shortValue, this.fieldType.toString());
    }
}