package com.jh.paymentchannel.util.abroad;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Serialize {
	
	public static String toJosnDate(Object object){
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		objectMapper.setDateFormat(dateFormat);
        String result = "";
        try{
        	result = objectMapper.writeValueAsString(object);
        }catch(JsonProcessingException e){
        	e.printStackTrace();
        }
		return result;
	}
	public static <T>T parseJson(Class<T> t,String jsonStr){
		T result = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		objectMapper.setDateFormat(dateFormat);
		try {
			result = (T)objectMapper.readValue(jsonStr.trim(), t);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public String replaceSpecialtyStr(String str){
        String pattern="\\s*|\t|\r|\n";//去除字符串中空格、换行、制表  
        return Pattern.compile(pattern).matcher(str).replaceAll("");  
           
    }  
	public static Map<String, Object> parseCodeInfo(String paramStr) {
		Map<String,Object> attribute=new HashMap<String,Object>();
		String[] KeyValuePairs=paramStr.split("&");
		if(null!=KeyValuePairs&&KeyValuePairs.length>0){
			for(String strKeyValuePair:KeyValuePairs){
				String[] keyValuePair=strKeyValuePair.split("=");
				if(null!=keyValuePair&&keyValuePair.length>0){
					String key=keyValuePair[0];
					String value="";
					if(keyValuePair.length>1){
						value=keyValuePair[1];
					}
					attribute.put(key, value);
				}
			}
		}
		return attribute;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseJsonToMap(String jsonStr){
		Map<String, Object> result = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			result = objectMapper.readValue(jsonStr.trim(), Map.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseJsonToStringMap(String jsonStr){
		Map<String, String> result = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			result = objectMapper.readValue(jsonStr.trim(), Map.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String parseMapToJson(Object obj){
		String result = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		objectMapper.setDateFormat(dateFormat);
		try {
			result = objectMapper.writeValueAsString(obj);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
