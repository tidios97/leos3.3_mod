package eu.europa.ec.digit.leos.pilot.export.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTests {
    @Test
    public void testIsEmpty() {
        Assertions.assertTrue(StringUtil.isEmpty(null));
        Assertions.assertTrue(StringUtil.isEmpty(""));
        Assertions.assertFalse(StringUtil.isEmpty("NotEmpty"));
    }

    @Test
    public void testIsEqual() {
        Assertions.assertTrue(StringUtil.isEqual(null, null));
        Assertions.assertTrue(StringUtil.isEqual("", ""));
        Assertions.assertTrue(StringUtil.isEqual("EqualText", "EqualText"));
        Assertions.assertFalse(StringUtil.isEqual(null, ""));
        Assertions.assertFalse(StringUtil.isEqual("", null));
        Assertions.assertFalse(StringUtil.isEqual("EqualText", "NotEqualText"));
        Assertions.assertFalse(StringUtil.isEqual("NotEqualText", "EqualText"));
    }
}
