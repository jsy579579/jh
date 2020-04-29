package com.jh.paymentchannel.util.xj;  
  
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;  
  
/** 
 * HttpClient GET POST PUT 请求 
 * @author huang 
 * @date 2013-4-10 
 */  
public class HttpRequest  
{  
  
    private static HttpRequest httpRequst=null;     
    private HttpRequest(){}    
    public static HttpRequest getInstance(){  
        if(httpRequst==null){  
            synchronized(HttpRequest.class){  
                if(httpRequst == null){  
                    httpRequst=new HttpRequest();  
                }  
            }    
        }  
        return httpRequst;  
    }  
      
    /** 
     * HttpClient GET请求 
     * @author huang 
     * @date 2013-4-9 
     * @param uri 
     * @return resStr 请求返回的JSON数据 
     */  
    public String doGet(String url){  
        String resStr = null;  
        HttpClient htpClient = new HttpClient();  
        GetMethod getMethod = new GetMethod(url);  
        getMethod.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());       
        try{  
            int statusCode = htpClient.executeMethod( getMethod );  
//            System.out.println(statusCode);  
            if(statusCode != HttpStatus.SC_OK){                
                System.out.println("Method failed: "+getMethod.getStatusLine());  
                return resStr;  
            }             
            byte[] responseBody = getMethod.getResponseBody();           
            resStr = new String(responseBody,"utf-8");  
        } catch (HttpException e) {  
            System.out.println("Please check your provided http address!");  //发生致命的异常，可能是协议不对或者返回的内容有问题  
        } catch (IOException e) {  
            System.out.println( "Network anomaly" );  //发生网络异常  
        }finally{  
            getMethod.releaseConnection(); //释放连接  
        }  
        return resStr;  
    }  
      
    /**
     * 向指定URL发送POST方法的请求
     * @param url   发送请求的URL
     * @return URL   所代表远程资源的响应结果
*/

    public static String httpPost(String reqUrl, String content, Map<String, String> headers){
        return httpRequest(reqUrl, "POST", content, headers);
    }


    public static String httpRequest(String reqUrl, String method, String content, 
    Map<String, String> headers){
        URL url;
        try {
            // 打开和URL之间的连接
            url = new URL(reqUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            // Read from the connection. Default is true.
            connection.setDoInput(true);
            // Set the post method. Default is GET
            connection.setRequestMethod(method);
            // Post cannot use caches
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection
                    .getOutputStream());
            // The URL-encoded contend
            out.writeBytes(content); 
            out.flush();
            out.close(); // flush and close
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(),"utf-8"));
            StringBuilder sb = new StringBuilder();
            String line="";
            while ((line = reader.readLine()) != null){
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            return sb.toString();
        } catch (IOException e) {
            System.out.println("error while posting request: "+ e.getMessage());
            return null;
        } 
    }
      
    /** 
     * HttpClient PUT请求 
     * @author huang 
     * @date 2013-4-10 
     * @return 
     */  
    @SuppressWarnings( "deprecation" )  
    public String doPut(String url,String jsonObj){  
        String resStr = null;  
        HttpClient htpClient = new HttpClient();  
        PutMethod putMethod = new PutMethod(url);  
        putMethod.addRequestHeader( "Content-Type","application/json" );  
        putMethod.getParams().setParameter( HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8" );  
        putMethod.setRequestBody( jsonObj );  
        try{  
            int statusCode = htpClient.executeMethod( putMethod );  
//            System.out.println(statusCode);  
            if(statusCode != HttpStatus.SC_OK){  
                System.out.println("Method failed: "+putMethod.getStatusLine());  
                return null;  
            }    
            byte[] responseBody = putMethod.getResponseBody();           
            resStr = new String(responseBody,"utf-8");  
        }catch(Exception e){  
            e.printStackTrace();  
        }finally{  
            putMethod.releaseConnection();  
        }  
        return resStr;  
    }  
}  