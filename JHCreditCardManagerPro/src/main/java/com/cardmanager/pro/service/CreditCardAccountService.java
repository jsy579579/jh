package com.cardmanager.pro.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.jh.common.tools.ResultWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardAccountHistoryBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardAccountHistory;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.scanner.RepaymentTaskScanner;
import com.cardmanager.pro.util.RestTemplateUtil;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Controller
@EnableAutoConfiguration
public class CreditCardAccountService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplateUtil util;

    @Autowired
    private CreditCardAccountBusiness creditCardAccountBusiness;

    @Autowired
    private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

    @Autowired
    private CreditCardAccountHistoryBusiness creditCardAccountHistoryBusiness;

    @Autowired
    private CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RepaymentTaskScanner repaymentTaskScanner;

    @Autowired
    JdbcTemplate jdbcTemplate;


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/keke18/update")
    public @ResponseBody
    Object getAll(@RequestParam(value = "Tips", required = false, defaultValue = "系统自动") String Tips, @RequestParam("oldtime") String oldtime,
                  @RequestParam(value = "nowTime", required = false) String nowTime,
                  @RequestParam(value = "version", required = false) String version) {
        int oldtimeInt = oldtime.trim().length();
        int nowTimeInt = nowTime.trim().length();
        if (nowTime != null && !nowTime.equals("")) {
            nowTime = nowTime;
        } else {
            Date t = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nowTime = df.format(t);
        }
        if (oldtimeInt > 20 && nowTimeInt > 20) {
            return "时间格式不正确";
        }
        List<RepaymentTaskPOJO> list = repaymentTaskPOJOBusiness.findAllStatus2(Tips, oldtime, nowTime, version);
        LOG.info("共有" + list.size() + "笔");
        BigDecimal bigDecimal = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_UP);
        int zero = 0;
        int change = 0;
        int count = 0;
        String url = "http://user/v1.0/user/query/bankName";
        for (RepaymentTaskPOJO repaymentTaskPOJO : list) {
            String bankCard = repaymentTaskPOJO.getCreditCardNumber();
            LOG.info("查询的银行卡号为：" + bankCard);
            MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
            requestEntity.add("card_no", bankCard);
            //跨服调用访问userBankInfo获取用户的身份证号
            String userBankInfo = null;
            try {
                userBankInfo = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                LOG.info("查询userBankInfo失败，详情：" + e.getMessage());
                continue;
            }
            JSONObject jn = typeJSON(userBankInfo);
            //判断用户的银行卡是否可用并且获取用户的身份证号
            if (jn.getString("respcode").equals("999999")) {
                LOG.info("未查到银行卡信息");
                continue;
            }
            String idcard = jn.getString("idcard");
            JSONObject userInfo = this.getUserInfo18(bankCard, idcard);
            String balance = userInfo.getString("balance");
            BigDecimal Basics = new BigDecimal(20).setScale(2);
            BigDecimal ReceiveBalance = new BigDecimal(balance).setScale(2);

            if (balance != null && Basics.compareTo(ReceiveBalance) == 1) {
                zero++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(bigDecimal);
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("余额为" + ReceiveBalance);
            } else {
                change++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(ReceiveBalance.subtract(BigDecimal.valueOf(2).setScale(2)));
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("要修改的余额为" + balance);
            }
            count++;
            LOG.info("执行了" + count + "笔");
            repaymentTaskPOJO.setTaskStatus(7);
            repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
            LOG.info("此次共获取" + list.size() + "要修改的数据,设置余额为0的有" + zero + ",修改通道余额的有" + change + "条。");
        }
        return "";
    }


    // 查询version  Userid bankCard
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/keke12/rest")
    public @ResponseBody
    Object test(@RequestParam Map map) {
        String order_code = "'" + map.get("order_code") + "'";
        String sql = "select * from t_repayment_task  where order_code =  " + order_code;
        Map<String, Object> hqxMap = jdbcTemplate.queryForMap(sql);
        return hqxMap;
    }

    // 查询version  Userid bankCard
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/keke12/upd")
    public @ResponseBody
    Object upd(@RequestParam Map map) {
        System.out.println("upd" + map);
        String bigRealAmount = map.get("bigRealAmount").toString();
        String user_id = map.get("user_id").toString();
        String credit_card_number = map.get("credit_card_number").toString();
        String version = map.get("version").toString();
        String sql = " update t_credit_card_account set blance=? where user_id=?  and  credit_card_number = ? and version = ? ";
        int update = jdbcTemplate.update(sql, new Object[]{bigRealAmount, user_id, credit_card_number, version});
        Map hqxMap = new HashMap();
        hqxMap.put("code", update);
        System.out.println("hqxMap==>" + hqxMap);
        return hqxMap;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/keke12/update")
    public @ResponseBody
    Object gethqnew(@RequestParam(value = "Tips", required = false, defaultValue = "系统自动") String Tips, @RequestParam("oldtime") String oldtime,
                    @RequestParam(value = "nowTime", required = false) String nowTime,
                    @RequestParam(value = "version", required = false) String version) {
        int oldtimeInt = oldtime.trim().length();
        int nowTimeInt = nowTime.trim().length();
        if (nowTime != null && !nowTime.equals("")) {
            nowTime = nowTime;
        } else {
            Date t = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nowTime = df.format(t);
        }
        if (oldtimeInt > 20 && nowTimeInt > 20) {
            return "时间格式不正确";
        }
        List<RepaymentTaskPOJO> list = repaymentTaskPOJOBusiness.findAllStatus2(Tips, oldtime, nowTime, version);
        LOG.info("共有" + list.size() + "笔");
        BigDecimal bigDecimal = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_UP);
        int zero = 0;
        int change = 0;
        int count = 0;
        String url = "http://user/v1.0/user/query/bankName";
        for (RepaymentTaskPOJO repaymentTaskPOJO : list) {
            String bankCard = repaymentTaskPOJO.getCreditCardNumber();
            LOG.info("查询的银行卡号为：" + bankCard);
            MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
            requestEntity.add("card_no", bankCard);
            //跨服调用访问userBankInfo获取用户的身份证号
            String userBankInfo = null;
            try {
                userBankInfo = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                LOG.info("查询userBankInfo失败，详情：" + e.getMessage());
                continue;
            }
            JSONObject jn = typeJSON(userBankInfo);
            //判断用户的银行卡是否可用并且获取用户的身份证号
            if (jn.getString("respcode").equals("999999")) {
                LOG.info("未查到银行卡信息");
                continue;
            }
            String idcard = jn.getString("idcard");
            JSONObject userInfo = this.getUserInfo12(bankCard, idcard);
            String balance = userInfo.getString("balance");
            BigDecimal Basics = new BigDecimal(20).setScale(2);
            BigDecimal ReceiveBalance = new BigDecimal(balance).setScale(2);

            if (balance != null && Basics.compareTo(ReceiveBalance) == 1) {
                zero++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(bigDecimal);
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("余额为" + ReceiveBalance);
            } else {
                change++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(ReceiveBalance.subtract(BigDecimal.valueOf(2).setScale(2)));
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("要修改的余额为" + balance);
            }
            count++;
            LOG.info("执行了" + count + "笔");
            repaymentTaskPOJO.setTaskStatus(7);
            repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
            LOG.info("此次共获取" + list.size() + "要修改的数据,设置余额为0的有" + zero + ",修改通道余额的有" + change + "条。");
        }
        return "";
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/keke31/update")
    public @ResponseBody
    Object gethx31(@RequestParam(value = "Tips", required = false, defaultValue = "系统自动") String Tips, @RequestParam("oldtime") String oldtime,
                   @RequestParam(value = "nowTime", required = false) String nowTime,
                   @RequestParam(value = "version", required = false) String version) {
        int oldtimeInt = oldtime.trim().length();
        int nowTimeInt = nowTime.trim().length();
        if (nowTime != null && !nowTime.equals("")) {
            nowTime = nowTime;
        } else {
            Date t = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nowTime = df.format(t);
        }
        if (oldtimeInt > 20 && nowTimeInt > 20) {
            return "时间格式不正确";
        }
        List<RepaymentTaskPOJO> list = repaymentTaskPOJOBusiness.findAllStatus2(Tips, oldtime, nowTime, "31");
        LOG.info("共有" + list.size() + "笔");
        BigDecimal bigDecimal = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_UP);
        int zero = 0;
        int change = 0;
        int count = 0;
        String url = "http://user/v1.0/user/query/bankName";
        for (RepaymentTaskPOJO repaymentTaskPOJO : list) {
            String bankCard = repaymentTaskPOJO.getCreditCardNumber();
          /*  LOG.info("查询的银行卡号为：" + bankCard);
            MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
            requestEntity.add("card_no", bankCard);
            //跨服调用访问userBankInfo获取用户的身份证号
            String userBankInfo = null;
            try {
                userBankInfo = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                LOG.info("查询userBankInfo失败，详情：" + e.getMessage());
                continue;
            }
            JSONObject jn = typeJSON(userBankInfo);
            //判断用户的银行卡是否可用并且获取用户的身份证号
            if (jn.getString("respcode").equals("999999")) {
                LOG.info("未查到银行卡信息");
                continue;
            }
            String idcard = jn.getString("idcard");*/
            JSONObject userInfo = this.getUserInfo31(bankCard);
            String balance = userInfo.getString("balance");
            BigDecimal Basics = new BigDecimal(20).setScale(2);
            BigDecimal ReceiveBalance = new BigDecimal(balance).setScale(2);

            if (balance != null && Basics.compareTo(ReceiveBalance) == 1) {
                zero++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(bigDecimal);
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("余额为" + ReceiveBalance);
            } else {
                change++;
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfo(bankCard);
                creditCardAccount.setBlance(ReceiveBalance.subtract(BigDecimal.valueOf(2).setScale(2)));
                creditCardAccountBusiness.save(creditCardAccount);
                LOG.info("要修改的余额为" + balance);
            }
            count++;
            LOG.info("执行了" + count + "笔");
            repaymentTaskPOJO.setTaskStatus(7);
            repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
            LOG.info("此次共获取" + list.size() + "要修改的数据,设置余额为0的有" + zero + ",修改通道余额的有" + change + "条。");
        }

        return "";
    }

//	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/tangJFHK32/update")
//	public @ResponseBody Object getJFHKAll(@RequestParam(value = "Tips",required = false,defaultValue = "系统自动") String Tips,@RequestParam("oldtime") String oldtime,
//										   @RequestParam(value = "nowTime",required = false)String nowTime){
//		Tips=Tips.trim();
//		int oldtimeInt=oldtime.trim().length();
//		int nowTimeInt=nowTime.trim().length();
//		if (nowTime != null && !nowTime.equals("")) {
//			nowTime=nowTime;
//		}else {
//			Date t = new Date();
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			nowTime = df.format(t);
//		}
//		if (oldtimeInt>20 && nowTimeInt>20 ) {
//			return "时间格式不正确";
//		}
//		List<RepaymentTaskPOJO> list= repaymentTaskPOJOBusiness.findAllStatus2AndJFHK(Tips,oldtime,nowTime);
//		BigDecimal bigDecimal=BigDecimal.ZERO.setScale(2,BigDecimal.ROUND_UP);
//		LOG.info("共有"+list.size()+"笔");
//		int zero=0;
//		int change=0;
//		int count=0;
//		for (RepaymentTaskPOJO repaymentTaskPOJO:list) {
//			String bankCard = repaymentTaskPOJO.getCreditCardNumber();
//			JSONObject userInfo = this.getBalanceInfo(bankCard);
//			String balance = userInfo.getString("balance");
//			BigDecimal Basics = new BigDecimal(2000).setScale(2);
//			BigDecimal ReceiveBalance = new BigDecimal(balance).setScale(2);
//
//			if (balance != null && Basics.compareTo(ReceiveBalance)== 1) {
//				zero++;
//				CreditCardAccount creditCardAccount = creditCardAccountBusiness.getChangeInfoJFHK(bankCard);
//				creditCardAccount.setBlance(bigDecimal);
//				creditCardAccountBusiness.save(creditCardAccount);
//				LOG.info("余额为"+ReceiveBalance);
//			}else {
//				change++;
//				CreditCardAccount creditCardAccount=creditCardAccountBusiness.getChangeInfoJFHK(bankCard);
//				creditCardAccount.setBlance(ReceiveBalance.subtract(BigDecimal.valueOf(1).setScale(2)));
//				creditCardAccountBusiness.save(creditCardAccount);
//				LOG.info("要修改的余额为"+balance);
//			}
//			count++;
//			LOG.info("执行了"+count+"笔");
//			repaymentTaskPOJO.setTaskStatus(7);
//			repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
//			LOG.info("此次共获取"+list.size()+"要修改的数据,设置余额为0的有"+zero+",修改通道余额的有"+change+"条。");
//		}
//		return null;
//	}

    public JSONObject getUserInfo(String bankCard) throws RuntimeException {
        String url = "http://paymentgateway/v1.0/paymentgateway/topup/tang/queryBalance";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("bankCard", bankCard);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }

    private JSONObject getUserInfo31(String bankCard) {
        String url = "http://paymentgateway/v1.0/paymentgateway/topup/hxdhx/balancequery1";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("bankCard", bankCard);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("查询余额出现错误，详情：", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;
    }

    public JSONObject getUserInfo18(String bankCard, String idCard) throws RuntimeException {
        String url = "http://paymentgateway/v1.0/paymentgateway/topup/hqx/balanceQuery1";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("bankCard", bankCard);
        requestEntity.add("idCard", idCard);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("查询余额出现错误，详情：", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }

    public JSONObject getUserInfo12(String bankCard, String idCard) throws RuntimeException {
        String url = "http://paymentgateway/v1.0/paymentgateway/topup/hqnew/balanceQuery1";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("bankCard", bankCard);
        requestEntity.add("idCard", idCard);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }

    public JSONObject getBalanceInfo(String bankCard) throws RuntimeException {
        String url = "http://paymentgateway/v1.0/paymentgateway/topup/tang/queryJFHKBalance";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("bankCard", bankCard);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }


    //类型转化
    public JSONObject typeJSON(String string) {
        JSONObject object = new JSONObject();
        JSONObject jn = object.fromObject(string);
        return jn;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/set/creditcardaccount")
    public @ResponseBody
    Object setCreditCardAccount(HttpServletRequest request,
                                @RequestParam("userId") String userId,
                                @RequestParam("creditCardNumber") String creditCardNumber,
                                @RequestParam(value = "creditBlance", required = false, defaultValue = "") String creditBlanceStr,
                                @RequestParam(value = "billDate", required = false, defaultValue = "") String billDateStr,
                                @RequestParam(value = "repaymentDate", required = false, defaultValue = "") String repaymentDateStr
    ) {
        Map<String, Object> map = new HashMap<>();
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        creditBlanceStr = creditBlanceStr.trim();
        billDateStr = billDateStr.trim();
        repaymentDateStr = repaymentDateStr.trim();

        BigDecimal creditBlance = null;
        int billDate = 0;
        int repaymentDate = 0;

        if (creditBlanceStr != null && !"".equals(creditBlanceStr)) {
            Map<String, Object> verifyMoneyMap = creditCardManagerAuthorizationHandle.verifyMoney(creditBlanceStr, 2, BigDecimal.ROUND_HALF_UP);
            if (!CommonConstants.SUCCESS.equals(verifyMoneyMap.get(CommonConstants.RESP_CODE))) {
                return verifyMoneyMap;
            }
            creditBlance = (BigDecimal) verifyMoneyMap.get(CommonConstants.RESULT);
        }

        if (billDateStr != null && !"".equals(billDateStr)) {
            try {
                billDate = Integer.valueOf(billDateStr);
            } catch (NumberFormatException e) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
                return map;
            }
            if (!(billDate > 0 && billDate < 32)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
                return map;
            }
        }

        if (repaymentDateStr != null && !"".equals(repaymentDateStr)) {
            try {
                repaymentDate = Integer.valueOf(repaymentDateStr);
            } catch (Exception e) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
                return map;
            }
            if (!(repaymentDate > 0 && repaymentDate < 32)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
                return map;
            }
        }

        List<CreditCardAccount> rreditCardAccounts = creditCardAccountBusiness.findByCreditCardNumber(creditCardNumber);
        for (CreditCardAccount creditCardAccount : rreditCardAccounts) {
            creditCardAccount.setCreditBlance(creditBlance != null ? creditBlance : creditCardAccount.getCreditBlance());
            creditCardAccount.setBillDate(0 != billDate ? billDate : creditCardAccount.getBillDate());
            creditCardAccount.setRepaymentDate(0 != repaymentDate ? repaymentDate : creditCardAccount.getRepaymentDate());
            creditCardAccount = creditCardAccountBusiness.save(creditCardAccount);
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "设置成功");
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/get/creditcardaccount")
    public @ResponseBody
    Object getCreditCardAccountByUserIdAndCreditCardNumber(HttpServletRequest request,
                                                           @RequestParam("userId") String userId,
                                                           @RequestParam("creditCardNumber") String creditCardNumber,
                                                           @RequestParam(value = "version", required = false, defaultValue = "1") String version
    ) {
        Map<String, Object> map = new HashMap<>();
        CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        map.put(CommonConstants.RESULT, creditCardAccount);
        return map;


    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/set/freezeblance/by/ordercode")
    public @ResponseBody
    Object setFreezeBlanceByOrderCode(HttpServletRequest request,
                                      @RequestParam(value = "orderCode") String orderCode,
                                      @RequestParam(value = "version", required = false, defaultValue = "1") String version
    ) {
        Map<String, Object> map = new HashMap<>();
        RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByOrderCode(orderCode);
        if (repaymentTaskPOJO != null && repaymentTaskPOJO.getOrderStatus() != 1 && repaymentTaskPOJO.getTaskStatus() != 2) {
            repaymentTaskPOJO.setOrderStatus(0);
            repaymentTaskPOJO.setTaskStatus(2);
            List<CreditCardAccountHistory> creditCardAccountHistory = creditCardAccountHistoryBusiness.findByTaskId(repaymentTaskPOJO.getRepaymentTaskId());
            creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getRepaymentTaskId(), 3, creditCardAccountHistory.get(0).getAmount(), "任务执行失败,增加余额", version, repaymentTaskPOJO.getCreateTime());
            repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
            repaymentTaskPOJO.setDescription("还款失败!");
            repaymentTaskPOJO.setReturnMessage("还款失败,原因未知,等待系统自动出款!");
            repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改成功!");
        map.put(CommonConstants.RESULT, repaymentTaskPOJO);
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/open/withdraw/account")
    public @ResponseBody
    Object openWithdrawAccount(HttpServletRequest request,
                               @RequestParam(value = "creditCardNumber") String creditCardNumber,
                               @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "brandId") String brandId,
                               @RequestParam(value = "amount") String amountStr,
                               @RequestParam(value = "version", required = false, defaultValue = "1") String version
    ) {
        Map<String, Object> map = new HashMap<>();
        CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByCreditCardNumberAndVersion(creditCardNumber, version);
        if (creditCardAccount == null) {
            LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", phone);
            requestEntity.add("brandid", brandId);
            Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("user", "/1.0/user/query/phonebrand", requestEntity);
            JSONObject resultJSONObject = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);

            String userId = resultJSONObject.getString("id");

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("type", "0");
            requestEntity.add("cardNo", creditCardNumber);
            restTemplateDoPost = util.restTemplateDoPost("user", "/v1.0/user/bank/default/cardno", requestEntity);
            resultJSONObject = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
            if (resultJSONObject == null || !resultJSONObject.containsKey("id")) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无该充值卡");
                return map;
            }

            Date nowTime = new Date();
            creditCardAccount = new CreditCardAccount();
            creditCardAccount.setUserId(userId);
            creditCardAccount.setCreditCardNumber(creditCardNumber);
            creditCardAccount.setPhone(phone);
            creditCardAccount.setLastUpdateTime(nowTime);
            creditCardAccount.setCreateTime(nowTime);
        }
        BigDecimal amount = new BigDecimal(amountStr);
        if (BigDecimal.ZERO.compareTo(amount) >= 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "提现金额有误!");
            return map;
        }
        creditCardAccount.setBlance(creditCardAccount.getBlance().add(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
        creditCardAccount = creditCardAccountBusiness.save(creditCardAccount);
        map = (Map<String, Object>) repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(request, creditCardAccount.getUserId(), creditCardNumber, null, "0", version);
        return map;
    }

    @RequestMapping("/v1.0/creditcardmanager/creditCardAccount/paymentOut")
    @ResponseBody
    public synchronized Object paymentOut(@RequestBody(required = false) CreditCardAccount creditCardAccount) throws InterruptedException {
        List<CreditCardAccount> all = creditCardAccountBusiness.findAll(creditCardAccount);
        RestTemplate restTemplate = new RestTemplate();
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        String result;
        JSONObject amountJSON;
        int i = 0;
        for (CreditCardAccount cardAccount : all) {
            requestEntity.clear();
            i++;
            URI uri = util.getServiceUrl("user", "error url request!");
            requestEntity.add("userId",cardAccount.getUserId());
            String idCard = restTemplate.postForObject(uri.toString() + "/v1.0/user/query/idcard/userid", requestEntity, String.class);
            if (idCard == null) {
                continue;
            }
            if(cardAccount.getVersion().equals("53")){
                requestEntity.clear();
                requestEntity.add("idCard",idCard);
                result = restTemplate.postForObject("http://shanqi111.cn/v1.0/paymentgateway/repayment/ldx/balanceQuery", requestEntity, String.class);
                amountJSON = JSONObject.fromObject(result);
                LOG.info("查询余额返会参数：{}",result);
                if("000000".equals(amountJSON.getString("resp_code"))){
                    String amounts = amountJSON.getString("resp_message");
                    amounts = amounts.substring(5);
                    // 查询用户卡余额
                    BigDecimal amount = new BigDecimal(amounts);
                    if(amount.compareTo(BigDecimal.valueOf(BigDecimal.ROUND_HALF_DOWN)) == 1){
                        cardAccount.setBlance(amount.subtract(BigDecimal.valueOf(1)));
                    }
                    // 保存
                    creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                }
            }else if(cardAccount.getVersion().equals("18")){
                try {
                    requestEntity.clear();
                    requestEntity.add("idCard",idCard);
                    requestEntity.add("bankCard",cardAccount.getCreditCardNumber());
                    result = restTemplate.postForObject("http://shanqi111.cn/v1.0/paymentgateway/topup/hqx/balanceQuery1", requestEntity, String.class);
                    LOG.info("查询余额返会参数：{}",result);
                    amountJSON = JSONObject.fromObject(result);
                    if(!"".equals(amountJSON.getString("balance"))){
                        String amounts = amountJSON.getString("balance");
                        // 查询用户卡余额
                        BigDecimal amount = new BigDecimal(amounts);
                        if(amount.compareTo(BigDecimal.valueOf(BigDecimal.ROUND_HALF_DOWN)) == 1){
                            cardAccount.setBlance(amount.subtract(BigDecimal.valueOf(1)));
                        }
                        // 保存
                        creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                    }
                } catch (RestClientException e) {
                    continue;
                }
            }else if(cardAccount.getVersion().equals("49")){
                requestEntity.clear();
                requestEntity.add("idCard",idCard);
                requestEntity.add("bankCard",cardAccount.getCreditCardNumber());
                result = restTemplate.postForObject("http://shanqi111.cn/v1.0/paymentgateway/topup/hqt/balanceQuery", requestEntity, String.class);
                LOG.info("查询余额返会参数：{}",result);
                amountJSON = JSONObject.fromObject(result);
                if("000000".equals(amountJSON.getString("resp_code"))){
                    String amounts = amountJSON.getString("resp_message");
                    String[] yue = amounts.split("余额");
                    amounts = yue[2];
                    BigDecimal amount = new BigDecimal(amounts);
                    if(amount.compareTo(BigDecimal.valueOf(BigDecimal.ROUND_HALF_DOWN)) == 1){
                        cardAccount.setBlance(amount.subtract(BigDecimal.valueOf(1)));
                    }
                    // 保存
                    creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                }
            }else if(cardAccount.getVersion().equals("33")){
                requestEntity.clear();
                requestEntity.add("idCard",cardAccount.getCreditCardNumber());
                result = restTemplate.postForObject("http://shanqi111.cn/v1.0/paymentgateway/topup/hxdhx/balancequery", requestEntity, String.class);
                LOG.info("查询余额返会参数：{}",result);
                amountJSON = JSONObject.fromObject(result);
                if("000000".equals(amountJSON.getString("resp_code"))){
                    String amounts = amountJSON.getString("resp_message");
                    BigDecimal amount = new BigDecimal(amounts);
                    if(amount.compareTo(BigDecimal.valueOf(BigDecimal.ROUND_HALF_DOWN)) == 1){
                        cardAccount.setBlance(amount.subtract(BigDecimal.valueOf(1)));
                    }
                    // 保存
                    creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                }
            }

            requestEntity.clear();
            requestEntity.add("creditCardNumber", cardAccount.getCreditCardNumber());
            requestEntity.add("userId", cardAccount.getUserId());
            requestEntity.add("version", cardAccount.getVersion());
            LOG.info("第{}条执行成功：{}",i,cardAccount);
            requestEntity.clear();
        }



        return ResultWrap.init(CommonConstants.SUCCESS, "执行成功！一共" + all.size() + "笔");
    }


}
