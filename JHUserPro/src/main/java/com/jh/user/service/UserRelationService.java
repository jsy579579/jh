package com.jh.user.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.jh.common.tools.http.HttpClient;
import cn.jh.common.utils.DateUtil;
import com.jh.user.business.*;
import com.jh.user.pojo.*;
import com.jh.user.util.ExPortExcelUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.juli.logging.Log;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class UserRelationService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private Util util;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private UserRealtionBusiness userRealtionBusiness;

    @Autowired
    private UserRelationBusiness userRelationBusiness;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserManageHistoryBusiness userManageHistoryBusiness;

    @Autowired
    private UserRebateHistoryBusiness userRebateHistoryBusiness;
    @Autowired
    private UserBalanceService userBalanceService;

    @Autowired
    private UserBalanceBusiness userBalanceBusiness;

    @Autowired
    private UserManageConfigBussiness userManageConfigBussiness;


    @Value("${schedule-task.on-off}")
    private String scheduleOnOff;


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/test")
    public @ResponseBody
    Object addUserRealtion() {
        userRealtionBusiness.addRealtion();
        return "OK";
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/query/preuser")
    public @ResponseBody
    Object getUsersPreUserGradeAndRate(HttpServletRequest request,
                                       @RequestParam(value = "userId") String userId,
                                       @RequestParam(value = "channelId") String channelId
    ) {
        Map<String, Object> map = new HashMap<>();
        User firstUser = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));
        List<UserRealtion> userRealtions = userRealtionBusiness.findByFirstUserId(Long.valueOf(userId));
        if (userRealtions.size() != 0) {
            for (UserRealtion userRealtion : userRealtions) {
                long preUserId = userRealtion.getPreUserId();
                Long firstUserId = userRealtion.getFirstUserId();
                @SuppressWarnings("unchecked")
                Map<String, Object> reateMap = (Map<String, Object>) channelService.queryChannelRateByuserid(request, preUserId, Long.valueOf(channelId));
                if (CommonConstants.SUCCESS.equals(reateMap.get(CommonConstants.RESP_CODE))) {
                    User user = userLoginRegisterBusiness.queryUserById(preUserId);
                    ChannelRate channelRate = new ChannelRate();
                    try {
                        BeanUtils.copyProperties(channelRate, reateMap.get(CommonConstants.RESULT));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                    LOG.info("用户费率================:" + channelRate);
                    userRealtion.setRate(channelRate.getRate());
                    userRealtion.setPreUserGrade(Integer.valueOf(user.getGrade()));
                    userRealtion.setFirstUserName(firstUser.getFullname());
                    userRealtion.setPreUserName(user.getFullname());
                    userRealtion.setFirstUserGrade(Integer.parseInt(firstUser.getGrade()));
                }
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            Map<String, List> userRealtionMap = new HashMap<String, List>();
            userRealtionMap.put(CommonConstants.RESULT, userRealtions);
            map.put(CommonConstants.RESULT, userRealtionMap);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功,但无上级数据");
        }
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/add/one/user")
    public @ResponseBody
    Object addOneUserRealtion(HttpServletRequest request,
                              @RequestParam(value = "userId") String userId
    ) {
        Map<String, Object> map = new HashMap<>();
        User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
        if (user != null) {
            userRealtionBusiness.addOneUserRealtion(user);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "添加成功");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "添加失败,无该用户");
        }
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/update/user/parente")
    public @ResponseBody
    Object updateOneUserPerUser(
            @RequestParam(value = "oriPhone") String oriPhone,
            @RequestParam(value = "brandId") String brandId,
            @RequestParam(value = "prePhone") String prePhone
    ) {
        Map<String, Object> map = new HashMap<>();
		/*if(true) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "该功能正在升级！！！！！！！！");
			return map;
		}*/


        oriPhone = oriPhone.trim();
        prePhone = prePhone.trim();
        boolean hasKey = false;
        String key = "/v1.0/user/realtion/update/user/parente:oriPhone=" + oriPhone + ";prePhone=" + prePhone + ";brandId=" + brandId;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "正在转移上下级关系,请等待!");
            return map;
        }
        User oriUser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(oriPhone, Long.valueOf(brandId));
        User preUser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(prePhone, Long.valueOf(brandId));

        if (oriPhone.equals(prePhone)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机号不能相等!");
            return map;
        }

        if (oriUser == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询不到要更改用户");
            return map;
        } else if (preUser == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询不到上级用户");
            return map;
        }
        System.out.println(oriUser);
        if (oriUser.getPreUserPhone().equals(prePhone)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "已经是该用户下级,无需再修改!");
            return map;
        }
        operations.set(key, key, 600, TimeUnit.SECONDS);
        List<UserRealtion> oriUserRealtions = userRealtionBusiness.findByPreUserId(oriUser.getId());
        List<Long> sonUserIds = new ArrayList<>();
        if (oriUserRealtions != null && oriUserRealtions.size() > 0) {
            for (UserRealtion userRealtion : oriUserRealtions) {
                if (preUser.getId() == userRealtion.getFirstUserId()) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "该上级用户是要更改用户的下级,无法进行调整!");
                    return map;
                }
                sonUserIds.add(userRealtion.getFirstUserId());
            }
        }
        oriUser.setPreUserId(preUser.getId());
        oriUser.setPreUserPhone(preUser.getPhone());
        userRealtionBusiness.deleteAndRebuildUserRealtion(oriUser, sonUserIds);
        redisTemplate.delete(key);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改成功");
        map.put(CommonConstants.RESULT, oriUser);
        return map;
    }

    /**
     * 将一个新用户转换成贴牌商,并删除上下级关系
     *
     * @param phone
     * @param brandId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/update/to/branduser")
    public @ResponseBody
    Object updateUserToBrandUser(
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "brandId") String brandId
    ) {
        Map<String, Object> map = new HashMap<>();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, Long.valueOf(brandId));
        if (user != null) {
            try {
                userRealtionBusiness.updateUserToBrandUser(user);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "修改失败");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查不到该用户数据!");
            return map;
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "设置贴牌用户成功!");
        return map;
    }

    /**
     * 将用户变为贴牌商,删除所有上级关系,并更新下级的上级关系
     *
     * @param phone
     * @param brandId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/update/to/branduser2")
    public @ResponseBody
    Object updateUserToBrandUser2(
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "brandId") String brandId
    ) {
        Map<String, Object> map = new HashMap<>();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, Long.valueOf(brandId));
        if (user != null) {
            try {
                userRealtionBusiness.updateUserToBrandUser2(user);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "修改失败");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查不到该用户数据!");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "设置贴牌用户成功!");
        return map;
    }

    /**
     * 查询指定user的所有下级
     *
     * @param userId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/find/user/all/sons")
    public @ResponseBody
    Object findSonUserByUserId(
            @RequestParam(value = "userId") String userId
    ) {
        Map<String, Object> map = new HashMap<>();
        List<UserRealtion> sonUserRealtions = userRealtionBusiness.findByPreUserId(Long.valueOf(userId));
        if (sonUserRealtions != null && sonUserRealtions.size() > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, sonUserRealtions);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功,但无下级用户");
        }
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/find/user/all/parent")
    public @ResponseBody
    Object findParentUserByUserId(
            @RequestParam(value = "userId") String userId
    ) {
        Map<String, Object> map = new HashMap<>();
        List<UserRealtion> parentUserRealtions = userRealtionBusiness.findByFirstUserId(Long.valueOf(userId));
        if (parentUserRealtions != null && parentUserRealtions.size() > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, parentUserRealtions);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功,但无下级用户");
        }
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/find/user/member/byphone")
    public @ResponseBody
    Object queryUserRelationByUserId(HttpServletRequest request,
                                     @RequestParam(value = "phone") String phone,
                                     @RequestParam(value = "start_time", required = false) String startTime,
                                     @RequestParam(value = "end_time", required = false) String endTime,
                                     @RequestParam(value = "level", required = false, defaultValue = "1") String level
    ) {

        Map<String, Object> map = new HashMap<String, Object>();

//		Date StartTimeDate = null;
//		try {
//			if (startTime != null && !startTime.trim().equalsIgnoreCase("")) {
//				StartTimeDate = DateUtil.getDateFromStr(startTime);
//			}
//		} catch (Exception e1) {
//			LOG.error("startTime转换异常===========================" + e1);
//			startTime = null;
//		}
//		Date endTimeDate = null;
//
//		try {
//			if (endTime != null && !endTime.trim().equalsIgnoreCase("")) {
//				endTimeDate = DateUtil.getDateFromStr(endTime);
//			}
//		} catch (Exception e) {
//			LOG.error("endTime转换异常============================" + e);
//			endTime = null;
//		}

        List<Long> findUserAgentChangeByTimeAndPhone = null;
        List<Long> findUserAgentChangeByTimeAndPhoneAndLevel = null;
        try {
            findUserAgentChangeByTimeAndPhone = userRelationBusiness.findUserAgentChangeByTimeAndPhone(startTime, endTime, phone);

            findUserAgentChangeByTimeAndPhoneAndLevel = userRelationBusiness.findUserAgentChangeByTimeAndPhoneAndLevel(startTime, endTime, phone, level);
        } catch (Exception e) {
            LOG.error("查询直推和间推会员失败======");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询失败");
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put("list1", findUserAgentChangeByTimeAndPhone);
        map.put("list2", findUserAgentChangeByTimeAndPhoneAndLevel);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");

        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/store/get/url")
    public @ResponseBody
    Object getStoreUrlByUserId(HttpServletRequest request,
                               String userId
    ) {
        User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
        if (user == null) {
            return ResultWrap.init(CommonConstants.FALIED, "用户不存在");
        }
        String url = "";
        String origcode = user.getOrigcode();
        if (origcode != null && !"".equals(origcode) && !"null".equalsIgnoreCase(origcode)) {
            url = origcode;
        } else {
            List<UserRealtion> userRealtions = userRealtionBusiness.findByFirstUserId(user.getId());
            for (UserRealtion userRealtion : userRealtions) {
                User user2 = userLoginRegisterBusiness.queryUserById(userRealtion.getPreUserId());
                origcode = user2.getOrigcode();
                if (origcode != null && !"".equals(origcode) && !"null".equalsIgnoreCase(origcode)) {
                    url = origcode;
                    break;
                }
            }
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", url);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/store/update/url")
    public @ResponseBody
    Object updateStoreUrlByUserId(HttpServletRequest request,
                                  String userId,
                                  String storeUrl
    ) {
        User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
        if (user == null) {
            return ResultWrap.init(CommonConstants.FALIED, "用户不存在");
        }
        user.setOrigcode(storeUrl);
        user = userLoginRegisterBusiness.saveUser(user);
        return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", user);
    }


    /**
     * 查询指定user的所有下级的userId
     *
     * @param userId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/realtion/find/user/all/sonsuserid")
    public @ResponseBody
    Object findSonUserIdByUserId(
            @RequestParam(value = "userId") long userId
    ) {

        Long[] firstUserIdByPreUserId = userRealtionBusiness.getFirstUserIdByPreUserId(userId);
        LOG.info("firstUserIdByPreUserId的长度======" + firstUserIdByPreUserId.length);

        List<Long> asList = Arrays.asList(firstUserIdByPreUserId);
        List<Long> list = new ArrayList<>(asList);
        list.add(userId);

        LOG.info("加了userId的firstUserIdByPreUserId的长度======" + list.size());

        if (firstUserIdByPreUserId != null && firstUserIdByPreUserId.length > 0) {

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", list);
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "查询成功,但无下级用户");
        }
    }

    /*
     *@description:查询出符合管理奖的人员信息。
     *@author: ives
     *@annotation:"没办法啊,必须写跨服调用才能配合管理奖的接口，我也不想写啊！"
     *@data:2019年9月5日 09:30:32
     */

    //	@RequestMapping(value = "/v1.0/user/realtion/awardPreuserId",method = RequestMethod.POST)
//	@ResponseBody

    @Scheduled(cron = "0 0 0 * * ?")
    public void awardPreuserId() throws ParseException {
        if ("true".equals(scheduleOnOff)) {
            LOG.info("================每日发放管理奖开始进行扫描");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime());
            LOG.info("======================获取时间" + yesterday + "==============的入账记录");
            List<UserManageConfig> userManageConfigList = userManageConfigBussiness.findAllStatus();

            //获取昨天所有的记录
            for (UserManageConfig uc : userManageConfigList) {
                Long brandId = uc.getBrandId();
                BigDecimal rate = uc.getRate();
                List<UserRebateHistory> userRebateHistories = userRebateHistoryBusiness.findUserRebateHistoryByDate(yesterday, brandId);
                LOG.info("============================当日贴牌ID为" + brandId + "有" + userRebateHistories.size() + "笔记录=====================");
                int count = 0;
                for (UserRebateHistory userRebate : userRebateHistories) {
                    String orderCode = userRebate.getOrderCode();
                    BigDecimal amount = userRebate.getRebate().multiply(rate).setScale(4, BigDecimal.ROUND_DOWN);
                    Date time = userRebate.getCreateTime();
                    int level1 = 4;
                    Long firstUserId = userRebate.getUserId();
                    int preUserGrad = 2;
                    //查询出用户四代上级以内，所有等级大于秀才的所有上级的id
                    List<UserRealtion> users = userRealtionBusiness.findByFirstUserIdAndPreUserGrade(firstUserId, level1, preUserGrad);
                    //判断这些对象是否推荐了三个秀才，秀才等级是2

                    for (UserRealtion ur : users) {
                        UserManageHistory userManageHistory = new UserManageHistory();
                        Long preUserId = ur.getPreUserId();
                        int firstUserIdGarde = 2;
                        int level = 1;
                        List<UserRealtion> verification = userRealtionBusiness.findByPreUserIdAndfirstUserGrade(preUserId, firstUserIdGarde, level);
                        //需要判断如果是等级2的话，是否每一条线都超过100 否则不发

                        //如果这个上级推荐了三个或者以上的直推下级
                        if (verification.size() >= 3) {
                            userManageHistory.setOrderCode(orderCode);
                            userManageHistory.setAmount(amount);
                            userManageHistory.setFirstUserId(ur.getFirstUserId());
                            userManageHistory.setFirstUserPhone(ur.getFirstUserPhone());
                            userManageHistory.setFirstUserGrade(ur.getFirstUserGrade());
                            userManageHistory.setPreUserId(preUserId);
                            userManageHistory.setPreUserPhone(ur.getPreUserPhone());
                            userManageHistory.setPreUserGrade(ur.getPreUserGrade());
                            userManageHistory.setLevel(ur.getLevel());
                            userManageHistory.setCreateTime(time);
                            userManageHistory.setStatus("0");
                            try {
                                if (userBalanceService.updateUserManageByuserId(userManageHistory)) {
                                    userManageHistory.setStatus("1");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                LOG.info("=================增加用户管理奖余额时出错，订单号==" + orderCode);
                            }
                            try {
                                UserManageHistory userManageHistory2 = userManageHistoryBusiness.save(userManageHistory);
                                LOG.info("第" + count + "条=================添加管理奖记录成功" + userManageHistory2.toString());
                                count++;

                            } catch (Exception e) {
                                e.printStackTrace();
                                LOG.info("================添加管理奖记录的时候发生了错误=" + userManageHistory.toString());
                            }
                            if (count % 50 == 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {
                            continue;
                        }
                    }

                }
                LOG.info(brandId + "添加管理奖结束，共" + count + "条数据");
            }
        }
    }





    /*
     *@description:查询管理奖的信息。
     *@author: ives
     *@annotation:"哎呀，写完了管理奖还要写管理奖对应的查询接口！我好烦啊！！！"
     *@data:2019年9月10日 09:30:32
     */

    @RequestMapping(value = "/v1.0/user/realtion/awardPreuserId/brandid", method = RequestMethod.POST)
    @ResponseBody
    public Object awardPreuserIdbrandid(@RequestParam(value = "brandId") String brandId,
                                        @RequestParam(value = "firPhone", required = false) String firPhone,
                                        @RequestParam(value = " prePhone", required = false) String prePhone,
                                        @RequestParam(value = "strTime", required = false) String strTime,
                                        @RequestParam(value = "endTime", required = false) String endTime,
                                        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        Map<String, Object> maps = new HashMap<>();
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        Long brandId1 = Long.valueOf(brandId);
        if (!"".equals(strTime) && null != strTime) {
            if (!"".equals(endTime) && null != endTime) {
                return awardPreuserIdbrandidAndTime(brandId1, firPhone, prePhone, strTime, endTime, page, size);
            }
        }
        if (null == firPhone) {
            firPhone = "";
        }
        if (null == prePhone) {
            prePhone = "";
        }
        List<UserManageHistory> userManageHistoryList = null;
        if ("".equals(firPhone) && "".equals(prePhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandId(brandId1, pageable);
        }
        if ("".equals(firPhone) && !"".equals(prePhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndPrePhone(brandId1, prePhone, pageable);
        }
        if ("".equals(prePhone) && !"".equals(firPhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndFirPhone(brandId1, firPhone, pageable);
        }
        if (!"".equals(prePhone) && !"".equals(firPhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndFirPhoneAndPrePhone(brandId1, firPhone, prePhone, pageable);
        }
        if (userManageHistoryList != null) {
            for (UserManageHistory u : userManageHistoryList) {
                u.setPreUserName(userLoginRegisterBusiness.queryUserByPhone(u.getPreUserPhone()).getFullname());
                u.setFirUserName(userLoginRegisterBusiness.queryUserByPhone(u.getFirstUserPhone()).getFullname());
            }
            maps.put("resp_message", "查询成功");
            maps.put("resp_code", "000000");
            maps.put("result", userManageHistoryList);
            return maps;
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "查询失败");
        }
    }


    /*
     *@description:查询管理奖的信息。（带时间）
     *@author: ives
     *@annotation:"这个接口其实跟上面那个差不多,我只是懒得重新写逻辑了！！！"
     *@data:2019年9月10日 09:30:32
     */
    public Object awardPreuserIdbrandidAndTime(Long brandId, String firPhone, String prePhone, String strTime, String endTime, int page, int size) {
        Map<String, Object> maps = new HashMap<>();
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        Date strdate = DateUtil.getDateFromStr(strTime);
        Date enddate = DateUtil.getDateFromStr(endTime);
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        if (null == firPhone) {
            firPhone = "";
    }
        if (null == prePhone) {
        prePhone = "";
    }
        List<UserManageHistory> userManageHistoryList = null;
        if ("".equals(firPhone) && "".equals(prePhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndTime(brandId, strdate, enddate, pageable);
        }
        if ("".equals(firPhone) && !"".equals(prePhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndPrePhoneAndTime(brandId, strdate, enddate, prePhone, pageable);
        }
        if ("".equals(prePhone) && !"".equals(firPhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndFirPhoneAndTime(brandId, strdate, enddate, firPhone, pageable);
        }
        if (!"".equals(prePhone) && !"".equals(firPhone)) {
            userManageHistoryList = userManageHistoryBusiness.findBybrandIdAndFirPhoneAndPrePhoneAndTime(brandId, strdate, enddate, firPhone, prePhone, pageable);
        }
        if (userManageHistoryList != null) {
            for (UserManageHistory u : userManageHistoryList) {
                u.setPreUserName(userLoginRegisterBusiness.queryUserByPhone(u.getPreUserPhone()).getFullname());
                u.setFirUserName(userLoginRegisterBusiness.queryUserByPhone(u.getFirstUserPhone()).getFullname());
            }
            maps.put("resp_message", "查询成功");
            maps.put("resp_code", "000000");
            maps.put("result", userManageHistoryList);
            return maps;
        } else {
            maps.put("resp_message", "查询失败");
            maps.put("resp_code", "999999");
            maps.put("result", "");
            return maps;
        }
    }

    /*
     *@description:管理奖每月的定时器清空用户每个月的管理累计金额,并且把用户信息导出到服务器上
     *@author: ives
     *@annotation:"说实在的,我并不想写这个接口，但是为了人家贴牌商方便,唉"
     *@data:2019年9月10日  15:60:32
     * UserManageTb
     */
    @RequestMapping(value = "/v1.0/user/realtion/awardPreuserId/manage/excelout")
    public void cleanUserManage(HttpServletRequest request, HttpServletResponse response) {

        String export = "用户名#userName,用户ID#userId,用户手机号#userPhone,当月管理金#amount,贴牌ID#brandId,贴牌名称#brandName";
        String[] excelHeader = export.split(",");
        List<UserManageTb> projectList = new ArrayList<UserManageTb>();
        BigDecimal bigDecimal = BigDecimal.ZERO;
        List<UserAccount> userAccounts = userBalanceBusiness.findByManage(bigDecimal);
        LOG.info("========================每月管理金个数" + userAccounts.size() + "===");
        UserManageTb userManageTb = null;
        for (UserAccount c : userAccounts) {
            try {
                userManageTb = new UserManageTb();
                User user = userLoginRegisterBusiness.queryUserById(c.getUserId());
                userManageTb.setBrandId(Long.toString(user.getBrandId()));
                userManageTb.setBrandName(user.getBrandname());
                userManageTb.setUserId(Long.toString(c.getUserId()));
                userManageTb.setUserName(user.getFullname());
                userManageTb.setAmount(c.getManage());
                userManageTb.setUserPhone(user.getPhone());
            } catch (Exception e) {
                e.printStackTrace();
            }
            projectList.add(userManageTb);
        }
        try {
            ExPortExcelUtil.export(response, "用户表", excelHeader, projectList);
            LOG.info("========================每月管理金导出成功");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("========================每月管理金导出失败");
        }
    }


    /*
     *@description:管理奖的清空接口（每月）
     *@author: ives
     *@annotation:""
     *@data:2019年9月10日  18:00:32
     * UserManageTb
     */
    @RequestMapping(value = "/v1.0/user/realtion/awardPreuserId/manage/clearmouth")
    @ResponseBody
    public Object clearmouth() {

        return null;
    }



    /*
     *@description:管理奖的后台控制开关
     *@author: ives
     *@annotation:"唉，送佛送到西嘛，还要给贴牌商写这个控制开关的接口"
     *@data:2019年9月16日  18:00:32
     * UserManageTb
     */
    @RequestMapping(value = "/v1.0/user/realtion/awardPreuserId/manage/updateUserManageConfig")
    @ResponseBody
    public Object updateUserManageConfig(@RequestParam(value="brandId")String brandId,
                                         @RequestParam(value="status")int status,
                                         @RequestParam(value="rate",required = false,defaultValue = "0.02")String rate
    ) {
        Map<String,Object> map=new HashMap<>();
        if("".equals(rate)||rate==null){
            rate="0.02";
        }

        UserManageConfig userManageConfig=userManageConfigBussiness.findByBrandId(Long.valueOf(brandId));
        if(null==userManageConfig){
            UserManageConfig userManageConfig1=new UserManageConfig();
            userManageConfig1.setBrandId(Long.valueOf(brandId));
            userManageConfig1.setRate(new BigDecimal(rate));
            userManageConfig1.setStatus(status);
            UserManageConfig u=userManageConfigBussiness.save(userManageConfig1);
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, u);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }else{
            if(userManageConfig.getStatus()==status&&userManageConfig.getRate().compareTo(new BigDecimal(rate))==0){
             return ResultWrap.init(CommonConstants.FALIED,"您的管理奖已经是当前配置，无需更改");
            }
            userManageConfig.setRate(new BigDecimal(rate));
            userManageConfig.setStatus(status);
            UserManageConfig u=userManageConfigBussiness.save(userManageConfig);
            List<UserManageConfig> list=new ArrayList<>();
            list.add(u);
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, list);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }
    }

    /*
     *@description:管理奖的后台查询开关
     *@author: ives
     *@annotation:"写了控制开关当然得有一个查询开关啦！"
     *@data:2019年9月16日  18:00:32
     * UserManageTb
     */
    @RequestMapping(value = "/v1.0/user/realtion/awardPreuserId/manage/selectUserManageConfig")
    @ResponseBody
    public Object selectUserManageConfig(@RequestParam(value="brandId")String brandId
    ) {
        Map<String,Object> map=new HashMap<>();
        List<UserManageConfig> list=new ArrayList<>();
        list.add(userManageConfigBussiness.findByBrandId(Long.valueOf(brandId)));
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT,list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }


    @RequestMapping(value = "/v1.0/user/realtion/level/query/firstUserId/and/preuserid")
    @ResponseBody
    public Object queryLevelByFirstUserIdAndPreUserId(Long firstUserId,Long preUserId){
        Map<String,Object> map=new HashMap<>();
        UserRealtion userRealtion= userRealtionBusiness.queryUserRelationByFirstUserIdAndPreUserId(firstUserId,preUserId);
        if (userRealtion == null){
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"查询失败");
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"查询成功");
        map.put(CommonConstants.RESULT,userRealtion);
        return map;
    }
}
