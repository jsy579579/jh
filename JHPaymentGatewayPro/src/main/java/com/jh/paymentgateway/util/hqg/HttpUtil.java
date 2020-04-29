package com.jh.paymentgateway.util.hqg;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;




public class HttpUtil {

	public static <T> String sendPost(T t,String url) {
		
		DataOutputStream out = null;
		URL u = null;
        URLConnection con = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
      //尝试发送请求
        try{
        	//SSLContext sc = SSLContext.getInstance("SSL");
            u = new URL(url);
            //打开和URL之间的连接
            con = u.openConnection();
            //设置通用的请求属性
            //con.setSSLSocketFactory(sc.getSocketFactory());
            //con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json"); //
            con.setUseCaches(false);
            //发送POST请求必须设置如下两行
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();
	        out = new DataOutputStream(con.getOutputStream());
	        String reqString = JsonUtils.objectToJson(t);

	        System.out.println("上送参数===>"+reqString);
	        out.write(reqString.getBytes("utf-8"));
	        // 刷新、关闭
	        out.flush();
	        out.close();
	        in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            String line;
            while((line = in.readLine()) != null) {
            	result.append(line).append(System.lineSeparator());
            }
            System.out.println("返回参数===>"+result);
        }catch (Exception e) {
        	e.printStackTrace();
		}
        return result.toString();
	}
	
	/*public static  BaseRps sendPost(CommonFormatReq req,String url) throws Exception{
		BaseRps baseRps=null;
		CloseableHttpResponse response=null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost=new HttpPost(url);
		
		RequestConfig requestConfig = RequestConfig.custom()  
			    .setConnectionRequestTimeout(59000)
			    .setConnectTimeout(59000)  
			    .setSocketTimeout(59000).build();
		httpPost.setConfig(requestConfig);
		//设置json
		String json = JsonUtils.objectToJson(req);
		System.out.println("发送数据===>"+json);
		StringEntity entity = new StringEntity(json);
		entity.setContentEncoding("utf-8");
		entity.setContentType("application/json");
	    httpPost.setEntity(entity);
	    //返回码
		response = httpclient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			String jsonData = EntityUtils.toString(response.getEntity());
			System.out.println("返回数据===>"+jsonData);
			CommonFormatRps commonFormatRps = JsonUtils.jsonToPojo(jsonData,CommonFormatRps.class);
			String result = commonFormatRps.getResult();
			System.out.println(result);
			baseRps= JsonUtils.jsonToPojo(result, BaseRps.class);
		}else {
			baseRps=new BaseRps();
			baseRps.setCode("-1");
			baseRps.setMessage("渠道请求异常，http返回码===>"+statusCode);
		}
		
		httpclient.close();
		response.close();
			
		return baseRps;
	}*/
}
