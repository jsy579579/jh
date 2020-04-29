package com.jh.paymentgateway.util.hlb;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Component
public class MyBeanUtils extends BeanUtils {

    @Value("${hlb.key}")
    private String key;

    private MyBeanUtils(){ }

    public Map convertBean(Object bean, Map retMap)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
        }
        for (Field f : fields) {
            String key = f.toString().substring(f.toString().lastIndexOf(".") + 1);
            Object value = f.get(bean);
            if(value == null)
                value = "";
            retMap.put(key, value);
        }
        return retMap;
    }

    public String getSigned(Map<String, String> map, String[] excludes){
        StringBuffer sb = new StringBuffer();
        Set<String> excludeSet = new HashSet<String>();
        excludeSet.add("sign");
        if(excludes != null){
            for(String exclude : excludes){
                excludeSet.add(exclude);
            }
        }
        for(String key : map.keySet()){
            if(!excludeSet.contains(key)){
                String value = map.get(key);
                value = (value == null ? "" : value);
                sb.append("&");
                sb.append(value);
            }
        }
        sb.append("&");
        sb.append(key);
        return sb.toString();
    }

    public String getSigned(Object bean, String[] excludes) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        Map map  = convertBean(bean, new LinkedHashMap());
        String signedStr = getSigned(map, excludes);
        return signedStr;
    }

}
