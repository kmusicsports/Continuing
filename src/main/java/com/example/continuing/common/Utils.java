package com.example.continuing.common;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

public class Utils {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");

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
    
    public static String getHeaderPath(HttpServletRequest request) {
    	String header = request.getHeader("REFERER");
		String headerPath = header.substring(header.indexOf("/", 10));
		return headerPath;
    }
    
    public static int string2Int(String time) {
    	int hour = Integer.valueOf(time.charAt(0) + time.charAt(1));
    	int minute = Integer.valueOf(time.charAt(3) + time.charAt(4));
    	return hour * 60 + minute;
    }
    
    public static Date str2date(String s) {
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
    
    public static Time str2time(String s) {
    	Time time = null;
    	try {
            long ms = stf.parse(s).getTime();
            // HH:mm で解釈できた場合
            time = new Time(ms);

        } catch (ParseException e) {
            // 変換できなかった場合
            // time は null のまま
        }
        
        return time;
    }
}
