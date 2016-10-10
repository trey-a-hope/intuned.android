package Services;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import Configuration.AppConfig;

public class DateTimeService {
    private final String LOGTAG = "TimeLineFragController";
    private boolean loggingOn = false;

    private static DateTimeService _dateTimeServiceInstance = new DateTimeService();

    public static DateTimeService getInstance() {
        return _dateTimeServiceInstance;
    }

    private DateTimeService() {
    }

    //Returns time difference between two DateTime objects as a string, i.e. "2 minutes ago."
    public String timeDifference(DateTime startDateTime, DateTime endDateTime){
        if(loggingOn) Log.v(LOGTAG, "StartDateTime : " + startDateTime.toString());
        if(loggingOn) Log.v(LOGTAG, "EndDateTime : " + endDateTime.toString());
        Duration duration = new Duration(startDateTime, endDateTime);
        //Get months.
        long months = duration.getStandardDays() / 30;
        if(loggingOn) Log.v(LOGTAG, String.valueOf("Months : " + months));
        if(months > 0){
            return String.valueOf(months) + (months == 1 ? " month" : " months");
        }
        //Get days.
        long days = duration.getStandardDays();
        if(loggingOn) Log.v(LOGTAG, String.valueOf("Days : " + days));
        if(days > 0){
            return String.valueOf(days) + (days == 1 ? " day" : " days");
        }
        //Get hours.
        long hours = duration.getStandardHours();
        if(loggingOn) Log.v(LOGTAG, String.valueOf("Hours : " + hours));
        if(hours > 0){
            return String.valueOf(hours) + (hours == 1 ? " hr" : " hrs");
        }
        //Get minutes.
        long minutes = duration.getStandardMinutes();
        if(loggingOn) Log.v(LOGTAG ,String.valueOf("Minutes : " + minutes));
        if(minutes > 0){
            return String.valueOf(minutes) + (minutes == 1 ? " min" : " mins");
        }
        //Get seconds.
        long seconds = duration.getStandardSeconds();
        if(loggingOn) Log.v(LOGTAG ,String.valueOf("Seconds : " + seconds));
        if(seconds > 0){
            return String.valueOf(seconds) + (seconds == 1 ? " sec" : " secs");
        }
        return "Just Now";
    }

    //Converts date string on firebase to DateTime object.
    public DateTime stringToDateTime(String dateString){
        if(dateString == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(AppConfig.DATE_TIME_FORMAT);
        DateTime dateTime = formatter.parseDateTime(dateString);
        return dateTime;
    }
}
