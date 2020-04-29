//package com.jh.paymentgateway.controller.ypl;
//
//import cn.jh.common.utils.CommonConstants;
//import com.alibaba.fastjson.JSONObject;
//import com.jh.paymentgateway.basechannel.BaseChannel;
//import com.jh.paymentgateway.business.TopupPayChannelBusiness;
//import com.jh.paymentgateway.config.RedisUtil;
//import com.jh.paymentgateway.pojo.PaymentRequestParameter;
//import com.jh.paymentgateway.pojo.ypl.*;
//import com.jh.paymentgateway.util.Util;
//import com.jh.paymentgateway.util.sdk.inlet.InletHelper;
//import com.jh.paymentgateway.util.sdk.inlet.domain.Business;
//import com.jh.paymentgateway.util.sdk.inlet.domain.Contact;
//import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantRequest;
//import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantResponse;
//import com.jh.paymentgateway.util.sdk.pay.PaymentHelper;
//import com.jh.paymentgateway.util.sdk.pay.domain.cashierPay.OrderGoods;
//import com.jh.paymentgateway.util.sdk.pay.domain.cashierPay.OrderInfo;
//import com.jh.paymentgateway.util.sdk.pay.domain.protocol.*;
//import com.jh.paymentgateway.util.sdk.pay.domain.split.SplitInfo;
//import com.jh.paymentgateway.util.sdk.utils.Config;
//import com.jh.paymentgateway.util.sdk.utils.RsaUtils;
//import org.aspectj.lang.annotation.Before;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.net.URISyntaxException;
//import java.net.URLEncoder;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static org.aspectj.runtime.internal.Conversions.longValue;
//
//@Controller
//@EnableAutoConfiguration
//public class YPLpageRequest extends BaseChannel {
//
//    private static final Logger LOG = LoggerFactory.getLogger(YPLpageRequest.class);
//
//    static final String NOTIFY_URL = "http://172.20.18.116:8080/demo/notify";//异步通知结果地址
//
//    @Before("")
//    public void initialize() throws URISyntaxException {
////        Config.initialize(new File(ClassLoader.getSystemResource("config_uat.properties").toURI()));
//        Config.initialize(new File(ClassLoader.getSystemResource("config_test.properties").toURI()));
//        System.setProperty("sdk.mode", "debug");
//    }
//
//
//
//
//    @Value("${payment.ipAddress}")
//    private String ip;
//
//    @Autowired
//    RedisUtil redisUtil;
//
//    @Autowired
//    Util util;
//
//    @Autowired
//    TopupPayChannelBusiness topupPayChannelBusiness;
//    private static final String customerCode = "5651300003039000";
//
//    /**
//     * 注册商户
//     * @param orderCode
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ypl/register")
//    public @ResponseBody
//    Object register(@RequestParam(value = "orderCode") String orderCode) throws Exception {
//        LOG.info("开始获取参数===========");
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String idCard = prp.getIdCard();
//        String cardType = prp.getDebitCardCardType();
//        String bankNo = prp.getDebitCardNo();
//        String amount = prp.getAmount();
//        String cardtype = prp.getCreditCardCardType();
//        String bankName = prp.getDebitBankName();
//        String bankCard = prp.getBankCard();
//        String userName = prp.getUserName();
//        String phone = prp.getPhone();
//        String rate = prp.getRate();
//        String phoneC = prp.getCreditCardPhone();
//        String cardName = prp.getCreditCardBankName();
//        String securityCode = prp.getSecurityCode();
//        String expiredTime = prp.getExpiredTime();
//        String debitCardNo = prp.getDebitCardNo();
//        String phoneD = prp.getDebitPhone();
//        Map<String, Object> map = new HashMap<>();
//        YPLRegister yplRegister = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        DBindCard dBindCard = topupPayChannelBusiness.getDBindCardByDebitCardNo(debitCardNo);
//        CBindCard cBindCard = topupPayChannelBusiness.getCBindCardByBankCard(bankCard);
//
//        YPLRegister register = new YPLRegister();
//
//        if (yplRegister == null) {  //未注册
//            //开始注册
//            LOG.info("ypl开始注册=======================");
//            InletMerchantRequest imr = new InletMerchantRequest();
//            imr.setCustomerCode(customerCode);
//            imr.setVersion("2.0");
//            imr.setName("上海百也特信息科技有限公司");
//            imr.setShortName("百也特");
//            imr.setMobile(phone);//商户手机号必填
//            imr.setType("50");//商户类别,必填，50：小微商户
//            imr.setAreaCode("2916");//归属省市区,必填
//            imr.setLealPersonName(userName);//姓名
//            imr.setLealPersonIdentificationType("0");
//            imr.setLealPersonIdentificationNo(idCard);//身份证   350322198408217715
//            imr.setSettMode("D0");
//            imr.setSettCircle("0");
//            imr.setBankAccountType("2");//结算账户类型1：对公2：对私
//            imr.setSettTarget("0");//结算目标,必填2：结算到易票联账户
//            imr.setNotifyURL("www.baidu.com");//异步通知URL,必填
//            List<Business> businessList = new ArrayList<Business>();
//            businessList.add(new Business("SPLITTED", "20180104", "20991231", "2", "100"));
//            imr.setBusinessList(businessList);//开通的业务列表必填
//            List<Contact> contactList = new ArrayList<Contact>();
//            contactList.add(new Contact(1, imr.getName(), imr.getMobile(), "", "123"));//1084357146@qq.com
//            imr.setContactList(contactList);//联系人信息列表必填
//            LOG.info("请求参数============="+imr);
//            InletMerchantResponse response = InletHelper.addMerchant(imr);
//            System.out.println("返回结果：" + JSONObject.toJSONString(response));
//            String code = response.getReturnCode();
//            if ("0000".equals(code)) {
//                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);//000000
//                map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//                map.put(CommonConstants.RESULT,
//                        ip + "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view?bankName="
//                                + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                                + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
//                                + "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
//                                + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
//                                + "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
//                return map;
//            } else {
//                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//                map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//                return map;
//            }
//        }
//
//        if (!yplRegister.getRate().equals(prp.getRate())) { //费率不同，修改费率
//
//            map = (Map<String, Object>) this.changeRate(rate, idCard);//TODO   参数  注意  还有返回值
//            if ("000000".equals(map.get("resp_code"))) {
//                //跳转到结算卡页面
//                map.put(CommonConstants.RESULT,
//                        ip + "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view?bankName="
//                                + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                                + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
//                                + "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
//                                + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
//                                + "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
//
//                return map;
//            }
//        }
//
//        if (dBindCard == null) {
//            LOG.info("开始进行储蓄卡绑卡===============");
//
//            LOG.info("*******************发起预签约绑卡****************");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESP_MESSAGE, "开始签约绑卡");
//            LOG.info(ip+ "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view?bankName="
//                    + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                    + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
//                    + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
//                    + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
//                    + securityCode + "&ipAddress=" + ip + "&isRegister=1");
//            //跳转到结算卡页面
//            map.put(CommonConstants.RESULT,
//                    ip + "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view?bankName="
//                            + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                            + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
//                            + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
//                            + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
//                            + securityCode + "&ipAddress=" + ip + "&isRegister=1");
//            return map;
//
//        }
//
//
//        if (cBindCard == null) {
//            LOG.info("跳转到绑卡页面============");
//            map.put(CommonConstants.RESULT,
//                    ip + "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view?bankName="
//                            + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                            + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
//                            + "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
//                            + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
//                            + "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
//
//            map.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
//            return map;
//            //  跳转到地区选择页面
//
//        }
//
//        LOG.info("直接发起扣款请求========================");
//        //跳转到商户选择页面
//        LOG.info("******************直接发起扣款请求****************");
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESP_MESSAGE, "直接发起支付");
//        //跳转到结算卡页面
//        map.put(CommonConstants.RESULT,
//                ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
//                        + URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
//                        + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
//                        + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
//                        + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
//                        + securityCode + "&ipAddress=" + ip + "&isRegister=2");
//        return map;
//
//    }
//
//
//    /**
//     * 页面直跳支付界面
//     *
//     * @param orderCode
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/pay-transfer")
//    public @ResponseBody Object PayTransfer(@RequestParam(value = "ordercode") String orderCode) throws IOException {
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String bankCard = prp.getBankCard();
//        String bankName = prp.getCreditCardBankName();
//        String cardtype = prp.getCreditCardCardType();
//        String securityCode = prp.getSecurityCode();
//        String exTime = prp.getExpiredTime();
//        String expiredTime = this.expiredTimeToMMYY(exTime);
//        String nature = prp.getCreditCardNature();
//        Map<String, Object> maps = new HashMap<String, Object>();
//        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        maps.put(CommonConstants.RESP_MESSAGE, "成功");
//        maps.put("redirect_url",
//                ip + "/v1.0/paymentgateway/quick/ypl/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
//                        + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&nature="
//                        + URLEncoder.encode(nature, "UTF-8") + "&bankCard=" + bankCard + "&ordercode=" + orderCode
//                        + "&ipAddress=" + ip + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
//        return maps;
//
//    }
//
//
//    /**
//     * 自选支付页面
//     *
//     * @param request
//     * @param response
//     * @param model
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ypl/pay-view")
//    public String returnHLJCQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//        // 设置编码
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//
//        String ordercode = request.getParameter("ordercode");
//        String bankName = request.getParameter("bankName");
//        String cardType = request.getParameter("cardType");
//        String bankCard = request.getParameter("bankCard");
//        String ipAddress = request.getParameter("ipAddress");
//        String expiredTime = request.getParameter("expiredTime");
//        String securityCode = request.getParameter("securityCode");
//        String nature = request.getParameter("nature");
//
//        model.addAttribute("orderCode", ordercode);
//        model.addAttribute("bankName", bankName);
//        model.addAttribute("cardType", cardType);
//        model.addAttribute("bankCard", bankCard);
//        model.addAttribute("ipAddress", ipAddress);
//        model.addAttribute("ip", "0");
//        model.addAttribute("ips", "0");
//        model.addAttribute("nature", "0");
//        model.addAttribute("phone", "123456");
//        model.addAttribute("expiredTime", expiredTime);
//        model.addAttribute("securityCode", securityCode);
//        model.addAttribute("nature", nature);
//        return "bqxkquickpay";
//    }
//
//
//    /**
//     * 信用卡绑卡页面
//     * @param request
//     * @param response
//     * @param model
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ypl/jump-Receivables_card-view")
//    public String pay(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//        LOG.info("/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view=========toyplbankinfo");
//        String bankName = request.getParameter("bankName");// 结算卡银行名称
//        String bankNo = request.getParameter("bankNo");// 结算卡卡号
//        String amount = request.getParameter("amount");
//        String ordercode = request.getParameter("ordercode");
//        String cardType = request.getParameter("cardType");// 结算卡的卡类型
//        String isRegister = request.getParameter("isRegister");
//        String cardtype = request.getParameter("cardtype");// 信用卡的卡类型
//        String bankCard = request.getParameter("bankCard");// 充值卡卡号
//        String cardName = request.getParameter("cardName");// 充值卡银行名称
//        String expiredTime = request.getParameter("expiredTime");
//        String securityCode = request.getParameter("securityCode");
//        String ipAddress = request.getParameter("ipAddress");
//
//        model.addAttribute("bankName", bankName);
//        model.addAttribute("bankNo", bankNo);
//        model.addAttribute("amount", amount);
//        model.addAttribute("ordercode", ordercode);
//        model.addAttribute("cardType", cardType);
//        model.addAttribute("isRegister", isRegister);
//        model.addAttribute("cardtype", cardtype);
//        model.addAttribute("bankCard", bankCard);
//        model.addAttribute("cardName", cardName);
//        model.addAttribute("expiredTime", expiredTime);
//        model.addAttribute("securityCode", securityCode);
//        model.addAttribute("ipAddress", ipAddress);
//
//        return "yplbankinfo";
//    }
//
//
//
//
//
//
//    /**
//     * 费率修改
//     *
//     * @param rate
//     * @param idCard
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ypl/chageRate")
//    public @ResponseBody
//    Object changeRate(@RequestParam(value = "rate") String rate,
//                      @RequestParam(value = "idCard") String idCard
//    ) throws Exception {
//
//        LOG.info("开始进行费率修改========================");
//
//
//        Map<String, Object> map = new HashMap<>();
//        YPLRegister register = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        register.setRate(rate);
//        topupPayChannelBusiness.createYPLRegister(register);
//        LOG.info("费率修改成功===============");
//
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESP_MESSAGE, "修改费率成功");
//
//        return map;
//    }
//
//    /**
//     * 绑定储蓄卡
//     *
//     * @param idCard
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ypl/bindingDBindCard")
//    public @ResponseBody
//    Object bindingDBindCard(@RequestParam(value = "idCard") String idCard,
//                            @RequestParam(value = "userName") String username,
//                            @RequestParam(value = "debitCardNo") String debitCardNo,
//                            @RequestParam(value = "phoneD") String phoneD,
//                            @RequestParam(value = "phoneC") String phoneC,
//                            @RequestParam(value = "bankCard") String bankCard,
//                            @RequestParam(value = "orderCode") String orderCode,
//                            @RequestParam(value = "securityCode") String securityCode,
//                            @RequestParam(value = "expiredTime") String expiredTime,
//                            @RequestParam(value = "cardName") String cardName
//    ) throws Exception {
//
//        LOG.info("开始绑定储蓄卡=================");
//        String mchtOrderNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); //交易编号,商户侧唯一
////        String customerCode = "";
////        long payAmount = 1;
////        String payCurrency = "CNY";
////        String notifyUrl = NOTIFY_URL+"/WithdrawalForSubMerchant";
////        String remark = "";
//        Map<String, Object> map = new HashMap<>();
//
//        YPLRegister yplRegister = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        String memberId = yplRegister.getMemberId();//子商户号
//        String publicKeyPath = Config.getPublicKeyFile().getAbsolutePath();
//
//        String userName = RsaUtils.encryptByPublicKey(username, RsaUtils.getPublicKey(publicKeyPath));////这里是门户中法人的名字，不是商家名字  我们对子商户没限定，可以是假的
//        String certificatesNo = RsaUtils.encryptByPublicKey(idCard, RsaUtils.getPublicKey(publicKeyPath));//联调贷记卡身份证
//        String bankCardNo = RsaUtils.encryptByPublicKey(debitCardNo, RsaUtils.getPublicKey(publicKeyPath));//贷记卡卡号
//        String phoneNum = RsaUtils.encryptByPublicKey(phoneD, RsaUtils.getPublicKey(publicKeyPath));//贷记卡手机号
//        //    String cvn = RsaUtils.encryptByPublicKey("331", RsaUtils.getPublicKey(publicKeyPath));
//        //2018年2月
//        // String expired = RsaUtils.encryptByPublicKey("2404", RsaUtils.getPublicKey(publicKeyPath));// 信用卡必填 yymm
//        ProtocolPayBindCardRequest request = new ProtocolPayBindCardRequest();
//        request.setCustomerCode(Config.getCustomerCode());
//        request.setVersion("2.0");
//        request.setMemberId(memberId);//子商户号ID  签约保存
//        request.setMchtOrderNo(mchtOrderNo);
//        request.setPhoneNum(phoneNum);//手机号
//        request.setUserName(userName);//持卡人姓名
//        request.setBankCardNo(bankCardNo);//银行卡
//        request.setBankCardType("debit");//debit:借记卡,credit:贷记卡;
//        // request.setCvn(cvn);//cvn  卡背后三位数  信用卡必填
//        // request.setExpired(expired);//卡有效期   信用卡必填  yymm
//        request.setCertificatesNo(certificatesNo);//身份证号
//        request.setCertificatesType("01");//
//        request.setNonceStr(UUID.randomUUID().toString().replaceAll("-", ""));
//        request.setIsSendIssuer(true);//是否上送到发卡行签约  无卡才上送
//        request.setBusinessCategory("efpsNocardService");//无卡产品
//
//
//        ProtocolPayBindCardResponse response = PaymentHelper.bindCard(request);
//
//        //这是打印出来的结果  可打印全部或部分返回的结果。
//        LOG.info("交易结果：" + JSONObject.toJSONString(response));
//        LOG.info("SmsNo：" + response.getSmsNo());
////        System.out.println("Protocol："  + response.getProtocol());
//
//        //   {"customerCode":"5651300003039000","memberId":"5651300003066231","returnCode":"0000","returnMsg":"Success","smsNo":"QY201907251532303054632"}
//        //  SmsNo：QY201907251532303054632
//        String code = response.getReturnCode();
//
//        if ("0000".equals(code)) {
//            LOG.info("储蓄卡绑卡成功============="); //  =======>跳转绑卡页面
//            //保存储蓄卡信息
//            DBindCard dBindCard = new DBindCard();
//            dBindCard.setCreateTime(new Date());
//            dBindCard.setDebitCardNo(debitCardNo);
//            dBindCard.setIdCard(idCard);
//            dBindCard.setPhone(phoneD);
//            dBindCard.setUserName(userName);
//
//            topupPayChannelBusiness.createDBindCard(dBindCard);
//
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESULT,
//                    ip + "/v1.0/paymentgateway/topup/ypl/toConfirmBindCard?bankCard=" + bankCard + "&userName="
//                            + userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
//                            + "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
//                            + "&ipAddress=" + ip);
//
//            map.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
//            return map;
//        } else {
//            LOG.info("储蓄卡绑卡失败============");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//            return map;
//        }
//
//    }
//
//    /**
//     * 储蓄卡绑卡页面
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view")
//    public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//        LOG.info("/v1.0/paymentgateway/quick/ypl/jump-Receivablescard-view=========toyplbankinfo");
//        String bankName = request.getParameter("bankName");// 结算卡银行名称
//        String bankNo = request.getParameter("bankNo");// 结算卡卡号
//        String amount = request.getParameter("amount");
//        String ordercode = request.getParameter("ordercode");
//        String cardType = request.getParameter("cardType");// 结算卡的卡类型
//        String isRegister = request.getParameter("isRegister");
//        String cardtype = request.getParameter("cardtype");// 信用卡的卡类型
//        String bankCard = request.getParameter("bankCard");// 充值卡卡号
//        String cardName = request.getParameter("cardName");// 充值卡银行名称
//        String expiredTime = request.getParameter("expiredTime");
//        String securityCode = request.getParameter("securityCode");
//        String ipAddress = request.getParameter("ipAddress");
//
//        model.addAttribute("bankName", bankName);
//        model.addAttribute("bankNo", bankNo);
//        model.addAttribute("amount", amount);
//        model.addAttribute("ordercode", ordercode);
//        model.addAttribute("cardType", cardType);
//        model.addAttribute("isRegister", isRegister);
//        model.addAttribute("cardtype", cardtype);
//        model.addAttribute("bankCard", bankCard);
//        model.addAttribute("cardName", cardName);
//        model.addAttribute("expiredTime", expiredTime);
//        model.addAttribute("securityCode", securityCode);
//        model.addAttribute("ipAddress", ipAddress);
//
//        return "yplbankinfo";
//    }
//
//
//    /**
//     * 页面直跳信用卡绑卡界面
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/transfer")
//    public @ResponseBody
//    Object Transfer(@RequestParam(value = "orderCode") String orderCode,
//                    @RequestParam(value = "expiredTime") String expiredTime,
//                    @RequestParam(value = "securityCode") String securityCode,
//                    @RequestParam(value = "ipAddress") String ipAddress) throws IOException {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESP_MESSAGE, "成功");
//        map.put("redirect_url", ip + "/v1.0/paymentgateway/quick/ypl/jump-bindcard-view?ordercode=" + orderCode
//                + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
//        return map;
//    }
//
//
//
//    /**
//     * 跳转到信用卡绑卡绑卡页面
//     */
//    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/bqx/jump-bindcard-view")
//    public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
//            throws IOException {
//
//        request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html;charset=utf-8");
//        LOG.info("/v1.0/paymentgateway/quick/bqx/jump-bindcard-view=========tobqxbindcard");
//        String ordercode = request.getParameter("ordercode");
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);
//        String bankCard = prp.getBankCard();
//        String bankName = prp.getCreditCardBankName();
//        String cardType = prp.getCreditCardCardType();
//        String expiredTime = request.getParameter("expiredTime");
//        String securityCode = request.getParameter("securityCode");
//
//
//
//        model.addAttribute("ordercode", ordercode);
//        model.addAttribute("expiredTime", expiredTime);
//        model.addAttribute("securityCode", securityCode);
//        model.addAttribute("bankName", bankName);
//        model.addAttribute("cardType", cardType);
//        model.addAttribute("bankCard", bankCard);
//        model.addAttribute("ipAddress", ip);
//        return "bqxbindcard";
//    }
//
//    /**
//     * 信用卡绑定
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ypl/bindingCBindCard")
//    public @ResponseBody
//    Object bindingCBindCard(
//                            @RequestParam(value = "orderCode") String orderCode,
//                            @RequestParam(value = "securityCode") String securityCode,
//                            @RequestParam(value = "expiredTime") String expiredTime
//    ) throws Exception {
//        LOG.info("开始绑定信用卡=====================");
//        Map<String, Object> map = new HashMap<>();
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String idCard = prp.getIdCard();
//
//        YPLRegister yplRegister = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        String memberId = yplRegister.getMemberId();
//
//
//        String mchtOrderNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); //交易编号,商户侧唯一
//        String publicKeyPath = Config.getPublicKeyFile().getAbsolutePath();
//
//        String userName = RsaUtils.encryptByPublicKey(prp.getUserName(), RsaUtils.getPublicKey(publicKeyPath));////这里是门户中法人的名字，不是商家名字  我们对子商户没限定，可以是假的
//        String certificatesNo = RsaUtils.encryptByPublicKey(idCard, RsaUtils.getPublicKey(publicKeyPath));//联调贷记卡身份证
//        String bankCardNo = RsaUtils.encryptByPublicKey(prp.getBankCard(), RsaUtils.getPublicKey(publicKeyPath));//贷记卡卡号
//        String phoneNum = RsaUtils.encryptByPublicKey(prp.getCreditCardPhone(), RsaUtils.getPublicKey(publicKeyPath));//贷记卡手机号
//        String cvn = RsaUtils.encryptByPublicKey(securityCode, RsaUtils.getPublicKey(publicKeyPath));
//        //2018年2月
//        String expired = RsaUtils.encryptByPublicKey(expiredTime, RsaUtils.getPublicKey(publicKeyPath));// 信用卡必填 yymm
//        ProtocolPayBindCardRequest request = new ProtocolPayBindCardRequest();
//        request.setCustomerCode(Config.getCustomerCode());
//        request.setVersion("2.0");
//        request.setMemberId(memberId);//子商户号ID  签约保存
//        request.setMchtOrderNo(mchtOrderNo);
//        request.setPhoneNum(phoneNum);//手机号
//        request.setUserName(userName);//持卡人姓名
//        request.setBankCardNo(bankCardNo);//银行卡
//        request.setBankCardType("credit");//debit:借记卡,credit:贷记卡;
//        request.setCvn(cvn);//cvn  卡背后三位数  信用卡必填
//        request.setExpired(expired);//卡有效期   信用卡必填  yymm
//        request.setCertificatesNo(certificatesNo);//身份证号
//        request.setCertificatesType("01");//
//        request.setNonceStr(UUID.randomUUID().toString().replaceAll("-", ""));
//        request.setIsSendIssuer(true);//是否上送到发卡行签约  无卡才上送
//        request.setBusinessCategory("efpsNocardService");//无卡产品
//        ProtocolPayBindCardResponse response = PaymentHelper.bindCard(request);
//        //这是打印出来的结果  可打印全部或部分返回的结果。
//        LOG.info("交易结果：" + JSONObject.toJSONString(response));
//        LOG.info("SmsNo：" + response.getSmsNo());
//        String code = response.getReturnCode();
//        String smsNo = response.getSmsNo();
//        if ("0000".equals(code)) {
//            LOG.info("信用卡绑卡接口成功============");
//            //本地保存信用卡信息
//            CBindCard cBindCard = new CBindCard();
//            cBindCard.setBankCard(prp.getBankCard());
//            cBindCard.setExpiredTime(expiredTime);
//            cBindCard.setIdCard(idCard);
//            cBindCard.setUserName(prp.getUserName());
//            cBindCard.setPhone(prp.getCreditCardPhone());
//            cBindCard.setSmsNo(smsNo);
//            topupPayChannelBusiness.createCBindCard(cBindCard);
//
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//            return map;
//        } else {
//            LOG.info("信用卡绑卡失败========================");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//            return map;
//
//        }
//
//        // {"customerCode":"5651300003039000","memberId":"5651300003066231","returnCode":"0000","returnMsg":"Success","smsNo":"QY201907251530213054631"}
//        // SmsNo：QY201907251530213054631
//
//    }
//
//
//    /**
//     * 页面直跳商户选择页面
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/merchant")
//    public @ResponseBody
//    Object merchant(@RequestParam(value = "orderCode") String orderCode,
//                    @RequestParam(value = "expiredTime") String expiredTime,
//                    @RequestParam(value = "securityCode") String securityCode,
//                    @RequestParam(value = "ipAddress") String ipAddress) throws IOException {
//        Map<String, Object> maps = new HashMap<String, Object>();
//        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        maps.put(CommonConstants.RESP_MESSAGE, "成功");
//        maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/ypl/jump-merchant-view?ordercode=" + orderCode
//                + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
//        return maps;
//    }
//
//
//
//
//
//
//
//
//
//    /**
//     * 查询子商户
//     *
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/QueryMerchant")
//    public @ResponseBody
//    Object queryMerchant(@RequestParam(value = "orderCode") String orderCode,
//                         @RequestParam(value = "idCard") String idCard,
//                         @RequestParam(value = "areaCode") String areaCode,
//                         @RequestParam(value = "mccCode") String mccCode
//    ) throws Exception {
//        LOG.info("开始查询商户=========================");
//        LOG.info("orderCode="+orderCode+"====idCard="+idCard+"===areaCode="+areaCode+"===mccCode=="+mccCode);
//        Map<String, Object> map = new HashMap<>();
//
//        YPLRegister yplRegister = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        String memberId = yplRegister.getMemberId();//5651300003066231
//        ProtocolQueryConsumerRequest request = new ProtocolQueryConsumerRequest();
//        request.setCustomerCode(customerCode);
//        request.setVersion("2.0");
//        request.setMemberId(memberId);
//        Date date = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("yyyymmddhhmmss");
//        String tradeNo = format.format(date);
//        request.setOutTradeNo(tradeNo);
//        request.setAreaCode(areaCode);
//        request.setMccCode("");
//        request.setNonceStr(UUID.randomUUID().toString().replaceAll("-", ""));
//        LOG.info("子商户查询接口请求参数==" + request.toString());
//        ProtocolQueryCunsumerResponse response = PaymentHelper.queryConsumer(request);
//        LOG.info("返回参数============" + response);
//        String code = response.getReturnCode();
//        if ("0000".equals(code)) {
//            LOG.info("查询子商户成功======================");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESULT, response.getSubCodeList());
//            LOG.info("商品列表========"+response.getSubCodeList());
//            map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//            return map;
//        } else {
//            LOG.info("查询子商户失败=====================");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, "查询子商户失败");
//            return map;
//        }
//    }
//
//
//    /**
//     * 交易接口
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/transaction")
//    public @ResponseBody
//    Object transaction(@RequestParam(value = "orderCode") String orderCode,
//                       @RequestParam(value = "commodity") String commodity
//
//    ) throws Exception {
//        LOG.info("开始ypl交易====================================");
//        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
//        String amount = prp.getAmount();
//        String realAmount = prp.getRealAmount();
//        String idCard = prp.getIdCard();
//        String bankCard = prp.getBankCard();
//        Map<String, Object> map = new HashMap<>();
//        BigDecimal bigAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0);
//        BigDecimal bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0);
//        String outTradeNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); //交易编号,商户侧唯一
//        String clientIp = prp.getIpAddress(); //IP
//        long payAmount = longValue(bigRealAmount);//支付金额,分为单位
//        String payCurrency = "CNY"; //币种，写死
//        String attachData = "attachData"; //备注数据,可空
//        String transactionStartTime = Config.getTransactionStartTime(); //交易开始时间
//        String transactionEndTime = ""; //交易结束时间
//        OrderInfo orderInfo = new OrderInfo();
//        orderInfo.setId("test");
//        orderInfo.setBusinessType("test");
//        orderInfo.addGood(new OrderGoods(commodity, "1", longValue(bigRealAmount)));
//        //orderInfo.addGood(new OrderGoods("82年的茅台", "1瓶", 1));
//        ProtocolPayRequest request = new ProtocolPayRequest();
//        request.setVersion("2.0");
//        YPLRegister yplRegister = topupPayChannelBusiness.getYPLRegisterByIdCard(idCard);
//        String memberId = yplRegister.getMemberId();
//        request.setMemberId(memberId);//会员号
//        request.setOutTradeNo(outTradeNo);
//        CBindCard cBindCard = topupPayChannelBusiness.getCBindCardByBankCard(bankCard);
//        String smsNo = cBindCard.getSmsNo();
//        String smsCode = cBindCard.getSmsCode();
//        request.setSmsNo(smsNo);
//        request.setSmsCode(smsCode);
//        request.setOrderInfo(orderInfo);
//        request.setPayAmount(payAmount);//单位  分
//        request.setPayCurrency(payCurrency);
//        request.setTransactionStartTime(transactionStartTime);
//        request.setNeedSplit(true);
//        List<SplitInfo> splitInfoList = new ArrayList<SplitInfo>();
//        splitInfoList.add(new SplitInfo(customerCode, longValue(bigRealAmount), 1));
//      //  splitInfoList.add(new SplitInfo(customerCode, 1, 0));
//        request.setSplitInfoList(splitInfoList);
//        request.setSplitNotifyUrl("http://www.baidu.com");
//        request.setSplitAttachData("");
//        request.setNonceStr(UUID.randomUUID().toString().replaceAll("-", ""));
//        LOG.info("交易接口请求参数=========" + request);
//        ProtocolPayResponse response = (ProtocolPayResponse) PaymentHelper.protocolPayPre(request);
//        LOG.info("交易结果：" + JSONObject.toJSONString(response));
//        LOG.info("token：" + response.getToken());
//        String code = response.getReturnCode();
//        if ("0000".equals(code)) {
//            LOG.info("交易处理成功=================");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESP_MESSAGE, response.getReturnMsg());
//            return map;
//        } else {
//            LOG.info("交易处理失败=======================");
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, "交易处理失败");
//            return map;
//        }
//    }
//
//
//
//    /**
//     * 页面加载查询第一级城市列表
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ypl/queryAddress")
//    public @ResponseBody
//    Object queryAddress(@RequestParam(value = "parentId") String parentId
//
//    ) throws IOException {
//
//        SimpleDateFormat format= new SimpleDateFormat("yyyyMMddHHmmss");
//        String  timeStamp= format.format(new Date());
//
//
//        if ("1".equals(parentId)) {
//            LOG.info("查询第一级城市信息================"+timeStamp);
//        } else {
//            LOG.info("开始查询下一级城市目录=========================="+timeStamp);
//
//        }
//
//        Map<String, Object> map = new HashMap<>();
//
//        List<YPLAddress> list = topupPayChannelBusiness.getYPLAddressByParentId(parentId);
//        LOG.info("城市信息==========" + list.toString());
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESULT, list);
//        map.put(CommonConstants.RESP_MESSAGE, "成功");
//        return map;
//
//    }
//
//    /**
//     * 查询所有的MCC
//     */
//    @RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/ypl/queryMCC"))
//    public @ResponseBody
//    Object queryMCC(@RequestParam(value = "parent") String parent) {
//        SimpleDateFormat format= new SimpleDateFormat("yyyyMMddHHmmss");
//        String  timeStamp= format.format(new Date());
//        if("0".equals(parent)){
//            LOG.info("开始查询商户类型========"+timeStamp);
//        }else {
//            LOG.info("开始查询商户========"+timeStamp);
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        List<YPLMCC> list = topupPayChannelBusiness.getYPLMCCByParent(parent);
//
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESULT, list);
//        map.put(CommonConstants.RESP_MESSAGE, "成功");
//        return map;
//    }
//
//    /**
//     * 保存短信验证码
//     *
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/ypl/saveSmsCode"))
//    public @ResponseBody
//    Object saveSmsCode(@RequestParam(value = "smsCode") String smsCode,
//                       @RequestParam(value = "bankCard") String bankCard
//    ) throws Exception {
//        LOG.info("开始保存短信验证码====================");
//        Map<String, Object> map = new HashMap<>();
//        CBindCard bindCard = topupPayChannelBusiness.getCBindCardByBankCard(bankCard);
//        bindCard.setSmsCode(smsCode);
//        topupPayChannelBusiness.createCBindCard(bindCard);
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESP_MESSAGE, "短信保存成功");
//        return map;
//
//    }
//
//
//
//    /**
//     * 将日期格式mmyy转化为yymm
//     *
//     * @param mmyy
//     * @return
//     */
//    public static String mmyyTOyymm(String mmyy) {
//        String mm = mmyy.substring(0, 2);
//        String yy = mmyy.substring(2, 4);
//        return yy + mm;
//    }
//
//
//
//}
