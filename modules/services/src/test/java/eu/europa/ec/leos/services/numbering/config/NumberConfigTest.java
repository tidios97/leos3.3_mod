package eu.europa.ec.leos.services.numbering.config;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberConfigTest {

    @Ignore
    @Test
    public void test_printArabicConfig() {
        NumberConfig numberConfig = new NumberConfigArabic();
        numberConfig.getNextNumberToShow();
        for (int i = 0; i < 200; i++) {
            numberConfig.incrementComplexValue();
            System.out.println(numberConfig.getComplexValue() + ", nr:" + numberConfig.getComplexValueToShow());
        }
    }

    @Test
    public void test_parseRoman() {
        NumberConfig numberConfig = new NumberConfigRoman();
        numberConfig.parseValue("i");
        assertEquals(1, numberConfig.getValue());
        assertEquals("i", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("xxxix");
        assertEquals(39, numberConfig.getValue());
        assertEquals("xxxix", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("MMMCMXCIX");
        assertEquals(3999, numberConfig.getValue());
        assertEquals("mmmcmxcix", numberConfig.getActualNumberToShow());
    }

    @Test
    public void test_parseAlpha() {
        NumberConfig numberConfig = new NumberConfigAlpha();
        numberConfig.parseValue("b");
        assertEquals(2, numberConfig.getValue());
        assertEquals("b", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("z");
        assertEquals(26, numberConfig.getValue());
        assertEquals("z", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("aa");
        assertEquals(27, numberConfig.getValue());
        assertEquals("aa", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("az");
        assertEquals(52, numberConfig.getValue());
        assertEquals("az", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("ba");
        assertEquals(53, numberConfig.getValue());
        assertEquals("ba", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("bz");
        assertEquals(78, numberConfig.getValue());
        assertEquals("bz", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("ca");
        assertEquals(79, numberConfig.getValue());
        assertEquals("ca", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("cz");
        assertEquals(104, numberConfig.getValue());
        assertEquals("cz", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("zz");
        assertEquals(702, numberConfig.getValue());
        assertEquals("zz", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("aaa");
        assertEquals(703, numberConfig.getValue());
        assertEquals("aaa", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("zzz");
        assertEquals(18278, numberConfig.getValue());
        assertEquals("zzz", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("aaaa");
        assertEquals(18279, numberConfig.getValue());
        assertEquals("aaaa", numberConfig.getActualNumberToShow());
        numberConfig.parseValue("zzzzz");
        assertEquals(12356630, numberConfig.getValue());
        assertEquals("zzzzz", numberConfig.getActualNumberToShow());
    }

    @Test
    public void test_arabicConfig() {
        NumberConfig numberConfig = new NumberConfigArabic();
        numberConfig.getNextNumberToShow();
        assertEquals("1", numberConfig.getActualNumberToShow());
        assertEquals("", numberConfig.getComplexValueToShow());

        // 1. Test first cycle "a" to "z"
        numberConfig.incrementComplexValue();
        assertEquals("a", numberConfig.getComplexValueToShow());
        for (int i = 0; i < 25; i++) {
            numberConfig.incrementComplexValue();
        }
        assertEquals("z", numberConfig.getComplexValueToShow());

        // 2. Test second cycle "aa" to "zz"
        numberConfig.incrementComplexValue();
        assertEquals("aa", numberConfig.getComplexValueToShow());
        numberConfig.incrementComplexValue();
        assertEquals("ab", numberConfig.getComplexValueToShow());
        for (int i = 0; i < 22; i++) {
            numberConfig.incrementComplexValue();
        }
        numberConfig.incrementComplexValue();
        assertEquals("ay", numberConfig.getComplexValueToShow()); // 51
        numberConfig.incrementComplexValue();
        assertEquals("az", numberConfig.getComplexValueToShow()); // 52

        // 3. Test third cycle "ba" to "bz"
        numberConfig.incrementComplexValue();
        assertEquals("ba", numberConfig.getComplexValueToShow()); // 53
        numberConfig.incrementComplexValue();
        assertEquals("bb", numberConfig.getComplexValueToShow()); // 54
        for (int i = 0; i < 22; i++) {
            numberConfig.incrementComplexValue();
        }
        numberConfig.incrementComplexValue();
        assertEquals("by", numberConfig.getComplexValueToShow()); // 77
        numberConfig.incrementComplexValue();
        assertEquals("bz", numberConfig.getComplexValueToShow()); // 78

        // 4. Test third cycle "ca" to the "cz"
        numberConfig.incrementComplexValue();
        assertEquals("ca", numberConfig.getComplexValueToShow()); // 79
        for (int i = 0; i < 23; i++) {
            numberConfig.incrementComplexValue();
        }
        numberConfig.incrementComplexValue();
        assertEquals("cy", numberConfig.getComplexValueToShow()); // 103
        numberConfig.incrementComplexValue();
        assertEquals("cz", numberConfig.getComplexValueToShow()); // 104

        // 5. make sure adding more 1000 articles we have the right "complex" number
        for (int i = 1; i < 1000; i++) {
            numberConfig.incrementComplexValue();
        }
        assertEquals("For config: " + numberConfig.toString(), "apk", numberConfig.getComplexValueToShow());
        assertEquals("1apk", numberConfig.getActualNumberToShow());

    }

}
