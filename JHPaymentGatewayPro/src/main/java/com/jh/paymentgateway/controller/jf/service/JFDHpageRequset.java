package com.jh.paymentgateway.controller.jf.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.jf.dao.JFDEBusiness;
import com.jh.paymentgateway.controller.jf.pojo.JFDEArea;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMcc;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMerchant;
import com.jh.paymentgateway.controller.tldh.dao.TLRegisterBusiness;
import com.jh.paymentgateway.controller.tldh.pojo.TLBankcode;
import com.jh.paymentgateway.controller.tldhx.dao.CheckedBankRepsository;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.JFBindCard;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jf.AES;
import com.jh.paymentgateway.util.jf.Base64;
import com.jh.paymentgateway.util.jf.HttpClient4Util;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class JFDHpageRequset extends BaseChannel {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Autowired
    private JFDEBusiness jfdEbusiness;
    @Autowired
    CheckedBankRepsository checkedBankRepsository;
    @Autowired
    private TLRegisterBusiness tlRegisterBusiness;

    @Value("${payment.ipAddress}")
    private String ip;

    private static final Logger LOG = LoggerFactory.getLogger(JFDHpageRequset.class);
    protected static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static String key = "EP4Z1D89BG2DQC3XVFSC7JMG5PF15ZBX";
    private static String partnerNo = "BYT0NPDH";
    private static String requestURL = "http://fast.jfpays.com:19085/rest/api/";

    // 与还款对接
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/jfdh/Dockentrance")
    public @ResponseBody Object Dockentrance(@RequestParam(value = "dbankCard") String deditBankCard,
                                             @RequestParam(value="bankCard") String bankCard,
                                             @RequestParam(value="bankName") String bankName,
                                             @RequestParam(value="phone") String phone,
                                             @RequestParam(value = "idCard") String idCard, @RequestParam(value = "dphone") String debitPhone,
                                             @RequestParam(value = "userName") String userName, @RequestParam(value = "dbankName") String debitBankName,
                                             @RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
                                             @RequestParam(value = "securityCode") String securityCode,
                                             @RequestParam(value = "expiredTime") String expiredTimes) throws Exception {

        String expiredTime=this.expiredTimeToMMYY(expiredTimes);
        LOG.info("即富大额代还请求参数==========dbankCard=="+deditBankCard+"====dphone======="+debitPhone+"=======dbankName========"+debitBankName);
        // 获取银行联行号
//        BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
        TLBankcode bcode=tlRegisterBusiness.findTLBankcodeByBankName(debitBankName);
        if (bcode == null) {
            return ResultWrap.init(CommonConstants.FALIED, "该到账卡银行暂不支持!");
        }
        String bankUnitNo = bcode.getBankCode();
//        String bankUnitNo="104100000004";
        Map<String, Object> maps = new HashMap<String, Object>();

        JFRegister jfR = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        JFBindCard jfB = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);

        if (jfR == null) {
            maps = (Map<String, Object>) this.register(deditBankCard, idCard, debitPhone, userName, rate, extraFee,bankUnitNo,debitBankName);
            LOG.info("JF进件结果============="+maps.toString());
            if(!CommonConstants.SUCCESS.equals(maps.get(CommonConstants.RESP_CODE))){
                return maps;
            }

        }else if (!extraFee.equals(jfR.getExtraFee()) | !bankCard.equals(jfR.getBankCard()) | !rate.equals(jfR.getRate())) {
            LOG.info("=====修改手续费,结算卡,费率,开通卡,去支付======");
            maps = (Map<String, Object>) modifyCard(bankCard, phone, rate, extraFee, idCard);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }

        }
        if (jfB == null || jfB.getStatus().equals("0")) {
            LOG.info("===================用户未绑卡，开始绑卡===========================");
            maps = ResultWrap.init("999996", "需要绑卡",
                    ip + "/v1.0/paymentgateway/repayment/jfdh/bindcard?bankCard=" + bankCard
                            + "&bankName="+ URLEncoder.encode(bankName, "UTF-8")+ "&cardType="
                            + URLEncoder.encode("0", "UTF-8") + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip);
            return maps;
        }


        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    /*
    JF进件
     */
    @RequestMapping(method = RequestMethod.POST,value ="/v1.0/paymentgateway/quick/jfdh/register" )
    public @ResponseBody Object register(@RequestParam("bankCard")String bankCard,
                                         @RequestParam("idCard")String idCard,
                                         @RequestParam("phone")String phone,
                                         @RequestParam("userName")String userName,
                                         @RequestParam("rate")String rate,
                                         @RequestParam("extraFee")String extraFee,
                                         @RequestParam("bankUnitNo")String bankUnitNo,
                                         @RequestParam("bankName")String bankName){
        LOG.info("===================第一次进件===========================");
        String url = requestURL + "610001";
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        Map<String, Object> rateList = new HashMap<String, Object>();
        rateList.put("QUICKPAY_WK_KB", rate);
        // 公共参数
        String orderId = getRandom();
        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610001");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        map.put("merchantCode", System.currentTimeMillis());
        map.put("merName", "上海百也特");
        map.put("merShortName", "富贵商城");
        map.put("bankAccountName", userName);
        map.put("idCardNo", idCard);
        map.put("phoneno", phone);
        map.put("merAddress", "上海宝山区长江南路华滋奔腾控股集团A栋1525号");
        map.put("bankAccountNo", bankCard);
        map.put("bankUnitNo", bankUnitNo);// 联行号
        map.put("bankName", bankName);
        map.put("productList", rateList);
        map.put("province", "530000");
        map.put("city", "530400");
        map.put("withdrawDepositSingleFee", getNumber(extraFee));

        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富进件请求地址:" + url);

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String platMerchantCode = jsonobj.getString("platMerchantCode");
            String headJson = jsonobj.getString("head");
            JSONObject MessageJson = JSONObject.parseObject(headJson);
            String message = MessageJson.getString("8");
            LOG.info("返回平台商户号：" + platMerchantCode);
            LOG.info("返回描述：" + message);
            if (!"".equals(platMerchantCode) && platMerchantCode != null) {
                JFRegister jfRegister = new JFRegister();
                jfRegister.setBankCard(bankCard);
                jfRegister.setExtraFee(extraFee);
                jfRegister.setRate(rate);
                jfRegister.setMerchantNo(platMerchantCode);
                jfRegister.setIdCard(idCard);
                jfRegister.setPhone(phone);
                topupPayChannelBusiness.createJFRegister(jfRegister);
                map.clear();
                map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE,message);
                return map;
            } else {
                map.clear();
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("即富进件---异常：" + message);
                return map;

            }
        } catch (Exception e) {
            e.printStackTrace();
            map.clear();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求异常");
            return map;
        }
    }

    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/jfdh/bindcard")
    public String returnJFDHBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

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
        String rate = request.getParameter("rate");
        String extraFee = request.getParameter("extraFee");
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

        return "jfdhhkbindcard";
    }





    /**
     * 开通支付卡短信6002
     *
     * @return
     */

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/jfdh/bindcardsms")
    public @ResponseBody Object OpenSMS(@RequestParam(value = "bankCard") String bankCard,
                                        @RequestParam(value = "bankName") String bankName,
                                        @RequestParam(value = "idCard") String idCard,
                                        @RequestParam(value = "phone") String phone,
                                        @RequestParam(value = "userName") String userName,
                                        @RequestParam(value = "securityCode") String securityCode,
                                        @RequestParam(value = "expiredTime") String expiredTime,
                                        @RequestParam(value = "rate") String rate,
                                        @RequestParam(value = "extraFee") String extraFee) {

        // 获取平台商户号
        JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        String MerchantNo = jf.getMerchantNo();

        String url = requestURL + "610002";
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        // 公共参数
        String orderId = getRandom();

        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610002");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        String openOrderId = getRandom();
        LOG.info("开卡流水号：" + openOrderId);
        map.put("openOrderId", openOrderId);
        map.put("platMerchantCode", MerchantNo);
        map.put("accountName", userName);
        map.put("cardNo", bankCard);
        map.put("certNo", idCard);
        map.put("phoneno", phone);
        map.put("cvn2", securityCode);
        map.put("expired", expiredTime);
        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富开卡短信请求地址:" + url);

        Object resultJson = null;

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            resultJson = JSON.toJSONString(result);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String headJson = jsonobj.getString("head");
            JSONObject HJson = JSONObject.parseObject(headJson);
            String message = HJson.getString("respMsg");
            String respCode = HJson.getString("respCode");
            LOG.info("返回描述：" + message);
            LOG.info("返回状态码：" + respCode);
            if ("000000".equals(respCode)) {
                LOG.info("状态码：1");
                try {
                    String openCardId = jsonobj.getString("openCardId");
                    LOG.info("开卡流水号为======="+openOrderId);
                    JFBindCard jfBindCards = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
                    if (jfBindCards==null){
                        JFBindCard jfBindCard = new JFBindCard();
                        jfBindCard.setBankCard(bankCard);
                        jfBindCard.setIdCard(idCard);
                        jfBindCard.setPhone(phone);
                        jfBindCard.setStatus("0");
                        jfBindCard.setBindingNum(openOrderId);
                        if (openCardId!=null&&!("").equals(openCardId)) {
                            LOG.info("支付卡号为=======" + openCardId);
                            jfBindCard.setOpenCardid(openCardId);
                        }
                        topupPayChannelBusiness.createJFBindCard(jfBindCard);
                    }else{
                        jfBindCards.setBindingNum(openCardId);
                        topupPayChannelBusiness.createJFBindCard(jfBindCards);
                    }
                    maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    maps.put(CommonConstants.RESP_MESSAGE, message);
                    return maps;

                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.info("即富开通支付卡短信=================未返回openCardId");
                }
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                maps.put("contractIds", openOrderId);
            } else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("即富开通支付卡短信---异常：" + message);
//                this.addOrderCauseOfFailure(orderCode, message, rip);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maps;

    }

    /**
     * 开通支付卡
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/confirmSms")
    public @ResponseBody Object OpenPaymentCard(@RequestParam(value = "bankCard") String bankCard,
                                                @RequestParam(value = "smsCode") String smsCode,
                                                @RequestParam(value = "idCard") String idCard) {


        JFBindCard jfB = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
        String openOrderId= jfB.getBindingNum();
        String phone=jfB.getPhone();
        String url = requestURL + "610003";
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        // 公共参数
        String orderId = getRandom();

        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610003");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        map.put("openOrderId", openOrderId);
        map.put("smsCode", smsCode);

        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富确认开卡请求地址:" + url);

        Object resultJson = null;

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            resultJson = JSON.toJSONString(result);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String openCardId = jsonobj.getString("openCardId");
            String rsHead = jsonobj.getString("head");

            JSONObject headJson = JSONObject.parseObject(rsHead);
            String message = headJson.getString("respMsg");
            String respCode = headJson.getString("respCode");
            LOG.info("机构绑卡序号：" + openCardId);
            if ("000000".equals(respCode)) {
                JFBindCard jfBindCard = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
                jfBindCard.setStatus("1");
                jfBindCard.setOpenCardid(openCardId);
                topupPayChannelBusiness.createJFBindCard(jfBindCard);

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功"); // 描述
                map.put("redirect_url",ip+"/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
                return map;

            } else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("即富开通支付卡---异常：" + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maps;

    }


    /**
     * 6.10免短信支付 610009
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/jfpay")
    public @ResponseBody Object toPay(@RequestParam(value = "ordercode") String orderCode) {
        LOG.info("进入即富消费==========");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String amount = prp.getRealAmount();
        String rip = prp.getIpAddress();
        String  extra   = prp.getExtra();
        LOG.info("进入即富消费=========="+prp.toString());
        String[] extras=extra.split("\\|");
        String[] companyname =extras[1].split("-");

        Map<String, Object> maps = new HashMap<String, Object>();
        // 获取平台商户号
        JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        String MerchantNo = jf.getMerchantNo();
        String rate = prp.getRate();
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
        // 获取开卡机构号
        JFBindCard jfBindCard = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
        String openCardId = jfBindCard.getOpenCardid();
        LOG.info("开卡号为=========="+openCardId);
        String url = requestURL + "610009";
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
//        String orderId = getRandom();

        head.put("charset", UTF_8);
        head.put("version", "1.0.0");
        head.put("partnerNo", partnerNo);
        head.put("partnerType","");
        head.put("txnCode", "610009");
        head.put("orderId", orderCode);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        map.put("rate",bigRate);
        map.put("platMerchantCode", MerchantNo);
        map.put("openCardId", openCardId);
        map.put("payAmount", getNumber(amount));
        map.put("remark", "大额代还");
        map.put("productCode", "QUICKPAY_WK_KB");
        map.put("notifyUrl", ip + "/v1.0/paymentgateway/repayment/jfdh/payNotifyUrl");
        map.put("city",companyname[1]);


        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderCode);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富支付短信请求地址:" + url);

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String workId = jsonobj.getString("workId");
            String rsHead = jsonobj.getString("head");
            JSONObject headJson = JSONObject.parseObject(rsHead);
            String message = headJson.getString("respMsg");
            String respCode = headJson.getString("respCode");
            String requestNo = headJson.getString("orderId");
            LOG.info("第三方流水号:" + requestNo);
            LOG.info("返回平台流水号：" + workId);
            if ("000000".equals(respCode)) {
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String URL = null;
                String results = null;
                URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/thirdordercode";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code", requestNo);
                try {
                    results = restTemplate.postForObject(URL, requestEntity, String.class);
                    LOG.info("*********************下单成功，添加第三方流水号***********************");
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("",e);
                }
                LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + results);
                maps.put(CommonConstants.RESP_CODE, "999998");
                maps.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                maps.put("orderId", workId);
                return maps;
            } else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("快捷支付---异常：" + message);
                return maps;
            }

        } catch (Exception e) {
            e.printStackTrace();
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "请求支付异常");
            return maps;
        }
    }

    /**
     * 即富消费异步回调地址
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/jfdh/payNotifyUrl")
    public void payNotifyUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("[即富还款]进入消费回调================================");

        String encryptData = request.getParameter("encryptData");
        String signature = request.getParameter("signature");
        try{
            String orderCode = request.getParameter("workId");
            LOG.info("消费回调：orderCode !!!!"+orderCode);

            LOG.info("消费回调：data: {} ", encryptData, signature);

            String dataPlain = AES.decode(org.apache.commons.codec.binary.Base64.decodeBase64(encryptData),
                    key.substring(0, 16));
            LOG.info("消费回调：dataPlain: {} ", dataPlain);
            String checkSign = DigestUtils.sha1Hex(encryptData + key.substring(16));

            LOG.info("消费回调：checkSign: {} ", checkSign);
            try {
                LOG.info("消费回调：signature: {} ", signature);
                signature = URLDecoder.decode(signature, UTF_8.name());
                LOG.info("消费回调：signature urldecode: {} ", signature);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (com.google.common.base.Objects.equal(signature, checkSign)) {
                LOG.error("签名验证成功");
                JSONObject jsonobj = JSONObject.parseObject(dataPlain);
                String head = jsonobj.getString("head");
                JSONObject headJson = JSONObject.parseObject(head);
                String orderId = headJson.getString("orderId");
                LOG.info("我方订单流水号：" + orderId);
                String orderStatus = jsonobj.getString("orderStatus");
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String URL = null;
                String result = null;
                PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderId);
                if ("01".equals(orderStatus)) {
                    LOG.info("*********************交易成功***********************");
                    String version="" ;
                    String channelTag=prp.getChannelTag();
                    if("JFDEDH_QUICK".equals(channelTag)){
                        version="40";
                    }else if("JFDEDH_QUICK1".equals(channelTag)){
                        version="41";
                    }else if("JFDEDH_QUICK2".equals(channelTag)){
                        version="42";
                    }else if("JFDEDH_QUICK3".equals(channelTag)){
                        version="43";
                    }

                    String url = ip + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                    requestEntity.add("orderCode", orderId);
                    requestEntity.add("version", version);
                    net.sf.json.JSONObject jsonObjects;
                    net.sf.json.JSONObject resultObj;
                    try {
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("RESULT================" + result);
                        jsonObjects = net.sf.json.JSONObject.fromObject(result);
                        resultObj = jsonObjects.getJSONObject("result");
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                    }
                    URL = ip+ChannelUtils.getCallBackUrl(ip);
                    //URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("status", "1");
                    requestEntity.add("order_code", orderId);
                    requestEntity.add("third_code", orderCode);
                    try {
                        result = restTemplate.postForObject(URL, requestEntity, String.class);
                        LOG.info("RESULT================" + result);

                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("",e);
                    }

                    LOG.info("订单状态修改成功===================" + orderId + "====================" + result);

                    LOG.info("订单已交易成功!");

                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();

                }else if("02".equals(orderStatus)){
                    LOG.error("支付失败");
                    this.addOrderCauseOfFailure(orderCode, "支付失败", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("03".equals(orderStatus)){
                    LOG.error("初始未支付");
                    this.addOrderCauseOfFailure(orderCode, "初始未支付", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("99".equals(orderStatus)){
                    LOG.error("支付超时");
                    this.addOrderCauseOfFailure(orderCode, "支付超时", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("04".equals(orderStatus)){
                    LOG.error("支付处理中");
                    this.addOrderCauseOfFailure(orderCode, "支付处理中，请稍后查询", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }
            } else {
                LOG.error("签名验证失败");
                this.addOrderCauseOfFailure(orderCode, "签名验证失败", ip);
                PrintWriter pw = response.getWriter();
                pw.print("000000");
                pw.close();
            }
        }catch (Exception e){
            LOG.info("没有该元素");
        }
    }


    /**
     * 余额查询6.14余额查询 610012
     * @param idCard
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/balanceQuery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) {
        // 获取平台商户号
        JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        String MerchantNo = jf.getMerchantNo();

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        String orderId = getRandom();
        String url = requestURL + "610012";
        //构建请求头
        head.put("charset", UTF_8);
        head.put("version", "1.0.0");
        head.put("partnerNo", partnerNo);
        head.put("partnerType","");
        head.put("txnCode", "610012");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);

        // 业务参数
        map.put("platMerchantCode", MerchantNo);

        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富支付短信请求地址:" + url);

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String balance=jsonobj.getString("balance");
            maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE,"商户余额为:"+balance);
            if (balance==null||balance.equals("")){
                maps.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE,"查询失败发生未知错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return maps;
    }

//    @RequestMapping(method = { RequestMethod.POST,
//            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/jfdh/tangdex")
//    public @ResponseBody Object a(){
//        int i=0;
//        int c=0;
//        List<JFDEMerchant> lists=jfdEbusiness.getAllProvincial();
//        for (JFDEMerchant jfdeMerchant:lists) {
//          String ciyt=jfdeMerchant.getCity();
//            System.out.println("该城市是"+ciyt);
//        JFDEArea jfdeArea=jfdEbusiness.getmccBycity(ciyt);
//        if (jfdeArea==null){
//            System.out.println("被删除的城市是"+ciyt);
//            jfdEbusiness.deleteByUserAndPointIndecs(ciyt);
//            i++;
//            System.out.println("一共删除了"+i+"数据");
//        }else {
//            c++;
//            System.out.println("一共跳过了"+c+"数据");
//        }
//
//        }
//        return lists;
//    }

    /**
     * 商户自选
     */

    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/repayment/jfdh/choosemerchant")
    public @ResponseBody Object merchant(@RequestParam(value = "area",required = false,defaultValue = "-1")String area,
                                         @RequestParam(value = "provincial",required = false,defaultValue = "-1")String provincial){
        Map<String,Object> maps=new HashMap<>();
        if (area.equals("-1") && provincial.equals("-1")) {
            List<JFDEMerchant> lists=jfdEbusiness.getAllProvincial();
            if (lists == null ||lists.size()==0) {
                maps.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE,"未获取到信息，请更换城市");
                return maps;
            }
            maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE,lists);
            return maps;
        }
        if (!"-1".equals(area)) {
            JFDEArea result=jfdEbusiness.getAllByArea(area);
            if (result!=null ){
                maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE,result);
            }else {
                maps.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE,"未获取到信息，请更换城市");
            }
            return maps;
        }
        if (!"-1".equals(provincial)) {
            List<JFDEMerchant> result=jfdEbusiness.getAllByProvincial(provincial);
            if (result!=null || result.size()!=0){
                maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE,result);
                return maps;
            }else {
                maps.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE,"未获取到信息，请更换城市");
                return maps;
            }
        }

        return maps;
    }

    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/repayment/jfdh/choosemcc")
    public @ResponseBody Object merchant(@RequestParam(value = "mcc",required = false,defaultValue = "-1")String mcc){
        Map<String,Object> maps=new HashMap<>();
        List<JFDEMcc> lists=jfdEbusiness.getAllMcc();
        maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE,lists);
        return maps;
    }



    /**
     * 修改结算卡，费率，手续费
     *
     * @return
     */
    public Object modifyCard(String bankCard, String phone, String rate, String ex, String idCard) {
        JFRegister jfRegister = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        String MerchantNo = jfRegister.getMerchantNo();
        String url = requestURL + "610006";
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> rateList = new HashMap<String, Object>();
        rateList.put("QUICKPAY_WK_KB", rate);
        String orderId = getRandom();
        // 公共参数
        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610006");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        map.put("platMerchantCode", MerchantNo);
        map.put("bankAccountNo", bankCard);
        map.put("phoneno", phone);
        map.put("productList", rateList);
        map.put("withdrawDepositSingleFee", getNumber(ex));
        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富结算卡修改请求地址:" + url);

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String rsHead = jsonobj.getString("head");
            JSONObject headJson = JSONObject.parseObject(rsHead);
            String message = headJson.getString("respMsg");
            String respCode = headJson.getString("respCode");
            if ("000000".equals(respCode)) {
                jfRegister.setBankCard(bankCard);
                jfRegister.setExtraFee(ex);
                jfRegister.setRate(rate);
                jfRegister.setCreateTime(new Date());
                topupPayChannelBusiness.createJFRegister(jfRegister);
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, message);
            } else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("修改费率,结算卡,手续费---异常：" + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return maps;
    }

    /**
     * 异步通知
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/jfdh/settleNotifyUrl")
    public void openFront(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("[即富还款]进入还款回调================================");

        String encryptData = request.getParameter("encryptData");
        String signature = request.getParameter("signature");
        try{
            String orderCode = request.getParameter("workId");
            LOG.info("还款回调：orderCode !!!!"+orderCode);

            LOG.info("还款回调：data: {} ", encryptData, signature);

            String dataPlain = AES.decode(org.apache.commons.codec.binary.Base64.decodeBase64(encryptData),
                    key.substring(0, 16));
            LOG.info("还款回调：dataPlain: {} ", dataPlain);
            String checkSign = DigestUtils.sha1Hex(encryptData + key.substring(16));

            LOG.info("还款回调：checkSign: {} ", checkSign);
            try {
                LOG.info("还款回调：signature: {} ", signature);
                signature = URLDecoder.decode(signature, UTF_8.name());
                LOG.info("还款回调：signature urldecode: {} ", signature);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (com.google.common.base.Objects.equal(signature, checkSign)) {
                LOG.error("签名验证成功");
                JSONObject jsonobj = JSONObject.parseObject(dataPlain);
                String head = jsonobj.getString("head");
                JSONObject headJson = JSONObject.parseObject(head);
                String orderId = headJson.getString("orderId");
                LOG.info("我方订单流水号：" + orderId);
                String orderStatus = jsonobj.getString("status");
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String URL = null;
                String result = null;
                PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderId);
                if ("01".equals(orderStatus)) {
                    String version="" ;
                    String channelTag=prp.getChannelTag();
                    if("JFDEDH_QUICK".equals(channelTag)){
                        version="40";
                    }else if("JFDEDH_QUICK1".equals(channelTag)){
                        version="41";
                    }else if("JFDEDH_QUICK2".equals(channelTag)){
                        version="42";
                    }else if("JFDEDH_QUICK3".equals(channelTag)){
                        version="43";
                    }
                    LOG.info("*********************交易成功通道为:"+version+"***********************");
                    String url = ip + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                    requestEntity.add("orderCode", orderId);
                    requestEntity.add("version", version);
                    net.sf.json.JSONObject jsonObjects;
                    net.sf.json.JSONObject resultObj;
                    try {
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("RESULT================" + result);
                        jsonObjects = net.sf.json.JSONObject.fromObject(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                    }
                    URL = ip+ChannelUtils.getCallBackUrl(ip);
                    //URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("status", "1");
                    requestEntity.add("order_code", orderId);
                    requestEntity.add("third_code", orderCode);
                    try {
                        result = restTemplate.postForObject(URL, requestEntity, String.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("",e);
                    }

                    LOG.info("订单状态修改成功===================" + orderId + "====================" + result);

                    LOG.info("订单已交易成功!");

                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();

                }else if("02".equals(orderStatus)){
                    LOG.error("支付失败");
                    this.addOrderCauseOfFailure(orderCode, "支付失败", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("03".equals(orderStatus)){
                    LOG.error("初始未支付");
                    this.addOrderCauseOfFailure(orderCode, "初始未支付", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("99".equals(orderStatus)){
                    LOG.error("支付超时");
                    this.addOrderCauseOfFailure(orderCode, "支付超时", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }else if("04".equals(orderStatus)){
                    LOG.error("支付处理中");
                    this.addOrderCauseOfFailure(orderCode, "支付处理中，请稍后查询", ip);
                    PrintWriter pw = response.getWriter();
                    pw.print("000000");
                    pw.close();
                }
            } else {
                LOG.error("签名验证失败");
                this.addOrderCauseOfFailure(orderCode, "签名验证失败", ip);
                PrintWriter pw = response.getWriter();
                pw.print("000000");
                pw.close();
            }
        }catch (Exception e){
            LOG.info("没有该元素");
        }
    }

    /**
     * 查询交易
     *
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/conQuery")
    public @ResponseBody Object QueryTransactions(@RequestParam(value = "orderCode") String orderCode) {

        String url = requestURL + "610008";
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        // 公共参数
        String orderId = getRandom();
        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610008");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        head.put("partnerType","OUTER");
        map.put("head", head);

        // 业务参数
        map.put("consumeOrderId", orderCode);
        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富查询请求地址:" + url);

        Object resultJson = null;

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            resultJson = JSON.toJSONString(result);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String statusDesc = jsonobj.getString("statusDesc");
            String headJson = jsonobj.getString("head");
            JSONObject HJson = JSONObject.parseObject(headJson);
            String message = HJson.getString("respMsg");
            String respCode = HJson.getString("respCode");
            // 01成功  02 失败 03 初始未支付  04 处理中
            String status = jsonobj.getString("status");
            LOG.info("返回描述：" + statusDesc);
            if ("01".equals(status)) {
                LOG.info("订单执行成功==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else if ("04".equals(status)) {
                LOG.info("订单处理中==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else if ("02".equals(status)) {
                LOG.info("订单执行失败==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else if ("03".equals(status)) {
                LOG.info("订单关闭==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else {
                LOG.info("订单号不存在==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "请求支付异常");
            return maps;
        }
    }
    /**
     * 付款61000
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/toreppay")
    public @ResponseBody Object toSettle(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("进入即富还款==========");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String extraFee=prp.getExtraFee();
        String amount = prp.getRealAmount();
        //即富通道出款逻辑，传参金额须+手续费，后通道方会进行处理，实际到账金额会扣除手续费
        amount=new BigDecimal(amount).add(new BigDecimal(extraFee)).setScale(2,BigDecimal.ROUND_DOWN).toString();
        String rip = prp.getIpAddress();
        String  extra   = prp.getExtra();
        LOG.info("进入即富还款=========="+prp.toString());

        Map<String, Object> maps = new HashMap<String, Object>();
        // 获取平台商户号
        JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
        String MerchantNo = jf.getMerchantNo();
        String rate = prp.getRate();
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
        // 获取开卡机构号
        JFBindCard jfBindCard = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
        String openCardId = jfBindCard.getOpenCardid();

        String url = requestURL + "610010";
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        String orderId = getRandom();

        head.put("charset", UTF_8);
        head.put("version", "1.0.0");
        head.put("partnerNo", partnerNo);
        head.put("partnerType","");
        head.put("txnCode", "610010");
        head.put("orderId", orderCode);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        map.put("head", head);
        // 业务参数
        map.put("rate",bigRate);
        map.put("platMerchantCode", MerchantNo);
        map.put("openCardId", openCardId);
        map.put("amount", getNumber(amount));
        map.put("remark", "大额代还");
        map.put("productCode", "QUICKPAY_WK_KB");
        map.put("backUrl", ip + "/v1.0/paymentgateway/repayment/jfdh/settleNotifyUrl");
        map.put("pingtai",orderCode);

        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderCode);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富支付短信请求地址:" + url);

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String workId = jsonobj.getString("workId");
            String rsHead = jsonobj.getString("head");
            JSONObject headJson = JSONObject.parseObject(rsHead);
            String message = headJson.getString("respMsg");
            String respCode = headJson.getString("respCode");
            String requestNo = headJson.getString("orderId");
            LOG.info("第三方流水号:" + requestNo);
            LOG.info("返回平台流水号：" + workId);
            if ("000000".equals(respCode)) {
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String URL = null;
                String results = null;
                URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/thirdordercode";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code", requestNo);
                try {
                    results = restTemplate.postForObject(URL, requestEntity, String.class);
                    LOG.info("*********************下单成功，添加第三方流水号***********************");
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("",e);
                }
                LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + results);
                maps.clear();
                maps.put(CommonConstants.RESP_CODE, "999998");
                maps.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行出款");
                maps.put("orderId", workId);
                return maps;
            } else {
                map.clear();
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, message);
                LOG.info("快捷支付短信---异常：" + message);
                return maps;
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.clear();
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "请求支付失败");
            return maps;
        }
    }


    /**
     * 支付订单查询
     * @param orderCode
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value ="/v1.0/paymentgateway/repayment/jfdh/reppayQuery")
    public @ResponseBody Object Queryreppay(@RequestParam(value = "orderCode") String orderCode) {

        String url = requestURL + "610011";
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        // 公共参数
        String orderId = getRandom();
        head.put("version", "1.0.0");
        head.put("charset", UTF_8);
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "610011");
        head.put("orderId", orderId);// 18-32位纯数字
        head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
        head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
        head.put("partnerType","");
        map.put("head", head);

        // 业务参数
        map.put("orderId", orderCode);
        // 发送
        String jsonStr = JSON.toJSONString(map);
        LOG.info("请求明文：" + jsonStr);
        String signData = getSign(key, jsonStr);
        String encryptData = getEncrypt(key, jsonStr);

        Map<String, String> params = Maps.newHashMap();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("orderId", orderId);
        params.put("partnerNo", partnerNo);
        params.put("ext", "");

        LOG.info("params : " + JSON.toJSONString(params));

        LOG.info("============ 即富查询请求地址:" + url);

        Object resultJson = null;

        try {
            byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
            if (resByte == null) {
                return "请求超时";
            }
            String resStr = new String(resByte, UTF_8);
            System.out.println("============ 返回报文原文:" + resStr);
            JSONObject resJson = JSON.parseObject(resStr);
            String sign = resJson.getString("signature");
            String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
            boolean signChecked = Objects.equals(sign.toUpperCase(),
                    DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
            Map<String, Object> result = new HashMap<>();
            result.put("返回源报文", resStr);
            result.put("返回明文", res);
            result.put("验签结果", signChecked);
            resultJson = JSON.toJSONString(result);
            LOG.info("返回明文：" + res);
            LOG.info("返回验签结果：" + signChecked);
            LOG.info("返回源报文：" + resStr);
            JSONObject jsonobj = JSONObject.parseObject(res);
            String statusDesc = jsonobj.getString("statusDesc");
            String headJson = jsonobj.getString("head");
            JSONObject HJson = JSONObject.parseObject(headJson);
            String message = HJson.getString("respMsg");
            String respCode = HJson.getString("respCode");
            String status = jsonobj.getString("status");
            LOG.info("返回描述：" + statusDesc);
            if ("01".equals(status)) {
                LOG.info("订单执行成功==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else if ("04".equals(status)) {
                LOG.info("订单处理中==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else if ("02".equals(status)) {
                LOG.info("订单执行失败==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            } else {
                LOG.info("订单号不存在==================");
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
                return maps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "请求支付失败");
            return maps;
        }
    }
    /**
     * 生成签名
     *
     * @param key
     * @param plainData
     * @return
     */
    public static String getSign(String key, String plainData) {

        return DigestUtils.sha1Hex(plainData + key.substring(16));
    }

    /**
     * 生成报文
     *
     * @param key
     * @param plainData
     * @return
     */
    public static String getEncrypt(String key, String plainData) {

        return Base64.encode(AES.encode(plainData, key.substring(0, 16)));
    }

    /**
     * 生成时间格式
     *
     * @param timeType
     * @return
     */
    public static String TimeFormat(String timeType) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeType);
        String nowTime = simpleDateFormat.format(new Date());
        LOG.info("当前时间：" + nowTime);
        return nowTime;

    }

    /**
     * 生成18位数订单号 当前时间：yyyyMMddHHmmss + 4位随机数
     *
     * @return
     */
    public static String getRandom() {

        String result = "";

        result += TimeFormat("yyyyMMddHHmmss");

        Double rand = Math.random() * 10000;

        if (rand < 10) {

            result += "000" + rand.toString().substring(0, 1);

        } else if (rand < 100) {

            result += "00" + rand.toString().substring(0, 2);

        } else if (rand < 1000) {

            result += "0" + rand.toString().substring(0, 3);

        } else {

            result += rand.toString().substring(0, 4);
        }
        LOG.info("18位数：" + result);

        return result;

    }

    /**
     * 金额/分
     *
     * @param ExtraFee
     * @return
     */
    public static String getNumber(String ExtraFee) {
        BigDecimal num1 = new BigDecimal(ExtraFee);
        BigDecimal num2 = new BigDecimal("100");
        BigDecimal rsNum = num1.multiply(num2);
        BigDecimal MS = rsNum.setScale(0, BigDecimal.ROUND_DOWN);
        LOG.info("金额/分：" + MS.toString());
        return MS.toString();
    }

}
