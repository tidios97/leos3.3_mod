package eu.europa.ec.leos.annotate.unit.services.impl.util;

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.services.impl.util.TextShortener;

public class TextShortenerTest {

    @Test
    public void testGetFirstGivenNumberOfCharacters_None() {

        Assert.assertEquals("", TextShortener.getFirstGivenNumberOfCharacters("abc", 0));
    }

    @Test
    public void testGetFirstGivenNumberOfCharacters_Number() {

        Assert.assertEquals("ab", TextShortener.getFirstGivenNumberOfCharacters("abc", 2));
    }

    @Test
    public void testGetFirstGivenNumberOfCharacters_NumberLargerThanTextLength() {

        Assert.assertEquals("jkl", TextShortener.getFirstGivenNumberOfCharacters("jkl", 5));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_None() {

        Assert.assertEquals("", TextShortener.getLastGivenNumberOfCharacters("def", 0));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_Number() {
        Assert.assertEquals("hi", TextShortener.getLastGivenNumberOfCharacters("ghi", 2));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_NumberLargerThanTextLength() {

        Assert.assertEquals("def", TextShortener.getLastGivenNumberOfCharacters("def", 5));
    }

}
