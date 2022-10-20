package eu.europa.ec.leos.services.numbering.config;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

public class NumberConfigRoman extends NumberConfigAbstract implements NumberConfig {

    private boolean isUpperCase;

    public NumberConfigRoman(boolean isUpperCase, String prefix, String suffix) {
        this.isUpperCase = isUpperCase;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumberConfigRoman() {
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
        val = val + getRoman(value) + getAlphaNumber(count);
        return isUpperCase ? val.toUpperCase() : val;
    }

    private String getRoman(int val) {
        val = Math.abs(val);
        String pointNum = join("", nCopies(val, "i"))
                .replace("iiiii", "v")
                .replace("iiii", "iv")
                .replace("vv", "x")
                .replace("viv", "ix")
                .replace("xxxxx", "l")
                .replace("xxxx", "xl")
                .replace("ll", "c")
                .replace("lxl", "xc")
                .replace("ccccc", "d")
                .replace("cccc", "cd")
                .replace("dd", "m")
                .replace("dcd", "cm");
        return pointNum;
    }

    @Override
    public void parseInitialValue(String numAsString) {
        int num = romanToInteger(numAsString);
        setInitialValue(num);
    }

    @Override
    public void parseValue(String numAsString) {
        int num = romanToInteger(numAsString);
        setValue(num);
    }

    public static int romanToInteger(String roman) {
        roman = roman.toUpperCase();
        Map<Character, Integer> numbersMap = new HashMap<>();
        numbersMap.put('I', 1);
        numbersMap.put('V', 5);
        numbersMap.put('X', 10);
        numbersMap.put('L', 50);
        numbersMap.put('C', 100);
        numbersMap.put('D', 500);
        numbersMap.put('M', 1000);

        int result = 0;
        for (int i = 0; i < roman.length(); i++) {
            char ch = roman.charAt(i);
            if (i > 0 && numbersMap.get(ch) > numbersMap.get(roman.charAt(i - 1))) {
                //Case 1
                result += numbersMap.get(ch) - 2 * numbersMap.get(roman.charAt(i - 1));
            } else {
                // Case 2: just add the corresponding number to result.
                result += numbersMap.get(ch);
            }
        }
        return result;
    }


    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }

}
