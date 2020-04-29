package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.WithDrawOrder;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.exception.ReqDataException;
import com.jh.paymentchannel.util.ump.exception.RetDataException;
import com.jh.paymentchannel.util.ump.paygate.v40.HttpRequest;
import com.jh.paymentchannel.util.ump.paygate.v40.Mer2Plat_v40;
import com.jh.paymentchannel.util.ump.paygate.v40.Plat2Mer_v40;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Service
public class UMPayEMFRequest implements PayRequest{
	private static final Logger LOG = LoggerFactory.getLogger(UMPayEMFRequest.class);
	
	
	@Autowired
	Util util;
	//联动优势地址
	public static final String umpurl = "http://pay.soopay.net/spay/pay/payservice.do";
	//商户入驻成功
	@Value("${ump.merid}")
	public   String merchantCode;
	
	@Value("${ump.merprikeypath}")
	private  String merprikeypath;
	
	@Value("${ump.platcertpath}")
	private  String platcertpath;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Override
	public WithDrawOrder payRequest(String ordercode,
			String cardno, String username, String amount, String bankname, String phone,  String priOrpub,String notifyURL,String returnURL) {
				//银行编号
				String bankAbbreviation=null;
				WithDrawOrder drawOrder = new WithDrawOrder();
				if(bankname!=null&&!bankname.equals("")){
					/**获取银行编号*/
					RestTemplate restTemplate=new RestTemplate();
					URI uri = util.getServiceUrl("user", "error url request!");
					String url = uri.toString() + "/v1.0/user/bank/acronym/name";
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("bank_name", bankname);
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================"+result);
					JSONObject jsonObject =  JSONObject.fromObject(result);
					JSONObject resultObj  =  jsonObject.getJSONObject("result");
					if(resultObj.containsKey("bankAcronym")){
						bankAbbreviation       =  resultObj.getString("bankAcronym");
					}else{
						restTemplate=new RestTemplate();
						uri = util.getServiceUrl("transactionclear", "error url request!");
						url = uri.toString() + "/v1.0/transactionclear/payment/update";
						/**根据的用户手机号码查询用户的基本信息*/
						requestEntity  = new LinkedMultiValueMap<String, String>();
						requestEntity.add("status", "2");
						requestEntity.add("order_code",  ordercode);
						restTemplate.postForObject(url, requestEntity, String.class);
						drawOrder.setReqcode("99999");
						drawOrder.setResmsg("银行卡不支持！！");
						return drawOrder;
					}
				}else{
					drawOrder.setReqcode(CommonConstants.FALIED);
					RestTemplate restTemplate=new RestTemplate();
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					/**根据的用户手机号码查询用户的基本信息*/
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "2");
					requestEntity.add("order_code",  ordercode);
					restTemplate.postForObject(url, requestEntity, String.class);
					drawOrder.setReqcode("99999");
					drawOrder.setResmsg("银行卡不支持！！");
					return drawOrder;
				}
				Map map = new HashMap();  
				//接口名称【service】
				map.put("service","epay_direct_req"); 
				//参数字符编码集【charset】： 	
				map.put("charset","UTF-8")  ;
				//商户编号【mer_id】
				map.put("mer_id",merchantCode)  ;
				//签名方式【sign_type】
				map.put("sign_type","RSA")  ;
				//服务器异步通知页面路径【notify_url】
				map.put("notify_url",ipAddress+"/v1.0/paymentchannel/topup/ump/notify_call");
				//响应数据格式【res_format】
				map.put("res_format","HTML");
				//版本号【version】
				map.put("version","4.0");
				//商户唯一订单号【order_id】
				map.put("order_id",ordercode);
				//商户订单日期【mer_date】
				map.put("mer_date",new SimpleDateFormat("yyyyMMdd").format(new Date()))  ;
				//付款金额【amount】
				map.put("amount",new  BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString());
				//收款方账户类型【recv_account_type】00:银行卡；02:U付账户
				map.put("recv_account_type","00")  ;
				//收款方账户属性【recv_bank_acc_pro】0对私；1对公
				map.put("recv_bank_acc_pro","0");
				//收款方账号【recv_account】
				map.put("recv_account",cardno);
				//收款方户名【recv_user_name】
				map.put("recv_user_name",username);
				//手续费扣款类型(1、外扣：从手续费账户扣。2、内扣 e秒付专用账户。内扣：付款金额（amount）=实际到账金额+手续费)
				map.put("cut_fee_type","2");
				//收款方账户的发卡行【recv_gate_id】
				map.put("recv_gate_id",bankAbbreviation);
				//收款方手机号【mobile_no】
				map.put("mobile_no",phone);
				LOG.info("联动提现请求="+map.toString());
				try {
					ReqData reqDataGet = Mer2Plat_v40.makeReqDataByGet(map);  
					String get_url = reqDataGet.getUrl();     //get请求报文，get方式请求此报文即可  
					String Result =  HttpRequest.sendGet(get_url);

					/**
					 * 以下是对联动的响应结果做验签的示例
					 * 
					 */
					
					Map dataMap = new HashMap();
					try {
						dataMap = Plat2Mer_v40.getResData(Result);
						LOG.info("dataMap.get(ret_code)="+dataMap.get("ret_code"));
						 if(dataMap.get("ret_code").equals("00131040")){
							LOG.debug("联动提现请求失败=");
							RestTemplate restTemplate=new RestTemplate();
							URI uri = util.getServiceUrl("transactionclear", "error url request!");
							String url = uri.toString() + "/v1.0/transactionclear/payment/update";
							
							/**根据的用户手机号码查询用户的基本信息*/
							MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
							requestEntity.add("status", "2");
							requestEntity.add("order_code",  ordercode);
							restTemplate.postForObject(url, requestEntity, String.class);
							drawOrder.setReqcode("99999");
							drawOrder.setResmsg("下单失败");
						}else{
							
							if(dataMap.get("ret_code").equals("4")){
								drawOrder.setReqcode("0000");
								drawOrder.setResmsg("提现成功");								
							}else{
								drawOrder.setReqcode("0000");
								drawOrder.setResmsg("下单成功");
							}
						}
					} catch (RetDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();LOG.error("",e);
						LOG.debug("联动提现请求失败=");
						RestTemplate restTemplate=new RestTemplate();
						URI uri = util.getServiceUrl("transactionclear", "error url request!");
						String url = uri.toString() + "/v1.0/transactionclear/payment/update";
						
						/**根据的用户手机号码查询用户的基本信息*/
						MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
						requestEntity.add("status", "2");
						requestEntity.add("order_code",  ordercode);
						restTemplate.postForObject(url, requestEntity, String.class);
						drawOrder.setReqcode("99999");
						drawOrder.setResmsg("下单失败");
						
					} //重要 ，不抛异常说明验签成功，并返回签名数据  传递 InputStream 或者 String
					Set set = dataMap.keySet();
					
				} catch (ReqDataException e) {
					LOG.debug("联动提现请求失败=");
					RestTemplate restTemplate=new RestTemplate();
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					/**根据的用户手机号码查询用户的基本信息*/
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "2");
					requestEntity.add("order_code",  ordercode);
					restTemplate.postForObject(url, requestEntity, String.class);
					drawOrder.setReqcode("99999");
					drawOrder.setResmsg("下单失败");
				}  
		
		return drawOrder;
	}

	@Override
	public WithDrawOrder queryPay(String ordercode) {
		
		return null;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		

	
	
	


}
