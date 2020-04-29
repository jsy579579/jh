package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.Md5Util;
import cn.jh.common.utils.TokenUtil;
import com.jh.user.business.BalanceTransfeRrecordbusiness;
import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.BalanceTransfeRrecord;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserManageHistory;
import com.jh.user.util.Util;
import com.jh.user.vo.UserBalanceVo;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;


@Controller
@EnableAutoConfiguration
public class UserBalanceService {

    private static final Logger LOG = LoggerFactory.getLogger(UserBalanceService.class);

    @Autowired
    private UserBalanceBusiness userBalBusiness;

    @Autowired
    BalanceTransfeRrecordbusiness balanceTransfeRrecordbusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private UserJpushService userJpushService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    Util util;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 获取用户当前账户信息
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/account/query/{token}")
    public @ResponseBody
    Object queryUserAccount(HttpServletRequest request,
                            @PathVariable("token") String token) {

        Map<String, Object> map = new HashMap<>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userBalBusiness.queryUserAccountByUserid(userId));
        return map;

    }

    /**
     * 获取用户当前账户信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/query/userId")
    public @ResponseBody
    Object queryUserAccountByUserId(HttpServletRequest request,
                                    @RequestParam(value = "user_id") long userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(userId);
        if (userAccount == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无该账户");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, userAccount);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
        }
        return map;

    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/balance/transfers")
    @Transactional
    public @ResponseBody
    Object UsertransfersTransfers(@RequestParam(value = "recipient") String recipient,
                                  @RequestParam(value = "assignor") String assignor,
                                  @RequestParam(value = "smscode",defaultValue = "1") String smscode,
                                  @RequestParam(value = "amount") String amount,
                                  @RequestParam(value = "paypasswd") String paypasswd) {
        Map<String, Object> map = new HashMap<String, Object>();
        String s = redisTemplate.opsForValue().get(assignor);
        LOG.info("================================缓存验证码"+s);
        if(!smscode.equals(s)){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "验证码不正确");
            return map;
        }
        User recipients = userLoginRegisterBusiness.findByPhone(recipient);
        User assignors = userLoginRegisterBusiness.findByPhone(assignor);
        if (recipients == null || assignors == null || recipients.getId() < 0 || assignors.getId() < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "改手机号在本系统中不存在");
            return map;
        }else if (!assignors.getPassword().equals(Md5Util.getMD5(paypasswd))||"".equals(assignors.getPassword())){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");
            return map;
        }
        //转让人
        UserAccount assignorUserAccount = userBalBusiness.lockUserAccount(assignors.getId());
        //接收人
        UserAccount recipientUserAccount = userBalBusiness.lockUserAccount(recipients.getId());
        //交易限制
        BigDecimal bigDecimal = balanceTransfeRrecordbusiness.queryAmountCount(assignorUserAccount.getUserId(), new Date(), new Date());
        //发起人对象判空
        if (assignorUserAccount == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无该账户");
            return map;
        } else if (recipientUserAccount == null) {//接收人对象判空
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无收款人账户");
            return map;
        } else if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "输入金额错误");
            return map;
        } else if (assignorUserAccount.getBalance().doubleValue() < Double.valueOf(amount)) {//判断转让人余额是否充足
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户余额不足");
            return map;
        }
        //单笔限额判断
        if (new BigDecimal(amount).doubleValue() >= 20000) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "单笔提现金额超限");
            return map;
        }
//        //单日限额判断
//        if (bigDecimal.doubleValue() >= 20000) {
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, "单日提现金额超限");
//            return map;
//        }
        //交易发起
        int i = userBalBusiness.userTransferAndReceiveBalance(assignorUserAccount.getUserId(),
                recipientUserAccount.getUserId(),
                new BigDecimal(amount).setScale(2, BigDecimal.ROUND_DOWN));
        if (i < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "未知错误请联系管理员");
            return map;
        }
        //订单记录
        balanceTransfeRrecordbusiness.TrackRecord(new BalanceTransfeRrecord(new BigDecimal(amount),
                new Date(),
                new BigDecimal("1"),
                recipientUserAccount.getUserId(),
                assignorUserAccount.getUserId()));
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "转让成功");
        return map;

    }


    /**
     * 获取用户当前账户信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/query/phone")
    public @ResponseBody
    Object queryUserAccountByPhone(HttpServletRequest request,
                                   @RequestParam(value = "phone") String phone,
                                   @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandid
    ) {

        Map<String, Object> map = new HashMap<String, Object>();
        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
        long userId = user.getId();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userBalBusiness.queryUserAccountByUserid(userId));
        return map;

    }


    /**
     * 锁定当前用户的账户信息
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/account/lock/{token}")
    public @ResponseBody
    Object lockUserAccount(HttpServletRequest request,
                           @PathVariable("token") String token) {
        Map<String, Object> map = new HashMap<>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }


        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userBalBusiness.lockUserAccount(userId));
        return map;
    }


    /**
     * 将用户的钱冻结到冻结账户里面
     * 默认的
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/freeze")
    public @ResponseBody
    Object freeUserAccount(HttpServletRequest request,
                           @RequestParam(value = "user_id") long userId,
                           @RequestParam(value = "amount") BigDecimal amount,
                           @RequestParam(value = "add_or_sub", required = false, defaultValue = "0") String addorsub,
                           @RequestParam(value = "order_code", required = false) String ordercode
    ) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        try {
            map.put(CommonConstants.RESULT, userBalBusiness.freezeUserAccount(userId, amount, addorsub, ordercode));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            return ResultWrap.err(LOG, CommonConstants.FALIED, "更新用户帐户异常!");
        }
        return map;

    }


    /**
     * 将用户的分润提现到用户的余额中间去
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/rebate/update")
    public @ResponseBody
    Object updateUserRebateAccount(HttpServletRequest request,
                                   @RequestParam(value = "user_id") long userId,
                                   @RequestParam(value = "amount", defaultValue = "0", required = false) BigDecimal amount,
                                   @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub,
                                   @RequestParam(value = "order_code", required = false) String ordercode
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        try {
            map.put(CommonConstants.RESULT, userBalBusiness.updateUserRebateAccount(userId, amount, ordercode));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrap.err(LOG, CommonConstants.FALIED, "更新用户帐户异常!");
        }
        return map;


    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/rebate/unfreeze")
    public @ResponseBody
    Object unfreezeUserbateAccount(HttpServletRequest request,
                                   @RequestParam(value = "user_id") long userId,
                                   @RequestParam(value = "amount") BigDecimal amount,
                                   @RequestParam(value = "add_or_sub", required = false, defaultValue = "0") String addorsub,
                                   @RequestParam(value = "order_code", required = false) String ordercode
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        try {
            map.put(CommonConstants.RESULT, userBalBusiness.freezeUserRebateAccount(userId, amount, addorsub, ordercode));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            return ResultWrap.err(LOG, CommonConstants.FALIED, "更新用户帐户异常!");
        }
        return map;
    }


    /**
     * 分润提现, 如果满足需求先将钱直接冻结到帐户里面
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/rebate/freeze")
    public @ResponseBody
    Object freezeAccountRebateBalance(HttpServletRequest request,
                                      @RequestParam(value = "user_id") long userId,
                                      @RequestParam(value = "realamount") String realamount,
                                      @RequestParam(value = "order_code") String ordercode
    ) {

        UserAccount userAcct = userBalBusiness.rebateFreezeAccount(userId, new BigDecimal(realamount), ordercode);
        Map<String, Object> map = new HashMap<>();

        /**提现失败 */
        if (userAcct != null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, userAcct);

        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "余额不足");

        }


        return map;


    }


    /**
     * 提现请求, 如果满足需求先把钱冻结到账户里面
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/withdraw/freeze")
    public @ResponseBody
    Object freezeAccountBalance(HttpServletRequest request,
                                @RequestParam(value = "user_id") long userId,
                                @RequestParam(value = "realamount") String realamount,
                                @RequestParam(value = "order_code") String ordercode
    ) {

        UserAccount userAcct = userBalBusiness.withdrawFreeAccount(userId, new BigDecimal(realamount), ordercode);
        Map<String, Object> map = new HashMap<>();

        /**提现失败 */
        if (userAcct != null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, userAcct);

        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "余额不足");

        }


        return map;

    }


    /**
     * 更新用户的账户信息 2019.5.31 更新接口名   ruanjiajun
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/update")
    public @ResponseBody
    Object updateUserAccount(HttpServletRequest request,
                             @RequestParam(value = "user_id") long userId,
                             @RequestParam(value = "amount", defaultValue = "0", required = false) BigDecimal amount,
                             @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub,
                             @RequestParam(value = "order_code", required = false) String ordercode
    ) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        try {
            map.put(CommonConstants.RESULT, userBalBusiness.updateUserAccount(userId, amount, addorsub, ordercode));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "更改用户帐户异常!");
            return map;
        }
        if (addorsub.equals("0")) {
            /**
             * 推送消息
             * /v1.0/user/jpush/tset
             * */
            String alert = "余额充值";
            String content = "亲爱的会员，您的账户到账" + amount.setScale(2, BigDecimal.ROUND_DOWN) + "元";
            String btype = "balanceadd";
            String btypeval = "";
            try {
                userJpushService.setJpushtest(request, userId, alert, content, btype, btypeval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;


    }


    /**
     * 查询交易变更历史
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/balance/query/{token}")
    public @ResponseBody
    Object pageBalanceQuery(HttpServletRequest request,
                            @PathVariable("token") String token,
                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                            @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                            @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                            @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
    ) {

        Map<String, Object> map = new HashMap<>();
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
        map.put(CommonConstants.RESULT, userBalBusiness.queryUserBalHistoryByUserid(userId, pageable));
        return map;
    }

    /**
     * 查询交易收益
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/balance/profit/{token}")
    public @ResponseBody
    Object pageBalanceQuery(HttpServletRequest request,
                            @PathVariable("token") String token
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
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, userBalBusiness.findSumUserBalByUserId(userId));

        return map;
    }

    /**
     * 更新用户分润余额         2019.6.1更新接口名  ruanjiajun
     *
     * @param request
     * @param userId
     * @param rebateAmount
     * @param orderCode
     * @param addorsub
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/rebate/updatebyrebateamount")
    public @ResponseBody
    Object updateUserRebateAccountByAmount(HttpServletRequest request,
                                           long userId,
                                           BigDecimal rebateAmount,
                                           String orderCode,
                                           @RequestParam(value = "addorsub", defaultValue = "0", required = false) String addorsub
    ) {
        try {
            userBalBusiness.updateUserRebate(userId, rebateAmount, "0", addorsub, orderCode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrap.err(LOG, CommonConstants.FALIED, "更新用户帐户异常!");
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "执行成功!");
    }


    /*
     *@description:更新管理奖
     *@author: ives
     *@annotation:"这个是用户管理奖的更新接口！！！"
     *@data:2019年9月10日 09:30:32
     */
    public boolean updateUserManageByuserId(UserManageHistory userManageHistory) {
        if (null != userManageHistory) {
            Long userId = userManageHistory.getPreUserId();
            BigDecimal amount = userManageHistory.getAmount();
            String status = userManageHistory.getStatus();
            UserAccount userAccount = userBalBusiness.updateUserManageByuserId(userId, amount, status);
            return true;
        }
        return false;
    }

    /*
     *@description:查询用户当月剩余管理奖接口
     *@author: ives
     *@annotation:"反正写了这个功能，前端太菜，也排不到档期。"
     *@data:2019年9月10日 09:30:32
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/rebate/selectUserManage")
    public @ResponseBody
    Object selectUserManage(@RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "brandId") String brandId,
                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                            @RequestParam(value = "size", defaultValue = "20", required = false) int size
    ) {
        Map<String, Object> maps = new HashMap();
        if ("".equals(brandId) || null == brandId) {
            return ResultWrap.err(LOG, CommonConstants.FALIED, "参数为空!");
        }
        if ("".equals(phone) || null == phone) {
            Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
            List<UserAccount> userAccounts = userBalBusiness.findManageByBrandId(brandId, pageable);
            maps.put("resp_message", "查询成功");
            maps.put("resp_code", "000000");
            maps.put("result", userAccounts);
            return maps;
        } else {
            UserAccount userAccount = userBalBusiness.findByBrandIdAndPhone(brandId, phone);
            maps.put("resp_message", "查询成功");
            maps.put("resp_code", "000000");
            maps.put("result", userAccount);
            return maps;
        }
    }

    /**
     * 根据贴牌查询用户获取分润总额及当前分润
     *
     * @param brandIds
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/balance/queryUserTotalRebate")
    @ResponseBody
    public Object queryUserTotalRebate(
            @RequestParam(value = "brand_id") String brandIds,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "20", required = false) int size
    ) {
        Map map = new HashMap<>();
        Long brandId = Long.valueOf(brandIds);
        //查询用户分润总额url
        String profitUrl = "http://transactionclear/v1.0/transactionclear/record/query/userid";
        //查询用户返佣总额url
        String disUrl = "http://transactionclear/v1.0/transactionclear/query/distributionSum/byPhone";
        List<UserBalanceVo> userBalanceVos = new ArrayList<>();
        if (phone != null && !"".equals(phone)) {
            User user = userLoginRegisterBusiness.findByPhone(phone);
            if (user == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在或未实名");
                return map;
            }
            UserBalanceVo userBalanceVo = new UserBalanceVo();
            userBalanceVo.setUserName(user.getFullname());
            userBalanceVo.setPhone(user.getPhone());
            //用户当前分润余额总和
            UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(user.getId());
            BigDecimal curBalance = userAccount.getBalance().add(userAccount.getRebateBalance());
            userBalanceVo.setCurBalance(curBalance);
            //获取总分润金额
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("acq_user_id", Long.toString(user.getId()));
            String profitResult = restTemplate.postForObject(profitUrl, requestEntity, String.class);
            JSONObject rebateJson = JSONObject.fromObject(profitResult);
            String rebateSum = rebateJson.getString("result");
            userBalanceVo.setTotalRebate(new BigDecimal(rebateSum));
            //获取总返佣金额
            MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
            requestEntity1.add("phone", user.getPhone());
            String disResult = restTemplate.postForObject(disUrl, requestEntity1, String.class);
            JSONObject disJson = JSONObject.fromObject(disResult);
            String disSum = disJson.getString("result");
            userBalanceVo.setTotalDistribution(new BigDecimal(disSum));
            //总收益
            userBalanceVo.setTotalProfit(userBalanceVo.getTotalRebate().add(userBalanceVo.getTotalDistribution()));
            //已提取收益
            userBalanceVo.setSpendBalance(userBalanceVo.getTotalProfit().subtract(userBalanceVo.getCurBalance()));
            userBalanceVos.add(userBalanceVo);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, userBalanceVos);
            return map;
        } else {
            Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.ASC, "id"));
            List<User> users = userLoginRegisterBusiness.queryUserByBrandIdAndRealname(brandId, "1", pageable);
            BigDecimal userNumber = userLoginRegisterBusiness.findBrandNumByBrandidAndRealAuth(brandId);
            LOG.info("当前贴牌实名人数==============" + userNumber);
            for (User user : users) {
                UserBalanceVo userBalanceVo = new UserBalanceVo();
                userBalanceVo.setUserName(user.getFullname());
                userBalanceVo.setPhone(user.getPhone());
                //用户当前分润余额总和
                UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(user.getId());
                BigDecimal curBalance = userAccount.getBalance().add(userAccount.getRebateBalance());
                userBalanceVo.setCurBalance(curBalance);
                //获取总分润金额
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("acq_user_id", Long.toString(user.getId()));
                String profitResult = restTemplate.postForObject(profitUrl, requestEntity, String.class);
                JSONObject rebateJson = JSONObject.fromObject(profitResult);
                String rebateSum = rebateJson.getString("result");
                userBalanceVo.setTotalRebate(new BigDecimal(rebateSum));
                //获取总返佣金额
                MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
                requestEntity1.add("phone", user.getPhone());
                String disResult = restTemplate.postForObject(disUrl, requestEntity1, String.class);
                JSONObject disJson = JSONObject.fromObject(disResult);
                String disSum = disJson.getString("result");
                userBalanceVo.setTotalDistribution(new BigDecimal(disSum));
                //总收益
                userBalanceVo.setTotalProfit(userBalanceVo.getTotalRebate().add(userBalanceVo.getTotalDistribution()));
                //已提取收益
                userBalanceVo.setSpendBalance(userBalanceVo.getTotalProfit().subtract(userBalanceVo.getCurBalance()));
                userBalanceVos.add(userBalanceVo);
            }
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, userBalanceVos);
            map.put("totalElements", userNumber);
            map.put("totalPages", userNumber.divide(new BigDecimal(size)).setScale(0, BigDecimal.ROUND_DOWN).subtract(new BigDecimal("1")));
            return map;
        }
    }

    /**
     * 根据贴牌号查询贴牌总收益，当前总余额，已提现余额
     *
     * @param brandIds
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/balance/queryBrandTotalRebate")
    @ResponseBody
    public Object queryBrandSumProfitByBrandId(
            @RequestParam(value = "brand_id") String brandIds) {
        Map map = new HashMap<>();
        UserBalanceVo userBalanceVo = new UserBalanceVo();
        Long brandId = Long.parseLong(brandIds);
        //分润总额查询地址
        String profitUrl = "/v1.0/transactionclear/transaction/queryProfitByBrandIdAndTradeTime";
        //获取总分润金额
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandIds);
        String profitResult = restTemplate.postForObject(profitUrl, requestEntity, String.class);
        JSONObject rebateJson = JSONObject.fromObject(profitResult);
        String rebateSum = rebateJson.getString("result");
        userBalanceVo.setTotalRebate(new BigDecimal(rebateSum));
        //返佣总额查询地址
        String disUrl = "http://transactionclear/v1.0/transactionclear/query/distributionSum/byPhone";
        //获取总返佣金额
//        MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
//        requestEntity1.add("phone", user.getPhone());
//        String disResult = restTemplate.postForObject(disUrl, requestEntity1, String.class);
//        JSONObject disJson=JSONObject.fromObject(disResult);
//        String disSum=disJson.getString("result");
//        userBalanceVo.setTotalDistribution(new BigDecimal(disSum));
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/account/Credit/transfers")
    public @ResponseBody
    Object UsertransfersTransfers(@RequestParam(value = "recipient") String recipient,
                                  @RequestParam(value = "assignor") String assignor,
                                  @RequestParam(value = "credit") String credit,
                                  @RequestParam(value = "paypasswd") String paypasswd) {
        Map<String, Object> map = new HashMap<String, Object>();
        User recipients = userLoginRegisterBusiness.findByPhone(recipient);
        User assignors = userLoginRegisterBusiness.findByPhone(assignor);
        if (recipients == null || assignors == null || recipients.getId() < 0 || assignors.getId() < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "改手机号在本系统中不存在");
            return map;
        }else if (!assignors.getPaypass().equals(Md5Util.getMD5(paypasswd))||"".equals(assignors.getPaypass())){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");
            return map;
        }
        //转让人
        UserAccount assignorUserAccount = userBalBusiness.lockUserAccount(assignors.getId());
        //接收人
        UserAccount recipientUserAccount = userBalBusiness.lockUserAccount(recipients.getId());
        //发起人对象判空
        if (assignorUserAccount == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无该账户");
            return map;
        } else if (recipientUserAccount == null) {//接收人对象判空
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "无接收人账户");
            return map;
        } else if (Tools.checkAmount(credit) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "输入积分错误");
            return map;
        } else if (assignorUserAccount.getCoinNew().doubleValue() < Double.valueOf(credit)) {//判断转让人积分是否充足
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户积分不足");
            return map;
        }

        //交易发起
        int i = userBalBusiness.userTransferAndReceiveCredit(assignorUserAccount.getUserId(),
                recipientUserAccount.getUserId(),
                new BigDecimal(credit).setScale(2, BigDecimal.ROUND_DOWN));
        if (i < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "未知错误请联系管理员");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "转让成功");
        return map;

    }
}
