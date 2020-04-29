package cn.jh.common.interceptor;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.jh.common.utils.RequestUtil;
import cn.jh.common.utils.UserInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 拦截器
 */
public class AuthenticationHeaderInterceptor implements HandlerInterceptor {
	
	private static Logger log = LoggerFactory.getLogger(AuthenticationHeaderInterceptor.class);
	private static ObjectMapper objectMapper;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		
		UserInfo userInfo = null;
		String userInfoJson = request.getHeader("user-info");
//    	log.info(String.format("AuthenticationHeaderInterceptor userInfoJson: %s", userInfoJson));
    	
    	if (userInfoJson != null && !userInfoJson.trim().equals("")) {
    		
    		try {
    			userInfoJson=URLDecoder.decode(userInfoJson,"utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
    		log.info(String.format("AuthenticationHeaderInterceptor userInfoJson  utf-8: %s", userInfoJson));
    		
    		
    		try {
    			userInfo = objectMapper.readValue(userInfoJson, UserInfo.class);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		RequestUtil.setupUserInfo(userInfoJson, userInfo);
    	}
    	
//    	log.info(String.format("AuthenticationHeaderInterceptor userInfo: %s", userInfo));
        
        return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}
