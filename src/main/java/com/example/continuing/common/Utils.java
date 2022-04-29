package com.example.continuing.common;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
	private static final SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static boolean isAllDoubleSpace(String s) {
        if (s == null || s.equals("")) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '　') { // 全角SPACE
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(String s) {
        if (s == null || s.equals("")) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '\t') { // 半角SPACE
                return false;
            }
        }
        return true;
    }
    
    public static int strToInt(String time) {
    	int hour = Integer.valueOf(time.substring(0, 2));
    	int minute = Integer.valueOf(time.substring(3, 5));
    	return hour * 60 + minute;
    }
    
    public static Date strToDate(String s) {
    	Date date = null;
        try {
            long ms = sdf.parse(s).getTime();
            // yyyy-MM-ddで解釈できた場合
            date = new Date(ms);

        } catch (ParseException e) {
            // 変換できなかった場合
            // date は null のまま
        }
        
        return date;
    }
    
    public static boolean checkTimeFormat(String s) {
        try {
            stf.parse(s).getTime();
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    public static Time strToTime(String s) {
    	Time time = null;
    	try {
            long ms = stf.parse(s).getTime();
            // HH:mm で解釈できた場合
            time = new Time(ms);

        } catch (ParseException e) {
            // 変換できなかった場合
            // time は null のまま
        	e.printStackTrace();
        }
        
        return time;
    }
    
    public static String dateToStr(Date date) {
    	return sdf2.format(date);
    }
    
    public static String timeToStr(Time time) {
    	return stf.format(time);
    }
    
    public static Timestamp dateAndTimeToTimestamp(Date date, Time time) {
    	String datetime = date + " " + time;
        
    	long ms = new Date(System.currentTimeMillis()).getTime(); 
        try {
        	ms = sdtf.parse(datetime).getTime();
        } catch (ParseException e){
            e.printStackTrace();
        }

        return new Timestamp(ms);
    }

    public static Timestamp timestampNow() {
        java.util.Date date = new java.util.Date();
        return new Timestamp(date.getTime());
    }
}
