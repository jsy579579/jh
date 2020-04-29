package com.jh.paymentgateway.controller.tldh.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.tldh.dao.TLRegisterBusiness;
import com.jh.paymentgateway.controller.tldh.pojo.TLAree;
import com.jh.paymentgateway.controller.tldh.pojo.TLBankcode;
import com.jh.paymentgateway.controller.tldh.pojo.TLBindCard;
import com.jh.paymentgateway.controller.tldh.pojo.TLRegister;
import com.jh.paymentgateway.controller.tldh.util.KKIpayUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import net.sf.json.JSONObject;
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;


@Controller
@EnableAutoConfiguration
public class TLpageRequest extends BaseChannel {
    private static final Logger LOG = LoggerFactory.getLogger(TLpageRequest.class);
    @Autowired
    private RedisUtil redisUtil;

    @Value("${payment.ipAddress}")
    private String ip;

    @Autowired
    private TLRegisterBusiness tlRegisterBusiness;

    //多多生活
    //static final String AGENT_NO="7013101000002";

    public static String AGENT_NO;

    @Value("${tl.agentNo}")
    public void setPrivateKey(String agentNo){
        AGENT_NO=agentNo;
    }

    static final String VERSION="2.0";
    static final String SIGN_TYPE="MD5";
    static final String PAY_TYPE="gyfc.unionpay.wap";


    // 与还款对接
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/tlt/Dockentrance")
    public @ResponseBody
    Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName,
                        @RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
                        @RequestParam(value = "securityCode") String securityCode,
                        @RequestParam(value = "expiredTime") String expiredTime) throws Exception {

        expiredTime=this.expiredTimeToMMYY(expiredTime);
        bankName= Util.queryBankNameByBranchName(bankName);
        Map<String, Object> maps = new HashMap<>();

        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        TLBindCard tlBindCard=tlRegisterBusiness.findTLBindCardByBankCard(bankCard);

        if (tlRegister == null) {
            maps = (Map<String, Object>) this.tlToRegister(bankCard, idCard, phone, userName, rate, extraFee,bankName);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }else if(!rate.equals(tlRegister.getRate())|| !extraFee.equals(tlRegister.getExtraFee())){
            maps = (Map<String, Object>) this.tlupRegister(bankCard, idCard, phone, userName, rate, extraFee,bankName);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }

        // 绑卡
        if (tlBindCard == null || tlBindCard.getStatus().equals("0")) {
            //查询通道绑卡结果
            Map<String,String> bindCardResultMap=queryBindCardResult(idCard,bankCard);
            String resp_code=bindCardResultMap.get(CommonConstants.RESP_CODE);
            if(CommonConstants.SUCCESS.equals(resp_code)){//已绑卡
                //更新绑卡信息
                confirmBindCard(bankCard,idCard,phone);
                return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
            }
            LOG.info("===================用户未绑卡，开始绑卡===========================");
            maps = ResultWrap.init("999996", "需要绑卡",
                    ip + "/v1.0/paymentgateway/repayment/tldh/bindcard?bankCard=" + bankCard
                            + "&bankName="+ URLEncoder.encode(bankName, "UTF-8")+ "&cardType="
                            + URLEncoder.encode("0", "UTF-8") + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip);
            return maps;
        }



        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/paymentgateway/topup/tlt/TLRegister")
    public @ResponseBody Object tlToRegister(@RequestParam(value = "bankCard") String bankCard,
                                             @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                                             @RequestParam(value = "userName") String userName, @RequestParam(value = "rate") String rate,
                                             @RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName){
        LOG.info("开始进件======================");
        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("1000")).setScale(1).toString();
        String extraFee1 = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
        Map<String,Object>map=new HashMap<>();
        Map<String, String> params=new HashMap<>();
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("merchant_name",userName);//【商户姓名】【M】如：张三
        params.put("idcard_no",idCard);//【身份证号】【M】
        params.put("phone",phone);//【手机号】【M】
        params.put("rate",rate1);//【商户费率】【M】商户所有通道公用费率 费率为千分比 5.0表示千分之5 费率计算   0.0050
        params.put("poundage",extraFee1);//【商户手续费】【M】商户所有通道公用手续费 手续费单位为分
        params.put("province","31");
        params.put("city","3101");
        params.put("district","310113");
        params.put("version","2.0");//【版本号】【M】目前版本“2.0”
        params.put("sign_type","MD5");//【加密类型】【M】MD5(目前仅支持md5)

        LOG.info("请求参数为:"+params.toString());
        Map<String,String>maps= null;

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/MerchantSettled",params);

        } catch (Exception e) {

            e.printStackTrace();
        }

            JSONObject fromObject = JSONObject.fromObject(maps);
            LOG.info("返回参数为 = " + fromObject);
            String returnCode = fromObject.getString("return_code");
            String returnMsg = fromObject.getString("return_msg");
            if ("00".equals(returnCode)) {
                String merchantNo= fromObject.getString("merchant_no");
                TLRegister tlregister=new TLRegister();
                tlregister.setUserName(userName);
                tlregister.setIdcard(idCard);
                tlregister.setBankCard(bankCard);
                tlregister.setPhone(phone);
                tlregister.setBankName(bankName);
                tlregister.setRate(rate);
                tlregister.setExtraFee(extraFee);
                tlregister.setMerchantCode(merchantNo);
                tlRegisterBusiness.createRegister(tlregister);
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
                return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }
    }

    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/tldh/bindcard")
    public String returnJFDHBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String bankName = request.getParameter("bankName");
        String cardType = request.getParameter("cardType");
        String bankCard = request.getParameter("bankCard");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String expiredTime = request.getParameter("expiredTime");
        String securityCode = request.getParameter("securityCode");
        String ipAddress = request.getParameter("ipAddress");
        String rate = request.getParameter("rate");
        String extraFee = request.getParameter("extraFee");
        String userName = request.getParameter("userName");

        model.addAttribute("bankName", bankName);
        model.addAttribute("cardType", cardType);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("idCard", idCard);
        model.addAttribute("phone", phone);
        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("rate", rate);
        model.addAttribute("extraFee", extraFee);
        model.addAttribute("userName", userName);

        return "tldhbindcard";
    }

    // 绑卡接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/bindCard")
    public @ResponseBody Object tlRegisterBusinessToBindCard(@RequestParam(value = "bankCard") String bankCard,
                               @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "userName") String userName,
                               @RequestParam(value = "securityCode") String securityCode,
                               @RequestParam(value = "expiredTime") String expiredTime,
                                               @RequestParam(value = "bankName") String bankName) throws UnsupportedEncodingException {
        LOG.info("进入绑卡接口=============");
        Map<String, String> params=new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        String merchantNo= tlRegister.getMerchantCode();
        TLBankcode tlBankcode=tlRegisterBusiness.findTLBankcodeByBankName(bankName);
        if (tlBankcode==null||"".equals(tlBankcode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "不支持该银行"); // 描述
            return map;
        }

        expiredTime=this.expiredTimeToMMYY(expiredTime);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("merchant_no",merchantNo);
        params.put("pay_type",PAY_TYPE);
        params.put("bankcard_no",bankCard);
        params.put("bank_name",bankName);
        params.put("bank_code",tlBankcode.getBin());
        params.put("mobile",phone);
        params.put("cvn2",securityCode);
        params.put("expiry",expiredTime);
        params.put("bill_date","1");
        params.put("repayment_date","27");
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
       LOG.info("商户绑卡请求参数为============:"+params.toString());
        Map<String,String>maps= null;

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/BankcardAdd",params);

            LOG.info("返回参数为 = " + maps.toString());
            JSONObject fromObject = JSONObject.fromObject(maps);

            String returnCode = fromObject.getString("return_code");
            String returnMsg = fromObject.getString("return_msg");

//         if("03".equals(returnCode)){//已绑卡
//             confirmBindCard(bankCard,idCard,phone);
//             map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//             map.put(CommonConstants.RESP_MESSAGE, "已绑卡"); // 描述
////             map.put(CommonConstants.RESULT,ip + "/v1.0/paymentgateway/repayment/tldh/returnBindcardMessage?bankCard=" + bankCard
////                     + "&idCard=" + idCard + "&phone=" + phone+ "&ipAddress=" + ip);
//             return map;
//         }
        if(!"00".equals(returnCode)&&!"03".equals(returnCode)){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("进入申请商户申请通道接口================================");
        params.clear();
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("merchant_no",merchantNo);
        params.put("pay_type",PAY_TYPE);
        params.put("bankcard_no",bankCard);
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)

        maps.clear();

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/PayChannelApply",params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject fromObject = JSONObject.fromObject(maps);
        LOG.info("申请商户申请通道接口返回参数为 = " + fromObject);
        String returnCode = fromObject.getString("return_code");
        String returnMsg = fromObject.getString("return_msg");
        if (!"00".equals(returnCode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }


        LOG.info("申请商户申请通道成功！！开始支付绑卡=================================");
        params.clear();
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("merchant_no",merchantNo);
        params.put("pay_type",PAY_TYPE);
        params.put("bankcard_no",bankCard);
        params.put("front_url",ip + "/v1.0/paymentgateway/repayment/tldh/returnBindcardMessage?bankCard=" + bankCard
                 + "&idCard=" + idCard + "&phone=" + phone+ "&ipAddress=" + ip);
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        LOG.info("支付请求绑卡参数为=============:"+params);
        maps.clear();

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/ApplyBindCard",params);

        } catch (Exception e) {
            e.printStackTrace();
        }

             fromObject = JSONObject.fromObject(maps);
            LOG.info("支付绑卡返回参数为 = " + fromObject);
             returnCode = fromObject.getString("return_code");
             returnMsg = fromObject.getString("return_msg");

        if ("00".equals(returnCode)) {
            try{
                String payData=fromObject.getString("pay_data");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
                map.put("payData",payData);
                return map;
            }catch (Exception e){
//                TLBindCard tlBindCard=tlRegisterBusiness.findTLBindCardByBankCard(bankCard);
//                if(tlBindCard==null){
//                    TLBindCard tl=new TLBindCard();
//                    tl.setUserName(userName);
//                    tl.setIdCard(idCard);
//                    tl.setPhone(phone);
//                    tl.setBankCard(bankCard);
//                    tl.setExpiredTime(expiredTime);
//                    tl.setSecurityCode(securityCode);
//                    tl.setStatus("1");
//                    tlRegisterBusiness.createBindCard(tl);
//                }else {
//                    tlBindCard.setStatus("1");
//                    tlRegisterBusiness.createBindCard(tlBindCard);
//                }
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功"); // 描述
//                map.put("redi",ip + "/v1.0/paymentgateway/repayment/tldh/returnBindcardMessage?bankCard=" + bankCard
//                        + "&idCard=" + idCard + "&phone=" + phone+ "&ipAddress=" + ip);
                return map;
            }
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }
    }



    /**
     * 统一下单（消费）
     * @param
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/topay")
    public @ResponseBody Object toPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        LOG.info("开始支付================================"); // TODO 消费城市
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard=prp.getIdCard();
        String bankcard=prp.getBankCard();
        String amount=prp.getRealAmount();
        String cvn2=prp.getSecurityCode();
        String extra=prp.getExtra();

        String[] a=extra.split("\\|");
        String[] cityResult =a[1].split("-");
        String  citys=cityResult[1];
        List<TLAree> lists=tlRegisterBusiness.findCityCodeByCity(citys);
        String city=lists.get(0).getCityCode().substring(0,4);
        String province=city.substring(0,2);



        String expiry=this.expiredTimeToMMYY(prp.getExpiredTime());
        String bigRealAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        String merchantNo=tlRegister.getMerchantCode();




        Map<String, String> map=new HashMap<>();
        Map<String, String> params=new HashMap<>();
        params.put("merchant_no",merchantNo);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        params.put("pay_type",PAY_TYPE);
        params.put("bankcard_no",bankcard);
        params.put("total_fee",bigRealAmount);//金额
        params.put("out_trade_no",orderCode);//订单号
        params.put("notify_url",ip+"/v1.0/paymentgateway/topup/tlt/pay/call_back");
        params.put("cvn2",cvn2);
        params.put("expiry",expiry);
        params.put("province",province);
        params.put("city",city);
        params.put("spbill_create_ip","116.234.78.38");
        LOG.info("请求参数为:"+params);
        Map<String,String>maps= null;

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/Unifiedorder",params);

        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
            return map;
        }
        JSONObject fromObject = JSONObject.fromObject(maps);
        LOG.info("返回参数为 = " + fromObject);
        String returnCode = fromObject.getString("return_code");
        String returnMsg = fromObject.getString("return_msg");
        if ("00".equals(returnCode)){
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }

    }

    /**
     * 消费异步回调
     * @param
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/tlt/pay/call_back")
    public void fayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
             LOG.info("通联消费异步回调进来了========================================================");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String status = request.getParameter("order_status");
        String orderCode = request.getParameter("out_trade_no");
        String orderNo = request.getParameter("order_no");//第三方流水号
        String payResult = request.getParameter("pay_result");


        LOG.info("回调参数====" + status + orderCode + orderNo + status + payResult);
        LOG.info("第三方流水号======orderid" + orderNo);
        LOG.info("订单===========dsorderid" + orderCode);

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

        String channelTag = prp.getChannelTag();
        String ipAddress = prp.getIpAddress();

        if ("paySuccess".equals(status)) {

            LOG.info("支付成功=============");
            String version = "56"; // TODO 修改通道标识
//            if ("HQG_QUICK".equalsIgnoreCase(channelTag)) {
//                version = "18";
//            }
            LOG.info("version======" + version);
            RestTemplate restTemplate = new RestTemplate();

            String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", orderCode);
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
            requestEntity.add("order_code", orderCode);
            requestEntity.add("third_code", orderNo); // 第三方订单号
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
            }

            LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

            LOG.info("订单已支付!");

            PrintWriter pw = response.getWriter();
            pw.print("ok");
            pw.close();

        } else if ("01".equals(status)) {
            LOG.info("交易处理中!");

            PrintWriter pw = response.getWriter();
            pw.print("ok");
            pw.close();
        } else {
            LOG.info("交易处理失败!");
            addOrderCauseOfFailure(orderCode, payResult, ipAddress);
            PrintWriter pw = response.getWriter();
            pw.print("ok");
            pw.close();
        }
    }

    /**
     * 出款接口
     * @param
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/toSettle")
    public @ResponseBody Object toSettle(@RequestParam(value = "orderCode") String orderCode) throws Exception {
        LOG.info("开始进入通联还款接口=====================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard=prp.getIdCard();
        String bankcard=prp.getBankCard();
        String amount=prp.getRealAmount();

        String bigRealAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        String merchantNo=tlRegister.getMerchantCode();


        Map<String, String> map=new HashMap<>();
        Map<String, String> params=new HashMap<>();
        params.put("merchant_no",merchantNo);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        params.put("pay_type",PAY_TYPE);

        params.put("bankcard_no",bankcard);
        params.put("total_fee",bigRealAmount);//金额
        params.put("out_withdraw_no",orderCode);//订单号
//        params.put("out_trade_no",orderCode);
        params.put("notify_url",ip+"/v1.0/paymentgateway/repayment/tlt/settleNotifyUrl");
        LOG.info("请求参数为:"+params);
        Map<String,String>maps=null;
        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/Withdraw",params);

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject fromObject = JSONObject.fromObject(maps);
        LOG.info("返回参数为 = " + fromObject);
        String returnCode = fromObject.getString("return_code");
        String returnMsg = fromObject.getString("respMsg");
        if ("00".equals(returnCode)){
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行出款");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }

    }

    /**
     * 还款异步回调
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/tlt/settleNotifyUrl")
    public void settleNotifyUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("通联还款异步回调进来了======");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                LOG.info(key + "=============" + s);
            }
        }
        String status = request.getParameter("order_status");
        String orderCode = request.getParameter("out_trade_no");//我方订单号
        String orderNo = request.getParameter("order_no");//第三方流水号
        String payResult = request.getParameter("pay_result");
        String sign = request.getParameter("sign");

        LOG.info("回调参数====" + status + orderCode + orderNo + status + payResult + sign);
        LOG.info("第三方流水号======orderid" + orderNo);
        LOG.info("订单===========dsorderid" + orderCode);

        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

        try {
        if("withdrawSuccess".equals(status)) {
            LOG.info("***********通联还款代付交易成功***********************");
            RestTemplate restTemplate = new RestTemplate();
            String version = "56";
//        		String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            String url = ip+"/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", orderCode);
            requestEntity.add("version", version);
            String result = null;
            JSONObject jsonObjects;
            JSONObject resultObj;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObjects = JSONObject.fromObject(result);
                resultObj = jsonObjects.getJSONObject("result");
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("", e);
            }

//        		url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            url = ip+"/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", orderCode);
                    requestEntity.add("third_code", orderNo); // 第三方订单号

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);
            LOG.info("订单已支付!");
        }else {
            LOG.info("支付失败");
            this.addOrderCauseOfFailure(orderCode, "支付失败", prp.getIpAddress());
        }
        PrintWriter pw = response.getWriter();
        pw.print("ok");
        pw.close();
    } catch (Exception e) {
        LOG.info("回调信息异常", e);
        PrintWriter pw = response.getWriter();
        pw.print("ok");
        pw.close();
    }
}


    /**
     * 商户余额查询
     * @param idcard
     * @param bankcard
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/queryBal")
    public @ResponseBody Object queryBal(@RequestParam(value = "idcard") String idcard,
                                         @RequestParam(value = "bankcard")String bankcard) throws Exception {
        Map<String, String> map=new HashMap<>();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idcard);
        if(tlRegister==null){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"无该用户信息");
            return map;
        }
        String merchantNo=tlRegister.getMerchantCode();

        Map<String, String> params=new HashMap<>();
        params.put("merchant_no",merchantNo);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        params.put("pay_type",PAY_TYPE);
        params.put("bank_no",bankcard);

        Map<String,String>maps=null;
        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/MerchantBalanceQuery",params);

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject fromObject = JSONObject.fromObject(maps);
        LOG.info("返回参数为 = " + fromObject);
        String returncode=fromObject.getString("return_code");
        String message=fromObject.getString("return_msg");
        if("00".equals(returncode)){
            String balance=fromObject.getString("balance");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"商户余额为"+balance+"分");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,message);
            return map;
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/tlupRegister")
    public @ResponseBody Object tlupRegister(@RequestParam(value = "bankCard") String bankCard,
                                             @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                                             @RequestParam(value = "userName") String userName, @RequestParam(value = "rate") String rate,
                                             @RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName) throws Exception {
        LOG.info("开始修改用户进件信息======================");
        String rate1 = new BigDecimal(rate).multiply(new BigDecimal("1000")).setScale(1).toString();
        String extraFee1 = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        String merchantNo=tlRegister.getMerchantCode();

        Map<String,Object>map=new HashMap<>();
        Map<String, String> params=new HashMap<>();
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("merchant_no",merchantNo);
        params.put("merchant_name",userName);
        params.put("idcard_no",idCard);
        params.put("phone",phone);
        params.put("rate",rate1);
        params.put("poundage",extraFee1);
        params.put("province","31");
        params.put("city","3101");
        params.put("district","310113");

        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)

        Map<String,String>maps= null;

        LOG.info("请求参数为:"+params);


        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/MerchantUpdate",params);

        } catch (Exception e) {

            e.printStackTrace();
        }

        JSONObject fromObject = JSONObject.fromObject(maps);
        LOG.info("返回参数为 = " + fromObject);
        String returnCode = fromObject.getString("return_code");
        String returnMsg = fromObject.getString("return_msg");
        if ("00".equals(returnCode)) {
            tlRegister.setUserName(userName);
            tlRegister.setIdcard(idCard);
            tlRegister.setBankCard(bankCard);
            tlRegister.setPhone(phone);
            tlRegister.setBankName(bankName);
            tlRegister.setRate(rate);
            tlRegister.setExtraFee(extraFee);
            tlRegister.setMerchantCode(merchantNo);
            tlRegisterBusiness.createRegister(tlRegister);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, returnMsg); // 描述
            return map;
        }
    }

    /**
     * 查询落地城市接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/choosecity")
    public @ResponseBody Object choose(@RequestParam(value = "province",required = false) String province) throws Exception {
        Map<String,Object> map=new HashMap<>();
        if(province==null||"".equals(province)){
            List<TLAree> provinces=tlRegisterBusiness.findAllProvince();
            if (provinces==null || provinces.size()==0){
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"当前未获取到省份，请稍后再试");
                return map;
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,provinces);
            return map;
        }else {
            List<TLAree> citys=tlRegisterBusiness.findCityByProvince(province);
            if (citys==null || citys.size()==0){
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"当前未获取到城市，请稍后再试");
                return map;
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,citys);
            return map;
        }
    }

    /**
     * 存储绑卡成功信息
     */
    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/tldh/returnBindcardMessage")
    public String TLDHBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");


        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String bankCard = request.getParameter("bankCard");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String ipAddress = request.getParameter("ipAddress");



        model.addAttribute("bankCard", bankCard);
        model.addAttribute("idCard", idCard);
        model.addAttribute("phone", phone);
        model.addAttribute("ipAddress", ipAddress);

        return "tlbindcardsuccess";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/createbindCard")
    public @ResponseBody Object createBindCard(@RequestParam(value = "bankCard") String bankCard,
                                               @RequestParam(value = "idCard") String idCard,
                                               @RequestParam(value = "phone") String phone) throws Exception {
        LOG.info("开始进入通联存储绑卡信息=====================================================");
        Map<String,Object>map=new HashMap<>();
        TLBindCard tlBindCard=tlRegisterBusiness.findTLBindCardByBankCard(bankCard);
        if(tlBindCard==null){
            TLBindCard tl=new TLBindCard();

            tl.setIdCard(idCard);
            tl.setPhone(phone);
            tl.setBankCard(bankCard);
            tl.setCreateTime(new Date());
            tl.setStatus("0");
            tlRegisterBusiness.createBindCard(tl);
        }else {
            tlBindCard.setStatus("0");
            tlBindCard.setCreateTime(new Date());
            tlRegisterBusiness.createBindCard(tlBindCard);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        return map;
    }

    /**
     * 确认绑卡成功，更新数据
     * @param bankCard
     * @param idCard
     * @param phone
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/confirmBindCard")
    public @ResponseBody Object confirmBindCard(@RequestParam(value = "bankCard") String bankCard,
                                               @RequestParam(value = "idCard") String idCard,
                                               @RequestParam(value = "phone") String phone) throws Exception {
        LOG.info("开始进入通联存储绑卡信息=====================================================");
        Map<String,Object>map=new HashMap<>();
        TLBindCard tlBindCard=tlRegisterBusiness.findTLBindCardByBankCard(bankCard);
        if(tlBindCard==null){
            TLBindCard tl=new TLBindCard();

            tl.setIdCard(idCard);
            tl.setPhone(phone);
            tl.setBankCard(bankCard);
            tl.setCreateTime(new Date());
            tl.setStatus("1");
            tlRegisterBusiness.createBindCard(tl);
        }else {
            tlBindCard.setStatus("1");
            tlBindCard.setCreateTime(new Date());
            tlRegisterBusiness.createBindCard(tlBindCard);
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        return map;
    }

    /**
     * 通联订单查询接口
     * @author jayden
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tlt/payQuery")
    public Object payQuery(@RequestParam(value = "orderCode") String orderCode,
                           @RequestParam(value = "transType") String transType,
                           @RequestParam(value="bankCard") String bankCard) throws Exception {
        LOG.info("开始进行交易查询=======================");
        Map<String, String> map=new HashMap<>();

        TLBindCard tlBindCard=tlRegisterBusiness.findTLBindCardByBankCard(bankCard);
        if(tlBindCard==null){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"该卡未绑卡");
            return map;
        }
        String icCard=tlBindCard.getIdCard();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(icCard);
        if(tlRegister==null){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"该用户未进件");
            return map;
        }
        String merchantNo=tlRegister.getMerchantCode();

        Map<String, String> params=new HashMap<>();
        params.put("merchant_no",merchantNo);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        params.put("type",transType);//01 查消费订单 02查提现订单
        params.put("out_trade_no",orderCode);//下单返回商户订单号
        LOG.info("请求参数为:"+params);
        Map<String,String>maps= null;

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/PayOrderQuery",params);

        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
            return map;
        }
        LOG.info("通联订单"+orderCode+"查询返回信息==========="+maps);
        String return_code=maps.get("return_code");
        String return_msg=maps.get("return_msg");
        if("00".equals(return_code)){//请求成功
            String order_status=maps.get("order_status");
            if("01".equals(transType)) {
                String pay_result=maps.get("pay_result");
                if(!"交易成功".equals(pay_result)){
                    com.alibaba.fastjson.JSONObject jsonObject=com.alibaba.fastjson.JSONObject.parseObject(pay_result);
                    pay_result=jsonObject.getString("errmsg");
                }
                if ("paySuccess".equals(order_status)) {
                    LOG.info("订单执行成功==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                } else if ("notPay".equals(order_status)) {
                    LOG.info("订单处理中==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                } else if ("payFailure".equals(order_status)) {
                    LOG.info("订单执行失败==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                } else {
                    LOG.info("订单号不存在==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                }
            }else{
                String pay_result=maps.get("pay_result");
                if ("withdrawSuccess".equals(order_status)) {
                    LOG.info("订单执行成功==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                }else if ("withdrawApply".equals(order_status)) {
                    LOG.info("订单处理中==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                } else if ("withdrawFailure".equals(order_status)) {
                    LOG.info("订单执行失败==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                } else {
                    LOG.info("订单号不存在==================");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, pay_result);
                    return map;
                }
            }
        }else{
            //请求失败
            LOG.info("请求失败==================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
            map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            return map;
        }

    }

    /**
     * 查询绑卡状态
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,value="/v1.0/paymentgateway/topup/tlt/BindCardQuery")
    @ResponseBody
    public Map queryBindCardResult(
            @RequestParam(value="idCard") String idCard,
            @RequestParam(value="bankCard") String bankCard
    ){
       Map map= new HashMap<String,String>();
        TLRegister tlRegister=tlRegisterBusiness.findTLRegisterByIdcard(idCard);
        if(tlRegister==null){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"该用户未进件");
            return map;
        }
        String merchantNo=tlRegister.getMerchantCode();

        Map<String, String> params=new HashMap<>();
        params.put("merchant_no",merchantNo);
        params.put("agent_no",AGENT_NO);//【代理商号】【M】
        params.put("version",VERSION);//【版本号】【M】目前版本“2.0”
        params.put("sign_type",SIGN_TYPE);//【加密类型】【M】MD5(目前仅支持md5)
        params.put("type","01");//01 查询卡
        params.put("bankcard_no",bankCard);
        params.put("pay_type",PAY_TYPE);//支付通道
        LOG.info("请求参数为:"+params.toString());
        Map<String,String>maps= null;

        try {
            maps = KKIpayUtil.dorequest("http://repayment.9580buy.com:8080/repayment-api/BindCardQuery",params);

        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
            return map;
        }
        LOG.info("通联用户========"+idCard+"绑卡查询返回信息==========="+maps.toString());
        String return_code=maps.get("return_code");
        String return_msg=maps.get("return_msg");
        String bind_status=maps.get("bind_status");
        String remark=maps.get("remark");
        if("00".equals(return_code)){
            if("bindSuccess".equals(bind_status)){
                LOG.info("通联绑卡成功=============bankCard=="+bankCard);
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE,remark);
                return map;
            }else if("bindApply".equals(bind_status)){
                LOG.info("通联绑卡申请中=============bankCard=="+bankCard);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,remark);
                return map;
            }else if("bindFail".equals(bind_status)){
                LOG.info("通联绑卡失败=============bankCard=="+bankCard);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,remark);
                return map;
            }else{
                LOG.info("通联绑卡失败=============bankCard=="+bankCard+"===bind_status==="+bind_status);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"查询失败，请稍后重试");
                return map;
            }
        }else{
            LOG.info("通联绑卡查询请求失败=============bankCard=="+bankCard);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,remark);
            return map;
        }

    }
}
