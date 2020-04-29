package com.jh.paymentgateway.util.xs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author:Chand
 * @Date:2016/3/29
 * @Description:
 */
@Component
public class Config {
	
    public static String BASE_URL     = "";
    public static String ORGNO        = "";
    public static String MERNO        = "";
    public static String KEY          = "";
    public static String PRIVATE_PATH = "";
    
    @Value("${xs.base_url}")
	public void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}
    @Value("${xs.orgno}")
	public void setORGNO(String oRGNO) {
		ORGNO = oRGNO;
	}
    @Value("${xs.merno}")
	public void setMERNO(String mERNO) {
		MERNO = mERNO;
	}
    @Value("${xs.key}")
	public void setKEY(String kEY) {
		KEY = kEY;
	}
    @Value("${xs.private_path}")
	public void setPRIVATE_PATH(String pRIVATE_PATH) {
		PRIVATE_PATH = pRIVATE_PATH;
	}
    
    
}
