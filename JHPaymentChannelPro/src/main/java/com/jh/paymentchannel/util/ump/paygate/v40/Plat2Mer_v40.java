package com.jh.paymentchannel.util.ump.paygate.v40;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.jh.paymentchannel.util.ump.common.Const;
import com.jh.paymentchannel.util.ump.exception.RetDataException;
import com.jh.paymentchannel.util.ump.exception.VerifyException;
import com.jh.paymentchannel.util.ump.log.ILogger;
import com.jh.paymentchannel.util.ump.log.LogManager;
import com.jh.paymentchannel.util.ump.util.DataUtil;
import com.jh.paymentchannel.util.ump.util.HttpMerParserUtil;
import com.jh.paymentchannel.util.ump.util.PlainUtil;
import com.jh.paymentchannel.util.ump.util.SignUtil;
import com.jh.paymentchannel.util.ump.util.StringUtil;

/**
 * ***********************************************************************
 * <br>description : 联动平台请求或响应给商户信息处理类
 * @author      umpay
 * @date        2014-8-1 上午09:24:31
 * @version     1.0  
 ************************************************************************
 */
public class Plat2Mer_v40 {
	private static final ILogger log = LogManager.getLogger();

	/**
	 * 
	 * <br>description : 商户请求平台后，解析平台响应给商户的html格式的数据，并验证平台签名
	 * @param html
	 * @return
	 * @throws RetDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:15:56
	 */
	public static Map getResData(String html)throws RetDataException{
		Map data = new HashMap();
		try{
			data = getData(html);
		}catch(Exception e){
			throw new RetDataException("解析后台平台响应数据出错",e);
		}
		return data;
	}
	
	/**
	 * 
	 * <br>description : 商户请求平台后，解析平台响应给商户的数据流格式的数据，并验证平台签名
	 * @param in
	 * @return
	 * @throws RetDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:16:07
	 */
	public static Map getResData(InputStream in) throws RetDataException{
		Map data = new HashMap();
		try{
			data = getDataByStream(in);
		}catch(Exception e){
			throw new RetDataException("解析后台平台响应数据出错",e);
		}
		return data;
	}
	
	/**
	 * 
	 * <br>description : 商户请求平台后，解析平台响应给商户的meta格式的数据，并验证平台签名
	 * @param meta
	 * @return
	 * @throws RetDataException
	 * @version     1.0
	 * @date        2014-7-24下午08:16:16
	 */
	public static Map getResDataByMeta(String meta) throws RetDataException{
		Map data = new HashMap();
		try{
			data = getDataByContent(meta);
		}catch(Exception e){
			throw new RetDataException("解析后台平台响应数据出错",e);
		}
		return data;
	}
	
	/**
	 * 
	 * <br>description : 解析平台主动通知给商户的数据并验签
	 * @param obj
	 * @return
	 * @throws VerifyException
	 * @version     1.0
	 * @date        2014-7-24下午08:16:45
	 */
	public static Map getPlatNotifyData(Object obj) throws VerifyException{
		Map data = DataUtil.getData(obj);
		log.debug("支付结果通知请求数据为:" + data);
		if(data==null || data.size()==0){
			throw new VerifyException("待解析的数据对象为空");
		}
		String sign = data.get(Const.SIGN).toString();
		Map retMap = PlainUtil.notifyPlain(obj,false);
		String plain = retMap.get(Const.PLAIN).toString();
		
		boolean checked = SignUtil.verify(sign, plain);
		if(!checked){
			throw new VerifyException("平台数据验签失败");
		}
		log.debug("平台数据验签成功！！！"+data);
		return data;
	}
	
	
	private static Map getData(String html) throws VerifyException{
		if(StringUtil.isEmpty(html)){
			throw new RuntimeException("请传入需解析的HTML");
		}
		String content = HttpMerParserUtil.getMeta(html);
		return getDataByContent(content);
	}
	
	private static Map getDataByStream(InputStream in) throws IOException,VerifyException{
		String html = HttpMerParserUtil.getHtml(in);
		log.info("根据流获取到的HTML为：" + html);
		String content = HttpMerParserUtil.getMeta(html);
		log.info("根据HTML获取到的meta内容为：" + content);
		return getDataByContent(content);
	}
	
	private static Map getDataByContent(String content) throws VerifyException{
		String plain = "",sign = "";
		Map map = new HashMap();
		try{
			map = PlainUtil.getResPlain(content);
			plain = map.get(Const.PLAIN).toString();
			sign =  map.get(Const.SIGN).toString();
		}catch(Exception e){
			log.info("请求数据分解发生异常" + e);
		}
		if(!SignUtil.verify(sign, plain)){
			throw new VerifyException("数据验签失败");
		}else{
			log.info("验签正确");
		}
		return map;
	}
}
