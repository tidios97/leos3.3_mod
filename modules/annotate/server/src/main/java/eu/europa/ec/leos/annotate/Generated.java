package eu.europa.ec.leos.annotate;

/**
 * simple (empty) annotation definition
 * purpose: prevent annotated methods being included in code coverage measures
 * by JaCoCo, e.g. equals and hashCode;
 * for this purpose, the annotation needs to be called "Generated"
 */
public @interface Generated {

}
