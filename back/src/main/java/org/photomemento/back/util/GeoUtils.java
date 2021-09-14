package org.photomemento.back.util;

import org.photomemento.back.exceptions.InvalidStateError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoUtils {

    public static final String SIGN = "sign";
    public static final String DEGS = "degs";
    public static final String MINS = "mins";
    public static final String SECS = "secs";
    public static final String DIR = "dir";
    private static final Pattern CAPTURE_COORDINATES_DEGREES = Pattern.compile("(?<" + SIGN + ">-?)(?<" + DEGS + ">\\d+)[^\\d]+(?<" + MINS + ">\\d+)[^\\d]+(?<" + SECS + ">[,.\\d]+)(?<" + DIR + ">[NSWE])");
    private static final String COORDINATES_DEGREES_NORTH_WEST = "%sN %sW";

    private GeoUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static String getDegMinSec(String latDegrees, String lonDegrees) {
        return String
                .format(COORDINATES_DEGREES_NORTH_WEST, latDegrees, lonDegrees)
                .replace(" ", "")
                .replace("\"", "");
    }

    public static double[] degreesToDecimal(String degMinSec) {
        double[] result = new double[2];
        Matcher m = CAPTURE_COORDINATES_DEGREES.matcher(degMinSec);

        for (int i = 0; i < 2 && m.find(); i++)
            result[i] = parseDegreeToDecimalInt(m);
        return result;
    }

    private static double parseDegreeToDecimalInt(Matcher m) {
        int sign = m.group(SIGN).length() > 0 ? -1 : 1;
        int deg = Integer.parseInt(m.group(DEGS));
        int min = Integer.parseInt(m.group(MINS));
        double sec = Float.parseFloat(m.group(SECS).replace(",", "."));
        return sign * (deg + min / 60f + sec / 3600f);
    }
}
