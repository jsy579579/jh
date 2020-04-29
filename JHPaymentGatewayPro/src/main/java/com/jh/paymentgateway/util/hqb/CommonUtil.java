package com.jh.paymentgateway.util.hqb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import sun.misc.BASE64Encoder;

/**
 * 通用工具类
 * @author CJF
 * @date 2016-6-15上午11:28:22
 *
 */
public class CommonUtil {
	
	/**
	 * http
	 * @param urlStr
	 * @param reponse
	 * @return
	 * @throws Exception
	 */
	public static String post(String urlStr, byte[] reponse) throws Exception {
		DataOutputStream objOutputStrm = null;
		OutputStream outStrm = null;
		InputStream inStrm = null;
		try {
			URL url = new URL(urlStr);
			// 此处的urlConnection对象实际上是根据URL的
			// 请求协议(此处是http)生成的URLConnection类
			// 的子类HttpURLConnection,故此处最好将其转化
			// 为HttpURLConnection类型的对象,以便用到  
			// HttpURLConnection更多的API.如下:
			URLConnection rulConnection = url.openConnection();

			HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;

			 //httpUrlConnection.setDoOutput(true);以后就可以使用conn.getOutputStream().write()    
			 //httpUrlConnection.setDoInput(true);以后就可以使用conn.getInputStream().read();  
			 //简单一句话：get请求的话默认就行了，post请求需要setDoOutput(true)，这个默认是false的。
			//get请求用不到conn.getOutputStream()，因为参数直接追加在地址后面，因此默认是false。  
			//post请求（比如：文件上传）需要往服务区传输大量的数据，这些数据是放在http的body里面的，因此需要在建立连接以后，往服务端写数据。  
			//因为总是使用conn.getInputStream()获取服务端的响应，因此默认值是true。
			
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
			// http正文内，因此需要设为true, 默认情况下是false;
			httpUrlConnection.setDoOutput(true);

			// 设置是否从httpUrlConnection读入，默认情况下是true;
			httpUrlConnection.setDoInput(true);

			// Post 请求不能使用缓存
			httpUrlConnection.setUseCaches(false);
   
			// 设定传送的内容类型是可序列化的java对象  
			// (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
//			httpUrlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
//			httpUrlConnection.setRequestProperty("Accept", "application/json");
			httpUrlConnection.setRequestProperty("Content-type", "application/octet-stream");

			// 设定请求的方法为"POST"，默认是GET
			httpUrlConnection.setRequestMethod("POST");

			// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，
			// 所以在开发中不调用上述的connect()也可以)。
			outStrm = httpUrlConnection.getOutputStream();

			// 现在通过输出流对象构建对象输出流对象，以实现输出可序列化的对象。
			objOutputStrm = new DataOutputStream(outStrm);

			// 向对象输出流写出数据，这些数据将存到内存缓冲区中
			objOutputStrm.write(reponse);
 
			// 刷新对象输出流，将任何字节都写入潜在的流中（些处为ObjectOutputStream）
			objOutputStrm.flush();

			// 调用HttpURLConnection连接对象的getInputStream()函数,
			// 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			inStrm = httpUrlConnection.getInputStream(); // <===注意，实际发送请求的代码段就在这里

			
			BufferedReader read = new BufferedReader(new InputStreamReader(inStrm, "UTF-8"));

			StringBuffer jsonString = new StringBuffer();
			String line = "";
			while ((line = read.readLine()) != null) {
				jsonString.append(line);
			}
			String json = jsonString.toString();
			
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(objOutputStrm != null)
				objOutputStrm.close();
			
			if(outStrm != null){
				outStrm.close();
			}
			
			if(inStrm!= null)
				inStrm.close();
			
		}

	};
	
	/**
	 * file转base64
	 * @param path 文件路径  D:\image\1.jpg
	 * @return
	 * @throws Exception
	 */
	public static String encodeBase64File(String path) throws Exception {
		FileInputStream inputFile = null;
		try {
			File file = new File(path);
			inputFile = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			inputFile.read(buffer);

			return java.net.URLEncoder.encode(new BASE64Encoder().encode(buffer), "utf-8");
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(inputFile != null){
				inputFile.close();
			}
		}
		return null;

	}
}
