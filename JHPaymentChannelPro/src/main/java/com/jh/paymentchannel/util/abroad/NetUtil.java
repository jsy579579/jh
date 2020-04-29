package com.jh.paymentchannel.util.abroad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * 网络操作类.
 * 
 * @Description 提供网络操作的相关方法.
 */
public class NetUtil {
	
	/** 返回结果 */
	private String result;
	/** 输入流 */
//	private BufferedInputStream input;
	private BufferedReader input;
	
	/** http链接 */
	private HttpURLConnection conn;
	/** https链接 */
	private HttpsURLConnection sconn;
	/** 输出流 */
//	private DataOutputStream output;
	private BufferedWriter output;
	
	private final String DEFAULT_ENCODE = "utf-8";

	/**
	 * 像指定地址发送post请求提交数据.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间(毫秒).
	 * @param attribute
	 *            发送请求参数,key为属性名,value为属性值.
	 * @param encode
	 * 			  指定编码方式
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public String sendPost(String path, int timeout, Map<String, String> attribute) throws IOException, TimeoutException {
		return sendPost(path, timeout, attribute, null);
	}
	/**
	 * 像指定地址发送post请求提交数据.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间(毫秒).
	 * @param attribute
	 *            发送请求参数,key为属性名,value为属性值.
	 * @param encode
	 * 			  指定编码方式
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public String sendPost(String path, int timeout, Map<String, String> attribute, String encode) throws IOException, TimeoutException {
		try {
			URL url = new URL(path);
//			log.error(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); 					// 设置输出,post请求必须设置.
			conn.setDoInput(true); 						// 设置输入,post请求必须设置.
			conn.setUseCaches(false); 					// 设置是否启用缓存,post请求不能使用缓存.
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("POST");
			conn.connect(); 									// 打开网络链接.
			if(encode == null || "".equals(encode)){
				output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), DEFAULT_ENCODE));
			}else{
				output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), encode));
			}
			
			if(attribute != null && attribute.keySet().size() > 0){
				output.write(getParams(attribute, encode)); 				// 将请求参数写入网络链接.
			}
			output.flush();
			return readResponse(encode);
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		}
	}
	public static String post(String actionUrl, Map<String, String> headParams,  
            Map<String, String> params, Map<String, File> files) throws IOException {  
  
        String BOUNDARY = java.util.UUID.randomUUID().toString();  
        String PREFIX = "--", LINEND = "\r\n";  
        String MULTIPART_FROM_DATA = "multipart/form-data";  
        String CHARSET = "UTF-8";  
  
        URL uri = new URL(actionUrl);  
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();  
        conn.setReadTimeout(30 * 1000); // 缓存的最长时间  
        conn.setDoInput(true);// 允许输入  
        conn.setDoOutput(true);// 允许输出  
        conn.setUseCaches(false); // 不允许使用缓存  
        conn.setRequestMethod("POST");  
        conn.setRequestProperty("connection", "keep-alive");  
        conn.setRequestProperty("Charsert", "UTF-8");  
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA  
                + ";boundary=" + BOUNDARY);  
        if(headParams!=null){  
            for(String key : headParams.keySet()){  
                conn.setRequestProperty(key, headParams.get(key));  
            }  
        }  
        StringBuilder sb = new StringBuilder();  
  
        if (params!=null) {  
            // 首先组拼文本类型的参数  
            for (Map.Entry<String, String> entry : params.entrySet()) {  
                sb.append(PREFIX);  
                sb.append(BOUNDARY);  
                sb.append(LINEND);  
                sb.append("Content-Disposition: form-data; name=\""  
                        + entry.getKey() + "\"" + LINEND);  
                sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);  
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);  
                sb.append(LINEND);  
                sb.append(entry.getValue());  
                sb.append(LINEND);  
            }  
              
        }  
          
        DataOutputStream outStream = new DataOutputStream(  
                conn.getOutputStream());  
        if (sb.toString() != null && !"".equals(sb.toString())) {  
            outStream.write(sb.toString().getBytes());  
        }  
          
  
        // 发送文件数据  
        if (files != null)  
            for (Map.Entry<String, File> file : files.entrySet()) {  
                StringBuilder sb1 = new StringBuilder();  
                sb1.append(PREFIX);  
                sb1.append(BOUNDARY);  
                sb1.append(LINEND);  
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""  
                        + file.getKey() + "\"" + LINEND);  
                sb1.append("Content-Type: application/octet-stream; charset="  
                        + CHARSET + LINEND);  
                sb1.append(LINEND);  
                outStream.write(sb1.toString().getBytes());  
  
                InputStream is = new FileInputStream(file.getValue());  
                byte[] buffer = new byte[1024];  
                int len = 0;  
                while ((len = is.read(buffer)) != -1) {  
                    outStream.write(buffer, 0, len);  
                }  
  
                is.close();  
                outStream.write(LINEND.getBytes());  
            }  
  
        // 请求结束标志  
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();  
        outStream.write(end_data);  
        outStream.flush();  
  
        // 得到响应码  
        int res = conn.getResponseCode();  
        InputStream in = conn.getInputStream();  
        if (res == 200) {  
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));    
                   StringBuffer buffer = new StringBuffer();    
                 String line = "";    
             while ((line = bufferedReader.readLine()) != null){    
                   buffer.append(line);    
             }    
  
//          int ch;  
//          StringBuilder sb2 = new StringBuilder();  
//          while ((ch = in.read()) != -1) {  
//              sb2.append((char) ch);  
//          }  
            return buffer.toString();  
        }  
        outStream.close();  
        conn.disconnect();  
        return in.toString();  
  
    }  
	
	/**
	 * 像指定地址发送post请求提交数据.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间(毫秒).
	 * @param attribute
	 *            发送请求参数,key为属性名,value为属性值.
	 * @param encode
	 * 			  指定编码方式
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public InputStream sendPostGetForBaiduVoice(String path, int timeout, Map<String, String> attribute, String encode) throws IOException, TimeoutException {
		try {
			URL url = new URL(path);
//			log.error(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); 					// 设置输出,post请求必须设置.
			conn.setDoInput(true); 						// 设置输入,post请求必须设置.
			conn.setUseCaches(false); 					// 设置是否启用缓存,post请求不能使用缓存.
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("POST");
			conn.connect(); 									// 打开网络链接.
			if(encode == null || "".equals(encode)){
				output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), DEFAULT_ENCODE));
			}else{
				output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), encode));
			}
			
			if(attribute != null && attribute.keySet().size() > 0){
				output.write(getParams(attribute, encode)); 				// 将请求参数写入网络链接.
			}
			output.flush();
			return conn.getInputStream();
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		}
	}
	
	public String sendPostForCust(String path, int timeout, Map<String, String> attribute) throws IOException, TimeoutException {
		URL url = new URL(path);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("AppKey", "d9d6127a7851ffa02bb71d7f1f671eae");
		con.setRequestProperty("sign", "123456");
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setUseCaches(false);
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(Serialize.parseMapToJson(attribute).getBytes("UTF-8"));
		os.close();
		String encoding = con.getContentEncoding();
		InputStream is = con.getInputStream();
		int read = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = is.read()) != -1) {
			baos.write(read);
		}
		byte[] data = baos.toByteArray();
		baos.close();
		String content = null;
		if (encoding != null) {
			content = new String(data, encoding);
		} else {
			content = new String(data, "UTF-8");
		}
		return content;
	}
	/**
	 * 像指定地址发送post请求提交数据.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间(毫秒).
	 * @param attribute
	 *            发送请求参数,key为属性名,value为属性值.
	 * @param encode
	 * 			  指定编码方式
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public String sendPostWeixin(String path, int timeout,Map<String, String> param, Map<String, Object> attribute, String encode) throws IOException, TimeoutException {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
	                new java.security.SecureRandom());
	 
	        URL console = new URL(path + "?" + getParams(param, "utf-8"));
	        sconn = (HttpsURLConnection) console.openConnection();
	        SSLSocketFactory sf = sc.getSocketFactory();
//	        SSLSocket socket = (SSLSocket)sf.createSocket();
//	        socket.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
	        sconn.setSSLSocketFactory(sf);
	        sconn.setHostnameVerifier(new TrustAnyHostnameVerifier());
	        sconn.setDoOutput(true);
	        sconn.connect();
			if(encode == null || "".equals(encode)){
				output = new BufferedWriter(new OutputStreamWriter(sconn.getOutputStream(), DEFAULT_ENCODE));
			}else{
				output = new BufferedWriter(new OutputStreamWriter(sconn.getOutputStream(), encode));
			}
			
			if(attribute != null && attribute.keySet().size() > 0){
				output.write(Serialize.parseMapToJson(attribute)); 				// 将请求参数写入网络链接.
			}
			output.flush();
			return readsResponse(encode);
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	
	public String sendPostForEMOS(String path, int timeout, String authorization, Map<String, Object> attribute, String encode) throws IOException, TimeoutException {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
	                new java.security.SecureRandom());
	 
	        URL console = new URL(path);
	        sconn = (HttpsURLConnection) console.openConnection();
	        SSLSocketFactory sf = sc.getSocketFactory();
//	        SSLSocket socket = (SSLSocket)sf.createSocket();
//	        socket.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
	        sconn.setSSLSocketFactory(sf);
	        sconn.setHostnameVerifier(new TrustAnyHostnameVerifier());
	        sconn.setDoOutput(true);
	        sconn.setDoInput(true);
	        sconn.setRequestMethod("POST");
	        sconn.setRequestProperty("Authorization", authorization);
			sconn.setRequestProperty("Content-Type", "application/json");
	        sconn.connect();
			if(encode == null || "".equals(encode)){
				output = new BufferedWriter(new OutputStreamWriter(sconn.getOutputStream(), DEFAULT_ENCODE));
			}else{
				output = new BufferedWriter(new OutputStreamWriter(sconn.getOutputStream(), encode));
			}
			
			if(attribute != null && attribute.keySet().size() > 0){
				System.out.println(Serialize.parseMapToJson(attribute)+"###############################################");
				output.write(Serialize.parseMapToJson(attribute)); 				// 将请求参数写入网络链接.
			}
			output.flush();
			return readsResponse(encode);
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	/**
	 * 像指定地址发送get请求.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间,单位为毫秒.
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public String sendGet(String path, int timeout) throws IOException, TimeoutException {
		return sendGet(path, timeout, null);
	}

	/**
	 * 像指定地址发送get请求.
	 * 
	 * @param path
	 *            数据提交路径.
	 * @param timeout
	 *            超时时间,单位为毫秒.
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             网络连接错误时抛出IOException.
	 * @throws TimeoutException
	 *             网络连接超时时抛出TimeoutException.
	 * 
	 * @version 1.1
	 * @updateInfo 捕获非致命异常SocketTimeoutException同时抛出致命异常TimeoutException.
	 */
	public String sendGet(String path, int timeout, String encode) throws IOException, TimeoutException {
		try {
			URL url = new URL(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false); // 设置是否启用缓存,post请求不能使用缓存.
			conn.setConnectTimeout(timeout);
			conn.setRequestMethod("GET");
			conn.connect(); // 打开网络链接.
			return readResponse(encode);
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		}
	}

	/**
	 * 读取服务器响应信息.
	 * 
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             读取信息发生错误时抛出IOException.
	 * 
	 * @version 1.0
	 * @updateInfo
	 */
	private String readResponseOld(String encode) throws IOException {
		String code = Integer.toString(conn.getResponseCode());
		if (code.startsWith("2")) { // 若响应码以2开头则读取响应头总的返回信息
			if(encode == null || "".equals(encode)){
				input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}else{
				input = new BufferedReader(new InputStreamReader(conn.getInputStream(), encode));
			}
			
			char[] charBuffer = new char[1024];
			StringBuffer sb = new StringBuffer();
			int length = -1;
			while ((length = input.read(charBuffer)) != -1) {
				sb.append(charBuffer.length == length ? charBuffer : Arrays.copyOf(charBuffer, length));
			}
			result = sb.toString().trim();
		} else { // 若响应码不以2开头则返回错误信息.
			return "error";
		}
		closeConnection();
		return result;
	}
	
	/**
	 * @Title: NetUtil.java 
	 * @Package com.flf.util 
	 * @Description: 读取响应数据接口
	 * @author XQX
	 * @date 2017-5-2 下午4:03:57
	 */
	private String readResponse(String encode) throws IOException {
		InputStream is = conn.getInputStream();
		int read = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = is.read()) != -1) {
			baos.write(read);
		}
		byte[] data = baos.toByteArray();
		baos.close();
		String content = null;
		if (encode != null) {
			content = new String(data, encode);
		} else {
			content = new String(data, "UTF-8");
		}
		return content;
	}
	/**
	 * 读取服务器响应信息.
	 * 
	 * @return 服务器的响应信息,当发生错误时返回响应码.
	 * @throws IOException
	 *             读取信息发生错误时抛出IOException.
	 * 
	 * @version 1.0
	 * @updateInfo
	 */
	private String readsResponse(String encode) throws IOException {
		String code = Integer.toString(sconn.getResponseCode());
		if (code.startsWith("2")) { // 若响应码以2开头则读取响应头总的返回信息
			if(encode == null || "".equals(encode)){
				input = new BufferedReader(new InputStreamReader(sconn.getInputStream()));
			}else{
				input = new BufferedReader(new InputStreamReader(sconn.getInputStream(), encode));
			}
			
			char[] charBuffer = new char[1024];
			StringBuffer sb = new StringBuffer();
			int length = -1;
			while ((length = input.read(charBuffer)) != -1) {
				sb.append(charBuffer.length == length ? charBuffer : Arrays.copyOf(charBuffer, length));
			}
			result = sb.toString().trim();
		} else { // 若响应码不以2开头则返回错误信息.
			return "error";
		}
		closeConnection();
		return result;
	}
	
	/**
	 * 将发送请求的参数构造为指定格式.
	 * 
	 * @param attribute
	 *            发送请求的参数,key为属性名,value为属性值.
	 * @param encode
	 * 			  指定编码
	 * @return 指定格式的请求参数.
	 * 
	 * @version 1.0
	 * @throws UnsupportedEncodingException 
	 * @updateInfo
	 */
	@SuppressWarnings("deprecation")
	private static String getParams(Map<String, String> attribute, String encode) throws UnsupportedEncodingException {
		Set<String> keys = attribute.keySet(); 				// 获取所有参数名
		Iterator<String> iterator = keys.iterator(); 		// 将所有参数名进行跌代
		StringBuffer params = new StringBuffer();
		// 取出所有参数进行构造
		while (iterator.hasNext()) {
			String key = iterator.next();
			String param = "";
			if(encode == null || "".equals(encode)){
				param = key + "=" + URLEncoder.encode(String.valueOf(attribute.get(key))) + "&";
			}else{
				param = key + "=" + URLEncoder.encode(String.valueOf(attribute.get(key)), encode) + "&";
			}
			
			params.append(param);
		}
		// 返回构造结果
		return params.toString().substring(0, params.toString().length() - 1);
	}

	/**
	 * 关闭链接与所有从链接中获得的流.
	 * 
	 * @throws IOException
	 *             关闭发生错误时抛出IOException.
	 * 
	 * @version 1.0
	 * @updateInfo
	 */
	private void closeConnection() throws IOException {
		if (input != null) {
			input.close();
		}
		if (output != null) {
			output.close();
		}
		if (conn != null) {
			conn.disconnect();
		}
	}

	/**
	 * 下载文件,下载文件存储至指定路径.
	 * 
	 * @param path
	 *            下载路径.
	 * @param savePath
	 *            存储路径.
	 * @return 下载成功返回true,若下载失败则返回false.
	 * @throws MalformedURLException
	 *             建立连接发生错误抛出MalformedURLException.
	 * @throws IOException
	 *             下载过程产生错误抛出IOException.
	 * 
	 * @version 1.2
	 * @updateInfo 取消图片的下载后缀,取消文件下载（除.jpg文件外）的tmp流程.
	 */
	public boolean downloadFile(String path, String savePath) throws MalformedURLException, IOException {
		File file = null;
		InputStream input = null;
		OutputStream output = null;
		boolean isComplete = false;
		try {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			int code = conn.getResponseCode();
			if (code == 200) {
				input = conn.getInputStream();
				if (savePath.endsWith(".jpg")) {
					savePath = savePath.replace(".jpg", ".tmp");
				}
				file = new File(savePath);
				File parent = file.getParentFile();
				if (!parent.exists())
					parent.mkdirs();
				file.createNewFile(); // 创建文件
				output = new FileOutputStream(file);
				byte buffer[] = new byte[1024];
				int read = 0;
				while ((read = input.read(buffer)) != -1) { // 读取信息循环写入文件
					output.write(buffer, 0, read);
				}
				output.flush();
				isComplete = true;
			} else {
				isComplete = false;
			}
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != output) {
				output.close();
			}
			if (isComplete) {
				if (savePath.endsWith(".tmp")) {
					file.renameTo(new File(savePath.replace(".tmp", "")));
				}
			}
		}
		return isComplete;
	}
	public static InputStream getInputStream(String accessToken, String mediaId) {  
        InputStream is = null;  
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="  
                + accessToken + "&media_id=" + mediaId;  
        try {  
            URL urlGet = new URL(url);  
            HttpURLConnection http = (HttpURLConnection) urlGet  
                    .openConnection();  
            http.setRequestMethod("GET"); // 必须是get方式请求  
            http.setRequestProperty("Content-Type",  
                    "application/x-www-form-urlencoded");  
            http.setDoOutput(true);  
            http.setDoInput(true);  
            System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒  
            System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒  
            http.connect();  
            // 获取文件转化为byte流  
            is = http.getInputStream();  
            File file = new File("c:\\aaa");
			File parent = file.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
			file.createNewFile(); // 创建文件
			FileOutputStream output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int read = 0;
			while ((read = is.read(buffer)) != -1) { // 读取信息循环写入文件
				output.write(buffer, 0, read);
			}
			output.flush();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return is;  
  
    }
	private static class TrustAnyTrustManager implements X509TrustManager {
		 
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
 
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
 
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }
 
    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
