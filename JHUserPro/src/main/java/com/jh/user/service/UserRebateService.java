package com.jh.user.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRebateHistoryBusiness;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserRebateHistory;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.TokenUtil;

@Controller
@EnableAutoConfiguration
public class UserRebateService {

	private static final Logger LOG = LoggerFactory.getLogger(UserRebateService.class);
	
	@Autowired 
	private UserRebateHistoryBusiness userRebateHistoryBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness  userLoginRegisterBusiness;
	
	@Autowired 
	private UserBalanceBusiness userBalBusiness;
	
	@Autowired
	private UserJpushService userJpushService;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private UserBalanceBusiness userBalanceBusiness;
	
	 @Autowired
	 Util util;
	 
	 /**更新用户的分润信息 */
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/filtrate")
		public @ResponseBody Object filtrateUserAccount(HttpServletRequest request,   
				@RequestParam(value = "user_id") long userId,
				@RequestParam(value = "rebate_amount",  defaultValue = "0") BigDecimal rebateamount,	
				@RequestParam(value = "order_code") String orderCode,				
				@RequestParam(value = "addorsub",  defaultValue = "0") String addorsub,
				@RequestParam(value = "order_type",  defaultValue = "0") String order_type
				){		
			Map<String,Object> map = new HashMap<String,Object>();
			//判断是否重复返利UserRebateHistory
			UserRebateHistory userRebateHistory=userRebateHistoryBusiness.findUserRebateHistory(userId, order_type, rebateamount.setScale(2, BigDecimal.ROUND_DOWN), addorsub, orderCode);
			if(userRebateHistory!=null) {
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "订单重复返利");
				LOG.info("重复返利订单"+orderCode);
			}else {
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "订单重复返利");
				map.put(CommonConstants.RESULT,userRebateHistory);
				LOG.info("重复返利订单"+orderCode);
			}
			return map;
			
		}
	
	/**更新用户的分润信息 2019.6.3更新接口名   ruanjiajun*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/update")
	public @ResponseBody Object updateUserAccount(HttpServletRequest request,   
			@RequestParam(value = "user_id") long userId,
			@RequestParam(value = "rebate_amount",  defaultValue = "0") BigDecimal rebateamount,	
			@RequestParam(value = "order_code") String orderCode,				
			@RequestParam(value = "addorsub",  defaultValue = "0") String addorsub,
			@RequestParam(value = "order_type",  defaultValue = "0") String order_type
			){		
		LOG.info("分润更新====================userId:" + userId + "=======================rebateamount:"+rebateamount+"========================orderCode:"+orderCode);
		Map<String,Object> map = new HashMap<String,Object>();
		//判断是否重复返利UserRebateHistory
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		UserAccount userAccount = userBalBusiness.updateUserRebate(userId, rebateamount, order_type,  addorsub, orderCode);
		
		map.put(CommonConstants.RESULT, userAccount);
		try {
			/**
			 * 推送消息
			 * /v1.0/user/jpush/tset
			 * */
			String rebatnum=rebateamount.setScale(2,BigDecimal.ROUND_DOWN).toString();
			if(!rebatnum.equals("0.00")){
				String alert ="账户分润";  
				String content = /*phone+"为您产生了一笔"+*/rebatnum+"元的分润已记入您的账户";
				String btype="rebate";
				String btypeval="";
				userJpushService.setJpushtest(request, userId, alert, content, btype, btypeval);
			}
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
		}
		return map;
	}

	/**更新用户的分润信息 2019.10.17拷贝接口，用于股东发放利润   ives*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/olderupdate")
	public @ResponseBody Object updateolderUserAccount(HttpServletRequest request,
												  @RequestParam(value = "user_id") long userId,
												  @RequestParam(value = "rebate_amount",  defaultValue = "0") BigDecimal rebateamount,
												  @RequestParam(value = "order_code") String orderCode,
												  @RequestParam(value = "addorsub",  defaultValue = "0") String addorsub,
												  @RequestParam(value = "order_type",  defaultValue = "5") String order_type
	){
		LOG.info("分润更新====================userId:" + userId + "=======================rebateamount:"+rebateamount+"========================orderCode:"+orderCode);
		Map<String,Object> map = new HashMap<String,Object>();
		//判断是否重复返利UserRebateHistory
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		UserAccount userAccount = userBalBusiness.updateUserRebate(userId, rebateamount, order_type,  addorsub, orderCode);

		map.put(CommonConstants.RESULT, userAccount);
		try {
			/**
			 * 推送消息
			 * /v1.0/user/jpush/tset
			 * */
			String rebatnum=rebateamount.setScale(2,BigDecimal.ROUND_DOWN).toString();
			if(!rebatnum.equals("0.00")){
				String alert ="账户股东分红";
				String content = /*phone+"为您产生了一笔"+*/rebatnum+"元的股东分红已记入您的账户";
				String btype="rebate";
				String btypeval="";
				userJpushService.setJpushtest(request, userId, alert, content, btype, btypeval);
			}
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
		}
		return map;
	}

	
	/**查询交易变更历史*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/query/{token}")
	public @ResponseBody Object pageBalanceQuery(HttpServletRequest request,  
			 @PathVariable("token") String token,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		
		Map<String,Object> map = new HashMap<String,Object>();
		long userId;
		try{
			userId = TokenUtil.getUserId(token);
		}catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		if(page <0){
			page = 0;
		}
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));

		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userRebateHistoryBusiness.queryUserRebateHistoryByUserid(userId, pageable));
		return map;
	}
	/**统计收益
	 * 当天、昨天、当月、当年、所有
	 * */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/query/sumrebate")
	public @ResponseBody Object pageBalanceQuery(HttpServletRequest request,  
			 @RequestParam(value = "user_id") long  userId,
			 @RequestParam(value = "end_time",  required = false) String  endTime,
			 @RequestParam(value = "start_time",  required = false) String startTime,
			 @RequestParam(value = "order_type", defaultValue = "1,0,2,3", required = false) String orderType
			){
		Map<String,Object> sumMap = new HashMap<String,Object>();
		String[] orderTypeA = orderType.split(",");
		{//当天
			Date startTimeDate = null;
			 if(startTime != null  && !startTime.equalsIgnoreCase("")){
				 startTimeDate = DateUtil.getDateFromStr(startTime);
				}else{
					startTime=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					startTimeDate = DateUtil.getDateFromStr(startTime);
				}
			 Date endTimeDate=null;
			 Calendar   calendar   =   new   GregorianCalendar(); 
		     calendar.setTime(startTimeDate); 
		     calendar.add(Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动 
		     endTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果 
		     BigDecimal sumRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeDate, endTimeDate);
		     sumMap.put("todayRebate",sumRebate!=null?sumRebate.setScale(2, BigDecimal.ROUND_DOWN):"0.00");
		}
		{//昨天
			Date startTimeDate = null;
			 if(startTime != null  && !startTime.equalsIgnoreCase("")){
				 startTimeDate = DateUtil.getDateFromStr(startTime);
				}else{
					startTime=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					startTimeDate = DateUtil.getDateFromStr(startTime);
				}
			Date endTimeDate=null;
			Calendar   calendar   =   new   GregorianCalendar(); 
		    calendar.setTime(startTimeDate); 
		    calendar.add(Calendar.DATE,-1);//把日期往后增加一天.整数往后推,负数往前移动 
		    startTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果 
		    calendar   =   new   GregorianCalendar(); 
		    calendar.setTime(startTimeDate); 
		    calendar.add(Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动 
		    endTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果 
		    BigDecimal sumRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeDate, endTimeDate);
			sumMap.put("yesterdayRebate",sumRebate!=null?sumRebate.setScale(2, BigDecimal.ROUND_DOWN):"0.00" );
		}
		{ //当月
			Date startTimeDate = null;
			 if(startTime != null  && !startTime.equalsIgnoreCase("")){
				 startTime=new SimpleDateFormat("yyyy-MM").format(DateUtil.getDateFromStr(startTime));
					startTimeDate = getYYMMDateFromStr(startTime);
				}else{
					startTime=new SimpleDateFormat("yyyy-MM").format(new Date());
					startTimeDate =getYYMMDateFromStr(startTime);
				}
			 Date endTimeDate=null;
			 Calendar   calendar   =   new   GregorianCalendar(); 
		     calendar.setTime(startTimeDate); 
		     calendar.add(Calendar.MONTH,1);//把日期往后增加一天.整数往后推,负数往前移动 
		     endTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果 
		     BigDecimal sumRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeDate, endTimeDate);
		     sumMap.put("monthRebate", sumRebate!=null?sumRebate.setScale(2, BigDecimal.ROUND_DOWN):"0.00");
		}
		{//所有
			Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
			Date endTimeDate=new Date();
			 BigDecimal sumRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeDate, endTimeDate);
			sumMap.put("allRebate",sumRebate!=null?sumRebate.setScale(2, BigDecimal.ROUND_DOWN):"0.00");
		}
		Map<String,Object> map = new HashMap<String,Object>();
		LOG.info("查询分润统计："+sumMap);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT,sumMap );
		return map;
	}
	/**统计收益
	 * 上月
	 * */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/rebate/query/sumrebatepremonth")
	public @ResponseBody Object pageBalanceQueryLastMonth(HttpServletRequest request,  
			 @RequestParam(value = "user_id") long  userId,
			 @RequestParam(value = "end_time",  required = false) String  endTime,
			 @RequestParam(value = "start_time",  required = false) String startTime,
			 @RequestParam(value = "order_type", defaultValue = "1,0", required = false) String orderType
			){
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> sumMap = new HashMap<String,Object>();
		try {
			String[] orderTypeA = orderType.split(",");
			Date startTimeDate = null;
			if(startTime != null  && !startTime.equalsIgnoreCase("")){
				startTime=new SimpleDateFormat("yyyy-MM").format(DateUtil.getDateFromStr(startTime));
				startTimeDate = getYYMMDateFromStr(startTime);
			}else{
				startTime=new SimpleDateFormat("yyyy-MM").format(new Date());
				startTimeDate =getYYMMDateFromStr(startTime);
			}
			Date endTimeDate=null;
			Calendar   calendar   =   new   GregorianCalendar(); 
			calendar.setTime(startTimeDate); 
			calendar.add(Calendar.MONTH,-1);//把日期往后增加一天.整数往后推,负数往前移动 
			endTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果 
			BigDecimal sumRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, endTimeDate,startTimeDate );
			if(null==sumRebate) {
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "上月没有收益");
			}else {
				sumMap.put("monthRebate", sumRebate!=null?sumRebate.setScale(2, BigDecimal.ROUND_DOWN):"0.00");
				LOG.info("查询分润统计："+sumMap);
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT,sumMap );
			}
			
		}catch(Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, e.getMessage());
		}
		return map;
	}
	public static Date getYYMMDateFromStr(String str){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date result = null;
		try {
			result =  sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();LOG.error("",e);
		}
		
		return result;
	}
	@RequestMapping(method = RequestMethod.POST,value="/v1.0/user/rebate/query/oneweek/{token}")
	public @ResponseBody Object getOneWeekRebate(HttpServletRequest request,
			@PathVariable(value="token")String token
			){
		Map<String,Object>map = new HashMap<>();
		Map<String,Object>sumRebateMap = new HashMap<>();
		Map<String,Object>returnSumRebateMap = new HashMap<>();
		long userId;
		try{
			userId = TokenUtil.getUserId(token);
		}catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar =  new GregorianCalendar(); 
		calendar.add(Calendar.DATE, -6);
		Date startDate = calendar.getTime();
		Date endDate = new Date();
		List<Object[]> models =  userRebateHistoryBusiness.findSumRebateHistoryByUserIdBetweenWeek(userId, new String[]{"0","1"},sdf.format(startDate),sdf.format(endDate));
		List<Object> weekRebate = new ArrayList<>();
		if(models!=null&&models.size()>0){
			for(int i = 0;i < models.size();i++){
				sumRebateMap.put(models.get(i)[0]+"", models.get(i)[1]);
			}
			if(!sumRebateMap.isEmpty()){
				for(int i = 0;i < 7;i++){
					calendar =  new GregorianCalendar();
					calendar.add(Calendar.DATE, -i);
					Object value = sumRebateMap.get(sdf.format(calendar.getTime()));
					returnSumRebateMap.put(i+"", value==null||"".equals(value)?0:value);
					weekRebate.add(value==null||"".equals(value)?BigDecimal.ZERO:value);
				}
			}else{
				for(int i = 0;i < 7;i++){
					returnSumRebateMap.put(i+"", 0);
					weekRebate.add(BigDecimal.ZERO);
				}
			}
		}else{
			for(int i = 0;i < 7;i++){
				returnSumRebateMap.put(i+"", 0);
				weekRebate.add(BigDecimal.ZERO);
			}
		}
		returnSumRebateMap.put("weekRebate", weekRebate);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, returnSumRebateMap);
		return map;
	}
	
	@RequestMapping(method = RequestMethod.POST,value="/v1.0/user/rebate/query/ranking/list")
	public @ResponseBody Object rebateRankingList(HttpServletRequest request,
			 @RequestParam(value="brandId")String brandId,
			 @RequestParam(value="userId")String userId,
			 @RequestParam(value="startDate",required=false,defaultValue="0")String startDate,
			 @RequestParam(value="endDate",required=false,defaultValue="0")String endDate,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "10", required = false) int size
			){
		String key = "rebateRankingList:brandId:" + brandId + ";userId:"+ userId +";size:" + size + ";startDate:" + startDate + ";endDate:" +endDate; 
		ValueOperations<String,Object> operations = redisTemplate.opsForValue();
		boolean hasKey = false;
		hasKey = redisTemplate.hasKey(key);
		if(hasKey){
			return operations.get(key);
		}
		
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		Date startTime = null;
		Date endTime = null;
		if("0".equals(startDate)){
			startDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
			startTime = calendar.getTime();
		}else{
			startTime = DateUtil.getDateStringConvert(new Date(), startDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
		}
		
		if("0".equals(endDate)){
			endDate = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd");
			endTime = new Date();
		}else{
			endTime = DateUtil.getDateStringConvert(new Date(), endDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
		}
		
		List<Object[]> models = userRebateHistoryBusiness.findSumByCreateDate(startDate,endDate,page*size,(page+1)*size,brandId);
		List<Object[]> models2 = userRebateHistoryBusiness.findSumByCreateDate(startDate,endDate,0,100,brandId);
		int count = page*size;
		int rankingCount = 0;
		String names = "李王张刘陈杨赵黄周吴徐孙胡朱高林何郭马罗梁宋郑谢韩唐冯于董萧程曹袁邓许傅沈曾彭吕苏卢蒋蔡贾丁魏薛叶阎余潘杜戴夏钟汪田任姜范方石姚谭廖邹熊金陆郝孔白崔康毛邱秦江史顾侯邵孟龙万段漕钱汤尹黎易常武乔贺赖龚文庞樊兰殷施陶洪翟安颜倪严牛温芦季俞章鲁葛伍韦申尤毕聂丛焦向柳邢路岳齐沿梅莫庄辛管祝左涂谷祁时舒耿牟卜路詹关苗凌费纪靳盛欧甄项曲成游阳裴席卫查屈鲍位覃霍翁隋植甘景薄单包司柏宁柯阮桂闵欧阳解强柴华车冉房边";
		String[] phones = {"139","138","137","136","135","134","147","150","151","152","157","158","159","178","182","183","184","187","188","130","131","132","155","156","185","","186","145","176","133","153","177","173","180","181","189","170","171"};
		BigDecimal beginAmount = BigDecimal.ZERO;
		if(models != null && models.size() >0){
			for(Object[] objs : models){
				rankingCount += 1;
				count += 1;
				Map<String,Object> userMap = new HashMap<>();
				User user = userLoginRegisterBusiness.queryUserById((int)objs[1]);
				String name = user.getFullname();
				if(name == null || "".equals(name)){
					Random random = new Random();
					name = names.toCharArray()[random.nextInt(names.length())]+"*";
				}else{
					name = name.substring(0,1) + "*" + (name.length()>=3?name.substring(2):"");
				}
				userMap.put("name", name);
				userMap.put("ranking", count);
				userMap.put("phone", user.getPhone().substring(0,3) + "****" + user.getPhone().substring(7, user.getPhone().length()));
				userMap.put("rebate", objs[0]+"");
				list.add(userMap);
				
				if(rankingCount == models.size() && rankingCount < size){
					beginAmount = ((BigDecimal) objs[0]).multiply(BigDecimal.valueOf(100));
					BigDecimal randomAmount = BigDecimal.ZERO;
					for(int i = 0;i <= size-rankingCount-1;i++){
						Random random = new Random();
						if(BigDecimal.ONE.compareTo(beginAmount) >= 0){
							beginAmount = BigDecimal.ONE;
						}else{
							randomAmount = BigDecimal.valueOf(random.nextInt(beginAmount.intValue()));
							beginAmount = beginAmount.subtract(randomAmount);
						}
						
						name = names.toCharArray()[random.nextInt(names.length())]+"*";
						userMap = new HashMap<>();
						userMap.put("name", name);
						userMap.put("ranking", i + rankingCount + 1);
						String phone = phones[random.nextInt(phones.length)];
						userMap.put("phone", phone + "****" + random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9));
						userMap.put("rebate", beginAmount.divide(BigDecimal.valueOf(100), 2,BigDecimal.ROUND_HALF_UP)+"");
						list.add(userMap);
					}
				}
			}
		}else{
			Random random = new Random();
			beginAmount = BigDecimal.valueOf(random.nextInt(100000));
			BigDecimal randomAmount = BigDecimal.ZERO;
			Map<String,Object> userMap = new HashMap<>();
			String name = "";
			for(int i = 0;i < size ; i++){
				if(BigDecimal.ONE.compareTo(beginAmount) >= 0){
					beginAmount = BigDecimal.ONE;
				}else{
					randomAmount = BigDecimal.valueOf(random.nextInt(beginAmount.intValue()));
					beginAmount = beginAmount.subtract(randomAmount);
				}
				
				name = names.toCharArray()[random.nextInt(names.length())]+"*";
				userMap = new HashMap<>();
				userMap.put("name", name);
				userMap.put("ranking",1 + i + rankingCount);
				String phone = phones[random.nextInt(phones.length)];
				userMap.put("phone", phone + "****" + random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9));
				userMap.put("rebate",  beginAmount.divide(BigDecimal.valueOf(100), 2,BigDecimal.ROUND_HALF_UP)+"");
				list.add(userMap);
			}
		}
		Map<String,Object> userMap = null;
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		BigDecimal rebateSum = userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(Long.valueOf(userId),new String[]{"0","1"}, startTime, endTime);
		Random random = new Random();
		String name = user.getFullname();
		if(name == null || "".equals(name)){
			name = names.toCharArray()[random.nextInt(names.length())]+"*";
		}else{
			name = name.substring(0,1) + "*" + (name.length()>=3?name.substring(2):"");
		}
		for(int i = 0; i < models2.size(); i++){
			Object[] objs = models2.get(i);
			if(userId.equals(objs[1]+"")){
				userMap = new HashMap<>();
				userMap.put("name", name);
				userMap.put("ranking",1 + i);
				userMap.put("phone", user.getPhone().substring(0,3) + "****" + user.getPhone().substring(7, user.getPhone().length()));
				userMap.put("rebate", objs[0]+"");
			}
		}
		
		if(userMap == null){
			userMap = new HashMap<>();
			userMap.put("name", name);
			userMap.put("ranking",101);
			userMap.put("phone", user.getPhone().substring(0,3) + "****" + user.getPhone().substring(7, user.getPhone().length()));
			if(rebateSum == null){
				rebateSum = BigDecimal.ZERO;
			}
			userMap.put("rebate", rebateSum+"");
		}
		list.add(userMap);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT,list);
		operations.set(key, map, 1, TimeUnit.MINUTES);
		return map;
	}




}
