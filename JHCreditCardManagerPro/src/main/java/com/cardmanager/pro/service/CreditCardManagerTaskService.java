package com.cardmanager.pro.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.*;
import com.cardmanager.pro.channel.ChannelFactory;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.executor.ConsumeExecutor;
import com.cardmanager.pro.executor.RepaymentExecutor;
import com.cardmanager.pro.pojo.*;
import com.cardmanager.pro.scanner.ConsumeTaskScanner;
import com.cardmanager.pro.util.CardConstss;
import com.cardmanager.pro.util.RestTemplateUtil;
import com.cardmanager.pro.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class CreditCardManagerTaskService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplateUtil util;

    @Autowired
    private CreditCardAccountBusiness creditCardAccountBusiness;

    @Autowired
    private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;

    @Autowired
    private RepaymentExecutor repaymentExecutor;

    @Autowired
    private ConsumeExecutor consumeExecutor;

    @Autowired
    private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

    @Autowired
    private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;

    @Autowired
    private RepaymentBillBusiness repaymentBillBusiness;

    @Autowired
    private ConsumeTaskScanner consumeTaskScanner;

    @Autowired
    private CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BaseExecutor baseExecutor;

    @Autowired
    private DeductionChargeBusiness deductionChargeBusiness;

    @Autowired
    private RepaymentDetailBusiness repaymentDetailBusiness;

    @Autowired
    private Util arrayUtil;

    /**
     * 前端调用   查询用户费率/手续费接口
     *
     * @param request
     * @param userId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/query/rate/by/userid")
    public @ResponseBody
    Object getChannelRateByUserId(HttpServletRequest request,
                                  @RequestParam("userId") String userId,
                                  @RequestParam("brandId") String brandId,
                                  @RequestParam(value = "version") String version
    ) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            return userChannelRate;
        }
        JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        //获取成本费率
        String extraFeeStr = resultJSONObject.getString("extraFee");
        //额外费率
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        //成本费率+额外费率向上取整2位
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        //费率4位
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
        map.put("serviceCharge", serviceCharge);
        map.put("rate", rate);
        return map;
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     * @throws RuntimeException
     */
    public JSONObject getUserInfo(String userId) throws RuntimeException {
        String url = "http://user/v1.0/user/find/by/userid";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            throw new RuntimeException(e);
        }
        return resultJSONObject;

    }

    /**
     * 验证是否有计划执行
     *
     * @param userId
     * @param creditCardNumber
     * @param version
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/doeshave/task/execute")
    public @ResponseBody
    Object doesHaveTaskExecute(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "creditCardNumber") String creditCardNumber,
            @RequestParam(value = "version") String version
    ) {
//		验证是否有批量生成的未执行计划
        boolean doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(userId, creditCardNumber, version);
        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(userId, creditCardNumber, version);
        if (doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO || doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            return ResultWrap.init(CommonConstants.FALIED, "您有未执行计划,请等待任务执行完后再生成计划!");
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "验证成功");
    }

    /**
     * 前端调用	生成前端展示的临时任务
     *
     * @param request
     * @param suserId
     * @param creditCardNumber
     * @param strExecuteDates
     * @param amount
     * @param brandId
     * @param scount
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/create/temporary/plan")
    public @ResponseBody
    Object generationTemporaryPlan(HttpServletRequest request,
                                   @RequestParam(value = "userId") String suserId,
                                   @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                   @RequestParam(value = "executeDate") String[] strExecuteDates,
                                   @RequestParam(value = "amount") String amount,
                                   @RequestParam(value = "brandId") String brandId,
                                   @RequestParam(value = "count") String scount,
                                   @RequestParam(value = "version") String version) {
        int number = 10;
        amount = amount.trim();
        scount = scount.trim();
        suserId = suserId.trim();
        creditCardNumber = creditCardNumber.trim();

        int repaymentCountLimit = 0;
        int consumeSingleMoneyLimit = 0;
        int consumeCountLimit = 0;
        String consumeChannelId = null;
        String repaymentChannelId = null;
        String consumeChannelTag = null;
        String repaymentChannelTag = null;

//		根据brandId查找配置信息
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        if (creditCardManagerConfig != null) {
//			消费配置信息
            //单笔消费限制
            consumeSingleMoneyLimit = creditCardManagerConfig.getConSingleLimitMoney().intValue();
            //消费次数限制
            consumeCountLimit = creditCardManagerConfig.getConSingleLimitCount();
            //消费通道id
            consumeChannelId = creditCardManagerConfig.getChannelId();
            //消费通道品牌
            consumeChannelTag = creditCardManagerConfig.getChannelTag();
//			还款配置信息
            //还款数量限制
            repaymentCountLimit = creditCardManagerConfig.getPaySingleLimitCount();
            //还款通道id
            repaymentChannelId = creditCardManagerConfig.getChannelId();
            //还款通道品牌
            repaymentChannelTag = creditCardManagerConfig.getChannelTag();
            //通道状态 1开  2闭
            int createOnOff = creditCardManagerConfig.getCreateOnOff();
            if (1 != createOnOff) {
                return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
        }

        Map<String, Object> map = new HashMap<>();
//		验证是否有批量生成的未执行计划
        boolean doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(suserId, creditCardNumber, version);
        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(suserId, creditCardNumber, version);
        if (doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO || doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您有未执行计划,请等待任务执行完后再生成计划!");
            return map;
        }

//		if (!CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version)) {
////			验证是否有首笔验证的已完成执行计划
////			null 没有 !null 有
//			ConsumeTaskPOJO firstConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0ConsumeTaskPOJO(suserId, creditCardNumber,version);
////			null 没有 !null 有
//			RepaymentTaskPOJO firstRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0RepaymentTaskPOJO(suserId, creditCardNumber,version);
//			if(firstConsumeTaskPOJO == null || firstRepaymentTaskPOJO == null){
//				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//				map.put(CommonConstants.RESP_MESSAGE, "您未完成首笔验证,请等待计划执行完后再生成计划!");
//				return map;
//			}
//		}


//		验证日期格式是否正确
        Date[] executeDates = new Date[strExecuteDates.length];
        try {
            for (int i = 0; i < strExecuteDates.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new Date(), strExecuteDates[i], "yyyy-MM-dd");
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:选择日期格式有误,正确格式为:2000-01-01");
            return map;
        }
//		获取用户账单日和还款日
        CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(suserId, creditCardNumber, version);
        //账单日
        Integer billDate = creditCardAccount.getBillDate();
        //还款日
        Integer repaymentDate = creditCardAccount.getRepaymentDate();

        Date dateNow = DateUtil.getDateStringConvert(new Date(), DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
        dateNow = new Date(dateNow.getTime() + 24 * 60 * 60 * 1000);
        //执行日期 设置为0
        Integer executeDay = 0;
//		验证日期是否是今天以后
        for (int i = 0; i < executeDates.length; i++) {
            if (dateNow.getTime() > executeDates[i].getTime()) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "只能选择今天以后的日期,请重新选择!");
                return map;
            }
//			如果有选择账单日和还款日,则验证任务执行日期是否在账单日之后还款日之前
            if (billDate != 0 && repaymentDate != 0) {
                executeDay = Integer.valueOf(DateUtil.getDateStringConvert(new String(), executeDates[i], "dd"));
                if (billDate > repaymentDate) {
                    if (!((billDate <= executeDay) || (repaymentDate >= executeDay))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "执行任务的日期只能为账单日之后还款日之前,请在该日期之间进行选择");
                        return map;
                    }
                } else {
                    if (!((billDate <= executeDay) && (repaymentDate >= executeDay))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "执行任务的日期只能为账单日之后还款日之前,请在该日期之间进行选择");
                        return map;
                    }
                }
            }
        }

        // 还款笔数验证
        int count;
        try {
            count = Integer.valueOf(scount);
        } catch (NumberFormatException e1) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "还款笔数输入不正确,请重新输入!");
            return map;
        }
        if (!(count != 0 && count >= strExecuteDates.length && count <= strExecuteDates.length * repaymentCountLimit)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:选择的日期所对应的还款笔数应大于等于" + strExecuteDates.length + "笔而且小于等于" + strExecuteDates.length * repaymentCountLimit + "笔");
            return map;
        }
//		金额验证
        BigDecimal totalAmount;
        try {
            totalAmount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_UP);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您输入的金额有误,请重新输入!");
            return map;

        }
//		总金额验证                       平均每笔金额= 总金额/笔数  .5向下取整2位
        BigDecimal perAveCountAmount = totalAmount.divide(new BigDecimal(scount), 2, BigDecimal.ROUND_HALF_DOWN);
        if (perAveCountAmount.compareTo(new BigDecimal(consumeSingleMoneyLimit * consumeCountLimit + number)) < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "输入金额过小,请增加输入金额或减少笔数");
            return map;
        }
//		信用卡验证
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request");
        String url = uri.toString() + "/v1.0/user/bank/verify/isuseable";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", suserId);
        requestEntity.add("bankCardNumber", creditCardNumber);
        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSONObject = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "生成计划失败,原因:该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
            return map;
        }

        JSONObject bankInfoJSON = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        String bankName = bankInfoJSON.getString("bankName");
        //获取支持的银行
        Map<String, Object> verifyDoesSupportBank = creditCardManagerAuthorizationHandle.verifyDoesSupportBank(version, bankName);
        if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
            return verifyDoesSupportBank;
        }

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(suserId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            return userChannelRate;
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//		单笔还款手续费
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
//		费率
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);

        // 减去每笔最低金额剩余总金额
//		BigDecimal scale = perAveCountAmount.divide(new BigDecimal((consumeSingleMoneyLimit * consumeCountLimit + number)),2,BigDecimal.ROUND_DOWN).setScale(0, BigDecimal.ROUND_DOWN);
//		BigDecimal surplusCountAmount = totalAmount.subtract(new BigDecimal(consumeSingleMoneyLimit * consumeCountLimit).multiply(new BigDecimal(count)).multiply(scale));
        BigDecimal surplusCountAmount = null;
        //预计还款金额乘以0.1
        surplusCountAmount = totalAmount.multiply(BigDecimal.valueOf(0.1)).setScale(0, BigDecimal.ROUND_DOWN);
        //获取每笔的平均金额
        BigDecimal preAvgAmount = totalAmount.subtract(surplusCountAmount).divide(BigDecimal.valueOf(count), 0, BigDecimal.ROUND_DOWN);
        //预计还款金额减去平均金额减去预留金额
        BigDecimal pointAmount = totalAmount.subtract(surplusCountAmount).subtract(preAvgAmount.multiply(BigDecimal.valueOf(count)));
        // 盈余金额/还款笔数 *1  最大每笔盈余金额
        int max = surplusCountAmount.divide(BigDecimal.valueOf(count), 0, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(1)).intValue();
        // 盈余金额/还款笔数 * 0.8 取整   最小每笔盈余金额
        int min = surplusCountAmount.divide(BigDecimal.valueOf(count), 0, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(0.8)).intValue();

        // 减去一笔还款中最低消费总金额剩余一笔还款的金额
        BigDecimal surplusSubCountAmount = null;
        // 随机每笔金额
        BigDecimal randomCountAmount = BigDecimal.ZERO;
        // 随机每次消费金额
        BigDecimal randomSubCountAmount = BigDecimal.ZERO;
        // 每笔计数金额
        BigDecimal[][] perCountAmount = new BigDecimal[count][consumeCountLimit + 1];

        Random random = new Random();
        // 分配每笔还款的金额
        for (int i = 0; i < count; i++) {
            //最小每笔盈余金额+ 0-1的小数* 1+最大-最小
            int result = min + (int) (Math.random() * ((max - min) + 1));
            if (i != count - 1) {
                /**
                 * compareTo ：前者>后者 1     后者>前者 -1   前者=后者 0
                 */
                if (surplusCountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    randomCountAmount = new BigDecimal(result);
                    surplusCountAmount = surplusCountAmount.subtract(randomCountAmount);
                    perCountAmount[i][0] = preAvgAmount.add(randomCountAmount);
                } else {
                    perCountAmount[i][0] = preAvgAmount;
                }
            } else {
                //最后一笔还款  平均每笔金额+盈余金额+点数
                perCountAmount[i][0] = preAvgAmount.add(surplusCountAmount).add(pointAmount);
            }


//			System.out.println("perCountAmount=============:" + perCountAmount[i][0]);
            surplusSubCountAmount = perCountAmount[i][0].subtract(new BigDecimal(consumeCountLimit * consumeSingleMoneyLimit));
            // 分配每笔还款的消费任务金额
            for (int j = 0; j < consumeCountLimit; j++) {
                if (j != consumeCountLimit - 1) {
//					System.out.println("surplusSubCountAmount====:"+surplusSubCountAmount);
                    if (surplusSubCountAmount.compareTo(BigDecimal.ZERO) > 0) {
                        int max2 = surplusSubCountAmount.multiply(BigDecimal.valueOf(0.6)).intValue();
                        int min2 = surplusSubCountAmount.multiply(BigDecimal.valueOf(0.4)).intValue();
                        result = min2 + (int) (Math.random() * ((max2 - min2) + 1));
                        randomSubCountAmount = new BigDecimal(result);
                        surplusSubCountAmount = surplusSubCountAmount.subtract(randomSubCountAmount);
                        perCountAmount[i][j + 1] = new BigDecimal(consumeSingleMoneyLimit).add(randomSubCountAmount);
                    } else {
                        perCountAmount[i][j + 1] = new BigDecimal(consumeSingleMoneyLimit);
                    }
                } else {
                    perCountAmount[i][j + 1] = new BigDecimal(consumeSingleMoneyLimit).add(surplusSubCountAmount);
                }

//				消费金额上限限制
                if (CardConstss.CARD_VERSION_1.equals(version) || CardConstss.CARD_VERSION_8.equals(version) || CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version) || CardConstss.CARD_VERSION_14.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(980)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }
					/*if(bankName!= null && bankName.contains("光大")){
						if(BigDecimal.valueOf(2000).compareTo(perCountAmount[i][j+1]) < 0){
							map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,光大银行信用卡单笔消费不能超过2000元,请减少还款金额,或者增加还款天数和笔数再重新生成计划");
							return map;
						}
					}else if(bankName!= null && bankName.contains("农业") || bankName.contains("招商")){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
						return map;
					}*/
                } else if (CardConstss.CARD_VERSION_2.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单笔消费不能超过1000元,请减少还款金额,或者减少预留金额百分比再重新生成计划!");
                        return map;
                    }
                    if (bankName != null && bankName.contains("交通")) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
                        return map;
                    } else if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                        if (BigDecimal.valueOf(400).compareTo(perCountAmount[i][0]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单日消费不能超过400元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }

                } else if (CardConstss.CARD_VERSION_5.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(9800)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }
                } else if (CardConstss.CARD_VERSION_7.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(4450)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }

                    if (bankName != null && bankName.contains("华夏")) {
                        if (BigDecimal.valueOf(2450).compareTo(perCountAmount[i][j + 1]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                            return map;
                        }
                    }

                    if (bankName != null && bankName.contains("平安")) {
                        if (BigDecimal.valueOf(1275).compareTo(perCountAmount[i][j + 1]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                            return map;
                        }
                    }
                } else if (CardConstss.CARD_VERSION_17.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }
                } else if (CardConstss.CARD_VERSION_18.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }
                    if (bankName != null && (bankName.contains("交通") || bankName.contains("花旗"))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
                        return map;
                    } else if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                        if (BigDecimal.valueOf(500).compareTo(perCountAmount[i][j + 1]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单日消费不能超过500元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }
                } else if (CardConstss.CARD_VERSION_19.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                        return map;
                    }
                    if (bankName != null && (bankName.contains("交通") || bankName.contains("花旗"))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
                        return map;
                    } else if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                        if (BigDecimal.valueOf(500).compareTo(perCountAmount[i][j + 1]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单日消费不能超过500元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }
                }
            }

            if (CardConstss.CARD_VERSION_15.equals(version)) {
                if (perCountAmount[i][0].compareTo(BigDecimal.valueOf(4900)) > 0) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "输入金额过大,请减少输入金额或增加还款笔数");
                    return map;
                }
            }
        }
        Date nowTime = new Date();
        // 初始化还款任务
        RepaymentTaskVO[] repaymentTaskVOs = new RepaymentTaskVO[count];
        for (int i = 0; i < count; i++) {
            // 对金额进行取角
            repaymentTaskVOs[i] = new RepaymentTaskVO();

//			repaymentTaskVOs[i].setConsumeTaskVOs(new ConsumeTaskVO[consumeCountLimit]);
            // 设置userId
            repaymentTaskVOs[i].setUserId(suserId);
            // 设置还款卡号
            repaymentTaskVOs[i].setCreditCardNumber(creditCardNumber);
            // 设置还款通道id
            repaymentTaskVOs[i].setChannelId(repaymentChannelId);
            // 设置还款通道tag
            repaymentTaskVOs[i].setChannelTag(repaymentChannelTag);
            // 设置还款金额
            repaymentTaskVOs[i].setAmount(perCountAmount[i][0]);
            // 设置还款手续费
            repaymentTaskVOs[i].setServiceCharge(serviceCharge);
            // 设置消费费率
            repaymentTaskVOs[i].setRate(rate);
            // 设置总手续费
            BigDecimal totalServiceCharge = BigDecimal.ZERO;
            if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                //总服务费=获取还款金额*费率+服务费
                totalServiceCharge = repaymentTaskVOs[i].getAmount().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
            } else {
                //总服务费=还款金额+服务费/1-费率 -还款金额
                totalServiceCharge = repaymentTaskVOs[i].getAmount().add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentTaskVOs[i].getAmount());
                //总服务费= 总服务费+0.01
                totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
            }

            repaymentTaskVOs[i].setTotalServiceCharge(totalServiceCharge);
            // 设置任务描述
            repaymentTaskVOs[i].setDescription("还款计划");
            // 设置创建时间
            repaymentTaskVOs[i].setCreateTime(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:mm:ss"));
        }

        //随机分配执行日期
        Date[] executeTime = new Date[strExecuteDates.length];
        for (int i = 0; i < strExecuteDates.length; i++) {
//			executeTime[i] = DateUtil.getDateFromStr(strExecuteDates[i]);
            executeTime[i] = DateUtil.getDateStringConvert(new Date(), strExecuteDates[i], "yyyy-MM-dd");
            repaymentTaskVOs[i].setExecuteDate(strExecuteDates[i]);
        }
//		还款笔数和日期的差值,差值笔数将随机分配至各天
        int differencCount = count - strExecuteDates.length;
        int index = 0;
        if (differencCount != 0 && differencCount == strExecuteDates.length) {
            int j = 0;
            for (int i = strExecuteDates.length; i < count; i++) {
                repaymentTaskVOs[i].setExecuteDate(strExecuteDates[j]);
                j++;
            }
        } else if (differencCount != 0) {
            for (int i = strExecuteDates.length; i < count; i++) {
                index = random.nextInt(strExecuteDates.length);
                if (strExecuteDates[index] != null) {
                    repaymentTaskVOs[i].setExecuteDate(strExecuteDates[index]);
                    strExecuteDates[index] = null;
                } else {
                    for (int j = 0; j < strExecuteDates.length; j++) {
                        if (strExecuteDates[j] != null) {
                            repaymentTaskVOs[i].setExecuteDate(strExecuteDates[j]);
                            strExecuteDates[j] = null;
                            break;
                        }
                    }
                }
            }
        }
        // 随机分配执行时间
        String initTime = null;
        String executeDateTime = null;
        Date initDateTime = null;
        int randomInt = 0;
        for (int i = 0; i < count; i++) {
            if (i >= strExecuteDates.length) {
                if (CardConstss.CARD_VERSION_2.equals(version)) {
                    initTime = " 14:30:00";
                } else if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version)) {
                    initTime = " 15:00:00";
                } else if (CardConstss.CARD_VERSION_4.equals(version)) {
                    initTime = " 15:30:00";
                } else {
                    initTime = " 16:00:00";
                }
            } else {
                initTime = " 09:00:00";
            }

            // 设置还款任务id
            repaymentTaskVOs[i].setRepaymentTaskId(repaymentTaskVOs[i].getExecuteDate().replace("-", "")
                    + DateUtil.getDateStringConvert(new String(), new Date(), "HHSSS")
                    + random.nextInt(9)
                    + random.nextInt(9)
                    + random.nextInt(9) + i + "1");
            // 设置消费子任务字段

//			String[] consumeTypeName = {"娱乐","购物","其他","通信","交通","住宿","餐饮"};
            List<String> consumeTypeName = new ArrayList<>();
            consumeTypeName.add("|娱乐");
            consumeTypeName.add("|购物");
            consumeTypeName.add("|其他");
            consumeTypeName.add("|通信");
            consumeTypeName.add("|交通");
            consumeTypeName.add("|住宿");
            consumeTypeName.add("|餐饮");
            for (int j = 0; j < consumeCountLimit; j++) {
//				repaymentTaskVOs[i].getConsumeTaskVOs()[j] = new ConsumeTaskVO();
                repaymentTaskVOs[i].getConsumeTaskVOs().add(new ConsumeTaskVO());
//				ConsumeTaskVO consumeTaskVO = repaymentTaskVOs[i].getConsumeTaskVOs()[j];
                ConsumeTaskVO consumeTaskVO = repaymentTaskVOs[i].getConsumeTaskVOs().get(j);
                // 设置userId
                consumeTaskVO.setUserId(suserId);
                // 设置消费通道id
                consumeTaskVO.setChannelId(consumeChannelId);
                // 设置消费通道tag
                consumeTaskVO.setChannelTag(consumeChannelTag);
                // 设置还款任务id
                consumeTaskVO.setRepaymentTaskId(repaymentTaskVOs[i].getRepaymentTaskId());
                // 设置消费卡号
                consumeTaskVO.setCreditCardNumber(creditCardNumber);
                // 设置消费任务
                consumeTaskVO.setDescription("消费计划");
                // 设置消费类型
                randomInt = new Random().nextInt(consumeTypeName.size());
                consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
                consumeTypeName.remove(randomInt);

                // 设置消费金额
                consumeTaskVO.setAmount(perCountAmount[i][j + 1]);
                //一半的服务费=从还款任务中取服务费/2
                BigDecimal halfServiceCharge = repaymentTaskVOs[i].getServiceCharge().divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_UP);
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    //消费服务费 = 消费金额*费率
                    BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
                    //真是金额 = 消费金额+服务费+一半的服务费
                    consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge).add(halfServiceCharge));
                    consumeTaskVO.setServiceCharge(consumeServiceCharge);
                } else if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
                    //消费服务费 = 消费金额+
                    BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().add(j == 0 ? repaymentTaskVOs[i].getServiceCharge() : BigDecimal.ZERO).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(consumeTaskVO.getAmount());
                    //真实金额= 消费金额+消费服务费
                    consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge));
                    //消费服务费
                    consumeTaskVO.setServiceCharge(consumeServiceCharge);
                } else {
                    if (j == 0) {
                        //设置真实金额=总金额+总服务费
                        consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(repaymentTaskVOs[i].getTotalServiceCharge()));
                        //设置服务费
                        consumeTaskVO.setServiceCharge(repaymentTaskVOs[i].getTotalServiceCharge());
                    } else {
                        //设置真实金额
                        consumeTaskVO.setRealAmount(consumeTaskVO.getAmount());
                    }
                }
                // 设置执行日期
                consumeTaskVO.setExecuteDate(repaymentTaskVOs[i].getExecuteDate());
                // 设置创建时间
                consumeTaskVO.setCreateTime(repaymentTaskVOs[i].getCreateTime());
                // 设置消费子任务id
                consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVOs[i].getRepaymentTaskId()) + (j + 1) + "");

                initDateTime = DateUtil.getDateStringConvert(new Date(), consumeTaskVO.getExecuteDate() + initTime, "yyyy-MM-dd HH:mm:ss");
                long minRandomTime = 30 * 60 * 1000;
                if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                    minRandomTime = 40 * 60 * 1000;
                }
                executeDateTime = DateUtil.getDateStringConvert(new String(), new Date(initDateTime.getTime() + minRandomTime + (random.nextInt(40 * 60 * 1000))), "yyyy-MM-dd HH:mm:ss");
                // 设置消费子任务执行日期时间
                consumeTaskVO.setExecuteDateTime(executeDateTime);
                initTime = executeDateTime.substring(executeDateTime.indexOf(" "));
            }
            initDateTime = DateUtil.getDateStringConvert(new Date(), repaymentTaskVOs[i].getExecuteDate() + initTime, "yyyy-MM-dd HH:mm:ss");
            long minRandomTime = 30 * 60 * 1000;
            if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
                minRandomTime = 40 * 60 * 1000;
            }
            executeDateTime = DateUtil.getDateStringConvert(new String(), new Date(initDateTime.getTime() + minRandomTime + (random.nextInt(40 * 60 * 1000))), "yyyy-MM-dd HH:mm:ss");
            // 设置还款子任务执行时间
            repaymentTaskVOs[i].setExecuteDateTime(executeDateTime);
        }
        Arrays.sort(repaymentTaskVOs);
        BigDecimal reservedAmount = BigDecimal.ZERO;
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        BigDecimal allConsumeAmount = BigDecimal.ZERO;
        Date minDate = null;
        Date maxDate = null;
        int consumeCount = 0;
        int i = 0;
        for (RepaymentTaskVO repaymentTaskVO : repaymentTaskVOs) {
            Date executeDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");

            if (i == 0) {
                reservedAmount = repaymentTaskVO.getAmount();
                minDate = executeDate;
                maxDate = executeDate;
            }

            if (minDate.compareTo(executeDate) > 0) {
                minDate = executeDate;
            }
            if (maxDate.compareTo(executeDate) < 0) {
                maxDate = executeDate;
            }

            if (repaymentTaskVO.getAmount().compareTo(reservedAmount) < 0) {
                reservedAmount = repaymentTaskVO.getAmount();
            }
            totalServiceCharge = totalServiceCharge.add(repaymentTaskVO.getTotalServiceCharge());
            List<ConsumeTaskVO> consumeTaskVOs = repaymentTaskVO.getConsumeTaskVOs();
            for (ConsumeTaskVO consumeTaskVO2 : consumeTaskVOs) {
                allConsumeAmount = allConsumeAmount.add(consumeTaskVO2.getRealAmount());
                consumeCount++;
            }
            i++;
        }

        List<String> executeDateStr = new ArrayList<>();
        for (Date date : executeTime) {
            executeDateStr.add(DateUtil.getDateStringConvert(new String(), date, "yyyy-MM-dd"));
        }

        map.put("userId", suserId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amount);
        map.put("reservedAmount", reservedAmount);
        map.put("brandId", brandId);
        map.put("executeDate", executeDateStr);
        map.put("totalServiceCharge", totalServiceCharge);
        map.put("version", version);
        map.put("rate", rate.toString());
        map.put("serviceCharge", serviceCharge);
        map.put("bankName", bankName);

        String executeDatess = DateUtil.getDateStringConvert(new String(), minDate, "yyyy/MM/dd") + "-" + DateUtil.getDateStringConvert(new String(), maxDate, "yyyy/MM/dd");
        map.put("allConsumeAmount", allConsumeAmount);
        map.put("allRepaymentAmount", amount);
        map.put("allServiceCharge", totalServiceCharge);
        map.put("consumeCount", consumeCount);
        map.put("repaymentCount", repaymentTaskVOs.length);
        map.put("executeDates", executeDatess);
        map.put("creditCardNumber", creditCardNumber);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "生成计划成功");
        map.put(CommonConstants.RESULT, repaymentTaskVOs);
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/create/temporary/percent/plan")
    public @ResponseBody
    Object generationTemporaryPlan2(HttpServletRequest request,
                                    @RequestParam(value = "userId") String suserId,
                                    @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                    @RequestParam(value = "executeDate") String[] strExecuteDates,
                                    @RequestParam(value = "amount") String amount,
                                    @RequestParam(value = "brandId") String brandId,
                                    @RequestParam(value = "count") String scount,
                                    @RequestParam(value = "version") String version
    ) {
        amount = amount.trim();
        scount = scount.trim();
        suserId = suserId.trim();
        BigDecimal amountPercent = new BigDecimal(scount).setScale(2, BigDecimal.ROUND_DOWN);
        BigDecimal preAmount = amountPercent.multiply(new BigDecimal(amount));
        int count = new BigDecimal(amount).divide(preAmount, 0, BigDecimal.ROUND_DOWN).intValue() + 1;

        creditCardNumber = creditCardNumber.trim();

        int repaymentCountLimit = 0;
        int repaysumeSingleMoneyLimit = 0;
        int consumeCountLimit = 0;
        String consumeChannelId = null;
        String repaymentChannelId = null;
        String consumeChannelTag = null;
        String repaymentChannelTag = null;

//		根据brandId查找配置信息
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        if (creditCardManagerConfig != null) {
            // 消费配置信息
            consumeCountLimit = creditCardManagerConfig.getConSingleLimitCount();
            consumeChannelId = creditCardManagerConfig.getChannelId();
            consumeChannelTag = creditCardManagerConfig.getChannelTag();
            // 还款配置信息
            repaymentCountLimit = creditCardManagerConfig.getPaySingleLimitCount();
            repaysumeSingleMoneyLimit = creditCardManagerConfig.getPaySingleLimitMoney().intValue();
            repaymentChannelId = creditCardManagerConfig.getChannelId();
            repaymentChannelTag = creditCardManagerConfig.getChannelTag();
            int createOnOff = creditCardManagerConfig.getCreateOnOff();
            if (1 != createOnOff) {
                return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
        }

        Map<String, Object> map = new HashMap<>();
//		验证是否有批量生成的未执行计划
        boolean doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(suserId, creditCardNumber, version);
        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(suserId, creditCardNumber, version);
        if (doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO || doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您有未执行计划,请等待任务执行完后再生成计划!");
            return map;
        }
//		验证是否有首笔验证的已完成执行计划
//		null 没有 !null 有
        ConsumeTaskPOJO firstConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0ConsumeTaskPOJO(suserId, creditCardNumber, version);
//		null 没有 !null 有
        RepaymentTaskPOJO firstRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0RepaymentTaskPOJO(suserId, creditCardNumber, version);
        if (firstConsumeTaskPOJO == null || firstRepaymentTaskPOJO == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您未完成首笔验证,请等待计划执行完后再生成计划!");
            return map;
        }
//		验证日期格式是否正确
        Date[] executeDates = new Date[strExecuteDates.length];
        try {
            for (int i = 0; i < strExecuteDates.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new Date(), strExecuteDates[i], "yyyy-MM-dd");
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:选择日期格式有误,正确格式为:2000-01-01");
            return map;
        }
//		获取用户账单日和还款日
        CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(suserId, creditCardNumber, version);
        Integer billDate = creditCardAccount.getBillDate();
        Integer repaymentDate = creditCardAccount.getRepaymentDate();

        Date dateNow = DateUtil.getDateStringConvert(new Date(), DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
        dateNow = new Date(dateNow.getTime() + 24 * 60 * 60 * 1000);
        Integer executeDay = 0;
//		验证日期是否是今天以后
        for (int i = 0; i < executeDates.length; i++) {
            if (dateNow.getTime() > executeDates[i].getTime()) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "只能选择今天以后的日期,请重新选择!");
                return map;
            }
//			如果有选择账单日和还款日,则验证任务执行日期是否在账单日之后还款日之前
            if (billDate != 0 && repaymentDate != 0) {
                executeDay = Integer.valueOf(DateUtil.getDateStringConvert(new String(), executeDates[i], "dd"));
                if (billDate > repaymentDate) {
                    if (!((billDate <= executeDay) || (repaymentDate >= executeDay - 2))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "执行任务的日期只能为账单日之后至还款日前两天,请在该日期之间进行选择");
                        return map;
                    }
                } else {
                    if (!((billDate <= executeDay) && (repaymentDate >= executeDay - 2))) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "执行任务的日期只能为账单日之后至还款日前两天,请在该日期之间进行选择");
                        return map;
                    }
                }
            }
        }

        // 还款笔数验证
        int count2;
        try {
            count2 = strExecuteDates.length * repaymentCountLimit;
        } catch (NumberFormatException e1) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "还款笔数输入不正确,请重新输入!");
            return map;
        }
        if (count > count2) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:选择的日期所对应的还款笔数应大于等于" + strExecuteDates.length + "笔而且小于等于" + strExecuteDates.length * repaymentCountLimit + "笔");
            return map;
        }
//		金额验证
        BigDecimal totalAmount;
        try {
            totalAmount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_UP);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您输入的金额有误,请重新输入!");
            return map;

        }
//		总金额验证
//		BigDecimal perAveCountAmount = totalAmount.divide(new BigDecimal(scount),2,BigDecimal.ROUND_HALF_DOWN);
//		if (perAveCountAmount.compareTo(new BigDecimal(consumeSingleMoneyLimit * consumeCountLimit + number)) < 0) {
//			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//			map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:当前金额可生成计划笔数为:" + (Integer.valueOf(amount.substring(0, amount.indexOf(".")==-1?amount.length():amount.indexOf(".")))/(consumeSingleMoneyLimit * consumeCountLimit + number))+ "笔");
//			return map;
//		}
//		信用卡验证
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request");
        String url = uri.toString() + "/v1.0/user/bank/verify/isuseable";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", suserId);
        requestEntity.add("bankCardNumber", creditCardNumber);
        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSONObject = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "生成计划失败,原因:该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
            return map;
        }

        resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        String bankName = resultJSONObject.getString("bankName");

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(suserId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            return userChannelRate;
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//		单笔还款手续费
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
//		费率
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
        ;

        preAmount = new BigDecimal(amount).divide(BigDecimal.valueOf(count), 0, BigDecimal.ROUND_DOWN);


        int max = 0;
        if (CardConstss.CARD_VERSION_1.equals(version)) {
            if (count < 5) {
                max = 100;
            } else if (count > 5 && count < 10) {
                max = 40;
            } else if (count > 10 && count < 20) {
                max = 20;
            } else {
                max = 6;
            }
        } else {
            if (count < 5) {
                max = 12;
            } else if (count > 5 && count < 10) {
                max = 10;
            } else if (count > 10 && count < 20) {
                max = 8;
            } else {
                max = 6;
            }
        }


        int min = max / 2;
        if (BigDecimal.valueOf(repaysumeSingleMoneyLimit).compareTo(preAmount) > 0) {
            throw new RuntimeException("单笔金额不足" + repaysumeSingleMoneyLimit + "元");
        } else if (BigDecimal.valueOf(repaysumeSingleMoneyLimit).compareTo(preAmount) == 0) {
            min = 0;
            max = 0;
        } else if (preAmount.subtract(BigDecimal.valueOf(repaysumeSingleMoneyLimit)).compareTo(BigDecimal.valueOf(max)) <= 0) {
            min = preAmount.subtract(BigDecimal.valueOf(repaysumeSingleMoneyLimit)).divide(BigDecimal.valueOf(2), 0, BigDecimal.ROUND_DOWN).intValue();
            max = preAmount.subtract(BigDecimal.valueOf(repaysumeSingleMoneyLimit)).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        }
        preAmount = preAmount.subtract(BigDecimal.valueOf(max));
//		System.out.println(preAmount);
//		System.out.println(count);

        BigDecimal[][] perCountAmount = new BigDecimal[count][2 + 1];

        BigDecimal surplusCountAmount = BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(max));

        BigDecimal surplusSubCountAmount = BigDecimal.ZERO;

        // 随机每笔金额
        BigDecimal randomCountAmount = BigDecimal.ZERO;
        // 随机每次消费金额
        BigDecimal randomSubCountAmount = BigDecimal.ZERO;
        totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < count; i++) {
            int result = min + (int) (Math.random() * ((max - min) + 1));
            if (i != count - 1) {
                if (surplusCountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    randomCountAmount = new BigDecimal(result);
                    perCountAmount[i][0] = preAmount.add(randomCountAmount);
                    surplusCountAmount = surplusCountAmount.subtract(randomCountAmount);
                } else {
                    perCountAmount[i][0] = preAmount;
                }
            } else {
                perCountAmount[i][0] = preAmount.add(surplusCountAmount);
            }
//			System.out.println(perCountAmount[i][0]);
            totalAmount = totalAmount.add(perCountAmount[i][0]);
        }
//		System.out.println(totalAmount);
        if (new BigDecimal(amount).compareTo(totalAmount) > 0) {
            perCountAmount[0][0] = perCountAmount[0][0].add(new BigDecimal(amount).subtract(totalAmount));
        }

        totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < count; i++) {
            surplusSubCountAmount = perCountAmount[i][0].multiply(BigDecimal.valueOf(0.1)).setScale(0, BigDecimal.ROUND_DOWN);
            BigDecimal preConsumeAmount = perCountAmount[i][0].subtract(surplusSubCountAmount).divide(BigDecimal.valueOf(2), 1, BigDecimal.ROUND_HALF_UP);
            // 分配每笔还款的消费任务金额
            for (int j = 0; j < consumeCountLimit; j++) {
                if (j != consumeCountLimit - 1) {
                    if (surplusSubCountAmount.compareTo(BigDecimal.ZERO) > 0) {
                        randomSubCountAmount = new BigDecimal(new Random().nextInt(surplusSubCountAmount.intValue()));
                        surplusSubCountAmount = surplusSubCountAmount.subtract(randomSubCountAmount);
                        perCountAmount[i][j + 1] = preConsumeAmount.add(randomSubCountAmount);
                    } else {
                        perCountAmount[i][j + 1] = preConsumeAmount;
                    }
                } else {
                    perCountAmount[i][j + 1] = preConsumeAmount.add(surplusSubCountAmount);
                }
//				System.out.println(perCountAmount[i][j+1].divide(BigDecimal.valueOf(10)));
                totalAmount = totalAmount.add(perCountAmount[i][j + 1]);


                if (CardConstss.CARD_VERSION_2.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单笔消费不能超过1000元,请减少还款金额,或者减少预留金额百分比再重新生成计划!");
                        return map;
                    }
                    if (bankName != null && bankName.contains("交通")) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
                        return map;
                    } else if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                        if (BigDecimal.valueOf(400).compareTo(perCountAmount[i][0]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单日消费不能超过400元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }
                } else if (CardConstss.CARD_VERSION_4.equals(version)) {
                    if (perCountAmount[i][j + 1].compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单笔消费不能超过1000元,请减少还款金额,或者减少预留金额百分比再重新生成计划!");
                        return map;
                    }

                    if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                        if (BigDecimal.valueOf(500).compareTo(perCountAmount[i][0]) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "生成计划失败,原因:由于银行限制,信用卡单日消费不能超过500元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }
                }
            }
        }
//		System.out.println(totalAmount);

        Date nowTime = new Date();
        // 初始化还款任务
        RepaymentTaskVO[] repaymentTaskVOs = new RepaymentTaskVO[count];
        for (int i = 0; i < count; i++) {
            repaymentTaskVOs[i] = new RepaymentTaskVO();

//			repaymentTaskVOs[i].setConsumeTaskVOs(new ConsumeTaskVO[consumeCountLimit]);
            // 设置userId
            repaymentTaskVOs[i].setUserId(suserId);
            // 设置还款卡号
            repaymentTaskVOs[i].setCreditCardNumber(creditCardNumber);
            // 设置还款通道id
            repaymentTaskVOs[i].setChannelId(repaymentChannelId);
            // 设置还款通道tag
            repaymentTaskVOs[i].setChannelTag(repaymentChannelTag);
            // 设置还款金额
            repaymentTaskVOs[i].setAmount(perCountAmount[i][0]);
            // 设置还款手续费
            repaymentTaskVOs[i].setServiceCharge(serviceCharge);
            // 设置消费费率
            repaymentTaskVOs[i].setRate(rate);
            // 设置总手续费
            BigDecimal totalServiceCharge = repaymentTaskVOs[i].getAmount().add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(repaymentTaskVOs[i].getAmount());
            totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
            repaymentTaskVOs[i].setTotalServiceCharge(totalServiceCharge);
            // 设置任务描述
            repaymentTaskVOs[i].setDescription("还款计划");
            // 设置创建时间
            repaymentTaskVOs[i].setCreateTime(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:mm:ss"));
        }

        //随机分配执行日期
        Date[] executeTime = new Date[count];

        for (int i = 0; i < (strExecuteDates.length > count ? count : strExecuteDates.length); i++) {
//			executeTime[i] = DateUtil.getDateFromStr(strExecuteDates[i]);
            executeTime[i] = DateUtil.getDateStringConvert(new Date(), strExecuteDates[i], "yyyy-MM-dd");
            repaymentTaskVOs[i].setExecuteDate(strExecuteDates[i]);
        }
//		还款笔数和日期的差值,差值笔数将随机分配至各天
        int differencCount = count - strExecuteDates.length;
        int index = 0;
        if (differencCount > 0 && differencCount == strExecuteDates.length) {
            int j = 0;
            for (int i = strExecuteDates.length; i < count; i++) {
                repaymentTaskVOs[i].setExecuteDate(strExecuteDates[j]);
                j++;
            }
        } else if (differencCount > 0) {
            for (int i = strExecuteDates.length; i < count; i++) {
                index = new Random().nextInt(strExecuteDates.length);
                if (strExecuteDates[index] != null) {
                    repaymentTaskVOs[i].setExecuteDate(strExecuteDates[index]);
                    strExecuteDates[index] = null;
                } else {
                    for (int j = 0; j < strExecuteDates.length; j++) {
                        if (strExecuteDates[j] != null) {
                            repaymentTaskVOs[i].setExecuteDate(strExecuteDates[j]);
                            strExecuteDates[j] = null;
                            break;
                        }
                    }
                }
            }
        }

        Random random = new Random();
        // 随机分配执行时间
        String initTime = null;
        String executeDateTime = null;
        Date initDateTime = null;
        int randomInt = 0;
        for (int i = 0; i < count; i++) {
            if (i >= strExecuteDates.length) {
                if (CardConstss.CARD_VERSION_2.equals(version)) {
                    initTime = " 14:30:00";
                } else if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version)) {
                    initTime = " 15:00:00";
                } else if (CardConstss.CARD_VERSION_4.equals(version)) {
                    initTime = " 15:30:00";
                } else {
                    initTime = " 16:00:00";
                }
            } else {
                initTime = " 09:00:00";
            }

            // 设置还款任务id
            repaymentTaskVOs[i].setRepaymentTaskId(repaymentTaskVOs[i].getExecuteDate().replace("-", "") + DateUtil.getDateStringConvert(new String(), new Date(), "HHSSS") + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + i + "1");
            // 设置消费子任务字段

//			String[] consumeTypeName = {"娱乐","购物","其他","通信","交通","住宿","餐饮"};
            List<String> consumeTypeName = new ArrayList<>();
            consumeTypeName.add("娱乐");
            consumeTypeName.add("购物");
            consumeTypeName.add("其他");
            consumeTypeName.add("通信");
            consumeTypeName.add("交通");
            consumeTypeName.add("住宿");
            consumeTypeName.add("餐饮");
            for (int j = 0; j < consumeCountLimit; j++) {
//				repaymentTaskVOs[i].getConsumeTaskVOs()[j] = new ConsumeTaskVO();
                repaymentTaskVOs[i].getConsumeTaskVOs().add(new ConsumeTaskVO());
//				ConsumeTaskVO consumeTaskVO = repaymentTaskVOs[i].getConsumeTaskVOs()[j];
                ConsumeTaskVO consumeTaskVO = repaymentTaskVOs[i].getConsumeTaskVOs().get(j);
                // 设置userId
                consumeTaskVO.setUserId(suserId);
                // 设置消费通道id
                consumeTaskVO.setChannelId(consumeChannelId);
                // 设置消费通道tag
                consumeTaskVO.setChannelTag(consumeChannelTag);
                // 设置还款任务id
                consumeTaskVO.setRepaymentTaskId(repaymentTaskVOs[i].getRepaymentTaskId());
                // 设置消费卡号
                consumeTaskVO.setCreditCardNumber(creditCardNumber);
                // 设置消费任务
                consumeTaskVO.setDescription("消费计划");
                // 设置消费类型
                randomInt = new Random().nextInt(consumeTypeName.size());
                consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
                consumeTypeName.remove(randomInt);

                // 设置消费金额
                consumeTaskVO.setAmount(perCountAmount[i][j + 1]);
                if (j == 0) {
                    consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(repaymentTaskVOs[i].getTotalServiceCharge()));
                    consumeTaskVO.setServiceCharge(repaymentTaskVOs[i].getTotalServiceCharge());
                } else {
                    consumeTaskVO.setRealAmount(consumeTaskVO.getAmount());
                }
                // 设置执行日期
                consumeTaskVO.setExecuteDate(repaymentTaskVOs[i].getExecuteDate());
                // 设置创建时间
                consumeTaskVO.setCreateTime(repaymentTaskVOs[i].getCreateTime());
                // 设置消费子任务id
                consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVOs[i].getRepaymentTaskId()) + (j + 1) + "");

                initDateTime = DateUtil.getDateStringConvert(new Date(), consumeTaskVO.getExecuteDate() + initTime, "yyyy-MM-dd HH:mm:ss");
                executeDateTime = DateUtil.getDateStringConvert(new String(), new Date(initDateTime.getTime() + (30 * 70 * 1000) + (random.nextInt(60 * 70 * 1000))), "yyyy-MM-dd HH:mm:ss");
                // 设置消费子任务执行日期时间
                consumeTaskVO.setExecuteDateTime(executeDateTime);
                initTime = executeDateTime.substring(executeDateTime.indexOf(" "));
            }
            initDateTime = DateUtil.getDateStringConvert(new Date(), repaymentTaskVOs[i].getExecuteDate() + initTime, "yyyy-MM-dd HH:mm:ss");
            executeDateTime = DateUtil.getDateStringConvert(new String(), new Date(initDateTime.getTime() + (30 * 70 * 1000) + (random.nextInt(60 * 70 * 1000))), "yyyy-MM-dd HH:mm:ss");
            // 设置还款子任务执行时间
            repaymentTaskVOs[i].setExecuteDateTime(executeDateTime);
        }
        Arrays.sort(repaymentTaskVOs);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "生成计划成功");
        map.put(CommonConstants.RESULT, repaymentTaskVOs);
        return map;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/save/all/task")
    public @ResponseBody
    Object saveRepaymentTaskAndConsumeTaskAndTaskBill(HttpServletRequest request,
                                                      @RequestParam(value = "taskJSON") String taskJSON,
                                                      //本地城市
                                                      @RequestParam(value = "city", required = false) String city,
                                                      //计划金额
                                                      @RequestParam(value = "amount") String amount,
                                                      //预留金额
                                                      @RequestParam(value = "reservedAmount") String reservedAmount,
                                                      //通道号
                                                      @RequestParam(value = "version") String version
    ) {
        //还款笔数
        RepaymentBill repaymentBill = new RepaymentBill();
        repaymentBill.setTaskAmount(new BigDecimal(amount));
        repaymentBill.setReservedAmount(new BigDecimal(reservedAmount));
        repaymentBill.setVersion(version);
        LOG.info("落地城市==============" + city);
        return this.saveRepaymentTaskAndConsumeTask(request, taskJSON, city, version, repaymentBill);
    }

    /**
     * @param request
     * @param taskJSON
     * @param city
     * @param version
     * @param repaymentBill
     * @return 根据用户选择的城市和通道和还款笔数 保存相应的还款计划和消费计划
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/save/all/plan")
    public @ResponseBody
    Object saveRepaymentTaskAndConsumeTask(HttpServletRequest request,
                                           @RequestParam(value = "taskJSON") String taskJSON,
                                           @RequestParam(value = "city", required = false) String city,
                                           @RequestParam(value = "version") String version,
                                           RepaymentBill repaymentBill
    ) {
        Map<String, Object> map = new HashMap<>();
        String userId = null;
        String creditCardNumber = null;
        String createTime = null;
        String brandId = "0";
        Date lastExecuteDateTime = new Date();
        BigDecimal rate = BigDecimal.ZERO;
        BigDecimal serviceCharge = BigDecimal.ZERO;
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        //验证String是否是空字符串或null
        Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle.verifyStringFiledIsNull(taskJSON);
        if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))) {
            return verifyStringFiledIsNullMap;
        }
        taskJSON = taskJSON.trim();
        try {
            taskJSON = URLDecoder.decode(taskJSON, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "参数URL解码失败");
            return map;
        }

//		将任务解析成JSONArray
        JSONArray allTaskJSONArray;
        try {
            allTaskJSONArray = JSONArray.fromObject(taskJSON);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:taskJSON参数异常!");
            return map;
        }
        JSONObject repaymentTaskJSONObject = null;
//		还款任务PO数组
        RepaymentTaskPOJO[] repaymentTaskPOJOs = new RepaymentTaskPOJO[allTaskJSONArray.size()];
//		消费任务PO数组
        List<ConsumeTaskPOJO> consumeTaskPOJOs = new ArrayList<>();
//		ConsumeTaskVO[] consumeTaskVOs = new ConsumeTaskVO[consumeCountLimit];
        List<ConsumeTaskVO> consumeTaskVOs = null;
        Map<String, Class> classMap = new HashMap<>();
        classMap.put("consumeTaskVOs", ConsumeTaskVO.class);
        ConsumeTaskVO consumeTaskVO = null;
        ConsumeTaskPOJO consumeTaskPOJO = null;
//		将解析出的JONSArray注入还款任务PO数组和消费任务PO数组中
        for (int i = 0; i < allTaskJSONArray.size(); i++) {
            repaymentTaskJSONObject = allTaskJSONArray.getJSONObject(i);
            repaymentTaskPOJOs[i] = (RepaymentTaskPOJO) JSONObject.toBean(repaymentTaskJSONObject, RepaymentTaskPOJO.class, classMap);
            repaymentTaskPOJOs[i].setVersion(version);
            String executeDateTime = repaymentTaskPOJOs[i].getExecuteDateTime();
            Date executeDate = DateUtil.getDateStringConvert(new Date(), executeDateTime, "yyyy-MM-dd HH:mm:ss");
            if (lastExecuteDateTime.compareTo(executeDate) < 0) {
                lastExecuteDateTime = executeDate;
            }

            if (userId == null) {
                userId = repaymentTaskPOJOs[i].getUserId();
                creditCardNumber = repaymentTaskPOJOs[i].getCreditCardNumber();
                createTime = repaymentTaskPOJOs[i].getCreateTime();
                rate = repaymentTaskPOJOs[i].getRate();
                serviceCharge = repaymentTaskPOJOs[i].getServiceCharge();
                CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
                brandId = creditCardAccount.getBrandId();
            }
            repaymentTaskPOJOs[i].setBrandId(brandId);

            totalServiceCharge = totalServiceCharge.add(repaymentTaskPOJOs[i].getTotalServiceCharge());


            if (CardConstss.CARD_VERSION_11.equals(version)) {
                repaymentTaskPOJOs[i].setRepaymentTaskId("WF2253553" + repaymentTaskPOJOs[i].getRepaymentTaskId().substring(2, repaymentTaskPOJOs[i].getRepaymentTaskId().length()));
            }

            consumeTaskVOs = repaymentTaskPOJOs[i].getConsumeTaskVOs();
            for (int j = 0; j < consumeTaskVOs.size(); j++) {
                consumeTaskPOJO = new ConsumeTaskPOJO();
                consumeTaskVO = consumeTaskVOs.get(j);
                BeanUtils.copyProperties(consumeTaskVO, consumeTaskPOJO);
                if (CardConstss.CARD_VERSION_1.equals(version) || CardConstss.CARD_VERSION_8.equals(version)) {
                    if (consumeTaskPOJO.getRealAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,单笔消费不能超过1000元,请减少还款金额,或者减少预留金额百分比再重新生成计划!");
                        return map;
                    }
                }

                //通道匹配
                if ((CardConstss.CARD_VERSION_6.equalsIgnoreCase(version) ||
                        CardConstss.CARD_VERSION_17.equalsIgnoreCase(version) ||
                        CardConstss.CARD_VERSION_18.equalsIgnoreCase(version) ||
                        CardConstss.CARD_VERSION_60.equals(version) ||
                        CardConstss.CARD_VERSION_10.equalsIgnoreCase(version) ||
                        CardConstss.CARD_VERSION_11.equals(version) ||
                        CardConstss.CARD_VERSION_12.equals(version) ||
                        CardConstss.CARD_VERSION_13.equals(version) ||
                        CardConstss.CARD_VERSION_14.equals(version) ||
                        CardConstss.CARD_VERSION_8.equals(version) ||
                        CardConstss.CARD_VERSION_20.equals(version) ||
                        CardConstss.CARD_VERSION_16.equals(version) ||
                        CardConstss.CARD_VERSION_15.equals(version) ||
                        CardConstss.CARD_VERSION_21.equals(version)) ||
                        CardConstss.CARD_VERSION_24.equals(version) ||
                        CardConstss.CARD_VERSION_25.equals(version) ||
                        CardConstss.CARD_VERSION_26.equals(version) ||
                        CardConstss.CARD_VERSION_27.equals(version) ||
                        CardConstss.CARD_VERSION_68.equals(version) ||
                        CardConstss.CARD_VERSION_31.equals(version) ||
                        CardConstss.CARD_VERSION_33.equals(version) ||
                        CardConstss.CARD_VERSION_32.equals(version) ||
                        CardConstss.CARD_VERSION_34.equals(version) ||
                        CardConstss.CARD_VERSION_35.equals(version) ||
                        CardConstss.CARD_VERSION_38.equals(version) ||
                        CardConstss.CARD_VERSION_40.equals(version) ||
                        CardConstss.CARD_VERSION_41.equals(version) ||
                        CardConstss.CARD_VERSION_42.equals(version) ||
                        CardConstss.CARD_VERSION_43.equals(version) ||
                        CardConstss.CARD_VERSION_44.equals(version) ||
                        CardConstss.CARD_VERSION_45.equals(version) ||
                        CardConstss.CARD_VERSION_49.equals(version) ||
                        CardConstss.CARD_VERSION_50.equals(version) ||
                        CardConstss.CARD_VERSION_51.equals(version) ||
                        CardConstss.CARD_VERSION_52.equals(version) ||
                        CardConstss.CARD_VERSION_53.equals(version) ||
                        CardConstss.CARD_VERSION_54.equals(version) ||
                        CardConstss.CARD_VERSION_55.equals(version) ||
                        CardConstss.CARD_VERSION_56.equals(version) ||
                        CardConstss.CARD_VERSION_57.equals(version) ||
                        CardConstss.CARD_VERSION_58.equals(version) ||
                        CardConstss.CARD_VERSION_59.equals(version) ||
                        CardConstss.CARD_VERSION_61.equals(version) ||
                        CardConstss.CARD_VERSION_62.equals(version) ||
                        CardConstss.CARD_VERSION_66.equals(version) ||
                        CardConstss.CARD_VERSION_69.equals(version) ||
                        CardConstss.CARD_VERSION_70.equals(version) ||
                        CardConstss.CARD_VERSION_80.equals(version) ||
                        CardConstss.CARD_VERSION_67.equals(version) ||
                        CardConstss.CARD_VERSION_29.equals(version) && city != null && !"null".equals(city) && !"".equals(city.trim())) {
                    if (!consumeTaskVO.getConsumeType().contains("|")) {
                        if (CardConstss.CARD_VERSION_6.equalsIgnoreCase(version) || CardConstss.CARD_VERSION_60.equals(version)) {
                            consumeTaskPOJO.setDescription(consumeTaskPOJO.getDescription() + "|" + city + "-" + consumeTaskVO.getConsumeType());
                        } else {
                            consumeTaskPOJO.setDescription(consumeTaskPOJO.getDescription() + "|" + consumeTaskVO.getConsumeType());
                        }
                    } else {
                        consumeTaskPOJO.setDescription(consumeTaskPOJO.getDescription() + "|" + city);
                    }
                } else {
                    consumeTaskPOJO.setDescription(consumeTaskPOJO.getDescription() + consumeTaskVO.getConsumeType());
                }

                consumeTaskPOJO.setVersion(version);
                if (CardConstss.CARD_VERSION_11.equals(version)) {
                    consumeTaskPOJO.setConsumeTaskId("WF2253553" + consumeTaskPOJO.getConsumeTaskId().substring(2, consumeTaskPOJO.getConsumeTaskId().length()));
                    consumeTaskPOJO.setRepaymentTaskId(repaymentTaskPOJOs[i].getRepaymentTaskId());
                }
                consumeTaskPOJO.setBrandId(brandId);
                consumeTaskPOJOs.add(consumeTaskPOJO);
            }
        }
        /**
         * 验证是否有批量生成未执行的消费计划
         * 验证是否有批量生成待执行的还款计划
         */
        boolean doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(repaymentTaskPOJOs[0].getUserId(), repaymentTaskPOJOs[0].getCreditCardNumber(), version);
        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(repaymentTaskPOJOs[0].getUserId(), repaymentTaskPOJOs[0].getCreditCardNumber(), version);
        if (doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO || doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您有未执行计划,请等待任务执行完后再生成计划!");
            return map;
        }

        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", repaymentTaskPOJOs[0].getUserId());
        requestEntity.add("cardNo", repaymentTaskPOJOs[0].getCreditCardNumber());
        Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("user", "/v1.0/user/bank/find/bankphone", requestEntity);
        if (CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
            JSONObject resulJSON = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
            String bankName = resulJSON.getString("bankName");

            if (CardConstss.CARD_VERSION_1.equals(version)) {
                if (bankName != null && bankName.contains("光大")) {
                    for (ConsumeTaskPOJO model : consumeTaskPOJOs) {
                        /**
                         * compareTo 指定数比参数小 返回-1
                         */
                        if (BigDecimal.valueOf(500).compareTo(model.getRealAmount()) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,光大银行信用卡单笔消费不能超过500元,请减少还款金额,或者增加还款天数再重新生成计划");
                            return map;
                        }
                    }
                }
            } else if (CardConstss.CARD_VERSION_2.equals(version)) {
                if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                    for (RepaymentTaskPOJO model : repaymentTaskPOJOs) {
                        if (BigDecimal.valueOf(400).compareTo(model.getAmount()) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,信用卡单日消费不能超过400元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                            return map;
                        }
                    }
                }
            } else if (CardConstss.CARD_VERSION_17.equals(version)) {
                for (RepaymentTaskPOJO model : repaymentTaskPOJOs) {
                    if (BigDecimal.valueOf(2000).compareTo(model.getAmount()) < 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,信用卡单日消费不能超过2000元,请减少还款金额,或者减少预留金额百分比再重新生成计划");
                        return map;
                    }
                }
            } else if (CardConstss.CARD_VERSION_18.equals(version)) {
                if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                    for (ConsumeTaskPOJO model : consumeTaskPOJOs) {
                        if (BigDecimal.valueOf(1000).compareTo(model.getRealAmount()) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,信用卡单笔消费不能超过1000元,请减少还款金额,或者增加还款天数再重新生成计划");
                            return map;
                        }
                    }
                }
            } else if (CardConstss.CARD_VERSION_19.equals(version)) {
                if (bankName != null && (bankName.contains("光大") || bankName.contains("中国银行"))) {
                    for (ConsumeTaskPOJO model : consumeTaskPOJOs) {
                        if (BigDecimal.valueOf(1000).compareTo(model.getRealAmount()) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,信用卡单笔消费不能超过1000元,请减少还款金额,或者增加还款天数再重新生成计划");
                            return map;
                        }
                    }
                }
            } else if (CardConstss.CARD_VERSION_7.equals(version)) {
                if (bankName != null && (bankName.contains("工商") || bankName.contains("华夏") || bankName.contains("平安"))) {
                    for (ConsumeTaskPOJO model : consumeTaskPOJOs) {
                        if (BigDecimal.valueOf(5000).compareTo(model.getRealAmount()) < 0) {
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:由于银行限制,信用卡单笔消费不能超过5000元,请减少还款金额,或者增加还款天数再重新生成计划");
                            return map;
                        }
                    }
                }
            }
            /**
             * verifyDoesSupportBank 返回支持的银行
             */
            Map<String, Object> verifyDoesSupportBank = creditCardManagerAuthorizationHandle.verifyDoesSupportBank(version, bankName);
            if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
                return verifyDoesSupportBank;
            }
        }

        /**
         * 数组保存消费计划，还款计划
         */
        try {
            consumeTaskPOJOBusiness.saveArrayListTaskAll(consumeTaskPOJOs, repaymentTaskPOJOs);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "保存计划失败,原因:服务器正忙,请重新生成计划后重试!");
            return map;
        }

        if (repaymentBill != null && repaymentBill.getTaskAmount().compareTo(BigDecimal.ZERO) > 0) {
            repaymentBill.setUserId(userId);
            repaymentBill.setCreditCardNumber(creditCardNumber);
            repaymentBill.setCreateTime(createTime);
            repaymentBill.setTaskCount(repaymentTaskPOJOs.length);
            repaymentBill.setRate(rate);
            repaymentBill.setServiceCharge(serviceCharge);
            repaymentBill.setTotalServiceCharge(totalServiceCharge);
            repaymentBill.setLastExecuteDateTime(DateUtil.getDateStringConvert(new String(), lastExecuteDateTime, "yyyy-MM-dd HH:mm:ss"));
            repaymentBill = repaymentBillBusiness.save(repaymentBill);
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "保存计划成功!");
        return map;
    }

    /**
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param brandId
     * @param version
     * @return 验证信息，通过用户信息 查询费率  有未完成的还款先还款 没有 则生成还款计划
     * 没卡的用户绑卡 生成 还款计划
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/verify/card/isuserable")
    public @ResponseBody
    Object verifyCardIsUseable(HttpServletRequest request,
                               @RequestParam(value = "userId") String userId,
                               @RequestParam(value = "creditCardNumber") String creditCardNumber,
                               @RequestParam(value = "brandId") String brandId,
                               @RequestParam(value = "version") String version
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        Map<String, Object> map = new HashMap<>();
        if ("".equals(userId) || "".equals(creditCardNumber) || "".equals(version)) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证失败,传入参数不能为空!");
            return map;
        }

        JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "验证失败,原因:该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
            return map;
        }
        JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        String securityCode = resultBankCardJSONObject.getString("securityCode");
        String billDay = resultBankCardJSONObject.getString("billDay");
        String repaymentDay = resultBankCardJSONObject.getString("repaymentDay");
        if (securityCode != null && (securityCode.length() != 3 || !securityCode.matches("^[0-9]*$"))) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "安全码有误,请重新设置");
            return map;
        }
        /**
         *  信用卡 表
         */
        CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);


        Map<String, Object> verifyCreditCardDoesHaveSecurityCodeAndExpiredTime = creditCardManagerAuthorizationHandle.verifyCreditCardDoesHaveSecurityCodeAndExpiredTime(userId, creditCardNumber);
        if (!CommonConstants.SUCCESS.equals(verifyCreditCardDoesHaveSecurityCodeAndExpiredTime.get(CommonConstants.RESP_CODE))) {
            if (!CardConstss.NO_CVN_OR_EXTIME.equals(verifyCreditCardDoesHaveSecurityCodeAndExpiredTime.get(CommonConstants.RESP_CODE))) {
                verifyCreditCardDoesHaveSecurityCodeAndExpiredTime.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            }
            return verifyCreditCardDoesHaveSecurityCodeAndExpiredTime;
        }

        String bankName = resultBankCardJSONObject.getString("bankName");
        Map<String, Object> verifyDoesSupportBank = creditCardManagerAuthorizationHandle.verifyDoesSupportBank(version, bankName);
        if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
            return verifyDoesSupportBank;
        }
		/*if(CardConstss.CARD_VERSION_2.equals(version) && model==null && bankName != null){
			if(bankName.contains("交通") || bankName.contains("农业")){
				return ResultWrap.init(CardConstss.NONSUPPORT, "抱歉,该银行卡暂不支持使用此功能");
			}
		}else if(CardConstss.CARD_VERSION_1.equals(version) && model==null && bankName!= null){
			if (bankName.contains("农业") || bankName.contains("招商") || bankName.contains("交通")) {
				return ResultWrap.init(CardConstss.NONSUPPORT, "抱歉,该银行卡暂不支持使用此功能");
			}
		}else if (CardConstss.CARD_VERSION_4.equals(version) && model==null && bankName!= null) {
			if(bankName.contains("建设")){
				return ResultWrap.init(CardConstss.NONSUPPORT, "抱歉,该银行卡暂不支持使用此功能");
			}
		}else if (CardConstss.CARD_VERSION_3.equals(version) && model==null && bankName!= null) {
			if(bankName.contains("浦发")){
				return ResultWrap.init(CardConstss.NONSUPPORT, "抱歉,该银行卡暂不支持使用此功能");
			}
		}*/

//		resultJSONObject = (JSONObject) verifyCreditCardDoesHaveSecurityCodeAndExpiredTime.get(CommonConstants.RESULT);

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            return userChannelRate;
        }
        int firstMoney = 0;
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        if (creditCardManagerConfig != null && creditCardManagerConfig.getCreateOnOff() == 1) {
            firstMoney = creditCardManagerConfig.getFirstMoney();
        } else {
            return ResultWrap.init(CardConstss.NONSUPPORT, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
        }
        int createOnOff = creditCardManagerConfig.getCreateOnOff();
        if (model == null && 1 != createOnOff) {
            return ResultWrap.init(CardConstss.NONSUPPORT, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        //成本费率
        String extraFeeStr = resultJSONObject.getString("extraFee");
        //额外费率
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
        BigDecimal totalserviceCharge = BigDecimal.ZERO;
        if (CardConstss.CARD_VERSION_1.equals(version)) {
            //金额+2/1-费率向上取整小数点后2位  - 金额
            totalserviceCharge = serviceCharge.add(BigDecimal.valueOf(firstMoney)).add(BigDecimal.valueOf(2)).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(BigDecimal.valueOf(firstMoney));
        } else if (CardConstss.CARD_VERSION_12.equals(version) || CardConstss.CARD_VERSION_13.equals(version)) {
            //服务费/2 取2位小数 +第一笔金额+2/1-费率 - 第一笔金额
            totalserviceCharge = serviceCharge.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP).add(BigDecimal.valueOf(firstMoney)).add(BigDecimal.valueOf(2)).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(BigDecimal.valueOf(firstMoney));
        } else {
            //服务费+第一笔金额/1-费率 - 第一笔金额
            totalserviceCharge = serviceCharge.add(BigDecimal.valueOf(firstMoney)).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP).subtract(BigDecimal.valueOf(firstMoney));
        }
        //还款金额=第一笔钱+总服务费
        BigDecimal amount = BigDecimal.valueOf(firstMoney).add(totalserviceCharge).setScale(2, BigDecimal.ROUND_UP);
        //根据用户的id获取用户的默认卡信息
        Map<String, Object> verifyDoesHaveBandCard = creditCardManagerAuthorizationHandle.verifyDoesHaveBandCard(userId, creditCardNumber, rateStr, serviceCharge.toString(), version, resultBankCardJSONObject);
        if (!CommonConstants.SUCCESS.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
            if (!CardConstss.TO_BAND_CARD.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CardConstss.NONSUPPORT, (String) verifyDoesHaveBandCard.get(CommonConstants.RESP_MESSAGE));
            }
            return verifyDoesHaveBandCard;
        }

        RepaymentTaskPOJO firstRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType0RepaymentTaskPOJO(userId, creditCardNumber, version);
        if (firstRepaymentTaskPOJO != null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "未对首笔消费进行还款,点击确定进行继续还款");
            return map;
        }

        /**
         * false 为无待执行还款任务,true为有待执行还款任务
         */

        boolean verifyDoesHaveTaskStatus1AndTaskType0AndOrderStatus4RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus1AndTaskType0AndOrderStatus4RepaymentTaskPOJO(userId, creditCardNumber, version);
        if (verifyDoesHaveTaskStatus1AndTaskType0AndOrderStatus4RepaymentTaskPOJO) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "点击确定继续进行还款");
            return map;
        }
        /**
         * 没有信用卡，绑定新卡 生成还款计划
         * 有卡直接验证 生成还款计划
         */
        if (model == null) {
//			if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
            String phone = null;
            try {
                JSONObject userInfo = this.getUserInfo(userId);
                resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                phone = resultJSONObject.getString("phone");
                brandId = resultJSONObject.getString("brandId");
            } catch (RuntimeException e) {
                e.printStackTrace();
                LOG.error("", e);
                return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
            }
            model = creditCardAccountBusiness.createNewAccount(userId, creditCardNumber, version, phone, Integer.valueOf(billDay), Integer.valueOf(repaymentDay), BigDecimal.ZERO, brandId);
            model.setBankName(bankName);
            return ResultWrap.init(CommonConstants.SUCCESS, "验证成功,可进入生成还款计划!", model);
//			}
//			map.put(CommonConstants.RESP_MESSAGE,"该卡首次使用,需要进行金额为"+amount+"元的首笔消费验证,扣除"+totalserviceCharge+"元手续费后,将还入"+firstMoney+"元到该卡中!");
//			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        } else {
            map.put(CommonConstants.RESP_MESSAGE, "验证成功,可进入生成还款计划!");
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            model.setBankName(bankName);
        }
        map.put(CommonConstants.RESULT, model);
        return map;
    }

    /**
     * @param request
     * @param userIdStr
     * @param creditCardNumber
     * @param phone
     * @param brandId
     * @param creditBlance
     * @param billDate
     * @param repaymentDate
     * @param version
     * @return
     */

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/first/use/credit/card/manager")
    public @ResponseBody
    Object registCreditCardManager(HttpServletRequest request,
                                   @RequestParam(value = "userId") String userIdStr,
                                   @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                   @RequestParam(value = "phone") String phone,
                                   @RequestParam(value = "brandId") String brandId,
                                   @RequestParam(value = "creditBlance", required = false, defaultValue = "0") String creditBlance,
                                   @RequestParam(value = "billDate", required = false, defaultValue = "0") String billDate,
                                   @RequestParam(value = "repaymentDate", required = false, defaultValue = "0") String repaymentDate,
                                   @RequestParam(value = "version") String version
    ) {
        Map<String, Object> map = new HashMap<>();

        int firstMoney = 0;
        String consumeChannelId = null;
        String repaymentChannelId = null;
        String consumeChannelTag = null;
        String repaymentChannelTag = null;
        /**
         * 信用卡管理表
         */
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        if (creditCardManagerConfig != null) {
            consumeChannelId = creditCardManagerConfig.getChannelId();
            consumeChannelTag = creditCardManagerConfig.getChannelTag();
            firstMoney = creditCardManagerConfig.getFirstMoney();
            repaymentChannelId = creditCardManagerConfig.getChannelId();
            repaymentChannelTag = creditCardManagerConfig.getChannelTag();
            int createOnOff = creditCardManagerConfig.getCreateOnOff();
            if (1 != createOnOff) {
                return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
        }

//		判断通道开放时间
        Map<String, Object> verifyOpenTime = creditCardManagerAuthorizationHandle.verifyOpenTime(version);
        if (!CommonConstants.SUCCESS.equals(verifyOpenTime.get(CommonConstants.RESP_CODE))) {
            return verifyOpenTime;
        }

//		限制用户同一时间多次请求
        Map<String, Object> restClientLimit = creditCardManagerAuthorizationHandle.restClientLimit("firstUseCreditCardManager", 10, userIdStr, creditCardNumber);
        if (!CommonConstants.SUCCESS.equals(restClientLimit.get(CommonConstants.RESP_CODE))) {
            return restClientLimit;
        }

//		获取用户的卡信息
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userIdStr);
        requestEntity.add("cardNo", creditCardNumber);
        String resultString = restTemplate.postForObject("http://user/v1.0/user/bank/find/bankphone", requestEntity, String.class);
        JSONObject resultJSON = JSONObject.fromObject(resultString);
        resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
        String bankName = resultJSON.getString("bankName");
		/*if(CardConstss.CARD_VERSION_1.equals(version) && bankName!= null &&(bankName.contains("招商") || bankName.contains("农业"))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "抱歉,该银行卡不支持使用此功能,请更换银行卡!");
			return map;
		}*/
        Map<String, Object> verifyDoesSupportBank = creditCardManagerAuthorizationHandle.verifyDoesSupportBank(version, bankName);
        if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
            return verifyDoesSupportBank;
        }

        userIdStr = userIdStr.trim();
        creditCardNumber = creditCardNumber.trim();
        JSONObject resultJSONObject = null;
//		验证是否为可用信用卡
        resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userIdStr, creditCardNumber);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "首次功能验证失败,原因:该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
            return map;
        }

//		验证该卡是否在其他号码使用
        Map<String, Object> creditCardNumberIsUse = creditCardManagerAuthorizationHandle.verifyCreditCardNumberIsUse(creditCardNumber, userIdStr, version);
        if (!CommonConstants.SUCCESS.equals(creditCardNumberIsUse.get(CommonConstants.RESP_CODE))) {
            return creditCardNumberIsUse;
        }

//		判断是否有注册,而且已完成首笔还款则无需进行首笔验证
        Map<String, Object> creditCardAccountMap = creditCardManagerAuthorizationHandle.verifyIsRegister(userIdStr, creditCardNumber, version);

//		判断是否完成首笔还款
        Map<String, Object> verifyIsCompletedFirstRepaymentTask = creditCardManagerAuthorizationHandle.verifyIsCompletedFirstRepaymentTask(userIdStr, creditCardNumber, version);
        RepaymentTaskPOJO firstRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0RepaymentTaskPOJO(userIdStr, creditCardNumber, version);
        if (firstRepaymentTaskPOJO != null) {
            if (!CommonConstants.SUCCESS.equals(creditCardAccountMap.get(CommonConstants.RESP_CODE))) {
                Date nowTime = new Date();
                CreditCardAccount creditCardAccount = new CreditCardAccount();
                creditCardAccount.setUserId(userIdStr);
                creditCardAccount.setCreditCardNumber(creditCardNumber);
                creditCardAccount.setPhone(phone);
                creditCardAccount.setBillDate(Integer.valueOf(billDate));
                creditCardAccount.setRepaymentDate(Integer.valueOf(repaymentDate));
                creditCardAccount.setCreditBlance(new BigDecimal(creditBlance));
                creditCardAccount.setLastUpdateTime(nowTime);
                creditCardAccount = creditCardAccountBusiness.save(creditCardAccount);
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "首笔任务完成,可进入生成计划!");
            return map;
        }

//		判断是否已经注册和是否完成首笔还款
        if (CommonConstants.SUCCESS.equals(creditCardAccountMap.get(CommonConstants.RESP_CODE)) && CommonConstants.SUCCESS.equals(verifyIsCompletedFirstRepaymentTask.get(CommonConstants.RESP_CODE))) {
            return creditCardAccountMap;
        }

//		判断是否有待完成的首笔还款任务
        RepaymentTaskPOJO waitNotifyRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus1AndOrderStatus4RepaymentTaskPOJO(userIdStr, creditCardNumber, version);

//		判断是否有待完成的首笔消费任务
        ConsumeTaskPOJO waitNotifyConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus1AndOrderStatus4ConsumeTaskPOJO(userIdStr, creditCardNumber, version);
        if (waitNotifyConsumeTaskPOJO != null) {
            JSONObject orderStatus = null;
            orderStatus = baseExecutor.getOrderStatusByVersion(waitNotifyConsumeTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_CONSUME, waitNotifyConsumeTaskPOJO.getVersion());
            LOG.info("任务查询结果=====" + orderStatus + "=====" + waitNotifyConsumeTaskPOJO);
            Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), waitNotifyConsumeTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, -15);
            if (CardConstss.WAIT_NOTIFY.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CommonConstants.FALIED, "等待首笔消费成功,请稍后!");
            } else if (CommonConstants.FALIED.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
                    waitNotifyConsumeTaskPOJO.setTaskStatus(0);
                    waitNotifyConsumeTaskPOJO.setOrderStatus(0);
                    consumeTaskPOJOBusiness.save(waitNotifyConsumeTaskPOJO);
                    return ResultWrap.init(CommonConstants.FALIED, "首笔消费失败,请再次点击进行首笔消费");
                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "等待首笔消费成功,请稍后!");
                }
            } else if (CommonConstants.SUCCESS.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                this.updateTaskStatusByOrderCode(request, waitNotifyConsumeTaskPOJO.getOrderCode(), waitNotifyConsumeTaskPOJO.getVersion());
                baseExecutor.updatePaymentOrderByOrderCode(waitNotifyConsumeTaskPOJO.getOrderCode());
                return ResultWrap.init(CommonConstants.FALIED, "首笔消费成功,请再次点击进行首笔还款!");
            }
        }

        if (waitNotifyRepaymentTaskPOJO != null) {
            JSONObject orderStatus = null;
            orderStatus = baseExecutor.getOrderStatusByVersion(waitNotifyRepaymentTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_REPAYMENT, waitNotifyRepaymentTaskPOJO.getVersion());
            LOG.info("任务查询结果=====" + orderStatus + "=====" + waitNotifyRepaymentTaskPOJO);
            Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), waitNotifyRepaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, -15);
            if (CardConstss.WAIT_NOTIFY.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CommonConstants.FALIED, "等待首笔还款成功,请稍后!");
            } else if (CommonConstants.FALIED.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
                    waitNotifyRepaymentTaskPOJO.setTaskStatus(0);
                    waitNotifyRepaymentTaskPOJO.setOrderStatus(0);
                    repaymentTaskPOJOBusiness.save(waitNotifyRepaymentTaskPOJO);
                    creditCardAccountBusiness.updateCreditCardAccountAndVersion(waitNotifyRepaymentTaskPOJO.getUserId(), waitNotifyRepaymentTaskPOJO.getCreditCardNumber(), waitNotifyRepaymentTaskPOJO.getRepaymentTaskId(), 3, waitNotifyRepaymentTaskPOJO.getRealAmount(), "首笔还款失败,解除冻结金额", waitNotifyRepaymentTaskPOJO.getVersion(), waitNotifyRepaymentTaskPOJO.getCreateTime());
                    return ResultWrap.init(CommonConstants.FALIED, "首笔还款失败,请再次点击进行首笔还款");
                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "等待首笔还款成功,请稍后!");
                }
            } else if (CommonConstants.SUCCESS.equals(orderStatus.getString(CommonConstants.RESP_CODE))) {
                this.updateTaskStatusByOrderCode(request, waitNotifyRepaymentTaskPOJO.getOrderCode(), waitNotifyRepaymentTaskPOJO.getVersion());
                baseExecutor.updatePaymentOrderByOrderCode(waitNotifyRepaymentTaskPOJO.getOrderCode());
                return ResultWrap.init(CommonConstants.SUCCESS, "首笔任务完成,可进入生成计划!");
            }
        }


//		验证是否有待执行的首笔消费任务
        ConsumeTaskPOJO firstConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType0ConsumeTaskPOJO(userIdStr, creditCardNumber, version);
//		必须先完成一笔消费任务
        if (firstConsumeTaskPOJO != null) {
            CreditCardAccount creditCardAccount2;
            creditCardAccount2 = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userIdStr, creditCardNumber, version);
            LOG.info("==========================================首笔帐号信息:" + creditCardAccount2);
            if (creditCardAccount2 == null) {
                Map<String, Object> executeFirstConsumeTask = (Map<String, Object>) this.executeFirstConsumeTask(request, userIdStr, creditCardNumber, version, phone, creditBlance, billDate, repaymentDate);
                Map<String, Object> executeFirstRepaymentTask = null;
                if (CommonConstants.SUCCESS.equals(executeFirstConsumeTask.get(CommonConstants.RESP_CODE))) {
                    executeFirstRepaymentTask = (Map<String, Object>) this.executeFirstRepaymentTask(request, userIdStr, creditCardNumber, version);
                    return executeFirstRepaymentTask;
                }
                executeFirstConsumeTask.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                return executeFirstConsumeTask;
            } else if (creditCardAccount2.getBlance().compareTo(BigDecimal.ZERO) > 0) {
                firstConsumeTaskPOJO.setTaskStatus(1);
                firstConsumeTaskPOJO.setOrderStatus(1);
                consumeTaskPOJOBusiness.save(firstConsumeTaskPOJO);
                Map<String, Object> executeFirstRepaymentTask = (Map<String, Object>) this.executeFirstRepaymentTask(request, userIdStr, creditCardNumber, version);
                return executeFirstRepaymentTask;
            }
        }

//		验证是否有待执行的首笔还款任务
        firstRepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType0RepaymentTaskPOJO(userIdStr, creditCardNumber, version);
//		验证是否有已完成的首笔消费任务
        firstConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveOrderStatus1AndTaskType0ConsumeTaskPOJO(userIdStr, creditCardNumber, version);
        if (firstConsumeTaskPOJO != null && firstRepaymentTaskPOJO != null) {
            CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userIdStr, creditCardNumber, version);
            if (creditCardAccount == null) {
                ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(userIdStr, creditCardNumber, 0, version);
//				Date nowTime = new Date();
//				creditCardAccount = new CreditCardAccount();
//				creditCardAccount.setUserId(userIdStr);
//				creditCardAccount.setCreditCardNumber(creditCardNumber);
//				creditCardAccount.setVersion(version);
//				creditCardAccount.setPhone(phone);
//				creditCardAccount.setBillDate(Integer.valueOf(billDate));
//				creditCardAccount.setRepaymentDate(Integer.valueOf(repaymentDate));
//				creditCardAccount.setCreditBlance(new BigDecimal(creditBlance));
//				creditCardAccount.setLastUpdateTime(nowTime);
//				creditCardAccount = creditCardAccountBusiness.save(creditCardAccount);
//				creditCardAccountBusiness.updateCreditCardAccountAndVersion(userIdStr, creditCardNumber, consumeTaskPOJO.getConsumeTaskId(),0, consumeTaskPOJO.getAmount(),"首笔消费任务",version);
                try {
                    JSONObject userInfo = this.getUserInfo(userIdStr);
                    resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                    brandId = userInfo.getString("brandId");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    LOG.error("", e);
                    return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
                }
                creditCardAccountBusiness.createNewAccountAndFirstConsume(userIdStr, creditCardNumber, version, phone, Integer.valueOf(billDate), Integer.valueOf(repaymentDate), new BigDecimal(creditBlance), consumeTaskPOJO, brandId);
            }
            Map<String, Object> executeFirstRepaymentTask = (Map<String, Object>) this.executeFirstRepaymentTask(request, userIdStr, creditCardNumber, version);
            if (CommonConstants.SUCCESS.equals(executeFirstRepaymentTask.get(CommonConstants.RESP_CODE))) {
                RepaymentTaskPOJO repaymentTaskPOJO = (RepaymentTaskPOJO) executeFirstRepaymentTask.get(CommonConstants.RESULT);
//				creditCardAccountBusiness.updateCreditCardAccount(userIdStr, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(), 1, repaymentTaskPOJO.getRealAmount(), "首笔还款任务");
            }
            executeFirstRepaymentTask.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            return executeFirstRepaymentTask;
        }

        boolean verifyDoesHaveTaskType0Task = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskType0Task(userIdStr, creditCardNumber, version);
        if (!verifyDoesHaveTaskType0Task) {
            return ResultWrap.err(LOG, CommonConstants.FALIED, "已有首笔验证任务,请稍后重试!");
        }
        //用户通道费率
        Map<String, Object> userChannelRate = getUserChannelRate(userIdStr, brandId, version);
        if (!CommonConstants.SUCCESS.equals(userChannelRate.get(CommonConstants.RESP_CODE))) {
            return userChannelRate;
        }
        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);

        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        BigDecimal serviceCharge = BigDecimal.ZERO;
        // 沃付还款版本号
        if (CardConstss.CARD_VERSION_1.equals(version)) {
            //成本费率+额外费率+2
            serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).add(BigDecimal.valueOf(2)).setScale(2, BigDecimal.ROUND_UP);
        } else {
            //成本费率+额外费率
            serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        }
        //费率
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
        //第一笔金额+服务费/1-费率
        BigDecimal amount = BigDecimal.valueOf(firstMoney).add(serviceCharge).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP);

        /**
         * 设置还款 消费参数并保存
         */
        RepaymentTaskPOJO repaymentTaskPOJO = new RepaymentTaskPOJO();
        Date nowTime = new Date();
        Random random = new Random();
        repaymentTaskPOJO.setAmount(new BigDecimal(firstMoney));
        repaymentTaskPOJO.setRealAmount(new BigDecimal(firstMoney));
        repaymentTaskPOJO.setUserId(userIdStr);
        repaymentTaskPOJO.setChannelId(repaymentChannelId);
        repaymentTaskPOJO.setChannelTag(repaymentChannelTag);
        repaymentTaskPOJO.setServiceCharge(serviceCharge);
        repaymentTaskPOJO.setTotalServiceCharge(amount.subtract(BigDecimal.valueOf(firstMoney)));
        repaymentTaskPOJO.setRate(rate);
        repaymentTaskPOJO.setVersion(version);
        repaymentTaskPOJO.setRepaymentTaskId(DateUtil.getDateStringConvert(new String(), nowTime, "yyyyMMddHHSSS") + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + "1");
        repaymentTaskPOJO.setCreditCardNumber(creditCardNumber);
        repaymentTaskPOJO.setDescription("尾号" + creditCardNumber.substring(creditCardNumber.length() - 4) + "首笔还款任务");
        repaymentTaskPOJO.setCreateTime(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:ss:mm"));
        repaymentTaskPOJO.setExecuteDate(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd"));
        repaymentTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:ss:mm"));
        repaymentTaskPOJO.setTaskType(0);
        ConsumeTaskPOJO consumeTaskPOJO = new ConsumeTaskPOJO();
        BeanUtils.copyProperties(repaymentTaskPOJO, consumeTaskPOJO);
        consumeTaskPOJO.setAmount(BigDecimal.valueOf(firstMoney));
        consumeTaskPOJO.setRealAmount(amount);
        consumeTaskPOJO.setChannelId(consumeChannelId);
        consumeTaskPOJO.setChannelTag(consumeChannelTag);
        consumeTaskPOJO.setVersion(version);
        consumeTaskPOJO.setServiceCharge(repaymentTaskPOJO.getTotalServiceCharge());
        consumeTaskPOJO.setConsumeTaskId(Long.valueOf(repaymentTaskPOJO.getRepaymentTaskId()) + 1 + "");
        consumeTaskPOJO.setDescription("尾号" + creditCardNumber.substring(creditCardNumber.length() - 4) + "首笔消费任务");
        consumeTaskPOJO.setRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
        consumeTaskPOJO.setCreateTime(DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:ss:mm"));

        repaymentTaskPOJOBusiness.saveReapymentTaskAndConsumeTask(repaymentTaskPOJO, consumeTaskPOJO);

        Map<String, Object> consumeMap;
        try {
            consumeMap = (Map<String, Object>) this.executeFirstConsumeTask(request, userIdStr, creditCardNumber, version, phone, creditBlance, billDate, repaymentDate);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "等待银行扣款,请稍后!");
            return map;
        }
        if (!CommonConstants.SUCCESS.equals(consumeMap.get(CommonConstants.RESP_CODE))) {
            if (CardConstss.WAIT_NOTIFY.equals(consumeMap.get(CommonConstants.RESP_CODE))) {
                return consumeMap;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, consumeMap.containsKey(CommonConstants.RESP_MESSAGE) ? ((String) consumeMap.get(CommonConstants.RESP_MESSAGE)).replaceAll("[0-9]", "").replaceAll("[\\[|\\]|\\.|\\:]", "").replaceAll("[A-Za-z]", "") : "消费失败!");
                return map;
            }
        }


//		nowTime = new Date();
//		CreditCardAccount creditCardAccount = null;
//		creditCardAccount = creditCardAccountBusiness.findByCreditCardNumberAndVersion(creditCardNumber, version);
//		if (creditCardAccount == null) {
//			creditCardAccount = creditCardAccountBusiness.createNewAccount(userIdStr, creditCardNumber, version, phone, Integer.valueOf(billDate), Integer.valueOf(repaymentDate), new BigDecimal(creditBlance));
//		}
//		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userIdStr, creditCardNumber, consumeTaskPOJO.getConsumeTaskId(),0, consumeTaskPOJO.getAmount(),"首笔消费任务",version);


//		consumeTaskPOJO = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskPOJO.getConsumeTaskId());
//		consumeTaskPOJO.setReturnMessage("消费成功");
//		consumeTaskPOJO.setTaskStatus(1);
//		consumeTaskPOJO.setOrderStatus(1);
//		consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
		/*creditCardAccount.setUserId(userIdStr);
		creditCardAccount.setCreditCardNumber(creditCardNumber);
		creditCardAccount.setVersion(version);
		creditCardAccount.setPhone(phone);
//		creditCardAccount.setBlance(creditCardAccount.getBlance().add(consumeTaskPOJO.getAmount()));
		creditCardAccount.setBillDate(Integer.valueOf(billDate));
		creditCardAccount.setRepaymentDate(Integer.valueOf(repaymentDate));
		creditCardAccount.setCreditBlance(new BigDecimal(creditBlance));
		creditCardAccount.setLastUpdateTime(nowTime);
		creditCardAccount = creditCardAccountBusiness.save(creditCardAccount);*/
//		creditCardAccountHistoryBusiness.createNewHistory(0, amount, consumeTaskPOJO.getConsumeTaskId(), creditCardAccount.getId(), creditCardAccount.getBlance(), "首笔消费任务");

        Map<String, Object> repaymentMap;
        try {
            repaymentMap = (Map<String, Object>) this.executeFirstRepaymentTask(request, userIdStr, creditCardNumber, version);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "等待出款中,请等待!");
            return map;
        }
        if (!CommonConstants.SUCCESS.equals(repaymentMap.get(CommonConstants.RESP_CODE))) {
            return repaymentMap;
        }
        repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
        repaymentTaskPOJO.setReturnMessage("还款成功!");
        repaymentTaskPOJO.setTaskStatus(1);
        repaymentTaskPOJO.setOrderStatus(1);
        repaymentTaskPOJO = repaymentTaskPOJOBusiness.updateTaskStatusAndOrderStatusAndReturnMessageByRepaymentTaskId(1, 1, "还款成功!", repaymentTaskPOJO.getRepaymentTaskId());
        creditCardAccountBusiness.updateCreditCardAccountAndVersion(userIdStr, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(), 4, repaymentTaskPOJO.getRealAmount(), "首笔还款成功减少冻结余额", version, repaymentTaskPOJO.getCreateTime());
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "首笔任务完成,可进入生成计划!");
        return map;
    }

    //	修改任务状态回调
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/update/taskstatus/by/ordercode")
    public @ResponseBody
    Object updateTaskStatusByOrderCode(HttpServletRequest request,
                                       @RequestParam(value = "orderCode") String orderCode,
                                       @RequestParam(value = "version") String version
    ) {
        Map<String, Object> map = baseExecutor.updateTaskStatusByOrderCode(orderCode, version);
        if (map.get("consumeTaskPOJO") != null) {
            ConsumeTaskPOJO consumeTaskPOJO = (ConsumeTaskPOJO) map.get("consumeTaskPOJO");
            //判断是否是首笔任务且是已完成状态
            if (consumeTaskPOJO.getTaskType() == 0 && consumeTaskPOJO.getOrderStatus() == 1) {
                this.executeFirstRepaymentTask(request, consumeTaskPOJO.getUserId(), consumeTaskPOJO.getCreditCardNumber(), version);
            }
        }
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/go/to/first/repaymenttask")
    public @ResponseBody
    Object executeFirstRepaymentTask(HttpServletRequest request,
                                     @RequestParam(value = "userId") String userId,
                                     @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                     @RequestParam(value = "version") String version
    ) {
        Map<String, Object> map = new HashMap<>();
        RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByTaskType0AndTaskStatus0RepaymentTaskPOJOAndVersion(userId, creditCardNumber, version);
        if (repaymentTaskPOJO == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您没有待执行的还款任务!");
            return map;
        }
        Map<String, Object> executeRepaymentTaskMap = null;
        CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        try {
            if (creditCardAccount.getBlance().compareTo(repaymentTaskPOJO.getAmount()) >= 0) {
                executeRepaymentTaskMap = repaymentExecutor.executeRepaymentTask(repaymentTaskPOJO);
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "您有待完成的首笔还款任务,请等待完成后再点击,如果等待时间过长,请联系管理员!");
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
            repaymentTaskPOJO.setErrorMessage(e.toString().substring(0, e.toString().length() >= 250 ? 250 : e.toString().length()));
            repaymentTaskPOJO.setTaskStatus(1);
            repaymentTaskPOJO.setReturnMessage("等待出款中,请等待!");
            repaymentTaskPOJO.setOrderStatus(4);
            repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "等待出款中,请稍后");
            return map;
        }
        repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
        if (!CommonConstants.SUCCESS.equals(executeRepaymentTaskMap.get(CommonConstants.RESP_CODE))) {
            if (CardConstss.WAIT_NOTIFY.equals(executeRepaymentTaskMap.get(CommonConstants.RESP_CODE))) {
                repaymentTaskPOJO.setTaskStatus(1);
                repaymentTaskPOJO.setReturnMessage("等待出款中,请等待!");
                repaymentTaskPOJO.setOrderStatus(4);
                repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "等待出款中,请稍后");
            } else {
                repaymentTaskPOJO.setErrorMessage(executeRepaymentTaskMap.containsKey(CommonConstants.RESP_MESSAGE) ? (String) executeRepaymentTaskMap.get(CommonConstants.RESP_MESSAGE) : "还款失败!");
                repaymentTaskPOJO.setReturnMessage("还款失败!");
                repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
                creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(), 3, repaymentTaskPOJO.getRealAmount(), "首笔还款失败增加余额", version, repaymentTaskPOJO.getCreateTime());
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, executeRepaymentTaskMap.containsKey(CommonConstants.RESP_MESSAGE) ? executeRepaymentTaskMap.get(CommonConstants.RESP_MESSAGE) : "还款失败!");
            }
            return map;
        }
        repaymentTaskPOJO.setReturnMessage("还款成功!");
        repaymentTaskPOJO.setTaskStatus(1);
        repaymentTaskPOJO.setOrderStatus(1);
        repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
        creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(), 4, repaymentTaskPOJO.getRealAmount(), "首笔还款成功减少冻结余额", version, repaymentTaskPOJO.getCreateTime());
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "首笔还款任务成功,请点击进入生成计划页面!");
        map.put(CommonConstants.RESULT, repaymentTaskPOJO);
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/go/to/first/consumetask")
    public @ResponseBody
    Object executeFirstConsumeTask(HttpServletRequest request,
                                   @RequestParam(value = "userId") String userId,
                                   @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                   @RequestParam(value = "version", required = false, defaultValue = "1") String version,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   @RequestParam(value = "creditBlance", required = false) String creditBlance,
                                   @RequestParam(value = "billDate", required = false) String billDate,
                                   @RequestParam(value = "repaymentDay", required = false) String repaymentDay
    ) {
        Map<String, Object> map = new HashMap<>();
        ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByTaskType0AndTaskStatus0ConsumeTaskPOJOAndVersion(userId, creditCardNumber, version);
        if (consumeTaskPOJO == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您没有待执行的消费任务!");
            return map;
        }
        Map<String, Object> executeRepaymentTaskMap;
        try {
            executeRepaymentTaskMap = consumeExecutor.executeConsumeTask(consumeTaskPOJO);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            consumeTaskPOJO = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskPOJO.getConsumeTaskId());
            consumeTaskPOJO.setTaskStatus(1);
            consumeTaskPOJO.setOrderStatus(4);
            consumeTaskPOJO.setReturnMessage("等待银行扣款,请稍后!");
            consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中,请稍后!");
            return map;
        }
        consumeTaskPOJO = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskPOJO.getConsumeTaskId());
        if (!CommonConstants.SUCCESS.equals(executeRepaymentTaskMap.get(CommonConstants.RESP_CODE))) {
            if (CardConstss.WAIT_NOTIFY.equals(executeRepaymentTaskMap.get(CommonConstants.RESP_CODE))) {
                consumeTaskPOJO.setTaskStatus(1);
                consumeTaskPOJO.setOrderStatus(4);
                consumeTaskPOJO.setReturnMessage("等待银行扣款,请稍后!");
                consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
            }
            return executeRepaymentTaskMap;
        }
        creditBlance = (creditBlance == null || "".equals(creditBlance.trim())) ? "0" : creditBlance;
        billDate = (billDate == null || "".equals(billDate.trim())) ? "0" : billDate;
        repaymentDay = (repaymentDay == null || "".equals(repaymentDay.trim())) ? "0" : repaymentDay;

        String brandId = "0";
        try {
            JSONObject userInfo = this.getUserInfo(userId);
            userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
            brandId = userInfo.getString("brandId");
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOG.error("", e);
            return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
        }
        creditCardAccountBusiness.createNewAccountAndFirstConsume(userId, creditCardNumber, version, phone, Integer.valueOf(billDate), Integer.valueOf(repaymentDay), new BigDecimal(creditBlance), consumeTaskPOJO, brandId);
//		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, consumeTaskPOJO.getConsumeTaskId(),0, consumeTaskPOJO.getAmount(),"首笔消费任务",version);

        consumeTaskPOJO.setTaskStatus(1);
        consumeTaskPOJO.setOrderStatus(1);
        consumeTaskPOJO.setReturnMessage("消费成功!");
        consumeTaskPOJOBusiness.save(consumeTaskPOJO);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "首笔消费成功,请继续点击进行还款操作!");
        map.put(CommonConstants.RESULT, consumeTaskPOJO);
        return map;
    }

    /**
     * 获取用户费率
     *
     * @param userId
     * @param brandId
     * @param version
     * @return <p>Description: </p>
     */
    public Map<String, Object> getUserChannelRate(String userId, String brandId, String version) {
        return baseExecutor.getUserChannelRate(userId, brandId, version);
    }


    @RequestMapping(value = "/v1.0/creditcardmanager/create/custom/task")
    public @ResponseBody
    Object createCustomPlan(
            @RequestParam() String taskJSON,
            @RequestParam() String province,
            @RequestParam() String city,
            @RequestParam() String userId,
            @RequestParam() String creditCardNumber
    ) {
        CreditCardManagerConfig managerConfig = creditCardManagerConfigBusiness.findByVersion("6");
        String channelId = managerConfig.getChannelId();
        String channelTag = managerConfig.getChannelTag();
        JSONObject userInfo = baseExecutor.getUserInfo(userId);
        userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
        String brandId = userInfo.getString("brandId");

        Map<String, Object> map = this.getUserChannelRate(userId, brandId, "6");
        if (!CommonConstants.SUCCESS.equals(map.get(CommonConstants.RESP_CODE))) {
            return map;
        }
        JSONObject rateJSON = (JSONObject) map.get(CommonConstants.RESULT);
        String rateStr = rateJSON.getString("rate");
        String extraFeeStr = rateJSON.getString("extraFee");
        String withdrawFeeStr = rateJSON.getString("withdrawFee");
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);

        List<RepaymentTaskVO> repaymentTaskVOs = new ArrayList<>();
        Date nowTime = new Date();
        JSONArray taskArray = JSONArray.fromObject(taskJSON);
        for (Object object : taskArray) {
            RepaymentTaskVO repaymentTaskVO = new RepaymentTaskVO();
            JSONObject task = (JSONObject) object;
            String amounts = task.getString("amount");
            String executeDateTimes = task.getString("execute_date_time") + ":00";
            JSONArray consumeTaskArray = task.getJSONArray("consumeTask");

            repaymentTaskVO.setUserId(userId);
            repaymentTaskVO.setCreditCardNumber(creditCardNumber);
            repaymentTaskVO.setChannelId(channelId);
            repaymentTaskVO.setChannelTag(channelTag);
            repaymentTaskVO.setAmount(new BigDecimal(amounts));
            repaymentTaskVO.setServiceCharge(serviceCharge);
            repaymentTaskVO.setRate(rate);
            repaymentTaskVO.setDescription("还款计划");
            String createTime = DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd HH:mm:ss");
            repaymentTaskVO.setCreateTime(createTime);
            String executeDate = DateUtil.getDateStringConvert(new String(), executeDateTimes, "yyyy-MM-dd");
            repaymentTaskVO.setExecuteDate(executeDate);

            repaymentTaskVO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), executeDateTimes, "yyyy-MM-dd HH:mm:ss"));
            repaymentTaskVO.setRepaymentTaskId(repaymentTaskVO.getExecuteDate().replace("-", "") + DateUtil.getDateStringConvert(new String(), new Date(), "HHSSS") + new Random().nextInt(9) + new Random().nextInt(9) + new Random().nextInt(9) + new Random().nextInt(9) + "1");
            List<ConsumeTaskVO> consumeTaskVOs = repaymentTaskVO.getConsumeTaskVOs();
            if (consumeTaskArray.size() > 3) {
                return ResultWrap.init(CommonConstants.FALIED, "单笔还款最多设置3笔消费任务");
            }
            for (Object object2 : consumeTaskArray) {

                JSONObject consumeTask = (JSONObject) object2;
                String consumeAmounts = consumeTask.getString("amount");
                String consumeExecuteDateTimes = consumeTask.getString("execute_date_time") + ":00";
                String consumeMerchant = consumeTask.getString("consumeMerchant");
                ConsumeTaskVO consumeTaskVO = new ConsumeTaskVO();
                consumeTaskVO.setAmount(new BigDecimal(consumeAmounts));
                consumeTaskVO.setRepaymentTaskId(repaymentTaskVO.getRepaymentTaskId());
                consumeTaskVO.setUserId(userId);
                consumeTaskVO.setCreditCardNumber(creditCardNumber);
                consumeTaskVO.setChannelId(channelId);
                consumeTaskVO.setChannelTag(channelTag);
                consumeTaskVO.setDescription("消费计划");
                consumeTaskVO.setConsumeType(consumeMerchant);
                consumeTaskVO.setExecuteDate(executeDate);
                consumeTaskVO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), consumeExecuteDateTimes, "yyyy-MM-dd HH:mm:ss"));
                consumeTaskVO.setCreateTime(createTime);
                consumeTaskVOs.add(consumeTaskVO);
            }

            if (new BigDecimal(amounts).compareTo(managerConfig.getPaySingleMaxMoney()) > 0) {
                return ResultWrap.init(CommonConstants.FALIED, "单笔还款任务金额最大为:" + managerConfig.getPaySingleMaxMoney().toString() + "元");
            }
            repaymentTaskVOs.add(repaymentTaskVO);
        }
        Collections.sort(repaymentTaskVOs, new Comparator<RepaymentTaskVO>() {
            @Override
            public int compare(RepaymentTaskVO o1, RepaymentTaskVO o2) {
                Date date1 = DateUtil.getDateStringConvert(new Date(), o1.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
                Date date2 = DateUtil.getDateStringConvert(new Date(), o2.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
                return date1.compareTo(date2);
            }
        });
        int repaymentCount = taskArray.size();
        int consumeCount = 0;
        BigDecimal allConsumeAmount = BigDecimal.ZERO;
        BigDecimal allRepaymentAmount = BigDecimal.ZERO;
        BigDecimal allServiceCharge = BigDecimal.ZERO;
        BigDecimal maxRepaymentAmount = BigDecimal.ZERO;
        List<Map<String, Date>> repaymentExecuteDates = new ArrayList<>();
        Date minDate = null;
        Date maxDate = null;
        int r = 0;
        for (RepaymentTaskVO repaymentTaskVO : repaymentTaskVOs) {
            Map<String, Date> dateMap = new HashMap<>();
            Date repaymentDateTemp = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
            dateMap.put("maxDate", repaymentDateTemp);
            if (r == 0) {
                minDate = repaymentDateTemp;
            }

            if (r == repaymentTaskVOs.size() - 1) {
                maxDate = repaymentDateTemp;
            }

            for (Map<String, Date> map2 : repaymentExecuteDates) {
                Date dateMin = map2.get("minDate");
                Date dateMax = map2.get("maxDate");
                if (dateMin.compareTo(repaymentDateTemp) <= 0 && dateMax.compareTo(repaymentDateTemp) >= 0) {
                    return ResultWrap.init(CommonConstants.FALIED, repaymentTaskVO.getExecuteDateTime() + "该任务时间与其他任务时间冲突,请调整");
                }
            }

            BigDecimal totalServiceCharge = BigDecimal.ZERO;
            allRepaymentAmount = allRepaymentAmount.add(repaymentTaskVO.getAmount());
            List<ConsumeTaskVO> consumeTaskVOs = repaymentTaskVO.getConsumeTaskVOs();
            Collections.sort(consumeTaskVOs, new Comparator<ConsumeTaskVO>() {
                @Override
                public int compare(ConsumeTaskVO o1, ConsumeTaskVO o2) {
                    Date date1 = DateUtil.getDateStringConvert(new Date(), o1.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
                    Date date2 = DateUtil.getDateStringConvert(new Date(), o2.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
                    return date1.compareTo(date2);
                }
            });
            int i = 0;
            Date minConsumeDate = null;
            for (ConsumeTaskVO consumeTaskVO : consumeTaskVOs) {
                consumeCount++;
                BigDecimal consumeRealAmount = BigDecimal.ZERO;
                BigDecimal consumeServiceCharge = BigDecimal.ZERO;
                BigDecimal consumeAmounts = consumeTaskVO.getAmount();
                Date consumeDateTemp = DateUtil.getDateStringConvert(new Date(), consumeTaskVO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
                ;
                if (i == 0) {
                    minConsumeDate = consumeDateTemp;

                    consumeRealAmount = consumeAmounts.add(serviceCharge).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP);
                    consumeServiceCharge = consumeRealAmount.subtract(consumeAmounts).setScale(2, BigDecimal.ROUND_UP);
                } else {
                    consumeRealAmount = consumeAmounts.divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP);
                    consumeServiceCharge = consumeRealAmount.subtract(consumeAmounts).setScale(2, BigDecimal.ROUND_UP);
                }

                if (consumeTaskVOs.size() - 1 == i) {
                    if (new Date(consumeDateTemp.getTime() + 30 * 60 * 1000).getTime() > repaymentDateTemp.getTime()) {
                        return ResultWrap.init(CommonConstants.FALIED, consumeTaskVO.getExecuteDateTime() + "该时间需比" + repaymentTaskVO.getExecuteDateTime() + "早30分钟以上");
                    }
                }

                consumeTaskVO.setRealAmount(consumeRealAmount);
                consumeTaskVO.setServiceCharge(consumeServiceCharge);
                consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVO.getRepaymentTaskId()) + (i + 1) + "");
                totalServiceCharge = totalServiceCharge.add(consumeServiceCharge);
                allConsumeAmount = allConsumeAmount.add(consumeRealAmount);
                if (consumeRealAmount.compareTo(managerConfig.getConSingleMaxMoney()) > 0) {
                    return ResultWrap.init(CommonConstants.FALIED, "单笔消费任务金额最大为:" + managerConfig.getConSingleMaxMoney().subtract(consumeServiceCharge).toString() + "元");
                }

                for (Map<String, Date> map2 : repaymentExecuteDates) {
                    Date dateMin = map2.get("minDate");
                    Date dateMax = map2.get("maxDate");
                    if (dateMin.compareTo(consumeDateTemp) <= 0 && dateMax.compareTo(consumeDateTemp) >= 0) {
                        return ResultWrap.init(CommonConstants.FALIED, consumeTaskVO.getExecuteDateTime() + "该任务时间与其他任务时间冲突,请调整");
                    }
                }

                i++;
            }
            dateMap.put("minDate", minConsumeDate);
            repaymentTaskVO.setTotalServiceCharge(totalServiceCharge);
            allServiceCharge = allServiceCharge.add(totalServiceCharge);
            if (maxRepaymentAmount.compareTo(repaymentTaskVO.getAmount().add(repaymentTaskVO.getTotalServiceCharge())) < 0) {
                maxRepaymentAmount = repaymentTaskVO.getAmount().add(repaymentTaskVO.getTotalServiceCharge());
            }
            repaymentExecuteDates.add(dateMap);
            r++;
        }
        String executeDates = DateUtil.getDateStringConvert(new String(), minDate, "yyyy/MM/dd") + "-" + DateUtil.getDateStringConvert(new String(), maxDate, "yyyy/MM/dd");
        Map<String, Object> reuslt = ResultWrap.init(CommonConstants.SUCCESS, "请求成功", repaymentTaskVOs);
        reuslt.put("allConsumeAmount", allConsumeAmount);
        reuslt.put("allRepaymentAmount", allRepaymentAmount);
        reuslt.put("allServiceCharge", allServiceCharge);
        reuslt.put("reservedAmount", maxRepaymentAmount.setScale(0, BigDecimal.ROUND_UP));
        reuslt.put("consumeCount", consumeCount);
        reuslt.put("repaymentCount", repaymentCount);
        reuslt.put("executeDates", executeDates);
        reuslt.put("rate", rate);
        reuslt.put("serviceCharge", serviceCharge);
        reuslt.put("city", province + "-" + city);
        reuslt.put("version", "6");
        reuslt.put("creditCardNumber", creditCardNumber);
        reuslt.put("userId", userId);
        return reuslt;
    }

    /**
     * 自动匹配可用的还款通道
     *
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param amounts
     * @param reservedAmounts
     * @param brandId
     * @param executeDates
     * @return <p>Description: </p>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/chooes/repayment/channel")
    public @ResponseBody
    Object chooesRepaymentChannel(HttpServletRequest request,
                                  @RequestParam(value = "userId") String userId,
                                  @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                  @RequestParam(value = "amount") String amounts,
                                  @RequestParam(value = "reservedAmount") String reservedAmounts,
                                  @RequestParam(value = "brandId") String brandId,
                                  @RequestParam(value = "executeDate") String[] executeDates
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        amounts = amounts.trim();
        reservedAmounts = reservedAmounts.trim();
        brandId = brandId.trim();
        try {
            if (BigDecimal.valueOf(20).compareTo(new BigDecimal(amounts)) >= 0) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "还款金额过小");
            }
            if (BigDecimal.valueOf(20).compareTo(new BigDecimal(reservedAmounts)) >= 0) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "预留金额过小");
            }
        } catch (Exception e) {
            return ResultWrap.init(CardConstss.FAIL_CODE, "金额输入有误");
        }

        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(userId, creditCardNumber, null);
        if (doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            return ResultWrap.init(CardConstss.FAIL_CODE, "有未执行计划,请等待任务执行完毕后再制定任务");
        }

        String url = "http://user/v1.0/user/bank/verify/isuseable";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        requestEntity.add("bankCardNumber", creditCardNumber);
        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSONObject = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CardConstss.FAIL_CODE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
        }

        JSONObject bankInfoJSON = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        String bankName = bankInfoJSON.getString("bankName");
        int billDate = bankInfoJSON.getInt("billDay");
        int repaymentDate = bankInfoJSON.getInt("repaymentDay");
        String securityCode = bankInfoJSON.getString("securityCode");
        String expiredTime = bankInfoJSON.getString("expiredTime");
        if (securityCode != null && (securityCode.length() != 3 || !securityCode.matches("^[0-9]*$"))) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "安全码有误,请重新设置");
        }
        if (expiredTime != null && (expiredTime.length() != 4 || !expiredTime.matches("^[0-9]*$"))) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "有效期有误,请重新设置");
        }
        if (billDate == 0 || repaymentDate == 0) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "请设置账单日和还款日");
        }

        if (repaymentDate - billDate < 3 && repaymentDate - billDate > -3) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "账单日和还款日设置有误,请重新设置");
        }


//		验证日期格式是否正确
        Date[] executeDate = new Date[executeDates.length];
        try {
            for (int i = 0; i < executeDates.length; i++) {
                executeDate[i] = DateUtil.getDateStringConvert(new Date(), executeDates[i], "yyyy-MM-dd");
            }
        } catch (Exception e) {
            executeDate = getExecuteDateBeforeRepaymentDay(billDate, repaymentDate);
            executeDates = new String[executeDate.length];
            for (int i = 0; i < executeDate.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new String(), executeDate[i], "yyyy-MM-dd");
            }
        }
        if (executeDate.length < 1) {
            executeDate = getExecuteDateBeforeRepaymentDay(billDate, repaymentDate);
            executeDates = new String[executeDate.length];
            for (int i = 0; i < executeDate.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new String(), executeDate[i], "yyyy-MM-dd");
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dateNow = calendar.getTime();
        Integer executeDay = 0;
//		验证日期是否是今天以后
        for (int i = 0; i < executeDates.length; i++) {
            if (dateNow.getTime() > executeDate[i].getTime()) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "请选择今天以后的日期");
            }
//			如果有选择账单日和还款日,则验证任务执行日期是否在账单日之后还款日之前
            if (billDate != 0 && repaymentDate != 0) {
                executeDay = Integer.valueOf(DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), executeDates[i], "yyyy-MM-dd"), "dd"));
                if (billDate > repaymentDate) {
                    if (!((billDate <= executeDay) || (repaymentDate >= executeDay))) {
                        return ResultWrap.init(CardConstss.FAIL_CODE, "请在账单日和还款日前之间选择日期");
                    }
                } else {
                    if (!((billDate <= executeDay) && (repaymentDate >= executeDay))) {
                        return ResultWrap.init(CardConstss.FAIL_CODE, "请在账单日和还款日前之间选择日期");
                    }
                }
            }
        }

        Map<String, Object> chooesRepaymentChannel = this.chooesRepaymentChannel(userId, creditCardNumber, brandId, amounts, reservedAmounts, executeDates, bankName);
        if (!CommonConstants.SUCCESS.equals(chooesRepaymentChannel.get(CommonConstants.RESP_CODE))) {
            return chooesRepaymentChannel;
        }

        List<CreditCardManagerConfig> creditCardManagerConfigs = (List<CreditCardManagerConfig>) chooesRepaymentChannel.get(CommonConstants.RESULT);
        String versions = "";
        for (CreditCardManagerConfig creditCardManagerConfig : creditCardManagerConfigs) {
            String version = creditCardManagerConfig.getVersion();
            RepaymentBrandStatus repaymentBrandStatusByBrandIdAndVersion = repaymentDetailBusiness.getRepaymentBrandStatusByBrandIdAndVersion(Integer.parseInt(brandId), version);

            if (repaymentBrandStatusByBrandIdAndVersion != null) {
                if (repaymentBrandStatusByBrandIdAndVersion.getStatus() != 1) {
                    continue;
                } else {
                    versions = versions + creditCardManagerConfig.getVersion() + ",";
                }
            } else {
                versions = versions + creditCardManagerConfig.getVersion() + ",";
            }
        }

        if ("".equals(versions)) {

            return ResultWrap.init(CommonConstants.FALIED, "暂无还款通道,请联系相关人员及时解决!");
        }

        versions = versions.substring(0, versions.length());
        url = "http://paymentchannel/v1.0/paymentchannel/topup/repaymentdetail/get/by/versions";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("versions", versions);
        resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSON = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CardConstss.FAIL_CODE, resultJSON.getString(CommonConstants.RESP_MESSAGE));
        }
        JSONArray resultArray = resultJSON.getJSONArray(CommonConstants.RESULT);
        for (Iterator iterator = resultArray.iterator(); iterator.hasNext(); ) {
            JSONObject json = (JSONObject) iterator.next();
            if (!"1".equals(json.getString("onOff"))) {
                iterator.remove();
                continue;
            }
            String version = json.getString("version");
            Map<String, Object> userChannelRate = this.getUserChannelRate(userId, brandId, version);
            JSONObject userRate = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
            String rateStr = userRate.getString("rate");
            String extraFeeStr = userRate.getString("extraFee");
            String withdrawFeeStr = userRate.getString("withdrawFee");
            BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
            ;
            BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
            json.put("rate", rate);
            json.put("serviceCharge", serviceCharge);
        }
        //将sort为1的通道显示顺序打乱
        List<JSONObject> list = new ArrayList<>();
        for (Iterator iterator = resultArray.iterator(); iterator.hasNext(); ) {
            JSONObject json = (JSONObject) iterator.next();
            if ("1".equals(json.getString("sort"))) {
                System.out.println(json);
                list.add(json);
                iterator.remove();
            }
        }
        Collections.shuffle(list);
        System.out.println("list====" + list);
        System.out.println("listsize====" + list.size());
        for (JSONObject afterJSONObject : list) {
            resultArray.add(afterJSONObject);
        }
        //fastJson做排序处理
        com.alibaba.fastjson.JSONObject fastJsonObject = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONArray afterArray = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject afterJson = null;
        String text = "";
        for (int i = 0; i < resultArray.size(); i++) {
            text = resultArray.getJSONObject(i).toString();
            afterJson = fastJsonObject.parseObject(text);
            afterArray.add(afterJson);
        }
        System.out.println("fastJson===" + afterArray);
        com.alibaba.fastjson.JSONArray array = arrayUtil.arraySort(afterArray);

        System.out.println(array.toString().replaceAll("null", "\"\""));
        Map<String, Object> map = ResultWrap.init(CommonConstants.SUCCESS, "查询成功", JSONArray.fromObject(array.toString().replaceAll("null", "\"\"")));
        map.put("userId", userId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amounts);
        map.put("reservedAmount", reservedAmounts);
        map.put("brandId", brandId);
        map.put("executeDate", executeDates);
        return map;
    }

    /**
     * 自动匹配可用的还款通道,极速还款
     *
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param amounts
     * @param reservedAmounts
     * @param brandId
     * @param executeDates
     * @return <p>Description: </p>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/chooes/repayment/quickchannel")
    public @ResponseBody
    Object chooesQuickRepaymentChannel(HttpServletRequest request,
                                       @RequestParam(value = "userId") String userId,
                                       @RequestParam(value = "creditCardNumber") String creditCardNumber,
                                       @RequestParam(value = "amount") String amounts,
                                       @RequestParam(value = "reservedAmount") String reservedAmounts,
                                       @RequestParam(value = "brandId") String brandId,
                                       @RequestParam(value = "executeDate") String[] executeDates,
                                       @RequestParam(value = "dayRepaymentCount", defaultValue = "3", required = false) String count  //一天还款的笔数
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        amounts = amounts.trim();
        reservedAmounts = reservedAmounts.trim();
        brandId = brandId.trim();
        try {
            if (BigDecimal.valueOf(20).compareTo(new BigDecimal(amounts)) >= 0) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "还款金额过小");
            }
            if (BigDecimal.valueOf(20).compareTo(new BigDecimal(reservedAmounts)) >= 0) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "预留金额过小");
            }
        } catch (Exception e) {
            return ResultWrap.init(CardConstss.FAIL_CODE, "金额输入有误");
        }

        boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(userId, creditCardNumber, null);
        if (doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO) {
            return ResultWrap.init(CardConstss.FAIL_CODE, "有未执行计划,请等待任务执行完毕后再制定任务");
        }

        String url = "http://user/v1.0/user/bank/verify/isuseable";
        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        requestEntity.add("bankCardNumber", creditCardNumber);
        String resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSONObject = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CardConstss.FAIL_CODE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty() ? "该卡不可用,请更换一张信用卡!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
        }

        JSONObject bankInfoJSON = resultJSONObject.getJSONObject(CommonConstants.RESULT);
        String bankName = bankInfoJSON.getString("bankName");
        int billDate = bankInfoJSON.getInt("billDay");
        int repaymentDate = bankInfoJSON.getInt("repaymentDay");
        String securityCode = bankInfoJSON.getString("securityCode");
        String expiredTime = bankInfoJSON.getString("expiredTime");
        if (securityCode != null && (securityCode.length() != 3 || !securityCode.matches("^[0-9]*$"))) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "安全码有误,请重新设置");
        }
        if (expiredTime != null && (expiredTime.length() != 4 || !expiredTime.matches("^[0-9]*$"))) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "有效期有误,请重新设置");
        }
        if (billDate == 0 || repaymentDate == 0) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "请设置账单日和还款日");
        }

        if (repaymentDate - billDate < 3 && repaymentDate - billDate > -3) {
            return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "账单日和还款日设置有误,请重新设置");
        }

//		验证日期格式是否正确
        Date[] executeDate = new Date[executeDates.length];
        try {
            for (int i = 0; i < executeDates.length; i++) {
                executeDate[i] = DateUtil.getDateStringConvert(new Date(), executeDates[i], "yyyy-MM-dd");
            }
        } catch (Exception e) {
            executeDate = getExecuteDateBeforeRepaymentDay(billDate, repaymentDate);
            executeDates = new String[executeDate.length];
            for (int i = 0; i < executeDate.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new String(), executeDate[i], "yyyy-MM-dd");
            }
        }
        if (executeDate.length < 1) {
            executeDate = getExecuteDateBeforeRepaymentDay(billDate, repaymentDate);
            executeDates = new String[executeDate.length];
            for (int i = 0; i < executeDate.length; i++) {
                executeDates[i] = DateUtil.getDateStringConvert(new String(), executeDate[i], "yyyy-MM-dd");
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dateNow = calendar.getTime();
        Integer executeDay = 0;
//		验证日期是否是今天以后
        for (int i = 0; i < executeDates.length; i++) {
            if (dateNow.getTime() > executeDate[i].getTime()) {
                return ResultWrap.init(CardConstss.FAIL_CODE, "请选择今天以后的日期");
            }
//			如果有选择账单日和还款日,则验证任务执行日期是否在账单日之后还款日之前
//			if(billDate!=0 && repaymentDate!=0){
//				executeDay = Integer.valueOf(DateUtil.getDateStringConvert(new String(),DateUtil.getDateStringConvert(new Date(), executeDates[i], "yyyy-MM-dd"),"dd"));
//				if(billDate > repaymentDate){
//					if(!((billDate <= executeDay)||(repaymentDate >= executeDay))){
//						return ResultWrap.init(CardConstss.FAIL_CODE,"请在账单日和还款日前之间选择日期");
//					}
//				}else{
//					if(!((billDate <= executeDay)&&(repaymentDate >= executeDay))){
//						return ResultWrap.init(CardConstss.FAIL_CODE,"请在账单日和还款日前之间选择日期");
//					}
//				}
//			}
        }

        Map<String, Object> chooesRepaymentChannel = this.chooesRepaymentQuickChannel(userId, creditCardNumber, brandId, amounts, reservedAmounts, executeDates, bankName, count);
        if (!CommonConstants.SUCCESS.equals(chooesRepaymentChannel.get(CommonConstants.RESP_CODE))) {
            return chooesRepaymentChannel;
        }

        List<CreditCardManagerConfig> creditCardManagerConfigs = (List<CreditCardManagerConfig>) chooesRepaymentChannel.get(CommonConstants.RESULT);
        String versions = "";
        for (CreditCardManagerConfig creditCardManagerConfig : creditCardManagerConfigs) {
            String version = creditCardManagerConfig.getVersion();
            RepaymentBrandStatus repaymentBrandStatusByBrandIdAndVersion = repaymentDetailBusiness.getRepaymentBrandStatusByBrandIdAndVersion(Integer.parseInt(brandId), version);

            if (repaymentBrandStatusByBrandIdAndVersion != null) {
                if (repaymentBrandStatusByBrandIdAndVersion.getStatus() != 1) {
                    continue;
                } else {
                    versions = versions + creditCardManagerConfig.getVersion() + ",";
                }
            } else {
                versions = versions + creditCardManagerConfig.getVersion() + ",";
            }
        }

        if ("".equals(versions)) {

            return ResultWrap.init(CommonConstants.FALIED, "暂无还款通道,请联系相关人员及时解决!");
        }

        versions = versions.substring(0, versions.length());
        url = "http://paymentchannel/v1.0/paymentchannel/topup/repaymentdetail/get/by/versions";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("versions", versions);
        resultString = restTemplate.postForObject(url, requestEntity, String.class);
        JSONObject resultJSON = JSONObject.fromObject(resultString);
        if (!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))) {
            return ResultWrap.init(CardConstss.FAIL_CODE, resultJSON.getString(CommonConstants.RESP_MESSAGE));
        }
        JSONArray resultArray = resultJSON.getJSONArray(CommonConstants.RESULT);
        for (Iterator iterator = resultArray.iterator(); iterator.hasNext(); ) {
            JSONObject json = (JSONObject) iterator.next();
            if (!"1".equals(json.getString("onOff"))) {
                iterator.remove();
                continue;
            }

            String version = json.getString("version");
            Map<String, Object> userChannelRate = this.getUserChannelRate(userId, brandId, version);
            JSONObject userRate = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
            String rateStr = userRate.getString("rate");
            String extraFeeStr = userRate.getString("extraFee");
            String withdrawFeeStr = userRate.getString("withdrawFee");
            BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
            ;
            BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
            json.put("rate", rate);
            json.put("serviceCharge", serviceCharge);
        }
        //将sort为1的通道显示顺序打乱
        List<JSONObject> list = new ArrayList<>();
        for (Iterator iterator = resultArray.iterator(); iterator.hasNext(); ) {
            JSONObject json = (JSONObject) iterator.next();
            if ("1".equals(json.getString("sort"))) {
                System.out.println(json);
                list.add(json);
                iterator.remove();
            }
        }
        Collections.shuffle(list);
        System.out.println("list====" + list);
        System.out.println("listsize====" + list.size());
        for (JSONObject afterJSONObject : list) {
            resultArray.add(afterJSONObject);
        }
        //fastJson做排序处理
        com.alibaba.fastjson.JSONObject fastJsonObject = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONArray afterArray = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject afterJson = null;
        String text = "";
        for (int i = 0; i < resultArray.size(); i++) {
            text = resultArray.getJSONObject(i).toString();
            afterJson = fastJsonObject.parseObject(text);
            afterArray.add(afterJson);
        }
        System.out.println("fastJson===" + afterArray);
        com.alibaba.fastjson.JSONArray array = arrayUtil.arraySort(afterArray);

        System.out.println(array.toString().replaceAll("null", "\"\""));
        Map<String, Object> map = ResultWrap.init(CommonConstants.SUCCESS, "查询成功", JSONArray.fromObject(array.toString().replaceAll("null", "\"\"")));
        map.put("userId", userId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amounts);
        map.put("reservedAmount", reservedAmounts);
        map.put("brandId", brandId);
        map.put("executeDate", executeDates);
        return map;
    }

    private Map<String, Object> chooesRepaymentQuickChannel(String userId, String creditCardNumber, String brandId, String amounts, String reservedAmounts, String[] executeDates, String bankName, String count) {
        BigDecimal amount = new BigDecimal(amounts);
        BigDecimal reservedAmount = new BigDecimal(reservedAmounts);
        List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findByCreateOnOff(1);
        List<CreditCardManagerConfig> supportChannel = new ArrayList<>();
        Map<String, Object> noSupportReason = new HashMap<>();
        for (CreditCardManagerConfig creditCardManagerConfig : creditCardManagerConfigs) {
            BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
            BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();
            String noSupportBank = creditCardManagerConfig.getNoSupportBank();
            String[] noSupportBanks = null;
            if (noSupportBank != null && !"0".equals(noSupportBank)) {
                noSupportBanks = noSupportBank.split("-");
            }
            if (noSupportBanks != null && noSupportBanks.length > 0) {
                boolean noSupport = false;
                for (int i = 0; i < noSupportBanks.length; i++) {
                    if (bankName.trim().contains(noSupportBanks[i].trim())) {
                        noSupport = true;
                        break;
                    }
                }
                if (noSupport) {
                    noSupportReason.put(creditCardManagerConfig.getChannelName(), "需更换银行卡");
                    continue;
                }

            }

//			if (amount.compareTo(paySingleLimitMoney) < 0) {
//				noSupportReason.put(creditCardManagerConfig.getChannelName(), "需增加还款金额");
//				continue;
//			}
//
//			if (reservedAmount.compareTo(paySingleLimitMoney) < 0) {
//				noSupportReason.put(creditCardManagerConfig.getChannelName(), "需增加预留金额");
//				continue;
//			}

//			查询用户费率
            Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), creditCardManagerConfig.getVersion());
            if (!CommonConstants.SUCCESS.equals(userChannelRate.get(CommonConstants.RESP_CODE))) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), userChannelRate.get(CommonConstants.RESP_MESSAGE));
                continue;
            }
            JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
            String rateStr = resultJSONObject.getString("rate");
            String extraFeeStr = resultJSONObject.getString("extraFee");
            String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//			单笔还款手续费
            BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
            ;
//			费率
            BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
            int maxRepaymentCount = 0;
            for (String executeDate : executeDates) {
                executeDate = DateUtil.getDateStringConvert(new String(), executeDate, "yyyy-MM-dd");
                if (todayDate.equals(executeDate)) {
                    calendar.set(Calendar.HOUR_OF_DAY, 9);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (now.compareTo(calendar.getTime()) <= 0) {
                        maxRepaymentCount = maxRepaymentCount + Integer.parseInt(count);
                        continue;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, 13);
                    if (now.compareTo(calendar.getTime()) <= 0) {
                        maxRepaymentCount = maxRepaymentCount + Integer.parseInt(count) - 1;
                        continue;
                    }
//					calendar.set(Calendar.HOUR_OF_DAY, 17);
//					if (now.compareTo(calendar.getTime()) <= 0) {
//						maxRepaymentCount = maxRepaymentCount + creditCardManagerConfig.getPaySingleLimitCount() - 2;
//						continue;
//					}

                } else {
                    maxRepaymentCount = maxRepaymentCount + Integer.parseInt(count);
                }
            }
            LOG.info("maxRepaymentCount==========" + maxRepaymentCount);
            Map<String, Object> createRepaymentAmount = channelFactory.getChannelRoot(creditCardManagerConfig.getVersion()).createRepaymentAmountQuick(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, maxRepaymentCount, rate, serviceCharge, userId, brandId, creditCardManagerConfig.getVersion(), bankName);
            if (!CommonConstants.SUCCESS.equals(createRepaymentAmount.get(CommonConstants.RESP_CODE))) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), createRepaymentAmount.get(CommonConstants.RESP_MESSAGE));
                continue;
            }

            if (CardConstss.CARD_VERSION_10.equals(creditCardManagerConfig.getVersion()) || CardConstss.CARD_VERSION_11.equals(creditCardManagerConfig.getVersion())) {
                List<BigDecimal> amountList = (List<BigDecimal>) createRepaymentAmount.get(CommonConstants.RESULT);

                calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, 1);
                String startDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
                calendar.add(Calendar.MONTH, +1);
                calendar.add(Calendar.DATE, -1);
                String endDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
                String[] versions = new String[]{CardConstss.CARD_VERSION_10, CardConstss.CARD_VERSION_11};
                List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(creditCardNumber, 1, versions, startDate, endDate);
                if ((amountList.size() * 2 + consumeTaskPOJOs.size()) > 20) {
                    continue;
                }
            }

            supportChannel.add(creditCardManagerConfig);
        }
        System.out.println(noSupportReason);
        if (supportChannel.size() < 1) {
            Set<String> keySet = noSupportReason.keySet();
            List<String> suggest = new ArrayList<>();
            if (keySet.size() > 0) {
                for (String key : keySet) {
                    Object value = noSupportReason.get(key);
                    suggest.add(key + ":" + value);
                }
            }
            return ResultWrap.init(CommonConstants.FALIED, "无支持通道,可根据以下建议进行调整", suggest);
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", supportChannel);
    }


    private static Date[] getExecuteDateBeforeRepaymentDay(int billDay, int repaymentDay) {
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(Calendar.DATE);
        int days = repaymentDay;
        if (repaymentDay > billDay) {
            if (date > repaymentDay) {
                calendar.add(Calendar.MONTH, +1);
                calendar.set(Calendar.DATE, billDay);
            } else if (date > billDay) {
                calendar.set(Calendar.DATE, date);
            } else {
                calendar.set(Calendar.DATE, billDay);
            }
        } else {
            calendar.add(Calendar.MONTH, +1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (date > repaymentDay && date < billDay) {
                calendar.set(Calendar.DATE, billDay);
                days = monthDay + repaymentDay;
            } else if (date >= billDay) {
                calendar.set(Calendar.DATE, date);
                days = monthDay + repaymentDay;
            } else {
                calendar.set(Calendar.DATE, date);
            }
        }
        date = calendar.get(Calendar.DATE);
        if (days + 1 - date < 1) {
            return null;
        }
        Date[] executeDates = new Date[days + 1 - date];
        calendar.add(Calendar.DATE, -1);
        for (int i = 0; i < days + 1 - date; i++) {
            calendar.add(Calendar.DATE, +1);
            Date executeDate = DateUtil.getDateStringConvert(new Date(), calendar.getTime(), "yyyy-MM-dd");
            executeDates[i] = executeDate;
        }
        return executeDates;
    }


    @Autowired
    private ChannelFactory channelFactory;

    /**
     * 生成还款任务和消费任务
     *
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param amount
     * @param reservedAmount
     * @param brandId
     * @param executeDate
     * @param version
     * @return <p>Description: </p>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/create/repayment/task")
    public @ResponseBody
    Object createRepaymentTask(HttpServletRequest request,
                               @RequestParam() String userId,
                               @RequestParam() String creditCardNumber,
                               @RequestParam() String amount,
                               @RequestParam() String reservedAmount,
                               @RequestParam() String brandId,
                               @RequestParam() String[] executeDate,
                               @RequestParam() String version,
                               @RequestParam(required = false, defaultValue = "0") String round,
                               @RequestParam(required = false, defaultValue = "3") int conCount
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        Map<String, Object> map = new HashMap<>();
        if ("".equals(userId) || "".equals(creditCardNumber) || "".equals(version)) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证失败,传入参数不能为空!");
            return map;
        }

        JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
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

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            return userChannelRate;
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);

        Map<String, Object> verifyDoesHaveBandCard = creditCardManagerAuthorizationHandle.verifyDoesHaveBandCard(userId, creditCardNumber, rateStr, serviceCharge.toString(), version, resultBankCardJSONObject);
        if (!CommonConstants.SUCCESS.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
            if (!CardConstss.TO_BAND_CARD.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CardConstss.NONSUPPORT, (String) verifyDoesHaveBandCard.get(CommonConstants.RESP_MESSAGE));
            }
            return verifyDoesHaveBandCard;
        }

        CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        if (model == null) {
            String phone = null;
            try {
                JSONObject userInfo = this.getUserInfo(userId);
                resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                phone = resultJSONObject.getString("phone");
                brandId = resultJSONObject.getString("brandId");
            } catch (RuntimeException e) {
                e.printStackTrace();
                LOG.error("", e);
                return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
            }
            model = creditCardAccountBusiness.createNewAccount(userId, creditCardNumber, version, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
        }
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        List<RepaymentTaskVO> creatTemporaryPlan = channelFactory.getChannelRoot(version).creatTemporaryPlan(userId, creditCardNumber, amount, reservedAmount, brandId, creditCardManagerConfig, executeDate, bankName, conCount);
        creatTemporaryPlan.sort(new Comparator<RepaymentTaskVO>() {
            @Override
            public int compare(RepaymentTaskVO o1, RepaymentTaskVO o2) {
                return o1.getExecuteDate().compareTo(o2.getExecuteDate());
            }
        });
        map = ResultWrap.init(CommonConstants.SUCCESS, "请求成功", creatTemporaryPlan);
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        BigDecimal allConsumeAmount = BigDecimal.ZERO;
        int consumeCount = 0;
        int i = 0;
        Date minDate = null;
        Date maxDate = null;
        for (RepaymentTaskVO repaymentTaskVO : creatTemporaryPlan) {
            if (i == 0) {
                minDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            if (i == creatTemporaryPlan.size() - 1) {
                maxDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            totalServiceCharge = totalServiceCharge.add(repaymentTaskVO.getTotalServiceCharge());
            for (ConsumeTaskVO consumeTaskVO : repaymentTaskVO.getConsumeTaskVOs()) {
                allConsumeAmount = allConsumeAmount.add(consumeTaskVO.getRealAmount());
                consumeCount++;
            }
            i++;
        }
        map.put("userId", userId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amount);
        map.put("reservedAmount", reservedAmount);
        map.put("brandId", brandId);
        map.put("executeDate", executeDate);
        map.put("totalServiceCharge", totalServiceCharge);
        map.put("version", version);
        map.put("rate", rate.toString());
        map.put("serviceCharge", serviceCharge);
        map.put("bankName", bankName);

        String executeDates = DateUtil.getDateStringConvert(new String(), minDate, "yyyy/MM/dd") + "-" + DateUtil.getDateStringConvert(new String(), maxDate, "yyyy/MM/dd");
        map.put("allConsumeAmount", allConsumeAmount);
        map.put("allRepaymentAmount", amount);
        map.put("allServiceCharge", totalServiceCharge);
        map.put("consumeCount", consumeCount);
        map.put("repaymentCount", creatTemporaryPlan.size());
        map.put("executeDates", executeDates);
        map.put("rate", rate);
        map.put("serviceCharge", serviceCharge);
        return map;
    }

    /**
     * 生成还款任务和消费任务,去掉小数点
     *
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param amount
     * @param reservedAmount
     * @param brandId
     * @param executeDate
     * @param version
     * @return <p>Description: </p>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/create/repayment/task1")
    public @ResponseBody
    Object createRepaymentTask1(HttpServletRequest request,
                                @RequestParam() String userId,
                                @RequestParam() String creditCardNumber,
                                @RequestParam() String amount,
                                @RequestParam() String reservedAmount,
                                @RequestParam() String brandId,
                                @RequestParam() String[] executeDate,
                                @RequestParam() String version,
                                @RequestParam(required = false, defaultValue = "0") String round,
                                @RequestParam(required = false, defaultValue = "3") int conCount
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        Map<String, Object> map = new HashMap<>();
        if ("".equals(userId) || "".equals(creditCardNumber) || "".equals(version)) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证失败,传入参数不能为空!");
            return map;
        }

        JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
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

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            return userChannelRate;
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);

        Map<String, Object> verifyDoesHaveBandCard = creditCardManagerAuthorizationHandle.verifyDoesHaveBandCard(userId, creditCardNumber, rateStr,
                serviceCharge.toString(), version, resultBankCardJSONObject);
        if (!CommonConstants.SUCCESS.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
            if (!CardConstss.TO_BAND_CARD.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CardConstss.NONSUPPORT, (String) verifyDoesHaveBandCard.get(CommonConstants.RESP_MESSAGE));
            }
            return verifyDoesHaveBandCard;
        }

        CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        if (model == null) {
            String phone = null;
            try {
                JSONObject userInfo = this.getUserInfo(userId);
                resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                phone = resultJSONObject.getString("phone");
                brandId = resultJSONObject.getString("brandId");
            } catch (RuntimeException e) {
                e.printStackTrace();
                LOG.error("", e);
                return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
            }
            model = creditCardAccountBusiness.createNewAccount(userId, creditCardNumber, version, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
        }
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        List<RepaymentTaskVO> creatTemporaryPlan = channelFactory.getChannelRoot(version).creatTemporaryPlan1(userId, creditCardNumber, amount, reservedAmount, brandId, creditCardManagerConfig, executeDate, bankName, conCount);
        creatTemporaryPlan.sort(new Comparator<RepaymentTaskVO>() {
            @Override
            public int compare(RepaymentTaskVO o1, RepaymentTaskVO o2) {
                return o1.getExecuteDate().compareTo(o2.getExecuteDate());
            }
        });
        map = ResultWrap.init(CommonConstants.SUCCESS, "请求成功", creatTemporaryPlan);
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        BigDecimal allConsumeAmount = BigDecimal.ZERO;
        int consumeCount = 0;
        int i = 0;
        Date minDate = null;
        Date maxDate = null;
        BigDecimal realRepaymentAmount = BigDecimal.ZERO;
        for (RepaymentTaskVO repaymentTaskVO : creatTemporaryPlan) {
            if (i == 0) {
                minDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            if (i == creatTemporaryPlan.size() - 1) {
                maxDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            totalServiceCharge = totalServiceCharge.add(repaymentTaskVO.getTotalServiceCharge());
            for (ConsumeTaskVO consumeTaskVO : repaymentTaskVO.getConsumeTaskVOs()) {
                allConsumeAmount = allConsumeAmount.add(consumeTaskVO.getRealAmount());
                consumeCount++;
            }
            realRepaymentAmount = realRepaymentAmount.add(repaymentTaskVO.getAmount());
            i++;
        }
        map.put("userId", userId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amount);
        map.put("reservedAmount", reservedAmount);
        map.put("brandId", brandId);
        map.put("executeDate", executeDate);
        map.put("totalServiceCharge", totalServiceCharge);
        map.put("version", version);
        map.put("rate", rate.toString());
        map.put("serviceCharge", serviceCharge);
        map.put("bankName", bankName);

        String executeDates = DateUtil.getDateStringConvert(new String(), minDate, "yyyy/MM/dd") + "-" + DateUtil.getDateStringConvert(new String(), maxDate, "yyyy/MM/dd");
        map.put("allConsumeAmount", allConsumeAmount);
        map.put("allRepaymentAmount", realRepaymentAmount);
        map.put("allServiceCharge", totalServiceCharge);
        map.put("consumeCount", consumeCount);
        map.put("repaymentCount", creatTemporaryPlan.size());
        map.put("executeDates", executeDates);
        map.put("rate", rate);
        map.put("serviceCharge", serviceCharge);
        return map;
    }


    /**
     * 生成还款任务和消费任务,极速还款，用户选择每日还款次数。一消一还.可以选择去小数点
     *
     * @param request
     * @param userId
     * @param creditCardNumber
     * @param amount
     * @param reservedAmount
     * @param brandId
     * @param executeDate
     * @param version
     * @return <p>Description: </p>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/create/repayment/quicktask")
    public @ResponseBody
    Object createRepaymentQuickTask(HttpServletRequest request,
                                    @RequestParam() String userId,
                                    @RequestParam() String creditCardNumber,
                                    @RequestParam() String amount,
                                    @RequestParam() String reservedAmount,
                                    @RequestParam() String brandId,
                                    @RequestParam() String[] executeDate,
                                    @RequestParam() String version,
                                    @RequestParam(required = false, defaultValue = "0") String round,//完美账单
                                    @RequestParam(value = "dayRepaymentCount", required = false, defaultValue = "3") String repayCount,
                                    @RequestParam(value = "conCount", required = false, defaultValue = "3") String conCount
    ) {
        userId = userId.trim();
        creditCardNumber = creditCardNumber.trim();
        Map<String, Object> map = new HashMap<>();
        if ("".equals(userId) || "".equals(creditCardNumber) || "".equals(version)) {
            map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证失败,传入参数不能为空!");
            return map;
        }

        JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
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

//		查询用户费率
        Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), version);
        if (!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))) {
            userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
            return userChannelRate;
        }

        resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
        String rateStr = resultJSONObject.getString("rate");
        String extraFeeStr = resultJSONObject.getString("extraFee");
        String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
        BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
        ;
        BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);

        Map<String, Object> verifyDoesHaveBandCard = creditCardManagerAuthorizationHandle.verifyDoesHaveBandCard(userId, creditCardNumber, rateStr, serviceCharge.toString(), version, resultBankCardJSONObject);
        if (!CommonConstants.SUCCESS.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
            if (!CardConstss.TO_BAND_CARD.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
                return ResultWrap.init(CardConstss.NONSUPPORT, (String) verifyDoesHaveBandCard.get(CommonConstants.RESP_MESSAGE));
            }
            return verifyDoesHaveBandCard;
        }

        CreditCardAccount model = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        if (model == null) {
            String phone = null;
            try {
                JSONObject userInfo = this.getUserInfo(userId);
                resultJSONObject = userInfo.getJSONObject(CommonConstants.RESULT);
                phone = resultJSONObject.getString("phone");
                brandId = resultJSONObject.getString("brandId");
            } catch (RuntimeException e) {
                e.printStackTrace();
                LOG.error("", e);
                return ResultWrap.init(CardConstss.NONSUPPORT, "获取用户信息失败!");
            }
            model = creditCardAccountBusiness.createNewAccount(userId, creditCardNumber, version, phone, billDate, repaymentDate, new BigDecimal(creditBlance), brandId);
        }
        CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
        //List<RepaymentTaskVO> creatTemporaryPlan = channelFactory.getChannelRoot(version).creatTemporaryPlan(userId, creditCardNumber, amount, reservedAmount, brandId, creditCardManagerConfig, executeDate, bankName);

//		Map<String, Object> r = channelFactory.getChannelRoot(version).creatQuickTemporaryPlan(userId, creditCardNumber, amount, reservedAmount, brandId, creditCardManagerConfig, executeDate, bankName, round, count);
//		if( !r.get(CommonConstants.RESP_CODE).equals(CommonConstants.SUCCESS)){
//			return  r;
//		}
//		List<RepaymentTaskVO> creatTemporaryPlan = (List<RepaymentTaskVO>) r.get(CommonConstants.RESULT);

        //@TODO
        //根据执行日期判定还款笔数 jayden
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
        int maxRepaymentCount = 0;
        for (String executeDate1 : executeDate) {
            executeDate1 = DateUtil.getDateStringConvert(new String(), executeDate1, "yyyy-MM-dd");
            if (todayDate.equals(executeDate1)) {
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                if (now.compareTo(calendar.getTime()) <= 0) {
                    maxRepaymentCount = maxRepaymentCount + Integer.parseInt(repayCount);
                    continue;
                }
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                if (now.compareTo(calendar.getTime()) <= 0) {
                    maxRepaymentCount = maxRepaymentCount + Integer.parseInt(repayCount) - 1;
                    continue;
                }
                calendar.set(Calendar.HOUR_OF_DAY, 16);
                if (now.compareTo(calendar.getTime()) <= 0) {
                    System.out.println("当前时间小于16点");
                    maxRepaymentCount = maxRepaymentCount + 1;
                    continue;
                }
            } else {
                maxRepaymentCount = maxRepaymentCount + Integer.parseInt(repayCount);
            }
        }
        String repaymentCount = String.valueOf(maxRepaymentCount);
        System.out.println("repaymentCount===========" + repaymentCount);
        System.out.println("executeDates=========" + Arrays.toString(executeDate));
        Map<String, Object> creatTemporaryPlan1 = channelFactory.getChannelRoot(version).creatQuickTemporaryPlan(userId, creditCardNumber, amount, reservedAmount, brandId, creditCardManagerConfig, executeDate, bankName, round, repayCount, conCount, repaymentCount);
        if (!CommonConstants.SUCCESS.equals(creatTemporaryPlan1.get(CommonConstants.RESP_CODE))) {
            Object message = creatTemporaryPlan1.get(CommonConstants.RESP_MESSAGE);
            return ResultWrap.init(CardConstss.FAIL_CODE, message.toString());
        }

        List<RepaymentTaskVO> creatTemporaryPlan = (List<RepaymentTaskVO>) creatTemporaryPlan1.get(CommonConstants.RESULT);
        creatTemporaryPlan.sort(new Comparator<RepaymentTaskVO>() {
            @Override
            public int compare(RepaymentTaskVO o1, RepaymentTaskVO o2) {
                return o1.getExecuteDate().compareTo(o2.getExecuteDate());
            }
        });
        map = ResultWrap.init(CommonConstants.SUCCESS, "请求成功", creatTemporaryPlan);
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        BigDecimal allConsumeAmount = BigDecimal.ZERO;
        int consumeCount = 0;
        int i = 0;
        Date minDate = null;
        Date maxDate = null;
        BigDecimal realRepaymentAmount = BigDecimal.ZERO;
        for (RepaymentTaskVO repaymentTaskVO : creatTemporaryPlan) {
            if (i == 0) {
                minDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            if (i == creatTemporaryPlan.size() - 1) {
                maxDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskVO.getExecuteDate(), "yyyy-MM-dd");
            }
            totalServiceCharge = totalServiceCharge.add(repaymentTaskVO.getTotalServiceCharge());
            for (ConsumeTaskVO consumeTaskVO : repaymentTaskVO.getConsumeTaskVOs()) {
                allConsumeAmount = allConsumeAmount.add(consumeTaskVO.getRealAmount());
                consumeCount++;
            }
            realRepaymentAmount = realRepaymentAmount.add(repaymentTaskVO.getAmount());
            i++;
        }
        map.put("userId", userId);
        map.put("creditCardNumber", creditCardNumber);
        map.put("amount", amount);
        map.put("reservedAmount", reservedAmount);
        map.put("brandId", brandId);
        map.put("executeDate", executeDate);
        map.put("totalServiceCharge", totalServiceCharge);
        map.put("version", version);
        map.put("rate", rate.toString());
        map.put("serviceCharge", serviceCharge);
        map.put("bankName", bankName);

        String executeDates = DateUtil.getDateStringConvert(new String(), minDate, "yyyy/MM/dd") + "-" + DateUtil.getDateStringConvert(new String(), maxDate, "yyyy/MM/dd");
        map.put("allConsumeAmount", allConsumeAmount);
        map.put("allRepaymentAmount", realRepaymentAmount);
        map.put("allServiceCharge", totalServiceCharge);
        map.put("consumeCount", consumeCount);
        map.put("repaymentCount", creatTemporaryPlan.size());
        map.put("executeDates", executeDates);
        map.put("rate", rate);
        map.put("serviceCharge", serviceCharge);
        return map;
    }


    public Map<String, Object> chooesRepaymentChannel(String userId, String creditCardNumber, String brandId, String amounts, String reservedAmounts, String[] executeDates, String bankName) {
        BigDecimal amount = new BigDecimal(amounts);
        BigDecimal reservedAmount = new BigDecimal(reservedAmounts);
        List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findByCreateOnOff(1);
        List<CreditCardManagerConfig> supportChannel = new ArrayList<>();
        Map<String, Object> noSupportReason = new HashMap<>();
        for (CreditCardManagerConfig creditCardManagerConfig : creditCardManagerConfigs) {
            if (CardConstss.CARD_VERSION_19.equals(creditCardManagerConfig.getVersion())) {
                continue;
            }

            BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
            BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();
            String noSupportBank = creditCardManagerConfig.getNoSupportBank();
            String[] noSupportBanks = null;
            if (noSupportBank != null && !"0".equals(noSupportBank)) {
                noSupportBanks = noSupportBank.split("-");
            }
            if (noSupportBanks != null && noSupportBanks.length > 0) {
                boolean noSupport = false;
                for (int i = 0; i < noSupportBanks.length; i++) {
                    if (bankName.trim().contains(noSupportBanks[i].trim())) {
                        noSupport = true;
                        break;
                    }
                }
                if (noSupport) {
                    noSupportReason.put(creditCardManagerConfig.getChannelName(), "需更换银行卡");
                    continue;
                }

            }

            if (amount.compareTo(paySingleLimitMoney) < 0) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), "需增加还款金额");
                continue;
            }

            if (reservedAmount.compareTo(paySingleLimitMoney) < 0) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), "需增加预留金额");
                continue;
            }

//			查询用户费率
            Map<String, Object> userChannelRate = getUserChannelRate(userId, brandId.trim(), creditCardManagerConfig.getVersion());
            if (!CommonConstants.SUCCESS.equals(userChannelRate.get(CommonConstants.RESP_CODE))) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), userChannelRate.get(CommonConstants.RESP_MESSAGE));
                continue;
            }
            JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
            String rateStr = resultJSONObject.getString("rate");
            String extraFeeStr = resultJSONObject.getString("extraFee");
            String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//			单笔还款手续费
            BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
            ;
//			费率
            BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
            int maxRepaymentCount = 0;
            for (String executeDate : executeDates) {
                executeDate = DateUtil.getDateStringConvert(new String(), executeDate, "yyyy-MM-dd");
                if (todayDate.equals(executeDate)) {
                    calendar.set(Calendar.HOUR_OF_DAY, 9);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (now.compareTo(calendar.getTime()) <= 0) {
                        maxRepaymentCount = maxRepaymentCount + creditCardManagerConfig.getPaySingleLimitCount();
                        continue;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, 13);
                    if (now.compareTo(calendar.getTime()) <= 0) {
                        maxRepaymentCount = maxRepaymentCount + 1;
                        continue;
                    }
//					calendar.set(Calendar.HOUR_OF_DAY, 17);
//					if (now.compareTo(calendar.getTime()) <= 0) {
//						maxRepaymentCount = maxRepaymentCount + creditCardManagerConfig.getPaySingleLimitCount() - 2;
//						continue;
//					}

                } else {
                    maxRepaymentCount = maxRepaymentCount + creditCardManagerConfig.getPaySingleLimitCount();
                }
            }

            Map<String, Object> createRepaymentAmount = channelFactory.getChannelRoot(creditCardManagerConfig.getVersion()).createRepaymentAmount(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, maxRepaymentCount, rate, serviceCharge, userId, brandId, creditCardManagerConfig.getVersion(), bankName);
            if (!CommonConstants.SUCCESS.equals(createRepaymentAmount.get(CommonConstants.RESP_CODE))) {
                noSupportReason.put(creditCardManagerConfig.getChannelName(), createRepaymentAmount.get(CommonConstants.RESP_MESSAGE));
                continue;
            }

            if (CardConstss.CARD_VERSION_10.equals(creditCardManagerConfig.getVersion()) || CardConstss.CARD_VERSION_11.equals(creditCardManagerConfig.getVersion())) {
                List<BigDecimal> amountList = (List<BigDecimal>) createRepaymentAmount.get(CommonConstants.RESULT);

                calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, 1);
                String startDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
                calendar.add(Calendar.MONTH, +1);
                calendar.add(Calendar.DATE, -1);
                String endDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
                String[] versions = new String[]{CardConstss.CARD_VERSION_10, CardConstss.CARD_VERSION_11};
                List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(creditCardNumber, 1, versions, startDate, endDate);
                if ((amountList.size() * 2 + consumeTaskPOJOs.size()) > 20) {
                    continue;
                }
            }

            supportChannel.add(creditCardManagerConfig);
        }
        System.out.println(noSupportReason);
        if (supportChannel.size() < 1) {
            Set<String> keySet = noSupportReason.keySet();
            List<String> suggest = new ArrayList<>();
            if (keySet.size() > 0) {
                for (String key : keySet) {
                    Object value = noSupportReason.get(key);
                    suggest.add(key + ":" + value);
                }
            }
            return ResultWrap.init(CommonConstants.FALIED, "无支持通道,可根据以下建议进行调整", suggest);
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", supportChannel);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/getchannel/bybrandid")
    public @ResponseBody
    Object getRepaymentChannelByBrandId(HttpServletRequest request,
                                        int brandId
    ) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        List<CreditCardManagerConfig> findAll = creditCardManagerConfigBusiness.findAll();

        for (CreditCardManagerConfig ccc : findAll) {
            String version = ccc.getVersion();
            int createOnOff = ccc.getCreateOnOff();
            if (createOnOff == 1) {
                RepaymentBrandStatus repaymentBrandStatusByBrandIdAndVersion = repaymentDetailBusiness.getRepaymentBrandStatusByBrandIdAndVersion(brandId, version);

                if (repaymentBrandStatusByBrandIdAndVersion != null) {
                    if (repaymentBrandStatusByBrandIdAndVersion.getStatus() == 1) {
                        jsonObject.put("status", "1");
                    } else {
                        jsonObject.put("status", "0");
                    }
                } else {
                    jsonObject.put("status", "1");
                }

                jsonObject.put("version", version);
                jsonObject.put("channelId", ccc.getChannelId());
                jsonObject.put("channelTag", ccc.getChannelTag());
                jsonObject.put("channelName", ccc.getChannelName());

                jsonArray.add(jsonObject);
            } else {

                continue;
            }
        }

        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonArray);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/getchannel/bybrandidandversion")
    public @ResponseBody
    Object getRepaymentChannelByBrandIdAndVersion(HttpServletRequest request,
                                                  int brandId,
                                                  String version
    ) {
        JSONObject jsonObject = new JSONObject();
        CreditCardManagerConfig findByVersion = creditCardManagerConfigBusiness.findByVersion(version);
        int createOnOff = findByVersion.getCreateOnOff();
        if (createOnOff == 1) {
            RepaymentBrandStatus repaymentBrandStatusByBrandIdAndVersion = repaymentDetailBusiness.getRepaymentBrandStatusByBrandIdAndVersion(brandId, version);

            if (repaymentBrandStatusByBrandIdAndVersion != null) {
                if (repaymentBrandStatusByBrandIdAndVersion.getStatus() == 1) {
                    jsonObject.put("status", "1");
                } else {
                    jsonObject.put("status", "0");
                }
            } else {
                jsonObject.put("status", "1");
            }

            jsonObject.put("version", version);
            jsonObject.put("channelId", findByVersion.getChannelId());
            jsonObject.put("channelTag", findByVersion.getChannelTag());
            jsonObject.put("channelName", findByVersion.getChannelName());

            return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonObject);
        } else {

            return ResultWrap.init(CommonConstants.FALIED, "已关闭!");
        }

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/controlchannel")
    public @ResponseBody
    Object controlRepaymentChannel(HttpServletRequest request,
                                   int brandId,
                                   String version,
                                   int status
    ) {

        RepaymentBrandStatus repaymentBrandStatusByBrandIdAndVersion = repaymentDetailBusiness.getRepaymentBrandStatusByBrandIdAndVersion(brandId, version);

        if (repaymentBrandStatusByBrandIdAndVersion == null) {

            RepaymentBrandStatus repaymentBrandStatus = new RepaymentBrandStatus();
            repaymentBrandStatus.setBrandId(brandId);
            repaymentBrandStatus.setVersion(version);
            repaymentBrandStatus.setStatus(status);

            repaymentDetailBusiness.createRepaymentBrandStatus(repaymentBrandStatus);

            return ResultWrap.init(CommonConstants.SUCCESS, "设置成功!");
        } else {

            repaymentBrandStatusByBrandIdAndVersion.setStatus(status);

            repaymentDetailBusiness.createRepaymentBrandStatus(repaymentBrandStatusByBrandIdAndVersion);

            return ResultWrap.init(CommonConstants.SUCCESS, "设置成功!");
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/tang")
    public @ResponseBody
    Object findAllStatus(@RequestParam("Status") String Status) {
//		String[] Statuss=Status.split(",");
        Integer code = Integer.valueOf(Status);
//		CreditCardAccount ca=new CreditCardAccount();
        Thread t = new Thread() {
            public void run() {
                int num = 0;
                int fail = 0;
                List<ConsumeTaskPOJO> ct = consumeTaskPOJOBusiness.findAllStatus4(code);
                LOG.info("共有" + ct.size() + "个数据");
                for (int i = 0; i < ct.size(); i++) {
                    ConsumeTaskPOJO consumeTaskPOJO = null;
                    CreditCardAccount creditCardAccount = null;
                    try {
                        consumeTaskPOJO = ct.get(i);
                        String CreditCardNumber = consumeTaskPOJO.getCreditCardNumber();
                        String Version = consumeTaskPOJO.getVersion();
                        creditCardAccount = creditCardAccountBusiness.getCreditCardAccount(Version, CreditCardNumber);
                    } catch (Exception e) {
                    }
                    if (creditCardAccount == null) {
                        num++;
                        try {
                            CreditCardAccount ca = new CreditCardAccount();
                            ca.setCreditCardNumber(consumeTaskPOJO.getCreditCardNumber());
                            ca.setVersion(consumeTaskPOJO.getVersion());
                            ca.setUserId(consumeTaskPOJO.getUserId());
                            ca.setBrandId(consumeTaskPOJO.getBrandId());
                            creditCardAccountBusiness.save(ca);
                        } catch (Exception e) {
                        }
                        LOG.info("这是需要修改的第" + num + "数据");
                    }
                    if (i % 50 == 0) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    fail++;
                    LOG.info("这是未修改条数" + fail);
                    continue;

                }
            }
        };
        t.start();
        return "结束！！";
    }

}
