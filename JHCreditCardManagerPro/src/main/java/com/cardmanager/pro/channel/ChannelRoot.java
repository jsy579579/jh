package com.cardmanager.pro.channel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.channel.behavior.ConsumeTaskCheckor;
import com.cardmanager.pro.channel.behavior.ConsumeTaskExecutor;
import com.cardmanager.pro.channel.behavior.DefaultConsumeTaskCheckor;
import com.cardmanager.pro.channel.behavior.DefaultConsumeTaskExecutor;
import com.cardmanager.pro.channel.behavior.DefaultRepaymentTaskCheckor;
import com.cardmanager.pro.channel.behavior.DefaultRepaymentTaskExecutor;
import com.cardmanager.pro.channel.behavior.DefaultTaskBuilder;
import com.cardmanager.pro.channel.behavior.RepaymentTaskCheckor;
import com.cardmanager.pro.channel.behavior.RepaymentTaskExecutor;
import com.cardmanager.pro.channel.behavior.TaskBuilder;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskVO;
import com.cardmanager.pro.util.SpringContextUtil;

import net.sf.json.JSONObject;

@Component
public class ChannelRoot extends BaseExecutor{
	
	private TaskBuilder taskBuilder;
	
	private Class<?> consumeTaskExecutor;
	
	private Class<?> consumeTaskCheckor;

	private Class<?> repaymentTaskExecutor;
	
	private Class<?> repaymentTaskCheckor;
	
	public JSONObject postForJSON(RestTemplate restTemplate,LinkedMultiValueMap<String, String> requestEntity,String url) {
		return restTemplate.postForObject(url, requestEntity,  JSONObject.class);
	}
	
	public List<RepaymentTaskVO> creatTemporaryPlan(String userId,String creditCardNumber,String amounts,String reservedAmounts,String brandId,CreditCardManagerConfig creditCardManagerConfig,String[] executeDates,String bankName,int conCount){
		TaskBuilder taskBuilder = this.getTaskBuilder();
		return taskBuilder.creatTemporaryPlan(userId, creditCardNumber, amounts, reservedAmounts, brandId, creditCardManagerConfig, executeDates, bankName,conCount);
	}

	public List<RepaymentTaskVO> creatTemporaryPlan1(String userId,String creditCardNumber,String amounts,String reservedAmounts,String brandId,CreditCardManagerConfig creditCardManagerConfig,String[] executeDates,String bankName,int conCount){
		TaskBuilder taskBuilder = this.getTaskBuilder();
		return taskBuilder.creatTemporaryPlan1(userId, creditCardNumber, amounts, reservedAmounts, brandId, creditCardManagerConfig, executeDates, bankName,conCount);
	}
	//round 是否去小数点，count每日还款笔数
	public Map<String,Object> creatQuickTemporaryPlan(String userId, String creditCardNumber, String amounts, String reservedAmounts, String brandId, CreditCardManagerConfig creditCardManagerConfig, String[] executeDates, String bankName, String round, String count,String conCount,String repaymentCount) {
		TaskBuilder taskBuilder = this.getTaskBuilder();
		return taskBuilder.creatQuickTemporaryPlan(userId, creditCardNumber, amounts, reservedAmounts, brandId, creditCardManagerConfig, executeDates, bankName, round, count,conCount,repaymentCount);
	}

	public Map<String, Object> createRepaymentAmount(BigDecimal amount, BigDecimal reservedAmount,BigDecimal paySingleLimitMoney, BigDecimal paySingleMaxMoney, int maxRepaymentCount, BigDecimal rate,BigDecimal serviceCharge, String userId, String brandId, String version, String bankName){
		TaskBuilder taskBuilder = this.getTaskBuilder();
		return taskBuilder.createRepaymentAmount(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, maxRepaymentCount, rate, serviceCharge, userId, brandId, version, bankName);
	}
	
	public void executeConsumeTask(ConsumeTaskPOJO consumeTaskPOJO) {
		this.getConsumeTaskExecuter().executeTask(consumeTaskPOJO);
	}
	
	public void checkConsumeTask(ConsumeTaskPOJO consumeTaskPOJO) {
		this.getConsumeTaskCheckor().checkTask(consumeTaskPOJO);
	}
	
	public void executeRepaymentTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		this.getRepaymentTaskExecuter().executeTask(repaymentTaskPOJO);
	}
	
	public void checkRepaymentTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		this.getRepaymentTaskCheckor().checkTask(repaymentTaskPOJO);
	}
	
	public void setTaskBuilder(TaskBuilder taskBuilder) {
		this.taskBuilder = taskBuilder;
	}
	
	public TaskBuilder getTaskBuilder() {
		if (taskBuilder == null) {
			return SpringContextUtil.getBeanOfClass(DefaultTaskBuilder.class);
		}
		return taskBuilder;
	}

	public ConsumeTaskExecutor getConsumeTaskExecuter() {
		if (consumeTaskExecutor == null) {
			consumeTaskExecutor = DefaultConsumeTaskExecutor.class;
		}
		return (ConsumeTaskExecutor) SpringContextUtil.getBeanOfClass(consumeTaskExecutor);
	}
	
	public ConsumeTaskCheckor getConsumeTaskCheckor() {
		if (consumeTaskCheckor == null) {
			consumeTaskCheckor = DefaultConsumeTaskCheckor.class;
		}
		return (ConsumeTaskCheckor) SpringContextUtil.getBeanOfClass(consumeTaskCheckor);
	}

	public RepaymentTaskExecutor getRepaymentTaskExecuter() {
		if (repaymentTaskExecutor == null) {
			repaymentTaskExecutor = DefaultRepaymentTaskExecutor.class;
		}
		return (RepaymentTaskExecutor) SpringContextUtil.getBeanOfClass(repaymentTaskExecutor);
	}
	
	public RepaymentTaskCheckor getRepaymentTaskCheckor() {
		if (repaymentTaskCheckor == null) {
			repaymentTaskCheckor = DefaultRepaymentTaskCheckor.class;
		}
		return (RepaymentTaskCheckor) SpringContextUtil.getBeanOfClass(repaymentTaskCheckor);
	}

	public void setConsumeTaskExecutor(Class<?> consumeTaskExecutor) {
		this.consumeTaskExecutor = consumeTaskExecutor;
	}

	public void setConsumeTaskCheckor(Class<?> consumeTaskCheckor) {
		this.consumeTaskCheckor = consumeTaskCheckor;
	}

	public void setRepaymentTaskExecutor(Class<?> repaymentTaskExecutor) {
		this.repaymentTaskExecutor = repaymentTaskExecutor;
	}

	public void setRepaymentTaskCheckor(Class<?> repaymentTaskCheckor) {
		this.repaymentTaskCheckor = repaymentTaskCheckor;
	}


	public Map<String, Object> createRepaymentAmountQuick(BigDecimal amount, BigDecimal reservedAmount,BigDecimal paySingleLimitMoney, BigDecimal paySingleMaxMoney, int maxRepaymentCount, BigDecimal rate,BigDecimal serviceCharge, String userId, String brandId, String version, String bankName){
		TaskBuilder taskBuilder = this.getTaskBuilder();
		return taskBuilder.createRepaymentAmountQuick(amount, reservedAmount, paySingleLimitMoney, paySingleMaxMoney, maxRepaymentCount, rate, serviceCharge, userId, brandId, version, bankName);
	}
}
	

	