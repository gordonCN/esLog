package com.log.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class StringUtils {
	
	
	
	public static String trim(String str) {
        return str == null ? null : str.trim();
    }
	public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 生成UUID的方法
     * @return
     */
    public static String getUUID(){
      UUID uuid=UUID.randomUUID();
      String str = uuid.toString(); 
      String uuidStr=str.replace("-", "");
      return uuidStr;
    }
    /**
     * 获取daystr
     * 
     * @param num
     * @return
     */
    public static String getDay(int num) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(Calendar.DATE, num);
      Date zero = calendar.getTime();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      return format.format(zero);
    }
    
    
}
