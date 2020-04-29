package com.jh.paymentgateway.controller.ap;


import cn.jh.common.tools.Log;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.apdh.APDHXRequestBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.apdh.APDHCityCode;
import com.jh.paymentgateway.pojo.apdh.APDHIps;
import com.jh.paymentgateway.pojo.apdh.APDHXBindCard;
import com.jh.paymentgateway.pojo.apdh.APDHXRegister;
import com.jh.paymentgateway.util.ap.*;
import com.jh.paymentgateway.util.ap.model.APBindCardBack;
import com.jh.paymentgateway.util.ap.model.BaseReqModel;
import com.jh.paymentgateway.util.ap.model.BaseRespModel;
import com.jh.paymentgateway.util.ap.security.AESUtil;
import com.jh.paymentgateway.util.ap.security.RsaUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@EnableAutoConfiguration
public class APDHXpageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(APDHXpageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private APDHXRequestBusiness apdhxRequestBusiness;

    @Value("${payment.ipAddress}")
    private String ip;

    @Value("${ap.keyStorePath}")
    private String keyStorePath;

    protected static String payUrl= "https://mp.anypayglobal.com/payment-pre/gateway/api/pay.json";
    protected static String mchtNo= "11909061520274";

    protected static String aesPwd= "yAejtq0K4j9vuLKd";
    protected static String rsaPwd= "6tB2xnnu";


    /**
     * 还款对接接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/Dockentrance")
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
        LOG.info("开始进行安派进件绑卡操作====================");
        Map<String,Object> maps = new HashMap<>();
        try{
            // APDHXRegister apdhxRegister = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
            APDHXBindCard apdhxBindCard = apdhxRequestBusiness.findAPDHXBindCardByBankdCard(bankCard);
            if(apdhxBindCard == null){

                APDHXBindCard apdhxBindCard1 = apdhxRequestBusiness.findAPDHXBindCardByBankdCard1(bankCard);
                //判断是否有绑卡请求过，有的话就调上游查询接口查询绑卡进度
                if(apdhxBindCard1 != null){
                    String bindSerialNo = apdhxBindCard1.getBindSerialNo();
                    Map<String,Object> o = (Map<String, Object>) apBindCardQuery(bindSerialNo);
                    String respCode = o.get("respCode").toString();
                    String respMessage = o.get("respMessage").toString();
                    if(respCode.equals("000000")){
                        JSONObject jsonObject = JSONObject.parseObject(respMessage);
                        if(jsonObject.getString("status").equals("1")){
                            apdhxBindCard1.setBindId(jsonObject.getString("bindId"));
                            apdhxRequestBusiness.saveAPDHXBindCard(apdhxBindCard1);
                            maps.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                            maps.put(CommonConstants.RESP_MESSAGE,"进件绑卡成功");
                            return maps;
                        }else if(jsonObject.getString("status").equals("0")){
                            if(isSameDay(new Date(), apdhxBindCard1.getCreateTime())){
                                maps.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                                maps.put(CommonConstants.RESP_MESSAGE,"绑卡处理中，请稍后在选择此通道。如果多次出现次提示请明天再试");
                                return maps;
                            }
                        }
                    }
                }
                maps.put(CommonConstants.RESP_CODE, "999996");
                maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
                maps.put(CommonConstants.RESULT,
                        ip + "/v1.0/paymentgateway/topup/apdhx/toBindcardPage?bankName="
                                + URLEncoder.encode(bankName, "UTF-8") + "&cardType="
                                + URLEncoder.encode("贷记卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
                                + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard=" + idCard
                                + "&userName=" + userName +  "&ipAddress=" + ip);
                return maps;
            }
            maps.put(CommonConstants.RESP_CODE, "000000");
            maps.put(CommonConstants.RESP_MESSAGE, "已经签约");
            return maps;
        }catch (Exception e){
            LOG.error("与还款对接接口出现异常======", e);
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "与还款对接失败");
            return maps;
        }
    }
    @RequestMapping(method = RequestMethod.GET, value = "v1.0/paymentgateway/topup/apdhx/toBindcardPage")
    public String returnCJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String expiredTime = request.getParameter("expiredTime");
        String securityCode = request.getParameter("securityCode");
        String bankName = request.getParameter("bankName");
        String cardType = request.getParameter("cardType");
        String bankCard = request.getParameter("bankCard");
        String ipAddress = request.getParameter("ipAddress");
        String userName = request.getParameter("userName");
        String phone = request.getParameter("phone");
        String idCard = request.getParameter("idCard");

        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("bankName", bankName);
        model.addAttribute("cardType", cardType);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("userName", userName);
        model.addAttribute("phone", phone);
        model.addAttribute("idCard", idCard);
        return "apdhxbindcard";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/apRegister")
    private Object apRegister(@RequestParam(value = "bankCard")String bankCard,
                              @RequestParam(value = "idCard")String idCard,
                              @RequestParam(value = "phone")String phone,
                              @RequestParam(value = "userName")String userName,
                              @RequestParam(value = "province")String province,
                              @RequestParam(value = "city")String city) {
        String resp = "";
        Map<String,Object> map = new HashMap<>();
        try{

            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("name", userName);//姓名
            data.put("cardId", idCard);//身份证号
            data.put("mobile", phone); //手机号
            data.put("province", province); //省code
            data.put("city", city);  //城市code
            data.put("merAddress", "**"); //详细地址
            data.put("certifTp", "01"); //证件类型：只支持身份的证
            data.put("accountId", bankCard); //结算卡号，贷记卡
            String unencrypted = JsonUtil.toJson(data);
            LOG.info("注册的请求参数："+unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));


            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("01");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);

            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return jsonObject;
            }
            String merchantCode = JSONObject.parseObject(respMessage).getString("userId");
            APDHXRegister save = apdhxRequestBusiness.save(bankCard, new Date(), idCard, merchantCode, phone, userName);
            LOG.info("安派注册成功================");
            map.put("respCode", "000000");
            map.put("respMessage", save);
            return map;
        } catch (Exception e){
            LOG.info("安派注册发生了异常，详情："+ e.getMessage());
            map.put("respCode", "999999");
            map.put("respMessage", e.getMessage());
            return map;
        }
    }
    /**
     * 协议申请接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/register")
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
            @RequestParam(value = "extraFee") String extraFee,
            @RequestParam(value = "province") String province,
            @RequestParam(value = "city") String city
    ) {
        Map<String, Object> map = new HashMap<>();
        APDHXRegister apdhxRegister = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
        if(apdhxRegister == null){
            Map<String,Object> o = (Map<String, Object>) apRegister(bankCard, idCard, phone, userName, province, city);
            if(!o.get("respCode").equals("000000")){
                LOG.error("安派注册失败，详情"+o.get("respMessage")+"================");
                return o;
            }
            apdhxRegister = (APDHXRegister) o.get("respMessage");
        }
        Map<String,Object> o = (Map<String, Object>) apBindCardSms(apdhxRegister.getMerchantCode(), bankCard, phone, apdhxRegister.getIdCard(), apdhxRegister.getUsername());
        if(!o.get("respCode").equals("000000")){
            LOG.error("安派发送绑卡短信失败，详情"+o.get("respMessage")+"================");
            if(o.get("respCode").equals("100014")&&o.get("respMessage").equals("银行卡已经绑定")) {
                APDHXBindCard apdhxBindCard  = apdhxRequestBusiness.findAPDHXBindCardByBankdCard1(bankCard);
                Map<String,Object> o1 = (Map<String, Object>) apBindCardQuery(apdhxBindCard.getBindSerialNo());
                if(o1.get("respCode").equals("000000")){
                    String message = o1.get("respMessage").toString();
                    if(JSONObject.parseObject(message).getString("status").equals("1")){
                        String bingId = JSONObject.parseObject(message).getString("bindId");
                        apdhxBindCard.setBindId(bingId);
                        apdhxRequestBusiness.saveAPDHXBindCard(apdhxBindCard);
                        o.put("respMessage","银行卡已经绑定,状态已更新");
                        return o;
                    }else{
                        LOG.info("银行卡已经绑定，但是查询结果为失败");
                        return o;
                    }
                }else{
                    LOG.info("银行卡已经绑定，但是查询结果为失败");
                    return o;
                }
            }
            return o;
        }
        map.put("respCode", "000000");
        map.put("respMessage", "发送短信成功");
        return map;
    }

    //安派绑卡确认接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/bindCardVerify")
    private Object bindCardVerify(@RequestParam(value = "phone") String phone,
                                     @RequestParam(value = "idCard") String idCard,
                                     @RequestParam(value = "rate") String rate,
                                     @RequestParam(value = "extraFee") String extraFee,
                                     @RequestParam(value = "authCode") String authCode,
                                  @RequestParam(value = "bankCard") String bankCard,
                                  @RequestParam(value = "securityCode") String securityCode,
                                  @RequestParam(value = "expiredTime") String expiredTime ) {
        LOG.info("开始安派绑卡确认====================");
        APDHXBindCard apdhxBindCard = apdhxRequestBusiness.findAPDHXBindCardByBankdCard1(bankCard);
        APDHXRegister apdhxRegister = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
        String bindSerialNo = apdhxBindCard.getBindSerialNo();
        String userName = apdhxBindCard.getUserName();
        String userId = apdhxRegister.getMerchantCode();

        String resp = "";
        Map<String, Object> map = new HashMap<>();
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("userId", userId);//注册时返回的用户id
            //String bindSerialNo = RandomUtil.generateNumber(20);
            data.put("bindSerialNo", bindSerialNo);//绑卡发送短信时候，生成的流水号
            data.put("smsCode", authCode);//短信验证码
            data.put("certifTp", "01");
            data.put("bankCardNo", bankCard);
            data.put("name", userName);
            data.put("mobile", phone);
            data.put("cardId", idCard);
            data.put("valid", expiredTimeToMMYY(expiredTime));//信用卡到期日
            data.put("cvn2", securityCode);//银行卡背面三位数

            String unencrypted = JsonUtil.toJson(data);

            LOG.info("绑卡确认的请求参数为："+ unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("03");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return jsonObject;
            }
            JSONObject jsonObject1 = JSONObject.parseObject(respMessage);
            if(jsonObject1.getString("status").equals("2")){
                map.put("respCode", "999999");
                map.put("respMessage", jsonObject1.getString("msg"));
                return map;
            }
            if(jsonObject1.getString("status").equals("1")){        //绑卡成功
                String bindId = jsonObject1.getString("bindId");
                apdhxBindCard.setBindId(bindId);
                apdhxRequestBusiness.saveAPDHXBindCard(apdhxBindCard);
            }
            LOG.info("安派确认绑卡成功================");
            map.put("respCode", "000000");
            map.put("respMessage", "绑卡成功");
            return map;
        } catch (Exception e){
            LOG.info("绑卡发生异常，详情："+ e.getMessage());
            map.put("respCode", "999999");
            map.put("respMessage", "绑卡发生异常，详情："+ e.getMessage());
            return map;
        }
    }

    //安派绑卡回调
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/bindCardBack")
    private Object bindCardBack(@RequestBody APBindCardBack apBindCardBack) throws Exception {
        LOG.info("安派绑卡回调来了====================");
        LOG.info("参数为："+apBindCardBack.toString());
        String data = apBindCardBack.getData();
        data = AESUtil.base64CbcDecrypt(data, aesPwd);
        JSONObject jsonObject = JSONObject.parseObject(data);

        return "ok";
    }

    //绑卡发短信接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/bindCard")
    public Object apBindCardSms( @RequestParam(value = "userId")String userId,
                                 @RequestParam(value = "bankCard")String bankCard,
                                 @RequestParam(value = "phone") String phone,
                                 @RequestParam(value = "idCard") String idCard,
                                 @RequestParam(value = "userName") String userName) {
        Map<String,Object> map = new HashMap<>();
        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            String bindSerialNo = RandomUtil.generateNumber(20);
            Map<String, Object> data = new TreeMap<>();
            data.put("userId", userId);//注册时返回的用户id
            data.put("bindSerialNo", bindSerialNo);//平台每次请求生成一个，小于32位，
            data.put("bankCardNo", bankCard);//银行卡号
            data.put("mobile", phone);//银行预留手机号
            data.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/apdhx/bindCardBack");//回调地址

            String unencrypted = JsonUtil.toJson(data);
            LOG.info("绑卡发短信请求参数："+ unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("02");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return jsonObject;
            }
            //如果已经绑过卡，直接将返回的bindId存入数据库
            JSONObject respJson = JSONObject.parseObject(respMessage);
            LOG.info("=================================通道返回的绑卡信息"+respJson.toString());
            if(respJson.getString("status").equals("1")){
                String bindId = respJson.getString("bindId");
                LOG.info("该银行卡已绑过卡，bindId为："+bindId);
                APDHXBindCard save = apdhxRequestBusiness.saveAPDHXBindCardContainsBindId(bankCard, new Date(), idCard, phone, userName,bindSerialNo, bindId );
                map.put("respCode", "666666");  //页面直接跳转到成功页面
                map.put("respMessage", save);
                return map;
            }

            APDHXBindCard save = apdhxRequestBusiness.saveAPDHXBindCard(bankCard, new Date(), idCard, phone, userName,bindSerialNo );
            LOG.info("安派发送绑卡短信成功================");
            map.put("respCode", "000000");
            map.put("respMessage", save);
            return map;
        } catch (Exception e){
            LOG.info("安派发送绑卡短信发生了异常，详情："+ e.getMessage());
            map.put("respCode", "999999");
            map.put("respMessage", e.getMessage());
            return map;
        }
    }

    //绑卡查询
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/bindCardquery")
    public Object apBindCardQuery( @RequestParam(value = "bindSerialNo")String bindSerialNo) {
        Map<String, Object> map = new HashMap<>();

        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("bindSerialNo", bindSerialNo);//绑卡时的流水号

            String unencrypted = JsonUtil.toJson(data);

            LOG.info("绑卡查询的请求参数为："+ unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("05");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return jsonObject;
            }
            LOG.info("安派绑卡查询成功================");
            map.put("respCode", "000000");
            map.put("respMessage", respMessage);
            return map;
        } catch (Exception e){
            LOG.info("安派绑卡查询发生了异常，详情："+ e.getMessage());
            map.put("respCode", "999999");
            map.put("respMessage", e.getMessage());
            return map;
        }
    }


    //安派获取城市列表
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/getcity")
    public Object apRegister(@RequestParam("code") String code ) {
        List<APDHCityCode> apdhCityCodes = new ArrayList<>();
        if(code.equals("0")){
            apdhCityCodes = apdhxRequestBusiness.findAPDHCityCode();
        }else{
            code = code.substring(0,2);
            apdhCityCodes = apdhxRequestBusiness.findAPDHCityCodeByCode(code);
        }
        return apdhCityCodes;
    }
    /**
     * 处理响应json数据
     * @param resp
     */
    public String getResp(String resp) {
        JSONObject jsonObject = new JSONObject();
        try {

            PublicKey publicKey = (PublicKey) RsaUtil.getPublicKeyFromFile(keyStorePath, rsaPwd);

            BaseRespModel model = JsonUtil.jsonToObject(resp, BaseRespModel.class);
            LOG.info("响应model实体对象: {}", JsonUtil.toJson(model));

            if(model.getResultCode() == 0 && StringUtils.isNotBlank(model.getData())) {
                String respdata = model.getData();
                String sign = model.getSign();
                String plaintext = AESUtil.base64CbcDecrypt(respdata, aesPwd);
                JSONObject plaintextJSON=JSONObject.parseObject(plaintext);
                LOG.info("解密data明文数据: {}", plaintext);

                String signAlgo = "SHA1WithRSA";

                Map<String, Object> params = JsonUtil.jsonToObject(plaintext, TreeMap.class);
                String str2Sign = params.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("&"));
                byte[] ckSignBytes = Base64.decodeBase64(sign.getBytes("UTF-8"));
                boolean checked = SignUtils.valifySign(publicKey, str2Sign.getBytes("UTF-8"), ckSignBytes, signAlgo);
                LOG.info("签名验证结果: {}", checked);
                jsonObject.put("respCode","000000");
                jsonObject.put("respMessage", plaintext);
                if("fail".equals(plaintextJSON.getString("status"))){//订单失败
                    jsonObject.put("respCode","000000");
                    jsonObject.put("respMessage", plaintext);
                    jsonObject.put("errorDesc", model.getErrorDesc());
                    return jsonObject.toJSONString();
                }
                return jsonObject.toJSONString();
            } else {
                LOG.info("响应data数据为空");
                jsonObject.put("respCode", model.getErrorCode());
                jsonObject.put("respMessage", model.getErrorDesc());
                return jsonObject.toJSONString();
            }
        } catch (Exception e){
            LOG.info("解析返回json时发生了异常，详情："+e.getMessage());
            jsonObject.put("respCode",  "999999");
            jsonObject.put("respMessage", "发生了异常，请联系管理员");
            return jsonObject.toJSONString();
        }
    }
    //消费接口
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/Precreate")
    public Object pay(@RequestParam(value = "orderCode") String orderCode) {
        Map<String, String> map = new HashMap<>();
        LOG.info("安派开始消费============================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        APDHXRegister apdhxRegisterByIdCard = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
        APDHXBindCard apdhxBindCardByBankdCard = apdhxRequestBusiness.findAPDHXBindCardByBankdCard(bankCard);
        if(apdhxRegisterByIdCard == null || apdhxBindCardByBankdCard == null){
            return ResultWrap.init(CommonConstants.FALIED, "注册信息或绑卡信息为空");
        }
        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("userId", apdhxRegisterByIdCard.getMerchantCode());
            data.put("bindId", apdhxBindCardByBankdCard.getBindId());
            BigDecimal amount = new BigDecimal(prp.getRealAmount()).multiply(new BigDecimal("100")).setScale(0);
            data.put("amount", amount.toString());
            BigDecimal fee = new BigDecimal(prp.getRealAmount()).multiply(new BigDecimal(prp.getRate()).multiply(new BigDecimal("100"))).setScale(0, BigDecimal.ROUND_UP);
            data.put("fee", fee.toString());
            data.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/apdhx/transfer/notifyurl");
            String extra = prp.getExtra();// 消费计划|福建省-泉州市-350500-440300
            LOG.info("extra====================="+extra);
            String[] split = extra.split("-");
            String province =split[0].substring(split[0].indexOf("|")+1).trim();
            String city =split[1].trim();
            List<APDHIps> apdhIpsList=apdhxRequestBusiness.findIpsByCity(city);
            if (apdhIpsList.size() ==0||apdhIpsList==null) {
                apdhIpsList=apdhxRequestBusiness.findIpsByProvince(province);
            }
            Random r = new Random();
            int n = r.nextInt(apdhIpsList.size());
            String ips=apdhIpsList.get(n).getStartIp1();
            int ipn=r.nextInt(255);
            String[] b=ips.split("\\.");
            String ip=b[0]+"."+b[1]+"."+b[2]+'.'+ipn;
            if(ip.length()>0){
                data.put("sourceIp",ip.trim());
                LOG.info("=============安派公网ip:"+ip.trim());
            }else{
                data.put("sourceIp", "113.110.226.94");
                LOG.info("=============安派公网ip:113.110.226.94");
            }
           // 公网IP

            data.put("spOrderId", prp.getOrderCode());
            data.put("province", split[2]);  // 省份编码
            data.put("city", split[3]);  // 城市编码

            String unencrypted = JsonUtil.toJson(data);
            LOG.info("消费的请求参数为："+ unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("06");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return ResultWrap.init(CommonConstants.FALIED, respMessage);
            }
            JSONObject respMessageJson = JSONObject.parseObject(respMessage);
            if(respMessageJson.getString("status").equals("fail")){
                return ResultWrap.init(CommonConstants.FALIED, "消费失败!");
            }else if(respMessageJson.getString("status").equals("success")){
                return ResultWrap.init("999998", "消费成功，等待银行扣款！");
            }else{
                return ResultWrap.init("999998", "请求成功，等待银行扣款！");
            }
        } catch (Exception e){
            return ResultWrap.init("999998", "消费过程发生异常，详情："+e.getMessage());
        }
    }

    // 交易查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/orderquery")
    public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("spOrderId", orderCode);

            String unencrypted = JsonUtil.toJson(data);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("08");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            String errorDesc = jsonObject.getString("errorDesc");
            if(!respCode.equals("000000")){
                return ResultWrap.init(CommonConstants.FALIED, respMessage);
            }
            JSONObject respMessageJson = JSONObject.parseObject(respMessage);
            if(respMessageJson.getString("status").equals("fail")){
                return ResultWrap.init(CommonConstants.FALIED, errorDesc);
            }else if(respMessageJson.getString("status").equals("success")){
                return ResultWrap.init(CommonConstants.SUCCESS, "消费成功!");
            }else{
                return ResultWrap.init(CommonConstants.WAIT_CHECK, "正在处理中！");
            }
        } catch (Exception e){
            return ResultWrap.init(CommonConstants.WAIT_CHECK, "查询消费发生异常，详情："+e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/transfer/notifyurl")
    private Object hxdhxBack(@RequestBody APBindCardBack apBindCardBack) throws Exception {
        LOG.info("安派消费/还款回调来了====================");
        LOG.info("参数为："+apBindCardBack.toString());

        String data = apBindCardBack.getData();
        data = AESUtil.base64CbcDecrypt(data, aesPwd);
        JSONObject jsonObject1 = JSONObject.parseObject(data);
        String orderId = jsonObject1.getString("spOrderId");
        LOG.info("安派订单号："+ orderId + "的回调状态为："+jsonObject1.getString("status"));
        if(!jsonObject1.getString("status").equals("success")){
            return "ok";
        }

        String version = "34";
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderId);
        RestTemplate restTemplate = new RestTemplate();
        String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("orderCode", orderId);
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
        requestEntity.add("order_code", orderId);
        requestEntity.add("third_code", "");
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        LOG.info("订单状态修改成功===================" + orderId + "====================" + result);
        return "ok";
    }

    // 安派提现接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/transfercreate")
    public @ResponseBody
    Object transferCreate(@RequestParam(value = "orderCode") String orderCode) {
        Map<String, Object> map = new HashMap<>();
        LOG.info("安派开始提现============================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        APDHXRegister apdhxRegisterByIdCard = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
        APDHXBindCard apdhxBindCardByBankdCard = apdhxRequestBusiness.findAPDHXBindCardByBankdCard(bankCard);
        if(apdhxRegisterByIdCard == null || apdhxBindCardByBankdCard == null){
            return ResultWrap.init(CommonConstants.FALIED, "注册信息或绑卡信息为空");
        }

        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("userId", apdhxRegisterByIdCard.getMerchantCode());
            data.put("bindId", apdhxBindCardByBankdCard.getBindId());
            BigDecimal amount = new BigDecimal(prp.getRealAmount()).multiply(new BigDecimal("100")).setScale(0);
            BigDecimal fee = new BigDecimal(prp.getExtraFee()).multiply(new BigDecimal("100")).setScale(0);

            data.put("amount", amount.toString());
            data.put("fee", fee.toString());
            data.put("spOrderId", orderCode);
            data.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/apdhx/transfer/notifyurl");//回调地址

            String unencrypted = JsonUtil.toJson(data);
            LOG.info("还款的请求参数为："+ unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("07");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return ResultWrap.init(CommonConstants.FALIED, respMessage);
            }
            JSONObject respMessageJson = JSONObject.parseObject(respMessage);
            if(respMessageJson.getString("status").equals("fail")){
                return ResultWrap.init(CommonConstants.FALIED, "消费失败!");
            }else if(respMessageJson.getString("status").equals("success")){
                return ResultWrap.init("999998", "还款成功，等待银行回款");
            }else{
                return ResultWrap.init("999998", "还款处理中");
            }
        } catch (Exception e){
            return ResultWrap.init("999998", "还款发生异常，详情："+e.getMessage());
        }
    }

    //提现查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/transferquery")
    public @ResponseBody Object backOpen(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        String resp = "";
        try{
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("spOrderId", orderCode);

            String unencrypted = JsonUtil.toJson(data);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("09");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            String errorDesc = jsonObject.getString("errorDesc");
            if(!respCode.equals("000000")){
                return ResultWrap.init(CommonConstants.FALIED, respMessage);
            }
            JSONObject respMessageJson = JSONObject.parseObject(respMessage);
            if(respMessageJson.getString("status").equals("fail")){
                return ResultWrap.init(CommonConstants.FALIED, errorDesc);
            }else if(respMessageJson.getString("status").equals("success")){
                return ResultWrap.init(CommonConstants.SUCCESS, "消费成功!");
            }else{
                return ResultWrap.init(CommonConstants.WAIT_CHECK, "正在处理中！");
            }
        } catch (Exception e){
            return ResultWrap.init(CommonConstants.WAIT_CHECK, "还款查询发生异常，详情："+e.getMessage());
        }
    }
    //安派余额查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/apdhx/balancequery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard,
                                             @RequestParam(value="bankCard")String bankCard) throws Exception {
        APDHXRegister apdhxRegisterByIdCard = apdhxRequestBusiness.findAPDHXRegisterByIdCard(idCard);
        APDHXBindCard apdhxBindCard=apdhxRequestBusiness.findAPDHXBindCardByBankdCard(bankCard);
        if(apdhxRegisterByIdCard == null || apdhxBindCard==null){
            return ResultWrap.init(CommonConstants.FALIED, "该用户的注册信息为空或未绑卡！");
        }
        String resp = "";
        try {
            PrivateKey privateKey = (PrivateKey) RsaUtil.getPrivateKeyFromFile(keyStorePath, rsaPwd);

            Map<String, Object> data = new TreeMap<>();
            data.put("userId", apdhxRegisterByIdCard.getMerchantCode());
            //2019.9.16新增绑卡id，可查询对应卡的通道余额信息
            data.put("bindId",apdhxBindCard.getBindId());

            String unencrypted = JsonUtil.toJson(data);
            LOG.info("余额查询请求参数为:" + unencrypted);
            String encryptedInfo = AESUtil.base64CbcEncrypt(unencrypted, aesPwd);

            String str2Sign = data.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signAlgo = "SHA1WithRSA";
            byte[] signBytes = SignUtils.sign(privateKey, str2Sign.getBytes("UTF-8"), signAlgo);
            String sign = new String(Base64.encodeBase64(signBytes), "UTF-8");

            BaseReqModel model = new BaseReqModel();
            model.setMchtNo(mchtNo);
            model.setData(encryptedInfo);
            model.setProductCode("1000");
            model.setRequestId(RandomUtil.generateNumber(32));
            model.setServiceCode("10");
            model.setReqTime(DateUtil.getCurrentDate(DateUtil.YYYYMMDDHHMMSS));
            model.setVersion("1.0.0");
            model.setSign(sign);

            LOG.info("请求model: {}", JsonUtil.toJson(model));
            resp = OkHttpUtil.httpClientJsonPostReturnAsString(payUrl, JsonUtil.toJson(model), 60);
            //LOG.info("响应resp json数据: {}", JsonUtil.toJson(resp));

            // 处理响应json数据
            JSONObject jsonObject = JSONObject.parseObject(getResp(resp));
            String respCode = jsonObject.getString("respCode");
            String respMessage = jsonObject.getString("respMessage");
            if(!respCode.equals("000000")){
                return ResultWrap.init(CommonConstants.FALIED, respMessage);
            }
            JSONObject respMessageJson = JSONObject.parseObject(respMessage);
            return ResultWrap.init(CommonConstants.SUCCESS, respMessageJson.getString("balance"));
        }catch (Exception e){
            return ResultWrap.init(CommonConstants.FALIED, "查询余额发生了异常，详情："+e.getMessage());
        }
    }


    //判断两个date是否是同一天
    public static boolean isSameDay(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameDay(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if(cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }




}
