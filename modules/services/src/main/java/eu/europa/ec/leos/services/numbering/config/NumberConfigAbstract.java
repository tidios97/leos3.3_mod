package eu.europa.ec.leos.services.numbering.config;

public abstract class NumberConfigAbstract implements NumberConfig {

    protected int initialValue;
    protected int value;
    protected int complexValue;
    protected String numberToShow;
    protected String prefix;
    protected String suffix;
    protected boolean isUpperCase;
    protected boolean isComplex;

    public NumberConfigAbstract() {
        setInitialValue(1);
    }

    @Override
    public String getNextNumberToShow() {
        if(initialValue == - value) {
            value = initialValue;
        } else {
            value++;
        }

        if (value == 0) {
            value++;// 0 is not in the scale. We skip from -1 to 1.
        }

        numberToShow = getActualNumberToShow();
        return numberToShow;
    }

    protected String getAlphaNumber(int intValue) {
        if (intValue == 0) {
            return "";
        }
        intValue = Math.abs(intValue);
        String alphaNumber = "";
        while (intValue != 0) {
            int charPosition = (intValue-1) % 26;
            alphaNumber = ((char) (charPosition + 97)) + alphaNumber;
            intValue = (intValue-1) / 26;
        }
        // "a"-"z" chars are located in the 97-122 indexes of the ASCII table, so shift all 96 positions.
        return alphaNumber;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value){
        this.value = value;
    }

    protected void setInitialValue(int value) {
       this.initialValue = value;
       this.value = -value;
    }

    @Override
    public int getComplexValue() {
        return complexValue;
    }

    @Override
    public String getComplexValueToShow() {
        return getAlphaNumber(complexValue);
    }

    @Override
    public void incrementComplexValue() {
        complexValue++;
    }

    @Override
    public void resetComplexValue() {
        complexValue = 0;
    }

    @Override
    public void setComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    @Override
    public boolean isComplex() {
        return isComplex;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getImplName() + "[")
                .append("value = ").append(value)
                .append(", complexValue = ").append(complexValue)
                .append(", numberToShow = ").append(numberToShow)
                .append(", prefix = ").append(prefix)
                .append(", suffix = ").append(suffix)
                .append(", isUpperCase = ").append(isUpperCase)
                .append(", isComplex = ").append(isComplex)
                .append("]");

        return sb.toString();
    }

    protected abstract String getImplName();
}
