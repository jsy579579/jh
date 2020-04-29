package com.jh.user.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.*;
import com.google.gson.JsonObject;
import com.jh.user.business.*;
import com.jh.user.pojo.*;
import com.netflix.discovery.converters.Auto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static java.util.stream.Collectors.toList;

@Api(value = "用户业务逻辑类")
@Controller
@EnableAutoConfiguration
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    @Autowired
    TUpgradeOrderViewBusiness tUpgradeOrderViewBusiness;
    @Autowired
    private UserBalanceBusiness userBalBusiness;
    @Autowired
    private BrandManageBusiness brandMangeBusiness;
    @Autowired
    UserRealtionBusiness userRealtionBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private UserShopsBusiness userShopsBusiness;

    @Autowired
    private UserBankInfoBusiness userBankInfoBusiness;

    @Autowired
    private ThirdLeveDistributionBusiness thirdLevelBusiness;

    @Autowired
    private UserRoleResourceBusiness userRoleResourceBusiness;

    @Autowired
    private ChannelRateBusiness channelRateBusiness;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserRelationBusiness userRelationBusiness;


    @Autowired
    private UserOlderConfigBusiness userOlderConfigBusiness;

    @Autowired
    private UserRebateService userRebateService;


    @Value("${schedule-task.on-off}")
    private String scheduleOnOff;

    @Value("${user.background.uploadpath}")
    private String uploadImg;

    @Value("${user.background.downloadpath}")
    private String downloadImg;

    @Value("${spring.cloud.client.ipAddress}")
    private String serverIpAddress;

    @Autowired
    private GetMoneyBusiness getMoneyBusiness;

    @Autowired
    private UserOlderRebateHistoryBusiness userOlderRebateHistoryBusiness;

    @Autowired
    Util util;

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/find/newuser/num")
    public @ResponseBody
    Object findUsergetnewnum(HttpServletRequest request,
                             @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandId,
                             // 开始时间
                             @RequestParam(value = "start_time", required = false) String startTime,
                             // 结束时间
                             @RequestParam(value = "end_time", required = false) String endTime) {
        Date StartTimeDate = null;
        if (startTime != null && !startTime.equalsIgnoreCase("")) {
            StartTimeDate = DateUtil.getDateFromStr(startTime);
        }
        Date endTimeDate = null;
        if (endTime != null && !endTime.equalsIgnoreCase("")) {
            endTimeDate = DateUtil.getDateFromStr(endTime);
        }

        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT,
                userLoginRegisterBusiness.queryUserNumByBidandtime(brandId, StartTimeDate, endTimeDate));
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return map;
    }

    // 获取shops表中随机的userid
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/randomuserid")
    public Object queryRandomUserid(HttpServletRequest request) {
        Random rand = new Random();
        Map map = new HashMap();
        try {
            String[] listuserid = userShopsBusiness.queryRandomUseridByAll();
            String userid = listuserid[rand.nextInt(listuserid.length - 1)];
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, userid);
            map.put(CommonConstants.RESP_MESSAGE, "获取成功");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "数据为空");
        }
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/by/phone/and/brandid")
    public @ResponseBody
    Object findUserByPhoneAndBrandId(HttpServletRequest request,
                                     @RequestParam(value = "phone") String phone, @RequestParam(value = "brandId") String brandId) {
        Map<String, Object> map = new HashMap<String, Object>();
        User model = null;
        try {
            model = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, Long.valueOf(brandId));
        } catch (Exception e) {
            LOG.error("根据phone和brandId查询用户异常,参数为phone=" + phone + ",brandId=" + brandId + ";");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户失败,请检查输入的参数是否正确");
            return map;
        }
        if (model == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无该用户,请确认该用户已注册!");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
        map.put(CommonConstants.RESULT, model);
        return map;

    }

    // 用户注销接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/delete/deleteUser")
    @ResponseBody
    public Object offUserActive(HttpServletRequest request,
                                @RequestParam(value = "phone", defaultValue = "", required = false) String phone,
                                @RequestParam(value = "brandId", defaultValue = "-1", required = false) long brandid,
                                @RequestParam(value = "id", defaultValue = "-1", required = false) long userid) {
        Map map = new HashMap();

        if (("".equals(phone) || phone == null) && brandid == -1 && userid == -1) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "必须输入至少一种查询条件");
            return map;
        } else if (userid == -1) {
            List<User> user = userLoginRegisterBusiness.queryUsersByPhone(phone);
            if (user.size() == 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该用户不存在");
                return map;
            } else {
                userid = user.get(0).getId();
            }
        } else if ((phone == null || "".equals(phone)) && brandid == -1) {
            LOG.info("============我是输入的userid============" + userid);
        }

        List<User> userLower = userLoginRegisterBusiness.findAfterUsers(userid);

        Brand brand = brandMangeBusiness.findBrandByManageid(userid);

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/payment/queryorderfailed";
        String result = "";
        try {
            requestEntity.add("userid", userid + "");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONArray resultArr = jsonObject.getJSONArray("result");

        UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(userid);
        double userBalance = 0.00;
        if (userAccount == null) {
            userBalance = 0.00;
        } else {
            userBalance = userAccount.getBalance().doubleValue();
            if (userBalance == 0.0) {
                userBalance = 0.00;
            } else {
                userBalance += 9;
            }
        }

        if (userLower.size() == 0 && brand == null && resultArr.size() == 0 && userBalance == 0.00) {

            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/realname/delrealname";
            try {
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("userId", userid + "");
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            try {
                userLoginRegisterBusiness.delUserByUserid(userid);
            } catch (Exception e) {
                LOG.info(e.getMessage());
                LOG.info("result========================还没有删除user表中记录");
            }
            userLoginRegisterBusiness.delUserByUserId(userid);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注销成功");
        } else {
            if (userLower.size() != 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该用户存在下级会员");
            } else if (brand != null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "brand表中存在该用户");
            } else if (resultArr.size() != 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该用户有待完成或已成功订单");
            } else if (userBalance != 0.00) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该用户余额不为0");
            }
        }
        return map;
    }

    // 代理商授权功能接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/auto/oriphone/{token}")
    @ResponseBody
    public Object userAuthManage(HttpServletRequest request, @PathVariable("token") String token,
                                 // 被授权者手机号
                                 @RequestParam(value = "phone") String oriphone,
                                 // 参数权限
                                 @RequestParam(value = "roleId") long roleid,

                                 @RequestParam(value = "lower_level", defaultValue = "0", required = false) int lowerLevel) {

        Map map = new HashMap();

        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        List<User> userOri = userLoginRegisterBusiness.queryUsersByPhone(oriphone);
        int userOriSize = userOri.size();
        if (userOriSize == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户不存在,请仔细核对用户手机号。");
            return map;
        }
        long userOriId = 0;
        long userOriBrandid = 0;
        for (int i = 0; i < userOriSize; i++) {
            userOriId = userOri.get(0).getId();
            userOriBrandid = userOri.get(0).getBrandId();
        }

        Long[] str2 = getUseridArray(userId, lowerLevel);

        if (userOri != null && ArrayUtils.contains(str2, userOriId)) {
            UserRole userroleAuto = userRoleResourceBusiness.UserRolepageByRUid(userId);
            UserRole userroleOri = userRoleResourceBusiness.UserRolepageByRUid(userOriId);
            long roleIdAuto = userroleAuto.getRoleId();
            long roleIdOri = 0;
            if (userroleOri == null) {
                roleIdOri = -1;
            } else {
                roleIdOri = userroleOri.getRoleId();
            }

            long[] roleName = {5, 4, 3, 2, 1};
            int indexAuto = 0;
            int indexOri = 0;
            int indexRoleid = 0;
            for (int i = 0; i < roleName.length; i++) {
                if (roleIdAuto == roleName[i]) {
                    indexAuto = i;
                }
                if (roleid == roleName[i]) {
                    indexRoleid = i;
                }
                if (roleIdOri != -1 && roleIdOri == roleName[i]) {
                    indexOri = i;
                } else if (roleIdOri == -1) {
                    indexOri = -1;
                }
            }
            if (indexAuto <= indexOri) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该用户权限大于等于您的权限");
                return map;
            }
            try {
                if (userroleOri != null && indexOri < indexAuto && indexRoleid < indexAuto) {
                    userRoleResourceBusiness.upuserRole(userOriId, roleid);
                } else if (indexOri < indexAuto && indexRoleid < indexAuto) {
                    UserRole role = new UserRole();
                    role.setCreateTime(new Date());
                    role.setCertigierUserId(userId);
                    role.setBrandId(userOriBrandid);
                    role.setRoleId(roleid);
                    role.setUserId(userOriId);
                    role.setStatus("0");
                    userRoleResourceBusiness.adduserRole(role);
                } else {
                    LOG.info("=============您设置的权限大于等于您的权限===========");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "该用户不存在且您设置的权限大于等于您的权限");
                    return map;
                }
            } catch (Exception e) {
                LOG.info(e.getMessage());
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "设置时权限出现异常");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户不是您的直推或间推");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    // 代理商费率设置接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/channelrate/{token}")
    @ResponseBody
    public Object userUpdateChannelRate(HttpServletRequest request, @PathVariable("token") String token,
                                        @RequestParam(value = "phone") String phone, @RequestParam(value = "rate") BigDecimal rate,
                                        @RequestParam(value = "channelId") long channelid,
                                        @RequestParam(value = "lower_level", defaultValue = "0", required = false) int lowerLevel) {
        Map map = new HashMap();

        long userid;
        long userAutoBrandid;
        try {
            userid = TokenUtil.getUserId(token);
            userAutoBrandid = TokenUtil.getBrandid(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        User userOri = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, userAutoBrandid);
        if (userOri == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户不存在,请仔细核对用户手机号。");
            return map;
        }
        long userOriId = userOri.getId();

        Long[] str2 = getUseridArray(userid, lowerLevel);
        if (str2.length == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户没有直推或间推");
            return map;
        }

        if (userOri != null && ArrayUtils.contains(str2, userOriId)) {
            ChannelRate channelRateOri = channelRateBusiness.findChannelRateByUserid(userOriId, channelid);
            BrandRate userAuto = brandMangeBusiness.findRateByBrandAndChannel(userAutoBrandid, channelid);
            BigDecimal extraFee = userAuto.getExtraFee();
            BigDecimal withdrawFee = userAuto.getWithdrawFee();
            BigDecimal channelRate = userAuto.getRate();
            ChannelRate channelRateAuto = channelRateBusiness.findChannelRateByUserid(userid, channelid);
            BigDecimal channelrateAuto;
            if (channelRateAuto == null) {
                channelrateAuto = userAuto.getRate();
            } else {
                channelrateAuto = channelRateAuto.getRate();
            }
            if (channelRateOri == null) {
                ChannelRate oriChannelRate = new ChannelRate();
                oriChannelRate.setBrandId(userAutoBrandid);
                oriChannelRate.setChannelId(channelid);
                oriChannelRate.setRate(channelRate);
                oriChannelRate.setExtraFee(extraFee);
                oriChannelRate.setUserId(userOriId);
                oriChannelRate.setWithdrawFee(withdrawFee);
                channelRateBusiness.mergeChannelRate(oriChannelRate);
                LOG.info("=========channelrate表中没有该用户，已重新添加成功=========");
            }
            try {
                int result = channelrateAuto.compareTo(rate);
                if (result != 1) {
                    channelRateBusiness.updChannelRateBybrandidAndChannelidanduserId(rate, userAutoBrandid, channelid,
                            userOriId);
                    LOG.info("=========channelrate表中该用户费率已经修改成功=========");
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "该用户费率低于您的费率");
                    return map;
                }
            } catch (Exception e) {
                LOG.info(e.getMessage());
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "用户通道费率查询返回为null");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户不是您的直推或间推");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    private Long[] getUseridArray(long userId, int lowerLevel) {
        List<User> users = new ArrayList<User>();
        List<User> iusers = new ArrayList<User>();
        Long[] str2 = {userId};
        for (int i = 1; lowerLevel == 0 ? (i > 0) : (i <= lowerLevel); i++) {
            StringBuilder sbd = new StringBuilder();
            iusers = userLoginRegisterBusiness.findInfoUsers(str2);
            if (iusers.size() == 0) {
                int userlength = users.size();
                str2 = new Long[userlength];
                for (int j = 0; j < userlength; j++) {
                    long userj = users.get(j).getId();
                    str2[j] = userj;
                }
                break;
            }
            for (User iuser : iusers) {
                users.add(iuser);
                sbd.append(iuser.getId() + ",");
            }
            if (sbd.length() > 0) {
                String[] str1 = sbd.toString().split(",");
                str2 = new Long[str1.length];
                int str1length = str1.length;
                for (int j = 0; j < str1length; j++) {
                    str2[j] = Long.valueOf(str1[j]);
                }
            }
        }
        return str2;
    }

    // 查询代理商权限下所有下级会员的信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/userrole/querybyroleid/{token}")
    @ResponseBody
    public Object queryOriRoleidByRoleid(HttpServletRequest request, @PathVariable("token") String token,
                                         @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                         @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                         @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                         @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
        Map map = new HashMap();
        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        long userid;
        try {
            userid = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        Page<UserRole> userrole = userRoleResourceBusiness.findUserRoleByRoleid(userid, pageable);
        long userroleSize = userrole.getTotalElements();
        if (userroleSize == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "没有下级会员被授权此权限！");
            return map;
        }
        List<Map> userInfo = new ArrayList<Map>();
        for (UserRole forUserRole : userrole) {
            Map map1 = new HashMap();
            long userId = forUserRole.getUserId();
            User user = userLoginRegisterBusiness.queryUserById(userId);
            if (user == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "该下级会员已经不存在了！");
                return map;
            }
            int shopstatus = Integer.parseInt(user.getShopsStatus());
            map1.put("userid", userId);
            map1.put("phone", user.getPhone());
            map1.put("fullname", user.getFullname());
            map1.put("roleid", user.getGrade());
            map1.put("status", forUserRole.getStatus());
            map1.put("usershopStatus", shopstatus == 1 ? "1" : (shopstatus == 0) ? "0" : (shopstatus == 2) ? "2" : "3");
            map1.put("realnameStatus", user.getFullname() == null ? "2" : "1");
            map1.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(forUserRole.getCreateTime()));
            userInfo.add(map1);
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        map.put(CommonConstants.RESULT, userInfo);
        return map;
    }

    // 查询该用户权限下会员的信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/userrole/querybyuserid/{token}")
    @ResponseBody
    public Object queryOriRoleidByUserid(HttpServletRequest request, @PathVariable("token") String token) {
        Map map = new HashMap();
        long userid;
        long brandid;
        try {
            userid = TokenUtil.getUserId(token);
            brandid = TokenUtil.getBrandid(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        Brand brand = brandMangeBusiness.findBrandById(brandid);
        List<UserRole> userrole = null;
        if (brand.getManageid() == userid) {
            userrole = userRoleResourceBusiness.findUserRoleByBrandid(brandid);
        } else {
            userrole = userRoleResourceBusiness.findUserRoleByUserid(userid);
        }
        if (userrole.size() == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您没有下级会员");
        } else {
            int userroleSize = userrole.size();
            List<Map> listMap = new ArrayList<Map>();
            for (int i = 0; i < userroleSize; i++) {
                Map userMap = new HashMap();
                long oriUserid = userrole.get(i).getUserId();
                User oriUser = userLoginRegisterBusiness.queryUserById(oriUserid);
                userMap.put("userinfo", oriUser);
                listMap.add(userMap);
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, listMap);
        }
        return map;
    }

    // 临时使用，添加t_user_role两个字段信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/userrole/querybyuserid/linshi")
    @ResponseBody
    public Object queryOriRoleidByLinshi(HttpServletRequest request) {
        Map map = new HashMap();
        List userrole = userRoleResourceBusiness.queryFindAll();
        Long[] usersid = new Long[userrole.size()];
        for (int i = 0; i < usersid.length; i++) {
            usersid[i] = (Long) userrole.get(i);
            User user = userLoginRegisterBusiness.queryUserById(usersid[i]);
            long brandid = 0;
            if (user != null) {
                brandid = user.getBrandId();
            }
            Brand branduser = userLoginRegisterBusiness.findBrandByBrandid(brandid);
            long brandManage = 0;
            if (branduser != null) {
                brandManage = branduser.getManageid();
            }
            userLoginRegisterBusiness.updateByAll(brandManage, usersid[i], brandid);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改成功");
        return map;
    }

    // 直推排行
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/")
    @ResponseBody
    public Object queryOriRoleidByRoleid(HttpServletRequest request, @RequestParam(value = "brand_id") long brandid,
                                         @RequestParam(value = "endtime", required = false) String endtime,
                                         @RequestParam(value = "starttime", required = false) String startTime,
                                         @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        List<UserRoleForm> ufList = userLoginRegisterBusiness.findUserPreList(brandid, startTime, endtime, size);
        Map map = new HashMap();

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改成功");
        map.put(CommonConstants.RESULT, ufList);
        return map;
    }

    // 后台上传分享二维码背景图
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/background/uploads")
    public @ResponseBody
    Object uploadBackgroundImgs(HttpServletRequest request,
                                @RequestParam(value = "brandId") long brandid, @RequestParam(value = "phone") String phone,
                                @RequestParam(value = "files") MultipartFile[] files) {
        Map map = new HashMap();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无此用户");
            return map;
        } else {
            String path = "appsm";
            File dir = new File(uploadImg + brandid + "/" + path);
            // 创建目录
            if (dir.mkdirs()) {
                System.out.println("创建目录" + uploadImg + brandid + "/" + path + ":成功！");

            } else {
                File[] tempfiles = dir.listFiles();
                for (File file : tempfiles) {
                }

                System.out.println("创建目录" + uploadImg + brandid + "/" + path + ":失败！");
            }

            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getOriginalFilename();// 获取文件名加后缀
                    String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());// 文件后缀
                    fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + fileF;// 新的文件名
                    File dest = new File(uploadImg + brandid + "/" + path + "/" + fileName);
                    try {
                        files[i].transferTo(dest);
                    } catch (IllegalStateException e) {
                        LOG.info(e.getMessage());
                    } catch (IOException e) {
                        LOG.info(e.getMessage());
                    }
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "上传失败");
                return map;
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "上传成功");
        return map;
    }

    // app获取二维码分享图片
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/background/downloads")
    public @ResponseBody
    Object downloadPic(HttpServletRequest request, @RequestParam(value = "brandid") String brandId,
                       @RequestParam(value = "phone") String phone) {

        Map map = new HashMap();
        long brandid = Long.parseLong(brandId);

        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无此用户信息");
            return map;
        }
        List<String> filepaths = new ArrayList<String>();
        String path = "appsm";
        if (brandId != null && !brandId.equals("")) {
            File file = new File(uploadImg + brandid + "/" + path);
            String[] filelist = file.list();
            if (filelist != null) {
                for (int i = 0; i < filelist.length; i++) {
                    filepaths.add(downloadImg + brandid + "/" + path + "/" + filelist[i]);
                }
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无二维码分享背景图");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "获取成功");
        map.put(CommonConstants.RESULT, filepaths);
        return map;
    }

    // 后台上传二维码
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/demoCode/uploads")
    public @ResponseBody
    Object uploadDemoCode(HttpServletRequest request,
                          @RequestParam(value = "brandId") long brandid, @RequestParam(value = "phone") String phone,
                          @RequestParam(value = "files") MultipartFile[] files) {
        Map map = new HashMap();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无此用户");
            return map;
        } else {
            String path = "demoCode";
            File dir = new File(uploadImg + brandid + "/" + path);
            // 创建目录
            if (dir.mkdirs()) {
                System.out.println("创建目录" + uploadImg + brandid + "/" + path + ":成功！");

            } else {
                File[] tempfiles = dir.listFiles();
                for (File file : tempfiles) {
                }

                System.out.println("创建目录" + uploadImg + brandid + "/" + path + ":失败！");
            }

            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getOriginalFilename();// 获取文件名加后缀
                    String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());// 文件后缀
                    fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + fileF;// 新的文件名
                    File dest = new File(uploadImg + brandid + "/" + path + "/" + fileName);
                    try {
                        files[i].transferTo(dest);
                    } catch (IllegalStateException e) {
                        LOG.info(e.getMessage());
                    } catch (IOException e) {
                        LOG.info(e.getMessage());
                    }
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "上传二维码失败");
                return map;
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "上传成功");
        return map;
    }

    // app获取二维码图片
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/demoCode/downloads")
    public @ResponseBody
    Object downloadDemoCode(HttpServletRequest request,
                            @RequestParam(value = "brandid") String brandId, @RequestParam(value = "phone") String phone) {

        Map map = new HashMap();
        long brandid = Long.parseLong(brandId);

        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无此用户信息");
            return map;
        }
        List<String> filepaths = new ArrayList<String>();
        String path = "demoCode";
        if (brandId != null && !brandId.equals("")) {
            File file = new File(uploadImg + brandid + "/" + path);
            String[] filelist = file.list();
            if (filelist != null) {
                for (int i = 0; i < filelist.length; i++) {
                    filepaths.add(downloadImg + brandid + "/" + path + "/" + filelist[i]);
                }
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无二维码图片");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "获取成功");
        map.put(CommonConstants.RESULT, filepaths);
        return map;
    }

    // 查询用户推荐下级的数据
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/getmoney/queryby/brandid")
    public @ResponseBody
    Object queryGetMoneyByBrandId(@RequestParam(value = "brandId") String brandId) {

        List<GetMoney> getMoney;
        try {
            getMoney = getMoneyBusiness.getGetMoneyByBrandId(brandId);
        } catch (Exception e) {
            e.printStackTrace();

            return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
        }

        if (getMoney != null && getMoney.size() > 0) {

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", getMoney);
        } else {

            return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", getMoney);
        }

    }

    // 后台分页查询用户推荐下级的数据
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/getmoney/queryby/brandidand/page")
    public @ResponseBody
    Object queryGetMoneyByBrandIdAndPage(@RequestParam(value = "brandId") String brandId,
                                         @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                         @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                         @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                         @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                         @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        Page<GetMoney> getMoneyByBrandIdAndPage;
        try {
            getMoneyByBrandIdAndPage = getMoneyBusiness.getGetMoneyByBrandIdAndPage(brandId, pageable);
        } catch (Exception e) {
            e.printStackTrace();

            return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
        }

        if (getMoneyByBrandIdAndPage != null) {

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", getMoneyByBrandIdAndPage);
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂无数据");
        }

    }

    // 添加用户推荐下级的数据
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/getmoney/addphone")
    public @ResponseBody
    Object addGetMoney(@RequestParam(value = "brandId") String brandId,
                       @RequestParam(value = "phone") String phone, @RequestParam(value = "content") String content,
                       @RequestParam(value = "money") String money) {

        GetMoney getMoney = new GetMoney();

        getMoney.setBrandId(brandId);
        getMoney.setPhone(phone);
        getMoney.setContent(content);
        getMoney.setMoney(new BigDecimal(money));
        getMoney.setStatus(1);

        try {
            GetMoney createGetMoney = getMoneyBusiness.createGetMoney(getMoney);
        } catch (Exception e) {
            e.printStackTrace();

            return ResultWrap.init(CommonConstants.FALIED, "添加失败,请稍后重试!");
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "添加成功!");

    }

    // 删除用户推荐下级的数据
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/getmoney/deleteby/brandid/andid")
    public @ResponseBody
    Object deleteGetMoneyByBrandIdAndId(@RequestParam(value = "brandId") String brandId,
                                        @RequestParam(value = "Id") String Id) {

        long[] l = null;
        if (Id.contains(",")) {

            String[] split = Id.split(",");
            l = new long[split.length];

            for (int i = 0; i < split.length; i++) {
                l[i] = Long.parseLong(split[i]);
            }

        } else {

            String[] split = {Id};
            l = new long[split.length];

            for (int i = 0; i < split.length; i++) {
                l[i] = Long.parseLong(split[i]);
            }

        }

        List<GetMoney> getMoneyByBrandIdAndId = getMoneyBusiness.getGetMoneyByBrandIdAndId(brandId, l);

        if (getMoneyByBrandIdAndId != null && getMoneyByBrandIdAndId.size() > 0) {

            for (GetMoney gm : getMoneyByBrandIdAndId) {
                try {
                    gm.setStatus(0);
                    gm.setUpdate_time(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

                    getMoneyBusiness.createGetMoney(gm);
                } catch (Exception e) {
                    e.printStackTrace();

                    continue;
                }

            }

            return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");

        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
        }
		
		
		
		/*if (getMoneyByBrandIdAndId != null && getMoneyByBrandIdAndId.size() > 0) {

			for (GetMoney gm : getMoneyByBrandIdAndId) {

				try {
					getMoneyBusiness.deleteGetMoneyByBrandIdAndId(gm);
				} catch (Exception e) {
					e.printStackTrace();

					continue;
				}

			}

			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}*/

    }


    // 读取头像的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/headportrait/getby/phone")
    public @ResponseBody
    Object getHeadPortrait(HttpServletRequest request,
                           @RequestParam(value = "brandId") String brandId,
                           @RequestParam(value = "phone") String phone) {

        String path = "http://106.15.47.73:8888/headportrait/brandid";

        String readpath = "/usr/local/nginx/html/headportrait/brandid";

        List<String> filepaths = new ArrayList<String>();
        File file = new File(readpath + "/" + brandId + "/" + phone);

        String path2 = null;
        if (file != null) {

            // path2 = file.getPath();

            String[] filelist = file.list();
            if (filelist != null) {
                for (int i = 0; i < filelist.length; i++) {
                    // filepaths.add(downloadPath + us.getUserId() + "/" + filelist[i]);
                    String string = filelist[i];

                    path2 = path + "/" + brandId + "/" + phone + "/" + string;
                }

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", path2);

            } else {

                return ResultWrap.init(CommonConstants.FALIED, "暂无上传头像", "");
            }

        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂无上传头像", "");
        }

        /*
         * String path2 = file.getPath();
         *
         * String[] filelist = file.list(); if (filelist != null) { for (int i = 0; i <
         * filelist.length; i++) { //filepaths.add(downloadPath + us.getUserId() + "/" +
         * filelist[i]); String string = filelist[i]; }
         *
         * }
         */

        // return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", path2);
    }


    // 更换头像的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/headportrait/updateby/phone")
    public @ResponseBody
    Object updateHeadPortrait(HttpServletRequest request,
                              @RequestParam(value = "brandId") String brandId,
                              @RequestParam(value = "phone") String phone) {

        String path = "/usr/local/nginx/html/headportrait/brandid";

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> files = multipartRequest.getFiles("image");

        File dir = new File(path + "/" + brandId + "/" + phone);
        // 创建目录
        if (dir.mkdirs()) {

        } else {

            File[] tempfiles = dir.listFiles();
            for (File file : tempfiles) {
                file.delete();
            }

            // System.out.println("创建目录" + path + phone + "失败！");
        }
        int i = 1;

        if (files != null && files.size() > 0) {
            for (MultipartFile file : files) {

                String fileName = file.getOriginalFilename();
                String prefix = fileName.substring(fileName.lastIndexOf("."));
                i++;
                File dest = new File(path + "/" + brandId + "/" + phone + "/" + System.currentTimeMillis() + i + prefix);

                try {
                    file.transferTo(dest);
                    Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
                    return ResultWrap.init(CommonConstants.SUCCESS, "更换头像成功!");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    LOG.error("", e);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("", e);
                }
            }
        }

        return ResultWrap.init(CommonConstants.FALIED, "更换头像失败!");
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrebateratio/getbybrandid/andgrade")
    public @ResponseBody
    Object getBrandRebateRatioByBrandIdAndGrade(HttpServletRequest request,
                                                int brandId,
                                                @RequestParam(value = "grade", required = false, defaultValue = "-1") int grade) {

        if (grade != -1) {
            BrandRebateRatio brandRebateRatioByBrandIdAndGrade = brandMangeBusiness.getBrandRebateRatioByBrandIdAndGrade(brandId, grade);

            if (brandRebateRatioByBrandIdAndGrade != null) {
                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandRebateRatioByBrandIdAndGrade);
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
            }
        } else {
            List<BrandRebateRatio> brandRebateRatioByBrandId = brandMangeBusiness.getBrandRebateRatioByBrandId(brandId);

            if (brandRebateRatioByBrandId != null && brandRebateRatioByBrandId.size() > 0) {
                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandRebateRatioByBrandId);
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
            }
        }

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrebateratio/queryall")
    public @ResponseBody
    Object queryBrandRebateRatioAll(HttpServletRequest request,
                                    int brandId
    ) {

        List<BrandRebateRatio> brandRebateRatioByBrandId = brandMangeBusiness.getBrandRebateRatioByBrandId(brandId);

        if (brandRebateRatioByBrandId != null && brandRebateRatioByBrandId.size() > 0) {

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandRebateRatioByBrandId);
        } else {

            return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", brandRebateRatioByBrandId);
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrebateratio/create/orupdate")
    public @ResponseBody
    Object createOrUpdateBrandRebateRatio(HttpServletRequest request,
                                          int brandId,
                                          @RequestParam(value = "id", required = false, defaultValue = "-1") long id,
                                          @RequestParam(value = "grade", required = false, defaultValue = "-1") int grade,
                                          @RequestParam(value = "status", required = false, defaultValue = "-1") int status,
                                          @RequestParam(value = "ratio", required = false, defaultValue = "-1") String ratio) {

        BrandRebateRatio brandRebateRatioByBrandIdAndId = null;
        //if(id == -1) {
        brandRebateRatioByBrandIdAndId = brandMangeBusiness.getBrandRebateRatioByBrandIdAndGrade(brandId, grade);
		/*}else {
			brandRebateRatioByBrandIdAndId = brandMangeBusiness.getBrandRebateRatioByBrandIdAndId(brandId, id);
		}*/

        ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId, grade);
        String gradeName = "普通会员";
        if (thirdLevelByBrandidandgrade != null) {
            gradeName = thirdLevelByBrandidandgrade.getName();
        }

        if (brandRebateRatioByBrandIdAndId == null) {
            BrandRebateRatio brandRebateRatio = new BrandRebateRatio();
            brandRebateRatio.setBrandId(brandId);
            brandRebateRatio.setGrade(grade);
            brandRebateRatio.setGradeName(gradeName);
            brandRebateRatio.setRatio(new BigDecimal(ratio));
            brandRebateRatio.setStatus(status);

            brandMangeBusiness.createBrandRebateRatio(brandRebateRatio);

            return ResultWrap.init(CommonConstants.SUCCESS, "执行成功!");
        } else {
            brandRebateRatioByBrandIdAndId.setRatio(!"-1".equals(ratio) ? new BigDecimal(ratio) : brandRebateRatioByBrandIdAndId.getRatio());
            brandRebateRatioByBrandIdAndId.setStatus(status != -1 ? status : brandRebateRatioByBrandIdAndId.getStatus());
            brandRebateRatioByBrandIdAndId.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

            brandMangeBusiness.createBrandRebateRatio(brandRebateRatioByBrandIdAndId);

            return ResultWrap.init(CommonConstants.SUCCESS, "执行成功!");
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrebateratio/delete")
    public @ResponseBody
    Object deleteBrandRebateRatio(HttpServletRequest request,
                                  int brandId,
                                  String id
    ) {
        long[] l = null;
        if (id.contains(",")) {
            String[] split = id.split(",");
            l = new long[split.length];

            for (int i = 0; i < split.length; i++) {
                l[i] = Long.parseLong(split[i]);
            }
        } else {
            String[] split = {id};
            l = new long[split.length];

            for (int i = 0; i < split.length; i++) {
                l[i] = Long.parseLong(split[i]);
            }

        }

        List<BrandRebateRatio> brandRebateRatioByBrandIdAndId = brandMangeBusiness.getBrandRebateRatioByBrandIdAndId(brandId, l);

        for (BrandRebateRatio brr : brandRebateRatioByBrandIdAndId) {
            brandMangeBusiness.deleteBrandRebateRatio(brr);
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
    }


    /*
     *@description:用户股东资格的添加
     *@author: ives
     *@annotation:"添加股东身份（股东没卵用，只是为了好看，贴牌商很虚伪）！"
     *@data:2019年10月09日  18:00:32
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/olderUpdate")
    public @ResponseBody
    Object olderUpdate(@RequestParam(value = "phone") String phone,
                       @RequestParam(value = "brandId") String brandId,
                       @RequestParam(value = "status") int status) {
        if ("".equals(brandId) || null == brandId) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        Map<String, Object> maps = new HashMap<>();
        Long brandid = Long.valueOf(brandId);
        if (brandid == null) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        if (0 != status && 1 != status) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }        //获取用户信息 ，看用户是否是股东
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        if (user.getOlder() == status) {
            return ResultWrap.init(CommonConstants.FALIED, "无需修改");
        } else {
            user.setOlder(status);
            User user1 = userLoginRegisterBusiness.saveUser(user);
            maps.put("resp_message", "修改");
            maps.put("resp_code", "000000");
            maps.put("result", user1);
            return maps;
        }
    }

    /*
     *@description:用户股东资格的查询
     *@author: ives
     *@annotation:"查看股东身份（股东没卵用，只是为了好看，贴牌商很虚伪）！"
     *@data:2019年10月09日  18:00:32
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/olderselect")
    public @ResponseBody
    Object olderselect(@RequestParam(value = "brandId") String brandId,
                       @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                       @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        if ("".equals(brandId) || null == brandId) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        Map<String, Object> maps = new HashMap<>();
        Long brandid = Long.valueOf(brandId);
        if (brandid == null) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        Page<User> userList = userLoginRegisterBusiness.queryUserByBrandIdAndOlder(brandid, pageable);
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESULT, userList);
        maps.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return maps;
    }

    /*
     *@description:用户股东资格的查询手机号查询
     *@author: ives
     *@annotation:"查看股东身份（股东没卵用，只是为了好看，贴牌商很虚伪）！"
     *@data:2019年10月09日  18:00:32
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/olderselectbyphone")
    public @ResponseBody
    Object olderSelectByPhone(@RequestParam(value = "brandId") String brandId,
                              @RequestParam(value = "phone") String phone) {
        if ("".equals(brandId) || null == brandId) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        Map<String, Object> maps = new HashMap<>();
        Long brandid = Long.valueOf(brandId);
        if (brandid == null) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        List<User> userlist = new ArrayList<>();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        userlist.add(user);
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESULT, userlist);
        maps.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return maps;
    }




    /*
     *@description:用户股东资格的定时器查询
     *@author: ives
     *@annotation:"自动更新添加股东身份（股东没卵用，只是为了好看，贴牌商很虚伪）！"
     *@data:2019年10月09日  18:00:32
     *
     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void updateOlder() {
//        if ("true".equals(scheduleOnOff)) {
//            olderatuoupdate("1");
//        }
//    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/olderautoupdate")
    @ResponseBody
    public Object olderatuoupdate(@RequestParam("status") String status) {
        if (!"1".equals(status)) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        } else {
            LOG.info("===========================正在执行升级股东身份");
            List<UserOlderConfig> userOlderConfigs = userOlderConfigBusiness.findAll();
            LOG.info("============获取到的配置信息有" + userOlderConfigs.size() + "条");
            for (UserOlderConfig userOlderConfig : userOlderConfigs) {
                if (userOlderConfig.getStatus() == 0) {
                    continue;
                }
                if (!userOlderConfig.getGrade().equals("1") && !userOlderConfig.getGrade().equals("2") && !userOlderConfig.getGrade().equals("3") && !userOlderConfig.getGrade().equals("4")) {
                    if (userOlderConfig.getGrade().equals("team1")) {
                        int[] grade = {1, 2};
                        Long count = Long.valueOf(userOlderConfig.getCount());
                        Long firstUser = Long.valueOf(userOlderConfig.getFirstUser());
                        Long[] userRealtionList = userRelationBusiness.findByCounts(count, firstUser, grade);
                        LOG.info("满足混合奖励的人数有=============================" + userRealtionList.length);
                        List<User> userList = userLoginRegisterBusiness.queryUserByIdIn(userRealtionList);
                        LOG.info("真正获取到的满足混合奖励的人数有=============================" + userList.size());
                        for (User user : userList) {
                            if (user.getOlder() != 1) {
                                user.setOlder(1);
                                userLoginRegisterBusiness.saveUser(user);
                            }
                        }
                    }
                    LOG.info("=======================================================================================");
                } else {
                    String grade1 = userOlderConfig.getGrade();
                    int grade = Integer.parseInt(grade1);
                    Long count = Long.valueOf(userOlderConfig.getCount());
                    Long firstUser = Long.valueOf(userOlderConfig.getFirstUser());
                    Long[] userRealtionList = userRelationBusiness.findByCount(count, firstUser, grade);
                    LOG.info("直推下级等级为" + userOlderConfig.getGrade() + "的满足条件的股东人数有==========" + userRealtionList.length);
                    List<User> userList = userLoginRegisterBusiness.queryUserByIdIn(userRealtionList);
                    LOG.info("真正获取到的满足奖励的人数有=============================" + userList.size());
                    for (User user : userList) {
                        if (user.getOlder() != 1) {
                            user.setOlder(1);
                            userLoginRegisterBusiness.saveUser(user);
                        }
                    }
                    LOG.info("=======================================================================================");
                }


            }
            LOG.info("执行结束");
            return ResultWrap.init(CommonConstants.SUCCESS, "操作成功");

        }

    }


    /*
     *@description:用户股东资格发放奖励的接口
     *@author: ives
     *@annotation:"客户都是骗子，之前说股东没用，现在又要给股东分钱。大骗子！！"
     *@data:2019年10月16日  17:00:32
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/olderrebate")
    @ApiOperation(value = "根据传入的金额和状态，去给所有的股东用户发放金额", notes = "status只有为1的时候会执行，其他参数会弹出")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "account", value = "要平分的金额", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "要启动接口的参数,只有为1的时候才会启动", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "brandId", value = "贴牌ID,不正确会访问不成功，相当于验证参数", required = true, dataType = "String")
    })
    @ResponseBody
    public Object olderrebate(@RequestParam("account") String account,
                              @RequestParam("status") int status,
                              @RequestParam("brandId") String brandId) {
        if (1 != status) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        if ("".equals(brandId) || brandId == null) {
            return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
        }
        Map<String, Object> maps = new HashMap<>();
        account = account.trim();
        BigDecimal blance = new BigDecimal(account).setScale(2, BigDecimal.ROUND_HALF_UP);
        Long brandid = Long.valueOf(brandId);
        List<User> userList = userLoginRegisterBusiness.findUserByBrandIdAndOlder(brandid);
        if (userList.isEmpty()) {
            return ResultWrap.init(CommonConstants.FALIED, "股东人数为0");
        }
        Long[] userids = new Long[userList.size()];
        int count = 0;
        for (User user : userList) {
            userids[count] = user.getId();
            count++;
        }
        BigDecimal pro = new BigDecimal(userList.size());
        LOG.info("===============brandId为：" + brandId + "============股东为" + pro.toString() + "人");
        LOG.info("==========================股东奖励开始发放========共发放" + blance + "元==========");
        BigDecimal rebate = blance.divide(pro);
        LOG.info("=============每个股东获的" + rebate.toString() + "元======================");
        List<UserAccount> userAccountList = userBalBusiness.queryUserAccountByUsers(userids);
        Calendar c1 = Calendar.getInstance();
        String executeDateTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
        String executeDate = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd");
        UserOlderRebateHistory userOlderRebateHistory;
        for (UserAccount userAccount : userAccountList) {
            Long userId = userAccount.getUserId();
            String orderCode = UUIDGenerator.getUUID();
            userOlderRebateHistory = new UserOlderRebateHistory();
            userOlderRebateHistory.setUserId(userId);
            userOlderRebateHistory.setBalance(rebate);
            userOlderRebateHistory.setOrderId(orderCode);
            userOlderRebateHistory.setExecuteDateTime(executeDateTime);
            userOlderRebateHistory.setExecuteDate(executeDate);
            userOlderRebateHistoryBusiness.saveUserOlderRebateHistory(userOlderRebateHistory);
            userRebateService.updateolderUserAccount(null, userId, rebate, orderCode, "0", "5");
        }
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESULT, "发放金额:" + blance + "元，发放人数：" + pro + "人，每人获得：" + rebate + "元");
        maps.put(CommonConstants.RESP_MESSAGE, "操作成功");
        return maps;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upgrade/orderview")
    @ResponseBody
    public Object userUpgradeOrder(@RequestParam(value = "brandid", defaultValue = "1", required = false) Long brandid) {
        Map map = new HashMap<>();
        if (brandid == 1) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, tUpgradeOrderViewBusiness.queryAll());
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, tUpgradeOrderViewBusiness.queryBrandAll(brandid));
        return map;
    }


    //信用积分查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/credit/score")
    @ResponseBody
    public Object userCreditScore(@RequestParam(value = "userid") Long userid) {
        UserAccount user = userBalBusiness.queryUserAccountByUserid(userid);
        Map map = new HashMap<>();
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "系统内无此人");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user.getCreditPoints());
        return map;
    }

    //信用积分添加
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/credit/score/add")
    @ResponseBody
    public Object userCreditScoreAdd(@RequestParam(value = "userid") Long userid) {
        Map map = new HashMap<>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, AndSetUser(0, 2, userid));
        return map;
    }


    /**
     * 三级分销信用分添加
     * @param begin
     * @param end
     * @param preUserid
     * @return
     */
    public Integer AndSetUser(Integer begin, Integer end, Long preUserid) {
        User user = userLoginRegisterBusiness.queryUserById(preUserid);
        List<UserRealtion> byFirstUserId = userRealtionBusiness.findByFirstUserId(user.getPreUserId());
        int checked = 0;
        for (int flag = begin; flag <= end; flag++) {
            if (flag == 0) {
                checked = userBalBusiness.updataCreditPoints(preUserid, new BigDecimal(1));
                LOG.info("直推添加信用分---------------------" + checked);
            } else if (flag == 1 && byFirstUserId.get(flag).getLevel() == 1) {
                checked = checked + userBalBusiness.updataCreditPoints(byFirstUserId.get(flag).getPreUserId(), new BigDecimal(1));
                LOG.info("一级间推添加信用分---------------------" + checked);
            } else if (flag == 2 && byFirstUserId.get(flag).getLevel() == 2) {
                checked = checked + userBalBusiness.updataCreditPoints(byFirstUserId.get(flag).getPreUserId(), new BigDecimal(1));
                LOG.info("二级间推添加信用分---------------------" + checked);
            }
        }
//        List<User> collect = PreUser.parallelStream().collect(toList());
//        List<User> collect1 = PreUserNoe.parallelStream().collect(toList());
//        if (collect1.size() > 0) {
//            collect.addAll(collect1);
//        }
//        List<User> collect2 = PreUserTwo.parallelStream().collect(toList());
//        if (collect2.size() > 0) {
//            collect.addAll(collect1);
//        }
//        List<User> listAllDistinct = collect.stream().distinct().collect(toList());//并集去重
//        List<User> CreditPointsAll = listAllDistinct.stream().filter(item -> Integer.parseInt(item.getGrade()) > 0).collect(toList());
        return checked;
    }









//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/older/selectolderrebate")
//    @ApiOperation(value="根据传入的用户ID去查询用户今日收益和昨日收益",notes = "股东分红")
//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "query",name="user_id",value = "用户的ID",required = true,dataType = "String"),
//    })
//    @ResponseBody
//    public Object olderRebateSelect(@RequestParam("user_id") String userId ){
//            if("".equals(userId)||userId==null){
//                return ResultWrap.init(CommonConstants.FALIED, "参数不正确");
//            }
//        Map<String,Object> maps=new HashMap<>();
//        Calendar c1 = Calendar.getInstance();
//        String executeDate=DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd");
//
//
//
//
//
//        return null;
//    }


}

