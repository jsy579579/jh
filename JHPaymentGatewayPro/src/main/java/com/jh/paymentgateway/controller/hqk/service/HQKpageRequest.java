package com.jh.paymentgateway.controller.hqk.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.hqk.dao.HQKregisterBusiness;
import com.jh.paymentgateway.controller.hqk.hqkUntil.CommonBean;
import com.jh.paymentgateway.controller.hqk.hqkUntil.CommonUtil;
import com.jh.paymentgateway.controller.hqk.hqkUntil.TransUtil;
import com.jh.paymentgateway.controller.hqk.pojo.HQKRegister;
import com.jh.paymentgateway.controller.hqk.pojo.HQKadree;
import com.jh.paymentgateway.controller.hqk.pojo.HQKbindCard;
import com.jh.paymentgateway.controller.hqk.until.SecurityUtils;
import com.jh.paymentgateway.controller.hqk.until.SmartRepayChannel;
import com.jh.paymentgateway.pojo.*;


import com.jh.paymentgateway.pojo.hq.HQNEWBindCard;
import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import com.jh.paymentgateway.util.utils.HashMapConver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class HQKpageRequest extends BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(HQKpageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HQKregisterBusiness hqKregisterBusiness;

    @Autowired
    private  TopupPayChannelBusiness topupPayChannelBusiness;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${payment.ipAddress}")
    private String ip;

    public static final String transcode = "053";// 小额的交易码

    public static final String cardType = "02"; // 卡类型 借记卡：01 贷记卡：02

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/Dockentrance")
    public @ResponseBody Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
                                             @RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "idCard") String idCard,
                                             @RequestParam(value = "phone") String phone, @RequestParam(value = "dphone") String dphone,
                                             @RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName1,
                                             @RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "extraFee") String extraFee,
                                             @RequestParam(value = "securityCode") String securityCode, @RequestParam(value = "rate") String rate,
                                             @RequestParam(value = "expiredTime") String expired) throws Exception {
        HQKbindCard hqnewbind = hqKregisterBusiness.getHQKbindCardByBankCard(bankCard);
        HQKRegister hqnewregister = hqKregisterBusiness.getHQKRegisterByIdCard(idCard);
        Map<String, Object> maps = new HashMap<>();
        String expireDate = this.expiredTimeToMMYY(expired);
        if (hqnewregister == null) {
            maps = (Map<String, Object>) this.hqRegister(dbankCard, idCard, phone, userName, rate, extraFee);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
//            maps = (Map<String, Object>) this.hqBindCard(bankCard, idCard, phone, userName, securityCode, expireDate);
//            if (!"000000".equals(maps.get("resp_code"))) {
//                return maps;
//            } else {
//                return ResultWrap.init(CommonConstants.SUCCESS, "签约成功");
//            }
        }
        if (hqnewbind == null || "0".equals(hqnewbind.getStatus())) {
//            maps = (Map<String, Object>) this.hqBindCard(bankCard, idCard, phone, userName, securityCode, expireDate);
//            if (!"000000".equals(maps.get("resp_code"))) {
//                return maps;
//            } else {
//                return ResultWrap.init(CommonConstants.SUCCESS, "签约成功");
//            }
            //跳转页面中转
            LOG.info("==================开始绑卡跳转页面中转====================");
            maps.put(CommonConstants.RESP_CODE, "999996");
            maps.put(CommonConstants.RESP_MESSAGE, "进入绑卡页面");
            maps.put(CommonConstants.RESULT,
                    ip + "/v1.0/paymentgateway/quick/hqk/bind-view?bankCard=" + bankCard
                            + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expireDate + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip);
            return maps;

        }
        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    /**
     * 页面跳转
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/hqk/bind-view")
    public String returnBindCardView(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String bankCard = request.getParameter("bankCard");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String expiredTime = request.getParameter("expiredTime");
        String securityCode = request.getParameter("securityCode");
        String ipAddress = request.getParameter("ipAddress");
        String rate = request.getParameter("rate");
        String extraFee = request.getParameter("extraFee");
        String userName = request.getParameter("userName");

        model.addAttribute("bankCard", bankCard);
        model.addAttribute("idCard", idCard);
        model.addAttribute("phone", phone);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("rate", rate);
        model.addAttribute("extraFee", extraFee);
        model.addAttribute("userName", userName);
        return "jfhkbindcard";
    }


    /**
     * 商户入驻
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/register")
    public @ResponseBody Object hqRegister(@RequestParam(value = "bankCard") String bankCard,
                                           @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                                           @RequestParam(value = "userName") String userName, @RequestParam(value = "rate") String rate,
                                           @RequestParam(value = "extraFee") String extraFee) throws Exception {
        LOG.info("开始进件======================");

        Map<String, Object> map = new HashMap<>();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> param = HashMapConver.getOrderByMap();
        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        String extraFee1 = new BigDecimal(extraFee).multiply(new BigDecimal("100")).toString();
        LOG.info("商户入驻费率：================" + rate1 + "%");
        LOG.info("商户入驻代付单笔手续费：================" + extraFee1);
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
        //2019.7.3环球通道要求所有用户进件费率在0.54%以上
        if(new BigDecimal(rate1).compareTo(new BigDecimal("0.54"))<0){
            param.put("futureRateValue", "0.55"); // 扣款费率  如果小于0.54则进件费率固定为0.54  环球通道要求
        }else {
            param.put("futureRateValue", rate1); // 扣款费率
        }
        param.put("fixAmount", extraFee1); // 还款手续费
        LOG.info("=========上送明文：" + param.toString());
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=========返回明文：" + resultMap.toString());
        String code = resultMap.get("returncode"); // 返回码
        String errtext = resultMap.get("errtext"); // 详情
        String merchno = resultMap.get("merchno"); // 平台商户号
        String subMerchantNo = resultMap.get("subMerchantNo"); // 子商户号
        if ("0000".equals(code)||"0053".equals(code)) {

            String rate2=new BigDecimal(rate1).divide(new BigDecimal("100")).stripTrailingZeros().toPlainString();
            LOG.info("进件费率======================="+rate2);
            HQKRegister hqRegister = new HQKRegister();
            hqRegister.setUserName(userName);
            hqRegister.setIdCard(idCard);
            hqRegister.setBankCard(bankCard);
            hqRegister.setMerchantCode(subMerchantNo);// 商户号
            hqRegister.setPhone(phone);
            hqRegister.setRate(rate2);
            hqRegister.setExtraFee(extraFee);
            hqRegister.setStatus("1");
            hqKregisterBusiness.createRegister(hqRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, "注册成功");
            map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
            return map;
        }

    }

    /**
     * 绑卡
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/hqBindCard")
    public @ResponseBody Object hqBindCard(@RequestParam(value = "bankCard") String bankCard,
                                           @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                                           @RequestParam(value = "userName") String userName,
                                           @RequestParam(value = "securityCode") String securityCode,
                                           @RequestParam(value = "expiredTime") String expiredTime) throws Exception {
        LOG.info("开始绑卡==================");

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
        HQKRegister hqRegister = hqKregisterBusiness.getHQKRegisterByIdCard(idCard);
        String merchantCode = hqRegister.getMerchantCode();
        param.put("subMerchantNo", merchantCode); // 子商户号 register方法返回
        param.put("bankcard", bankCard); // 银行卡号
        param.put("username", userName);// 真实姓名
        param.put("idcard", idCard);// 身份证号
        param.put("mobile", phone);// 预留手机号
        param.put("cardType", cardType); // 卡类型 借记卡：01 贷记卡：02
        param.put("cvn2", securityCode); // CVN2 安全码 卡类型为贷记卡时必填
        param.put("expireDate", expiredTime); // 信用卡有效期 格式：MMYY卡类型为贷记卡时必填
        param.put("returnUrl", ip+"/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");// 前台跳转地址 TODO 支付页面
        param.put("notifyUrl", ip+"/v1.0/paymentgateway/topup/hqk/bindcard/notify_call");// 异步通知地址
        LOG.info("=========上送明文：" + param.toString());
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=========返回明文：" + resultMap.toString());
        String code = resultMap.get("returncode");
        if ("0000".equals(code)) {//||"0055".equals(code)
            String bindId = resultMap.get("bindId");
            HQKbindCard hqnewbind = hqKregisterBusiness.getHQKbindCardByBankCard(bankCard);
            if (hqnewbind ==null){
                HQKbindCard hqnewbinds = new HQKbindCard();
                hqnewbinds.setBankCard(bankCard);
                hqnewbinds.setCreateTime(new Date());
                hqnewbinds.setIdCard(idCard);
                hqnewbinds.setPhone(phone);
                hqnewbinds.setStatus("1");
                hqnewbinds.setBindId(bindId);
                hqnewbinds.setDsorderid(dsorderid);
                hqKregisterBusiness.createBindcard(hqnewbinds);
            }else {
                hqnewbind.setBankCard(bankCard);
                hqnewbind.setCreateTime(new Date());
                hqnewbind.setIdCard(idCard);
                hqnewbind.setPhone(phone);
                hqnewbind.setStatus("1");
                hqnewbind.setBindId(bindId);
                hqnewbind.setDsorderid(dsorderid);
                hqKregisterBusiness.createBindcard(hqnewbind);
            }

            Object url = resultMap.get("bindUrl");
            LOG.info("开始跳转至"+url+"绑卡");
            map.put(CommonConstants.RESP_CODE, "999996");	//跳转到绑卡页面
            map.put(CommonConstants.RESP_MESSAGE, "需要跳转绑卡页面");
            map.put("html", url);
            return map;
//            if(bindId == null){
//                String url = resultMap.get("bindUrl");
//                LOG.info("开始跳转至"+url+"绑卡");
//                map.put(CommonConstants.RESP_CODE, "999996");	//跳转到绑卡页面
//                map.put(CommonConstants.RESP_MESSAGE, "需要跳转绑卡页面");
//                map.put(CommonConstants.RESULT,url);
//                return map;
//            }else{
//                LOG.info("开始本地绑卡==================");
//                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//                map.put(CommonConstants.RESULT,ip+"/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
//                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功");
//                return map;
//            }

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "绑卡失败");
            return map;
        }
    }

    /**
     * TODO 修改地址 绑卡异步回调
     */
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqk/bindcard/notify_call")
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
        LOG.info("返回参数=====" + merchno + paytime + status + orderid + dsorderid +
                subMerchantNo + bindId + sign);

        LOG.info("请求绑卡流水号dsorderid-----------" + dsorderid);
        LOG.info("请求绑卡商户号merchno-----------" + merchno);
        LOG.info("status-----------" + status); // 状态

        if ("00".equals(status)) {
            LOG.info("*********************绑卡成功***********************");

            HQKbindCard hqBindCard = hqKregisterBusiness.getHQKbindCardByDsorderid(dsorderid);
            hqBindCard.setStatus("1");
            hqBindCard.setBindId(bindId); // 绑卡标识

            hqKregisterBusiness.createBindcard(hqBindCard);
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
     * 支付接口
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/topay")
    public @ResponseBody Object toPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {

        LOG.info("开始支付================================");

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String extraFee = prp.getExtraFee();
        String cvn2=prp.getSecurityCode();
        String expireDates=prp.getExpiredTime();
        String expireDate=this.expiredTimeToMMYY(expireDates);


        Map<String, Object> map = new HashMap<>();
        String realAmount = prp.getRealAmount();
        HQKbindCard hqBindCard = hqKregisterBusiness.getHQKbindCardByBankCard(bankCard);
        String brandId = hqBindCard.getBindId();
        HQKRegister hqnewRegister = hqKregisterBusiness.getHQKRegisterByIdCard(idCard);
        String merchantCode = hqnewRegister.getMerchantCode();

        String extra = prp.getExtra();// 消费计划|福建省-泉州市-350500
        String cityName = extra.substring(extra.indexOf("-") + 1);
        LOG.info("=======================================消费城市：" + cityName);
        String provinceCode = null;
        try {
            HQKadree hqe = hqKregisterBusiness.findProvinceCodeByCity(cityName);
            provinceCode = hqe.getRegionCode();
            LOG.info("=======================================HQDH消费城市编码：" + provinceCode);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("=======================================消费城市：" + cityName + "未匹配");
        }
        SmartRepayChannel smart = new SmartRepayChannel();

        String amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

        Map<String, String> param = HashMapConver.getOrderByMap();
        param.put("methodname", "pay");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        param.put("ordersn", ordersn); // 流水号
        param.put("dsorderid", orderCode); // 商户订单号
        param.put("subMerchantNo", merchantCode);// 子商户号
        param.put("bindId", brandId); // TODO bindCard 返回 绑卡标识
        param.put("bankcard", bankCard); // 银行卡号
        param.put("amount", amount); // 单位（分） 金额
        // param.put("userFee",);//扣款手续费 该字段不传则按入驻上传的扣款费率计算手续费hqnew
        if (provinceCode != null) {
            param.put("province", provinceCode); // 消费城市
        }
        param.put("cardType", "02");
        param.put("cvn2", cvn2);
        param.put("expireDate", expireDate);
        // param.put("mcc", "");//行业
        param.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqk/pay/notify_call"); // 异步通知地址
        LOG.info("=========上送明文：" + param.toString());
        Map<String, String> resultMap = smart.allRequestMethod(param);
        LOG.info("=========返回明文：" + resultMap.toString());
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
            map.put(CommonConstants.RESP_MESSAGE, "交易失败400");
            return map;
        }
    }

//	public static void main(String[] args) {
//		String amount = new BigDecimal("100.000").multiply(new BigDecimal("100")).setScale(0).toString();
//		System.out.println(amount);
//	}
    /**
     * 支付异步回调
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqk/pay/notify_call")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOG.info("支付回调回来了！！！！！！！！！！！！！！");
        // TODO JSON 返回的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }

        String transtype = request.getParameter("transtype");// 业务类型
        String merchno = request.getParameter("merchno");// 商户号
        String signType = request.getParameter("signType"); // 状态 ---------135
        String status = request.getParameter("status");// 状态 00:成功，02:失败，01:处理中
        String message = request.getParameter("message"); // 订单描述信息
        String orderid = request.getParameter("orderid");// 我司订单号
        String dsorderid = request.getParameter("dsorderid");// 商户订单号
        String amount = request.getParameter("amount");// 加密校验值
        String paytime = request.getParameter("paytime");// 交易时间
        String sign = request.getParameter("sign");// 加密校验值
        LOG.info("返回参数=====" + transtype + merchno + signType + status + message + orderid + dsorderid + amount
                + paytime + sign);

        LOG.info("请求绑卡流水号dsorderid-----------" + dsorderid);
        LOG.info("请求绑卡商户号merchno-----------" + merchno);
        LOG.info("status-----------" + status);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
        String ipAddress = prp.getIpAddress();
        String channelTag = prp.getChannelTag();

        if ("00".equals(status)) {
            LOG.info("支付成功=============");
            String version = "44";
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
            // url = prp.getIpAddress() +
            // "/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", dsorderid);
            requestEntity.add("third_code", "");
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


    // 代付接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/transfer")
    public @ResponseBody Object toSettle(@RequestParam(value = "orderCode") String orderCode) {

        Map<String, Object> map = new HashMap();

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        HQKbindCard bindCard = hqKregisterBusiness.getHQKbindCardByBankCard(bankCard);
        HQKRegister hqnewRegister = hqKregisterBusiness.getHQKRegisterByIdCard(idCard);
        String subMerchantNo = hqnewRegister.getMerchantCode();
        String realAmount = prp.getRealAmount();
        String bindId = bindCard.getBindId();
        String extraFee = prp.getExtraFee();
        List consums =new ArrayList();
        RestTemplate restTemplate = new RestTemplate();
        //获取对应的消费单号
        String url = prp.getIpAddress() + "/v1.0/creditcardmanager/findConsumeTaskId";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode",orderCode);
        String result = null;
        JSONObject jsonObject;
        JSONObject resultObj;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
           String repCode = jsonObject.getString("resp_code");
            if (!CommonConstants.SUCCESS.equals(repCode)){
                map.put(CommonConstants.RESP_CODE, "999998");
                String message = jsonObject.getString("resp_message");
                map.put(CommonConstants.RESP_MESSAGE,message);
                return map;
            }
            JSONArray resultLists=jsonObject.getJSONArray("resp_message");
            consums=Arrays.asList(resultLists.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        SmartRepayChannel smart = new SmartRepayChannel();


        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);

        CommonBean trans = new CommonBean();
        trans.setMethodname("withDraw");
        trans.setTranscode("053");
        trans.setVersion("0100");
        trans.setOrdersn(ordersn);
        trans.setDsorderid(orderCode);
        trans.setMerchantNo("shbyt2019071016");
        trans.setBindId(bindId);
        trans.setSubMerchantNo(subMerchantNo);
        trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqk/transfer/notify_call");
        trans.setOrderIds(consums);

        LOG.info("=========上送明文：" + trans.toString());
        TransUtil tu = new TransUtil();
        try {
            String uri="http://pay.huanqiuhuiju.com/authsys/api/sdj/pay/execute.do";
            byte[] reponse = tu.packet(trans, "dc3becc5e60764edaa6bb78e4ac05e0e");
            String response = CommonUtil.post(uri, reponse);

        LOG.info("=========返回明文：" + response.toString());

            JSONObject fromObject = JSONObject.fromObject(response);
            String code = fromObject.getString("returncode");

        if ("0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "代付成功，等待银行出款");
            return map;

        } else if ("0003".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "代付处理中，等待银行出款");
            return map;
        } else if ("0002".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "代付状态异常，等待查询");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "代付失败400");
            LOG.info("=========不是try里面的失败");
            return map;
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE, "代付失败400");
     return map;
    }

    // 代付接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/tang")
    public @ResponseBody Object abc(@RequestParam(value = "orderCode",defaultValue = "201909281921218841",required = false) String orderCode) {

        Map<String, Object> map = new HashMap();

//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard ="342423199508166835" ;//prp.getIdCard();
        String bankCard = "6227080412154540";//prp.getBankCard();
        HQKbindCard bindCard = hqKregisterBusiness.getHQKbindCardByBankCard(bankCard);
        HQKRegister hqnewRegister = hqKregisterBusiness.getHQKRegisterByIdCard(idCard);
        String subMerchantNo = hqnewRegister.getMerchantCode();
        String realAmount ="11";// prp.getRealAmount();
        String bindId = bindCard.getBindId();
        String extraFee = "1";//prp.getExtraFee();
        List consums =new ArrayList();
        RestTemplate restTemplate = new RestTemplate();
        //获取对应的消费单号
        String url = ip + "/v1.0/creditcardmanager/findConsumeTaskId";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode",orderCode);
        String result = null;
        JSONObject jsonObject;
        JSONObject resultObj;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            String repCode = jsonObject.getString("resp_code");
            if (!CommonConstants.SUCCESS.equals(repCode)){
                map.put(CommonConstants.RESP_CODE, "999998");
                String message = jsonObject.getString("resp_message");
                map.put(CommonConstants.RESP_MESSAGE,message);
                return map;
            }
            JSONArray resultLists=jsonObject.getJSONArray("resp_message");
            consums=Arrays.asList(resultLists.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        SmartRepayChannel smart = new SmartRepayChannel();


        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);

        CommonBean trans = new CommonBean();
        trans.setMethodname("withDraw");
        trans.setTranscode("053");
        trans.setVersion("0100");
        trans.setOrdersn(ordersn);
        trans.setDsorderid(orderCode);
        trans.setMerchantNo("shbyt2019071016");
        trans.setBindId(bindId);
        trans.setSubMerchantNo(subMerchantNo);
        trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqk/transfer/notify_call");
        trans.setOrderIds(consums);
        LOG.info("=========上送明文：" + trans.toString());
        TransUtil tu = new TransUtil();
        try {
            String uri="http://pay.huanqiuhuiju.com/authsys/api/sdj/pay/execute.do";
            byte[] reponse = tu.packet(trans, "dc3becc5e60764edaa6bb78e4ac05e0e");
            String response = CommonUtil.post(uri, reponse);

            LOG.info("=========返回明文：" + response.toString());

            JSONObject fromObject = JSONObject.fromObject(response);
            String code = fromObject.getString("returncode");

            if ("0000".equals(code)) {
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "代付成功，等待银行出款");
                return map;

            } else if ("0003".equals(code)) {
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "代付处理中，等待银行出款");
                return map;
            } else if ("0002".equals(code)) {
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "代付状态异常，等待查询");
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "代付失败400");
                LOG.info("=========不是try里面的失败");
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE, "代付失败400");
        return map;
    }

    /**
     * 代付回调
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqk/transfer/notify_call")
    public void transferNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOG.info("支付回调回来了！！！！！！！！！！！！！！");
        // TODO JSON 返回的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }

        String transtype = request.getParameter("transtype");// 业务类型
        String merchno = request.getParameter("merchno");// 商户号
        String signType = request.getParameter("signType"); // 状态 ---------135
        String status = request.getParameter("status");// 状态 00:成功，02:失败，01:处理中
        String message = request.getParameter("message"); // 订单描述信息
        String orderid = request.getParameter("orderid");// 我司订单号
        String dsorderid = request.getParameter("dsorderid");// 商户订单号
        String amount = request.getParameter("amount");// 加密校验值
        String paytime = request.getParameter("paytime");// 交易时间
        String sign = request.getParameter("sign");// 加密校验值
        LOG.info("返回参数=====" + transtype + merchno + signType + status + message + orderid + dsorderid + amount
                + paytime + sign);

        LOG.info("请求绑卡流水号dsorderid-----------" + dsorderid);
        LOG.info("请求绑卡商户号merchno-----------" + merchno);
        LOG.info("status-----------" + status);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
        String ipAddress = prp.getIpAddress();
        String channelTag = prp.getChannelTag();
        if ("00".equals(status)) {
            LOG.info("*********************支付成功***********************");

            String version = "44";
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
            // url = prp.getIpAddress() +
            // "/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", dsorderid);
            requestEntity.add("third_code", "");
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
     * 订单状态查询
     *
     * @param orderCode
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/query")
    public @ResponseBody Object query(@RequestParam(value = "orderCode") String orderCode,
                                      @RequestParam(value = "transType") String transtype) throws IOException {
        LOG.info("开始订单查询===================");

        Map<String, Object> map = new HashMap<>();
        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();

        request.put("dsorderid", orderCode); // 产生 商户订单号
        request.put("transtype", transtype); // TODO 135 提现交易 136 代付

        Map<String, String> resultMap = smart.allRequestMethod(request);
        String status = resultMap.get("status");

        if ("00".equals(status)) {

            LOG.info("订单执行成功==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else if ("02".equals(status)) {
            LOG.info("订单执行失败==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "失败");
            return map;
        } else if ("01".equals(status)) {
            LOG.info("订单处理中==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, "处理中");
            return map;
        } else if ("04".equals(status)) {
            LOG.info("订单关闭==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "订单关闭");
            return map;
        } else {
            LOG.info("订单号不存在==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
            return map;
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/balanceQuery1")
    public @ResponseBody Object balanceQuery1(@RequestParam(value = "idCard") String idCard,
                                              @RequestParam(value = "bankCard") String bankCard) throws IOException {
        Map<String, Object> o = (Map<String, Object>) balanceQuery(idCard, bankCard);
        String str = o.get("result").toString();
        str = str.replace("=",":");
        String string = com.alibaba.fastjson.JSONObject.parseObject(str).getString("currAccountBalance");
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("balance", string);
        return jsonObject;
    }

    /**
     * 查询商户余额
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/balanceQuery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard,
                                             @RequestParam(value = "bankCard") String bankCard) throws IOException {
        LOG.info("开始余额===================");

        Map<String, Object> map = new HashMap<>();
        HQNEWBindCard hqBindCard = topupPayChannelBusiness.getHQNEWBindCardByBankCard(bankCard);
        HQNEWRegister hqnewRegister = topupPayChannelBusiness.getHQNEWRegisterByIdCard(idCard);
        String merchantCode = hqnewRegister.getMerchantCode();
        String brandId = hqBindCard.getBindId();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "queryBalance");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", dsorderid); // 商户订单号
        request.put("subMerchantNo", merchantCode);// 子商户号
        // request.put("cardType",""); 借记卡：01 贷记卡：02
        request.put("bindId", brandId); // 绑卡标识 bindCard 返回
        Map<String, String> resultMap = smart.allRequestMethod(request);
        String code = resultMap.get("returncode");

        Map<String, Object> map1 = new HashMap<>();
        map1.put("currAccountBalance",resultMap.get("currAccountBalance"));

        if ("0000".equals(code)) {
            LOG.info("查询成功=========================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, map1);
            return map;
        } else {

            LOG.info("查询失败=========================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "失败");
            return map;
        }

    }

    /**
     * 修改绑卡信息
     *
     * @param bankCard
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/changeBindCard")
    public @ResponseBody Object changeBindCard(@RequestParam(value = "bankCard") String bankCard,
                                               @RequestParam(value = "newPhone") String newPhone, @RequestParam(value = "idCard") String idCard)
            throws Exception {

        LOG.info("修改绑卡信息====================");

        Map<String, Object> map = new HashMap();

        HQNEWBindCard hqBindCard = topupPayChannelBusiness.getHQNEWBindCardByBankCard(bankCard);
        HQNEWRegister hqnewRegister = topupPayChannelBusiness.getHQNEWRegisterByIdCard(idCard);
        String merchantCode = hqnewRegister.getMerchantCode();

        String brandId = hqBindCard.getBindId();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "updateCardInfo");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", dsorderid); // 商户订单号

        request.put("subMerchantNo", merchantCode);// 子商户号
        request.put("bindId", brandId); // 绑卡标识 BindCard接口返回
        request.put("mobile", newPhone); // 预留手机号 //TODO 修改手机号
        // request.put("cvn2",""); // CVN2 格式：MMYY
        LOG.info("=========上送明文：" + request.toString());
        Map<String, String> resultMap = smart.allRequestMethod(request);
        LOG.info("=========返回明文：" + resultMap.toString());
        String code = resultMap.get("returncode");

        if ("0000".equals(code)) {

            LOG.info("修改绑卡信息成功===============");
            hqBindCard.setPhone(newPhone);
            topupPayChannelBusiness.createHQNEWBindCard(hqBindCard);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {

            LOG.info("修改绑卡信息失败===============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "失败");
            return map;
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/changeRate")
    public @ResponseBody Object changeRate(@RequestParam(value = "bankCard") String bankCard,
                                           @RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
                                           @RequestParam(value = "idCard") String idCard) throws Exception {
        LOG.info("开始修改费率=============================");

        Map<String, Object> map = new HashMap<>();

        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).toString();

        SmartRepayChannel smart = new SmartRepayChannel();

        Map<String, String> request = HashMapConver.getOrderByMap();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        request.put("methodname", "update"); // 方法名
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", dsorderid); // 商户订单号
        HQNEWRegister hqnewRegister = topupPayChannelBusiness.getHQNEWRegisterByIdCard(idCard);
        String merchantCode = hqnewRegister.getMerchantCode();
        request.put("subMerchantNo", merchantCode);// 子商户号 register返回

        request.put("futureRateValue", bigRate); // 扣款费率
        request.put("fixAmount", bigExtraFee); // 还款手续费

        LOG.info("=========上送明文：" + request.toString());
        Map<String, String> resultMap = smart.allRequestMethod(request);
        LOG.info("=========返回明文：" + resultMap.toString());

        String code = resultMap.get("returncode"); // 返回码

        if ("0000".equals(code)) {
            LOG.info("修改费率成功===============");
            hqnewRegister.setExtraFee(extraFee);
            hqnewRegister.setRate(rate);
            topupPayChannelBusiness.createHQNEWRegister(hqnewRegister);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {

            LOG.info("修改费率失败===============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "失败");
            return map;
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/chooseprovince")
    public @ResponseBody Object chooseprovince() throws Exception {
        Map<String,Object> map=new HashMap<>();
        List<HQKadree>lists=hqKregisterBusiness.findAllAdree();
        if (lists==null||lists.size()==0) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"系统异常");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,lists);
            return map;
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqk/chooseCity")
    public @ResponseBody Object chooseCity(@RequestParam("province")String province) throws Exception {
        Map<String,Object> map=new HashMap<>();
        List<HQKadree>lists=hqKregisterBusiness.findCityByProvince(province);
        if (lists==null||lists.size()==0) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"系统异常");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,lists);
            return map;
        }
    }
}
