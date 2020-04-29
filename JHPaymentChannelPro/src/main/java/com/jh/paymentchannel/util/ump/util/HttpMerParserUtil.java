/* *****************  JAVA头文件说明  ****************
 * file name  :  HttpMerParser.java
 * owner      :  zhangwl
 * copyright  :  UMPAY
 * description:  
 * modified   :  Dec 1, 2009
 * *************************************************/ 

package com.jh.paymentchannel.util.ump.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jh.paymentchannel.util.ump.log.ILogger;
import com.jh.paymentchannel.util.ump.log.LogManager;

/**
 * ***********************************************************************
 * <br>description : http数据解析工具类
 * @author      umpay
 * @date        2014-7-25 上午10:19:47
 * @version     1.0  
 ************************************************************************
 */
public class HttpMerParserUtil{
	
	private static final ILogger log = LogManager.getLogger();

	/**
	 * 
	 * <br>description : 对通知商户的HTML文档进行解析(通知数据以&分隔,数据以键值对出现key1=value1&key2=value2...keyn=valuen),并生成对应的键值数据
	 * @param html 待解析HTML文档
	 * @return
	 * @version     1.0
	 * @date        2014-7-25上午10:24:20
	 */
	public static Map getDataByHtml(String html){
		String meta = getMeta(html);
		log.info("getDataByHtml content=" + meta);
		return getDataByContent(meta);
	}	
	
	/**
	 * 
	 * <br>description : 解析通知商户的HTML文档,获取meta标签中的内容
	 * @param html 待解析的HTML文档
	 * @return
	 * @version     1.0
	 * @date        2014-7-25上午10:24:57
	 */
	public static String getMeta(String html){
		StringBuffer sb = new StringBuffer(html);
		log.info("请求解析Html内容:"+sb);
		try {
			while (true) {
				// find ">"
				int ei = sb.indexOf(">");
				if (ei == -1)
					return null;
				String str = sb.substring(0, ei + 1);
				sb.delete(0, ei + 1);
				// find "<"
				int si = str.indexOf("<");
				if (si == -1)
					continue;
				// 获得字符串"<...>"
				str = str.substring(si);
				// 删除 字符串"<...>"中的回车换行\r\n \r \n都换成" "
				str = str.replace('\r', ' ');
				str = str.replace('\n', ' ');
				StringBuffer sbf = new StringBuffer(str);
				while ((sbf.charAt(1)) == ' ') {
					sbf.delete(1, 2);
				}
				// 查找第一个单词后的空格，并取出第一个单词
				ei = sbf.indexOf(" ");
				if (ei < 0)
					ei = sbf.indexOf(">");
				if (ei == -1)
					continue;
				String tag = sbf.substring(1, ei);
				if (tag.equalsIgnoreCase("meta")) {
					int index = sbf.indexOf(" CONTENT");
					if (index == -1)
						continue;
					sbf.delete(0, index);
					index = sbf.indexOf("=");
					sbf.delete(0, index);
					index = sbf.indexOf("\"");
					sbf.delete(0, index + 1);
					index = sbf.lastIndexOf("\"");
					sbf.delete(index, sbf.length());
					log.info("content=" + sbf.toString());
					return sbf.toString();
				}
			}
		} catch (Exception e) {
			StackTraceElement[] ste = e.getStackTrace();
			StringBuffer sb1 = new StringBuffer();
			sb1.append(e.getMessage() + "\r\n");
			for (int i = 0; i < ste.length; i++) {
				sb1.append(ste[i].toString() + "\r\n");
			}
			return null;
		}
	}
	
	/**
	 * <br>description : 对以&符分隔,数据以键值对形式出现的商户通知数据进行解析,并生成对应的键值数据
	 * @param meta 待解析通知数据例:merId=9996&amtType=1&retCode=1001&retMsg=zOG9u8r9vt1bP13R6dakyqew3A==&versin=3.0
	 * @return
	 * @version     1.0
	 * @date        2014-7-25上午10:26:14
	 */
	private static Map getDataByContent(String meta){
		Map map = new LinkedHashMap();		
		String[] data = splitMeta(meta,"&");
		for(int i=0;i<data.length;i++){
			String value = data[i];
			String[] temp = value.split("=");
			if(temp.length>=2){
				map.put(temp[0], value.substring(value.indexOf("=")+1, value.length()));
			}else if(temp.length==1){
				map.put(temp[0], "");
			}
		}
		return map;
	}

	/**
	 * 根据输入流获取响应HTML
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String getHtml(InputStream in) throws IOException{
		StringBuffer buff = new StringBuffer();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while((line=reader.readLine())!=null){
				line = new String(line.getBytes(),"UTF-8");
				buff.append(line);
			}
		}finally{
			if(reader!=null)reader.close();
		}
		return buff.toString();
	}
	
	/**
	 * 对meta数据根据分割标识进行切割,获取各数据值
	 * @param meta meta标签的content值
	 * @param splitStr 切割标识如:"|"和"&"
	 * @return 
	 */
	private static String[] splitMeta(String meta,String splitStr) {
	    if (meta.endsWith(splitStr)) meta += " ";
	    return meta.split("["+splitStr+"]");
	}
	/**
	 * 根据content获取明文串
	 * @param content
	 * @return
	 */
	public static String getPlain(String content){
		String plain = content.substring(0, content.lastIndexOf("|"));
		log.info("getPlain data: content=" + content + ",plain=" + plain);
		return plain;
	}
	/**
	 * 根据content获取密文串
	 * @param content
	 * @return
	 */
	public static String getSign(String content){
		String sign = content.substring(content.lastIndexOf("|")+1, content.length());
		log.info("getSign data: content=" + content + ",sign=" + sign);
		return sign;
	}		
}



