package eu.europa.ec.leos.annotate.services.impl.util;

public final class TextShortener {

    private TextShortener() {
        // Prevent instantiation as all methods are static.
    }

    public static String getFirstGivenNumberOfCharacters(final String text, final int limit) {

        final int end = Math.min(text.length(), limit);
        return text.substring(0, end);
    }

    public static String getLastGivenNumberOfCharacters(final String text, final int limit) {

        final int start = text.length() - Math.min(limit, text.length());
        return text.substring(start);
    }
}
