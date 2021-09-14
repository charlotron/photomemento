package org.photomemento.back.util;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.types.Constants;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

    public static final ZoneId UTC = ZoneId.of("UTC");

    private static final String REGEX_FULL_YEAR = "((?:19)[7-9]\\d|(?:20)\\d{2})";                      //ALLOWED: FULL DATE 1970+ OR 20XX YEAR
    private static final String REGEX_MONTH = "((?:0[1-9])|(?:1[0-2]))";                                //ALLOWED: 01->12
    private static final String REGEX_DAY_OF_MONTH = "((?:0[1-9])|(?:[12]\\d)|(?:3[01]))";              //ALLOWED: 01->31
    private static final String REGEX_HOURS = "((?:[01]\\d)|(?:2[0-3]))";                               //ALLOWED: 00->23
    private static final String REGEX_MINS_SECS = "([0-5]\\d)";                                          //ALLOWED: 00->23
    private static final String REGEX_MILLIS = "\\d{3,9}";                                               //ALLOWED 3 to 9 digits (depend on precision)
    private static final String REGEX_DATE_ENG_SEP = "\\-";
    private static final String REGEX_TIME_SEP = "\\:";
    private static final String REGEX_DATE_TIME_SEP = "T";
    private static final String REGEX_MILLIS_SEP = "\\.";
    private static final String REGEX_UTZ_END = "Z";

    private static final Pattern UTZ_PATTERN = Pattern.compile(
            REGEX_FULL_YEAR + REGEX_DATE_ENG_SEP + REGEX_MONTH + REGEX_DATE_ENG_SEP + REGEX_DAY_OF_MONTH + REGEX_DATE_TIME_SEP +
                    REGEX_HOURS + REGEX_TIME_SEP + REGEX_MINS_SECS + REGEX_TIME_SEP + REGEX_MINS_SECS + REGEX_MILLIS_SEP +
                    REGEX_MILLIS + REGEX_UTZ_END);

    private static final Pattern FULL_DATE_TIME_PATTERN = Pattern.compile("^" + REGEX_FULL_YEAR + REGEX_MONTH + REGEX_DAY_OF_MONTH + REGEX_HOURS + REGEX_MINS_SECS + REGEX_MINS_SECS + ".*$");
    private static final Pattern FULL_DATE_PATTERN = Pattern.compile("^" + REGEX_FULL_YEAR + REGEX_MONTH + REGEX_DAY_OF_MONTH + ".*$");
    private static final Pattern FULL_YEAR_PATTERN = Pattern.compile("^" + REGEX_FULL_YEAR + ".*$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^" + REGEX_MONTH + ".*$");
    private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("^" + REGEX_DAY_OF_MONTH + ".*$");
    private static final Pattern HOURS_MINS_SECS_PATTERN = Pattern.compile("^" + REGEX_HOURS + REGEX_MINS_SECS + REGEX_MINS_SECS + ".*$");
    private static final Pattern HOURS_PATTERN = Pattern.compile("^" + REGEX_HOURS + ".*$");
    private static final Pattern MINS_SECS_PATTERN = Pattern.compile("^" + REGEX_MINS_SECS + ".*$");
    private static final String SECONDS_HUMAN = "%ss";
    private static final String MINUTES_HUMAN = "%sm";
    private static final String HOURS_HUMAN = "%sh";
    private static final String HOURS_MINUTES_HUMAN = HOURS_HUMAN + " " + MINUTES_HUMAN;
    private static final String DAYS_HUMAN = "%dd";
    private static final String DAYS_HOURS_HUMAN = DAYS_HUMAN + "d " + HOURS_HUMAN;

    private DateUtils() {
        throw new InvalidStateError("Should not be used");
    }

    /**
     * If a date has been generated manually, ie 20120102, the hours, mins and secs are set to 0, so it has to compete with a valid date like (2012/02/12 20:00:00), but the second one is more precise
     * If date is different just take the oldest
     * If difference is in time, take the most precise
     */
    public static Instant getMostAccurateOldestDate(Instant a, Instant b) { //NOSONAR

        if (a == null) return b;
        if (b == null) return a;

        ZonedDateTime za = getZonedDateTime(a);
        ZonedDateTime zb = getZonedDateTime(b);

        //Distinct year? just return the oldest
        if (za.getYear() < zb.getYear()) return a;
        if (zb.getYear() < za.getYear()) return b;

        //Same year, check month
        if (za.getMonthValue() < zb.getMonthValue()) return a;
        if (zb.getMonthValue() < za.getMonthValue()) return b;

        //Same month, check day
        if (za.getDayOfMonth() < zb.getDayOfMonth()) return a;
        if (zb.getDayOfMonth() < za.getDayOfMonth()) return b;

        //Same day, check hour
        if (zb.getHour() == 0 && za.getHour() != 0) return a;
        if (za.getHour() == 0 && zb.getHour() != 0) return b;
        if (za.getHour() < zb.getHour()) return a;
        if (zb.getHour() < za.getHour()) return b;

        //Same hour, check minutes
        if (zb.getMinute() == 0 && za.getMinute() != 0) return a;
        if (za.getMinute() == 0 && zb.getMinute() != 0) return b;
        if (za.getMinute() < zb.getMinute()) return a;
        if (zb.getMinute() < za.getMinute()) return b;

        //Same minutes, check min
        return a.compareTo(b) < 0 ? a : b;
    }

    public static Instant newInstant(int year, int month, int day) {
        return newInstant(year, month, day, 0, 0, 0);
    }

    public static Instant newInstant(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, UTC).toInstant();
    }

    public static Instant detectDate(String filename) {
        if (!StringUtils.hasText(filename)) return null;

        Instant date = detectUTZInString(filename);
        if (date != null) return date;

        List<String> cleanedStrParts = cleanString(filename);
        if (CollectionUtils.isEmpty(cleanedStrParts)) return null;

        return detectDateFromParts(cleanedStrParts);
    }


    private static Instant detectUTZInString(String text) {
        Matcher matcher = UTZ_PATTERN.matcher(text);
        return matcher.find() ?
                Instant.parse(matcher.group(0)) :
                null;
    }

    /**
     * Removes non important chars from string
     */
    private static List<String> cleanString(String filename) {
        if (!StringUtils.hasText(filename)) return null; //NOSONAR

        String cleanedStr = filename
                .replaceAll("[.][^.]+$", Constants.EMPTY_STRING)                                    //Remove extension
                .replaceAll("\\d*[a-zA-ZçÇñÑüÜáéíóúÁÉÍÓÚ]+\\d*", Constants.EMPTY_STRING)            //Remove all compound digit
                .replaceAll("[:/\\\\\\-]+", Constants.EMPTY_STRING)                                 //Remove all date separator chars
                .replaceAll("[^\\d]+", Constants.WHITESPACE)                                          //Remove rest of characters distinct than digits
                .trim();
        if (cleanedStr.isEmpty()) return null;//NOSONAR
        return Arrays.asList(cleanedStr.split(Constants.WHITESPACE));
    }


    private static Instant detectDateFromParts(List<String> cleanedStrParts) {
        //1st) Look for full date matches
        for (String str : cleanedStrParts) {
            InstantBuilder ib = ifMatchesFillDateData(null, str, FULL_DATE_TIME_PATTERN, I_B_FIELD.YEAR, I_B_FIELD.MONTH, I_B_FIELD.DAY_OF_MONTH, I_B_FIELD.HOURS, I_B_FIELD.MINS, I_B_FIELD.SECS);
            if (ib != null && ib.hasValidFullDateTime()) return ib.build();
        }

        InstantBuilder ib = null;
        List<Instant> candidates = new ArrayList<>();
        //Look for full date matches over parts
        for (int i = 0; i < cleanedStrParts.size(); i++) {
            //--- FULL DATE YYYYMMDD
            ib = ifMatchesFillDateData(ib, cleanedStrParts.get(i), FULL_DATE_PATTERN, I_B_FIELD.YEAR, I_B_FIELD.MONTH, I_B_FIELD.DAY_OF_MONTH);
            //Check for time
            if (ib != null && ib.hasValidDate()) {
                candidates.add(detectDateTimeFromParts(cleanedStrParts, i, ib));
                continue;
            }

            //--- FULL DATE (split) YYYY MM DD
            if ((i + 2) < cleanedStrParts.size()) {
                ib = ifMatchesFillDateData(ib, cleanedStrParts.get(i), FULL_YEAR_PATTERN, I_B_FIELD.YEAR);
                ib = ifMatchesFillDateData(ib, cleanedStrParts.get(i + 1), MONTH_PATTERN, I_B_FIELD.MONTH);
                ib = ifMatchesFillDateData(ib, cleanedStrParts.get(i + 2), DAY_OF_MONTH_PATTERN, I_B_FIELD.DAY_OF_MONTH);

                //Check for time
                if (ib != null && ib.hasValidDate())
                    candidates.add(detectDateTimeFromParts(cleanedStrParts, i + 2, ib));
            }
        }

        return candidates.stream().reduce(null, DateUtils::getMostAccurateOldestDate);
    }

    private static Instant detectDateTimeFromParts(List<String> cleanedStrParts, int i, InstantBuilder ib) {
        // ----- FULL TIME HHMMSS
        if ((i + 1) < cleanedStrParts.size()) {
            ifMatchesFillDateData(ib, cleanedStrParts.get(i + 1), HOURS_MINS_SECS_PATTERN, I_B_FIELD.HOURS, I_B_FIELD.MINS, I_B_FIELD.SECS);
        }

        // ----- FULL TIME (split) HH MM SS
        if (!ib.hasValidTime() && (i + 3) < cleanedStrParts.size()) {   //NOSONAR
            ifMatchesFillDateData(ib, cleanedStrParts.get(i + 1), HOURS_PATTERN, I_B_FIELD.HOURS);
            ifMatchesFillDateData(ib, cleanedStrParts.get(i + 2), MINS_SECS_PATTERN, I_B_FIELD.MINS);
            ifMatchesFillDateData(ib, cleanedStrParts.get(i + 3), MINS_SECS_PATTERN, I_B_FIELD.SECS);
        }

        return ib.build(); //PARTIAL MATCH, FULL DATE DETECTED (without time but better than nothing)
    }

    private static InstantBuilder ifMatchesFillDateData(InstantBuilder ib, String str, Pattern pattern, I_B_FIELD... fields) {
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) return ib;
        if (fields.length != matcher.groupCount()) throw new PhotoMementoError("This is unexpected expects matching fields equals to found date parts");
        if (ib == null) ib = new InstantBuilder();
        for (int i = 0; i < fields.length; i++)
            ib.setField(fields[i], matcher.group(i + 1));
        return ib;
    }

    public enum I_B_FIELD {YEAR, MONTH, DAY_OF_MONTH, HOURS, MINS, SECS, MILLIS}

    @Setter
    @NoArgsConstructor
    private static class InstantBuilder {

        private String year;
        private String month;
        private String dayOfMonth;
        private String hours;
        private String mins;
        private String secs;
        private String millis;

        public void setField(I_B_FIELD f, String val) {
            switch (f) {
                case YEAR:
                    setYear(val);
                    break;
                case MONTH:
                    setMonth(val);
                    break;
                case DAY_OF_MONTH:
                    setDayOfMonth(val);
                    break;
                case HOURS:
                    setHours(val);
                    break;
                case MINS:
                    setMins(val);
                    break;
                case SECS:
                    setSecs(val);
                    break;
                case MILLIS:
                    setMillis(val);
                    break;
            }
        }

        Instant build() {
            String byear = this.year;
            String bmonth = this.month;
            String bdayOfMonth = this.dayOfMonth;

            String bhours = Optional.ofNullable(this.hours).orElse("00");
            String bmins = Optional.ofNullable(this.mins).orElse("00");
            String bsecs = Optional.ofNullable(this.secs).orElse("00");
            String bmillis = Optional.ofNullable(this.millis).orElse("000");

            if (!StringUtils.hasText(byear) || !StringUtils.hasText(bmonth) || !StringUtils.hasText(bdayOfMonth)) return null;

            String utzStr = String.format("%s-%s-%sT%s:%s:%s.%sZ", byear, bmonth, bdayOfMonth, bhours, bmins, bsecs, bmillis);

            return Instant.parse(utzStr);
        }

        public boolean hasValidDate() {
            return StringUtils.hasText(year) || StringUtils.hasText(month) || StringUtils.hasText(dayOfMonth);
        }

        public boolean hasValidTime() {
            return StringUtils.hasText(hours) || StringUtils.hasText(mins) || StringUtils.hasText(secs);
        }

        public boolean hasValidFullDateTime() {
            return hasValidDate() && hasValidTime();
        }
    }

    //TODO: Remove later
    public static void main(String... args) {
        List<String> strs = List.of(
                "2034-01-16T07:48:18.154629Z.something",
                "201401160748181211221121212",
                "20190116 200038",
                "19800116 20 00 38",
                "19800116 20 00 38",
                "dsc 2021010498154629.jpg",
                "30 20340116 074818154629Z azk.something",
                "20200930-WA0026-98-0  099.mp4",
                "10_06.08.1997.mp4",
                "2014 01 16 07 48 18",
                "VID_20210705_162421"
        );
        strs.forEach(str -> {
            System.out.printf("%n-----------------------------------%n[original] %s%n", str);
            System.out.printf("[res] (clean: %s) -> %s%n%n", cleanString(str), detectDate(str));
        });
    }

    public static ZonedDateTime getZonedDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC);
    }

    public static int getYear(Instant instant) {
        return getZonedDateTime(instant).getYear();
    }

    /**
     * Elapsed Time
     */
    public static String getTimeElapsedHuman(long timeElapsed) {
        return getHumanTime(TimeUnit.MILLISECONDS.toSeconds(timeElapsed));
    }

    /**
     * Function that returns human style size
     */
    public static String getHumanTime(long s) {
        if (TimeUnit.SECONDS.toMinutes(s) == 0)
            return String.format(SECONDS_HUMAN, s);
        else if (TimeUnit.SECONDS.toHours(s) == 0) {
            long m = TimeUnit.SECONDS.toMinutes(s);
            return String.format(MINUTES_HUMAN, m);
        } else if (TimeUnit.SECONDS.toDays(s) == 0) {
            long h = TimeUnit.SECONDS.toHours(s);
            long m = TimeUnit.SECONDS.toMinutes(s - TimeUnit.HOURS.toSeconds(h));
            return m > 0 ?
                    String.format(HOURS_MINUTES_HUMAN, h, m) :
                    String.format(HOURS_HUMAN, h);
        }
        long d = TimeUnit.SECONDS.toDays(s);
        long h = TimeUnit.SECONDS.toHours(s - TimeUnit.DAYS.toSeconds(d));
        return h > 0 ?
                String.format(DAYS_HOURS_HUMAN, d, h) :
                String.format(DAYS_HUMAN, d);
    }
}
