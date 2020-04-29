package com.jh.paymentgateway.controller;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.NewTopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.hxdhd.HXDCity;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDBindCard;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDRegister;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hxdh.HttpClientUtils;
import com.jh.paymentgateway.util.hxdh.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

import static com.jh.paymentgateway.controller.HXDHXTopupRequest.getNumber;

/**
 * @author huhao
 * @title: HXDHDTopupRequest
 * @projectName juhepay
 * @description: 环迅大额
 * @date 2019/8/14 16:55
 */
@Controller
public class HXDHDTopupRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(HXDHXTopupRequest.class);

    private static final String URL = "https://www.dh0102.com/oss-transaction/gateway/";

    //公钥
    private static final String KEY = "3fab2a1d6ec9724b06b57590775c316e";//测试: 96ea552bcd55253ba90bbcffcf81e654  正式 3fab2a1d6ec9724b06b57590775c316e

    //商户号
    private static final String MID = "000010000000049";//测试:000010000000001   正式 000010000000049

    //默认字符串
    private static final String ENCRYPT_ID = "000010000000049";//测试:000010000000001   正式 000010000000049

    //通道标识
    private static final String AGENCY_TYPE = "ffkj";//测试:hxkjdh  正式 fd

    //Api版本号 默认为 1
    private static final Integer API_VERSION = 1;

    @Autowired
    private NewTopupPayChannelBusiness newTopupPayChannelBusiness;



    @Autowired
    RedisUtil redisUtil;

    @Value("${payment.ipAddress}")
    private String ipAddress;



    //请求地址：https://www.dh0102.com/oss-transaction/gateway/{method}

    /*发送参数格式为{"content":"{xxx}","sign":"xxx"}
        所有参数均在放在content中包括通用参数和每个接口不同的参数
                sign=md5({"content":"{xxx}","key":"xxx"},”utf-8”)
        返回值格式为{"result":"{"code":"000000","data":{xxx},"message":"成功"}","sign":"xxx"}
        sign=md5({"key":"xxx","result":"{"code":"000000","data":{xxx},"message":"成功"}"},”utf-8”)
        content和result内容为json格式字符串，非json对象*/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/dockingEntrance")
    public @ResponseBody
    Object docking(@RequestParam(value = "bankCard") String bankCard,
                    @RequestParam(value = "idCard") String idCard,
                    @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "userName") String userName,
                    @RequestParam(value = "bankName") String bankName1,
                    @RequestParam(value = "extraFee") String extraFee,
                    @RequestParam(value = "rate") String rate,
                    @RequestParam(value = "expiredTime") String expired,
                   @RequestParam(value = "securityCode") String securityCode) throws Exception {


        LOG.info("开始判断用户是否绑卡==========================================");
        Map<String,Object> map = new HashMap<>();
        String expiredTime = this.expiredTimeToYYMM(expired);
        HXDHDRegister register = newTopupPayChannelBusiness.getHXDHDRegisterByBankCard(bankCard);
        HXDHDBindCard bindCard = newTopupPayChannelBusiness.getHXDHDBindCardByBankCard(bankCard);
        if (register == null || "0".equals(register.getStatus()) || bindCard == null){
                LOG.info("用户未进件或绑卡==========================================");
                map.put(CommonConstants.RESP_CODE,"999996");
                map.put(CommonConstants.RESP_MESSAGE,"用户未进件或绑卡");
                LOG.info(ipAddress + "/v1.0/paymentgateway/topup/hxdhd/returnBindCardPage?ipAddress="+ipAddress+ "&bankCard=" + bankCard
                        + "&bankName=" + URLEncoder.encode(bankName1, "UTF-8")
                        + "&securityCode=" + securityCode
                        + "&expiredTime="+ expiredTime+ "&idCard="+ idCard+ "&userName="+ userName+ "&phone="+ phone + "&rate" + rate + "&extraFee" + extraFee);
                map.put(CommonConstants.RESULT,ipAddress + "/v1.0/paymentgateway/topup/hxdhd/returnBindCardPage?ipAddress="+ipAddress+ "&bankCard=" + bankCard
                        + "&bankName=" + URLEncoder.encode(bankName1, "UTF-8")
                        + "&securityCode=" + securityCode
                        + "&expiredTime="+ expiredTime+ "&idCard="+ idCard+ "&userName="+ userName+ "&phone="+ phone + "&rate=" + rate + "&extraFee=" + extraFee);
                return map;
        }
        LOG.info("环迅大额绑卡验证成功===============");
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"验证成功");
        return map;
    }


    /**
     * 跳转到绑卡页面
     * @param request
     * @param response
     * @param model
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hxdhd/returnBindCardPage")
    public Object returnBindCardPage(HttpServletRequest request, HttpServletResponse response, Model model) throws UnsupportedEncodingException {
        LOG.info("================================环迅代还大额绑卡跳转页面============================================");
        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String ipAddress = request.getParameter("ipAddress"); //ip地址
        String bankCard = request.getParameter("bankCard"); //银行卡号
        String bankName = request.getParameter("bankName"); //银行名称
        String securityCode = request.getParameter("securityCode");  //安全码
        String expiredTime = request.getParameter("expiredTime");  //有效期
        String idCard = request.getParameter("idCard");   //身份证
        String userName = request.getParameter("userName"); //姓名
        String phone = request.getParameter("phone");  //手机号
        String rate = request.getParameter("rate");
        String extraFee = request.getParameter("extraFee");

        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("bankName", bankName);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("idCard", idCard);
        model.addAttribute("userName", userName);
        model.addAttribute("phone", phone);
        model.addAttribute("rate",rate);
        model.addAttribute("extraFee",extraFee);
        return "hxdbindcard";
    }


    //绑卡成功跳转页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/hxdhd/bindcardsuccess")
    public String wmykBindCardSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map map = new HashMap();
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        return "cjbindcardsuccess";

    }

    /**
     * 下单接口
     * @param orderCode 订单号
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/pay")
    @ResponseBody
    public Object pay(@RequestParam(value = "orderCode") String orderCode){
        LOG.info("================/v1.0/paymentgateway/topup/hxkjd/pay===================Action");
        Map<String, Object> map = new HashMap<>();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

        LOG.info("开始进入下单===========================订单号:"+orderCode);
        String method = "fastpayPrecreate2";
        Map<String, Object> contentMap = creatContentMap();
        contentMap.put("method",method);
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("srcAmt",prp.getAmount());
        contentMap.put("bizOrderNumber",orderCode);//订单号
        contentMap.put("notifyUrl",ipAddress + "/v1.0/paymentgateway/topup/hxdhd/transfer/notifyurl");   //异步通知地址
        contentMap.put("accountNumber",prp.getBankCard());//信用卡
        contentMap.put("tel",prp.getPhone());
        BigDecimal bigRate = new BigDecimal(prp.getRate()).multiply(new BigDecimal(100));
        contentMap.put("fastpayFee",bigRate);
        contentMap.put("agencyType",HXDHDTopupRequest.AGENCY_TYPE);
        contentMap.put("holderName",prp.getUserName());
        contentMap.put("idcard",prp.getIdCard());
        contentMap.put("settAccountNumber","ABC333007");
        contentMap.put("settAccountTel","100011");
        String extra = prp.getExtra();// 消费计划|福建省-泉州市-350500
        LOG.info("extra====================="+extra);
        String cityName = extra.substring(extra.indexOf("-") + 1);
        LOG.info("=======================================落地城市：" + cityName);
        contentMap.put("city",cityName);//落地城市
        contentMap.put("cvv2",prp.getSecurityCode());//落地城市
        contentMap.put("expired",prp.getExpiredTime());
        contentMap.put("mcc","");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:=========================" + requestMap);
        String resultJSON = null;
        LOG.info("发送请求:=========================Action");
        try {
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + method, null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            if (!HXDHDTopupRequest.SUCCESS.equals(code)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString(HXDHDTopupRequest.MESSAGE));
                return map;
            }
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;
        } catch (Exception e) {
            LOG.info("请求发送异常=========================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求发送异常");
            return map;
        }
    }


    /**
     * 异步通知调用接口
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/transfer/notifyurl")
    public String topupBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("HXDHD消费/还款异步回调通知=================");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        //参数:bizOrderNumber，completedTime，mid，srcAmt，sign
        String bizOrderNumber = request.getParameter("bizOrderNumber");
        String completedTime = request.getParameter("completedTime");
        String mid = request.getParameter("mid");
        String srcAmt = request.getParameter("srcAmt");
        String sign = request.getParameter("sign");
        String md5Sign = MD5Utils.encode("bizOrderNumber="+bizOrderNumber+"&completedTime="+completedTime+"&mid="+mid+"&srcAmt="+srcAmt+"&key="+HXDHDTopupRequest.KEY,"utf-8");
        if(!sign.equals(md5Sign)){
            LOG.info("验签失败!!!!");
            return "false";
        }

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(bizOrderNumber);

        String version = "50";
        RestTemplate restTemplate = new RestTemplate();
        String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode", bizOrderNumber);
        requestEntity.add("version", version);
        String result = null;
        net.sf.json.JSONObject jsonObject;
        net.sf.json.JSONObject resultObj;

        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT=================================" + result);
            jsonObject = net.sf.json.JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("status", "1");
        requestEntity.add("order_code", bizOrderNumber);
        requestEntity.add("third_code", "");
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("",e);
        }
        LOG.info("订单状态修改成功===================" + bizOrderNumber + "====================" + result);
        return "success";
    }

    /**
     * 查询订单状态接口
     * @param orderCode    订单号
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/orderquery")
    public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        LOG.info("================/v1.0/paymentgateway/topup/hxdhd/orderquery===================Action");
        Map<String, Object> map = new HashMap<>();
        LOG.info("开始进入查询订单状态=================================订单号:" + orderCode);
        String method = "fastpayQuery";
        Map<String, Object> contentMap = creatContentMap();
        contentMap.put("method",method);
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("bizOrderNumber",orderCode);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:=============================" + requestMap);
        String resultJSON = null;
        try {
            LOG.info("发送请求:=================================Action");
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + method, null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            if (HXDHDTopupRequest.SUCCESS.equals(code)){
                String date = jsonObject.getString(HXDHDTopupRequest.DATE);
                JSONObject dateObject = JSONObject.parseObject(date);
                String txnStatus = dateObject.getString("txnStatus");
                String dataMessage = dateObject.getString("dataMessage");
                if ("p".equals(txnStatus)){
                    LOG.info("支付中==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.WAIT_CHECK);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
                if ("s".equals(txnStatus)){
                    LOG.info("交易成功==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
                if ("c".equals(txnStatus)){
                    LOG.info("交易关闭==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
            }
            String message = jsonObject.getString(HXDHDTopupRequest.MESSAGE);
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,message);
            return map;
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求发送失败");
            return map;
        }
    }

    /**
     * 结算接口
     * @param orderCode   订单号
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/transfercreate")
    public @ResponseBody Object transferCreate(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("================/v1.0/paymentgateway/topup/hxdhd/transfercreate===================Action");
        Map<String, Object> map = new HashMap<>();
        LOG.info("开始进入结算=================================订单号:" + orderCode);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("环迅开始结算==================================================");
        Map<String, Object> reqContentMap = creatContentMap();
        String method = "fastpayTransferCreate";
        reqContentMap.put("method",method);
        reqContentMap.put("mid",HXDHDTopupRequest.MID);
        reqContentMap.put("bizOrderNumber",orderCode);
        reqContentMap.put("notifyUrl",ipAddress + "/v1.0/paymentgateway/topup/hxdhd/transfer/notifyurl");
        reqContentMap.put("accountNumber",prp.getBankCard());
        reqContentMap.put("extraFee",prp.getExtraFee());//结算手续费

        String amout=prp.getRealAmount();
        String extraFee=prp.getExtraFee();
        String realAmount=new BigDecimal(amout).add(new BigDecimal(extraFee)).setScale(2).toString();
//        int a = Integer.valueOf(getNumber(prp.getRealAmount()));
//        int e = Integer.valueOf(getNumber(prp.getExtraFee()));
        reqContentMap.put("srcAmt",realAmount);//结算金额 （到账金额=srcAmt-extraFee）   srcAmt=到账金额+extraFee
        reqContentMap.put("idcard",prp.getIdCard());
        reqContentMap.put("holderName",prp.getUserName());
        reqContentMap.put("tel",prp.getPhone());
        reqContentMap.put("agencyType",HXDHDTopupRequest.AGENCY_TYPE);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(reqContentMap));
        requestMap.put("sign", getSign(reqContentMap));
        LOG.info("请求报文:=========================" + requestMap);
        String resultJSON = null;
        LOG.info("发送请求:=========================Action");
        try {
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + method, null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            if (!HXDHDTopupRequest.SUCCESS.equals(code)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString(HXDHDTopupRequest.MESSAGE));
                return map;
            }
            String date = jsonObject.getString(HXDHDTopupRequest.DATE);
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行出款");
            map.put(CommonConstants.RESULT,date);
            return map;
        } catch (Exception ex) {
            LOG.info("请求发送异常=========================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求发送异常");
            return map;
        }
    }

    /**
     * 结算状态查询
     * @param orderCode  订单号
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhd/transferquery")
    @ResponseBody
    public Object transferQuery(@RequestParam(value = "orderCode") String orderCode){
        Map<String, Object> map = new HashMap<>();
        LOG.info("=============/v1.0/paymentgateway/topup/hxdhd/transferquery=======================Action");
        LOG.info("开始进入结算状态查询======================订单号:" + orderCode);
        Map<String, Object> contentMap = creatContentMap();
        String method = "fastpayTransferQuery";
        contentMap.put("method",method);
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("bizOrderNumber",orderCode);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:================================" + requestMap);
        String resultJSON = null;
        LOG.info("发送请求:================================Action");

        try {
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + method, null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            if (HXDHDTopupRequest.SUCCESS.equals(code)){
                String date = jsonObject.getString(HXDHDTopupRequest.DATE);
                JSONObject dateObject = JSONObject.parseObject(date);
                String txnStatus = dateObject.getString("txnStatus");
                String dataMessage = dateObject.getString("dataMessage");
                if ("p".equals(txnStatus)){
                    LOG.info("支付中==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.WAIT_CHECK);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
                if ("s".equals(txnStatus)){
                    LOG.info("交易成功==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
                if ("c".equals(txnStatus)){
                    LOG.info("交易关闭==================" + orderCode);
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE,dataMessage);
                    return map;
                }
            }
            String message = jsonObject.getString(HXDHDTopupRequest.MESSAGE);
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,message);
            return map;
        } catch (Exception e) {
            LOG.info("请求发送异常=========================" + e);
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求发送异常");
            return map;
        }
    }



    /**
     * 根据银行卡查询余额
     * @param idCard   银行卡号
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxkjd/balance/query/card")
    @ResponseBody
    public Object queryBalance(@RequestParam("bankCard") String idCard){
        Map<String, Object> map = new HashMap<>();
        LOG.info("=============/v1.0/paymentgateway/topup/hxkjd/balance/query/card=======================Action");
        LOG.info("开始进入余额查询======================卡号:" + idCard);
        Map<String, Object> contentMap = creatContentMap();
        contentMap.put("method","fastpayTransferBalanceQuery");
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("idcard",idCard);
        contentMap.put("agencyType",HXDHDTopupRequest.AGENCY_TYPE);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:=========================" + requestMap);
        String resultJSON = null;
        LOG.info("发送请求:=========================Action");
        try {
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + "fastpayTransferBalanceQuery", null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            if (!HXDHDTopupRequest.SUCCESS.equals(code)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"余额查询失败");
                return map;
            }
            String data = jsonObject.getString(HXDHDTopupRequest.DATE);
            if ("-1".equals(data)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE,"该用户无交易");
                return map;
            }
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"余额查询成功");
            map.put(CommonConstants.RESULT,data);
            return map;
        } catch (Exception e) {
            LOG.info("请求发送异常=========================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求发送异常");
            return map;
        }
    }

    /**
     * 发送验证码
     * @param idCard            身份证
     * @param accountNumber     信用卡号
     * @param userName          姓名
     * @param phone             手机号
     * @param expired           有效期
     * @param cvv               安全码
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxkjd/bind/send")
    @ResponseBody
    public Object bindCard(@RequestParam("id_card") String idCard,
                           @RequestParam("account_number") String accountNumber,
                           @RequestParam("user_name") String userName,
                           @RequestParam("phone") String phone,
                           @RequestParam("expired") String expired,
                           @RequestParam("cvv2") String cvv){

        Map<String, Object> map = new HashMap<>();
        LOG.info("====================/v1.0/paymentgateway/topup/hxkjd/bind/send=======================Action");
        Map<String, Object> contentMap = HXDHDTopupRequest.creatContentMap();
        contentMap.put("method","fastpayOpenToken2BySms");
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("idcard",idCard);
        contentMap.put("agencyType",HXDHDTopupRequest.AGENCY_TYPE);
        contentMap.put("accountNumber",accountNumber);
        contentMap.put("holderName",userName);

        contentMap.put("tel",phone);
        contentMap.put("cvv2",cvv);


        contentMap.put("expired",expiredTimeToYYMM(expired));
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:=========================" + requestMap);
        String resultString = null;
        try {
            LOG.info("发送请求:=========================Action");
            resultString = HttpClientUtils.doPost(HXDHDTopupRequest.URL + "fastpayOpenToken2BySms", null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultString);
            JSONObject resultJSONObject = JSONObject.parseObject(resultString);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            String message = jsonObject.getString(HXDHDTopupRequest.MESSAGE);
            LOG.info("响应码:=============================="+code);
            if (!HXDHDTopupRequest.SUCCESS.equals(code)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,message);
                return map;
            }
            String dateS = jsonObject.getString(HXDHDTopupRequest.DATE);
            JSONObject dateJSONObject = JSONObject.parseObject(dateS);
            String isSign = dateJSONObject.getString("isSign");
            String bizOrderNumber = dateJSONObject.getString("bizOrderNumber");
            LOG.info("isSign:=============================="+isSign+"...........bizOrderNumber:========================"+bizOrderNumber);
            HXDHDRegister register = newTopupPayChannelBusiness.getHXDHDRegisterByBankCard(accountNumber);
            HXDHDBindCard bindCard = newTopupPayChannelBusiness.getHXDHDBindCardByBankCard(accountNumber);
            if(HXDHDTopupRequest.SIGN_SUCCESS.equals(isSign)){
                LOG.info("商户已开通:==============================");
                LOG.info("保存用户信息中=============================...");
                if (register == null){
                    HXDHDRegister hxdhdRegister = new HXDHDRegister();
                    hxdhdRegister.setPhone(phone);
                    hxdhdRegister.setUserName(userName);
                    hxdhdRegister.setIdCard(idCard);
                    hxdhdRegister.setBankCard(accountNumber);
                    hxdhdRegister.setCreateTime(new Date());
                /*hxdhdRegister.setExtraFee(extraFee);
                //hxdhdRegister.setMerchantCode();
                hxdhdRegister.setRate(rate);*/
                    hxdhdRegister.setStatus("1");
                    LOG.info("用户信息:==============================" + hxdhdRegister);
                    register = newTopupPayChannelBusiness.createHXDHDRegister(hxdhdRegister);
                    if (register == null){
                        LOG.info("保存用户进件失败==============================");
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE,"用户信息保存失败");
                        return map;
                    }
                }
                if (bindCard == null){
                    LOG.info("保存用户绑卡信息中==============================");
                    HXDHDBindCard hxdhdBindCard = new HXDHDBindCard();
                    hxdhdBindCard.setIdCard(idCard);
                    hxdhdBindCard.setBankCard(accountNumber);
                    hxdhdBindCard.setCreateTime(new Date());
                    hxdhdBindCard.setPhone(phone);
                    hxdhdBindCard.setStatus("1");
                    LOG.info("绑卡信息:==============================" + hxdhdBindCard);
                    bindCard = newTopupPayChannelBusiness.createHXDHDBindCard(hxdhdBindCard);
                    if (bindCard == null){
                        LOG.info("保存用户绑卡信息失败==============================");
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE,"用户信息保存失败");
                        return map;
                    }
                    LOG.info("用户注册绑卡成功:==============================" + hxdhdBindCard);
                }
                map.put(CommonConstants.RESP_CODE,"666666");
                map.put(CommonConstants.RESP_MESSAGE,"商户已开通");
                map.put("redirect_url",ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
                return map;
            }
            LOG.info("商户未开通:==============================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"验证码发送成功");
            if(register == null){
                register = new HXDHDRegister();
                register.setPhone(phone);
                register.setUserName(userName);
                register.setIdCard(idCard);
                register.setBankCard(accountNumber);
                register.setCreateTime(new Date());
                register.setMerchantCode(bizOrderNumber);   //订单号
                register.setStatus("0");
            }else{
                register.setMerchantCode(bizOrderNumber);   //订单号
            }
            LOG.info("用户进件start:==============================" + register);
            HXDHDRegister res = newTopupPayChannelBusiness.createHXDHDRegister(register);
            LOG.info("用户进件end:==============================" + res);
            return map;
        } catch (Exception e) {
            LOG.info("发生异常"+e.toString());
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"亲，网络走失了哦!!!");
            return map;
        }
    }


    /**
     * 短信确认
     * @param idCard            身份证
     * @param accountNumber     信用卡
     * @param userName          姓名
     * @param phone             手机号
     * @param cvv               安全码
     * @param expired           有效期
     * @param smsCode              验证码
     * @param extraFee          额外费率
     * @param rate              费率
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxkjd/bind/check")
    @ResponseBody
    public Object check(@RequestParam("idCard") String idCard,
                        @RequestParam("bankCard") String accountNumber,
                        @RequestParam("userName") String userName,
                        @RequestParam("phone") String phone,
                        @RequestParam("securityCode") String cvv,
                        @RequestParam("expiredTime") String expired,
                        @RequestParam("smsCode") String smsCode,
                        @RequestParam("extraFee") String extraFee,
                        @RequestParam("rate") String rate){
        Map<String, Object> map = new HashMap<>();
        LOG.info("=============/v1.0/paymentgateway/topup/hxkjd/bind/check=======================Action!!!");
        Map<String, Object> contentMap = creatContentMap();
        HXDHDRegister hxdhdRegister = newTopupPayChannelBusiness.getRegisterByBankCard(accountNumber);
        LOG.info("HXDHDRegister：=======================" + hxdhdRegister);
        contentMap.put("method","fastpayOpenToken2CheckSms");
        contentMap.put("mid",HXDHDTopupRequest.MID);
        contentMap.put("idcard",idCard);
        contentMap.put("agencyType",HXDHDTopupRequest.AGENCY_TYPE);
        contentMap.put("accountNumber",accountNumber);
        contentMap.put("holderName",userName);
        contentMap.put("tel",phone);
        contentMap.put("cvv2",cvv);
        contentMap.put("expired",expiredTimeToYYMM(expired));
        contentMap.put("smsCode",smsCode);
        contentMap.put("bizOrderNumber", hxdhdRegister.getMerchantCode());
        //contentMap.put("bizOrderNumber", bizOrderNumber);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("content",JSON.toJSONString(contentMap));
        requestMap.put("sign", getSign(contentMap));
        LOG.info("请求报文:=========================" + requestMap);
        String resultJSON = null;
        try {
            LOG.info("发送请求:=========================Action");
            resultJSON = HttpClientUtils.doPost(HXDHDTopupRequest.URL + "fastpayOpenToken2CheckSms", null, JSON.toJSONString(requestMap));
            LOG.info("响应报文:=============================="+resultJSON);
            JSONObject resultJSONObject = JSONObject.parseObject(resultJSON);
            String resultS = resultJSONObject.getString(CommonConstants.RESULT);
            JSONObject jsonObject = JSONObject.parseObject(resultS);
            String code = jsonObject.getString(HXDHDTopupRequest.CODE);
            LOG.info("响应码:=============================="+code);
            if (!HXDHDTopupRequest.SUCCESS.equals(code)){
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"支付卡开通确认失败");
                return map;
            }
            String dateS = jsonObject.getString(HXDHDTopupRequest.DATE);
            JSONObject dateJSONObject = JSONObject.parseObject(dateS);
            String isSign = dateJSONObject.getString("isSign");
            if(!HXDHDTopupRequest.SIGN_SUCCESS.equals(isSign)){
                LOG.info("商户开通失败:==============================");
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"商户开通失败");
                return map;
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"发送请求失败");
            return map;
        }



        LOG.info("保存用户信息中=============================...");
        //HXDHDRegister hxdhdRegister = new HXDHDRegister();
        hxdhdRegister.setPhone(phone);
        hxdhdRegister.setUserName(userName);
        hxdhdRegister.setIdCard(idCard);
        hxdhdRegister.setBankCard(accountNumber);
        hxdhdRegister.setCreateTime(new Date());
        hxdhdRegister.setExtraFee(extraFee);
        //hxdhdRegister.setMerchantCode();
        hxdhdRegister.setRate(rate);
        hxdhdRegister.setStatus("1");
        LOG.info("用户信息:==============================" + hxdhdRegister);
        HXDHDRegister register = newTopupPayChannelBusiness.createHXDHDRegister(hxdhdRegister);
        if (register == null){
            LOG.info("保存用户进件失败==============================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"用户信息保存失败");
            return map;
        }


        LOG.info("保存用户绑卡信息中==============================");
        HXDHDBindCard hxdhdBindCard = new HXDHDBindCard();
        hxdhdBindCard.setIdCard(idCard);
        hxdhdBindCard.setBankCard(accountNumber);
        hxdhdBindCard.setCreateTime(new Date());
        hxdhdBindCard.setPhone(phone);
        hxdhdBindCard.setStatus("1");
        LOG.info("绑卡信息:==============================" + hxdhdBindCard);
        HXDHDBindCard bindCard = newTopupPayChannelBusiness.createHXDHDBindCard(hxdhdBindCard);
        if (bindCard == null){
            LOG.info("保存用户绑卡信息失败==============================");
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"用户信息保存失败");
            return map;
        }
        LOG.info("用户注册绑卡成功:==============================" + hxdhdBindCard);
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"用户信息保存失败");
        map.put("redirect_url",ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
        return map;
    }

    /**
     * 三级联动
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxkjd/city/query")
    @ResponseBody
    public Object listArea(@RequestParam(value = "id")int id){
        LOG.info("三级联动======================" + id);
        Map<String, Object> map = new HashMap<>();
        List<HXDCity> list = null;
        try {
            list = newTopupPayChannelBusiness.listAreaInfo(id);
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"查询成功");
            map.put(CommonConstants.RESULT,list);
            return map;
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,e);
            return map;
        }
    }


    /**
     * 创建通用请求内容
     * @return
     */
    private static Map<String,Object> creatContentMap(){
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("encryptId",HXDHDTopupRequest.ENCRYPT_ID);
        contentMap.put("apiVersion",HXDHDTopupRequest.API_VERSION);
        contentMap.put("txnDate",System.currentTimeMillis());
        return contentMap;
    }

    /**
     * 获得sign
     * @param content 请求内容
     * @return MD5加密字符串,Sign
     */
    public static String getSign(Map<String,Object> content){
        Map<String, Object> map = new HashMap<>();
        map.put("content",JSON.toJSONString(content));
        map.put("key",HXDHDTopupRequest.KEY);
        String param = JSON.toJSONString(map);
        return MD5Utils.encode(param, "utf-8");
}

    private static final String CODE = "code";

    private static final String DATE = "data";

    private static final String MESSAGE = "message";

    private static final String SUCCESS = "000000";

    private static final String SIGN_SUCCESS = "t"; //开户成功

}
