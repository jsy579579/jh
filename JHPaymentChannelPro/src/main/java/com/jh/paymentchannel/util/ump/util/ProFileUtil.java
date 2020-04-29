package com.jh.paymentchannel.util.ump.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ***********************************************************************
 * <br>description : 配置文件解析类
 * @author      umpay
 * @date        2014-8-1 上午09:31:01
 * @version     1.0  
 ************************************************************************
 */
public class ProFileUtil {
	private final static String fileName = "SignVerProp.properties";
	private final static String pro_url_pix = "http://pay.soopay.net";
	private final static String pro_url_wap_pix = "plat.wap.url";
	
	public static byte[] getFileByte(String pro)throws IOException{
		byte[] b = null;
		InputStream in = null;
		try{
			String filepath =pro;
			if(null == filepath)throw new RuntimeException("没有找到配置信息"+pro);
			in = new FileInputStream(new File(filepath));
			if(null == in)throw new RuntimeException("文件不存在"+filepath);
			b = new byte[20480];
			in.read(b);
		}finally{
			if(null!=in)in.close();
		}
		return b;
	}
	
	/**
	 * 
	 * <br>description : 获取配置文件key值
	 * @param pro
	 * @return
	 * @version     1.0
	 * @date        2014-8-1上午09:31:41
	 */
	public static String getPro(String pro){
		InputStream in = null;
		try{
			Properties prop = new Properties();
			in = ProFileUtil.class.getClassLoader().getResourceAsStream(fileName); 
			if(null == in)throw new RuntimeException("没有找到配置文件"+fileName);
			prop.load(in);
			in.close();
			return StringUtil.trim(prop.getProperty(pro));
		}catch(Exception ex){
			RuntimeException rex = new RuntimeException(ex.getMessage());
			rex.setStackTrace(ex.getStackTrace());
			throw rex;
		}finally{
			if(null!=in){
				try{
					in.close();
				}catch(Exception ex){
					RuntimeException rex = new RuntimeException(ex.getMessage());
					rex.setStackTrace(ex.getStackTrace());
					throw rex;
				}
			}
			
		}
	}
	
	/**
	 * 获取平台请求地址
	 * @return
	 */
	public static String getUrlPix(){
		return pro_url_pix;
	}
	
}
