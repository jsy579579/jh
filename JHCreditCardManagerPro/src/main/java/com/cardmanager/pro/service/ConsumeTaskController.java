package com.cardmanager.pro.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.ConsumeTaskService;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentTaskService;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.scanner.RepaymentTaskScanner;
import com.cardmanager.pro.util.CardConstss;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jayden
 */
@Controller
public class ConsumeTaskController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConsumeTaskService consumeTaskService;

    @Autowired
    private RepaymentTaskService repaymentTaskService;

    @Autowired
    CreditCardManagerTaskService creditCardManagerTaskService;

    @Autowired
    private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;

    @Autowired
    CreditCardAccountBusiness creditCardAccountBusiness;

    @Autowired
    CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;

    @Autowired
    RepaymentTaskScanner repaymentTaskScanner;

    @Autowired
    BaseExecutor baseExecutor;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 根据失败描述进行消费补单接口
     *
     * @param request
     * @param message
     * @param executeDate
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/createRepaymentTask/manual")
    @ResponseBody
    public Object findConsumeTaskFailedByReturnMessage(HttpServletRequest request,
                                                       @RequestParam(value = "return_message") String message,
                                                       @RequestParam(value = "execute_date") String executeDate
    ) {
        Map map = new HashMap<String, Object>();
        log.info("message==========="+message);
        log.info("executeDate==========="+executeDate);
        List<ConsumeTaskPOJO> consumeTaskPOJOList = consumeTaskService.findAbnormalConsumeTaskByMessage(message, 0, 2, executeDate);
        log.info("当前条件异常订单数量=========" + consumeTaskPOJOList.size());
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
        int successCount = count - 1;
        //int second=0;
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            String consumeTaskId = consumeTask.getConsumeTaskId();
//            long now=System.currentTimeMillis();
//            String nowTime1=Long.toString(now);
//            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
          String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1, consumeTaskId.length()))) {
                log.info("进入第一笔消费失败场景============");
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
                BigDecimal repaymentAmount = repaymentTask.getAmount();
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = consumeTaskPOJO.size();

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
                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
                        } else {
                            consumeTaskNew.setConsumeTaskId(comsumeTaskId2);
                            consumeTaskNew.setExecuteDateTime(consume2ExecuteTime);
                            consumeTaskNew.setServiceCharge(BigDecimal.ZERO);
                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().setScale(0,BigDecimal.ROUND_UP));
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
                }
                count++;
            }else{
                log.info("进入第二笔消费补单===========");
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                String consumeId2 = consumeTask.getConsumeTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                ConsumeTaskPOJO consumeTaskPOJO2 = consumeTaskService.findConsumeTaskByconsumeTaskId2(consumeId2);
                BigDecimal repaymentAmount = consumeTaskPOJO2.getAmount();
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = 1;

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
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

            if (count % 50 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
////                String creditCardNumber=repaymentTask.getCreditCardNumber();
////                String userId=repaymentTask.getUserId();
////                log.info("userId========="+userId+"creditCardNumber=========="+creditCardNumber);
////                //String reservedAmount=new BigDecimal(amount).divide(new BigDecimal("2")).add(new BigDecimal("50")).toString();
////                String reservedAmount=new BigDecimal(amount).toString();
////                log.info("预留金=============="+reservedAmount);
////                String[] executeDate=new String[2];
////                Calendar c = Calendar.getInstance();
////                c.add(Calendar.DAY_OF_MONTH, 1);
//                System.out.println("增加一天后日期:"+sdf.format(c.getTime()));
//                executeDate[0]=sdf.format(c.getTime());
//                executeDate[1]=sdf.format(c.getTime());
//                String version=repaymentTask.getVersion();
//                String city=consumeTask.getDescription().substring(5,consumeTask.getDescription().length());
//                log.info("city==========="+city);
//                JSONObject resultJSONObject=new JSONObject();
//                Object temporaryPlan=creditCardManagerTaskService.createRepaymentTask(request,userId,creditCardNumber,amount,reservedAmount,repaymentTask.getBrandId(),executeDate,version);
//                String taskJSON=temporaryPlan.toString();
//                creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(request,taskJSON,city,amount,reservedAmount,version);
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数" + consumeTaskPOJOList.size() + "消费补单计划创建成功，成功数量======" + (count - 1));
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 根据失败描述进行消费补单接口（分2笔）
     *
     * @param request
     * @param message
     * @param executeDate
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/consumeTaskReplacement/manual/2")
    @ResponseBody
    public Object findConsumeTaskFailedByReturnMessage2(HttpServletRequest request,
                                                       @RequestParam(value = "return_message") String message,
                                                       @RequestParam(value = "execute_date") String executeDate
    ) {
        Map map = new HashMap<String, Object>();
        List<ConsumeTaskPOJO> consumeTaskPOJOList = consumeTaskService.findAbnormalConsumeTaskByMessage(message, 0, 2, executeDate);
        log.info("当前条件异常订单数量=========" + consumeTaskPOJOList.size());
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
        int successCount = count - 1;
        //int second=0;
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            String consumeTaskId = consumeTask.getConsumeTaskId();
//            long now=System.currentTimeMillis();
//            String nowTime1=Long.toString(now);
//            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1, consumeTaskId.length()))) {
                log.info("进入第一笔消费失败场景===========orderCode==="+consumeTaskId);
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
                BigDecimal repaymentAmount = repaymentTask.getAmount();
                BigDecimal consumeAmount2=repaymentAmount.divide(new BigDecimal("2")).setScale(0,BigDecimal.ROUND_DOWN);
                BigDecimal consumeAmount1=repaymentAmount.subtract(consumeAmount2);
                System.out.println("生成后第1笔消费金额========="+consumeAmount1+"==========生成后第2笔消费金额========="+consumeAmount2);
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = consumeTaskPOJO.size();

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

                log.info("consumeTaskPOJO=============" + consumeCount);
                RepaymentTaskPOJO repaymentTaskNew = new RepaymentTaskPOJO();
                try {
                    for (int i = 0; i < consumeTaskPOJO.size(); i++) {
                        ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();
                        ConsumeTaskPOJO consumeTaskNew2 = new ConsumeTaskPOJO();

                        //新生成消费订单对象，存入数据库
                        if (i == 0) {
                            consumeTaskNew.setConsumeTaskId(comsumeTaskId1);
                            consumeTaskNew.setExecuteDateTime(consume1ExecuteTime);
                            consumeTaskNew.setServiceCharge(totalServiceCharge);
                            consumeTaskNew.setRealAmount(consumeAmount1.add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
                        } else {
                            consumeTaskNew.setConsumeTaskId(comsumeTaskId2);
                            consumeTaskNew.setExecuteDateTime(consume2ExecuteTime);
                            consumeTaskNew.setServiceCharge(BigDecimal.ZERO);
                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().setScale(0,BigDecimal.ROUND_UP));
                        }
                        consumeTaskNew.setRepaymentTaskId(repaymentTaskId1);
                        //新生成的消费金额
                        consumeTaskNew.setAmount(consumeAmount1);
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

                        //将新生的第二笔订单对象赋值，存入数据库
                        consumeTaskNew2.setConsumeTaskId(comsumeTaskId2);
                        consumeTaskNew2.setExecuteDateTime(consume2ExecuteTime);
                        consumeTaskNew2.setServiceCharge(BigDecimal.ZERO);
                        consumeTaskNew2.setRealAmount(consumeAmount2);
                        consumeTaskNew2.setRepaymentTaskId(repaymentTaskId1);
                        //新生成的消费金额
                        consumeTaskNew2.setAmount(consumeAmount2);
                        consumeTaskNew2.setExecuteDate(sdf.format(today));
                        consumeTaskNew2.setCreateTime(consumeTaskPOJO.get(i).getCreateTime());
                        consumeTaskNew2.setDescription("消费补单重置" + consumeTaskPOJO.get(i).getDescription());
                        consumeTaskNew2.setUserId(consumeTaskPOJO.get(i).getUserId());
                        consumeTaskNew2.setTaskStatus(0);
                        consumeTaskNew2.setCreditCardNumber(consumeTaskPOJO.get(i).getCreditCardNumber());
                        consumeTaskNew2.setTaskType(consumeTaskPOJO.get(i).getTaskType());
                        consumeTaskNew2.setReturnMessage("");
                        consumeTaskNew2.setOrderStatus(0);
                        consumeTaskNew2.setOrderCode("0");
                        consumeTaskNew2.setErrorMessage("");
                        consumeTaskNew2.setChannelId(consumeTaskPOJO.get(i).getChannelId());
                        consumeTaskNew2.setChannelTag(consumeTaskPOJO.get(i).getChannelTag());
                        consumeTaskNew2.setVersion(consumeTaskPOJO.get(i).getVersion());
                        consumeTaskNew2.setBrandId(consumeTaskPOJO.get(i).getBrandId());
                        consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew2);


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
                }
                count++;
            }else{
                log.info("进入第二笔消费补单===========orderCode=="+consumeTaskId);
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                String consumeId2 = consumeTask.getConsumeTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                ConsumeTaskPOJO consumeTaskPOJO2 = consumeTaskService.findConsumeTaskByconsumeTaskId2(consumeId2);
                BigDecimal repaymentAmount = consumeTaskPOJO2.getAmount();
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = 1;

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
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

                    //将原消费订单置为7，删除状态
//                    consumeTaskPOJO2.setTaskStatus(7);
//                    consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO2);

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

            if (count % 50 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
////                String creditCardNumber=repaymentTask.getCreditCardNumber();
////                String userId=repaymentTask.getUserId();
////                log.info("userId========="+userId+"creditCardNumber=========="+creditCardNumber);
////                //String reservedAmount=new BigDecimal(amount).divide(new BigDecimal("2")).add(new BigDecimal("50")).toString();
////                String reservedAmount=new BigDecimal(amount).toString();
////                log.info("预留金=============="+reservedAmount);
////                String[] executeDate=new String[2];
////                Calendar c = Calendar.getInstance();
////                c.add(Calendar.DAY_OF_MONTH, 1);
//                System.out.println("增加一天后日期:"+sdf.format(c.getTime()));
//                executeDate[0]=sdf.format(c.getTime());
//                executeDate[1]=sdf.format(c.getTime());
//                String version=repaymentTask.getVersion();
//                String city=consumeTask.getDescription().substring(5,consumeTask.getDescription().length());
//                log.info("city==========="+city);
//                JSONObject resultJSONObject=new JSONObject();
//                Object temporaryPlan=creditCardManagerTaskService.createRepaymentTask(request,userId,creditCardNumber,amount,reservedAmount,repaymentTask.getBrandId(),executeDate,version);
//                String taskJSON=temporaryPlan.toString();
//                creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(request,taskJSON,city,amount,reservedAmount,version);
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数" + consumeTaskPOJOList.size() + "消费补单计划创建成功，成功数量======" + (count - 1));
        map.put("reponseCode", "000000");
        return map;
    }
    /**
     * 根据失败描述及用户id进行个人消费补单接口
     *
     * @param request
     * @param message
     * @param executeDate
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/createRepaymentTaskByUseId/manual")
    @ResponseBody
    public Object findConsumeTaskFailedByReturnMessageUseId(HttpServletRequest request,
                                                            @RequestParam(value = "return_message") String message,
                                                            @RequestParam(value = "start_date") String executeDate,//起始时间
                                                            @RequestParam(value = "user_id") String userId,
                                                            @RequestParam(value = "card_no") String cardNo) {
        Map map = new HashMap<String, Object>();
        List<ConsumeTaskPOJO> consumeTaskPOJOList = consumeTaskService.findAbnormalConsumeTaskByMessageUserId(message, 0, 2, executeDate, userId, cardNo);
        log.info("当前条件异常订单数量=========" + consumeTaskPOJOList.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Random random = new Random();
        Date today = new Date();
        String createTime = DateUtil.getDateStringConvert(new String(), today, "yyyy-MM-dd HH:mm:ss");
        int count = 1;
        int minute = 0;
        int sucessCount = count - 1;
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            Calendar r = Calendar.getInstance();
            c1.add(Calendar.MINUTE, minute + 20);
            c2.add(Calendar.MINUTE, minute + 40);
            r.add(Calendar.MINUTE, minute + 60);
            String consume1ExecuteTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
            String consume2ExecuteTime = DateUtil.getDateStringConvert(new String(), c2.getTime(), "yyyy-MM-dd HH:mm:ss");
            String repaymentExecuteTime = DateUtil.getDateStringConvert(new String(), r.getTime(), "yyyy-MM-dd HH:mm:ss");
            log.info("第1笔消费补单执行时间=========" + consume1ExecuteTime);
            log.info("第2笔消费补单执行时间=========" + consume2ExecuteTime);
            String consumeTaskId = consumeTask.getConsumeTaskId();
            long now=System.currentTimeMillis();
            String nowTime1=Long.toString(now);
            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
//            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS") +random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1, consumeTaskId.length()))) {
                log.info("进入第一笔消费失败场景============");
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
                BigDecimal repaymentAmount = repaymentTask.getAmount();
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = consumeTaskPOJO.size();

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
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
                        consumeTaskNew.setCreateTime(createTime);
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

                        //将原消费订单置为4，不可补单状态
                        consumeTaskPOJO.get(i).setTaskStatus(4);
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
                    repaymentTaskNew.setCreateTime(createTime);
                    repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTaskNew);
                    log.info("消费补单生成还款计划成功======");
                    //将原订单号置为7，删除状态
                    repaymentTask.setTaskStatus(7);
                    repaymentTaskService.saveRepaymentTaskByRepaymentTask(repaymentTask);
                    log.info("成功生成第" + count + "笔消费补单");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                minute += 60;
            }
            if (count % 50 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
////                String creditCardNumber=repaymentTask.getCreditCardNumber();
////                String userId=repaymentTask.getUserId();
////                log.info("userId========="+userId+"creditCardNumber=========="+creditCardNumber);
////                //String reservedAmount=new BigDecimal(amount).divide(new BigDecimal("2")).add(new BigDecimal("50")).toString();
////                String reservedAmount=new BigDecimal(amount).toString();
////                log.info("预留金=============="+reservedAmount);
////                String[] executeDate=new String[2];
////                Calendar c = Calendar.getInstance();
////                c.add(Calendar.DAY_OF_MONTH, 1);
//                System.out.println("增加一天后日期:"+sdf.format(c.getTime()));
//                executeDate[0]=sdf.format(c.getTime());
//                executeDate[1]=sdf.format(c.getTime());
//                String version=repaymentTask.getVersion();
//                String city=consumeTask.getDescription().substring(5,consumeTask.getDescription().length());
//                log.info("city==========="+city);
//                JSONObject resultJSONObject=new JSONObject();
//                Object temporaryPlan=creditCardManagerTaskService.createRepaymentTask(request,userId,creditCardNumber,amount,reservedAmount,repaymentTask.getBrandId(),executeDate,version);
//                String taskJSON=temporaryPlan.toString();
//                creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(request,taskJSON,city,amount,reservedAmount,version);
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数" + consumeTaskPOJOList.size() + "消费补单计划创建成功，成功数量======" + (count - 1));
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 根据失败描述进行换通道补单
     *
     * @param version
     * @param changeVersion
     * @param executeDate
     * @return
     * @author jayden
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/replacement/manual/changeversion")
    @ResponseBody
    public Object replacementOrderChangeVersionByVersion(//异常通道号
                                                                         @RequestParam(value = "version") String version,
                                                                         //需要更换的通道号
                                                                         @RequestParam(value = "change_version") String changeVersion,
                                                                         @RequestParam(value = "execute_date") String executeDate) {

        log.info("进入切换通道补单接口");
        Map map = new HashMap<>();
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findCardManangerByVersion(changeVersion);
        String channelId = creditCardManagerConfig.getChannelId();
        String channelTag = creditCardManagerConfig.getChannelTag();
        String noSupportBank = creditCardManagerConfig.getNoSupportBank();
        List<ConsumeTaskPOJO> consumeTaskPOJOList = consumeTaskService.findAbnormalConsumeTaskByMessage(version, 0, 0, executeDate);
        log.info("当前条件异常订单数量=========" + consumeTaskPOJOList.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Calendar r = Calendar.getInstance();
        c1.add(Calendar.MINUTE, 20);
        c2.add(Calendar.MINUTE, 60);
        r.add(Calendar.MINUTE, 100);
        String consume1ExecuteTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
        String consume2ExecuteTime = DateUtil.getDateStringConvert(new String(), c2.getTime(), "yyyy-MM-dd HH:mm:ss");
        String repaymentExecuteTime = DateUtil.getDateStringConvert(new String(), r.getTime(), "yyyy-MM-dd HH:mm:ss");
        log.info("第1笔消费补单执行时间=========" + consume1ExecuteTime);
        log.info("第2笔消费补单执行时间=========" + consume2ExecuteTime);
        Random random = new Random();
        Date today = new Date();
        String createTime = DateUtil.getDateStringConvert(new String(), today, "yyyy-MM-dd HH:mm:ss");
        int count = 1;
        int successCount = count - 1;
        int noSupportBankCount = 0;
        int noBindCardCount = 0;
        int queryRateCount = 0;
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            String consumeTaskId = consumeTask.getConsumeTaskId();
            long now=System.currentTimeMillis();
            String nowTime1=Long.toString(now);
//            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1, consumeTaskId.length()))) {
                log.info("进入第一笔消费失败场景============");

                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
                BigDecimal repaymentAmount = repaymentTask.getAmount();
                //根据卡号获取银行名称
                String card = repaymentTask.getCreditCardNumber();
//                String url = "http://user/v1.0/user/query/bankName";
//                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//                requestEntity.add("card_no", card);
//                try {
//                    String result = restTemplate.postForObject(url, requestEntity, String.class);
//                    log.info("RESULT================" + result);
//                    JSONObject jsonObject = JSONObject.fromObject(result);
//                    if (result == null) {
//                        log.info("用户已删卡=====");
//                        continue;
//                    }
//                    String bankName = jsonObject.getString("bankName");
//                    log.info("银行名称==========" + bankName);
//                    if (bankName.contains(noSupportBank)) {
//                        log.info("该卡不支持该通道===");
//                        noSupportBankCount++;
//                        continue;
//                    }
//                } catch (RestClientException e) {
//                    e.printStackTrace();
//                    log.info("查询银行卡失败========");
//                    continue;
//                }
                //判定银行卡在该通道是否已绑卡，未绑卡则跳出本次循环
                if (!"31".equals(changeVersion)) {
                    String url1 = "http://paymentgateway/v1.0/paymentgateway/query/bindCardByVersion";
                    MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
                    requestEntity1.add("card_no", card);
                    requestEntity1.add("version", version);
                    try {
                        String result = restTemplate.postForObject(url1, requestEntity1, String.class);
                        log.info("RESULT================" + result);
                        if (result == null || "".equals(result)) {
                            log.info("该卡未在该通道绑卡");
                            noBindCardCount++;
                            continue;
                        }
                    } catch (RestClientException e) {
                        e.printStackTrace();
                        log.info("查询通道绑卡失败========");
                        continue;
                    }
                }


                String userId = consumeTask.getUserId();
                String brandId = consumeTask.getBrandId();
                //查询用户费率
                Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), changeVersion);
                if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
//                    userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
//                    return userChannelRate;
                    queryRateCount++;
                    continue;
                }
                JSONObject result = null;
                result = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
                String rateStr = result.getString("rate");
                String extraFeeStr = result.getString("extraFee");
                String withdrawFeeStr = result.getString("withdrawFee");
                BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
                ;
                BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);


//                BigDecimal rate=repaymentTask.getRate();
//                String version=repaymentTask.getVersion();
//                BigDecimal serviceCharge=repaymentTask.getServiceCharge();
                int consumeCount = consumeTaskPOJO.size();

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(changeVersion) || CardConstss.CARD_VERSION_11.equals(changeVersion)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                } else {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                    totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
                }
                log.info("consumeTaskPOJO=============" + consumeCount);

                //获取当前用户信用卡信息
                JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, card);
                if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
                    map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
                    map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
                    return map;
                }
                JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
                int billDate = resultBankCardJSONObject.getInt("billDay");
                int repaymentDate = resultBankCardJSONObject.getInt("repaymentDay");
                int creditBlance = resultBankCardJSONObject.getInt("creditBlance");
                String bankName = resultBankCardJSONObject.getString("bankName");
                CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, card, changeVersion);
                if (model == null) {
                    String phone = null;
                    try {
                        JSONObject userInfo = this.getUserInfo(userId);
                        resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                        phone = resultJSONObject.getString("phone");
                        brandId = resultJSONObject.getString("brandId");
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        log.error("获取用户信息失败!", e);
//                        return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
                        continue;
                    }
                    model = creditCardAccountBusiness.createNewAccount(userId, card, changeVersion, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
                }
                try {
                    //判定城市是否符合环迅要求
                    String desc=consumeTaskPOJO.get(0).getDescription();
                    String[] desc1=desc.split("\\|");
                    String province=desc1[1];
                    String[] city=province.split("-");
                    String city1=city[1];
                    city1=city1.substring(0,city1.length()-1);

                    String url="http://paymentgateway/v1.0/paymentgateway/topup/hxdhx/querybycity";
                    MultiValueMap requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("city",city1);
                    String result1=restTemplate.postForObject(url,requestEntity,String.class);
                    com.alibaba.fastjson.JSONObject jsonObject=com.alibaba.fastjson.JSONObject.parseObject(result1);
                    String repoCode=jsonObject.getString(CommonConstants.RESP_CODE);
                    if(!CommonConstants.SUCCESS.equals(repoCode)){
                        log.info("不支持的落地城市=="+city1);
                        noSupportBankCount++;
                        continue;
                    }
                    com.alibaba.fastjson.JSONObject resultObj=jsonObject.getJSONObject(CommonConstants.RESULT);
                    String hxProvince=resultObj.getString("province");
                    String hxCity=resultObj.getString("city");
                    String area=hxProvince+"-"+hxCity;
                    log.info("落地城市=="+area);
                    for (int i = 0; i < consumeTaskPOJO.size(); i++) {
//                        ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();
//                        //新生成消费订单对象，存入数据库
//                        if (i == 0) {
//                            consumeTaskNew.setConsumeTaskId(consumeTaskPOJO.get(i).getConsumeTaskId());
//                            consumeTaskNew.setExecuteDateTime(consumeTaskPOJO.get(i).getExecuteDateTime());
//                            consumeTaskNew.setServiceCharge(totalServiceCharge);
//                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
//                        } else {
//                            consumeTaskNew.setConsumeTaskId(consumeTaskPOJO.get(i).getConsumeTaskId());
//                            consumeTaskNew.setExecuteDateTime(consumeTaskPOJO.get(i).getExecuteDateTime());
//                            consumeTaskNew.setServiceCharge(BigDecimal.ZERO);
//                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount());
//                        }
//                        consumeTaskNew.setRepaymentTaskId(repaymentTaskId1);
//                        consumeTaskNew.setAmount(consumeTaskPOJO.get(i).getAmount());
//                        consumeTaskNew.setExecuteDate(consumeTaskPOJO.get(i).getExecuteDate());
//                        consumeTaskNew.setCreateTime(repaymentTask.getCreateTime());
//                        consumeTaskNew.setDescription("消费计划|"+area);
//                        consumeTaskNew.setUserId(consumeTaskPOJO.get(i).getUserId());
//                        consumeTaskNew.setTaskStatus(0);
//                        consumeTaskNew.setCreditCardNumber(consumeTaskPOJO.get(i).getCreditCardNumber());
//                        consumeTaskNew.setTaskType(consumeTaskPOJO.get(i).getTaskType());
//                        consumeTaskNew.setReturnMessage("");
//                        consumeTaskNew.setOrderStatus(0);
//                        consumeTaskNew.setOrderCode("0");
//                        consumeTaskNew.setErrorMessage("");
//                        consumeTaskNew.setChannelId(channelId);
//                        consumeTaskNew.setChannelTag(channelTag);
//                        consumeTaskNew.setVersion(changeVersion);
//                        consumeTaskNew.setBrandId(consumeTaskPOJO.get(i).getBrandId());
//                        consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew);
//
//                        int n = i + 1;
//                        log.info("消费补单生成第" + n + "消费计划成功======");
//
//                        //将原消费订单置为7，删除状态
//                        consumeTaskPOJO.get(i).setTaskStatus(7);
//                        consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO.get(i));
                        if (i == 0) {
                            consumeTaskPOJO.get(i).setServiceCharge(totalServiceCharge);
                            consumeTaskPOJO.get(i).setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
                        } else {
                            consumeTaskPOJO.get(i).setServiceCharge(BigDecimal.ZERO);
                            consumeTaskPOJO.get(i).setRealAmount(consumeTaskPOJO.get(i).getAmount());
                        }
                        consumeTaskPOJO.get(i).setChannelId(channelId);
                        consumeTaskPOJO.get(i).setChannelTag(channelTag);
                        consumeTaskPOJO.get(i).setVersion(changeVersion);
                        consumeTaskPOJO.get(i).setDescription("消费计划|"+area);
                        consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO.get(i));
                    }

                    //新生成还款订单对象，存入数据库
//                    repaymentTaskNew.setUserId(repaymentTask.getUserId());
//                    repaymentTaskNew.setBrandId(repaymentTask.getBrandId());
//                    repaymentTaskNew.setCreditCardNumber(repaymentTask.getCreditCardNumber());
//                    repaymentTaskNew.setRepaymentTaskId(repaymentTaskId1);
//                    repaymentTaskNew.setOrderCode("0");
//                    repaymentTaskNew.setAmount(repaymentTask.getAmount());
//                    repaymentTaskNew.setRealAmount(BigDecimal.ZERO);
//                    repaymentTaskNew.setRate(rate);
//                    repaymentTaskNew.setServiceCharge(serviceCharge);
//                    repaymentTaskNew.setTotalServiceCharge(totalServiceCharge);
//                    repaymentTaskNew.setReturnServiceCharge(repaymentTask.getReturnServiceCharge());
//                    repaymentTaskNew.setChannelId(channelId);
//                    repaymentTaskNew.setChannelTag(channelTag);
//                    repaymentTaskNew.setTaskType(repaymentTask.getTaskType());
//                    repaymentTaskNew.setTaskStatus(0);
//                    repaymentTaskNew.setOrderStatus(0);
//                    repaymentTaskNew.setDescription(repaymentTask.getDescription());
//                    repaymentTaskNew.setReturnMessage("");
//                    repaymentTaskNew.setErrorMessage("");
//                    repaymentTaskNew.setVersion(changeVersion);
//                    repaymentTaskNew.setExecuteDate(repaymentTask.getExecuteDate());
//                    repaymentTaskNew.setExecuteDateTime(repaymentTask.getExecuteDateTime());
//                    repaymentTaskNew.setCreateTime(repaymentTask.getCreateTime());
                    repaymentTask.setChannelId(channelId);
                    repaymentTask.setChannelTag(channelTag);
                    repaymentTask.setVersion(changeVersion);
                    repaymentTask.setTotalServiceCharge(totalServiceCharge);
                    repaymentTask.setServiceCharge(serviceCharge);
                    repaymentTask.setRate(rate);
                    repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTask);
                    log.info("切换通道修改还款计划成功======");
                    //将原订单号置为7，删除状态
//                    repaymentTask.setTaskStatus(7);
//                    repaymentTaskService.saveRepaymentTaskByRepaymentTask(repaymentTask);
                    log.info("成功生成第" + count + "笔切换通道订单");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
            }
            if (count % 50 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
////                String creditCardNumber=repaymentTask.getCreditCardNumber();
////                String userId=repaymentTask.getUserId();
////                log.info("userId========="+userId+"creditCardNumber=========="+creditCardNumber);
////                //String reservedAmount=new BigDecimal(amount).divide(new BigDecimal("2")).add(new BigDecimal("50")).toString();
////                String reservedAmount=new BigDecimal(amount).toString();
////                log.info("预留金=============="+reservedAmount);
////                String[] executeDate=new String[2];
////                Calendar c = Calendar.getInstance();
////                c.add(Calendar.DAY_OF_MONTH, 1);
//                System.out.println("增加一天后日期:"+sdf.format(c.getTime()));
//                executeDate[0]=sdf.format(c.getTime());
//                executeDate[1]=sdf.format(c.getTime());
//                String version=repaymentTask.getVersion();
//                String city=consumeTask.getDescription().substring(5,consumeTask.getDescription().length());
//                log.info("city==========="+city);
//                JSONObject resultJSONObject=new JSONObject();
//                Object temporaryPlan=creditCardManagerTaskService.createRepaymentTask(request,userId,creditCardNumber,amount,reservedAmount,repaymentTask.getBrandId(),executeDate,version);
//                String taskJSON=temporaryPlan.toString();
//                creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(request,taskJSON,city,amount,reservedAmount,version);
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数==" + consumeTaskPOJOList.size() + "======消费补单计划创建成功，成功数量======" + (count - 1) + "=====不支持银行数量==========" + noSupportBankCount +
                "===未绑卡数===" + noBindCardCount + "=====查询费率失败量====" + queryRateCount);
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 根据失败描述对失败消费订单进行换通道补单（未执行订单切换通道）
     *
     * @param version
     * @param changeVersion
     * @param executeDate
     * @return
     * @author jayden
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/replacement/failorder/changeversion")
    @ResponseBody
    public Object replacementOrderChangeVersionByReturnMessageAndVersion(//异常通道号
                                                                         @RequestParam(value = "version") String version,
                                                                         //需要更换的通道号
                                                                         @RequestParam(value = "change_version") String changeVersion,
                                                                         @RequestParam(value = "execute_date") String executeDate
                                                                         //失败原因
                                                                         //@RequestParam(value="return_message")String returnMessage
    ) {

        log.info("进入切换通道补单接口================================");
        Map map = new HashMap<>();
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findCardManangerByVersion(changeVersion);
        String channelId = creditCardManagerConfig.getChannelId();
        String channelTag = creditCardManagerConfig.getChannelTag();
        String noSupportBank = creditCardManagerConfig.getNoSupportBank();
       // List<String>  creditCardNos= consumeTaskService.findFailOrderByVersionAndReturnMessage(version, 0, 0,null ,executeDate);
        List<String>  creditCardNos= consumeTaskService.findFailOrderByVersionAndReturnMessage(version, 0, 0 ,executeDate);
        log.info("当前条件异常订单数量=========" + creditCardNos.size());
        log.info("当前条件异常订单卡号=========" + creditCardNos);
        //当前卡号在当前通道未执行的订单
        List<ConsumeTaskPOJO> consumeTaskPOJOList=consumeTaskService.findOrderByCreditCardNo(creditCardNos,0,0,executeDate,version);
        log.info("当前条件需切换订单数量=========" + consumeTaskPOJOList.size());
//        if(consumeTaskPOJOList.size()>0){
//            return map;
//        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Calendar r = Calendar.getInstance();
        c1.add(Calendar.MINUTE, 20);
        c2.add(Calendar.MINUTE, 60);
        r.add(Calendar.MINUTE, 100);
        String consume1ExecuteTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
        String consume2ExecuteTime = DateUtil.getDateStringConvert(new String(), c2.getTime(), "yyyy-MM-dd HH:mm:ss");
        String repaymentExecuteTime = DateUtil.getDateStringConvert(new String(), r.getTime(), "yyyy-MM-dd HH:mm:ss");
        log.info("第1笔消费补单执行时间=========" + consume1ExecuteTime);
        log.info("第2笔消费补单执行时间=========" + consume2ExecuteTime);
        Random random = new Random();
        Date today = new Date();
        String createTime = DateUtil.getDateStringConvert(new String(), today, "yyyy-MM-dd HH:mm:ss");
        int count = 1;
        int successCount = count - 1;
        int noSupportBankCount = 0;
        int noBindCardCount = 0;
        int queryRateCount = 0;
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            String consumeTaskId = consumeTask.getConsumeTaskId();
            long now=System.currentTimeMillis();
            String nowTime1=Long.toString(now);
//            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("2".equals(consumeTaskId.substring(consumeTaskId.length() - 1, consumeTaskId.length()))) {
                log.info("进入第一笔消费失败场景============");

                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
                BigDecimal repaymentAmount = repaymentTask.getAmount();
                //根据卡号获取银行名称
                String card = repaymentTask.getCreditCardNumber();
//                String url = "http://user/v1.0/user/query/bankName";
//                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//                requestEntity.add("card_no", card);
//                try {
//                    String result = restTemplate.postForObject(url, requestEntity, String.class);
//                    log.info("RESULT================" + result);
//                    JSONObject jsonObject = JSONObject.fromObject(result);
//                    if (result == null) {
//                        log.info("用户已删卡=====");
//                        continue;
//                    }
//                    String bankName = jsonObject.getString("bankName");
//                    log.info("银行名称==========" + bankName);
//                    if (bankName.contains(noSupportBank)) {
//                        log.info("该卡不支持该通道===");
//                        noSupportBankCount++;
//                        continue;
//                    }
//                } catch (RestClientException e) {
//                    e.printStackTrace();
//                    log.info("查询银行卡失败========");
//                    continue;
//                }
                //判定银行卡在该通道是否已绑卡，未绑卡则跳出本次循环
//                if (!"31".equals(changeVersion)) {
//                    String url1 = "http://paymentgateway/v1.0/paymentgateway/query/bindCardByVersion";
//                    MultiValueMap<String, String> requestEntity1 = new LinkedMultiValueMap<String, String>();
//                    requestEntity1.add("card_no", card);
//                    requestEntity1.add("version", version);
//                    try {
//                        String result = restTemplate.postForObject(url1, requestEntity1, String.class);
//                        log.info("RESULT================" + result);
//                        if (result == null || "".equals(result)) {
//                            log.info("该卡未在该通道绑卡");
//                            noBindCardCount++;
//                            continue;
//                        }
//                    } catch (RestClientException e) {
//                        e.printStackTrace();
//                        log.info("查询通道绑卡失败========");
//                        continue;
//                    }
//                }


                String userId = consumeTask.getUserId();
                String brandId = consumeTask.getBrandId();
                //查询用户费率
                Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), changeVersion);
                if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
//                    userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
//                    return userChannelRate;
                    queryRateCount++;
                    continue;
                }
                JSONObject result = null;
                result = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
                String rateStr = result.getString("rate");
                String extraFeeStr = result.getString("extraFee");
                String withdrawFeeStr = result.getString("withdrawFee");
                BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
                ;
                BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);


//                BigDecimal rate=repaymentTask.getRate();
//                String version=repaymentTask.getVersion();
//                BigDecimal serviceCharge=repaymentTask.getServiceCharge();
                int consumeCount = consumeTaskPOJO.size();

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(changeVersion) || CardConstss.CARD_VERSION_11.equals(changeVersion)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                } else {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
                    totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
                }
                log.info("consumeTaskPOJO=============" + consumeCount);

                //获取当前用户信用卡信息
                JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, card);
                if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
//                    map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
//                    map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
//                    return map;
                    log.info("该卡不可用,请更换一张信用卡=============");
                    continue;
                }
                JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
                int billDate = resultBankCardJSONObject.getInt("billDay");
                int repaymentDate = resultBankCardJSONObject.getInt("repaymentDay");
                int creditBlance = resultBankCardJSONObject.getInt("creditBlance");
                String bankName = resultBankCardJSONObject.getString("bankName");
                CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, card, changeVersion);
                if (model == null) {
                    String phone = null;
                    try {
                        JSONObject userInfo = this.getUserInfo(userId);
                        resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                        phone = resultJSONObject.getString("phone");
                        brandId = resultJSONObject.getString("brandId");
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        log.error("获取用户信息失败!", e);
//                        return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
                        continue;
                    }
                    model = creditCardAccountBusiness.createNewAccount(userId, card, changeVersion, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
                }
                try {
                    //判定城市是否符合环迅要求
                    String desc=consumeTaskPOJO.get(0).getDescription();
                    String[] desc1=desc.split("\\|");
                    String province=desc1[1];
                    String[] city=province.split("-");
                    String city1=city[1];
                    city1=city1.substring(0,city1.length()-1);
                    String url="http://paymentgateway/v1.0/paymentgateway/topup/hxdhx/querybycity";
                    MultiValueMap requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("city",city1);
                    String result1=restTemplate.postForObject(url,requestEntity,String.class);
                    com.alibaba.fastjson.JSONObject jsonObject=com.alibaba.fastjson.JSONObject.parseObject(result1);
                    String repoCode=jsonObject.getString(CommonConstants.RESP_CODE);
                    if(!CommonConstants.SUCCESS.equals(repoCode)){
                        log.info("不支持的落地城市=="+city1);
                        noSupportBankCount++;
                        continue;
                    }
                    com.alibaba.fastjson.JSONObject resultObj=jsonObject.getJSONObject(CommonConstants.RESULT);
                    String hxProvince=resultObj.getString("province");
                    String hxCity=resultObj.getString("city");
                    String area=hxProvince+"-"+hxCity;
                    log.info("落地城市=="+area);
                    for (int i = 0; i < consumeTaskPOJO.size(); i++) {
//                        ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();
//                        //新生成消费订单对象，存入数据库
//                        if (i == 0) {
//                            consumeTaskNew.setConsumeTaskId(consumeTaskPOJO.get(i).getConsumeTaskId());
//                            consumeTaskNew.setExecuteDateTime(consumeTaskPOJO.get(i).getExecuteDateTime());
//                            consumeTaskNew.setServiceCharge(totalServiceCharge);
//                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
//                        } else {
//                            consumeTaskNew.setConsumeTaskId(consumeTaskPOJO.get(i).getConsumeTaskId());
//                            consumeTaskNew.setExecuteDateTime(consumeTaskPOJO.get(i).getExecuteDateTime());
//                            consumeTaskNew.setServiceCharge(BigDecimal.ZERO);
//                            consumeTaskNew.setRealAmount(consumeTaskPOJO.get(i).getAmount());
//                        }
//                        consumeTaskNew.setRepaymentTaskId(repaymentTaskId1);
//                        consumeTaskNew.setAmount(consumeTaskPOJO.get(i).getAmount());
//                        consumeTaskNew.setExecuteDate(consumeTaskPOJO.get(i).getExecuteDate());
//                        consumeTaskNew.setCreateTime(repaymentTask.getCreateTime());
//                        consumeTaskNew.setDescription("消费计划|"+area);
//                        consumeTaskNew.setUserId(consumeTaskPOJO.get(i).getUserId());
//                        consumeTaskNew.setTaskStatus(0);
//                        consumeTaskNew.setCreditCardNumber(consumeTaskPOJO.get(i).getCreditCardNumber());
//                        consumeTaskNew.setTaskType(consumeTaskPOJO.get(i).getTaskType());
//                        consumeTaskNew.setReturnMessage("");
//                        consumeTaskNew.setOrderStatus(0);
//                        consumeTaskNew.setOrderCode("0");
//                        consumeTaskNew.setErrorMessage("");
//                        consumeTaskNew.setChannelId(channelId);
//                        consumeTaskNew.setChannelTag(channelTag);
//                        consumeTaskNew.setVersion(changeVersion);
//                        consumeTaskNew.setBrandId(consumeTaskPOJO.get(i).getBrandId());
//                        consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew);
//
//                        int n = i + 1;
//                        log.info("消费补单生成第" + n + "消费计划成功======");
//
//                        //将原消费订单置为7，删除状态
//                        consumeTaskPOJO.get(i).setTaskStatus(7);
//                        consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO.get(i));
                        if (i == 0) {
                            consumeTaskPOJO.get(i).setServiceCharge(totalServiceCharge);
                            consumeTaskPOJO.get(i).setRealAmount(consumeTaskPOJO.get(i).getAmount().add(totalServiceCharge).setScale(0,BigDecimal.ROUND_UP));
                        } else {
                            consumeTaskPOJO.get(i).setServiceCharge(BigDecimal.ZERO);
                            consumeTaskPOJO.get(i).setRealAmount(consumeTaskPOJO.get(i).getAmount());
                        }
                        consumeTaskPOJO.get(i).setChannelId(channelId);
                        consumeTaskPOJO.get(i).setChannelTag(channelTag);
                        consumeTaskPOJO.get(i).setVersion(changeVersion);
                        consumeTaskPOJO.get(i).setDescription("消费计划|"+area);
                        consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO.get(i));
                    }

                    //新生成还款订单对象，存入数据库
//                    repaymentTaskNew.setUserId(repaymentTask.getUserId());
//                    repaymentTaskNew.setBrandId(repaymentTask.getBrandId());
//                    repaymentTaskNew.setCreditCardNumber(repaymentTask.getCreditCardNumber());
//                    repaymentTaskNew.setRepaymentTaskId(repaymentTaskId1);
//                    repaymentTaskNew.setOrderCode("0");
//                    repaymentTaskNew.setAmount(repaymentTask.getAmount());
//                    repaymentTaskNew.setRealAmount(BigDecimal.ZERO);
//                    repaymentTaskNew.setRate(rate);
//                    repaymentTaskNew.setServiceCharge(serviceCharge);
//                    repaymentTaskNew.setTotalServiceCharge(totalServiceCharge);
//                    repaymentTaskNew.setReturnServiceCharge(repaymentTask.getReturnServiceCharge());
//                    repaymentTaskNew.setChannelId(channelId);
//                    repaymentTaskNew.setChannelTag(channelTag);
//                    repaymentTaskNew.setTaskType(repaymentTask.getTaskType());
//                    repaymentTaskNew.setTaskStatus(0);
//                    repaymentTaskNew.setOrderStatus(0);
//                    repaymentTaskNew.setDescription(repaymentTask.getDescription());
//                    repaymentTaskNew.setReturnMessage("");
//                    repaymentTaskNew.setErrorMessage("");
//                    repaymentTaskNew.setVersion(changeVersion);
//                    repaymentTaskNew.setExecuteDate(repaymentTask.getExecuteDate());
//                    repaymentTaskNew.setExecuteDateTime(repaymentTask.getExecuteDateTime());
//                    repaymentTaskNew.setCreateTime(repaymentTask.getCreateTime());
                    repaymentTask.setChannelId(channelId);
                    repaymentTask.setChannelTag(channelTag);
                    repaymentTask.setVersion(changeVersion);
                    repaymentTask.setTotalServiceCharge(totalServiceCharge);
                    repaymentTask.setServiceCharge(serviceCharge);
                    repaymentTask.setRate(rate);
                    repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTask);
                    log.info("切换通道修改还款计划成功======");
                    //将原订单号置为7，删除状态
//                    repaymentTask.setTaskStatus(7);
//                    repaymentTaskService.saveRepaymentTaskByRepaymentTask(repaymentTask);
                    log.info("成功生成第" + count + "笔切换通道订单");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
            }
            if (count % 50 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
////                String creditCardNumber=repaymentTask.getCreditCardNumber();
////                String userId=repaymentTask.getUserId();
////                log.info("userId========="+userId+"creditCardNumber=========="+creditCardNumber);
////                //String reservedAmount=new BigDecimal(amount).divide(new BigDecimal("2")).add(new BigDecimal("50")).toString();
////                String reservedAmount=new BigDecimal(amount).toString();
////                log.info("预留金=============="+reservedAmount);
////                String[] executeDate=new String[2];
////                Calendar c = Calendar.getInstance();
////                c.add(Calendar.DAY_OF_MONTH, 1);
//                System.out.println("增加一天后日期:"+sdf.format(c.getTime()));
//                executeDate[0]=sdf.format(c.getTime());
//                executeDate[1]=sdf.format(c.getTime());
//                String version=repaymentTask.getVersion();
//                String city=consumeTask.getDescription().substring(5,consumeTask.getDescription().length());
//                log.info("city==========="+city);
//                JSONObject resultJSONObject=new JSONObject();
//                Object temporaryPlan=creditCardManagerTaskService.createRepaymentTask(request,userId,creditCardNumber,amount,reservedAmount,repaymentTask.getBrandId(),executeDate,version);
//                String taskJSON=temporaryPlan.toString();
//                creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(request,taskJSON,city,amount,reservedAmount,version);
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数==" + consumeTaskPOJOList.size() + "======消费补单计划创建成功，成功数量======" + (count - 1) + "=====不支持银行数量==========" + noSupportBankCount +
                "===未绑卡数===" + noBindCardCount + "=====查询费率失败量====" + queryRateCount);
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 第二笔消费失败补单接口
     *
     * @param message
     * @param executeDate
     * @return
     * @author jayden
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/consumeTask2FailedReplacementOrder/manual")
    @ResponseBody
    public Object consumeTask2FailedReplacementOrder(
            @RequestParam(value = "return_message") String message,
            @RequestParam(value = "execute_date") String executeDate
    ) {

        Map map = new HashMap<String, Object>();
        List<ConsumeTaskPOJO> consumeTaskPOJOList = consumeTaskService.findAbnormalConsumeTaskByMessage(message, 0, 2, executeDate);
        log.info("当前条件异常订单数量=========" + consumeTaskPOJOList.size());
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
        for (ConsumeTaskPOJO consumeTask : consumeTaskPOJOList) {
            String consumeTaskId = consumeTask.getConsumeTaskId();
            long now=System.currentTimeMillis();
            String nowTime1=Long.toString(now);
            String orderCode = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String comsumeTaskId1 = orderCode + "2";
            String comsumeTaskId2 = orderCode + "3";
            String repaymentTaskId1 = orderCode + "1";
            if ("3".equals(consumeTaskId.substring(consumeTaskId.length() - 1))) {
                log.info("进入第二笔消费失败补单===============");
                String repaymentTaskId = consumeTask.getRepaymentTaskId();
                String consumeId2 = consumeTask.getConsumeTaskId();
                //根据还款订单号查询还款订单信息
                RepaymentTaskPOJO repaymentTask = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
                //根据还款订单号查询消费订单信息
                ConsumeTaskPOJO consumeTaskPOJO2 = consumeTaskService.findConsumeTaskByconsumeTaskId2(consumeId2);
                BigDecimal repaymentAmount = consumeTaskPOJO2.getAmount();
                BigDecimal rate = repaymentTask.getRate();
                String version = repaymentTask.getVersion();
                BigDecimal serviceCharge = repaymentTask.getServiceCharge();
                int consumeCount = 1;

                //计算还款订单总费用
                BigDecimal totalServiceCharge = BigDecimal.ZERO;
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
                } else if (consumeCount == 1) {
                    totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
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

                    //将原消费订单置为7，删除状态
//                    consumeTaskPOJO2.setTaskStatus(7);
//                    consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO2);

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
                }
                count++;
            }

        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数" + consumeTaskPOJOList.size() + "========消费补单计划创建成功，成功数量======" + (count - 1));
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 根据还款失败的订单重新生成还款订单（更换订单号）
     *
     * @param executeDate
     * @param version
     * @param description
     * @return
     * @author jayden
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/createRepaymentTask/replacementOrderRepayment")
    @ResponseBody
    public Object replacementOrderRepayment(
            @RequestParam(value = "execute_date") String executeDate,
            @RequestParam(value = "version") String[] version,
            @RequestParam(value = "description") String description
    ) {
        Map map = new HashMap<>();
        List<RepaymentTaskPOJO> repaymentTaskPOJOList = repaymentTaskService.findRepaymentTaskByDescriptionAndVersionAndExeCuteDate(description, version, executeDate, 2, 0);
        log.info("当前条件异常订单数量=========" + repaymentTaskPOJOList.size());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar tomorrow=Calendar.getInstance();
        Calendar r = Calendar.getInstance();
        r.set(2019,7,8,7,10,00);
        tomorrow.add(Calendar.DATE,1);
        String repaymentExecuteTime = DateUtil.getDateStringConvert(new String(), r.getTime(), "yyyy-MM-dd HH:mm:ss");
        log.info("repaymentExecuteTime========"+repaymentExecuteTime);
        String executeDateTomrrow= DateUtil.getDateStringConvert(new String(), tomorrow.getTime(), "yyyy-MM-dd");
        Random random = new Random();
        Date today = new Date();
        String createTime = DateUtil.getDateStringConvert(new String(), today, "yyyy-MM-dd HH:mm:ss");
        int count = 1;
        int successCount = count - 1;
        //int second=0;
        for (RepaymentTaskPOJO repaymentTask : repaymentTaskPOJOList) {
            //String repaymentTaskId = repaymentTask.getRepaymentTaskId();
            long now=System.currentTimeMillis();
            String nowTime1=Long.toString(now);
            String orderCode = DateUtil.getDateStringConvert(new String(), tomorrow.getTime(), "yyyyMMddHHSSS")+nowTime1.substring(nowTime1.length()-2)+random.nextInt(9)+random.nextInt(9);
            log.info("orderCode============" + orderCode);
            String repaymentTaskId1 = orderCode + "1";
            //根据还款订单号查询还款订单信息
//            RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskService.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
//            //根据还款订单号查询消费订单信息
//            List<ConsumeTaskPOJO> consumeTaskPOJO = consumeTaskService.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
//            BigDecimal repaymentAmount = repaymentTask.getAmount();
//            BigDecimal rate = repaymentTask.getRate();
//            String version = repaymentTask.getVersion();
//            BigDecimal serviceCharge = repaymentTask.getServiceCharge();
//            int consumeCount = consumeTaskPOJO.size();
//
//            //计算还款订单总费用
//            BigDecimal totalServiceCharge = BigDecimal.ZERO;
//            if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
//                totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
//            } else if (consumeCount == 1) {
//                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
//            } else {
//                totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentAmount);
//                totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
//            }

//            log.info("consumeTaskPOJO=============" + consumeCount);

            try {
                RepaymentTaskPOJO repaymentTaskNew = new RepaymentTaskPOJO();

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
                repaymentTaskNew.setTotalServiceCharge(repaymentTask.getTotalServiceCharge());
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
                repaymentTaskNew.setExecuteDate(executeDateTomrrow);
                repaymentTaskNew.setExecuteDateTime(repaymentExecuteTime);
                repaymentTaskNew.setCreateTime(createTime);
                repaymentTaskService.saveRepaymentTaskByRepaymentTaskPOJO(repaymentTaskNew);
                log.info("消费补单生成还款计划成功======");
                //将原订单号置为7，删除状态
                repaymentTask.setTaskStatus(7);
                repaymentTaskService.saveRepaymentTaskByRepaymentTask(repaymentTask);
                log.info("成功生成第" + count + "笔消费补单还款订单");
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
        }
        map.put("message", "success");
        map.put("result", "当前条件总失败订单数" + repaymentTaskPOJOList.size() + "========消费补单计划创建成功，成功数量======" + (count - 1));
        map.put("reponseCode", "000000");
        return map;
    }

    /**
     * 获取用户费率
     * @param userId
     * @param brandId
     * @param version
     * @return
     * <p>Description: </p>
     */
    public Map<String,Object> getUserChannelRate(String userId,String brandId,String version){
        return 	baseExecutor.getUserChannelRate(userId, brandId, version);
    }

    public JSONObject getUserInfo(String userId) throws RuntimeException{
        String url = "http://user/v1.0/user/find/by/userid";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();log.error("",e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }

    /**
     * 根据选择通道对账户余额不为0的用户进行批量出款
     * @author jayden
     */
//    @RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/autoRepaymentByVersions")
//    @ResponseBody
//    private Object autoRepayment(@RequestParam(value="versions") String[] versions
//    ) {
//        Map map=new HashMap<>();
//        List<CreditCardManagerConfig> configs = creditCardManagerConfigBusiness.findCardManagerConfigsByVersions(versions);
//        for (CreditCardManagerConfig creditCardManagerConfig : configs) {
//            if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
//                LOG.info("====================version"+creditCardManagerConfig.getVersion()+"自动出款任务开始执行====================");
//                repaymentTaskScanner.executeRepaymentByBlanceThan0(creditCardManagerConfig.getVersion());
//            }
//        }
//        map.put("message","操作成功");
//        map.put("code","000000");
//        return map;
//    }
    /**
     * 紧急处理订单的接口，如果需要使用的话需要修改持久层，我并没有时间去传参
     * @author ives
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/createRepaymentTask/ives")
    @ResponseBody
    public Object replacementOrderRepayment(@RequestParam (value="status")String status){
        if(!"1".equals(status)){
            return null;
        }
        //获取订单
        List<ConsumeTaskPOJO> consumeTaskPOJOList=consumeTaskService.findByives();
        log.info("=========="+consumeTaskPOJOList.size());
        int count=0;
        int minute = 0;
        for(ConsumeTaskPOJO consumeTaskPOJO:consumeTaskPOJOList){
            count++;
            BigDecimal con2=consumeTaskPOJO.getRealAmount().divide(new BigDecimal("2"));
            consumeTaskPOJO.setRealAmount(con2);
            consumeTaskPOJO.setAmount(con2.subtract(consumeTaskPOJO.getServiceCharge()));
            consumeTaskService.saveConsumeTaskByconsumeTask(consumeTaskPOJO);

            String o=consumeTaskPOJO.getConsumeTaskId();
            String orderCode=o.substring(0,o.length()-1);
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(consumeTaskPOJO.getExecuteDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar c1=Calendar.getInstance();
            c1.setTime(date);
            c1.add(Calendar.MINUTE, minute + 20);
            String consume1ExecuteTime = DateUtil.getDateStringConvert(new String(), c1.getTime(), "yyyy-MM-dd HH:mm:ss");
            ConsumeTaskPOJO consumeTaskNew = new ConsumeTaskPOJO();
            consumeTaskNew.setExecuteDateTime(consume1ExecuteTime);
            consumeTaskNew.setServiceCharge(new BigDecimal("0.00"));
            consumeTaskNew.setAmount(con2);
            consumeTaskNew.setRealAmount(con2);
            consumeTaskNew.setConsumeTaskId(orderCode+"4");
            consumeTaskNew.setRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId());
            consumeTaskNew.setExecuteDate(consumeTaskPOJO.getExecuteDate());
            consumeTaskNew.setCreateTime(consumeTaskPOJO.getCreateTime());
            consumeTaskNew.setDescription("消费分批"+consumeTaskPOJO.getDescription());
            consumeTaskNew.setUserId(consumeTaskPOJO.getUserId());
            consumeTaskNew.setTaskStatus(0);
            consumeTaskNew.setCreditCardNumber(consumeTaskPOJO.getCreditCardNumber());
            consumeTaskNew.setTaskType(consumeTaskPOJO.getTaskType());
            consumeTaskNew.setReturnMessage("");
            consumeTaskNew.setOrderStatus(0);
            consumeTaskNew.setOrderCode("0");
            consumeTaskNew.setErrorMessage("");
            consumeTaskNew.setChannelId(consumeTaskPOJO.getChannelId());
            consumeTaskNew.setChannelTag(consumeTaskPOJO.getChannelTag());
            consumeTaskNew.setVersion(consumeTaskPOJO.getVersion());
            consumeTaskNew.setBrandId(consumeTaskPOJO.getBrandId());
            consumeTaskService.saveConsumeTaskByconsumeTaskPOJO(consumeTaskNew);
            log.info("============第"+count+"笔计划搞定====================");
            if(count%50==0){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }


        return null;
    }

    @RequestMapping(method = RequestMethod.POST,value="/v1.0/creditcardmanager/replaceorder/createAccount")
    @ResponseBody
    public Object processCreditAccout(
            @RequestParam(value="version") String version,
            @RequestParam(value="execute_date") String executeDate
    ){
        Map map=new HashMap<>();
        List<ConsumeTaskPOJO> consumeTaskPOJO=consumeTaskService.findByVersion(version,executeDate);
        log.info("查询订单用户数量"+consumeTaskPOJO.size());
        String userId="";
        String card="";
        String brandId="";
        int count=0;
        for(ConsumeTaskPOJO data:consumeTaskPOJO) {
            userId=data.getUserId();
            card=data.getCreditCardNumber();
            //获取当前用户信用卡信息
            JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, card);
            if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
                continue;
            }
            JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
            int billDate = resultBankCardJSONObject.getInt("billDay");
            int repaymentDate = resultBankCardJSONObject.getInt("repaymentDay");
            int creditBlance = resultBankCardJSONObject.getInt("creditBlance");
            String bankName = resultBankCardJSONObject.getString("bankName");

            CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, card, version);
            if (model == null) {
                String phone = null;
                try {
                    JSONObject userInfo = this.getUserInfo(userId);
                    resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                    phone = resultJSONObject.getString("phone");
                    brandId = resultJSONObject.getString("brandId");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    log.error("获取用户信息失败!", e);
//                        return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
                    continue;
                }
                try {
                    model = creditCardAccountBusiness.createNewAccount(userId, card, version, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
                    count++;
                    log.info("新增成功====user_id=="+userId);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"查询成功");
        return map;
    }

}
