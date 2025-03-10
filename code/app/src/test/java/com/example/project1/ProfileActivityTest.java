package com.example.project1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


class TimestampUtil {
    public static long convertTimestampToMillis(String timestampStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestampStr);
            return (date != null) ? date.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }
}


public class ProfileActivityTest {

    @Test
    public void testConvertTimestampToMillis_valid() {
        String validTimestamp = "12:00 PM - January 01, 2020";
        long expected = 1577880000000L;
        long actual = TimestampUtil.convertTimestampToMillis(validTimestamp);
        assertEquals("Valid timestamp should be converted correctly", expected, actual);
    }

    @Test
    public void testConvertTimestampToMillis_invalid() {
        String invalidTimestamp = "invalid timestamp";
        long actual = TimestampUtil.convertTimestampToMillis(invalidTimestamp);
        assertEquals("Invalid timestamp should return 0", 0L, actual);
    }
}
