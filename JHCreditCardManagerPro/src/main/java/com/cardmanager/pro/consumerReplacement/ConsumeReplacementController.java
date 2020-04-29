package com.cardmanager.pro.consumerReplacement;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.service.CreditCardManagerConfigService;
import com.cardmanager.pro.util.CardConstss;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jayden
 */
@Controller
public class ConsumeReplacementController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConsumeTaskService consumeTaskService;

    @Autowired
    private RepaymentTaskService repaymentTaskService;

    @Autowired
    private ConsumeReplacementService consumeReplacementService;

    @Autowired
    private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;

    @Autowired
    private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
    /**
     * 根据失败订单进行消费补单（客户端界面）
     * @author jayden
     * @param request
     * @param failedConsumeTaskId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/consumeTaskReplacementOrderByFailedOrder")
    @ResponseBody
    public Object findConsumeTaskFailedByReturnMessage(HttpServletRequest request,
                                                       @RequestParam(value = "consume_task_id") String failedConsumeTaskId
    ) {
        Map map = new HashMap<String, Object>();
        ConsumeTaskPOJO failedConsumeTask = consumeReplacementService.findByConsumeTaskId(failedConsumeTaskId, 2, 0);
        if(failedConsumeTask == null ){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "抱歉，未查到消费失败账单或消费账单失败后已被删除");
            return map;
        }
        String version=failedConsumeTask.getVersion();
        CreditCardManagerConfig creditCardManagerConfig=creditCardManagerConfigBusiness.findByVersion(version);
        int createOnOff=creditCardManagerConfig.getCreateOnOff();
        //判定通道是否已关闭
        if(createOnOff==0){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "抱歉，该通道已关闭，无法进行消费补单,请更换通道制定还款计划");
            return map;
        }
        //判定操作时间是否在交易时间内
        Date now=new Date();
        Calendar startLimitTime=Calendar.getInstance();
        Calendar endLimitTime=Calendar.getInstance();
        //开始时间7：00
        startLimitTime.set(Calendar.HOUR_OF_DAY,7);
        startLimitTime.set(Calendar.MINUTE,0);
        startLimitTime.set(Calendar.SECOND,0);
        //结束时间20：00
        endLimitTime.set(Calendar.HOUR_OF_DAY,20);
        endLimitTime.set(Calendar.MINUTE,0);
        endLimitTime.set(Calendar.SECOND,0);
        Date startTime=startLimitTime.getTime();
        Date endTime=endLimitTime.getTime();
        if(now.compareTo(startTime)<0||now.compareTo(endTime)>0){
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "当前时间不可进行补单，请在每天7:00-20:00之间进行补单操作");
            return map;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Calendar r = Calendar.getInstance();
        c1.add(Calendar.MINUTE, 20);
        c2.add(Calendar.MINUTE, 40);
        r.add(Calendar.MINUTE, 60);
        String consume1ExecuteTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
        String consume2ExecuteTime = DateUtil.getDateStringConvert(new String(), c2.getTime(), "yyyy-MM-dd HH:mm:ss");
        String repaymentExecuteTime = DateUtil.getDateStringConvert(new String(), r.getTime(), "yyyy-MM-dd HH:mm:ss");
        log.info("第1笔消费补单执行时间=========" + consume1ExecuteTime);
        log.info("第2笔消费补单执行时间=========" + consume2ExecuteTime);
        Random random = new Random();
        Date today = new Date();
        String createTime = DateUtil.getDateStringConvert(new String(), today, "yyyy-MM-dd HH:mm:ss");
        int count = 1;
        String consumeTaskId = failedConsumeTask.getConsumeTaskId();
//            long now=System.currentTimeMillis();
//            String nowTime1=Long.toString(now);
        // String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
        String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
        log.info("orderCode============" + orderCode);
        String comsumeTaskId1 = orderCode + "2";
        String comsumeTaskId2 = orderCode + "3";
        String repaymentTaskId1 = orderCode + "1";
        if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1))) {
            log.info("进入第一笔消费失败场景============");
            String repaymentTaskId = failedConsumeTask.getRepaymentTaskId();
            //根据还款订单号查询还款订单信息
            RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
            //根据还款订单号查询消费订单信息
            List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
            //需要还款的金额精确到小数点后2位
            BigDecimal repaymentAmount = repaymentTask.getAmount();
            //获取需要还款的汇率小数点4位
            BigDecimal rate = repaymentTask.getRate();
            //获取版本
            version = repaymentTask.getVersion();
            //获取服务费
            BigDecimal serviceCharge = repaymentTask.getServiceCharge();
            int consumeCount = consumeTaskPOJO.size();

            //计算还款订单总费用
            BigDecimal totalServiceCharge = BigDecimal.ZERO;
            if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
            } else if (consumeCount == 1) {
                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                totalServiceCharge=totalServiceCharge.add(BigDecimal.valueOf(0.01));
            } else {
                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
            }

            log.info("consumeTaskPOJO=============" + consumeCount);
            RepaymentTaskPOJO repaymentTaskNew = new RepaymentTaskPOJO();
            try {
                for (int i = 0; i < consumeTaskPOJO.size(); i++) {
                    ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();

                    //新生成消费订单对象，存入数据库

                    if (i == 0) {
                        consumeTaskNew.setConsumeTaskId(comsumeTaskId1);
                        consumeTaskNew.setExecuteDateTime(consume1ExecuteTime);
                        consumeTaskNew.setServiceCharge(totalServiceCharge);
                        consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge));
                    } else {
                        consumeTaskNew.setConsumeTaskId(comsumeTaskId2);
                        consumeTaskNew.setExecuteDateTime(consume2ExecuteTime);
                        consumeTaskNew.setServiceCharge(BigDecimal.ZERO);
                        consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount());
                    }
                    consumeTaskNew.setRepaymentTaskId(repaymentTaskId1);
                    consumeTaskNew.setAmount(consumeTaskPOJO.get(i).getAmount());
                    consumeTaskNew.setExecuteDate(sdf.format(today));
                    consumeTaskNew.setCreateTime(consumeTaskPOJO.get(i).getCreateTime());
                    consumeTaskNew.setDescription("消费补单重置" + consumeTaskPOJO.get(i).getDescription());
                    consumeTaskNew.setUserId(consumeTaskPOJO.get(i).getUserId());
                    consumeTaskNew.setTaskStatus(0);
                    consumeTaskNew.setCreditCardNumber(consumeTaskPOJO.get(i).getCreditCardNumber());
                    consumeTaskNew.setTaskType(consumeTaskPOJO.get(i).getTaskType());
                    consumeTaskNew.setReturnMessage("");
                    consumeTaskNew.setOrderStatus(0);
                    consumeTaskNew.setOrderCode("0");
                    consumeTaskNew.setErrorMessage("");
                    consumeTaskNew.setChannelId(consumeTaskPOJO.get(i).getChannelId());
                    consumeTaskNew.setChannelTag(consumeTaskPOJO.get(i).getChannelTag());
                    consumeTaskNew.setVersion(consumeTaskPOJO.get(i).getVersion());
                    consumeTaskNew.setBrandId(consumeTaskPOJO.get(i).getBrandId());
                    consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew);

                    int n = i + 1;
                    log.info("消费补单生成第" + n + "消费计划成功======");

                    //将原消费订单置为7，删除状态
                    consumeTaskPOJO.get(i).setTaskStatus(7);
                    consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO.get(i));
                }

                //新生成还款订单对象，存入数据库
                repaymentTaskNew.setUserId(repaymentTask.getUserId());
                repaymentTaskNew.setBrandId(repaymentTask.getBrandId());
                repaymentTaskNew.setCreditCardNumber(repaymentTask.getCreditCardNumber());
                repaymentTaskNew.setRepaymentTaskId(repaymentTaskId1);
                repaymentTaskNew.setOrderCode("0");
                repaymentTaskNew.setAmount(repaymentTask.getAmount());
                repaymentTaskNew.setRealAmount(BigDecimal.ZERO);
                repaymentTaskNew.setRate(repaymentTask.getRate());
                repaymentTaskNew.setServiceCharge(repaymentTask.getServiceCharge());
                repaymentTaskNew.setTotalServiceCharge(totalServiceCharge);
                repaymentTaskNew.setReturnServiceCharge(repaymentTask.getReturnServiceCharge());
                repaymentTaskNew.setChannelId(repaymentTask.getChannelId());
                repaymentTaskNew.setChannelTag(repaymentTask.getChannelTag());
                repaymentTaskNew.setTaskType(repaymentTask.getTaskType());
                repaymentTaskNew.setTaskStatus(0);
                repaymentTaskNew.setOrderStatus(0);
                repaymentTaskNew.setDescription("消费补单重置" + repaymentTask.getDescription());
                repaymentTaskNew.setReturnMessage("");
                repaymentTaskNew.setErrorMessage("");
                repaymentTaskNew.setVersion(repaymentTask.getVersion());
                repaymentTaskNew.setExecuteDate(sdf.format(today));
                repaymentTaskNew.setExecuteDateTime(repaymentExecuteTime);
                repaymentTaskNew.setCreateTime(repaymentTask.getCreateTime());
                repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTaskNew);
                log.info("消费补单生成还款计划成功======");
                //将原订单号置为7，删除状态
                repaymentTask.setTaskStatus(7);
                repaymentTaskService.saveRepaymentTaskByRepaymentTask(repaymentTask);
                log.info("成功生成第" + count + "笔消费补单");
            } catch (Exception e) {
                e.printStackTrace();
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "抱歉，补单失败，请重试或联系客服");
                return map;
            }
        }else {
            log.info("进入第二笔消费补单===========");
            String repaymentTaskId = failedConsumeTask.getRepaymentTaskId();
            String consumeId2 = failedConsumeTask.getConsumeTaskId();
            //根据还款订单号查询还款订单信息
            RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
            //根据还款订单号查询消费订单信息
            ConsumeTaskPOJO consumeTaskPOJO2 = consumeTaskService.findConsumeTaskByconsumeTaskId2(consumeId2);
            BigDecimal repaymentAmount = consumeTaskPOJO2.getAmount();
            BigDecimal rate = repaymentTask.getRate();
            version = repaymentTask.getVersion();
            BigDecimal serviceCharge = repaymentTask.getServiceCharge();
            int consumeCount = 1;

            //计算还款订单总费用
            BigDecimal totalServiceCharge = BigDecimal.ZERO;
            if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
            } else if (consumeCount == 1) {
                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
            } else {
                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
            }

            RepaymentTaskPOJO repaymentTaskNew = new RepaymentTaskPOJO();
            ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();

            try {
                //新生成消费订单对象，存入数据库
                consumeTaskNew.setExecuteDateTime(consume1ExecuteTime);
                consumeTaskNew.setServiceCharge(totalServiceCharge);
                consumeTaskNew.setAmount(repaymentAmount);
                consumeTaskNew.setRealAmount(repaymentAmount.add(totalServiceCharge));
                consumeTaskNew.setConsumeTaskId(comsumeTaskId2);
                consumeTaskNew.setRepaymentTaskId(repaymentTaskId1);
                consumeTaskNew.setExecuteDate(sdf.format(today));
                consumeTaskNew.setCreateTime(createTime);
                consumeTaskNew.setDescription("消费补单重置" + consumeTaskPOJO2.getDescription());
                consumeTaskNew.setUserId(consumeTaskPOJO2.getUserId());
                consumeTaskNew.setTaskStatus(0);
                consumeTaskNew.setCreditCardNumber(consumeTaskPOJO2.getCreditCardNumber());
                consumeTaskNew.setTaskType(consumeTaskPOJO2.getTaskType());
                consumeTaskNew.setReturnMessage("");
                consumeTaskNew.setOrderStatus(0);
                consumeTaskNew.setOrderCode("0");
                consumeTaskNew.setErrorMessage("");
                consumeTaskNew.setChannelId(consumeTaskPOJO2.getChannelId());
                consumeTaskNew.setChannelTag(consumeTaskPOJO2.getChannelTag());
                consumeTaskNew.setVersion(consumeTaskPOJO2.getVersion());
                consumeTaskNew.setBrandId(consumeTaskPOJO2.getBrandId());
                consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew);
                log.info("消费补单生成第" + count + "笔消费计划成功======");

                //将原消费订单置为4，不可补单状态
                consumeTaskPOJO2.setTaskStatus(4);
                consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO2);

                //新生成还款订单对象，存入数据库
                repaymentTaskNew.setUserId(repaymentTask.getUserId());
                repaymentTaskNew.setBrandId(repaymentTask.getBrandId());
                repaymentTaskNew.setCreditCardNumber(repaymentTask.getCreditCardNumber());
                repaymentTaskNew.setRepaymentTaskId(repaymentTaskId1);
                repaymentTaskNew.setOrderCode("0");
                repaymentTaskNew.setAmount(repaymentAmount);
                repaymentTaskNew.setRealAmount(BigDecimal.ZERO);
                repaymentTaskNew.setRate(repaymentTask.getRate());
                repaymentTaskNew.setServiceCharge(repaymentTask.getServiceCharge());
                repaymentTaskNew.setTotalServiceCharge(totalServiceCharge);
                repaymentTaskNew.setReturnServiceCharge(repaymentTask.getReturnServiceCharge());
                repaymentTaskNew.setChannelId(repaymentTask.getChannelId());
                repaymentTaskNew.setChannelTag(repaymentTask.getChannelTag());
                repaymentTaskNew.setTaskType(repaymentTask.getTaskType());
                repaymentTaskNew.setTaskStatus(0);
                repaymentTaskNew.setOrderStatus(0);
                repaymentTaskNew.setDescription("消费补单重置" + repaymentTask.getDescription());
                repaymentTaskNew.setReturnMessage("");
                repaymentTaskNew.setErrorMessage("");
                repaymentTaskNew.setVersion(repaymentTask.getVersion());
                repaymentTaskNew.setExecuteDate(sdf.format(today));
                repaymentTaskNew.setExecuteDateTime(repaymentExecuteTime);
                repaymentTaskNew.setCreateTime(createTime);
                repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTaskNew);
                log.info("消费补单生成第" + count + "笔还款计划成功======");
            } catch (Exception e) {
                e.printStackTrace();
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "抱歉，补单失败，请重试或联系客服");
                return map;
            }
        }
//        if (count % 50 == 0) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS );
        map.put(CommonConstants.RESP_MESSAGE, "补单成功，消费补单计划将在20分钟后执行，还款计划将在60分钟后执行，请留意执行结果");
        return map;
    }

    /**
     *通过还款订单号找到对应的消费订单号
     * @param orderCode 还款订单号
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/findConsumeTaskId")
    @ResponseBody
    public Object findConsumeTaskId(@RequestParam("orderCode") String orderCode){
        Map<String,Object>map=new HashMap<>();

        List<ConsumeTaskPOJO> lists=consumeTaskPOJOBusiness.findByRepaymentTaskid(orderCode);
        if (lists ==null ||lists.size()==0){
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"未获取到订单号");
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,lists);
        return map;
    }
}
