package com.jh.paymentchannel.util.ump.util;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.jh.paymentchannel.util.ump.common.Const;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.exception.ParameterCheckException;
import com.jh.paymentchannel.util.ump.exception.ReqDataException;
import com.jh.paymentchannel.util.ump.log.ILogger;
import com.jh.paymentchannel.util.ump.log.LogManager;


/**
 * ***********************************************************************
 * <br>description : 商户请求联动平台组织数据帮助类
 * @author      umpay
 * @date        2014-7-25 上午10:53:38
 * @version     1.0  
 ************************************************************************
 */
public class Mer2PlatUtils {
	private static ILogger log_ = LogManager.getLogger();
	
	/**
	 * 
	 * <br>description :V4.0版获取请求对象,组织时校验请求数据格式
	 * @param appname 请求联动接入系统应用名称
	 * @param obj 请求数据对象
	 * @param method 请求方式 get or post
	 * @return
	 * @throws ReqDataException
	 * @version     1.0
	 * @date        2014-7-25上午10:57:54
	 */
	public static ReqData getReqData(String appname,Object obj ,String method) throws ReqDataException{
		if(obj==null || "".equals(method) ){
			log_.info("后台直连请求参数错误,obj或method为空");
			throw new RuntimeException("请求参数异常,obj或method为空!");
		}
		
		//得到数据对象
		Map<String, String> map = DataUtil.getData(obj);
		Map<String, String> mapfield = new HashMap();;
		if(map.get(Const.SERVICE)==null || !ServiceMapUtil.getServiceRule().containsKey(map.get(Const.SERVICE)))
			throw new ParameterCheckException("请求的服务类型错误，service为空或字段值错误！");
		
		doEncrypt(map);
		
		//组织请求联动的url
		ReqData data = new ReqData();
		String url = getUrl(appname,Const.PAYSERVICE);
		log_.debug("url=" + url);
		
		//校验请求数据格式、生成签名
		Map returnMap = PlainUtil.getPlain(obj);
		
		//组织http请求数据
		String plain = returnMap.get(Const.PLAIN).toString();
		String sign = returnMap.get(Const.SIGN).toString();
		if(Const.METHOD_GET.equalsIgnoreCase(method)){
			try{
				sign = URLEncoder.encode(sign,"UTF-8");
			}catch(Exception e){
				throw new ReqDataException("字符编码出现异常,请联系联动优势开发人员");
			}
			String param = plain + "&" + "sign=" + sign;
			log_.debug("param=" + param);
			data.setUrl(url + "?" + param);
			log_.info("请求平台应访问的完整url为：" + data.getUrl());
			data.setPlain(plain);
			data.setSign(sign);
			log_.info("返回商户数据为："+data.toString());
			return data;
		}else if(Const.METHOD_POST.equalsIgnoreCase(method)){
			data.setUrl(url);
			map.put(Const.SIGN, sign);
			for(String key : map.keySet()){
				if(StringUtil.isNotEmpty(StringUtil.trim(map.get(key)))){
					mapfield.put(key, map.get(key));
				}
			}
			data.setField(mapfield);
			data.setSign(sign);
			data.setPlain(plain);
			log_.info("返回给商户的ReqData为"+data.toString());
			return data;
		}else{
			throw new RuntimeException("未能获得数据请求的方式:" + method);
		}
	}
	
	/**
	 * V4.0版获取请求对象，组织时不校验请求数据格式
	 * @param obj 请求数据对象
	 * @param method 请求方式 get or post
	 * @return
	 * @throws ReqDataException
	 */
	public static ReqData makeReqData(String appname,Object obj ,String method) throws ReqDataException{
		//得到数据对象
		Map<String, String> map = DataUtil.getData(obj);
		Map<String, String> mapfield = new HashMap();
		Map<String, String> mp = new HashMap();
		mp.putAll(map);
		//需RSA加密字段
		doEncrypt(mp);
		
		//组织请求联动的url
		String url = getUrl(appname,Const.PAYSERVICE);
		log_.debug("url=" + url);
		if(obj==null ||StringUtil.isEmpty(method) ){
			log_.info("后台直连请求参数错误,obj或method为空");
			throw new RuntimeException("请求参数异常,obj或method为空!");
		}
		
		
		//获取签名
		Map returnMap = PlainUtil.getPlainNocheck(mp);
		String plain = returnMap.get(Const.PLAIN).toString();
		String sign = returnMap.get(Const.SIGN).toString();
		
		//组织http请求数据
		ReqData data = new ReqData();
		if(Const.METHOD_GET.equalsIgnoreCase(method)){
			try{
				sign = URLEncoder.encode(sign,"UTF-8");
			}catch(Exception e){
				throw new ReqDataException("字符编码出现异常,请联系联动优势开发人员");
			}
			String param = plain + "&" + "sign=" + sign;
			log_.debug("param=" + param);
			data.setUrl(url + "?" + param);
			log_.info("请求平台应访问的完整url为：" + data.getUrl());
			data.setPlain(plain);
			data.setSign(sign);
			return data;
		}else if(Const.METHOD_POST.equalsIgnoreCase(method)){
			data.setUrl(url);
			mp.put(Const.SIGN, sign);
			for(String key : mp.keySet()){
				if(StringUtil.isNotEmpty(StringUtil.trim(mp.get(key)))){
					mapfield.put(key, mp.get(key));
				}
			}
			data.setField(mapfield);
			data.setSign(sign);
			data.setPlain(plain);
			log_.info("返回给商户的ReqData为"+data.toString());
			
			return data;
		}else{
			throw new RuntimeException("未能获得数据请求的方式:" + method);
		}
	}
	private static void doEncrypt(Map map) throws ReqDataException {
		Iterator it = map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next().toString();
			Object ob = map.get(key);
			String value = null;
			if(ob!= null){
				value = ob.toString();
			}
			
			//从配置文件获取需要加密的要素
			String encryptParamters = StringUtil.trim("card_id,valid_date,cvv2,pass_wd,identity_code,card_holder,recv_account,recv_user_name,identity_holder,identityCode,cardHolder,mer_cust_name,account_name,bank_account,endDate");
			String[] params = encryptParamters.split(",");
			HashSet encryptId = new HashSet();
			for(String param:params){
				if(StringUtil.isNotEmpty(param)){
					encryptId.add(param);
				}
			}
			
			//如果没有获取到，去默认的
			if(encryptId.size()==0){
				encryptId = ServiceMapUtil.getEncryptId();
			}
			
			try{
				if(encryptId.contains(key)&& StringUtil.isNotEmpty(value)){
					value = CipherUtil.Encrypt(value);
					map.put(key,value);
				}
			}catch(Exception e){
				log_.info("请求数据"+ key+ "=" + value +"数据加密发生异常" + e.getMessage());
				throw new ReqDataException("公钥证书进行加密发生异常");
			}
		}
	}
	
	private static String getUrl(String appname,String funcode){
		//获取配置的平台URL
		String platurl =  ProFileUtil.getUrlPix();
		if(platurl==null||"".equals(platurl.trim())){
			platurl = "http://pay.soopay.net";
		}
		return platurl +"/" + appname+ Const.UMPAYSTIE_SERVICE;
	}
	
}
