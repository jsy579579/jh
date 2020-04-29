package com.jh.paymentgateway.controller.yxe.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.tldhx.service.TLDHXpageRequset;
import com.jh.paymentgateway.controller.yxe.dao.YXEBusiness;
import com.jh.paymentgateway.controller.yxe.domain.JiFuPayMsg;
import com.jh.paymentgateway.controller.yxe.pojo.YXEAddress;
import com.jh.paymentgateway.controller.yxe.pojo.YXEBankBin;
import com.jh.paymentgateway.controller.yxe.pojo.YXERegister;
import com.jh.paymentgateway.controller.yxe.util.MerchantApiUtil;
import com.jh.paymentgateway.controller.yxe.util.SimpleHttpUtils;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 飏支付通道 (小额)
 */
@Controller
@EnableAutoConfiguration
public class YXEpageRequset extends BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(YXEpageRequset.class);

    @Value("${yxe.payKey}")
    private String payKey;// 商户支付Key

    @Value("${yxe.paySecret}")
    private String paySecret;// 签名

    @Value("${yxe.platMerchant}")
    private String platMerchant;// 平台商户

    @Value("${yxe.url}")
    private String URL;// 请求地址 （生产地址）

    @Autowired
    private YXEBusiness yxeBusiness;

    @Value("${payment.ipAddress}")
    private String ip;

    @Autowired
    private RedisUtil redisUtil;

    // 与还款对接
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/dockentrance")
    public @ResponseBody
    Object dockentrance(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "bankName") String bankName,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "idCard") String idCard,
                        @RequestParam(value = "userName") String userName,
                        @RequestParam(value = "rate") String rate,
                        @RequestParam(value = "extraFee") String extraFee,
                        @RequestParam(value = "securityCode") String securityCode,
                        @RequestParam(value = "expiredTime") String expiredTime) throws UnsupportedEncodingException {
        LOG.info("飏支付代还请求参数==========bankCard==" + bankCard + "====phone=======" + phone + "=======userName========" + userName);
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(idCard, bankCard);
        if (yxeRegister == null || yxeRegister.getStatus().equals(0)) {
            LOG.info("===================用户未绑卡，开始绑卡===========================");
            return ResultWrap.init("999996", "需要绑卡",
                    ip + "/v1.0/paymentgateway/repayment/yxe/bindcard?bankCard=" + bankCard
                            + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&cardType="
                            + URLEncoder.encode("0", "UTF-8") + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip);
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/yxe/bindcard")
    public String returnJFDHBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        LOG.info("飏支付跳转到绑卡页面");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String bankName = request.getParameter("bankName");
        String cardType = request.getParameter("cardType");
        String bankCard = request.getParameter("bankCard");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String expiredTime = request.getParameter("expiredTime");
        String securityCode = request.getParameter("securityCode");
        String ipAddress = request.getParameter("ipAddress");
        String rate = request.getParameter("rate"); // 用户费率
        String extraFee = request.getParameter("extraFee"); // 额外费率
        String userName = request.getParameter("userName");

        model.addAttribute("bankName", bankName);
        model.addAttribute("cardType", cardType);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("idCard", idCard);
        model.addAttribute("phone", phone);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("rate", rate);
        model.addAttribute("extraFee", extraFee);
        model.addAttribute("userName", userName);

        return "yxebindcard";
    }

    // 绑卡发送短信
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/bindcardsms")
    @ResponseBody
    public Object BindCardNoSms(
            @RequestParam(value = "bankCard") String bankCard,
            @RequestParam(value = "bankName") String bankName,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "securityCode") String securityCode,
            @RequestParam(value = "expiredTime") String expiredTime,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "extraFee") String extraFee
    ) {
        Map map = new HashMap();
        // 查询用户是否帮过卡 没有则新建绑卡信息 有则修改
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(idCard, bankCard);
        if (null == yxeRegister) {
            yxeRegister = new YXERegister();
        }
        Long userId = Long.valueOf(TLDHXpageRequset.getRandom());
        LOG.info("进入飏支付小额开卡短信请求，userName:" + userName +
                ",phone:" + phone + ",bankCard:" + bankCard + ",idCard" + idCard);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("platMerchant", platMerchant); // 平台商户 由平台提供
        paramMap.put("payKey", payKey);// 商户支付Key
        paramMap.put("productType", "70000101"); // 支付产品类型 固定值即可
        paramMap.put("quotaType", "SMALL_AMOUNT"); // 额度类型 当该值为空时，表示不区分大小额 SMALL_AMOUNT 小额 LARGE_AMOUNT 大额

        paramMap.put("bankAccountName", userName); // 持卡人姓名  必填
        paramMap.put("certNo", idCard); // 证件号码 必填
        paramMap.put("phoneNo", phone); // 手机号 必填
        paramMap.put("bankAccountNo", bankCard); // 信用卡号 必填
        paramMap.put("bankAccountType", "PRIVATE_CREDIT_ACCOUNT"); // 银行卡类型 PRIVATE_CREDIT_ACCOUNT 信用卡 只支持信用卡
        paramMap.put("merchantNo", "10100000");//必填  填此即可
        paramMap.put("cvn2", securityCode); // 信用卡cvn2 必填
        paramMap.put("expDate", expiredTime); // 信用卡有效期  YYMM 必填
        String random = TLDHXpageRequset.getRandom(); // 生成订单号
        paramMap.put("orderNo", random); // 商户订单号
        // paramMap.put("operFlag", "balance"); // 操作标识 余额查询时传此标识

        JiFuPayMsg msg = new JiFuPayMsg();
        msg.setUserId(userId.toString()); // 用户id 必填 最长32位
        paramMap.put("extendParam", JSON.toJSONString(msg));// 备用参数
        paramMap.put("sign", MerchantApiUtil.getSign(paramMap, paySecret));
        LOG.info("请求报文Map:" + paramMap);

        String url = URL + "/bigpay-web-gateway/authentication/init";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        JSONObject jsonObject = JSON.parseObject(payResult);
        LOG.info("飏支付绑卡发送短信返回信息：" + jsonObject);
        String resultCode = jsonObject.getString("resultCode");// 返回码
        JSONObject payMessage = JSON.parseObject(jsonObject.getString("payMessage"));
        if ("SUCCESS".equals(resultCode)) {// 请求成功
            yxeRegister.setExtraFee(extraFee);
            yxeRegister.setRate(rate);
            yxeRegister.setBankCard(bankCard);
            yxeRegister.setPhone(phone);
            yxeRegister.setUserName(userName);
            yxeRegister.setIdCard(idCard);
            yxeRegister.setSecurityCode(securityCode);
            yxeRegister.setExpiredTime(expiredTime);
            yxeRegister.setOrderId(random);
            yxeRegister.setStatus(0);   // 0 绑卡未成功 1 绑卡成功
            yxeRegister.setBankName(bankName);
            yxeRegister.setUserId(Long.valueOf(userId));
            try {
                yxeRegister.setTradeId(payMessage.getString("tradeId"));
            } catch (Exception e) {
                LOG.error("获取短信验证码失败!上游未返回：tradeId" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "获取短信验证码失败!");
                return map;
            }
            yxeBusiness.createYXERegister(yxeRegister);
            LOG.info("获取短信验证码成功!通道反馈信息：" + resultCode);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "获取短信验证码成功!");
            return map;
        } else {// 请求失败
            LOG.info("获取短信验证码失败!通道反馈信息：" + resultCode);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "获取短信验证码失败!");
            return map;
        }
    }

    // 绑卡确认
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/confirmSms")
    @ResponseBody
    public Object OpenPaymentCard(@RequestParam(value = "bankCard") String bankCard,
                                  @RequestParam(value = "smsCode") String smsCode,
                                  @RequestParam(value = "idCard") String idCard) {
        Map map = new HashMap();
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(idCard, bankCard);
        if (null == yxeRegister) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请先获取短信验证码！");
            return map;
        }
        LOG.info("进入飏支付小额绑卡确认请求，userName:" + yxeRegister.getUserName() +
                ",phone:" + yxeRegister.getPhone() + ",bankCard:" + bankCard + ",idCard" + idCard + "，验证码" + smsCode);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("productType", "70000101");
        paramMap.put("bankAccountType", "PRIVATE_CREDIT_ACCOUNT");  //银行卡类型  PRIVATE_CREDIT_ACCOUNT 信用卡 只支持信用卡
        paramMap.put("payKey", payKey);// 商户支付Key
        paramMap.put("platMerchant", platMerchant);//平台商户  平台提供

        paramMap.put("orderNo", yxeRegister.getOrderId()); // 与绑卡发短信接口订单号相同
        paramMap.put("bankAccountNo", yxeRegister.getBankCard()); // 信用卡号  必填
        paramMap.put("phoneNo", yxeRegister.getPhone()); // 手机号  必填
        paramMap.put("certNo", yxeRegister.getIdCard()); // 证件号码  必填
        paramMap.put("bankAccountName", yxeRegister.getUserName()); //持卡人名称  必填
        paramMap.put("expDate", yxeRegister.getExpiredTime()); //信用卡有效期  MMYY  必填
        paramMap.put("cvn2", yxeRegister.getSecurityCode()); //cvv2  必填
        paramMap.put("smsCode", smsCode); // 短信验证码  必填
        paramMap.put("extendParam", yxeRegister.getTradeId());// 备用参数  绑卡发短信返回tradeId
        paramMap.put("sign", MerchantApiUtil.getSign(paramMap, paySecret));

        LOG.info("绑卡请求报文Map:" + paramMap);

        String url = URL + "/bigpay-web-gateway/authentication/check";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        JSONObject jsonObject = JSON.parseObject(payResult);
        LOG.info("飏支付绑卡确认返回信息：" + jsonObject);
        Object resultCode = jsonObject.get("resultCode");// 返回码
        JSONObject payMessage = JSON.parseObject(jsonObject.getString("payMessage"));// 请求结果(请求成功时)
        Object resMsg = jsonObject.get("resMsg");
        if ("SUCCESS".equals(resultCode.toString())) {// 请求成功
            yxeRegister.setOpenCardId(payMessage.getString("openCardId"));
            yxeRegister.setStatus(1);
            yxeBusiness.createYXERegister(yxeRegister);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "绑卡成功!");
            map.put("redirect_url", ip + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
            LOG.info("飏支付绑卡成功:" + yxeRegister);
            return map;
        } else {// 请求失败
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "绑卡失败!" + resMsg);
            LOG.info("飏支付绑卡失败:" + yxeRegister);
            return map;
        }
    }

    // 飏支付支付请求
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/jfpay")
    @ResponseBody
    public Object toPay(@RequestParam(value = "ordercode") String orderCode) {
        Map map = new HashMap();
        LOG.info("进入飏支付消费======订单号：" + orderCode);
        // 查询订单信息
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("进入飏支付消费======订单信息：" + prp.toString());
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String amount = prp.getRealAmount();
        String extra = prp.getExtra(); // 消费计划|福建省-泉州市
        String rate = prp.getRate();
        String creditCardBankName = prp.getCreditCardBankName();    // 信用卡银行名
        String cityName = extra.substring(extra.indexOf("-") + 1);
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(idCard, bankCard);
        if (yxeRegister == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户未绑卡，发起支付失败");
            return map;
        }
        // 获取落地城市（消费的城市）
        YXEAddress yxeAddress = yxeBusiness.getYXEAddressByCityName(cityName);
        if (yxeAddress == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "暂时不支持该城市");
            return map;
        }
        String cityCode = yxeAddress.getId().toString(); // 城市
        String provinceCode = cityCode.substring(0, 2) + "0000"; // 省份
        // 获取银行编码
        YXEBankBin yxeBankBin = yxeBusiness.getYXEBankBinByBankName(creditCardBankName);
        if (yxeBankBin == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "暂时不支持该银行！");
            return map;
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("platMerchant", platMerchant); // 平台商户 平台提供
        paramMap.put("payKey", payKey); // 商户支付Key
        paramMap.put("productType", "40000103"); // 产品类型 固定值
        paramMap.put("quotaType", "SMALL_AMOUNT"); // 额度类型 当该值为空时，表示不区分大小额
        paramMap.put("orderPrice", amount); // 支付金额(元)
        paramMap.put("feeRate", rate); // 支付费率  费率为0.5%，则填写0.005即可
        paramMap.put("outTradeNo", orderCode); // 支付订单号
        paramMap.put("orderTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 订单时间
        paramMap.put("notifyUrl", ip + "/v1.0/paymentgateway/repayment/yxe/payNotifyUrl"); // 后台异步通知地址
        paramMap.put("productName", "个体商户"); // 不能为空，自由填写商品名称即可
        JiFuPayMsg msg = new JiFuPayMsg();
        msg.setOpenCardId(yxeRegister.getOpenCardId()); // 绑卡返回的OpenCardId 必填
        msg.setUserId(yxeRegister.getUserId().toString()); // 用户id 必填 最长32位 和绑卡接口userId一致
        paramMap.put("extendParam", JSON.toJSONString(msg));// 扩展参数
        paramMap.put("provinceCode", provinceCode); // 省份
        paramMap.put("cityCode", cityCode); // 城市
        // 银行卡私密信息
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("bankAccountType", "PRIVATE_CREDIT_ACCOUNT"); // 银行卡类型 PRIVATE_CREDIT_ACCOUNT 对私信用卡  只支持信用卡
        objectMap.put("phoneNo", yxeRegister.getPhone()); // 手机号 必填
        objectMap.put("bankCode", yxeBankBin.getBin()); // 银行编码
        objectMap.put("bankAccountName", yxeRegister.getUserName()); // 账户姓名 必填
        objectMap.put("bankAccountNo", yxeRegister.getBankCard()); // 信用卡号 必填
        objectMap.put("certType", "IDENTITY"); // 证件类型 IDENTITY 身份证
        objectMap.put("certNo", yxeRegister.getIdCard()); // 身份证号 必填
        objectMap.put("cvn2", yxeRegister.getSecurityCode()); // CVN2 必填
        objectMap.put("expDate", yxeRegister.getExpiredTime()); // MMYY 必填
        String secretContent = MerchantApiUtil.aesEncode(objectMap, paySecret);
        paramMap.put("secretContent", secretContent); // 加密密文
        paramMap.put("sign", MerchantApiUtil.getSign(paramMap, paySecret)); // 签名
        LOG.info("飏支付消费请求报文Map:" + paramMap);

        String url = URL + "/bigpay-web-gateway/quickPay/initPay";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        JSONObject jsonObject = JSON.parseObject(payResult);
        LOG.info("飏支付消费返回信息" + payResult);
        Object code = jsonObject.get("resultCode"); // 返回码
        Object payMessage = jsonObject.get("payMessage"); // 请求结果(请求成功时)
        Object errMsg = jsonObject.get("errMsg"); // 错误信息(请求失败时)
        Object payStatus = jsonObject.get("payStatus");
        if (!"0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付消费请求失败，原因" + errMsg);
            return map;
        }
        if ("SUCCESS".equals(payStatus)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;
        } else if ("PAYING".equals(payStatus)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errMsg);
            return map;
        }
    }

    // 飏支付消费回调
    @RequestMapping(value = "/v1.0/paymentgateway/repayment/yxe/payNotifyUrl")
    @ResponseBody
    public Object payNotifyUrl(HttpServletRequest request) {
        try {
            Map<String, String> map = this.getParamrterMap(request);
            LOG.info("飏支付消费回调======result:" + map);
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(map.get("outTradeNo"));
            if (map.get("tradeStatus").equals("SUCCESS")) {
                LOG.info("飏支付消费回调======消费成功，订单信息：{}", prp);
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                RestTemplate restTemplate = new RestTemplate();
                String URL;
                String result = null;
                String version = "";
                String channelTag = prp.getChannelTag();
                if ("YXE_QUICK".equals(channelTag)) {
                    version = "80";
                }
                LOG.info("*********************交易成功通道为:" + version + "***********************");
                String url = ip + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                requestEntity.add("orderCode", prp.getOrderCode());
                requestEntity.add("version", version);
                try {
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("飏支付消费回调异常", e);
                }
                URL = ip + ChannelUtils.getCallBackUrl(ip);
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", prp.getOrderCode());
                try {
                    result = restTemplate.postForObject(URL, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("飏支付修改订单异常===/v1.0/transactionclear/payment/update", e);
                }
                LOG.info("飏支付订单状态修改成功===订单号:" + prp.getOrderCode() + "===result:" + result);
                LOG.info("飏支付订单已交易成功!");
            }
            return ResultWrap.init(CommonConstants.SUCCESS, "回调成功", "SUCCESS");
        } catch (Exception e) {
            LOG.info("飏支付消费回调======异常" + e);
            return ResultWrap.init(CommonConstants.FALIED, "回调失败", "FALIED");
        }
    }

    // 飏支付消费结果查询
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/payQuery")
    @ResponseBody
    public Object payQuery(HttpServletRequest request,
                           @RequestParam(value = "orderCode") String orderCode) throws IOException {
        Map<String, Object> map = new HashMap<>();
        LOG.info("进入飏支付消费结果查询======订单号：" + orderCode);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("platMerchant", platMerchant); // 平台商户 平台提供
        paramMap.put("payKey", payKey); // 商户支付Key
        paramMap.put("outTradeNo", orderCode); // 原交易订单号
        // 签名及生成请求API的方法
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign); // 签名
        LOG.info("进入飏支付消费结果查询请求原文" + paramMap.toString());
        String url = URL + "/bigpay-web-gateway/query/singleOrder";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        LOG.info("进入飏支付消费结果查询结果" + payResult);
        JSONObject jsonObject = JSON.parseObject(payResult);
        Object resultCode = jsonObject.get("resultCode"); // 返回码
        Object errMsg = jsonObject.get("errMsg"); // 错误信息(请求失败时)
        Object orderStatus = jsonObject.get("orderStatus");
        if (!"0000".equals(resultCode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付消费请求失败");
            return map;
        }
        if ("SUCCESS".equals(orderStatus)) {
            Object trxNo = jsonObject.get("trxNo");
            LOG.info("订单执行成功==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付消费支付成功");
            return map;
        } else if ("PAYING".equals(orderStatus)) {
            LOG.info("订单执行失败==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付消费支付失败");
            return map;
        } else if ("WAITING_PAYMENT".equals(orderStatus)) {
            LOG.info("订单处理中==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付消费处理中，等待银行扣款");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errMsg);
            return map;
        }
    }

    // 飏支付还款
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/toreppay")
    @ResponseBody
    public Object toSettle(@RequestParam(value = "orderCode") String orderCode) {
        Map map = new HashMap();
        LOG.info("进入飏支付还款======订单号：" + orderCode);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("进入飏支付还款======订单信息：" + prp);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String extraFee = prp.getExtraFee();
        String amount = prp.getRealAmount();
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(idCard, bankCard);
        if (yxeRegister == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户未绑卡，发起支付失败");
            return map;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("platMerchant", platMerchant);// 平台商户 平台提供
        paramMap.put("payKey", payKey);
        paramMap.put("quotaType", "SMALL_AMOUNT ");// 额度类型 当该值为空时，表示不区分大小额
        paramMap.put("proxyType", "T0"); // 交易类型 固定值
        paramMap.put("productType", "QUICKPAY"); // QUICKPAY 固定值

        paramMap.put("outTradeNo", orderCode); // 商户T0出款订单号
        paramMap.put("orderPrice", amount); // 订单金额, 单位:元
        paramMap.put("settFee", extraFee); // 结算手续费

        paramMap.put("bankAccountType", "PRIVATE_CREDIT_ACCOUNT"); // 收款银行卡类型
        paramMap.put("certNo", yxeRegister.getIdCard()); // 身份证号 必填
        paramMap.put("phoneNo", yxeRegister.getPhone());  // 手机号 必填
        paramMap.put("receiverName", yxeRegister.getUserName());// 收款人姓名 必填
        paramMap.put("certType", "IDENTITY"); // 收款人证件类型 固定值
        paramMap.put("receiverAccountNo", yxeRegister.getBankCard()); // 收款人银行卡号 必填

        paramMap.put("bankClearNo", "265464"); // 代付清算行号  可随意填，但不能为空
        paramMap.put("bankBranchNo", "613123"); // 代付开户行支行行号   可随意填，但不能为空
        paramMap.put("bankName", "中国工商银行"); // 开户行名称  可随意填，但不能为空
        paramMap.put("bankCode", "ICBC"); // 银行编码   可随意填，但不能为空
        paramMap.put("bankBranchName", "中国建设银行杭州支行"); // 代付开户行支行名称  可随意填，但不能为空
        paramMap.put("province", "浙江"); // 开户省份  可随意填，但不能为空
        paramMap.put("city", "杭州"); // 开户城市  可随意填，但不能为空

        paramMap.put("openCardId", yxeRegister.getOpenCardId()); //绑卡返回的openCardId 必填

        JiFuPayMsg msg = new JiFuPayMsg();
        msg.setUserId(yxeRegister.getUserId().toString()); // 用户id 必填 最长32位 和绑卡接口userId一致
        paramMap.put("extendParam", JSON.toJSONString(msg));// 扩展参数

        LOG.info("进入飏支付还款请求原文：" + paramMap);
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign);// 签名

        String url = URL + "/bigpay-web-gateway/accountProxyPay/initPay";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);

        LOG.info("进入飏支付还款返回信息：" + payResult);
        JSONObject jsonObject = JSON.parseObject(payResult);
        Object resultCode = jsonObject.get("resultCode");//返回码
        Object errMsg = jsonObject.get("resMsg");//错误信息(请求失败时)
        Object remitStatus = jsonObject.get("remitStatus");
        if (!"0000".equals(resultCode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付还款请求失败，原因" + errMsg);
            return map;
        }
        if ("REMIT_SUCCESS".equals(remitStatus)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "结算成功，等待银行出款");
            return map;
        } else if ("REMITTING".equals(remitStatus)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "结算处理中，等待银行出款");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errMsg);
            return map;
        }
    }

    // 飏支付还款结果查询
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/reppayQuery")
    @ResponseBody
    public Object reppayQuery(@RequestParam(value = "orderCode") String orderCode) {
        Map map = new HashMap();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("payKey", payKey); // 商户支付Key
        paramMap.put("outTradeNo", orderCode);//原交易订单号
        paramMap.put("platMerchant", platMerchant);//平台商户  平台提供
        /////签名及生成请求API的方法///
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign);
        String url = URL + "/bigpay-web-gateway/proxyPayQuery/query";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        LOG.info("进入飏支付还款查询返回信息：" + payResult);
        JSONObject jsonObject = JSON.parseObject(payResult);
        Object resultCode = jsonObject.get("resultCode");//返回码
        Object errMsg = jsonObject.get("errMsg");//错误信息(请求失败时)
        Object remitStatus = jsonObject.get("remitStatus");
        if (!"0000".equals(resultCode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付还款请求失败");
            return map;
        }
        if ("REMIT_SUCCESS".equals(remitStatus)) {
            LOG.info("订单执行成功==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付还款支付成功");
            return map;
        } else if ("REMIT_FAIL".equals(remitStatus)) {
            LOG.info("订单执行失败==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付还款支付失败");
            return map;
        } else if ("REMITTING".equals(remitStatus)) {
            LOG.info("订单处理中==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, "飏支付还款处理中，等待银行打款");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errMsg);
            return map;
        }
    }

    // 余额查询
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/balanceQuery")
    @ResponseBody
    public Object balanceQuery(@RequestParam(value = "bankCard") String bankCard) {
        Map map = new HashMap();
        // 写入YXE注册表
        YXERegister yxeRegister = yxeBusiness.getYXERegisterByIdCard(bankCard);
        if (null == yxeRegister) {
            yxeRegister = new YXERegister();
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("platMerchant", platMerchant); // 平台商户 由平台提供
        paramMap.put("payKey", payKey);// 商户支付Key
        paramMap.put("productType", "70000101"); // 支付产品类型 固定值即可
        paramMap.put("quotaType", "SMALL_AMOUNT"); // 额度类型 当该值为空时，表示不区分大小额 SMALL_AMOUNT 小额 LARGE_AMOUNT 大额
        paramMap.put("bankAccountName", yxeRegister.getUserName()); // 持卡人姓名  必填
        paramMap.put("certNo", yxeRegister.getIdCard()); // 证件号码 必填
        paramMap.put("phoneNo", yxeRegister.getPhone()); // 手机号 必填
        paramMap.put("bankAccountNo", bankCard); // 信用卡号 必填
        paramMap.put("bankAccountType", "PRIVATE_CREDIT_ACCOUNT"); // 银行卡类型 PRIVATE_CREDIT_ACCOUNT 信用卡 只支持信用卡
        paramMap.put("merchantNo", "10100000");//必填  填此即可
        paramMap.put("cvn2", yxeRegister.getSecurityCode()); // 信用卡cvn2 必填
        paramMap.put("expDate", yxeRegister.getExpiredTime()); // 信用卡有效期  YYMM 必填
        String random = TLDHXpageRequset.getRandom(); // 生成订单号
        paramMap.put("orderNo", random); // 商户订单号
        paramMap.put("operFlag", "balance"); // 操作标识 余额查询时传此标识
        JiFuPayMsg msg = new JiFuPayMsg();
        msg.setUserId(yxeRegister.getUserId().toString()); // 用户id 必填 最长32位
        paramMap.put("extendParam", JSON.toJSONString(msg));// 备用参数
        paramMap.put("sign", MerchantApiUtil.getSign(paramMap, paySecret));
        LOG.info("请求报文Map:" + paramMap);
        String url = URL + "/bigpay-web-gateway/authentication/init";
        String payResult = SimpleHttpUtils.httpPost(url, paramMap);
        LOG.info("飏支付余额查询结果:" + payResult);
        JSONObject jsonObject = JSON.parseObject(payResult);
        String resultCode = jsonObject.getString("resultCode");// 返回码
        JSONObject payMessage = JSON.parseObject(jsonObject.getString("payMessage"));
        if ("SUCCESS".equals(resultCode)) {// 请求成功
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, payMessage.getString("balance"));
            return map;
        } else {// 请求失败
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询余额失败!");
            return map;
        }
    }

    // 支持的城市 省市二级联动
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/yxe/findByCity")
    @ResponseBody
    public Object findByCity(@RequestParam(value = "provinceId", required = false, defaultValue = "0") String provinceId) {
        try {
            List<YXEAddress> list = yxeBusiness.findByCity(provinceId);
            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);
        } catch (Exception e) {
            return ResultWrap.init(CommonConstants.FALIED, "查询失败", e);
        }
    }

    // 获取数据
    private Map getParamrterMap(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        request.setCharacterEncoding("utf-8");
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        LOG.info("飏支付接收上游异步通知值：" + params);
        return params;
    }
}
