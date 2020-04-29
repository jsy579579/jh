package com.cardmanager.pro.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;  
	}
	
	  
    // Spring应用上下文环境  
    private static ApplicationContext applicationContext;  
  
    /** 
     * @return ApplicationContext 
     */  
    public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }  
  
    /** 
     * 获取对象 
     *  
     * @param name 
     * @return Object
     * @throws BeansException 
     */  
    public static Object getBean(String name) throws BeansException {  
        return applicationContext.getBean(name);  
    } 
    
    
    public static <T> T getBeanOfClass(Class<T> clazz) throws BeansException {  
    	return applicationContext.getBean(clazz);
    } 
    
    public static Object getBeanOfType(String name) throws BeansException {  
    	try {
			Map<String, ?> beansMap = applicationContext.getBeansOfType(Class.forName(name));
			Collection<?> valueSet = beansMap.values();
			ArrayList<?> valueList = new ArrayList<>(valueSet);
			return valueList.get(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
    }  

}
