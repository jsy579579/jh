package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.ChannelDetail;
import com.jh.paymentchannel.pojo.TopupPayChannelRoute;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



@Controller
@EnableAutoConfiguration
public class ChannelRouteService {

	
	private static final Logger log = LoggerFactory.getLogger(ChannelRouteService.class);
	
	@Autowired
	private Util util;
	
	@Autowired
	private TopupPayChannelBusiness  topupPayChannelBusiness;
	
	/**获取指定路由配置**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/route/query")
	public @ResponseBody Object paymentchannelrouteByBrandChanne(HttpServletRequest request, 
			@RequestParam(value = "brandcode") String brandcode,
			@RequestParam(value = "channel_type") String channelType,	
			@RequestParam(value = "channel_tag") String channelTag){
		
		TopupPayChannelRoute payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandcode(brandcode, channelType, channelTag);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, payChannelRoute.getTargetChannelTag());
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	/**路由查询接口**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/route/query/all")
	public @ResponseBody Object paymentchannelroute(HttpServletRequest request, 
			//贴牌id
			@RequestParam(value = "brandcode", defaultValue = "", required = false) String brandcode,
			//
			@RequestParam(value = "channel_type", defaultValue = "", required = false) String channelType,	
			//
			@RequestParam(value = "channel_tag", defaultValue = "", required = false) String channelTag){
		
		Map map = new HashMap();
		
		List<TopupPayChannelRoute> payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandId(brandcode, channelType, channelTag);
		
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, payChannelRoute);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**添加/修改路由**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/route/add")
	public @ResponseBody Object paymentchannelrouteadd(HttpServletRequest request, 
			//贴牌id
			@RequestParam(value = "brandcode") String brandcode,
			
			///**渠道标识*/
			@RequestParam(value = "channel_tag") String channelTag,
			
			///**渠道哦名字*/
			@RequestParam(value = "channel_name") String channelName,	

			///***目标走的通到*/
			@RequestParam(value = "target_channel_tag") String targetChannelTag,	

			/**类型  0 为充值通道，  类型 1 为代付渠道     类型2为提现渠道*/
			@RequestParam(value = "channel_type", required = false) String channelType){
		
		Map map = new HashMap();
		
		if(channelType==null||channelType.length()==0||channelType.length()>1){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "channel_type不能为空");
			return map;
		}
		if(brandcode==null||brandcode.equals("")){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌编号不能为空");
			return map;
		}
		if(channelTag==null||channelTag.equals("")){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "渠道标识不能为空");
			return map;
		}
		if(channelName==null||channelName.equals("")){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "渠道名字不能为空");
			return map;
		}
		if(targetChannelTag==null||targetChannelTag.equals("")){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "目标走的通到不能为空");
			return map;
		}
		TopupPayChannelRoute payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandcode(brandcode, channelType, channelTag);
		
		
		if(payChannelRoute==null){
			payChannelRoute=new TopupPayChannelRoute();
		}
		payChannelRoute.setBrandcode(brandcode);
		payChannelRoute.setChannelcode(channelTag);
		payChannelRoute.setChannelName(channelName);
		payChannelRoute.setTargetChannelTag(targetChannelTag);
		payChannelRoute.setType(channelType);
		payChannelRoute.setCreateTime(new Date());
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, topupPayChannelBusiness.saveTopupPayChannelRoute(payChannelRoute));
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**获取系统通道**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/detail/query/all")
	public @ResponseBody Object ChannelDetail(HttpServletRequest request, 
			//贴牌id
			@RequestParam(value = "brandcode", defaultValue = "", required = false) String brandcode,
			//
			@RequestParam(value = "channel_type", defaultValue = "", required = false) String channelType,	
			//
			@RequestParam(value = "channel_tag", defaultValue = "", required = false) String channelTag){
		
		Map map = new HashMap();
		
		List<ChannelDetail> payChannelRoute = topupPayChannelBusiness.getChannelDetail();
		
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, payChannelRoute);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	
	
	//一键配置通道路由
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/channelroute/config")
	public @ResponseBody Object configTopupPayChannelRoute(HttpServletRequest request,
			@RequestParam(value="brand_code") String brandcode,
			@RequestParam(value="brand_code1", defaultValue="2", required=false) String brandcode1
			){
			
			//根据brand_code1(默认值等于2) 查询出通道路由 并存入 一个list集合
			List<TopupPayChannelRoute> tpcr = topupPayChannelBusiness.getPayChannelByBrandcode(brandcode1);
			
			//根据brand_code(输入要配置通道路由的贴牌ID) 查询该贴牌是否已配置通道路由
			List<TopupPayChannelRoute> list = topupPayChannelBusiness.getPayChannelByBrandcode(brandcode);
			
			Map map = new HashMap();
			
			//循环遍历这个集合
			for(TopupPayChannelRoute top:tpcr){

				TopupPayChannelRoute topup = new TopupPayChannelRoute();
				
				int a=0;
				
				if(list!=null&&list.size()>0){
					for(TopupPayChannelRoute newtop:list){
						if(newtop!=null&&newtop.getChannelcode()!=null&&newtop.getType()!=null){
							if (newtop.getChannelcode().equals(top.getChannelcode())&&newtop.getType().equals(top.getType())) {
								a=1;
								break;
							}
						}
					}
				}
				if(a==1){
					continue;
				}
				topup.setBrandcode(brandcode);
				topup.setChannelcode(top.getChannelcode());
				topup.setChannelName(top.getChannelName());
				topup.setTargetChannelTag(top.getTargetChannelTag());
				topup.setType(top.getType());
				topup.setCreateTime(top.getCreateTime());
				//调用一个save方法 将数据存入数据库
				topupPayChannelBusiness.configTopupPayChannelRoute(topup);
				
			}
				
				
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");	
			return map;
			
	}
	
	
	
	//一键配置所有贴牌的通道路由
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/channelroute/config/allbrand")
	public @ResponseBody Object configAllBrandTopupPayChannelRoute(HttpServletRequest request ){
		
		Map<String, String> map = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/all";
		List<String> list = new ArrayList<String>();;
		JSONObject jsonObject;
		JSONArray jsonArray = null;
		try {
			String result = restTemplate.getForObject(url, String.class);
			log.info("result=========="+result);
			
			jsonObject = JSONObject.fromObject(result);
			jsonArray = jsonObject.getJSONArray("result");
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
		}
		
		for(int i = 0; i<jsonArray.size(); i++) {
			JSONObject jsonObject2 = jsonArray.getJSONObject(i);
			String brandId = jsonObject2.getString("id");
			
			list.add(brandId);
		}
		log.info("list==="+list);
		
		//查询出 节付宝的 通道路由 并存入 一个list集合
		List<TopupPayChannelRoute> tpcr = topupPayChannelBusiness.getPayChannelByBrandcode("2");
		
		for(int j = 0; j<list.size(); j++) {
			
			//查询出 目标贴牌 的 通道路由 并存入 一个list集合
			List<TopupPayChannelRoute> tpcr1 = topupPayChannelBusiness.getPayChannelByBrandcode(list.get(j));
			
			//循环遍历这个集合
			for(TopupPayChannelRoute top:tpcr){
				
				int a = 0;
				
				TopupPayChannelRoute topup = new TopupPayChannelRoute();
				
				if(tpcr1!=null&&tpcr1.size()>0){
					for(TopupPayChannelRoute newtop:tpcr1){
						if(newtop!=null&&newtop.getChannelcode()!=null&&newtop.getType()!=null){
							if (newtop.getChannelcode().equals(top.getChannelcode())&&newtop.getType().equals(top.getType())) {
								a = 1;
								break;
							}
						}
					}
				}
				
				if(a==1){
					continue;
				}
				topup.setBrandcode(list.get(j));
				topup.setChannelcode(top.getChannelcode());
				topup.setChannelName(top.getChannelName());
				topup.setTargetChannelTag(top.getTargetChannelTag());
				topup.setType(top.getType());
				topup.setCreateTime(top.getCreateTime());
				//调用一个save方法 将数据存入数据库
				try {
					topupPayChannelBusiness.configTopupPayChannelRoute(topup);
				} catch (Exception e) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "失败");
				}
				
			}
			
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	
	
}
