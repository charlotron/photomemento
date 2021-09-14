package org.photomemento.back.types.size;

import org.photomemento.back.types.Constants;

/**
 * Description:
 * - This class is used to handle bytes
 * - Byte is the most common unit used for measure storage and every unit is a pow of 1024
 */
public enum ByteUnit {
    BYTE(0), KILOBYTE(1), MEGABYTE(2), GIGABYTE(3), TERABYTE(4), PETABYTE(5), EXABYTE(6), ZETTABYTE(7), YOTTABYTE(8);
    /**
     * How many bits has a byte
     */
    private static final int BIT_PER_BYTE = 8;
    /**
     * How many is the base for the pow, each unit
     */
    private static final int BYTES_POW_EACH_UNIT = 1024;
    /**
     * Byte name
     */
    private static final String BYTE_NAME = "Byte";
    /**
     * Short byte name first char
     */
    private static final char BYTE_CHAR = 'B';

    /**
     * Bytes of every unit in the enum
     */
    private final double bytesUnit;
    /**
     * This is to establish max value to consider this unit as suitable
     */
    private final double internalByteCap;
    /**
     * Short name for this byte unit
     */
    private final String shortName;
    /**
     * Sortest name for this byte unit
     */
    private final String shortestName;

    /**
     * Ctor. for ByteUnit type
     */
    ByteUnit(int bytePow) {
        this.bytesUnit = Math.pow(BYTES_POW_EACH_UNIT, bytePow);
        this.internalByteCap = bytesUnit * BYTES_POW_EACH_UNIT - 1;
        char firstChar = name().charAt(0);
        this.shortName = firstChar == BYTE_CHAR ? BYTE_NAME : firstChar + BYTE_NAME;
        this.shortestName = firstChar == BYTE_CHAR ? BYTE_CHAR + "" : firstChar + "" + BYTE_CHAR;
    }

    /**
     * converts to BYTE unit
     */
    public double toByte(double value) {
        return convert(this, BYTE, value);
    }

    /**
     * converts to KILOBYTE unit
     */
    public double toKByte(double value) {
        return convert(this, KILOBYTE, value);
    }

    /**
     * converts to MEGABYTE unit
     */
    public double toMByte(double value) {
        return convert(this, MEGABYTE, value);
    }

    /**
     * converts to GIGABYTE unit
     */
    public double toGByte(double value) {
        return convert(this, GIGABYTE, value);
    }

    /**
     * converts to PETABYTE unit
     */
    public double toPByte(double value) {
        return convert(this, PETABYTE, value);
    }

    /**
     * converts to EXABYTE unit
     */
    public double toEByte(double value) {
        return convert(this, EXABYTE, value);
    }

    /**
     * converts to ZETABYTE unit
     */
    public double toZByte(double value) {
        return convert(this, ZETTABYTE, value);
    }

    /**
     * converts to YOTABYTE unit
     */
    public double toYByte(double value) {
        return convert(this, YOTTABYTE, value);
    }

    /**
     * converts to BIT unit
     */
    public double toBit(double value) {
        return convert(this, BitUnit.BIT, value);
    }

    /**
     * converts to KILOBIT unit
     */
    public double toKBit(double value) {
        return convert(this, BitUnit.KILOBIT, value);
    }

    /**
     * converts to MEGABIT unit
     */
    public double toMBit(double value) {
        return convert(this, BitUnit.MEGABIT, value);
    }

    /**
     * converts to GIGABIT unit
     */
    public double toGBit(double value) {
        return convert(this, BitUnit.GIGABIT, value);
    }

    /**
     * converts to PETABIT unit
     */
    public double toPBit(double value) {
        return convert(this, BitUnit.PETABIT, value);
    }

    /**
     * converts to EXABIT unit
     */
    public double toEBit(double value) {
        return convert(this, BitUnit.EXABIT, value);
    }

    /**
     * converts to ZETABIT unit
     */
    public double toZBit(double value) {
        return convert(this, BitUnit.ZETTABIT, value);
    }

    /**
     * converts to YOTABIT unit
     */
    public double toYBit(double value) {
        return convert(this, BitUnit.YOTTABIT, value);
    }

    /**
     * Converts a value between one and another
     */
    public static double convert(ByteUnit from, ByteUnit to, double value) {
        //No changes?
        if (from == to)
            return value;
        //Converting to bytes
        if (from != BYTE)
            value = from.getBytesUnit() * value;
        //Convert to bytes? then return the result
        if (to == BYTE)
            return value;
        //Greater than bytes? divide by its bytes per unit
        return value / to.getBytesUnit();
    }

    /**
     * Converts a value between one and another
     */
    public static double convert(ByteUnit from, BitUnit to, double value) {
        //Converting to bits
        if (from != BYTE)
            value = from.getBytesUnit() * BIT_PER_BYTE * value;
        //Convert to bits then return the result
        if (to == BitUnit.BIT)
            return value;
        //Delegate in BIT convert
        return BitUnit.convert(BitUnit.BIT, to, value);
    }

    /**
     * Returns number of bytes for this unit
     */
    public double getBytesUnit() {
        return bytesUnit;
    }

    /**
     * Short version of this byte unit
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Shortest version of this byte unit
     */
    public String getShortestName() {
        return shortestName;
    }

    /**
     * Prints a value using current data, this is equals to call toString(value, true, 2, Constants.WHITESPACE)
     */
    public String toString(double value) {
        return toString(value, true, 2, Constants.WHITESPACE);
    }

    /**
     * Prints a value using current data
     */
    public String toString(double value, boolean isShowShortestName, int decimals, String separator) {
        if (decimals < 0)
            decimals = 0;
        if (decimals > 0)
            value = Math.round(value * decimals) / (double) decimals;
        else
            value = Math.round(value);

        String res = value + "";
        if (res.endsWith(".0"))
            res = res.substring(0, res.length() - 2);
        if (isShowShortestName) {
            if (separator == null)
                return res + shortestName;
            else
                return res + separator + shortestName;
        }
        return res;
    }

    /**
     * Prints a value using current data, this is equals to call toStringMostSuitableForBytes(value, true, 2, Constants.WHITESPACE)
     */
    public static String toStringMostSuitableForBytes(double value) {
        return toStringMostSuitableForBytes(value, true, 2, Constants.WHITESPACE);
    }

    /**
     * Prints a value using the most suitable unit for given value in bytes
     */
    public static String toStringMostSuitableForBytes(double valueInBytes, boolean isShowShortestName, int decimals, String separator) {
        ByteUnit byteunit = getMostSuitableUnitForBytes(valueInBytes);
        double convertedValue = convert(BYTE, byteunit, valueInBytes);
        return byteunit.toString(convertedValue, isShowShortestName, decimals, separator);
    }

    /**
     * Gets the most suitable unit to represent given value in bytes
     */
    public static ByteUnit getMostSuitableUnitForBytes(double valueInBytes) {
        for (ByteUnit unit : values())
            if (valueInBytes >= unit.bytesUnit && valueInBytes <= unit.internalByteCap)
                return unit;
        return ByteUnit.BYTE;
    }
}
