package com.jh.paymentchannel.util.wxwap;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;



/**
 * @ClassName: testMZFB
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 
 * @date 2018-7-10 上午10:49:30
 */
public class testMZFB {

	private static final Logger logger = LoggerFactory.getLogger(testMZFB.class);	
	private static final String PrePayURL     =   "IP端口/Pay/YZfb/Pay";		 //转账
	private static final String PrePayQueyURL =   "IP端口/Pay/YZfb/orderQuery";//转账查询

	public static String transferAccount(){

		String res ="";
		try {

			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
			Date date = new Date();
			String updateDate=sdf.format(date);
			String  inTradeOrderNo=updateDate;

			SortedMap<String,String> dataMapIn=new TreeMap<String, String>();
			dataMapIn.put("pay_memberid", "10007");//间连号
			dataMapIn.put("pay_orderid", inTradeOrderNo);
			dataMapIn.put("pay_amount", "0.01");
			dataMapIn.put("pay_applydate", inTradeOrderNo);
			dataMapIn.put("pay_bankcode", "ALIPAY");
			dataMapIn.put("pay_notifyurl", "localhost:9999/swPayInterface/MZFBAliNotify");
			dataMapIn.put("pay_callbackurl", "");
			String localMd5=mapToString(dataMapIn)+"&key="+"ggeWq1BO6HpXmmfUYzA4y1yPkWHadj";
			
			logger.info("******************localMd5:" + localMd5 );
			String sign =  Md5Util.getMD5(localMd5).toUpperCase();
			dataMapIn.put("pay_md5sign", sign);

			String sendMsg= mapToString(dataMapIn).replace(">", "");

			System.out.println("sengMsg:"+sendMsg);

			res = doPostQueryCmd(PrePayURL,sendMsg);

			JSONObject json = JSONObject.fromObject(res);

			logger.info("***********json:"+json.toString());
			res = json.toString();
			System.out.println("url:");
			System.out.println(json.get("payurl").toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return res;

	}



	public static String transferAccountQuery(){

		String res ="";
		JSONObject json =new JSONObject();
		try {
			SortedMap<String,String> dataMapIn=new TreeMap<String, String>();
			dataMapIn.put("memberid", "10007");//间连号
			dataMapIn.put("orderid", "20180711021337");
			String localMd5=mapToString(dataMapIn)+"&key="+"ggeWq1BO6HpXmmfUYzA4y1yPkWHadj";
			logger.info("******************localMd5" + localMd5 );
			String sign =   Md5Util.getMD5(localMd5).toUpperCase();
			dataMapIn.put("sign", sign);
			logger.info("******************dataMapIn："+dataMapIn.toString());				
			String sendMsg= mapToString(dataMapIn).replace(">", "");				
			logger.info("sengMsg:"+sendMsg);				
			res = doPostQueryCmd(PrePayQueyURL,sendMsg);				
			logger.info("***************" + res + "****************");
			json = JSONObject.fromObject(res);
			logger.info("***********json:"+json.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json.toString();

	}


	public static String mapToString (Map<String, String> params){

		StringBuffer sb =new StringBuffer();
		String result ="";

		if (params == null || params.size() <= 0) {
			return "";
		}
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (value == null || value.equals("")) {
				continue;
			}
			sb.append(key+"=>"+value+"&");
		}

		result=sb.toString().substring(0,sb.length()-1);

		return result;
	}


	public static String doPostQueryCmd(String strURL, String req) {
		String result = null;
		BufferedReader in = null;
		BufferedOutputStream out = null;
		try {
			URL url = new URL(strURL);
			URLConnection con = url.openConnection();
			HttpURLConnection httpUrlConnection  =  (HttpURLConnection) con;
			httpUrlConnection.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);
			out = new BufferedOutputStream(con.getOutputStream());
			byte outBuf[] = req.getBytes("utf-8");
			out.write(outBuf);
			out.close();

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			StringBuffer sb = new StringBuffer();
			String data = null;

			while ((data = in.readLine()) != null) {
				sb.append(data);
			}

			logger.info("res:"+sb.toString());
			result = sb.toString();		
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		if (result == null)
			return "";
		else
			return result;
	}


}
