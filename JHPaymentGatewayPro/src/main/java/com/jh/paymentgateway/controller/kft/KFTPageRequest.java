package com.jh.paymentgateway.controller.kft;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.RandomUtils;
import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.LMTAddress;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.kft.KFTRegister;
import com.lycheepay.gateway.client.GBPService;
import com.lycheepay.gateway.client.GatewayClientException;
import com.lycheepay.gateway.client.KftService;
import com.lycheepay.gateway.client.dto.base.BaseReqParameters;
import com.lycheepay.gateway.client.dto.gbp.*;
import com.lycheepay.gateway.client.dto.gbp.sameID.*;
import org.apache.commons.lang3.StringUtils;
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
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description 快付通
 */

@Controller
@EnableAutoConfiguration
public class KFTPageRequest extends BaseChannel {
    private static final Logger logger = LoggerFactory.getLogger(KFTPageRequest.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GBPService gbpService;

    @Autowired
    private KftService kftService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Value("${payment.ipAddress}")
    private String ip;

    @Value("${kft.merchantId}")
    private String merchantId;//2019032100098370

    @Value("${kft.version}")
    private String version;//1.0.0-IEST

    private static final String REQ_NO_PREFIX = "kft_gbp";

/**
     * 验证三要素接口名称
     */

    private static final String VERIFY_THREE_MESSAGE_SERVICE_NAME = "gbp_threeMessage_verification";

/**
     * 验证三要素产品编号
     */

    private static final String VERIFY_THREE_MESSAGE_PRODUCT_NO = "GBP00001";

/**
     * 快捷协议代扣协议申请(快捷协议代扣步骤1)服务名
     */

    private static final String TREATY_COLLECT_APPLY_SERVICE_NAME = "gbp_treaty_collect_apply";

/**
     * 快捷协议代扣协议确定(快捷协议代扣步骤2)服务名
     */

    private static final String CONFIRM_TREATY_COLLECT_APPLY_SERVICE_NAME = "gbp_confirm_treaty_collect_apply";


    private static final String EXCUTE_TREATY_COLLECT_SERICE_NAME = "gbp_same_id_credit_card_treaty_collect";


    private static final String EXCUTE_TREATY_COLLECT_PRODUCT_NO = "GBPTM003";



public void destory() throws Exception {
        GBPService.destory();
    }



    //业务流程走向1.判断进件2绑卡3

/**
     * 设置通用参数
     */

    public void setCommonParam(BaseReqParameters dto, String codeNo) {
        logger.info("REQ_NO_PREFIX={},merchantId={},version={}",REQ_NO_PREFIX,merchantId,version);
        dto.setReqNo(REQ_NO_PREFIX + System.currentTimeMillis());// 商户可以根据此参数来匹配请求和响应信息；快付通将此参数原封不动的返回给商户
        dto.setReqNo(codeNo);
        dto.setMerchantId(merchantId);// 替换成快付通提供的商户ID，测试生产不一样
        dto.setVersion(version);// 接口版本号，测试:1.0.0-IEST,生产:1.0.0-PRD
    }


/**
     * 跳转到账卡页面
     */

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kft/toPayPage")
    public Object toPayPage(@RequestParam(value = "orderCode") String orderCode)
            throws IOException {
        logger.info("跳转到账卡页面-----------------");
        Map<String, Object> maps = new HashMap<String, Object>();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String cardNo = prp.getDebitCardNo();
        String amount = prp.getAmount();
        String bankName = prp.getDebitBankName();
        String cardName = prp.getCreditCardBankName();
        String rip = prp.getIpAddress();


         if (cardName.contains("交通")) {

            if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) < 0) {

                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, "交通银行卡交易金额限制为4000-20000以内,请核对重新输入金额!");

                this.addOrderCauseOfFailure(orderCode, "交通银行卡交易金额限制为4000-20000以内,请核对重新输入金额!", rip);

                return maps;

            }
         } else if (cardName.contains("邮政")) {
            if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) < 0) {

                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为4000-20000以内,请核对重新输入金额!");

                this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为4000-20000以内,请核对重新输入金额!", rip);

                return maps;

            }
         }
   /* else if (cardName.contains("招商")) {
            if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) < 0) {

                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, "招商银行卡交易金额限制为4000-20000以内,请核对重新输入金额!");

                this.addOrderCauseOfFailure(orderCode, "招商银行卡交易金额限制为4000-20000以内,请核对重新输入金额!", rip);

                return maps;
            }
    }*/


        maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/kft/toHtml?ordercode=" + orderCode
                + "&bankCard=" + cardNo
                + "&amount=" + amount
                + "&bankName=" + URLEncoder.encode(bankName, "UTF-8")
                + "&ipAddress=" + ip);
        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE, "跳转到到账卡页面");

        return maps;
    }




/**
     * 省市区页面跳转到交易中转接口
     *
     * @param
     * @param
     * @return
     * @throws
     * @throws IOException
     */

    @RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/kft/to/pay-view"))
    public @ResponseBody Object toPayView(@RequestParam(value = "orderCode") String orderCode,
                                          @RequestParam(value = "provinceOfBank") String provinceOfBank,
                                          @RequestParam(value = "cityOfBank") String cityOfBank,
                                          @RequestParam(value = "areaOfBank") String areaOfBank,
                                          @RequestParam(value = "provinceCode") String provinceCode,
                                          @RequestParam(value = "cityCode") String cityCode,
                                          @RequestParam(value = "areaCode") String areaCode) throws Exception {
        logger.info("kft:orderCode------------------------：" + orderCode+","+provinceOfBank+","+cityOfBank+","+areaOfBank+","+provinceCode+","+cityCode+","+areaCode);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String phoneC = prp.getCreditCardPhone();
        String amount = prp.getAmount();
        String cardNo = prp.getDebitCardNo();
        String bankName = prp.getDebitBankName();

        Map<String, Object> maps = new HashMap<>();

        maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        maps.put(CommonConstants.RESP_MESSAGE, "直接跳转交易页面");
        maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/kft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
                + "&bankCard=" + cardNo
                + "&orderCode=" + orderCode
                + "&ipAddress=" + ip
                + "&ips=" + prp.getIpAddress()
                + "&phone=" + phoneC
                + "&provinceOfBank=" + URLEncoder.encode(provinceOfBank, "UTF-8")
                + "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8")
                + "&areaOfBank=" + URLEncoder.encode(areaOfBank, "UTF-8")
                + "&provinceCode=" + provinceCode
                + "&cityCode=" + cityCode
                + "&areaCode=" + areaCode
                + "&amount=" + amount + "&isRegister=1");
        return maps;
    }


/**
     * 跳转到省市区选择页面
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kft/toHtml")
    public String toHtml(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        logger.info("kftPay------------------跳转到省市区选择页面");

        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String orderCode = request.getParameter("ordercode");
        String ipAddress = request.getParameter("ipAddress");

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("ipAddress", ipAddress);

        return "kftlinkage";
    }


/**
     * 跳转到交易界面
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kft/pay-view")
    public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        logger.info("kftPay------------------跳转到交易界面");

        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        //
        String ordercode = request.getParameter("orderCode");
        String bankName = request.getParameter("bankName");
        String bankCard = request.getParameter("bankCard");
        String ipAddress = request.getParameter("ipAddress");
        String phone = request.getParameter("phone");
        String ips = request.getParameter("ips");
        String amount = request.getParameter("amount");
        String provinceOfBank = request.getParameter("provinceOfBank");
        String cityOfBank = request.getParameter("cityOfBank");
        String areaOfBank = request.getParameter("areaOfBank");
        String provinceCode = request.getParameter("provinceCode");
        String cityCode = request.getParameter("cityCode");
        String areaCode = request.getParameter("areaCode");

        model.addAttribute("ordercode", ordercode);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("phone", phone);
        model.addAttribute("ips", ips);
        model.addAttribute("amount", amount);
        model.addAttribute("provinceOfBank", provinceOfBank);
        model.addAttribute("cityOfBank", cityOfBank);
        model.addAttribute("areaOfBank", areaOfBank);
        model.addAttribute("provinceCode", provinceCode);
        model.addAttribute("cityCode", cityCode);
        model.addAttribute("areaCode", areaCode);

        return "kftpay";
    }


/**
     * 跳转到交易成功中转页面
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kft/paysuccess-view")
    public String paysuccessView(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        logger.info("kftPay------------------跳转到交易成功中转页面");

        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String ordercode = request.getParameter("orderCode");
        String bankName = request.getParameter("bankName");
        String bankCard = request.getParameter("bankCard");
        String ipAddress = request.getParameter("ipAddress");
        String amount = request.getParameter("amount");
        String ips = request.getParameter("ips");
        String realamount = request.getParameter("realamount");

        model.addAttribute("orderCode", ordercode);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("amount", amount);
        model.addAttribute("realAmount", realamount);
        model.addAttribute("ips", ips);

        return "kftpaysuccess";
    }


/**
     * 根据市id查询该市所有的区
     *
     * @param
     * @param
     * @return
     * @throws IOException
     */

    @RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/kft/area/queryall"))
    public @ResponseBody Object findArea(@RequestParam(value = "cityId") String cityId) {
        logger.info("cityId------------------------：" + cityId);
        Map map = new HashMap();
        List<LMTAddress> list = topupPayChannelBusiness.findLMTCityByCityId(cityId);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

/**
     * 根据省份id查询该省份所有的市
     *
     * @param
     * @param
     * @return
     * @throws IOException
     */

    @RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/kft/city/queryall"))
    public @ResponseBody Object findCity(@RequestParam(value = "provinceId") String provinceId) {
        logger.info("provinceid---------------------：" + provinceId);
        Map map = new HashMap();
        List<LMTAddress> list = topupPayChannelBusiness.findLMTCityByProvinceId(provinceId);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }


/**
     * 查询所有省/直辖市/自治区
     *
     * @param
     * @param
     * @return
     * @throws IOException
     */

    @RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/kft/province/queryall"))
    public @ResponseBody Object findProvince() {
        Map map = new HashMap();
        List<LMTAddress> list = topupPayChannelBusiness.findLMTProvince();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }





/* *
     中转页面 不选择地区
     /
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/kft/jump/pay")
    public String jumpPay(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        logger.info("跳转到到账卡页面-----------------");

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String ordercode = request.getParameter("ordercode");
        String ipAddress = request.getParameter("ipAddress");
        String bankName = request.getParameter("bankName");
        String bankCard = request.getParameter("bankCard");
        String amount = request.getParameter("amount");

        model.addAttribute("ordercode", ordercode);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("amount", amount);
        model.addAttribute("ipAddress", ipAddress);

        return "kftpaymessage";
    }*/


    private String yuan2Fen(String yuan) {
        BigDecimal fenBd = new BigDecimal(yuan).multiply(new BigDecimal(100));
        fenBd = fenBd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return fenBd.toString();
    }


    @RequestMapping(value = "/v1.0/paymentgateway/quick/kft/place-order-sms-test", method = RequestMethod.POST)
    @ResponseBody
    public Object excuteSmsCollectTest(@RequestParam String orderNo) throws Exception {
        SameIDCreditCardCollectFromBankAccountDTO dto = new SameIDCreditCardCollectFromBankAccountDTO();
        logger.info("进入测试方法place-order-sms-test,orderNo:{}", orderNo);
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_sms_collect");// 接口名称，固定不变
        dto.setProductNo("GBP00003");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一 Kfbp15242214177991
        dto.setTradeName("同名卡快捷代扣测试");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount("100000");// 单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档
        dto.setCustBankNo("1051000");// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo("62123456789123456");// 本次交易中,从客户的哪张卡上扣钱
        dto.setCustBindPhoneNo("17630399122");// 银行绑定手机号 17630399122   15806578880
        dto.setCustName("张三");// 付款人的真实姓名
        dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit("2");// 客户账户借记贷记类型,0存折 1借记 2贷记
        dto.setCustCardValidDate("0115");//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        dto.setCustCardCvv2("375");//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        // dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
        dto.setCustCertificationType("0");// 客户证件类型,目前只支持身份证,详情请看文档
        dto.setCustID("44123456785454549");// 证件号码
        dto.setRemark("短信验证");// 商户可额外填写付款方备注信息,此信息会传给银行,会在银行的账单信息中显示(具体如何显示取决于银行方,快付通不保证银行肯定能显示)
        dto.setRateAmount("400");// 手续费
        dto.setNotifyUrl("http://106.15.56.208/v1.0/paymentgateway/topup/kft/fastpay/call-back");//回调地址
        logger.info("请求参数:" + dto);
        logger.info("gbpService============"+gbpService);
        SameIDCreditCardTradeResultDTO result = gbpService
                .sameIDCreditCardSmsCollect(dto);
        logger.info("响应结果:" + result);
        //destory();
        return result;
    }


/**
     * @Author zhangchaofeng
     * @Description 快捷代扣步骤一，发送短信
     * @Date 17:01 2019/4/22
     * @Param [orderCode]
     * @return java.lang.Object
     **/

    @RequestMapping(value = "/v1.0/paymentgateway/quick/kft/place-order-sms", method = RequestMethod.POST)
    @ResponseBody
    public Object excuteSmsCollect(@RequestParam(value = "ordercode") String orderCode, @RequestParam(value = "cityOfBank",required = false) String cityOfBank, @RequestParam(value = "areaOfBank", required = false) String areaOfBank) throws Exception {
        logger.info("快捷代扣步骤一，发送短信，orderCode={},cityCode={}",orderCode,cityOfBank);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> maps = new HashMap<String, Object>();
        String rip = prp.getIpAddress();
        String debitBankNo = prp.getDebitCardNo();
        String bankCard = prp.getBankCard();
        String amount = prp.getAmount();
        String idCard = prp.getIdCard();
        String phoneC = prp.getCreditCardPhone();
        String userName = prp.getUserName();
        String cvn2 = prp.getSecurityCode();
        String ExpiredTime = prp.getExpiredTime();
        String expiredTime = this.expiredTimeToMMYY(ExpiredTime);
        String realAmount = prp.getRealAmount();
        String orderNo = prp.getOrderCode();
        String rateAmount = prp.getExtraFee();
        String creditCardBankName = prp.getCreditCardBankName();

        //算出总得手续费
        BigDecimal amountBig = new BigDecimal(amount);
        BigDecimal realAmountBig = new BigDecimal(realAmount);
        BigDecimal handlingFeeBig = amountBig.subtract(realAmountBig).subtract(new BigDecimal("0.5")); //少扣5毛，一会代付的时候扣

        String handlingFee = yuan2Fen(handlingFeeBig.toString());

        logger.info("/v1.0/paymentgateway/quick/kft/place-order-sms:======prp-"+prp.toString());

        SameIDCreditCardCollectFromBankAccountDTO dto = new SameIDCreditCardCollectFromBankAccountDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_sms_collect");// 接口名称，固定不变
        dto.setProductNo("GBP00003");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一
        dto.setTradeName("快付通同名卡快捷代扣测试");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(yuan2Fen(amount));// 单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(creditCardBankName);
        if(kftBankCodeByName == null && kftBankCodeByName.size()==0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "没匹配到扣款卡信息");
            return maps;
        }
        String cityCode;
        for (String bankCode:kftBankCodeByName){
            logger.info("快付通银行代号：{}",bankCode);
        }
        if(StringUtils.equals(cityOfBank,"市辖区")||StringUtils.equals(cityOfBank,"市")){
            cityCode = topupPayChannelBusiness.findKFTCityCodeByProvinceAndCityName(areaOfBank);
        }else {
            cityCode = topupPayChannelBusiness.findKFTCityCodeByProvinceAndCityName(cityOfBank);
        }
        if(StringUtils.isBlank(cityCode)){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "匹配城市代号失败");
            logger.info("快付通匹配城市代号失败,cityName={},areaName={}",cityOfBank,areaOfBank);
        }
        dto.setCityCode(cityCode);
        dto.setCustBankNo(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo(bankCard);// 本次交易中,从客户的哪张卡上扣钱
        dto.setCustBindPhoneNo(phoneC);// 银行绑定手机号
        dto.setCustName(userName);// 付款人的真实姓名
        //dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit("2");// 客户账户借记贷记类型,0存折 1借记 2贷记
        dto.setCustCardValidDate(expiredTime);//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        dto.setCustCardCvv2(cvn2);//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        // dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
        dto.setCustCertificationType("0");// 客户证件类型,目前只支持身份证,详情请看文档
        dto.setCustID(idCard);// 证件号码
        dto.setRemark("短信验证");// 商户可额外填写付款方备注信息,此信息会传给银行,会在银行的账单信息中显示(具体如何显示取决于银行方,快付通不保证银行肯定能显示)
        dto.setRateAmount(handlingFee);// 手续费
        //dto.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/kft/fastpay/call-back");//回调地址
        logger.info("请求参数:" + dto);
        SameIDCreditCardTradeResultDTO result = gbpService
                .sameIDCreditCardSmsCollect(dto);
        logger.info("响应结果:" + result);
        if (result.getStatus() == 0) {
            //提交成功transactionclear
            //String url = rip + "/v1.0/transactionclear/payment/update/thirdordercode";
            //restTemplete 是加了负载均衡的，不用直接用具体的ip或者域名来请求，这样会找不到这个服务，会报错，
            // use the application name in  eureka   it's ok

        	RestTemplate restTemplate=new RestTemplate();
            String url = prp.getIpAddress()+"/v1.0/transactionclear/payment/update/thirdordercode";


            logger.info("第三方流水号添加成功，进行补单操作======="+url);
            logger.info("发短信成功，添加第三方订单号url====="+url);
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("order_code", orderCode);
            multiValueMap.add("third_code", result.getOrderNo());
            try {
                restTemplate.postForObject(url, multiValueMap, String.class);
            } catch (RestClientException e) {
                e.printStackTrace();
                logger.info("第三方流水号添加成功" + result.getOrderNo());
            }
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "SUCCESS");
            maps.put("orderId", result.getOrderNo());
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails());
            logger.info("===========================快付通请求下单异常：" + result.getFailureDetails());
            this.addOrderCauseOfFailure(result.getOrderNo(), result.getFailureDetails() + "[下单异常：" + result.getOrderNo() + "]", rip);
        }
        //destory();
        return maps;
    }


/**
     * @Author zhangchaofeng
     * @Description 快捷代扣步骤二，确认扣款
     * @Date 17:02 2019/4/22
     * @Param [orderCode, smsCode, trade_no]
     * @return java.lang.Object
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kft/fast-pay")
    public @ResponseBody
    Object fastPay(@RequestParam(value = "orderCode") String orderCode,
                   @RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "orderId") String trade_no)
            throws Exception {
        Map<String,Object> map = new HashMap<>();
        PaymentRequestParameter parameter=redisUtil.getPaymentRequestParameter(orderCode);
        SameIDCreditCardSmsCollectConfirmDTO dto = new SameIDCreditCardSmsCollectConfirmDTO();
        setCommonParam(dto,orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_collect_confirm");// 接口名称，固定不变
        dto.setProductNo("GBP00003");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(parameter.getOrderCode());// 同快捷代扣申请订单号一致
        dto.setSmsCode(smsCode);// 短信验证码
        dto.setCustBindPhoneNo(parameter.getCreditCardPhone());// 手机号
        dto.setConfirmFlag("1");// 1确认，2取消,确认支付时，短信验证码不能为空

        logger.info("请求参数:" + dto);
        SameIDCreditCardTradeResultDTO result = gbpService
                .sameIDCreditCardSmsCollectConfirm(dto);
        logger.info("响应结果:" + result);

        if(result.getStatus()==1 || result.getStatus()==0){
            logger.info("快付通代扣确认接口成功，开始内部调用单笔付款接口");
            Map<String,String> map1 = excutePayByRule(orderCode);
            if(StringUtils.equals(map1.get(CommonConstants.RESP_CODE), CommonConstants.SUCCESS)){
                logger.info("kft3.5代笔付款成功==============");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "交易成功");
               // map.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
                map.put("redirect_url", "http://139.196.125.48/v1.0/paymentchannel/topup/sdjpaysuccess");
            } else {
                logger.info("kft3.5代笔付款失败==============");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, map1.get(CommonConstants.RESP_MESSAGE));
                this.addOrderCauseOfFailure(orderCode, map1.get(CommonConstants.RESP_MESSAGE) + "[请求支付异常:" + trade_no + "]", parameter.getIpAddress());
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails());
            this.addOrderCauseOfFailure(orderCode, result.getFailureDetails() + "[请求支付异常:" + trade_no + "]", parameter.getIpAddress());
        }
        //destory();

        return map;
    }



/**
     * @Author zhangchaofeng
     * @Description // 单笔付款
     * @Date 20:16 2019/5/6
     * @Param [orderCode]
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kft/excutePayByRule")
    @ResponseBody
    public Map<String,String> excutePayByRule(@RequestParam String orderCode) throws Exception {
        logger.info("进入单笔代还接口，orderCode={}===========",orderCode);
        PaymentRequestParameter parameter=redisUtil.getPaymentRequestParameter(orderCode);
        Map<String,String> maps = Maps.newHashMap();
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(parameter.getDebitBankName());
        if(kftBankCodeByName == null || kftBankCodeByName.size()==0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "单笔付款匹配到账卡信息失败");
            return maps;
        }
        for (String bankCode:kftBankCodeByName){
            logger.info("快付通银行代号：{}",bankCode);
        }
//        Map<String,String> resMap = excuteSendVerifyThreeMessage(orderCode+ RandomUtils.generateString(2), parameter.getUserName(), "0", parameter.getIdCard(), kftBankCodeByName.get(0), parameter.getDebitCardNo(), "1");
//
//        if(StringUtils.equals(resMap.get(CommonConstants.RESP_CODE), CommonConstants.SUCCESS)){
//            logger.info("三要素验证成功==========");
//        }else {
//            logger.info("三要素验证失败==========");
//            logger.info(resMap.get(CommonConstants.RESP_MESSAGE));
//            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            maps.put(CommonConstants.RESP_MESSAGE, resMap.get(CommonConstants.RESP_MESSAGE));
//            return maps;
//        }
        BigDecimal realAmount = new BigDecimal(parameter.getRealAmount());
        BigDecimal newRealAmount = new BigDecimal("0.5");
        SameIDCreditCardPayToBankAccountDTO dto = new SameIDCreditCardPayToBankAccountDTO();
        setCommonParam(dto,orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_verify_pay");// 接口名称，固定不变
        dto.setProductNo("GBP00013");// 替换成快付通提供的产品编号，测试生产不一样
        //生成一个单笔付款的订单号，在原有的订单号上拼上一个随机两位的串
        String thirdOrderCode = orderCode+ RandomUtils.generateString(2);
        logger.info("=========orderCode:{}----单笔代付订单号thirdOrderCode:{}",orderCode, thirdOrderCode);
        dto.setOrderNo(thirdOrderCode);// 订单号同一个商户必须保证唯一
        dto.setTradeName("同名卡单笔付款测试");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(yuan2Fen(realAmount.add(newRealAmount).toString()));// 单位:分,不支持小数点,为到账金额+5毛。因为手续费要扣5毛
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档

        dto.setCustBankNo(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo(parameter.getDebitCardNo());// 本次交易中,对客户的哪张卡上付钱
        // dto.setCustPhone("15555555555"); //客户手机号
        dto.setCustName(parameter.getUserName());// 付款人的真实姓名
        dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit("1");// 客户账户借记贷记类型,0存折 1借记 2贷记
        // dto.setCustCardValidDate("0115");//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        // dto.setCustCardCvv2("375");//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        // dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
        dto.setCustCertificationType("0");// 客户证件类型,目前只支持身份证,详情请看文档
        dto.setCustID(parameter.getIdCard());// 证件号码
        //dto.setCustProtocolNo("2018000111001210");// 可空 客户协议编号 扣款人在快付通备案的协议号。
        dto.setRemark("单笔付款");// 商户可额外填写付款方备注信息,此信息会传给银行,会在银行的账单信息中显示(具体如何显示取决于银行方,快付通不保证银行肯定能显示)
        dto.setRateAmount(yuan2Fen("0.5"));// 固定5毛 手续费
        dto.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/kft/fastpay/call-back");//回调地址
        logger.info("===========单笔代扣请求参数:" + dto);
        SameIDCreditCardTradeResultDTO result = gbpService
                .sameIDCreditCardGbpPay(dto);
        logger.info("===========单笔代扣响应结果:" + result);
        if(result.getStatus()==1 || result.getStatus()==0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "交易成功");
            if(result.getStatus()==1){
                //直接返终态就没有回调 1成功和2失败是终态 0处理中不是终态
                //如果是1，则代表成功，则修改订单得状态
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String url;
                logger.info("*********************交易成功***********************");

                url = parameter.getIpAddress()+ChannelUtils.getCallBackUrl(parameter.getIpAddress());
                logger.info("第三方流水号添加成功，进行补单操作======="+url);
                //url = parameter.getIpAddress() + "/v1.0/transactionclear/payment/update";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code",thirdOrderCode);
                try {
                    restTemplate.postForObject(url, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
                logger.info("订单状态修改成功===================" + orderCode + "====================" + result);
            }

        }else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails());
        }
        return maps;
    }



/**
     * @Author ruanjiajun  新增参数出款卡及出款银行
     * @Description // 单笔付款
     * @Date 20:16 2019/5/13
     * @Param [orderCode]
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kft/excutePayByRule/Compensation")
    @ResponseBody
    public Map<String,String> excutePayByRuleNew(@RequestParam String orderCode,
    		@RequestParam String debitCardNo,
    		@RequestParam String bankName
    		) throws Exception {
        logger.info("进入单笔代还接口，orderCode={}===========",orderCode);
        PaymentRequestParameter parameter=redisUtil.getPaymentRequestParameter(orderCode);
        Map<String,String> maps = Maps.newHashMap();
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(bankName);
        if(kftBankCodeByName == null || kftBankCodeByName.size()==0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "单笔付款匹配到账卡信息失败");
            return maps;
        }
        for (String bankCode:kftBankCodeByName){
            logger.info("快付通银行代号：{}",bankCode);
        }
//        Map<String,String> resMap = excuteSendVerifyThreeMessage(orderCode+ RandomUtils.generateString(2), parameter.getUserName(), "0", parameter.getIdCard(), kftBankCodeByName.get(0), bankName, "1");
//
//        if(StringUtils.equals(resMap.get(CommonConstants.RESP_CODE), CommonConstants.SUCCESS)){
//            logger.info("三要素验证成功==========");
//        }else {
//            logger.info("三要素验证失败==========");
//            logger.info(resMap.get(CommonConstants.RESP_MESSAGE));
//            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            maps.put(CommonConstants.RESP_MESSAGE, resMap.get(CommonConstants.RESP_MESSAGE));
//            return maps;
//        }

        SameIDCreditCardPayToBankAccountDTO dto = new SameIDCreditCardPayToBankAccountDTO();
        setCommonParam(dto,orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_verify_pay");// 接口名称，固定不变
        dto.setProductNo("GBP00013");// 替换成快付通提供的产品编号，测试生产不一样
        //生成一个单笔付款的订单号，在原有的订单号上拼上一个随机两位的串
        String thirdOrderCode = orderCode+ RandomUtils.generateString(2);
        dto.setOrderNo(thirdOrderCode);// 订单号同一个商户必须保证唯一
        dto.setTradeName("同名卡单笔付款测试");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(yuan2Fen(parameter.getRealAmount()));// 单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档

        dto.setCustBankNo(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo(debitCardNo);// 本次交易中,对客户的哪张卡上付钱
        // dto.setCustPhone("15555555555"); //客户手机号
        dto.setCustName(parameter.getUserName());// 付款人的真实姓名
        dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit("1");// 客户账户借记贷记类型,0存折 1借记 2贷记
        // dto.setCustCardValidDate("0115");//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        // dto.setCustCardCvv2("375");//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        // dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
        dto.setCustCertificationType("0");// 客户证件类型,目前只支持身份证,详情请看文档
        dto.setCustID(parameter.getIdCard());// 证件号码
        //dto.setCustProtocolNo("2018000111001210");// 可空 客户协议编号 扣款人在快付通备案的协议号。
        dto.setRemark("单笔付款");// 商户可额外填写付款方备注信息,此信息会传给银行,会在银行的账单信息中显示(具体如何显示取决于银行方,快付通不保证银行肯定能显示)
        //dto.setRateAmount(yuan2Fen(parameter.getExtraFee()));// 手续费   代扣的时候可以不要手续费，钱已经被扣了，在通道的虚拟账户里
        dto.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/kft/fastpay/call-back");//回调地址
        logger.info("===========单笔代扣请求参数:" + dto);
        SameIDCreditCardTradeResultDTO result = gbpService
                .sameIDCreditCardGbpPay(dto);
        logger.info("===========单笔代扣响应结果:" + result);
        if(result.getStatus()==1 || result.getStatus()==0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "交易成功");
            if(result.getStatus()==1){
                //直接返终态就没有回调 1成功和2失败是终态 0处理中不是终态
                //如果是1，则代表成功，则修改订单得状态
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String url;
                logger.info("*********************交易成功***********************");

                url = parameter.getIpAddress()+ChannelUtils.getCallBackUrl(parameter.getIpAddress());
                logger.info("第三方流水号添加成功，进行补单操作======="+url);
                //url = parameter.getIpAddress() + "/v1.0/transactionclear/payment/update";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code",thirdOrderCode);
                try {
                    restTemplate.postForObject(url, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
                logger.info("订单状态修改成功===================" + orderCode + "====================" + result);
            }

        }else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails());
        }
        return maps;
    }

/**
     * @Author zhangchaofeng
     * @Description //未付资金查询
     * @Date 13:54 2019/5/7
     * @Param [orderCode]
     * @return java.lang.Object
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kft/excuteBalanceAmountQuery")
    @ResponseBody
    public Object excuteBalanceAmountQuery(@RequestParam String orderCode) throws GatewayClientException {
        PaymentRequestParameter parameter=redisUtil.getPaymentRequestParameter(orderCode);
        SameIDCreditCardNotPayBalanceQueryDTO dto = new SameIDCreditCardNotPayBalanceQueryDTO();
        setCommonParam(dto,orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_not_pay_balance");// 接口名称，固定不变
        dto.setProductNo("GBP00006");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setCustID(parameter.getIdCard());// 用户身份证号码
        logger.info("请求参数:" + dto);
        SameIDCreditCardQueryResponseDTO result = gbpService
                .sameIDCreditCardBalanceAmountQuery(dto);// 发往快付通验证并返回结果
        logger.info("响应结果:" + result);
        return result;
    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description //三要素验证
     * @Date 9:35 2019/4/17
     * @Param [orderNo, custName, custCertificationType, custID, custBankNo, custBankAccountNo, custAccountCreditOrDebit]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kft/verifythreemessage")
    @ResponseBody
    public Map<String,String> excuteSendVerifyThreeMessage(
            @RequestParam String orderNo,
            @RequestParam String custName,
            @RequestParam String custCertificationType,
            @RequestParam String custID,
            @RequestParam String custBankNo,
            @RequestParam String custBankAccountNo,
            @RequestParam String custAccountCreditOrDebit
    ) throws Exception {
        Map<String,String> map = Maps.newHashMap();
        BankCardDetailDTO dto = new BankCardDetailDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(VERIFY_THREE_MESSAGE_SERVICE_NAME);// 接口名称，固定不变
        dto.setProductNo(VERIFY_THREE_MESSAGE_PRODUCT_NO);// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一
        dto.setCustName(custName);// 客户姓名
        dto.setCustCertificationType(custCertificationType);// 客户证件类型,目前只支持身份证,详情请看文档  身份证0
        dto.setCustID(custID);// 客户证件号码
        dto.setCustBankNo(custBankNo);// 银行卡行别
        dto.setCustBankAccountNo(custBankAccountNo);// 需要验证的银行卡号
        dto.setCustAccountCreditOrDebit(custAccountCreditOrDebit);// 客户账户借记贷记类型,0存折 1借记 2贷记
        logger.info("三要素验证请求参数:{}", dto);
        SameIDCreditCardTradeResultDTO result = gbpService.threeMessageVerify(dto);
        logger.info("三要素验证响应结果:{}", result);
        //destory();
        if(result.getStatus()==1){
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "验证成功");
        }else if(result.getStatus()==2) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails());
        }
        return map;
    }



/**
     * 支付异步通知，回调
     *
     * @param request
     * @param response
     * @throws IOException
     */

    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/kft/fastpay/call-back")
    public void paycallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("快付通---支付异步回调参数：" + request.toString());
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                logger.info(key + "=============" + s);
            }
        }
        String tradeNo = request.getParameter("orderNo");
        String resultCode = request.getParameter("status");
        String resultMsg = request.getParameter("failureDetails");
        String merchantId = request.getParameter("merchantId");
        //之前是在原有订单号基础上拼上一个四位的串，现在需要还原一下
        String orderCode = tradeNo.substring(0,tradeNo.length()-2);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        if(prp == null || StringUtils.isBlank(prp.getIdCard())){
            logger.info("从缓存查询数据为空,orderNo = {}", tradeNo);
            return;
        }
        if ("1".equals(resultCode)) {
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            String url;
            String result = null;
            logger.info("*********************交易成功***********************");
            url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");

            requestEntity.add("order_code", orderCode);
            requestEntity.add("third_code",tradeNo);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("", e);
            }

            logger.info("订单状态修改成功===================" + orderCode + "====================" + result);
            //保存商户merchantId
            KFTRegister register=new KFTRegister();
            register.setIdCard(prp.getIdCard());
            register.setMerchantId(merchantId);
            register.setPhone(prp.getPhone());
            register.setRate(prp.getRate());
            register.setExtraFee(prp.getExtraFee());
            register.setBankCard(prp.getBankCard());
            topupPayChannelBusiness.createKFTRegister(register);
            logger.info("保存商户merchantId : {},用户身份证 : {}", merchantId, prp.getIdCard());

            logger.info("订单已交易成功!");

            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else if ("2".equals(resultCode)) {
            logger.info("交易失败");
            this.addOrderCauseOfFailure(orderCode, resultMsg + "[支付状态码:2]", prp.getIpAddress());
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();

        } else if ("0".equals(resultCode)) {
            logger.info("交易处理中，请稍后查询");
            this.addOrderCauseOfFailure(orderCode, resultMsg + "[支付状态码:0]", prp.getIpAddress());
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else {
            this.addOrderCauseOfFailure(orderCode, resultMsg + "[未知渠道状态]", prp.getIpAddress());
            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        }

    }




/**
     * @Author zhangchaofeng
     * @Description 交易查询
     * @Date 11:48 2019/4/24
     * @Param status: 交易记录的状态 0：处理中 ,1：成功, 2：失败 不填默认全部
     *        type: 交易类型 1：收款,2：付款 不填默认全部
     *        orderNo: 交易订单号 可空
     * @return void
     **/

    @RequestMapping(name = "/v1.0/paymentgateway/topup/kft/executeTradeQuery", method = RequestMethod.POST)
    @ResponseBody
    public Object executeTradeQuery(@RequestParam(value = "merchantId") String merchantId,
                                   @RequestParam("startDate") String startDate,
                                   @RequestParam("endDate") String endDate,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "type", required = false) String type,
                                   @RequestParam(value = "orderNo", required = false) String orderNo) throws Exception {
        SameIDCreditCardTradeQueryDTO dto = new SameIDCreditCardTradeQueryDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_trade_record_query");// 接口名称，固定不变
        dto.setMerchantId(merchantId);
        dto.setProductNo("GBPTM006");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setStartDate(startDate);// 查询交易的开始日期
        dto.setEndDate(endDate);// 查询交易的结束日期
        dto.setStatus(status);// 交易记录的状态 0：处理中 ,1：成功, 2：失败 不填默认全部
        dto.setTradeType(type);// 交易类型 1：收款,2：付款 不填默认全部
        dto.setOrderNo(orderNo);// 交易订单号 可空

        logger.info("请求参数:" + dto);
        SameIDCreditCardQueryResponseDTO result = gbpService
                .sameIDCreditCardTradeQuery(dto);
        logger.info("响应结果:" + result.getStatus() + "--" + result.getDetails());
        //destory();
        return result;
    }



/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description //快捷协议代扣协议申请(快捷协议代扣步骤1)
     * @Date 10:30 2019/4/17
     * @Param [orderNo, treatyType, note, startDate, endDate, holderName, bankType, bankCardType, bankCardNo, mobileNo, certificateType, certificateNo]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kft/treatycollectapply")
    @ResponseBody
    public Object treatyCollectApply(@RequestParam String orderNo,
                                     @RequestParam String treatyType,
                                     @RequestParam(value = "note", required = false) String note,
                                     @RequestParam String startDate,
                                     @RequestParam String endDate,
                                     @RequestParam String holderName,
                                     @RequestParam String bankType,
                                     @RequestParam String bankCardType,
                                     @RequestParam String bankCardNo,
                                     @RequestParam String mobileNo,
                                     @RequestParam String certificateType,
                                     @RequestParam String certificateNo
    ) throws Exception {
        TreatyApplyDTO dto = new TreatyApplyDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(TREATY_COLLECT_APPLY_SERVICE_NAME);// 接口名称，固定不变
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一
        dto.setTreatyType(treatyType);// 11借计卡扣款 12信用卡扣款
        dto.setNote(note);// 协议简要说明，可空
        dto.setStartDate(startDate);// 生效日期,日期格式yyyyMMdd
        dto.setEndDate(endDate);// 协议失效日期,日期格式yyyyMMdd
        dto.setHolderName(holderName);// 持卡人真实姓名
        dto.setBankType(bankType);// 银行卡行别，测试环境只支持建行卡 1051000
        dto.setBankCardType(bankCardType);// 0存折 1借记 2贷记   1
        dto.setBankCardNo(bankCardNo);// 银行卡号  522848231231231232
        dto.setMobileNo(mobileNo);// 银行预留手机号
        dto.setCertificateType(certificateType);// 持卡人证件类型，0身份证
        dto.setCertificateNo(certificateNo);// 证件号码
        // dto.setCustCardValidDate("信用卡有效期");//可空，信用卡扣款时必填
        // dto.setCustCardCvv2("信用卡cvv2");//可空，信用卡扣款时必填
        logger.info("请求信息为：" + dto.toString());
        TreatyApplyResultDTO result = gbpService.treatyCollectApply(dto);// 发往快付通验证并返回结果
        logger.info("响应信息为:" + result.toString());
        //destory();
        return result;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kft/confirmTreatyCollectApply")
    @ResponseBody
    public Object confirmTreatyCollectApply(
            @RequestParam String orderNo,
            @RequestParam String smsSeq,
            @RequestParam String authCode,
            @RequestParam String holderName,
            @RequestParam String bankCardNo
    ) throws Exception {
        TreatyConfirmDTO dto = new TreatyConfirmDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(CONFIRM_TREATY_COLLECT_APPLY_SERVICE_NAME);// 接口名称，固定不变
        dto.setOrderNo(orderNo);// 同协议代扣申请订单号一致
        dto.setSmsSeq(smsSeq);// 协议代扣申请返回的短信流水号
        dto.setAuthCode(authCode);//验证码
        dto.setHolderName(holderName);// 持卡人姓名，与申请时一致
        dto.setBankCardNo(bankCardNo);// 银行卡号，与申请时一致
        logger.info("请求信息为：" + dto.toString());
        TreatyConfirmResultDTO result = gbpService.confirmTreatyCollectApply(dto);// 发往快付通验证并返回结果
        logger.info("响应信息为:" + result.toString());
        //destory();
        return result;
    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description ////快捷协议代扣
     * @Date 11:17 2019/4/17
     * @Param [orderNo, treatyNo, amount, currency, holderName, bankType, bankCardNo, rateAmount, notifyUrl]
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kft/excuteTreatyCollect")
    @ResponseBody
    public Object excuteTreatyCollect(
            @RequestParam String orderNo,
            @RequestParam String treatyNo,
            @RequestParam String amount,
            @RequestParam String currency,
            @RequestParam String holderName,
            @RequestParam String bankType,
            @RequestParam String bankCardNo,
            @RequestParam String rateAmount,
            @RequestParam String notifyUrl
    ) throws Exception {
        SameIDCreditCardTreatyCollectDTO dto = new SameIDCreditCardTreatyCollectDTO();
        setCommonParam(dto,orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(EXCUTE_TREATY_COLLECT_SERICE_NAME);// 接口名称，固定不变
        dto.setProductNo(EXCUTE_TREATY_COLLECT_PRODUCT_NO);// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一
        dto.setTreatyNo(treatyNo);// 协议代扣申请确认返回的协议号
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(amount);// 此次交易的具体金额,单位:分,不支持小数点
        dto.setCurrency(currency);// 快付通定义的扣费币种,详情请看文档 CNY
        dto.setHolderName(holderName);// 持卡人姓名，与申请时一致
        dto.setBankType(bankType);// 客户银行账户行别;快付通定义的行别号,详情请看文档 1051000
        dto.setBankCardNo(bankCardNo);// 银行卡号，与申请时一致，本次交易中,从客户的哪张卡上扣钱
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        // dto.setCustCardValidDate("信用卡有效期");//可空，信用卡扣款时必填
        // dto.setCustCardCvv2("信用卡cvv2");//可空，信用卡扣款时必填
        dto.setRateAmount(rateAmount);// 手续费
        dto.setNotifyUrl(notifyUrl);//回调地址
        logger.info("请求参数:" + dto);
        SameIDCreditCardTreatyCollectResultDTO result = gbpService
                .sameIDCreditCardTreatyCollect(dto);
        logger.info("响应结果:" + result);
        //destory();
        return result;
    }

}

