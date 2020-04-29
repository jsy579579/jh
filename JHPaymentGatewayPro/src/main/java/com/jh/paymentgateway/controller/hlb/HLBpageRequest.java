package com.jh.paymentgateway.controller.hlb;

import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;

import com.jh.paymentgateway.pojo.hlb.*;

import com.jh.paymentgateway.util.PaymentChannelConstants;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hlb.Disguiser;
import com.jh.paymentgateway.util.hlb.HttpClientService;
import com.jh.paymentgateway.util.hlb.MyBeanUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @title: HLBpageRequest
 * @projectName: juhepayment
 * @description: TODO
 * @author: huhao
 * @date: 2019/10/2 16:27
 */
@SuppressWarnings("ALL")
@RestController
public class HLBpageRequest {
    private static final Logger logger = LoggerFactory.getLogger(HLBpageRequest.class);

    @Value("${hlb.url}")
    private String url;

    @Value("${hlb.customerNumber}")
    private String customerNumber;

    private String ipAddress = "http://www.shanqi111.cn";

    @Value("${hlb.orderIp}")
    private String orderIp;

    @Autowired
    private MyBeanUtils myBeanUtils;

    @Autowired
    private Util util;

    /**
     * 下单接口
     * @param P2_orderId
     * @param P5_orderAmount
     * @param P12_goodsName
     * @param P13_goodsDetail
     * @param P14_desc
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/createOrder",method = RequestMethod.POST)
    public Object createOrder(@RequestParam("P2_orderId")String P2_orderId,
                              @RequestParam("P5_orderAmount")String P5_orderAmount,
                              @RequestParam("P12_goodsName")String P12_goodsName,
                              @RequestParam("P13_goodsDetail")String P13_goodsDetail,
                              @RequestParam("P14_desc")String P14_desc,
                              @RequestParam("channelTag")String channelTag) {
        logger.info("--------进入主被扫创建订单接口----------");
        logger.info("请求参数：--------P2_orderId:" + P2_orderId + "-----------P5_orderAmount:" + P5_orderAmount + "-----------P12_goodsName:" +
                P12_goodsName + "-------------P13_goodsDetail:" + P13_goodsDetail + "----------P14_desc:" + P14_desc + "----------channelTag" + channelTag);
        Map<String, Object> map = new HashMap<>();
        AppCreateOrderVo orderVo = new AppCreateOrderVo();
        orderVo.setP2_orderId(P2_orderId);
        orderVo.setP5_orderAmount(P5_orderAmount);
        orderVo.setP12_goodsName(P12_goodsName);
        orderVo.setP13_goodsDetail(P13_goodsDetail);
        orderVo.setP14_desc(P14_desc);
        orderVo.setP1_bizType("AppPay");
        orderVo.setP3_customerNumber(customerNumber);
        orderVo.setP4_payType("SCAN");
        orderVo.setP6_currency("CNY");
        orderVo.setP7_authcode("1");
        if (PaymentChannelConstants.CHANNEL_HLB.equals(channelTag)){
            orderVo.setP8_appType("ALIPAY");
        }else if(PaymentChannelConstants.CHANNEL_YL_HLB.equals(channelTag)){
            orderVo.setP8_appType("UNIONPAY");
        }
        orderVo.setP9_notifyUrl(ipAddress + "/v1.0/paymentgateway/topup/hlb/notify");
        orderVo.setP10_successToUrl("");
        orderVo.setP11_orderIp(orderIp);
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(orderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, new String[]{"P15_subMerchantId","P16_appId", "P17_limitCreditPay", "P18_goodsTag", "P19_guid","P20_marketingRule","P21_identity","hbfqNum","deviceInfo"});
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + requestMap);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap, url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                AppCreateOrderResponseVo orderResponseVo = JSONObject.parseObject(resultMsg, AppCreateOrderResponseVo.class);
                String[] excludes = {"rt3_retMsg", "rt12_openId", "rt13_orderStatus", "rt14_fundBillList", "rt15_channelRetCode", "rt16_outTransactionOrderId",
                        "rt17_bankType", "rt18_subOpenId"};
                String assemblyRespOriSign = myBeanUtils.getSigned(orderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = orderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0000".equals(orderResponseVo.getRt2_retCode())) {
                        String rt8_qrcode = orderResponseVo.getRt8_qrcode();
                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE,"下单成功");
                        map.put(CommonConstants.RESULT,rt8_qrcode);
                        return map;
                    } else {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, orderResponseVo.getRt5_orderId()+"-"+orderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:"+resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "交易异常:" + e.getMessage());
        }
        return map;
    }

    /**
     * 支付回调
     * @param notifyResponseVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/notify",method = RequestMethod.POST)
    public String notify(NotifyResponseVo notifyResponseVo) {
        try {
            String assemblyRespOriSign = myBeanUtils.getSigned(notifyResponseVo, null);
            logger.info("组装返回结果签名串：" + assemblyRespOriSign);
            String responseSign = notifyResponseVo.getSign();
            logger.info("响应签名：" + responseSign);
            String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
            logger.info("验证签名：" + checkSign);
            if (checkSign.equals(responseSign)) {
                // 验证签名成功()
                // 商户根据根据支付结果做业务处理

                //
                if ("SUCCESS".equals(notifyResponseVo.getRt4_status())){
                    logger.info("异步回调订单已成功======");
                    RestTemplate restTemplate=new RestTemplate();
                    URI uri = util.getServiceUrl("transactionclear", "error url request!");
                    String url = uri.toString() +  "/v1.0/transactionclear/payment/update";
                    /**根据的用户手机号码查询用户的基本信息*/
                    MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("status", "1");
                    requestEntity.add("order_code", notifyResponseVo.getRt2_orderId());
                    String result = restTemplate.postForObject(url, requestEntity, String.class);
                    logger.info("更新订单成功：==========" + result);

                    restTemplate=new RestTemplate();
                    uri = util.getServiceUrl("good", "error url request!");
                    url = uri.toString() +  "/v1.0/good/order/update";
                    requestEntity  = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("status", "2");
                    requestEntity.add("order_code", notifyResponseVo.getRt2_orderId());
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    logger.info("更新商城订单成功：==========" + result);

                }
                return "success";// 反馈处理结果
            } else {
                return "fail 验证签名失败";
            }
        } catch (Exception e) {
            return "fail 系统内部错误" + e.getMessage();// 反馈处理结果
        }

    }

    /**
     * 交易订单查询接口
     * @param queryOrderVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/query",method = RequestMethod.POST)
    public Object queryOrder(QueryOrderVo queryOrderVo) {
        logger.info("--------进入交易订单查询接口----------");
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(queryOrderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, new String[]{"P4_serialNumber"});
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + map);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap, url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                QueryOrderResponseVo queryOrderResponseVo = JSONObject.parseObject(resultMsg, QueryOrderResponseVo.class);
                String[] excludes = {"rt3_retMsg", "rt10_desc", "rt11_openId", "rt12_channelOrderNum", "rt13_orderCompleteDate",
                        "rt14_cashFee", "rt15_couponFee", "rt16_onlineCardType", "rt17_fundBillList", "rt18_outTransactionOrderId",
                        "rt19_bankType", "rt20_subOpenId", "P21_identity"};
                String assemblyRespOriSign = myBeanUtils.getSigned(queryOrderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = queryOrderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0000".equals(queryOrderResponseVo.getRt2_retCode())) {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "订单查询成功");
                        map.put(CommonConstants.RESULT,queryOrderResponseVo);
                    } else {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, queryOrderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:"+resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"请求失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"请求失败:"+ e.getMessage());
        }
        return map;
    }

    /**
     * 订单退款接口
     * @param orderVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/refund",method = RequestMethod.POST)
    public Object refund(AppPayRefundOrderVo orderVo) {
        logger.info("--------进入订单退款请求接口----------");
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(orderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, new String[]{"P7_desc","P8_orderSerialNumber"});
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + map);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap, url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                AppPayRefundOrderResponseVo orderResponseVo = JSONObject.parseObject(resultMsg, AppPayRefundOrderResponseVo.class);
                String[] excludes = {"rt3_retMsg"};
                String assemblyRespOriSign = myBeanUtils.getSigned(orderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = orderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0001".equals(orderResponseVo.getRt2_retCode())) {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "退款请求成功");
                        map.put(CommonConstants.RESULT,resultMsg);
                    } else {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, orderResponseVo.getRt5_orderId()+"-"+orderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:"+resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "交易异常" + e.getMessage());
        }
        return map;
    }

    /**
     * 退款查询接口
     * @param orderVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/refundQuery",method = RequestMethod.POST)
    public Object toRefundQuery(AppPayRefundQueryVo orderVo) {
        logger.info("--------进入订单退款请求接口----------");
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(orderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, new String[]{"P4_serialNumber"});
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + requestMap);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap, url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                AppPayRefundQueryResponseVo orderResponseVo = JSONObject.parseObject(resultMsg, AppPayRefundQueryResponseVo.class);
                String[] excludes = {"rt3_retMsg","rt15_appPayType","rt11_refundOrderCompleteDate","rt12_refundChannelOrderNum","rt13_desc",
                        "rt14_refundOrderAttribute"};

                String assemblyRespOriSign = myBeanUtils.getSigned(orderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = orderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0000".equals(orderResponseVo.getRt2_retCode())) {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "退款查询成功");
                        map.put(CommonConstants.RESULT,resultMsg);
                    } else {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, orderResponseVo.getRt5_orderId()+"-"+orderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:"+resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "交易异常" + e.getMessage());
        }
        return map;
    }

    /**
     * 退款回调
     * @param refundNotifyResponseVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/refund/notify",method = RequestMethod.POST)
    public String notify(RefundNotifyResponseVo refundNotifyResponseVo) {
        try {
            String assemblyRespOriSign = myBeanUtils.getSigned(refundNotifyResponseVo, null);
            logger.info("组装返回结果签名串：" + assemblyRespOriSign);
            String responseSign = refundNotifyResponseVo.getSign();
            logger.info("响应签名：" + responseSign);
            String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
            logger.info("验证签名：" + checkSign);
            if (checkSign.equals(responseSign)) {
                // 验证签名成功()
                // 商户根据根据支付结果做业务处理

                //
                return "success";// 反馈处理结果
            } else {
                return "fail 验证签名失败";
            }
        } catch (Exception e) {
            return "fail 系统内部错误" + e.getMessage();// 反馈处理结果
        }

    }

    /**
     * 关闭订单接口
     * @param orderVo
     * @return
     */
    @RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/close",method = RequestMethod.POST)
    public Object close(AppPayCloseOrderVo orderVo) {
        logger.info("--------进入关闭订单接口----------");
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(orderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, null);
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + requestMap);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap, url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                AppPayCloseResponseVo orderResponseVo = JSONObject.parseObject(resultMsg, AppPayCloseResponseVo.class);
                String[] excludes = {"rt3_retMsg"};
                String assemblyRespOriSign = myBeanUtils.getSigned(orderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = orderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0000".equals(orderResponseVo.getRt2_retCode())) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "订单关闭成功");
                        map.put(CommonConstants.RESULT, resultMsg);
                    } else {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, orderResponseVo.getRt5_orderId() + "-" + orderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:" + resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "交易异常" + e.getMessage());
        }
        return map;
    }

    /**
     * 微信公众号支付
     * @param orderVo
     * @return
     *//*@RequestMapping(value = "/v1.0/paymentgateway/topup/hlb/publicCreateOrder")
    public Object publicCreateOrder(HttpServletRequest request, HttpServletResponse response, Model model,AppPayPublicCreateOrderVo orderVo) {
        logger.info("--------进入公众号/服务窗建订单接口----------");
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, String> requestMap = myBeanUtils.convertBean(orderVo, new LinkedHashMap());
            String oriMessage = myBeanUtils.getSigned(requestMap, new String[]{"P19_subscribeAppId", "P21_goodsTag", "P22_guid","P23_marketingRule","P24_identity","splitBillType","ruleJson","timeExpire"});
            logger.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            logger.info("签名串：" + sign);
            requestMap.put("sign", sign);
            logger.info("发送参数：" + requestMap);
            Map<String, Object> resultMap = HttpClientService.getHttpResp(requestMap ,url);
            logger.info("响应结果：" + resultMap);
            if ((Integer) resultMap.get("statusCode") == HttpStatus.SC_OK) {
                String resultMsg = (String) resultMap.get("response");
                AppPayPublicOrderResponseVo orderResponseVo = JSONObject.parseObject(resultMsg, AppPayPublicOrderResponseVo.class);
                //建议是加签/验签的固定参数,具体按照文档要求加签.因为新增参数需要历史兼容是排除签名的
                String[] excludes = {"rt3_retMsg", "rt13_channelRetCode","rt14_appPayType"};
                String assemblyRespOriSign = myBeanUtils.getSigned(orderResponseVo, excludes);
                logger.info("组装返回结果签名串：" + assemblyRespOriSign);
                String responseSign = orderResponseVo.getSign();
                logger.info("响应签名：" + responseSign);
                String checkSign = Disguiser.disguiseMD5(assemblyRespOriSign.trim());
                if (checkSign.equals(responseSign)) {
                    if ("0000".equals(orderResponseVo.getRt2_retCode())) {
                        String rt10_payInfo = orderResponseVo.getRt10_payInfo();
                        logger.info("返回的支付详情：" + rt10_payInfo);
                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE,"下单成功");
                        map.put(CommonConstants.RESULT,rt10_payInfo);
                    } else {
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, orderResponseVo.getRt5_orderId()+"-"+orderResponseVo.getRt3_retMsg());
                    }
                } else {
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "验签失败:"+resultMsg);
                }
            } else {
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "交易异常:" + e.getMessage());
        }
        return map;
    }*/
}
