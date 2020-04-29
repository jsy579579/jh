package com.jh.paymentgateway.controller.xs.service;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.xs.daomapping.XSBindCardImpl;
import com.jh.paymentgateway.controller.xs.daomapping.XsXeaddresslmpl;
import com.jh.paymentgateway.controller.xs.daomapping.XsxeRegisterlmpl;
import com.jh.paymentgateway.controller.xs.pojo.TSXsxeAddress;
import com.jh.paymentgateway.controller.xs.pojo.XSXEBindCard;
import com.jh.paymentgateway.controller.xs.pojo.XsXeRegistr;
import com.jh.paymentgateway.controller.xs.util.Common;
import com.jh.paymentgateway.controller.xs.util.HttpClientUtil;
import com.jh.paymentgateway.controller.xs.util.MD5Utils;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@EnableAutoConfiguration
public class XsPageReques {

    @Autowired
    XSBindCardImpl xSBindCard;

    @Autowired
    private EntityManager em;
    //支付报文
    @Autowired
    private RedisUtil redisUtil;
    //redis服务
    @Autowired
    RedisUtil redis;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    XsXeaddresslmpl xsXeaddresslmpl;//落地
    @Autowired
    XsxeRegisterlmpl xsXeRegistrimpl;//进件
    @Autowired
    XsxeRegisterlmpl XsxeRegisterimpl;//绑卡


    @Value("${payment.ipAddress}")
    private String ip;
    @Value("${xs.Merchantsip}")
    private String Merchantsip;
    @Value("${xs.Merchants}")
    private String Merchants;

    @Value("${xs.kyes}")
    private String kyes;

    String frontUrl = "/v1.0/paymentgateway/channel/success";//前端回调地址
    String notifyUrl = "/v1.0/paymentgateway/xs/callback";//异步回调地址
    String rac = "/v1.0/paymentgateway/xs/repaymenttaskAndAgreeToConsumptionCallback";


    private static final Logger LOG = LoggerFactory.getLogger(XsPageReques.class);

    /**
     * 进件
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xsxe/Dockentrance")
    @ResponseBody
    public Object Dockentrance(@RequestParam(value = "userName") String username,
                               @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "bankCard") String bank_card,
                               @RequestParam(value = "idCard") String idcard) {
        Map<String, Object> maps = new HashMap<>();
        try {
            XsXeRegistr xsxeBindCard1 = xsXeRegistrimpl.queryByBankCard(bank_card);
            LOG.info("进件对象："+xsxeBindCard1);
            if (xsxeBindCard1 == null) {
                maps.put(CommonConstants.RESP_CODE, "999996");
                maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
                maps.put(CommonConstants.RESULT, this.xsXeBindCardSms(username, phone, bank_card, idcard, UUIDGenerator.getUUID()));
                return maps;
            }
            //判断是否有绑卡请求过，有的话就调上游查询接口查询绑卡进度
            if (xsxeBindCard1.getStatus() != null) {
                if (xsxeBindCard1.getStatus().equals("1")) {
                    maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    maps.put(CommonConstants.RESP_MESSAGE, "进件绑卡成功");
                    return maps;
                }
            }
        } catch (
                Exception e) {
            LOG.error("与还款对接接口出现异常======", e);
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "与还款对接失败");
            return maps;
        }
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE, "用户已经绑卡");
        return maps;
    }


    /**
     * 通道绑卡
     *
     * @param username
     * @param phone
     * @param bank_card
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/xsXe/bindCard")
    public Object xsXeBindCardSms(@RequestParam(value = "userName") String username,
                                  @RequestParam(value = "phone") String phone,
                                  @RequestParam(value = "bankCard") String bank_card,
                                  @RequestParam(value = "idCard") String idcard,
                                  @RequestParam(value = "order_code") String order_code) {
        HashMap<String, Object> map = new HashMap<>();
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        String status = null;
//        String pay_number = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Map params = new HashMap();
        params.put("cardNo", bank_card);
        params.put("merUserIp", ip+Merchantsip);
        params.put("frontUrl", ip+frontUrl);
        params.put("notifyUrl", notifyUrl);
        params.put("userid", Merchants);
        LOG.info("请求签约报文================" + params);
        /** 参与验签的字段 */
        String sign = md5Utils.getSignParam(params);
        sign = sign + "&key=" + kyes;
        LOG.info("计算签名的报文为===========：" + sign);
        sign = md5Utils.getKeyedDigest(sign, "");
        /** 上送的值为 */
        params.put("sign", sign);
        params.put("orderCode", "xs_dhFrontSign");
        params.put("pay_number", order_code);
        String baowen = md5Utils.getSignParam(params);
        LOG.info("上送的报文为================：" + baowen);
        String result = "";
        try {
            result = HttpClientUtil.sendPostRequest(Common.URL, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("返回报文为================：" + result);
        JSONObject jsonObject = JSON.parseObject(result);
        if (!"0000".equals(jsonObject.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.get("respInfo"));
            em.clear();
            xSBindCard.saveAndFlush(new XSXEBindCard(username, phone, bank_card, idcard, new Date(), "0"));
            return map;
        }
        em.clear();
        XSXEBindCard xsxeBindCard = xSBindCard.saveAndFlush(new XSXEBindCard(username, phone, bank_card, idcard, new Date(), "1"));
        redisTemplate.opsForValue().set(order_code, xsxeBindCard, 60 * 10, TimeUnit.SECONDS);
        LOG.info("绑卡对象==================================" + xsxeBindCard);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, jsonObject.get("html"));
        return JSON.toJSON(map);
    }

    @RequestMapping(value = "/v1.0/paymentgateway/channel/success")
    public String notifyurlSuccess() {
        return "xsxesuccess";
    }

    /**
     * 回调
     *
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/xs/callback")
    public void xsBindCardCallback(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("新生小额xs回调来了====================");
        Map parameterMap = null;
        try {
            parameterMap = getParamrterMap(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("==================================回调报文：" + parameterMap.toString());

        XSXEBindCard dto = (XSXEBindCard) redisTemplate.opsForValue().get(parameterMap.get("pay_number"));
        XsXeRegistr xsXeRegistr = new XsXeRegistr();
        if (!(parameterMap == null)) {
            xsXeRegistr.setUsername(parameterMap.containsKey("statusMsg") == true ?
                    parameterMap.get("statusMsg").toString() : "");
            xsXeRegistr.setUsername(dto.getUsername());
            xsXeRegistr.setPhone(dto.getPhone());
            xsXeRegistr.setBankCard(dto.getBankCard());
            xsXeRegistr.setIdCard(dto.getIdCard());
            xsXeRegistr.setPayProtocolNo(parameterMap.containsKey("payProtocolNo") == true ?
                    parameterMap.get("payProtocolNo").toString() : "");
            xsXeRegistr.setBankCode(parameterMap.containsKey("bankCode") == true ?
                    parameterMap.get("bankCode").toString() : "");
            xsXeRegistr.setBizProtocolNo(parameterMap.containsKey("bizProtocolNo") == true ?
                    parameterMap.get("bizProtocolNo").toString() : "");
            xsXeRegistr.setCreateTime(new Date());
            xsXeRegistr.setShortCardNo(parameterMap.containsKey("shortCardNo") == true ?
                    parameterMap.get("shortCardNo").toString() : "");
            if (!"0000".equals(parameterMap.get("status").toString())) {
                xsXeRegistr.setStatusmsg(parameterMap.get("statusMsg").toString());
                xsXeRegistr.setStatus(parameterMap.containsKey("status") == true ? "0" : "");
            }
            xsXeRegistr.setStatus(parameterMap.containsKey("status") == true ? "1" : "");
            xsXeRegistr.setPayNumber(parameterMap.containsKey("pay_number") == true ?
                    parameterMap.get("pay_number").toString() : "");

            LOG.info("==================================回调对象保存" + dto.toString());
            em.clear();
            xsXeRegistrimpl.saveAndFlush(xsXeRegistr);
            PrintWriter pw;
            try {
                pw = response.getWriter();
                pw.print("success");
                pw.close();
            } catch (IOException e) {
                LOG.info("==================================返回异常");
            }
        }
    }


    /**
     * 预消费
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/AdvanceConsumetask")
    @ResponseBody
    public Object Advanceconsumetask(@RequestParam(value = "oraderCode") String oraderCode) {
        PaymentRequestParameter bean = redis.getPaymentRequestParameter(oraderCode);//订单对象
        LOG.info("订单对象-----------------------------" + bean.toString());
        XsXeRegistr xsxeBindCard1 = xsXeRegistrimpl.queryByBankCard(bean.getBankCard());//签约对象
        LOG.info("订单对象-----------------------------" + xsxeBindCard1.toString());
        HashMap<String, Object> map = new HashMap<>();
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        Map params = new HashMap();
        params.put("payProtocolNo", xsxeBindCard1.getPayProtocolNo());
        params.put("bizProtocolNo", xsxeBindCard1.getBizProtocolNo());
        params.put("rate", bean.getRate());
        params.put("amount", bean.getAmount());
        // .substring(0,bean.getExtra().indexOf("-"))
        params.put("province", bean.getExtra().substring(bean.getExtra().indexOf("|") + 1, bean.getExtra().length()));
        params.put("notifyUrl", ip + rac);
        params.put("userid", Merchants);
        System.out.println("预消费入参=====" + params);
        /** 参与验签的字段 */
        String sign = md5Utils.getSignParam(params);
        sign = sign + "&key=" + kyes;
        LOG.info("计算签名的报文为：" + sign);
        sign = md5Utils.getKeyedDigest(sign, "");
        /** 上送的值为 */
        params.put("sign", sign);
        params.put("orderCode", "xs_dhReqPay");
        params.put("pay_number", bean.getOrderCode());
        String baowen = md5Utils.getSignParam(params);
        LOG.info("上送的报文为==================：" + baowen);
        String result = null;
        try {
            result = HttpClientUtil.sendPostRequest(Common.URL, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("预消费result==================：" + result);
        JSONObject payCallBacke = JSON.parseObject(result);
        if (!"0000".equals(payCallBacke.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "新生小额预消费错误=====错误描述为：" + payCallBacke.get("respInfo"));
            return map;
        }
        LOG.info("进入确认消费==================");
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(this.AgreeToConsumption(oraderCode, payCallBacke.get("gallery_number").toString())));
        if (!"0000".equals(jsonObject.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.get("respInfo"));
            return map;
        }
        map.put(CommonConstants.RESP_CODE, "999998");
        map.put(CommonConstants.RESP_MESSAGE, "消费成功,等待回调");
        return map;
    }

    /**
     * 确认消费
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/AgreeToConsumption")
    @ResponseBody
    public Object AgreeToConsumption(@RequestParam(value = "oraderCode") String oraderCode, String gallery_number) {
        PaymentRequestParameter bean = redis.getPaymentRequestParameter(oraderCode);//订单对象
        LOG.info("订单对象-----------------------------" + bean.toString());
        XsXeRegistr xsxeBindCard1 = xsXeRegistrimpl.queryByBankCard(bean.getBankCard());//签约对象
        LOG.info("订单对象-----------------------------" + xsxeBindCard1.toString());
        Map map = new HashMap<String, Object>();
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        Map params = new HashMap();
        params.put("gallery_number", gallery_number);
        params.put("userid", Merchants);
        params.put("merUserIp", Merchantsip);
        params.put("paymentTerminalInfo", "01|AA01BB");
        params.put("receiverTerminalInfo", "01|AA01BB|CN|");
        params.put("deviceInfo", Merchantsip + "||||||");
        params.put("accountIdHash", "|");
        /** 参与验签的字段 */
        String sign = md5Utils.getSignParam(params);
        sign = sign + "&key=" + kyes;
        LOG.info("计算签名的报文为：" + sign);
        sign = md5Utils.getKeyedDigest(sign, "");
        LOG.info("计算加密签名的报文为：" + sign);
        /** 上送的值为 */
        params.put("sign", sign);
        params.put("orderCode", "xs_dhConfirmPay");
        params.put("pay_number", bean.getOrderCode());
        params.put("merUserIp", Merchantsip);
        LOG.info("确认消费入参===================：" + params);
        String baowen = md5Utils.getSignParam(params);
        LOG.info("确认消费上送的报文为==================：" + baowen);
        String result = null;
        try {
            result = HttpClientUtil.sendPostRequest(Common.URL, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        LOG.info("确认消费参数result===============================" + result);
        JSONObject payCallBacke = JSON.parseObject(result);
        if (!"0000".equals(payCallBacke.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, payCallBacke.get("respInfo"));
            return map;
        }

        return payCallBacke;
    }

    /**
     * 余额查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/query/balance")
    @ResponseBody
    public Object repaymentQueryBalanece(@RequestParam(value = "brankCard") String brankCard) {
        XsXeRegistr xsxeBindCard1 = xsXeRegistrimpl.queryByBankCard(brankCard);//签约对象
        LOG.info("订单对象-----------------------------" + xsxeBindCard1.toString());
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        Map params = new HashMap();
        params.put("userid", Merchants);
        params.put("payProtocolNo", xsxeBindCard1.getPayProtocolNo());
        params.put("bizProtocolNo", xsxeBindCard1.getBizProtocolNo());
        System.out.println("=====" + params);
        /** 参与验签的字段 */
        String sign = md5Utils.getSignParam(params);
        sign = sign + "&key=" + kyes;
        System.out.println("计算签名的报文为：" + sign);
        sign = md5Utils.getKeyedDigest(sign, "");
        /** 上送的值为 */
        params.put("sign", sign);
        params.put("orderCode", "xs_dhBalanceQuery");
        params.put("pay_number", xsxeBindCard1.getPayNumber());
        String baowen = md5Utils.getSignParam(params);
        LOG.info("上送的报文为：" + baowen);
        String result = null;

        try {
            result = HttpClientUtil.sendPostRequest(Common.URL, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject repaymentBalance = JSON.parseObject(result);
        LOG.info("返回报文为：" + repaymentBalance.toJSONString());
        Map map = new HashMap<>();
        if (!"0000".equals(repaymentBalance.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, repaymentBalance.get("respInfo"));
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "balance：" + repaymentBalance.get("balance"));
        return map;
    }

    /**
     * 订单查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xsxe/query/order/status")
    @ResponseBody
    public Object consumeQueryBalanece(@RequestParam(value = "oraderCode") String oraderCode) {
        PaymentRequestParameter bean = redis.getPaymentRequestParameter(oraderCode);//订单对象
        LOG.info("订单对象-----------------------------" + bean.toString());
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        Map map = new HashMap<>();
        Map params = new HashMap();
        params.put("userid", Merchants);
        if ("10".equals(bean.getOrderType())) {
            LOG.info("=====" + params);
            /** 参与验签的字段 */
            String sign = md5Utils.getSignParam(params);
            sign = sign + "&key=" + kyes;
            System.out.println("计算签名的报文为：" + sign);
            sign = md5Utils.getKeyedDigest(sign, "");
            /** 上送的值为 */
            params.put("sign", sign);
            params.put("orderCode", "xs_dhOrderQuery");
            params.put("pay_number", bean.getOrderCode());
            String baowen = md5Utils.getSignParam(params);
            LOG.info("上送的报文为：" + baowen);
            //发起
            String result = null;
            try {
                result = HttpClientUtil.sendPostRequest(Common.URL, params);
                LOG.info("返回报文===============================" + result);

            } catch (Exception e) {
                LOG.info("请求发送异常===============================" + e);
            }
            return this.spltStr(JSON.parseObject(result), 10);//回调对象
        } else if ("11".equals(bean.getOrderType())) {
            LOG.info("=====" + params);
            /** 参与验签的字段 */
            String sign = md5Utils.getSignParam(params);
            sign = sign + "&key=" + kyes;
            System.out.println("计算签名的报文为：" + sign);
            sign = md5Utils.getKeyedDigest(sign, "");
            /** 上送的值为 */
            params.put("sign", sign);
            params.put("orderCode", "xs_dhWithdrawQuery");
            params.put("pay_number", bean.getOrderCode());
            String baowen = md5Utils.getSignParam(params);
            LOG.info("上送的报文为：" + baowen);
            //发起
            String result = null;
            try {
                result = HttpClientUtil.sendPostRequest(Common.URL, params);
                LOG.info("返回报文===============================" + result);

            } catch (Exception e) {
                LOG.info("请求发送异常===============================" + e);
            }
            return this.spltStr(JSON.parseObject(result), 11);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE, "无此订单号");
        return map;
    }

    /**
     * 代还
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/repaymenttask")
    @ResponseBody
    public Object repaymenttask(@RequestParam(value = "oraderCode") String oraderCode) {
        PaymentRequestParameter bean = redis.getPaymentRequestParameter(oraderCode);//订单对象
        LOG.info("订单对象-----------------------------" + bean.toString());
        XsXeRegistr xsxeBindCard1 = xsXeRegistrimpl.queryByBankCard(bean.getBankCard());//签约对象
        LOG.info("签约对象-----------------------------" + xsxeBindCard1.toString());
        // 发送 POST 请求
        MD5Utils md5Utils = new MD5Utils();
        Map params = new HashMap();
        params.put("userid", Merchants);
        params.put("cardNo", bean.getBankCard());
        params.put("identityCode", bean.getIdCard());
        params.put("bankName", bean.getCreditCardBankName());
        params.put("mobileNo", bean.getPhone());
        params.put("holderName", bean.getUserName());
        params.put("payProtocolNo", xsxeBindCard1.getPayProtocolNo());
        params.put("bizProtocolNo", xsxeBindCard1.getBizProtocolNo());
        params.put("amount", bean.getAmount());
        params.put("fee", bean.getExtraFee());
        params.put("notifyUrl", ip + rac);
        System.out.println("参与排序报文=====" + params);
        /** 参与验签的字段 */
        String sign = md5Utils.getSignParam(params);
        sign = sign + "&key=" + kyes;
        LOG.info("计算签名的报文为：" + sign);
        sign = md5Utils.getKeyedDigest(sign, "");
        LOG.info("计算加密签名的报文为：" + sign);
        /** 上送的值为 */
        params.put("sign", sign);
        params.put("orderCode", "xs_dhWithdraw");
        params.put("pay_number", bean.getOrderCode());
        String baowen = md5Utils.getSignParam(params);
        LOG.info("上送的报文为======================：" + baowen);
        String result = null;
        try {
            result = HttpClientUtil.sendPostRequest(Common.URL, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("代付响应参数===============================" + result);
        Map map = new HashMap<>();
        JSONObject payCallBacke = JSON.parseObject(result);
        if (!"0000".equals(payCallBacke.get("respCode"))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, payCallBacke.get("respInfo"));
            return map;
        }
        map.put(CommonConstants.RESP_CODE, "999998");
        map.put(CommonConstants.RESP_MESSAGE, "消费成功,等待回调");
        return map;
    }

    //回调
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/repaymenttaskAndAgreeToConsumptionCallback")
    public void repaymenttaskAndAgreeToConsumptionCallback(HttpServletRequest request, HttpServletResponse resp) {

        Map parameterMap = null;
        try {
            parameterMap = getParamrterMap(request);
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(parameterMap.get("pay_number").toString());
            if ("0000".equals(parameterMap.get("status"))) {
                LOG.info("*********************支付成功***********************");
                String version = null;
                if ("XSXE_QUICK".equalsIgnoreCase(prp.getChannelTag())) {
                    version = "57";
                }
                LOG.info("version======" + version);
                RestTemplate restTemplate = new RestTemplate();
                String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("orderCode", parameterMap.get("pay_number").toString());
                requestEntity.add("version", version);
                String result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                JSONObject taskstatus = JSON.parseObject(result);
                if ("".equals(taskstatus.getJSONObject("result"))) {
                    LOG.info("还款订单处理=======================" + taskstatus.toString());
                }
                url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", parameterMap.get("pay_number").toString());
                requestEntity.add("third_code", parameterMap.get("orderId").toString()); // 第三方订单号
                restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("订单状态修改成功===================" + parameterMap.get("pay_number").toString() + "====================" + result);
                LOG.info("订单已支付!");
                PrintWriter pw = resp.getWriter();
                pw.print("success");
                pw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/xs/query/repayment/address")
    @ResponseBody
    public Object getProvince(@RequestParam(value = "province", defaultValue = "0") String province) {
        Map map = new HashMap<>();
        List<TSXsxeAddress> tsXsxeAddress;
        if (province.equals("0")) {
            tsXsxeAddress = xsXeaddresslmpl.getTSXsxeAddress();
        } else {
            tsXsxeAddress = xsXeaddresslmpl.queryTSXsxeAddress(province);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, tsXsxeAddress);
        return map;
    }

    /**
     * 获取数据
     *
     * @param request
     * @return
     * @throws Exception
     */
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

        LOG.info("新生小额接收上游异步通知值：" + params);
        return params;
    }

    /**
     * 返回验证
     *
     * @param result
     * @return
     */
    public static Map spltStr(JSONObject result, Integer type) {
        Map map = new HashMap<>();
        if (type == 10) {
            if (!"0000".equals(result.get("respCode"))) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("respInfo"));
            }
            Object orderStatus = result.get("orderStatus");//回调状态
            if ("0".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                map.put(CommonConstants.RESP_MESSAGE, "交易已创建,等待出款");
            } else if ("1".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "交易成功");
            } else if ("2".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("orderFailedMsg"));
            } else if ("3".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("orderFailedMsg"));
            }
        } else {
            if (!"0000".equals(result.get("respCode"))) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("respInfo"));
            }
            Object orderStatus = result.get("orderStatus");//回调状态
            if ("00".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                map.put(CommonConstants.RESP_MESSAGE, "交易已创建,等待出款");
            } else if ("01".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "交易成功");
            } else if ("02".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("orderFailedMsg"));
            } else if ("03".equals(orderStatus)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, result.get("orderFailedMsg"));
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE, "未知订单");
        return map;
    }

}
