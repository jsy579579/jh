package com.cardmanager.pro.service;



import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.pojo.RepaymentBill;



import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.pojo.CreditCardAccount;

import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class ReplacementOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ReplacementOrderService.class);

    @Autowired
    ConsumeTaskPOJOBusiness ConsumeTaskbusiness;

    @Autowired
    RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;

    @Autowired
    RepaymentBillBusiness repaymentBillBusiness;

    @Autowired
    CreditCardAccountBusiness creditCardAccountBusiness;




    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/change/ConsumeTask")
    public @ResponseBody
    Object changeConsumeTask(@RequestParam(value = "consume_task_id") String consumeTaskId,
                             @RequestParam(value = "amount", required = false) String amount,
                             @RequestParam(value = "real_amount", required = false) String realAmount,
                             @RequestParam(value = "task_status") Integer taskStatus,
                             @RequestParam(value = "order_status") Integer orderStatus) {
        // 根据消费计划consume_task_id获取计划详情
        ConsumeTaskPOJO consumeTaskPOJO = ConsumeTaskbusiness.findByConsumeTaskId(consumeTaskId);
        LOG.info("===========查询消费计划详情：" + JSONObject.fromObject(consumeTaskPOJO));
        // 更新计划执行状态
        consumeTaskPOJO.setTaskStatus(taskStatus);
        consumeTaskPOJO.setOrderStatus(orderStatus);
       /* String createTime = consumeTaskPOJO.getCreateTime();
        RepaymentBill repaymentBill = repaymentBillBusiness.queryTaskAmountByCreateTime(createTime);
        //还款金额=taskamount-消费计划金额
        if (7==taskStatus){
            BigDecimal amount1 = repaymentBill.getTaskAmount().subtract(new BigDecimal(amount));
            repaymentBill.setTaskAmount(amount1);
            repaymentBill.setTaskCount(repaymentBill.getTaskCount()-1);
            repaymentBillBusiness.save(repaymentBill);
            LOG.info("==============保存的还款计划:"+repaymentBill+"==========");
        }*/
        ConsumeTaskPOJO consumeTaskPOJO_update = null;
        try {
            consumeTaskPOJO_update = ConsumeTaskbusiness.save(consumeTaskPOJO);
            LOG.info("===========返回消费计划详情：" + JSONObject.fromObject(consumeTaskPOJO_update));
            LOG.info("===消费计划详情：" + consumeTaskId + "===计划状态：" + consumeTaskPOJO_update.getTaskStatus() + "===订单状态："
                    + consumeTaskPOJO_update.getOrderStatus() + "=====修改成功！");
            return ResultWrap.init(CommonConstants.SUCCESS, "删除消费计划成功",
                    JSONObject.fromObject(consumeTaskPOJO_update));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("===消费计划详情：" + consumeTaskId + "===计划状态：" + taskStatus + "===订单状态：" + orderStatus + "====修改失败！");
        }
        return ResultWrap.init(CommonConstants.FALIED, "删除消费计划失败");

    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/change/RepaymentTask")
    public @ResponseBody
    Object changeRepaymentTask(@RequestParam(value = "repayment_task_id") String repaymentTaskId,
                               @RequestParam(value = "amount", required = false) String amount,
                               @RequestParam(value = "real_amount", required = false) String realAmount,
                               @RequestParam(value = "task_status") Integer taskStatus,
                               @RequestParam(value = "order_status") Integer orderStatus) {
        // 根据消费计划consume_task_id获取计划详情
        RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
        LOG.info("===========查询还款计划详情：" + JSONObject.fromObject(repaymentTaskPOJO));
        // 更新计划执行状态
        int i = 1 + 1;
        System.out.println("==========" + i);
        RepaymentTaskPOJO repaymentTaskPOJO_update = null;
        List<ConsumeTaskPOJO> consumeTaskPOJOs = ConsumeTaskbusiness.findAllByRepayment(repaymentTaskId);
        String createTime = repaymentTaskPOJO.getCreateTime();
        if (consumeTaskPOJOs.size() > 0) {
            for (ConsumeTaskPOJO c : consumeTaskPOJOs) {
                if (c.getTaskStatus() == 2 || c.getTaskStatus() == 4 || c.getTaskStatus() == 0) {
                    if (taskStatus==7){
                        RepaymentBill repaymentBill = repaymentBillBusiness.queryTaskAmountByCreateTime(createTime);
                        LOG.info("获取创建时间："+createTime);
                        BigDecimal taskAmount = repaymentBill.getTaskAmount();
                        BigDecimal amount1 = c.getAmount();
                        BigDecimal subtract = taskAmount.subtract(amount1);
                        LOG.info("==================获取删除计划后的属性");
                        repaymentBill.setTotalServiceCharge(repaymentBill.getTotalServiceCharge().subtract(c.getServiceCharge()));
                        repaymentBill.setTaskCount(repaymentBill.getTaskCount()-1);
                        repaymentBill.setTaskAmount(subtract);

                        repaymentBillBusiness.save(repaymentBill);

                    }
                    repaymentTaskPOJO.setTaskStatus(taskStatus);
                    repaymentTaskPOJO.setOrderStatus(orderStatus);
                    repaymentTaskPOJO_update = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
                    changeConsumeTask(c.getConsumeTaskId(), null, null, 7, 0);


                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "消费成功的记录无法删除！");
                }
            }
            return ResultWrap.init(CommonConstants.SUCCESS, "删除还款计划成功",
                    JSONObject.fromObject(repaymentTaskPOJO_update));
        } else {
            if (repaymentTaskPOJO.getTaskStatus() != 1 && repaymentTaskPOJO.getTaskStatus() != 3) {
                repaymentTaskPOJO.setTaskStatus(taskStatus);
                repaymentTaskPOJO.setOrderStatus(orderStatus);
                //queryTaskAmountByCreateTime(repaymentTaskPOJO,createTime);
                repaymentTaskPOJO_update = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
                return ResultWrap.init(CommonConstants.SUCCESS, "删除还款计划成功",
                        JSONObject.fromObject(repaymentTaskPOJO_update));
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "当前订单无需删除！");
            }
        }

    }



    @RequestMapping(value = "/v1.0/creditcardmanager/change/RepaymentTask/All", method = RequestMethod.POST)
    @ResponseBody
    public Object AllRepaymentTask(@RequestParam(value = "repaymentIds") String[] repaymentIds) {
        if (repaymentIds.length > 0) {
            for (String r : repaymentIds) {
                changeRepaymentTask(r, null, null, 7, 0);
            }
            return ResultWrap.init(CommonConstants.SUCCESS, "删除还款计划成功");
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
    }


//    @RequestMapping(value="/v1.0/creditcardmanager/change/consumeandrepayment/all",method = RequestMethod.POST)
//    @ResponseBody
//    public void consumeandrepayment(@RequestParam(value = "repaymentId") String repaymentId){
//
//       ConsumeTaskbusiness.deleteAllByStatusAndReturnMessage(7);
//       repaymentTaskPOJOBusiness.deleteAllByStatusAndReturnMessage(7);
//
//    }


    //消费还款计划查询失败的
    @RequestMapping(value = "/v1.0/creditcardmanager/show/consumeandrepayment/phone", method = RequestMethod.POST)
    @ResponseBody
    public Object consumeAndRepaymentPhone(@RequestParam(value = "phone", required = false) String phone,
                                           @RequestParam(value = "brandId") String brandId,
                                           @RequestParam(value = "type") String type,
                                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                           @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        Map<String, Object> maps = new HashMap<>();
        if (!type.equals("11") && !type.equals("10")) {
            return ResultWrap.init(CommonConstants.FALIED, "参数为空");
        }
        if ("".equals(brandId)) {
            return ResultWrap.init(CommonConstants.FALIED, "贴牌ID为空");

        }
        String url = "http://user/v1.0/user/query/phone";
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        if (!"".equals(phone)) {
            String userId = null;
            try {
                MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
                requestEntity.add("phone", phone);
                requestEntity.add("brandId", brandId);
                String result = restTemplate.postForObject(url, requestEntity, String.class);
                JSONObject result1 = JSONObject.fromObject(result);
                JSONObject result2 = result1.getJSONObject("result");
                userId = result2.getString("id");
            } catch (RestClientException e) {
                e.printStackTrace();
                return ResultWrap.init(CommonConstants.FALIED, "未找到该用户");

            }
            if ("10".equals(type)) {
                List<ConsumeTaskPOJO> consumeTaskPOJOList = ConsumeTaskbusiness.findByUserIdAndBrandId(userId, brandId, pageable);
                for (ConsumeTaskPOJO c : consumeTaskPOJOList) {
                    c.setVersionName(creditCardManagerConfigBusiness.findByVersion(c.getVersion()).getChannelName());
                }

                maps.put("resp_message", "查询成功");
                maps.put("resp_code", "000000");
                maps.put("result", consumeTaskPOJOList);
                return maps;
            }
            if ("11".equals(type)) {
                List<RepaymentTaskPOJO> repaymentTaskPOJOList = repaymentTaskPOJOBusiness.findByUserIdAndBrandId(userId, brandId, pageable);

                for (RepaymentTaskPOJO c : repaymentTaskPOJOList) {
                    c.setChannelName(creditCardManagerConfigBusiness.findByVersion(c.getVersion()).getChannelName());
                }
                maps.put("resp_message", "查询成功");
                maps.put("resp_code", "000000");
                maps.put("result", repaymentTaskPOJOList);
                return maps;
            }

        } else {
            if ("10".equals(type)) {
                List<ConsumeTaskPOJO> consumeTaskPOJOList = ConsumeTaskbusiness.findByBrandId(brandId, pageable);

                for (ConsumeTaskPOJO c : consumeTaskPOJOList) {
                    c.setVersionName(creditCardManagerConfigBusiness.findByVersion(c.getVersion()).getChannelName());
                }
                maps.put("resp_message", "查询成功");
                maps.put("resp_code", "000000");
                maps.put("result", consumeTaskPOJOList);
                return maps;
            }
            if ("11".equals(type)) {
                List<RepaymentTaskPOJO> repaymentTaskPOJOList = repaymentTaskPOJOBusiness.findByBrandId(brandId, pageable);

                for (RepaymentTaskPOJO c : repaymentTaskPOJOList) {
                    c.setChannelName(creditCardManagerConfigBusiness.findByVersion(c.getVersion()).getChannelName());
                }

                maps.put("resp_message", "查询成功");
                maps.put("resp_code", "000000");
                maps.put("result", repaymentTaskPOJOList);
                return maps;
            }
        }
        return ResultWrap.init(CommonConstants.FALIED, "查询失败");
    }


    /**
     * 批量补单余额不足的订单
     * @param message
     * @param time
     * @return
     */
    @RequestMapping(value = "/v1.0/creditcardmanager/replacementorder/balance",method = RequestMethod.POST)
    @ResponseBody
    public Object replacementOrderByBalance(@RequestParam(value = "message",defaultValue = "余额不足",required = false)String message,
                                            @RequestParam(value = "time",required = false)String time) throws InterruptedException {
        // 例 ：2019-11-11
        if (time == null){
            time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
        List<RepaymentTaskPOJO> list = repaymentTaskPOJOBusiness.findByExecuteDateAndMessage(message,time);
        LOG.info("共有：{}笔余额不足的还款订单",list.size());
        if (list != null){
            String userId;
            String version;
            String creditCardNumber;
            String url;
            String orderCode;
            JSONObject jsonObject;
            int count = 0;
            MultiValueMap<String, Object> requestMap = null;
            String resultString ;
            String orderJson;
            JSONObject amountJSON;
            JSONObject jsonObject1;
            String idCard;
            CreditCardAccount cardAccount;
            for (RepaymentTaskPOJO taskPOJO : list) {
                userId = taskPOJO.getUserId();
                version = taskPOJO.getVersion();
                creditCardNumber = taskPOJO.getCreditCardNumber();
                orderCode = taskPOJO.getOrderCode();
                switch (version){
//                    //环球小额g
//                    case "18" :
//                        LOG.info("环球小额g=============================");
//                        url = "http://paymentgateway/v1.0/paymentgateway/orderparameter/get";
//                        requestMap =new LinkedMultiValueMap<>();
//                        requestMap.add("orderCode",orderCode);
//                        resultString = restTemplate.postForObject(url, requestMap, String.class);
//                        jsonObject = JSONObject.fromObject(resultString);
//                        orderJson = jsonObject.getString("orderJson");
//                        jsonObject1 = JSONObject.fromObject(orderJson);
//                        idCard = jsonObject1.getString("idCard");
//                        url = "http://paymentgateway/v1.0/paymentgateway/topup/hqx/balanceQuery1" ;
//                        requestMap =new LinkedMultiValueMap<>();
//                        requestMap.add("idCard",idCard);
//                        requestMap.add("bankCard",creditCardNumber);
//                        resultString = restTemplate.postForObject(url, requestMap, String.class);
//                        amountJSON = JSONObject.fromObject(resultString);
//                        String balance = amountJSON.getString("balance");
//                        BigDecimal amount = new BigDecimal(balance);
//                        if(amount.compareTo(BigDecimal.ONE) > 0){
//                            cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
//                            cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
//                        }else{
//                            cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
//                            cardAccount.setBlance(BigDecimal.ZERO);
//                        }
//                        creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
//                        ++ count;
//                        break;
//                        // 环迅小额
//                    case "31" :
//                        LOG.info("环迅小额=============================");
//                        url = "http://paymentgateway/v1.0/paymentgateway/topup/hxdhx/balancequery" ;
//                        requestMap =new LinkedMultiValueMap<>();
//                        requestMap.add("idCard",creditCardNumber);
//                        resultString = restTemplate.postForObject(url, requestMap, String.class);
//                        amountJSON = JSONObject.fromObject(resultString);
//                        if("000000".equals(amountJSON.getString("resp_code"))){
//                            String amounts = amountJSON.getString("resp_message");
//                            cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
//                            amount = new BigDecimal(amounts);
//                            if(amount.compareTo(BigDecimal.ONE) > 0){
//                                cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
//                            }else{
//                                cardAccount.setBlance(BigDecimal.ZERO);
//                            }
//                            creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
//                            ++ count;
//                        }
//                        break;
//                        //环迅大额
//                    case "50" :case "51" :case "52" :
//                        LOG.info("环迅大额=============================");
//                        url = "http://paymentgateway/v1.0/paymentgateway/topup/hxkjd/balance/query/card" ;
//                        requestMap =new LinkedMultiValueMap<>();
//                        requestMap.add("bankCard",creditCardNumber);
//                        resultString = restTemplate.postForObject(url, requestMap, String.class);
//                        amountJSON = JSONObject.fromObject(resultString);
//                        if("000000".equals(amountJSON.getString("resp_code"))){
//                            String amounts = amountJSON.getString("result");
//                            cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
//                            amount = new BigDecimal(amounts);
//                            if(amount.compareTo(BigDecimal.ONE) > 0){
//                                cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
//                            }else{
//                                cardAccount.setBlance(BigDecimal.ZERO);
//                            }
//                            creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
//                            ++ count;
//                        }
//                        break;
                        // 即富通联小额
                    /*case "54" :
                        url = "http://paymentgateway/v1.0/paymentgateway/orderparameter/get";
                        requestMap =new LinkedMultiValueMap<>();
                        requestMap.add("orderCode",orderCode);
                        resultString = restTemplate.postForObject(url, requestMap, String.class);
                        jsonObject = JSONObject.fromObject(resultString);
                        orderJson = jsonObject.getString("orderJson");
                        jsonObject1 = JSONObject.fromObject(orderJson);
                        idCard = jsonObject1.getString("idCard");
                        url = "http://paymentgateway/v1.0/paymentgateway/repayment/tldhx/balanceQuery" ;
                        requestMap =new LinkedMultiValueMap<>();
                        requestMap.add("idCard",idCard);
                        resultString = restTemplate.postForObject(url, requestMap, String.class);
                        amountJSON = JSONObject.fromObject(resultString);
                        if("000000".equals(amountJSON.getString("resp_code"))){
                            String string = amountJSON.getString("resp_message");
                            String amounts = string.split("\\:")[1];
                            CreditCardAccount cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
                            amount = new BigDecimal(amounts);
                            if(amount.compareTo(BigDecimal.ONE) > 0){
                                cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
                                creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                                ++ count;
                            }
                        }
                        break;*/
                    /*case "40" :case "41" :case "42" :case "43" :
                        url = "http://paymentgateway/v1.0/paymentgateway/orderparameter/get";
                        requestMap =new LinkedMultiValueMap<>();
                        requestMap.add("orderCode",orderCode);
                        resultString = restTemplate.postForObject(url, requestMap, String.class);
                        jsonObject = JSONObject.fromObject(resultString);
                        orderJson = jsonObject.getString("orderJson");
                        jsonObject1 = JSONObject.fromObject(orderJson);
                        idCard = jsonObject1.getString("idCard");
                        url = "http://paymentgateway/v1.0/paymentgateway/repayment/jfdh/balanceQuery" ;
                        requestMap =new LinkedMultiValueMap<>();
                        requestMap.add("idCard",idCard);
                        resultString = restTemplate.postForObject(url, requestMap, String.class);
                        amountJSON = JSONObject.fromObject(resultString);
                        if("000000".equals(amountJSON.getString("resp_code"))){
                            String string = amountJSON.getString("resp_message");
                            String amounts = string.split("\\:")[1];
                            amount = new BigDecimal(amounts);
                            amount = amount.divide(new BigDecimal(100));
                            CreditCardAccount cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
                            if(amount.compareTo(BigDecimal.ONE) > 0){
                                cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
                                creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                                ++ count;
                            }
                        }
                        break;*/
                    case "53":
                        LOG.info("环迅大额=============================");
                        url = "http://shanqi111.cn/v1.0/paymentgateway/repayment/ldx/balanceQuery";
                        requestMap =new LinkedMultiValueMap<>();
                        requestMap.add("bankCard",creditCardNumber);
                        resultString = restTemplate.postForObject(url, requestMap, String.class);
                        amountJSON = JSONObject.fromObject(resultString);
                        if("000000".equals(amountJSON.getString("resp_code"))){
                            String amounts = amountJSON.getString("resp_message");
                            amounts = amounts.substring(5);
                            cardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
                            BigDecimal amount = new BigDecimal(amounts);
                            if(amount.compareTo(BigDecimal.ONE) > 0){
                                cardAccount.setBlance(amount.subtract(BigDecimal.ONE));
                            }else{
                                cardAccount.setBlance(BigDecimal.ZERO);
                            }
                            creditCardAccountBusiness.updateCreditCardAccount(cardAccount);
                            ++ count;
                        }
                        break;
                }
                Thread.sleep(1000);
                }
            LOG.info("共成功了：{}",count);
            }
        return null;
        }

}



