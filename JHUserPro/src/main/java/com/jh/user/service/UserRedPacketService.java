package com.jh.user.service;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserRedPacketBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.UserRedPacket;
import com.jh.user.util.LotteryUtil;
import com.jh.user.util.Util;
import java.util.Random;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;

@Controller
@EnableAutoConfiguration
public class UserRedPacketService {
	@Autowired
	Util util;
	private static final Logger LOG = LoggerFactory.getLogger(UserRedPacketService.class);
	@Autowired
	private UserRedPacketBusiness userRedPacketBusiness;
	@Autowired
	private BrandManageBusiness brandManageBusiness;
	/** 添加红包信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/redPacket/save/{token}")
	public @ResponseBody Object saveUserRedPacket(HttpServletRequest request,
			@PathVariable("token") String token,
			/***编号***/
			@RequestParam(value = "id",  defaultValue = "0",  required=false) long id,
			/***奖励描述***/
			@RequestParam(value = "red_title",  defaultValue = "",  required=false) String redTitle,
			/***奖励类型  0:现金奖励； 1：积分奖励 ；3：其他奖励***/
			@RequestParam(value = "type") int type,
			/***区间最大值****/
			@RequestParam(value = "max_balance") BigDecimal maxBalance,
			/***区间最小值****/
			@RequestParam(value = "min_balance") BigDecimal minBalance,
			/***获奖概率***/
			@RequestParam(value = "ratio") int ratio
			) {
		Map map = new HashMap();
		long userId;
		long brandId;
		try {
			userId = TokenUtil.getUserId(token);
			brandId=TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		Brand brand =brandManageBusiness.findBrandById(brandId);
		//判断是否是贴牌商
		if(brand!=null && brand.getManageid()==userId){
			UserRedPacket  UserRedPacket=null;
			if(id!=0){
				UserRedPacket =userRedPacketBusiness.findUserRedPacketById(id);
			}
			if(UserRedPacket==null){
				UserRedPacket=new UserRedPacket();
				UserRedPacket.setCreateTime(new Date());
			}
			
			
			if(maxBalance.compareTo(minBalance)==-1){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESULT, "");
				map.put(CommonConstants.RESP_MESSAGE, "区间最大值不能小于区间最小值");
				return 	map;
			}
			UserRedPacket.setRedTitle(redTitle);
			UserRedPacket.setMaxBalance(maxBalance);
			UserRedPacket.setMinBalance(minBalance);
			UserRedPacket.setType(type);
			UserRedPacket.setBrandId(brandId);
			UserRedPacket.setRatio(ratio);
			userRedPacketBusiness.saveUserRedPacket(UserRedPacket);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, UserRedPacket);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESULT, "");
			map.put(CommonConstants.RESP_MESSAGE, "你无此权限");
		}
		return map;
	}
	
	/***
	 * 抽奖获取抽奖金额
	 * **/
	/** 添加红包信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/redPacket/get/rednum")
	public @ResponseBody Object getrednume(HttpServletRequest request,
			/***贴牌编号***/
			@RequestParam(value = "brand_id") long brandId
			) {
		Map map = new HashMap();
		List<UserRedPacket> UserRedPacketList =userRedPacketBusiness.findUserRedPacketByBid(brandId);
		UserRedPacket UserRedPacket=null;
		int ishan=0;
		int type =0;
		if(UserRedPacketList == null || UserRedPacketList.size() == 0){
			
		}else{
			if(UserRedPacketList.size()==1){
				UserRedPacket=UserRedPacketList.get(0);
			}else{
				int x=0;
				Map<Integer, String>  indexRedId = new HashMap<Integer, String>();
				List<Double> indexs = new ArrayList<Double>();  
				for(UserRedPacket userRedPacket : UserRedPacketList) {
					indexs.add(new Double(userRedPacket.getRatio()));
					indexRedId.put(x, userRedPacket.getId()+"");
					x++;
				}
				LotteryUtil ll = new LotteryUtil(indexs);  
				int index = ll.randomColunmIndex();  
				if(index < 0) {
					
				}else{
					UserRedPacket=userRedPacketBusiness.findUserRedPacketById(Long.parseLong(indexRedId.get(index)));
				}	
			}
			if(UserRedPacket!=null){
				int maxInt =0;
				int minInt=0;
				if(UserRedPacket.getType()==0){
					maxInt=UserRedPacket.getMaxBalance().multiply(new BigDecimal(100)).intValue();
					minInt=UserRedPacket.getMinBalance().multiply(new BigDecimal(100)).intValue();
				}else{
					maxInt=UserRedPacket.getMaxBalance().intValue();
					minInt=UserRedPacket.getMinBalance().intValue();
				}
				type=UserRedPacket.getType();
				Random rand = new Random();
				ishan = rand.nextInt(maxInt-minInt+1) + maxInt;
			}
		}
		Map<String,String> mapResult = new HashMap<String,String>();
		mapResult.put("ishan", ishan+"");
		mapResult.put("type", type+"");
		if(ishan>0){
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, mapResult);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESULT, "");
			map.put(CommonConstants.RESP_MESSAGE, "暂无数据");
		}
		
		
		return map;
	}
	
	
	
	
	
	
}
