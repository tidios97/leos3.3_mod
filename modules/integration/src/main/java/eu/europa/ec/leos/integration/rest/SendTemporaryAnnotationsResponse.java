package eu.europa.ec.leos.integration.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SendTemporaryAnnotationsResponse {
    private final String createdId;

    @JsonCreator
    public SendTemporaryAnnotationsResponse(@JsonProperty("createdId") final String createdId) {
        this.createdId = createdId;
    }

    public String getCreatedId() {
        return this.createdId;
    }
}
