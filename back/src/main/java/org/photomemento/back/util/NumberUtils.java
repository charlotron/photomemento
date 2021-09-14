package org.photomemento.back.util;

import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.types.Constants;

public class NumberUtils {

    private NumberUtils() {
        throw new InvalidStateError("Should not be used");
    }

    /**
     * Seeks for consecutive digits inside a string
     * ie: "alksdfj 2983 añldk" returns 2983 as integer
     */
    public static int extractDigits(String str) {
        return Integer.parseInt(str.replaceAll("[^\\d]+", ""));
    }

    /**
     * Seeks for consecutive digits inside a string
     * ie: "alksdfj 2983 añldk" returns 2983 as integer
     */
    public static long extractDigitsLong(String str) {
        return Long.parseLong(str.replaceAll("[^\\d]+", ""));
    }
    /**
     * Seeks for consecutive digits inside a string
     * ie: "alksdfj 2983 añldk" returns 2983 as integer
     */
    public static float extractDigitsFloat(String str) {
        return Float.parseFloat(str.replaceAll("[^\\d.]+", ""));
    }
    /**
     * Seeks for consecutive digits inside a string
     * ie: "alksdfj 2983 añldk" returns 2983 as integer
     */
    public static double extractDigitsDouble(String str) {
        return Double.parseDouble(str.replaceAll("[^\\d.]+", ""));
    }

    /**
     * Seeks for consecutive digits inside a string allowing negative integers
     * ie: "alksdfj -2983 añldk" returns -2983 as integer
     */
    public static int extractInteger(String str) {
        return Integer.parseInt(str.replaceAll("[^\\d\\-]+", ""));
    }

    /**
     * Parse as float, but also converting commas to dot
     */
    public static float parseAsFloatFixingCommas(String str) {
        return Float.parseFloat(str.trim().replace(Constants.COMMA,Constants.DOT));
    }
    /**
     * Parse as double, but also converting commas to dot
     */
    public static double parseAsDoubleFixingCommas(String str) {
        return Double.parseDouble(str.trim().replace(Constants.COMMA,Constants.DOT));
    }
}
