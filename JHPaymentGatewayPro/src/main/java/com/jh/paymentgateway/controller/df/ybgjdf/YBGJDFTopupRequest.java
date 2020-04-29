package com.jh.paymentgateway.controller.df.ybgjdf;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.df.YBGJDFChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.ybgjdf.YbgjdfBankCode;
import com.jh.paymentgateway.util.df.ybgjdf.CommunicationConsumeSecurityWarper;
import com.jh.paymentgateway.util.df.ybgjdf.CommunicationProvideSecurityWarper;
import com.jh.paymentgateway.util.df.ybgjdf.HttpUtil;
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
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class YBGJDFTopupRequest extends BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(YBGJDFTopupRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private YBGJDFChannelBusiness ybgjdfChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Value("${ybgj.partnerNo}")
    private String partnerNo;

    @Value("${ybgj.myPrivateKey}")
    private String myPrivateKey;

    @Value("${ybgj.publicKeyPartner}")
    private String publicKeyPartner;


    //下单接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybgjdf/pay")
    public @ResponseBody
    Object pay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        Map<String, Object> maps = new HashMap<String, Object>();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String accountNo = prp.getDebitCardNo(); //到账卡号
        String accountName = prp.getUserName(); //户名
        String bankName = prp.getDebitBankName();//到账卡名字
        YbgjdfBankCode ybgjdfBankCodeByBankName = ybgjdfChannelBusiness.findYbgjdfBankCodeByBankName(bankName);
        if(ybgjdfBankCodeByBankName == null){
            maps.put(CommonConstants.RESP_CODE, "99999");
            maps.put(CommonConstants.RESP_MESSAGE, "下单失败，到账卡银行不支持，请联系客服");
            return ResultWrap.init(CommonConstants.SUCCESS, "下单失败",maps);
        }
        String bankNo = ybgjdfBankCodeByBankName.getBankCode();

        String certificateNo = prp.getIdCard();
        BigDecimal realAmount = new BigDecimal(prp.getRealAmount()).multiply(new BigDecimal("100")).setScale(0);
        String txnAmt = realAmount.toString();
        String mobile = prp.getPhone();

        String txnCode = "102001";
        Map<String,String> head = new HashMap<String,String>();
        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo",partnerNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);  //单笔代付
        head.put("traceId", orderCode); //订单号
        Date date = new Date();
        head.put("reqDate", dateToStringyyyMMdd(date));
        head.put("reqTime",dateToString(date));

        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("head",head);
        reqMap.put("callBackUrl", ipAddress + "/v1.0/paymentgateway/topup/ybgjdf/pay/notifyurl"); //异步回调地址
       // reqMap.put("callBackUrl", "http://139.196.125.48/v1.0/paymentgateway/topup/ybgjdf/pay/notifyurl");
        reqMap.put("accountCategory", "PERSON");    //收款人种类 PERSON-个人 ENTERPRISE-企业
        reqMap.put("accountNo", accountNo);// 收款账户
        reqMap.put("accountName", accountName);
        reqMap.put("bankNo",bankNo);
        reqMap.put("bankName",bankName);
        reqMap.put("certificateType", "ID");
        reqMap.put("certificateNo",certificateNo);
        reqMap.put("currency", "156");
        reqMap.put("txnAmt",txnAmt);         //单位：分
        //reqMap.put("txnAmt","100");
        reqMap.put("mobile",mobile);
        reqMap.put("purpose", "代付");
        String s = JSONObject.toJSONString(reqMap);
        LOG.info("易百管家代付请求的参数为："+s);

        System.out.println(publicKeyPartner+"========="+myPrivateKey);

        //上游代付公钥
        PublicKey publicKeyplatform = getPublicKey(publicKeyPartner);

        //平台秘钥
        PrivateKey privatekeypartner = getPrivateKey(myPrivateKey);


        //加密串
        String randomAESKey =CommunicationConsumeSecurityWarper.getRandomAESKey();
        LOG.info("AES密钥为：" + randomAESKey);

        String cipherB64AESKey = CommunicationConsumeSecurityWarper.getCipherB64AESKeyByplatformPublicKey(randomAESKey,
                publicKeyplatform);

        String cipherB64PlainText = CommunicationConsumeSecurityWarper.getCipherB64PlainTextByPartnerAESKey(s, randomAESKey);

        String signB64PlainText = CommunicationConsumeSecurityWarper.getSignB64PlainTextByPartnerPrivateKey(s, privatekeypartner);

        Map<String, String> nvps = new HashMap<String, String>();
        nvps.put("encryptData", cipherB64PlainText);
        nvps.put("encryptKey", cipherB64AESKey);
        nvps.put("signData", signB64PlainText);
        nvps.put("traceId", orderCode);
        nvps.put("partnerNo", partnerNo);
        String respstr;
//        https://payfor.jfpays.com/rest/v1/api/
        try{
            respstr = HttpUtil.sendPost("https://payfor.jfpays.com/rest/v1/api/" + txnCode, nvps );
            JSONObject jsonObject = JSONObject.parseObject(respstr);
            String cipherPartnerAES = CommunicationProvideSecurityWarper.getPlainTextByAESKey(jsonObject.getString("cipherPartnerAES"), randomAESKey);
            LOG.info("易百管家代付响应参数为：" + cipherPartnerAES);
            String respCode = JSONObject.parseObject(JSONObject.parseObject(cipherPartnerAES).getString("head")).getString("respCode");
            String respMsg = JSONObject.parseObject(JSONObject.parseObject(cipherPartnerAES).getString("head")).getString("respMsg");
            if("000000".equals(respCode)|"000001".equals(respCode)){
                maps.put(CommonConstants.RESP_CODE, "00000");
                maps.put(CommonConstants.RESP_MESSAGE, "下单成功");
                return ResultWrap.init(CommonConstants.SUCCESS, "下单成功",maps);
            }else {
                maps.put(CommonConstants.RESP_CODE, "99999");
                maps.put(CommonConstants.RESP_MESSAGE, respMsg);
                return ResultWrap.init(CommonConstants.SUCCESS, "下单失败",maps);
            }

        }catch (Exception e){
            LOG.error("易百管家代付接口出现异常======", e);
            maps.put(CommonConstants.RESP_CODE, "99999");
            maps.put(CommonConstants.RESP_MESSAGE, "下单失败");
            return ResultWrap.init(CommonConstants.SUCCESS, "下单失败",maps);
        }
    }
    //代付异步通知
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybgjdf/pay/notifyurl")
    public @ResponseBody
    Object payNotify(HttpServletRequest request, HttpServletResponse response){
        LOG.info("ybgjdf通道异步通知进来了===================================");
        try{
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<String> keySet = parameterMap.keySet();
            for (String key : keySet) {
                String[] strings = parameterMap.get(key);
                for (String s : strings) {
                    LOG.info(key + "=============" + s);
                }
            }
            String encryptKey = request.getParameter("encryptKey");
            String encryptData = request.getParameter("encryptData");
            String signData = request.getParameter("signData");
            String traceld = request.getParameter("traceld");
            String partnerNo = request.getParameter("partnerNo");

            PublicKey publicKeyUp = getPublicKey(publicKeyPartner);
            //平台秘钥
            PrivateKey privatekeypartner = getPrivateKey(myPrivateKey);

            //解密128bit 16位AES密钥
            String partnerAESKey = CommunicationProvideSecurityWarper.getPartnerAESKeyByPlatformPrivateKey(encryptKey,
                    privatekeypartner);

            //使用partnerAESKey解密数据
            String plainText = CommunicationProvideSecurityWarper.getPlainTextByAESKey(encryptData, partnerAESKey);

            //验签
            boolean b = CommunicationProvideSecurityWarper.checkSignB64PlainTextByPartnerPublicKey(plainText, signData,
                    publicKeyUp);
            if(!b){
                LOG.info("ybgjdf通道异步通知验签失败");
                return "000000";
            }
            LOG.info("ybgjdf通道异步通知的json字符串为："+plainText);
            JSONObject jsonObject = JSONObject.parseObject(plainText);
            String head = jsonObject.getString("head");
            String traceId = jsonObject.getString("traceId");
            String respCode = JSONObject.parseObject(head).getString("respCode");
            String platformId = JSONObject.parseObject(head).getString("platformId");
            if(!respCode.equals("000000")){
                LOG.info(jsonObject.getString("traceId")+"订单失败，详情：" + JSONObject.parseObject(head).getString("respMsg"));
                return "000000";
            }
            String result = null;
            RestTemplate restTemplate = new RestTemplate();
            String url = ipAddress + ChannelUtils.getCallBackUrl(ipAddress);
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", traceId);
            requestEntity.add("third_code", platformId);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("修改订单状态失败", e);
            }

        }catch (Exception e){
            LOG.error("ybgjdf通道异步通知发生了异常："+ e);
            return "000000";
        }
        return "000000";
    }
    //交易查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybgjdf/query")
    public @ResponseBody
    Object payQuery(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        String traceId = UUID.randomUUID().toString().replace("-","");
        String txnCode = "102020";
        Map<String,String> head = new HashMap<String,String>();
        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo",partnerNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);  //查询
        head.put("traceId", traceId); //订单号
        Date date = new Date();
        head.put("reqDate", dateToStringyyyMMdd(date));
        head.put("reqTime",dateToString(date));

        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("head",head);
        reqMap.put("oriTraceId", orderCode); //查询订单号

        String s = JSONObject.toJSONString(reqMap);
        LOG.info("易百管家代付交易查询请求的参数为："+s);



        //上游代付公钥
        //PublicKey publicKeyplatform = RSAKey.getRSAPublicKeyByRelativeFileSuffix("key/keyPlatformKb/jf_public_test_key_2048.pem", "pem");
        PublicKey publicKeyplatform = getPublicKey(publicKeyPartner);
        //平台秘钥
        //PrivateKey privatekeypartner = RSAKey.getRSAPrivateKeyByRelativePathFileSuffix("key/KeyPartnerKb/1_pkcs8_rsa_private_key_2048.pem", "pem");
        PrivateKey privatekeypartner = getPrivateKey(myPrivateKey);
        //加密串
        String randomAESKey = CommunicationConsumeSecurityWarper.getRandomAESKey();

        LOG.info("AES密钥："+ randomAESKey);

        String cipherB64AESKey = CommunicationConsumeSecurityWarper.getCipherB64AESKeyByplatformPublicKey(randomAESKey,
                publicKeyplatform);

        String cipherB64PlainText = CommunicationConsumeSecurityWarper.getCipherB64PlainTextByPartnerAESKey(s, randomAESKey);

        String signB64PlainText = CommunicationConsumeSecurityWarper.getSignB64PlainTextByPartnerPrivateKey(s, privatekeypartner);


        Map<String, String> nvps = new HashMap<String, String>();
        nvps.put("encryptData", cipherB64PlainText);
        nvps.put("encryptKey", cipherB64AESKey);
        nvps.put("signData", signB64PlainText);
        nvps.put("traceId", traceId);
        nvps.put("partnerNo", partnerNo);

        String respstr = HttpUtil.sendPost("https://payfor.jfpays.com/rest/v1/api/" + txnCode, nvps );
        JSONObject jsonObject = JSONObject.parseObject(respstr);
        String str = CommunicationProvideSecurityWarper.getPlainTextByAESKey(jsonObject.get("cipherPartnerAES").toString(), randomAESKey);
        LOG.info("易百管家代付交易查询响应的参数为："+ str);
        Map<String, Object> maps = new HashMap<>();
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE, str);
        return maps;
    }

    //查询余额
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybgjdf/queryBalance")
    public @ResponseBody
    Object payQuery() throws Exception {
        String traceId = UUID.randomUUID().toString().replace("-","");
        String txnCode = "103001";
        Map<String,String> head = new HashMap<String,String>();
        head.put("version", "1.0.0");
        head.put("charset", "UTF-8");
        head.put("partnerNo",partnerNo);
        head.put("partnerType", "OUTER");
        head.put("txnCode", txnCode);  //查询商户余额
        head.put("traceId", traceId); //订单号
        Date date = new Date();
        head.put("reqDate", dateToStringyyyMMdd(date));
        head.put("reqTime",dateToString(date));

        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("head",head);
        reqMap.put("cutTime", dateToString(new Date())); //查询订单号
        reqMap.put("partnerAccType",  "01");            //01-代付基本账户 02-代付T0账户

        String s = JSONObject.toJSONString(reqMap);
       LOG.info("易百管家余额查询请求的参数为："+s);



        //上游代付公钥
        //PublicKey publicKeyplatform = RSAKey.getRSAPublicKeyByRelativeFileSuffix("key/keyPlatformKb/jf_public_test_key_2048.pem", "pem");
        PublicKey publicKeyplatform = getPublicKey(publicKeyPartner);
        //平台秘钥
        //PrivateKey privatekeypartner = RSAKey.getRSAPrivateKeyByRelativePathFileSuffix("key/KeyPartnerKb/1_pkcs8_rsa_private_key_2048.pem", "pem");
        PrivateKey privatekeypartner = getPrivateKey(myPrivateKey);
        //加密串
        String randomAESKey = CommunicationConsumeSecurityWarper.getRandomAESKey();

        LOG.info("AES密钥："+ randomAESKey);

        String cipherB64AESKey = CommunicationConsumeSecurityWarper.getCipherB64AESKeyByplatformPublicKey(randomAESKey,
                publicKeyplatform);

        String cipherB64PlainText = CommunicationConsumeSecurityWarper.getCipherB64PlainTextByPartnerAESKey(s, randomAESKey);

        String signB64PlainText = CommunicationConsumeSecurityWarper.getSignB64PlainTextByPartnerPrivateKey(s, privatekeypartner);


        Map<String, String> nvps = new HashMap<String, String>();
        nvps.put("encryptData", cipherB64PlainText);
        nvps.put("encryptKey", cipherB64AESKey);
        nvps.put("signData", signB64PlainText);
        nvps.put("traceId", traceId);
        nvps.put("partnerNo", partnerNo);

        String respstr = HttpUtil.sendPost("https://payfor.jfpays.com/rest/v1/api/" + txnCode, nvps );
        JSONObject jsonObject = JSONObject.parseObject(respstr);
        String str = CommunicationProvideSecurityWarper.getPlainTextByAESKey(jsonObject.get("cipherPartnerAES").toString(), randomAESKey);
        LOG.info("易百管家余额查询响应m的参数为："+ str);
        Map<String, Object> maps = new HashMap<>();
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE, str);
        return maps;
    }

    public static String dateToString(Date date){
        DateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = format1.format(date);
        return dateString;
    }
    public static String dateToStringyyyMMdd(Date date){
        DateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        String dateString = format1.format(date);
        return dateString;
    }
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
}
