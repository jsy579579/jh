package com.jh.paymentgateway.controller.kft;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.RandomUtils;
import cn.jh.common.utils.UUIDGenerator;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.kft.KFTBindCard;
import com.jh.paymentgateway.util.Util;
import com.lycheepay.gateway.client.GBPService;
import com.lycheepay.gateway.client.GatewayClientException;
import com.lycheepay.gateway.client.KftService;
import com.lycheepay.gateway.client.dto.base.BaseReqParameters;
import com.lycheepay.gateway.client.dto.gbp.TreatyApplyDTO;
import com.lycheepay.gateway.client.dto.gbp.TreatyApplyResultDTO;
import com.lycheepay.gateway.client.dto.gbp.TreatyConfirmDTO;
import com.lycheepay.gateway.client.dto.gbp.TreatyConfirmResultDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
 * @date 2019/5/22
 * @description 快付通代还
 */

@Controller
@EnableAutoConfiguration
public class KFTDHPageRequest extends BaseChannel {
    private static final Logger logger = LoggerFactory.getLogger(KFTDHPageRequest.class);

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

    @Value("${kft.merchantIdXe}")
    private String merchantIdXe;//2019032100098370

    @Value("${kft.version}")
    private String version;//1.0.0-IEST

    private static final String REQ_NO_PREFIX = "kft_gbp";


/**
     * 快捷协议代扣协议申请(快捷协议代扣步骤1)服务名
     */

    private static final String TREATY_COLLECT_APPLY_SERVICE_NAME = "gbp_same_id_treaty_collect_apply";
/**
     * 快捷协议代扣协议确定(快捷协议代扣步骤2)服务名
     */

    private static final String CONFIRM_TREATY_COLLECT_APPLY_SERVICE_NAME = "gbp_same_id_confirm_treaty_collect_apply";

    public void destory() throws Exception {
        GBPService.destory();
    }
/**
     * 设置通用参数
     */

    public void setCommonParam(BaseReqParameters dto, String codeNo) {
        logger.info("REQ_NO_PREFIX={},merchantId={},version={}", REQ_NO_PREFIX, merchantIdXe, version);
        dto.setReqNo(REQ_NO_PREFIX + System.currentTimeMillis());// 商户可以根据此参数来匹配请求和响应信息；快付通将此参数原封不动的返回给商户
        dto.setReqNo(codeNo);
        dto.setMerchantId(merchantIdXe);// 替换成快付通提供的商户ID，测试生产不一样
        dto.setVersion(version);// 接口版本号，测试:1.0.0-IEST,生产:1.0.0-PRD
    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description 跟还款对接的接口
     * @Date 15:31 2019/5/22
     * @Param [request, bankCard, idCard, phone, bankName, userName, expiredTime, securityCode]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kftdh/torepayment")
    public @ResponseBody
    Object kftdhToRepayment(HttpServletRequest request,
                            @RequestParam(value = "bankCard") String bankCard,
                            @RequestParam(value = "idCard") String idCard,
                            @RequestParam(value = "phone") String phone,
                            @RequestParam(value = "bankName") String bankName,
                            @RequestParam(value = "userName") String userName,
                            @RequestParam(value = "expiredTime") String expiredTime,
                            @RequestParam(value = "securityCode") String securityCode) throws Exception {

        KFTBindCard kftBindCard = topupPayChannelBusiness.findKftBindCardByBankCard(bankCard);

        if(StringUtils.isNotBlank(expiredTime)) {
        	expiredTime = this.expiredTimeToMMYY(expiredTime);
        }

        if (kftBindCard == null) {

            return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
                    ip + "/v1.0/paymentgateway/topup/tokft/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
                            + "&cardType=" + URLEncoder.encode("信用卡", "UTF-8") + "&bankCard=" + bankCard + "&phone="
                            + phone + "&userName=" + URLEncoder.encode(userName, "UTF-8") + "&idCard=" + idCard
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);
        } else {
            if (!"1".equals(kftBindCard.getStatus())) {

                return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
                        ip + "/v1.0/paymentgateway/topup/tokft/bindcard?bankName="
                                + URLEncoder.encode(bankName, "UTF-8") + "&cardType="
                                + URLEncoder.encode("信用卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
                                + "&userName=" + URLEncoder.encode(userName, "UTF-8") + "&idCard=" + idCard
                                + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, "已完成鉴权验证!");
            }
        }

    }



/**
     * @Author zhangchaofeng
     * @Description //未付资金查询
     * @Date 13:54 2019/5/7
     * @Param [orderCode]
     * @return java.lang.Object
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kftdh/excuteBalanceAmountQuery")
    @ResponseBody
    public Object excuteBalanceAmountQuery(@RequestParam(value = "idCard") String idCard) throws GatewayClientException {
        SameIDCreditCardNotPayBalanceQueryDTO dto = new SameIDCreditCardNotPayBalanceQueryDTO();
        setCommonParam(dto, UUIDGenerator.getUUID());// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_not_pay_balance");// 接口名称，固定不变
        dto.setProductNo("GBP00006");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setCustID(idCard);// 用户身份证号码
        logger.info("请求参数:" + dto);
        SameIDCreditCardQueryResponseDTO result = gbpService
                .sameIDCreditCardBalanceAmountQuery(dto);// 发往快付通验证并返回结果
        logger.info("响应结果:" + result);
        if(StringUtils.equals(result.getStatus(),"1")){
            return ResultWrap.init(CommonConstants.SUCCESS,"查询成功", result);
        } else {
            return ResultWrap.init(CommonConstants.FALIED,"查询失败");
        }
    }


/**
     * @Author zhangchaofeng
     * @Description 交易状态
     * @Date 20:27 2019/5/24
     * @Param [orderNo, endDate]
     * @return java.lang.Object
     **/

    @RequestMapping(value = "/v1.0/paymentgateway/topup/kftdh/excuteTradeQuery", method = RequestMethod.POST)
    @ResponseBody
    public Object excuteTradeQuery(@RequestParam(value = "orderCode") String orderCode) throws GatewayClientException {
        SameIDCreditCardTradeQueryDTO dto = new SameIDCreditCardTradeQueryDTO();
        setCommonParam(dto,orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_trade_record_query");// 接口名称，固定不变
        dto.setProductNo("GBP00005");// 替换成快付通提供的产品编号，测试生产不一样
        dto.setOrderNo(orderCode);
        dto.setStartDate("20190524");// 查询交易的开始日期
        dto.setEndDate(new SimpleDateFormat("yyyyMMdd")
                .format(new Date()));// 查询交易的结束日期
        //dto.setStatus("1");// 交易记录的状态 0：处理中 ,1：成功, 2：失败 不填默认全部
        //dto.setTradeType("1");// 交易类型 1：收款,2：付款 不填默认全部
        logger.info("快付通实名卡交易查询，请求参数:" + dto);
        SameIDCreditCardQueryResponseDTO result = gbpService
                .sameIDCreditCardTradeQuery(dto);
        logger.info("快付通实名卡交易查询，响应结果:" + result.getStatus() + "--" + result.getDetails());
        if (StringUtils.equals(result.getStatus(), "1") && StringUtils.isNotBlank(result.getDetails())) {
            JSONArray jsonDetails = JSONArray.parseArray(result.getDetails());
            if(jsonDetails.isEmpty()){
                return ResultWrap.init(CommonConstants.FALIED,"未匹配到交易信息");
            }
            JSONObject detailObj = (JSONObject) jsonDetails.get(0);

            String payStatus = detailObj.getString("status");

            if (StringUtils.isNotBlank(payStatus) && StringUtils.equals(payStatus, "1")) {
                return ResultWrap.init(CommonConstants.SUCCESS, "成功");
            } else if (StringUtils.equals(payStatus, "0")) {
                return ResultWrap.init("999998", "处理中");
            } else {
                String failureDetails = detailObj.getString("failureDetails");
                return ResultWrap.init(CommonConstants.FALIED, failureDetails);
            }

        } else {
            return ResultWrap.init(CommonConstants.FALIED, result.getFailureDetails());
        }


    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description //快捷协议代扣协议申请(快捷协议代扣步骤1)
     * @Date 10:30 2019/4/17
     * @Param [orderNo, treatyType, note, startDate, endDate, holderName, bankType, bankCardType, bankCardNo, mobileNo, certificateType, certificateNo]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kftdh/treatycollectapply")
    @ResponseBody
    public Object treatyCollectApply(@RequestParam String expiredTime,
                                     @RequestParam String userName,
                                     @RequestParam String bankName,
                                     @RequestParam String bankCard,
                                     @RequestParam String phone,
                                     @RequestParam String idCard,
                                     @RequestParam String securityCode
    ) throws Exception {
        String orderNo = UUID.randomUUID().toString().replaceAll("-", "");
        logger.info("================进入快付通代扣协议申请接口，orderNo={}", orderNo);
        TreatyApplyDTO dto = new TreatyApplyDTO();
        setCommonParam(dto, orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(TREATY_COLLECT_APPLY_SERVICE_NAME);// 接口名称，固定不变
        dto.setOrderNo(orderNo);// 订单号同一个商户必须保证唯一
        dto.setTreatyType("12");// 11借计卡扣款 12信用卡扣款
        //dto.setNote(note);// 协议简要说明，可空
        dto.setStartDate("20190522");// 生效日期,日期格式yyyyMMdd
        dto.setEndDate(this.generateExpireDate(this.expiredTimeToYYMM(expiredTime)));// 协议失效日期,日期格式yyyyMMdd
        dto.setHolderName(userName);// 持卡人真实姓名

        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(bankName);
        if (kftBankCodeByName == null || kftBankCodeByName.size() == 0) {
            logger.info("快捷协议代扣协议申请(快捷协议代扣步骤1)==>未匹配到银行卡行别信息bankName={}", bankName);
            return ResultWrap.init(CommonConstants.FALIED, "未匹配到银行卡行别信息");
        }
        dto.setBankType(kftBankCodeByName.get(0));// 银行卡行别，测试环境只支持建行卡 1051000
        dto.setBankCardType("2");// 0存折 1借记 2贷记
        dto.setBankCardNo(bankCard);// 银行卡号
        dto.setMobileNo(phone);// 银行预留手机号
        dto.setCertificateType("0");// 持卡人证件类型，0身份证
        dto.setCertificateNo(idCard);// 证件号码
        dto.setCustCardValidDate(expiredTime);//可空，信用卡扣款时必填
        dto.setCustCardCvv2(securityCode);//可空，信用卡扣款时必填
        logger.info("快捷协议代扣协议申请(快捷协议代扣步骤1)请求信息为：" + dto.toString());
        TreatyApplyResultDTO result = gbpService.treatyCollectApply(dto);// 发往快付通验证并返回结果
        logger.info("快捷协议代扣协议申请(快捷协议代扣步骤1)响应信息为:" + result.toString());
        //destory();
        if (result.getStatus() == 0 || result.getStatus() == 1) {
            logger.info("快捷协议代扣协议申请(快捷协议代扣步骤1)====成功=====");
            return resultMap(CommonConstants.SUCCESS, "操作成功", result.getOrderNo(), result.getSmsSeq());
        } else {
            logger.info("快捷协议代扣协议申请(快捷协议代扣步骤1)====失败=====");
            return ResultWrap.init(CommonConstants.FALIED, result.getFailureDetails());
        }
    }

    private Map<String, Object> resultMap(String respCode, String respMesg, String orderNo, String smsSeq) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESULT, orderNo);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);
        map.put("smsSeq", smsSeq);
        return map;
    }


/**
     * @Author zhangchaofeng
     * @Description 把日期mm和yy交换位置
     * @Date 13:57 2019/5/28
     * @Param [date]
     * @return java.lang.String
     **/

       private String exchangeDate(String date){
           if(StringUtils.isBlank(date) || date.length() < 4){
                return "";
            }
            return date.substring(2,4) + date.substring(0,2);
        }


/**
     * @return java.lang.String
     * @Author zhangchaofeng
     * @Description 把信用卡的失效日期 MM-yy转为yyyy-mm-dd 保证结果日期在失效期之前
     * @Date 19:45 2019/5/22
     * @Param [expiredTime]
     **/

    private static String mmyyDateFormat(String expiredTime) {
        String yy = expiredTime.substring(expiredTime.length() - 2, expiredTime.length() - 0);
        String expiredTimeYMD = "20" + yy + expiredTime.substring(0, 2);
        if (StringUtils.equals(expiredTimeYMD.substring(expiredTimeYMD.length() - 1, expiredTimeYMD.length() - 0), "1")) {
            int year = Integer.parseInt(yy) - 1;
            expiredTimeYMD = "20" + year + "12" + "30";
        } else {
            int varDate = Integer.parseInt(expiredTimeYMD) - 1;
            expiredTimeYMD = varDate + "28";
        }
        return expiredTimeYMD;
    }
/**
     * @Author zhangchaofeng
     * @Description 将信用卡的过期时间YYMM 变成 yyyy-mm-dd 保证结果日期在失效期之前
     * @Date 11:31 2019/5/28
     * @Param [date]
     * @return java.lang.String
     **/

    private static String generateExpireDate(String date){
        if(StringUtils.isBlank(date) || date.length() < 4){
            return "";
        }
        String expDate;
        String mm = date.substring(2,4);
        Integer yy = Integer.parseInt(date.substring(0,2));
        if(StringUtils.equals(mm,"01")){
            yy = yy - 1;
            expDate = "20" + yy + "1230";
        }else if (StringUtils.equals(date.substring(2,3),"0")){
            Integer month = Integer.parseInt(date.substring(3,4))-1;
            expDate = "20" + yy + "0" + month + "28";
        } else if(StringUtils.equals(date.substring(2,4),"10")){
            expDate = "20" + yy + "09" + "28";
        } else {
            Integer month = Integer.parseInt(date.substring(2,4))-1;
            expDate = "20" + yy + month + "28";
        }
        return expDate;
    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description 快捷协议代扣协议确定(快捷协议代扣步骤2)
     * @Date 11:13 2019/5/22
     * @Param [orderNo, smsSeq, authCode, holderName, bankCardNo]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kftdh/confirmTreatyCollectApply")
    @ResponseBody
    public Object confirmTreatyCollectApply(
            @RequestParam String orderNo,
            @RequestParam String smsSeq,
            @RequestParam String smsCode,
            @RequestParam String userName,
            @RequestParam String bankCard,
            @RequestParam String phone,
            @RequestParam String expiredTime,
            @RequestParam String securityCode
    ) throws Exception {
        TreatyConfirmDTO dto = new TreatyConfirmDTO();
        setCommonParam(dto, orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService(CONFIRM_TREATY_COLLECT_APPLY_SERVICE_NAME);// 接口名称，固定不变
        dto.setOrderNo(orderNo);// 同协议代扣申请订单号一致
        dto.setSmsSeq(smsSeq);// 协议代扣申请返回的短信流水号
        dto.setAuthCode(smsCode);//验证码
        dto.setHolderName(userName);// 持卡人姓名，与申请时一致
        dto.setBankCardNo(bankCard);// 银行卡号，与申请时一致
        dto.setCustCardCvv2(securityCode);
        dto.setCustCardValidDate(expiredTime);
        logger.info("快捷协议代扣协议确定(快捷协议代扣步骤2)请求信息为：" + dto.toString());
        TreatyConfirmResultDTO result = gbpService.confirmTreatyCollectApply(dto);// 发往快付通验证并返回结果
        logger.info("快捷协议代扣协议确定(快捷协议代扣步骤2)响应信息为:" + result.toString());
        //destory();
        if ((result.getStatus() == 1 || result.getStatus() == 0) && StringUtils.isNotBlank(result.getTreatyId())) {
            logger.info("快捷协议代扣协议确定(快捷协议代扣步骤2)绑卡成功===orderNo={}", orderNo);
            KFTBindCard kftBindCard = new KFTBindCard();
            kftBindCard.setKftOrderNo(orderNo);
            kftBindCard.setBankCard(bankCard);
            kftBindCard.setPhone(phone);
            kftBindCard.setUserName(userName);
            kftBindCard.setTreatyId(result.getTreatyId());
            kftBindCard.setStatus("1");
            KFTBindCard kftBindCard1 = topupPayChannelBusiness.saveKftBindCard(kftBindCard);
            logger.info("===============已保存绑卡信息kftBindCard={}", kftBindCard1);
            return ResultWrap.init(CommonConstants.SUCCESS, "绑卡成功",
                    ip + "/v1.0/paymentgateway/topup/toght/bindcardsuccesspage");
        } else {
            logger.info("快捷协议代扣协议确定(快捷协议代扣步骤2)绑卡失败===orderNo={}", orderNo);
            return ResultWrap.init(CommonConstants.FALIED, result.getFailureDetails());
        }
    }

    private static String yuan2Fen(String yuan) {
        BigDecimal fenBd = new BigDecimal(yuan).multiply(new BigDecimal(100));
        fenBd = fenBd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return fenBd.toString();
    }


/**
     * @return java.lang.Object
     * @Author zhangchaofeng
     * @Description //快捷协议代扣
     * @Date 11:17 2019/4/17
     * @Param [orderNo,cityName]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kftdh/excuteTreatyCollect")
    @ResponseBody
    public Map<String, Object> excuteTreatyCollect(
            @RequestParam String orderNo,
            @RequestParam String cityName
    ) throws Exception {
        logger.info("============进入KFT快捷协议代扣---orderNo={},cityName={}",orderNo,cityName);
        PaymentRequestParameter parameter = redisUtil.getPaymentRequestParameter(orderNo);
        if (parameter == null || StringUtils.isBlank(parameter.getIdCard())) {
            return ResultWrap.init(CommonConstants.FALIED, "未匹配到订单信息-orderNo={}", orderNo);
        }
        String amount = parameter.getAmount();
        SameIDCreditCardTreatyCollectDTO dto = new SameIDCreditCardTreatyCollectDTO();
        setCommonParam(dto, orderNo);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_treaty_collect");// 接口名称，固定不变
        dto.setProductNo("GBP00004");// 替换成快付通提供的产品编号，测试生产不一样
        String thirdOrderCode = orderNo + RandomUtils.generateString(2);
        dto.setOrderNo(thirdOrderCode);// 订单号同一个商户必须保证唯一
        logger.info("----------生成协议代扣订单号=={}",thirdOrderCode);
        KFTBindCard kftBindCardByBankCard = topupPayChannelBusiness.findKftBindCardByBankCard(parameter.getBankCard());
        if (kftBindCardByBankCard == null || StringUtils.isBlank(kftBindCardByBankCard.getTreatyId())) {
            logger.info("TreatyId为空====bankCardNo={}", parameter.getBankCard());
            return ResultWrap.init(CommonConstants.FALIED, "匹配绑卡信息失败");
        }
        dto.setTreatyNo(kftBindCardByBankCard.getTreatyId());// 协议代扣申请确认返回的协议号
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        if (StringUtils.isBlank(amount)) {
            logger.info("amount为空=================orderNo={}", orderNo);
            return ResultWrap.init(CommonConstants.FALIED, "系统内部错误，金额为空");
        }
        dto.setAmount(yuan2Fen(amount));// 此次交易的具体金额,单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档 CNY
        dto.setHolderName(parameter.getUserName());// 持卡人姓名，与申请时一致
        String bankName = parameter.getCreditCardBankName();
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(bankName);
        if(kftBankCodeByName == null && kftBankCodeByName.size()==0){
            return ResultWrap.init(CommonConstants.FALIED, "没匹配到银行卡信息");
        }
        dto.setBankType(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档 1051000
        dto.setBankCardNo(parameter.getBankCard());// 银行卡号，与申请时一致，本次交易中,从客户的哪张卡上扣钱
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        String kftCityCode = topupPayChannelBusiness.findKFTCityCodeByProvinceAndCityName(cityName);
        if (StringUtils.isBlank(kftCityCode)) {
            return ResultWrap.init(CommonConstants.FALIED, "未匹配到城市编号");
        }
        dto.setCityCode(kftCityCode);
        dto.setCustCardValidDate(this.expiredTimeToMMYY(parameter.getExpiredTime()));//可空，信用卡扣款时必填
        dto.setCustCardCvv2(parameter.getSecurityCode());//可空，信用卡扣款时必填
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        dto.setSourceIP(Util.getIpAddress(request));
        double rate;
        if (StringUtils.isBlank(parameter.getRate())) {
            return ResultWrap.init(CommonConstants.FALIED, "费率不能为空");
        }
        rate = Double.parseDouble(parameter.getRate());
        logger.info("============手续费rate={}", rate);
        String rateAmount = String.valueOf(Math.round(rate * Integer.parseInt(yuan2Fen(amount))));
        dto.setRateAmount(rateAmount);// 手续费
        dto.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/kftdh/treaty/notifycall");//回调地址
        logger.info("请求参数:" + dto);
        SameIDCreditCardTreatyCollectResultDTO result = gbpService
                .sameIDCreditCardTreatyCollect(dto);
        logger.info("响应结果:" + result);
        //destory();
        if (result.getStatus() == 1) {
            logger.info("交易成功，协议代扣流水号thirdOrderCode-----------" + result.getOrderNo() + ",交易金额：" + amount);
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);

            if (prp == null) {
                logger.info("订单号有误，未找到支付信息，orderNo={}", orderNo);
            }

            logger.info("*********************交易成功***********************");

            RestTemplate restTemplate = new RestTemplate();
            String url;

            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            //url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", orderNo);
            requestEntity.add("third_code", thirdOrderCode);
            try {
                restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("", e);
            }

            logger.info("订单状态修改成功===================" + orderNo + "====================" + result);

            logger.info("订单已交易成功!");

            return ResultWrap.init(CommonConstants.SUCCESS, "Success");

        } else if (result.getStatus() == 0) {
            logger.info("协议代扣处理中！！！！！！");
            return ResultWrap.init("999998", "Processing");
        } else {
            logger.info("协议代扣失败！！！！！！！！failureDeatails={}",result.getFailureDetails());
            return ResultWrap.init(CommonConstants.FALIED, result.getFailureDetails());
        }
    }


/**
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @Author zhangchaofeng
     * @Description // 单笔付款(包鉴权)
     * @Date 20:16 2019/5/6
     * @Param [orderCode]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kftdh/excutePayByRule")
    @ResponseBody
    public Map<String, Object> excutePayByRule(@RequestParam String orderCode,
                                               @RequestParam(value = "custAccountCreditOrDebit", required = false, defaultValue = "2") String custAccountCreditOrDebit)
            throws Exception {
        logger.info("进入单笔代还接口，orderCode={}===========", orderCode);
        PaymentRequestParameter parameter = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> maps = Maps.newHashMap();
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(parameter.getCreditCardBankName());
        if (kftBankCodeByName == null || kftBankCodeByName.size() == 0) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "单笔付款匹配到账卡信息失败");
            return maps;
        }
        for (String bankCode : kftBankCodeByName) {
            logger.info("快付通银行代号：{}", bankCode);
        }
        SameIDCreditCardPayToBankAccountDTO dto = new SameIDCreditCardPayToBankAccountDTO();
        setCommonParam(dto, orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_verify_pay");// 接口名称，固定不变
        dto.setProductNo("GBP00013");// 替换成快付通提供的产品编号，测试生产不一样
        //生成一个单笔付款的订单号，在原有的订单号上拼上一个随机两位的串
        String thirdOrderCode = orderCode + RandomUtils.generateString(2);
        logger.info("=========orderCode:{}----单笔代付订单号thirdOrderCode:{}", orderCode, thirdOrderCode);
        dto.setOrderNo(thirdOrderCode);// 订单号同一个商户必须保证唯一
        dto.setTradeName("同名卡单笔付款");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(yuan2Fen(parameter.getRealAmount()));// 单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档

        dto.setCustBankNo(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo(parameter.getBankCard());// 本次交易中,对客户的哪张卡上付钱
        dto.setCustPhone(parameter.getCreditCardPhone()); //客户手机号
        dto.setCustName(parameter.getUserName());// 付款人的真实姓名
        dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit(custAccountCreditOrDebit);// 客户账户借记贷记类型,0存折 1借记 2贷记

        dto.setCustCardValidDate(this.expiredTimeToMMYY(parameter.getExpiredTime()));//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        dto.setCustCardCvv2(parameter.getSecurityCode());//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        //dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
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
        if (result.getStatus() == 1 || result.getStatus() == 0) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "交易成功");
            if (result.getStatus() == 1) {
                //直接返终态就没有回调 1成功和2失败是终态 0处理中不是终态
                //如果是1，则代表成功，则修改订单得状态
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String url;
                logger.info("*********************交易成功***********************");
                url = parameter.getIpAddress() + ChannelUtils.getCallBackUrl(parameter.getIpAddress());
                //url = parameter.getIpAddress() + "/v1.0/transactionclear/payment/update";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code", thirdOrderCode);
                try {
                    restTemplate.postForObject(url, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
                logger.info("订单状态修改成功===================" + orderCode + "====================" + result);
            }

        } else {
            logger.info("============还款失败，orderNo={}，失败原因={}",orderCode,result.getFailureDetails());
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails() + "-----代付单号=" + thirdOrderCode);
        }
        return maps;
    }




/**
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @Author zhangchaofeng
     * @Description // 手动单笔付款(包鉴权)
     * @Date 20:16 2019/5/6
     * @Param [orderCode]
     **/

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kftdh/excutePayByRuleManual")
    @ResponseBody
    public Map<String, Object> excutePayByRuleManual(@RequestParam String orderCode,
    												@RequestParam String amountFen,
                                               @RequestParam(value = "custAccountCreditOrDebit", required = false, defaultValue = "2") String custAccountCreditOrDebit)
            throws Exception {
        logger.info("进入单笔代还接口，orderCode={}===========", orderCode);
        PaymentRequestParameter parameter = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> maps = Maps.newHashMap();
        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(parameter.getCreditCardBankName());
        if (kftBankCodeByName == null || kftBankCodeByName.size() == 0) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "单笔付款匹配到账卡信息失败");
            return maps;
        }
        for (String bankCode : kftBankCodeByName) {
            logger.info("快付通银行代号：{}", bankCode);
        }
        SameIDCreditCardPayToBankAccountDTO dto = new SameIDCreditCardPayToBankAccountDTO();
        setCommonParam(dto, orderCode);// 通用参数设定 请求编号 商户ID 接口版本号
        dto.setService("gbp_same_id_credit_card_verify_pay");// 接口名称，固定不变
        dto.setProductNo("GBP00013");// 替换成快付通提供的产品编号，测试生产不一样
        //生成一个单笔付款的订单号，在原有的订单号上拼上一个随机两位的串
        String thirdOrderCode = orderCode + RandomUtils.generateString(2);
        logger.info("=========orderCode:{}----单笔代付订单号thirdOrderCode:{}", orderCode, thirdOrderCode);
        dto.setOrderNo(thirdOrderCode);// 订单号同一个商户必须保证唯一
        dto.setTradeName("同名卡单笔付款");// 简要概括此次交易的内容
        // dto.setMerchantBankAccountNo("商户对公账号");//
        // 商户用于收款的银行账户,资金不落地模式时必填（重要参数）
        dto.setTradeTime(new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()));// 交易时间,注意此时间取值一般为商户方系统时间而非快付通生成此时间
        dto.setAmount(amountFen);// 单位:分,不支持小数点
        dto.setCurrency("CNY");// 快付通定义的扣费币种,详情请看文档

        dto.setCustBankNo(kftBankCodeByName.get(0));// 客户银行账户行别;快付通定义的行别号,详情请看文档
        // dto.setCustBankAccountIssuerNo("302584044253");//支行行号,可空
        dto.setCustBankAccountNo(parameter.getBankCard());// 本次交易中,对客户的哪张卡上付钱
        dto.setCustPhone(parameter.getCreditCardPhone()); //客户手机号
        dto.setCustName(parameter.getUserName());// 付款人的真实姓名
        dto.setCustBankAcctType("1");// 可空，指客户的银行账户是个人账户还是企业账户 1个人 2企业
        dto.setCustAccountCreditOrDebit(custAccountCreditOrDebit);// 客户账户借记贷记类型,0存折 1借记 2贷记

        dto.setCustCardValidDate(this.expiredTimeToMMYY(parameter.getExpiredTime()));//可空，信用卡的正下方的四位数，前两位是月份，后两位是年份；如果客户付款的信用卡，不可空
        dto.setCustCardCvv2(parameter.getSecurityCode());//可空，信用卡的背面的三位数，如果客户付款的信用卡，不可空
        //dto.setInstalments("12");//可空，信用卡分期付款时需填写此值
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
        if (result.getStatus() == 1 || result.getStatus() == 0) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            maps.put(CommonConstants.RESP_MESSAGE, "交易成功");
            if (result.getStatus() == 1) {
                //直接返终态就没有回调 1成功和2失败是终态 0处理中不是终态
                //如果是1，则代表成功，则修改订单得状态
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                String url;
                logger.info("*********************交易成功***********************");
                url = parameter.getIpAddress() + ChannelUtils.getCallBackUrl(parameter.getIpAddress());
                //url = parameter.getIpAddress() + "/v1.0/transactionclear/payment/update";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", orderCode);
                requestEntity.add("third_code", thirdOrderCode);
                try {
                    restTemplate.postForObject(url, requestEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
                logger.info("订单状态修改成功===================" + orderCode + "====================" + result);
            }

        } else {
            logger.info("============还款失败，orderNo={}，失败原因={}",orderCode,result.getFailureDetails());
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, result.getFailureDetails() + "-----代付单号=" + thirdOrderCode);
        }
        return maps;
    }




/**
     * @Author zhangchaofeng
     * @Description 快捷支付异步通知接口
     * @Date 19:05 2019/5/6
     * @Param [request, response]
     * @return void
     **/

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET}, value = "/v1.0/paymentgateway/topup/kftdh/treaty/notifycall")
    public void hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("KFT快捷支付异步通知进来了=======");

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] strings = parameterMap.get(key);
            for (String s : strings) {
                logger.info(key + "=============" + s);
            }
        }
        String orderNo = request.getParameter("orderNo");
        String status = request.getParameter("status");

        if ("1".equals(status)) {
            String merchantNo = request.getParameter("merchantId");
            String amount = request.getParameter("amount");

            logger.info("交易流水号orderCode-----------" + orderNo + ",交易金额：" + amount);
            logger.info("交易商户号merchantNo-----------" + merchantNo);
            logger.info("交易状态orderStatus-----------" + status);

            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);

            logger.info("*********************交易成功***********************");

            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            String url = null;
            String result = null;

            url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
            //url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("order_code", orderNo);
            requestEntity.add("third_code", "");
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("", e);
            }

            logger.info("订单状态修改成功===================" + orderNo + "====================" + result);

            logger.info("订单已交易成功!");

            PrintWriter pw = response.getWriter();
            pw.print("success");
            pw.close();
        } else {
            String failureDetails = request.getParameter("failureDetails");
            logger.info("orderNo={}交易异常!detail={}", orderNo, failureDetails);

            PrintWriter pw = response.getWriter();
            pw.print("failed");
            pw.close();
        }
    }



/**
     * @return java.lang.String
     * @Author zhangchaofeng
     * @Description jump to bindcard page
     * @Date 17:03 2019/5/22
     * @Param [request, response, model]
     **/

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tokft/bindcard")
    public String returnGHTBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String expiredTime = request.getParameter("expiredTime");
        String securityCode = request.getParameter("securityCode");
        String bankName = request.getParameter("bankName");
        String cardType = request.getParameter("cardType");
        String bankCard = request.getParameter("bankCard");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String userName = request.getParameter("userName");
        String ipAddress = request.getParameter("ipAddress");

        model.addAttribute("expiredTime", expiredTime);
        model.addAttribute("securityCode", securityCode);
        model.addAttribute("bankName", bankName);
        model.addAttribute("cardType", cardType);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("idCard", idCard);
        model.addAttribute("phone", phone);
        model.addAttribute("userName", userName);
        model.addAttribute("ipAddress", ipAddress);

        return "kftbindcard";
    }


}

