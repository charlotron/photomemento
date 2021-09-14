package org.photomemento.back.types.size;


import org.photomemento.back.types.Constants;

/**
 * - This class is used to handle bits
 * - bit is the most common unit used for measure storage and every unit is a pow of 1000
 */
public enum BitUnit {
    BIT(0),
    KILOBIT(1),
    MEGABIT(2),
    GIGABIT(3),
    TERABIT(4),
    PETABIT(5),
    EXABIT(6),
    ZETTABIT(7),
    YOTTABIT(8);
    /**
     * How many bits has a byte
     */
    private static final int BIT_PER_BYTE = 8;
    /**
     * How many is the base for the pow, each unit
     */
    private static final int BITS_POW_EACH_UNIT = 1000;
    /**
     * Bit name
     */
    private static final String BIT_NAME = "bit";
    /**
     * Short bit name first char
     */
    private static final char BIT_CHAR = 'b';

    /**
     * Bits of every unit in the enum
     */
    private final double bitsUnit;
    /**
     * This is to establish max value to consider this unit as suitable
     */
    private final double internalBitCap;
    /**
     * Short name for this bit unit
     */
    private final String shortName;
    /**
     * Sortest name for this bit unit
     */
    private final String shortestName;

    /**
     * Ctor. for BitUnit type
     */
    BitUnit(int bitPow) {
        this.bitsUnit = Math.pow(BITS_POW_EACH_UNIT, bitPow);
        this.internalBitCap = bitsUnit * BITS_POW_EACH_UNIT - 1;
        char firstChar = name().toLowerCase().charAt(0);
        this.shortName = firstChar == BIT_CHAR ? BIT_NAME : firstChar + BIT_NAME;
        this.shortestName = firstChar == BIT_CHAR ? BIT_CHAR + "" : firstChar + "" + BIT_CHAR;
    }

    /**
     * converts to BIT unit
     */
    public double toBit(double value) {
        return convert(this, BIT, value);
    }

    /**
     * converts to KILOBIT unit
     */
    public double toKBit(double value) {
        return convert(this, KILOBIT, value);
    }

    /**
     * converts to MEGABIT unit
     */
    public double toMBit(double value) {
        return convert(this, MEGABIT, value);
    }

    /**
     * converts to GIGABIT unit
     */
    public double toGBit(double value) {
        return convert(this, GIGABIT, value);
    }

    /**
     * converts to PETABIT unit
     */
    public double toPBit(double value) {
        return convert(this, PETABIT, value);
    }

    /**
     * converts to EXABIT unit
     */
    public double toEBit(double value) {
        return convert(this, EXABIT, value);
    }

    /**
     * converts to ZETABIT unit
     */
    public double toZBit(double value) {
        return convert(this, ZETTABIT, value);
    }

    /**
     * converts to YOTABIT unit
     */
    public double toYBit(double value) {
        return convert(this, YOTTABIT, value);
    }

    /**
     * converts to BYTE unit
     */
    public double toByte(double value) {
        return convert(this, ByteUnit.BYTE, value);
    }

    /**
     * converts to KILOBYTE unit
     */
    public double toKByte(double value) {
        return convert(this, ByteUnit.KILOBYTE, value);
    }

    /**
     * converts to MEGABYTE unit
     */
    public double toMByte(double value) {
        return convert(this, ByteUnit.MEGABYTE, value);
    }

    /**
     * converts to GIGABYTE unit
     */
    public double toGByte(double value) {
        return convert(this, ByteUnit.GIGABYTE, value);
    }

    /**
     * converts to PETABYTE unit
     */
    public double toPByte(double value) {
        return convert(this, ByteUnit.PETABYTE, value);
    }

    /**
     * converts to EXABYTE unit
     */
    public double toEByte(double value) {
        return convert(this, ByteUnit.EXABYTE, value);
    }

    /**
     * converts to ZETABYTE unit
     */
    public double toZByte(double value) {
        return convert(this, ByteUnit.ZETTABYTE, value);
    }

    /**
     * converts to YOTABYTE unit
     */
    public double toYByte(double value) {
        return convert(this, ByteUnit.YOTTABYTE, value);
    }

    /**
     * Converts a value between one and another
     */
    public static double convert(BitUnit from, BitUnit to, double value) {
        //No changes?
        if (from == to)
            return value;
        //Converting to bits
        if (from != BIT)
            value = from.getBitsUnit() * value;
        //Convert to bits? then return the result
        if (to == BIT)
            return value;
        //Greater than bits? divide by its bits per unit
        return value / to.getBitsUnit();
    }

    /**
     * Converts a value between one and another
     */
    public static double convert(BitUnit from, ByteUnit to, double value) {
        //Converting to bits
        if (from != BIT)
            value = Math.round(from.getBitsUnit() / BIT_PER_BYTE) * value;
        //Convert to bits then return the result
        if (to == ByteUnit.BYTE)
            return value;
        //Delegate in BIT convert
        return BitUnit.convert(BitUnit.BIT, to, value);
    }

    /**
     * Returns number of bits for this unit
     */
    public double getBitsUnit() {
        return bitsUnit;
    }

    /**
     * Short version of this bit unit
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Shortest version of this bit unit
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
     * Prints a value using current data, this is equals to call toStringMostSuitableForBits(value, true, 2, Constants.WHITESPACE)
     */
    public static String toStringMostSuitableForBits(double value) {
        return toStringMostSuitableForBits(value, true, 2, Constants.WHITESPACE);
    }

    /**
     * Prints a value using the most suitable unit for given value in bits
     */
    public static String toStringMostSuitableForBits(double valueInBits, boolean isShowShortestName, int decimals, String separator) {
        BitUnit bitunit = getMostSuitableUnitForBits(valueInBits);
        double convertedValue = convert(BIT, bitunit, valueInBits);
        return bitunit.toString(convertedValue, isShowShortestName, decimals, separator);
    }

    /**
     * Gets the most suitable unit to represent given value in bits
     */
    public static BitUnit getMostSuitableUnitForBits(double valueInBits) {
        for (BitUnit unit : values()) {
            if (valueInBits >= unit.bitsUnit && valueInBits <= unit.internalBitCap)
                return unit;
        }
        return BitUnit.BIT;
    }
}
