package com.jh.notice.business;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jh.notice.pojo.HttpNotify;
import com.jh.notice.pojo.HttpNotifyInterval;

public interface HttpCallbackBusiness {

	public List<HttpNotify> findNoCallHttpNotify();
	
	public void  callBack();
	
	public HttpNotifyInterval getNextInterval(int index);
	
	public HttpNotify  mergeNotify(HttpNotify notify);
	
	public List<HttpNotify> findAllCallback(Pageable pageable, String status,  Date startTime,  Date endTime);
}
