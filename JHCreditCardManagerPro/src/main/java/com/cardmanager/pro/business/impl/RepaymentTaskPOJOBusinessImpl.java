package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO_;
import com.cardmanager.pro.repository.RepaymentTaskPOJORepository;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import org.springframework.web.client.RestTemplate;

@Service
public class RepaymentTaskPOJOBusinessImpl implements RepaymentTaskPOJOBusiness {
	@Autowired
	private RepaymentTaskPOJORepository repaymentTaskPOJORepository;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private EntityManager em;

	@Autowired
	private RestTemplate restTemplate;

	@Transactional
	@Override
	public RepaymentTaskPOJO save(RepaymentTaskPOJO repaymentTaskPOJO) {
		RepaymentTaskPOJO model = repaymentTaskPOJORepository.saveAndFlush(repaymentTaskPOJO);
		return model;
	}

	@Override
	public int queryTaskStatus0CountAndVersion(String userIdStr, String creditCardNumber, int taskStatus,String version) {
		return repaymentTaskPOJORepository.queryTaskStatusCountAndVersion(userIdStr,creditCardNumber,taskStatus,version);
	}

//	不缓存
	@Override
	public List<RepaymentTaskPOJO> findTaskTypeAndTaskStatus0RepaymentTaskAndVersion(int taskType,String version) {
		Date nowDate = null;
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, +3);
			nowDate = calendar.getTime();
		}else {
			nowDate = new Date();
		}
		String nowDateTime = DateUtil.getDateStringConvert(new String(), nowDate, "yyyy-MM-dd HH:mm:ss");
		String nowDateString = DateUtil.getDateStringConvert(new String(), nowDate, "yyyy-MM-dd");
		return repaymentTaskPOJORepository.findTaskTypeAndTaskStatusRepaymentTaskAndVersion(taskType,0,nowDateTime,nowDateString,version);
	}

	@Override
	public int queryTaskStatus0AndTaskType0CountAndVersion(String userId, String creditCardNumber, int taskStatus, int taskType,String version) {
		int queryTaskStatusAndTaskTypeCount = repaymentTaskPOJORepository.queryTaskStatusAndTaskTypeCountAndVersion(userId,creditCardNumber,taskStatus,taskType,version);
		return queryTaskStatusAndTaskTypeCount;
	}

	@Override
	public int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber, int orderStatus,int taskType,String version) {
		int queryOrderStatusAndTaskTypeCount = repaymentTaskPOJORepository.queryOrderStatusAndTaskTypeCountAndVersion(userId,creditCardNumber,orderStatus,taskType,version);
		return queryOrderStatusAndTaskTypeCount;
	}

	@Override
	@Transactional
	public RepaymentTaskPOJO[] saveArray(RepaymentTaskPOJO[] repaymentTaskPOJOs) {
		for(int i = 0;i < repaymentTaskPOJOs.length;i++){
			repaymentTaskPOJOs[i] = this.save(repaymentTaskPOJOs[i]);
		}
		return repaymentTaskPOJOs;
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByTaskStatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		return repaymentTaskPOJORepository.findByTaskStatusRepaymentTaskAndVersion(userId,creditCardNumber,0,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByTaskStatus1AndOrderStatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable) {
		return repaymentTaskPOJORepository.findByTaskStatusAndOrderStatusRepaymentTaskAndVersion(userId,creditCardNumber,1,0,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByTaskStatus1AndOrderStatus1RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable) {
		return repaymentTaskPOJORepository.findByTaskStatusAndOrderStatusRepaymentTaskAndVersion(userId,creditCardNumber,1,1,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByTaskStatus2AndOrderStatus1RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable) {
		return repaymentTaskPOJORepository.findByTaskStatusAndOrderStatusRepaymentTaskAndVersion(userId,creditCardNumber,2,1,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByTaskStatus2AndOrderstatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable) {
		return repaymentTaskPOJORepository.findByTaskStatusAndOrderStatusRepaymentTaskAndVersion(userId,creditCardNumber,2,0,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable) {
		return repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version,pageable);
	}

	@Override
	public RepaymentTaskPOJO findByRepaymentTaskId(String repaymentTaskId) {
		RepaymentTaskPOJO model = repaymentTaskPOJORepository.findByRepaymentTaskId(repaymentTaskId);
		if(model !=null){
			if (model.getCreateTime().lastIndexOf(".") != -1) {
				model.setCreateTime(model.getCreateTime().substring(0, model.getCreateTime().lastIndexOf(".")));
			}
			if (model.getExecuteDateTime().lastIndexOf(".") != -1) {
				model.setExecuteDateTime(model.getExecuteDateTime().substring(0, model.getExecuteDateTime().lastIndexOf(".")));
			}
		}
		return model;
	}

	@Override
	public RepaymentTaskPOJO findByOrderCode(String orderCode) {
		return repaymentTaskPOJORepository.findByOrderCode(orderCode);
	}

	@Transactional
	@Override
	public void updateTaskStatus4AndReturnMessageByRepaymentTaskId(String repaymentTaskId, String returnMessage) {
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJORepository.findByRepaymentTaskId(repaymentTaskId);
		if(repaymentTaskPOJO!=null){
			repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
			repaymentTaskPOJO.setTaskStatus(4);
			repaymentTaskPOJO.setReturnMessage(returnMessage);
			repaymentTaskPOJO = this.save(repaymentTaskPOJO);
		}
	}

//	不缓存
	@Transactional
	@Override
	public RepaymentTaskPOJO createNewRepaymentTaskPOJO(BigDecimal amount, String userId, String creditCardNumber,int taskType, String description,String createTime,BigDecimal serviceCharge,BigDecimal rate,BigDecimal returnServiceCharge,String channelId,String channelTag,String version,Date executeDate,String brandId) {
		RepaymentTaskPOJO repaymentTaskPOJO = new RepaymentTaskPOJO();
		Date nowTime = new Date();
		if (executeDate == null) {
			nowTime = new Date();
		}else {
			nowTime = executeDate;
		}
		Random random = new Random();
		repaymentTaskPOJO.setAmount(amount);
		repaymentTaskPOJO.setRealAmount(amount);
		repaymentTaskPOJO.setRate(rate);
		repaymentTaskPOJO.setChannelId(channelId);
		repaymentTaskPOJO.setChannelTag(channelTag);
		repaymentTaskPOJO.setVersion(version);
		repaymentTaskPOJO.setServiceCharge(serviceCharge);
		repaymentTaskPOJO.setUserId(userId);
		repaymentTaskPOJO.setBrandId(brandId);
		repaymentTaskPOJO.setReturnServiceCharge(returnServiceCharge);
		repaymentTaskPOJO.setRepaymentTaskId(DateUtil.getDateStringConvert(new String(), nowTime,"yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+"1");
		repaymentTaskPOJO.setCreditCardNumber(creditCardNumber);
		repaymentTaskPOJO.setDescription(description);
		repaymentTaskPOJO.setCreateTime(createTime);
		repaymentTaskPOJO.setExecuteDate(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd"));
		repaymentTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:ss:mm"));
		repaymentTaskPOJO.setTaskType(taskType);
		repaymentTaskPOJO = this.save(repaymentTaskPOJO);
		return repaymentTaskPOJO;
	}
	
	@Transactional
	@Override
	public RepaymentTaskPOJO createNewRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTaskPOJO) {
		String creditCardNumber = repaymentTaskPOJO.getCreditCardNumber();
		Date date = DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, +40);
		return this.createNewRepaymentTaskPOJO(repaymentTaskPOJO.getAmount(), repaymentTaskPOJO.getUserId(), creditCardNumber, 1, "尾号" + creditCardNumber.substring(creditCardNumber.length() - 4) + "|系统自动生成出款任务", repaymentTaskPOJO.getCreateTime(), repaymentTaskPOJO.getServiceCharge(), repaymentTaskPOJO.getRate(), repaymentTaskPOJO.getReturnServiceCharge(), repaymentTaskPOJO.getChannelId(), repaymentTaskPOJO.getChannelTag(), repaymentTaskPOJO.getVersion(),calendar.getTime(),repaymentTaskPOJO.getBrandId());
	}
	

	@Override
	public RepaymentTaskPOJO findByTaskType0AndTaskStatus0RepaymentTaskPOJOAndVersion(String userId, String creditCardNumber,String version) {
		return repaymentTaskPOJORepository.findByTaskTypeAndTaskStatusRepaymentTaskPOJOAndVersion(userId,creditCardNumber,0,0,version);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<Object[]> findByCreateTimeAndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		return repaymentTaskPOJORepository.findByCreateTimeAndVersion(userId,creditCardNumber,version,pageable);
	}

	@Override
	public List<RepaymentTaskPOJO> findByCreateTimeAndVersion(String userId, String creditCardNumber, String createTime,String version) {
		List<RepaymentTaskPOJO> models = repaymentTaskPOJORepository.findByCreateTimeAndVersion(userId, creditCardNumber,createTime,version);
		if(models != null && models.size() >0){
			for(RepaymentTaskPOJO model:models){
				model.setCreateTime(model.getCreateTime().substring(0,model.getCreateTime().lastIndexOf(".")==-1?model.getCreateTime().length():model.getCreateTime().lastIndexOf(".")));
				model.setExecuteDateTime(model.getExecuteDateTime().substring(0, model.getExecuteDateTime().lastIndexOf(".")==-1?model.getExecuteDateTime().length():model.getExecuteDateTime().lastIndexOf(".")));
			}
		}
		return models; 
	}

	@Override
	public int findByOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber,String version) {
		int findByOrderStatusAndTaskTypeCount = repaymentTaskPOJORepository.findByOrderStatusAndTaskTypeCountAndVersion(userId,creditCardNumber,1,0,version);
		return findByOrderStatusAndTaskTypeCount;
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<Object[]> findByCreateTimeAndTaskStatus1AndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		return repaymentTaskPOJORepository.findByCreateTimeAndTaskStatusAndVersion(userId,creditCardNumber,1,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<Object[]> findByCreateTimeAndTaskStatus2AndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		return repaymentTaskPOJORepository.findByCreateTimeAndTaskStatusAndVersion(userId,creditCardNumber,2,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<Object[]> findByCreateTimeAndExecuteDate0AndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		String date = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd");
		return repaymentTaskPOJORepository.findByCreateTimeAndExecuteDate0AndVersion(userId,creditCardNumber,date,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public List<Object[]> findByCreateTimeAndExecuteDate1AndVersion(String userId, String creditCardNumber,String version,Pageable pageable) {
		String date = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd");
		return repaymentTaskPOJORepository.findByCreateTimeAndExecuteDate1AndVersion(userId, creditCardNumber,date,version,pageable);
	}

//	不缓存
	@Override
	public List<RepaymentTaskPOJO> findByExecuteDateAndVersion(String userId, String creditCardNumber,String version) {
		return repaymentTaskPOJORepository.findByExecuteDateAndVersion(userId, creditCardNumber,version);
	}

	@Override
//	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public Page<RepaymentTaskPOJO> findByUserIdAndVersion(String userId,String version,Pageable pageable) {
		return repaymentTaskPOJORepository.findByUserIdAndVersion(userId,version,pageable);
	}

	@Override
	@Cacheable(value = "RepaymentTaskPOJO",keyGenerator="keyGenerator")
	public BigDecimal findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(String userId,String creditCardNumber,String version) {
		return repaymentTaskPOJORepository.findSumRealAmountByUserIdAndCreditCardNumberAndOrderStatusAndVersion(userId,creditCardNumber,1,version);
	}

	@Override
	public int queryTaskStatus1AndOrderStatus4CountAndVersion(String userId, String creditCardNumber, int taskStatus,int orderStatus,String version) {
		int queryTaskStatusAndOrderStatusCount = repaymentTaskPOJORepository.queryTaskStatusAndOrderStatusCountAndVersion( userId, creditCardNumber, taskStatus,orderStatus,version);
		return queryTaskStatusAndOrderStatusCount;
	}

	@Override
	public RepaymentTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version) {
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJORepository.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber,taskType,version);
		return repaymentTaskPOJO;
	}

	@Override
	public int findByUserIdAndCreditCardNumberAndCreateTimeAndOrderStatus1CountAndVersion(String userId, String creditCardNumber,
			String createTime,String version) {
		return repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndCreateTimeAndOrderStatusCountAndVersion(userId,creditCardNumber,createTime,1,version);
	}

	@Override
	public List<RepaymentTaskPOJO> findByExecuteDateAndOrderStatusAndVersion(String executeDate, int orderStatus,String version) {
		return repaymentTaskPOJORepository.findByExecuteDateAndOrderStatusAndVersion(executeDate,orderStatus,version);
	}

	@Override
	public RepaymentTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId,String creditCardNumber, int taskStatus, int taskType, int orderStatus, String version) {
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId,creditCardNumber,taskStatus,taskType,orderStatus,version);
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			return repaymentTaskPOJOs.get(0);
		}
		return null;
	}

	@Override
	public List<Object[]> findByCreateTimeAndVersion(String userId, String version, Pageable pageable) {
		return repaymentTaskPOJORepository.findByCreateTimeAndVersion(userId,version,pageable);
	}

	@Override
	public List<RepaymentTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus,String version,Pageable pageable) {
		if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			String nowTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(),"yyyy-MM-dd HH:ss:mm");
			return repaymentTaskPOJORepository.findByTaskTypeAndOrderStatusAndVersionAndExecuteDateTimeLessThan(taskType,orderStatus,version,nowTime,pageable);
		}else {
			return repaymentTaskPOJORepository.findByTaskTypeAndOrderStatusAndVersion(taskType,orderStatus,version,pageable);
		}
	}

	@Transactional
	@Override
	public void saveReapymentTaskAndConsumeTask(RepaymentTaskPOJO repaymentTaskPOJO, ConsumeTaskPOJO consumeTaskPOJO) {
		this.save(repaymentTaskPOJO);
		consumeTaskPOJOBusiness.save(consumeTaskPOJO);
	}
	
	@Transactional
	@Override
	public RepaymentTaskPOJO updateTaskStatusAndOrderStatusAndReturnMessageByRepaymentTaskId(int taskStatus,int orderStatus, String returnMessage, String repaymentTaskId) {
		RepaymentTaskPOJO repaymentTaskPOJO = this.findByRepaymentTaskId(repaymentTaskId);
		if (repaymentTaskPOJO != null && repaymentTaskPOJO.getOrderStatus().intValue() != 1) {
			repaymentTaskPOJO.setTaskStatus(taskStatus);
			repaymentTaskPOJO.setOrderStatus(orderStatus);
			repaymentTaskPOJO.setReturnMessage(returnMessage);
			repaymentTaskPOJO = this.save(repaymentTaskPOJO);
		}
		return repaymentTaskPOJO;
	}

	@Override
	public List<RepaymentTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType, int taskStatus,
			String version, String e) {
		return repaymentTaskPOJORepository.findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(taskType, taskStatus,version,e);
	}

	@Override
	public List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndCreateTime(String userId, String creditCardNumber,String createTime) {
		List<RepaymentTaskPOJO> models =  repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndCreateTimeAndTaskStatusNotOrderByExecuteDateTimeAsc(userId, creditCardNumber,createTime,7);
		if(models != null && models.size() >0){
			for(RepaymentTaskPOJO model:models){
				model.setCreateTime(model.getCreateTime().substring(0,model.getCreateTime().lastIndexOf(".")==-1?model.getCreateTime().length():model.getCreateTime().lastIndexOf(".")));
				model.setExecuteDateTime(model.getExecuteDateTime().substring(0, model.getExecuteDateTime().lastIndexOf(".")==-1?model.getExecuteDateTime().length():model.getExecuteDateTime().lastIndexOf(".")));
			}
		}
		return models; 
	}

	@Override
	public Page<Map<String,Object>> findByUserIdAndCreditCardNumberAndOrderStatusGroupByMonth(String userId,String creditCardNumber, int orderStatus, Pageable pageable) {
		List<Object[]> list = repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberGroupByMonth(userId,creditCardNumber);
		int pageSize = pageable.getPageSize();
		int pageNumber = pageable.getPageNumber();
		
		Map<String,Object> map = null;
		List<Map<String,Object>> content = new ArrayList<>();
		for (int i = pageSize*pageNumber; i < (pageNumber+1)*pageSize; i++) {
			if (i >= list.size()) {
				break;
			}else {
				Object[] objects = list.get(i);
				map = new HashMap<>();
				map.put("month", objects[0]);
				map.put("amount", objects[1]);
				content.add(map);
			}
		}
		Page<Map<String,Object>> pages = new PageImpl<Map<String,Object>>(content, pageable, list.size());
		return pages;
	}

	@Override
	@Transactional
	public void delete(RepaymentTaskPOJO repaymentTaskPOJO) {
		repaymentTaskPOJORepository.delete(repaymentTaskPOJO);
	}

	@Override
	public Page<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndOrderStatusAndExecuteDate(String userId,String creditCardNumber, int orderStatus, String month, Pageable pageable) {
		Page<RepaymentTaskPOJO> page = repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndExecuteDate(userId,creditCardNumber, month, pageable);
		List<RepaymentTaskPOJO> content = page.getContent();
		for (RepaymentTaskPOJO model : content) {
			model.setCreateTime(model.getCreateTime().substring(0,model.getCreateTime().lastIndexOf(".")));
			model.setExecuteDateTime(model.getExecuteDateTime().substring(0, model.getExecuteDateTime().lastIndexOf(".")));
		}
		return page;
	}

	@Override
	public List<Object[]> findByUserIdAndCreditCardNumberAndOrderStatusAndMonthGroupByCreateTime(String userId,String creditCardNumber, int orderStatus,String month) {
		return repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndMonthGroupByCreateTime(userId,creditCardNumber,month);
	}

	@Override
	public RepaymentTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatus(String userId,String cardNo, int taskStatus, int taskType, int orderStatus) {
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJORepository.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatus(userId, cardNo, taskStatus, taskType, orderStatus);
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			return repaymentTaskPOJOs.get(0);
		}
		return null;
	}

	@Override
	public int findByCreditCardNumberAndTaskTypeAndTaskStatus(String creditCardNumber, int taskType, int taskStatus) {
		return repaymentTaskPOJORepository.findByCreditCardNumberAndTaskTypeAndTaskStatus(creditCardNumber, taskType, taskStatus);
	}

	public static boolean isNullString(String str) {
		return str==null||"".equals(str.trim())||"null".equalsIgnoreCase(str.trim());
	}
	
	@Override
	public Map<String, Object> getTask(String startTime, String endTime, String version, String creditCardNumber,Set<String> userIds, String brandId, String createTime, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
		Root<RepaymentTaskPOJO> root = query.from(RepaymentTaskPOJO.class);
		
		List<Predicate> predicatesList = new ArrayList<>();
        //version in查询 ,in语句
        if (!isNullString(version)) {
            In<String> in = criteriaBuilder.in(root.get(RepaymentTaskPOJO_.version));
            String[] ver = version.split(",");
            for (String v : ver) {
				in.value(v);
			}
            predicatesList.add(criteriaBuilder.and(in));
        }
        
        if (userIds != null && userIds.size() > 0) {
            In<String> in = criteriaBuilder.in(root.get(RepaymentTaskPOJO_.userId));
            for (String userId : userIds) {
            	in.value(userId);
			}
            predicatesList.add(criteriaBuilder.and(in));
		}
        
        // endTime 小于等于 <= 语句
        if (!isNullString(endTime)) {
        	
            predicatesList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get(RepaymentTaskPOJO_.executeDateTime), endTime)));
        }
        //startTime 大于等于 >= 语句
        if (!isNullString(startTime)) {
            predicatesList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get(RepaymentTaskPOJO_.executeDateTime), startTime)));
        }
        
        //creditCardNumber equal 语句
        if (!isNullString(creditCardNumber)) {
        	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(RepaymentTaskPOJO_.creditCardNumber), creditCardNumber)));
		}
        
        //createTime equal 语句
        if (!isNullString(createTime)) {
        	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(RepaymentTaskPOJO_.createTime), createTime)));
        }
        
        query.where(criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()])));
        
        Expression<BigDecimal> tcExp = root.get(RepaymentTaskPOJO_.realAmount);  
        query.select(criteriaBuilder.tuple(criteriaBuilder.sum(tcExp)));
		List<Tuple> resultList = em.createQuery(query).getResultList();
		
		Page<RepaymentTaskPOJO> result = repaymentTaskPOJORepository.findAll((root1, criteriaQuery, criteriaBuilder1) -> {
			 return criteriaBuilder1.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
		Map<String,Object> resultMap = ResultWrap.init(CommonConstants.SUCCESS, "查询成功",result);
		BigDecimal realAmount = BigDecimal.ZERO;
		realAmount = (BigDecimal) resultList.get(0).get(0);
		if (realAmount == null) {
			realAmount = BigDecimal.ZERO;
		}
		resultMap.put("realAmount", realAmount);
		return resultMap;
	}

	@Override
	public int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(String creditCardNumber, int taskType,int taskStatus, String nowTime) {
		return repaymentTaskPOJORepository.findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(creditCardNumber, taskType,taskStatus, nowTime);
	}

	@Override
	public List<RepaymentTaskPOJO> findAllStatus2(String tips, String oldtime, String nowTime, String version) {

		return repaymentTaskPOJORepository.findByDescriptionLikeAndExecuteDate(tips, oldtime, nowTime, version);
	}

	@Override
	public List<RepaymentTaskPOJO> findByUserIdAndBrandId(String userId, String brandId, Pageable pageable) {
		List<RepaymentTaskPOJO> result=repaymentTaskPOJORepository.findByUserIdAndBrandId(userId,brandId,pageable);
		return result;
	}

	@Override
	public List<RepaymentTaskPOJO> findByBrandId(String brandId, Pageable pageable) {
		List<RepaymentTaskPOJO> result=repaymentTaskPOJORepository.findByBrandId(brandId,pageable);
		return result;
	}

	@Override
	public List<RepaymentTaskPOJO> findTask() {
		Specification specification = new Specification() {
			@Override
			public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(cb.like(root.get("returnMessage"), "%系统维护%"));
				Predicate[] p = new Predicate[predicates.size()];
				query.where(cb.and(predicates.toArray(p)));
				query.groupBy(root.get("creditCardNumber"));
				return query.getRestriction();
			}
		};
		return repaymentTaskPOJORepository.findAll(specification);
	}

	public List<RepaymentTaskPOJO> findByExecuteDateAndMessage(String message, String time) {
		List<RepaymentTaskPOJO> result=repaymentTaskPOJORepository.findByExecuteDateAndMessage(message,time);
		return result;
	}


}
