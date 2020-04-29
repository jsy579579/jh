package com.jh.paymentchannel.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.BrandManageBusiness;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;
import com.jh.paymentchannel.util.SwiftSignUtils;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.XmlUtils;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class SwiftCallBackService {

	
	private static final Logger LOG = LoggerFactory.getLogger(SwiftCallBackService.class);
	
	@Autowired
	Util util;
	
	/*@Value("${swiftpass.merkey}")
	private String merKey;*/
	
	@Autowired
	private BrandManageBusiness brandManageBusiness; 
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/swift/notify_call")
	public @ResponseBody Object notifyCall(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		 try {
	            req.setCharacterEncoding("utf-8");
	            resp.setCharacterEncoding("utf-8");
	            resp.setHeader("Content-type", "text/html;charset=UTF-8");
	            String resString = XmlUtils.parseRequst(req);
	            //System.out.println("通知内容：" + resString);
	            LOG.info("SwiftCallBackService====="+resString);
	            String respString = "fail";
	            if(resString != null && !"".equals(resString)){
	                Map<String,String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
	                String res = XmlUtils.toXml(map);
	                //System.out.println("通知内容：" + res);
	                
	                
	                
	                
	                
	                if(map.containsKey("sign")){
	                	
	                	/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
	            		URI uri = util.getServiceUrl("transactionclear", "error url request!");
	            		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
	            		
	            		RestTemplate restTemplate=new RestTemplate();
	            		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
	            		requestEntity.add("order_code",  map.get("out_trade_no"));
	            		String result = restTemplate.postForObject(url, requestEntity, String.class);
	            		
	            		JSONObject jsonObject =  JSONObject.fromObject(result);
	            		JSONObject resultObj  =  jsonObject.getJSONObject("result");
	            		String brandid  =  resultObj.getString("brandid");
	            		SwiftBrandMerchant brandmerchant = brandManageBusiness.getSwiftBrandMerchant(brandid);
	                	
	                    if(!SwiftSignUtils.checkParam(map, brandmerchant.getSubMerchantKey())){
	                        res = "验证签名不通过";
	                        respString = "fail";
	                    }else{
	                        String status = map.get("status");
	                        if(status != null && "0".equals(status)){
	                            String result_code = map.get("result_code");
	                            if(result_code != null && "0".equals(result_code)){
	                               /* if(TestPayServlet.orderResult == null){
	                                    TestPayServlet.orderResult = new HashMap<String,String>();
	                                }
	                                String out_trade_no = map.get("out_trade_no");
	                                TestPayServlet.orderResult.put(out_trade_no, "1");
	                                //System.out.println(TestPayServlet.orderResult);*/
	                            	
	                            	/**更新订单状态*/
	    							/**调用下单，需要得到用户的订单信息*/
	    						    restTemplate=new RestTemplate();
	    							
	    							uri = util.getServiceUrl("transactionclear", "error url request!");
	    							url = uri.toString() + "/v1.0/transactionclear/payment/update";
	    							
	    							/**根据的用户手机号码查询用户的基本信息*/
	    							requestEntity  = new LinkedMultiValueMap<String, String>();
	    							requestEntity.add("status", "1");
	    							requestEntity.add("order_code",   map.get("out_trade_no"));
	    							requestEntity.add("third_code", map.get("transaction_id"));
	    							result = restTemplate.postForObject(url, requestEntity, String.class);
	    							
	    							/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
	    							uri = util.getServiceUrl("transactionclear", "error url request!");
	    							url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
	    							
	    							
	    							requestEntity  = new LinkedMultiValueMap<String, String>();
	    							requestEntity.add("order_code",   map.get("out_trade_no"));
	    							result = restTemplate.postForObject(url, requestEntity, String.class);
	    							
	    							jsonObject =  JSONObject.fromObject(result);
	    							resultObj  =  jsonObject.getJSONObject("result");
	    							String outMerOrdercode  =  resultObj.getString("outMerOrdercode");
	    							String orderdesc        =  resultObj.getString("desc");
	    							String phone            =  resultObj.getString("phone");
	    							String  tranamount      =  resultObj.getString("amount");
	    							String channelTag       =  resultObj.getString("channelTag");
	    							String notifyURL        =  resultObj.getString("outNotifyUrl");
	    							//String openid           =  resultObj.getString("openid");
	    							
	    							/*if(openid != null && !openid.equalsIgnoreCase("")){
	    								
	    								if(channelTag.equalsIgnoreCase("GONGZHONGHAO_WEXIN")){
	    									sendMessage(map.get("out_trade_no"), tranamount+"元", openid);
	    								}
	    								
	    								
	    							}*/
	    							
	    							
	    							
	    							if(outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")){
	    								uri = util.getServiceUrl("channel", "error url request!");
	    								url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
	    								requestEntity  = new LinkedMultiValueMap<String, String>();
	    								requestEntity.add("merchant_no",  phone);
	    								requestEntity.add("amount",  tranamount);
	    								requestEntity.add("channel_tag",  channelTag);
	    								requestEntity.add("order_desc",  URLEncoder.encode(orderdesc, "UTF-8"));
	    								requestEntity.add("order_code",  outMerOrdercode);
	    								requestEntity.add("sys_order",   map.get("out_trade_no"));
	    								requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
	    								result = restTemplate.postForObject(url, requestEntity, String.class);
	    							}
	                                 
	                            } 
	                        } 
	                        respString = "success";
	                    }
	                }
	            }
	            resp.getWriter().write(respString);
	        } catch (Exception e) {
	            e.printStackTrace();LOG.error("",e);
	        }
		 
		 
		 return null;
	}
	
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/swift/return_call")
	public String returnCall(HttpServletRequest req, HttpServletResponse resp,  Model model) throws IOException{
		
		
		String message  = "交易成功";
		
		 try {
	            String resString = XmlUtils.parseRequst(req);
	            
	            if(resString != null && !"".equals(resString)){
	                Map<String,String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
	                String res = XmlUtils.toXml(map);
	                //System.out.println("通知内容：" + res);
	                if(map.containsKey("sign")){
	                	
	                	/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
	            		URI uri = util.getServiceUrl("transactionclear", "error url request!");
	            		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
	            		
	            		RestTemplate restTemplate=new RestTemplate();
	            		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
	            		requestEntity.add("order_code",  map.get("out_trade_no"));
	            		String result = restTemplate.postForObject(url, requestEntity, String.class);
	            		
	            		JSONObject jsonObject =  JSONObject.fromObject(result);
	            		JSONObject resultObj  =  jsonObject.getJSONObject("result");
	            		String brandid  =  resultObj.getString("brandid");
	            		SwiftBrandMerchant brandmerchant = brandManageBusiness.getSwiftBrandMerchant(brandid);
	                	
	                	
	                    if(!SwiftSignUtils.checkParam(map, brandmerchant.getSubMerchantKey())){
	                        res = "验证签名不通过";
	                        message   = "交易失败";
	                    
	                    }else{
	                        String status = map.get("status");
	                        if(status != null && "0".equals(status)){
	                            String result_code = map.get("result_code");
	                            if(result_code != null && "0".equals(result_code)){
	                              
	                                 
	                            } 
	                        } 
	                    }
	                    
	                }else{
	                	
	                	message  = "交易失败";
	                	
	                }
	            }
	           
	        } catch (Exception e) {
	            e.printStackTrace();LOG.error("",e);
	        }
		 
		
		
		
		model.addAttribute("message", message);
		return "paymentreturn";
		
	}
	
	
	public void sendMessage(String ordercode,  String amount, String openid){
		
		
		 String ownerName = "恭喜您! 您的交易已经成功了 ";
		  String type = "订单号";
		  String result = "已成功";
		  String remark = "";
		  String data="'first':{'value':'"+ownerName+"','color':'#173177'},'accountType':{'value':'"+type+"','color':'#173177'},'account':{'value':'"+ordercode+"','color':'#173177'},'amount':{'value':'"+amount+"','color':'#173177'},'result':{'value':'"+result+"','color':'#173177'},'remark':{'value':'"+remark+"','color':'#173177'}";

		  
		  String jsonData="{'touser':'"+openid+"','template_id':'EgNQS-uyOtdEGR44avENGCQ4v5h4lJbxd3RajsrsZ1g','data':{"+data+"}}";
		  
		  RestTemplate restTemplate=new RestTemplate();
		  String userAccessToken =  "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx964293b158c6e6af&secret=57206890198734345a8f8f98dfaec682";
		  
		  String resultStr = restTemplate.getForObject(userAccessToken,  String.class);
		  JSONObject jsonObject =  JSONObject.fromObject(resultStr);
		  String accessToken = jsonObject.getString("access_token");
		  
		  restTemplate=new RestTemplate();
		  
//		  access_token= "12pfEZ3Jy8OdZ1AHSgbuGZZ-bFMaW9Vil1osyVeglzyiftjE1_OAslHPeOQ-D7uaklw-NTUoAZouJL9QrJ05kix1AmlfHfBfnbh5IN2Gg5Q7x3vnPTIUl1ZZVsmHYlKGBCEgABAEFH";
	      
	      jsonObject = JSONObject.fromObject(jsonData);

	  
	      HttpEntity<Object> requestEntity = new HttpEntity<Object>(jsonObject,null); 

	      ResponseEntity<String> resultStrx  =restTemplate.exchange("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+accessToken,HttpMethod.POST,requestEntity,String.class);

	      
	      JSONObject returnJson = JSONObject.fromObject(resultStrx.getBody());
	      
	      String errcode=returnJson.getString("errcode");
	      System.out.println("errcode="+errcode);
		
		
	}
	
	
	
	
}
