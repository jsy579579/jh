package com.jh.paymentgateway.controller;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.CityCodeBusiness;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.tl.*;
import com.jh.paymentgateway.util.tl.AesTool;
import com.jh.paymentgateway.util.tl.CertUtil;
import com.jh.paymentgateway.util.tl.HttpUtil;
import com.jh.paymentgateway.util.tl.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class TLDHXQuickpageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(TLDHXQuickpageRequest.class);

    private final static String MD5KEY = "z0jHmVqnHZB90Biq4QVYDCug";

    private final static String AESKEY = "z0jHmVqnHZB90Biq4QVYDCug";

    private final static String ALIAS = "shbyt";

    private final static String KEYSTOREPASSWORD = "0zZbD6";

    private final static String ALIASPASSWORD = "0zZbD6";

    private final static String COMID = "10000059";

    //private final static String KEYSTOREPATH = "D:/10000059.pfx";
    private final static String KEYSTOREPATH = "/product/deploy/tl/10000059.pfx";

    //private final static String PATH = "D:/10000059.cer";
    private final static String PATH = "/product/deploy/tl/10000059.cer";
    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CityCodeBusiness cityCodeBusiness;

    // 跟还款对接的接口
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/dockingEntrance")
    public @ResponseBody
    Object docking1(@RequestParam(value = "bankCard") String bankCard,
                    @RequestParam(value = "idCard") String idCard,
                    @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "userName") String userName,
                    @RequestParam(value = "extraFee") String extraFee,
                    @RequestParam(value = "rate") String rate,
                    @RequestParam(value = "securityCode") String securityCode,
                    @RequestParam(value = "expiredTime") String expired) throws Exception {


        Map<String,Object> map = new HashMap<>();
        TLDHXRegister register = topupPayChannelBusiness.getTLDHXRegisterByIdCard(idCard);
        TLDHXBindCard bindCard = topupPayChannelBusiness.getTLDHXBindCardByBankCard(bankCard);
        Map<String, Object> maps = new HashMap<>();

        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        //进件
        if (register==null){
            maps = (Map<String, Object>) this.tlToRegister(bankCard,idCard,phone,userName,rate,extraFee);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }else if(!register.getRate().equals(rate1) || !register.getExtraFee().equals(extraFee)){
                //修改末端渠道费率
            maps = (Map<String, Object>)this.UpdateUser(register.getMerchantCode(),register.getBankCard(),register.getPhone(),register.getUserName(),
                    rate1, extraFee, register.getIdCard());
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }
        //绑卡
        if(bindCard==null){
            maps = (Map<String, Object>)this.tlToBindCard(bankCard,idCard,phone,userName,securityCode,expired);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"进件绑卡成功");
        return map;
    }

    /**
     * 进件
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/tldhxRegister")
    public @ResponseBody Object tlToRegister(@RequestParam(value = "bankCard") String bankCard,
                                             @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                                             @RequestParam(value = "userName") String userName, @RequestParam(value = "rate") String rate,
                                             @RequestParam(value = "extraFee") String extraFee) {
        LOG.info("通联开始进件======================");
        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        Map<String, Object> map = new HashMap<>();

        String actionName = "CREATE_USER";

        Map<String, String> actionInfo = new HashMap();
        actionInfo.put("COM_ID", COMID);
        actionInfo.put("BANK_CARD_ID", AesTool.encrypt(bankCard, AESKEY));
        actionInfo.put("CARD_ID", AesTool.encrypt(idCard, AESKEY));
        actionInfo.put("MOBILE", AesTool.encrypt(phone, AESKEY));
        actionInfo.put("ACCOUNT_NAME", userName);
        actionInfo.put("RATE", rate1);
        actionInfo.put("SINGLE_FEE", extraFee);
        actionInfo.put("NONCE_STR", UUID.randomUUID().toString().replace("-", "").toString());//32位随机字符串
        actionInfo.put("SIGN", Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(actionInfo)),MD5KEY));
        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", actionInfo);

        LOG.info("通联进件的请求参数为："+jsonReq.toJSONString());

        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/merchantServer", jsonReq.toJSONString());
        LOG.info("通联进件的响应参数为："+ resp);
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")){
            LOG.info("进件成功======================");
            Map<String, Object> respMessage = (Map)JSON.parse(respMap.get("ACTION_INFO").toString());

            TLDHXRegister tldhxRegister = new TLDHXRegister();
            tldhxRegister.setBankCard(bankCard);
            tldhxRegister.setCreateTime(new Date());
            tldhxRegister.setExtraFee(extraFee);
            tldhxRegister.setIdCard(idCard);
            tldhxRegister.setPhone(phone);
            tldhxRegister.setRate(rate);
            tldhxRegister.setStatus("1");
            tldhxRegister.setUserName(userName);
            tldhxRegister.setMerchantCode(respMessage.get("USER_INFO_ID").toString());
            topupPayChannelBusiness.createTLDHXRegister(tldhxRegister);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "进件成功"); // 描述
            return map;
        }else{
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, respMap.get("MESSAGE")); // 描述
            return map;
        }
    }
    /**
     * 修改末端渠道费率
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/tldhxUpdateUser")
    public @ResponseBody Object UpdateUser(@RequestParam(value = "merchantCode") String merchantCode,
                                           @RequestParam(value = "bankCode") String bankCode,
                                           @RequestParam(value = "phone") String phone,
                                           @RequestParam(value = "username") String username,
                                           @RequestParam(value = "rate") String rate,
                                           @RequestParam(value = "extraFee") String extraFee,
                                           @RequestParam(value = "idCard") String idCard
                                           ) {

        LOG.info("通联开始修改末端渠道费率======================");
        Map<String, Object> map = new HashMap<>();
        String actionName = "UPDATE_USER";
        Map<String, String> actionInfo = new HashMap();
        actionInfo.put("COM_ID", COMID);
        actionInfo.put("USER_INFO_ID", merchantCode);  //末端渠道号
        actionInfo.put("BANK_CARD_ID", AesTool.encrypt(bankCode, AESKEY));
        actionInfo.put("MOBILE", AesTool.encrypt(phone, AESKEY));
        actionInfo.put("ACCOUNT_NAME", username);
        actionInfo.put("RATE", rate);
        actionInfo.put("SINGLE_FEE", extraFee);
        actionInfo.put("NONCE_STR",UUID.randomUUID().toString().replace("-", "").toString());//32位随机字符串
        actionInfo.put("SIGN", Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(actionInfo)),MD5KEY));

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", actionInfo);

        LOG.info("修改末端用户费率的请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/merchantServer", jsonReq.toJSONString());
        LOG.info("修改末端用户费率的响应参数为："+ resp);

        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")) {
            LOG.info("修改末端用户费率成功======================");

            Map<String, Object> respMessage = (Map)JSON.parse(respMap.get("ACTION_INFO").toString());

            TLDHXRegister tldhxRegister = topupPayChannelBusiness.getTLDHXRegisterByIdCard(idCard);
            tldhxRegister.setExtraFee(extraFee);
            tldhxRegister.setRate(rate);
            tldhxRegister.setMerchantCode(respMessage.get("USER_INFO_ID").toString());

            topupPayChannelBusiness.createTLDHXRegister(tldhxRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "进件成功"); // 描述
            return map;
        }else{
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, respMap.get("MESSAGE")); // 描述
            return map;
        }
    }

//    public static void main(String[] args) {
//        String str = "{\"ACTION_INFO\":{\"USER_INFO_ID\":\"10055173\",\"COM_ID\":\"10000059\",\"NONCE_STR\":\"kk4kot7ivpzpoy6v04idtuv52uogkwci\",\"SIGN\":\"854BBF53C79DB93007174567D3865721\"},\"ACTION_RETURN_CODE\":\"000000\",\"ACTION_NAME\":\"UPDATE_USER\"}";
//        Map<String, Object> respMap = (Map)JSON.parse(str);
//        System.out.println(respMap.get("ACTION_RETURN_CODE").equals("000000"));
//        Map<String, Object> respMessage = (Map)JSON.parse(respMap.get("ACTION_INFO").toString());
//        System.out.println(respMessage.get("USER_INFO_ID").toString());
//       // Map<String, Object> respMessage = (Map)JSON.parse(respMap.get("ACTION_INFO"));
//    }
    // 绑卡接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/bindCard")
    public Object tlToBindCard(@RequestParam(value = "bankCard") String bankCard,
                               @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "userName") String userName,
                               @RequestParam(value = "securityCode") String securityCode,
                               @RequestParam(value = "expiredTime") String expiredTime) {
        LOG.info("通联开始绑卡==================");
        Map<String, Object> map = new HashMap<>();

        String actionName = "BIND_CARD_API";

        Map<String, String> actionInfo = new HashMap();
        actionInfo.put("COM_ID", COMID);
        actionInfo.put("CARD_NO", AesTool.encrypt(bankCard, AESKEY)); //卡号
        actionInfo.put("ACCOUNT_NAME", userName); //户名
        actionInfo.put("ID_CARD", AesTool.encrypt(idCard, AESKEY));
        actionInfo.put("MOBILE", AesTool.encrypt(phone, AESKEY));
        actionInfo.put("CVN2", AesTool.encrypt(securityCode, AESKEY)); //安全码
        actionInfo.put("EXPIRED", expiredTime);//有效期
        actionInfo.put("NAME", userName);
        actionInfo.put("CARD_TYPE", "1"); //卡类型1、贷记卡，2、借记卡
        actionInfo.put("ACCT_PPE", "0"); //是否对公户，0：否、1：是
        actionInfo.put("NONCE_STR",UUID.randomUUID().toString().replace("-", "").toString());
        actionInfo.put("SIGN",Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(actionInfo)),MD5KEY));
        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_INFO", actionInfo);
        jsonReq.put("ACTION_NAME", actionName);

        LOG.info("通联绑卡请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/merchantServer", jsonReq.toJSONString());
        LOG.info("通联绑卡响应参数为："+ resp );
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000") || respMap.get("ACTION_RETURN_CODE").equals("100000")){
            LOG.info("通联绑卡成功======================");

            TLDHXBindCard tldhxBindCard = new TLDHXBindCard();
            tldhxBindCard.setBankCard(bankCard);
            tldhxBindCard.setIdCard(idCard);
            tldhxBindCard.setUserName(userName);
            tldhxBindCard.setCreateTime(new Date());
            tldhxBindCard.setPhone(phone);
            tldhxBindCard.setExpiredTime(expiredTime);
            tldhxBindCard.setSecurityCode(securityCode);
            tldhxBindCard.setStatus("1");
            topupPayChannelBusiness.createTLDHXBingCard(tldhxBindCard);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "进件成功"); // 描述
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, respMap.get("MESSAGE")); // 描述
            return map;
        }
    }
    //通联余额查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/balancequery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) throws Exception {
        TLDHXRegister tldhxRegister = topupPayChannelBusiness.getTLDHXRegisterByIdCard(idCard);
        String actionName = "QUERY_USER_INFO";
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, String> json = new HashMap();
        json.put("COM_ID", COMID);
        json.put("USER_INFO_ID", tldhxRegister.getMerchantCode());
        json.put("NONCE_STR", UUID.randomUUID().toString().replace("-","").toString());
        json.put("SIGN", Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(json)),MD5KEY));

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", json);

        LOG.info("终端渠道查询请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/merchantServer", jsonReq.toJSONString());
        LOG.info("终端渠道查询响应参数为："+ resp);
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, respMap.get("ACTION_INFO"));
        }else{
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, respMap.get("MESSAGE"));
        }
        return maps;
    }
    //消费接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/Precreate")
    public @ResponseBody
    Object pay(@RequestParam(value = "orderCode") String orderCode){
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("订单号："+orderCode +"，通联开始消费============================");

        String actionName = "CUP_QUICK";

        TLDHXRegister tldhxRegister = topupPayChannelBusiness.getTLDHXRegisterByIdCard(prp.getIdCard());

        String extra = prp.getExtra();// 消费计划|福建省-泉州市-350500
        LOG.info("=======================================消费城市：" + extra);

        String cityCode = extra.split("-")[2];

        Map<String, String> json = new HashMap();
        json.put("COM_ID", COMID);
        json.put("OUT_TRADE_NO", orderCode);
        json.put("CARD_NO", prp.getBankCard());
        json.put("USER_INFO_ID", tldhxRegister.getMerchantCode()); //终端渠道编号
        json.put("CITY", cityCode);
        json.put("AMOUNT", prp.getRealAmount());
        json.put("MER_URL", "http://101.132.160.107/v1.0/paymentchannel/topup/sdjpaysuccess");
        json.put("NOTIFY_URL", ipAddress + "/v1.0/paymentgateway/topup/tldhx/transfer/notifyurl");
        json.put("NONCE_STR", UUID.randomUUID().toString().replace("-","").toString());
        json.put("SIGN", Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(json)),MD5KEY));

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", json);

        LOG.info("通联消费请求参数为："+ jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/payServer", jsonReq.toJSONString());
        LOG.info("通联消费的响应参数为："+ resp);
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")) {
            LOG.info("通联消费成功======================");
            return ResultWrap.init("999998", "等待银行扣款中");
        }else {
            return ResultWrap.init(CommonConstants.FALIED, respMap.get("MESSAGE").toString());
        }
    }
    //消费查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/orderquery")
    public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {

        Map<String, Object> maps = new HashMap<String, Object>();
        String actionName = "QUERY_PAY_ORDER";
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        TLDHXRegister tldhxRegister = topupPayChannelBusiness.getTLDHXRegisterByIdCard(prp.getIdCard());
        Map<String, String> json = new HashMap();
        json.put("COM_ID", COMID);
        json.put("OUT_TRADE_NO", orderCode);
        json.put("USER_INFO_ID", tldhxRegister.getMerchantCode());
        //json.put("CREATE_DATE", orderCode.substring(0,8));
        json.put("NONCE_STR", UUID.randomUUID().toString().replace("-","").toString());
        json.put("SIGN", Signature.getSignMD5(JSONObject.parseObject(JSONObject.toJSONString(json)),MD5KEY));

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", json);

        LOG.info("通联消费查询请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/payServer", jsonReq.toJSONString());
        LOG.info("通联消费查询的响应参数为："+ resp);
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")){
            Map<String, Object> respInfo = (Map)JSON.parse(respMap.get("ACTION_INFO").toString());
            if(respInfo.get("RESULT_CODE").equals("0")){
                if(respInfo.get("TRADE_STATE").equals("SUCCESS")){
                    maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
                    String version = "32";
                    RestTemplate restTemplate = new RestTemplate();
                    String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("orderCode", orderCode);
                    requestEntity.add("version", version);
                    String result = null;
                    net.sf.json.JSONObject jsonObject;
                    try {
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("RESULT================" + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                    }
                    url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("status", "1");
                    requestEntity.add("order_code", orderCode);
                    requestEntity.add("third_code", respInfo.get("TRANSACTION_ID").toString());
                    result = null;
                    try {
                        restTemplate = new RestTemplate();
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                    }
                    LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);
                }
            }else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
            }
        }else{
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, respMap.get("MESSAGE"));
        }
        return maps;
    }
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/test")
    public String topupBack2(@RequestBody TLbackBody tLbackBody, HttpServletResponse response) throws Exception {
        LOG.info(tLbackBody.toString());
        TLbackInfo tLbackInfo = tLbackBody.gettLbackInfo();
        Map<String, String> map = new HashMap<>();
        map.put("COM_ID", tLbackInfo.getComId());
        map.put("USER_INFO_ID", tLbackInfo.getUserInfoId());
        map.put("ORDER_ID", tLbackInfo.getOrderId());
        map.put("STATUS", tLbackInfo.getStatus());
        map.put("AMOUNT", tLbackInfo.getAmount());
        map.put("LOAN_AMOUNT", tLbackInfo.getLoanAmount());
        map.put("IN_ACCOUNT", tLbackInfo.getInAccount());
        map.put("IN_NAME", tLbackInfo.getInName());
        map.put("MSG", tLbackInfo.getMsg());
        map.put("NONCE_STR", tLbackInfo.getNonceStr());
        map.put("SIGN", tLbackInfo.getSign());
        boolean b = Signature.checkSignRSA((JSONObject) JSON.toJSON(map), PATH);
        LOG.info(String.valueOf(b));
        return "dd";
    }
    // 消费的异步通知接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/transfer/notifyurl")
    public String topupBack(@RequestBody TLPayBackBody tlPayBackBody, HttpServletResponse response) throws Exception {
        LOG.info("通联消费异步通知======");
        LOG.info(tlPayBackBody.toString());

        TLPayBackInfo tlPayBackInfo = tlPayBackBody.getTlPayBackInfo();
        String sign = tlPayBackBody.getSign();

        String payResult = tlPayBackInfo.getPayResult();
        String payInfo = tlPayBackInfo.getPayInfo();
        String transactionId = tlPayBackInfo.getTransactionId();
        String outTradeNo = tlPayBackInfo.getOutTradeNo();

        Map<String, String> map = new HashMap<>();
        map.put("PAY_RESULT", tlPayBackInfo.getPayResult());
        map.put("PAY_INFO", tlPayBackInfo.getPayInfo());
        map.put("TRANSACTION_ID", tlPayBackInfo.getTransactionId());
        map.put("OUT_TRADE_NO", tlPayBackInfo.getOutTradeNo());
        map.put("AMOUNT", tlPayBackInfo.getAmount());
        map.put("GMT_PAYMENT", tlPayBackInfo.getGmtPayment());
        map.put("COM_ID", tlPayBackInfo.getComId());
        map.put("USER_INFO_ID", tlPayBackInfo.getUserInfoId());
        map.put("NONCE_STR", tlPayBackInfo.getNonceStr());
        map.put("SIGN", sign);
        boolean checkSignMD5 = Signature.checkSignMD5((JSONObject)JSON.toJSON(map), MD5KEY);
        if(!checkSignMD5){
            LOG.info("签名不正确");
            return "SUCCESS";
        }
        if(!payResult.equals("0")){
            LOG.info(outTradeNo+"订单支付失败，详情："+payInfo);
            return "SUCCESS";
        }
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(outTradeNo);
        String version = "32";
        RestTemplate restTemplate = new RestTemplate();
        String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode", outTradeNo);
        requestEntity.add("version", version);
        String result = null;
        net.sf.json.JSONObject jsonObject;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("status", "1");
        requestEntity.add("order_code", outTradeNo);
        requestEntity.add("third_code", transactionId);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        LOG.info("订单状态修改成功===================" + outTradeNo + "====================" + result);
        return "SUCCESS";
    }
    // 代付接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/transfercreate")
    public @ResponseBody
    Object transferCreate(@RequestParam(value = "orderCode") String orderCode) {
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        TLDHXRegister tldhxRegister = topupPayChannelBusiness.getTLDHXRegisterByIdCard(prp.getIdCard());
        LOG.info("通联开始结算============================");
        String actionName = "SETTLE";
        Map<String, String> json = new HashMap();
        json.put("COM_ID", COMID);
        json.put("USER_INFO_ID", tldhxRegister.getMerchantCode()); //末端商户号
        json.put("BANK_ACCOUNT",prp.getBankCard()); //收款账户
        json.put("ACCOUNT_NAME", prp.getUserName()); //收款户名
        json.put("SETTLE_TYPE", "2"); //代付类型 1或者为空为普通代付，2、为同名代付
        json.put("AMOUNT", prp.getRealAmount());//代付订单金额，不包含手续费。手续费从账户余额扣除
        json.put("NOTIFY_URL",ipAddress + "/v1.0/paymentgateway/topup/tldhx/settle/notifyurl");//通知地址
        json.put("AGENT_ORDER_ID", orderCode);//订单号
        json.put("NONCE_STR", UUID.randomUUID().toString().replace("-","").toString());//随机字符串

        String signbefore = Signature.getBase(JSONObject.parseObject(JSONObject.toJSONString(json)));
        LOG.info("待签名字符串："+ signbefore);
        String sign = CertUtil.sign(signbefore, KEYSTOREPATH, KEYSTOREPASSWORD, ALIAS, ALIASPASSWORD);

        json.put("SIGN", sign);

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", json);

        LOG.info("通联代付请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/payServer", jsonReq.toJSONString());
        LOG.info("通联代付响应参数为："+ resp );
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")){
            String respInfo = respMap.get("ACTION_INFO").toString();
            Map<String, Object> respInfoMap =  (Map)JSON.parse(respInfo);
            if(respInfoMap.get("STATUS").equals("2")){
                return ResultWrap.init(CommonConstants.FALIED,respInfoMap.get("MSG").toString() );
            }else{
                redisUtil.savePaymentRequestParameter(respInfoMap.get("ORDER_ID").toString(), prp);//将第三方的订单号做键存入redis。
                return ResultWrap.init("999998","出款成功");
            }
        }else{
            return ResultWrap.init(CommonConstants.FALIED,respMap.get("MESSAGE").toString() );
        }
    }
    //代付查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/transferquery")
    public @ResponseBody Object backOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        String actionName = "QUERY_SETTLE";
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, String> json = new HashMap();
        json.put("COM_ID", COMID);
        json.put("AGENT_ORDER_ID", orderCode); //代理订单号
        //json.put("CREATE_DATE",orderCode.substring(0,8)); //订单创建时间 yyyyMMdd
        json.put("NONCE_STR", UUID.randomUUID().toString().replace("-","").toString());//随机字符串

        String signbefore = Signature.getBase(JSONObject.parseObject(JSONObject.toJSONString(json)));
        String sign = CertUtil.sign(signbefore, KEYSTOREPATH, KEYSTOREPASSWORD, ALIAS, ALIASPASSWORD);

        json.put("SIGN", sign);

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("ACTION_NAME", actionName);
        jsonReq.put("ACTION_INFO", json);

        LOG.info("通联代付查询请求参数为："+jsonReq.toJSONString());
        String resp = HttpUtil.sendPost("http://116.62.126.152:8888/wphkps/payServer", jsonReq.toJSONString());
        LOG.info("通联代付查询响应参数为："+resp);
        Map<String, Object> respMap = (Map)JSON.parse(resp);
        if(respMap.get("ACTION_RETURN_CODE").equals("000000")){
            String respInfo = respMap.get("ACTION_INFO").toString();
            Map<String, Object> respInfoMap =  (Map)JSON.parse(respInfo);
            if(respInfoMap.get("STATUS").equals("2")){
                return ResultWrap.init(CommonConstants.FALIED,respInfoMap.get("MSG").toString() );
            }else{

                String version = "32";
                RestTemplate restTemplate = new RestTemplate();
                String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("orderCode", orderCode);
                requestEntity.add("version", version);
                String result = null;
                net.sf.json.JSONObject jsonObject;
                try {
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("", e);
                }

                url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code", respInfoMap.get("ORDER_ID").toString());
                result = null;
                try {
                   restTemplate = new RestTemplate();
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("", e);
                }
                LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);
                return ResultWrap.init("999998","出款成功");
            }
        }else{
            return ResultWrap.init(CommonConstants.FALIED,respMap.get("MESSAGE").toString() );
        }
    }

    //代付通知接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/settle/notifyurl")
    public String topupBack1(@RequestBody TLbackBody tLbackBody, HttpServletResponse response) throws Exception {
        LOG.info("通联代付异步通知======");
        LOG.info(tLbackBody.toString());

        TLbackInfo tLbackInfo = tLbackBody.gettLbackInfo();
        String ORDER_ID = tLbackInfo.getOrderId(); //
        String status = tLbackInfo.getStatus();

        Map<String, String> map = new HashMap<>();
        map.put("COM_ID", tLbackInfo.getComId());
        map.put("USER_INFO_ID", tLbackInfo.getUserInfoId());
        map.put("ORDER_ID", tLbackInfo.getOrderId());
        map.put("STATUS", tLbackInfo.getStatus());
        map.put("AMOUNT", tLbackInfo.getAmount());
        map.put("LOAN_AMOUNT", tLbackInfo.getLoanAmount());
        map.put("IN_ACCOUNT", tLbackInfo.getInAccount());
        map.put("IN_NAME", tLbackInfo.getInName());
        map.put("MSG", tLbackInfo.getMsg());
        map.put("NONCE_STR", tLbackInfo.getNonceStr());
        map.put("SIGN", tLbackInfo.getSign());

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ORDER_ID);
        boolean checkSignRSA = Signature.checkSignRSA((JSONObject) JSON.toJSON(map), PATH);
        if(!checkSignRSA){
            LOG.info("签名不正确");
            return "SUCCESS";
        }
        String orderId = prp.getOrderCode();
        if(!status.equals("6")){
            LOG.info("订单支付失败，详情："+tLbackInfo.getMsg());
            return "SUCCESS";
        }
        String version = "32";
        RestTemplate restTemplate = new RestTemplate();
        String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode", orderId);
        requestEntity.add("version", version);
        String result = null;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("status", "1");
        requestEntity.add("order_code", orderId);
        requestEntity.add("third_code", ORDER_ID);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
       LOG.info("订单状态修改成功===================" + orderId + "====================" + result);
        return "SUCCESS";
    }
    //获取城市编号 2级联动
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tldhx/city")
    public @ResponseBody Object getcity(@RequestParam(value = "pid") String pid){
        List<TLDHXCity> cities = new ArrayList<>();

        try{
            cities = cityCodeBusiness.getTLDHXCitybyPid(pid);
            if(cities.size()==0){
                return ResultWrap.init(CommonConstants.FALIED, "查询失败");
            }
         }catch (Exception e){
            e.printStackTrace();
            return ResultWrap.init(CommonConstants.FALIED, "查询失败");
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", cities );
    }
}
