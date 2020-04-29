package com.jh.paymentgateway.controller.ld.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.ld.business.*;
import com.jh.paymentgateway.controller.ld.pojo.*;
import com.jh.paymentgateway.controller.ld.util.HttpsClientUtil;
import com.jh.paymentgateway.controller.ld.util.SignUtil;
import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;
import com.jh.paymentgateway.pojo.LDRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class LDQuickPageRequest extends BaseChannel {


    public static final String key = "d9d50859d1f8117f37c4efdeedd75045";

    //汇潮大额快捷
    public static final String MERCHANT_NO="1356182";//机构号  小

    private static final Logger LOG = LoggerFactory.getLogger(LDQuickPageRequest.class);

    private static final String URL="http://pay.juxingzhifu.com";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LDMerchantBusiness merchantBusiness;

    @Autowired
    private LDRegisterBusiness LDRegisterBusiness;

    @Autowired
    private LDBindCardBusiness ldBindCardBusiness;

    @Autowired
    private LDBankBranchBusiness ldBankBranchBusiness;

    @Autowired
    private LDMerchantBusiness ldMerchantBusiness;

    @Autowired
    private HCDMerchantBusiness hcdMerchantBusiness;

    @Value("${payment.ipAddress}")
    private String ip;


    // 进件注册
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ld/Dockentrance")
    public @ResponseBody Object Dockentrance(@RequestParam(value = "orderCode") String orderCode) throws Exception {
//        String expiredTime=this.expiredTimeToMMYY(expiredTimes);
//        LOG.debug("汇潮大额进件注册信息======="+dbankCard +"===="+ dbankName+"===="+dphone +"===="+bankCard +"===="+bankName +"===="+phone);
//        //储蓄卡信息
//        String debitCardNo = dbankCard;
//        String debitCardName = dbankName;
//        String debitCardPhone = dphone;
        Map map=new HashMap<>();
        PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(orderCode);

        orderCode = bean.getOrderCode();
        LOG.info("订单号："+orderCode);
        String bankCard = bean.getBankCard();
//        String bankName = bean.getDebitBankName();
        String userName = bean.getUserName();
        String phone = bean.getCreditCardPhone();
        String ipAddress = bean.getIpAddress();
        String rate = bean.getRate();
        String extraFee = bean.getExtraFee();
        String idCard = bean.getIdCard();
        String orderType = bean.getOrderType();
        String userId = bean.getUserId();
        String cardtype = bean.getCreditCardCardType();
        String bankNo = bean.getDebitCardNo();
        String cardType = bean.getDebitCardCardType();
        String bankName = bean.getCreditCardBankName();
        String debitCardName=bean.getDebitBankName();
        String debitCardNo =bean.getDebitCardNo();
        String debitCardPhone=bean.getDebitPhone();
        String amount = bean.getAmount();
        String expiredTime = bean.getExpiredTime();
        String securityCode = bean.getSecurityCode();
        Map<String, Object> maps = new HashMap<String, Object>();
        LDQuickRegister LDQuickRegister = LDRegisterBusiness.queryByIdCard(idCard);
        LDBindCard ldBindCard = ldBindCardBusiness.queryByBankCard(bankCard);

        //是否进件
        if(LDQuickRegister==null) {
            debitCardName = Util.queryBankNameByBranchName(debitCardName);
            maps = (Map<String, Object>) this.toReg(idCard, userName, debitCardName, debitCardNo, debitCardPhone);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        }else {
            if(!debitCardNo.equals(LDQuickRegister.getBankCard())||!debitCardPhone.equals(LDQuickRegister.getPhone())) {
                LOG.info("===================汇潮大额用户储蓄卡信息是否改变，去修改注册信息===========================");
                debitCardName = Util.queryBankNameByBranchName(debitCardName);
                maps = (Map<String, Object>) this.toUpdateReg(idCard, userName, debitCardName, debitCardNo, debitCardPhone);
                if (!"000000".equals(maps.get("resp_code"))) {
                    return maps;
                }
            }
        }

        //是否绑卡
        if(ldBindCard==null || "0".equals(ldBindCard.getStatus())) {
            //跳转到绑卡页面
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
            maps.put(CommonConstants.RESULT,
                    ip + "/v1.0/paymentgateway/repayment/ld/bindcard?bankCard=" + bankCard
                            + "&bankName="+ URLEncoder.encode(bankName, "UTF-8")+ "&cardType="
                            + URLEncoder.encode("0", "UTF-8") + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip+"&orderCode="+orderCode);
            LOG.info("绑卡请求返回==============="+maps.toString());
            return maps;
        }else {
            String bigExtraFee = new BigDecimal(extraFee).setScale(1).toString();
            // 判断用户是否修改费率或单笔手续费
            if (!rate.equals(ldBindCard.getRate()) | !bigExtraFee.equals(ldBindCard.getExtraFee())) {
                LOG.info("汇潮大额用户修改本地费率===== "+ldBindCard.getBankCard()+"===="+rate+"=="+bigExtraFee);
                ldBindCard.setRate(rate);
                ldBindCard.setExtraFee(bigExtraFee);
                ldBindCard.setUpdateTime(new Date());
                ldBindCardBusiness.create(ldBindCard);
            }
        }
        map.clear();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "进入支付页面"); // 描述
        //跳转城市选择页面,进行支付
        map.put(CommonConstants.RESULT,
                ip + "/v1.0/paymentgateway/quick/ldquick/choose/city?ordercode=" + orderCode
                        + "&ipAddress=" + ip);
        return map;
    }

    /**
     * 注册接口
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ld/toReg")
    public @ResponseBody Object toReg(
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "debitCardName") String debitCardName,
            @RequestParam(value = "debitCardNo") String debitCardNo,
            @RequestParam(value = "debitCardPhone") String debitCardPhone) {
        LOG.info("=========汇潮大额注册接口===========");
        Map<String, Object> map = new HashMap<>();
        String address = "/api/register";
        String branchNo = "000000000000";
        LDBankBranch ykBankBranchNo = ldBankBranchBusiness.findByDebitCardName(debitCardName);
        if(ykBankBranchNo!=null) {
            //有问题的地方
            branchNo = ykBankBranchNo.getBankbranchNo();
        }
        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("id_cardno", idCard);//商户身份证
        signmap.put("phone", debitCardPhone);//电话
        signmap.put("merchant_name", userName);//商户姓名
        signmap.put("merchant_province", "上海市");//商户所在省
        signmap.put("merchant_city", "上海市");//
        signmap.put("merchant_district", "宝山区");//商户所在区
        signmap.put("bank_province", "上海市");//支行所在省
        signmap.put("bank_city", "上海市");//支行所在市
        signmap.put("bank_address", "上海市");//支行地址
        signmap.put("bank_branch_name", debitCardName+"上海市安亭支行");//支行名称
        signmap.put("bank_branch_no", branchNo);//支行联行号
        signmap.put("bank_cardno", debitCardNo);//储蓄卡号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);
        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]注册请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            LOG.info("[汇潮大额]注册成功");
            String merchantno = JSON.getString("merchantno");
            msg = JSON.getString("message");
            LDQuickRegister ldQuickRegister=LDRegisterBusiness.queryByIdCard(idCard);
            if(ldQuickRegister==null){
                LDQuickRegister ykRegister = new LDQuickRegister();
                ykRegister.setIdCard(idCard);
                ykRegister.setRealName(userName);
                ykRegister.setBankCard(debitCardNo);
                ykRegister.setPhone(debitCardPhone);
                ykRegister.setMerchantNo(merchantno);
                ykRegister.setCreateTime(new Date());
                ykRegister.setUpdateTime(new Date());
                LDRegisterBusiness.create(ykRegister);
            }else{
                ldQuickRegister.setIdCard(idCard);
                ldQuickRegister.setRealName(userName);
                ldQuickRegister.setBankCard(debitCardNo);
                ldQuickRegister.setPhone(debitCardPhone);
                ldQuickRegister.setMerchantNo(merchantno);
                ldQuickRegister.setCreateTime(new Date());
                ldQuickRegister.setUpdateTime(new Date());
                LDRegisterBusiness.create(ldQuickRegister);
            }

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册成功"); // 描述
            return map;
        }else if("1".equals(resultCode)&&JSON.getString("errCodeDes").contains("重复注册")){//已进件
            LOG.info("[汇潮大额]已注册，将用户商户号存入数据库,idCard====="+idCard);
            String errCodeDes = JSON.getString("errCodeDes");
            String merchantno=processMerchantNo(errCodeDes);
            msg = JSON.getString("message");
            LDQuickRegister ldQuickRegister=LDRegisterBusiness.queryByIdCard(idCard);
            if(ldQuickRegister==null){
                LDQuickRegister ykRegister = new LDQuickRegister();
                ykRegister.setIdCard(idCard);
                ykRegister.setRealName(userName);
                ykRegister.setBankCard(debitCardNo);
                ykRegister.setPhone(debitCardPhone);
                ykRegister.setMerchantNo(merchantno);
                ykRegister.setCreateTime(new Date());
                ykRegister.setUpdateTime(new Date());
                LDRegisterBusiness.create(ykRegister);
            }else{
                ldQuickRegister.setIdCard(idCard);
                ldQuickRegister.setRealName(userName);
                ldQuickRegister.setBankCard(debitCardNo);
                ldQuickRegister.setPhone(debitCardPhone);
                ldQuickRegister.setMerchantNo(merchantno);
                ldQuickRegister.setCreateTime(new Date());
                ldQuickRegister.setUpdateTime(new Date());
                LDRegisterBusiness.create(ldQuickRegister);
            }

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册成功"); // 描述
            return map;
        } else{
            LOG.info("[汇潮大额]注册失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }

    }

    /**
     * 修改注册信息接口
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldquick/toUpdateReg")
    public @ResponseBody Object toUpdateReg(
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "debitCardName") String debitCardName,
            @RequestParam(value = "debitCardNo") String debitCardNo,
            @RequestParam(value = "debitCardPhone") String debitCardPhone) {
        LOG.info("=========汇潮大额修改注册信息接口===========");
        Map<String, Object> map = new HashMap<>();
        String branchNo = "000000000000";
        LDBankBranch ykBankBranchNo = ldBankBranchBusiness.findByDebitCardName(debitCardName);
        if(ykBankBranchNo!=null) {
            branchNo = ykBankBranchNo.getBankbranchNo();
        }
        LDQuickRegister ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ykRegister.getMerchantNo();
        String address = "/api/modify";
        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("id_cardno", idCard);//商户身份证
        signmap.put("phone", debitCardPhone);//电话
        signmap.put("merchant_name", userName);//商户姓名
        signmap.put("merchant_province", "上海市");//商户所在省
        signmap.put("merchant_city", "上海市");//
        signmap.put("merchant_district", "宝山区");//商户所在区
        signmap.put("bank_province", "上海市");//支行所在省
        signmap.put("bank_city", "上海市");//支行所在市
        signmap.put("bank_address", "上海市");//支行地址
        signmap.put("bank_branch_name", debitCardName+"上海市安亭支行");//支行名称
        signmap.put("bank_branch_no", branchNo);//支行联行号
        signmap.put("bank_cardno", debitCardNo);//储蓄卡号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]修改注册信息请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            LOG.info("[汇潮大额]修改注册信息成功");
            ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
            ykRegister.setBankCard(debitCardNo);
            ykRegister.setPhone(debitCardPhone);
            ykRegister.setRealName(userName);
            ykRegister.setUpdateTime(new Date());
            LDRegisterBusiness.create(ykRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册信息修改成功"); // 描述
            return map;
        }else {
            LOG.info("[汇潮大额]修改注册信息失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/ld/bindcard")
    public String returnYKKJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
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
        String orderCode = request.getParameter("orderCode");

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
        model.addAttribute("orderCode", orderCode);

        return "ldquickbindcard";
    }

    /**
     * 信用卡绑卡发送验证码接口
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldquick/toSendSms")
    public @ResponseBody Object toSendSms(
            @RequestParam(value = "bankCard") String bankCard,
            @RequestParam(value = "bankName") String bankName,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "securityCode") String securityCode,
            @RequestParam(value = "expiredTime") String expiredTime,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "extraFee") String extraFee
    ) {
        LOG.info("请求发短信接收参数=======" + bankCard + "==" + bankName + "==" + idCard + "==" + phone + "==" + userName + "=="
                + userName + "==" + securityCode + "==" + expiredTime + "==" + rate + "==" + extraFee + "==");
        LOG.info("=========汇潮大额绑卡验证码发送接口===========");
        expiredTime = this.expiredTimeToMMYY(expiredTime);
        String bigExtraFee = new BigDecimal(extraFee).setScale(1).toString();
        LDQuickRegister ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ykRegister.getMerchantNo();
        Map<String, Object> map = new HashMap<>();
        String address = "/api/sendMessage";
        // 交易流水号
        String openOrderNum = UUID.randomUUID().toString().replaceAll("-", "");
        LOG.info("[汇潮大额]流水号==========="+openOrderNum);
        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("openOrderNum", openOrderNum);//开卡订单号
        signmap.put("bankcardNum", bankCard);//支持的信用卡号
        signmap.put("cvv", securityCode);//安全码
        signmap.put("expired_time", expiredTime);//有效期 MMYY
        signmap.put("phone", phone);//手机号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);
        LDBindCard ykBindCard = ldBindCardBusiness.queryByBankCard(bankCard);
        if(ykBindCard == null) {
            ykBindCard = new LDBindCard();
            ykBindCard.setCreateTime(new Date());
        }
        ykBindCard.setRealName(userName);
        ykBindCard.setIdCard(idCard);
        ykBindCard.setBankCard(bankCard);
        ykBindCard.setPhone(phone);
        ykBindCard.setExpiredTime(expiredTime);
        ykBindCard.setSecurityCode(securityCode);
        ykBindCard.setRate(rate);
        ykBindCard.setExtraFee(bigExtraFee);
        ykBindCard.setStatus("0");
        ykBindCard.setOrderNum(openOrderNum);
        ykBindCard.setMerchantNo(merchantNo);
        ykBindCard.setUpdateTime(new Date());
        ldBindCardBusiness.create(ykBindCard);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]绑卡请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            msg = JSON.getString("message");
            LOG.info("[智极还]绑卡短信请求成功"+msg);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            map.put("requestNo", openOrderNum);//绑卡流水号
            map.put("idCard", idCard);//证件号
            return map;
        }else {
            LOG.info("[汇潮大额]请求发短信失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 短信确认
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldquick/confirmSms")
    public @ResponseBody Object confirmSms(
            @RequestParam(value = "requestNo") String requestNo,
            @RequestParam(value = "bankCard") String bankCard,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "smsCode") String smsCode,
            @RequestParam(value="ordercode") String ordercode) {
        LOG.info("汇潮大额短信确认接收参数=======" + requestNo + "==" +bankCard+ "==" + idCard + "==" + smsCode+"=====ordercode===="+ordercode);
        LOG.info("=========汇潮大额确认验证码接口===========");
        Map<String, Object> map = new HashMap<>();

        PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(ordercode);
        String bankName=bean.getCreditCardBankName();
        String cardtype=bean.getCreditCardCardType();

        // 绑卡交易流水号
        String openOrderNum = requestNo;
        LDQuickRegister ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ykRegister.getMerchantNo();
        String address = "/api/getMessageCode";
        try {
            Map<String, String> signmap = new HashMap<String, String>();
            signmap.put("mechanism_id", MERCHANT_NO);//机构编号
            signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
            signmap.put("openOrderNum", openOrderNum);//开卡订单号
            signmap.put("messageCode", smsCode);//验证码

            String sign = SignUtil.genSign(signmap, key);
            signmap.put("sign", sign);
            String json = JSON.toJSONString(signmap);
            LOG.info("[汇潮大额]请求参数=="+json);
            LOG.info("[汇潮大额]请求地址=="+URL+address);
            String result = null;
            result = HttpsClientUtil.sendRequest(URL+address, json);
            JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
            LOG.info("[汇潮大额]请求结果=="+result);

            String returnCode = JSON.getString("returnCode");
            String msg = null;
            if(!"0".equals(returnCode)) {
                LOG.info("[汇潮大额]短com.jh.paymentgateway.controller.qysh.YKHKEntrance信确认请求通讯失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
                return map;
            }
            String resultCode = JSON.getString("resultCode");
            if("0".equals(resultCode)) {
                msg = JSON.getString("message");
                LOG.info("[汇潮大额]短信确认成功=="+msg);
                LDBindCard ykBindCard = ldBindCardBusiness.queryByBankCard(bankCard);
                if(ykBindCard==null) {return ResultWrap.init(CommonConstants.FALIED, "查询绑卡信息出错!");}
                ykBindCard.setStatus("1");
                ldBindCardBusiness.create(ykBindCard);

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功"); // 描述
//                map.put("redirect_url",ip+"/v1.0/paymentgateway/quick/ld/pay-view");
                //绑卡成功，跳转城市选择页面
                map.put("redirect_url",
                        ip + "/v1.0/paymentgateway/quick/ldquick/choose/city?ordercode=" + ordercode
                                + "&ipAddress=" + ip);
                return map;
            }else {
                LOG.info("[汇潮大额]短信确认失败");
                msg = JSON.getString("errCodeDes");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
                return map;
            }
        } catch (Exception e) {
            LOG.error("请求异常", e);
        }
        return map;
    }

    /**
     * 跳转支付页面
     *
     * @param request
     * @param response
     * @param model
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ldquick/pay-view")
    public String returnLDQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String ordercode = request.getParameter("ordercode");
        String bankName = request.getParameter("bankName");
        String cardType = request.getParameter("cardType");
        String bankCard = request.getParameter("bankCard");
        String ipAddress = request.getParameter("ipAddress");

        model.addAttribute("ordercode", ordercode);
        model.addAttribute("bankName", bankName);
        model.addAttribute("cardType", cardType);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("ipAddress", ipAddress);

        return "ldquickpaymessage";
    }

    /**
     * 跳转到省市区选择页面
     * @param request
     * @param response
     * @param model
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ldquick/choose/city")
    public String toHtml(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        LOG.info("汇潮大额------------------跳转到省市区选择页面");

        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String orderCode = request.getParameter("ordercode");
        String ipAddress = request.getParameter("ipAddress");

        model.addAttribute("ordercode", orderCode);
        model.addAttribute("ipAddress", ipAddress);

        return "ldquerycity";
    }

    /**
     * 开始消费
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/toPay")
    public @ResponseBody Object toPay(@RequestParam(value = "orderCode") String orderCode,
                                      @RequestParam(value="city")String cityName) {
        LOG.info("[汇潮大额]开始消费================================orderCode======="+orderCode);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("============="+prp.toString());
        Map<String, Object> map = new HashMap<>();
        String amount = prp.getAmount();//信用卡扣款金额
        String rate = prp.getRate();//费率
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
        LOG.info("[汇潮大额]消费金额==" +amount+ "====费率==="+bigRate);
        String phone = prp.getPhone();//信用卡预留手机号
        String bankCard = prp.getBankCard();//信用卡号
        String bankName = prp.getCreditCardBankName();//信用卡名称
//        String extra = prp.getExtra();//地区
        //页面传值city
        String extra =cityName ;
        LOG.info("orderCode======"+orderCode+"====消费城市====="+extra);
        String outTradeNo = orderCode;//流水号

        LDBindCard ykBindCard = ldBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ykBindCard.getMerchantNo();//商户号


        try {
//            String[] a=extra.split("\\|");
//            String[] companyname =a[1].split("-");
//            String province=companyname[0];
//            String city=companyname[1];
//
//            if(province.contains("省")&&city.contains("市")){
//                //省 市
//                lists=merchantBusiness.getAllByProvinceAndCity(province,city);
//            }else if(province.contains("市")&&city.contains("区")){
//                //市 区
//                lists=merchantBusiness.getByCityAndCounty(province,city);
//            }else if (province.contains("省")&&city.contains("区")){
//                //省 区
//                lists=merchantBusiness.getByProvinceAndCounty(province,city);
//            }else{
//                //省 市 模糊查询
//                lists=merchantBusiness.getByProvinceAndCityLike(province,city);
//            }
            String address = "/api/createOrder";
            List<HCDMerchant> lists=null;
            lists=merchantBusiness.queryByCity(extra);
            LOG.info("落地商户数量============="+lists.size());
//            int num=(int)Math.random()*lists.size()-1;
//            if (num<0){
//                num=0;
//            }
            Random random=new Random();
            int num=random.nextInt(lists.size()-1);
            String companyName=null;
            if(lists.size()>0){
                //汇潮大额需传compant_name值
                companyName=lists.get(num).getCompanyName();
            }
            LOG.info("[汇潮大额]请求流水号========"+outTradeNo);
            Map<String, String> signmap = new HashMap<String, String>();
            signmap.put("mechanism_id", MERCHANT_NO);//机构编号
            signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
            signmap.put("outTradeNo", outTradeNo);//开卡订单号
            signmap.put("rate", bigRate);//费率
            signmap.put("bankcardNum", bankCard);//消费卡号
            signmap.put("company_name", companyName);//自定义账单名称
            signmap.put("amount", amount);//金额 元
//        signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/hchk/payNotifyUrl");//异步回调地址
            signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/ld/payNotifyUrl");

            String sign = SignUtil.genSign(signmap, key);
            signmap.put("sign", sign);
            String json = JSON.toJSONString(signmap);
            LOG.info("[汇潮大额]请求参数=="+json);
            LOG.info("[汇潮大额]请求地址=="+URL+address);
            String result = null;
            result = HttpsClientUtil.sendRequest(URL+address, json);
            JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
            LOG.info("[汇潮大额]请求结果=="+result);

            String returnCode = JSON.getString("returnCode");
            String msg = null;
            if(!"0".equals(returnCode)) {
                LOG.info("[汇潮大额]消费请求通讯失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
                map.put("redirect_url",ip+"/v1.0/paymentchannel/topup/rs/jump/error");
                return map;
            }
            String resultCode = JSON.getString("resultCode");
            outTradeNo = JSON.getString("outTradeNo");
            if("0".equals(resultCode)) {
                LOG.info("[汇潮大额]消费交易成功========"+outTradeNo);
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                map.put("redirect_url",ip+"/v1.0/paymentchannel/topup/sdjpaysuccess");
                return map;
            }else {
                LOG.info("[汇潮大额]消费交易失败");
                msg = JSON.getString("message");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
                map.put("redirect_url",ip+"/v1.0/paymentchannel/topup/rs/jump/error");
                return map;
            }
        } catch (Exception e) {
            LOG.error("请求异常", e);
            map.clear();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
            map.put("redirect_url",ip+"/v1.0/paymentchannel/topup/rs/jump/error");
        }
        return map;
    }


    /**
     *   消费异步回调
     *
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/ld/payNotifyUrl")
    public void payNotifyUrl(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LOG.info("[汇潮大额]进入消费回调================================");
        try {
            BufferedReader streamReader = new BufferedReader( new InputStreamReader(req.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());

            LOG.info("[汇潮大额]消费回调参数信息： " + jsonObject.toJSONString());
            String paystate = jsonObject.getString("paystate");
            String dsorderid = jsonObject.getString("outTradeNo");//返回流水号
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
            if("1".equals(paystate)) {
                LOG.info("***********汇潮大额消费交易成功***********************orderCode======="+dsorderid);
                //进行出款交易
                this.toSettle(dsorderid);
            }else {
                LOG.info("支付失败");
                this.addOrderCauseOfFailure(dsorderid, "支付失败", prp.getIpAddress());
            }
            PrintWriter pw = res.getWriter();
            pw.print("success");
            pw.close();
        } catch (Exception e) {
            LOG.info("回调信息异常", e);
            PrintWriter pw = res.getWriter();
            pw.print("success");
            pw.close();
        }
    }

    /**
     *   消费订单查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/payQuery")
    public @ResponseBody Object payQuery(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[汇潮大额]开始消费订单查询================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String bankCard = prp.getBankCard();//信用卡号
        LDBindCard ykBindCard = ldBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ykBindCard.getMerchantNo();//商户号

        String address = "/get/orderInfo";
        String outTradeNo = orderCode;
        LOG.info("[汇潮大额]请求流水号========"+outTradeNo);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("outTradeNo", outTradeNo);//开卡订单号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            String paystate = JSON.getString("paystate");
            if("1".equals(paystate)) {
                LOG.info("[汇潮大额]订单交易成功========");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                return map;
            }else if("4".equals(paystate)||"5".equals(paystate)||"6".equals(paystate)) {
                LOG.info("[汇潮大额]订单交易处理中========");
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "交易处理中");
                return map;
            }else {
                LOG.info("[汇潮大额]订单交易失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "订单交易失败"); // 描述
                return map;
            }
        }else {
            LOG.info("[汇潮大额]订单交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     *   余额查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/balanceQuery")
    public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) {
        LOG.info("[汇潮大额]开始进行余额查询=======================");
        Map<String, Object> map = new HashMap<>();
        LDQuickRegister ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
        if(ykRegister==null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该卡号不存在"); // 描述
            return map;
        }
        String merchantNo = ykRegister.getMerchantNo();//商户号
        String address = "/get/banlance";
        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        //signmap.put("cardNumber", bankCard);//填写改卡号则为银行卡余额模式使用,除非必要要求否则不填

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            String banlance = JSON.getString("banlance");
            LOG.info("[汇潮大额]商户余额为==" + banlance);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "商户余额为" + banlance); // 描述
            return map;
        }else {
            LOG.info("[汇潮大额]余额查询失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 开始提现
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/toSettle")
    public @ResponseBody Object toSettle(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[汇潮大额]开始代付================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String idCard = prp.getIdCard();
        //出款到储蓄卡
        String bankCard = prp.getDebitCardNo();
        String realAmount = prp.getRealAmount();
        String extraFee = prp.getExtraFee();
        BigDecimal cashAmount = new BigDecimal(realAmount).add(new BigDecimal(extraFee)).setScale(2);
        LOG.info("[汇潮大额]还款金额：================"+realAmount+"$$"+cashAmount.toString());
        String fee = new BigDecimal(extraFee).setScale(1).toString();
        LDQuickRegister ykRegister = LDRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ykRegister.getMerchantNo();//商户号

        String address = "/api/createMonOrder";
        String repayOrderNum = orderCode;
        LOG.info("汇潮大额代付请求流水号========"+repayOrderNum);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("repayOrderNum", repayOrderNum);//开卡订单号
        signmap.put("fee", fee);//订单单笔手续费 （单位元）
        signmap.put("bankcardNum", bankCard);//开卡成功的卡号
        signmap.put("amount", cashAmount.toString());//金额 元 (到账进额=amount-fee)
//        signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/hchk/settleNotifyUrl");//异步回调地址
        signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/ld/settleNotifyUrl");

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]代付请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            LOG.info("[汇潮大额]代付交易成功========");
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行出款");
            return map;
        }else {
            LOG.info("[汇潮大额]代付交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     *   代付订单查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/settleQuery")
    public @ResponseBody Object settleQuery(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[汇潮大额]开始代付订单查询================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String bankCard = prp.getBankCard();//信用卡号
        LDBindCard ykBindCard = ldBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ykBindCard.getMerchantNo();//商户号

        String address = "/get/reapayOrderInfo";
        String RepayOutTradeNo = orderCode;
        LOG.info("请求流水号========"+RepayOutTradeNo);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("RepayOutTradeNo", RepayOutTradeNo);//开卡订单号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            String status = JSON.getString("status");
            if("1".equals(status)) {
                LOG.info("[汇潮大额]订单交易成功========");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                return map;
            }else if("0".equals(status)) {
                LOG.info("[汇潮大额]订单交易处理中========");
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "交易处理中");
                return map;
            }else {
                LOG.info("[汇潮大额]订单交易失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "订单交易失败"); // 描述
                return map;
            }
        }else {
            LOG.info("[汇潮大额]订单交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     *   还款异步回调
     *
     */
    @RequestMapping(method = { RequestMethod.POST,
            RequestMethod.GET }, value = "/v1.0/paymentgateway/repayment/ld/settleNotifyUrl")
    public void settleNotifyUrl(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LOG.info("[汇潮大额]进入代付回调================================");
        try {
            BufferedReader streamReader = new BufferedReader( new InputStreamReader(req.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());

            LOG.info("[汇潮大额]还款回调参数信息： " + jsonObject.toJSONString());
            String repayState = jsonObject.getString("repayState");
            String dsorderid = jsonObject.getString("outTradeNo");//返回流水号
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
            if("1".equals(repayState)) {
                LOG.info("***********汇潮大额代付交易成功***********************dsorderid==========="+dsorderid);
                RestTemplate restTemplate = new RestTemplate();
//                String version = "35";
////        		String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
//                String url = ip+"/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
//                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//                requestEntity.add("orderCode", dsorderid);
//                requestEntity.add("version", version);
//                String result = null;
//                net.sf.json.JSONObject jsonObjects;
//                net.sf.json.JSONObject resultObj;
//                try {
//                    result = restTemplate.postForObject(url, requestEntity, String.class);
//                    LOG.info("RESULT================" + result);
//                    jsonObjects = net.sf.json.JSONObject.fromObject(result);
//                    resultObj = jsonObjects.getJSONObject("result");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LOG.error("", e);
//                }

//        		url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
                String url = ip+"/v1.0/transactionclear/payment/update";

                MultiValueMap requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", dsorderid);
                //        requestEntity.add("third_code", orderid); // 第三方订单号

                String result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);
                LOG.info("订单已支付!");
            }else {
                LOG.info("支付失败");
                this.addOrderCauseOfFailure(dsorderid, "支付失败", prp.getIpAddress());
            }
            PrintWriter pw = res.getWriter();
            pw.print("success");
            pw.close();
        } catch (Exception e) {
            LOG.info("回调信息异常", e);
            PrintWriter pw = res.getWriter();
            pw.print("success");
            pw.close();
        }
    }

    /**
     * 手动代付接口
     * @param bankCard 代付的储蓄卡号或信用卡号（必须是已开卡的卡号）
     * @param merchantNo 用户在通道注册进件时返回的 商户号
     * @param realAmount 手动代还金额=余额-手续费（如 30.83-0.5=30.33 ）
     * @param extraFee  手续费（如 0.5）
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/transferbymanual")
    public @ResponseBody Object transferByManual(@RequestParam(value = "bankCard") String bankCard,
                                                 @RequestParam(value = "merchantNo") String merchantNo,@RequestParam(value = "realAmount") String realAmount,
                                                 @RequestParam(value = "extraFee", required = true, defaultValue = "0.5") String extraFee)
            throws Exception {
        LOG.info("[汇潮大额]-===============进入手动代付=====================");
        Map<String, String> map = new HashMap<String, String>();
        String fee = new BigDecimal(extraFee).setScale(1).toString();
        String cashAmount = new BigDecimal(realAmount).add(new BigDecimal(fee)).setScale(2).toString();//代付金额
        LOG.info("[汇潮大额]获取到的代付金额：===============" +cashAmount+ "====手续费==="+fee);

        String address = "/api/createMonOrder";
        String repayOrderNum = UUID.randomUUID().toString().replaceAll("-", "");
        LOG.info("[汇潮大额]生成的代付订单号========"+repayOrderNum);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("repayOrderNum", repayOrderNum);//开卡订单号
        signmap.put("fee", fee);//订单单笔手续费 （单位元）
        signmap.put("bankcardNum", bankCard);//开卡成功的卡号
        signmap.put("amount", cashAmount);//金额 元 (到账进额=amount-fee)
        //signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/hchk/settleNotifyUrl");//异步回调地址（不是必穿）

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[汇潮大额]请求参数=="+json);
        LOG.info("[汇潮大额]请求地址=="+URL+address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL+address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[汇潮大额]请求结果=="+result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if(!"0".equals(returnCode)) {
            LOG.info("[汇潮大额]代付请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)) {
            LOG.info("[汇潮大额]代付交易成功========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        }else {
            LOG.info("[汇潮大额]代付交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        }
    }

    /**
     *   商户查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ldquick/queryHCProvince")
    public @ResponseBody Object queryHCMerchant(@RequestParam(value = "province",required = false,defaultValue = "1")String province,
                                                @RequestParam(value = "city",required = false)String city,
                                                @RequestParam(value = "county",required = false)String county) {
        LOG.info("[汇潮大额]开始进行商户查询=======================");
        Map<String, Object> map = new HashMap<>();
        List<LDmerchant> list=ldMerchantBusiness.getHCMerchantProvince();
        if (list == null||list.size()==0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"发送未知错误，请稍后尝试");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put("province",list);
        return map;
    }
    /**
     *   商户查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ld/query/province")
    public @ResponseBody Object queryHCMerchantAll(@RequestParam(value = "province",required = false)String province) {
        LOG.info("[汇潮大额]开始进行商户查询=======================");
        Map<String, Object> map = new HashMap<>();
        if (province==null||"".equals(province)){
            List<HCDMerchant> list=merchantBusiness.getAllProvince();
            if (list == null ||list.size()==0) {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"系统繁忙，请稍后再试");
                return map;
            }else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE,"查询成功");
                map.put(CommonConstants.RESULT,list);
                return map;
            }
        }else {
            LOG.info("province========"+province);
            List<HCDMerchant> list=merchantBusiness.getAllByCity(province);
            if (list == null ||list.size()==0) {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"未查询到省市");
                return map;
            }else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE,"查询成功");
                map.put(CommonConstants.RESULT,list);
                return map;
            }
        }
    }

    /**
     * 获取汇潮大额已进件商户号
     * @author jayden
     * @param text
     * @return
     */
    public static String processMerchantNo(String text) {
//        String text="子商户:阮嘉骏,身份证:310113199203231919,已存在,商户号为:1482306,请勿重复注册...";
        String[] no=text.split("\\:");
        System.out.println(Arrays.toString(no));
        no=no[3].split(",");
        String merchantNo=no[0];
        System.out.println(merchantNo);
        return merchantNo;
    }

    @RequestMapping(method = RequestMethod.GET,value="/v1.0/paymentgateway/quick/process/hcdmerchant")
    public Object processHcdMerchant() throws InterruptedException {
        LOG.info("进入汇潮大额商户池修改");
        Map map=new HashMap<>();
        List<HCDMerchant> hcdMerchants=hcdMerchantBusiness.queryAll();
        LOG.info("商户数量=========="+hcdMerchants.size());
        int count=0;
        for(HCDMerchant data:hcdMerchants){
            if(count%50==0){
                Thread.sleep(1000);
            }
            String address=data.getAddress().substring(0,3);
            data.setProvince(address);
            hcdMerchantBusiness.save(data);
            LOG.info("保存成功=============="+data.toString());
            count++;
        }
        return map;
    }
    @RequestMapping(method = RequestMethod.GET,value="/v1.0/paymentgateway/quick/process/hcdmerchant1")
    public Object processHcdMerchant1(){
        LOG.info("进入汇潮大额商户池修改");
        List<HCDMerchant> hcdMerchants=hcdMerchantBusiness.queryProvince();
        return null;
    }

}
