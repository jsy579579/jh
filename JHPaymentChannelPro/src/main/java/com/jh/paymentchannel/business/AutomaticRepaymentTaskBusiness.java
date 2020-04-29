package com.jh.paymentchannel.business;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.jh.paymentchannel.pojo.AutomaticRepaymentTask;

public interface AutomaticRepaymentTaskBusiness {
//	添加一条记录
	AutomaticRepaymentTask addAutomaticRepaymentTask(AutomaticRepaymentTask model);
//	根据orderCode查找一条记录
	AutomaticRepaymentTask findByOrderCode(String orderCode);
//	更新一条记录的status
	AutomaticRepaymentTask updateType(AutomaticRepaymentTask model);
//	根据executionTime查询一条记录
	List<AutomaticRepaymentTask> findByExecutionTime(String start,String end,String type) throws ParseException;
//	根据userId和status=0查询
	List<AutomaticRepaymentTask> findByUserIdAndStatusA(int userId,String bindId);
//	根据userId和status!=0查询
	List<AutomaticRepaymentTask> findByUserIdAndStatusB(int userId,String bindId);
//	只根据userId查询
	List<AutomaticRepaymentTask> findByUserIdAndStatusC(int userId,String bindId);
//	只根据userId/bankNo查询
	List<AutomaticRepaymentTask> findByUserIdAndStatusD(int userId,String bankNo);
//	只根据userId/bankNo查询
	List<AutomaticRepaymentTask> findByUserIdAndStatusE(int userId,String bankNo);
//	根据卡号查询查询记录
	List<AutomaticRepaymentTask> findByBindId(String bindId);
	void delete(AutomaticRepaymentTask model);
//	根据payCard、time和Type还有status查询多条记录
	List<AutomaticRepaymentTask> findByPayCardAndStatus(String start,String end,String type,String payCard);
	
	List<AutomaticRepaymentTask> findByUserId(int userId);
	
	String SumAmount(String start,String end);
	//通过batchNo查询automaticRepaymentTask
	List<AutomaticRepaymentTask> queryAutomaticByBatchNo(String batchNo);


}
