package com.jh.user.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.jh.user.async.AsyncMethod;
import com.jh.user.business.BrandAlertBusiness;
import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRoleResourceBusiness;
import com.jh.user.jdpush.Jdpush;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandAlert;
import com.jh.user.pojo.JdpushHistory;
import com.jh.user.pojo.User;
import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.TokenUtil;

@Controller
@EnableAutoConfiguration
public class UserJpushService {

    private static final Logger LOG = LoggerFactory.getLogger(UserJpushService.class);

    @Autowired
    private UserRoleResourceBusiness userRoleResourceBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    Util util;

    @Autowired
    private BrandManageBusiness brandMangeBusiness;

    @Autowired
    private BrandAlertBusiness brandAlertBusiness;

    @Autowired
    private AsyncMethod asyncMethod;

    @Value("${version.ios.number}")
    private String IOSNum;

    @Value("${version.android.number}")
    private String AanroidNum;

    @Value("${version.android.url}")
    private String AanroidUrl;

    /***
     *
     * 推送测试
     * **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/tset")
    public @ResponseBody
    Object setJpushtest(HttpServletRequest request,
                        @RequestParam(value = "userId") long userId,
                        //标题
                        @RequestParam(value = "alert") String alert,
                        //内容
                        @RequestParam(value = "content") String content,
                        //标识
                        @RequestParam(value = "btype") String btype,
                        //标识值
                        @RequestParam(value = "btypeval") String btypeval
    ) {
        asyncMethod.JpushTest(userId, alert, content, btype, btypeval);
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, "");
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    public void JpushTest(long userId, String alert, String content, String btype, String btypeval) {
        User user = userLoginRegisterBusiness.queryUserById(userId);
        Brand brand = brandMangeBusiness.findBrandById(user.getBrandId());
        String APPKEY = brand.getAppkey();
        String MASTERSECRET = brand.getMastersecret();
        JdpushHistory jdh = new JdpushHistory();
        jdh.setUserid(userId);
        jdh.setBtype(btype);
        jdh.setContent(content);
        jdh.setCreateTime(new Date());
        jdh.setTitle(alert);
        Map<String, Object> jdhmap = new HashMap<>();
        jdhmap.put(btype, btypeval);

        BrandAlert brandAlertByBrandIdAndType = brandAlertBusiness.getBrandAlertByBrandIdAndType(brand.getId() + "", btype);
        //status 为0 代表弹窗, status 为1代表不弹窗
        String status = "0";
        if (brandAlertByBrandIdAndType != null) {
            status = "1";
        }
        jdhmap.put("isPush", status);
        if (APPKEY != null && !APPKEY.equals("") && MASTERSECRET != null && !MASTERSECRET.equals("")) {
            Jdpush.sendPushNotice(APPKEY, MASTERSECRET, jdh.getUserid() + "", jdh.getTitle(), jdhmap, jdh.getContent());
            userRoleResourceBusiness.addJdpushHistory(jdh);
            LOG.info("UserJpushService_finduserroleresource", jdh.toString());
            userRoleResourceBusiness.delJdpushHistoryByUid(userId, 0);
        }
    }


    /***
     *
     * 平台推送
     * **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/tset/brand")
    public @ResponseBody
    Object setJpushtestall(HttpServletRequest request,
                           @RequestParam(value = "brand_id") long[] brandIds,
                           //标题
                           @RequestParam(value = "alert") String alert,
                           //内容
                           @RequestParam(value = "content") String content,
                           //截止时间
                           @RequestParam(value = "end_time", required = false) String endtime
    ) {
        /**
         * 获取通道信息
         * *URL：/v1.0/user/brand/query/id
         * **/
        for (long brandId : brandIds) {
            Brand brand = brandMangeBusiness.findBrandById(brandId);
            String APPKEY = brand.getAppkey();
            String MASTERSECRET = brand.getMastersecret();

            JdpushHistory jdh = new JdpushHistory();
            jdh.setUserid(0);
            jdh.setBrandId(brandId);
            jdh.setBtype("marquee");
            jdh.setContent(content);
            jdh.setCreateTime(new Date());
            jdh.setEndTime(DateUtil.getYYMMHHMMSSDateFromStr(endtime));
            jdh.setTitle(alert);
            Map jdhmap = new HashMap();
            jdhmap.put("marquee", brand.getName());

            BrandAlert brandAlertByBrandIdAndType = brandAlertBusiness.getBrandAlertByBrandIdAndType(brand.getId() + "", "marquee");
            //status 为0 代表弹窗, status 为1代表不弹窗
            String status = "0";
            if (brandAlertByBrandIdAndType != null) {
                status = "1";
            }
            jdhmap.put("isPush", status);

            if (APPKEY != null && !APPKEY.equals("") && MASTERSECRET != null && !MASTERSECRET.equals("")) {
                try {
                    Jdpush.sendPushNoticeAll(APPKEY, MASTERSECRET, jdh.getTitle(), jdhmap, jdh.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                userRoleResourceBusiness.addJdpushHistory(jdh);
                LOG.info("UserJpushService_finduserroleresource", jdh.toString());
            }
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "发送成功");
    }

    /***
     *
     * 平台Android推送
     * **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/android/brand")
    public @ResponseBody
    Object setJpushtestall(HttpServletRequest request,
                           @RequestParam(value = "brand_id") long brandId,
                           //标题
                           @RequestParam(value = "alert") String alert,
                           //内容
                           @RequestParam(value = "content") String content,
                           //标识
                           @RequestParam(value = "btype") String btype,
                           //标识值
                           @RequestParam(value = "btypeval") String btypeval
    ) {
        /**
         * 获取通道信息
         * *URL：/v1.0/user/brand/query/id
         * **/
        Brand brand = brandMangeBusiness.findBrandById(brandId);
        String APPKEY = brand.getAppkey();
        String MASTERSECRET = brand.getMastersecret();
        JdpushHistory jdh = new JdpushHistory();
        jdh.setUserid(0);
        jdh.setBrandId(brandId);
        jdh.setBtype(btype);
        jdh.setContent(content);
        jdh.setCreateTime(new Date());
        jdh.setTitle(alert);
        Map jdhmap = new HashMap();
        jdhmap.put(btype, btypeval);

        BrandAlert brandAlertByBrandIdAndType = brandAlertBusiness.getBrandAlertByBrandIdAndType(brand.getId() + "", btype);
        //status 为0 代表弹窗, status 为1代表不弹窗
        String status = "0";
        if (brandAlertByBrandIdAndType != null) {
            status = "1";
        }
        jdhmap.put("isPush", status);

        if (APPKEY != null && !APPKEY.equals("") && MASTERSECRET != null && !MASTERSECRET.equals("")) {
            Jdpush.sendPushNoticeAndroid(APPKEY, MASTERSECRET, jdh.getTitle(), jdhmap, jdh.getContent());
            userRoleResourceBusiness.delJdpushHistoryByuserId(0l, brandId, btype);
            userRoleResourceBusiness.addJdpushHistory(jdh);
            LOG.info("UserJpushService_finduserroleresource", jdh.toString());
        }
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, jdh);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /***
     * sendPushNoticeAll
     * **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/history/add")
    public @ResponseBody
    Object addJdpushHistory(HttpServletRequest request,

                            @RequestParam(value = "userid") long userid,
                            @RequestParam(value = "btype") String btype,
                            @RequestParam(value = "content") String content,
                            @RequestParam(value = "title") String title

    ) {

        JdpushHistory jdh = new JdpushHistory();
        jdh.setUserid(userid);
        jdh.setBtype(btype);
        jdh.setContent(content);
        jdh.setCreateTime(new Date());
        jdh.setTitle(title);
        Map map = new HashMap();

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userRoleResourceBusiness.addJdpushHistory(jdh));
        return map;
    }


    //查询推送记录
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/jpush/history/{token}")
    public @ResponseBody
    Object findJdpushHistory(HttpServletRequest request,
                             @PathVariable("token") String token,
                             @RequestParam(value = "user_id", defaultValue = "0", required = false) long user_Id,
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
        if (user_Id != 0) {
            map.put(CommonConstants.RESULT, userRoleResourceBusiness.findJdpushHistoryByuserId(user_Id, pageable));
        } else {
            map.put(CommonConstants.RESULT, userRoleResourceBusiness.findJdpushHistoryByuserId(userId, pageable));
        }

        return map;
    }


	/**
	 * 查询推送记录
	 */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/jpush/history/brand/{token}")
    public @ResponseBody
    Object findJdpushHistoryByBrand(HttpServletRequest request,
                                    @PathVariable("token") String token,
                                    @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandId,
                                    @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                    @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                    @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
    ) {
        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        User user = userLoginRegisterBusiness.queryUserById(userId);
        if (brandId != -1) {
            userRoleResourceBusiness.delJdpushHistoryByUid(0, brandId);
        } else {
            userRoleResourceBusiness.delJdpushHistoryByUid(0, user.getBrandId());
        }
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        if (brandId != -1) {
            map.put(CommonConstants.RESULT, userRoleResourceBusiness.findJdpushHistoryByBrandId(brandId, pageable));
        } else {
            map.put(CommonConstants.RESULT, userRoleResourceBusiness.findJdpushHistoryByBrandId(user.getBrandId(), pageable));
        }

        return map;
    }


    //通过id删除推送
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/history/delete")
    public @ResponseBody
    Object deleteJdpushHistoryById(HttpServletRequest request,
                                   @RequestParam(value = "id") long id) {
        Map map = new HashMap();
        try {
            userRoleResourceBusiness.delJdpushHistoryById(id);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "删除成功");
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "删除失败");
            LOG.error(e.getMessage());
        }
        return map;
    }

    /***
     *
     * 给接收的贴牌管理员推送消息
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/jpush/brandManage")
    public @ResponseBody
    Object setJpushBrandManage(HttpServletRequest request,
                               @RequestParam(value = "alert") String alert, // 标题
                               @RequestParam(value = "brandId") long[] brandId, // user_id
                               @RequestParam(value = "content") String content, // 内容
                               @RequestParam(value = "btype", defaultValue = "brandManager", required = false) String btype, // 标识
                               @RequestParam(value = "btypeval", defaultValue = "", required = false) String btypeval // 标识值
    ) {
        // 循环遍历接收的数组，依次添加推送
        for (long brandid : brandId) {
            Brand brand = brandMangeBusiness.findBrandById(brandid);
            String APPKEY = brand.getAppkey();
            long userid = brand.getManageid();
            String MASTERSECRET = brand.getMastersecret();
            JdpushHistory jdh = new JdpushHistory();
            jdh.setUserid(userid);
            jdh.setBtype(btype);
            jdh.setContent(content);
            jdh.setCreateTime(new Date());
            jdh.setTitle(alert);
            Map<String, Object> jdhmap = new HashMap<>();
            jdhmap.put(btype, btypeval);
            if (APPKEY != null && !APPKEY.equals("") && MASTERSECRET != null && !MASTERSECRET.equals("")) {
                Jdpush.sendPushNotice(APPKEY, MASTERSECRET, jdh.getUserid() + "", jdh.getTitle(), jdhmap,
                        jdh.getContent());
                userRoleResourceBusiness.addJdpushHistoryTest(jdh);
                LOG.info("UserJpushService_finduserroleresource", jdh.toString());
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

}
