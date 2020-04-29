package com.jh.paymentchannel.business.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.pojo.AutomaticRepaymentTask;
import com.jh.paymentchannel.repository.AutomaticRepaymentTaskRepository;
@Service
public class AutomaticRepaymentTaskBusinessImpl implements com.jh.paymentchannel.business.AutomaticRepaymentTaskBusiness {

	@Autowired
	AutomaticRepaymentTaskRepository automaticRepaymentTaskRepository;
	@Autowired
	private EntityManager em;
	
	@Override
	@Transactional
	public AutomaticRepaymentTask addAutomaticRepaymentTask(AutomaticRepaymentTask model) {
		AutomaticRepaymentTask art=automaticRepaymentTaskRepository.save(model);
		em.flush();
		em.clear();
		return art;
	}

	@Override
	public AutomaticRepaymentTask findByOrderCode(String orderCode) {
		return automaticRepaymentTaskRepository.findByOrderCode(orderCode);
	}

	@Override
	@Transactional
	public AutomaticRepaymentTask updateType(AutomaticRepaymentTask model) {
		AutomaticRepaymentTask art = automaticRepaymentTaskRepository.save(model);
		em.flush();
		em.clear();
		return art;
	}

	@Override
	public List<AutomaticRepaymentTask> findByExecutionTime(String start,String end,String type) throws ParseException {
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = sdf.parse(start);
		Date date2 = sdf.parse(end);*/
		return automaticRepaymentTaskRepository.findByExecutionTime(start,end,type);
	}

	@Override
	public List<AutomaticRepaymentTask> findByUserIdAndStatusA(int userId,String bindId) {
		return automaticRepaymentTaskRepository.findByUserIdAndStatusA(userId,bindId);
	}
	
	@Override
	public List<AutomaticRepaymentTask> findByUserIdAndStatusB(int userId,String bindId) {
		return automaticRepaymentTaskRepository.findByUserIdAndStatusB(userId,bindId);
	}

	@Override
	public List<AutomaticRepaymentTask> findByUserIdAndStatusC(int userId,String bindId) {
		return automaticRepaymentTaskRepository.findByUserIdAndStatusC(userId,bindId);
	}
	
	@Override
	public List<AutomaticRepaymentTask> findByUserIdAndStatusD(int userId,String bankNo) {
		return automaticRepaymentTaskRepository.findByUserIdAndStatusD(userId,bankNo);
	}
	
	@Override
	public List<AutomaticRepaymentTask> findByUserIdAndStatusE(int userId,String bankNo) {
		return automaticRepaymentTaskRepository.findByUserIdAndStatusE(userId,bankNo);
	}

	@Override
	public List<AutomaticRepaymentTask> findByBindId(String bindId) {
		return automaticRepaymentTaskRepository.findByBindId(bindId);
	}

	@Override
	@Transactional
	public void delete(AutomaticRepaymentTask model) {
		automaticRepaymentTaskRepository.delete(model);
		em.flush();
		em.clear();
	}

	@Override
	public List<AutomaticRepaymentTask> findByPayCardAndStatus(String start,String end,String type,String payCard) {
		return automaticRepaymentTaskRepository.findByPayCardAndStatus( start,end, type,payCard);
	}

	@Override
	public List<AutomaticRepaymentTask> findByUserId(int userId) {
		// TODO Auto-generated method stub
		return automaticRepaymentTaskRepository.findByUserId(userId);
	}

	@Override
	public List<AutomaticRepaymentTask> queryAutomaticByBatchNo(String batchNo) {
		List<AutomaticRepaymentTask> task = new ArrayList<AutomaticRepaymentTask>();
		 task = automaticRepaymentTaskRepository.findAutomaticByBatchNo(batchNo);
		return task;
	}

	@Override
	public String SumAmount(String start, String end) {
		// TODO Auto-generated method stub
		return automaticRepaymentTaskRepository.SumAmount(start, end);
	}

}
