package com.jh.paymentgateway.controller.qj;

import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.qj.QJBindCard;
import com.jh.paymentgateway.pojo.qj.QJRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zy
 * @date 2019/6/12
 * @description 钱嘉
 */
@Controller
@EnableAutoConfiguration
public class QJPageRequest extends BaseChannel {

    private static final Logger LOG = LoggerFactory.getLogger(QJPageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ip;

    private static String key = "22fce5b72e4a7fed864ada0945b1d2ce"; //公钥

    private final static String encryptId = "000600000000180";//默认字符串

    private final static Integer apiVersion = 1;//api版本号

    private static final String serverUrl = "https://www.znyoo.com/oss-transaction/gateway/";//请求地址

    private static final String mid = "000600000000180";

    private static final String agencyType = "xwk66";


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/qj/register")
    public Object getRegister(@RequestParam(value = "orderCode") String orderCode)
            throws IOException {
        LOG.info("开始进入注册进件====================");

        Map<String, Object> map = new HashMap<>();

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

        String idCard = prp.getIdCard();//身份证号
        String bankCard = prp.getBankCard(); //银行卡号
        String phone = prp.getPhone();//预留电话
        String userName = prp.getUserName(); // 姓名
        String securityCode = prp.getSecurityCode(); //安全码
        String extraFee = prp.getExtraFee();
        String expiredTime = prp.getExpiredTime();
        String rate = prp.getRate();

        SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");

        String date = sd.format(new Date());

        QJRegister qjRegister = topupPayChannelBusiness.getQJRegisterByIdCard(idCard);

        QJBindCard qjBindCard = topupPayChannelBusiness.getQJBindCardByBankCard(bankCard);


        if (qjBindCard == null) {  //卡信息绑定
            LOG.info("进入绑卡入口============");
            QJBindCard bindCard = new QJBindCard();
            bindCard.setBankCard(bankCard);
            bindCard.setIdCard(idCard);
            bindCard.setUserName(userName);
            bindCard.setStatus("1");
            bindCard.setExpiredTime(expiredTime);
            bindCard.setExtraFee(extraFee);
            bindCard.setSecurityCode(securityCode);
            bindCard.setCreateTime(date);
            bindCard.setPhone(phone);
            LOG.info("办卡参数============" + "bankCard" + bankCard + "idCard" + idCard + "userName" + userName + "expiredTime" + expiredTime + "extraFee" + extraFee + "securityCode" + securityCode + "phone" + phone);
            topupPayChannelBusiness.createQJBindCard(bindCard);
            map.put(CommonConstants.RESULT, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "本地绑卡成功");
            return map;
        }


        if (qjRegister == null) { //未注册
            QJRegister register = new QJRegister();

            register.setIdCard(idCard);
            register.setRate(rate);
            register.setBankCard(bankCard);
            register.setExtraFee(extraFee);
            register.setPhone(phone);
            register.setCreateTime(date);
            LOG.info("注册参数===========" + "idCard" + idCard + "rate" + rate + "bankCard" + bankCard + "extraFee" + extraFee + "phone" + phone + "userName" + userName);
            topupPayChannelBusiness.createQJRegister(register);
            map.put(CommonConstants.RESULT, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "本地进件");
            return map;
        }
        map.put(CommonConstants.RESULT, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "用户已绑卡并进件");
        return map;
    }

    /**
     * 下单接口
     *
     * @param orderCode
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/qj/getPay")
    public Object getPay(@RequestParam(value = "orderCode") String orderCode)
            throws IOException {


        Map<String, Object> map = new HashMap();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String amount = prp.getAmount();//金额
        String phone = prp.getPhone();
        String rate = prp.getRate();
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
        String userName = prp.getUserName();
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard(); //信用卡号

        String debitCardNo = prp.getDebitCardNo(); //储蓄卡
        String debitPhone = prp.getDebitPhone();
        String extraFee = prp.getExtraFee();


        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        String method = "fastpayPrecreate2";      // 调用方法名  默认值：fastpayPrecreate2
        obj.put("mid", mid);//商户号
        obj.put("srcAmt", amount); //金额（元）//Double
//        Date date = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//        String time = format.format(date);
//        String bizOrderNumber = "qj" + format.format(date);
//        LOG.info("订单号=======" + bizOrderNumber);

        obj.put("bizOrderNumber", orderCode);//商户订单号（不传则由后台自行生成）
        obj.put("notifyUrl", "http://106.15.26.208/v1.0/paymentgateway/topup/qj/call_back"); //异步通知地址以http或https开头
        obj.put("accountNumber", bankCard);//卡号　 信用卡
        obj.put("tel", phone);//预留手机号
        obj.put("frontUrl", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess"); //v1.0/paymentgateway/topup/qj/paysuccessView
        System.out.println(bigRate);
        obj.put("fastpayFee", bigRate);//   交易手续费 0.5%传0.5  通道：0.41  Double
        obj.put("agencyType", agencyType);   //通道标识，钱嘉提供
        obj.put("holderName", userName); //姓名
        obj.put("idcard", idCard); //身份证
        obj.put("settAccountNumber", debitCardNo);// 结算卡  储蓄卡
        obj.put("settAccountTel", debitPhone);//结算卡预留手机号
        obj.put("extraFee", extraFee); // 额外手续费（元），通道：0.5  Double

        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("method", "fastpayPrecreate2"); //方法名
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());//当前时间戳

        content.put("content", JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content, SerializerFeature.WriteMapNullValue);
        System.out.println("signStr==" + signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl + method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj, SerializerFeature.WriteMapNullValue);
        System.out.println("signStr==" + signStr);
        sign = md5(signStr, "utf-8");
        System.out.println(sign.equals(resultObj.getString("sign")));
        String resultStr = resultObj.getString("result");
        String html = JSONObject.parseObject(resultStr).getJSONObject("data").getString("openHtml");
        System.out.println(html);
        System.out.println("============================================");
        System.out.println("signStr"+signStr);



        JSONObject jsonObject = JSONObject.parseObject(signStr);
        Object result1 = jsonObject.get("result");
        System.out.println("result1================="+result1);


        JSONObject jsonObject1 = JSONObject.parseObject((String) result1);
        Object code = jsonObject1.get("code");
        System.out.println("code====================="+code);
        Object data = jsonObject1.get("data");
        System.out.println("data==============="+data.toString());

        Map<String, Object> maps= JSON.parseObject(data.toString(), Map.class);
        String  openUrl = (String) maps.get("openUrl");
        System.out.println(openUrl);

        if ("000000".equals(code)) {
            LOG.info("下单成功==============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT,openUrl);
            map.put(CommonConstants.RESP_MESSAGE, "下单成功");
            return map;
        }
        LOG.info("下单失败==============");
        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE, "下单失败");
        return map;
    }

    /**
     * 后台回调
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/qj/call_back")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOG.info("支付回调回来了==============================");

        Map<String,Object> map = new HashMap();


        //TODO  JSON 返回的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }

        String bizOrderNumber = request.getParameter("bizOrderNumber");// 商户订单号
        String completedTime = request.getParameter("completedTime");// 完成时间
        String mid = request.getParameter("mid"); //商户号
        String srcAmt = request.getParameter("srcAmt");// 金额
        String sign = request.getParameter("sign"); //签名

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(bizOrderNumber);


        LOG.info("返回参数=====" + "bizOrderNumber" + bizOrderNumber + "completedTime" + completedTime + "mid" + mid + srcAmt + "sign" + sign);

        LOG.info("订单号bizOrderNumber-----------" + bizOrderNumber);
        LOG.info("请求绑卡商户号mid-----------" + mid);
        LOG.info("交易成功=================================");

        LOG.info("*********************交易成功***********************");

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        String url = null;
        String result = null;

        url = prp.getIpAddress()+ ChannelUtils.getCallBackUrl(prp.getIpAddress());
        //url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

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

        LOG.info("订单已交易成功!");

        PrintWriter pw = response.getWriter();
        pw.print("success");
        pw.close();
    }

    /**
     * 查询订单状态接口
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/qj/payQuery")
    public Object payQuery(@RequestParam(value = "orderCode") String orderCode)
            throws IOException {

        Map<String,Object> map = new HashMap<>();
        JSONObject content = new JSONObject();
        JSONObject obj = new JSONObject();
        String method = "fastpayQuery";
        obj.put("method", "fastpayQuery");//方法名
        obj.put("mid", mid);
        obj.put("bizOrderNumber", orderCode);
        obj.put("encryptId", encryptId);
        obj.put("apiVersion", apiVersion);
        obj.put("txnDate", Calendar.getInstance().getTimeInMillis());//当前时间戳
        content.put("content", JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
        content.put("key", key);
        String signStr = JSON.toJSONString(content, SerializerFeature.WriteMapNullValue);
        System.out.println("signStr==" + signStr);
        String sign = md5(signStr, "utf-8");
        content.remove("key");
        content.put("sign", sign);
        String result = sendHttpsPost(serverUrl + method, JSON.toJSONString(content));
        JSONObject resultObj = JSONObject.parseObject(result);
        JSONObject resultSignObj = new JSONObject();
        resultSignObj.put("result", resultObj.getString("result"));
        resultSignObj.put("key", key);
        signStr = JSON.toJSONString(resultSignObj, SerializerFeature.WriteMapNullValue);
        System.out.println("signStr==" + signStr);
        sign = md5(signStr, "utf-8");
        System.out.println(sign.equals(resultObj.getString("sign")));
        String resultStr = resultObj.getString("result");
        String html = JSONObject.parseObject(resultStr).getJSONObject("data").getString("openHtml");
        System.out.println(html);


        JSONObject jsonObject = JSONObject.parseObject(signStr);
        Object result1 = jsonObject.get("result");
        System.out.println("result1================="+result1);


        JSONObject jsonObject1 = JSONObject.parseObject((String) result1);
        Object code = jsonObject1.get("code");
        System.out.println("code====================="+code);
        Object data = jsonObject1.get("data");
        System.out.println("data==============="+data.toString());

        Map<String, Object> maps= JSON.parseObject(data.toString(), Map.class);
        String  status = (String) maps.get("txnStatus");
       // System.out.println(status);


        if("s".equals(status)){
            LOG.info("交易成功=========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"交易成功");
            return map ;
        }else  if("p".equals(status)){
            LOG.info("交易处理中=============");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE,"交易处理中");
            return map ;
        }else {
            LOG.info("交易失败=========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"交易失败");
            return map ;
        }

    }




    /**
     * httpPost  请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String sendHttpsPost(String url, String params) {
        DataOutputStream out = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
        URL u = null;
        HttpsURLConnection con = null;
        //尝试发送请求
        try {
            System.out.println(params);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()},
                    new java.security.SecureRandom());
            u = new URL(url);
            //打开和URL之间的连接
            con = (HttpsURLConnection) u.openConnection();
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
            while ((line = in.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            System.out.println(result);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException ex) {
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
            return new X509Certificate[]{};
        }

    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * MD5 加密
     *
     * @param s
     * @param entype
     * @return
     */
    public final static String md5(String s, String entype) {
        String result = "";
        char hexDigits[] =
                {'0', '1', '2', '3',
                        '4', '5', '6', '7',
                        '8', '9', 'a', 'b',
                        'c', 'd', 'e', 'f'};
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
