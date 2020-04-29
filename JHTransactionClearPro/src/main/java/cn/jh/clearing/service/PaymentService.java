package cn.jh.clearing.service;


import cn.jh.clearing.async.AsyncMethod;
import cn.jh.clearing.business.*;
import cn.jh.clearing.equalGradeRebate.EqualGradeRebateConfig;
import cn.jh.clearing.equalGradeRebate.EqualGradeRebateConfigBusiness;
import cn.jh.clearing.pojo.*;
import cn.jh.clearing.util.Digest;
import cn.jh.clearing.util.DownloadExcelUtil;
import cn.jh.clearing.util.RestTemplateUtil;
import cn.jh.clearing.util.Util;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.*;
import net.sf.json.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.jh.clearing.business.*;
import cn.jh.clearing.pojo.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import cn.jh.clearing.async.AsyncMethod;
import cn.jh.clearing.equalGradeRebate.EqualGradeRebateConfig;
import cn.jh.clearing.equalGradeRebate.EqualGradeRebateConfigBusiness;
import cn.jh.clearing.util.DataEncrypt;
import cn.jh.clearing.util.Digest;
import cn.jh.clearing.util.DownloadExcelUtil;
import cn.jh.clearing.util.RestTemplateUtil;
import cn.jh.clearing.util.Util;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.Md5Util;
import cn.jh.common.utils.RandomUtils;
import cn.jh.common.utils.TokenUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sun.misc.Request;

import static java.math.BigDecimal.ROUND_DOWN;


@Controller
@EnableAutoConfiguration
public class PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentService.class);

    //来源编号

    private String msgSrcId = "4627";

    @Autowired
    private PaymentOrderBusiness paymentOrderBusiness;

    @Autowired
    private ProfitRecordBusiness profitRecordBusiness;

    @Autowired
    private DistributionBusiness distributionBusiness;

    @Autowired
    private CoinRecordBusiness coinRecordBusiness;

    @Autowired
    private ChannelCostRateBusiness channelCostRateBusiness;

    @Autowired
    private ChannelTagBusiness channelTagBusiness;

    @Autowired
    private AbroadRatioBusiness abroadRatioBusiness;

    @Autowired
    private NotifyOrderBusiness notifyOrderBusiness;

    @Autowired
    private fixedProfitBusiness fixedProfitBusiness;

    @Autowired
    private ProfitOnoffBusiness profitOnoffBusiness;

    @Autowired
    Util util;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AsyncMethod asyncMethod;

    @Value("${schedule-task.on-off}")
    private String scheduleTaskOnOff;

    /**
     * 获取用户的交易明喜
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/{token}")
    public @ResponseBody
    Object pagePaymentQueryByUserId(HttpServletRequest request,
                                    @PathVariable("token") String token,
                                    // 开始时间
                                    @RequestParam(value = "start_time", required = false) String startTime,
                                    // 订单号
                                    @RequestParam(value = "order_code", required = false) String orderCode,
                                    // 状态
                                    @RequestParam(value = "status", defaultValue = "-1", required = false) String status,
                                    // 外放订单号
                                    @RequestParam(value = "out_order_code", required = false) String outorderCode,
                                    // 结束时间
                                    @RequestParam(value = "end_time", required = false) String endTime,
                                    // **0 充值 1支付 2提现 3退款 */
                                    @RequestParam(value = "type", defaultValue = "0,1,2,3,10,11", required = false) String type,
                                    @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                    @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                    @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                    @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        if (orderCode != null && !orderCode.equals("")) {
            PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, paymentOrder);
        }
        if (outorderCode != null && !outorderCode.equals("")) {
            PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOderByOutOrdercode(orderCode);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, paymentOrder);
        }
        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        Date StartTimeDate = null;
        if (startTime != null && !startTime.equalsIgnoreCase("")) {
            StartTimeDate = DateUtil.getDateFromStr(startTime);
        }
        Date endTimeDate = null;

        if (endTime != null && !endTime.equalsIgnoreCase("")) {
            endTimeDate = DateUtil.getDateFromStr(endTime);
        }
        String[] str1 = status.split(",");
        if (status == null || status.equals("") || str1.length == 0) {
            str1 = null;
        }
        Page<PaymentOrder> paymentOrders = null;
        if (status.equals("-1")) {
            paymentOrders = paymentOrderBusiness.queryPaymentOrderByUserid(userId, type, StartTimeDate, endTimeDate, pageable);
            List<PaymentOrder> content = paymentOrders.getContent();
            for (PaymentOrder paymentOrder : content) {
                if (CommonConstants.ORDER_TYPE_CONSUME.equals(paymentOrder.getType())) {
                    paymentOrder.setExtraFee(BigDecimal.ZERO);
                    paymentOrder.setCostfee(BigDecimal.ZERO);
                }
            }
            map.put(CommonConstants.RESULT, paymentOrders);
        } else {
            paymentOrders = paymentOrderBusiness.queryAllPaymentOrder(userId + "", type, str1, StartTimeDate, endTimeDate, pageable);
            List<PaymentOrder> content = paymentOrders.getContent();
            for (PaymentOrder paymentOrder : content) {
                if (CommonConstants.ORDER_TYPE_CONSUME.equals(paymentOrder.getType())) {
                    paymentOrder.setExtraFee(BigDecimal.ZERO);
                    paymentOrder.setCostfee(BigDecimal.ZERO);
                }
            }
            map.put(CommonConstants.RESULT, paymentOrders);
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;
    }

    /**
     * 更新第三方的订单号
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/update/thirdordercode")
    public @ResponseBody
    Object updatePaymentThirdOrdercde(HttpServletRequest request,
                                      @RequestParam(value = "order_code") String orderCode,
                                      @RequestParam(value = "third_code") String thirdcode) {
        Map map = new HashMap();
        paymentOrderBusiness.updateThirdcodeByOrdercode(thirdcode, orderCode);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /**
     * 更新第三方的订单号
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/update/autoclearing")
    public @ResponseBody
    Object updateAutoClearing(HttpServletRequest request,
                              @RequestParam(value = "order_code") String orderCode,
                              @RequestParam(value = "auto_clearing", required = false) String autoclearing) {
        Map<String, Object> map = new HashMap<>();
        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
        if (autoclearing != null && !"".equals(autoclearing)) {
            paymentOrder.setAutoClearing(autoclearing);
            paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        }
        map.put(CommonConstants.RESULT, paymentOrder);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /** 根据订单号获取订单对象 */
    /**
     * 获取用户的交易明喜
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/ordercode")
    public @ResponseBody
    Object queryPaymentOrder(HttpServletRequest request,
                             @RequestParam(value = "order_code") String orderCode) {
        Map<String, Object> map = new HashMap<>();
        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
        if (paymentOrder == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAYMENT_NOT_EXIST);
            map.put(CommonConstants.RESP_MESSAGE, "订单不存在");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, paymentOrder);
            return map;
        }
    }

    /** 根据订单号获取订单对象 */
    /**
     * 获取用户的交易明喜
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/thirdcode")
    public @ResponseBody
    Object queryPaymentOrderByThirdcode(HttpServletRequest request,
                                        @RequestParam(value = "third_code") String thirdcode) {
        Map map = new HashMap();

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderByThirdcode(thirdcode);
        if (paymentOrder == null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAYMENT_NOT_EXIST);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, paymentOrder);
            return map;
        }
    }

    /**
     * 根据外放的订单号， 获取订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/outordercode")
    public @ResponseBody
    Object queryPaymentOrderByOutOrdercode(HttpServletRequest request,
                                           @RequestParam(value = "order_code") String orderCode) {
        Map map = new HashMap();

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOderByOutOrdercode(orderCode);
        if (paymentOrder == null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAYMENT_NOT_EXIST);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            map.put(CommonConstants.RESULT, paymentOrder);
            return map;
        }
    }

    // 根据多个条件查询订单信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/all")
    public @ResponseBody
    Object pageAllPaymentQuery(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(value = "phone", defaultValue = "", required = false) String phone,
                               // 订单号
                               @RequestParam(value = "order_code", required = false) String order_code,
                               // 外放订单号
                               @RequestParam(value = "out_order_code", required = false) String outorderCode,
                               // 用户编号
                               @RequestParam(value = "userid", defaultValue = "0", required = false) long userid,
                               // 贴牌id
                               @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brand_id,

                               // 等级
                               @RequestParam(value = "grade", required = false) String grade,

                               // 身份证号
                               @RequestParam(value = "idcard", required = false) String idcard,

                               // 银行卡号
                               @RequestParam(value = "cardNo", required = false) String cardNo,

                               /// **0 充值 1支付 2提现 3退款 */
                               @RequestParam(value = "type", required = false) String order_type,

                               /** 0 待完成 1已成功已结算 2已取消 3待处理 4已成功待结算 */
                               @RequestParam(value = "status", defaultValue = "", required = false) String order_status,
                               @RequestParam(value = "isDownload", defaultValue = "0", required = false) String isDownload,

                               @RequestParam(value = "start_time", required = false) String startTime,
                               @RequestParam(value = "end_time", required = false) String endTime,
                               @RequestParam(value = "userName", required = false) String userName,
                               @RequestParam(value = "bankName", required = false) String bankName,
                               @RequestParam(value = "debitBankName", required = false) String debitBankName,
                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                               @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                               @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Date StartTimeDate = null;

        //2019.5.24更改为不限制数据条数  ruanjiajun
		/*if (size > 1000) {
			size = 1000;
		} else if ("1".equals(isDownload)) {
			size = 2000;
		}*/
        try {
            if (startTime != null && !startTime.trim().equalsIgnoreCase("")) {
                StartTimeDate = DateUtil.getDateFromStr(startTime);
            }
        } catch (Exception e1) {
            LOG.error("startTime转换异常===========================");
            startTime = null;
        }
        Date endTimeDate = null;

        try {
            if (endTime != null && !endTime.trim().equalsIgnoreCase("")) {
                endTimeDate = DateUtil.getDateFromStr(endTime);
            }
        } catch (Exception e) {
            LOG.error("endTime转换异常============================");
            endTime = null;
        }

        List<PaymentOrder> PaymentOrders = new ArrayList<PaymentOrder>();

        Map<String, Object> map = new HashMap<String, Object>();

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        // 根据多个条件查询订单信息
        Map queryPaymentOrderAll = paymentOrderBusiness.queryPaymentOrderAll(startTime, endTime, phone, order_code, order_type, order_status, brand_id, cardNo, userName, bankName, debitBankName, pageable);
        if (queryPaymentOrderAll.containsKey("content")) {
            List<PaymentOrder> list = (List<PaymentOrder>) queryPaymentOrderAll.get("content");
            if (list == null || list.size() == 0 && order_code != null && !"".equals(order_code.trim())) {
                PaymentOrder paymentOrder = paymentOrderBusiness.findByThirdOrdercode(order_code);
                if (paymentOrder != null) {
                    list = new ArrayList<>();
                    list.add(paymentOrder);
                    queryPaymentOrderAll.put("content", list);
                }
            }
        }

        if ("1".equals(isDownload)) {
            if (queryPaymentOrderAll.containsKey("content")) {
                List<PaymentOrder> list = (List<PaymentOrder>) queryPaymentOrderAll.get("content");
                if (list != null && list.size() > 0) {
                    String downloadFile;
                    try {
                        downloadFile = DownloadExcelUtil.downloadFile(request, response, list, new PaymentOrderExcel());
                        LOG.info("downloadFile======" + downloadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
                        return map;
                    }
                    if (downloadFile == null) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
                        return map;
                    } else {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
                        map.put(CommonConstants.RESULT, downloadFile);
                        return map;
                    }
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
                return map;
            }
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, queryPaymentOrderAll);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /** 创建或更新支付/充值/订单 */
    /**
     * 如果选择三级分销产品升级， 那么product_id需要填入三级分销的产品 id realamount手机充值用
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/add")
    public @ResponseBody
    Object addPaymentOrder(HttpServletRequest request,
                           @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                           @RequestParam(value = "desc", defaultValue = "", required = false) String desc,
                           @RequestParam(value = "desc_code", defaultValue = "", required = false) String descCode,
                           @RequestParam(value = "notify_url", defaultValue = "", required = false) String notifyUrl,
                           @RequestParam(value = "return_url", defaultValue = "", required = false) String returnUrl,
                           @RequestParam(value = "out_order_code", defaultValue = "", required = false) String outOrderCode,
                           @RequestParam(value = "phone") String phone,
                           @RequestParam(value = "openid", required = false) String openid,
                           @RequestParam(value = "orderCode", required = false) String orderCode,
                           @RequestParam(value = "brand_id", required = false, defaultValue = "-1") String brand_id,
                           @RequestParam(value = "amount") String amount,
                           // 车牌号
                           @RequestParam(value = "carNo", required = false) String carNo,
                           // 手机充值用的真实
                           @RequestParam(value = "realamount", required = false) String realamount,
                           // 充值手机号
                           @RequestParam(value = "phonebill", required = false) String phonebill,
                           @RequestParam(value = "remark", defaultValue = "", required = false) String remark,
                           @RequestParam(value = "channel_tag") String channeltag,
                           @RequestParam(value = "bank_card", required = false) String bankcard,
                           @RequestParam(value = "product_id", required = false) String prodid) {
        Map<String, Object> map = new HashMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();

        long brandId = -1;
        try {
            brandId = Long.valueOf(brand_id);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/phone";

        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject;
        JSONObject resultObj;
        long userId;
        long brandid;
        String brandname;
        String fullname;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            userId = resultObj.getLong("id");
            brandid = resultObj.getLong("brandId");
            brandname = resultObj.getString("brandname");
            fullname = resultObj.getString("fullname");
        } catch (Exception e) {
            LOG.error("根据手机号查询用户信息失败=============================");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "根据手机号查询用户信息失败,请确认手机号是否正确!");
            map.put(CommonConstants.RESULT, "根据手机号查询用户信息失败,请确认手机号是否正确!");
            return map;
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/channel/query";

        PaymentOrder paymentOrder = new PaymentOrder();

        /** 根据的渠道标识或去渠道的相关信息 */
        if (!channeltag.equalsIgnoreCase("JIEFUBAO")) {

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_tag", channeltag);

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            long channelId = resultObj.getLong("id");
            String channelName = resultObj.getString("name");
            String channelTag = resultObj.getString("channelTag");
            String channelType = resultObj.getString("channelType");
            String autoclearing = resultObj.getString("autoclearing");
            // 成本费率
            String costRateStr = resultObj.getString("costRate");
            double costRate = Double.valueOf(costRateStr);

            /** 根据用户的信息获取用户的渠道费率 */
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/channel/rate/query/userid";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_id", channelId + "");
            requestEntity.add("user_id", userId + "");
            LOG.info("参数================" + requestEntity);

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/channel/rate/query/userid:" + result);
            jsonObject = JSONObject.fromObject(result);

            if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "查询用户费率有误,请稍后重试!");
                return map;
            }

            resultObj = jsonObject.getJSONObject("result");
            String rate = resultObj.getString("rate");
            double userRate = Double.valueOf(rate);
            // 当用户费率小于成本费率时不生成订单!
            System.out.println("type=====" + type);
            if (!CommonConstants.ORDER_TYPE_WITHDRAW.equalsIgnoreCase(type) && userRate < costRate) {
                LOG.error("用户费率低于成本费率了!!!生成订单失败=============================用户手机号:" + phone);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.您的费率有异常!请联系您的管理员!!!");
                return map;
            }

            String extraFee = resultObj.getString("extraFee");
            String withdrawFee = resultObj.getString("withdrawFee");
            paymentOrder.setChannelid(channelId);
            paymentOrder.setChannelname(channelName);
            paymentOrder.setChannelTag(channelTag);
            paymentOrder.setOpenid(openid);

            String creditBankName = null;
            //if(!"2".equals(type) && !"SPALI_PAY".equalsIgnoreCase(channelTag) && !"CARD_EVA".equalsIgnoreCase(channelTag)) {
            if (bankcard != null && !"".equals(bankcard)) {
                LOG.info("判断为不需要充值卡的订单======");
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/bank/default/cardnoand/type";
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
                multiValueMap.add("cardno", bankcard);
                multiValueMap.add("type", "0");
                multiValueMap.add("userId", userId + "");
                String result1 = restTemplate.postForObject(url, multiValueMap, String.class);
                LOG.info("接口/v1.0/user/bank/default/cardnoand/type====RESULT=========" + result1);

                jsonObject = JSONObject.fromObject(result1);
                resultObj = jsonObject.getJSONObject("result");

                creditBankName = resultObj.getString("bankName");

            }

            paymentOrder.setBankName(creditBankName);
            paymentOrder.setBankcard(bankcard);
            /** 默认是不自动清算 */
            paymentOrder.setAutoClearing(autoclearing);
            paymentOrder.setRate(new BigDecimal(rate));
            BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("") || extraFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(extraFee);
            BigDecimal newwithdrawFee = withdrawFee == null || withdrawFee.equalsIgnoreCase("") || withdrawFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(withdrawFee);

            paymentOrder.setExtraFee(newextraFee.add(newwithdrawFee));
            paymentOrder.setCostfee(newwithdrawFee);
            paymentOrder.setChannelType(channelType);
            /** 充值到账要扣除手续费的 */
            if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP)) {
                BigDecimal fee = new BigDecimal("0.00");
                if ("0".equals(autoclearing.trim())) {
                    fee = new BigDecimal(amount).multiply(new BigDecimal(rate));
                } else {
                    fee = new BigDecimal(amount).multiply(new BigDecimal(rate)).add(paymentOrder.getExtraFee());
                }

                if ("JF_QUICK".equalsIgnoreCase(channeltag) || "JFX_QUICK1".equalsIgnoreCase(channeltag) || "JFT_QUICK2".equalsIgnoreCase(channeltag)
                        || "TYT_QUICK1".equalsIgnoreCase(channeltag) || "KB_QUICK1".equalsIgnoreCase(channeltag)
                        || "JFS_QUICK".equalsIgnoreCase(channeltag)) {
                    paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, ROUND_DOWN));
                } else {
                    paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));
                }

            } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) { // 单笔的提现费用

                paymentOrder.setRealAmount(new BigDecimal(amount).subtract(paymentOrder.getExtraFee()).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {

                /** 扣除通道的手续费.add(paymentOrder.getExtraFee()) */
                BigDecimal fee = new BigDecimal(amount).multiply(new BigDecimal(rate));

                paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_REFUND)) {

            } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAYMENT)) {
                BigDecimal fee = paymentOrder.getExtraFee();

                paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));
            }

        } else {
            paymentOrder.setChannelname("JIEFUBAO");
            paymentOrder.setChannelTag("JIEFUBAO");
            paymentOrder.setChannelType("2");
            paymentOrder.setRate(BigDecimal.ZERO);
            paymentOrder.setExtraFee(BigDecimal.ZERO);
            paymentOrder.setRealAmount(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP));

        }

        // String withdrawFee = resultObj.getString("withdrawFee");

        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setUserid(userId);
        paymentOrder.setBrandid(brandid);
        paymentOrder.setBrandname(brandname);
        if (remark == null) {
            paymentOrder.setRemark("");
        } else {
            paymentOrder.setRemark(remark);
        }
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(desc);
        paymentOrder.setDescCode(descCode);
        if (orderCode == null || "".equals(orderCode.trim()) || "null".equalsIgnoreCase(orderCode)) {
            if ("YB_PAY".equalsIgnoreCase(channeltag) || "YB_PAY2".equalsIgnoreCase(channeltag)
                    || "YB_QUICK".equals(channeltag) || "BQ_QUICK".equals(channeltag) || "CJ_QUICK_Q".equals(channeltag)
                    || "YB_QUICKN".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(UUIDGenerator.getUUID().substring(0, 20));
            } else if ("CREDICTPAY".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(orderCode);
            } else if ("YH_QUICK".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(getNumberOrderCode());
            } else if ("LD_QUICK1".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(getLD1NumberOrderCode());
            } else if ("WF_QUICK".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(getWFNumberOrderCode());
            } else if ("BQX_QUICK".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(getOrderId());
            } else if ("YS_QRCODE".equalsIgnoreCase(channeltag)) {
                paymentOrder.setOrdercode(genMerOrderId(msgSrcId));
            } else {
                paymentOrder.setOrdercode(UUIDGenerator.getUUID());
            }
        } else {
            paymentOrder.setOrdercode(orderCode);
        }

        paymentOrder.setUserName(fullname);
        paymentOrder.setPhone(phone);
        paymentOrder.setOutMerOrdercode(outOrderCode);
        paymentOrder.setType(type);

        if (("0".equals(type) && !"ABROAD_CONSUME".equals(channeltag) && !"SPALI_PAY".equals(channeltag) && !"CARD_EVA".equals(channeltag)) || ("2".equals(type) && !"JIEFUBAO".equals(channeltag))) {

            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/bank/default/userid";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
            multiValueMap.add("user_id", userId + "");
            result = restTemplate.postForObject(url, multiValueMap, String.class);
            LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);

            jsonObject = JSONObject.fromObject(result);
            String respCode = jsonObject.getString("resp_code");
            if ("000000".equals(respCode)) {
                resultObj = jsonObject.getJSONObject("result");
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "需绑定默认提现借记卡!");
                return map;
            }

            String cardNo = resultObj.getString("cardNo");
            String debitBankName = resultObj.getString("bankName");

            paymentOrder.setDebitBankCard(cardNo);
            paymentOrder.setDebitBankName(debitBankName);
        }

        paymentOrder.setOutNotifyUrl(notifyUrl);
        paymentOrder.setOutReturnUrl(returnUrl);
        paymentOrder.setStatus(CommonConstants.ORDER_READY);
        // BigDecimal newWithdrawFee = withdrawFee == null ||
        // withdrawFee.equalsIgnoreCase("") ||
        // withdrawFee.equalsIgnoreCase("null")? BigDecimal.ZERO : new
        // BigDecimal(withdrawFee);

        if (prodid != null && !prodid.equalsIgnoreCase("")) {
            paymentOrder.setThirdlevelid(prodid);
        }
        LOG.info("RESULT================" + paymentOrder.getOrdercode() + "************************************");
        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, paymentOrder);
        return map;

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/addnew")
    public @ResponseBody
    Object addPaymentOrderNew(HttpServletRequest request,
                              @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                              @RequestParam(value = "desc", defaultValue = "", required = false) String desc,
                              @RequestParam(value = "desc_code", defaultValue = "", required = false) String descCode,
                              @RequestParam(value = "phone") String phone,
                              @RequestParam(value = "brandId", required = false, defaultValue = "-1") long brandId,
                              @RequestParam(value = "amount") String amount,
                              @RequestParam(value = "channelTag") String channelTag,
                              @RequestParam(value = "bankCard", required = false) String bankCard,
                              @RequestParam(value = "creditBankName", required = false) String creditBankName,
                              @RequestParam(value = "debitBankCard", required = false) String debitBankCard,
                              @RequestParam(value = "debitBankName", required = false) String debitBankName,
                              @RequestParam(value = "remark", defaultValue = "", required = false) String remark,
                              @RequestParam(value = "outOrderCode", defaultValue = "", required = false) String outOrderCode,
                              @RequestParam(value = "productId", required = false) String productId) {
        Map<String, Object> map = new HashMap<String, Object>();

        String url = "http://user/v1.0/user/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("/v1.0/user/query/phone  RESULT================" + result);
        JSONObject jsonObject;
        JSONObject resultObj;
        long userId;
        String brandName;
        String fullName;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            userId = resultObj.getLong("id");
            brandName = resultObj.getString("brandname");
            fullName = resultObj.getString("fullname");
        } catch (Exception e) {
            LOG.error("根据手机号查询用户信息失败=============================");
            LOG.error("", e);

            return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
        }

        PaymentOrder paymentOrder = new PaymentOrder();

        /** 根据的渠道标识或去渠道的相关信息 */
        if (!"JIEFUBAO".equalsIgnoreCase(channelTag)) {

            url = "http://user/v1.0/user/channel/query";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_tag", channelTag);

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("/v1.0/user/channel/query  RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            long channelId = resultObj.getLong("id");
            String channelName = resultObj.getString("name");
            String channelType = resultObj.getString("channelType");
            String autoclearing = resultObj.getString("autoclearing");
            String costRateStr = resultObj.getString("costRate");
            double costRate = Double.valueOf(costRateStr);

            /** 根据用户的信息获取用户的渠道费率 */
            url = "http://user/v1.0/user/channel/rate/query/userid";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_id", channelId + "");
            requestEntity.add("user_id", userId + "");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("/v1.0/user/channel/rate/query/userid  RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);

            if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {

                return ResultWrap.init(CommonConstants.FALIED, "查询用户费率出现异常,请稍后重试!");
            }

            resultObj = jsonObject.getJSONObject("result");
            String rate = resultObj.getString("rate");
            double userRate = Double.valueOf(rate);
            // 当用户费率小于成本费率时不生成订单!
            if (!CommonConstants.ORDER_TYPE_WITHDRAW.equalsIgnoreCase(type) && userRate < costRate) {
                LOG.error("用户费率低于成本费率了!!!生成订单失败=============================用户手机号:" + phone);

                return ResultWrap.init(CommonConstants.FALIED, "系统检测您的费率异常,请及时联系您的管理员处理相关问题,谢谢!");
            }

            String extraFee = resultObj.getString("extraFee");
            String withdrawFee = resultObj.getString("withdrawFee");
            paymentOrder.setChannelid(channelId);
            paymentOrder.setChannelname(channelName);
            paymentOrder.setChannelTag(channelTag);
            paymentOrder.setOpenid("");

			/*String creditBankName = null;
			if(bankCard != null && !"".equals(bankCard)) {
				LOG.info("判断为不需要充值卡的订单======");
				url = "http://user/v1.0/user/bank/default/cardnoand/type";
				MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
				multiValueMap.add("cardno", bankCard);
				multiValueMap.add("type", "0");
				multiValueMap.add("userId", userId + "");
				String result1 = restTemplate.postForObject(url, multiValueMap, String.class);
				LOG.info("接口/v1.0/user/bank/default/cardnoand/type====RESULT=========" + result1);

				jsonObject = JSONObject.fromObject(result1);
				resultObj = jsonObject.getJSONObject("result");

				creditBankName = resultObj.getString("bankName");

			}*/

            paymentOrder.setBankName(creditBankName);
            paymentOrder.setBankcard(bankCard);
            /** 默认是不自动清算 */
            paymentOrder.setAutoClearing("0");
            paymentOrder.setRate(new BigDecimal(rate));
            BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("") || extraFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(extraFee);
            BigDecimal newwithdrawFee = withdrawFee == null || withdrawFee.equalsIgnoreCase("") || withdrawFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(withdrawFee);

            paymentOrder.setExtraFee(newextraFee.add(newwithdrawFee));
            paymentOrder.setCostfee(newwithdrawFee);
            paymentOrder.setChannelType(channelType);
            /** 充值到账要扣除手续费的 */
            if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP)) {
                BigDecimal fee = new BigDecimal("0.00");
                if ("0".equals(autoclearing.trim())) {
                    fee = new BigDecimal(amount).multiply(new BigDecimal(rate));
                } else {
                    fee = new BigDecimal(amount).multiply(new BigDecimal(rate)).add(paymentOrder.getExtraFee());
                }

                if ("JFX_QUICK1".equalsIgnoreCase(channelTag) || "JFT_QUICK2".equalsIgnoreCase(channelTag) || "KB_QUICK1".equalsIgnoreCase(channelTag)) {
                    paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, ROUND_DOWN));
                } else {
                    paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));
                }

            }

        } else {
            paymentOrder.setChannelname("JIEFUBAO");
            paymentOrder.setChannelTag("JIEFUBAO");
            paymentOrder.setChannelType("2");
            paymentOrder.setRate(BigDecimal.ZERO);
            paymentOrder.setExtraFee(BigDecimal.ZERO);
            paymentOrder.setRealAmount(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP));

        }

        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setUserid(userId);
        paymentOrder.setBrandid(brandId);
        paymentOrder.setBrandname(brandName);
        if (remark == null) {
            paymentOrder.setRemark("");
        } else {
            paymentOrder.setRemark(remark);
        }
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(desc);
        paymentOrder.setDescCode(descCode);
        if ("YB_PAY".equalsIgnoreCase(channelTag) || "YB_QUICK".equals(channelTag)) {
            paymentOrder.setOrdercode(UUIDGenerator.getUUID().substring(0, 20));
        } else {
            paymentOrder.setOrdercode(UUIDGenerator.getUUID());
        }

        paymentOrder.setUserName(fullName);
        paymentOrder.setPhone(phone);
        paymentOrder.setOutMerOrdercode(outOrderCode);
        paymentOrder.setType(type);
        paymentOrder.setDebitBankCard(debitBankCard);
        paymentOrder.setDebitBankName(debitBankName);
        paymentOrder.setOutNotifyUrl("");
        paymentOrder.setOutReturnUrl("");
        paymentOrder.setStatus(CommonConstants.ORDER_READY);

        if (productId != null && !productId.equalsIgnoreCase("")) {
            paymentOrder.setThirdlevelid(productId);
        }
        LOG.info("RESULT================" + paymentOrder.getOrdercode() + "************************************");
        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, paymentOrder);
        return map;

    }


    //创建境外消费订单的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/addabroadconsumption/ordercode")
    public @ResponseBody
    Object addAbroadConsumptionOrderCode(HttpServletRequest request,
                                         @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                         @RequestParam(value = "desc", defaultValue = "", required = false) String desc,
                                         @RequestParam(value = "descCode", defaultValue = "", required = false) String descCode,
                                         @RequestParam(value = "notifyUrl", defaultValue = "", required = false) String notifyUrl,
                                         @RequestParam(value = "returnUrl", defaultValue = "", required = false) String returnUrl,
                                         @RequestParam(value = "outOrderCode", defaultValue = "", required = false) String outOrderCode,
                                         @RequestParam(value = "phone") String phone,
                                         @RequestParam(value = "orderCode", required = false) String orderCode,
                                         @RequestParam(value = "brandId", required = false, defaultValue = "-1") int brandId,
                                         @RequestParam(value = "amount") String amount,
                                         @RequestParam(value = "realAmount", required = false) String realamount,
                                         @RequestParam(value = "remark", defaultValue = "", required = false) String remark,
                                         @RequestParam(value = "channelTag") String channeltag) {
        Map<String, Object> map = new HashMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();

        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/phone";

        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject;
        JSONObject resultObj;
        long userId;
        String brandname;
        String fullname;
        String grade;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            userId = resultObj.getLong("id");
            brandname = resultObj.getString("brandname");
            fullname = resultObj.getString("fullname");
            grade = resultObj.getString("grade");
        } catch (Exception e) {
            LOG.error("根据手机号查询用户信息失败=============================");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "根据手机号查询用户信息失败,请确认手机号是否正确!");
            map.put(CommonConstants.RESULT, "根据手机号查询用户信息失败,请确认手机号是否正确!");
            return map;
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/channel/query";

        PaymentOrder paymentOrder = new PaymentOrder();

        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("channel_tag", channeltag);

        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        long channelId = resultObj.getLong("id");
        String channelName = resultObj.getString("name");
        String channelTag = resultObj.getString("channelTag");
        String channelType = resultObj.getString("channelType");

        paymentOrder.setChannelid(channelId);
        paymentOrder.setChannelname(channelName);
        paymentOrder.setChannelTag(channelTag);

        /** 默认是不自动清算 */
        paymentOrder.setAutoClearing("0");

        AbroadRatio abroadRatioByBrandIdAndGrade = abroadRatioBusiness.getAbroadRatioByBrandIdAndGrade(brandId, Integer.parseInt(grade));

        paymentOrder.setRate(abroadRatioByBrandIdAndGrade.getRate());

        paymentOrder.setExtraFee(BigDecimal.ZERO);
        paymentOrder.setCostfee(BigDecimal.ZERO);
        paymentOrder.setChannelType(channelType);

        paymentOrder.setRealAmount(new BigDecimal(amount).multiply(abroadRatioByBrandIdAndGrade.getRate()).setScale(2, BigDecimal.ROUND_HALF_DOWN));

        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setUserid(userId);
        paymentOrder.setBrandid(brandId);
        paymentOrder.setBrandname(brandname);
        if (remark == null) {
            paymentOrder.setRemark("");
        } else {
            paymentOrder.setRemark(remark);
        }
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(desc);
        paymentOrder.setDescCode(descCode);
        if (orderCode == null || "".equals(orderCode.trim()) || "null".equalsIgnoreCase(orderCode)) {
            paymentOrder.setOrdercode(UUIDGenerator.getUUID());
        } else {
            paymentOrder.setOrdercode(orderCode);
        }

        paymentOrder.setUserName(fullname);
        paymentOrder.setPhone(phone);
        paymentOrder.setOutMerOrdercode(outOrderCode);
        paymentOrder.setType(type);

        paymentOrder.setOutNotifyUrl(notifyUrl);
        paymentOrder.setOutReturnUrl(returnUrl);
        paymentOrder.setStatus(CommonConstants.ORDER_READY);
        LOG.info("RESULT================" + paymentOrder.getOrdercode() + "************************************");
        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);

        return ResultWrap.init(CommonConstants.SUCCESS, "成功", paymentOrder);

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/add2")
    public @ResponseBody
    Object addPaymentOrderFirst(HttpServletRequest request,
                                @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                @RequestParam(value = "desc", defaultValue = "", required = false) String desc,
                                @RequestParam(value = "userId", defaultValue = "", required = false) long userId,
                                @RequestParam(value = "bankCard", defaultValue = "", required = false) String bankCard,
                                @RequestParam(value = "amount", defaultValue = "", required = false) String amount,
                                @RequestParam(value = "orderCode", defaultValue = "", required = false) String orderCode,
                                @RequestParam(value = "channel_tag", defaultValue = "", required = false) String channelTag) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setType("9");
        paymentOrder.setDesc(desc);
        paymentOrder.setOrdercode(orderCode);
        paymentOrder.setChannelTag(channelTag);
        paymentOrder.setUserid(userId);
        paymentOrder.setBankcard(bankCard);
        paymentOrder.setCreateTime(new Date());
        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
        Map map = new HashMap();
        if (null == paymentOrder || "".equals(paymentOrder)) {
            map.put("resp_message", "新增订单失败");
            return map;
        }
        map.put("result", paymentOrder.toString());
        map.put("resp_message", "新增订单成功");
        return map;
    }

    /** 创建或更新支付/充值/订单 */
    /**
     * 如果选择三级分销产品升级， 那么product_id需要填入三级分销的产品 id realamount手机充值用
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/type1/add")
    public @ResponseBody
    Object addPaymentOrderType1(HttpServletRequest request,
                                @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                @RequestParam(value = "desc", defaultValue = "", required = false) String desc,
                                @RequestParam(value = "desc_code", defaultValue = "", required = false) String descCode,
                                @RequestParam(value = "notify_url", defaultValue = "", required = false) String notifyUrl,
                                @RequestParam(value = "return_url", defaultValue = "", required = false) String returnUrl,
                                @RequestParam(value = "out_order_code", defaultValue = "", required = false) String outOrderCode,
                                @RequestParam(value = "userid") String userid,
                                @RequestParam(value = "openid", required = false) String openid,
                                @RequestParam(value = "amount") String amount,
                                // 车牌号
                                @RequestParam(value = "carNo", required = false) String carNo,
                                // 手机充值用的真实
                                @RequestParam(value = "realamount", required = false) String realamount,
                                // 充值手机号
                                @RequestParam(value = "phonebill", required = false) String phonebill,
                                @RequestParam(value = "remark", defaultValue = "", required = false) String remark,
                                @RequestParam(value = "channel_tag") String channeltag,
                                @RequestParam(value = "bank_card", required = false) String bankcard,
                                @RequestParam(value = "product_id", required = false) String prodid) {

        RestTemplate restTemplate = new RestTemplate();

        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/id";

        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", userid);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        long userId = resultObj.getLong("id");
        long brandid = resultObj.getLong("brandId");
        String brandname = resultObj.getString("brandname");
        String phone = resultObj.getString("phone");

        /*
         * uri = util.getServiceUrl("paymentchannel", "error url request!"); url =
         * uri.toString() + "/v1.0/paymentchannel/route/query";
         *//** 根据的渠道标识或去渠道的相关信息 *//*
         * requestEntity = new LinkedMultiValueMap<String, String>();
         * requestEntity.add("brandcode", brandid+""); requestEntity.add("channel_type",
         * type); requestEntity.add("channel_tag", channeltag); result =
         * restTemplate.postForObject(url, requestEntity, String.class); jsonObject =
         * JSONObject.fromObject(result); String targetChannelTag =
         * jsonObject.getString("result");
         */

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/channel/query";

        PaymentOrder paymentOrder = new PaymentOrder();

        /** 根据的渠道标识或去渠道的相关信息 */
        if (channeltag.equalsIgnoreCase("JIEFUBAO") && !descCode.equals("RedPayment")) {

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_tag", channeltag);

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            long channelId = resultObj.getLong("id");
            String channelName = resultObj.getString("name");
            String channelTag = resultObj.getString("channelTag");
            String channelType = resultObj.getString("channelType");
            /** 根据用户的信息获取用户的渠道费率 */
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/channel/rate/query/userid";

            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_id", channelId + "");
            requestEntity.add("user_id", userId + "");

            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);

            if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.下单失败,臣妾已经尽力了,请重试!");
                return map;
            }

            resultObj = jsonObject.getJSONObject("result");
            String rate = resultObj.getString("rate");
            String extraFee = resultObj.getString("extraFee");

            paymentOrder.setChannelid(channelId);
            paymentOrder.setChannelname(channelName);
            paymentOrder.setChannelTag(channelTag);
            paymentOrder.setOpenid(openid);
            paymentOrder.setBankcard(bankcard);
            /** 默认是不自动清算 */
            paymentOrder.setAutoClearing("0");
            paymentOrder.setRate(new BigDecimal(rate));
            BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("")
                    || extraFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(extraFee);
            paymentOrder.setExtraFee(newextraFee);
            paymentOrder.setChannelType(channelType);
            /** 充值到账要扣除手续费的 */
            if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
                paymentOrder.setRealAmount(realamount != null ? new BigDecimal(realamount) : new BigDecimal(amount));
                paymentOrder.setChannelname("节付宝");
                paymentOrder.setChannelTag("JIEFUBAO");
                paymentOrder.setChannelType("1");
                paymentOrder.setPhoneBill(phonebill != null ? phonebill : "");
                paymentOrder.setCarNo(carNo != null ? carNo : "");
            }
        } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY) && channeltag.equals("JIEFUBAO")) {

            paymentOrder.setChannelname("JIEFUBAO");
            paymentOrder.setChannelTag("JIEFUBAO");
            paymentOrder.setChannelType(type);
            paymentOrder.setRate(BigDecimal.ZERO);
            paymentOrder.setExtraFee(BigDecimal.ZERO);
            paymentOrder.setRealAmount(new BigDecimal(amount));
        } else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP) && channeltag.equals("JIEFUBAO")) {

            paymentOrder.setChannelname("JIEFUBAO");
            paymentOrder.setChannelTag("JIEFUBAO");
            paymentOrder.setChannelType(type);
            paymentOrder.setRate(BigDecimal.ZERO);
            paymentOrder.setExtraFee(BigDecimal.ZERO);
            paymentOrder.setRealAmount(new BigDecimal(amount));
        }/*else if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_COIN)&&channeltag.equals("JIEFUBAO") ) {

			paymentOrder.setChannelname("JIEFUBAO");
			paymentOrder.setChannelTag("JIEFUBAO");
			paymentOrder.setChannelType(type);
			paymentOrder.setRate(BigDecimal.ZERO);
			paymentOrder.setExtraFee(BigDecimal.ZERO);
			paymentOrder.setRealAmount(new BigDecimal(amount));
		}*/
        // String withdrawFee = resultObj.getString("withdrawFee");

        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setUserid(userId);
        paymentOrder.setBrandid(brandid);
        paymentOrder.setBrandname(brandname);
        if (remark == null) {
            paymentOrder.setRemark("");
        } else {
            paymentOrder.setRemark(remark);
        }
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(desc);
        paymentOrder.setDescCode(descCode);
        paymentOrder.setOrdercode(UUIDGenerator.getUUID());
        paymentOrder.setPhone(phone);
        paymentOrder.setOutMerOrdercode(outOrderCode);
        paymentOrder.setType(type);
        paymentOrder.setOutNotifyUrl(notifyUrl);
        paymentOrder.setOutReturnUrl(returnUrl);
        paymentOrder.setStatus(CommonConstants.ORDER_READY);
        // BigDecimal newWithdrawFee = withdrawFee == null ||
        // withdrawFee.equalsIgnoreCase("") ||
        // withdrawFee.equalsIgnoreCase("null")? BigDecimal.ZERO : new
        // BigDecimal(withdrawFee);

        if (prodid != null && !prodid.equalsIgnoreCase("")) {
            paymentOrder.setThirdlevelid(prodid);
        }

        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, paymentOrder);
        return map;

    }

    /**
     * 更新用户的订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/type1/update")
    public @ResponseBody
    Object updatePaymentOrderType1(HttpServletRequest request,
                                   @RequestParam(value = "order_code") String ordercode,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "third_code", required = false) String thirdcode,
                                   @RequestParam(value = "direct_clearing", required = false, defaultValue = "0") String directclearing) {

        Map<String, Object> resultmap = new HashMap<String, Object>();

        if (status != null && !status.equalsIgnoreCase("")) {

            if (paymentOrderBusiness.queryPaymentOrderBycodeAndStatus(ordercode, status) != null) {
                resultmap.put(CommonConstants.RESP_CODE, CommonConstants.ERRRO_ORDER_HAS_CHECKED);
                resultmap.put(CommonConstants.RESP_MESSAGE, "已经处理");
                return resultmap;
            }

        }

        /** 根据的用户手机号码查询用户的基本信息 */
        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

        if (thirdcode != null && !thirdcode.equalsIgnoreCase("")) {
            order.setThirdOrdercode(thirdcode);
        }
        order.setUpdateTime(new Date());

        if (status != null && !status.equalsIgnoreCase("")) {
            order.setStatus(status);
        }

        paymentOrderBusiness.mergePaymentOrder(order);

        /** 提现成功或者提现失败的情况下，都需要将金额首先解冻 */
        if (status != null && !status.equalsIgnoreCase("")
                && (status.equalsIgnoreCase("1") || status.equalsIgnoreCase("2"))) {

            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {

                if (order.getChannelTag().equalsIgnoreCase("JIEFUBAO")) {
                    RestTemplate restTemplate = new RestTemplate();
                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    URI uri = util.getServiceUrl("user", "error url request!");
                    String url = uri.toString() + "/v1.0/user/account/freeze";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("order_code", order.getOrdercode());
                    requestEntity.add("user_id", order.getUserid() + "");
                    requestEntity.add("amount", order.getRealAmount().toString());
                    requestEntity.add("add_or_sub", "1");
                    restTemplate.postForObject(url, requestEntity, String.class);

                }

            }

        }

        /** 充值的订单, 要发放分润， 发放积分 */
        if (status != null && !status.equalsIgnoreCase("") && status.equalsIgnoreCase(CommonConstants.ORDER_SUCCESS)) {
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)
                    || order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP)) {

                /** 表示是分润提现到余额 */
                if (order.getChannelTag().equalsIgnoreCase("JIEFUBAO")) {
                    if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
                        /** 首先更新用户的账户余额 **/
                        /** 如果通道自动清算的就不需要更新用户的账户了 */
                        if (order.getAutoClearing().equalsIgnoreCase("0")) {
                            RestTemplate restTemplate = new RestTemplate();
                            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                            URI uri = util.getServiceUrl("user", "error url request!");
                            String url = uri.toString() + "/v1.0/user/account/update";
                            requestEntity = new LinkedMultiValueMap<String, String>();
                            requestEntity.add("amount", order.getRealAmount().toString());
                            requestEntity.add("user_id", order.getUserid() + "");
                            requestEntity.add("order_code", order.getOrdercode());

                            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
                                /** 提现的时候减钱 */
                                requestEntity.add("addorsub", "1");

                            }
                            restTemplate.postForObject(url, requestEntity, String.class);

                            if (order.getDescCode() != null && order.getDescCode().equals("MobilePayment")) {
                                restTemplate = new RestTemplate();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + order.getBrandid();
                                String result = restTemplate.getForObject(url, String.class);
                                JSONObject jsonObject = JSONObject.fromObject(result);
                                LOG.info("RESULT================MobilePayment" + result);
                                JSONObject resultbrand = jsonObject.getJSONObject("result");
                                /******
                                 * 添加贴牌的钱
                                 *****/
                                restTemplate = new RestTemplate();
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/account/update";
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                requestEntity.add("amount", order.getRealAmount().toString());
                                requestEntity.add("user_id", resultbrand.getString("manageid"));
                                requestEntity.add("order_code", order.getOrdercode());
                                /** 话费充值的时候向贴牌 */
                                requestEntity.add("addorsub", "0");
                                restTemplate.postForObject(url, requestEntity, String.class);

                            }
                        }

                    } else if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP)) {

                        if (order.getDescCode() != null && order.getDescCode().equals("RedPayment")) {

                            /****
                             * 检查余额
                             ***/
                            RestTemplate restTemplate = new RestTemplate();
                            URI uri = util.getServiceUrl("user", "error url request!");
                            String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + order.getBrandid();
                            String result = restTemplate.getForObject(url, String.class);
                            JSONObject jsonObject = JSONObject.fromObject(result);
                            LOG.info("RESULT================purchaseShopping" + result);
                            JSONObject resultbrand = jsonObject.getJSONObject("result");

                            uri = util.getServiceUrl("user", "error url request!");
                            url = uri.toString() + "/v1.0/user/account/query/userId";
                            // **根据的用户手机号码查询用户的基本信息*/
                            LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                            requestEntity.add("user_id", resultbrand.getString("manageid"));
                            restTemplate = new RestTemplate();
                            result = restTemplate.postForObject(url, requestEntity, String.class);
                            LOG.info("RESULT================purchaseShopping" + result);
                            jsonObject = JSONObject.fromObject(result);
                            JSONObject UserAccount = jsonObject.getJSONObject("result");
                            BigDecimal balance = new BigDecimal("0");
                            if (UserAccount.containsKey("balance")) {
                                balance = new BigDecimal(UserAccount.getString("balance"));
                            }
                            if (balance.compareTo(order.getRealAmount()) == -1) {
                                resultmap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                                resultmap.put(CommonConstants.RESP_MESSAGE, "成功");
                                return resultmap;

                            }

                            /** 添加用户钱 */
                            {
                                restTemplate = new RestTemplate();
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/account/update";
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                requestEntity.add("amount", order.getRealAmount().toString());
                                requestEntity.add("user_id", order.getUserid() + "");
                                requestEntity.add("order_code", order.getOrdercode());
                                restTemplate.postForObject(url, requestEntity, String.class);
                            }

                            /******
                             * 减去贴牌的钱
                             *****/
                            {
                                restTemplate = new RestTemplate();
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/account/update";
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                requestEntity.add("amount", order.getRealAmount().toString());
                                requestEntity.add("user_id", resultbrand.getString("manageid"));
                                requestEntity.add("order_code", order.getOrdercode());
                                /** 提现的时候减钱 */
                                requestEntity.add("addorsub", "1");
                                restTemplate.postForObject(url, requestEntity, String.class);

                            }

                        }

                    }

                }

            }

            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
                /** 循环处理用户的分润 */
                rebate(ordercode, order.getUserid(), order.getChannelid(), order.getAmount(), order.getRate(),
                        order.getBrandid(), order.getType());
            }
//            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_COIN)) {
//             //** 发放积分 *//*
//				redsendCoin(ordercode, order.getType(), order.getUserid(), order.getPhone(), order.getBrandid(),order.getBrandname(), order.getAmount());
//				LOG.info("==============="+ordercode+"===============发放积分执行完毕");
//			}

        }
        resultmap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        resultmap.put(CommonConstants.RESP_MESSAGE, "成功");
        return resultmap;
    }

    /**
     * 更新用户的订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/sendrebate")
    public @ResponseBody
    Object sendRebate(HttpServletRequest request,
                      @RequestParam(value = "order_code") String ordercode) {
        Map resultmap = new HashMap();
        /** 根据的用户手机号码查询用户的基本信息 */
        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
        /** 循环处理用户的分润 */
        rebate(ordercode, order.getUserid(), order.getChannelid(), order.getAmount(), order.getRate(),
                order.getBrandid(), order.getType());
        resultmap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        resultmap.put(CommonConstants.RESP_MESSAGE, "成功");
        return resultmap;

    }
    //2019.5.7 更新接口名

    /**
     * 更新用户的订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/update")
    public @ResponseBody
    Object updatePaymentOrder(HttpServletRequest request,
                              @RequestParam(value = "order_code") String ordercode,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "third_code", required = false) String thirdcode,
                              @RequestParam(value = "direct_clearing", required = false, defaultValue = "0") String directclearing) {
        Map<String, Object> resultmap = new HashMap<String, Object>();
        LOG.info("/v1.0/transactionclear/payment/update===============ordercode:" + ordercode + "=====status:" + status);
        if (status == null || "".equals(status.trim())) {
            return ResultWrap.err(LOG, CommonConstants.FALIED, "订单状态有误!");
        }
        status = status.trim();

        if (paymentOrderBusiness.queryPaymentOrderBycodeAndStatus(ordercode, status) != null) {
            resultmap.put(CommonConstants.RESP_CODE, CommonConstants.ERRRO_ORDER_HAS_CHECKED);
            resultmap.put(CommonConstants.RESP_MESSAGE, "已经处理");
            return resultmap;
        }

        /** 根据的用户手机号码查询用户的基本信息 */
        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
        if (order == null) {
            resultmap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            resultmap.put(CommonConstants.RESP_MESSAGE, "未获取到该订单!");
            return resultmap;
        }

        if ("1".equals(status)) {
            LOG.info("添加分润信息的订单======" + ordercode);
            try {
                createProfitByOrderCode(ordercode);
            } catch (Exception e) {
                LOG.error("添加分润信息有误======");

                order.setPhoneBill("0.00");
                order.setCarNo("0.00");
            }

            if ("10".equals(order.getType()) || "11".equals(order.getType())) {
                BrandNotifyConfig brandNotifyConfigByBrandId = notifyOrderBusiness.getBrandNotifyConfigByBrandId(Integer.parseInt(order.getBrandid() + ""));
                if (brandNotifyConfigByBrandId != null) {
                    String secretKey = brandNotifyConfigByBrandId.getSecretKey();
                    String ipAddress = brandNotifyConfigByBrandId.getIpAddress();
                    String notifyUrl = brandNotifyConfigByBrandId.getNotifyUrl();

                    String content = sendOrderNotify(secretKey, ordercode, order.getAmount() + "", order.getRealAmount() + "", order.getUserid() + "", order.getRate() + "", order.getExtraFee() + "");
                    NotifyOrder notifyOrder = new NotifyOrder();
                    notifyOrder.setContent(content);
                    notifyOrder.setNotifyUrl(ipAddress + notifyUrl);
                    notifyOrder.setNotifyTime(new Date());
                    notifyOrder.setCreateTime(new Date());
                    notifyOrder = notifyOrderBusiness.save(notifyOrder);
                }
            }
        }

        if ("ABROAD_CONSUME".equalsIgnoreCase(order.getChannelTag())) {

            sendThirdDistributionByAbroadConsume(ordercode, order.getUserid(), order.getPhone(), order.getRealAmount(), Integer.parseInt(order.getBrandid() + ""));

            order.setUpdateTime(new Date());
            if (status != null && !status.equalsIgnoreCase("")) {
                order.setStatus(status);
            }

            paymentOrderBusiness.mergePaymentOrder(order);

            resultmap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            resultmap.put(CommonConstants.RESP_MESSAGE, "境外消费订单处理完成!");
            return resultmap;

        }

        if ("CARD_EVA".equalsIgnoreCase(order.getChannelTag())) {

            cardEvaRebate(ordercode, order.getUserid(), order.getChannelid(), order.getAmount(), order.getBrandid());

            order.setUpdateTime(new Date());
            if (status != null && !status.equalsIgnoreCase("")) {
                order.setStatus(status);
            }

            paymentOrderBusiness.mergePaymentOrder(order);

            resultmap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            resultmap.put(CommonConstants.RESP_MESSAGE, "卡测评订单分润处理完成!");
            return resultmap;

        }

        boolean hasKey = false;
        String key = "/v1.0/transactionclear/payment/update:ordercode=" + ordercode + ";status=" + status + ";thirdcode=" + thirdcode + ";directclearing=" + directclearing;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            return ResultWrap.init(CommonConstants.ORDER_SUCCESS, "订单处理中");
        }
        operations.set(key, key, 30, TimeUnit.SECONDS);


        LOG.info("/v1.0/transactionclear/payment/update===============" + order);
        if (thirdcode != null && !thirdcode.equalsIgnoreCase("")) {
            order.setThirdOrdercode(thirdcode);
        }

        /** 如果状态是成功, 判断交易类型 */
        if (status != null && !"".equalsIgnoreCase(status) && status.equalsIgnoreCase("1") && !order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY) && !order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
            /** 判断通道的结算类型 */
            String clearingType = order.getChannelType();
            if (clearingType.equalsIgnoreCase(CommonConstants.CLEARING_T_0)) {
                /** 判断当前是否是休息日 */
                Calendar today = Calendar.getInstance();
                if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    status = "4";
                } else {
                    status = "1";
                }
            } else if (clearingType.equalsIgnoreCase(CommonConstants.CLEARING_T_1)) {
                /** 判断当前是否是休息日 */
                Calendar today = Calendar.getInstance();
                if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    status = "4";
                } else {
                    /** 判断当前日期和订单的生成日期是否相隔24小时 */
                    int day = getIntervalDays(order.getCreateTime(), today.getTime());
                    if (day > 0) {
                        status = "1";
                    } else {
                        status = "4";
                    }
                }
            } else if (clearingType.equalsIgnoreCase(CommonConstants.CLEARING_D_0)) {
                status = "1";
            } else if (clearingType.equalsIgnoreCase(CommonConstants.CLEARING_D_1)) {
                Calendar today = Calendar.getInstance();
                /** 判断当前日期和订单的生成日期是否相隔24小时 */
                int day = getIntervalDays(order.getCreateTime(), today.getTime());
                if (day > 0) {
                    status = "1";
                } else {
                    status = "4";
                }
            } else {
                status = "1";
            }
        }

        order.setStatus(status);
        if (status.equals("4")) {
            this.sendPushMessage(order.getUserid() + "", order.getRealAmount());
        }


        LOG.info("=======判断是否是操作余额订单=======ordercode=" + ordercode + "==============status=" + status);
        if ("HQB_QUICK".equalsIgnoreCase(order.getChannelTag()) || "SS_QUICK".equalsIgnoreCase(order.getChannelTag())) {
            order.setCreateTime(new Date());
        }

        paymentOrderBusiness.mergePaymentOrder(order);
        /** 提现成功或者提现失败的情况下，都需要将金额首先解冻 */
        if (status.equalsIgnoreCase("1") || status.equalsIgnoreCase("2")) {
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
                if (!order.getChannelTag().equalsIgnoreCase("JIEFUBAO")) {
                    // RestTemplate restTemplate = new RestTemplate();
                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    // URI uri = util.getServiceUrl("user", "error url request!");
                    // String url = uri.toString() + "/v1.0/user/account/freeze";
                    String url = "http://user/v1.0/user/account/freeze";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("order_code", order.getOrdercode());
                    requestEntity.add("user_id", order.getUserid() + "");
                    requestEntity.add("amount", order.getAmount().toString());
                    requestEntity.add("add_or_sub", "1");
                    String resultString = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("===============" + ordercode + "===============余额解冻结果==========" + resultString);
                } else {
                    // RestTemplate restTemplate = new RestTemplate();
                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    // URI uri = util.getServiceUrl("user", "error url request!");
                    // String url = uri.toString() + "/v1.0/user/account/rebate/unfreeze";
                    String url = "http://user/v1.0/user/account/rebate/unfreeze";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("order_code", order.getOrdercode());
                    requestEntity.add("user_id", order.getUserid() + "");
                    requestEntity.add("amount", order.getAmount().toString());
                    requestEntity.add("add_or_sub", "1");
                    String resultString = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("===============" + ordercode + "===============分润余额解冻结果==========" + resultString);
                }
            }
        }

        LOG.info("===============" + ordercode + "===============是否变更余额判断");
        /** 充值的订单, 要发放分润， 发放积分 */
        if (status.equalsIgnoreCase(CommonConstants.ORDER_SUCCESS)) {
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP) || order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
                /** 表示是分润提现到余额 */
                if (order.getChannelTag().equalsIgnoreCase("JIEFUBAO")) {
                    BigDecimal realAmount = order.getRealAmount();

                    Map<String, Object> brandRebateRatio = brandRebateRatio(order);
                    Long preUserId = null;
                    BigDecimal setScale = BigDecimal.ZERO;
                    if (CommonConstants.SUCCESS.equals(brandRebateRatio.get(CommonConstants.RESP_CODE))) {
                        Map<String, Object> map = (Map<String, Object>) brandRebateRatio.get(CommonConstants.RESULT);
                        preUserId = (Long) map.get("preUserId");
                        setScale = (BigDecimal) map.get("setScale");
                    }

                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    String url = "http://user/v1.0/user/account/rebate/updatebyrebateamount";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("userId", order.getUserid() + "");
                    requestEntity.add("rebateAmount", realAmount + "");
                    requestEntity.add("orderCode", order.getOrdercode());
                    requestEntity.add("addorsub", "2");
                    String resultString = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("===============" + ordercode + "===============分润余额变更结果==========" + resultString);

                    if (setScale.compareTo(BigDecimal.ZERO) != 0) {

                        String uuid = UUIDGenerator.getUUID();
                        LOG.info("生成的订单号=====" + uuid);

                        addPaymentOrder(request, "2", "分润提现管理奖", null, null, null, null, order.getPhone(), null, uuid, null, setScale + "", null, null, null, "提现管理奖返给直推上级", "JIEFUBAO", null, null);

                        requestEntity = new LinkedMultiValueMap<String, String>();
                        url = "http://user/v1.0/user/account/rebate/updatebyrebateamount";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("userId", preUserId + "");
                        requestEntity.add("rebateAmount", setScale + "");
                        requestEntity.add("orderCode", uuid);
                        resultString = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("===============" + ordercode + "===============分润余额变更结果==========" + resultString);

                        realAmount = realAmount.subtract(setScale);

                        updatePaymentOrder(request, uuid, "1");

                    }

                    /** 首先更新用户的账户余额 **/
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    url = "http://user/v1.0/user/account/update";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("amount", realAmount + "");
                    requestEntity.add("user_id", order.getUserid() + "");
                    requestEntity.add("order_code", order.getOrdercode());
                    resultString = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("===============" + ordercode + "===============分润余额变更结果==========" + resultString);

                } else {
                    /** 首先更新用户的账户余额 **/
                    /** 如果通道自动清算的就不需要更新用户的账户了 */
                    if (order.getAutoClearing().equalsIgnoreCase("0")) {
                        RestTemplate restTemplate = new RestTemplate();
                        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                        URI uri = util.getServiceUrl("user", "error url request!");
                        String url = uri.toString() + "/v1.0/user/account/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("user_id", order.getUserid() + "");
                        requestEntity.add("order_code", order.getOrdercode());
                        if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
                            requestEntity.add("amount", order.getAmount().toString());
                            /** 提现的时候减钱 */
                            requestEntity.add("addorsub", "1");
                        } else {
                            requestEntity.add("amount", order.getRealAmount().toString());
                        }
                        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("===============" + ordercode + "===============余额变更结果==========" + resultString);
                    }
                }
            }

            LOG.info("===============" + ordercode + "===============是否分润判断");
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP) || (CommonConstants.ORDER_TYPE_REPAYMENT.equals(order.getType()) && !"HQB_QUICK".equalsIgnoreCase(order.getChannelTag())) || (CommonConstants.ORDER_TYPE_CONSUME.equals(order.getType()) && "HQB_QUICK".equalsIgnoreCase(order.getChannelTag()))) {
                /** 循环处理用户的分润 */
                asyncMethod.rebate(ordercode, order.getUserid(), order.getChannelid(), order.getAmount(), order.getRate(), order.getBrandid(), order.getType());
                LOG.info("===============" + ordercode + "===============分润执行完毕");
            }
            LOG.info("===============" + ordercode + "===============是否品牌返利");
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP) || order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
                /** 品牌返利 */
                if (!order.getChannelTag().equals("JIEFUBAO")) {
                    asyncMethod.brandClearing(order);
                }
                LOG.info("===============" + ordercode + "===============品牌返利执行完毕");
            }

            /** 如果是支付订单 */
            LOG.info("===============" + ordercode + "===============是否是支付订单判断");
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY) && (order.getDescCode() == null || !order.getDescCode().equals("shopping"))) {
                /** 更新用户的等级 */
                if (order.getThirdlevelid() != null && !order.getThirdlevelid().equalsIgnoreCase("")) {
                    /** 获取用户的等级信息 */
                    // RestTemplate restTemplate = new RestTemplate();
                    // URI uri = util.getServiceUrl("user", "error url request!");
                    String url = "http://user/v1.0/user/thirdlevel/prod/query/" + order.getThirdlevelid();
                    String result = restTemplate.getForObject(url, String.class);
                    JSONObject jsonObject = JSONObject.fromObject(result);
                    JSONObject objResObject = jsonObject.getJSONObject("result");
                    String grade = objResObject.getString("grade");
                    url = "http://user/v1.0/user/brand/query/id?brand_id=" + order.getBrandid();
                    result = restTemplate.getForObject(url, String.class);
                    jsonObject = JSONObject.fromObject(result);

                    jsonObject = jsonObject.getJSONObject(CommonConstants.RESULT);
                    int isNewRebate = jsonObject.getInt("isNewRebate");
                    MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();

                    if (isNewRebate == 0) {
                        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/thirdlevel/ratio/query/" + order.getBrandid();
                        result = restTemplate.getForObject(url, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            map.put(jsonArray.optJSONObject(i).optString("preLevel"), new BigDecimal(jsonArray.optJSONObject(i).optString("ratio")));
                        }
                        /** 计算用户三级返佣 */
                        /** 三级分销的金额也是扣除手续费以后的金额 */
                        sendThirdDistribution(ordercode, order.getUserid(), order.getChannelid(), order.getPhone(), order.getRealAmount(), map, order.getBrandid());
                        LOG.info("===============" + ordercode + "===============第一套返佣执行完毕");
                    } else if (isNewRebate == 1) {
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/thirdlevel/ratio/query/by/thirdlevelid";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("brandId", order.getBrandid() + "");
                        requestEntity.add("thirdLevelId", order.getThirdlevelid());
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray(CommonConstants.RESULT);
                        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            map.put(jsonArray.optJSONObject(i).optString("preLevel"), new BigDecimal(jsonArray.optJSONObject(i).optString("ratio")));
                        }
                        sendThirdDistributionNew(ordercode, order.getUserid(), order.getChannelid(), order.getPhone(), order.getRealAmount(), map, order.getBrandid(), grade);
                        LOG.info("===============" + ordercode + "===============第二套返佣执行完毕");
                    } else if (isNewRebate == 2) {
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/thirdlevel/ratio2/query/by/thirdlevelid";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("brandId", order.getBrandid() + "");
                        requestEntity.add("thirdLevelId", order.getThirdlevelid());
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray(CommonConstants.RESULT);
                        Map<String, String> map = new HashMap<String, String>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            map.put(jsonArray.optJSONObject(i).optString("preLevel"),
                                    new BigDecimal(jsonArray.optJSONObject(i).optString("constantReturn")) + "," +
                                            new BigDecimal(jsonArray.optJSONObject(i).optString("straightReturn")) + "," +
                                            new BigDecimal(jsonArray.optJSONObject(i).optString("betweenreturn")));
                        }
                        //计算返佣
                        sendThirdDistributionNew2(ordercode, order.getUserid(), order.getPhone(), order.getRealAmount(), map, order.getBrandid(), grade);
                        LOG.info("===============" + ordercode + "===============第三套返佣执行完毕");

                    } else if (isNewRebate == 3) {
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/thirdlevel/ratio2/query/by/thirdlevelid";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("brandId", order.getBrandid() + "");
                        requestEntity.add("thirdLevelId", order.getThirdlevelid());
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray(CommonConstants.RESULT);
                        Map<String, String> map = new HashMap<String, String>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            map.put(jsonArray.optJSONObject(i).optString("preLevel"),
                                    new BigDecimal(jsonArray.optJSONObject(i).optString("constantReturn")) + "," +
                                            new BigDecimal(jsonArray.optJSONObject(i).optString("straightReturn")) + "," +
                                            new BigDecimal(jsonArray.optJSONObject(i).optString("betweenreturn")));
                        }
                        sendThirdDistributionNew3(ordercode, order.getUserid(), order.getPhone(), order.getRealAmount(), map, order.getBrandid(), grade);
                        LOG.info("===============" + ordercode + "===============第四套返佣执行完毕");
                    }

                    /** 更新用户的等级 **/
                    url = "http://user/v1.0/user/grade/update";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("grade", grade);
                    requestEntity.add("id", order.getUserid() + "");
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    //信用分添加
                    url = "http://user/v1.0/user/credit/score/add";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("userid", order.getUserid()+"");
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("-------------------------信用分发放完毕");
                    String notice = "高级会员";
                    if (order.getAmount().compareTo(new BigDecimal(398)) >= 0) {
                        notice = "超级会员";
                    }
                    // 短信提醒
                    URI uri4 = util.getServiceUrl("notice", "error url request!");
                    url = uri4.toString() + "/v1.0/notice/sms/inform/sendTwo";
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("user_id", "1");
                    requestEntity.add("phone", order.getPhone());
                    requestEntity.add("tpl_id", "193581");
                    requestEntity.add("brand_id", "100195");
                    requestEntity.add("content", notice);
                    // 发送请求
                    RestTemplate rest = new RestTemplate();
                    try {
                        result = rest.postForObject(url, requestEntity, String.class);
                    } catch (Exception e) {
                        LOG.error("会员升级通知短信发送失败！------/v1.0/notice/sms/inform/sendTwo");
                    }
                }
            } else if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY) && order.getDescCode() != null && order.getDescCode().equals("shopping")) {
                /** 循环处理用户的分润 */
                rebate(ordercode, order.getUserid(), order.getChannelid(), order.getAmount(), order.getRate(), order.getBrandid(), order.getType());
                /*** 转给贴牌商 **/
                {
                    /** 通过用户ID判定是否为贴牌 */
                    // RestTemplate restTemplate = new RestTemplate();
                    // URI uri = util.getServiceUrl("user", "error url request!");
                    String url = "http://user/v1.0/user/brand/query/id?brand_id=" + order.getBrandid();
                    String result = restTemplate.getForObject(url, String.class);
                    JSONObject jsonObject = JSONObject.fromObject(result);
                    LOG.info("RESULT================purchaseShopping" + result);
                    JSONObject resultbrand = jsonObject.getJSONObject("result");

                    // uri = util.getServiceUrl("user", "error url request!");
                    url = "http://user/v1.0/user/query/id";
                    // **根据的用户手机号码查询用户的基本信息*/
                    LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("id", resultbrand.getString("manageid"));
                    // restTemplate = new RestTemplate();
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================purchaseShopping" + result);
                    jsonObject = JSONObject.fromObject(result);
                    JSONObject resultObju = jsonObject.getJSONObject("result");
                    String phone = null;
                    if (resultObju.containsKey("phone")) {
                        phone = resultObju.getString("phone");
                    }
                    /** 计算分润的钱 */
                    BigDecimal rebate = order.getRealAmount().setScale(2, BigDecimal.ROUND_HALF_UP);

                    /** 存贮分润记录明细 */
                    ProfitRecord profitRecord = new ProfitRecord();
                    profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
                    profitRecord.setBrandId(order.getBrandid() + "");
                    profitRecord.setAcqAmount(rebate);
                    profitRecord.setAcqphone(phone);
                    profitRecord.setAcqrate(new BigDecimal("0.0"));
                    profitRecord.setAcquserid(Long.parseLong(resultObju.getString("id")));
                    profitRecord.setAmount(order.getRealAmount());
                    profitRecord.setCreateTime(new Date());
                    profitRecord.setOrdercode(ordercode);
                    profitRecord.setOriphone(order.getPhone());
                    profitRecord.setOrirate(order.getRate());
                    profitRecord.setOriuserid(order.getUserid());
                    profitRecord.setRemark("产品剩余金额");
                    profitRecord.setScale(BigDecimal.ONE);
                    profitRecord.setType("3");
                    profitRecordBusiness.merge(profitRecord);

                    /** 存储 用户的分润记录 */
                    // restTemplate = new RestTemplate();
                    // uri = util.getServiceUrl("user", "error url request!");
                    url = "http://user/v1.0/user/rebate/update";
                    /** 根据的渠道标识或去渠道的相关信息 */
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("rebate_amount", rebate.toString());
                    requestEntity.add("user_id", resultObju.getString("id"));
                    requestEntity.add("order_code", ordercode);
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                }
            }
            LOG.info("===============" + ordercode + "===============是否发放积分判断");
            if (order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_TOPUP) || order.getType().equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY) || CommonConstants.ORDER_TYPE_CONSUME.equals(order.getType())) {
                /** 发放积分 */
                grantCoin(ordercode, order.getType(), order.getUserid(), order.getBrandid(), order.getAmount(), order.getRealAmount());
                LOG.info("===============" + ordercode + "===============发放积分执行完毕");
            }
        }
        //会员推会员
        if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(order.getType()) || CommonConstants.ORDER_TYPE_TOPUP.equals(order.getType())) {
            ProfitOnoff profitOnoff = profitOnoffBusiness.getProfitOnoffByBrandId("" + order.getBrandid());
            if (profitOnoff != null) {
                if ("1".equals(profitOnoff.getOnOff())) {
                    sendThirdDistributionHH(order);
                }
            }
        }

        redisTemplate.delete(key);
        resultmap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        resultmap.put(CommonConstants.RESP_MESSAGE, "成功");
        LOG.info("===============" + ordercode + "===============处理完毕");
        return resultmap;
    }

    /**
     * 会员推会员 普通会员推普通会员，下级还款上级拿万10
     *
     * @param order
     */
    private void sendThirdDistributionHH(PaymentOrder order) {
        LOG.info("会员推会员发放分润开始============order:=============" + order);
        Long brandId = order.getBrandid();
        String orderCode = order.getOrdercode();
        String phone = order.getPhone();
        BigDecimal rate = order.getRate();
        Long userId = order.getUserid();
        String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandId;
        String result = restTemplate.getForObject(url, String.class);

        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");

        String manageId = resultObj.getString("manageid");
        url = "http://user/v1.0/user/query/id";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", userId + "");
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT===========分润生产者：==========" + result);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String preUserId = resultObj.getString("preUserId");
        String preUserPhone = resultObj.getString("preUserPhone");
        int grade = resultObj.getInt("grade");
        if (grade == 0) {
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", preUserId);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT==============分润受益者：==========" + result);
            if (result != null) {
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
                int preGgrade = resultObj.getInt("grade");
                if (preGgrade == 0) {
                    BigDecimal ratio = new BigDecimal(0.0010);
                    BigDecimal amount = order.getAmount();
                    BigDecimal acqAmount = amount.multiply(ratio).setScale(2, BigDecimal.ROUND_HALF_DOWN);

                    ProfitRecord profitRecord = new ProfitRecord();
                    profitRecord.setBrandId(brandId + "");
                    profitRecord.setAcqAmount(acqAmount);
                    profitRecord.setAcqphone(preUserPhone);
                    profitRecord.setAcqrate(new BigDecimal("0.0"));
                    profitRecord.setAcquserid(Long.parseLong(preUserId));
                    profitRecord.setAmount(amount);
                    profitRecord.setCreateTime(new Date());
                    profitRecord.setOrdercode(orderCode);
                    profitRecord.setOriphone(phone);
                    profitRecord.setOrirate(rate);
                    profitRecord.setOriuserid(userId);
                    profitRecord.setOriUserName(order.getUserName());
                    profitRecord.setRemark("会员直推分润");
                    profitRecord.setScale(BigDecimal.ONE);
                    profitRecord.setType("6");
                    LOG.info("生成分润明细：=================" + profitRecord);
                    //添加分润明细
                    profitRecordBusiness.merge(profitRecord);
                    //更新分润余额
                    this.updateRebate(acqAmount + "", preUserId, orderCode, manageId);
                }
            }
        }
    }


    //发放境外消费的返佣
    private void sendThirdDistributionByAbroadConsume(String ordercode, long userid, String phone, BigDecimal amount, int brandid) {

        String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
        String result = restTemplate.getForObject(url, String.class);

        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");

        String manageId = resultObj.getString("manageid");

        url = "http://user/v1.0/user/query/id";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", userid + "");
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String preUserId = resultObj.getString("preUserId");
        String preUserPhone = resultObj.getString("preUserPhone");
        String userName = resultObj.getString("fullname");
        int grade = resultObj.getInt("grade");


        url = "http://user/v1.0/user/query/id";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", preUserId);
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        int preGgrade = resultObj.getInt("grade");
        String preUserName = resultObj.getString("fullname");

        AbroadRatio abroadRatio = abroadRatioBusiness.getAbroadRatioByBrandIdAndGrade(brandid, preGgrade);

        BigDecimal ratio = abroadRatio.getRatio();
        BigDecimal acqAmount = amount.multiply(ratio).setScale(2, BigDecimal.ROUND_HALF_DOWN);

        this.updateRebate(acqAmount + "", preUserId, ordercode, manageId);
        mergeThirdDistribution(preUserPhone, Long.parseLong(preUserId), preGgrade + "", amount, acqAmount, ratio, ordercode, phone, userid, grade + "", "境外消费返佣", 1, userName, preUserName);

    }

    /**
     * 发放用户的返佣 新逻辑2
     **/
    private void sendThirdDistributionNew2(String ordercode, long userid, String phone, BigDecimal amount,
                                           Map<String, String> map, long brandid, String grade) {
        int i = 1;
        int j = 0;
        long lowerUserId = userid;
        String lowerPhone = phone;

        BigDecimal remainingAmount = amount;
        // RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        // URI uri = util.getServiceUrl("user", "user url request!");
        String url = "http://user/v1.0/user/query/id";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity.add("id", userid + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/user/query/id:" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        long preUserId = resultObj.getLong("preUserId");
        String preUserPhone = resultObj.getString("preUserPhone");
        String oriGrade = resultObj.getString("grade");

        // restTemplate = new RestTemplate();
        // uri = util.getServiceUrl("user", "error url request!");
        url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
        result = restTemplate.getForObject(url, String.class);

        /** 根据的渠道标识或去渠道的相关信息 */
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String manageId = resultObj.getString("manageid");
//		int rebateCount = resultObj.getInt("rebateCount");
        //固定奖励金额
        BigDecimal constantReturn = null;
        //直推额外金额
        BigDecimal straightReturn = null;
        //间推额外金额
        BigDecimal betweenreturn = null;
        Map<String, String> gradeMap = new HashMap<String, String>();
        /** 根据userid 找到上级， 如果上级的等级大于等于他就发放返佣，然后继续循环方法 */
        while (true) {
            j++;
			/*if (i > rebateCount) {
				break;
			}*/
            if (preUserId != 0) {
                // restTemplate = new RestTemplate();
                // uri = util.getServiceUrl("user", "error url request!");
                url = "http://user/v1.0/user/query/id";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("id", preUserId + "");
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
                String preGrade = resultObj.getString("grade");
                preUserPhone = resultObj.getString("phone");
                long xPreUserId = resultObj.getLong("preUserId");
                /** 如果上级没有购买三级分销或者前面的三级分销等级比自己的等级低 */
//				if (grantGrade>Integer.parseInt(preGrade)) {
//					preUserId = xPreUserId;
//					continue;
//				} else
                {
                    if (preUserId == Long.valueOf(manageId).longValue()) {
                        break;
                    }
                    if (map.containsKey(preGrade)) {
                        String[] grantAmounts = map.get(preGrade).split(",");
                        //固定奖励金额
                        constantReturn = new BigDecimal(grantAmounts[0]).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //直推额外金额
                        straightReturn = new BigDecimal(grantAmounts[1]).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //间推额外金额
                        betweenreturn = new BigDecimal(grantAmounts[2]).setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else {
                        preUserId = xPreUserId;
                        continue;
                    }
					/*if(gradeMap.containsKey(preGrade)){
						//间推返佣
						if(betweenreturn!=null&&betweenreturn.compareTo(BigDecimal.ZERO)>0
								&&remainingAmount.subtract(betweenreturn).compareTo(BigDecimal.ZERO)>0){
							DistributionRecord(preUserPhone, preUserId, amount, betweenreturn,
									betweenreturn, ordercode, lowerPhone, lowerUserId, "获得下级间推返佣");
							remainingAmount=remainingAmount.subtract(betweenreturn);
						}

					}else{
					}*/

                    if (j == 1) {
                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey(preGrade)
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put(preGrade, "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                        //直推返佣
                        if (straightReturn != null && straightReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(straightReturn).compareTo(BigDecimal.ZERO) > 0) {
                            DistributionRecord(preUserPhone, preUserId, amount, straightReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级直推返佣");
                            remainingAmount = remainingAmount.subtract(straightReturn);
                        }
                    } else if (j == 2) {
                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey(preGrade)
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put(preGrade, "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                        //间推返佣
                        if (betweenreturn != null && betweenreturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(betweenreturn).compareTo(BigDecimal.ZERO) > 0) {
                            DistributionRecord(preUserPhone, preUserId, amount, betweenreturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级间推返佣");
                            remainingAmount = remainingAmount.subtract(betweenreturn);
                        }

                    } else {
                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey(preGrade)
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put(preGrade, "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                    }

                }
                lowerUserId = preUserId;
                lowerPhone = preUserPhone;
                preUserId = xPreUserId;
                i++;
            } else {
                break;
            }
        }

        // 将剩余的产品金额给贴牌商
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/query/id";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", manageId);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String managephone = resultObj.getString("phone");

            /** 存贮分润记录明细 */
            DistributionRecord profitRecord = new DistributionRecord();
            profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
            profitRecord.setAcqAmount(remainingAmount);
            profitRecord.setAcqphone(managephone);
            profitRecord.setAcquserid(Long.parseLong(manageId));
            profitRecord.setAcqratio(BigDecimal.ZERO);
            profitRecord.setAmount(amount);
            profitRecord.setCreateTime(new Date());
            profitRecord.setOrdercode(ordercode);
            profitRecord.setOriphone(phone);
            profitRecord.setOriuserid(userid);
            profitRecord.setRemark("产品剩余分润");
            profitRecordBusiness.merge(profitRecord);

            // 增加贴牌商余额
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/rebate/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", remainingAmount.toString());
            requestEntity.add("user_id", manageId);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
        }
    }

    /**
     * 发放用户的返佣 新逻辑4
     **/
    private void sendThirdDistributionNew3(String ordercode, long userid, String phone, BigDecimal amount,
                                           Map<String, String> map, long brandid, String grade) {
        int i = 1;
        int j = 0;
        long lowerUserId = userid;
        String lowerPhone = phone;

        BigDecimal remainingAmount = amount;
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        String url = "http://user/v1.0/user/query/id";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity.add("id", userid + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/user/query/id:" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        long preUserId = resultObj.getLong("preUserId");
        String preUserPhone = resultObj.getString("preUserPhone");
        String oriGrade = resultObj.getString("grade");

        url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
        result = restTemplate.getForObject(url, String.class);

        /** 根据的渠道标识或去渠道的相关信息 */
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String manageId = resultObj.getString("manageid");
        int rebateCount = resultObj.getInt("rebateCount");
        //固定奖励金额
        BigDecimal constantReturn = null;
        //直推额外金额
        BigDecimal straightReturn = null;
        //间推额外金额
        BigDecimal betweenreturn = null;
        Map<String, String> gradeMap = new HashMap<String, String>();
        Map<String, String> levelMap = new HashMap<String, String>();

        /** 根据userid 找到上级， 如果上级的等级大于等于他就发放返佣，然后继续循环方法 */
        while (true) {
            j++;
            if (i > rebateCount) {
                break;
            }
            if (preUserId != 0) {
                // restTemplate = new RestTemplate();
                // uri = util.getServiceUrl("user", "error url request!");
                url = "http://user/v1.0/user/query/id";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("id", preUserId + "");
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
                String preGrade = resultObj.getString("grade");
                preUserPhone = resultObj.getString("phone");
                long xPreUserId = resultObj.getLong("preUserId");
                /** 如果上级没有购买三级分销或者前面的三级分销等级比自己的等级低 */
                {
                    if (preUserId == Long.valueOf(manageId).longValue()) {
                        break;
                    }
                    if (j == 1) {
                        //给第一层赋值
                        levelMap.put("level1", preGrade);
                    }
                    if (map.containsKey(preGrade)) {
                        String[] grantAmounts = map.get(preGrade).split(",");
                        //固定奖励金额
                        constantReturn = new BigDecimal(grantAmounts[0]).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //直推额外金额
                        straightReturn = new BigDecimal(grantAmounts[1]).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //间推额外金额
                        betweenreturn = new BigDecimal(grantAmounts[2]).setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else {
                        preUserId = xPreUserId;
                        continue;
                    }

                    if (j == 1) {

                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey("constantReturn")
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put("constantReturn", "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                        //直推返佣
                        if (straightReturn != null && straightReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(straightReturn).compareTo(BigDecimal.ZERO) > 0) {
                            DistributionRecord(preUserPhone, preUserId, amount, straightReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级直推返佣");
                            remainingAmount = remainingAmount.subtract(straightReturn);
                        }
                    } else if (j == 2) {
                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey("constantReturn")
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put("constantReturn", "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                        //间推返佣
                        //获取用户等级
                        int level1Grade = Integer.parseInt(levelMap.get("level1"));

                        if (betweenreturn != null && betweenreturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(betweenreturn).compareTo(BigDecimal.ZERO) > 0
                                && level1Grade < 2) {
                            DistributionRecord(preUserPhone, preUserId, amount, betweenreturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级间推返佣");
                            remainingAmount = remainingAmount.subtract(betweenreturn);
                        }

                    } else {
                        //固定奖励金额
                        if (constantReturn != null && constantReturn.compareTo(BigDecimal.ZERO) > 0
                                && remainingAmount.subtract(constantReturn).compareTo(BigDecimal.ZERO) > 0
                                && !gradeMap.containsKey("constantReturn")
                        ) {
                            DistributionRecord(preUserPhone, preUserId, amount, constantReturn,
                                    BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, "获得下级固定返佣");
                            gradeMap.put("constantReturn", "");
                            remainingAmount = remainingAmount.subtract(constantReturn);
                        }
                    }

                }
                lowerUserId = preUserId;
                lowerPhone = preUserPhone;
                preUserId = xPreUserId;
                i++;
            } else {
                break;
            }
        }

        // 将剩余的产品金额给贴牌商
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/query/id";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", manageId);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String managephone = resultObj.getString("phone");

            /** 存贮分润记录明细 */
            DistributionRecord profitRecord = new DistributionRecord();
            profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
            profitRecord.setAcqAmount(remainingAmount);
            profitRecord.setAcqphone(managephone);
            profitRecord.setAcquserid(Long.parseLong(manageId));
            profitRecord.setAcqratio(BigDecimal.ZERO);
            profitRecord.setAmount(amount);
            profitRecord.setCreateTime(new Date());
            profitRecord.setOrdercode(ordercode);
            profitRecord.setOriphone(phone);
            profitRecord.setOriuserid(userid);
            profitRecord.setRemark("产品剩余分润");
            profitRecordBusiness.merge(profitRecord);

            // 增加贴牌商余额
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/rebate/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", remainingAmount.toString());
            requestEntity.add("user_id", manageId);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
        }
    }

    private void DistributionRecord(String preUserPhone, long preUserId, BigDecimal amount, BigDecimal acqAmount
            , BigDecimal acqratio, String ordercode, String lowerPhone, long lowerUserId, String remark) {

        /** 存贮分润记录明细 */
        DistributionRecord profitRecord = new DistributionRecord();
        profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
        profitRecord.setAcqphone(preUserPhone);
        profitRecord.setAcquserid(preUserId);
        profitRecord.setAmount(amount);
        profitRecord.setAcqAmount(acqAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
        profitRecord.setAcqratio(acqratio);
        profitRecord.setCreateTime(new Date());
        profitRecord.setOrdercode(ordercode);
        profitRecord.setOriphone(lowerPhone);
        profitRecord.setOriuserid(lowerUserId);
        profitRecord.setRemark(remark);
        profitRecordBusiness.merge(profitRecord);
        /** 修改用户的分润 */
        // restTemplate = new RestTemplate();
        // uri = util.getServiceUrl("user", "error url request!");
        String url = "http://user/v1.0/user/rebate/update";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("rebate_amount",
                acqAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        requestEntity.add("user_id", preUserId + "");
        requestEntity.add("order_code", ordercode);
        requestEntity.add("order_type", "1");
        String result = restTemplate.postForObject(url, requestEntity, String.class);

    }

    /**
     * 发放用户的返佣 新逻辑
     **/
    private void sendThirdDistributionNew(String ordercode, long userid, long channelId, String phone, BigDecimal amount,
                                          Map<String, BigDecimal> map, long brandid, String grade) {
        int i = 1;

        long lowerUserId = userid;
        String lowerPhone = phone;
        BigDecimal remainingAmount = amount;
        String manageId = null;
        String oriGrade = null;
        String oriFullName = null;
        int count = 0;
        long level = 0;
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userid + "");
        requestEntity.add("channelId", channelId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            if (jsonArray.size() > 0) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    Object jsonObject = jsonArray.get(j);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, jsonObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }
            }

            String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
            String result = restTemplate.getForObject(url, String.class);

            /** 根据的渠道标识或去渠道的相关信息 */
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            manageId = resultObj.getString("manageid");
            int rebateCount = resultObj.getInt("rebateCount");
            BigDecimal rate = null;
            BigDecimal rateTemp = BigDecimal.ZERO;

            url = "http://user/v1.0/user/query/id";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity.add("id", userid + "");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            oriGrade = resultObj.getString("grade");
            oriFullName = resultObj.getString("fullname");
            UserRealtion preUserRealtion = null;
            for (UserRealtion userRealtion : userRealtions) {
                String preGrade = userRealtion.getPreUserGrade() + "";
                String preUserPhone = userRealtion.getPreUserPhone();
                Long preUserId = userRealtion.getPreUserId();
                level = userRealtion.getLevel();

                String firstPhone = "";
                Long firstUserId = null;
                String firstUserName = null;
                String firstGrade = null;
                if (count == 0) {
                    firstPhone = userRealtion.getFirstUserPhone();
                    firstUserId = userRealtion.getFirstUserId();
                    firstUserName = userRealtion.getFirstUserName();
                    firstGrade = oriGrade;
                } else {
                    firstPhone = preUserRealtion.getPreUserPhone();
                    firstUserId = preUserRealtion.getPreUserId();
                    firstUserName = preUserRealtion.getPreUserName();
                    firstGrade = preUserRealtion.getPreUserGrade() + "";
                }

                if (i > rebateCount) {
                    LOG.info("返佣次数已达上限======");
                    if (preUserId == Long.valueOf(manageId).longValue()) {
                        break;
                    }
                    mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                    preUserRealtion = userRealtion;
                    count += 1;
                    continue;

                } else {
                    if ("0".equals(preGrade) || (new BigDecimal(preGrade).compareTo(new BigDecimal(oriGrade)) < 0)) {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        count += 1;
                        continue;
                    } else {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
                        try {
                            if ((map.get(preGrade).subtract(rateTemp)).compareTo(BigDecimal.ZERO) <= 0) {

                                mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                                preUserRealtion = userRealtion;
                                count += 1;
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        rateTemp = map.get(preGrade).subtract(rateTemp);
                        rate = rateTemp;
                        rateTemp = map.get(preGrade);

                        mergeThirdDistribution(preUserPhone, preUserId, preGrade, amount, rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(preGrade), ordercode, firstPhone, firstUserId, firstGrade, "获得下级用户返佣", level, firstUserName, userRealtion.getPreUserName());
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(preGrade), ordercode, firstPhone, firstUserId, firstGrade, "获得下级用户返佣", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        count += 1;
                        /** 存贮分润记录明细 *//*

						/** 修改用户的分润 */
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/rebate/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("rebate_amount", rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        requestEntity.add("user_id", preUserId + "");
                        requestEntity.add("order_code", ordercode);
                        requestEntity.add("order_type", "1");
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        remainingAmount = remainingAmount.subtract(rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    i++;
                }
            }
        }

        // 发放卓越奖
        /*PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
        remainingAmount = sendExcellenceAward(order, remainingAmount);*/


        // 将剩余的产品金额给贴牌商
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            String url = "http://user/v1.0/user/query/id";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", manageId);
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            String managephone = resultObj.getString("phone");
            String manageGrade = resultObj.getString("grade");
            String fullName = resultObj.getString("fullname");

            mergeThirdDistribution(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount, BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, oriGrade, "产品剩余分润", level, oriFullName, fullName);
            mergeThirdDistributionCopy(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount, BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, oriGrade, "产品剩余分润", level, oriFullName, fullName);
            /** 存贮分润记录明细 */

            // 增加贴牌商余额
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/rebate/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", remainingAmount.toString());
            requestEntity.add("user_id", manageId);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
        }
    }

    public BigDecimal sendExcellenceAward(PaymentOrder order, BigDecimal remainingAmount) {
        LOG.info("开始发放卓越奖============order:=============" + order);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        BigDecimal acqAmount = BigDecimal.ZERO;
        long userId = order.getUserid();
        BigDecimal amount = order.getAmount();
        String ordercode = order.getOrdercode();
        Long userid = order.getUserid();
        String phone = order.getPhone();
        String userName = order.getUserName();
        Long brandId = order.getBrandid();

        String grade = null;
        Long preId;
        String prePhone;
        String preGrade;
        // 分润发起源
        String url = uri.toString() + "/v1.0/user/find/by/userid";
        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("userId", userId);
        String respJSON = restTemplate.postForObject(url, requestMap, String.class);
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(respJSON);
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (CommonConstants.SUCCESS.equals(respCode)) {
            String userJSON = jsonObject.getString(CommonConstants.RESULT);
            com.alibaba.fastjson.JSONObject userObject = com.alibaba.fastjson.JSONObject.parseObject(userJSON);
            grade = userObject.getString("grade");
        }
        // 卓越奖收益人
        url = uri.toString() + "/v1.0/user/query/partner/id";
        requestMap = new LinkedMultiValueMap<>();
        requestMap.add("userId", userId);
        respJSON = restTemplate.postForObject(url, requestMap, String.class);
        LOG.info("卓越奖查询上三级合伙人返回信息：========" + respJSON);
        com.alibaba.fastjson.JSONObject respJSONObject = com.alibaba.fastjson.JSONObject.parseObject(respJSON);
        if (CommonConstants.SUCCESS.equals(respJSONObject.getString(CommonConstants.RESP_CODE))) {
            String userJSON = respJSONObject.getString(CommonConstants.RESULT);
            com.alibaba.fastjson.JSONObject userObject = com.alibaba.fastjson.JSONObject.parseObject(userJSON);
            preId = Long.valueOf(userObject.getString("id"));
            prePhone = userObject.getString("phone");
            preGrade = userObject.getString("grade");

            if (amount.compareTo(new BigDecimal(99)) == 0) {
                acqAmount = BigDecimal.ONE;
            }
            if (amount.compareTo(new BigDecimal(399)) == 0) {
                acqAmount = new BigDecimal(3);
            }
            remainingAmount = remainingAmount.subtract(acqAmount);
            DistributionRecord dbr = new DistributionRecord();
            dbr.setOrdercode(ordercode);
            dbr.setAmount(amount);
            dbr.setOriuserid(userid);
            dbr.setOriphone(phone);
            dbr.setOriGrade(grade);
            dbr.setOriUserName(userName);
            dbr.setAcquserid(preId);
            dbr.setAcqphone(prePhone);
            dbr.setAcqratio(BigDecimal.ZERO);
            dbr.setAcqGrade(preGrade);
            dbr.setAcqAmount(acqAmount);
            dbr.setRemark("卓越奖分润");

            dbr.setCreateTime(new Date());
            url = uri.toString() + "/v1.0/user/realtion/level/query/firstUserId/and/preuserid";
            requestMap = new LinkedMultiValueMap<>();
            requestMap.add("firstUserId", userId);
            requestMap.add("preUserId", preId);
            respJSON = restTemplate.postForObject(url, requestMap, String.class);
            LOG.info("查询的用户关系等级：======" + respJSON);
            com.alibaba.fastjson.JSONObject parseObject = com.alibaba.fastjson.JSONObject.parseObject(respJSON);
            Long level = null;
            if (CommonConstants.SUCCESS.equals(parseObject.getString(CommonConstants.RESP_CODE))) {
                String userRealtion = parseObject.getString(CommonConstants.RESULT);
                com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(userRealtion);
                level = Long.valueOf(jsonObject1.getString("level"));
            }
            dbr.setLevel(level);
            DistributionRecord record = distributionBusiness.addDistribution(dbr);
            LOG.info("分润明细添加结果：============" + record);
            url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandId;
            String result = restTemplate.getForObject(url, String.class);
            com.alibaba.fastjson.JSONObject ss = com.alibaba.fastjson.JSONObject.parseObject(result);
            com.alibaba.fastjson.JSONObject resultObjE = jsonObject.getJSONObject("result");

            String manageId = resultObjE.getString("manageid");
            //更新余额
            this.updateRebate(acqAmount + "", preId + "", ordercode, manageId);
            LOG.info("====================卓越奖发放完成====================");
        }
        return remainingAmount;
    }

    /**
     * 发放申请信用卡成功返佣
     *
     * @param ordercode
     * @param userid
     * @param channelId
     * @param phone
     * @param amount
     * @param brandid
     * @param grade
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/thirdDistribution/send")
    public void sendThirdDistributionCreditCard(
            @RequestParam(value = "ordercode") String ordercode,
            @RequestParam(value = "userId") String userid,
            @RequestParam(value = "channelId") String channelId,
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "amount") BigDecimal amount,
            @RequestParam(value = "brandId") String brandid,
            @RequestParam(value = "grade") String grade) {
        LOG.info("信用卡发放返佣开始了======================");
        //查询当前贴牌信用卡返佣比率
        String url = "http://user/v1.0/user/creditcard/ratio/query";
        MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
        requestEntity1.add("brand_id", brandid);
        String result = restTemplate.postForObject(url, requestEntity1, String.class);
        JSONObject jsonObjectRatio = JSONObject.fromObject(result);
        JSONArray jsonArrayRatio = jsonObjectRatio.getJSONArray("result");
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        for (int i = 0; i < jsonArrayRatio.size(); i++) {
            map.put(jsonArrayRatio.optJSONObject(i).optString("preLevel"), new BigDecimal(jsonArrayRatio.optJSONObject(i).optString("creditRatio")));
        }
        LOG.info("map=========" + map);

        int i = 1;
        long userId = Long.parseLong(userid);
        long lowerUserId = userId;
        String lowerPhone = phone;
        BigDecimal remainingAmount = amount;
        String manageId = null;
        String oriGrade = null;
        String oriFullName = null;
        int count = 0;
        long level = 0;
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userid + "");
        requestEntity.add("channelId", channelId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            if (jsonArray.size() > 0) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    Object jsonObject = jsonArray.get(j);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, jsonObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }
            }

            url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
            result = restTemplate.getForObject(url, String.class);

            /** 根据的渠道标识或去渠道的相关信息 */
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            manageId = resultObj.getString("manageid");
            int rebateCount = resultObj.getInt("rebateCount");
            BigDecimal rate = null;
            BigDecimal rateTemp = BigDecimal.ZERO;

            url = "http://user/v1.0/user/query/id";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity.add("id", userid + "");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            oriGrade = resultObj.getString("grade");
            oriFullName = resultObj.getString("fullname");
            UserRealtion preUserRealtion = null;
            for (UserRealtion userRealtion : userRealtions) {
                String preGrade = userRealtion.getPreUserGrade() + "";
                String preUserPhone = userRealtion.getPreUserPhone();
                Long preUserId = userRealtion.getPreUserId();
                level = userRealtion.getLevel();

                String firstPhone = "";
                Long firstUserId = null;
                String firstUserName = null;
                String firstGrade = null;
                if (count == 0) {
                    firstPhone = userRealtion.getFirstUserPhone();
                    firstUserId = userRealtion.getFirstUserId();
                    firstUserName = userRealtion.getFirstUserName();
                    firstGrade = oriGrade;
                } else {
                    firstPhone = preUserRealtion.getPreUserPhone();
                    firstUserId = preUserRealtion.getPreUserId();
                    firstUserName = preUserRealtion.getPreUserName();
                    firstGrade = preUserRealtion.getPreUserGrade() + "";
                }

                if (i > rebateCount) {
                    LOG.info("返佣次数已达上限======");
                    if (preUserId == Long.valueOf(manageId).longValue()) {
                        break;
                    }
                    mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                    preUserRealtion = userRealtion;
                    count += 1;
                    continue;

                } else {
                    if ("0".equals(preGrade) || (new BigDecimal(preGrade).compareTo(new BigDecimal(oriGrade)) < 0)) {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        count += 1;
                        continue;
                    } else {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
                        try {
                            if ((map.get(level + "").subtract(rateTemp)).compareTo(BigDecimal.ZERO) <= 0) {

                                mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                                preUserRealtion = userRealtion;
                                count += 1;
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
//                        rateTemp = map.get(preGrade).subtract(rateTemp);
//                        rate = rateTemp;
//                        rateTemp = map.get(preGrade);
                        rate = map.get(level + "");
                        mergeThirdDistribution(preUserPhone, preUserId, preGrade, amount, rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(level + ""), ordercode, firstPhone, firstUserId, firstGrade, "获得下级用户返佣（信用卡）", level, firstUserName, userRealtion.getPreUserName());
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(level + ""), ordercode, firstPhone, firstUserId, firstGrade, "获得下级用户返佣（信用卡）", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        count += 1;
                        /** 存贮分润记录明细 *//*

						/** 修改用户的分润 */
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/rebate/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("rebate_amount", rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        requestEntity.add("user_id", preUserId + "");
                        requestEntity.add("order_code", ordercode);
                        requestEntity.add("order_type", "1");
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        remainingAmount = remainingAmount.subtract(rate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    i++;
                }
            }
        }

        // 将剩余的产品金额给贴牌商
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/query/id";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", manageId);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            String managephone = resultObj.getString("phone");
            String manageGrade = resultObj.getString("grade");
            String fullName = resultObj.getString("fullname");

            mergeThirdDistribution(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount, BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, oriGrade, "产品剩余分润（信用卡）", level, oriFullName, fullName);
            mergeThirdDistributionCopy(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount, BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, oriGrade, "产品剩余分润（信用卡）", level, oriFullName, fullName);
            /** 存贮分润记录明细 */

            // 增加贴牌商余额
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/rebate/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", remainingAmount.toString());
            requestEntity.add("user_id", manageId);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
        }
    }

    private void mergeThirdDistribution(String preUserPhone, long preUserId, String preGrade, BigDecimal amount, BigDecimal acqAmount, BigDecimal acqRatio, String orderCode, String lowerPhone, long lowerUserId, String oriGrade, String remark, long level, String oriUserName, String acqUserName) {

        DistributionRecord profitRecord = new DistributionRecord();
        profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
        profitRecord.setAcqphone(preUserPhone);
        profitRecord.setAcquserid(preUserId);
        profitRecord.setAcqGrade(preGrade);
        profitRecord.setAmount(amount);
        profitRecord.setAcqAmount(acqAmount);
        profitRecord.setAcqratio(acqRatio);
        profitRecord.setCreateTime(new Date());
        profitRecord.setOrdercode(orderCode);
        profitRecord.setOriphone(lowerPhone);
        profitRecord.setOriuserid(lowerUserId);
        profitRecord.setOriGrade(oriGrade);
        profitRecord.setRemark(remark);
        profitRecord.setLevel(level);
        profitRecord.setOriUserName(oriUserName);
        profitRecord.setAcqUserName(acqUserName);
        profitRecordBusiness.merge(profitRecord);

    }

    private void mergeThirdDistributionCopy(String preUserPhone, long preUserId, String preGrade, BigDecimal amount, BigDecimal acqAmount, BigDecimal acqRatio, String orderCode, String lowerPhone, long lowerUserId, String oriGrade, String remark, long level, String oriUserName, String acqUserName) {

        DistributionRecordCopy profitRecord = new DistributionRecordCopy();
        profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
        profitRecord.setAcqphone(preUserPhone);
        profitRecord.setAcquserid(preUserId);
        profitRecord.setAcqGrade(preGrade);
        profitRecord.setAmount(amount);
        profitRecord.setAcqAmount(acqAmount);
        profitRecord.setAcqratio(acqRatio);
        profitRecord.setCreateTime(new Date());
        profitRecord.setOrdercode(orderCode);
        profitRecord.setOriphone(lowerPhone);
        profitRecord.setOriuserid(lowerUserId);
        profitRecord.setOriGrade(oriGrade);
        profitRecord.setRemark(remark);
        profitRecord.setLevel(level);
        profitRecord.setOriUserName(oriUserName);
        profitRecord.setAcqUserName(acqUserName);
        profitRecordBusiness.mergeCopy(profitRecord);

    }

    /**
     * 发放用户的返佣
     **/
    private void sendThirdDistribution(String ordercode, long userid, long channelId, String phone, BigDecimal amount,
                                       Map<String, BigDecimal> map, long brandid) {

        //long lowerUserId = userid;
        //String lowerPhone = phone;
        BigDecimal remainingAmount = amount;
        String manageId = null;
        String oriGrade = null;
        String oriFullName = null;
        long level = 0;
        int i = 1;

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userid + "");
        requestEntity.add("channelId", channelId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            if (jsonArray.size() > 0) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    Object jsonObject = jsonArray.get(j);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, jsonObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }

                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }
            }

            String url = "http://user/v1.0/user/query/id";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity.add("id", userid + "");
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            oriGrade = resultObj.getString("grade");
            oriFullName = resultObj.getString("fullname");

            url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
            result = restTemplate.getForObject(url, String.class);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            manageId = resultObj.getString("manageid");
            int rebateCount = 0;
            UserRealtion preUserRealtion = null;
            for (UserRealtion userRealtion : userRealtions) {
                String preGrade = userRealtion.getPreUserGrade() + "";
                String preUserPhone = userRealtion.getPreUserPhone();
                Long preUserId = userRealtion.getPreUserId();
                level = userRealtion.getLevel();

                String firstPhone = "";
                Long firstUserId = null;
                String firstUserName = null;
                String firstGrade = null;
                if (rebateCount == 0) {
                    firstPhone = userRealtion.getFirstUserPhone();
                    firstUserId = userRealtion.getFirstUserId();
                    firstUserName = userRealtion.getFirstUserName();
                    firstGrade = oriGrade;
                } else {
                    firstPhone = preUserRealtion.getPreUserPhone();
                    firstUserId = preUserRealtion.getPreUserId();
                    firstUserName = preUserRealtion.getPreUserName();
                    firstGrade = preUserRealtion.getPreUserGrade() + "";
                }

                if (i > 3) {
                    LOG.info("返佣次数已达上限======");
                    if (preUserId == Long.valueOf(manageId).longValue()) {
                        break;
                    }
                    //mergeThirdDistribution(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                    mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                    preUserRealtion = userRealtion;
                    rebateCount += 1;
                    continue;

                } else {

                    if ("0".equals(preGrade) || (new BigDecimal(preGrade).compareTo(new BigDecimal(oriGrade)) < 0)) {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, firstPhone, firstUserId, firstGrade, "下级用户返佣记录", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        rebateCount += 1;
                        continue;
                    } else {
                        if (preUserId == Long.valueOf(manageId).longValue()) {
                            break;
                        }
						/*if ((map.get(preGrade).subtract(rateTemp)).compareTo(BigDecimal.ZERO) <= 0) {

							mergeThirdDistribution(preUserPhone, preUserId, preGrade, amount, BigDecimal.ZERO, BigDecimal.ZERO, ordercode, lowerPhone, lowerUserId, oriGrade, "下级用户返佣记录", level);

							continue;
						}*/

                        /** 存贮返佣记录明细 */
                        mergeThirdDistribution(preUserPhone, preUserId, preGrade, amount, map.get(preGrade).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(preGrade), ordercode, firstPhone, firstUserId, firstGrade, "产品分润", level, firstUserName, userRealtion.getPreUserName());
                        mergeThirdDistributionCopy(preUserPhone, preUserId, preGrade, amount, map.get(preGrade).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP), map.get(preGrade), ordercode, firstPhone, firstUserId, firstGrade, "产品分润", level, firstUserName, userRealtion.getPreUserName());
                        preUserRealtion = userRealtion;
                        rebateCount += 1;
                        /** 修改用户的分润 */
                        // restTemplate = new RestTemplate();
                        // uri = util.getServiceUrl("user", "error url request!");
                        url = "http://user/v1.0/user/rebate/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("rebate_amount", map.get(preGrade).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        requestEntity.add("user_id", preUserId + "");
                        requestEntity.add("order_code", ordercode);
                        requestEntity.add("order_type", "1");
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        remainingAmount = remainingAmount.subtract(map.get(preGrade).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    i++;

                }

            }

        }


        // 将剩余的产品金额给贴牌商
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            String url = "http://user/v1.0/user/query/id";
            /** 根据的渠道标识或去渠道的相关信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("id", manageId);
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/query/id:" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            String managephone = resultObj.getString("phone");
            String manageGrade = resultObj.getString("grade");
            String fullName = resultObj.getString("fullname");

            mergeThirdDistribution(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount,
                    BigDecimal.ZERO, ordercode, phone, userid, oriGrade, "产品剩余分润", level, oriFullName, fullName);
            mergeThirdDistributionCopy(managephone, Long.parseLong(manageId), manageGrade, amount, remainingAmount,
                    BigDecimal.ZERO, ordercode, phone, userid, oriGrade, "产品剩余分润", level, oriFullName, fullName);
            /** 存贮分润记录明细 */

            // 增加贴牌商余额
            // restTemplate = new RestTemplate();
            // uri = util.getServiceUrl("user", "error url request!");
            url = "http://user/v1.0/user/rebate/update";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", remainingAmount.toString());
            requestEntity.add("user_id", manageId);
            requestEntity.add("order_code", ordercode);
            requestEntity.add("order_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
        }
    }

    /**
     * 发放用户的积分
     */
    private void sendCoin(String ordercode, String type, long userid, String phone, long brandid, String brandname,
                          BigDecimal amount) {

        // RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        // URI uri = util.getServiceUrl("user", "error url request!");
        String url = "http://user/v1.0/user/brandcoin/query";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandid + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        String ratio = resultObj.getString("ratio");

        BigDecimal coinBd = amount.divide(new BigDecimal(ratio), RoundingMode.DOWN);

        CoinRecord coinRecord = new CoinRecord();
        coinRecord.setBrand_id(brandid);
        coinRecord.setCoin(coinBd.intValue());
        coinRecord.setCreateTime(new Date());
        coinRecord.setOrdercode(ordercode);
        coinRecord.setBrand_name(brandname);
        if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
            coinRecord.setRemark("消费返积分");
        } else {
            coinRecord.setRemark("充值返积分");
        }
        coinRecord.setType(CommonConstants.COIN_TYPE_ADD);
        coinRecord.setUserid(userid);
        coinRecord.setUserphone(phone);
        coinRecordBusiness.mergeCoinRecord(coinRecord);

        /** 存储 用户的积分记录 */
        // restTemplate = new RestTemplate();
        // uri = util.getServiceUrl("user", "error url request!");
        url = "http://user/v1.0/user/coin/update/userid";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        requestEntity.add("coin", coinBd.intValue() + "");
        requestEntity.add("order_code", ordercode);
        result = restTemplate.postForObject(url, requestEntity, String.class);
    }

    /**
     * 发放用户的积分
     */
    private void redsendCoin(String ordercode, String type, long userid, String phone, long brandid, String brandname,
                             BigDecimal coin) {
        BigDecimal coinBd = coin;
        CoinRecord coinRecord = new CoinRecord();
        coinRecord.setBrand_id(brandid);
        coinRecord.setCoin(coinBd.intValue());
        coinRecord.setCreateTime(new Date());
        coinRecord.setOrdercode(ordercode);
        coinRecord.setBrand_name(brandname);
        if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_PAY)) {
            coinRecord.setRemark("消费返积分");
        } else {
            coinRecord.setRemark("充值返积分");
        }
        coinRecord.setType(CommonConstants.COIN_TYPE_ADD);
        coinRecord.setUserid(userid);
        coinRecord.setUserphone(phone);
        coinRecordBusiness.mergeCoinRecord(coinRecord);

        /** 存储 用户的积分记录 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        String url = "http://user/v1.0/user/coin/update/userid";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        requestEntity.add("coin", coinBd.intValue() + "");
        requestEntity.add("order_code", ordercode);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
    }


//	@RequestMapping(method = RequestMethod.POST,value = "/v1.0/transactionclear/payment/arebate2")
//	@ResponseBody
//	public Object rebate2(@RequestParam(value="status")String status){
//		if(status.equals("1")){
//			Long userId=Long.valueOf("850471206");
//			Long channelId=Long.valueOf("232");
//			BigDecimal amount=new BigDecimal("100.00");
//			BigDecimal currate=new BigDecimal("0.0060");
//			Long brandId =Long.valueOf("100247");
//			rebate2("201909051322150471",userId,channelId,amount,currate,brandId);
//		}
//		return null;
//	}

    private void rebate2(String ordercode, long userid, long channelId, BigDecimal amount, BigDecimal currate, long brandid) {
        boolean isCancel = false;
        boolean isequalRebateRate = false;
        String equalRebateRate = "";

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/id";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity.add("id", userid + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        String grade = resultObj.getString("grade");
        Integer equalGrade = Integer.valueOf("0".equals(grade) ? "1" : grade);
        /** 根据userid 找到上级， 如果上级的费率比他低就发放分润，然后继续循环方法 */
        while (true) {

            if (isCancel) {
                break;
            }

            restTemplate = new RestTemplate();
            requestEntity = new LinkedMultiValueMap<String, String>();
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/query/id";

            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity.add("id", userid + "");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String oriphone = resultObj.getString("phone");
            long preuserid = resultObj.getLong("preUserId");
            grade = resultObj.getString("grade");

            String preuserphone = resultObj.getString("preUserPhone");

            /** 用户证明有上级用户 */
            if (preuserid != 0 && preuserphone != null && !preuserphone.equalsIgnoreCase("")) {

                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/channel/rate/query/userid";

                /** 根据的渠道标识或去渠道的相关信息 */
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("channel_id", channelId + "");
                requestEntity.add("user_id", preuserid + "");

                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);

                if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
                    LOG.error("亲.无渠道费率,请配置渠道费率哦!======userId:" + preuserid + "channelId:" + channelId);
                    // throw new RuntimeException("亲.无渠道费率,请配置渠道费率哦!");
                    userid = preuserid;
                    continue;
                }

                resultObj = jsonObject.getJSONObject("result");
                String rate = resultObj.getString("rate");

                /** 证明上级的费率小于当前 */
                if (new BigDecimal(rate).compareTo(currate) < 0) {

                    /** 计算分润的钱 */
                    BigDecimal rebate = (currate.subtract(new BigDecimal(rate))).multiply(amount).setScale(2,
                            BigDecimal.ROUND_HALF_UP);
                    if (rebate.toString().equals("0.00")) {
                        /** 当前用户作为下一次循环的初始用户了 */
                        userid = preuserid;
                        currate = new BigDecimal(rate);
                        continue;
                    }
                    /** 存贮分润记录明细 */
                    ProfitRecord profitRecord = new ProfitRecord();
                    profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
                    profitRecord.setAcqAmount(rebate);
                    profitRecord.setAcqphone(preuserphone);
                    profitRecord.setAcqrate(new BigDecimal(rate));
                    profitRecord.setAcquserid(preuserid);
                    profitRecord.setAmount(amount);
                    profitRecord.setCreateTime(new Date());
                    profitRecord.setOrdercode(ordercode);
                    profitRecord.setOriphone(oriphone);
                    profitRecord.setOrirate(currate);
                    profitRecord.setOriuserid(userid);
                    profitRecord.setRemark("差额费率分润");
                    profitRecord.setScale(BigDecimal.ONE);
                    profitRecord.setType("0");
                    try {
                        profitRecordBusiness.merge(profitRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /** 存储 用户的分润记录 */
                    restTemplate = new RestTemplate();
                    uri = util.getServiceUrl("user", "error url request!");
                    url = uri.toString() + "/v1.0/user/rebate/update";
                    /** 根据的渠道标识或去渠道的相关信息 */
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("rebate_amount", rebate.toString());
                    requestEntity.add("user_id", preuserid + "");
                    requestEntity.add("order_code", ordercode);
                    try {
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                    } catch (RestClientException e) {
                        e.printStackTrace();
                    }

                    /** 当前用户作为下一次循环的初始用户了 */
                    userid = preuserid;
                    currate = new BigDecimal(rate);
                    continue;

                } else {

                    if (!isequalRebateRate) {
                        /** 根据品牌id获取同级的分润比率 */
                        restTemplate = new RestTemplate();
                        uri = util.getServiceUrl("user", "error url request!");
                        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
                        result = restTemplate.getForObject(url, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        resultObj = jsonObject.getJSONObject("result");
                        equalRebateRate = resultObj.getString("equalRebateRate");

                        isequalRebateRate = true;
                    }
                    /** 寻找比自己费率低的 */

                    while (true) {

                        /** 先发放同级分润 */
                        if (equalRebateRate != null && !equalRebateRate.equalsIgnoreCase("")
                                && !equalRebateRate.equalsIgnoreCase("null")) {

                            if (new BigDecimal(equalRebateRate).compareTo(BigDecimal.ZERO) > 0) {

                                restTemplate = new RestTemplate();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/query/id";
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                requestEntity.add("id", preuserid + "");
                                result = restTemplate.postForObject(url, requestEntity, String.class);
                                LOG.info("RESULT================" + result);
                                jsonObject = JSONObject.fromObject(result);
                                resultObj = jsonObject.getJSONObject("result");
                                String pregrade = resultObj.getString("grade");
                                if (equalGrade.equals(Integer.valueOf(pregrade))
                                        && Integer.parseInt(pregrade) == Integer.parseInt(grade)) {
                                    equalGrade = equalGrade + 1;

                                    uri = util.getServiceUrl("user", "error url request!");
                                    url = uri.toString() + "/v1.0/user/rebate/query/sumrebatepremonth";
                                    requestEntity = new LinkedMultiValueMap<String, String>();
                                    requestEntity.add("user_id", String.valueOf(userid));
                                    restTemplate = new RestTemplate();
                                    result = restTemplate.postForObject(url, requestEntity, String.class);
                                    LOG.info("RESULT================purchaseShopping" + result);
                                    jsonObject = JSONObject.fromObject(result);
                                    LOG.info("jsonObject================" + jsonObject);
                                    BigDecimal currentrebate = new BigDecimal("0");
                                    if (jsonObject.containsKey("result")) {
                                        JSONObject UserAccount = jsonObject.getJSONObject("result");
                                        currentrebate = new BigDecimal(UserAccount.getString("monthRebate"));
                                    }
                                    uri = util.getServiceUrl("user", "error url request!");
                                    url = uri.toString() + "/v1.0/user/rebate/query/sumrebatepremonth";
                                    requestEntity = new LinkedMultiValueMap<String, String>();
                                    requestEntity.add("user_id", String.valueOf(preuserid));
                                    restTemplate = new RestTemplate();
                                    result = restTemplate.postForObject(url, requestEntity, String.class);
                                    LOG.info("RESULT================purchaseShopping" + result);
                                    jsonObject = JSONObject.fromObject(result);
                                    LOG.info("jsonObject================" + jsonObject);
                                    BigDecimal prerebate = new BigDecimal("0");
                                    if (jsonObject.containsKey("result")) {
                                        JSONObject UserAccount = jsonObject.getJSONObject("result");
                                        prerebate = new BigDecimal(UserAccount.getString("monthRebate"));
                                    }
                                    /** 计算分润的钱 */
                                    BigDecimal rebate = new BigDecimal("0");
                                    if (currentrebate.compareTo(prerebate) < 0) {
                                        rebate = new BigDecimal(equalRebateRate).multiply(currentrebate).setScale(2,
                                                BigDecimal.ROUND_HALF_UP);
                                    } else {
                                        rebate = new BigDecimal(equalRebateRate).multiply(prerebate).setScale(2,
                                                BigDecimal.ROUND_HALF_UP);
                                    }
                                    LOG.info("===============用户ID" + preuserid + "," + "获得分润:" + rebate.toString()
                                            + "===============");
                                    if (rebate.toString().equals("0.00")) {
                                        userid = preuserid;
                                        currate = new BigDecimal(rate);
                                        break;
                                    }
                                    /** 存贮分润记录明细 */
                                    ProfitRecord profitRecord = new ProfitRecord();
                                    profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
                                    profitRecord.setAcqAmount(rebate);
                                    profitRecord.setAcqphone(preuserphone);
                                    profitRecord.setAcqrate(new BigDecimal(rate));
                                    profitRecord.setAcquserid(preuserid);
                                    profitRecord.setAmount(amount);
                                    profitRecord.setCreateTime(new Date());
                                    profitRecord.setOrdercode(ordercode);
                                    profitRecord.setOriphone(oriphone);
                                    profitRecord.setOrirate(currate);
                                    profitRecord.setOriuserid(userid);
                                    profitRecord.setRemark("平级分润");
                                    profitRecord.setScale(BigDecimal.ONE);
                                    profitRecord.setType("3");
                                    profitRecordBusiness.merge(profitRecord);

                                    /** 存储 用户的分润记录 */
                                    restTemplate = new RestTemplate();
                                    uri = util.getServiceUrl("user", "error url request!");
                                    url = uri.toString() + "/v1.0/user/rebate/update";
                                    /** 根据的渠道标识或去渠道的相关信息 */
                                    requestEntity = new LinkedMultiValueMap<String, String>();
                                    requestEntity.add("rebate_amount", rebate.toString());
                                    requestEntity.add("user_id", preuserid + "");
                                    requestEntity.add("order_code", ordercode);
                                    result = restTemplate.postForObject(url, requestEntity, String.class);

                                    LOG.info("平级分润================================受益人:" + preuserid + ",等级:" + pregrade
                                            + ",平级等级:" + equalGrade);

                                }
                            }
                        }
                        restTemplate = new RestTemplate();
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        restTemplate = new RestTemplate();
                        uri = util.getServiceUrl("user", "error url request!");
                        url = uri.toString() + "/v1.0/user/query/id";

                        /** 根据的用户手机号码查询用户的基本信息 */
                        requestEntity.add("id", preuserid + "");
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        LOG.info("RESULT================" + result);
                        jsonObject = JSONObject.fromObject(result);
                        resultObj = jsonObject.getJSONObject("result");
                        preuserid = resultObj.getLong("preUserId");
                        grade = resultObj.getString("grade");
                        preuserphone = resultObj.getString("preUserPhone");

                        /** 用户证明有上级用户 */
                        if (preuserid != 0 && preuserphone != null && !preuserphone.equalsIgnoreCase("")) {

                            restTemplate = new RestTemplate();
                            uri = util.getServiceUrl("user", "error url request!");
                            url = uri.toString() + "/v1.0/user/channel/rate/query/userid";

                            /** 根据的渠道标识或去渠道的相关信息 */
                            requestEntity = new LinkedMultiValueMap<String, String>();
                            requestEntity.add("channel_id", channelId + "");
                            requestEntity.add("user_id", preuserid + "");

                            result = restTemplate.postForObject(url, requestEntity, String.class);
                            LOG.info("RESULT================" + result);
                            jsonObject = JSONObject.fromObject(result);

                            if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
                                LOG.error("亲.无渠道费率,请配置渠道费率哦!======userId:" + preuserid + "channelId:" + channelId);
                                // throw new RuntimeException("亲.无渠道费率,请配置渠道费率哦!");
                                userid = preuserid;
                                continue;
                            }

                            resultObj = jsonObject.getJSONObject("result");
                            rate = resultObj.getString("rate");

                            /** 证明上级的费率小于当前 */
                            if (new BigDecimal(rate).compareTo(currate) < 0) {

                                /** 计算分润的钱 */
                                BigDecimal rebate = (currate.subtract(new BigDecimal(rate))).multiply(amount)
                                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (rebate.toString().equals("0.00")) {
                                    /** 当前用户作为下一次循环的初始用户了 */
                                    userid = preuserid;
                                    currate = new BigDecimal(rate);
                                    break;
                                }
                                /** 存贮分润记录明细 */
                                ProfitRecord profitRecord = new ProfitRecord();
                                profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
                                profitRecord.setAcqAmount(rebate);
                                profitRecord.setAcqphone(preuserphone);
                                profitRecord.setAcqrate(new BigDecimal(rate));
                                profitRecord.setAcquserid(preuserid);
                                profitRecord.setAmount(amount);
                                profitRecord.setCreateTime(new Date());
                                profitRecord.setOrdercode(ordercode);
                                profitRecord.setOriphone(oriphone);
                                profitRecord.setOrirate(currate);
                                profitRecord.setOriuserid(userid);
                                profitRecord.setRemark("差额费率分润");
                                profitRecord.setScale(BigDecimal.ONE);
                                profitRecord.setType("0");
                                profitRecordBusiness.merge(profitRecord);

                                /** 存储 用户的分润记录 */
                                restTemplate = new RestTemplate();
                                uri = util.getServiceUrl("user", "error url request!");
                                url = uri.toString() + "/v1.0/user/rebate/update";
                                /** 根据的渠道标识或去渠道的相关信息 */
                                requestEntity = new LinkedMultiValueMap<String, String>();
                                requestEntity.add("rebate_amount", rebate.toString());
                                requestEntity.add("user_id", preuserid + "");
                                requestEntity.add("order_code", ordercode);
                                result = restTemplate.postForObject(url, requestEntity, String.class);

                                /** 当前用户作为下一次循环的初始用户了 */
                                userid = preuserid;
                                currate = new BigDecimal(rate);
                                break;
                            }

                        } else {
                            isCancel = true;
                            break;

                        }
                    }

                }

            } else {

                break;
            }
        }

    }

    /** 循环处理用户的分润 */
    /**
     * 分润的发放逻辑如下，如果刷卡用户的上级登记比自己高，那上级那自己的分润，如果上级的等级美欧自己的高，那么继续寻找上面一个比自己低的拿分润
     * 拿到分润的用户作为下一次循环的起点， 直接没有上级为止
     */
    public void rebate(String ordercode, long userid, long channelId, BigDecimal amount, BigDecimal currate, long brandid, String orderType) {
        BigDecimal currate2 = currate;
        long level = 0;
        BigDecimal currate1 = null;
        BigDecimal fixedRebate = BigDecimal.ZERO;
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userid + "");
        requestEntity.add("channelId", channelId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            BigDecimal totalRebate = BigDecimal.ZERO;
            BigDecimal minRate = BigDecimal.ZERO;
            if (jsonArray.size() > 0) {
                int rebateCount = 0;
                UserRealtion preUserRealtion = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    Object jsonObject = jsonArray.get(i);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, jsonObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                    if (i == 0) {
                        minRate = userRealtion.getRate();
                    } else if (minRate.compareTo(userRealtion.getRate()) > 0) {
                        minRate = userRealtion.getRate();
                    }
                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }

                // UserRealtion brandUserRealtion = userRealtions.get(userRealtions.size()-1);
                totalRebate = currate.subtract(minRate).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                LOG.info("订单====" + ordercode + "====总分润为====:" + totalRebate);

                //BigDecimal currate1 = currate;
                String firstPhone = "";
                Long firstUserId = null;
                String firstUserName = null;
                for (UserRealtion userRealtion : userRealtions) {
                    if (BigDecimal.ZERO.compareTo(totalRebate) >= 0) {
                        break;
                    }
                    if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType) || CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
                        if (userRealtion.getLevel().compareTo(1) == 0) {
                            Integer firstUserGrade = userRealtion.getFirstUserGrade();
                            Integer preUserGrade = userRealtion.getPreUserGrade();
                            firstPhone = userRealtion.getFirstUserPhone();
                            firstUserId = userRealtion.getFirstUserId();
                            firstUserName = userRealtion.getFirstUserName();
                            if (firstUserGrade == 0 && preUserGrade == 0) {
                                fixedProfit getfixedProfitByBrandIdAndGrade = fixedProfitBusiness.getfixedProfitByBrandIdAndGrade(brandid + "", preUserGrade);
                                String fixedProfit = "0";
                                if (getfixedProfitByBrandIdAndGrade != null) {
                                    fixedProfit = getfixedProfitByBrandIdAndGrade.getFixedProfit();
                                    fixedRebate = amount.multiply(new BigDecimal(fixedProfit)).setScale(2, BigDecimal.ROUND_HALF_DOWN);

                                    totalRebate = totalRebate.subtract(fixedRebate);
                                    if (BigDecimal.ZERO.compareTo(totalRebate) > 0) {
                                        fixedRebate = totalRebate.add(fixedRebate);
                                    }

                                    if (fixedRebate.compareTo(BigDecimal.ZERO) > 0) {
                                        LOG.info("表示直推分润大于0======");
                                        requestEntity = new LinkedMultiValueMap<String, String>();
                                        requestEntity.add("rebate_amount", fixedRebate + "");
                                        requestEntity.add("user_id", userRealtion.getPreUserId() + "");
                                        requestEntity.add("order_code", ordercode);
                                        requestEntity.add("order_type", "2");
                                        Map<String, Object> resultMap = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/rebate/update", requestEntity);
                                        LOG.info("=====orderCode:" + ordercode + "=====userId:" + userRealtion.getPreUserId() + "=====rebateAmount:" + fixedRebate + "=====结果:" + resultMap);

                                        profitRecordBusiness.createNewProfitRecord(fixedRebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate, "5", BigDecimal.ONE, "直推奖励分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                                        profitRecordBusiness.createProfitRecordCopy(fixedRebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate1, "5", BigDecimal.ONE, "直推奖励分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());

                                        currate = currate.subtract(new BigDecimal(fixedProfit));
                                    }
                                }
                            }
                        }
                    }

                    BigDecimal preRate = userRealtion.getRate();

                    level = userRealtion.getLevel();
                    if (currate.compareTo(preRate) > 0) {

                        BigDecimal rebate = currate.subtract(preRate).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        if (BigDecimal.ZERO.compareTo(rebate) >= 0) {
                            continue;
                        }

                        if (rebateCount == 0) {
                            firstPhone = userRealtion.getFirstUserPhone();
                            firstUserId = userRealtion.getFirstUserId();
                            firstUserName = userRealtion.getFirstUserName();
                            currate1 = currate;
                        } else {
                            firstPhone = preUserRealtion.getPreUserPhone();
                            firstUserId = preUserRealtion.getPreUserId();
                            firstUserName = preUserRealtion.getPreUserName();
                            currate1 = preUserRealtion.getRate();
                        }

                        totalRebate = totalRebate.subtract(rebate);
                        if (BigDecimal.ZERO.compareTo(totalRebate) > 0) {
                            rebate = totalRebate.add(rebate);
                        }

                        this.updateRebate(rebate + "", userRealtion.getPreUserId() + "", ordercode, null);
                        if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType) || CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
                            profitRecordBusiness.createNewProfitRecord(rebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate, "5", BigDecimal.ONE, "差额费率分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                            profitRecordBusiness.createProfitRecordCopy(rebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate1, "5", BigDecimal.ONE, "差额费率分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        } else {
                            profitRecordBusiness.createNewProfitRecord(rebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate, "0", BigDecimal.ONE, "差额费率分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                            profitRecordBusiness.createProfitRecordCopy(rebate, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate1, "0", BigDecimal.ONE, "差额费率分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        }

                        currate = userRealtion.getRate();
                        preUserRealtion = userRealtion;
                        rebateCount += 1;
                    } else {
                        LOG.info("刷卡人的上级但是无费率差======");

                        if (rebateCount == 0) {
                            firstPhone = userRealtion.getFirstUserPhone();
                            firstUserId = userRealtion.getFirstUserId();
                            firstUserName = userRealtion.getFirstUserName();
                            currate1 = currate;
                        } else {
                            firstPhone = preUserRealtion.getPreUserPhone();
                            firstUserId = preUserRealtion.getPreUserId();
                            firstUserName = preUserRealtion.getPreUserName();
                            currate1 = preUserRealtion.getRate();
                        }

                        if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType) || CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
                            profitRecordBusiness.createProfitRecordCopy(BigDecimal.ZERO, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate1, "5", BigDecimal.ONE, "无费率差分润记录", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        } else {
                            profitRecordBusiness.createProfitRecordCopy(BigDecimal.ZERO, userRealtion.getPreUserPhone(), userRealtion.getRate(), userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, currate1, "0", BigDecimal.ONE, "无费率差分润记录", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        }
                        preUserRealtion = userRealtion;
                        rebateCount += 1;
                    }
                }
            }

            if (jsonArray.size() > 0) {
                String manageId = "";
                List<EqualGradeRebateConfig> equalGradeRebateConfigs = equalGradeRebateConfigBusiness.findByBrandId(brandid + "");

                if (equalGradeRebateConfigs != null && equalGradeRebateConfigs.size() > 0) {
                    /** 根据品牌id获取同级的分润比率 */
                    Map<String, Object> restTemplateDoGet = restTemplateUtil.restTemplateDoGet("user", "/v1.0/user/brand/query/id?brand_id=" + brandid);
                    if (CommonConstants.SUCCESS.equals(restTemplateDoGet.get(CommonConstants.RESP_CODE))) {
                        JSONObject resultJSON = (JSONObject) restTemplateDoGet.get(CommonConstants.RESULT);
                        manageId = resultJSON.getString("manageid");
                    }

                    Map<Integer, BigDecimal> equalGradeRebate = new HashMap<>();
                    for (EqualGradeRebateConfig equalGradeRebateConfig : equalGradeRebateConfigs) {
                        equalGradeRebate.put(equalGradeRebateConfig.getGrade(), equalGradeRebateConfig.getRate());
                    }

                    Integer equalGrade = null;
                    int maxGrade = 0;
                    int beganGrade = 1;
                    for (int i = 0; i < userRealtions.size(); i++) {
                        UserRealtion userRealtion = userRealtions.get(i);
                        Integer preUserGrade = userRealtion.getPreUserGrade();
                        if (i == 0) {
                            maxGrade = preUserGrade.intValue();
                            if (userRealtion.getFirstUserGrade().intValue() != 0) {
                                beganGrade = userRealtion.getFirstUserGrade().intValue();
                            }
                        }

                        if (maxGrade < preUserGrade.intValue()) {
                            maxGrade = preUserGrade.intValue();
                        }
                    }

                    equalGrade = beganGrade;

                    for (int i = beganGrade; i <= maxGrade; i++) {
                        int equalCount = 0;
                        for (int j = 0; j < userRealtions.size() - 1; j++) {
                            UserRealtion userRealtion2 = userRealtions.get(j);
                            if (userRealtion2.getPreUserGrade().compareTo(equalGrade) == 0) {
                                if (equalGradeRebate.containsKey(equalGrade)) {
                                    BigDecimal rate = equalGradeRebate.get(equalGrade);
                                    BigDecimal rebate = equalGradeRebate.get(equalGrade).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                                    if (i == beganGrade) {
                                        if (equalCount == 0) {
                                            if (BigDecimal.ZERO.compareTo(rebate) < 0) {
                                                this.updateRebate(rebate + "", userRealtion2.getPreUserId() + "", ordercode, manageId);
                                                profitRecordBusiness.createNewProfitRecord(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            }
                                            profitRecordBusiness.createProfitRecordCopy(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            equalGrade += 1;
                                        } else if (equalCount % 2 == 0) {
                                            if (BigDecimal.ZERO.compareTo(rebate) < 0) {
                                                this.updateRebate(rebate + "", userRealtion2.getPreUserId() + "", ordercode, manageId);
                                                profitRecordBusiness.createNewProfitRecord(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            }
                                            profitRecordBusiness.createProfitRecordCopy(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            equalGrade += 1;
                                        }
                                    } else {
                                        if (equalCount % 2 != 0) {
                                            if (BigDecimal.ZERO.compareTo(rebate) < 0) {
                                                this.updateRebate(rebate + "", userRealtion2.getPreUserId() + "", ordercode, manageId);
                                                profitRecordBusiness.createNewProfitRecord(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            }
                                            profitRecordBusiness.createProfitRecordCopy(rebate, userRealtion2.getPreUserPhone(), rate, userRealtion2.getPreUserId(), amount, ordercode, userRealtion2.getFirstUserId(), userRealtion2.getFirstUserPhone(), currate2, "3", BigDecimal.ONE, "平级分润", brandid + "", level, userRealtion2.getFirstUserName(), userRealtion2.getPreUserName());
                                            equalGrade += 1;
                                        }
                                    }
                                    equalCount += 1;
                                }
                            }
                        }
                        if (i == equalGrade.intValue()) {
                            equalGrade += 1;
                        }
                    }
                }
            }
        }
    }

    @Autowired
    private EqualGradeRebateConfigBusiness equalGradeRebateConfigBusiness;

    //卡测评的返分润方法
    public void cardEvaRebate(String ordercode, long userid, long channelId, BigDecimal amount, long brandid) {
        long level = 0;

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userid + "");
        requestEntity.add("channelId", channelId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            if (jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    Object jsonObject = jsonArray.get(i);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, jsonObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }

                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("brand_id", brandid + "");
                requestEntity.add("channel_id", channelId + "");
                restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/brandrate/query", requestEntity);
                if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
                    JSONObject resultJSON = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
                    String extraFee = resultJSON.getString("extraFee");

                    LOG.info("订单====" + ordercode + "====总分润为====:" + extraFee);

                    BigDecimal rebate = new BigDecimal(extraFee).divide(new BigDecimal("2"));
                    LOG.info("rebate======" + rebate);

                    int i = 0;
                    for (UserRealtion userRealtion : userRealtions) {
                        if (BigDecimal.ZERO.compareTo(new BigDecimal(extraFee)) >= 0) {
                            break;
                        }
                        i++;
                        String firstPhone = userRealtion.getFirstUserPhone();
                        Long firstUserId = userRealtion.getFirstUserId();
                        String firstUserName = userRealtion.getFirstUserName();
                        level = userRealtion.getLevel();

                        if (i == 1) {
                            LOG.info("直推上级的卡测评分润======");
                            this.updateRebate(rebate + "", userRealtion.getPreUserId() + "", ordercode, null);
                            profitRecordBusiness.createNewProfitRecord(rebate, userRealtion.getPreUserPhone(), BigDecimal.ZERO, userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, BigDecimal.ZERO, "0", BigDecimal.ONE, "信用卡测评直推上级奖励分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        }

                        if (i == userRealtions.size()) {
                            LOG.info("贴牌商的卡测评分润======");
                            BigDecimal rebate1 = new BigDecimal(extraFee).subtract(rebate);
                            LOG.info("rebate1======" + rebate1);
                            this.updateRebate(rebate + "", userRealtion.getPreUserId() + "", ordercode, null);
                            profitRecordBusiness.createNewProfitRecord(rebate1, userRealtion.getPreUserPhone(), BigDecimal.ZERO, userRealtion.getPreUserId(), amount, ordercode, firstUserId, firstPhone, BigDecimal.ZERO, "0", BigDecimal.ONE, "信用卡测评剩余奖励分润", brandid + "", level, firstUserName, userRealtion.getPreUserName());
                        }

                    }

                }

            }


        }
    }


    private void updateRebate(String rebateAmount, String userId, String orderCode, String manageId) {
        MultiValueMap<String, String> requestEntity = null;
        if (null == manageId) {
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("rebate_amount", rebateAmount);
            requestEntity.add("user_id", userId);
            requestEntity.add("order_code", orderCode);
            Map<String, Object> resultMap = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/rebate/update", requestEntity);
            LOG.info("=====orderCode:" + orderCode + "=====userId:" + userId + "=====rebateAmount:" + rebateAmount + "=====结果:" + resultMap);
        } else {
            if (!"".equals(manageId) && null != manageId) {
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("user_id", manageId);
                Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/account/query/userId", requestEntity);
                if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
                    JSONObject userAccountJSON = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
                    String balance = userAccountJSON.getString("balance");
                    String rebateBalance = userAccountJSON.getString("rebateBalance");
                    if (new BigDecimal(rebateAmount).compareTo(new BigDecimal(balance)) <= 0 || new BigDecimal(rebateAmount).compareTo(new BigDecimal(rebateBalance)) <= 0) {

                        // 增加用户分润
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("rebate_amount", rebateAmount);
                        requestEntity.add("user_id", userId);
                        requestEntity.add("order_code", orderCode);
                        restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/rebate/update", requestEntity);
                        // 减少贴牌商余额
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("amount", rebateAmount);
                        requestEntity.add("user_id", manageId);
                        requestEntity.add("addorsub", "1");
                        requestEntity.add("order_code", orderCode);
                        restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/account/update", requestEntity);

                    }
                }
            }
        }
    }

    private void sendPushMessage(String userId, BigDecimal realAmount) {
        /**
         * 推送消息 /v1.0/user/jpush/tset
         */
        String alert = "余额充值";
        String content = "亲爱的会员，" + realAmount.setScale(2, ROUND_DOWN) + "元订单已下单成功，等待结算！";
        String btype = "balanceadd";
        String btypeval = "";
        /** 获取身份证实名信息 */
        // URI uri = util.getServiceUrl("user", "error url request!");
        // String url = uri.toString() + "/v1.0/user/jpush/tset";
        String url = "http://user/v1.0/user/jpush/tset";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        requestEntity.add("alert", alert + "");
        requestEntity.add("content", content + "");
        requestEntity.add("btype", btype + "");
        requestEntity.add("btypeval", btypeval + "");
        // RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.postForObject(url, requestEntity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            LOG.error("", e);
        }
    }

    public int getIntervalDays(Date fDate, Date oDate) {

        /*
         * if (null == fDate || null == oDate) {
         *
         * return -1;
         *
         * }
         *
         * long intervalMilli = oDate.getTime() - fDate.getTime();
         *
         * return (int) (intervalMilli / (24 * 60 * 60 * 1000));
         */

        Calendar aCalendar = Calendar.getInstance();

        aCalendar.setTime(fDate);

        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);

        aCalendar.setTime(oDate);
        // aCalendar.add(Calendar.HOUR_OF_DAY, -17);
        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

        return day2 - day1;

    }

    /**
     * 查询提现总金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/userid")
    public @ResponseBody
    Object findAllChannelByuserid(HttpServletRequest request,
                                  @RequestParam(value = "user_id") long userid, @RequestParam(value = "type") String type,
                                  @RequestParam(value = "status", defaultValue = "1", required = false) String status,
                                  @RequestParam(value = "auto_clearing", defaultValue = "0", required = false) String autoClearing) {
        String[] str1 = type.split(",");
        String[] str2 = status.split(",");
        BigDecimal simrealAmount = paymentOrderBusiness.findsumPaymentOrder(userid, str1, str2, autoClearing);
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, simrealAmount == null ? "0.00" : simrealAmount.setScale(2, ROUND_DOWN));
        return map;
    }

    // 批量查询指定type的总金额
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/byuserids")
    public @ResponseBody
    Object findAllChannelSumByUserIds(HttpServletRequest request,
                                      @RequestParam(value = "userIds") String[] suserIds, @RequestParam(value = "type") String type,
                                      @RequestParam(value = "status", required = false, defaultValue = "1") String status,
                                      @RequestParam(value = "autoClearing", required = false, defaultValue = "0") String autoClearing) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> sumMap = new HashMap<String, Object>();
        long[] userIds = new long[suserIds.length];
        if (suserIds != null && suserIds.length > 0) {
            for (int i = 0; i < suserIds.length; i++) {
                userIds[i] = Long.valueOf(suserIds[i]);
            }
            List<Object[]> models = paymentOrderBusiness.findSumByUserIds(userIds, type, status, autoClearing);
            if (models != null) {
                for (int i = 0; i < models.size(); i++) {
                    sumMap.put(models.get(i)[0].toString(), models.get(i)[1]);
                }
                for (String userid : suserIds) {
                    if (!sumMap.containsKey(userid))
                        sumMap.put(userid, "0.00");
                }
            } else {
                for (String userid : suserIds) {
                    sumMap.put(userid, "0.00");
                }
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, sumMap);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询失败");
        }
        return map;
    }

    /**
     * 查询提现总金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/sum/userid")
    public @ResponseBody
    Object findAllChannelByuseridsum(HttpServletRequest request,
                                     @RequestParam(value = "user_id") long userid,
                                     @RequestParam(value = "type", defaultValue = "", required = false) String type,
                                     @RequestParam(value = "status", defaultValue = "1,4", required = false) String status,
                                     @RequestParam(value = "end_time", required = false) String startTime,
                                     @RequestParam(value = "auto_clearing", defaultValue = "0", required = false) String autoClearing) {

        Date startTimeDate = null;
        if (startTime != null && !startTime.equalsIgnoreCase("")) {
            startTimeDate = DateUtil.getDateFromStr(startTime);
        } else {
            startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            startTimeDate = DateUtil.getDateFromStr(startTime);
        }
        Date endTimeDate = null;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTimeDate);
        calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
        endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
        /** 总入金 **/
        BigDecimal RechargeSum = null;
        /** 总提现 **/
        BigDecimal withdrawSum = null;
        /** 交易笔数 **/
        int transactionCount = 0;
        {
            type = "0";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            RechargeSum = paymentOrderBusiness.findsumPaymentOrder(userid, str1, str2, autoClearing, startTimeDate,
                    endTimeDate);
        }
        {
            type = "2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            withdrawSum = paymentOrderBusiness.findsumPaymentOrder(userid, str1, str2, autoClearing, startTimeDate,
                    endTimeDate);
        }
        {
            type = "0,2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            transactionCount = paymentOrderBusiness.findsumPaymentOrderCount(userid, str1, str2, autoClearing,
                    startTimeDate, endTimeDate);
        }

        Map sumall = new HashMap();
        sumall.put("RechargeSum", RechargeSum == null ? "0.00" : RechargeSum.setScale(2, ROUND_DOWN));
        sumall.put("withdrawSum", withdrawSum == null ? "0.00" : withdrawSum.setScale(2, ROUND_DOWN));
        sumall.put("transactionCount", transactionCount);
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, sumall);
        return map;
    }

    /**
     * 查询提现总金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/sum/userid/query")
    public @ResponseBody
    Object findAllChannelByuseridquerysum(HttpServletRequest request,
                                          @RequestParam(value = "user_id") long userid,
                                          @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                          @RequestParam(value = "status", defaultValue = "1,4", required = false) String status,
                                          @RequestParam(value = "auto_clearing", defaultValue = "0", required = false) String autoClearing) {

        int transactionCount = 0;
        String[] str1 = type.split(",");
        String[] str2 = status.split(",");
        Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
        Date endTimeDate = new Date();
        transactionCount = paymentOrderBusiness.findsumPaymentOrderCount(userid, str1, str2, autoClearing,
                startTimeDate, endTimeDate);

        // Map sumall = new HashMap();
        // sumall.put("transactionCount",transactionCount);
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, transactionCount);
        return map;
    }


//      public static void main(String[] args) throws Exception{
//
//
//      String date1 = "2016-07-09 11:23:23"; String date2 = "2016-07-10 17:23:23";
//
//
//      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//      Date newDate1 = sdf.parse(date1); Date newDate2 = sdf.parse(date2);
//
//      Calendar aCalendar = Calendar.getInstance();
//
//      aCalendar.setTime(newDate1);
//
//      int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
//
//      aCalendar.setTime(newDate2); aCalendar.add(Calendar.HOUR_OF_DAY, -17); int
//      day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
//      System.out.println(day2 - day1);
//
//      }


    /**
     * 查询提现brand总金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/sum/brand")
    public @ResponseBody
    Object findAllChannelByBrandsum(HttpServletRequest request,
                                    @RequestParam(value = "brand_id") long brandId,
                                    @RequestParam(value = "type", defaultValue = "1", required = false) String type,
                                    @RequestParam(value = "status", defaultValue = "1,4", required = false) String status,
                                    @RequestParam(value = "end_time", required = false) String startTime,
                                    @RequestParam(value = "auto_clearing", defaultValue = "0,1", required = false) String autoClearing) {
        Map daysum = new HashMap();
        Map monthsum = new HashMap();
        Map allsum = new HashMap();
        Map body = new HashMap();
        {
            Date startTimeDate = null;
            if (startTime != null && !startTime.equalsIgnoreCase("")) {
                startTimeDate = DateUtil.getDateFromStr(startTime);
            } else {
                startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                startTimeDate = DateUtil.getDateFromStr(startTime);
            }
            Date endTimeDate = null;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTimeDate);
            calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
            endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            body.put("daysum", getBrandsum(brandId, startTimeDate, endTimeDate, type, status, autoClearing));
        }
        {
            Date startTimeDate = null;
            if (startTime != null && !startTime.equalsIgnoreCase("")) {
                startTime = new SimpleDateFormat("yyyy-MM").format(DateUtil.getDateFromStr(startTime));
                startTimeDate = getYYMMDateFromStr(startTime);
            } else {
                startTime = new SimpleDateFormat("yyyy-MM").format(new Date());
                startTimeDate = getYYMMDateFromStr(startTime);
            }
            Date endTimeDate = null;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTimeDate);
            calendar.add(calendar.MONTH, 1);// 把日期往后增加一天.整数往后推,负数往前移动
            endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            body.put("monthsum", getBrandsum(brandId, startTimeDate, endTimeDate, type, status, autoClearing));
        }

        {
            Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
            Date endTimeDate = new Date();
            body.put("allsum", getBrandsum(brandId, startTimeDate, endTimeDate, type, "1", autoClearing));
        }
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, body);
        return map;
    }

    /**
     * 获取红包或其他类型今日交易笔数
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/desccode/query/userid")
    public @ResponseBody
    Object findAllChannelByDescCode(HttpServletRequest request,
                                    @RequestParam(value = "user_id") long userid,
                                    @RequestParam(value = "type", defaultValue = "0", required = false) String type,
                                    @RequestParam(value = "status", defaultValue = "1", required = false) String status,
                                    @RequestParam(value = "desc_code", defaultValue = "RedPayment", required = false) String desccode) {
        String[] str1 = type.split(",");
        String[] str2 = status.split(",");
        Date startTimeDate = null;
        String startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        startTimeDate = DateUtil.getDateFromStr(startTime);
        Date endTimeDate = null;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTimeDate);
        calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
        endTimeDate = calendar.getTime(); //
        List<PaymentOrder> simrealAmount = paymentOrderBusiness.findsumPaymentOrderByDescCode(userid, str1, str2,
                desccode, startTimeDate, endTimeDate);
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        if (simrealAmount != null && simrealAmount.size() > 0) {
            map.put(CommonConstants.RESULT, simrealAmount);
        }

        return map;
    }

    /**
     * 查询提现Platform总金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/sum/platform")
    public @ResponseBody
    Object findAllChannelByPlatformsum(HttpServletRequest request,
                                       @RequestParam(value = "type", defaultValue = "", required = false) String type,
                                       @RequestParam(value = "status", defaultValue = "1,4", required = false) String status,
                                       @RequestParam(value = "end_time", required = false) String startTime,
                                       @RequestParam(value = "auto_clearing", defaultValue = "0,1", required = false) String autoClearing) {
        Map daysum = new HashMap();
        Map monthsum = new HashMap();
        Map allsum = new HashMap();
        Map body = new HashMap();
        {
            Date startTimeDate = null;
            if (startTime != null && !startTime.equalsIgnoreCase("")) {
                startTimeDate = DateUtil.getDateFromStr(startTime);
            } else {
                startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                startTimeDate = DateUtil.getDateFromStr(startTime);
            }
            Date endTimeDate = null;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTimeDate);
            calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
            endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            body.put("daysum", getPlatformsum(startTimeDate, endTimeDate, type, status, autoClearing));
        }
        {
            Date startTimeDate = null;
            if (startTime != null && !startTime.equalsIgnoreCase("")) {
                startTime = new SimpleDateFormat("yyyy-MM").format(DateUtil.getDateFromStr(startTime));
                startTimeDate = getYYMMDateFromStr(startTime);
            } else {
                startTime = new SimpleDateFormat("yyyy-MM").format(new Date());
                startTimeDate = getYYMMDateFromStr(startTime);
            }
            Date endTimeDate = null;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTimeDate);
            calendar.add(calendar.MONTH, 1);// 把日期往后增加一天.整数往后推,负数往前移动
            endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果

            body.put("monthsum", getPlatformsum(startTimeDate, endTimeDate, type, status, autoClearing));
        }

        {
            Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
            Date endTimeDate = new Date();
            body.put("allsum", getPlatformsum(startTimeDate, endTimeDate, type, "1", autoClearing));
        }
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, body);
        return map;
    }

    // 查询用公众号支付商铺扫码的订单
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/queryPaymentOrder")
    public @ResponseBody
    Object findPaymentOrder(HttpServletRequest request,
                            @RequestParam(value = "brand_id", defaultValue = "", required = false) String brandid,
                            @RequestParam(value = "user_id", defaultValue = "", required = false) String userid,
                            @RequestParam(value = "order_status", defaultValue = "0,1,2,3,4", required = false) String status,
                            @RequestParam(value = "order_code", defaultValue = "", required = false) String ordercode,
                            @RequestParam(value = "third_order_code", defaultValue = "", required = false) String thirdOrdercode,
                            @RequestParam(value = "channel_tag", defaultValue = "3", required = false) String channel_no,
                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                            @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                            @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                            @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        String[] statu = status.split(",");
        String[] channel = channel_no.split("");

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        Page<PaymentOrder> querypaymentOrder = null;

        if (channel_no != null && !channel_no.equals("")) {
            URI uri = util.getServiceUrl("user", "error url request!");
            String url = uri.toString() + "/v1.0/user/channel/channelno";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_no", channel_no);

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("result========" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            if (jsonObject.containsKey("result") && jsonObject.getString("result").length() > 0) {
                channel = jsonObject.getString("result").split(",");
            }

            querypaymentOrder = paymentOrderBusiness.queryPaymentOrder(userid, brandid, ordercode, statu,
                    thirdOrdercode, channel, pageable);
        }

        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, querypaymentOrder);

        return map;
    }

    private Map getPlatformsum(Date startTimeDate, Date endTimeDate, String type, String status, String autoClearing) {
        Map Platformsum = new HashMap();
        /** 总入金 **/
        BigDecimal RechargeSum = null;
        /** 总购买 **/
        BigDecimal buyGoodsSum = null;
        /** 总提现 **/
        BigDecimal withdrawSum = null;
        /** 总交易笔数 **/
        int transactionCount = 0;
        // 提现笔数
        int withdrawCount = 0;
        // 充值笔数
        int rechargeCount = 0;
        // 购买商品笔数
        int buyGoodsCount = 0;

        // 购买商品的金额
        {
            type = "1";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            buyGoodsSum = paymentOrderBusiness.findsumPaymentOrderPlatform(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 充值的金额
        {
            type = "0";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            RechargeSum = paymentOrderBusiness.findsumPaymentOrderPlatform(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 提现的金额
        {
            type = "2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            withdrawSum = paymentOrderBusiness.findsumPaymentOrderPlatform(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 总交易笔数
        {
            type = "0,1,2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            transactionCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 充值的交易笔数
        {
            type = "0";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            rechargeCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 购买商品的交易笔数
        {
            type = "1";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            buyGoodsCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 提现的交易笔数
        {
            type = "2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            withdrawCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 充值的金额
        Platformsum.put("RechargeSum", RechargeSum == null ? "0.00" : RechargeSum.setScale(2, ROUND_DOWN));
        // 购买商品的金额
        Platformsum.put("buyGoodsSum", buyGoodsSum == null ? "0.00" : buyGoodsSum.setScale(2, ROUND_DOWN));
        /** 总提现 **/
        Platformsum.put("withdrawSum", withdrawSum == null ? "0.00" : withdrawSum.setScale(2, ROUND_DOWN));
        /** 总交易笔数 **/
        Platformsum.put("transactionCount", transactionCount);
        Platformsum.put("rechargeCount", rechargeCount);
        Platformsum.put("buyGoodsCount", buyGoodsCount);
        Platformsum.put("withdrawCount", withdrawCount);

        return Platformsum;
    }

    private Map getBrandsum(long brandId, Date startTimeDate, Date endTimeDate, String type, String status,
                            String autoClearing) {
        Map Platformsum = new HashMap();
        /** 总入金 **/
        BigDecimal RechargeSum = null;
        /** 总购买 **/
        BigDecimal buyGoodsSum = null;
        /** 总提现 **/
        BigDecimal withdrawSum = null;
        /** 交易笔数 **/
        int transactionCount = 0;
        // 提现笔数
        int withdrawCount = 0;
        // 充值笔数
        int rechargeCount = 0;
        // 购买商品笔数
        int buyGoodsCount = 0;

        {
            type = "0";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            RechargeSum = paymentOrderBusiness.findsumPaymentOrderBrand(brandId, str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        {
            type = "1";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            buyGoodsSum = paymentOrderBusiness.findsumPaymentOrderBrand(brandId, str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        {
            type = "2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            withdrawSum = paymentOrderBusiness.findsumPaymentOrderBrand(brandId, str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        {
            type = "0,1,2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            transactionCount = paymentOrderBusiness.findsumPaymentOrderBrandCount(brandId, str1, str2, clear,
                    startTimeDate, endTimeDate);
        }
        // 充值的交易笔数
        {
            type = "0";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            rechargeCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 购买商品的交易笔数
        {
            type = "1";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            buyGoodsCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }
        // 提现的交易笔数
        {
            type = "2";
            String[] str1 = type.split(",");
            String[] str2 = status.split(",");
            String[] clear = autoClearing.split(",");
            withdrawCount = paymentOrderBusiness.findsumPaymentOrderPlatformCount(str1, str2, clear, startTimeDate,
                    endTimeDate);
        }

        Platformsum.put("RechargeSum", RechargeSum == null ? "0.00" : RechargeSum.setScale(2, ROUND_DOWN));
        Platformsum.put("withdrawSum", withdrawSum == null ? "0.00" : withdrawSum.setScale(2, ROUND_DOWN));
        Platformsum.put("buyGoodsSum", buyGoodsSum == null ? "0.00" : buyGoodsSum.setScale(2, ROUND_DOWN));
        Platformsum.put("transactionCount", transactionCount);
        Platformsum.put("rechargeCount", rechargeCount);
        Platformsum.put("buyGoodsCount", buyGoodsCount);
        Platformsum.put("withdrawCount", withdrawCount);

        return Platformsum;
    }


    // 盛迪嘉通道——修改订单号
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/updateOrderCode")
    public @ResponseBody
    Object updateOrderCode(HttpServletRequest request,
                           @RequestParam(value = "oldOrderCode") String orderCode,
                           @RequestParam(value = "newOrderCode") String orderNo) {

        String substring = orderNo.substring(0, 10);

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("order_code", orderCode);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        JSONObject resultObj = jsonObject.getJSONObject("result");
        String userid = resultObj.getString("userid");

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/sdj/querysdjid";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid);
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String sdjUserId = resultObj.getString("sdjUserId");

        PaymentOrder paymentOrder = null;

        if (substring.equals(sdjUserId)) {
            paymentOrder = paymentOrderBusiness.updatePaymentOrder(orderCode, orderNo);
        }

        Map map = new HashMap();
        if (paymentOrder != null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "修改成功");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "修改失败");
            return map;
        }

    }

    /*
     * public static void main(String[] args) throws Exception{ BigDecimal amount =
     * new BigDecimal("600"); BigDecimal remainingAmount = amount;
     *
     * Map map = new HashMap();
     *
     * map.put("1", "0.2"); map.put("2", "0.2"); map.put("3", "0.3");
     *
     * for(int i=1; i < 5; i++){ String rebate = (String)map.get(i+""); if(rebate !=
     * null){
     *
     * remainingAmount = remainingAmount.subtract(new
     * BigDecimal(rebate).multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP)); }
     * }
     *
     * System.out.println(remainingAmount);
     *
     *
     *
     * }
     */
    public static Date getYYMMDateFromStr(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date result = null;
        try {
            result = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            LOG.error("", e);
        }
        return result;
    }

    /*
     * @RequestMapping(method=RequestMethod.POST,value=
     * "/v1.0/transactionclear/payment/getExcel") public Object
     * getExcel(HttpServletRequest request){ HSSFWorkbook workbook = new
     * HSSFWorkbook(); HSSFSheet sheet = workbook.createSheet("统计表");
     * createTitle(workbook, sheet); List<PaymentOrder> entities =
     * paymentOrderBusiness.queryPaymentOrderAll(startTime, endTime, phone,
     * ordercode, ordertype, orderstatus, brandid, pageAble)
     *
     * //设置日期格式 HSSFCellStyle style=workbook.createCellStyle();
     * style.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
     *
     * //新增数据行，并且设置单元格数据 int rowNum = 1; for (StatisticsInfo
     * statisticsInfo:entities) {
     *
     * HSSFRow row = sheet.createRow(rowNum);
     * row.createCell(0).setCellValue(statisticsInfo.getId());
     * row.createCell(1).setCellValue(statisticsInfo.getMoney().toString());
     * row.createCell(2).setCellValue(statisticsInfo.getDescription()); HSSFCell
     * cell = row.createCell(3); cell.setCellValue(statisticsInfo.getCurrentdate());
     * cell.setCellStyle(style); rowNum++; }
     *
     * //拼装blobName String fileName = "测试数据统计表.xlsx"; SimpleDateFormat dateFormat =
     * new SimpleDateFormat("yyyyMMdd"); String dateTime = dateFormat.format(new
     * Date()); String blobName = dateTime + "/" +
     * UUID.randomUUID().toString().replaceAll("-", "") + "/" + fileName;
     *
     * //获取或创建container CloudBlobContainer blobContainer =
     * BlobHelper.getBlobContainer("temp", storageConfig); //设置文件类型，并且上传到azure blob
     * try { CloudBlockBlob blob = blobContainer.getBlockBlobReference(blobName);
     * ByteArrayOutputStream out = new ByteArrayOutputStream(); workbook.write(out);
     * ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
     *
     * blob.upload(in, out.toByteArray().length); Map map = new HashMap();
     * map.put("fileName", fileName); map.put("excelUrl", blob.getUri().toString());
     *
     * ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(),
     * ResultStatusCode.OK.getErrmsg(), map); return resultMsg;
     *
     * } catch (Exception e) { ResultMsg resultMsg = new
     * ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),
     * ResultStatusCode.SYSTEM_ERR.getErrmsg(), null); return resultMsg; } }
     *
     *
     * private void createTitle(HSSFWorkbook workbook, HSSFSheet sheet) { HSSFRow
     * row = sheet.createRow(0);
     * //设置列宽，setColumnWidth的第二个参数要乘以256，这个参数的单位是1/256个字符宽度 sheet.setColumnWidth(2,
     * 12*256); sheet.setColumnWidth(3, 17*256);
     *
     * //设置为居中加粗 HSSFCellStyle style = workbook.createCellStyle(); HSSFFont font =
     * workbook.createFont(); font.setBold(true);
     * style.setAlignment(HSSFCellStyle.ALIGN_CENTER); style.setFont(font);
     *
     * HSSFCell cell; cell = row.createCell(0); cell.setCellValue("序号");
     * cell.setCellStyle(style);
     *
     * cell = row.createCell(1); cell.setCellValue("金额"); cell.setCellStyle(style);
     *
     * cell = row.createCell(2); cell.setCellValue("描述"); cell.setCellStyle(style);
     *
     * cell = row.createCell(3); cell.setCellValue("日期"); cell.setCellStyle(style);
     * }
     */

    public String getNumberOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        StringBuffer sb = new StringBuffer(sdf.format(now));
        Random random = new Random();
        for (int i = 0; i < 18; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String genMerOrderId(String msgId) {
        String date = DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS");
        String rand = RandomStringUtils.randomNumeric(7);
        return msgId + date + rand;
    }

    public String getLD1NumberOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        StringBuffer sb = new StringBuffer(sdf.format(now));
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public String getWFNumberOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        StringBuffer sb = new StringBuffer(sdf.format(now));
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String substring = uuid.substring(0, 4);
        return sb.append(substring).toString();
    }

    public static String getOrderId() {
        SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHH");
        String date = sd.format(new Date());
        int uuid = ((int) ((Math.random() * 9 + 1) * 100000));
        String orderId = date + String.valueOf(uuid);
        LOG.info("14位时间戳流水：" + orderId);

        return orderId;

    }

    // 获取待结算订单和成功订单
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/queryorderfailed")
    @ResponseBody
    public Object queryOrderFailed(HttpServletRequest request, @RequestParam(value = "userid") long userid) {
        Map map = new HashMap();
        String[] status = {"0", "1"};
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, paymentOrderBusiness.findOrderByUseridAndStatus(userid, status));
        return map;
    }

    // 修改环球B通道的还款订单金额
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/update/repayment/order/amount")
    @ResponseBody
    public Object updateRepaymentOrderAmountByOrderCode(HttpServletRequest request,
                                                        @RequestParam(value = "orderCode") String orderCode,
                                                        @RequestParam(value = "amount") String amount
    ) {
        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
        if (paymentOrder == null) {
            return ResultWrap.init(CommonConstants.FALIED, "订单不存在!");
        }

        Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(amount, 2, BigDecimal.ROUND_HALF_UP);
        if (!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))) {
            return verifyMoney;
        }

        if ("HQB_QUICK".equalsIgnoreCase(paymentOrder.getChannelTag())) {
            paymentOrder.setAmount((BigDecimal) verifyMoney.get(CommonConstants.RESULT));
            paymentOrder.setRealAmount((BigDecimal) verifyMoney.get(CommonConstants.RESULT));
            paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "修改成功", paymentOrder);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/add/credit/card/manager/order")
    public @ResponseBody
    Object createPaymentOrderToCreditCardManager(HttpServletRequest request,
                                                 @RequestParam(value = "userId") String userIdStr,
                                                 @RequestParam(value = "rate") String rateStr,
                                                 @RequestParam(value = "type") String type,
                                                 @RequestParam(value = "amount") String amountStr,
                                                 @RequestParam(value = "realAmount") String realAmountStr,
                                                 @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                                 @RequestParam(value = "channelTag") String channelTag,
                                                 @RequestParam(value = "orderCode") String orderCode,
                                                 @RequestParam(value = "serviceCharge") String serviceCharge,
                                                 @RequestParam(value = "description", required = false, defaultValue = "") String description,
                                                 @RequestParam(value = "remark", required = false, defaultValue = "") String remark) {
        userIdStr = userIdStr.trim();
        rateStr = rateStr.trim();
        amountStr = amountStr.trim();
        realAmountStr = realAmountStr.trim();
        serviceCharge = serviceCharge.trim();
        orderCode = orderCode.trim();
        type = type.trim();
        channelTag = channelTag.trim();
        Map map = new HashMap();
        long userId = Long.valueOf(userIdStr);
        BigDecimal rate = new BigDecimal(rateStr);

        RestTemplate restTemplate1 = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/find/by/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userIdStr);
        String resultString;
        JSONObject resultJSONObject;
        try {
            resultString = restTemplate1.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单失败,服务器正忙,请稍后重试!");
            return map;
        }
        LOG.info("RESULT============/v1.0/user/find/by/userid:" + resultString);
        if (!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单失败,无该用户数据!");
            return map;
        }

        resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        long brandId = resultJSONObject.getLong("brandId");
        String phone = resultJSONObject.getString("phone");
        String brandName = resultJSONObject.getString("brandname");
        String fullname = resultJSONObject.getString("fullname");

        PaymentOrder paymentOrder = new PaymentOrder();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/channel/query";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("channel_tag", channelTag);
        resultString = restTemplate1.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/user/channel/query:" + resultString);
        resultJSONObject = JSONObject.fromObject(resultString);
        resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        long channelId = resultJSONObject.getLong("id");
        String channelName = resultJSONObject.getString("name");
        String channelTag2 = resultJSONObject.getString("channelTag");
        String channelType = resultJSONObject.getString("channelType");
        String autoClearing = resultJSONObject.getString("autoclearing");
        String costRateStr = resultJSONObject.getString("costRate");
        BigDecimal costRate = new BigDecimal(costRateStr);
        if (!(BigDecimal.ZERO.compareTo(rate) == 0)) {
            if (costRate.compareTo(rate) > 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "下单失败,您的费率有异常,请联系管理员!");
                return map;
            }
        }
        paymentOrder.setChannelid(channelId);
        paymentOrder.setChannelname(channelName);
        paymentOrder.setChannelTag(channelTag2);

        url = "http://user/v1.0/user/bank/default/cardnoand/type";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
        multiValueMap.add("cardno", creditCardNumber);
        multiValueMap.add("type", "0");
        multiValueMap.add("userId", userId + "");
        String result = restTemplate.postForObject(url, multiValueMap, String.class);
        LOG.info("接口/v1.0/user/bank/default/cardnoand/type====RESULT=========" + result);

        JSONObject jsonObject = JSONObject.fromObject(result);
        String bankName = null;
        try {
            JSONObject resultObj = jsonObject.getJSONObject("result");
            bankName = resultObj.getString("bankName");
        } catch (Exception e1) {
            e1.printStackTrace();
            return ResultWrap.init(CommonConstants.FALIED, "获取银行卡信息失败,可能已被删除!");
        }

        paymentOrder.setBankName(bankName);
        paymentOrder.setBankcard(creditCardNumber);
        paymentOrder.setAutoClearing(autoClearing);
        paymentOrder.setRate(rate);
        paymentOrder.setChannelType(channelType);

        LinkedMultiValueMap<String, String> requestEntity2 = new LinkedMultiValueMap<String, String>();
        requestEntity2.add("channel_id", channelId + "");
        requestEntity2.add("user_id", userId + "");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/channel/rate/query/userid", requestEntity2);
        if (!CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            return restTemplateDoPost;
        }
        resultJSONObject = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
        LOG.info("==========/v1.0/user/channel/rate/query/userid:" + resultJSONObject);
        String withdrawFee = resultJSONObject.getString("withdrawFee");
        String extraFee = resultJSONObject.getString("extraFee");

        BigDecimal newwithdrawFee = withdrawFee == null || withdrawFee.equalsIgnoreCase("") || withdrawFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(withdrawFee);
        BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("") || extraFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(extraFee);

        paymentOrder.setExtraFee(newwithdrawFee.add(newextraFee));
        paymentOrder.setCostfee(newwithdrawFee);

        paymentOrder.setAmount(new BigDecimal(amountStr));
        paymentOrder.setRealAmount(new BigDecimal(realAmountStr));
        paymentOrder.setUserid(userId);
        paymentOrder.setBrandid(brandId);
        paymentOrder.setBrandname(brandName);
        paymentOrder.setRemark(remark);
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(description);
        paymentOrder.setOrdercode(orderCode);
        paymentOrder.setUserName(fullname);
        paymentOrder.setPhone(phone);
        paymentOrder.setType(type);
        paymentOrder.setStatus(CommonConstants.ORDER_READY);

        LOG.info("RESULT================" + paymentOrder.getOrdercode() + "************************************");
        try {
            paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单异常");
            return map;
        }
        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, paymentOrder);
        return map;
    }

    // 订单添加失败信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/update/remark")
    public @ResponseBody
    Object addErrorMsg(HttpServletRequest request,
                       @RequestParam(value = "ordercode") String ordercode,
                       @RequestParam(value = "remark") String remark,
                       @RequestParam(value = "createTime", required = false, defaultValue = "-1") String createTime
    ) {
        Map map = new HashMap();
        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
        if (paymentOrder == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "订单查询失败");
        } else {
            paymentOrder.setRemark(remark);
            if (!"-1".equals(createTime)) {
                paymentOrder.setUpdateTime(new Date());
                ;
            }

            paymentOrderBusiness.mergePaymentOrder(paymentOrder);

            //paymentOrderBusiness.addErrorByOrderCode(ordercode, remark);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
        }
        return map;
    }


    //根据userId、时间、channelTag、orderStatus查询交易金额
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/querytransaction/sumamount")
    public @ResponseBody
    Object getEveryDayMaxLimitAmount(HttpServletRequest request,
                                     @RequestParam(value = "userId") String userId,
                                     @RequestParam(value = "channelTag") String channelTag,
                                     @RequestParam(value = "orderStatus") String orderStatus
    ) {


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();


        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = new Date();

        LOG.info("date1====" + sdf1.format(zero));
        LOG.info("date2====" + sdf1.format(date));

        BigDecimal everyDayMaxLimit;
        try {
            everyDayMaxLimit = paymentOrderBusiness.getEveryDayMaxLimit(sdf1.format(zero), sdf1.format(date), channelTag, orderStatus, Long.valueOf(userId));
            LOG.info("查询结果为======" + everyDayMaxLimit);
            if (everyDayMaxLimit == null) {
                everyDayMaxLimit = BigDecimal.ZERO;
            }
            LOG.info("重新赋值的结果为======" + everyDayMaxLimit);
        } catch (NumberFormatException e) {
            e.printStackTrace();

            return ResultWrap.init(CommonConstants.FALIED, "查询失败");
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", everyDayMaxLimit);

    }

    // 通道成功率
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/finduccess")
    public @ResponseBody
    Object findSuccessRate(HttpServletRequest request,
                           /**
                            * 贴牌筛选
                            */
                           @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandId, /***
                            * 时间筛选
                            **/
                           @RequestParam(value = "starttime", required = false) String startTime,
                           @RequestParam(value = "endtime", required = false) String endTime) {
        Date StartTimeDate = null;
        if (startTime != null && startTime.trim().length() > 0) {
            StartTimeDate = DateUtil.getDateFromStr(startTime);
        }
        Date endTimeDate = null;
        if (endTime != null && endTime.trim().length() > 0) {
            endTimeDate = DateUtil.getDateFromStr(endTime);
        }

        if ((startTime == null || startTime.trim().length() == 0)
                && (endTime == null || endTime.trim().length() == 0)) {
            StartTimeDate = DateUtil.getDateFromStr(DateUtil.getDateFromStr(new Date()));
        }

        List<PaymentOrderNumber> SuccessRate = paymentOrderBusiness.findOrderSuccessRate(brandId, StartTimeDate,
                endTimeDate);

        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, SuccessRate);

        return map;
    }

//	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/deletepayment/byordercode")
//	public @ResponseBody Object deletePaymentByOrdercode(HttpServletRequest request,
//			@RequestParam(value = "ordercode") String ordercode) {
//
//		Map map = new HashMap();
//
//		paymentOrderBusiness.deletePaymentOrderByOrderCode(ordercode);
//
//		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//		map.put(CommonConstants.RESP_MESSAGE, "成功");
//		return map;
//
//	}

    /*
     * @RequestMapping(method = RequestMethod.POST, value =
     * "/v1.0/transactionclear/payment/deletepayment/bylist") public @ResponseBody
     * Object deletePaymentByList(HttpServletRequest request,
     *
     * @RequestParam(value = "startTime", required = false) String startTime,
     *
     * @RequestParam(value = "endTime", required = false) String endTime,
     *
     * @RequestParam(value = "channelTag", required = false) String[] channelTag,
     *
     * @RequestParam(value = "status", required = false) String status,
     *
     * @RequestParam(value = "remark", required = false) String remark) {
     *
     * Map map = new HashMap();
     *
     * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); Date date = new
     * Date(); Calendar calendar = Calendar.getInstance(); calendar.setTime(date);
     * calendar.add(Calendar.DAY_OF_MONTH, +1); date = calendar.getTime();
     *
     * Date date2 = new Date();
     *
     * LOG.info("====" + sdf.format(date)); LOG.info("====" + sdf.format(date2));
     *
     * String[] strings = { "LD_QUICK", "LF_QUICK" };
     *
     * try { List<PaymentOrder> list =
     * paymentOrderBusiness.findOrderByTimeAndChannelTagAndStatusAndRemark(sdf.
     * format(date2), sdf.format(date), strings, status);
     *
     * LOG.info("=============" + list.size());
     *
     * // paymentOrderBusiness.deletePaymentOrder(list); } } catch (Exception e) {
     * LOG.info("操作失败=====" + e); map.put(CommonConstants.RESP_CODE,
     * CommonConstants.FALIED); map.put(CommonConstants.RESP_MESSAGE, "失败"); return
     * map; }
     *
     * map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
     * map.put(CommonConstants.RESP_MESSAGE, "成功"); return map;
     *
     * }
     */


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/earning/report")
    public @ResponseBody
    Object earningReport(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                         @RequestParam(value = "startTime", required = false) String startTime,
                         @RequestParam(value = "endTime", required = false) String endTime) {

        Map<String, Object> map = new HashMap<String, Object>();

        boolean hasKey = false;
        String key = "/v1.0/transactionclear/earning/report:phone=" + phone + ";startTime=" + startTime + ";endTime="
                + endTime;
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            return operations.get(key);
        }

        Map<String, Object> map2 = null;
        try {
            String url = "http://user/v1.0/user/realtion/find/user/member/byphone";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", phone);
            requestEntity.add("start_time", startTime);
            requestEntity.add("end_time", endTime);
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/user/realtion/find/user/member/byphone======result==========" + result);
            JSONObject fromObject = JSONObject.fromObject(result);
            // 间推
            JSONArray jsonArray1 = fromObject.getJSONArray("list1");
            // 直推
            JSONArray jsonArray2 = fromObject.getJSONArray("list2");
            long l;
            long[] long1 = new long[jsonArray1.size()];
            for (int i = 0; i < jsonArray1.size(); i++) {
                l = jsonArray1.getLong(i);
                long1[i] = l;
            }

            Map<String, Object> maps = new HashMap<String, Object>();

            maps = (Map<String, Object>) paymentOrderBusiness.queryPaymentOrderSumAmountByUserId(long1, startTime,
                    endTime);

            // 充值的金额
            BigDecimal jiantuirechargeAmount = (BigDecimal) maps.get("big");
            // 购买产品的金额
            BigDecimal jiantuibuyAmount = (BigDecimal) maps.get("big1");
            // 还款的金额
            BigDecimal jiantuirepaymentAmount = (BigDecimal) maps.get("big2");

            LOG.info("jiantuirechargeAmount=====" + jiantuirechargeAmount);
            LOG.info("jiantuibuyAmount=====" + jiantuibuyAmount);
            LOG.info("jiantuirepaymentAmount=====" + jiantuirepaymentAmount);

            long l2;
            long[] long2 = new long[jsonArray2.size()];

            for (int j = 0; j < jsonArray2.size(); j++) {
                l2 = jsonArray2.getLong(j);
                long2[j] = l2;
            }

            Map<String, Object> map1 = new HashMap<String, Object>();

            map1 = (Map<String, Object>) paymentOrderBusiness.queryPaymentOrderSumAmountByUserId(long2, startTime,
                    endTime);

            // 充值的金额
            BigDecimal zhituirechargeAmount = (BigDecimal) map1.get("big");
            // 购买产品的金额
            BigDecimal zhituibuyAmount = (BigDecimal) map1.get("big1");
            // 还款的金额
            BigDecimal zhituirepaymentAmount = (BigDecimal) map1.get("big2");

            LOG.info("zhituirechargeAmount=====" + zhituirechargeAmount);
            LOG.info("zhituibuyAmount=====" + zhituibuyAmount);
            LOG.info("zhituirepaymentAmount=====" + zhituirepaymentAmount);

            // 个人分润总和
            BigDecimal profitAmount = profitRecordBusiness.queryProfitRecordSumAcqAmountByPhone(phone, startTime,
                    endTime);
            LOG.info("profitAmount=======" + profitAmount);

            // 个人返佣总和
            BigDecimal distributionAmount = distributionBusiness.queryDistributionSumAcqAmountByPhone(phone, startTime,
                    endTime);
            LOG.info("distributionAmount=======" + distributionAmount);

            map2 = new HashMap<String, Object>();

            map2.put("jiantuiNum", jsonArray1.size());// 间推人数
            map2.put("zhituiNum", jsonArray2.size());// 直推人数
            map2.put("jiantuirechargeAmount", jiantuirechargeAmount);// 间推充值总金额
            map2.put("jiantuibuyAmount", jiantuibuyAmount);// 间推购买总金额
            map2.put("jiantuirepaymentAmount", jiantuirepaymentAmount);// 间推还款总金额
            map2.put("zhituirechargeAmount", zhituirechargeAmount);// 直推充值总金额
            map2.put("zhituibuyAmount", zhituibuyAmount);// 直推购买总金额
            map2.put("zhituirepaymentAmount", zhituirepaymentAmount);// 直推还款总金额
            map2.put("profitAmount", profitAmount);// 个人分润总金额
            map2.put("distributionAmount", distributionAmount);// 个人返佣总金额
        } catch (RestClientException e) {
            LOG.info("查询失败======");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询失败");

            return map;
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, map2);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");

        operations.set(key, map, 5, TimeUnit.HOURS);
        return map;
    }


//	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/querysumamount/byuserids/anddate")
//	public @ResponseBody Object querySumAmountByUserIdsAndDate(
//			@RequestParam(value = "userId") long[] userId,
//			@RequestParam(value = "startTime") String startTime,
//			@RequestParam(value = "endTime") String endTime
//			) {
//
//		BigDecimal queryPaymentOrderSumAmountByUserIds = null;
//		try {
//			Date startTimeDate = DateUtil.getYYMMHHMMSSDateFromStr(startTime);
//			Date endTimeDate = DateUtil.getYYMMHHMMSSDateFromStr(endTime);
//
//			String[] type = new String[3];
//
//			type[0] = "0";
//			type[1] = "1";
//			type[2] = "10";
//
//			queryPaymentOrderSumAmountByUserIds = paymentOrderBusiness.queryPaymentOrderSumAmountByUserIds(userId, type, startTimeDate, endTimeDate);
//
//			LOG.info("queryPaymentOrderSumAmountByUserIds======" + queryPaymentOrderSumAmountByUserIds);
//		} catch (Exception e) {
//			LOG.info("/v1.0/transactionclear/querysumamount/byuserids/anddate  出错啦======" + e);
//
//			return ResultWrap.init(CommonConstants.FALIED, "查询出错!");
//		}
//
//		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", queryPaymentOrderSumAmountByUserIds);
//	}


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/create/outpaymentorder")
    public @ResponseBody
    Object createOutOfPaymentOrder(HttpServletRequest request,
                                   @RequestParam(value = "userId") String userId, @RequestParam(value = "amount") String amount,
                                   @RequestParam(value = "channelTag") String channeltag,
                                   // 出款卡
                                   @RequestParam(value = "bankCard") String bankcard,
                                   // 到账卡
                                   @RequestParam(value = "debitBankCard") String debitBankCard,
                                   @RequestParam(value = "description", defaultValue = "", required = false) String description,
                                   @RequestParam(value = "outNotifyUrl", defaultValue = "", required = false) String outNotifyUrl,
                                   @RequestParam(value = "outReturnUrl", defaultValue = "", required = false) String outReturnUrl,
                                   @RequestParam(value = "outOrderCode") String outOrderCode,
                                   @RequestParam(value = "remark", defaultValue = "", required = false) String remark) {
        Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(amount, 2, BigDecimal.ROUND_HALF_UP);
        if (!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))) {
            return verifyMoney;
        }

        PaymentOrder paymentOrder = paymentOrderBusiness.findByOutMerOrdercode(outOrderCode);
        if (paymentOrder != null) {
            return ResultWrap.init(CommonConstants.FALIED, "订单已存在");
        }

        String url = "http://user/v1.0/user/channel/rate/query/by/channelTag";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId + "");
        requestEntity.add("channelTag", channeltag);
        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSON = JSONObject.fromObject(resultString);

        if (!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CommonConstants.FALIED, resultJSON.getString(CommonConstants.RESP_MESSAGE));
        }

        JSONObject channelRateJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
        JSONObject userJSON = resultJSON.getJSONObject("user");
        Long brandId = userJSON.getLong("brandId");
        String brandName = userJSON.getString("brandname");
        String phone = userJSON.getString("phone");

        JSONObject channelJSON = resultJSON.getJSONObject("channel");
        paymentOrder = new PaymentOrder();

        long channelId = channelJSON.getLong("id");
        String channelName = channelJSON.getString("name");
        String channelTag = channelJSON.getString("channelTag");
        String channelType = channelJSON.getString("channelType");
        String autoclearing = channelJSON.getString("autoclearing");
        // 成本费率
        String costRateStr = channelJSON.getString("costRate");

        double costRate = Double.valueOf(costRateStr);

        String rate = channelRateJSON.getString("rate");
        double userRate = Double.valueOf(rate);
        // 当用户费率小于成本费率时不生成订单!
        if (userRate < costRate) {
            LOG.error("用户费率低于成本费率了!!!生成订单失败=============================用户userId:" + userId);
            return ResultWrap.init(CommonConstants.FALIED, "下单失败,原因:费率异常!");
        }

        String extraFee = channelRateJSON.getString("extraFee");
        String withdrawFee = channelRateJSON.getString("withdrawFee");
        paymentOrder.setChannelid(channelId);
        paymentOrder.setChannelname(channelName);
        paymentOrder.setChannelTag(channelTag);
        paymentOrder.setBankcard(bankcard);
        paymentOrder.setDebitBankCard(debitBankCard);
        paymentOrder.setAutoClearing(autoclearing);
        paymentOrder.setRate(new BigDecimal(rate));
        BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("") || extraFee.equalsIgnoreCase("null")
                ? BigDecimal.ZERO
                : new BigDecimal(extraFee);
        BigDecimal newwithdrawFee = withdrawFee == null || withdrawFee.equalsIgnoreCase("")
                || withdrawFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(withdrawFee);

        paymentOrder.setExtraFee(newextraFee.add(newwithdrawFee));
        paymentOrder.setCostfee(newwithdrawFee);
        paymentOrder.setChannelType(channelType);
        /** 充值到账要扣除手续费的 */
        BigDecimal fee = new BigDecimal("0.00");
        if ("0".equals(autoclearing.trim())) {
            fee = new BigDecimal(amount).multiply(new BigDecimal(rate));
        } else {
            fee = new BigDecimal(amount).multiply(new BigDecimal(rate)).add(paymentOrder.getExtraFee());
        }
        paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));

        paymentOrder.setAmount(new BigDecimal(amount));
        paymentOrder.setUserid(Long.valueOf(userId));
        paymentOrder.setBrandid(brandId);
        paymentOrder.setBrandname(brandName);
        if (remark == null) {
            paymentOrder.setRemark("");
        } else {
            paymentOrder.setRemark(remark);
        }
        paymentOrder.setCreateTime(new Date());
        paymentOrder.setUpdateTime(new Date());
        paymentOrder.setDesc(description);
        paymentOrder.setOrdercode(UUIDGenerator.getDateTimeOrderCode());

        paymentOrder.setPhone(phone);
        paymentOrder.setType("0");

        paymentOrder.setOutMerOrdercode(outOrderCode);
        paymentOrder.setOutNotifyUrl(outNotifyUrl);
        paymentOrder.setOutReturnUrl(outReturnUrl);
        paymentOrder.setStatus(CommonConstants.ORDER_READY);
        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
        return ResultWrap.init(CommonConstants.SUCCESS, "下单成功", paymentOrder);
    }

    // @Scheduled(cron = "0 0 0/1 * * ?")
    //@Scheduled(cron = "0 0/4 * * * ?")
//	public void scheduleFindPaymentOrder() {
//		if ("true".equals(scheduleTaskOnOff)) {
//			updateYBQuickOrderStatus();
//			LOG.info("继续下一个任务=======");
//			findPaymentOrderBySomething();
//		}
//	}

    //@Scheduled(cron = "0 0/2 * * * ?")
    public void scheduleQueryGHTQuickTransferOrder() {
        if ("true".equals(scheduleTaskOnOff)) {
            queryGHTQuickTransferOrder();
        }
    }


    /*
     * @Scheduled(cron = "0 0/2 * * * ?") public void scheduleupdateYBQuickRemark()
     * { if ("true".equals(scheduleTaskOnOff)) { updateYBQuickOrderStatus(); } }
     */

//	// 将未请求的订单状态修改为已取消
//	public void findPaymentOrderBySomething() {
//
//		LOG.info("将未请求的订单状态修改为已取消 开始执行======");
//
//		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date date = new Date();
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(date);
//		calendar.add(Calendar.DAY_OF_MONTH, +1);
//		date = calendar.getTime();
//
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        Date date = new Date();
//
//        LOG.info("date1====" + sdf1.format(zero));
//        LOG.info("date2====" + sdf1.format(date));
//
//        BigDecimal everyDayMaxLimit;
//        try {
//            everyDayMaxLimit = paymentOrderBusiness.getEveryDayMaxLimit(sdf1.format(zero), sdf1.format(date), channelTag, orderStatus, Long.valueOf(userId));
//            LOG.info("查询结果为======" + everyDayMaxLimit);
//            if (everyDayMaxLimit == null) {
//                everyDayMaxLimit = BigDecimal.ZERO;
//            }
//            LOG.info("重新赋值的结果为======" + everyDayMaxLimit);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//
//            return ResultWrap.init(CommonConstants.FALIED, "查询失败");
//        }
//
//        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", everyDayMaxLimit);
//
//    }

//    // 通道成功率
//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/finduccess")
//    public @ResponseBody
//    Object findSuccessRate(HttpServletRequest request,
//                           /**
//                            * 贴牌筛选
//                            */
//                           @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandId, /***
//                            * 时间筛选
//                            **/
//                           @RequestParam(value = "starttime", required = false) String startTime,
//                           @RequestParam(value = "endtime", required = false) String endTime) {
//        Date StartTimeDate = null;
//        if (startTime != null && startTime.trim().length() > 0) {
//            StartTimeDate = DateUtil.getDateFromStr(startTime);
//        }
//        Date endTimeDate = null;
//        if (endTime != null && endTime.trim().length() > 0) {
//            endTimeDate = DateUtil.getDateFromStr(endTime);
//        }
//
//        if ((startTime == null || startTime.trim().length() == 0)
//                && (endTime == null || endTime.trim().length() == 0)) {
//            StartTimeDate = DateUtil.getDateFromStr(DateUtil.getDateFromStr(new Date()));
//        }
//
//        List<PaymentOrderNumber> SuccessRate = paymentOrderBusiness.findOrderSuccessRate(brandId, StartTimeDate,
//                endTimeDate);
//
//        Map map = new HashMap();
//        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//        map.put(CommonConstants.RESULT, SuccessRate);
//
//        return map;
//    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/deletepayment/byordercode")
    public @ResponseBody
    Object deletePaymentByOrdercode(HttpServletRequest request,
                                    @RequestParam(value = "ordercode") String ordercode) {

        Map map = new HashMap();

        paymentOrderBusiness.deletePaymentOrderByOrderCode(ordercode);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /*
     * @RequestMapping(method = RequestMethod.POST, value =
     * "/v1.0/transactionclear/payment/deletepayment/bylist") public @ResponseBody
     * Object deletePaymentByList(HttpServletRequest request,
     *
     * @RequestParam(value = "startTime", required = false) String startTime,
     *
     * @RequestParam(value = "endTime", required = false) String endTime,
     *
     * @RequestParam(value = "channelTag", required = false) String[] channelTag,
     *
     * @RequestParam(value = "status", required = false) String status,
     *
     * @RequestParam(value = "remark", required = false) String remark) {
     *
     * Map map = new HashMap();
     *
     * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); Date date = new
     * Date(); Calendar calendar = Calendar.getInstance(); calendar.setTime(date);
     * calendar.add(Calendar.DAY_OF_MONTH, +1); date = calendar.getTime();
     *
     * Date date2 = new Date();
     *
     * LOG.info("====" + sdf.format(date)); LOG.info("====" + sdf.format(date2));
     *
     * String[] strings = { "LD_QUICK", "LF_QUICK" };
     *
     * try { List<PaymentOrder> list =
     * paymentOrderBusiness.findOrderByTimeAndChannelTagAndStatusAndRemark(sdf.
     * format(date2), sdf.format(date), strings, status);
     *
     * LOG.info("=============" + list.size());
     *
     * // paymentOrderBusiness.deletePaymentOrder(list); } } catch (Exception e) {
     * LOG.info("操作失败=====" + e); map.put(CommonConstants.RESP_CODE,
     * CommonConstants.FALIED); map.put(CommonConstants.RESP_MESSAGE, "失败"); return
     * map; }
     *
     * map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
     * map.put(CommonConstants.RESP_MESSAGE, "成功"); return map;
     *
     * }
     */

    // 根据订单号查询用户进件的结算卡
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/findbankcard/byordercode")
    public @ResponseBody
    Object findBankCardByOrderCode(@RequestParam(value = "ordercode") String orderCode) {
        Map<String, String> map = new HashMap<String, String>();

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);

        String object = null;
        String phone = paymentOrder.getPhone();
        String channelTag = paymentOrder.getChannelTag();
        long userid = paymentOrder.getUserid();

        if ("UMP_PAY".equals(channelTag)) {

            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("user", "error url request!");
            String url = uri.toString() + "/v1.0/user/bank/default/userid";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("user_id", userid + "");
            String result;
            JSONObject jsonObject;
            JSONObject resultObj = null;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/user/bank/default/userid--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("查询订单有误=====");
                e.printStackTrace();
                LOG.error("", e);
                map.put(CommonConstants.RESP_CODE, "failed");
                map.put(CommonConstants.RESP_MESSAGE, "查询订单信息有误");
                return map;
            }

            object = resultObj.getString("cardNo");

        } else {

            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            String url = uri.toString() + "/v1.0/paymentchannel/querybankcard/byphone/andchanneltag";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", phone);
            requestEntity.add("channel_tag", channelTag);
            String result;
            JSONObject jsonObject;
            JSONObject resultObj = null;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentchannel/querybankcard/byphone/andchanneltag--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                // resultObj = jsonObject.getJSONObject("result");
                object = (String) jsonObject.get("result");
            } catch (Exception e) {
                LOG.error("查询订单有误=====");
                e.printStackTrace();
                LOG.error("", e);
                map.put(CommonConstants.RESP_CODE, "failed");
                map.put(CommonConstants.RESP_MESSAGE, "查询订单信息有误");
                return map;
            }

        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/querysumamount/byuserids/anddate")
    public @ResponseBody
    Object querySumAmountByUserIdsAndDate(
            @RequestParam(value = "userId") long[] userId,
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime
    ) {

        BigDecimal queryPaymentOrderSumAmountByUserIds = null;
        try {
            Date startTimeDate = DateUtil.getYYMMHHMMSSDateFromStr(startTime);
            Date endTimeDate = DateUtil.getYYMMHHMMSSDateFromStr(endTime);

            String[] type = new String[3];

            type[0] = "0";
            type[1] = "1";
            type[2] = "10";

            queryPaymentOrderSumAmountByUserIds = paymentOrderBusiness.queryPaymentOrderSumAmountByUserIds(userId, type, startTimeDate, endTimeDate);

            LOG.info("queryPaymentOrderSumAmountByUserIds======" + queryPaymentOrderSumAmountByUserIds);
        } catch (Exception e) {
            LOG.info("/v1.0/transactionclear/querysumamount/byuserids/anddate  出错啦======" + e);

            return ResultWrap.init(CommonConstants.FALIED, "查询出错!");
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", queryPaymentOrderSumAmountByUserIds);
    }


//    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/create/outpaymentorder")
//    public @ResponseBody
//    Object createOutOfPaymentOrder(HttpServletRequest request,
//                                   @RequestParam(value = "userId") String userId, @RequestParam(value = "amount") String amount,
//                                   @RequestParam(value = "channelTag") String channeltag,
//                                   // 出款卡
//                                   @RequestParam(value = "bankCard") String bankcard,
//                                   // 到账卡
//                                   @RequestParam(value = "debitBankCard") String debitBankCard,
//                                   @RequestParam(value = "description", defaultValue = "", required = false) String description,
//                                   @RequestParam(value = "outNotifyUrl", defaultValue = "", required = false) String outNotifyUrl,
//                                   @RequestParam(value = "outReturnUrl", defaultValue = "", required = false) String outReturnUrl,
//                                   @RequestParam(value = "outOrderCode") String outOrderCode,
//                                   @RequestParam(value = "remark", defaultValue = "", required = false) String remark) {
//        Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(amount, 2, BigDecimal.ROUND_HALF_UP);
//        if (!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))) {
//            return verifyMoney;
//        }
//
//        PaymentOrder paymentOrder = paymentOrderBusiness.findByOutMerOrdercode(outOrderCode);
//        if (paymentOrder != null) {
//            return ResultWrap.init(CommonConstants.FALIED, "订单已存在");
//        }
//
//        String url = "http://user/v1.0/user/channel/rate/query/by/channelTag";
//        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//        requestEntity.add("userId", userId + "");
//        requestEntity.add("channelTag", channeltag);
//        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
//        JSONObject resultJSON = JSONObject.fromObject(resultString);
//
//        if (!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))) {
//            return ResultWrap.init(CommonConstants.FALIED, resultJSON.getString(CommonConstants.RESP_MESSAGE));
//        }
//
//        JSONObject channelRateJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
//        JSONObject userJSON = resultJSON.getJSONObject("user");
//        Long brandId = userJSON.getLong("brandId");
//        String brandName = userJSON.getString("brandname");
//        String phone = userJSON.getString("phone");
//
//        JSONObject channelJSON = resultJSON.getJSONObject("channel");
//        paymentOrder = new PaymentOrder();
//
//        long channelId = channelJSON.getLong("id");
//        String channelName = channelJSON.getString("name");
//        String channelTag = channelJSON.getString("channelTag");
//        String channelType = channelJSON.getString("channelType");
//        String autoclearing = channelJSON.getString("autoclearing");
//        // 成本费率
//        String costRateStr = channelJSON.getString("costRate");
//
//        double costRate = Double.valueOf(costRateStr);
//
//        String rate = channelRateJSON.getString("rate");
//        double userRate = Double.valueOf(rate);
//        // 当用户费率小于成本费率时不生成订单!
//        if (userRate < costRate) {
//            LOG.error("用户费率低于成本费率了!!!生成订单失败=============================用户userId:" + userId);
//            return ResultWrap.init(CommonConstants.FALIED, "下单失败,原因:费率异常!");
//        }
//
//        String extraFee = channelRateJSON.getString("extraFee");
//        String withdrawFee = channelRateJSON.getString("withdrawFee");
//        paymentOrder.setChannelid(channelId);
//        paymentOrder.setChannelname(channelName);
//        paymentOrder.setChannelTag(channelTag);
//        paymentOrder.setBankcard(bankcard);
//        paymentOrder.setDebitBankCard(debitBankCard);
//        paymentOrder.setAutoClearing(autoclearing);
//        paymentOrder.setRate(new BigDecimal(rate));
//        BigDecimal newextraFee = extraFee == null || extraFee.equalsIgnoreCase("") || extraFee.equalsIgnoreCase("null")
//                ? BigDecimal.ZERO
//                : new BigDecimal(extraFee);
//        BigDecimal newwithdrawFee = withdrawFee == null || withdrawFee.equalsIgnoreCase("")
//                || withdrawFee.equalsIgnoreCase("null") ? BigDecimal.ZERO : new BigDecimal(withdrawFee);
//
//        paymentOrder.setExtraFee(newextraFee.add(newwithdrawFee));
//        paymentOrder.setCostfee(newwithdrawFee);
//        paymentOrder.setChannelType(channelType);
//        /** 充值到账要扣除手续费的 */
//        BigDecimal fee = new BigDecimal("0.00");
//        if ("0".equals(autoclearing.trim())) {
//            fee = new BigDecimal(amount).multiply(new BigDecimal(rate));
//        } else {
//            fee = new BigDecimal(amount).multiply(new BigDecimal(rate)).add(paymentOrder.getExtraFee());
//        }
//        paymentOrder.setRealAmount(new BigDecimal(amount).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN));
//
//        paymentOrder.setAmount(new BigDecimal(amount));
//        paymentOrder.setUserid(Long.valueOf(userId));
//        paymentOrder.setBrandid(brandId);
//        paymentOrder.setBrandname(brandName);
//        if (remark == null) {
//            paymentOrder.setRemark("");
//        } else {
//            paymentOrder.setRemark(remark);
//        }
//        paymentOrder.setCreateTime(new Date());
//        paymentOrder.setUpdateTime(new Date());
//        paymentOrder.setDesc(description);
//        paymentOrder.setOrdercode(UUIDGenerator.getDateTimeOrderCode());
//
//        paymentOrder.setPhone(phone);
//        paymentOrder.setType("0");
//
//        paymentOrder.setOutMerOrdercode(outOrderCode);
//        paymentOrder.setOutNotifyUrl(outNotifyUrl);
//        paymentOrder.setOutReturnUrl(outReturnUrl);
//        paymentOrder.setStatus(CommonConstants.ORDER_READY);
//        paymentOrder = paymentOrderBusiness.mergePaymentOrder(paymentOrder);
//        LOG.info("RESULT================" + paymentOrder.toString() + "************************************");
//        return ResultWrap.init(CommonConstants.SUCCESS, "下单成功", paymentOrder);
//    }



    /*
     * @Scheduled(cron = "0 0/2 * * * ?") public void scheduleupdateYBQuickRemark()
     * { if ("true".equals(scheduleTaskOnOff)) { updateYBQuickOrderStatus(); } }
     */

//    // 将未请求的订单状态修改为已取消
//    public void findPaymentOrderBySomething() {
//
//        LOG.info("将未请求的订单状态修改为已取消 开始执行======");
//
//        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.DAY_OF_MONTH, +1);
//        date = calendar.getTime();
//
//
//		/*Date date1 = new Date();
//		Calendar calendar1 = Calendar.getInstance();
//		calendar1.setTime(date1);
//		calendar1.add(Calendar.DAY_OF_MONTH, -1);
//		date1 = calendar1.getTime();*/
//
//        Calendar calendar1 = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        Date zero = calendar.getTime();
//
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        Date date2 = new Date();
//		/*Calendar calendar2 = Calendar.getInstance();
//		calendar2.setTime(date2);
//		calendar2.add(Calendar.MINUTE, -5);
//		date2 = calendar2.getTime();*/
//
//
//        LOG.info("date====" + sdf1.format(date));
//        LOG.info("date1====" + sdf1.format(zero));
//        LOG.info("date2====" + sdf1.format(date2));
//
//        //String[] strings = {"YB_QUICK", "YB_PAY", "YLDZ_QUICK", "JP_QUICK", "JF_QUICK", "KY_QUICK_R", "HQ_QUICK1", "HQ_QUICK2"};
//
//        List<String> channelTag = channelTagBusiness.getChannelTag();
//
//        LOG.info("channelTag======" + channelTag);
//
//        String[] array = new String[channelTag.size()];
//        String[] strings = channelTag.toArray(array);
//
//        LOG.info("strings======" + strings);
//
//        List<PaymentOrder> list = paymentOrderBusiness.findOrderByTimeAndChannelTagAndStatus(sdf1.format(zero),
//                sdf1.format(date2), strings, "0", "");
//
//        LOG.info("需要改成 已取消的=============" + list.size());
//
//        // 修改订单状态为 已取消
//        if (list != null || list.size() > 0) {
//            for (PaymentOrder po : list) {
//                PaymentOrder queryPaymentOrderBycode = paymentOrderBusiness.queryPaymentOrderBycode(po.getOrdercode());
//
//                queryPaymentOrderBycode.setStatus("2");
//
//                paymentOrderBusiness.mergePaymentOrder(queryPaymentOrderBycode);
//            }
//        }
//
//        List<PaymentOrder> list1 = paymentOrderBusiness
//                .findOrderByTimeAndChannelTagAndStatusAndRemark(sdf1.format(zero), sdf1.format(date2), strings, "2");
//
//        LOG.info("需要改成 待完成的=============" + list1.size());
//        // 修改订单状态为 待完成
//        if (list1 != null || list1.size() > 0) {
//            for (PaymentOrder po : list1) {
//                PaymentOrder queryPaymentOrderBycode1 = paymentOrderBusiness.queryPaymentOrderBycode(po.getOrdercode());
//
//                queryPaymentOrderBycode1.setStatus("0");
//
//                paymentOrderBusiness.mergePaymentOrder(queryPaymentOrderBycode1);
//            }
//        }
//
//        LOG.info("将未请求的订单状态修改为已取消 执行完成======");
//    }


    // 银联5===================================================
    //@Scheduled(cron = "0 0/3 * * * ?")
    public void updateYBQuickOrderStatus() {

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		/*Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		date = calendar.getTime();*/

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date1 = new Date();
		/*Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		calendar1.add(Calendar.MINUTE, -10);
		date1 = calendar1.getTime();*/

        LOG.info("====" + sdf1.format(zero));
        LOG.info("====" + sdf1.format(date1));

        String[] status = {"0", "2"};

        List<String> findYBpayOrder = paymentOrderBusiness.findYBpayOrder(sdf1.format(zero), sdf1.format(date1), "YB_PAY", status);

        //List<String> findYBpayOrder =paymentOrderBusiness.findYBpayOrder("2018-05-31", "2018-06-01", "YB_PAY", status);

        LOG.info("银联5需要查询的订单数======" + findYBpayOrder.size());

        HttpServletRequest request = null;

        if (findYBpayOrder != null && findYBpayOrder.size() > 0) {

            for (String orderCode : findYBpayOrder) {

                try {
                    queryOrderCode(request, orderCode);
                } catch (Exception e) {
                    e.printStackTrace();

                    continue;
                }

            }

            LOG.info("银联5查询订单任务执行完成======");
        } else {
            LOG.info("银联5暂无执行任务======");
        }

        // ===========

        List<String> findYBquickOrder = paymentOrderBusiness.findYBpayOrder(sdf1.format(zero), sdf1.format(date1), "YB_QUICK", status);

        //List<String> findYBquickOrder = paymentOrderBusiness.findYBpayOrder("2018-05-31", "2018-06-01", "YB_QUICK",  status);

        LOG.info("银联4需要查询的订单数======" + findYBquickOrder.size());

        if (findYBquickOrder != null && findYBquickOrder.size() > 0) {

            for (String orderCode : findYBquickOrder) {

                try {
                    queryOrderCodeYBQuick(request, orderCode);
                } catch (Exception e) {
                    e.printStackTrace();

                    continue;
                }

            }

            LOG.info("银联4查询订单任务执行完成======");
        } else {
            LOG.info("银联4暂无执行任务======");
        }

    }

    // 银联5的订单查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/query/ordercode")
    public @ResponseBody
    Object queryOrderCode(HttpServletRequest request,
                          @RequestParam(value = "ordercode") String orderCode) {

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);

        param2[0].setValue("10015703385");
        param2[1].setValue(orderCode);
        param2[2].setValue(paymentOrder.getCreateTime().toString());
        param2[3].setValue("1");
        param2[param2.length - 1].setValue(hmacSign2());
        PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/tradeReviceQuery.action");
        HttpClient client = new HttpClient();
        postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        postMethod.setRequestBody(param2);
        String code = null;
        String balance = null;
        int status2;
        try {
            status2 = client.executeMethod(postMethod);
            LOG.info("==========status2==========" + status2);
            String response = postMethod.getResponseBodyAsString();
            LOG.info("==========response==========" + response);

            JSONObject fromObject = JSONObject.fromObject(response);

            code = (String) fromObject.get("code");

            if ("0000".equals(code)) {

                JSONArray jsonArray = fromObject.getJSONArray("tradeReceives");
                if (jsonArray == null || jsonArray.size() == 0) {
                    LOG.info("tradeReceives为空======");
                    return null;
                }

                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++) {

                    jsonObject = (JSONObject) jsonArray.get(i);

                }

                LOG.info("jsonObject======" + jsonObject);

                String status = jsonObject.getString("status");
                String errorMessage = "";

                if (jsonObject.has("errorMessage")) {
                    errorMessage = jsonObject.getString("errorMessage");
                }
                /*
                 * else { errorMessage = "未知失败原因,请联系易宝客服"; }
                 */

                // LOG.info("status======"+status);
                // LOG.info("errorMessage======"+errorMessage);

                if ("FAIL".equalsIgnoreCase(status)) {
                    if (!errorMessage.contains("支付记录不存在") && !"".equals(errorMessage)) {

                        addErrorMsg(request, orderCode, errorMessage, "-1");

                        updatePaymentOrder(request, orderCode, "0", "", "");

                        return null;
                    } else {

                        // updatePaymentOrder(request, orderCode, "2", "", "");
                        return null;
                    }

                } else if ("INIT".equalsIgnoreCase(status)) {

                    // updatePaymentOrder(request, orderCode, "2", "", "");
                    return null;
                }

            } else {
                LOG.info("请求查询订单失败======");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("", e);
            return null;
        }

        return null;
    }

    private static NameValuePair[] param2 = {
            // 大商户编号
            new NameValuePair("mainCustomerNumber", ""),
            /*
			 * // 小商户编号 new NameValuePair("customerNumber", ""),
			 */
            // 出款订单号
            new NameValuePair("requestId", ""),

            new NameValuePair("createTimeBegin", ""), new NameValuePair("pageNo", ""),
            // 签名串
            new NameValuePair("hmac", ""),

    };

    private static String hmacSign2() {
        StringBuilder hmacStr = new StringBuilder();
        for (NameValuePair nameValuePair : param2) {
            if (nameValuePair.getName().equals("hmac")) {
                continue;
            }
            hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

        }

        String hmac = Digest.hmacSign(hmacStr.toString(),
                "oF34lTpB9x9v05D2B0eP1r18EDX71THlT4Go5X0s6V7T85gh2J63j30iPh38");

        return hmac;
    }

    // 银联5=========================================================end

    // 银联4的订单查询接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/query/ordercode/ybquick")
    public @ResponseBody
    Object queryOrderCodeYBQuick(HttpServletRequest request,
                                 @RequestParam(value = "ordercode") String orderCode) {

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);

        param2[0].setValue("10015053457");
        param2[1].setValue(orderCode);
        param2[2].setValue(paymentOrder.getCreateTime().toString());
        param2[3].setValue("1");
        param2[param2.length - 1].setValue(hmacSign());
        PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/tradeReviceQuery.action");
        HttpClient client = new HttpClient();
        postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        postMethod.setRequestBody(param2);
        String code = null;
        String balance = null;
        int status2;
        try {
            status2 = client.executeMethod(postMethod);
            // LOG.info("==========status2==========" + status2);
            String response = postMethod.getResponseBodyAsString();
            // LOG.info("==========response==========" + response);

            JSONObject fromObject = JSONObject.fromObject(response);

            code = (String) fromObject.get("code");

            if ("0000".equals(code)) {

                JSONArray jsonArray = fromObject.getJSONArray("tradeReceives");

                if (jsonArray == null || jsonArray.size() == 0) {
                    LOG.info("tradeReceives为空======");
                    return null;
                }

                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++) {

                    jsonObject = (JSONObject) jsonArray.get(i);

                }

                LOG.info("jsonObject======" + jsonObject);

                String status = jsonObject.getString("status");
                String errorMessage = "";

                if (jsonObject.has("errorMessage")) {
                    errorMessage = jsonObject.getString("errorMessage");
                }

                if ("FAIL".equalsIgnoreCase(status)) {
                    if (!errorMessage.contains("支付记录不存在") && !"".equals(errorMessage)) {

                        addErrorMsg(request, orderCode, errorMessage, "-1");

                        updatePaymentOrder(request, orderCode, "0", "", "");

                        return null;
                    } else {

                        return null;
                    }

                } else if ("INIT".equalsIgnoreCase(status)) {

                    return null;
                }

            } else {
                LOG.info("请求查询订单失败======");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("", e);
            return null;
        }

        return null;
    }

    private static String hmacSign() {
        StringBuilder hmacStr = new StringBuilder();
        for (NameValuePair nameValuePair : param2) {
            if (nameValuePair.getName().equals("hmac")) {
                continue;
            }
            hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

        }

        String hmac = Digest.hmacSign(hmacStr.toString(),
                "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w");

        return hmac;
    }


    //@Scheduled(cron = "0 0/2 * * * ?")
    public void queryGHTQuickTransferOrder() {
        LOG.info("开始执行查询请求高汇通代付成功,等待银行出款的订单======");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date1 = new Date();

        LOG.info("====" + sdf1.format(zero));
        LOG.info("====" + sdf1.format(date1));

        String[] strings = {"GHT_QUICK", "GHT_QUICK1"};

        //查询出系统请求代付成功的订单
		/*List<PaymentOrder> list = paymentOrderBusiness.findOrderByTimeAndChannelTagAndStatus(sdf1.format(zero),
				sdf1.format(date1), strings, "0", "代付成功,等待银行出款");*/

        List<PaymentOrder> list = paymentOrderBusiness.findOrderByUpdateTimeTimeAndChannelTagAndStatus(sdf1.format(zero),
                sdf1.format(date1), strings, "0", "代付成功,等待银行出款");

        if (list != null || list.size() > 0) {
            for (PaymentOrder po : list) {

                String url = "http://101.132.255.217/v1.0/paymentgateway/topup/ghtquick/transfer/orderquery";
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("orderCode", po.getThirdOrdercode());
                String result = new RestTemplate().postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentgateway/topup/ghtquick/transfer/orderquery====RESULT================" + result);
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.fromObject(result);
                } catch (Exception e) {
                    LOG.error("查询支付订单信息出错======" + e);
                    continue;
                }
                String respCode = jsonObject.getString("resp_code");

                if ("000000".equals(respCode)) {

                    updatePaymentOrder(null, po.getOrdercode(), "1", "", "");

                    try {
                        createProfitByOrderCode(po.getOrdercode());
                    } catch (Exception e) {
                        LOG.error("添加分润信息有误======", e);

                        po.setPhoneBill("0.00");
                        po.setCarNo("0.00");

                        paymentOrderBusiness.mergePaymentOrder(po);
                    }

					/*queryPaymentOrderBycode.setRemark("订单交易成功");

					paymentOrderBusiness.mergePaymentOrder(queryPaymentOrderBycode);*/

                    LOG.info("订单状态修改成功===================" + po.getOrdercode() + "====================" + result);

                    LOG.info("订单已完成!");

                } else if ("999999".equals(respCode)) {

                    String resp_message = jsonObject.getString("resp_message");

                    LOG.info("代付失败!");
                    po.setRemark("代付失败: " + resp_message);

                    paymentOrderBusiness.mergePaymentOrder(po);

                } else {

                    continue;
                }

            }
        }

    }


    //计算通道的分润
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/paymentorder/calculationprofit")
    public @ResponseBody
    Object calculationProfit(HttpServletRequest request,
                             @RequestParam(value = "channelTag") String channelTag,
                             @RequestParam(value = "repaymentOrQuick") String repaymentOrQuick,
                             @RequestParam(value = "start_time", required = false) String startTime,
                             @RequestParam(value = "end_time", required = false) String endTime
    ) {
        //0:快捷   1:还款

        ChannelCostRate channelCostRate = channelCostRateBusiness.getChannelCostRateByChannelTag(channelTag);

        BigDecimal calculationProfit = null;
        try {
            //calculationProfit = paymentOrderBusiness.getCalculationProfit(channelTag, repaymentOrQuick, channelCostRate.getCostRate(), channelCostRate.getCostExtraFee(), startTime, endTime);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrap.init(CommonConstants.FALIED, "查询失败");
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", calculationProfit);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/updateordercode")
    public @ResponseBody
    Object updatePaymentOrder(HttpServletRequest request,
                              @RequestParam(value = "order_code") String ordercode,
                              @RequestParam(value = "status", required = false) String status
    ) {

        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

        if (order != null) {

            order.setStatus("1");
            paymentOrderBusiness.mergePaymentOrder(order);

            return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "无该订单信息!");
        }

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/updateorderinfo")
    public @ResponseBody
    Object updatePaymentOrderInfo(HttpServletRequest request,
                                  @RequestParam(value = "order_code") String ordercode,
                                  @RequestParam(value = "orderDesc", required = false, defaultValue = "-1") String orderDesc,
                                  @RequestParam(value = "outNotifyUrl", required = false, defaultValue = "-1") String outNotifyUrl,
                                  @RequestParam(value = "outMerOrdercode", required = false, defaultValue = "-1") String outMerOrdercode,
                                  @RequestParam(value = "outReturnUrl", required = false, defaultValue = "-1") String outReturnUrl
    ) {

        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

        if (order != null) {

            order.setDesc("-1".equals(orderDesc) ? order.getDesc() : orderDesc);
            order.setOutNotifyUrl("-1".equals(outNotifyUrl) ? order.getOutNotifyUrl() : outNotifyUrl);
            order.setOutMerOrdercode("-1".equals(outMerOrdercode) ? order.getOutMerOrdercode() : outMerOrdercode);
            order.setOutReturnUrl("-1".equals(outReturnUrl) ? order.getOutReturnUrl() : outReturnUrl);

            paymentOrderBusiness.mergePaymentOrder(order);

            return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "无该订单信息!");
        }

    }


	/*@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/qwerty")
	public @ResponseBody Object testQwerty(HttpServletRequest request,
			@RequestParam(value = "startTime") String startTime,
			@RequestParam(value = "endTime") String endTime
			) throws Exception {

		String[] orderType = {"1","2"};
		String[] channelTag = {"JIEFUBAO","RECHARGE_ACCOUNT","ABROAD_CONSUME","CARD_EVA","SPALI_PAY"};

		List<String> paymentOrder = paymentOrderBusiness.getPaymentOrderByStartTimeAndEndTime(orderType, channelTag, startTime, endTime);

		LOG.info("paymentOrder======" + paymentOrder);

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", paymentOrder);
	}*/


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query/qwertyui")
    public @ResponseBody
    Object testQwertyui(HttpServletRequest request
    ) throws Exception {

        String str = "ad12ce4e974b47538c0c0ce308b29f,c307495e31e2494f9b599f1072567e,bd576d0c6ca64fb1be4584f5bc9efd,bfb3a5d07d8e48c1bb4027ed62fa7a,a70f1c0ca245493d816cecc06f0b9a,1828f4edae344ec7befdbae4680d66,6324aba623ba44f2a9e8deb3465f28,34d12afa7d984d46a142bdcbd75df6,c2dd1f9b1c94461d89f07af7cf7a36,438ddae4d98045d49341b56249d438,1870a1d936e3406c8fd15bf4f348d0,13bac1da5f404388aa47997712561c,022922e9100d4f21944c1be4a3f61c,02b3e591a590451a9a4a08b77673ea,02562463ff7545cb9b0457e9f0f075,c20b105a3cca4e939e9b6572e2af7d,5074244d2f8b40cc99f5c25089612e,4e463ce5a3ee48e2ab3b77b90ff36d,9dc422c9c4fb44499bb8bbbfeb6f94";


        String[] split = str.split(",");

        LOG.info("长度=====" + split.length);

        int j = 1;

        for (String strs : split) {

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://106.15.47.73/v1.0/transactionclear/payment/createprofit/byordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("orderCode", strs);
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");

            LOG.info("order_code=====" + str);
            LOG.info("j=====" + j++);

        }

        return null;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/createprofit/byordercode")
    public @ResponseBody
    Object createProfitByOrderCode(@RequestParam(value = "orderCode") String orderCode
    ) {

        PaymentOrder order = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
        if (order == null) {
            return ResultWrap.init(CommonConstants.FALIED, "未获取到该订单!");
        }

        String url = "http://user/v1.0/user/brandrate/query";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", order.getBrandid() + "");
        requestEntity.add("channel_id", order.getChannelid() + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);

        JSONObject fromObject = JSONObject.fromObject(result);
        JSONObject jsonObject = fromObject.getJSONObject("result");
        String minRate = jsonObject.getString("minrate");
        String withdrawFee = jsonObject.getString("withdrawFee");

        BigDecimal brandProfit = BigDecimal.ZERO;
        BigDecimal costProfit = BigDecimal.ZERO;
        BigDecimal amount = order.getAmount();
        BigDecimal realAmount = order.getRealAmount();
        String channelTag = order.getChannelTag();

        ChannelCostRate channelCostRateByChannelTag = channelCostRateBusiness.getChannelCostRateByChannelTag(channelTag);
        BigDecimal costRate = channelCostRateByChannelTag.getCostRate();
        BigDecimal costExtraFee = channelCostRateByChannelTag.getCostExtraFee();

        if ("0".equals(order.getType())) {

            brandProfit = amount.subtract(realAmount).subtract(amount.multiply(new BigDecimal(minRate))).subtract(new BigDecimal(withdrawFee)).setScale(2, ROUND_DOWN);

            if (channelCostRateByChannelTag.getIsBankRate() == 1) {
                ChannelBankRate channelBankRateByChannelTagAndBankName = channelCostRateBusiness.getChannelBankRateByChannelTagAndBankName(channelTag, Util.queryBankNameByBranchName(order.getBankName()));

                if (channelBankRateByChannelTagAndBankName != null) {
                    BigDecimal costRate1 = channelBankRateByChannelTagAndBankName.getCostRate();
                    BigDecimal extraFee = channelBankRateByChannelTagAndBankName.getExtraFee();

                    costProfit = amount.subtract(realAmount).subtract(amount.multiply(costRate1)).subtract(extraFee).setScale(2, ROUND_DOWN);
                } else {

                    costProfit = amount.subtract(realAmount).subtract(amount.multiply(costRate)).subtract(costExtraFee).setScale(2, ROUND_DOWN);
                }
            } else {

                costProfit = amount.subtract(realAmount).subtract(amount.multiply(costRate)).subtract(costExtraFee).setScale(2, ROUND_DOWN);
            }

            order.setPhoneBill(brandProfit + "");
            order.setCarNo(costProfit.subtract(brandProfit) + "");
        }

        if ("10".equals(order.getType())) {

            BigDecimal subtract = order.getRate().subtract(new BigDecimal(minRate));
            brandProfit = realAmount.multiply(subtract).setScale(2, ROUND_DOWN);

            if (channelCostRateByChannelTag.getIsBankRate() == 1) {
                ChannelBankRate channelBankRateByChannelTagAndBankName = channelCostRateBusiness.getChannelBankRateByChannelTagAndBankName(channelTag, Util.queryBankNameByBranchName(order.getBankName()));

                if (channelBankRateByChannelTagAndBankName != null) {
                    BigDecimal costRate1 = channelBankRateByChannelTagAndBankName.getCostRate();
                    BigDecimal extraFee = channelBankRateByChannelTagAndBankName.getExtraFee();

                    costProfit = realAmount.multiply(new BigDecimal(minRate).subtract(costRate1)).setScale(2, ROUND_DOWN);
                } else {

                    costProfit = realAmount.multiply(new BigDecimal(minRate).subtract(costRate)).setScale(2, ROUND_DOWN);
                }
            } else {

                costProfit = realAmount.multiply(new BigDecimal(minRate).subtract(costRate)).setScale(2, ROUND_DOWN);
            }

            order.setPhoneBill(brandProfit + "");

            if ("HQB_QUICK".equals(channelTag) || "SS_QUICK".equals(channelTag)) {

                String substring = orderCode.substring(orderCode.length() - 1, orderCode.length());
                if ("3".equals(substring)) {
                    order.setCarNo(costProfit.add(order.getExtraFee().subtract(costExtraFee)).setScale(2, ROUND_DOWN) + "");
                } else {
                    order.setCarNo(costProfit + "");
                }
            } else {
                order.setCarNo(costProfit + "");
            }
        }

        if ("11".equals(order.getType())) {

            BigDecimal extraFee = order.getExtraFee();
            brandProfit = extraFee.subtract(new BigDecimal(withdrawFee)).setScale(2, ROUND_DOWN);
            costProfit = new BigDecimal(withdrawFee).subtract(costExtraFee).setScale(2, ROUND_DOWN);
            if ("HQB_QUICK".equals(channelTag) || "SS_QUICK".equals(channelTag)) {
                order.setPhoneBill(BigDecimal.ZERO + "");
                order.setCarNo(BigDecimal.ZERO + "");
            } else {
                order.setPhoneBill(brandProfit + "");
                order.setCarNo(costProfit + "");
            }
        }

        LOG.info("brandProfit======" + brandProfit);
        LOG.info("costProfit======" + costProfit);

        paymentOrderBusiness.mergePaymentOrder(order);

        return ResultWrap.init(CommonConstants.SUCCESS, "添加分润成功!");
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/externalRelease/query/byOrderCode")
    public @ResponseBody
    Object externalReleaseQueryByOrderCode(HttpServletRequest request,
                                           String orderCode,
                                           long brandId,
                                           String secretKey
    ) {

        String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandId;
        String result = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(result);

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString(CommonConstants.RESP_MESSAGE));
        }

        jsonObject = jsonObject.getJSONObject(CommonConstants.RESULT);
        String brandType = jsonObject.getString("brandType");
        if ("2".equals(brandType)) {
            return ResultWrap.init(CommonConstants.FALIED, "暂时停止查询订单信息,请联系相关人员解决!");
        }
        String number = jsonObject.getString("number");
        if (!secretKey.trim().equals(number.trim())) {
            return ResultWrap.init(CommonConstants.FALIED, "密钥验证错误,请仔细核对!");
        }

        boolean hasKey = false;
        String key = "/v1.0/transactionclear/externalRelease/query/byOrderCode:orderCode=" + orderCode;
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            return opsForValue.get(key);
        }

        PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(orderCode);
        if (paymentOrder == null) {

            return ResultWrap.init(CommonConstants.ERROR_PAYMENT_NOT_EXIST, "订单号不存在!");
        } else {

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("orderCode", paymentOrder.getOrdercode());
            jsonObj.put("amount", paymentOrder.getAmount());
            jsonObj.put("realAmount", paymentOrder.getRealAmount());
            jsonObj.put("rate", paymentOrder.getRate());
            jsonObj.put("extraFee", paymentOrder.getExtraFee());
            jsonObj.put("orderStatus", paymentOrder.getStatus());
            jsonObj.put("orderMsg", paymentOrder.getRemark());
            jsonObj.put("phone", paymentOrder.getPhone());

            Map<String, Object> map = new HashMap<String, Object>();

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
            map.put(CommonConstants.RESULT, jsonObj);

            opsForValue.set(key, map, 10, TimeUnit.SECONDS);

            //return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonObject);
            return map;
        }
    }


    private static String sendOrderNotify(String key, String orderCode, String amount, String realAmount, String userId, String rate, String serviceCharge) {
        Map<String, String> map = new TreeMap<>();
        map.put("orderCode", orderCode);
        map.put("amount", amount);
        map.put("realAmount", realAmount);
        map.put("userId", userId);
        map.put("rate", rate);
        map.put("serviceCharge", serviceCharge);
        Set<String> keySet = map.keySet();
        StringBuffer sb = new StringBuffer();
        for (String keyStr : keySet) {
            sb.append(keyStr + map.get(keyStr));
        }
        sb.append(key);
        System.out.println(sb.toString());
        String sign = Md5Util.getMD5(sb.toString());
        map.put("sign", sign);
        return JSONObject.fromObject(map).toString();
    }


    @Scheduled(cron = "0 0/1 * * * ?")
    public void scheduler() {
        if ("true".equals(scheduleTaskOnOff)) {
            List<NotifyOrder> notifyOrders = notifyOrderBusiness.findByNotifyTimeLessThan(new Date());
            for (NotifyOrder notifyOrder : notifyOrders) {
                LOG.info("回调订单==========" + notifyOrder);
                asyncMethod.sendNotifyOrder(notifyOrder);
            }
        }
    }


    public Map<String, Object> brandRebateRatio(PaymentOrder paymentOrder) {
        long brandId = paymentOrder.getBrandid();
        long userId = paymentOrder.getUserid();

        String url = "http://user/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", paymentOrder.getPhone());
        requestEntity.add("brandId", brandId + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject resultObj;
        String brandname;
        String grade;
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            grade = resultObj.getString("grade");
        } catch (Exception e) {
            LOG.error("根据手机号查询用户信息失败=============================" + e);
            return ResultWrap.init(CommonConstants.FALIED, "查询用户信息异常,请稍后重试!");
        }

        url = "http://user/v1.0/user/brand/query/id?brand_id=" + paymentOrder.getBrandid();
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        resultObj = jsonObject.getJSONObject("result");
        String manageId = resultObj.getString("manageid");

        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId + "");
        requestEntity.add("channelId", "1");
        Map<String, Object> restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/realtion/query/preuser", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = ((JSONObject) restTemplateDoPost.get(CommonConstants.RESULT)).getJSONArray(CommonConstants.RESULT);
            List<UserRealtion> userRealtions = new ArrayList<UserRealtion>();
            if (jsonArray.size() > 0) {
                UserRealtion preUserRealtion = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    Object object = jsonArray.get(i);
                    UserRealtion userRealtion = new UserRealtion();
                    try {
                        BeanUtils.copyProperties(userRealtion, object);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }

                    userRealtions.add(userRealtion);
                    LOG.info("用户上级为=================" + userRealtion);
                }

                BigDecimal setScale = BigDecimal.ZERO;
                Long preUserId = null;
                for (UserRealtion userRealtion : userRealtions) {
                    Integer preUserGrade = userRealtion.getPreUserGrade();
                    preUserId = userRealtion.getPreUserId();
                    if (preUserId != Long.parseLong(manageId)) {
                        if (preUserGrade >= Integer.parseInt(grade)) {
                            requestEntity = new LinkedMultiValueMap<String, String>();
                            requestEntity.add("brandId", brandId + "");
                            requestEntity.add("grade", preUserGrade + "");
                            restTemplateDoPost = restTemplateUtil.restTemplateDoPost("user", "/v1.0/user/brandrebateratio/getbybrandid/andgrade", requestEntity);
                            if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
                                JSONObject jsonObj = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
                                int status = jsonObj.getInt("status");
                                if (status == 1) {
                                    String ratio = jsonObj.getString("ratio");
                                    setScale = paymentOrder.getRealAmount().multiply(new BigDecimal(ratio)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    break;
                                }
                            }
                        }
                    }
                }
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("preUserId", preUserId);
                map.put("setScale", setScale);

                return ResultWrap.init(CommonConstants.SUCCESS, "执行成功!", map);
            }
        }

        return restTemplateDoPost;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/querybyjuniorand/{token}")
    public @ResponseBody
    Object pagePaymentQueryByUserIdAndJunior(HttpServletRequest request,
                                             @PathVariable("token") String token,
                                             // 开始时间
                                             @RequestParam(value = "startTime", required = false) String startTime,
                                             // 订单号
                                             @RequestParam(value = "orderCode", required = false) String orderCode,
                                             // 状态
                                             @RequestParam(value = "status", required = false) String status,
                                             // 结束时间
                                             @RequestParam(value = "endTime", required = false) String endTime,
                                             // **0 充值 1支付 2提现 3退款 */
                                             @RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                             @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                             @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                                             @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));

        String url = "http://user/v1.0/user/realtion/find/user/all/sonsuserid";
        /** 根据的用户手机号码查询用户的基本信息 */
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId + "");
        JSONObject postForObject = restTemplate.postForObject(url, requestEntity, JSONObject.class);
        LOG.info("postForObject================" + postForObject);
        if (CommonConstants.SUCCESS.equals(postForObject.getString(CommonConstants.RESP_CODE))) {
            JSONArray jsonArray = postForObject.getJSONArray(CommonConstants.RESULT);
            LOG.info("jsonArray================" + jsonArray);

            String[] str = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                str[i] = jsonArray.getInt(i) + "";
            }

            LOG.info("str====" + str);

            Date startDate = DateUtil.getDateStringConvert(new Date(), startTime, "yyyy-MM-dd");
            Date endDate = DateUtil.getDateStringConvert(new Date(), endTime, "yyyy-MM-dd");

            Page<PaymentOrder> queryPaymentOrderByUserIdsAndMore = paymentOrderBusiness.queryPaymentOrderByUserIdsAndMore(str, type, status, startDate, endDate, pageAble);

            LOG.info("queryPaymentOrderByUserIdsAndMore====" + queryPaymentOrderByUserIdsAndMore);


            return ResultWrap.init(CommonConstants.SUCCESS, "", queryPaymentOrderByUserIdsAndMore);
        } else {

            return postForObject;
        }

    }

    /**
     * 统计用户下级交易金额
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/getlowerleveltradingvolume")
    public @ResponseBody
    Object getLowerLevelTradingVolume(int userId,
                                      @RequestParam(value = "level", required = false, defaultValue = "0") String level,
                                      @RequestParam(value = "startTime", required = false) String startTime,
                                      @RequestParam(value = "endTime", required = false) String endTime) {

        Map<String, Object> sumMap = new HashMap<String, Object>();
        String type = "10";
        String status = "1";
        String type1 = "0";
        String[] str = type1.split(",");
        String[] str1 = type.split(",");
        String[] str2 = status.split(",");

        if (level.equals("0")) {
            {// 当天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果  long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate
                BigDecimal findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1", startTimeDate, endTimeDate);
                BigDecimal findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmount(userId, str, str2, "1", startTimeDate, endTimeDate);

                sumMap.put("todayReceivables", findsumPaymentOrderAmount != null ? findsumPaymentOrderAmount.setScale(2, ROUND_DOWN) : "0.00");
                sumMap.put("todayRepayment", findsumPaymentOrder != null ? findsumPaymentOrder.setScale(2, ROUND_DOWN) : "0.00");
            }
            {// 昨天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, -1);// 把日期往后增加一天.整数往后推,负数往前移动
                startTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
                calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
                BigDecimal findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1", startTimeDate, endTimeDate);

                BigDecimal findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmount(userId, str, str2, "1", startTimeDate, endTimeDate);
                sumMap.put("yesterdayReceivables", findsumPaymentOrderAmount != null ? findsumPaymentOrderAmount.setScale(2, ROUND_DOWN) : "0.00");
                sumMap.put("yesterdayRepayment", findsumPaymentOrder != null ? findsumPaymentOrder.setScale(2, ROUND_DOWN) : "0.00");
            }
            {// 所有
                Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
                Date endTimeDate = new Date();

                BigDecimal findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1",
                        startTimeDate, endTimeDate);
                findsumPaymentOrder = findsumPaymentOrder != null ? findsumPaymentOrder.setScale(2, ROUND_DOWN)
                        : BigDecimal.ZERO;

                BigDecimal findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmount(userId, str, str2,
                        "1", startTimeDate, endTimeDate);
                findsumPaymentOrderAmount = findsumPaymentOrderAmount != null ? findsumPaymentOrderAmount.setScale(2, ROUND_DOWN) : BigDecimal.ZERO;

                sumMap.put("all", findsumPaymentOrder.add(findsumPaymentOrderAmount));
            }
            LOG.info("查询分润统计：" + sumMap);

            /*
             * BigDecimal findsumPaymentOrder =
             * paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1");
             * LOG.info("findsumPaymentOrder======" + findsumPaymentOrder);
             */

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", sumMap);
        } else if (level.equals("-1")) {
            {// 当天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果  long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate
                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);
                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);

                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (a == null) {
                    sumMap.put("todayReceivables", am);
                }
                if (re == null) {
                    sumMap.put("todayRepayment", ra);
                } else {
                    sumMap.put("todayReceivables", a);
                    sumMap.put("todayRepayment", re);
                }
            }
            {// 昨天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, -1);// 把日期往后增加一天.整数往后推,负数往前移动
                startTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
                calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果

                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);

                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);

                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (a == null) {
                    sumMap.put("yesterdayReceivables", am);
                }
                if (re == null) {
                    sumMap.put("yesterdayRepayment", ra);
                } else {
                    sumMap.put("yesterdayReceivables", a);
                    sumMap.put("yesterdayRepayment", re);
                }
            }
            {// 所有
                Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
                Date endTimeDate = new Date();

                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);

                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);
                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (re == null) {
                    ra = 0.00;
                } else {
                    ra = Double.valueOf(re.toString());
                }
                if (a == null) {
                    am = 0.00;
                } else {
                    am = Double.valueOf(a.toString());
                }

                Double all = ra + am;

                sumMap.put("all", all);
            }
            LOG.info("查询分润统计：" + sumMap);

            /*
             * BigDecimal findsumPaymentOrder =
             * paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1");
             * LOG.info("findsumPaymentOrder======" + findsumPaymentOrder);
             */

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", sumMap);


        } else {
            {// 当天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果  long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate
                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);
                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);

                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (a == null) {
                    sumMap.put("todayReceivables", am);
                }
                if (re == null) {
                    sumMap.put("todayRepayment", ra);
                } else {
                    sumMap.put("todayReceivables", a);
                    sumMap.put("todayRepayment", re);
                }
            }
            {// 昨天
                Date startTimeDate = null;
                if (startTime != null && !startTime.equalsIgnoreCase("")) {
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                } else {
                    startTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    startTimeDate = DateUtil.getDateFromStr(startTime);
                }
                Date endTimeDate = null;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, -1);// 把日期往后增加一天.整数往后推,负数往前移动
                startTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果
                calendar = new GregorianCalendar();
                calendar.setTime(startTimeDate);
                calendar.add(Calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
                endTimeDate = calendar.getTime(); // 这个时间就是日期往后推一天的结果

                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);

                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);

                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (a == null) {
                    sumMap.put("yesterdayReceivables", am);
                }
                if (re == null) {
                    sumMap.put("yesterdayRepayment", ra);
                } else {
                    sumMap.put("yesterdayReceivables", a);
                    sumMap.put("yesterdayRepayment", re);
                }
            }
            {// 所有
                Date startTimeDate = DateUtil.getDateFromStr("2017-05-01");
                Date endTimeDate = new Date();

                Map<String, Object> findsumPaymentOrder = paymentOrderBusiness.findsumPaymentOrderByUserIdAndLevel(userId, str1, str2, "1", level, startTimeDate, endTimeDate);

                Map<String, Object> findsumPaymentOrderAmount = paymentOrderBusiness.findsumPaymentOrderAmountByUserIdAndLevel(userId, str, str2, "1", level, startTimeDate, endTimeDate);
                Double ra = 0.00;
                Double am = 0.00;

                Object re = findsumPaymentOrder.get("ra");
                System.out.println(re);
                Object a = findsumPaymentOrderAmount.get("a");
                System.out.println(re);

                if (re == null) {
                    ra = 0.00;
                } else {
                    ra = Double.valueOf(re.toString());
                }

                if (a == null) {
                    am = 0.00;
                } else {
                    am = Double.valueOf(a.toString());
                }
                ra = Double.valueOf(re.toString());
                am = Double.valueOf(a.toString());
                Double all = ra + am;

                sumMap.put("all", all);
            }
            LOG.info("查询分润统计：" + sumMap);

            /*
             * BigDecimal findsumPaymentOrder =
             * paymentOrderBusiness.findsumPaymentOrder(userId, str1, str2, "1");
             * LOG.info("findsumPaymentOrder======" + findsumPaymentOrder);
             */

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", sumMap);
        }
    }


//    /*
//     *@description:管理奖的判定逻辑。
//     *@author: ives
//     *@annotation:"写这个方法并不是我的本意，如果有什么问题，不要找我"
//     *@data:2019年9月5日 09:30:32
//     */
//    public void award(String userId,String amount,String orderCode) {
//        String url="http://user/v1.0/transactionclear/payment/update/remark";
//        MultiValueMap<String ,String> multiValueMap=new LinkedMultiValueMap<>();
//        multiValueMap.add("userId",userId);
//        multiValueMap.add("amount",amount);
//        multiValueMap.add("orderCode",orderCode);
//        String result = restTemplate.postForObject(url, multiValueMap, String.class);
//
//    }


    /*
     *@description:用户要求的发放积分，并且积分可控（只有消费订单发放积分，快捷不发积分）
     *@author: ives
     *@annotation:"这个需求是我大哥的,他太懒了不想写,扔给了我,你说说这是人干的事吗"
     *@data:2019年9月5日 09:30:32
     */
    public Object grantCoin(String ordercode, String type, long userId, long brandId, BigDecimal amount, BigDecimal realAmount) {
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        String url = "http://user/v1.0/user/query/id";
        requestEntity.add("id", userId + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        String grade1 = null;
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(result);
        com.alibaba.fastjson.JSONObject resultObj = jsonObject.getJSONObject("result");
        if (jsonObject.getString("result") == null) {
            return null;
        }
        grade1 = resultObj.getString("grade");
        if ("".equals(grade1) || grade1 == null) {
            return null;
        }
        MultiValueMap<String, Object> requestEntity1 = new LinkedMultiValueMap<String, Object>();
        BigDecimal ration = BigDecimal.ZERO;
        if ("11".equals(type) || "10".equals(type) || "0".equals(type)) {
            String url1 = "http://user/v1.0/user/coin/config/query";
            requestEntity1.add("brandId", brandId + "");
            requestEntity1.add("grade", grade1);
            requestEntity1.add("status", "1");
            String result1 = restTemplate.postForObject(url1, requestEntity1, String.class);
            com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSONObject.parseObject(result1);
            com.alibaba.fastjson.JSONObject resultObj1 = jsonObject1.getJSONObject("result");
            if (jsonObject1.getString("result") == null) {
                return null;
            }
            String ratios = resultObj1.getString("ratio");
            ration = new BigDecimal(ratios);

        }
        //blance为后面需要用到的手续费
        BigDecimal blance = BigDecimal.ZERO;
        BigDecimal coin = BigDecimal.ZERO;
        if ("11".equals(type)) {
            return null;
        } else if ("10".equals(type)) {
            blance = realAmount.subtract(amount).subtract(new BigDecimal("1.00"));
            if (blance.add(new BigDecimal("1.00")).compareTo(new BigDecimal("0.00")) == 0) {
                return null;
            }
            coin = blance.multiply(ration).setScale(1, ROUND_DOWN);
        }
//        else if ("0".equals(type)) {
//            blance = amount.subtract(realAmount);
//            coin = blance.multiply(ration).setScale(1, ROUND_DOWN);
//        }
//        else if (type.equals("1")) {
//            //如果是购买产品的话，需要判定数据库中表里的数据，是否反积分。
//            String url2 = "http://user/v1.0/user/coin/query/brandcoinconfig/select";
//            MultiValueMap<String, Object>  requestEntity2 = new LinkedMultiValueMap<String, Object>();
//           requestEntity2.add("user_id", userId + "");
//            String result3 = restTemplate.postForObject(url2, requestEntity2, String.class);
//            com.alibaba.fastjson.JSONObject  jsonObject3 = com.alibaba.fastjson.JSONObject .parseObject(result3);
//            if (jsonObject3.getString("result")==null) {
//                return null;
//            }
//            com.alibaba.fastjson.JSONObject resultObj3=jsonObject3.getJSONObject("result");
//            String status = resultObj3.getString("status");
//            String ratioType=resultObj3.getString("type");
//            BigDecimal coinType=new BigDecimal(ratioType);
//            if ("1".equals(status)) {
//                coin = amount.multiply(new BigDecimal("1.0").subtract(coinType)).setScale(1, ROUND_DOWN);
//                if(new BigDecimal("1.0").subtract(coinType).compareTo(new BigDecimal("0.0"))>0){
//                    url = "http://user/v1.0/user/coin/update/preuserid/new";
//                    requestEntity1 = new LinkedMultiValueMap<String, Object>();
//                    requestEntity1.add("user_id", userId + "");
//                    requestEntity1.add("order_code", ordercode);
//                    requestEntity1.add("coin", coin.toString());
//                    result = restTemplate.postForObject(url, requestEntity1, String.class);
//                }
//            } else {
//                return null;
//            }
//            coin=amount.multiply(coinType).setScale(1,ROUND_DOWN);
//        }
        url = "http://user/v1.0/user/coin/update/useridnew";
        requestEntity1 = new LinkedMultiValueMap<String, Object>();
        requestEntity1.add("user_id", userId + "");
        requestEntity1.add("coin", coin.toString());
        requestEntity1.add("order_code", ordercode);
        result = restTemplate.postForObject(url, requestEntity1, String.class);
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/financial/supermarket/update")
    @ResponseBody
    public Object updateOrderStatus(
            @RequestParam(value = "ordercode") String ordercode
    ) {
        Map map = new HashMap<>();
        return map;
    }
}
