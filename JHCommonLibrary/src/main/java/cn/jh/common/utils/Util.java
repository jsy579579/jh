package cn.jh.common.utils;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    @Autowired
    private LoadBalancerClient loadBalancer;

    /**
     * TODO: Complement this with a simpler version without fallback-url!
     *
     * @param serviceId
     * @param fallbackUri
     * @return
     */
    public URI getServiceUrl(String serviceId, String fallbackUri) {
        URI uri = null;
        try {
            ServiceInstance instance = loadBalancer.choose(serviceId);
            uri = instance.getUri();
            LOG.debug("Resolved serviceId '{}' to URL '{}'.", serviceId, uri);
        } catch (RuntimeException e) {
            e.printStackTrace();
            uri = URI.create(fallbackUri);
            LOG.warn("Failed to resolve serviceId '{}'. Fallback to URL '{}'.", serviceId, uri);
        }

        return uri;
    }
    
    public Date StrToDate(String str) {
    	  
    	   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	   Date date = null;
    	   try {
    	    date = format.parse(str);
    	   } catch (ParseException e) {
    	    e.printStackTrace();
    	   }
    	   return date;
    	}
    
    public String DateToStr(Date date) {
    	  
    	   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	   String str = format.format(date);
    	   return str;
    	} 

    public <T> ResponseEntity<T> createOkResponse(T body) {
        return createResponse(body, HttpStatus.OK);
    }

    public <T> ResponseEntity<T> createResponse(T body, HttpStatus httpStatus) {
        return new ResponseEntity<T>(body, httpStatus);
        
    }
    
    public String blogTrim(String blog){
    	
    	String str1="";
    	String str2="";
    	String str3="";
    	
    	//如果blog中不包含图片，则不用处理，直接返回
    	if(!blog.contains("src=")){
    		System.out.println("不包含src的blog"+blog);
    		return blog;
    	}
    	str1=blog.substring(0,blog.indexOf("src="));
    	str2=blog.substring(blog.indexOf("src="),blog.indexOf(">")).replace(" ", "+");
    	str3=blog.substring(blog.indexOf(">"));
    	
    	blog=str1+str2+str3;
    	
    	return blog;
    }
    

}
