package eu.europa.ec.leos.annotate.model.web.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.europa.ec.leos.annotate.Generated;

/**
 * Class representing the simple structure transmitted as response in case of successful reset of an annotation status
 */
@JsonIgnoreProperties(ignoreUnknown = true) // required to avoid deserialisation failures for constant field 'reset'
public class JsonResetSuccessResponse extends JsonSuccessResponseBase {

    private static final boolean reset = true;

    // -------------------------------------
    // Constructor
    // -------------------------------------
    
    // default constructor required for deserialisation
    public JsonResetSuccessResponse() {
        super();
    }
    
    public JsonResetSuccessResponse(final String annotationId) {
        super(annotationId);
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------
    
    @Generated
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getReset() {
        return reset;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------
    
    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(id, reset);
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
        final JsonResetSuccessResponse other = (JsonResetSuccessResponse) obj;
        return Objects.equals(this.id, other.id); // static field left out
    }
}
