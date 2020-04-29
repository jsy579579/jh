package cn.jh.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * RequestUtil 工具类 不知道干啥用的
 */
public class RequestUtil {
	
	private static Logger log = LoggerFactory.getLogger(RequestUtil.class);

	private static ThreadLocal<Object[]> userInfoKeeper = new ThreadLocal<Object[]>();
	
	public static void setupUserInfo(String userInfoJson, UserInfo userInfo) {
		
		log.info(String.format("RequestUtil setupUserInfo - userInfoJson: %s", userInfoJson));
		log.info(String.format("RequestUtil setupUserInfo - userInfo: %s", userInfo));
		
		userInfoKeeper.set(new Object[]{userInfoJson, userInfo});
	}
	
	public static UserInfo getUserInfo(HttpServletRequest request) {
		Object[] data = (Object[])userInfoKeeper.get();
		UserInfo userInfo = null;
		if (data != null) {
			userInfo = (UserInfo)data[1];
		}
		log.info(String.format("RequestUtil getUserInfo: %s", userInfo));
    	return userInfo;
	}
	
	public static void setUserInfoHeader(HttpHeaders headers) {
		Object[] data = (Object[])userInfoKeeper.get();
		if (data != null) {
			String userInfoJson = (String)data[0];
			String encodedUserInfoJson = userInfoJson;
			try {
				encodedUserInfoJson = URLEncoder.encode(userInfoJson,"UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info(String.format("RequestUtil setUserInfoHeader - encodedUserInfoJson: %s", encodedUserInfoJson));
			headers.set("user-info", encodedUserInfoJson);
		}
	}

	
}

