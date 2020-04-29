package com.jh.paymentchannel.util.ump.paygate.v40;

import java.util.HashMap;
import java.util.Map;

import com.jh.paymentchannel.util.ump.common.Const;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.exception.ParameterCheckException;
import com.jh.paymentchannel.util.ump.exception.ReqDataException;
import com.jh.paymentchannel.util.ump.log.ILogger;
import com.jh.paymentchannel.util.ump.log.LogManager;
import com.jh.paymentchannel.util.ump.util.Mer2PlatUtils;
import com.jh.paymentchannel.util.ump.util.PlainUtil;
import com.jh.paymentchannel.util.ump.util.ProFileUtil;

/**
 * ***********************************************************************
 * <br>description : 商户请求联动平台处理类
 * @author      umpay
 * @date        2014-8-1 上午09:24:01
 * @version     1.0  
 ************************************************************************
 */
public class Mer2Plat_v40 {
	private static ILogger log_ = LogManager.getLogger();
	
	/**
	 * 
	 * <br>description : 组织用HTTP GET 形式请求平台的数据，内部已对请求的数据进行正则校验
	 * @param obj
	 * @return
	 * @throws ReqDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:18:04
	 */
	public static ReqData ReqDataByGet(Object obj) throws ReqDataException{
		return reqDataByService(obj,Const.METHOD_GET);
	}
	
	/**
	 * 
	 * <br>description : 组织用HTTP POST 形式请求平台的数据，内部已对请求的数据进行正则校验
	 * @param obj
	 * @return
	 * @throws ReqDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:18:04
	 */
	public static ReqData ReqDataByPost(Object obj) throws ReqDataException{
		return reqDataByService(obj,Const.METHOD_POST);
	}
	
	/**
	 * 
	 * <br>description : 组织用HTTP GET 形式请求平台的数据
	 * @param obj
	 * @return
	 * @throws ReqDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:18:04
	 */
	public static ReqData makeReqDataByGet(Object obj) throws ReqDataException{
		return makeReqDataByService(obj,Const.METHOD_GET);
	}
	
	/**
	 * 
	 * <br>description : 组织用HTTP POST 形式请求平台的数据
	 * @param obj
	 * @return
	 * @throws ReqDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:18:04
	 */
	public static ReqData makeReqDataByPost(Object obj) throws ReqDataException{
		return makeReqDataByService(obj,Const.METHOD_POST);
	}
	
	/**
	 * 
	 * <br>description : 平台通知商户后，获取商户响应给平台的数据
	 * @param obj
	 * @return
	 * @throws ParameterCheckException
	 * @version     1.0
	 * @date        2014-7-25上午09:26:36
	 */
	public static String merNotifyResData(Object obj)throws ParameterCheckException{
		Map map = new HashMap();
		map.putAll(PlainUtil.notifyPlain(obj, true));
		String plain = map.get(Const.PLAIN).toString();
		String sign = map.get(Const.SIGN).toString();
		return plain + "&sign=" + sign;
	}
	
	private static ReqData reqDataByService(Object obj, String method) throws ReqDataException{
		String appname = getAppName();
		log_.info("获取到应用的名称：" + appname);
		return Mer2PlatUtils.getReqData(appname, obj, method);
	}
	
	private static ReqData makeReqDataByService(Object obj, String method) throws ReqDataException{
		String appname = getAppName();
		log_.info("获取到应用的名称：" + appname);
		return Mer2PlatUtils.makeReqData(appname, obj, method);
	}
	
	private static String getAppName(){
		String name = Const.PLAT_APP_NAME_PAY;
		return name;
	}
	
	/*private static String getAppName(){
		String pay_appname_configname = "plat.pay.product.name";
		String name = ProFileUtil.getPro(pay_appname_configname);
		if(name==null || "".equals(name)){
			name = Const.PLAT_APP_NAME_PAY;
		}
		return name;
	}*/

}
