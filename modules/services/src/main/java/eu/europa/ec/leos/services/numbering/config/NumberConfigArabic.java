package eu.europa.ec.leos.services.numbering.config;

public class NumberConfigArabic extends NumberConfigAbstract implements NumberConfig {

    public NumberConfigArabic(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumberConfigArabic() {
        this("", "");
    }

    @Override
    public String getActualNumberToShow() {
        int count = complexValue;
        if (value < 0) {
            // When numbering start from -1, we do not start complexValue from "a"
            // instead of -1a, -1b, -1c we show -1, -1a, -1b. Shift 1 alpha char to the left.
            count = complexValue - 1;
        }
        return value + getAlphaNumber(count);
    }

    @Override
    public void parseInitialValue(String numAsString){
        int num = stringToInteger(numAsString);
        setInitialValue(num);
    }

    @Override
    public void parseValue(String numAsString) {
        int num = stringToInteger(numAsString);
        setValue(num);
    }

    public int stringToInteger(String numAsString) {
        int num;
        try {
            num = Integer.parseInt(numAsString);
        } catch (NumberFormatException e) {
            num = 1;
        }
        return num;
    }

    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }

}
