package com.jh.user.service;

import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.DateUtil;
import com.jh.user.business.*;
import com.jh.user.pojo.*;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;


@Controller
@EnableAutoConfiguration
public class UserCoinService {

    private static final Logger LOG = LoggerFactory.getLogger(UserCoinService.class);


    @Autowired
    private UserCoinBusiness userCoinBusiness;


    @Autowired
    private UserBalanceBusiness userBalBusiness;

    @Autowired
    private BrandCoinConfigBusiness brandCoinConfigBusiness;

    @Autowired
    private UserRealtionBusiness userRealtionBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;


    @Autowired
    private UserCoinHistoryNewBusiness userCoinHistoryNewBusiness;



    /**
     * 获取用户的积分流水
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/{token}")
    public @ResponseBody
    Object pageCoinQuery(HttpServletRequest request,
                         @PathVariable("token") String token,
                         @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                         @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                         @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                         @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
    ) {

        Map map = new HashMap();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.queryUserCoinHistoryByUserid(userId, pageable));
        return map;

    }


    /**
     * 更新用户的积分
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/update/userid")
    public @ResponseBody
    Object updateUserCoinByUserid(HttpServletRequest request,
                                  @RequestParam(value = "user_id") long userId,
                                  @RequestParam(value = "coin", defaultValue = "0", required = false) int coin,
                                  @RequestParam(value = "order_code") String orderCode,
                                  @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub
    ) {
        Map map = new HashMap();
        UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(userId);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.updateUserCoin(userAccount, coin, addorsub, orderCode));
        return map;

    }

    /**
     * 更新用户的积分（新） ives
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/update/useridnew")
    @ResponseBody
    public Object updateUserCoinByUseridNew(@RequestParam(value = "user_id") long userId,
                                            @RequestParam(value = "coin", defaultValue = "0", required = false) String coin,
                                            @RequestParam(value = "order_code") String orderCode,
                                            @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub) {
        Map map = new HashMap();
        BigDecimal coin1 = new BigDecimal(coin);
        UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(userId);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.updateUserCoinNew(userAccount, coin1, addorsub, orderCode));
        return map;
    }


    /**
     * 更新用户的账户信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/update/{token}")
    public @ResponseBody
    Object updateUserCoin(HttpServletRequest request,
                          @PathVariable("token") String token,
                          @RequestParam(value = "coin", defaultValue = "0", required = false) int coin,
                          @RequestParam(value = "order_code") String ordercode,
                          @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub
    ) {

        Map map = new HashMap();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(userId);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.updateUserCoin(userAccount, coin, addorsub, ordercode));
        return map;

    }

    /**
     * 积分排行
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/ranking/query")
    public @ResponseBody
    Object queryUserCoin(HttpServletRequest request,
                         @RequestParam(value = "brand_id") int brandid
    ) {

        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.queryRankingListCoin(brandid));
        return map;
    }

    /**
     * 收益排行
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/earnings/ranking/query")
    public @ResponseBody
    Object queryUserEarnings(HttpServletRequest request,
                             @RequestParam(value = "brand_id") int brandid
    ) {
        Map map = new HashMap();
        List<RankingList> rankingList = userCoinBusiness.queryRankingListEarnings(brandid);
        for (int i = 3; i < rankingList.size(); i++) {
            Date date = rankingList.get(i).getUpdatetime();
            long id = rankingList.get(i).getId();
            int coin = rankingList.get(i).getCion().intValue();
            int day1 = date.getDay();
            Date dat = new Date();
            int day2 = dat.getDay();
            /*每天同一个brandid只修改一次金额*/
            if (day1 != day2) {
                Random ran = new Random();
                int num = ran.nextInt(10000);
                rankingList.get(i).setCion(new BigDecimal(num + coin));
                rankingList.get(i).setId(id);
                rankingList.get(i).setUpdatetime(dat);
                userCoinBusiness.addRankingList(rankingList.get(i));
            }

        }
        List<RankingList> rankingList2 = userCoinBusiness.queryRankingListEarnings(brandid);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, rankingList2);
        return map;
    }

    /****添加排名信息***/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/ranking/add")
    public @ResponseBody
    Object addUserCoin(HttpServletRequest request,
                       /**修改ID***/
                       @RequestParam(value = "id", defaultValue = "0", required = false) long id,

                       @RequestParam(value = "brand_id") long brandid,
                       /*** 用户姓名* ***/
                       @RequestParam(value = "username") String username,
                       /**积分**/
                       @RequestParam(value = "cion") BigDecimal cion,
                       /**收益**/
                       @RequestParam(value = "earnings") BigDecimal earnings

    ) {

        Map map = new HashMap();

        RankingList rankingList = userCoinBusiness.queryRankingListByid(id);
        if (rankingList == null) {
            rankingList = new RankingList();
            rankingList.setCreateTime(new Date());
        }
        rankingList.setBrandId(brandid);
        rankingList.setCion(cion);
        rankingList.setUsername(username);
        rankingList.setEarnings(earnings);
        rankingList.setUpdatetime(new Date());
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userCoinBusiness.addRankingList(rankingList));
        return map;
    }


    /**
     * 获取贴牌的积分发放配置
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/config/query")
    @ResponseBody
    public Object queryCoinConfig(@RequestParam(value = "brandId") long brandId,
                                  @RequestParam(value = "grade") String grade,
                                  @RequestParam(value = "status") String status) {
        Map map = new HashMap();
        int grade1 = Integer.parseInt(grade);
        int status1 = Integer.parseInt(status);

        BrandCoinConfig brandCoinConfig = brandCoinConfigBusiness.findByBrandIdAndGradeAndStatus(brandId, grade1, status1);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, brandCoinConfig);
        return map;
    }


    /**
     * 给用户的直推上级添加积分
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/update/preuserid/new")
    @ResponseBody
    public Object grantPreUserCoin(@RequestParam(value = "user_id") long userId,
                                   @RequestParam(value = "order_code") String orderCode,
                                   @RequestParam(value = "coin", defaultValue = "0", required = false) String coin) {

        Map map = new HashMap();
        BigDecimal coins = new BigDecimal(coin);
        UserRealtion userRealtion = userRealtionBusiness.findByFirstUserIdAndLevel(userId, 1);
        long preuserId = userRealtion.getPreUserId();
        return updateUserCoinByUseridNew(preuserId, coin, orderCode, "0");
    }


    /**
     * 判断贴牌设置的购买产品是否发放积分
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcoinconfig/select")
    @ResponseBody
    public Object brandCoinConfigSelect(@RequestParam(value = "user_id") long userId) {

        Map map = new HashMap();
        User user = userLoginRegisterBusiness.queryUserById(userId);
        Long brandId = user.getBrandId();
        int grade = Integer.parseInt(user.getGrade());
        BrandCoinGradeConfig brandCoinGradeConfig = brandCoinConfigBusiness.findBrandGradeByGradeAndBrandId(brandId, grade);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, brandCoinGradeConfig);
        return map;
    }


    /*
     *@description:发放积分的后台开关查询
     *@author: ives
     *@annotation:"这个是后台控制发放积分的费率和等级的
     *@data:2019年9月19日  18:00:32
     * UserManageTb
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcorinconfig/select")
    @ResponseBody
    public Object brandCoinConfigquery(@RequestParam(value = "brandId") String brandId) {
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        Map map = new HashMap();
        Long brandid = Long.valueOf(brandId);
        List<BrandCoinConfig> brandCoinConfig = userCoinBusiness.findByBrand(brandid);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, brandCoinConfig);
        return map;
    }

    /*
     *@description:发放积分的后台开关查询
     *@author: ives
     *@annotation:"这个是后台控制发放积分的费率和等级的
     *@data:2019年9月19日  18:00:32
     * UserManageTb
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcorinconfig/update")
    @ResponseBody
    public Object brandCoinConifgupdate(@RequestParam(value = "brandId") String brandId,
                                        @RequestParam(value = "grade") String grade,
                                        @RequestParam(value = "ratio") String ratio,
                                        @RequestParam(value = "status") String status) {
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        Map map = new HashMap();
        int status1=Integer.parseInt(status);
        Long brandid=Long.valueOf(brandId);
        int grade1=Integer.parseInt(grade);
        BigDecimal ratio1=new BigDecimal(ratio);
		BrandCoinConfig brandCoinConfig = userCoinBusiness.findByBrandAndGradeAndratio(brandid,grade1);
		BrandCoinConfig brandCoinConfig1=null;
		if(brandCoinConfig==null){
			brandCoinConfig=new BrandCoinConfig();
			brandCoinConfig.setGrade(grade1);
			brandCoinConfig.setRatio(ratio1);
			brandCoinConfig.setStatus(1);
			brandCoinConfig1=userCoinBusiness.saveBrandCoinConfig(brandCoinConfig);
		}else{
		    if(brandCoinConfig.getRatio().compareTo(ratio1)==0&&brandCoinConfig.getStatus()==status1) {
                return ResultWrap.init(CommonConstants.FALIED, "您已经是当前配置，无需更改");
            }
			brandCoinConfig.setStatus(status1);
			brandCoinConfig.setRatio(ratio1);
			brandCoinConfig.setGrade(grade1);
			 brandCoinConfig1=userCoinBusiness.saveBrandCoinConfig(brandCoinConfig);
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brandCoinConfig1);
		return map;
    }

	/*
	 *@description:发放积分的后台开关查询
	 *@author: ives
	 *@annotation:"控制会员是否进行充值返积分
	 *@data:2019年9月19日  18:00:32
	 * UserManageTb
	 *
	 *
	 *
	 *
	 * @description：新的控制用户是否发放积分
	 * @author: ives
	 * @annotation: "我只能说白痴客户临时改需求，然后还不承认，说我没听明白需求，我是真的服了，干他大爷"
	 * @data: 2019年9月24日 13:48:27
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcoringradeconfig/update")
	@ResponseBody
	public Object brandCorinGradeConfigUpdate(@RequestParam(value = "status")String status,
											  @RequestParam(value="brandId")String BrandId,
                                              @RequestParam(value="type",required = false)String type,
                                              @RequestParam(value = "grade")int grade){
        if ("".equals(BrandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
		Long brandId=Long.valueOf(BrandId);
		Map map = new HashMap();
		BrandCoinGradeConfig brandCoinGradeConfig=brandCoinConfigBusiness.findBrandGradeByGradeAndBrandId(brandId,grade+1);
		if(brandCoinGradeConfig==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "网络连接超时！");
			return map;
		}else{
		    brandCoinGradeConfig.setStatus(Integer.parseInt(status));
		    if(!"".equals(type)&&type!=null){
                BigDecimal type1=new BigDecimal(type);
                brandCoinGradeConfig.setType(type1);
            }
			BrandCoinGradeConfig brandCoinGradeConfig1=brandCoinConfigBusiness.saveBrandCoinGradeConfig(brandCoinGradeConfig);
            List<BrandCoinGradeConfig> list=new ArrayList<>();
            list.add(brandCoinGradeConfig1);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;

		}
	}

	/*
	 *@description:发放积分的后台开关查询
	 *@author: ives
	 *@annotation:"查询会员是否进行充值返积分
	 *@data:2019年9月19日  18:00:32
	 * UserManageTb
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcoringradeconfig/select")
	@ResponseBody
	public Object brandCorinGradeConfigQuery(@RequestParam(value = "brandId")String BrandId){
		Map map = new HashMap();
        List<BrandCoinGradeConfig> list=new ArrayList<>();
        if ("".equals(BrandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
		Long brandId=Long.valueOf(BrandId);
		BrandCoinGradeConfig brandCoinGradeConfig=brandCoinConfigBusiness.findBrandGradeByBrandId(brandId);
		if(brandCoinGradeConfig==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "尽请期待！");
			return map;
		}
        list.add(brandCoinGradeConfig);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, list);
		return map;
	}

    /*
     *@description:查询积分历史记录的接口
     *@author: ives
     *@annotation:"唉，说实话我是真的讨厌写这种接口，我也懒得作判定。啥时候测出bug啥时候在优化嘛
     *@data:2019年9月19日  18:00:32
     * UserManageTb
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/coin/query/brandcoringradehistory/select")
    @ResponseBody
    public Object corinHistorySelect(@RequestParam(value = "brandId")String BrandId,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     @RequestParam(value = "strTime", required = false) String strTime,
                                     @RequestParam(value = "endTime", required = false) String endTime,
                                     @RequestParam(value="order_code",required = false)String orderCode,
                                     @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                     @RequestParam(value = "size", defaultValue = "20", required = false) int size){
        Map map=new HashMap();
        Long brandId=Long.valueOf(BrandId);
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        if (!"".equals(strTime) && null != strTime) {
            if (!"".equals(endTime) && null != endTime) {
                return coinUserIdBrandidAndTime(brandId, phone , strTime, endTime, orderCode,page, size);
            }
        }
        if (null == phone) {
            phone = "";
        }
        if(null==orderCode){
            orderCode="";
        }
        List<UserCoinHistoryNew> userCoinHistoryNewsList = null;
        if("".equals(orderCode)) {
            if ("".equals(phone)) {
                List<User> userList = userLoginRegisterBusiness.queryUserByBrandId(brandId);
                Long[] longs = new Long[userList.size()];
                for (int i = 0; i < userList.size(); i++) {
                    longs[i] = userList.get(i).getId();
                }
                userCoinHistoryNewsList = userCoinHistoryNewBusiness.findByUserIdList(longs, pageable);

            } else {
                User user = userLoginRegisterBusiness.queryUserByPhone(phone);
                userCoinHistoryNewsList = userCoinHistoryNewBusiness.findByUserId(user.getId(), pageable);
            }
        }else{
            userCoinHistoryNewsList=userCoinHistoryNewBusiness.findByOrderCode(orderCode,pageable);
        }

        for (UserCoinHistoryNew u : userCoinHistoryNewsList) {
                u.setPhone(userLoginRegisterBusiness.queryUserById(u.getUserId()).getPhone());
                u.setFullname(userLoginRegisterBusiness.queryUserById(u.getUserId()).getFullname());

        }
        map.put("resp_message", "查询成功");
        map.put("resp_code", "000000");
        map.put("result", userCoinHistoryNewsList);
        return map;
    }

    public Object coinUserIdBrandidAndTime(long brandId,String phone,String strTime, String endTime,String orderCode, int page, int size){
        Map<String, Object> map = new HashMap<>();
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        Date strdate = DateUtil.getDateFromStr(strTime);
        Date enddate = DateUtil.getDateFromStr(endTime);
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        if (null == phone) {
            phone = "";
        }
        if(null==orderCode){
            orderCode="";
        }
        List<UserCoinHistoryNew> userCoinHistoryNewsList = null;
        if("".equals(orderCode)) {
            if ("".equals(phone)) {
                List<User> userList = userLoginRegisterBusiness.queryUserByBrandId(brandId);
                Long[] longs = new Long[userList.size()];
                for (int i = 0; i < userList.size(); i++) {
                    longs[i] = userList.get(i).getId();
                }
                userCoinHistoryNewsList = userCoinHistoryNewBusiness.findByUserIdListAndTime(longs,strdate,enddate,pageable);
            } else {
                User user = userLoginRegisterBusiness.queryUserByPhone(phone);
                userCoinHistoryNewsList = userCoinHistoryNewBusiness.findByUserIdAndTime(user.getId(),strdate,enddate, pageable);
            }
        }else{
            userCoinHistoryNewsList=userCoinHistoryNewBusiness.findByOrderCodeAndTime(orderCode,strdate,enddate,pageable);
        }
        for (UserCoinHistoryNew u : userCoinHistoryNewsList) {
            try {
                u.setPhone(userLoginRegisterBusiness.queryUserById(u.getUserId()).getPhone());
                u.setFullname(userLoginRegisterBusiness.queryUserById(u.getUserId()).getFullname());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        map.put("resp_message", "查询成功");
        map.put("resp_code", "000000");
        map.put("result", userCoinHistoryNewsList);
        return map;
    }





}
