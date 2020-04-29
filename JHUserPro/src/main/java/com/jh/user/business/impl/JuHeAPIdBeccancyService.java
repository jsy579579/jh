package com.jh.user.business.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.jh.user.util.MD5Util;


public class JuHeAPIdBeccancyService {

	/**
	 * 城市列表
	 * 
	 * **/
	public static String wzdjCitylist(String key){
		
		 String url =  "http://v.juhe.cn/wzdj/citylist.php?key="+key;
		
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	/**
	 * 违章查询
	 * */
	public static String wzdjQuerywz(String carNo, String frameNo, String enginNo ,String key ,String carType ,String provinceid,String cityid){
		
		
		 String url =  "http://v.juhe.cn/wzdj/querywz.php?carNo="+carNo+"&frameNo="+frameNo+"&enginNo="+enginNo+"&carType="+carType+"&provinceid="+provinceid+"&cityid="+cityid+"&key="+key;
		
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	/**
	 *.提交订单
	 * 
	 * */
	public static String wzdjSubmitOrder(String recordIds ,String carNo, String contactName, String tel, String userOrderId,String key){
		
		 String url =  "http://v.juhe.cn/wzdj/submitOrder.php?recordIds="+recordIds+"&carNo="+carNo+"&contactName="+contactName+"&tel="+tel+"&userOrderId="+userOrderId+"&key="+key;
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
	/**
	 *.订单支付
	 * 
	 * */
	public static String wzdjPayOrder(String userOrderId ,String key){
		
		 String url =  "http://v.juhe.cn/wzdj/payOrder.php?userOrderId="+userOrderId+"&key="+key;
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
	/**
	 *.证件上传
	 * 
	 * */
	public static String wzdjUpload(String userOrderId ,String ownerName, String contactName, File file,String key){
		
		 String url =  "http://v.juhe.cn/wzdj/submitOrder.php?userOrderId="+userOrderId+"&ownerName="+ownerName+"&contactName="+contactName+"&file="+file+"&key="+key;
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	  /** 
     * 上传图片 
     *  
     * @param urlStr 
     * @param textMap 
     * @param fileMap 
     * @return 
     */  
    public static String formUpload(String key, Map<String, String> textMap,  
            Map<String, MultipartFile> fileMap) {  
    	 String urlStr = "http://v.juhe.cn/wzdj/upload.php?key="+key;  
        String res = "";  
        HttpURLConnection conn = null;  
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符  
        try {  
            URL url = new URL(urlStr);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(5000);  
            conn.setReadTimeout(30000);  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Connection", "Keep-Alive");  
            conn.setRequestProperty("User-Agent",  
                            "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");  
            conn.setRequestProperty("Content-Type",  
                    "multipart/form-data; boundary=" + BOUNDARY);  
   
            OutputStream out = new DataOutputStream(conn.getOutputStream());  
            // text  
            if (textMap != null) {  
                StringBuffer strBuf = new StringBuffer();  
                Iterator iter = textMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry entry = (Map.Entry) iter.next();  
                    String inputName = (String) entry.getKey();  
                    String inputValue = (String) entry.getValue();  
                    if (inputValue == null) {  
                        continue;  
                    }  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append(  
                            "\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\""  
                            + inputName + "\"\r\n\r\n");  
                    strBuf.append(inputValue);  
                }  
                out.write(strBuf.toString().getBytes());  
            }  
   
            // file  
            if (fileMap != null) {  
                Iterator iter = fileMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry entry = (Map.Entry) iter.next();  
                    String inputName = (String) entry.getKey();  
                    MultipartFile file = (MultipartFile) entry.getValue();  
                    if (file == null) {  
                        continue;  
                    }  
//                    File file = new File(inputValue);  
                    String filename = file.getName();  
                    String contentType =file.getContentType();  
                    if (filename.endsWith(".jpg")) {  
                        contentType = "image/jpeg";  
                    }  
                    if (contentType == null || contentType.equals("")) {  
                        contentType = "application/octet-stream";  
                    }  
   
                    StringBuffer strBuf = new StringBuffer();  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append(  
                            "\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\""  
                            + inputName + "\"; filename=\"" + filename  
                            + "\"\r\n");  
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");  
   
                    out.write(strBuf.toString().getBytes());  
   
                    DataInputStream in = new DataInputStream(file.getInputStream());  
                    int bytes = 0;  
                    byte[] bufferOut = new byte[1024];  
                    while ((bytes = in.read(bufferOut)) != -1) {  
                        out.write(bufferOut, 0, bytes);  
                    }  
                    in.close();  
                }  
            }  
   
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
            out.write(endData);  
            out.flush();  
            out.close();  
   
            // 读取返回数据  
            StringBuffer strBuf = new StringBuffer();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(  
                    conn.getInputStream()));  
            String line = null;  
            while ((line = reader.readLine()) != null) {  
                strBuf.append(line).append("\n");  
            }  
            res = strBuf.toString();  
            reader.close();  
            reader = null;  
        } catch (Exception e) {  
            System.out.println("发送POST请求出错。" + urlStr);  
            e.printStackTrace();  
        } finally {  
            if (conn != null) {  
                conn.disconnect();  
                conn = null;  
            }  
        }  
        return res;  
    }  
	/**
	 *.订单详情
	 * 
	 * */
	public static String wzdjOrderDetail(String userOrderId ,String key){
		
		 String url =  "http://v.juhe.cn/wzdj/orderDetail.php?userOrderId="+userOrderId+"&key="+key;
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
	/**
	 *.配置回调地址
	 * 
	 * */
	public static String wzdjCallbackConfig(String callbackurl ,String key){
		
		 String url =  "http://v.juhe.cn/wzdj/callbackConfig.php?callbackurl="+callbackurl+"&key="+key;
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
}
