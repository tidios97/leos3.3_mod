package eu.europa.ec.leos.annotate.model.web.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.europa.ec.leos.annotate.Generated;

/**
 * Class representing the simple structure transmitted as response in case of successful treatment of annotations 
 */
@JsonIgnoreProperties(ignoreUnknown = true) // required to avoid deserialisation failures for constant field 'deleted'
public class JsonBulkTreatSuccessResponse {

    private static final boolean treated = true;

    // -------------------------------------
    // Constructor
    // -------------------------------------

    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public JsonBulkTreatSuccessResponse() {
        // default constructor required for deserialisation
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getTreated() {
        return treated;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(treated);
    }

    @Generated
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final JsonBulkTreatSuccessResponse other = (JsonBulkTreatSuccessResponse) obj;
        return Objects.equals(this, other); // static field left out
    }
}
