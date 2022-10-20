package eu.europa.ec.leos.services.numbering.config;

public interface NumberConfig {

    String getActualNumberToShow();
    String getNextNumberToShow();
    void parseValue(String numAsString);
    int getValue();
    void setValue(int value);
    void parseInitialValue(String numAsString);

    String getPrefix();
    String getSuffix();

    void setComplex(boolean isComplex);
    boolean isComplex();

    int getComplexValue();
    String getComplexValueToShow();
    void incrementComplexValue();
    void resetComplexValue();
}
