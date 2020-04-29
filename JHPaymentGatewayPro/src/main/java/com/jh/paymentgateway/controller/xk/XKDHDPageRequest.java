package com.jh.paymentgateway.controller.xk;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.HQXpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.xkdhd.XKArea;
import com.jh.paymentgateway.pojo.xkdhd.XKBankType;
import com.jh.paymentgateway.pojo.xkdhd.XKDHDBindCard;
import com.jh.paymentgateway.pojo.xkdhd.XKDHDRegister;
import com.jh.paymentgateway.util.Base64;
import com.jh.paymentgateway.util.xk.GyfConfig;
import com.jh.paymentgateway.util.xk.PayAction;
import com.jh.paymentgateway.util.xk.PayConfig;
import com.jh.paymentgateway.util.xk.SignMessageUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v2_1.functions.Str;
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
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@EnableAutoConfiguration
public class XKDHDPageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(HQXpageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ip;


    /**
     * 还款对接接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/Dockentrance")
    public @ResponseBody
    Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "idCard") String idCard,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "userName") String userName,
                        @RequestParam(value = "bankName") String bankName,
                        @RequestParam(value = "securityCode") String securityCode,
                        @RequestParam(value = "expiredTime") String expiredTime,
                        @RequestParam(value = "rate") String rate,
                        @RequestParam(value = "extraFee") String extraFee) throws Exception {

        LOG.info("开始进行进件绑卡操作====================");

        XKDHDRegister xkdhdRegister = topupPayChannelBusiness.getXKDHDRegisterByIdCard(idCard);
        XKDHDBindCard xkdhdBindCard = topupPayChannelBusiness.getXKDHDBindCardByBankCard(bankCard);
        Map<String, String> map = new HashMap<>();

        if (xkdhdRegister == null) {
            //进件注册     直接跳转到绑卡页面

            LOG.info("跳转进件绑卡页面===============");
            map.put(CommonConstants.RESP_CODE,  "999996");
            map.put(CommonConstants.RESP_MESSAGE, "跳转到绑卡页面");
            //跳转到结算卡页面
            map.put(CommonConstants.RESULT,
                    ip + "/v1.0/paymentgateway/quick/xk/jump-Receivablescard-view?bankName="
                            + URLEncoder.encode(bankName, "UTF-8")
                            + "&bankCard=" + bankCard
                            + "&cardName=" + URLEncoder.encode(bankName, "UTF-8")
                            + "&userName=" + URLEncoder.encode(userName, "UTF-8")
                            + "&expiredTime=" + expiredTime
                            + "&securityCode=" + securityCode
                            + "&extraFee=" + extraFee
                            + "&idCard=" + idCard
                            + "&rate=" + rate
                            + "&phone=" + phone
                            + "&ipAddress=" + ip + "&isRegister=1");
            return map;
        }


        if (xkdhdBindCard == null) {
            LOG.info("跳转进件绑卡页面=========================");
            map.put(CommonConstants.RESP_CODE, "999996");
            map.put(CommonConstants.RESP_MESSAGE, "跳转到绑卡页面");
            //跳转到结算卡页面
            map.put(CommonConstants.RESULT,
                    ip + "/v1.0/paymentgateway/quick/xk/jump-Receivablescard-view?bankName="
                            + URLEncoder.encode(bankName, "UTF-8")
                            + "&bankCard=" + bankCard
                            + "&cardName=" + URLEncoder.encode(bankName, "UTF-8")
                            + "&userName=" + URLEncoder.encode(userName, "UTF-8")
                            + "&expiredTime=" + expiredTime
                            + "&securityCode=" + securityCode
                            + "&extraFee=" + extraFee
                            + "&idCard=" + idCard
                            + "&rate=" + rate
                            + "&phone=" + phone
                            + "&ipAddress=" + ip + "&isRegister=1");
            return map;
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    /**
     * 跳转结算卡页面
     *
     * @param request
     * @param response
     * @param model
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/xk/jump-Receivablescard-view")
    public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        LOG.info("/v1.0/paymentgateway/quick/xk/jump-Receivablescard-view=========toxkbindcard");
        String bankName = request.getParameter("bankName");// 结算卡银行名称
        String bankCard = request.getParameter("bankCard");// 信用卡的卡类型
        String cardName = request.getParameter("cardName");// 充值卡卡号
        String expiredTime = request.getParameter("expiredTime");// 充值卡银行名称
        String securityCode = request.getParameter("securityCode");
        String extraFee = request.getParameter("extraFee");
        String idCard = request.getParameter("idCard");
        String rate = request.getParameter("rate");
        String phone = request.getParameter("phone");
        String ipAddress = request.getParameter("ipAddress");
        String isRegister = request.getParameter("isRegister");
        String userName = request.getParameter("userName");


        model.addAttribute("bankName", bankName);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("cardName", cardName);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("isRegister", isRegister);
        model.addAttribute("extraFee", extraFee);
        model.addAttribute("idCard", idCard);
        model.addAttribute("rate", rate);
        model.addAttribute("phone", phone);
        model.addAttribute("userName", userName);
        model.addAttribute("ipAddress", ipAddress);

        return "xkbindcard";
    }

    /**
     * 协议申请接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/register")
    public @ResponseBody
    Object register(
            @RequestParam(value = "bankCard") String bankCard,
            @RequestParam(value = "expiredTime") String expiredTime,
            @RequestParam(value = "bankName") String bankName,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "securityCode") String securityCode,
            @RequestParam(value = "extraFee") String extraFee
    ) {
        LOG.info("开始XKDHD签约===================");
        Map<String, Object> map = new HashMap<>();
        Map<String, String> parms = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNo = format.format(new Date());
        parms.put("orderNo", orderNo);//订单号
        parms.put("note", "绑定协议申请");//绑定协议申请
        parms.put("treatyType", "12"); // 协议类型：11：借记卡扣款  12；信用卡扣款
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        String startDate = format1.format(new Date());
        parms.put("startDate", startDate); //协议生效时间：yyyyMMdd
        String endDate = "20" + expiredTime + "01";
        parms.put("endDate", endDate);//协议失效时间：yyyyMMdd
        XKBankType xkBankType = topupPayChannelBusiness.getXKBankTypeByBankName(bankName);
        if(xkBankType == null){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该银行不支持，请联系管理员");
            return map;
        }
        String bankType = xkBankType.getBankType();
        parms.put("bankType", bankType);//银行类别；附录
        parms.put("bankCardNo", bankCard);//银行卡号
        parms.put("holderName", userName);//持卡人姓名
        parms.put("mobileNo", phone);//手机号
        parms.put("certificateNo", idCard);//身份证号
        parms.put("custCardValidDate", mmyyToyymm(expiredTime));//有效期mmyy
        parms.put("custCardCvv2", securityCode);//cvv2
        LOG.info("小卡大额签约请求参数："+ com.alibaba.fastjson.JSONObject.toJSONString(parms));
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.registerUrl);
        LOG.info("小卡大额签约返回参数为："+ com.alibaba.fastjson.JSONObject.toJSONString(respMap));
        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");
        if ("00".equals(code)) {
            XKDHDRegister register = new XKDHDRegister();
            register.setBankCard(bankCard);
            register.setBankType(bankType);
            register.setCreateTime(new Date());
            register.setExpiredTime(expiredTime);
            register.setExtraFee(extraFee);
            register.setIdCard(idCard);
            register.setOrderNo(orderNo);
            register.setPhone(phone);
            register.setUserName(userName);
            register.setRate(rate);
            register.setSecurityCode(securityCode);
            Map<String, Object> result = (Map<String, Object>) respMap.get("respMap");
            String smsSeq = (String) result.get("smsSeq");
            register.setSmsSeq(smsSeq);
            topupPayChannelBusiness.createXKDHDRxegister(register);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }
    /**
     * 将mmyy转化为yymm
     *
     * @param expiredTime
     * @return
     */
    private String mmyyToyymm(String expiredTime) {
        String mm = expiredTime.substring(0, 2);
        String yy = expiredTime.substring(2, 4);
        return yy + mm;
    }


    /**
     * 绑卡
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/bindCardVerify")
    public @ResponseBody
    Object bindCardVerify(
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "extraFee") String extraFee,
            @RequestParam(value = "authCode") String authCode
    ) {
        LOG.info("开始XKDHD签约确认====================");
        Map<String, Object> map = new HashMap<>();

        XKDHDRegister xkdhdRegister = topupPayChannelBusiness.getXKDHDRegisterByIdCard(idCard);
        String orderNo = xkdhdRegister.getOrderNo();
        String smsSeq = xkdhdRegister.getSmsSeq();
        String userName = xkdhdRegister.getUserName();
        String bankCard = xkdhdRegister.getBankCard();
        String expiredTime = xkdhdRegister.getExpiredTime();
        String securityCode = xkdhdRegister.getSecurityCode();

        Map<String, String> parms = new HashMap<>();
        parms.put("orderNo", orderNo);//绑定订单号：同原申请订单
        parms.put("smsSeq", smsSeq);//短信流水号
        parms.put("authCode", authCode);//短信验证码
        parms.put("holderName", userName);//持卡人姓名
        parms.put("bankCardNo", bankCard);//银行卡号
        parms.put("custCardValidDate", mmyyToyymm(expiredTime));//信用卡有效期 协议类型：12信用卡扣款-必填
        parms.put("custCardCvv2", securityCode);//信用卡的cvv2协议类型：12信用卡扣款-必填
        LOG.info("小卡大额确认签约请求参数："+ com.alibaba.fastjson.JSONObject.toJSONString(parms));
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.bindCardVerifyUrl);
        LOG.info("小卡大额确认签约返回参数为："+ com.alibaba.fastjson.JSONObject.toJSONString(respMap));
        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");


        if ("00".equals(code)) {
            XKDHDBindCard bindCard = new XKDHDBindCard();
            bindCard.setAuthCode(authCode);
            bindCard.setBankCard(bankCard);
            bindCard.setCreateTime(new Date());
            bindCard.setExpiredTime(expiredTime);
            bindCard.setIdCard(idCard);
            bindCard.setOrderNo(orderNo);
            bindCard.setSmsSeq(smsSeq);
            bindCard.setUserName(userName);
            bindCard.setSecurityCode(securityCode);
            Map<String, Object> result = (Map<String, Object>) respMap.get("respMap");
            String treatyId = (String) result.get("treatyId");
            bindCard.setTreatyId(treatyId);
            topupPayChannelBusiness.createXKDHDBindCard(bindCard);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }


    /**
     * 消费接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/topay")
    public @ResponseBody
    Object topay(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("开始执行消费计划================");

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String bankCard = prp.getBankCard();
        String amount = prp.getAmount();
        String idCard = prp.getIdCard();
        String ipAddress = prp.getIpAddress();
        String rate = prp.getRate();
        String extra = prp.getExtra();//消费计划|福建省-泉州市-350500
        String cityCode = extra.split("-")[2];

        XKDHDBindCard bindCard = topupPayChannelBusiness.getXKDHDBindCardByBankCard(bankCard);
        XKDHDRegister register = topupPayChannelBusiness.getXKDHDRegisterByIdCard(idCard);
        String treatyId = bindCard.getTreatyId();
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).toString();
        Map<String, String> map = new HashMap<>();
        Map<String, String> parms = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNo = format.format(new Date());
        parms.put("orderNo", orderCode);//支付订单号
        parms.put("treatyNo", treatyId);//协议号：协议确认接口获得
        parms.put("amount", amount);// 交易金额：元
        parms.put("rateAmount", bigRate);//手续费：百分比
        parms.put("notify_url", ip + "/v1.0/paymentgateway/topup/xkdhd/pay/call_back");//异步通知地址
        parms.put("bankType", register.getBankType());//银行行别：附录
        parms.put("holderName", prp.getUserName());//	持卡人真实姓名
        parms.put("bankCardNo", prp.getBankCard());//	银行卡号
        parms.put("sourceIP", ipAddress);//	用户IP
        parms.put("custCardValidDate", mmyyToyymm(prp.getExpiredTime()));//	信用卡有效期  协议类型：12信用卡扣款-必填
        parms.put("custCardCvv2", prp.getSecurityCode());//	信用卡的cvv  协议类型：12信用卡扣款-必填

        parms.put("cityCode", cityCode);//城市编码：附录（落地）
        LOG.info("小卡代还消费请求参数：" + com.alibaba.fastjson.JSONObject.toJSONString(parms));
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.bindCardPayUrl);
        LOG.info("小卡代还消费响应参数：" + com.alibaba.fastjson.JSONObject.toJSONString(respMap));
        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");
        if ("00".equals(code)) {
            LOG.info("消费计划执行成功============");
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        } else {
            LOG.info("消费计划失败==============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }


    /**
     * 消费接口回调
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/xkdhd/pay/call_back")
    public String fayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.info("小卡代还消费支付异步回调进来了======");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }

        String tranData = request.getParameter("tranData");
        tranData = new String(Base64.decode(tranData));
        LOG.info("异步回调的参数为：" + tranData);
        com.alibaba.fastjson.JSONObject jsonObjectresp = com.alibaba.fastjson.JSONObject.parseObject(tranData);
        String payFlowNo = jsonObjectresp.getString("payFlowNo");
        String orderNo = jsonObjectresp.getString("orderNo");
        String transAmt = jsonObjectresp.getString("transAmt");
        String feeAmt = jsonObjectresp.getString("feeAmt");
        String payStatus = jsonObjectresp.getString("payStatus");
        String signature = jsonObjectresp.getString("signature");
        boolean b = SignMessageUtil.verifyMessage(tranData, PayConfig.KEY, PayConfig.SIGNTYPE);
        if(!b){
            System.out.println("订单"+orderNo + "异步回调验签失败");
        }
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
        String channelTag = prp.getChannelTag();
        String ipAddress = prp.getIpAddress();

        if ("01".equals(payStatus)) {

            LOG.info("支付成功=============");
            String version = "33"; // TODO 修改通道标识
            LOG.info("version======" + version);
            RestTemplate restTemplate = new RestTemplate();

            String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", orderNo);
            requestEntity.add("version", version);

            String result = null;
            JSONObject jsonObject;
            JSONObject resultObj;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                return "SUCCESS";
            }

            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            // url = prp.getIpAddress() +
            // "/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", orderNo);
            requestEntity.add("third_code", payFlowNo); // 第三方订单号
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
            }

            LOG.info("订单状态修改成功===================" + orderNo + "====================" + result);

            LOG.info("订单已支付!");

            return "SUCCESS";

        } else if ("00".equals(payStatus)) {
            LOG.info("交易未支付!");

            return "SUCCESS";
        } else if ("02".equals(payStatus)) {

            LOG.info("交易处理失败!");
            addOrderCauseOfFailure(orderNo, "交易处理失败", ipAddress);
            return "SUCCESS";
        } else if ("03".equals(payStatus)) {
            LOG.info("交易支付中!");

            return null;
        } else {
            LOG.info("交易已关闭");

            return "SUCCESS";
        }
    }



    /**
     * 还款接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/transfer")
    public @ResponseBody
    Object transfer(
            @RequestParam(value = "orderCode") String orderCode
    ) {

        LOG.info("开始执行还款任务======================");

        Map<String, String> map = new HashMap<>();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String amount = prp.getAmount();
        String bankCard = prp.getBankCard();
        String userName = prp.getUserName();
        String expiredTime = prp.getExpiredTime();
        String securityCode = prp.getSecurityCode();
        String extraFee = prp.getExtraFee();

        XKDHDRegister register = topupPayChannelBusiness.getXKDHDRegisterByIdCard(idCard);
        String bankType = register.getBankType();


        Map<String, String> parms = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNo = format.format(new Date());
        parms.put("orderNo", orderCode);//商户订单号
        parms.put("notify_url", ip +"/v1.0/paymentgateway/topup/xkdhd/repay/call_back");//回调地址
        parms.put("tradeName", "代付到信用卡");//交易内容
        parms.put("amount", amount);//代付金额，单位：元
        parms.put("rateAmount", extraFee);//代付金额，单位：元
        parms.put("custBankAccountNo", bankCard);//客户银行账户号
        parms.put("custName", userName);//客户姓名
        parms.put("custBankNo", bankType);//银行行别码（见附录）
        parms.put("custAccountCreditOrDebit", "2");//客户账户类型 1-借记 2-贷记 4-未知
        parms.put("custCardValidDate", mmyyToyymm(expiredTime));//客户信用卡有效期
        parms.put("custCardCvv2", securityCode);//客户信用卡的cvv2
        parms.put("custID", idCard);//客户证件号码
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.repay);
        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");

        if ("00".equals(code)) {
            LOG.info("还款计划执行成功===========");
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;

        } else {
            LOG.info("还款计划执行失败=========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }


    /**
     * 还款接口回调
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/xkdhd/repay/call_back")
    public String refayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.info("还款支付异步回调进来了======");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String tranData = request.getParameter("tranData");
        tranData = new String(Base64.decode(tranData));
        LOG.info("异步回调的参数为：" + tranData);
        com.alibaba.fastjson.JSONObject jsonObjectresp = com.alibaba.fastjson.JSONObject.parseObject(tranData);
        String payFlowNo = jsonObjectresp.getString("payFlowNo");
        String orderNo = jsonObjectresp.getString("orderNo");
        String transAmt = jsonObjectresp.getString("transAmt");
        String feeAmt = jsonObjectresp.getString("feeAmt");
        String payStatus = jsonObjectresp.getString("payStatus");
        String signature = jsonObjectresp.getString("signature");
        boolean b = SignMessageUtil.verifyMessage(tranData, PayConfig.KEY, PayConfig.SIGNTYPE);
        if(!b){
            System.out.println("订单"+orderNo + "异步回调验签失败");
        }
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
        String channelTag = prp.getChannelTag();
        String ipAddress = prp.getIpAddress();

        if ("01".equals(payStatus)) {

            LOG.info("支付成功=============");
            String version = "33"; // TODO 修改通道标识
            LOG.info("version======" + version);
            RestTemplate restTemplate = new RestTemplate();

            String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", orderNo);
            requestEntity.add("version", version);

            String result = null;
            JSONObject jsonObject;
            JSONObject resultObj;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                return "SUCCESS";
            }

            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            // url = prp.getIpAddress() +
            // "/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", orderNo);
            requestEntity.add("third_code", payFlowNo); // 第三方订单号
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                return "SUCCESS";
            }

            LOG.info("订单状态修改成功===================" + orderNo + "====================" + result);

            LOG.info("订单已支付!");

            return "SUCCESS";

        } else if ("00".equals(payStatus)) {
            LOG.info("交易未支付!");

            return "SUCCESS";
        } else if ("02".equals(payStatus)) {

            LOG.info("交易处理失败!");
            addOrderCauseOfFailure(orderNo, "交易处理失败", ipAddress);

            return "SUCCESS";
        } else if ("03".equals(payStatus)) {
            LOG.info("交易支付中!");

            return null;
        } else {
            LOG.info("交易已关闭");

            return "SUCCESS";
        }
    }


    /**
     * 用户资金查询
     *
     * @param idCard
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/queryCustBalance")
    public @ResponseBody
    Object queryCustBalance(
            @RequestParam(value = "idCard") String idCard
    ) {
        LOG.info("开始用户资金查询=========");
        Map<String, Object> map = new HashMap<>();
        Map<String, String> parms = new HashMap<>();
        parms.put("custID", idCard);
        parms.put("pageNum", "1");
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.queryCustBalance);

        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");
        if ("00".equals(code)) {
            LOG.info("用户资金查询成功==========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        } else {
            LOG.info("用户资金查询失败=============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }

    /**
     * 支付结果查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/payOrderQuery")
    public @ResponseBody
    Object payOrderQuery(
            @RequestParam(value = "orderCode") String orderCode
    ) {
        LOG.info("开始支付结果查询=============");
        Map<String, String> map = new HashMap<>();

        Map<String, String> parms = new HashMap<>();

        parms.put("orderNo", orderCode);//订单号
        LOG.info("小卡代还大额支付查询请求参数：" + orderCode);
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.payOrderQueryURl);
        LOG.info("小卡代还大额支付查询响应参数：" + com.alibaba.fastjson.JSONObject.toJSONString(respMap));
        String code = (String) respMap.get("responseCode");

        String responseMsg = (String) respMap.get("responseMsg");

        if ("00".equals(code)) {
            LOG.info("支付结果查询成功===========");
            String respMap1=  respMap.get("respMap").toString();
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(respMap1);
            String payStatus = jsonObject.getString("payStatus");
            if("01".equals(payStatus)){
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功");
            }else if("03".equals(payStatus)){
                map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("respDesc"));
            }else{
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("respDesc"));
            }
            return map;
        } else {
            LOG.info("支付结果查询失败============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }

    /**
     * 还款结果查询
     *
     * @param orderCode
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/repayOrderQuery")
    public @ResponseBody
    Object repayOrderQuery(
            @RequestParam(value = "orderCode") String orderCode
    ) {

        LOG.info("开始还款结果查询============");
        Map<String, String> map = new HashMap<>();

        Map<String, String> parms = new HashMap<>();
        parms.put("orderNo", orderCode);
        LOG.info("小卡代还大额还款查询请求参数：" + orderCode);
        Map<String, Object> respMap = PayAction.pay(parms, GyfConfig.repayOrderQueryUrl);
        LOG.info("小卡代还大额还款查询响应参数：" + com.alibaba.fastjson.JSONObject.toJSONString(respMap));
        String code = (String) respMap.get("responseCode");
        String responseMsg = (String) respMap.get("responseMsg");

        if ("00".equals(code)) {
            String respMap1=  respMap.get("respMap").toString();
            LOG.info("还款结果查询成功===========");
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(respMap1);
            String payStatus = jsonObject.getString("payStatus");
            if("01".equals(payStatus)){
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "还款成功");
            }else if("03".equals(payStatus)){
                map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("respDesc"));
            }else{
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("respDesc"));
            }
            return map;
        } else {
            LOG.info("还款结果查询失败===============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, responseMsg);
            return map;
        }
    }

    /**
     * 落地页面二级联动
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xkdhd/queryArea")
    public @ResponseBody
    Object queryArea(
            @RequestParam(value = "parentId") String parentId
    ) {

        if ("0".equals(parentId)) {
            LOG.info("开始查询省级城市==========");
        } else {
            LOG.info("开始查询市级城市===============");
        }

        Map<String, Object> map = new HashMap<>();
        List<XKArea> list = topupPayChannelBusiness.getXKAreaByParentId(parentId);
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT,list);
        map.put(CommonConstants.RESP_MESSAGE,"成功");
        return map ;


    }


}
