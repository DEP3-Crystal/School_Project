package utils;

import java.sql.Timestamp;

public class TimeUtils {

    public static Timestamp getValueOfTimestamp(String date) {
        return Timestamp.valueOf(date);
    }
}
