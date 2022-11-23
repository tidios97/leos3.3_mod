package eu.europa.ec.leos.integration.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnnotateStatusResponse {
    private final String status;

    @JsonCreator
    public AnnotateStatusResponse(@JsonProperty("status") final String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
