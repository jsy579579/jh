package com.jh.paymentchannel.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;




public class HttpUtils {

    public static byte[] sendPost(String urlStr, String request, String encode,int timeOutInSeconds) throws Exception {
        HttpURLConnection http = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(urlStr);
            http = (HttpURLConnection)url.openConnection();
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setUseCaches(false);
            http.setConnectTimeout(timeOutInSeconds*1000);//�������ӳ�ʱ
            http.setReadTimeout(timeOutInSeconds*1000);//���ö�ȡ��ʱ
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="+encode);
            http.connect();

            outputStream = http.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(outputStream, encode);
            osw.write(request);
            osw.flush();
            osw.close();

            if (http.getResponseCode() == 200) {
                inputStream = http.getInputStream();
                byte[] returnValue1 = IOUtils.toByteArray(inputStream);
                return returnValue1;
            }else{
                throw new RuntimeException("http read ["+http.getResponseCode()+"]");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (http != null) http.disconnect();
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
    
    
    
    public static String doGet(String urlStr)     
    {    
        URL url = null;    
        HttpURLConnection conn = null;    
        InputStream is = null;    
        ByteArrayOutputStream baos = null;    
        try   
        {    
            url = new URL(urlStr);    
            conn = (HttpURLConnection) url.openConnection();    
            /*conn.setReadTimeout("10000");    
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);*/    
            conn.setRequestMethod("GET");    
            conn.setRequestProperty("accept", "*/*");    
            conn.setRequestProperty("connection", "Keep-Alive");    
            if (conn.getResponseCode() == 200)    
            {    
                is = conn.getInputStream();    
                baos = new ByteArrayOutputStream();    
                int len = -1;    
                byte[] buf = new byte[128];    
     
                while ((len = is.read(buf)) != -1)    
                {    
                    baos.write(buf, 0, len);    
                }    
                baos.flush();    
                return baos.toString();    
            } else   
            {    
                throw new RuntimeException(" responseCode is not 200 ... responseCode is :" + conn.getResponseCode());    
            }    
     
        } catch (Exception e)    
        {    
            e.printStackTrace();    
        } finally   
        {    
            try   
            {    
                if (is != null)    
                    is.close();    
            } catch (IOException e)    
            {    
            }    
            try   
            {    
                if (baos != null)    
                    baos.close();    
            } catch (IOException e)    
            {    
            }    
            conn.disconnect();    
        }    
             
        return null ;    
     
    } 
    
    
}
