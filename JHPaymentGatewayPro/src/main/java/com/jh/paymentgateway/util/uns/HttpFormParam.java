package com.jh.paymentgateway.util.uns;

import java.io.IOException;  
import java.io.InputStream;  
import java.io.PrintWriter;  
import java.net.HttpURLConnection;  
import java.net.URL;  
import java.net.URLConnection;  
import java.net.URLEncoder;  
import java.util.Map;  
import java.util.Scanner;  

public class HttpFormParam { 
	/**
	 * post form 请求
	 * @param urlString  请求地址
	 * @param nameValuePairs  请求参数
	 * @return 
	 * @throws IOException
	 */
    public static String doPost(String urlString, Map<String, String> nameValuePairs)  
            throws IOException {  
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true); 
        try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {  
            boolean first = true;  
            for (Map.Entry<String, String> pair : nameValuePairs.entrySet()) {  
                if (first)  
                    first = false;  
                else  
                    out.print('&');  
                	String name = pair.getKey().toString();  
                	String value = pair.getValue().toString();  
                	out.print(name);  
                	out.print('=');  
                	out.print(URLEncoder.encode(value, "UTF-8"));  
            }   
        }  
	    StringBuffer response = new StringBuffer(""); 
        try (Scanner in = new Scanner(connection.getInputStream())) {  
            while (in.hasNextLine()) {
            	String str = new String(in.nextLine().getBytes(),"UTF-8");
            	response.append(str);  
            	response.append("\n");  
            }  
        } catch (IOException e) {  
            if (!(connection instanceof HttpURLConnection))  
                throw e;  
            InputStream err = ((HttpURLConnection) connection).getErrorStream();  
            if (err == null)  
                throw e;  
            Scanner in = new Scanner(err);
            String str = new String(in.nextLine().getBytes(),"UTF-8");
            response.append(str);  
            response.append("\n");  
            in.close();  
        }  
        return response.toString();  
    }  
}
