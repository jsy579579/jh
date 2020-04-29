package com.jh.user.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.SignInBusiness;
import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.SignCalc;
import com.jh.user.pojo.SignCoin;
import com.jh.user.pojo.SignCommonCoin;
import com.jh.user.pojo.SignDetail;
import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.User;
import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;

/**
 * 签到
 */
@Controller
@EnableAutoConfiguration
public class SignInService {

    private static final Logger LOG = LoggerFactory.getLogger(SignInService.class);

    @Autowired
    Util util;

    @Autowired
    private SignInBusiness signInBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private ThirdLeveDistributionBusiness thirdLeveDistributionBusiness;

    @Value("${schedule-task.on-off}")
    private String scheduleTaskOnOff;

    // 签到的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/dosign")
    public @ResponseBody
    Object doSign(HttpServletRequest request, @RequestParam(value = "userId") String userId) {
        LOG.info("userId为：" + userId + " 的用户进入签到接口======");
        // 获取当天的时间
        String date = DateUtil.getDateFromStr(new Date());
        // 根据当天时间和userId查询是否已签到
        SignDetail sd = signInBusiness.getSignDetailByUserIdAndDate(userId, date);

        if (sd == null) {
            //根据userId查询对应的签到记录
            SignCalc scbu = signInBusiness.getSignCalcByUserId(userId);
            int i = 0;

            if (scbu == null) {
                LOG.info("userId为：" + userId + " 的用户第一次使用签到功能======");
                int signCoin = this.getSignCoinByBrandIdAndGradeAndUserIdAndContinueDays(userId, 1);

                if (signCoin != -1) {

                    try {
                        userCoinService.updateUserCoinByUserid(request, Long.parseLong(userId), signCoin,
                                UUID.randomUUID().toString().replace("-", ""), "0");
                    } catch (NumberFormatException e) {
                        LOG.info("更新用户积分有误======", e.getMessage());

                        return ResultWrap.init(CommonConstants.FALIED, "当前签到人数过多,请稍后重试!");
                    }

                    SignDetail signDetail = new SignDetail();

                    signDetail.setUserId(userId);
                    signDetail.setCoins(signCoin + "");
                    signDetail.setSignDate(date);

                    signInBusiness.createSignDetail(signDetail);

                    SignCalc signCalc = new SignCalc();
                    LOG.info("userId为：" + userId + " 的signCalc.getContinueDays()======" + signCalc.getContinueDays());
                    signCalc.setUserId(userId);
                    signCalc.setContinueDays(signCalc.getContinueDays() + 1);
                    signCalc.setLastUpdateTime(
                            DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

                    signInBusiness.createSignCalc(signCalc);

                    return ResultWrap.init(CommonConstants.SUCCESS, "签到成功!");

                } else {

                    return ResultWrap.init(CommonConstants.FALIED, "该贴牌未配置签到奖励积分!");
                }

            } else {
                //获取连续签到天数
                int continueDays = scbu.getContinueDays();
                //在获取的连续签到天数的基础上加1
                int continueDaysAdd = continueDays + 1;

                User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));

                if (user != null) {
                    long brandId = user.getBrandId();
                    String grade = user.getGrade();
                    //根据brandId和grade获取对应连续签到天数奖励积分的集合
                    List<SignCoin> sclist = signInBusiness.getSignCoinByBrandIdAndGrade(brandId + "", grade);
                    //遍历奖励积分的集合
                    for (SignCoin sc : sclist) {
                        //判断本次签到的连续签到天数与设置的连续签到天数奖励的天数是否相同
                        if (continueDaysAdd == sc.getContinueDays()) {
                            //在此标识i=1表示本次签到是否得到连续签到奖励
                            i = 1;
                            break;
                        }
                    }

                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "无该会员数据!");
                }
                //判断i=1表示连续签到天数有对应的连续签到奖励,否则就按照单次签到奖励发放
                if (i == 1) {
                    //根据userId和连续签到天数获取奖励积分
                    int signCoin = this.getSignCoinByBrandIdAndGradeAndUserIdAndContinueDays(userId, continueDaysAdd);

                    try {
                        userCoinService.updateUserCoinByUserid(request, Long.parseLong(userId), signCoin,
                                UUID.randomUUID().toString().replace("-", ""), "0");
                    } catch (NumberFormatException e) {
                        LOG.info("更新用户积分有误======", e.getMessage());

                        return ResultWrap.init(CommonConstants.FALIED, "当前签到人数过多,请稍后重试!");
                    }

                    SignDetail signDetail = new SignDetail();

                    signDetail.setUserId(userId);
                    signDetail.setCoins(signCoin + "");
                    signDetail.setSignDate(date);

                    signInBusiness.createSignDetail(signDetail);

                    scbu.setContinueDays(continueDaysAdd);
                    scbu.setLastUpdateTime(
                            DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

                    signInBusiness.createSignCalc(scbu);

                    return ResultWrap.init(CommonConstants.SUCCESS, "签到成功!");

                } else {
                    //根据userId和单次签到天数获取对应的奖励积分
                    int signCoin = this.getSignCoinByBrandIdAndGradeAndUserIdAndContinueDays(userId, 1);

                    try {
                        userCoinService.updateUserCoinByUserid(request, Long.parseLong(userId), signCoin,
                                UUID.randomUUID().toString().replace("-", ""), "0");
                    } catch (NumberFormatException e) {
                        LOG.info("更新用户积分有误======", e.getMessage());

                        return ResultWrap.init(CommonConstants.FALIED, "当前签到人数过多,请稍后重试!");
                    }

                    SignDetail signDetail = new SignDetail();

                    signDetail.setUserId(userId);
                    signDetail.setCoins(signCoin + "");
                    signDetail.setSignDate(date);

                    signInBusiness.createSignDetail(signDetail);

                    scbu.setContinueDays(continueDaysAdd);
                    scbu.setLastUpdateTime(
                            DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

                    signInBusiness.createSignCalc(scbu);

                    return ResultWrap.init(CommonConstants.SUCCESS, "签到成功!");

                }

            }

        } else {

            return ResultWrap.init(CommonConstants.SUCCESS, "今日已签到");

        }

    }

    // 判断当天是否已签到
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/isdosign")
    public @ResponseBody
    Object isDoSign(HttpServletRequest request, @RequestParam(value = "userId") String userId) {

        User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));

        if ("1".equals(user.getRealnameStatus())) {
            //根据userId查询用户签到账户
            SignCalc signCalcByUserId = signInBusiness.getSignCalcByUserId(userId);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(new Date());

            if (signCalcByUserId != null) {
                //获取用户最近一次的签到时间
                String lastUpdateTime = signCalcByUserId.getLastUpdateTime();
                String ymd = lastUpdateTime.substring(0, 10);

                LOG.info("当天时间format======" + format);
                LOG.info("最后签到时间ymd======" + ymd);
                //判断用户最近一次的签到时间和当天时间是否相同
                if (!format.equals(ymd)) {

                    return ResultWrap.init(CommonConstants.SUCCESS, "今日未签到!", "0");
                } else {

                    return ResultWrap.init(CommonConstants.SUCCESS, "今日已签到!", "1");
                }
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "今日未签到!", "0");
            }

        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂未实名,无法使用签到功能!");
        }

    }

    // 查询已签到日期
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/getsign")
    public @ResponseBody
    Object getSign(HttpServletRequest request, @RequestParam(value = "userId") String userId) {

        User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));

        if ("1".equals(user.getRealnameStatus())) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());

            String year = format.substring(0, 4);
            String month = format.substring(4, 6);

            LOG.info("year======" + year);
            LOG.info("month======" + month);
            //获取本月的第一天
            String firstDayOfMonth = getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
            //获取下个月的第一天
            String firstDayOfNextMonth = getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month) + 1);

            LOG.info("firstDayOfMonth======" + firstDayOfMonth);
            LOG.info("firstDayOfNextMonth======" + firstDayOfNextMonth);

            List<String> list;
            try {
                list = signInBusiness.getSignDateByUserIdAndStartTimeAndEndTime(userId, firstDayOfMonth,
                        firstDayOfNextMonth);
            } catch (Exception e) {
                LOG.info("查询签到天数有误======", e.getMessage());
                return ResultWrap.init(CommonConstants.FALIED, "当前签到人数过多,请稍后重试!", "");
            }
            LOG.info("list======" + list);

            if (list != null && list.size() > 0) {

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", list);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "暂无签到数据", list);
            }

        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂未实名,无法使用签到功能!");
        }

    }

    // 根据年月查询签到记录的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/getsignby/yearandmonth")
    public @ResponseBody
    Object getSignByYearAndMonth(@RequestParam(value = "userId") String userId,
                                 @RequestParam(value = "year") int year, @RequestParam(value = "month") int month) {

        LOG.info("year======" + year);
        LOG.info("month======" + month);
        //获取前端传过来的月份的第一天
        String firstDayOfMonth = getFirstDayOfMonth(year, month);
        //获取前端传过来的月份的下个月的第一天
        String firstDayOfNextMonth = getFirstDayOfMonth(year, month + 1);

        LOG.info("firstDayOfMonth======" + firstDayOfMonth);
        LOG.info("firstDayOfNextMonth======" + firstDayOfNextMonth);

        List<String> list;
        try {
            list = signInBusiness.getSignDateByUserIdAndStartTimeAndEndTime(userId, firstDayOfMonth,
                    firstDayOfNextMonth);
        } catch (Exception e) {
            LOG.info("查询签到天数有误======", e.getMessage());
            return ResultWrap.init(CommonConstants.FALIED, "当前签到人数过多,请稍后重试!", "");
        }
        LOG.info("list======" + list);

        if (list != null && list.size() > 0) {

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", list);
        } else {

            return ResultWrap.init(CommonConstants.SUCCESS, "暂无签到数据", list);
        }

    }

    // 查询签到获得积分以及距离连续签到奖励天数的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/getsigncoin/andbonusdays")
    public @ResponseBody
    Object getSigncoinAndBonusDays(@RequestParam(value = "userId") String userId) {

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();

        SignCalc scbu = signInBusiness.getSignCalcByUserId(userId);

        if (scbu == null) {

            map.put("days", 0);
            map.put("bonuscoin", 0);
            return ResultWrap.init(CommonConstants.SUCCESS, "用户未使用签到功能!", map);
        }

        User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));

        if (user != null) {
            long brandId = user.getBrandId();
            String grade = user.getGrade();

            List<SignCoin> sc = signInBusiness.getSignCoinByBrandIdAndGrade(brandId + "", grade);

            if (sc != null && sc.size() > 0) {
                int j = 0;
                int k = 0;
                for (SignCoin scoin : sc) {
                    // 签到积分表对应的奖励天数与用户签到天数对比
                    k++;
                    if (scoin.getContinueDays() != scbu.getContinueDays()) {

                        int i = scoin.getContinueDays() - scbu.getContinueDays();
                        // 如果i<0 说明用户连续签到天数比当前循环的奖励天数多
                        if (i < 0) {
                            j++;
                            continue;
                        } else {

                            SignCommonCoin scc = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId + "", grade);

                            map.put("days", i);
                            map.put("bonuscoin", scc.getBonusCoin());
                            // 返回还有多少天
                            break;
                        }
                    } else {

                        if (scbu.getContinueDays() == 1) {

                            SignCommonCoin scc = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId + "", grade);

                            int i = sc.get(1).getContinueDays() - scbu.getContinueDays();
                            map.put("days", i);
                            map.put("bonuscoin", scc.getBonusCoin());
                            // 返回还有多少天
                            break;

                        } else {

                            SignCoin signCoin = signInBusiness.getSignCoinByBrandIdAndGradeAndContinueDays(brandId + "",
                                    grade, scbu.getContinueDays());

                            if (k < sc.size()) {
                                int i = sc.get(k).getContinueDays() - scbu.getContinueDays();
                                map.put("days", i);
                                map.put("bonuscoin", signCoin.getBonusCoin());
                                // 返回还有多少天
                                break;
                            } else {
                                int i = sc.get(sc.size() - 1).getContinueDays() - scbu.getContinueDays();
                                map.put("days", i);
                                map.put("bonuscoin", signCoin.getBonusCoin());
                                // 返回还有多少天
                                maps.put(CommonConstants.RESP_CODE, "000001");
                                maps.put(CommonConstants.RESULT, map);
                                maps.put(CommonConstants.RESP_MESSAGE, "您当月连续签到奖励已全部获得");

                                return maps;
                            }

                        }

                    }

                }

                if (j >= sc.size()) {

                    SignCommonCoin scc = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId + "", grade);

                    if (scc != null) {

                        map.put("days", 0);
                        map.put("bonuscoin", scc.getBonusCoin());

                        maps.put(CommonConstants.RESP_CODE, "000001");
                        maps.put(CommonConstants.RESULT, map);
                        maps.put(CommonConstants.RESP_MESSAGE, "您当月连续签到奖励已全部获得");

                        return maps;
                    } else {

                        return ResultWrap.init(CommonConstants.FALIED, "未配置该贴牌对应的签到奖励积分!");
                    }

                } else {

                    maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    maps.put(CommonConstants.RESULT, map);
                    maps.put(CommonConstants.RESP_MESSAGE, "获取积分和距离奖励天数");
                    return maps;
                }

            } else {

                return ResultWrap.init(CommonConstants.FALIED, "该贴牌未配置签到奖励积分!", sc);
            }

        } else {

            return ResultWrap.init(CommonConstants.FALIED, "无该用户数据!");
        }

    }

    // 添加连续签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/addsigncoin")
    public @ResponseBody
    Object addSignCoin(@RequestParam(value = "brandId") String brandId,
                       @RequestParam(value = "grade") String grade,
                       @RequestParam(value = "continueDays", required = false) int continueDays,
                       @RequestParam(value = "bonusCoin", required = false) String bonusCoin) {

        ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLeveDistributionBusiness
                .getThirdLevelByBrandidandgrade(Long.parseLong(brandId), Integer.parseInt(grade));

        SignCoin signCoinByBrandIdAndGrade = signInBusiness.getSignCoinByBrandIdAndGradeAndContinueDays(brandId, grade,
                continueDays);

        if (signCoinByBrandIdAndGrade == null) {

            SignCoin signCoin = new SignCoin();
            signCoin.setBrandId(brandId);
            signCoin.setGrade(grade);

            if (thirdLevelByBrandidandgrade == null) {
                signCoin.setGradeName("普通用户");
            } else {
                signCoin.setGradeName(thirdLevelByBrandidandgrade.getName());
            }
            signCoin.setContinueDays(continueDays);
            signCoin.setBonusCoin(bonusCoin);

            signInBusiness.createSignCoin(signCoin);

            return ResultWrap.init(CommonConstants.SUCCESS, "添加成功!");
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "当前品牌和等级对应的奖励积分已存在,请勿重复添加!");
        }

    }

    // 修改连续签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/updatesigncoin")
    public @ResponseBody
    Object updateSignCoin(@RequestParam(value = "brandId") String brandId,
                          @RequestParam(value = "grade") String grade,
                          @RequestParam(value = "continueDays", required = false, defaultValue = "-1") int continueDays,
                          @RequestParam(value = "bonusCoin", required = false, defaultValue = "-1") String bonusCoin) {

        SignCoin sc = signInBusiness.getSignCoinByBrandIdAndGradeAndContinueDays(brandId, grade, continueDays);
        try {
            if (continueDays != -1) {
                sc.setContinueDays(continueDays);
            }
            if (!"-1".equals(bonusCoin)) {
                sc.setBonusCoin(bonusCoin);
            }

            if ("0".equals(grade)) {
                sc.setGradeName("普通用户");
            } else {
                ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLeveDistributionBusiness.getThirdLevelByBrandidandgrade(Long.parseLong(brandId), Integer.parseInt(grade));
                sc.setGradeName(thirdLevelByBrandidandgrade.getName());
            }

            signInBusiness.createSignCoin(sc);

        } catch (Exception e) {
            LOG.error("修改签到对应积分有误======", e.getMessage());
            return ResultWrap.init(CommonConstants.FALIED, "修改失败!");
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
    }

    // 查询连续签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/getsigncoin")
    public @ResponseBody
    Object getSignCoin(@RequestParam(value = "brandId") String brandId,
                       @RequestParam(value = "grade", required = false, defaultValue = "-1") String grade) {

        if (!"-1".equals(grade)) {
            List<SignCoin> signCoinByBrandIdAndGrade = signInBusiness.getSignCoinByBrandIdAndGrade(brandId, grade);

            if (signCoinByBrandIdAndGrade != null && signCoinByBrandIdAndGrade.size() > 0) {

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", signCoinByBrandIdAndGrade);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", signCoinByBrandIdAndGrade);
            }

        } else {
            List<SignCoin> signCoinByBrandId = signInBusiness.getSignCoinByBrandId(brandId);

            List<SignCommonCoin> signCommonCoinByBrandId = signInBusiness.getSignCommonCoinByBrandId(brandId);
            if (signCommonCoinByBrandId.isEmpty()) {

                List<ThirdLevelDistribution> allThirdLevelPrd = thirdLeveDistributionBusiness.getAllThirdLevelPrd(Long.parseLong(brandId));

                for (ThirdLevelDistribution tl : allThirdLevelPrd) {

                    SignCommonCoin signCommonCoin = new SignCommonCoin();
                    signCommonCoin.setBrandId(brandId);
                    signCommonCoin.setGrade(tl.getGrade() + "");
                    signCommonCoin.setGradeName(tl.getName());
                    signCommonCoin.setBonusCoin("0");
                    signCommonCoin.setContinueDays(1);

                    signInBusiness.createSignCommonCoin(signCommonCoin);

                }

                SignCommonCoin signCommonCoin2 = new SignCommonCoin();
                signCommonCoin2.setBrandId(brandId);
                signCommonCoin2.setGrade("0");
                signCommonCoin2.setGradeName("普通用户");
                signCommonCoin2.setBonusCoin("0");
                signCommonCoin2.setContinueDays(1);

                signInBusiness.createSignCommonCoin(signCommonCoin2);

            }

            if (signCoinByBrandId != null && signCoinByBrandId.size() > 0) {

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", signCoinByBrandId);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", signCoinByBrandId);
            }

        }

    }

    // 删除连续签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/deletesigncoin")
    public @ResponseBody
    Object deleteSignCoin(
            @RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
            @RequestParam(value = "id") String Id) {

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

        List<SignCoin> signCoinByBrandIdAndId = signInBusiness.getSignCoinByBrandIdAndId(brandId, l);

        if (signCoinByBrandIdAndId != null && signCoinByBrandIdAndId.size() > 0) {

            for (SignCoin sc : signCoinByBrandIdAndId) {

                try {
                    signInBusiness.deleteSignCoin(sc);
                } catch (Exception e) {
                    LOG.error("删除签到对应积分有误======", e.getMessage());
                    continue;
                }

            }

            return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
        }

    }

    // 添加单日签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/addsigncommoncoin")
    public @ResponseBody
    Object addSignCommonCoin(@RequestParam(value = "brandId") String brandId,
                             @RequestParam(value = "grade") String grade,
                             @RequestParam(value = "commonDay", required = false, defaultValue = "1") int commonDay,
                             @RequestParam(value = "commonDaybonusCoin", required = false, defaultValue = "-1") String commonDaybonusCoin) {

        SignCommonCoin signCommonCoinByBrandIdAndGrade = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId,
                grade);

        ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLeveDistributionBusiness
                .getThirdLevelByBrandidandgrade(Long.parseLong(brandId), Integer.parseInt(grade));

        if (signCommonCoinByBrandIdAndGrade == null) {
            SignCommonCoin scc = new SignCommonCoin();
            scc.setBrandId(brandId);
            scc.setGrade(grade);

            if (thirdLevelByBrandidandgrade == null) {
                scc.setGradeName("普通用户");
            } else {
                scc.setGradeName(thirdLevelByBrandidandgrade.getName());
            }
            scc.setContinueDays(commonDay);
            scc.setBonusCoin(commonDaybonusCoin);

            signInBusiness.createSignCommonCoin(scc);

            return ResultWrap.init(CommonConstants.SUCCESS, "添加成功!");
        } else {
            signCommonCoinByBrandIdAndGrade.setBrandId(brandId);
            signCommonCoinByBrandIdAndGrade.setGrade(grade);

            if (thirdLevelByBrandidandgrade == null) {
                signCommonCoinByBrandIdAndGrade.setGradeName("普通用户");
            } else {
                signCommonCoinByBrandIdAndGrade.setGradeName(thirdLevelByBrandidandgrade.getName());
            }
            signCommonCoinByBrandIdAndGrade.setContinueDays(commonDay);
            signCommonCoinByBrandIdAndGrade.setBonusCoin(commonDaybonusCoin);

            signInBusiness.createSignCommonCoin(signCommonCoinByBrandIdAndGrade);

            return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
        }

    }

    // 根据等级查询单日签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/getsigncommoncoinby/grade")
    public @ResponseBody
    Object getSignCommonCoinByGrade(@RequestParam(value = "brandId") String brandId,
                                    @RequestParam(value = "grade", required = false, defaultValue = "-1") String grade) {

        List<SignCommonCoin> list = new ArrayList<SignCommonCoin>();
        if (!"-1".equals(grade)) {

            SignCommonCoin scc = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId, grade);

            if (scc != null) {

                list.add(scc);

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", list);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", list);
            }

        } else {

            List<SignCommonCoin> signCommonCoinByBrandId = signInBusiness.getSignCommonCoinByBrandId(brandId);

            if (signCommonCoinByBrandId != null && signCommonCoinByBrandId.size() > 0) {

                return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", signCommonCoinByBrandId);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", signCommonCoinByBrandId);
            }

        }

    }

    // 删除单日签到对应积分的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/signin/deletesigncommoncoin")
    public @ResponseBody
    Object deleteSignCommonCoin(
            @RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
            @RequestParam(value = "id") String Id) {

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

        List<SignCommonCoin> signCommonCoinByBrandIdAndId = signInBusiness.getSignCommonCoinByBrandIdAndId(brandId, l);

        if (signCommonCoinByBrandIdAndId != null && signCommonCoinByBrandIdAndId.size() > 0) {

            for (SignCommonCoin scc : signCommonCoinByBrandIdAndId) {

                try {
                    signInBusiness.deleteSignCommonCoin(scc);
                } catch (Exception e) {
                    LOG.error("删除签到对应积分有误======", e.getMessage());
                    continue;
                }

            }

            return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
        }

    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleClearLastMonthSignInData() throws Exception {
        if ("true".equals(scheduleTaskOnOff)) {
            clearLastMonthSignInData();
        }
    }

    // @Scheduled(cron = "0 54 10 22 * ?")
    // 每个月的第一天自动清零上个月的签到数据
    public void clearLastMonthSignInData() throws Exception {
        LOG.info("开始执行自动清零上个月的签到数据======");

        List<SignCalc> signCalcAll = signInBusiness.getSignCalcAll();

        SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd");
        String format = ymd.format(new Date());

        String year = format.substring(0, 4);
        String month = format.substring(4, 6);

        LOG.info("year======" + year);
        LOG.info("month======" + month);

        String firstDayOfMonth = getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (SignCalc sc : signCalcAll) {

            // Date date = new Date();
            try {
                Date lastUpdateTime = sdf.parse(sc.getLastUpdateTime());
                Date firstDay = sdf.parse(firstDayOfMonth);
                if (firstDay.before(lastUpdateTime)) {
                    sc.setContinueDays(1);

                    signInBusiness.createSignCalc(sc);
                } else {
                    sc.setContinueDays(0);

                    signInBusiness.createSignCalc(sc);

                }
            } catch (Exception e) {
                LOG.error("自动清零上个月签到数据有误======", e.getMessage());
                continue;
            }

        }

        LOG.info("执行自动清零上个月的签到数据完成======");

    }

    // 获取每个月第一天的方法
    public String getFirstDayOfMonth(int year, int month) {

        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最小天数
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String firstDayOfMonth = sdf.format(cal.getTime());

        return firstDayOfMonth;
    }

    ;

    // 获取每个月最后一天的方法
    public String getLastDayOfMonth(int year, int month) {

        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String lastDayOfMonth = sdf.format(cal.getTime());

        return lastDayOfMonth;
    }

    // 根据brandId、grade、userId、continueDays获取连续签到对应的积分
    public int getSignCoinByBrandIdAndGradeAndUserIdAndContinueDays(String userId, int continueDays) {

        User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));

        if (user != null) {
            long brandId = user.getBrandId();
            String grade = user.getGrade();

            int signCoin = 0;
            try {
                if (continueDays == 1) {
                    SignCommonCoin signCommonCoin = signInBusiness.getSignCommonCoinByBrandIdAndGrade(brandId + "",
                            grade);

                    String bonusCoin = signCommonCoin.getBonusCoin();

                    signCoin = Integer.parseInt(bonusCoin);
                } else {

                    signCoin = signInBusiness.getSignCoin(brandId + "", grade, continueDays);
                }

            } catch (Exception e) {
                LOG.info("", e.getMessage());

                return -1;
            }

            return signCoin;
        } else {

            return 0;
        }

    }

}
