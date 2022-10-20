package eu.europa.ec.leos.annotate.model.web.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.europa.ec.leos.annotate.Generated;

/**
 * Class representing the simple structure transmitted as response in case of successful treatment of an annotation 
 */
@JsonIgnoreProperties(ignoreUnknown = true) // required to avoid deserialisation failures for constant field 'treated'
public class JsonTreatSuccessResponse extends JsonSuccessResponseBase {

    private static final boolean treated = true;

    // -------------------------------------
    // Constructor
    // -------------------------------------
    
    // default constructor required for deserialisation
    public JsonTreatSuccessResponse() {
        super();
    }
    
    public JsonTreatSuccessResponse(final String annotationId) {
        super(annotationId);
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
        return Objects.hash(id, treated);
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
        final JsonTreatSuccessResponse other = (JsonTreatSuccessResponse) obj;
        return Objects.equals(this.id, other.id); // static field left out
    }
}
