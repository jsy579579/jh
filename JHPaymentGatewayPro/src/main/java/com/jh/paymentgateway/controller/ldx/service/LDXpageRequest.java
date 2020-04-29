package com.jh.paymentgateway.controller.ldx.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.ldx.business.LDXAreaBusiness;
import com.jh.paymentgateway.controller.ldx.business.LDXBindCardBusiness;
import com.jh.paymentgateway.controller.ldx.business.LDXRegisterBusiness;
import com.jh.paymentgateway.controller.ldx.pojo.LDXArea;
import com.jh.paymentgateway.controller.ldx.pojo.LDXBindCard;
import com.jh.paymentgateway.controller.ldx.pojo.LDXRegister;
import com.jh.paymentgateway.controller.ldx.util.HttpsClientUtil;
import com.jh.paymentgateway.controller.ldx.util.SignUtil;
import com.jh.paymentgateway.controller.qysh.dao.QYSHRegisterBusiness;
import com.jh.paymentgateway.controller.qysh.pojo.QYSHBankBranch;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;


@Controller
@EnableAutoConfiguration
public class LDXpageRequest extends BaseChannel {


    public static String key;

    //联动优势小额
    public static String MERCHANT_NO;//机构号  小

    private static final Logger LOG = LoggerFactory.getLogger(LDXpageRequest.class);

    private static final String URL = "http://pay.juxingzhifu.com";

    @Value("${ldx.partnerNo}")
    public void setPrivateKey(String merchantNo){
        MERCHANT_NO=merchantNo;
    }

    @Value("${ldx.key}")
    public void setpublicKey(String key1){
        key=key1;
    }



    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    LDXRegisterBusiness ldxRegisterBusiness;
    
    @Autowired
    private LDXBindCardBusiness ldxBindCardBusiness;

    @Autowired
    private QYSHRegisterBusiness qyshRegisterBusiness;

    @Autowired
    private LDXAreaBusiness ldxAreaBusiness;

    @Value("${payment.ipAddress}")
    private String ip;


    // 进件注册
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/Dockentrance")
    public @ResponseBody
    Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
                        @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "userName") String userName, @RequestParam(value = "securityCode") String securityCode,
                        @RequestParam(value = "expiredTime") String expiredTimes, @RequestParam(value = "rate") String rate,
                        @RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName,
                        @RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "dphone") String dphone,
                        @RequestParam(value = "dbankName") String dbankName) throws Exception {
        String expiredTime = this.expiredTimeToMMYY(expiredTimes);
        LOG.debug("联动小额还款进件注册信息=======" + dbankCard + "====" + dbankName + "====" + dphone + "====" + bankCard + "====" + bankName + "====" + phone);
        //储蓄卡信息
        String debitCardNo = dbankCard;
        String debitCardName = dbankName;
        String debitCardPhone = dphone;

        Map<String, Object> maps = new HashMap<String, Object>();
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
        //是否进件
        if (ldxRegister == null) {
            debitCardName = Util.queryBankNameByBranchName(debitCardName);
            maps = (Map<String, Object>) this.toReg(idCard, userName, debitCardName, debitCardNo, debitCardPhone);
            if (!"000000".equals(maps.get("resp_code"))) {
                return maps;
            }
        } else {
            if (!debitCardNo.equals(ldxRegister.getBankCard()) || !debitCardPhone.equals(ldxRegister.getPhone())) {
                LOG.info("===================联动小额还款用户储蓄卡信息是否改变，去修改注册信息===========================");
                debitCardName = Util.queryBankNameByBranchName(debitCardName);
                maps = (Map<String, Object>) this.toUpdateReg(idCard, userName, debitCardName, debitCardNo, debitCardPhone);
                if (!"000000".equals(maps.get("resp_code"))) {
                    return maps;
                }
            }
        }

        //是否绑卡
        if (ldxBindCard == null || "0".equals(ldxBindCard.getStatus())) {
            //跳转到绑卡页面
            maps.put(CommonConstants.RESP_CODE, "999996");
            maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
            maps.put(CommonConstants.RESULT,
                    ip + "/v1.0/paymentgateway/repayment/ldx/bindcard?bankCard=" + bankCard
                            + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&cardType="
                            + URLEncoder.encode("0", "UTF-8") + "&idCard=" + idCard + "&phone=" + phone
                            + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
                            + "&rate=" + rate + "&extraFee=" + extraFee
                            + "&userName=" + userName + "&ipAddress=" + ip);
            return maps;
        } else {
            String bigExtraFee = new BigDecimal(extraFee).setScale(1).toString();
            // 判断用户是否修改费率或单笔手续费
            if (!rate.equals(ldxBindCard.getRate()) | !bigExtraFee.equals(ldxBindCard.getExtraFee())) {
                LOG.info("联动小额还款用户修改本地费率===== " + ldxBindCard.getBankCard() + "====" + rate + "==" + bigExtraFee);
                ldxBindCard.setRate(rate);
                ldxBindCard.setExtraFee(bigExtraFee);
                ldxBindCard.setUpdateTime(new Date());
                ldxBindCardBusiness.create(ldxBindCard);
            }
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "已签约");
    }

    /**
     * 注册接口
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/toReg")
    public @ResponseBody
    Object toReg(
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "debitCardName") String debitCardName,
            @RequestParam(value = "debitCardNo") String debitCardNo,
            @RequestParam(value = "debitCardPhone") String debitCardPhone) {
        LOG.info("=========联动小额还款注册接口===========");
        Map<String, Object> map = new HashMap<>();
        String address = "/api/register";
        String branchNo = "000000000000";
        QYSHBankBranch ykBankBranchNo = qyshRegisterBusiness.getQYSHbanbranch(debitCardName);
        if (ykBankBranchNo != null) {
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
        signmap.put("bank_branch_name", debitCardName + "上海市安亭支行");//支行名称
        signmap.put("bank_branch_no", branchNo);//支行联行号
        signmap.put("bank_cardno", debitCardNo);//储蓄卡号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);
        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]注册请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            LOG.info("[联动小额还款]注册成功");
            String merchantno = JSON.getString("merchantno");
            msg = JSON.getString("message");
            LDXRegister ldxRegister = new LDXRegister();
            ldxRegister.setIdCard(idCard);
            ldxRegister.setRealName(userName);
            ldxRegister.setBankCard(debitCardNo);
            ldxRegister.setPhone(debitCardPhone);
            ldxRegister.setMerchantNo(merchantno);
            ldxRegister.setCreateTime(new Date());
            ldxRegisterBusiness.create(ldxRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册成功"); // 描述
            return map;
        } else if("1".equals(resultCode)&&JSON.getString("errCodeDes").contains("重复注册")){//已进件
            LOG.info("[联动小额]已注册，将用户商户号存入数据库,idCard====="+idCard);
            String errCodeDes = JSON.getString("errCodeDes");
            String merchantno=processMerchantNo(errCodeDes);
            msg = JSON.getString("message");
            LDXRegister ldxRegister= ldxRegisterBusiness.queryByIdCard(idCard);
            if(ldxRegister==null){
                LDXRegister ldxRegister1 = new LDXRegister();
                ldxRegister1.setIdCard(idCard);
                ldxRegister1.setRealName(userName);
                ldxRegister1.setBankCard(debitCardNo);
                ldxRegister1.setPhone(debitCardPhone);
                ldxRegister1.setMerchantNo(merchantno);
                ldxRegister1.setCreateTime(new Date());
                ldxRegister1.setUpdateTime(new Date());
                ldxRegisterBusiness.create(ldxRegister1);
            }else{
                ldxRegister.setIdCard(idCard);
                ldxRegister.setRealName(userName);
                ldxRegister.setBankCard(debitCardNo);
                ldxRegister.setPhone(debitCardPhone);
                ldxRegister.setMerchantNo(merchantno);
                ldxRegister.setCreateTime(new Date());
                ldxRegister.setUpdateTime(new Date());
                ldxRegisterBusiness.create(ldxRegister);
            }

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册成功"); // 描述
            return map;
        }else {
            LOG.info("[联动小额还款]注册失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 修改注册信息接口
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/toUpdateReg")
    public @ResponseBody
    Object toUpdateReg(
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "debitCardName") String debitCardName,
            @RequestParam(value = "debitCardNo") String debitCardNo,
            @RequestParam(value = "debitCardPhone") String debitCardPhone) {
        LOG.info("=========联动小额还款修改注册信息接口===========");
        Map<String, Object> map = new HashMap<>();
        String branchNo = "000000000000";
        QYSHBankBranch ykBankBranchNo = qyshRegisterBusiness.getQYSHbanbranch(debitCardName);
        if (ykBankBranchNo != null) {
            branchNo = ykBankBranchNo.getBankbranchNo();
        }
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ldxRegister.getMerchantNo();
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
        signmap.put("bank_branch_name", debitCardName + "上海市安亭支行");//支行名称
        signmap.put("bank_branch_no", branchNo);//支行联行号
        signmap.put("bank_cardno", debitCardNo);//储蓄卡号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]修改注册信息请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            LOG.info("[联动小额还款]修改注册信息成功");
            ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
            ldxRegister.setBankCard(debitCardNo);
            ldxRegister.setPhone(debitCardPhone);
            ldxRegister.setRealName(userName);
            ldxRegister.setUpdateTime(new Date());
            ldxRegisterBusiness.create(ldxRegister);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "注册信息修改成功"); // 描述
            return map;
        } else {
            LOG.info("[联动小额还款]修改注册信息失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    // 跳转到绑卡页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/repayment/ldx/bindcard")
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

        return "ldxbindcard";
    }

    /**
     * 信用卡绑卡发送验证码接口
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/toSendSms")
    public @ResponseBody
    Object toSendSms(
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
        LOG.info("=========联动小额还款绑卡验证码发送接口===========");
        expiredTime = this.expiredTimeToMMYY(expiredTime);
        String bigExtraFee = new BigDecimal(extraFee).setScale(1).toString();
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ldxRegister.getMerchantNo();
        Map<String, Object> map = new HashMap<>();
        String address = "/api/sendMessage";
        // 交易流水号
        String openOrderNum = UUID.randomUUID().toString().replaceAll("-", "");
        LOG.info("[联动小额还款]流水号===========" + openOrderNum);
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
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
        if (ldxBindCard == null) {
            ldxBindCard = new LDXBindCard();
            ldxBindCard.setCreateTime(new Date());
        }
        ldxBindCard.setRealName(userName);
        ldxBindCard.setIdCard(idCard);
        ldxBindCard.setBankCard(bankCard);
        ldxBindCard.setPhone(phone);
        ldxBindCard.setExpiredTime(expiredTime);
        ldxBindCard.setSecurityCode(securityCode);
        ldxBindCard.setRate(rate);
        ldxBindCard.setExtraFee(bigExtraFee);
        ldxBindCard.setStatus("0");
        ldxBindCard.setOrderNum(openOrderNum);
        ldxBindCard.setMerchantNo(merchantNo);
        ldxBindCard.setUpdateTime(new Date());
        ldxBindCardBusiness.create(ldxBindCard);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]绑卡请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if("0".equals(resultCode)&&JSON.getString("message").contains("开卡成功")) {
            LOG.info("用户已绑卡=====================bankcard===="+bankCard);
            ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
            if (ldxBindCard == null) {
                return ResultWrap.init(CommonConstants.FALIED, "查询绑卡信息出错!");
            }
            ldxBindCard.setStatus("1");
            ldxBindCardBusiness.create(ldxBindCard);
            map.put(CommonConstants.RESP_CODE, "666666");
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            map.put("redirect_url", ip + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
            return map;
        }
        if ("0".equals(resultCode)) {
            msg = JSON.getString("message");
            LOG.info("[智极还]绑卡短信请求成功" + msg);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            map.put("requestNo", openOrderNum);//绑卡流水号
            map.put("idCard", idCard);//证件号
            return map;
        }else{
            LOG.info("[联动小额还款]请求发短信失败");
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
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/confirmSms")
    public @ResponseBody
    Object confirmSms(
            @RequestParam(value = "requestNo") String requestNo,
            @RequestParam(value = "bankCard") String bankCard,
            @RequestParam(value = "idCard") String idCard,
            @RequestParam(value = "smsCode") String smsCode) {
        LOG.info("联动小额还款短信确认接收参数=======" + requestNo + "==" + bankCard + "==" + idCard + "==" + smsCode);
        LOG.info("=========联动小额还款确认验证码接口===========");
        Map<String, Object> map = new HashMap<>();
        // 绑卡交易流水号
        String openOrderNum = requestNo;
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ldxRegister.getMerchantNo();
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
            LOG.info("[联动小额还款]请求参数==" + json);
            LOG.info("[联动小额还款]请求地址==" + URL + address);
            String result = null;
            result = HttpsClientUtil.sendRequest(URL + address, json);
            JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
            LOG.info("[联动小额还款]请求结果==" + result);

            String returnCode = JSON.getString("returnCode");
            String msg = null;
            if (!"0".equals(returnCode)) {
                LOG.info("[联动小额还款]短com.jh.paymentgateway.controller.qysh.YKHKEntrance信确认请求通讯失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
                return map;
            }
            String resultCode = JSON.getString("resultCode");
            if ("0".equals(resultCode)) {
                msg = JSON.getString("message");
                LOG.info("[联动小额还款]短信确认成功==" + msg);
                LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
                if (ldxBindCard == null) {
                    return ResultWrap.init(CommonConstants.FALIED, "查询绑卡信息出错!");
                }
                ldxBindCard.setStatus("1");
                ldxBindCardBusiness.create(ldxBindCard);

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "绑卡成功"); // 描述
                map.put("redirect_url", ip + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
                return map;
            } else {
                LOG.info("[联动小额还款]短信确认失败");
                msg = JSON.getString("errCodeDes");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
                return map;
            }
        } catch (Exception e) {
            LOG.error("请求异常", e);
            map.clear();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
            return map;
        }

    }

    /**
     * 开始消费
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/toPay")
    public @ResponseBody
    Object toPay(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[联动小额还款]开始消费================================" + orderCode);
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        LOG.info("=============" + prp.toString());
        Map<String, Object> map = new HashMap<>();
        String amount = prp.getRealAmount();//扣款金额
        String rate = prp.getRate();//费率
        String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
        LOG.info("[联动小额还款]消费金额==" + amount + "====费率===" + bigRate);
        String phone = prp.getPhone();//信用卡预留手机号
        String bankCard = prp.getBankCard();//信用卡号
        String bankName = prp.getCreditCardBankName();//信用卡名称
        String extra = prp.getExtra();//上海市-上海市
        String outTradeNo = orderCode;//流水号

        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ldxBindCard.getMerchantNo();//商户号


        try {
            //String[] companyname = extra.split("-");
            /*String province = companyname[2];
            String city = companyname[3];*/
            //String companyName = companyname[2] + "-" + companyname[3];

            String[] companyname = extra.split("\\|");
            String companyName = companyname[2];
            // List<LDXArea> lists = null;
//            if (province.contains("省") && city.contains("市")) {
//                //省 市
//                lists = merchantBusiness.getAllByProvinceAndCity(province, city);
//            } else if (province.contains("市") && city.contains("区")) {
//                //市 区
//                lists = merchantBusiness.getByCityAndCounty(province, city);
//            } else if (province.contains("省") && city.contains("区")) {
//                //省 区
//                lists = merchantBusiness.getByProvinceAndCounty(province, city);
//            } else {
//                //省 市 模糊查询
//                lists = merchantBusiness.getByProvinceAndCityLike(province, city);
//            }
            /*lists=ldxAreaBusiness.getAllByCity(city);


            Random random=new Random();
            String areaCode = null;
            String companyCode=null;
            String companyName=null;
            if (lists.size() > 1) {
                int num=random.nextInt(lists.size()-1);
                companyCode = lists.get(num).getCompanyCode();
                areaCode=lists.get(num).getCityCode();
                companyCode=new BigDecimal(companyCode).setScale(0).toString();
                areaCode=new BigDecimal(areaCode).setScale(0).toString();
                companyName=areaCode+"-"+companyCode;
            }else if(lists.size() ==1){
                companyCode = lists.get(0).getCompanyCode();
                areaCode=lists.get(0).getCityCode();
                companyCode=new BigDecimal(companyCode).setScale(0).toString();
                areaCode=new BigDecimal(areaCode).setScale(0).toString();
                companyName=areaCode+"-"+companyCode;
            }*/
            LOG.info("联动小额orderCode======="+orderCode+"======地区码====="+companyName);
            String address = "/api/createOrder";
            LOG.info("[联动小额还款]请求流水号========" + outTradeNo);
            Map<String, String> signmap = new HashMap<String, String>();
            signmap.put("mechanism_id", MERCHANT_NO);//机构编号
            signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
            signmap.put("outTradeNo", outTradeNo);//开卡订单号
            signmap.put("rate", bigRate);//费率
            signmap.put("bankcardNum", bankCard);//消费卡号
            if(companyName!=null){
                signmap.put("company_name", companyName);//自定义账单名称
            }
            signmap.put("amount", amount);//金额 元
//        signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/ldx/payNotifyUrl");//异步回调地址
            signmap.put("notifyUrl", ip + "/v1.0/paymentgateway/repayment/ldx/payNotifyUrl");

            String sign = SignUtil.genSign(signmap, key);
            signmap.put("sign", sign);
            String json = JSON.toJSONString(signmap);
            LOG.info("[联动小额还款]请求参数==" + json);
            LOG.info("[联动小额还款]请求地址==" + URL + address);
            String result = null;
            result = HttpsClientUtil.sendRequest(URL + address, json);
            JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
            LOG.info("[联动小额还款]请求结果==" + result);

            String returnCode = JSON.getString("returnCode");
            String msg = null;
            if (!"0".equals(returnCode)) {
                LOG.info("[联动小额还款]消费请求通讯失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
                return map;
            }
            String resultCode = JSON.getString("resultCode");
            outTradeNo = JSON.getString("outTradeNo");
            if ("0".equals(resultCode)) {
                LOG.info("[联动小额还款]消费交易成功========" + outTradeNo);
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                return map;
            } else {
                LOG.info("[联动小额还款]消费交易失败");
                msg = JSON.getString("message");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
                return map;
            }
        } catch (Exception e) {
            LOG.error("请求异常", e);
            map.clear();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付失败"); // 描述
        }
        return map;
    }


    /**
     * 消费异步回调
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/repayment/ldx/payNotifyUrl")
    public void payNotifyUrl(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LOG.info("[联动小额还款]进入消费回调================================");
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());

            LOG.info("[联动小额还款]消费回调参数信息： " + jsonObject.toJSONString());
            String paystate = jsonObject.getString("paystate");
            String dsorderid = jsonObject.getString("outTradeNo");//返回流水号
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
            if ("1".equals(paystate)) {
                LOG.info("***********联动小额还款消费交易成功***********************");
                RestTemplate restTemplate = new RestTemplate();
                String version = "53";
                String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
//                String url = "http://139.196.125.48/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("orderCode", dsorderid);
                requestEntity.add("version", version);
                String result = null;
                net.sf.json.JSONObject jsonObjects;
                net.sf.json.JSONObject resultObj;
                try {
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================" + result);
                    jsonObjects = net.sf.json.JSONObject.fromObject(result);
                    resultObj = jsonObjects.getJSONObject("result");
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("", e);
                }

                url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
//                url = "http://139.196.125.48" + "/v1.0/transactionclear/payment/update";

                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", dsorderid);
                //        requestEntity.add("third_code", orderid); // 第三方订单号

                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);
                LOG.info("订单已支付!");
            } else {
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
     * 消费订单查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/payQuery")
    public @ResponseBody
    Object payQuery(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[联动小额还款]开始消费订单查询================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String bankCard = prp.getBankCard();//信用卡号
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ldxBindCard.getMerchantNo();//商户号

        String address = "/get/orderInfo";
        String outTradeNo = orderCode;
        LOG.info("[联动小额还款]请求流水号========" + outTradeNo);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("outTradeNo", outTradeNo);//开卡订单号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            String paystate = JSON.getString("paystate");
            if ("1".equals(paystate)) {
                LOG.info("[联动小额还款]订单交易成功========");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                return map;
            } else if ("4".equals(paystate) || "5".equals(paystate) || "6".equals(paystate)) {
                LOG.info("[联动小额还款]订单交易处理中========");
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "交易处理中");
                return map;
            } else {
                LOG.info("[联动小额还款]订单交易失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "订单交易失败"); // 描述
                return map;
            }
        } else {
            LOG.info("[联动小额还款]订单交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 余额查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/balanceQuery")
    public @ResponseBody
    Object balanceQuery(@RequestParam(value = "idCard") String idCard) {
        LOG.info("[联动小额还款]开始进行余额查询=======================");
        Map<String, Object> map = new HashMap<>();
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        if (ldxRegister == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该卡号不存在"); // 描述
            return map;
        }
        String merchantNo = ldxRegister.getMerchantNo();//商户号
        String address = "/get/banlance";
        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        //signmap.put("cardNumber", bankCard);//填写改卡号则为银行卡余额模式使用,除非必要要求否则不填

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            String banlance = JSON.getString("banlance");
            LOG.info("[联动小额还款]商户余额为==" + banlance);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "商户余额为" + banlance); // 描述
            return map;
        } else {
            LOG.info("[联动小额还款]余额查询失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 开始代付
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/toSettle")
    public @ResponseBody
    Object toSettle(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[联动小额还款]开始代付================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String idCard = prp.getIdCard();
        String bankCard = prp.getBankCard();
        String realAmount = prp.getRealAmount();
        String extraFee = prp.getExtraFee();
        BigDecimal cashAmount = new BigDecimal(realAmount).add(new BigDecimal(extraFee)).setScale(2);
        LOG.info("[联动小额还款]还款金额：================" + realAmount + "$$" + cashAmount.toString());
        String fee = new BigDecimal(extraFee).setScale(1).toString();
        LDXRegister ldxRegister = ldxRegisterBusiness.queryByIdCard(idCard);
        String merchantNo = ldxRegister.getMerchantNo();//商户号

        String address = "/api/createMonOrder";
        String repayOrderNum = orderCode;
        LOG.info("联动小额还款代付请求流水号========" + repayOrderNum);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("repayOrderNum", repayOrderNum);//开卡订单号
        signmap.put("fee", fee);//订单单笔手续费 （单位元）
        signmap.put("bankcardNum", bankCard);//开卡成功的卡号
        signmap.put("amount", cashAmount.toString());//金额 元 (到账进额=amount-fee)
//        signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/ldx/settleNotifyUrl");//异步回调地址
        signmap.put("notifyUrl", ip + "/v1.0/paymentgateway/repayment/ldx/settleNotifyUrl");

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]代付请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            LOG.info("[联动小额还款]代付交易成功========");
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行出款");
            return map;
        } else {
            LOG.info("[联动小额还款]代付交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 代付订单查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/settleQuery")
    public @ResponseBody
    Object settleQuery(@RequestParam(value = "orderCode") String orderCode) {
        LOG.info("[联动小额还款]开始代付订单查询================================");
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        Map<String, Object> map = new HashMap<>();
        String bankCard = prp.getBankCard();//信用卡号
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankCard);
        String merchantNo = ldxBindCard.getMerchantNo();//商户号

        String address = "/get/reapayOrderInfo";
        String RepayOutTradeNo = orderCode;
        LOG.info("请求流水号========" + RepayOutTradeNo);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("RepayOutTradeNo", RepayOutTradeNo);//开卡订单号

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]消费请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            String status = JSON.getString("status");
            if ("1".equals(status)) {
                LOG.info("[联动小额还款]订单交易成功========");
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
                return map;
            } else if ("0".equals(status)) {
                LOG.info("[联动小额还款]订单交易处理中========");
                map.put(CommonConstants.RESP_CODE, "999998");
                map.put(CommonConstants.RESP_MESSAGE, "交易处理中");
                return map;
            } else {
                LOG.info("[联动小额还款]订单交易失败");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "订单交易失败"); // 描述
                return map;
            }
        } else {
            LOG.info("[联动小额还款]订单交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 还款异步回调
     */
    @RequestMapping(method = {RequestMethod.POST,
            RequestMethod.GET}, value = "/v1.0/paymentgateway/repayment/ldx/settleNotifyUrl")
    public void settleNotifyUrl(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LOG.info("[联动小额还款]进入代付回调================================");
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());

            LOG.info("[联动小额还款]还款回调参数信息： " + jsonObject.toJSONString());
            String repayState = jsonObject.getString("repayState");
            String dsorderid = jsonObject.getString("outTradeNo");//返回流水号
            PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
            if ("1".equals(repayState)) {
                LOG.info("***********联动小额还款代付交易成功***********************");
                RestTemplate restTemplate = new RestTemplate();
                String version = "53";
//        		String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                String url = ip + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("orderCode", dsorderid);
                requestEntity.add("version", version);
                String result = null;
                net.sf.json.JSONObject jsonObjects;
                net.sf.json.JSONObject resultObj;
                try {
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================" + result);
                    jsonObjects = net.sf.json.JSONObject.fromObject(result);
                    resultObj = jsonObjects.getJSONObject("result");
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("", e);
                }

//        		url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
                url = ip + "/v1.0/transactionclear/payment/update";

                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("status", "1");
                requestEntity.add("order_code", dsorderid);
                //        requestEntity.add("third_code", orderid); // 第三方订单号

                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);
                LOG.info("订单已支付!");
            } else {
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
     *
     * @param bankCard   代付的储蓄卡号或信用卡号（必须是已开卡的卡号）
     * @param merchantNo 用户在通道注册进件时返回的 商户号
     * @param realAmount 手动代还金额=余额-手续费（如 30.83-0.5=30.33 ）
     * @param extraFee   手续费（如 0.5）
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/transferbymanual")
    public @ResponseBody
    Object transferByManual(@RequestParam(value = "bankCard") String bankCard,
                            @RequestParam(value = "merchantNo") String merchantNo, @RequestParam(value = "realAmount") String realAmount,
                            @RequestParam(value = "extraFee", required = true, defaultValue = "0.5") String extraFee)
            throws Exception {
        LOG.info("[联动小额还款]-===============进入手动代付=====================");
        Map<String, String> map = new HashMap<String, String>();
        String fee = new BigDecimal(extraFee).setScale(1).toString();
        String cashAmount = new BigDecimal(realAmount).add(new BigDecimal(fee)).setScale(2).toString();//代付金额
        LOG.info("[联动小额还款]获取到的代付金额：===============" + cashAmount + "====手续费===" + fee);

        String address = "/api/createMonOrder";
        String repayOrderNum = UUID.randomUUID().toString().replaceAll("-", "");
        LOG.info("[联动小额还款]生成的代付订单号========" + repayOrderNum);

        Map<String, String> signmap = new HashMap<String, String>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno", merchantNo);//入网成功后返回的merchantno
        signmap.put("repayOrderNum", repayOrderNum);//开卡订单号
        signmap.put("fee", fee);//订单单笔手续费 （单位元）
        signmap.put("bankcardNum", bankCard);//开卡成功的卡号
        signmap.put("amount", cashAmount);//金额 元 (到账进额=amount-fee)
        //signmap.put("notifyUrl", ip+"/v1.0/paymentgateway/repayment/ldx/settleNotifyUrl");//异步回调地址（不是必穿）

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject JSON = com.alibaba.fastjson.JSON.parseObject(result);
        LOG.info("[联动小额还款]请求结果==" + result);

        String returnCode = JSON.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]代付请求通讯失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求通讯失败"); // 描述
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        }
        String resultCode = JSON.getString("resultCode");
        if ("0".equals(resultCode)) {
            LOG.info("[联动小额还款]代付交易成功========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        } else {
            LOG.info("[联动小额还款]代付交易失败");
            msg = JSON.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            map.put("orderNum", repayOrderNum); // 请求单号
            return map;
        }
    }

    /**
     * 商户查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/queryHCProvince")
    public @ResponseBody
    Object queryHCMerchant(@RequestParam(value = "province", required = false, defaultValue = "1") String province,
                           @RequestParam(value = "city", required = false) String city,
                           @RequestParam(value = "county", required = false) String county) {
        LOG.info("[联动小额还款]开始进行商户查询=======================");
        Map<String, Object> map = new HashMap<>();
        List<LDXArea> list = ldxAreaBusiness.queryAll();
        if (list == null || list.size() == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "发送未知错误，请稍后尝试");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put("province", list);
        return map;
    }

    /**
     * 查询商户池
     * @param bankcard
     * @param provinceCode
     * @param cityCode
     * @param size
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/qryCompanyInfo")
    public @ResponseBody
    Object qryCompanyInfo(@RequestParam(value = "bankcard") String bankcard,
                          @RequestParam(value = "provinceCode") String provinceCode,
                            @RequestParam(value = "cityCode") String cityCode,
                          @RequestParam(value = "size",defaultValue = "20",required = false) String size)
            throws Exception {
        LOG.info("[联动小额商户池查询]-============================银行卡号========{}",bankcard);
        Map<String, Object> map = new HashMap<>();
        if(bankcard == null || "".equals(bankcard) || bankcard.trim().length() == 0){
            return ResultWrap.err(LOG,CommonConstants.FALIED,"卡号不能为空",null);
        }
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bankcard);
        LOG.info("[联动小额用户绑卡信息]-===================================={}",ldxBindCard);
        if(ldxBindCard == null){
            return ResultWrap.err(LOG,CommonConstants.FALIED,"该卡通道未绑卡",null);
        }
        String merchantNo = ldxBindCard.getMerchantNo();
        String address = "/get/qryCompanyInfo";

        Map<String, String> signmap = new HashMap<>();
        signmap.put("mechanism_id", MERCHANT_NO);//机构编号
        signmap.put("merchantno",merchantNo);//入网成功后返回的merchantno
        signmap.put("channelNo", "LDX");//
        signmap.put("provinceCode", provinceCode);// 省份简码
        signmap.put("cityCode", cityCode);// 地区码
        signmap.put("size", size);// 条数

        String sign = SignUtil.genSign(signmap, key);
        signmap.put("sign", sign);
        String json = JSON.toJSONString(signmap);
        LOG.info("[联动小额还款]请求参数==" + json);
        LOG.info("[联动小额还款]请求地址==" + URL + address);
        String result = null;
        result = HttpsClientUtil.sendRequest(URL + address, json);
        JSONObject jsonObj = com.alibaba.fastjson.JSON.parseObject(result);
        String returnCode = jsonObj.getString("returnCode");
        String msg = null;
        if (!"0".equals(returnCode)) {
            LOG.info("[联动小额还款]获取商户请求失败");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "获取商户请求失败"); // 描述
            return map;
        }
        String resultCode = jsonObj.getString("resultCode");
        if ("0".equals(resultCode)) {
            LOG.info("[联动小额还款]获取商户成功========");
            String dataString = jsonObj.getString("data");
            JSONObject jsonObject = JSONObject.parseObject(dataString);
            String companyTermString = jsonObject.getString("companyTerm");
            JSONArray jsonArray = JSONArray.parseArray(companyTermString);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "获取商户成功");
            map.put(CommonConstants.RESULT, jsonArray);
            return map;
        } else {
            LOG.info("[联动小额还款]获取商户失败");
            msg = jsonObj.getString("errCodeDes");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, msg); // 描述
            return map;
        }
    }

    /**
     * 商户查询
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/repayment/ldx/queryHCAll")
    public @ResponseBody
    Object queryHCMerchantAll(@RequestParam(value = "province", required = false) String province) {
        LOG.info("[联动小额还款]开始进行商户查询=======================");
        Map<String, Object> map = new HashMap<>();
        if (province == null) {
            List<LDXArea> list = ldxAreaBusiness.queryAll();
            if (list == null || list.size() == 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "系统繁忙，请稍后再试");
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "查询成功");
                map.put(CommonConstants.RESULT, list);
                return map;
            }
        } else {
            List<LDXArea> list = ldxAreaBusiness.getAllByProvince(province);
            if (list == null || list.size() == 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "系统繁忙，请稍后再试");
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "查询成功");
                map.put(CommonConstants.RESULT, list);
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

    @RequestMapping(value = "/v1.0/paymentgateway/repayment/ldx/getidcard",method = RequestMethod.POST)
    @ResponseBody
    public String getIdCard(@RequestParam(value = "bandCard") String bandCard){
        LDXBindCard ldxBindCard = ldxBindCardBusiness.queryByBankCard(bandCard);
        String idCard = ldxBindCard.getIdCard();
        return idCard;
    }
}
