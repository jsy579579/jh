package com.jh.paymentgateway.controller;


import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.hx.HXDHXRequestBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.hx.HXDHAddress;
import com.jh.paymentgateway.pojo.hx.HXDHXBindCard;
import com.jh.paymentgateway.pojo.hx.HXDHXRegister;
import com.jh.paymentgateway.util.Util;

import com.jh.paymentgateway.util.hxdh.MccMapUtils;
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

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class HXDHXTopupRequest  extends BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(HXDHXTopupRequest.class);

    private final static String key = "3fab2a1d6ec9724b06b57590775c316e";

    private final static String encryptId = "000010000000049";

    private final static Integer apiVersion = 1;

    private final static String mid = "000010000000049";

    private final static String agencyType = "hx";

    //private static final String serverUrl = "https://xxxx/oss-transaction/gateway/";
    private static final String serverUrl = "https://www.dh0102.com/oss-transaction/gateway/";

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private HXDHXRequestBusiness hxdhxRequestBusiness;

    @Autowired
    RedisUtil redisUtil;

    // 跟还款对接的接口
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/dockingEntrance")
    public @ResponseBody
    Object docking1(@RequestParam(value = "bankCard") String bankCard,
                    @RequestParam(value = "idCard") String idCard,
                    @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "userName") String userName,
                    @RequestParam(value = "bankName") String bankName1,
                    @RequestParam(value = "extraFee") String extraFee,
                    @RequestParam(value = "rate") String rate,
                    @RequestParam(value = "expiredTime") String expired) throws Exception {


        Map<String,Object> map = new HashMap<>();
        String expiredTime = this.expiredTimeToYYMM(expired);
        HXDHXRegister register = topupPayChannelBusiness.getHXDHXRegisterByIdCard(idCard);
        HXDHXBindCard bindCard = topupPayChannelBusiness.getHXDHXBindCardByBankCard(bankCard);
       // String bankName = Util.queryBankNameByBranchName(bankName1);
        if (register==null){
            LOG.info("开始进件===============");

            HXDHXRegister hxdhxRegister = new HXDHXRegister();
            hxdhxRegister.setBankCard(bankCard);
            hxdhxRegister.setCreateTime(new Date());
            hxdhxRegister.setExtraFee(extraFee);
            hxdhxRegister.setIdCard(idCard);
            hxdhxRegister.setPhone(phone);
            hxdhxRegister.setRate(rate);
            hxdhxRegister.setStatus("1");
            hxdhxRegister.setUserName(userName);
            topupPayChannelBusiness.createHXDHXRegister(hxdhxRegister);
        }
        if(bindCard==null){
            HXDHXBindCard hxdhxBindCard = new HXDHXBindCard();
            hxdhxBindCard.setBankCard(bankCard);
            hxdhxBindCard.setIdCard(idCard);
            hxdhxBindCard.setStatus("1");
            hxdhxBindCard.setCreateTime(new Date());
            hxdhxBindCard.setPhone(phone);
            topupPayChannelBusiness.createHXDHXBingCard(hxdhxBindCard);

        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"进件绑卡成功");
         return map;
    }

    //下单接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/Precreate")
    public @ResponseBody
    Object pay(@RequestParam(value = "orderCode") String orderCode){
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("环迅开始下单============================");
        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        String method = "fastpayPrecreate2";
        obj.put("mid", mid);
        obj.put("srcAmt", prp.getAmount());
        obj.put("bizOrderNumber",orderCode);
        obj.put("notifyUrl", ipAddress + "/v1.0/paymentgateway/topup/hxdhx/transfer/notifyurl"); //TODO 回调地址
        obj.put("accountNumber",prp.getBankCard());//信用卡
        obj.put("tel",prp.getPhone());//手机号
        BigDecimal bigRate = new BigDecimal(prp.getRate()).multiply(new BigDecimal(100));
        obj.put("fastpayFee",bigRate);//费率
        obj.put("agencyType",agencyType);//TODO 通道标识T
        obj.put("holderName",prp.getUserName());//持卡人姓名
        obj.put("idcard",prp.getIdCard());//身份证
        obj.put("settAccountNumber","");//无意义
        obj.put("settAccountTel","");//无意义
        obj.put("cvv2",prp.getSecurityCode());//安全码

        obj.put("expired",this.expiredTimeToYYMM(prp.getExpiredTime()));//过期时间 YYMM
        String extra = prp.getExtra();// 消费计划|福建省-泉州市-350500

        LOG.info("extra====================="+extra);

        String cityName = extra.substring(extra.indexOf("-") + 1);
        LOG.info("=======================================消费城市：" + cityName);
        obj.put("city",cityName);//消费城市
        obj.put("mcc","");//行业类别

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("method",method);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());

        content.put("content", JSONObject.toJSONString(obj,SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX下单请求内容================"+signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl+method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj,SerializerFeature.WriteMapNullValue);

        LOG.info("HXDHX下单响应内容================="+signStr);
        sign = md5(signStr, "utf-8");
        //System.out.println(sign.equals(resultObj.getString("sign")));
        String resultStr = resultObj.getString("result");
        //String html = JSONObject.parseObject(resultStr).getJSONObject("data").getString("openHtml");
        String returnCode = JSONObject.parseObject(resultStr).getString("code");
        //System.out.println(html);
        if ("000000".equals(returnCode) ) {
            return ResultWrap.init("999998", "等待银行扣款中");
        } else {
            String errtext = JSONObject.parseObject(resultStr).getString("message");
            return ResultWrap.init(CommonConstants.FALIED, errtext);
        }
    }

    // 交易查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/orderquery")
    public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {

        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        Map<String, Object> maps = new HashMap<String, Object>();
        String method = "fastpayQuery";

        obj.put("mid", mid);
        obj.put("method",method);
        obj.put("bizOrderNumber",orderCode);

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());

        content.put("content", JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX订单查询请求内容================"+signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl+method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj,SerializerFeature.WriteMapNullValue);

        LOG.info("HXDHX订单查询响应内容=================" + signStr);

        String resultStr = resultObj.getString("result");
        String returnCode = JSONObject.parseObject(resultStr).getString("code");
        String message = JSONObject.parseObject(resultStr).getString("message");

        if ("000000".equals(returnCode)) {
            String txnStatus = JSONObject.parseObject(JSONObject.parseObject(resultStr).getString("data")).getString("txnStatus");
            String dataMessage = JSONObject.parseObject(JSONObject.parseObject(resultStr).getString("data")).getString("dataMessage");
            if("s".equals(txnStatus)){      //s代表成功， p 代表支付中 c代表交易关闭
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, dataMessage);
            }else if("p".equals(txnStatus)){
                maps.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                maps.put(CommonConstants.RESP_MESSAGE, dataMessage);
            }else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, dataMessage);
            }
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, message);
        }
        return maps;
    }

    // 代付的异步通知接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/transfer/notifyurl")
    public String topupBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("HXDHX下单异步通知======");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String bizOrderNumber = request.getParameter("bizOrderNumber");
        String completedTime = request.getParameter("completedTime");
        String mid = request.getParameter("mid");
        String srcAmt = request.getParameter("srcAmt");
        String sign = request.getParameter("sign");
        String md5Sign = md5("bizOrderNumber="+bizOrderNumber+"&completedTime="+completedTime+"&mid="+mid+"&srcAmt="+srcAmt+"&key="+key,"utf-8");
        if(!sign.equals(md5Sign)){
            LOG.info("签名不正确");
            return "false";
        }

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(bizOrderNumber);
        String version = "31";
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
            LOG.info("RESULT================" + result);
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
            LOG.error("", e);
        }
        LOG.info("订单状态修改成功===================" + bizOrderNumber + "====================" + result);

        return "success";
    }

    // 环迅结算接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/transfercreate")
    public @ResponseBody
    Object transferCreate(@RequestParam(value = "orderCode") String orderCode) {
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("环迅开始结算============================");
        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        String method = "fastpayTransferCreate";
        obj.put("mid",mid);

        String amout=prp.getRealAmount();
        String extraFee=prp.getExtraFee();
        String realAmount=new BigDecimal(amout).add(new BigDecimal(extraFee)).setScale(2).toString();

//        int a = Integer.valueOf(getNumber(prp.getRealAmount()));
//        int e = Integer.valueOf(getNumber(prp.getExtraFee()));
        obj.put("srcAmt",realAmount); //结算金额 （到账金额=srcAmt-extraFee）   srcAmt=到账金额+extraFee
        obj.put("bizOrderNumber",orderCode);
        obj.put("notifyUrl", ipAddress + "/v1.0/paymentgateway/topup/hxdhx/transfer/notifyurl"); //TODO 回调地址
        obj.put("accountNumber",prp.getBankCard());//信用卡
        obj.put("extraFee",prp.getExtraFee());//结算手续费
        obj.put("idcard",prp.getIdCard());//身份证
        obj.put("holderName",prp.getUserName());//持卡人姓名
        obj.put("tel",prp.getPhone());//手机号
        obj.put("agencyType",agencyType);//TODO 通道标识T

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("method",method);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());

        content.put("content", JSONObject.toJSONString(obj,SerializerFeature.WriteMapNullValue));
        content.put("key", key);

        String signStr = JSON.toJSONString(content,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX结算请求内容================"+signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl+method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX结算响应内容================="+signStr);
        String resultStr = resultObj.getString("result");
        String returnCode = JSONObject.parseObject(resultStr).getString("code");
        String messsage = JSONObject.parseObject(resultStr).getString("message");

        if("000000".equals(returnCode)){
            return ResultWrap.init("999998",messsage);
        }else{
            String errtext = JSONObject.parseObject(resultStr).getString("message");
            return ResultWrap.init(CommonConstants.FALIED, errtext);
        }
    }
    //结算查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/transferquery")
    public @ResponseBody Object backOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        Map<String, Object> maps = new HashMap<String, Object>();
        String method = "fastpayTransferQuery";

        obj.put("mid", mid);
        obj.put("method",method);
        obj.put("bizOrderNumber",orderCode);

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());

        content.put("content", JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX结算查询请求内容================"+signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl+method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj,SerializerFeature.WriteMapNullValue);

        LOG.info("HXDHX就算查询响应内容=================" + signStr);

        String resultStr = resultObj.getString("result");
        String returnCode = JSONObject.parseObject(resultStr).getString("code");
        String message = JSONObject.parseObject(resultStr).getString("message");

        if ("000000".equals(returnCode)) {
            String txnStatus = JSONObject.parseObject(JSONObject.parseObject(resultStr).getString("data")).getString("txnStatus");
            if("s".equals(txnStatus)){
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, JSONObject.parseObject(resultStr).getString("data"));
            }else{
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, JSONObject.parseObject(resultStr).getString("data"));
            }
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, message);
        }
        return  maps;
    }
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/balancequery1")
    public @ResponseBody Object balanceQuery1(@RequestParam(value = "bankCard") String bankCard) throws Exception {
        Map<String, Object> o = (Map<String, Object>) balanceQuery(bankCard);
        String resp_message = o.get("resp_message").toString();
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("balance",resp_message);
        return jsonObject;
    }

    //环迅余额查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/balancequery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) throws Exception {
        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        Map<String, Object> maps = new HashMap<String, Object>();
        String method = "fastpayTransferBalanceQuery";

        obj.put("mid", mid);
        obj.put("method",method);
        obj.put("idcard",idCard);
        obj.put("agencyType",agencyType);

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());

        content.put("content", JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX余额查询请求内容================"+signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl+method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj,SerializerFeature.WriteMapNullValue);
        LOG.info("HXDHX余额查询响应内容=================" + signStr);
        String resultStr = resultObj.getString("result");
        String returnCode = JSONObject.parseObject(resultStr).getString("code");
        String message = JSONObject.parseObject(resultStr).getString("message");
        if ("000000".equals(returnCode)) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, JSONObject.parseObject(resultStr).getString("data"));
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, message);
        }
        return maps;
    }







    /**
     * 通道方法
     *
     * @param url
     * @param params
     * @return
     */
    public static String sendHttpsPost(String url, String params){
        DataOutputStream out = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
        URL u = null;
        HttpsURLConnection con = null;
        //尝试发送请求
        try{
            System.out.println(params);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
                    new java.security.SecureRandom());
            u = new URL(url);
            //打开和URL之间的连接
            con = (HttpsURLConnection)u.openConnection();
            //设置通用的请求属性
            con.setSSLSocketFactory(sc.getSocketFactory());
            con.setHostnameVerifier(new TrustAnyHostnameVerifier());
            //con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json"); //
            con.setUseCaches(false);
            //发送POST请求必须设置如下两行
            con.setDoOutput(true);
            con.setDoInput(true);

            con.connect();
            out = new DataOutputStream(con.getOutputStream());
            out.write(params.getBytes("utf-8"));
            // 刷新、关闭
            out.flush();
            out.close();
            //读取返回内容
            //InputStream is = con.getInputStream();
            //定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            String line;
            while((line = in.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            System.out.println(result);
            return result.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
                if(con != null) {
                    con.disconnect();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();
    }


    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    public final static String md5(String s, String entype) {
        String result = "";
        char hexDigits[] =
                { '0', '1', '2', '3',
                        '4', '5', '6', '7',
                        '8', '9', 'a', 'b',
                        'c', 'd', 'e', 'f' };
        try {
            byte[] strTemp = s.getBytes(entype);
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte b = md[i];
                str[k++] = hexDigits[b >> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            result = new String(str);
        } catch (Exception e)
        {e.printStackTrace();}
        return result;
    }



    //环迅获取城市列表
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/getcity")
    public Object apRegister(@RequestParam("province") String province ) {
        List<HXDHAddress> hxdhAddresses = new ArrayList<>();
        List<HXDHAddress> hxdhAddressesResp = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        try{
            if(province.equals("0")){
                hxdhAddresses = hxdhxRequestBusiness.getHXDHAddressby0();
            }else{
                hxdhAddresses = hxdhxRequestBusiness.getHXDHAddressbyProvince(province);
            }

            Map<String, String> mccMap = MccMapUtils.init();
            for (HXDHAddress hxdhAddress : hxdhAddresses) {
                Map<String, String> mccNames = new HashMap<>(16);
                String[] mccs = hxdhAddress.getMcc().split(","); //5699:26,5094:12,5813:27,5411:25,7992:22
                for (String mcc : mccs) {
                    String s = mcc.split(":")[0];
                    if(mccMap.get(s) != null){
                        mccNames.put(mccMap.get(s), s);
                    }
                }
                hxdhAddress.setMccNamesAndCode(mccNames);
                hxdhAddressesResp.add(hxdhAddress);
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "获取成功");
            map.put(CommonConstants.RESULT,hxdhAddressesResp);
        }catch (Exception e){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "获取城市列表发生异常,详情："+e.getMessage());
        }
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hxdhx/querybycity")
    @ResponseBody
    public Object hxQueryAddressByCity(@RequestParam("city") String city ) {
       Map map= new HashMap<>();
       List<HXDHAddress> hxdhAddresses = hxdhxRequestBusiness.getHXDHAddressbyCity(city);
       if(hxdhAddresses.size()>0){
           map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
           map.put(CommonConstants.RESP_MESSAGE, "获取成功");
           map.put(CommonConstants.RESULT,hxdhAddresses.get(0));
           return map;
       }else{
           map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
           return map;
       }

    }


    /**
     * 金额/元
     *
     * @param ExtraFee
     * @return
     */
    public static String getNumber(String ExtraFee) {
        BigDecimal num1 = new BigDecimal(ExtraFee);
        BigDecimal MS = num1.setScale(0, BigDecimal.ROUND_DOWN);
        return MS.toString();
    }
}
