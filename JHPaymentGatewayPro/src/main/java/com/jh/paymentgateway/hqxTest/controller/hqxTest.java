package com.jh.paymentgateway.hqxTest.controller;

import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;

import com.jh.paymentgateway.controller.HQXpageRequest;
import com.jh.paymentgateway.pojo.hq.HQNEWBindCard;
import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import com.jh.paymentgateway.util.HttpUtil;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hqx.HashMapConver;
import com.jh.paymentgateway.util.hqx.SmartRepayChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class hqxTest {


    //日志
    private static final Logger log = LoggerFactory.getLogger(hqxTest.class);

    @Autowired
    JdbcTemplate jdbcTemplate;


    @Autowired
    Util utl;


    /**
     * 商户余额查询
     * idCard : 身份证
     * bankCard ： 银行卡
     * subMerchantNo : 子商户号
     * bindId : 绑卡标识
     *
     * @return
     * @throws Exception
     */


    public Object balanceQuery(Map parmap) throws Exception {
        log.info("开始进入商户余额查询========================" + parmap);
        Map<String, Object> map = new HashMap<>();
        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "queryBalance");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String dsorderid = format.format(date);
        request.put("dsorderid", dsorderid); // 商户订单号
        request.put("subMerchantNo", parmap.get("subMerchantNo").toString());// 子商户号
        request.put("bindId", parmap.get("bindId").toString());// 绑卡标识
        Map<String, Object> resultMap = smart.allRequestMethodquery(request);
        log.info("resultMap = >" + resultMap);
        if (resultMap.get("returncode").toString().equals("0000")) {
            if (!resultMap.get("currAccountBalance").toString().equals("0")) {
                if (Double.valueOf(resultMap.get("currAccountBalance").toString()) - 1 > 1) {
                    map.put("currAccountBalance", Double.valueOf(resultMap.get("currAccountBalance").toString()) - 1);
                    map.put("code", "0");
                } else {
                    map.put("code", "1");
                }
            } else {
                map.put("code", "1");
            }
        } else {
            map.put("code", "1");
        }

//        java.lang.String code = resultMap.get("returncode").toString();
//        java.lang.String frozenamount = resultMap.get("frozenamount").toString();// 商户冻结余额
//        java.lang.String currAccountBalance = resultMap.get("currAccountBalance").toString();// 当前可用余额
//        String errtext = resultMap.get("errtext").toString();
//        if ("0000".equals(code)) {
//            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
//            map.put(CommonConstants.RESP_MESSAGE, "商户冻结余额" + frozenamount + "当前可用余额" + currAccountBalance);
//            log.info("balanceQuery  返回查询结果 map==>" + map);
//            return map;
//        } else {
//            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//            map.put(CommonConstants.RESP_MESSAGE, errtext);
        return map;
        //}

    }


    //
    @RequestMapping("/v1.0/paymentgateway/hq/hqxid")
    @ResponseBody
    public void hqxa() throws Exception {
        //  查询用户的信息
        Map<String, Object> df = new HashMap<>();
        String sql = "select * from hqx_de";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        RestTemplate restTemplate = new RestTemplate();
        for (int i = 0; i < maps.size(); i++) {
//            Map map1 = new HashMap();
            Map<String, Object> stringObjectMap = maps.get(i);
            String idCard = "'" + stringObjectMap.get("idCard").toString() + "'";
            //String sqlStr = "select * from t_hqx_register where idcard =" + idCard;
            String sqlStr = "select * from t_hqnew_register where id_card =" + idCard;

            System.out.println(sqlStr);
            Map<String, Object> hqxMap = jdbcTemplate.queryForMap(sqlStr);
            String merchant_code = hqxMap.get("merchant_code").toString();  // 子商户号
            Map<String, Object> restMap = new HashMap<>();
            restMap.put("idCard", hqxMap.get("id_card"));  // 身份证
            restMap.put("bankCard", hqxMap.get("bank_card")); // 银行卡
            restMap.put("subMerchantNo", merchant_code);  // 子商户号
            restMap.put("bindId", stringObjectMap.get("merzId")); // ID
            System.out.println("restMap");
            //  小额 hqx
            //Map o = (Map) balanceQuery1(restMap);
            //  大额 hqx_de
            Map o = (Map) balanceQuery1(restMap);
            if (o.get("code").toString().equals("0")) {
                // 开始代还
                restMap.put("bigRealAmount", Double.valueOf(o.get("currAccountBalance").toString())); //金额
                String order = String.valueOf(stringObjectMap.get("merOrderId"));
                // 查询version order_code user_id
                URI uri = utl.getServiceUrl("creditcardmanager", "error url request!");
                System.out.println(uri.toString());
                String url = uri.toString() + "/v1.0/creditcardmanager/open/keke12/rest";
                MultiValueMap<String, String> map1 = new LinkedMultiValueMap();
                map1.add("order_code", order);
                Map map = (Map) restTemplate.postForObject(url, map1, Object.class);
                System.out.println("map=" + map);
                map.put("bigRealAmount", restMap.get("bigRealAmount"));
                // 修改余额
                URI uri1 = utl.getServiceUrl("creditcardmanager", "error url request!");
                String url1 = uri1.toString() + "/v1.0/creditcardmanager/open/keke12/upd";
                map1.clear();
                map1.setAll(map);
                map1.add("bigRealAmount", restMap.get("bigRealAmount").toString());
                Map map2 = (Map) restTemplate.postForObject(url1, map1, Object.class);
                //
                // 修改余额
                URI uri2 = utl.getServiceUrl("creditcardmanager", "error url request!");
                String url2 = uri2.toString() + "/v1.0/creditcardmanager/clear/by/userid/creditcardnumber";
                map1.add("userId", map.get("user_id").toString());
                map1.add("creditCardNumber", map.get("credit_card_number").toString());
                //map.put("version",map.get("user_id"));
                Map map3 = (Map) restTemplate.postForObject(url2, map1, Object.class);
                System.out.println("map3=>" + map3);
            }
        }

    }


    /**
     * 查询商户余额
     *
     * @return
     */
    public Object balanceQuery1(@RequestParam Map map) throws IOException {
        log.info("开始余额===================");

//        Map<String, Object> map = new HashMap<>();
//        HQNEWBindCard hqBindCard = topupPayChannelBusiness.getHQNEWBindCardByBankCard(bankCard);
//        HQNEWRegister hqnewRegister = topupPayChannelBusiness.getHQNEWRegisterByIdCard(idCard);
//        String merchantCode = hqnewRegister.getMerchantCode();
//        String brandId = hqBindCard.getBindId();

        com.jh.paymentgateway.util.utils.SmartRepayChannel smart = new com.jh.paymentgateway.util.utils.SmartRepayChannel();

        Map<String, String> request = com.jh.paymentgateway.util.utils.HashMapConver.getOrderByMap();
        request.put("methodname", "queryBalance");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = format.format(date);
        String dsorderid = "xt" + ordersn;
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", dsorderid); // 商户订单号
        request.put("subMerchantNo", map.get("subMerchantNo").toString());// 子商户号
        request.put("bindId", map.get("bindId").toString());// 绑卡标识
        Map<String, String> resultMap = smart.allRequestMethod(request);
        String code = resultMap.get("returncode").toString();
        Map<String, Object> map1 = new HashMap<>();

        map1.putAll(resultMap);


        if (map1.get("returncode").toString().equals("0000")) {
            if (!String.valueOf(map1.get("currAccountBalance")).equals("0")) {
                if (Double.valueOf(String.valueOf(map1.get("currAccountBalance"))) - 1 > 1) {
                    map.put("currAccountBalance", Double.valueOf(map1.get("currAccountBalance").toString()) - 1);
                    map.put("code", "0");
                } else {
                    map.put("code", "1");
                }
            } else {
                map.put("code", "1");
            }
        } else {
            map.put("code", "1");
        }
        return map;
    }


    //
    @RequestMapping("/v1.0/paymentgateway/hq/hqxis_s")
    @ResponseBody
    public void hqx() throws Exception {
        //  查询用户的信息
        Map<String, Object> df = new HashMap<>();
        String sql = "select * from hqx";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        for (int i = 0; i < maps.size(); i++) {
            Map<String, Object> stringObjectMap = maps.get(i);
            String idCard = "'" + stringObjectMap.get("idCard").toString() + "'";
            String sqlStr = "select * from t_hqx_register where idcard =" + idCard;
            System.out.println(sqlStr);
            Map<String, Object> hqxMap = jdbcTemplate.queryForMap(sqlStr);
            String merchant_code = hqxMap.get("merchant_code").toString();  // 子商户号
            Map<String, Object> restMap = new HashMap<>();
            restMap.put("idCard", stringObjectMap.get("idCard"));  // 身份证
            restMap.put("bankCard", stringObjectMap.get("bardId")); // 银行卡
            restMap.put("subMerchantNo", merchant_code);  // 子商户号
            restMap.put("bindId", stringObjectMap.get("merzId")); // ID

            System.out.println("restMap");
            Map o = (Map) balanceQuery(restMap);
            if (o.get("code").toString().equals("0")) {
                // 开始代还
                restMap.put("bigRealAmount", Double.valueOf(o.get("currAccountBalance").toString())); //金额
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                String format1 = format.format(date) + "asdfghjk";
                restMap.put("orderCode", format1); // 商户订单号
                log.info("restMap" + restMap);
                Map transfer = (Map) transfer(restMap);
                if (transfer.get("resp_code").toString().equals("000000")) {
                    // 代还成功
                    // 插入数据
                    String insret = "insert into hqx_id (orderId,merId,bankCard,idCard,state,money,orderIds) values (?,?,?,?,?) ";
                    int update = jdbcTemplate.update(sql, stringObjectMap.get("orderId"), stringObjectMap.get("merOrderId"), stringObjectMap.get("bardId"), stringObjectMap.get("idCard"), transfer.get("errtext"),
                            restMap.get("bigRealAmount").toString(), restMap.get("orderCode"));
                }
            }
            System.out.println(o);
        }

    }

//    @RequestMapping("/v1.0/paymentgateway/hqx/adf")
//    @ResponseBody
//    public void a(@RequestParam Map<String, Object> map) {
//        Map<String, Object> restMap = new HashMap<>();
//        restMap.put("orderCode", ""); // 商户订单号
//        restMap.put("subMerchantNo", "");  // 子商户号
//        restMap.put("bindId", "");   // 绑卡标识 bindCard 返回
//        restMap.put("bankCard", "");  // 银行卡号 信用卡
//        restMap.put("bigRealAmount", ""); // 金额 （分） -1
//    }

    /**
     * 开始代付
     *
     * @return
     */
    public Object transfer(Map<String, Object> map) throws Exception {
        log.info("开始进入还款计划========================");
        SmartRepayChannel smart = new SmartRepayChannel();
        Map<String, String> request = HashMapConver.getOrderByMap();
        request.put("methodname", "withDraw");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ordersn = "xl" + format.format(date);
        request.put("ordersn", ordersn); // 流水号
        request.put("dsorderid", map.get("orderCode").toString()); // 商户订单号
        request.put("subMerchantNo", map.get("subMerchantNo").toString());// 子商户号
        request.put("bindId", map.get("bindId").toString()); // 绑卡标识 bindCard 返回
        request.put("bankcard", map.get("bankCard").toString());// 银行卡号 信用卡
        // request.put("userFee","");// 还款手续费 该字段不传则按入驻上传的还款手续费计算
        request.put("amount", map.get("bigRealAmount").toString());// 金额 （分）
        request.put("notifyUrl", "http://localhost" + "/v1.0/paymentgateway/topup/hqx/transfer/call_back"); // 异步通知地址
        log.info("/hqx/transfer================" + request.toString());
        Map<String, String> resultMap = smart.allRequestMethod(request);
        log.info("=============环球小额落地：" + resultMap.toString());
        String code = resultMap.get("returncode");// 放回码
        String errtext = resultMap.get("errtext");
        if ("0000".equals(code)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
            return map;

        } else if ("0003".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
            return map;
        } else if ("0002".equals(code)) {
            map.put(CommonConstants.RESP_CODE, "999998");
            map.put(CommonConstants.RESP_MESSAGE, "支付状态异常，等待查询");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, errtext);
            return map;
        }

    }


}
