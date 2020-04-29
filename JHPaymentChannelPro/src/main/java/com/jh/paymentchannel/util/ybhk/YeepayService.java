package com.jh.paymentchannel.util.ybhk;


import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;







public class YeepayService {
	public static final String  register_URL="registerURI";
	public static final String  customerInforQuery_URL="customerInforQueryURI";
	public static final String  feeSetApi_URL="feeSetApiURI";
	public static final String  queryFeeSetApi_URL="queryFeeSetApiURI";
	public static final String customerInforUpdate_URL="customerInforUpdateURI";
	public static final String  receiveApi_URL="receiveApiURI";
	public static final String secondPayApi_URL="SecondPayURI";
	public static final String  tradeReviceQuery_URL="tradeReviceQueryURI";
	public static final String  withDrawApi_URL="withDrawApiURI";
	public static final String  transferQuery_URL="transferQueryURI";
	public static final String  customerBalanceQuery_URL="customerBalanceQueryURI";
	public static final String  transferToCustomer_URL="transferToCustomerURI";
	public static final String bindBankCard_URL="bindBankCardURI";
	public static final String bindBankCardSms_URL="bindBankCardSmsURI";
	public static final String bindBankCardConfirm_URL="bindBankCardConfirmURI";

	//参与验签的参数
	public static String register[]={"mainCustomerNumber", "requestId","customerType","businessLicence","bindMobile","signedName","linkMan","idCard","legalPerson", "minSettleAmount","riskReserveDay","bankAccountNumber","bankName","accountName","manualSettle"};
	public static String feeSetApi[]={"customerNumber","mainCustomerNumber","productType","rate","hmac"};
	
	/**
	
 * 获取商编
	 */
	
	 public static  String mainCustomerNumber(){
		 return Config.getInstance().getValue("mainCustomerNumber");
	 }
	 /**
	  * 获取秘钥
	  */
	 public  static String HmacKey(){
		 return Config.getInstance().getValue("HmacKey");
	 }
	/**
	 * 获取秘钥的前十六位
	 */
	public static String get16Hmac(){
		String private16=Config.getInstance().getValue("HmacKey");
		String str=private16.substring(0,16);
		return  str;


	}
	/**
	  * 获取地址
	  */
	 public static String getURI(String typeurl){
		 return Config.getInstance().getValue(typeurl);	 
	 }
	 /**
	  * 拿数组生成hmac参数
	  */
	 public static String madeHmac(String before[],String HmacKey){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<before.length;i++){
			if(before.length!=0||!before.equals("")){
				sb.append(before[i]);
			}
			}
		System.out.println("参加hmac生成的原串："+sb);	
		 return Digest.hmacSign(sb.toString(), HmacKey);
		 
	 }
	/**
	 * 拿map生成hmac参数
	 */

	public static String makeHmac(Map<String,String> map,String  HmacKey){
		//遍历map拼串
		StringBuffer  sb=new StringBuffer();
		for(Map.Entry<String,String> entry:map.entrySet()){
			if(!entry.getValue().equals("")&&entry.getValue()!=null){
				sb.append(entry.getValue());

			}
		}
		System.out.print("参加签名的原串："+sb.toString());
		return Digest.hmacSign(sb.toString(), HmacKey);

	}
/**
 * 验hmac
 */
public  static String verifyHmac(Map<String,Object> map,String HmacKey){
	StringBuffer  sb=new StringBuffer();
	for(Map.Entry<String,Object> entry:map.entrySet()){
		if(!entry.getValue().equals("")&&entry.getValue()!=null){
			sb.append(entry.getValue());

		}
	}
	System.out.print("参加签名的原串："+sb.toString());

	return  Digest.hmacSign(sb.toString(), HmacKey);
}
	/**
	 *
	 * @param param
	 * @param reqskb
	 * @param url
	 * @return
	 */
	public static TreeMap sendToSkb( String[] param,String[] reqskb,String url ){

		  String result=null;
		  TreeMap<String, Object> responseMap= null;		
		  List<Part> list=new ArrayList<Part>();
		for(int i=0;i<param.length&&i<reqskb.length;i++){
		  list.add(new StringPart(reqskb[i],param[i]));	  
      }
		 System.out.println("list:"+list);
		 
	Part[] part= (Part[]) list.toArray(new Part[list.size()]);
		PostMethod postMethod = new PostMethod(url);
        HttpClient client = new HttpClient();  
		 postMethod.setRequestEntity(new MultipartRequestEntity(part, postMethod.getParams()));
				try {
					int status = client.executeMethod(postMethod);
					  if (status == HttpStatus.SC_OK) {
			               result = postMethod.getResponseBodyAsString();
		       responseMap= JSON.parseObject(result, new TypeReference<TreeMap<String, Object>>() {});
			       		
			                 System.out.println("result" + result);             
			            } else if (status == HttpStatus.SC_MOVED_PERMANENTLY
			                    || status == HttpStatus.SC_MOVED_TEMPORARILY) {
			              Header locationHeader = (Header) postMethod.getResponseHeader("location");
			                String location = null;
			                if (locationHeader != null) {
			                    location = locationHeader.getValue();
			                    System.out.println("The page was redirected to:" + location);
			                } else {
			                    System.err.println("Location field value is null.");
			                }
			            } else {
			                System.out.println("fail======" + status);
			            }
					
				} catch (IOException e) {
					throw new RuntimeException("execute   is fail");
				}finally{
					// 释放连接
					postMethod.releaseConnection();

				}
				return responseMap;					

	}

	/**
	 * namevaluePair[]的发送
	 */
	public  static  TreeMap  send(NameValuePair[] pairs, String tradeReviceQueryURL){
		String result=null;
		TreeMap<String, Object> responseMap= null;

		PostMethod postMethod = new PostMethod(tradeReviceQueryURL);
		try{

		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		postMethod.setRequestBody(pairs);

		int status = client.executeMethod(postMethod);
		if (status == HttpStatus.SC_OK) {
			 result = postMethod.getResponseBodyAsString();

			System.out.println("result" + result);
			responseMap= JSON.parseObject(result, new TypeReference<TreeMap<String, Object>>() {});
//对json串进行处理


		} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
				|| status == HttpStatus.SC_MOVED_TEMPORARILY) {
			// 从头中取出转向的地址
			org.apache.commons.httpclient.Header locationHeader = postMethod
					.getResponseHeader("location");
			String location = null;
			if (locationHeader != null) {
				location = locationHeader.getValue();
				System.out.println("The page was redirected to:" + location);
			} else {
				System.err.println("Location field value is null.");
			}
		} else {
			System.out.println("fail======" + status);
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		// 释放连接
		postMethod.releaseConnection();
	}
	return responseMap;
}




//	public static TreeMap sendToSkb2(Map<String,String> basicParamMap, Map<String,File> fileParamMap,String url ) throws FileNotFoundException {
//		List<Part> partList = new ArrayList<>();
//
//		if (basicParamMap != null && basicParamMap.size() > 0) {
//			for(Map.Entry<String,String> entry : basicParamMap.entrySet()){
//			//	partList.add(new StringPart(entry.getKey(), URLEncoder.encode(entry.getValue(),"utf-8")));
//				partList.add(new StringPart(entry.getKey(), entry.getValue(),"utf-8"));
//			}
//		}
//
//		if (fileParamMap != null && fileParamMap.size() > 0) {
//			for (Map.Entry<String, File> entry : fileParamMap.entrySet()) {
//				partList.add(new FilePart(entry.getKey(), entry.getValue()));
//				//partList.add(new FilePart(entry.getKey(), entry.getValue(),"image/png","utf-8"));
//			}
//		}
//System.err.println("partList********"+partList);
//		String result=null;
//		TreeMap<String, Object> responseMap= null;
//
//		Part[] part= (Part[]) partList.toArray(new Part[partList.size()]);
//		System.out.println("********part"+part.length);
//		for(int i=0;i<part.length;i++){
//			System.out.println("~~~~~~~~~"+part[i]);
//		}
//		PostMethod postMethod = new PostMethod(url);
//	//	postMethod.addRequestHeader("Content-Type", "multipart/form-data; charset=UTF-8");
//		//postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//		postMethod.setRequestHeader("Content-Type", "multipart/form-data; charset=UTF-8");
//
//		HttpClient client = new HttpClient();
//		System.out.println("$$$$$$+postMethod"+postMethod.getParameters());
//		postMethod.setRequestEntity(new MultipartRequestEntity(part, postMethod.getParams()));
//			try {
//			int status = client.executeMethod(postMethod);
//			if (status == HttpStatus.SC_OK) {
//				result = postMethod.getResponseBodyAsString();
//				responseMap= JSON.parseObject(result, new TypeReference<TreeMap<String, Object>>() {});
//
//				System.out.println("result" + result);
//			} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
//					|| status == HttpStatus.SC_MOVED_TEMPORARILY) {
//				Header locationHeader = (Header) postMethod.getResponseHeader("location");
//				String location = null;
//				if (locationHeader != null) {
//					location = locationHeader.getValue();
//					System.out.println("The page was redirected to:" + location);
//				} else {
//					System.err.println("Location field value is null.");
//				}
//			} else {
//				System.out.println("fail======" + status);
//			}
//
//		} catch (IOException e) {
//			throw new RuntimeException("execute   is fail");
//		}finally{
//			// 释放连接
//			postMethod.releaseConnection();
//
//		}
//		return responseMap;
//	}


}
