package com.jh.paymentgateway.controller.hqm.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.hqm.dao.HQMBusiness;
import com.jh.paymentgateway.controller.hqm.pojo.HQMRegister;
import com.jh.paymentgateway.controller.hqm.pojo.HQMbindCard;
import com.jh.paymentgateway.controller.hqm.util.HashMapConver;
import com.jh.paymentgateway.controller.hqm.util.SmartRepayChannel;
import com.jh.paymentgateway.pojo.HQERegion;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Controller
@EnableAutoConfiguration
public class HQMpageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(HQMpageRequest.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HQMBusiness topupPayChannelBusiness;

    @Autowired
    private TopupPayChannelBusiness t1;

    @Value("${payment.ipAddress}")
    private String ip;

    @Autowired
    Executor executor;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public static final String cardType = "02"; // 卡类型

    /**
     * 还款对接接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/Dockentrance")
    public @ResponseBody
    Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "idCard") String idCard,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "userName") String serName,
                        @RequestParam(value = "bankName") String bankName,
                        @RequestParam(value = "rate") String rate,
                        @RequestParam(value = "extraFee") String extraFee,
                        @RequestParam(value = "securityCode") String securityCode,
                        @RequestParam(value = "expiredTime") String expiredTime) throws Exception {
        HQMbindCard hqMbindCardByBankCard = topupPayChannelBusiness.findHQMbindCardByBankCard(bankCard);
        HQMRegister hqmRegisterByIdCard = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        Map<String, Object> maps = new HashMap<>();
        // 进件注册
        if (hqmRegisterByIdCard == null) {
            maps = (Map<String, Object>) this.hqmoRegister(bankCard, idCard, phone, serName, rate, extraFee);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }
        // 绑卡
        if (hqMbindCardByBankCard == null || "0".equals(hqMbindCardByBankCard.getStatus())) {
            maps = (Map<String, Object>) this.hqMoBindCard(bankCard, idCard, phone, serName, securityCode,
                    this.expiredTimeToMMYY(expiredTime));
            return maps;
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");

    }

    /**
     * 进件
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/HQMRegister")
    public @ResponseBody
    Object hqmoRegister(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "idCard") String idCard,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "userName") String userName,
                        @RequestParam(value = "rate") String rate,
                        @RequestParam(value = "extraFee") String extraFee) {

        LOG.info("开始进件======================");
        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        String extraFee1 = new BigDecimal(extraFee).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();

        Map<String, Object> map = new HashMap<>();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> param = HashMapConver.getOrderByMap();

        param.put("methodname", "register"); // 方法
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "hq" + ordersn;
        param.put("dsorderid", dsorderid); // 商户订单号
        param.put("bankcard", bankCard); // 银行卡号 //信用卡
        param.put("username", userName); // 真实姓名
        param.put("idcard", idCard);// 身份证号
        param.put("mobile", phone); // 预留手机号
        param.put("futureRateValue", rate1); // 扣款费率
        param.put("fixAmount", extraFee1); // 还款手续费
        LOG.info("/hqm/HQMRegister=====================" + param.toString());
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        String code = resultMap.get("returncode"); // 返回码
        String errtext = resultMap.get("errtext"); // 详情

        if ("0000".equals(code) || "0052".equals(code)) {
            String subMerchantNo = resultMap.get("subMerchantNo"); // 商户号

            HQMRegister hqmRegister = new HQMRegister();
            hqmRegister.setUserName(userName);
            hqmRegister.setIdCard(idCard);
            hqmRegister.setMerchantCode(subMerchantNo);
            hqmRegister.setPhone(phone);
            hqmRegister.setRate(rate);
            hqmRegister.setBankCard(bankCard);
            hqmRegister.setExtraFee(extraFee);
            topupPayChannelBusiness.createHQMRegister(hqmRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
            return map;
        }
    }


    /**
     * 绑卡接口
     *
     * @param bankCard
     * @param idCard
     * @param phone
     * @param userName
     * @param securityCode
     * @param expiredTime
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/bindCard")
    @ResponseBody
    public Object hqMoBindCard(@RequestParam(value = "bankCard") String bankCard,
                               @RequestParam(value = "idCard") String idCard,
                               @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "userName") String userName,
                               @RequestParam(value = "securityCode") String securityCode,
                               @RequestParam(value = "expiredTime") String expiredTime) {

        LOG.info("开始绑卡==================");
        HQMbindCard hqmbind = topupPayChannelBusiness.findHQMbindCardByBankCard(bankCard);
        Map<String, Object> map = new HashMap<>();

        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> param = HashMapConver.getOrderByMap();
        param.put("methodname", "bindCard"); // 方法名
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        param.put("ordersn", ordersn); // 流水号
        param.put("dsorderid", dsorderid); // 商户订单号
        HQMRegister hqmRegister = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        String subMerchantNo = hqmRegister.getMerchantCode();
        param.put("merchno", subMerchantNo);
        param.put("subMerchantNo", subMerchantNo); // 子商户号 register方法返回
        param.put("bankcard", bankCard); // 银行卡号
        param.put("username", userName);// 真实姓名
        param.put("idcard", idCard);// 身份证号
        param.put("mobile", phone);// 预留手机号
        param.put("cardType", "02"); // 卡类型 借记卡：01 贷记卡：02
        param.put("cvn2", securityCode); // CVN2 安全码 卡类型为贷记卡时必填
        param.put("expireDate", expiredTimeToMMYY(expiredTime)); // 信用卡有效期 格式：MMYY卡类型为贷记卡时必填
        param.put("returnUrl", ip + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");// 前台跳转地址
        param.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqm/bindcard/call_back");// 异步通知地址
        LOG.info("/hqm/bindCard=====================" + param.toString());
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        String code = resultMap.get("returncode");
        String errtext = resultMap.get("errtext");
        if ("0000".equals(code) || "0055".equals(code)) {
            if (resultMap.containsKey("bindId")) {
                String bindId = resultMap.get("bindId");
                LOG.info("存储本地绑卡orderid==================" + dsorderid);
                if(hqmbind == null){
                    hqmbind = new HQMbindCard();
                }
                hqmbind.setBankCard(bankCard);
                hqmbind.setCreateTime(new Date());
                hqmbind.setIdCard(idCard);
                hqmbind.setPhone(phone);
                hqmbind.setUserName(userName);
                hqmbind.setOrderId(dsorderid);
                hqmbind.setBindId(bindId);
                hqmbind.setStatus("1");
                topupPayChannelBusiness.createHQMbindCard(hqmbind);
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功");
                return map;
            }
            // 跳转绑卡银联页面
            if (resultMap.containsKey("bindUrl")) {
                String bindUrl = resultMap.get("bindUrl");
                LOG.info("存储本地绑卡orderid==================" + dsorderid);
                if (hqmbind == null) {
                    if(hqmbind == null){
                        hqmbind = new HQMbindCard();
                    }
                   hqmbind.setBankCard(bankCard);
                   hqmbind.setUserName(userName);
                   hqmbind.setCreateTime(new Date());
                   hqmbind.setIdCard(idCard);
                   hqmbind.setPhone(phone);
                   hqmbind.setOrderId(dsorderid);
                   hqmbind.setStatus("0");
                    topupPayChannelBusiness.createHQMbindCard(hqmbind);
                } else if ("0".equals(hqmbind.getStatus())) {
                    hqmbind.setOrderId(dsorderid);
                    topupPayChannelBusiness.createHQMbindCard(hqmbind);
                }
                map.put(CommonConstants.RESP_CODE, "999996");
                map.put(CommonConstants.RESP_MESSAGE, "请求绑卡成功,等待回调");
                map.put(CommonConstants.RESULT, bindUrl);
                redisTemplate.opsForValue().set(bankCard, bindUrl, 60 * 3, TimeUnit.SECONDS);
                return map;
            }

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }
        return map;
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess")
    public String jumpSuccessPages(HttpServletResponse response) {
        try {
            response.getWriter().println("SUCCESS");
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "xsxesuccess";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/v1.0/paymentgateway/topup/hqm/jumpPagesOn")
    public void jumpPagesOn(HttpServletResponse response, @RequestParam("bankCard") String bankCard) {
        response.setContentType("text/html;charset=utf-8");
        Object from = redisTemplate.opsForValue().get(bankCard);
        try {
            response.getWriter().println(from);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 绑卡回调
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/hqm/bindcard/call_back")
    public void bindcardNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOG.info("绑卡回调回来了！！！！！！！！！！！！！！");
        // TODO JSON 返回的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }

        String merchno = request.getParameter("merchno");// 商户号
        String paytime = request.getParameter("paytime");// 交易时间
        String status = request.getParameter("status"); // 状态 00:成功，02:失败
        String orderid = request.getParameter("orderid");// 我司订单号
        String dsorderid = request.getParameter("dsorderid"); // 商户订单号
        String subMerchantNo = request.getParameter("subMerchantNo");// 子商户号
        String bindId = request.getParameter("bindId");// 绑卡标识
        String sign = request.getParameter("sign");// 加密校验值
        LOG.info("返回参数=====" + merchno + paytime + status + orderid + dsorderid + subMerchantNo + bindId + sign);

        LOG.info("请求绑卡流水号dsorderid-----------" + dsorderid);
        LOG.info("请求绑卡商户号merchno-----------" + merchno);
        LOG.info("status-----------" + status);

        if ("00".equals(status)) {
            LOG.info("*********************绑卡成功***********************");
            HQMbindCard hqMbindCard = topupPayChannelBusiness.getHQXBindCardByOrderId(dsorderid);
            hqMbindCard.setBindId(bindId);
            hqMbindCard.setStatus("1");
            hqMbindCard.setUpdateTime(new Date());
            topupPayChannelBusiness.createHQMbindCard(hqMbindCard);

            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else {
            LOG.info("绑卡异常!");
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        }
    }

    /**
     * 修改商户费率
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/updateRate")
    public Object changeRate(@RequestParam(value = "idCard") String idCard,
                             @RequestParam(value = "rate") String rate,
                             @RequestParam(value = "extraFee") String extraFee) {
        LOG.info("开始修改费率=============================");

        Map<String, Object> map = new HashMap<>();

        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros()
                .toPlainString();
        String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> request = HashMapConver.getOrderByMap();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        request.put("methodname", "update"); // 方法名
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", dsorderid); // 商户订单号
        HQMRegister hqRegister = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        String subMerchantNo = hqRegister.getMerchantCode();
        request.put("subMerchantNo", subMerchantNo);// 子商户号 register返回

        request.put("futureRateValue", bigRate); // 扣款费率
        request.put("fixAmount", bigExtraFee); // 还款手续费
        LOG.info("/hqm/updateRate==========" + request.toString());
        Map<String, String> resultMap = smart.allRequestMethod(request);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        String code = resultMap.get("returncode"); // 返回码
        String errtext = resultMap.get("errtext");
        if ("0000".equals(code)) {
            LOG.info("修改费率成功===============");
            hqRegister.setExtraFee(extraFee);
            hqRegister.setRate(rate);
            topupPayChannelBusiness.createHQMRegister(hqRegister);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {
            LOG.info("修改费率失败===============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }
    }


    /**
     * 开始消费
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/topay")
    @ResponseBody
    public Object topay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        LOG.info("开始支付================================hqm");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String realAmount = prp.getRealAmount();
        String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
        String extra = prp.getExtra();// //消费计划|福建省-泉州市
        String cityName = extra.substring(extra.indexOf("-") + 1);
        LOG.info("=======================================消费城市：" + cityName);
        String provinceCode = null;
            try {
                List<HQERegion> hqe = t1.getHQERegionByParentName(cityName);
                provinceCode = hqe.get(0).getRegionCode();
            LOG.info("=======================================hqm消费城市编码：" + provinceCode);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("=======================================消费城市：" + cityName + "未匹配");
        }
        Map<String, Object> map = new HashMap<>();
        HQMbindCard hqBindCard = topupPayChannelBusiness.findHQMbindCardByBankCard(bankCard);
        if (null == hqBindCard) {
            return ResultWrap.init(CommonConstants.FALIED, "该卡未绑定");
        }
        String brandId = hqBindCard.getBindId();
        HQMRegister hqRegister = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        if (null == hqRegister) {
            return ResultWrap.init(CommonConstants.FALIED, "用户未注册该通道");
        }
        String subMerchantNo = hqRegister.getMerchantCode();
        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> param = HashMapConver.getOrderByMap();
        param.put("methodname", "pay");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = "xl" + format.format(date);
        param.put("ordersn", ordersn); // 流水号
        param.put("dsorderid", orderCode); // 商户订单号
        param.put("subMerchantNo", subMerchantNo);// 子商户号
        param.put("bindId", brandId); // TODO bindCard 返回 绑卡标识
        param.put("bankcard", bankCard); // 银行卡号
        param.put("amount", bigRealAmount); // 单位（分） 金额
        if (provinceCode != null) {
            param.put("province", provinceCode); // 消费城市
        }
        LOG.info("hqm/topay==========" + param.toString());
        param.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqm/pay/call_back"); // 异步通知地址
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        String errtext = resultMap.get("errtext");
        String code = resultMap.get("returncode"); // 返回码
        if ("0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;

        } else if ("0003".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
            return map;
        } else if ("0002".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付状态异常，等待查询");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }
    }

    /**
     * 消费交易异步通知
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/hqm/pay/call_back")
    public void fayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.info("消费支付异步回调进来了======");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String transtype = request.getParameter("transtype");
        String merchno = request.getParameter("merchno");
        String signType = request.getParameter("signType");
        String status = request.getParameter("status");
        String message = request.getParameter("message");
        String orderid = request.getParameter("orderid");
        String dsorderid = request.getParameter("dsorderid");
        String paytime = request.getParameter("paytime");
        LOG.info("回调参数====" + transtype + merchno + signType + status + message + orderid + dsorderid + paytime);
        LOG.info("第三方流水号======orderid" + orderid);
        LOG.info("订单===========dsorderid" + dsorderid);

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
        String ipAddress = prp.getIpAddress();
        if ("00".equals(status)) {
            LOG.info("支付成功=============");
            String version = "49"; // TODO 修改通道标识
            LOG.info("version======" + version);
            RestTemplate restTemplate = new RestTemplate();
            String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", dsorderid);
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
            }
            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", dsorderid);
            requestEntity.add("third_code", orderid); // 第三方订单号
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
            }
            LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);
            LOG.info("订单已支付!");
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else if ("01".equals(status)) {
            LOG.info("交易处理中!");
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else {
            LOG.info("交易处理失败!");
            addOrderCauseOfFailure(dsorderid, message, ipAddress);
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();

        }
    }

    /**
     * 开始代付
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/transfer")
    @ResponseBody
    public Object transfer(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        LOG.info("开始进入还款计划========================");
        Map<String, Object> map = new HashMap();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        HQMbindCard hqbindCard = topupPayChannelBusiness.findHQMbindCardByBankCard(bankCard);
        HQMRegister hqRegister = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        String subMerchantNo = hqRegister.getMerchantCode();
        String realAmount = prp.getRealAmount();
        String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
        String bindId = hqbindCard.getBindId();
        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "withDraw");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = "xl" + format.format(date);
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", orderCode); // 商户订单号
        request.put("subMerchantNo", subMerchantNo);// 子商户号
        request.put("bindId", bindId); // 绑卡标识 bindCard 返回
        request.put("bankcard", bankCard);// 银行卡号 信用卡
        request.put("amount", bigRealAmount);// 金额 （分）
        request.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqm/transfer/call_back"); // 异步通知地址
        LOG.info("/hqm/transfer================" + request.toString());
        Map<String, String> resultMap = smart.allRequestMethod(request);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        String code = resultMap.get("returncode");// 放回码
        String errtext = resultMap.get("errtext");
        if ("0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;

        } else if ("0003".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
            return map;
        } else if ("0002".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付状态异常，等待查询");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }
    }

    /**
     * 代付交易异步通知
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/hqm/transfer/call_back")
    public void transferNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("代付支付异步回调进来了======");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String status = request.getParameter("status");
        String message = request.getParameter("message");
        String orderid = request.getParameter("orderid");
        String dsorderid = request.getParameter("dsorderid");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
        String ipAddress = prp.getIpAddress();
        if ("00".equals(status)) {
            LOG.info("*********************支付成功***********************");
            String version = "49";
            LOG.info("version======" + version);
            RestTemplate restTemplate = new RestTemplate();
            String url = ip + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", dsorderid);
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
                LOG.error("", e);
            }
            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", dsorderid);
            requestEntity.add("third_code", orderid); // 第三方订单号
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
            }
            LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);
            LOG.info("订单已支付!");
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else if ("01".equals(status)) {
            LOG.info("交易处理中!");
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else {
            LOG.info("交易处理失败!");
            addOrderCauseOfFailure(dsorderid, message, ipAddress);
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        }
    }

    // 余额查询
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/balanceQuery1")
    public Object balanceQuery1(@RequestParam(value = "bankCard") String bankCard,
                                @RequestParam(value = "idCard") String idCard) throws Exception {
        Map<String, Object> o = (Map<String, Object>) balanceQuery(bankCard, idCard);
        String resp_message = o.get("resp_message").toString();
        String str = resp_message.split("可用余额")[1].toString();
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("balance", str);
        return jsonObject;
    }

    /**
     * 商户余额查询
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/balanceQuery")
    public Object balanceQuery(@RequestParam(value = "bankCard") String bankCard,
                               @RequestParam(value = "idCard") String idCard) throws Exception {
        LOG.info("开始进入商户余额查询========================");
        Map<String, Object> map = new HashMap<>();
        HQMbindCard hqMbindCard = topupPayChannelBusiness.findHQMbindCardByBankCard(bankCard);
        HQMRegister hqmRegister = topupPayChannelBusiness.findHQMRegisterByIdCard(idCard);
        String bindId = hqMbindCard.getBindId();
        String subMerchantNo = hqmRegister.getMerchantCode();

        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "queryBalance");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String dsorderid = format.format(date);
        request.put("dsorderid", dsorderid); // 商户订单号
        request.put("subMerchantNo", subMerchantNo);// 子商户号
        request.put("bindId", bindId);// 绑卡标识
        Map<String, Object> resultMap = smart.allRequestMethodquery(request);
        String code = resultMap.get("returncode").toString();
        String frozenamount = resultMap.get("frozenamount").toString();// 商户冻结余额
        String currAccountBalance = resultMap.get("currAccountBalance").toString();// 当前可用余额
        String errtext = resultMap.get("errtext").toString();
        if ("0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "商户冻结余额" + frozenamount + "当前可用余额" + currAccountBalance);
            return map;

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }

    }

    /**
     * 查询接口
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqm/payQuery")
    public Object payQuery(@RequestParam(value = "orderCode") String orderCode,
                           @RequestParam(value = "transType") String transtype) throws Exception {
        LOG.info("开始进行交易查询=======================");
        Map<String, Object> map = new HashMap<>();

        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();
        Date date = new Date();
        request.put("dsorderid", orderCode); // 产生 商户订单号
        request.put("transtype", transtype); // TODO 133 交易 134 代付

        Map<String, String> resultMap = smart.allRequestMethod(request);
        String status = resultMap.get("status");
        String amount = resultMap.get("amount"); // 金额
        String orderid = resultMap.get("orderid");// 第三方流水号
        String dsorderid = resultMap.get("dsorderid");// 订单号
        String message = resultMap.get("message");// 返回信息\
        LOG.info("amount==" + amount + "orderid=" + orderid + "dsorderid=" + dsorderid);
        LOG.info("=============环球小额落地：" + resultMap.toString());
        if ("00".equals(status)) {
            LOG.info("订单执行成功==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, message);
            return map;
        } else if ("01".equals(status)) {
            LOG.info("订单处理中==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, message);
            return map;
        } else if ("02".equals(status)) {
            LOG.info("订单执行失败==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, message);
            return map;
        } else if ("04".equals(status)) {
            LOG.info("订单关闭==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, message);
            return map;
        } else {
            LOG.info("订单号不存在==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, message);
            return map;
        }
    }


    String uri = "";
    final String channel_id = "207";

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/Batchsynchronization/tg")
    public Object batchsyncization() {
        String sql = "select bank_card from Paymentgateway.t_hqx_bindcard";
        List<String> hqx = jdbcTemplate.queryForList(sql, String.class);
        sql = "select bank_card from Paymentgateway.t_hqt_bindcard";
        List<String> hqt = jdbcTemplate.queryForList(sql, String.class);
        hqx.addAll(hqt);
        List<String> collect = hqx.stream().distinct().collect(toList());
        for (int i = 0; i < collect.size(); i++) {
            int finalI = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    uri = "http://user/v1.0/user/bank/userBankInfo/cardnoAndChannelId";
                    LinkedMultiValueMap user = new LinkedMultiValueMap<String, Object>();
                    user.add("cardno", collect.get(finalI));
                    user.add("channel_id", channel_id);
                    Map resultStr = restTemplate.postForObject(uri, user, HashMap.class);
                    synchronized (resultStr) {
                        if (null != resultStr.get(CommonConstants.RESULT) & !CommonConstants.FALIED.equals(resultStr.get(CommonConstants.RESP_CODE))) {
                            Map map = (Map) resultStr.get(CommonConstants.RESULT);
                            uri = "http://paymentgateway/v1.0/paymentgateway/topup/hqm/Dockentrance";
                            LinkedMultiValueMap Register = new LinkedMultiValueMap();
                            Register.add("bankCard", map.get("bankCard"));
                            Register.add("idCard", map.get("idCard"));
                            try {
                                Register.add("userName", URLEncoder.encode(map.get("userName") + "", "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Register.add("phone", map.get("phone"));
                            Register.add("bankName", "");
                            Register.add("rate", map.get("rate"));
                            Register.add("extraFee", map.get("withdrawFee"));
                            Register.add("securityCode", map.get("securityCode"));
                            Register.add("expiredTime", map.get("expiredTime"));
                            restTemplate.postForObject(uri, Register, HashMap.class);
                        }
                    }
                }
            });
        }
        return collect.size();
    }

}
