package com.cardmanager.pro.consumerReplacement;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.scanner.RepaymentTaskScanner;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;


@Controller
public class RepaymentButton {

    @Autowired
    private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;

    @Autowired
    private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

    @Autowired
    private RepaymentTaskScanner repaymentTaskScanner;

    @Autowired
    private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;

    @Autowired
    private RestTemplate template;

    @Value("${paymentgateway.ip}")
    private String ip;

    @Autowired
    private CreditCardAccountBusiness creditCardAccountBusiness;

    private static String url = "http://user/v1.0/user/query/bankName";
    private static final Logger LOG = LoggerFactory.getLogger(RepaymentButton.class);


    //yeah ，这是一个注释
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/zp/repaymentbutton")
    @ResponseBody
    public Object repaymentbutton(@RequestParam(value = "repaymentId") String repaymentId,
                                  @RequestParam(value = "amount", required = false) String amount,
                                  @RequestParam(value = "version", required = false) String version) {
        Map<String, Object> map = new HashMap<>();
        //判定操作时间是否在交易时间内
        Date now = new Date();
        Calendar startLimitTime = Calendar.getInstance();
        Calendar endLimitTime = Calendar.getInstance();
        startLimitTime.set(Calendar.HOUR_OF_DAY, 7);
        startLimitTime.set(Calendar.MINUTE, 0);
        startLimitTime.set(Calendar.SECOND, 0);
        endLimitTime.set(Calendar.HOUR_OF_DAY, 23);
        endLimitTime.set(Calendar.MINUTE, 0);
        endLimitTime.set(Calendar.SECOND, 0);
        Date startTime = startLimitTime.getTime();
        Date endTime = endLimitTime.getTime();
        if (now.compareTo(startTime) < 0 || now.compareTo(endTime) > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "当前时间不可进行还款补单，请在每天7:00-23:00之间进行补单操作");
            return map;
        }

        String idcard;
        String phone;
        String user_id;
        RepaymentTaskPOJO repaymentTaskPOJO;
        String cardNo;
        //获取到用户的id
        if (!"".equals(repaymentId)) {
            repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentId);
            List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentId);
            version = repaymentTaskPOJO.getVersion();
            BigDecimal amount1 = BigDecimal.ZERO;
            BigDecimal amount2 = BigDecimal.ZERO;
            for (ConsumeTaskPOJO cu : consumeTaskPOJOs) {
                if (cu.getOrderStatus() == 1 && cu.getTaskStatus() == 1) {
                    amount1 = cu.getAmount();
                    amount2 = amount2.add(amount1);
                }
            }
            //对amount进行重新赋值，获取的是订单表里面的消费成功的订单号。
            amount = amount2.toString();
            cardNo = repaymentTaskPOJO.getCreditCardNumber();
            user_id = repaymentTaskPOJO.getUserId();
            MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
            requestEntity.add("card_no", cardNo);
            //跨服调用访问userBankInfo获取用户的身份证号
            String userBankInfo = template.postForObject(url, requestEntity, String.class);
            JSONObject jn = typeJSON(userBankInfo);
            //判断用户的银行卡是否可用并且获取用户的身份证号
            idcard = jn.getString("idcard");
            phone = jn.getString("phone");
            CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByCreditCardNumberAndVersion(cardNo, version);
            BigDecimal blance = creditCardAccount.getBlance();
            String blance1 = blance.toString();
            if (blance1.equals("0.00")) {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，您的本地余额为0,无法出款");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "操作失敗，参数为空，请联系客服处理");

        }
        CreditCardManagerConfig config = creditCardManagerConfigBusiness.findByVersion(version);
        if (null == config) {
            return ResultWrap.init(CommonConstants.FALIED, "操作失敗，您的当前渠道已关闭，请联系客服处理");
        }
        if (config.getScanOnOff() != 1) {
            return ResultWrap.init(CommonConstants.FALIED, "操作失敗，您的当前渠道的扫描未开启，请联系客服处理");
        }
        if (config.getRepaymentOnOff() != 1) {
            return ResultWrap.init(CommonConstants.FALIED, "操作失敗，您的当前渠道的还款未开启，请联系客服处理");
        }
        if (version.equals("14")) {
            String account14 = queryBlance14(idcard);
            LOG.info("身份证号是" + idcard + "的身份证下余额是" + account14 + "===============================");
            if (compare(account14, amount)) {
                LOG.info("进入14通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，您的账户余额不足该笔还款");
            }
        } else if (version.equals("15")) {
            String account15 = queryBlance15(idcard);
            LOG.info("身份证号是" + idcard + "的身份证下余额是" + account15 + "===============================");
            if (compare(account15, amount)) {
                LOG.info("进入15通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "余额不足或消费金额不是当日消费，补还款请联系客服");
                return map;

            }
        } else if (version.equals("18")) {
            String account18 = queryBlance18(idcard, cardNo);
            LOG.info("身份证号是" + idcard + "的身份证下余额是" + account18 + "===============================");
            if (compare(account18, amount)) {
                LOG.info("进入18通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {

                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "通道余额不足，补还款请联系客服");
                return map;

            }
        }else if(version.equals("49")){
            String account49 = queryBlance49(idcard, cardNo);
            LOG.info("身份证号是" + idcard + "的身份证下余额是" + account49 + "===============================");
            if (compare(account49, amount)) {
                LOG.info("进入49通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {

                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "通道余额不足，补还款请联系客服");
                return map;

            }

        } else if (version.equals("25") || version.equals("26")) {
            String account2526 = queryBlance2526(idcard);
            LOG.info("身份证号是" + idcard + "的身份证下余额是" + account2526 + "===============================");
            if (compare(account2526, amount)) {
                LOG.info("进入25.26通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "消费计划不是当日消费或通道余额不足，补还款请联系客服");
                return map;
            }
        } else if (version.equals("31")) {
            String account31 = queryBlance31(cardNo);
            LOG.info("卡号是" + cardNo + "的余额为" + account31 + "===============================");
            if (compare(account31, amount)) {
                LOG.info("进入31通道出款" + amount + "===============================");
                BigDecimal bigDecimal = new BigDecimal(amount);
                String account = bigDecimal.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，余额不足本次出款，请联系客服处理");
            }
        } else if (version.equals("12") || version.equals("13") || version.equals("37")) {
            String account12 = queryBlance12(idcard, cardNo);
            BigDecimal bigDecimal = new BigDecimal(account12);
            LOG.info("卡号是" + cardNo + "的余额为" + account12 + "===============================");
            if (compare(account12, amount)) {
                LOG.info("进入12通道出款" + amount + "===============================");
                BigDecimal bigDecimal1 = new BigDecimal(amount);
                String account = bigDecimal1.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，余额不足本次出款，请联系客服处理");
            }
        } else if (version.equals("35")) {
            String account35 = queryBalance35(idcard);
            BigDecimal bigDecimal = new BigDecimal(account35);
            LOG.info("卡号是" + cardNo + "的余额为" + account35 + "===============================");
            if (compare(account35, amount)) {
                LOG.info("进入35通道出款" + amount + "===============================");
                BigDecimal bigDecimal1 = new BigDecimal(amount);
                String account = bigDecimal1.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，余额不足本次出款，请联系客服处理");
            }
        } else if (version.equals("34")) {
            String account34 = queryBalance34(idcard, cardNo);
            BigDecimal bigDecimal = new BigDecimal(account34).divide(new BigDecimal("100"));
            LOG.info("卡号是" + cardNo + "的余额为" + account34 + "===============================");
            if (compare(account34, amount)) {
                LOG.info("进入34通道出款" + amount + "===============================");
                BigDecimal bigDecimal1 = new BigDecimal(amount);
                String account = bigDecimal1.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，余额不足本次出款，请联系客服处理");
            }
        } else if (version.equals("40") || version.equals("41") || version.equals("42") || version.equals("43")) {
            String account40 = queryBlance40(idcard);
            BigDecimal bigDecimal = new BigDecimal(account40).divide(new BigDecimal("100"));
            LOG.info("卡号是" + cardNo + "的余额为" + account40 + "===============================");
            if (compare(account40, amount)) {
                LOG.info("进入40通道出款" + amount + "===============================");
                BigDecimal bigDecimal1 = new BigDecimal(amount);
                String account = bigDecimal1.toString();
                LOG.info("修改blance金额为===========" + bigDecimal + "============");
                Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
                JSONObject rj = JSONObject.fromObject(ro);
                if (rj.getString("resp_code").equals("000000")) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
                    map.put(CommonConstants.RESULT, repaymentTaskPOJO);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
                    return map;
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "操作失敗，余额不足本次出款，请联系客服处理");
            }
        }
        //最后的判定
        else {
            return ResultWrap.init(CommonConstants.FALIED, "当前通道暂不支持补还款。");
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "操作成功，交易处理中");
    }


    //40,41,42,43通道的余额查询
    private String queryBlance40(String idCard) {
        String url40 = ip + "/v1.0/paymentgateway/repayment/jfdh/balanceQuery";
        MultiValueMap<String, Object> requestEntity40 = new LinkedMultiValueMap<>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity40.add("idCard", idCard);
        String blance40 = restTemplate.postForObject(url40, requestEntity40, String.class);
        JSONObject jn = typeJSON(blance40);
        String account40 = jn.getString("resp_message");
        account40 = account40.substring(account40.indexOf("为") + 2, account40.length());
        return account40;
    }

    //34通道的余额查询
    private String queryBalance34(String idCard, String bankCard) {
        String url34 = ip + "/v1.0/paymentgateway/topup/apdhx/balancequery";
        MultiValueMap<String, Object> requestEntity34 = new LinkedMultiValueMap<>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity34.add("idCard", idCard);
        requestEntity34.add("bankCard", bankCard);
        String blance34 = restTemplate.postForObject(url34, requestEntity34, String.class);
        JSONObject jn = typeJSON(blance34);
        String account34 = jn.getString("resp_message");
        return account34;
    }

    //35通道的余额查询
    private String queryBalance35(String idCard) {
        MultiValueMap<String, Object> requestEntity35 = new LinkedMultiValueMap<>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity35.add("idCard", idCard);
        String url35 = ip + "/v1.0/paymentgateway/repayment/hchk/balanceQuery";
        String blance35 = restTemplate.postForObject(url35, requestEntity35, String.class);
        JSONObject jn = typeJSON(blance35);
        String account35 = jn.getString("resp_message");
        account35 = account35.substring(account35.indexOf("为") + 1, account35.length());
        return account35;
    }

    private String queryBlance31(String cardNo) {
        MultiValueMap<String, Object> requestEntity31 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity31.add("bankCard", cardNo);
        String url31 = ip + "/v1.0/paymentgateway/topup/hxdhx/balancequery1";
        String blance31 = restTemplate.postForObject(url31, requestEntity31, String.class);
        JSONObject jn31 = typeJSON(blance31);
        String account31 = jn31.getString("balance").toString();
        return account31;
    }

    private String queryBlance18(String idcard, String cardNo) {
        MultiValueMap<String, Object> requestEntity18 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity18.add("idCard", idcard);
        requestEntity18.add("bankCard", cardNo);
        String url18 = ip + "/v1.0/paymentgateway/topup/hqx/balanceQuery";
        String blance18 = restTemplate.postForObject(url18, requestEntity18, String.class);
        JSONObject jn18 = typeJSON(blance18);
        String account18 = jn18.getString("resp_message").toString();
        account18 = account18.split("可用余额")[1];
        return account18;
    }

    private String queryBlance49(String idcard, String cardNo) {
        MultiValueMap<String, Object> requestEntity18 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity18.add("idCard", idcard);
        requestEntity18.add("bankCard", cardNo);
        String url18 = ip + "/v1.0/paymentgateway/topup/hqt/balanceQuery";
        String blance18 = restTemplate.postForObject(url18, requestEntity18, String.class);
        JSONObject jn18 = typeJSON(blance18);
        String account18 = jn18.getString("resp_message").toString();
        account18 = account18.split("可用余额")[1];
        return account18;
    }


    //25.26通道余额查询
    public String queryBlance2526(String idcard) {
        MultiValueMap<String, Object> requestEntit2526 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntit2526.add("idCard", idcard);
        String url2526 = ip + "/v1.0/paymentgateway/topup/cjhk/walletQuery";
        String blance2526 = restTemplate.postForObject(url2526, requestEntit2526, String.class);
        JSONObject jn2526 = typeJSON(blance2526);
        String resulet = jn2526.getString("resp_message");
        String result = resulet.substring(resulet.indexOf(":") + 1, resulet.length());
        JSONObject jsonObject = JSONObject.fromObject(result);
        String account2526 = jsonObject.getString("quickpayD0Balance");
        return account2526;
    }

    //33通道的余额查询
    public String queryBlance33(String bankCard) {
        MultiValueMap<String, Object> requestEntit33 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntit33.add("bankCard", bankCard);
        String url33 = ip + "/v1.0/paymentgateway/topup/hxdhx2/balancequery";
        String blance33 = restTemplate.postForObject(url33, requestEntit33, String.class);
        JSONObject jn33 = typeJSON(blance33);
        String account = jn33.getString("resp_message");
        String account33 = account.substring(account.indexOf("为") + 1, account.length());
        return account33;
    }

    //15通道余额查询
    public String queryBlance15(String idcard) {
        MultiValueMap<String, Object> requestEntit15 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntit15.add("idCard", idcard);
        String url15 = ip + "/v1.0/paymentgateway/topup/cjx/walletQuery";
        String blance15 = restTemplate.postForObject(url15, requestEntit15, String.class);
        JSONObject jn15 = typeJSON(blance15);
        String resulet = jn15.getString("resp_message");
        String result = resulet.substring(resulet.indexOf("分") + 1, resulet.length());
        JSONObject jsonObject = JSONObject.fromObject(result);
        String account15 = jsonObject.getString("quickpayD0Balance");
        LOG.info("当前账户余额============" + account15);
        return account15;
    }

    //12、13通道余额查询
    private String queryBlance12(String idcard, String cardNo) {
        MultiValueMap<String, Object> requestEntity12 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity12.add("idCard", idcard);
        requestEntity12.add("bankCard", cardNo);
        String url12 = ip + "/v1.0/paymentgateway/topup/hqnew/balanceQuery";
        String blance12 = restTemplate.postForObject(url12, requestEntity12, String.class);
        JSONObject jn12 = typeJSON(blance12);
        String account12 = com.alibaba.fastjson.JSONObject.parseObject(jn12.getString("result")).get("currAccountBalance").toString();
        return account12;
    }

    //14通道余额查询
    public String queryBlance14(String idcard) {
        MultiValueMap<String, Object> requestEntity14 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntity14.add("idCard", idcard);
        String url14 = ip + "/v1.0/paymentgateway/topup/hqnew/balanceQuery";
        String blance14 = restTemplate.postForObject(url14, requestEntity14, String.class);
        JSONObject jn14 = typeJSON(blance14);
        String account14 = jn14.getString("result");
        return account14;
    }

    //36.40.41通道的余额查询
    public String queryBlance36(String bankCard) {
        MultiValueMap<String, Object> requestEntit35 = new LinkedMultiValueMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        requestEntit35.add("bankCard", bankCard);
        String url33 = ip + "/v1.0/paymentgateway/repayment/hxdhd/balanceQuery";
        String blance33 = restTemplate.postForObject(url33, requestEntit35, String.class);
        JSONObject jn33 = typeJSON(blance33);
        String account = jn33.getString("resp_message");
        String account33 = account.substring(account.indexOf("为") + 1, account.length());
        return account33;
    }


    //

    //类型转化
    public JSONObject typeJSON(String string) {
        JSONObject object = new JSONObject();
        JSONObject jn = object.fromObject(string);
        return jn;
    }

    //判断大小
    public boolean compare(String blance, String amount) {
        boolean a = new BigDecimal(blance).compareTo(new BigDecimal(amount)) == 1;
        return a;
    }

    //生成订单号
    public String getOrderIdByTime() {
        String order = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHmmssSSS") + "1";
        LOG.info("生成订单号===============================" + order);
        return order;
    }


    //修改blance
    public CreditCardAccount updateBlance(CreditCardAccount creditCardAccount, String account) {
        creditCardAccount.setBlance(new BigDecimal(account));
        return creditCardAccountBusiness.save(creditCardAccount);

    }

    //出款
    public Object paragraph(String amount, String user_id, String cardNo, String version, RepaymentTaskPOJO repaymentTaskPOJO) {
        Map<String, Object> map = new HashMap<>();

        LOG.info("进入还款补单自动出款" + amount + "===============================");
        BigDecimal bigDecimal = new BigDecimal(amount);
        String account = bigDecimal.toString();
        LOG.info("修改blance金额为===========" + bigDecimal + "============");
        Object ro = repaymentTaskScanner.clearAccountByUserIdAndCreditCardNumber(null, user_id, cardNo, account, null, version);
        JSONObject rj = JSONObject.fromObject(ro);
        if (rj.getString("resp_code").equals("000000")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
            map.put(CommonConstants.RESULT, repaymentTaskPOJO);
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
            return map;
        }
    }


    /*
     *@description:
     *@author: ives
     *@annotation:"根据Version去获取通道配置对象"
     *@data:2019年9月16日  18:00:32
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/zp/selectversion")
    @ResponseBody
    public Object selectVersionConfig(@RequestParam(value = "version") String version) {
        CreditCardManagerConfig config = creditCardManagerConfigBusiness.findByVersion(version);
        return config;
    }


}
