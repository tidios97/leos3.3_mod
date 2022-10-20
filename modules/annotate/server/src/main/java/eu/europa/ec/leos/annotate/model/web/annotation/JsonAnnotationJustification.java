/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.annotate.model.web.annotation;

import eu.europa.ec.leos.annotate.Generated;

import java.util.Objects;

/**
 * class representing the justification assigned to a suggestion;
 * this property of a {@link JsonAnnotation} is null for other annotation types
 */
public class JsonAnnotationJustification {

    private String text;

    // -------------------------------------
    // Constructors
    // -------------------------------------
    public JsonAnnotationJustification() {
        // default constructor
    }

    public JsonAnnotationJustification(final JsonAnnotationJustification orig) {
        this.text = orig.text;
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    public String getText() {
        return text;
    }

    @Generated
    public void setText(final String text) {
        this.text = text;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(text);
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
        final JsonAnnotationJustification other = (JsonAnnotationJustification) obj;
        return Objects.equals(this.text, other.text);
    }
}
