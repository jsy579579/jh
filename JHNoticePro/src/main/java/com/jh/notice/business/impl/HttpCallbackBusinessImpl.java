package com.jh.notice.business.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.notice.business.HttpCallbackBusiness;
import com.jh.notice.pojo.HttpNotify;
import com.jh.notice.pojo.HttpNotifyInterval;
import com.jh.notice.repository.HttpNotifyIntervalRepository;
import com.jh.notice.repository.HttpNotifyRepository;
import com.jh.notice.util.NoticeConstants;

@Service
public class HttpCallbackBusinessImpl implements HttpCallbackBusiness{

	
	private static final Logger LOG = LoggerFactory.getLogger(HttpCallbackBusinessImpl.class);
	
	@Autowired
    private HttpNotifyRepository httpNotifyRepository;
	
	
	@Autowired
	private HttpNotifyIntervalRepository httpNotifyIntervalRepository;
	
	@Override
	public List<HttpNotify> findNoCallHttpNotify() {
		return httpNotifyRepository.findHttpNotifys(new Date());
	}

	@Override
	@Transactional
	public HttpNotify mergeNotify(HttpNotify notify) {
		
		return httpNotifyRepository.save(notify);
	}

	@Override
	public HttpNotifyInterval getNextInterval(int index) {
		return httpNotifyIntervalRepository.findHttpNotifyInterval(index);
	}
	
	@Override
	public void callBack() {
		
		 List<HttpNotify> httpNotifys =  findNoCallHttpNotify();
		 for(HttpNotify notify  : httpNotifys){
			 LOG.info("....................."+notify.getNotifyURL());
			 callBacking(notify);
		 }
		
		
	}
	
	@Async("myAsync")
	@Transactional
	public void callBacking(HttpNotify notify){
		
		Calendar cal = Calendar.getInstance();
		String responseCode = HttpPostConnection.post(notify.getNotifyURL(), notify.getParams());
		
		LOG.info("responseCode ....................."+responseCode);
		
		if(responseCode != null && !responseCode.equalsIgnoreCase(NoticeConstants.SUCCESS_CALL)){

			/**更新下一次的时间*/
			cal.setTime(notify.getNextCallTime());
			HttpNotifyInterval notifyInterval = getNextInterval(NoticeConstants.MAX_NOTIFY_COUNT - notify.getRemainCnt() + 1);
			/**减少次数*/
			notify.setRemainCnt(notify.getRemainCnt() - 1);
			cal.add(Calendar.MINUTE, notifyInterval.getIntervalTime());  
			notify.setNextCallTime(cal.getTime());
			

		}else{
			notify.setStatus(NoticeConstants.CALL_SUCCESS);
		}
		
		mergeNotify(notify);
	}

	@Override
	public List<HttpNotify> findAllCallback(Pageable pageable, String status,
			Date startTime, Date endTime) {
		
		
		Page<HttpNotify>  result = null;
		if(status !=null && !status.equalsIgnoreCase("")){
			
			if(startTime !=  null){
				
				if(endTime != null){
					
					result = httpNotifyRepository.findHttpNotifyByStatusStartEndTime(status, startTime, endTime, pageable);
					
				}else{
					
					result = httpNotifyRepository.findHttpNotifyByStatusStartTime(status, startTime, pageable);
					
				}
				
			}else{
			
					result = httpNotifyRepository.findHttpNotifyByStatus(status, pageable);
				
			}
			
		}else{
			
			if(startTime !=  null){
				
				if(endTime != null){
					
					
					result= httpNotifyRepository.findHttpNotifyByStartEndTime(startTime, endTime, pageable);
					
				}else{
					
					result= httpNotifyRepository.findHttpNotifyByStartTime(startTime, pageable);
					
				}
			}else{
				
					result= httpNotifyRepository.findAll(pageable);
				
			}
			
		}
		
		return result.getContent();
		
	}

}
