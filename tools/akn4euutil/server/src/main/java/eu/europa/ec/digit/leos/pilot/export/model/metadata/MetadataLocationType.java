package eu.europa.ec.digit.leos.pilot.export.model.metadata;

import java.lang.IllegalArgumentException;

public enum MetadataLocationType {
    BRUSSELS("BRUSSELS"),
    LUXEMBOURG("LUXEMBOURG"),
    STRASBOURG("STRASBOURG");

    private final String location;

    private MetadataLocationType(String location){
        this.location = location;
    }

    private String getLocation(){
        return this.location;
    }

    @Override
    public String toString(){
        return this.location;
    }

    public static MetadataLocationType valueOfLocation(String location) throws IllegalArgumentException {
        if (MetadataLocationType.BRUSSELS.getLocation().equals(location)){
            return MetadataLocationType.BRUSSELS;
        }
        if (MetadataLocationType.LUXEMBOURG.getLocation().equals(location)){
            return MetadataLocationType.LUXEMBOURG;
        }
        if (MetadataLocationType.STRASBOURG.getLocation().equals(location)){
            return MetadataLocationType.STRASBOURG;
        }

        throw new IllegalArgumentException("Invalid metadata location type");
    }
}