package com.example.continuing.common;

import javax.servlet.http.HttpServletRequest;

public class Utils {

	/**
     * 引数が全角SPACEだけで構成されていればtrueを返す
     * 
     * @param s チェック対象
     * @return true:全角SPACEのみ, "", null false:左記以外
     */
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

    /**
     * 引数が"" or 半角SPACE/TABだけで構成されているならtrueを返す
     * 
     * @param s チェック対象
     * @return true:半角SPACE/TABのみ or "", null, false:左記以外
     */
    public static boolean isBlank(String s) {
        if (s == null || s.equals("")) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '\t') {// 半角SPACE
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
}
