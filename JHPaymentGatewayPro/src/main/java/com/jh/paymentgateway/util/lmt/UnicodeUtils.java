package com.jh.paymentgateway.util.lmt;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;

import net.sf.json.JSONObject;

public class UnicodeUtils {
	public static  String toUnicode(String input) {
        input = input.trim();
        String output = escape(input).toLowerCase().replace("%u", "\\u");
        return output.replaceAll("(?i)%7b", "{").replaceAll("(?i)%7d", "}").replaceAll("(?i)%3a", ":")
                .replaceAll("(?i)%2c", ",").replaceAll("(?i)%27", "'").replaceAll("(?i)%22", "\"")
                .replaceAll("(?i)%5b", "[").replaceAll("(?i)%5d", "]").replaceAll("(?i)%3D", "=")
                .replaceAll("(?i)%20", " ").replaceAll("(?i)%3E", ">").replaceAll("(?i)%3C", "<")
                .replaceAll("(?i)%3F", "?").replaceAll("(?i)%5c", "\\");
    }
 public static String escape(String input) {
        int len = input.length();
        int i;
        char j;
        StringBuffer result = new StringBuffer();
        result.ensureCapacity(len * 6);
        for (i = 0; i < len; i++) {
            j = input.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)) {
                result.append(j);
            } else if (j < 256) {
                result.append("%");
                if (j < 16) {
                    result.append("0");
                }
                result.append(Integer.toString(j, 16));
            } else {
                result.append("%u");
                result.append(Integer.toString(j, 16));
            }
        }
        return result.toString();

    }
 	
/* public static void main(String[] args) {
	
	String province = "广东省"; 
	String city = "广州市"; 
	String area = "天河区";
	String provinceCode = "123";
	String cityCode = "123";
	String areaCode = "123";
	
	Map<String, Object> addressMaps = new TreeMap<String, Object>();
	addressMaps.put("province",UnicodeUtils.toUnicode(province));
	addressMaps.put("province_code",provinceCode);
	addressMaps.put("city",UnicodeUtils.toUnicode(city));
	addressMaps.put("city_code",cityCode);
	addressMaps.put("area",UnicodeUtils.toUnicode(area));
	addressMaps.put("area_code",areaCode);
	
	Object json = JSONObject.fromObject(addressMaps);
	
	String unescapeJavaScript = StringEscapeUtils.unescapeJavaScript(json.toString());
	
	System.out.println(unescapeJavaScript);
	 
	 
}*/
 
}
