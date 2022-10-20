package eu.europa.ec.leos.annotate;

import org.springframework.util.StringUtils;

/**
 * class for hosting the known systems (aka. "authorities") for which specific logic exists
 */
public final class Authorities {

    // -------------------------------------
    // Public authority constants
    // -------------------------------------
    public static final String ISC = "ISC";
    public static final String EdiT = "LEOS";
    public static final String Support = "SUPPORT";

    // -------------------------------------
    // Constructors
    // -------------------------------------
    private Authorities() {
        // prevent instantiation using a private constructor
    }

    // -------------------------------------
    // Help functions
    // -------------------------------------

    // check if a given authority represents the ISC
    public static boolean isIsc(final String authority) {

        return StringUtils.hasLength(authority) && ISC.equals(authority);
    }

    // check if a given authority represents the LEOS / EdiT
    public static boolean isLeos(final String authority) {

        return StringUtils.hasLength(authority) && EdiT.equals(authority);
    }
    
    public static boolean isSupport(final String authority) {
        
        return StringUtils.hasLength(authority) && Support.equals(authority.toUpperCase());
    }
}
