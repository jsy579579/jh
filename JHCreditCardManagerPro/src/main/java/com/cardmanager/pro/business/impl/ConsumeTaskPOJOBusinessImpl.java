package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.repository.ConsumeTaskPOJORepository;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.DateUtil;

@Service
public class ConsumeTaskPOJOBusinessImpl implements ConsumeTaskPOJOBusiness {
	@Autowired
	private ConsumeTaskPOJORepository consumeTaskPOJORepository;

	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private RepaymentBillBusiness repaymentBillBusiness;
	

	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public ConsumeTaskPOJO save(ConsumeTaskPOJO consumeTaskPOJO) {
		consumeTaskPOJO = consumeTaskPOJORepository.saveAndFlush(consumeTaskPOJO);
		return consumeTaskPOJO;
	}

	@Override
	public int queryTaskStatus0CountAndVersion(String userIdStr, String creditCardNumber, int taskStatus,String version) {
		return consumeTaskPOJORepository.queryTaskStatusCountAndVersion(userIdStr, creditCardNumber, taskStatus,version);
	}

	// 不缓存
	@Override
	public List<ConsumeTaskPOJO> findTaskType2AndTaskStatus0RepaymentTaskAndVersion(String version) {
		Date nowDate = new Date();
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			Calendar dalendar = Calendar.getInstance();
			dalendar.add(Calendar.HOUR_OF_DAY, +1);
			nowDate = dalendar.getTime();
		}
		String nowDateTime = DateUtil.getDateStringConvert(new String(), nowDate, "yyyy-MM-dd HH:mm:ss");
		String nowDateString = DateUtil.getDateStringConvert(new String(), nowDate, "yyyy-MM-dd");
		return consumeTaskPOJORepository.findTaskTypeAndTaskStatusRepaymentTaskAndVersion(2, 0, nowDateTime, nowDateString,version);
	}

	@Override
	public int queryTaskStatus0AndTaskType0CountAndVersion(String userId, String creditCardNumber, int taskStatus, int taskType,String version) {
		int queryTaskStatusAndTaskTypeCount = consumeTaskPOJORepository.queryTaskStatusAndTaskTypeCountAndVersion(userId, creditCardNumber, taskStatus,
				taskType,version);
		return queryTaskStatusAndTaskTypeCount;
	}

	@Override
	public int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber, int OrderStatus,int TaskType,String version) {
		int queryOrderStatusAndTaskTypeCount = consumeTaskPOJORepository.queryOrderStatusAndTaskTypeCountAndVersion(userId, creditCardNumber, OrderStatus,
				TaskType,version);
		return queryOrderStatusAndTaskTypeCount;
	}

	@Transactional
	@Override
	public List<ConsumeTaskPOJO> saveArrayList(List<ConsumeTaskPOJO> consumeTaskPOJOs) {
		for (int i = 0; i < consumeTaskPOJOs.size(); i++) {
			consumeTaskPOJOs.set(i, this.save(consumeTaskPOJOs.get(i)));
		}
		return consumeTaskPOJOs;
	}

	@Override
	public List<ConsumeTaskPOJO> findByRepaymentTaskId(String repaymentTaskId) {
		List<ConsumeTaskPOJO> models = consumeTaskPOJORepository.findByRepaymentTaskId(repaymentTaskId);
		if(models != null && models.size() > 0){
			for(ConsumeTaskPOJO model:models){
				model.setCreateTime(model.getCreateTime().substring(0, model.getCreateTime().lastIndexOf(".")));
				model.setExecuteDateTime(model.getExecuteDateTime().substring(0, model.getExecuteDateTime().lastIndexOf(".")));
			}
		}
		return models;
	}

	@Override
	public ConsumeTaskPOJO findByOrderCode(String orderCode) {
		return consumeTaskPOJORepository.findByOrderCode(orderCode);
	}

	// 不缓存
	@Transactional
	@Override
	public void updateTaskStatus4AndReturnMessageByRepaymentTaskId(String repaymentTaskId, String returnMessage) {
		List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJORepository.findByRepaymentTaskId(repaymentTaskId);
		if (consumeTaskPOJOs != null && consumeTaskPOJOs.size() > 0) {
			String consumeTaskId;
			for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
				consumeTaskId = consumeTaskPOJO.getConsumeTaskId();
				if (!"2".equals(consumeTaskId.substring(consumeTaskId.length() - 1))) {
					consumeTaskPOJO.setRealAmount(BigDecimal.ZERO);
					consumeTaskPOJO.setTaskStatus(4);
					consumeTaskPOJO.setOrderStatus(0);
					consumeTaskPOJO.setReturnMessage(returnMessage);
					consumeTaskPOJO = this.save(consumeTaskPOJO);
				}
			}
		}
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
		if (repaymentTaskPOJO != null) {
			RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getCreateTime());
			if (repaymentBill != null) {
				repaymentBill.setRepaymentedCount(repaymentBill.getRepaymentedCount()+1);
				if (repaymentBill.getRepaymentedCount() >= repaymentBill.getTaskCount()) {
					if (repaymentBill.getTaskStatus() == 2 || repaymentBill.getTaskStatus() == 3) {
						repaymentBill.setTaskStatus(3);
					}else {
						repaymentBill.setTaskStatus(1);
					}
				}
				repaymentBillBusiness.save(repaymentBill);
			}
			repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
			repaymentTaskPOJO.setTaskStatus(4);
			repaymentTaskPOJO.setReturnMessage(returnMessage);
			repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		}
	}

	@Transactional
	@Override
	public void saveArrayListTaskAll(List<ConsumeTaskPOJO> consumeTaskPOJOs, RepaymentTaskPOJO[] repaymentTaskPOJOs) {
		for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
			this.save(consumeTaskPOJO);
		}
		for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTaskPOJOs) {
			repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		}
	}

	@Override
	public int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber,String version) {
		return consumeTaskPOJORepository.queryOrderStatusAndTaskTypeCountAndVersion(userId, creditCardNumber, 1, 0,version);
	}

	@Override
	public ConsumeTaskPOJO findByTaskType0AndTaskStatus0ConsumeTaskPOJOAndVersion(String userId, String creditCardNumber,String version) {
		return consumeTaskPOJORepository.findByTaskTypeAndTaskStatusConsumeTaskPOJOAndVersion(userId, creditCardNumber, 0, 0,version);
	}

	@Transactional
	@Override
	public void deleteRepaymentTaskAndConsumeTask(RepaymentTaskPOJO repaymentTaskPOJO,
			List<ConsumeTaskPOJO> consumeTaskPOJOs) {
		for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
			consumeTaskPOJORepository.delete(consumeTaskPOJO);
		}
		repaymentTaskPOJOBusiness.delete(repaymentTaskPOJO);
	}

	@Override
	public ConsumeTaskPOJO findByConsumeTaskId(String consumeTaskId) {
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJORepository.findByConsumeTaskId(consumeTaskId);
		return consumeTaskPOJO;
	}

	@Override
	@Cacheable(value = "ConsumeTaskPOJO",keyGenerator="keyGenerator")
	public BigDecimal findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(String userId,String creditCardNumber,String version) {
		return consumeTaskPOJORepository.findSumRealAmountByUserIdAndCreditCardNumberAndOrderStatusAndVersion(userId,creditCardNumber,1,version);
	}

	@Override
	public ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(String userIdStr, String creditCardNumber,int taskType,String version) {
		ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskType = consumeTaskPOJORepository.findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(userIdStr,creditCardNumber,taskType,version);
		return findByUserIdAndCreditCardNumberAndTaskType;
	}

	@Override
	public ConsumeTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version) {
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJORepository.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber,taskType,version);
		return consumeTaskPOJO;
	}

	@Transactional
	@Override
	public ConsumeTaskPOJO createNewConsumeTaskPOJO(BigDecimal amount, String userId, String creditCardNumber, int taskType,
			String description, String createTime, BigDecimal serviceCharge, BigDecimal rate,
			BigDecimal returnServiceCharge, String channelId, String channelTag,String version) {
		ConsumeTaskPOJO consumeTaskPOJO = new ConsumeTaskPOJO();
		consumeTaskPOJO.setAmount(amount.multiply(BigDecimal.valueOf(-1)));
		consumeTaskPOJO.setRealAmount(amount.multiply(BigDecimal.valueOf(-1)));
		consumeTaskPOJO.setUserId(userId);
		consumeTaskPOJO.setCreditCardNumber(creditCardNumber);
		consumeTaskPOJO.setChannelId(channelId);
		consumeTaskPOJO.setChannelTag(channelTag);
		consumeTaskPOJO.setTaskType(taskType);
		consumeTaskPOJO.setDescription(description);
		consumeTaskPOJO.setCreateTime(createTime);
		consumeTaskPOJO.setServiceCharge(serviceCharge);
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByExecuteDateAndVersion(userId,creditCardNumber,version);
		String repaymentTaskId = "0";
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0){
			repaymentTaskId = repaymentTaskPOJOs.get(0).getRepaymentTaskId();
		}
		consumeTaskPOJO.setRepaymentTaskId(repaymentTaskId);
		consumeTaskPOJO.setConsumeTaskId(repaymentTaskId + new Random().nextInt(100));
		consumeTaskPOJO.setExecuteDate(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd"));
		consumeTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:ss:mm"));
		this.save(consumeTaskPOJO);
		return consumeTaskPOJO;
	}

	@Override
	public List<ConsumeTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus, String version) {
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			String nowTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(),"yyyy-MM-dd HH:ss:mm");
			return consumeTaskPOJORepository.findByTaskTypeAndOrderStatusAndVersionAndExecuteDateTimeLessThan(taskType,orderStatus,version,nowTime);
		}else {
			return consumeTaskPOJORepository.findByTaskTypeAndOrderStatusAndVersion(taskType,orderStatus,version);
		}
	}

	@Transactional
	@Override
	public ConsumeTaskPOJO updateTaskStatusAndOrderStatusAndReturnMessageByConsumeTaskId(int taskStatus,int orderStatus, String returnMessage, String orderCode) {
		ConsumeTaskPOJO consumeTaskPOJO = this.findByConsumeTaskId(orderCode);
		if (consumeTaskPOJO != null && consumeTaskPOJO.getOrderStatus().intValue() == 1) {
			consumeTaskPOJO.setOrderStatus(orderStatus);
		}
		consumeTaskPOJO.setTaskStatus(taskStatus);
		consumeTaskPOJO.setReturnMessage(returnMessage);
		consumeTaskPOJO = this.save(consumeTaskPOJO);
		return consumeTaskPOJO;
	}

	@Override
	public List<ConsumeTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType, int taskStatus,String version, String executeDate) {
		return consumeTaskPOJORepository.findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(taskType, taskStatus,version,executeDate);
	}

	@Override
	public ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId, String creditCardNumber, int taskStatus, int taskType, int orderStatus, String version) {
		return consumeTaskPOJORepository.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, taskStatus, taskType, orderStatus, version);
	}

	@Override
	public List<ConsumeTaskPOJO> findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(String createTime,String cardNo, String userId, String version) {
		return consumeTaskPOJORepository.findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(createTime,cardNo, userId, version);
	}

	@Override
	public int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(String creditCardNumber,int taskType, int taskStatus, String executeDateTime) {
		return consumeTaskPOJORepository.findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(creditCardNumber,taskType, taskStatus, executeDateTime);
	}

	@Override
	public List<ConsumeTaskPOJO> findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(String creditCardNumber,
			int orderStatus,String[] versions, String startDate, String endDate) {
		return consumeTaskPOJORepository.findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(creditCardNumber,orderStatus, versions,startDate, endDate);
	}

	@Override
	public List<ConsumeTaskPOJO> findAllStatus4(Integer orderStatus) {
		List<ConsumeTaskPOJO> result=consumeTaskPOJORepository.findAllStatus4(orderStatus);
		return result;
	}

	@Override
	public List<ConsumeTaskPOJO> findAllByRepayment(String repaymentTaskId) {
		List<ConsumeTaskPOJO> result=consumeTaskPOJORepository.findAllByRepayment(repaymentTaskId);
		return result;
	}

	@Override
	public List<ConsumeTaskPOJO> findByUserIdAndBrandId(String userId, String brandId, Pageable pageable) {
		List<ConsumeTaskPOJO> result=consumeTaskPOJORepository.findByUserIdAndBrandId(userId,brandId,pageable);

		return result;
	}

	//通过还款订单号找到对应的消费订单号
    @Override
    public List<ConsumeTaskPOJO> findByRepaymentTaskid(String repaymentTaskId) {
	    em.clear();
        List<ConsumeTaskPOJO> result=consumeTaskPOJORepository.findByRepaymentTaskid(repaymentTaskId);
        return result;
    }

    @Override
	public List<ConsumeTaskPOJO> findByBrandId(String brandId, Pageable pageable) {
		List<ConsumeTaskPOJO> result=consumeTaskPOJORepository.findByBrandId(brandId,pageable);
		return result;
	}

//	@Override
//	@Transactional
//	public void deleteAllByStatusAndReturnMessage(int taskStatus) {
//		consumeTaskPOJORepository.deleteAllByStatusAndReturnMessage(taskStatus);
//	}




}
