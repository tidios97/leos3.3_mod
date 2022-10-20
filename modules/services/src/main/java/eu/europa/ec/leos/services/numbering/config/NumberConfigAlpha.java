package eu.europa.ec.leos.services.numbering.config;

public class NumberConfigAlpha extends NumberConfigAbstract implements NumberConfig {

    public NumberConfigAlpha(boolean isUpperCase, String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.isUpperCase = isUpperCase;
    }

    public NumberConfigAlpha() {
        this(false, "", "");
    }

    @Override
    public String getActualNumberToShow() {
        int count = complexValue;
        if (value < 0) {
            // When numbering start from -1, we do not start complexValue from "a"
            // instead of -1a, -1b, -1c we show -1, -1a, -1b. Shift 1 alpha char to the left.
            count = complexValue - 1;
        }

        String val = "";
        if (value < 0) {
            val = "-";
        }
        val = val + getAlphaNumber(value) + getAlphaNumber(count);
        return isUpperCase ? val.toUpperCase() : val;
    }

    @Override
    public void parseInitialValue(String numAsString){
        int num = charToInteger(numAsString);
        setInitialValue(num);
    }

    @Override
    public void parseValue(String numAsString){
        int num = charToInteger(numAsString);
        setValue(num);
    }

    public int charToInteger(String numAsString) {
        int numAsInt = 0;
        for (int a = 0; a < numAsString.length(); a++) {
            int pow = numAsString.length() - 1 - a;
            numAsInt = numAsInt + ((numAsString.charAt(a) - 96) * (int) (Math.pow(26, pow)));
        }
        return numAsInt;
    }

    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }

}
