package com.cardmanager.pro.channel.behavior;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskVO;

public interface TaskBuilder {

	public List<RepaymentTaskVO> creatTemporaryPlan(String userId,String creditCardNumber,String amounts,String reservedAmounts,String brandId,CreditCardManagerConfig creditCardManagerConfig,String[] executeDates,String bankName,int conCount);

	public List<RepaymentTaskVO> creatTemporaryPlan1(String userId,String creditCardNumber,String amounts,String reservedAmounts,String brandId,CreditCardManagerConfig creditCardManagerConfig,String[] executeDates,String bankName,int conCount);

	public Map<String, Object> createRepaymentAmount(BigDecimal amount, BigDecimal reservedAmount,BigDecimal paySingleLimitMoney, BigDecimal paySingleMaxMoney, int maxRepaymentCount, BigDecimal rate,BigDecimal serviceCharge, String userId, String brandId, String version, String bankName);

    Map<String, Object> creatQuickTemporaryPlan(String userId, String creditCardNumber, String amounts, String reservedAmounts, String brandId, CreditCardManagerConfig creditCardManagerConfig, String[] executeDates, String bankName, String round, String count,String conCount,String repaymentCount);

	Map<String, Object> createRepaymentAmountQuick(BigDecimal amount, BigDecimal reservedAmount, BigDecimal paySingleLimitMoney, BigDecimal paySingleMaxMoney, int maxRepaymentCount, BigDecimal rate, BigDecimal serviceCharge, String userId, String brandId, String version, String bankName);
}
