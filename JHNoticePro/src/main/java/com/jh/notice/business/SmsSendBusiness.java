package com.jh.notice.business;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.jh.notice.pojo.SMSRecord;

public interface SmsSendBusiness {
	
	/***
	 * 分页查询用户的短信记录
	 * @param pageable
	 * @param phone
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<SMSRecord> findSmsRecord(Pageable pageable, String phone, Date startTime,  Date endTime);

	/**发送短信*/
	public void sendSmsMessage(String phone,String tpl_id,  Map<String, String> params,String ipAddress);
	
	/**发送通知**/
	public void sendSmsInformMessage(String phone,String tpl_id, Map<String, String> params,String ipAddress,String brandId) ;
	
	
	/**根据手机号获取最后一条的短信验证码*/
	public String querySmscodeByPhone(String phone);

	public int findCountByIpAddress(String ipAddress);

	public int findCountByPhoneAndDate(String phone);

	/**
	 * 新发送通知
	 */
	void sendSmsNotice(String phone,String tpl_id, Map<String, String> params,String ipAddress);

}
