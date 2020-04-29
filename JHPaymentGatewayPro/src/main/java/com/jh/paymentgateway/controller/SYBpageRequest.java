package com.jh.paymentgateway.controller;


import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.*;
import com.jh.paymentgateway.util.syb.IpayConstants;
import com.jh.paymentgateway.util.syb.IpayUtil;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class SYBpageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(SYBpageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ip;



    //2.1接口公共参数
    public static Map<String, String> buildBasicMap(){
        TreeMap<String,String> params = new TreeMap<String,String>();
        params.put("orgid", IpayConstants.SYB_ORGID);// 平台分配的机构号
        params.put("appid", IpayConstants.SYB_ORGAPPID); //平台分配的机构APPID 生产环境:0000495
        params.put("version", "11");  //接口版本号 默认11   可空
        params.put("randomstr", "SYBGG"+System.currentTimeMillis()+""); //商户自行生成的随机字符串
//        params.put("reqip", "");   //请求IP  可空
//        params.put("reqtime", ""); //请求时间戳  可空
        return params;
    }
    public static void print(Map<String, String> map){
        System.out.println("返回数据如下:");
        if(map!=null){
            for(String key:map.keySet()){
                System.out.println(key+";"+map.get(key));
            }
        }
    }



    // 与还款对接
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/syb/register")
    public @ResponseBody
    Object getRegister(@RequestParam(value = "bankCard") String bankCard,
                       @RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "idCard") String idCard,
                       @RequestParam(value = "phone") String phone, @RequestParam(value = "dphone") String dphone,
                       @RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName1,
                       @RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "extraFee") String extraFee,
                       @RequestParam(value = "securityCode") String securityCode, @RequestParam(value = "rate") String rate,
                       @RequestParam(value = "expiredTime") String expired) throws Exception {

//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);


//        String creditCardBankName = prp.getCreditCardBankName();
//        String userName = prp.getUserName();
//        String phoneD = prp.getDebitPhone();
//        String rate = prp.getRate();
//        String extraFee = prp.getExtraFee();
//        String idCard = prp.getIdCard();
//        String debitCard = prp.getDebitCardNo();
//        String creditCardCardType = prp.getCreditCardCardType();
//        String exTime = prp.getExpiredTime();
//        String expiredTime = this.expiredTimeToMMYY(exTime);
//        String securityCode = prp.getSecurityCode();
//        String bankCard = prp.getBankCard();

        expired = this.expiredTimeToMMYY(expired);
        Map<String, Object> maps = new HashMap<String, Object>();
        SYBRegister syb = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);

        SYBBindCard sybBindCard = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);

        if (syb == null) {
            LOG.info("===================用户没进件，开始进件===========================");
            maps = (Map<String, Object>) this.sendinformation(userName, dphone, idCard, dbankCard, rate, extraFee);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }

        if (!syb.getRate().equals(rate) || !syb.getExtraFee().equals(extraFee)) {
            LOG.info("=====================================费率不匹配，开始修改费率=======================================");
            maps = (Map<String, Object>) this.changerate(rate, extraFee,idCard);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }

        if (sybBindCard == null || sybBindCard.getStatus().equals("0")) {
                LOG.info("===================用户未绑卡，开始绑卡===========================");
                LOG.info("==========================================传过去的信用卡名字"+bankName1);
                maps = ResultWrap.init("999996", "需要绑卡",
                    ip + "/v1.0/paymentgateway/quick/syb/bind-view?ipAddress="+ip+ "&bankCard=" + bankCard
                                + "&bankName=" + URLEncoder.encode(bankName1, "UTF-8")
                                + "&securityCode=" + securityCode
                                + "&expiredTime="+ expired+ "&idCard="+ idCard+ "&userName="+ userName+ "&phone="+ phone);
                return maps;


            }
//        else {
//                LOG.info("===================已开通卡,跳转到省市商户选择页面===========================");
//                maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/syb/tocityhtml?orderCode=" + orderCode + "&ipAddress=" + ip);
//                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//                maps.put(CommonConstants.RESP_MESSAGE, "跳转到省市区选择页面");
//            }

//            return maps;
        return ResultWrap.init(CommonConstants.SUCCESS, "验证成功");
        }


        //商户信息提交（进件）
        public Object sendinformation (String userName, String phoneD, String idCard, String debitCard, String
        rate, String extraFee) throws Exception {
            Map<String, String> params = buildBasicMap();   //公共参数
            String rate0 = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
            params.put("belongorgid", "201003992003");//拓展代理商号 生产环境机构号201003992003
            params.put("outcusid", "SYB" + System.currentTimeMillis());   //商户外部唯一标记，商户代理商平台或外部其他系统的唯一标记码
            params.put("cusname", "莘丽有限公司");  //商户名称
            params.put("cusshortname", "莘丽"); //商户简称
            params.put("merprovice", "310000"); //所在省  以国标excel表省市码为准
            params.put("areacode", "310100"); //所在市  以国标excel表省市码为准
            params.put("legal", userName); //法人姓名
            params.put("idno", idCard);   //法人代表证件号
            params.put("phone", phoneD);   //法人手机号码
            params.put("address", "上海市宝山区");  //注册地址
            params.put("acctid", debitCard);    //账户号  银行卡
            params.put("acctname", userName); //账户名
            params.put("accttp", "00");  //卡折类型   00-借记卡  01-存折
            params.put("expanduser", "");  //拓展人  可空
            params.put("prodlist", "[{'trxcode':'QUICKPAY_NOSMS','feerate':'" + rate0 + "'}，{'trxcode':'TRX_PAY','feerate':'"+extraFee+"'}]"); //产品列表的json   在文档3.1 产品1是落地小额快捷
            params.put("settfee", extraFee); //手续费
            String retcode = null;
            String retmsg = null;
            try {
                Map<String, String> map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/org/addcus", params);
                retcode = map.get("retcode");
                retmsg = map.get("retmsg");
                SYBRegister sybRegister = new SYBRegister();
                if ("SUCCESS".equals(retcode)) {
                    LOG.info("收银宝支付==============================入网成功");
                    String cusid = map.get("cusid");
                    String outcusid = map.get("outcusid");
                    sybRegister.setIdCard(idCard);
                    sybRegister.setBankCard(debitCard);
                    sybRegister.setCusId(cusid);
                    sybRegister.setOutcusId(outcusid);
                    sybRegister.setPhone(phoneD);
                    sybRegister.setRate(rate);
                    sybRegister.setExtraFee(extraFee);
                    sybRegister.setUserName(userName);
                    topupPayChannelBusiness.createSYBRegister(sybRegister);
                    return ResultWrap.init(CommonConstants.SUCCESS, retmsg);
                } else {
                    LOG.info("收银宝支付==============================入网失败");
                    return ResultWrap.init(CommonConstants.FALIED, retmsg);
                }

            } catch (Exception e) {
                e.printStackTrace();
                LOG.info("收银宝支付==============================入网异常");
                return ResultWrap.init(CommonConstants.FALIED, retmsg);
            }
//{"appid":"0000495","cusid":"101005001547","outcusid":"SYB1561604885020","retcode":"SUCCESS","retmsg":"处理成功","sign":"B619F004D6E1D5FD80727061ADB74045"}
        }

    /**
     * 修改费率
     *
     * @param rate
     * @param extraFee
     * @param idCard
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/syb/changerate")
        public  Object changerate(@RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee")String extraFee,@RequestParam(value = "idCard") String idCard) throws Exception{
            Map<String, String> params = buildBasicMap();//公共参数
//            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//            String idCard = prp.getIdCard();
            SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String bankCard = sybRegister.getBankCard();
        String cusId = sybRegister.getCusId();
//            String debitCardNo = prp.getDebitCardNo();
            String settfee = extraFee.substring(0, extraFee.indexOf("."));
            String rate0 = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
            Map<String, Object> maps = new HashMap<>();
            params.put("cusid", cusId);  //商户号
            params.put("acctid", bankCard); //账户号
            params.put("accttp", "00");  //卡折类型
            params.put("prodlist", "[{'trxcode':'QUICKPAY_NOSMS','feerate':'"+rate0+"'},{'trxcode':'TRX_PAY','feerate':'"+settfee+"'}]");//支付产品信息列表
            params.put("settfee", settfee);//实时到账手续费
            Map<String, String> map = null;
            LOG.info("params=============="+params);
            try {
                map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/org/updatesettinfo", params);
                System.out.println("map = " + map);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
                return ResultWrap.err(LOG, CommonConstants.FALIED, "syb修改费率请求异常,请稍后重试!");
            }
            String retcode = map.get("retcode");
            String retmsg = map.get("retmsg");
            if ("SUCCESS".equals(retcode)) {
                LOG.info("================================修改费率成功============================================");
                sybRegister.setRate(rate);
                sybRegister.setExtraFee(extraFee);
                topupPayChannelBusiness.createSYBRegister(sybRegister);
                maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                maps.put(CommonConstants.RESP_MESSAGE, retmsg);
            } else {
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, retmsg);
                LOG.info("syb修改费率-------异常:" + retmsg);
            }

            return maps;

        }
//{"appid":"0000495","retcode":"SUCCESS","retmsg":"处理成功","sign":"6092D12F6B4829F170978C23CB03385D"}




    //绑卡转接页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/syb/bind-view")
    public String returnSYBbindcard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

        LOG.info("================================绑卡转接页面============================================");
        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String ipAddress = request.getParameter("ipAddress");
        String bankCard = request.getParameter("bankCard");
        String bankName = request.getParameter("bankName");
        String securityCode = request.getParameter("securityCode");
        String expiredTime = request.getParameter("expiredTime");
        String idCard = request.getParameter("idCard");
        String userName = request.getParameter("userName");
        String phone = request.getParameter("phone");

        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("bankName", bankName);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("idCard", idCard);
        model.addAttribute("userName", userName);
        model.addAttribute("phone", phone);


        return "sybbindcard";
    }


    //收银宝绑卡发送短信
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/syb/open-sms")
    public @ResponseBody Object OpenSMS(@RequestParam(value = "expiredTime") String expiredTime,
                                        @RequestParam(value = "securityCode") String securityCode,
                                        @RequestParam(value = "bankCard") String bankCard,
                                        @RequestParam(value = "idCard") String idCard,
                                        @RequestParam(value = "userName") String userName,
                                        @RequestParam(value = "phone") String phone) throws Exception {
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String bankCard = prp.getBankCard();
//        String idCard = prp.getIdCard();
//        String userName = prp.getUserName();
//        String phone = prp.getCreditCardPhone();
        LOG.info("=================绑卡发送短信expiredTime===" + expiredTime+"securityCode===" + securityCode);
        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String cusId = sybRegister.getCusId();

        Map<String, String> params = buildBasicMap();//公共参数
        params.put("cusid", cusId);  //cusid
        String meruserid = "SYBBK" + System.currentTimeMillis();
        params.put("meruserid", meruserid);  //meruserid 自己定义
        params.put("cardno", bankCard);  //银行卡
        params.put("acctname", userName); //账户名
        params.put("accttype", "02");  //卡类型   02 信用卡
        params.put("validdate", expiredTime);  //有效期     mmyy
        params.put("cvv2", securityCode);  //安全码
        params.put("idno", idCard);  //身份证号
        params.put("tel", phone);  //预留手机号码
        Map<String, String> map = null;
        String trxstatus = null;
        String retmsg = null;
        Map<String, Object> maps = new HashMap<String, Object>();
        SYBBindCard sybbk = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);
        try {
            map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/org/bindcard", params);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            return ResultWrap.err(LOG, CommonConstants.FALIED, "syb绑卡请求异常,请稍后重试!");
        }
        retmsg = map.get("retmsg");
        trxstatus = map.get("trxstatus");

        if ("1999".equals(trxstatus)) {
            if (sybbk == null) {
                SYBBindCard sybBindCard = new SYBBindCard();
                sybBindCard.setBankCard(bankCard);
                sybBindCard.setIdCard(idCard);
                sybBindCard.setMeruserId(meruserid);
                sybBindCard.setPhone(phone);
                sybBindCard.setStatus("0");
                sybBindCard.setUserName(userName);
                sybBindCard.setCvv(securityCode);
                sybBindCard.setValiddate(expiredTime);
                topupPayChannelBusiness.createSYBBindCard(sybBindCard);
            } else {
                sybbk.setMeruserId(meruserid);
                topupPayChannelBusiness.createSYBBindCard(sybbk);
            }
            LOG.info("========================绑卡发送验证码成功=============================");
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
        }
        return maps;
    }

//meruserid=  SYBBK1561606343424      这个要存，确认绑卡接口得用   每次都是不同
        //{"appid":"0000495","retcode":"SUCCESS","retmsg":"处理成功","sign":"F501C796689178A01FD707678C8BEE40","trxstatus":"1999"}



    /**
     * 收银宝确认绑卡
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/syb/open-card")
    public @ResponseBody Object OpenPaymentCard(@RequestParam(value = "smsCode") String smsCode,
                                                @RequestParam(value = "expiredTime") String expiredTime,
                                                @RequestParam(value = "securityCode") String securityCode,
                                                @RequestParam(value = "bankCard") String bankCard,
                                                @RequestParam(value = "idCard") String idCard,
                                                @RequestParam(value = "phone") String phone,
                                                @RequestParam(value = "userName") String userName) throws Exception {

//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String bankCard = prp.getBankCard();
//        String idCard = prp.getIdCard();
//        String phone = prp.getCreditCardPhone();
//        String userName = prp.getUserName();
        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        SYBBindCard sybBindCard = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);
        String cusId = sybRegister.getCusId();
        String meruserId = sybBindCard.getMeruserId();
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, String> params = buildBasicMap(); //公共参数
        params.put("cusid", cusId);  //商户号
        params.put("meruserid", meruserId); //商户用户号
        params.put("cardno", bankCard); //银行卡
        params.put("acctname", userName);  //账户名
        params.put("accttype", "02"); //卡类型
        params.put("validdate", expiredTime); //有效期
        params.put("cvv2", securityCode);  //安全码
        params.put("idno", idCard); //身份证号
        params.put("tel", phone); //预留手机号码
        params.put("smscode", smsCode); //短信验证码
        params.put("thpinfo", "绑卡确认"); //交易透传信息 则原样带上,同2.5
        Map<String, String> map = null;
        try {
            map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/org/bindcardconfirm", params);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            return ResultWrap.err(LOG, CommonConstants.FALIED, "syb请求异常,请稍后重试!");
        }


        String trxstatus = map.get("trxstatus");
        String retmsg = map.get("retmsg");
        String agreeid = map.get("agreeid");
        if ("0000".equals(trxstatus)) {
            LOG.info("================================收银宝确认绑卡成功============================================");
            sybBindCard.setAgreeId(agreeid);
            sybBindCard.setStatus("1");
            topupPayChannelBusiness.createSYBBindCard(sybBindCard);
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
            maps.put("redirect_url",ip + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
            LOG.info("收银宝开通支付卡---异常：" + retmsg);
        }
        return maps;
    }
//meruserid=SYBBK1561605960130
//{"agreeid":"201906271133032797","appid":"0000495","retcode":"SUCCESS","retmsg":"处理成功","sign":"E75C25908845E7360FA1F375DA78FE08","trxstatus":"0000"}


//    /**
//     * 跳转到省市商户选择页面
//     * @param request
//     * @param response
//     * @param model
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/syb/tocityhtml")
//    public String tosybchoosecity(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//
//        LOG.info("================================跳转到省市商户选择页面============================================");
//        // 设置编码
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//
//        String ordercode = request.getParameter("orderCode");
//        String ipAddress = request.getParameter("ipAddress");
//
//        model.addAttribute("orderCode", ordercode);
//        model.addAttribute("ipAddress", ipAddress);
//
//        return "syblinkage";
//    }
//
//
//    /**
//     * 三级联动跳转到交易页面的中转页面
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/syb/topay")
//    public @ResponseBody Object tosybpay(@RequestParam(value = "orderCode") String orderCode,
//                                         @RequestParam(value = "ipAddress") String ipAddress,
//                                         @RequestParam(value = "city") String city,
//                                         @RequestParam(value = "mccid") String mccid)
//            throws IOException {
//        LOG.info("================================三级联动跳转到交易页面的中转页面============================================");
////        String ordercode = request.getParameter("ordercode");
////        String ipAddress = request.getParameter("ipAddress");
////        String city = request.getParameter("city");
////        String mccid = request.getParameter("mccid");
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String bankName = prp.getCreditCardBankName();
//        String bankCard = prp.getBankCard();
//        String amount = prp.getAmount();
//
//        Map<String, Object> maps = new HashMap<>();
//
//        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        maps.put(CommonConstants.RESP_MESSAGE, "直接跳转交易页面");
//        maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/syb/pay?bankName=" + URLEncoder.encode(bankName, "UTF-8")
//                + "&bankCard=" + bankCard
//                + "&orderCode=" + orderCode
//                + "&ipAddress=" + ipAddress                 //看下这里有没有问题
//                + "&city=" + city
//                + "&mccid=" + mccid
//                + "&amount=" + amount);
//        return maps;
//    }
//
//    /**
//     * 跳转到交易页面
//     * @param request
//     * @param response
//     * @param model
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/syb/pay")
//    public String topay(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//
//        LOG.info("================================跳转到交易页面============================================");
//        // 设置编码
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//
//        String orderCode = request.getParameter("orderCode");
//        String ipAddress = request.getParameter("ipAddress");
//        String city = request.getParameter("city");
//        String mccid = request.getParameter("mccid");
//        String amount = request.getParameter("amount");
//        String bankCard = request.getParameter("bankCard");
//        String bankName = request.getParameter("bankName");
//
//        model.addAttribute("ordercode", orderCode);
//        model.addAttribute("ipAddress", ipAddress);
//        model.addAttribute("city", city);
//        model.addAttribute("mccid", mccid);
//        model.addAttribute("amount", amount);
//        model.addAttribute("bankCard", bankCard);
//        model.addAttribute("bankName", bankName);
//
//        return "sybpay";
//    }

    /**
     * 收银宝代还 支付
     * @param orderCode
     * @param city
     * @param mccid
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/quick/syb/payone")
public @ResponseBody Object payone(@RequestParam(value = "orderCode") String orderCode,@RequestParam(value = "mccid") String mccid, @RequestParam(value = "city") String city){
        LOG.info("==========================================收银宝代还支付========================================");
        LOG.info("============订单号为" +orderCode + "city" + city + "mccid" +mccid);
    PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
    String idCard = prp.getIdCard();
    String bankCard = prp.getBankCard();
    String amount = prp.getAmount();
    LOG.info("==========================amount=="+amount+"===============================");
    String rip = prp.getIpAddress();
    String amount2 = new BigDecimal(amount).multiply(new BigDecimal("100")).toString();
        String settleAmount = amount2.substring(0, amount2.indexOf("."));
        LOG.info("==========================settleAmount=="+settleAmount+"===============================");
    SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
    SYBBindCard sybBindCard = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);
    String cusId = sybRegister.getCusId();
    String agreeId = sybBindCard.getAgreeId();
    Map<String, Object> maps = new HashMap<>();
    Map<String, String> params = buildBasicMap();  //公共参数
    params.put("cusid", cusId);  //商户号
    params.put("orderid", orderCode);  //商户订单号
    params.put("agreeid", agreeId);//协议编号	签约返回
//    params.put("trxcode", "QUICKPAY_OF_HP"); // 交易类型  文档3.1的产品类型
    params.put("amount", settleAmount); //订单金额
    params.put("fee", ""); //手续费 为空,则通过商户入网设置的产品费率进行计算不为空,不允许低于该商户所属代理商的手续费，建议为空
    params.put("currency", "CNY"); //币种  暂只支持CNY
    params.put("subject", "快捷支付交易申请"); //订单内容，订单的展示标题
    params.put("validtime", "240"); //订单有效时间  最大720分钟
    params.put("city", city); //市别 以国标excel表省市码为准
    params.put("mccid", mccid); //行业   详见附录3.2
    params.put("trxreserve", "支付"); //交易备注  用于用户订单个性化信息交易完成通知会带上本字段
    params.put("notifyurl", ip +"/v1.0/paymentgateway/topup/syb/sybpayback");//  回调地址，通知url必须为直接可访问的url，不能携带参数。 //
    Map<String, String> map = null;
    try {
        map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/qpay/quickpass", params);
    } catch (Exception e) {
        e.printStackTrace();
        LOG.error("", e);
        return ResultWrap.err(LOG, CommonConstants.FALIED, "syb支付请求异常,请稍后重试!");
    }
    String trxstatus = map.get("trxstatus");
    String retmsg = map.get("retmsg");
    if ("0000".equals(trxstatus)) {
        LOG.info("=============================syb代付支付成功=====================================");
//        String trxid = map.get("trxid");
//        String thpinfo = map.get("thpinfo");
//        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        maps.put(CommonConstants.RESP_MESSAGE, retmsg);
//        maps.put("trxid", trxid);
//        maps.put("thpinfo", thpinfo);
        String trxid = map.get("trxid");
        this.updatePaymentOrderThirdOrder(prp.getIpAddress(), orderCode, trxid);
        return ResultWrap.init("999998", "支付处理中");
    }
        return ResultWrap.err(LOG, CommonConstants.FALIED, retmsg);
//    10块{"appid":"0000495","errmsg":"请输入短信验证码","orderid":"SYBDD1561692767462","retcode":"SUCCESS","retmsg":"处理成功","sign":"0302CC6B98511E4549B5FF89D8426069","thpinfo":"{\"sign\":\"\",\"tphtrxcrtime\":\"\",\"tphtrxid\":0,\"trxflag\":\"trx\",\"trxsn\":\"\"}","trxid":"19060016102699","trxstatus":"1999"}
}




    /**
     * 消费回调
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/syb/sybpayback")
    public @ResponseBody Object sybpayback(HttpServletRequest request, HttpServletResponse response) {

        String trxstatus = request.getParameter("trxstatus");//
        LOG.info("trxstatus==========================================="+trxstatus );
        LOG.info("=====================================收银宝消费回调=================================================");
        String cusorderid = null;
        if ("000000".equals(trxstatus)) {
            cusorderid = request.getParameter("cusorderid");
            LOG.info("cusorderid==========================================="+cusorderid );
            PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(cusorderid);
            this.updateSuccessPaymentOrder(bean.getIpAddress(), cusorderid);
            try {
                response.getWriter().println("success");
            } catch (IOException e) {
                e.printStackTrace();
                return "success";
            }
            return null;
        }
        return ResultWrap.err(LOG, CommonConstants.FALIED, cusorderid + "非成功回调",cusorderid);

//消费回调信息  linkId===YCXF1561016385342orderNo===16119062015443631001orderStatus===20
// orderTime===20190620154437orderMemo===消费成功sign===5bd5190d0275ecea7575d7130b6e2789
    }





//    /**
//     * 收银宝快捷支付确认
//     * @param orderCode
//     * @param smsCode
//     * @param trxid
//     * @param thpinfo
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/quick/syb/paytwo")
//    public @ResponseBody
//    Object paytwo(@RequestParam(value = "orderCode") String orderCode,
//                  @RequestParam(value = "smsCode") String smsCode,
//                  @RequestParam(value = "trxid") String trxid,
//                  @RequestParam(value = "thpinfo")String thpinfo) throws Exception {
//        LOG.info("==========================================输入验证码，支付确认中========================================");
//        Map<String, Object> maps = new HashMap<>();
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String idCard = prp.getIdCard();
//        String bankCard = prp.getBankCard();
//        String rip = prp.getIpAddress();
//        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
//        SYBBindCard sybBindCard = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);
//        String agreeId = sybBindCard.getAgreeId();
//        String cusId = sybRegister.getCusId();
//        Map<String, String> params = buildBasicMap();
//        params.put("cusid", cusId);  //商户号
//        params.put("trxid", trxid); //交易单号 平台的交易流水号
//        params.put("agreeid", agreeId);// 协议编号
//        params.put("smscode", smsCode); //短信验证码
//        params.put("thpinfo", thpinfo); //交易透传信息支付申请或者错误码为1999返回的thpinfo原样带上
//        Map<String, String> map = null;
//        try {
//            map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/qpay/confirmpay", params);
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOG.error("", e);
//            return ResultWrap.err(LOG, CommonConstants.FALIED, "syb支付确认请求异常,请稍后重试!");
//        }
//        String trxstatus = map.get("trxstatus");
//        String retmsg = map.get("retmsg");
//        if ("0000".equals(trxstatus)) {
//            LOG.info("================================快捷支付成功============================================");
//            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
//            maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/topaysuccess?orderCode=" + orderCode
//                    + "&bankName=" + URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&bankCard="
//                    + prp.getBankCard() + "&amount=" + prp.getAmount() + "&realAmount=" + prp.getRealAmount());
//            this.cash(orderCode);
//        } else {
//            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            maps.put(CommonConstants.RESP_MESSAGE, retmsg);
//            LOG.info("syb快捷支付---异常：" + retmsg);
//            this.addOrderCauseOfFailure(orderCode, retmsg, rip);
//        }
//        return maps;
//    }
////10块{"appid":"0000495","fintime":"20190628113354","retcode":"SUCCESS","retmsg":"处理成功","sign":"6DE1FC59F03D98B7F8A81082655753C6","trxid":"19060016102699","trxstatus":"0000"}

    /**
     * 收银宝付款接口
     * @param orderCode
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/quick/syb/cash")
    public@ResponseBody
    Object cash(@RequestParam(value = "orderCode")String orderCode) {
        Map<String, String> params = buildBasicMap();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String bankCard = prp.getBankCard();
        String idCard = prp.getIdCard();
        String extraFee = prp.getExtraFee();
        SYBBindCard sybBindCard = topupPayChannelBusiness.findSYBBindCardbybankcard(bankCard);
        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String agreeId = sybBindCard.getAgreeId();
        String cusId = sybRegister.getCusId();
        String Amount = new BigDecimal(prp.getRealAmount()).setScale(2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(extraFee))
                .multiply(new BigDecimal("100")).toString();
        String settleAmount = Amount.substring(0, Amount.indexOf("."));
        params.put("cusid", cusId);   //  商户号
        params.put("orderid", orderCode);  //商户提现流水 商户平台唯一,同一单号不允许重复提交
        params.put("amount", settleAmount);  // 提现金额  单位分
        params.put("isall", ""); //全额提现  如果设置了全额提取,则amount无效 1-代表全额提取
        params.put("fee", ""); //为空,则读取商户入网设置的产品手续费不为空,不允许低于该商户所属代理商的手续费 建议为空 单位分
        params.put("agreeid", agreeId);
        params.put("trxreserve", "付款"); //用于用户订单个性化信息交易完成通知会带上本字段
        params.put("notifyurl", ip + "/v1.0/paymentgateway/topup/syb/sybhkpayback"); //收交易结果通知回调地址，通知url必须为直接可访问的url，不能携带参数
        Map<String, String> map = null;
        try {
            map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/acct/pay", params);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("======================================syb付款异常============================================");
        }
        String trxstatus = map.get("trxstatus");
        String errmsg = map.get("errmsg");

        if ("0000".equals(trxstatus)) {
            LOG.info("=============================syb代付付款成功=====================================");

            String trxid = map.get("trxid");
            this.updatePaymentOrderThirdOrder(prp.getIpAddress(), orderCode, trxid);
            return ResultWrap.init("999998", "支付处理中");
        }
        return ResultWrap.err(LOG, CommonConstants.FALIED, errmsg);
        }


//10块{"acctno":"621226****1265","actualamount":"947","amount":"997","appid":"0000495","errmsg":"处理成功","fee":"50","fintime":"20190628113447","orderid":"SYBTX1234567","retcode":"SUCCESS","retmsg":"处理成功","sign":"A3D637C2F6F674AA8A98251D7F0CD868","trxid":"19060016105265","trxstatus":"0000"}

    /**
     * 代付回调接口
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/syb/sybhkpayback")
    public @ResponseBody Object sybreturnback(HttpServletRequest request, HttpServletResponse response) {

        String trxstatus = request.getParameter("trxstatus");//
        LOG.info("trxstatus==========================================="+trxstatus );
        LOG.info("=====================================收银宝还款回调=================================================");
        String cusorderid = null;
        if ("0000".equals(trxstatus)) {
            cusorderid = request.getParameter("cusorderid");
            LOG.info("cusorderid==========================================="+cusorderid );
            PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(cusorderid);
            this.updateSuccessPaymentOrder(bean.getIpAddress(), cusorderid);
            this.notifyCardManager(bean.getIpAddress(), cusorderid);
            try {
                response.getWriter().println("success");
            } catch (IOException e) {
                e.printStackTrace();
                return "success";
            }
            return null;
        }
        return ResultWrap.err(LOG, CommonConstants.FALIED, cusorderid + "非成功回调",cusorderid);
    }

    private void notifyCardManager(String ipAddress,String orderCode) {
        String url = ipAddress+"/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
        MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
        multiValueMap.add("orderCode", orderCode);
        multiValueMap.add("version", "29");
        try {
            String result = new RestTemplate().postForObject(url, multiValueMap, String.class);
            LOG.info(result);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * 收银宝查询余额
     * @param idCard
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/quick/syb/querybalance")
    public @ResponseBody Object querybalance(@RequestParam(value = "idCard") String idCard) throws Exception{
    SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
    String cusId = sybRegister.getCusId();
    Map<String, String> params = buildBasicMap();
    params.put("cusid", cusId);
    Map<String, String> map = null;
    try {
        map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/acct/balance", params);
    } catch (Exception e) {
        e.printStackTrace();
        LOG.error("", e);
        return ResultWrap.err(LOG, CommonConstants.FALIED, "syb查询余额请求异常,请稍后重试!");
    }
    String retcode = map.get("retcode");
    String retmsg = map.get("retmsg");

    if ("SUCCESS".equals(retcode)) {
        String balance = map.get("balance");
        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",new BigDecimal(balance).divide(BigDecimal.valueOf(100),2,BigDecimal.ROUND_DOWN));
    }
    return ResultWrap.err(LOG, CommonConstants.FALIED, retmsg);
}

//{"appid":"0000495","balance":"0","retcode":"SUCCESS","retmsg":"处理成功","sign":"9C218BB37839302697078E77B63F737A"}



//
//@RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/syb/test")
//public @ResponseBody void test() {
//    SYBRegister sybRegister = new SYBRegister();
//    sybRegister.setExtraFee("1");
//    sybRegister.setBankCard("123465798");
//    sybRegister.setIdCard("2313");
//    topupPayChannelBusiness.createSYBRegister(sybRegister);
//    SYBRegister sybRegister1 = topupPayChannelBusiness.findSYBRegisterbyIdcard("2313");
//    System.out.println("sybRegister1 = " + sybRegister1);
//    SYBBindCard sybBindCard = new SYBBindCard();
//    sybBindCard.setBankCard("12346565465465465");
//    topupPayChannelBusiness.createSYBBindCard(sybBindCard);
//    SYBBindCard sybBindCardbybankcard = topupPayChannelBusiness.findSYBBindCardbybankcard("12346565465465465");
//    System.out.println("sybBindCardbybankcard = " + sybBindCardbybankcard);
//}


    /**
     * 支付订单查询接口
     * @param orderCode
     * @return
     * <p>Description: </p>
     */
    @RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/syb/query/order/status")
    public @ResponseBody Object queryPayOrderStatus(String orderCode,String orderType) throws Exception {
        if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
            return this.getSYBOrderStatus(orderCode);
        }else if(CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
            return this.getRepaymentPayOrderStatus(orderCode);
        }else {
            return ResultWrap.err(LOG, CommonConstants.FALIED, "订单类型错误");
        }
    }




    /**
     * 快捷订单查询
     */
    private Map<String, Object> getSYBOrderStatus(String orderCode) throws Exception {
        LOG.info("=================================还款支付订单查询接口进来了==========================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String cusId = sybRegister.getCusId();
        Map<String, String> params = buildBasicMap();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = dateFormat.format(date);


        params.put("cusid", cusId);
        params.put("orderid", orderCode);
        params.put("trxid", "");
        params.put("date", format);
        Map<String, String> map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/qpay/query", params);
        String trxstatus = map.get("trxstatus");
        String errmsg = map.get("errmsg");

        if ("0000".equals(trxstatus)) {
            return ResultWrap.init(CommonConstants.SUCCESS, errmsg);
        } else if ("2000".equals(trxstatus)) {
            return ResultWrap.init("999998", "支付处理中");
        }
        return ResultWrap.err(LOG, CommonConstants.FALIED, errmsg);
    }

//{"acct":"6225757564009232","appid":"0000495","errmsg":"交易成功","fintime":"20190702154024","orderid":"SYBDD1562053171902","retcode":"SUCCESS","retmsg":"处理成功","sign":"D412BB25FCB434545813EF4AE24A934E","trxamt":"1000","trxcode":"QUICKPAY_OF_HP","trxid":"19070001072244","trxstatus":"0000"}


    /**
     * 代付订单查询接口
     */
    private Map<String,Object> getRepaymentPayOrderStatus(String orderCode) throws Exception {
        LOG.info("=================================代付订单查询接口进来了==========================");

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        SYBRegister sybRegister = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String cusId = sybRegister.getCusId();
        Map<String, String> params = buildBasicMap();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = dateFormat.format(date);

        params.put("cusid", cusId);
        params.put("orderid", orderCode);
        params.put("trxid", "");
        params.put("date", format);
        Map<String, String> map = IpayUtil.dorequest("https://ipay.allinpay.com/apiweb/qpay/query", params);
        String trxstatus = map.get("trxstatus");
        String errmsg = map.get("errmsg");

        if ("0000".equals(trxstatus)) {
            return ResultWrap.init(CommonConstants.SUCCESS, errmsg);
        } else if ("2000".equals(trxstatus)) {
            return ResultWrap.init("999998", "支付处理中");
        }
        return ResultWrap.err(LOG, CommonConstants.FALIED, errmsg);
    }



    /**
         * 查询所有的省
         */
        @RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/syb/provice/queryall"))
        public @ResponseBody Object findprovice () {
            Map map = new HashMap();
            List<SYBAddress> list = topupPayChannelBusiness.findSYBAllprovice();
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, list);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }

        /**
         * 根据省份id查询该省份所有的市
         */
        @RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/syb/city/queryall"))
        public @ResponseBody Object findCity (@RequestParam(value = "provinceId") String provinceId){
            LOG.info("provinceid---------------------：" + provinceId);
            Map map = new HashMap();
            List<SYBAddress> list = topupPayChannelBusiness.findSYBarea(provinceId);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, list);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }


        /**
         * 查询所有的MCC
         */
        @RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/syb/mcc/queryall"))
        public @ResponseBody Object findSYBMCC () {
            Map map = new HashMap();
            List<SYBMCC> list = topupPayChannelBusiness.findallmcc();
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, list);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }
    }


