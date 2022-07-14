package com.bkprojects.soundsleep;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtil {
    /**
     * Converts the out time of date picker to a user friendly readable time like 04:00 PM
     *
     * @param calendar The calender object
     * @return String
     */
    public static String getTimeIn12Hours(Calendar calendar) {
        //TODO fix this hour issue warning, Is the HOUR not in 24 hour format
        int hour = calendar.get(Calendar.HOUR), minute = calendar.get(Calendar.MINUTE);
        String am_pm = "AM";
        String s_minute = Integer.toString(minute);
        String s_hour = Integer.toString(hour);

        if (hour == 0) {
            s_hour = Integer.toString(12);
        } else if (hour > 12) {
            hour = hour - 12;
            s_hour = Integer.toString(hour);
            am_pm = "PM";
        }
        if (hour < 10 && hour != 0) {
            s_hour = "0" + hour;
        }
        if (minute < 10) {
            s_minute = "0" + minute;
        }
        return s_hour + ":" + s_minute + " " + am_pm;
    }

    /**
     * Overloaded method of getTimeIn12Hours, which accepts a datetime string which was formatted and saved.
     *
     * @param datetimeString The datetime string stored
     * @param defaultTime The default time value which is used across application
     * @return Returns time in UI friendly string i.e. 04:00 PM
     * @throws ParseException Exception
     */
    public static String getTimeIn12Hours(String datetimeString, String defaultTime) throws ParseException {
        if (datetimeString.equalsIgnoreCase(defaultTime)) {
            return defaultTime;
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        calendar.setTime(dateFormat.parse(datetimeString));
        return getTimeIn12Hours(calendar);
    }

    /**
     * Takes a Time String in Hour:Minutes AM/PM (Ex 04:12 AM) representation and
     * returns a calender object of that representation
     *
     * @param time Time in 12 Hours string format
     * @return Calendar
     */
    public static Calendar getTimeInCalender(String time) {
        Calendar calendar = Calendar.getInstance();
        String[] timeSplit = time.split(":");
        calendar.set(Calendar.HOUR, Integer.parseInt(timeSplit[0]));
        timeSplit = timeSplit[1].split(" ");
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeSplit[0]));
        if (timeSplit[1].equalsIgnoreCase("AM")) {
            calendar.set(Calendar.AM_PM, Calendar.AM);
        } else {
            calendar.set(Calendar.AM_PM, Calendar.PM);
        }
        return calendar;
    }

    /**
     * This method is to facilitate the UI. As the time stored in buttons is in a user friendly string, we first need to convert it in a date time string
     * the method also ensures, that start time is of current date whereas end time is of future
     *
     * @param time The Time string from the UI which is user friendly
     * @return a date time string
     */
    public static String getCalenderStringFromUIValue(String time, boolean isStartTime) {
        //Convert this time into calendar object
        Calendar calObj = getTimeInCalender(time);
        if (!isStartTime && calObj.before(Calendar.getInstance())) {
            calObj.add(Calendar.DAY_OF_YEAR, 1);
        }
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        return dateFormat.format(calObj.getTime());
    }

    /**
     * Get the alarm time for the next day alarm, after the alarm for current day has been activated.
     *
     * @param datetime the timestamp stored in String.
     * @return returns the same time but after adding 1 day
     */
    public static String getNextAlarmTime(String datetime) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        calendar.setTime(dateFormat.parse(datetime));
        //Add 1 day to current time it is being set for next day
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dateFormat.format(calendar.getTime());
    }
}
