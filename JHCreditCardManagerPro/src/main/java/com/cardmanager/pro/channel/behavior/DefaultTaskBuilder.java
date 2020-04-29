package com.cardmanager.pro.channel.behavior;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.pojo.ConsumeTaskVO;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskVO;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class DefaultTaskBuilder extends ChannelRoot implements TaskBuilder {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Override
	public List<RepaymentTaskVO> creatTemporaryPlan(String userId, String creditCardNumber, String amounts,String reservedAmounts, String brandId, CreditCardManagerConfig creditCardManagerConfig,String[] executeDates, String bankName,int conCount) {
//		还款金额
		BigDecimal amount = new BigDecimal(amounts);
//		预留金额
		BigDecimal reservedAmount = new BigDecimal(reservedAmounts);
		
		String version = creditCardManagerConfig.getVersion();
		String channelId = creditCardManagerConfig.getChannelId();
		String channelTag = creditCardManagerConfig.getChannelTag();
		BigDecimal conSingleLimitMoney = creditCardManagerConfig.getConSingleLimitMoney();
		BigDecimal conSingleMaxMoney = creditCardManagerConfig.getConSingleMaxMoney();
		int conSingleLimitCount = creditCardManagerConfig.getConSingleLimitCount();
		BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
		int paySingleLimitCount = creditCardManagerConfig.getPaySingleLimitCount();
		BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();
		
		Date[] executeDate = new Date[executeDates.length];
		for(int i= 0;i < executeDates.length;i++){
			executeDate[i] = DateUtil.getDateStringConvert(new Date(), executeDates[i],"yyyy-MM-dd");
		}
		
//		查询用户费率
		Map<String, Object> userChannelRate = getUserChannelRate(userId,brandId.trim(),version);
		JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = resultJSONObject.getString("rate");
		String extraFeeStr = resultJSONObject.getString("extraFee");
		String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//		单笔还款手续费
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);;
//		费率
		BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
		Map<String, Object> createRepaymentAmount = this.createRepaymentAmount(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, executeDates.length*paySingleLimitCount,rate,serviceCharge, userId, brandId, version,bankName);
//		System.out.println(createRepaymentAmount);
		if (!CommonConstants.SUCCESS.equals(createRepaymentAmount.get(CommonConstants.RESP_CODE))) {
			return null;
		}
		List<BigDecimal> singleAmounts = (List<BigDecimal>) createRepaymentAmount.get(CommonConstants.RESULT);
//		System.out.println(singleAmounts);
//		进行随机排序
		Collections.sort(singleAmounts,new Comparator<BigDecimal>() {
			@Override
			public int compare(BigDecimal o1, BigDecimal o2) {
				return new Random().nextInt()-new Random().nextInt();
			}
		});
//		System.out.println(singleAmounts);
		List<RepaymentTaskVO> repaymentTaskVOs = new ArrayList<>();
		int amountIndex = 0;
		Date now = new Date();
		String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
		out:
		for (Date executeTime : executeDate) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(executeTime);
			
			String exeDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
			int repaymentCount = paySingleLimitCount;
			if (todayDate.equals(exeDate)) {
				calendar.set(Calendar.HOUR_OF_DAY, 17);
				calendar.set(Calendar.MINUTE, 0);
				if (now.compareTo(calendar.getTime()) <= 0) {
					calendar.set(Calendar.HOUR_OF_DAY, 12);
					if (now.compareTo(calendar.getTime()) <= 0) {
						calendar.set(Calendar.HOUR_OF_DAY, 9);
						if (now.compareTo(calendar.getTime()) <= 0) {
							calendar.set(Calendar.HOUR_OF_DAY, 9);
						}else {
							calendar.set(Calendar.HOUR_OF_DAY, 12);
							if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
								repaymentCount = 1;
							}else {
								repaymentCount = 2;
							}
						}
					}else {
						calendar.set(Calendar.HOUR_OF_DAY, 17);
						repaymentCount = 1;
					}
				}else {
					continue;
				}
				
			}else {
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 0);
			}
			String startExecuteDateTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
			for (int i = 0; i < repaymentCount; i++) {
				if (amountIndex == singleAmounts.size()) {
					break out;
				}
				int count = conSingleLimitCount;
				if (!CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version) && singleAmounts.get(amountIndex).compareTo(conSingleMaxMoney) < 0) {
					count = 1+new Random().nextInt(conSingleLimitCount);
				}
				
				if (i > 0) {
					Date date = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
					startExecuteDateTime = DateUtil.getDateStringConvert(new String(), new Date(date.getTime()+180*60*1000), "yyyy-MM-dd HH:mm:ss");
				}
				if(conCount==1||conCount==2){
					count=conCount;
				}
				RepaymentTaskVO repaymentTaskVO = createRepaymentTaskVO(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version);
				repaymentTaskVOs.add(repaymentTaskVO);
				startExecuteDateTime = repaymentTaskVO.getExecuteDateTime();
				amountIndex++;
//				System.out.println(repaymentTaskVO);
			}
		}
		return repaymentTaskVOs;
	}


	@Override
	public List<RepaymentTaskVO> creatTemporaryPlan1(String userId, String creditCardNumber, String amounts,String reservedAmounts, String brandId, CreditCardManagerConfig creditCardManagerConfig,String[] executeDates, String bankName,int conCount) {
//		还款金额
		BigDecimal amount = new BigDecimal(amounts);
//		预留金额
		BigDecimal reservedAmount = new BigDecimal(reservedAmounts);

		String version = creditCardManagerConfig.getVersion();
		String channelId = creditCardManagerConfig.getChannelId();
		String channelTag = creditCardManagerConfig.getChannelTag();
		BigDecimal conSingleLimitMoney = creditCardManagerConfig.getConSingleLimitMoney();
		BigDecimal conSingleMaxMoney = creditCardManagerConfig.getConSingleMaxMoney();
		int conSingleLimitCount = creditCardManagerConfig.getConSingleLimitCount();
		BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
		int paySingleLimitCount = creditCardManagerConfig.getPaySingleLimitCount();
		BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();

		Date[] executeDate = new Date[executeDates.length];
		for(int i= 0;i < executeDates.length;i++){
			executeDate[i] = DateUtil.getDateStringConvert(new Date(), executeDates[i],"yyyy-MM-dd");
		}

//		查询用户费率
		Map<String, Object> userChannelRate = getUserChannelRate(userId,brandId.trim(),version);
		JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = resultJSONObject.getString("rate");
		String extraFeeStr = resultJSONObject.getString("extraFee");
		String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//		单笔还款手续费
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);;
//		费率
		BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
		Map<String, Object> createRepaymentAmount = this.createRepaymentAmount(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, executeDates.length*paySingleLimitCount,rate,serviceCharge, userId, brandId, version,bankName);
//		System.out.println(createRepaymentAmount);
		if (!CommonConstants.SUCCESS.equals(createRepaymentAmount.get(CommonConstants.RESP_CODE))) {
			return null;
		}
		List<BigDecimal> singleAmounts = (List<BigDecimal>) createRepaymentAmount.get(CommonConstants.RESULT);
//		System.out.println(singleAmounts);
//		进行随机排序
		Collections.sort(singleAmounts,new Comparator<BigDecimal>() {
			@Override
			public int compare(BigDecimal o1, BigDecimal o2) {
				return new Random().nextInt()-new Random().nextInt();
			}
		});
//		System.out.println(singleAmounts);
		List<RepaymentTaskVO> repaymentTaskVOs = new ArrayList<>();
		int amountIndex = 0;
		Date now = new Date();
		String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
		out:
		for (Date executeTime : executeDate) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(executeTime);

			String exeDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
			int repaymentCount = paySingleLimitCount;
			if (todayDate.equals(exeDate)) {
				calendar.set(Calendar.HOUR_OF_DAY, 17);
				calendar.set(Calendar.MINUTE, 0);
				if (now.compareTo(calendar.getTime()) <= 0) {
					calendar.set(Calendar.HOUR_OF_DAY, 12);
					if (now.compareTo(calendar.getTime()) <= 0) {
						calendar.set(Calendar.HOUR_OF_DAY, 9);
						if (now.compareTo(calendar.getTime()) <= 0) {
							calendar.set(Calendar.HOUR_OF_DAY, 9);
						}else {
							calendar.set(Calendar.HOUR_OF_DAY, 12);
							repaymentCount = 2;
						}
					}else {
						calendar.set(Calendar.HOUR_OF_DAY, 17);
						repaymentCount = 1;
					}
				}else {
					continue;
				}

			}else {
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 0);
			}
			String startExecuteDateTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
			for (int i = 0; i < repaymentCount; i++) {
				if (amountIndex == singleAmounts.size()) {
					break out;
				}
				int count = conSingleLimitCount;
				if (!CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version) && singleAmounts.get(amountIndex).compareTo(conSingleMaxMoney) < 0) {
					count = 1+new Random().nextInt(conSingleLimitCount);
				}

				if (i > 0) {
					Date date = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
					startExecuteDateTime = DateUtil.getDateStringConvert(new String(), new Date(date.getTime()+180*60*1000), "yyyy-MM-dd HH:mm:ss");
				}
				if(conCount==1||conCount==2){
					count=conCount;
				}
				//RepaymentTaskVO repaymentTaskVO = createRepaymentTaskVO(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version);
				RepaymentTaskVO repaymentTaskVO = createRepaymentTaskVO1(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version);
				repaymentTaskVOs.add(repaymentTaskVO);
				startExecuteDateTime = repaymentTaskVO.getExecuteDateTime();
				amountIndex++;
//				System.out.println(repaymentTaskVO);
			}
		}
		//完美账单模式（消费金额去小数点，还款金额增加小数点）
		return this.prefectBill(repaymentTaskVOs);
	}

	private List prefectBill(List<RepaymentTaskVO> repaymentTaskVOs) {
		LOG.info("进入完美账单模式，调整消费及还款金额===============");
		for(RepaymentTaskVO repaymentTaskVO:repaymentTaskVOs){
			BigDecimal repaymentTaskVOAmount=repaymentTaskVO.getAmount();
			List<ConsumeTaskVO> consumeTaskVOs=repaymentTaskVO.getConsumeTaskVOs();
			if(consumeTaskVOs.size()==1){//一消一还
				//第一笔消费金额增加还款金额的万1，并向上取整，防止还款金额不够
				BigDecimal newConsumeAmount=consumeTaskVOs.get(0).getRealAmount().add(repaymentTaskVOAmount.multiply(new BigDecimal("0.0001"))).setScale(0,BigDecimal.ROUND_UP);
				consumeTaskVOs.get(0).setRealAmount(newConsumeAmount);
				BigDecimal newTotalServiceCharge=newConsumeAmount.multiply(repaymentTaskVO.getRate()).add(repaymentTaskVO.getServiceCharge()).setScale(2,BigDecimal.ROUND_UP).add(new BigDecimal("0.01"));
				BigDecimal newRepaymentAmount=newConsumeAmount.subtract(newTotalServiceCharge);
				repaymentTaskVO.setTotalServiceCharge(newTotalServiceCharge);
				consumeTaskVOs.get(0).setServiceCharge(newTotalServiceCharge);
				repaymentTaskVO.setAmount(newRepaymentAmount);
				consumeTaskVOs.get(0).setAmount(newRepaymentAmount);
			}else{//两消一还
				ConsumeTaskVO consumeTask1=null;
				ConsumeTaskVO consumeTask2=null;
				for(ConsumeTaskVO consumeTaskVO:consumeTaskVOs){
					String consumeTaskId=consumeTaskVO.getConsumeTaskId();
					if("2".equals(consumeTaskId.substring(consumeTaskId.length()-1))){
						consumeTask1=consumeTaskVO;
					}else{
						consumeTask2=consumeTaskVO;
					}
				}
				//第一笔消费金额增加还款金额的万1，并向上取整，防止还款金额不够
				BigDecimal newConsumeAmount=consumeTask1.getRealAmount().add(repaymentTaskVOAmount.multiply(new BigDecimal("0.0001"))).setScale(0,BigDecimal.ROUND_UP);
				consumeTask1.setRealAmount(newConsumeAmount);
				BigDecimal sumConsumeAmount=consumeTask1.getRealAmount().add(consumeTask2.getRealAmount());
				BigDecimal newTotalServiceCharge=sumConsumeAmount.multiply(repaymentTaskVO.getRate()).add(repaymentTaskVO.getServiceCharge()).setScale(2,BigDecimal.ROUND_UP).add(new BigDecimal("0.01"));
				repaymentTaskVO.setTotalServiceCharge(newTotalServiceCharge);
				consumeTask1.setServiceCharge(newTotalServiceCharge);
				repaymentTaskVO.setAmount(sumConsumeAmount.subtract(newTotalServiceCharge));
				consumeTask1.setAmount(repaymentTaskVO.getAmount().subtract(consumeTask2.getAmount()));
			}
		}
		return repaymentTaskVOs;
	}


	@Override
	public Map<String, Object> creatQuickTemporaryPlan(String userId, String creditCardNumber, String amounts,String reservedAmounts, String brandId, CreditCardManagerConfig creditCardManagerConfig,String[] executeDates, String bankName, String round, String oneDayCount,String conCount,String TotalRepaymentCount) {
//		还款金额
		BigDecimal amount = new BigDecimal(amounts);
//		预留金额
		BigDecimal reservedAmount = new BigDecimal(reservedAmounts);

		String version = creditCardManagerConfig.getVersion();
		String channelId = creditCardManagerConfig.getChannelId();
		String channelTag = creditCardManagerConfig.getChannelTag();
		BigDecimal conSingleLimitMoney = creditCardManagerConfig.getConSingleLimitMoney();
		BigDecimal conSingleMaxMoney = creditCardManagerConfig.getConSingleMaxMoney();
		int conSingleLimitCount = creditCardManagerConfig.getConSingleLimitCount();
		int consuCount=Integer.valueOf(conCount); //用户选择消费笔数
		BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
		//int paySingleLimitCount = creditCardManagerConfig.getPaySingleLimitCount();
		int paySingleLimitCount = Integer.valueOf(oneDayCount);      //用户选择一天还几次
		BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();

		//还款笔数
		int repayCount=Integer.valueOf(TotalRepaymentCount);
		Date[] executeDate = new Date[executeDates.length];
		for(int i= 0;i < executeDates.length;i++){
			executeDate[i] = DateUtil.getDateStringConvert(new Date(), executeDates[i],"yyyy-MM-dd");
		}

//		查询用户费率
		Map<String, Object> userChannelRate = getUserChannelRate(userId,brandId.trim(),version);
		JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = resultJSONObject.getString("rate");
		String extraFeeStr = resultJSONObject.getString("extraFee");
		String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
//		单笔还款手续费
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);;
//		费率
		BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
//		Map<String, Object> createRepaymentAmount = this.createRepaymentAmountQuick(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, executeDates.length*paySingleLimitCount,rate,serviceCharge, userId, brandId, version,bankName);
		Map<String, Object> createRepaymentAmount = this.createRepaymentAmountQuick(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, repayCount,rate,serviceCharge, userId, brandId, version,bankName);
//		System.out.println(createRepaymentAmount);
		if (!CommonConstants.SUCCESS.equals(createRepaymentAmount.get(CommonConstants.RESP_CODE))) {
			return createRepaymentAmount;
		}
		List<BigDecimal> singleAmounts = (List<BigDecimal>) createRepaymentAmount.get(CommonConstants.RESULT);
		System.out.println(singleAmounts);
//		进行随机排序
		Collections.sort(singleAmounts,new Comparator<BigDecimal>() {
			@Override
			public int compare(BigDecimal o1, BigDecimal o2) {
				return new Random().nextInt()-new Random().nextInt();
			}
		});
//		System.out.println(singleAmounts);
		List<RepaymentTaskVO> repaymentTaskVOs = new ArrayList<>();
		int amountIndex = 0;
		Date now = new Date();
		String todayDate = DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd");
		out:
		for (Date executeTime : executeDate) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(executeTime);

			String exeDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
			int repaymentCount = paySingleLimitCount;
			if (todayDate.equals(exeDate)) {
				calendar.set(Calendar.HOUR_OF_DAY, 16);
				calendar.set(Calendar.MINUTE, 0);
				if (now.compareTo(calendar.getTime()) <= 0) {
					calendar.set(Calendar.HOUR_OF_DAY, 12);
					if (now.compareTo(calendar.getTime()) <= 0) {
						calendar.set(Calendar.HOUR_OF_DAY, 9);
						if (now.compareTo(calendar.getTime()) <= 0) {
							calendar.set(Calendar.HOUR_OF_DAY, 9);
						}else {
							calendar.set(Calendar.HOUR_OF_DAY, 12);
						}
					}else {
						calendar.set(Calendar.HOUR_OF_DAY, 16);
						repaymentCount = 1;
					}
				}else {
					continue;
				}

			}else {
				calendar.set(Calendar.HOUR_OF_DAY, 9);
				calendar.set(Calendar.MINUTE, 0);
			}
			String startExecuteDateTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
			for (int i = 0; i < repaymentCount; i++) {
				if (amountIndex == singleAmounts.size()) {
					break out;
				}
				int count = conSingleLimitCount;


				if (i > 0) {
					Date date = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
					startExecuteDateTime = DateUtil.getDateStringConvert(new String(), new Date(date.getTime()+120*60*1000), "yyyy-MM-dd HH:mm:ss");
				}
				if(consuCount==1||consuCount==2){
					count=consuCount;
				}
				//RepaymentTaskVO repaymentTaskVO = createRepaymentTaskVO(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version);
				//RepaymentTaskVO repaymentTaskVO = createRepaymentTaskVO1(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version);
				RepaymentTaskVO repaymentTaskVO = createQuickRepaymentTaskVO(count, conSingleLimitMoney, conSingleMaxMoney, singleAmounts.get(amountIndex), userId, creditCardNumber, rate, serviceCharge, channelTag, channelId, now, startExecuteDateTime, version, round);
				//判定消费单笔限额是否超限
				List<ConsumeTaskVO> consumeTaskVOS=repaymentTaskVO.getConsumeTaskVOs();
				for(ConsumeTaskVO consumeTaskVO:consumeTaskVOS){
					if(consumeTaskVO.getRealAmount().compareTo(conSingleMaxMoney)>0){
						return ResultWrap.init(CommonConstants.FALIED, "单笔消费金额超限，请增加日还款次数或消费次数");
					}
				}
				repaymentTaskVOs.add(repaymentTaskVO);
				startExecuteDateTime = repaymentTaskVO.getExecuteDateTime();
				amountIndex++;
//				System.out.println(repaymentTaskVO);
			}
		}
		if("1".equals(round)){
			repaymentTaskVOs=this.prefectBill(repaymentTaskVOs);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "成功", repaymentTaskVOs);
	}



	@Override
	public Map<String,Object> createRepaymentAmount(BigDecimal amount,BigDecimal reservedAmount,BigDecimal paySingleLimitMoney,BigDecimal paySingleMaxMoney,int maxRepaymentCount,BigDecimal rate,BigDecimal serviceCharge,String userId,String brandId,String version,String bankName){
		int maxCount = amount.divide(paySingleLimitMoney,0,BigDecimal.ROUND_DOWN).intValue();
		int minCount = amount.divide(paySingleMaxMoney, 0, BigDecimal.ROUND_UP).intValue();

		BigDecimal residueReservedAmount = BigDecimal.ZERO;

		if (reservedAmount.compareTo(paySingleMaxMoney) > 0) {
			residueReservedAmount = reservedAmount.subtract(paySingleMaxMoney);
			reservedAmount = paySingleMaxMoney;
		}
		BigDecimal rateCharge = rate.multiply(amount).setScale(2,BigDecimal.ROUND_HALF_UP);
		int count = amount.divide(reservedAmount.subtract(rateCharge).subtract(reservedAmount.multiply(BigDecimal.valueOf(0.05))), 0, BigDecimal.ROUND_UP).intValue() + 1;
		if (count > maxCount) {
			count = maxCount;
		}
		if (count < minCount) {
			count = minCount;
		}

		if (maxRepaymentCount < count) {
			return ResultWrap.init(CommonConstants.FALIED, "需增加还款天数或预留金额");
		}

		BigDecimal avgAmount = null;
		int randomAmountLimit = 0;
		avgAmount = amount.divide(BigDecimal.valueOf(count),0,BigDecimal.ROUND_UP);
		if (avgAmount.compareTo(paySingleLimitMoney) < 0) {
			avgAmount = paySingleLimitMoney;
		}
//		System.out.println("count:"+count);
//		System.out.println("avgAmount:"+avgAmount);
		BigDecimal reservedAmount2 = reservedAmount.subtract(rateCharge);
		reservedAmount2 = reservedAmount2.subtract(serviceCharge.multiply(BigDecimal.valueOf(count)).add(BigDecimal.valueOf(5))).setScale(0, BigDecimal.ROUND_DOWN);
//		System.out.println("reservedAmount2:"+reservedAmount2);
		if (reservedAmount2.compareTo(paySingleLimitMoney) < 0) {
			return ResultWrap.init(CommonConstants.FALIED, "需增加预留金额");
		}
		reservedAmount2 = reservedAmount2.add(residueReservedAmount);
		if (reservedAmount2.compareTo(paySingleMaxMoney) > 0) {
			reservedAmount2 = paySingleMaxMoney;
		}

		randomAmountLimit = reservedAmount2.subtract(avgAmount).intValue();
//		System.out.println("randomAmountLimit:"+randomAmountLimit);


		int count2 = count/2;
//		System.out.println("count2:" + count2);
		List<BigDecimal> singleAmounts = new ArrayList<>();
		BigDecimal realTotalAmount = BigDecimal.ZERO;
		BigDecimal maxSingleAmount = BigDecimal.ZERO;
		int maxSingleAmountIndex = 0;
		for (int i = 0; i < count2; i++) {
			if (avgAmount.compareTo(BigDecimal.valueOf(randomAmountLimit)) < 0) {
				randomAmountLimit = avgAmount.subtract(paySingleLimitMoney).intValue();
			}

			if (avgAmount.subtract(paySingleLimitMoney).compareTo(BigDecimal.valueOf(randomAmountLimit)) < 0) {
				randomAmountLimit = avgAmount.subtract(paySingleLimitMoney).intValue();
			}
			BigDecimal singleAmount1;
			if (randomAmountLimit <= 0) {
				singleAmount1 = avgAmount;
			}else {
				singleAmount1 = avgAmount.add(BigDecimal.valueOf(new Random().nextInt(randomAmountLimit)));
			}
			BigDecimal singleAmount2 = avgAmount.multiply(BigDecimal.valueOf(2)).subtract(singleAmount1);

			singleAmounts.add(singleAmount1);
			singleAmounts.add(singleAmount2);
			realTotalAmount = realTotalAmount.add(singleAmount1).add(singleAmount2);
			if (maxSingleAmount.compareTo(singleAmount1) < 0) {
				maxSingleAmount = singleAmount1;
				maxSingleAmountIndex = 2*i;
			}

			if (maxSingleAmount.compareTo(singleAmount2) < 0) {
				maxSingleAmount = singleAmount2;
				maxSingleAmountIndex = 2*i+1;
			}
		}
//		System.out.println(singleAmounts);
//		System.out.println("realTotalAmount:"+realTotalAmount);
//		System.out.println("maxSingleAmountIndex:"+maxSingleAmountIndex);
//		System.out.println("maxSingleAmount:"+maxSingleAmount);
		if (amount.compareTo(realTotalAmount) >0) {
			singleAmounts.add(amount.subtract(realTotalAmount));
		}else {
			singleAmounts.set(maxSingleAmountIndex, maxSingleAmount.subtract(realTotalAmount.subtract(amount)));
		}

		for (BigDecimal singleAmount : singleAmounts) {
			if (singleAmount.compareTo(paySingleLimitMoney) < 0 || singleAmount.compareTo(reservedAmount2) > 0) {
				return ResultWrap.init(CommonConstants.FALIED, "需增加预留金额");
			}
			if (singleAmount.compareTo(paySingleMaxMoney) > 0) {
				return ResultWrap.init(CommonConstants.FALIED, "需减少还款金额");
			}

			if (CardConstss.CARD_VERSION_15.equals(version)) {
				if (bankName != null && bankName.contains("工商") && singleAmount.compareTo(BigDecimal.valueOf(4900)) > 0) {
					return ResultWrap.init(CommonConstants.FALIED, "需减少还款金额");
				}
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "生成成功",singleAmounts);
	}

	@Override
	public Map<String,Object> createRepaymentAmountQuick(BigDecimal amount,BigDecimal reservedAmount,BigDecimal paySingleLimitMoney,BigDecimal paySingleMaxMoney,int maxRepaymentCount,BigDecimal rate,BigDecimal serviceCharge,String userId,String brandId,String version,String bankName){
		int maxCount = amount.divide(paySingleLimitMoney,0,BigDecimal.ROUND_DOWN).intValue();
		int minCount = amount.divide(paySingleMaxMoney, 0, BigDecimal.ROUND_UP).intValue();

		BigDecimal residueReservedAmount = BigDecimal.ZERO;

		if (reservedAmount.compareTo(paySingleMaxMoney) > 0) {
			residueReservedAmount = reservedAmount.subtract(paySingleMaxMoney);
			reservedAmount = paySingleMaxMoney;
		}
		BigDecimal rateCharge = rate.multiply(amount).setScale(2,BigDecimal.ROUND_HALF_UP);
//		int count = amount.divide(reservedAmount.subtract(rateCharge).subtract(reservedAmount.multiply(BigDecimal.valueOf(0.05))), 0, BigDecimal.ROUND_UP).intValue() + 1;
//		if (count > maxCount) {
//			count = maxCount;
//		}
//		if (count < minCount) {
//			count = minCount;
//		}
//
//		if (maxRepaymentCount < count) {
//			return ResultWrap.init(CommonConstants.FALIED, "需增加还款天数或预留金额");
//		}
		int count=maxRepaymentCount;
		BigDecimal avgAmount = null;
		int randomAmountLimit = 0;
		avgAmount = amount.divide(BigDecimal.valueOf(count),0,BigDecimal.ROUND_UP);
		if (avgAmount.compareTo(paySingleLimitMoney) < 0) {
			avgAmount = paySingleLimitMoney;
		}
//		System.out.println("count:"+count);
//		System.out.println("avgAmount:"+avgAmount);
		BigDecimal reservedAmount2 = reservedAmount.subtract(rateCharge);
		reservedAmount2 = reservedAmount2.subtract(serviceCharge.multiply(BigDecimal.valueOf(count)).add(BigDecimal.valueOf(5))).setScale(0, BigDecimal.ROUND_DOWN);
//		System.out.println("reservedAmount2:"+reservedAmount2);
		if (reservedAmount2.compareTo(paySingleLimitMoney) < 0) {
			return ResultWrap.init(CommonConstants.FALIED, "需增加预留金额");
		}
		reservedAmount2 = reservedAmount2.add(residueReservedAmount);
		if (reservedAmount2.compareTo(paySingleMaxMoney) > 0) {
			reservedAmount2 = paySingleMaxMoney;
		}

		randomAmountLimit = reservedAmount2.subtract(avgAmount).intValue();
//		System.out.println("randomAmountLimit:"+randomAmountLimit);


		int count2 = count/2;
//		System.out.println("count2:" + count2);
		List<BigDecimal> singleAmounts = new ArrayList<>();
		BigDecimal realTotalAmount = BigDecimal.ZERO;
		BigDecimal maxSingleAmount = BigDecimal.ZERO;
		int maxSingleAmountIndex = 0;
		for (int i = 0; i < count2; i++) {
			if (avgAmount.compareTo(BigDecimal.valueOf(randomAmountLimit)) < 0) {
				randomAmountLimit = avgAmount.subtract(paySingleLimitMoney).intValue();
			}

			if (avgAmount.subtract(paySingleLimitMoney).compareTo(BigDecimal.valueOf(randomAmountLimit)) < 0) {
				randomAmountLimit = avgAmount.subtract(paySingleLimitMoney).intValue();
			}
			BigDecimal singleAmount1;
			if (randomAmountLimit <= 0) {
				singleAmount1 = avgAmount;
			}else {
				singleAmount1 = avgAmount.add(BigDecimal.valueOf(new Random().nextInt(randomAmountLimit)));
			}
			BigDecimal singleAmount2 = avgAmount.multiply(BigDecimal.valueOf(2)).subtract(singleAmount1);

			singleAmounts.add(singleAmount1);
			singleAmounts.add(singleAmount2);
			realTotalAmount = realTotalAmount.add(singleAmount1).add(singleAmount2);
			if (maxSingleAmount.compareTo(singleAmount1) < 0) {
				maxSingleAmount = singleAmount1;
				maxSingleAmountIndex = 2*i;
			}

			if (maxSingleAmount.compareTo(singleAmount2) < 0) {
				maxSingleAmount = singleAmount2;
				maxSingleAmountIndex = 2*i+1;
			}
		}
//		System.out.println(singleAmounts);
//		System.out.println("realTotalAmount:"+realTotalAmount);
//		System.out.println("maxSingleAmountIndex:"+maxSingleAmountIndex);
//		System.out.println("maxSingleAmount:"+maxSingleAmount);
		if (amount.compareTo(realTotalAmount) >0) {
			singleAmounts.add(amount.subtract(realTotalAmount));
		}else {
			singleAmounts.set(maxSingleAmountIndex, maxSingleAmount.subtract(realTotalAmount.subtract(amount)));
		}

		for (BigDecimal singleAmount : singleAmounts) {
			if (singleAmount.compareTo(paySingleLimitMoney) < 0 || singleAmount.compareTo(reservedAmount2) > 0) {
				return ResultWrap.init(CommonConstants.FALIED, "需增加预留金额");
			}
			if (singleAmount.compareTo(paySingleMaxMoney) > 0) {
				return ResultWrap.init(CommonConstants.FALIED, "需减少还款金额");
			}

			if (CardConstss.CARD_VERSION_15.equals(version)) {
				if (bankName != null && bankName.contains("工商") && singleAmount.compareTo(BigDecimal.valueOf(4900)) > 0) {
					return ResultWrap.init(CommonConstants.FALIED, "需减少还款金额");
				}
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "生成成功",singleAmounts);
	}
	
	private RepaymentTaskVO createRepaymentTaskVO(int consumeCount,BigDecimal minConsumeAmount,BigDecimal maxConsumeAmount,BigDecimal repaymentAmount,String userId,String creditCardNumber,BigDecimal rate,BigDecimal serviceCharge,String channelTag,String channelId,Date now,String startExecuteDateTime,String version) {
		RepaymentTaskVO repaymentTaskVO = new RepaymentTaskVO();
		repaymentTaskVO.setAmount(repaymentAmount);
		repaymentTaskVO.setUserId(userId);
		repaymentTaskVO.setCreditCardNumber(creditCardNumber);
		repaymentTaskVO.setChannelId(channelId);
		repaymentTaskVO.setChannelTag(channelTag);
		repaymentTaskVO.setRate(rate);
		repaymentTaskVO.setServiceCharge(serviceCharge);
		repaymentTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		
		String executeDate = DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss") , "yyyy-MM-dd");
		Date startTime = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
		long minRandomTime = 30 * 60 * 1000;
		int randomTime = 30 * 60 * 1000;
		List<String> consumeExcuteDateTimes = new ArrayList<>();
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			minRandomTime = 40 * 60 * 1000;
			randomTime = 20 * 60 * 1000;
		}
		
		String consumeExcuteDateTime = null;
		for (int i = 0; i < consumeCount+1; i++) {
			consumeExcuteDateTime = DateUtil.getDateStringConvert(new String(),new Date(startTime.getTime()+minRandomTime+(new Random().nextInt(randomTime))),"yyyy-MM-dd HH:mm:ss");
			startTime = DateUtil.getDateStringConvert(new Date(),consumeExcuteDateTime , "yyyy-MM-dd HH:mm:ss");
			consumeExcuteDateTimes.add(consumeExcuteDateTime);
		}
		
		BigDecimal totalServiceCharge = BigDecimal.ZERO;
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
		}else if (consumeCount == 1) {
			totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
		}else {
			totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
		}
		repaymentTaskVO.setTotalServiceCharge(totalServiceCharge);
		
		repaymentTaskVO.setDescription("还款计划");
		repaymentTaskVO.setExecuteDateTime(consumeExcuteDateTime);
		repaymentTaskVO.setExecuteDate(executeDate);
		repaymentTaskVO.setRepaymentTaskId(repaymentTaskVO.getExecuteDate().replace("-", "")+DateUtil.getDateStringConvert(new String(), new Date(),"HHSSS")+ new Random().nextInt(9)+ new Random().nextInt(9)+new Random().nextInt(9)+new Random().nextInt(9)+"1");
		List<String> consumeTypeName = new ArrayList<>();
		consumeTypeName.add("|娱乐");
		consumeTypeName.add("|购物");
		consumeTypeName.add("|其他");
		consumeTypeName.add("|通信");
		consumeTypeName.add("|交通");
		consumeTypeName.add("|住宿");
		consumeTypeName.add("|餐饮");
		
		BigDecimal avgConsumeAmount = minConsumeAmount;
		
		BigDecimal residueAmount = repaymentAmount.subtract(avgConsumeAmount.multiply(BigDecimal.valueOf(consumeCount)));
		List<BigDecimal> consumeAmounts = new ArrayList<>();
		for (int i = 0; i < consumeCount; i++) {
			BigDecimal consumeAmount = null;
			BigDecimal randomAmount;
			if (residueAmount.intValue() <= 0) {
				randomAmount = BigDecimal.ZERO;
			}else {
				randomAmount = BigDecimal.valueOf(new Random().nextInt(residueAmount.intValue()));
			}
			if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
				consumeAmount = avgConsumeAmount.add(randomAmount);
			}else {
				consumeAmount = avgConsumeAmount;
			}
			
			
			if (i == consumeCount-1) {
				consumeAmount = avgConsumeAmount.add(residueAmount);
				randomAmount = residueAmount;
			}
			
			if (consumeAmount.compareTo(maxConsumeAmount.subtract(totalServiceCharge)) > 0) {
				int random = new Random().nextInt(10);
				consumeAmount = maxConsumeAmount.subtract(totalServiceCharge).setScale(0, BigDecimal.ROUND_DOWN).subtract(BigDecimal.valueOf(random));
				randomAmount = consumeAmount.subtract(avgConsumeAmount);
			}
			
			residueAmount = residueAmount.subtract(randomAmount);
			consumeAmounts.add(consumeAmount);
//			System.out.println("residueAmount:"+residueAmount);
		}
		
		if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
			consumeAmounts.set(0, consumeAmounts.get(0).add(residueAmount));
		}
		
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (BigDecimal bigDecimal : consumeAmounts) {
//			System.out.println(bigDecimal);
			totalAmount = totalAmount.add(bigDecimal);
		}
//		System.out.println("totalAmount:"+totalAmount);
		for (int i = 0; i < consumeCount; i++) {
			repaymentTaskVO.getConsumeTaskVOs().add(new ConsumeTaskVO());
			ConsumeTaskVO consumeTaskVO = repaymentTaskVO.getConsumeTaskVOs().get(i);
			consumeTaskVO.setUserId(userId);
			consumeTaskVO.setCreditCardNumber(creditCardNumber);
			consumeTaskVO.setChannelId(channelId);
			consumeTaskVO.setChannelTag(channelTag);
			// 设置消费任务
			consumeTaskVO.setDescription("消费计划");
			// 设置消费类型
			int randomInt = new Random().nextInt(consumeTypeName.size());
			consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
			consumeTypeName.remove(randomInt);
			consumeTaskVO.setAmount(consumeAmounts.get(i));
			if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
				BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
				consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge).add(serviceCharge.divide(BigDecimal.valueOf(2),2,BigDecimal.ROUND_HALF_UP)));
				consumeTaskVO.setServiceCharge(consumeServiceCharge);
			}else if(CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
				BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().add(i==0?repaymentTaskVO.getServiceCharge():BigDecimal.ZERO).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(consumeTaskVO.getAmount());
				consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge));
				consumeTaskVO.setServiceCharge(consumeServiceCharge);
			}else {
				if (i == 0) {
					consumeTaskVO.setRealAmount(consumeAmounts.get(i).add(totalServiceCharge));
					consumeTaskVO.setServiceCharge(totalServiceCharge);
				}else {
					consumeTaskVO.setRealAmount(consumeAmounts.get(i));
				}
			}
			consumeTaskVO.setRepaymentTaskId(repaymentTaskVO.getRepaymentTaskId());
			consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVO.getRepaymentTaskId())+(i+1)+"");
			consumeTaskVO.setExecuteDateTime(consumeExcuteDateTimes.get(i));
			consumeTaskVO.setExecuteDate(executeDate);
			consumeTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		}
//		System.out.println(consumeExcuteDateTimes);
		return repaymentTaskVO;
	}


	private RepaymentTaskVO createRepaymentTaskVO1(int consumeCount,BigDecimal minConsumeAmount,BigDecimal maxConsumeAmount,BigDecimal repaymentAmount,String userId,String creditCardNumber,BigDecimal rate,BigDecimal serviceCharge,String channelTag,String channelId,Date now,String startExecuteDateTime,String version) {
		RepaymentTaskVO repaymentTaskVO = new RepaymentTaskVO();
		repaymentTaskVO.setAmount(repaymentAmount);
		repaymentTaskVO.setUserId(userId);
		repaymentTaskVO.setCreditCardNumber(creditCardNumber);
		repaymentTaskVO.setChannelId(channelId);
		repaymentTaskVO.setChannelTag(channelTag);
		repaymentTaskVO.setRate(rate);
		repaymentTaskVO.setServiceCharge(serviceCharge);
		repaymentTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));

		String executeDate = DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss") , "yyyy-MM-dd");
		Date startTime = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
		long minRandomTime = 30 * 60 * 1000;
		int randomTime = 30 * 60 * 1000;
		List<String> consumeExcuteDateTimes = new ArrayList<>();
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			minRandomTime = 40 * 60 * 1000;
			randomTime = 20 * 60 * 1000;
		}

		String consumeExcuteDateTime = null;
		for (int i = 0; i < consumeCount+1; i++) {
			consumeExcuteDateTime = DateUtil.getDateStringConvert(new String(),new Date(startTime.getTime()+minRandomTime+(new Random().nextInt(randomTime))),"yyyy-MM-dd HH:mm:ss");
			startTime = DateUtil.getDateStringConvert(new Date(),consumeExcuteDateTime , "yyyy-MM-dd HH:mm:ss");
			consumeExcuteDateTimes.add(consumeExcuteDateTime);
		}

		BigDecimal totalServiceCharge = BigDecimal.ZERO;
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			totalServiceCharge = repaymentAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).add(serviceCharge);
		}else if (consumeCount == 1) {
			totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
		}else {
			totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			//totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
		}
		repaymentTaskVO.setTotalServiceCharge(totalServiceCharge);

		repaymentTaskVO.setDescription("还款计划");
		repaymentTaskVO.setExecuteDateTime(consumeExcuteDateTime);
		repaymentTaskVO.setExecuteDate(executeDate);
		repaymentTaskVO.setRepaymentTaskId(repaymentTaskVO.getExecuteDate().replace("-", "")+DateUtil.getDateStringConvert(new String(), new Date(),"HHSSS")+ new Random().nextInt(9)+ new Random().nextInt(9)+new Random().nextInt(9)+new Random().nextInt(9)+"1");
		List<String> consumeTypeName = new ArrayList<>();
		consumeTypeName.add("|娱乐");
		consumeTypeName.add("|购物");
		consumeTypeName.add("|其他");
		consumeTypeName.add("|通信");
		consumeTypeName.add("|交通");
		consumeTypeName.add("|住宿");
		consumeTypeName.add("|餐饮");

		BigDecimal avgConsumeAmount = minConsumeAmount;

		BigDecimal residueAmount = repaymentAmount.subtract(avgConsumeAmount.multiply(BigDecimal.valueOf(consumeCount)));
		List<BigDecimal> consumeAmounts = new ArrayList<>();
		for (int i = 0; i < consumeCount; i++) {
			BigDecimal consumeAmount = null;
			BigDecimal randomAmount;
			if (residueAmount.intValue() <= 0) {
				randomAmount = BigDecimal.ZERO;
			}else {
				randomAmount = BigDecimal.valueOf(new Random().nextInt(residueAmount.intValue()));
			}
			if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
				consumeAmount = avgConsumeAmount.add(randomAmount);
			}else {
				consumeAmount = avgConsumeAmount;
			}


			if (i == consumeCount-1) {
				consumeAmount = avgConsumeAmount.add(residueAmount);
				randomAmount = residueAmount;
			}

			if (consumeAmount.compareTo(maxConsumeAmount.subtract(totalServiceCharge)) > 0) {
				int random = new Random().nextInt(10);
				consumeAmount = maxConsumeAmount.subtract(totalServiceCharge).setScale(0, BigDecimal.ROUND_DOWN).subtract(BigDecimal.valueOf(random));
				randomAmount = consumeAmount.subtract(avgConsumeAmount);
			}

			residueAmount = residueAmount.subtract(randomAmount);
			consumeAmounts.add(consumeAmount);
//			System.out.println("residueAmount:"+residueAmount);
		}

		if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
			consumeAmounts.set(0, consumeAmounts.get(0).add(residueAmount));
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (BigDecimal bigDecimal : consumeAmounts) {
//			System.out.println(bigDecimal);
			totalAmount = totalAmount.add(bigDecimal);
		}
//		System.out.println("totalAmount:"+totalAmount);
		for (int i = 0; i < consumeCount; i++) {
			repaymentTaskVO.getConsumeTaskVOs().add(new ConsumeTaskVO());
			ConsumeTaskVO consumeTaskVO = repaymentTaskVO.getConsumeTaskVOs().get(i);
			consumeTaskVO.setUserId(userId);
			consumeTaskVO.setCreditCardNumber(creditCardNumber);
			consumeTaskVO.setChannelId(channelId);
			consumeTaskVO.setChannelTag(channelTag);
			// 设置消费任务
			consumeTaskVO.setDescription("消费计划");
			// 设置消费类型
			int randomInt = new Random().nextInt(consumeTypeName.size());
			consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
			consumeTypeName.remove(randomInt);
			consumeTaskVO.setAmount(consumeAmounts.get(i));
			if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
				BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
				consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge).add(serviceCharge.divide(BigDecimal.valueOf(2),2,BigDecimal.ROUND_HALF_UP)));
				consumeTaskVO.setServiceCharge(consumeServiceCharge);
			}else if(CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
				BigDecimal consumeServiceCharge = consumeTaskVO.getAmount().add(i==0?repaymentTaskVO.getServiceCharge():BigDecimal.ZERO).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(consumeTaskVO.getAmount());
				consumeTaskVO.setRealAmount(consumeTaskVO.getAmount().add(consumeServiceCharge));
				consumeTaskVO.setServiceCharge(consumeServiceCharge);
			}else {
				if (i == 0) {
					consumeTaskVO.setRealAmount(consumeAmounts.get(i).add(totalServiceCharge));
					consumeTaskVO.setServiceCharge(totalServiceCharge);
				}else {
					consumeTaskVO.setRealAmount(consumeAmounts.get(i));
				}
			}
			consumeTaskVO.setRepaymentTaskId(repaymentTaskVO.getRepaymentTaskId());
			consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVO.getRepaymentTaskId())+(i+1)+"");
			consumeTaskVO.setExecuteDateTime(consumeExcuteDateTimes.get(i));
			consumeTaskVO.setExecuteDate(executeDate);
			consumeTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		}
//		System.out.println(consumeExcuteDateTimes);
		return repaymentTaskVO;
	}

	private RepaymentTaskVO createQuickRepaymentTaskVO(int consumeCount,BigDecimal minConsumeAmount,BigDecimal maxConsumeAmount,BigDecimal repaymentAmount,String userId,String creditCardNumber,BigDecimal rate,BigDecimal serviceCharge,String channelTag,String channelId,Date now,String startExecuteDateTime,String version, String rount) {
		RepaymentTaskVO repaymentTaskVO = new RepaymentTaskVO();
		repaymentTaskVO.setAmount(repaymentAmount);
		repaymentTaskVO.setUserId(userId);
		repaymentTaskVO.setCreditCardNumber(creditCardNumber);
		repaymentTaskVO.setChannelId(channelId);
		repaymentTaskVO.setChannelTag(channelTag);
		repaymentTaskVO.setRate(rate);
		repaymentTaskVO.setServiceCharge(serviceCharge);
		repaymentTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));

		String executeDate = DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss") , "yyyy-MM-dd");
		Date startTime = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
		long minRandomTime = 30 * 60 * 1000;
		int randomTime = 30 * 60 * 1000;
		List<String> consumeExcuteDateTimes = new ArrayList<>();
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			minRandomTime = 40 * 60 * 1000;
			randomTime = 20 * 60 * 1000;
		}

		String consumeExcuteDateTime = null;
		for (int i = 0; i < consumeCount+1; i++) {
			consumeExcuteDateTime = DateUtil.getDateStringConvert(new String(),new Date(startTime.getTime()+minRandomTime+(new Random().nextInt(randomTime))),"yyyy-MM-dd HH:mm:ss");
			startTime = DateUtil.getDateStringConvert(new Date(),consumeExcuteDateTime , "yyyy-MM-dd HH:mm:ss");
			consumeExcuteDateTimes.add(consumeExcuteDateTime);
		}

		BigDecimal totalServiceCharge = BigDecimal.ZERO;
		if (consumeCount == 1) {
			if(rount.equals("1")){
				totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			}else{
				totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			}
		}else {
			if(rount.equals("1")){
				totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			}else{
				totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
				totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
			}
			//totalServiceCharge = repaymentAmount.add(serviceCharge).setScale(2, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate),0,BigDecimal.ROUND_UP).subtract(repaymentAmount);
			//totalServiceCharge = totalServiceCharge.add(BigDecimal.valueOf(0.01));
		}
		repaymentTaskVO.setTotalServiceCharge(totalServiceCharge);

		repaymentTaskVO.setDescription("还款计划");
		repaymentTaskVO.setExecuteDateTime(consumeExcuteDateTime);
		repaymentTaskVO.setExecuteDate(executeDate);
		repaymentTaskVO.setRepaymentTaskId(repaymentTaskVO.getExecuteDate().replace("-", "")+DateUtil.getDateStringConvert(new String(), new Date(),"HHSSS")+ new Random().nextInt(9)+ new Random().nextInt(9)+new Random().nextInt(9)+new Random().nextInt(9)+"1");
		List<String> consumeTypeName = new ArrayList<>();
		consumeTypeName.add("|娱乐");
		consumeTypeName.add("|购物");
		consumeTypeName.add("|其他");
		consumeTypeName.add("|通信");
		consumeTypeName.add("|交通");
		consumeTypeName.add("|住宿");
		consumeTypeName.add("|餐饮");

		BigDecimal avgConsumeAmount = minConsumeAmount;

		BigDecimal residueAmount = repaymentAmount.subtract(avgConsumeAmount.multiply(BigDecimal.valueOf(consumeCount)));
		List<BigDecimal> consumeAmounts = new ArrayList<>();
		for (int i = 0; i < consumeCount; i++) {
			BigDecimal consumeAmount = null;
			BigDecimal randomAmount;
			if (residueAmount.intValue() <= 0) {
				randomAmount = BigDecimal.ZERO;
			}else {
				randomAmount = BigDecimal.valueOf(new Random().nextInt(residueAmount.intValue()));
			}
			if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
				consumeAmount = avgConsumeAmount.add(randomAmount);
			}else {
				consumeAmount = avgConsumeAmount;
			}


			if (i == consumeCount-1) {
				consumeAmount = avgConsumeAmount.add(residueAmount);
				randomAmount = residueAmount;
			}

			if (consumeAmount.compareTo(maxConsumeAmount.subtract(totalServiceCharge)) > 0) {
				int random = new Random().nextInt(10);
				consumeAmount = maxConsumeAmount.subtract(totalServiceCharge).setScale(0, BigDecimal.ROUND_DOWN).subtract(BigDecimal.valueOf(random));
				randomAmount = consumeAmount.subtract(avgConsumeAmount);
			}

			residueAmount = residueAmount.subtract(randomAmount);
			consumeAmounts.add(consumeAmount);
//			System.out.println("residueAmount:"+residueAmount);
		}

		if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
			consumeAmounts.set(0, consumeAmounts.get(0).add(residueAmount));
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (BigDecimal bigDecimal : consumeAmounts) {
//			System.out.println(bigDecimal);
			totalAmount = totalAmount.add(bigDecimal);
		}
//		System.out.println("totalAmount:"+totalAmount);
		for (int i = 0; i < consumeCount; i++) {
			repaymentTaskVO.getConsumeTaskVOs().add(new ConsumeTaskVO());
			ConsumeTaskVO consumeTaskVO = repaymentTaskVO.getConsumeTaskVOs().get(i);
			consumeTaskVO.setUserId(userId);
			consumeTaskVO.setCreditCardNumber(creditCardNumber);
			consumeTaskVO.setChannelId(channelId);
			consumeTaskVO.setChannelTag(channelTag);
			// 设置消费任务
			consumeTaskVO.setDescription("消费计划");
			// 设置消费类型
			int randomInt = new Random().nextInt(consumeTypeName.size());
			consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
			consumeTypeName.remove(randomInt);
			consumeTaskVO.setAmount(consumeAmounts.get(i));
			if (i == 0) {
				consumeTaskVO.setRealAmount(consumeAmounts.get(i).add(totalServiceCharge));
				consumeTaskVO.setServiceCharge(totalServiceCharge);
			}else {
				consumeTaskVO.setRealAmount(consumeAmounts.get(i));
			}

			consumeTaskVO.setRepaymentTaskId(repaymentTaskVO.getRepaymentTaskId());
			consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVO.getRepaymentTaskId())+(i+1)+"");
			consumeTaskVO.setExecuteDateTime(consumeExcuteDateTimes.get(i));
			consumeTaskVO.setExecuteDate(executeDate);
			consumeTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		}
//		System.out.println(consumeExcuteDateTimes);
		return repaymentTaskVO;
	}

}
