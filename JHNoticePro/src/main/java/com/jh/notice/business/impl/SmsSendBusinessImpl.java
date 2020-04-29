package com.jh.notice.business.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jh.notice.business.SmsSendBusiness;
import com.jh.notice.config.PropertiesConfig;
import com.jh.notice.pojo.SMSInform;
import com.jh.notice.pojo.SMSRecord;
import com.jh.notice.pojo.SMSRoute;
import com.jh.notice.pojo.SMSTemplate;
import com.jh.notice.repository.SmsInformRepository;
import com.jh.notice.repository.SmsRepository;
import com.jh.notice.repository.SmsRouteRepository;
import com.jh.notice.repository.SMSTemplateRepository;
import com.jh.notice.util.NoticeConstants;

import cn.jh.common.utils.DateUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

@EnableAutoConfiguration
@Service
public class SmsSendBusinessImpl implements SmsSendBusiness{

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private PropertiesConfig propertiesConfig;
	
	@Autowired
	private SmsRepository smsRepository;
	
	@Autowired
	private SmsInformRepository smsInformRepository;
	
	@Autowired
    private SmsRouteRepository smsRouteRepository;
	
	@Autowired
    private SMSTemplateRepository smsTemplateRepository;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	/**分页获取用户的记录*/
	@Override
	public List<SMSRecord> findSmsRecord(Pageable pageable, String phone, Date startTime,  Date endTime) {
		
		
		Page<SMSRecord>  result = null;
		if(phone !=null && !phone.equalsIgnoreCase("")){
			
			if(startTime !=  null){
				
				if(endTime != null){
					
					result = smsRepository.findSmsRecordByPhoneStartEndTime(phone, startTime, endTime, pageable);
					
				}else{
					
					result = smsRepository.findSmsRecordByPhoneStartTime(phone, startTime, pageable);
					
				}
				
			}else{
			
				result = smsRepository.findSmsRecord(phone,  pageable);
				
			}
			
		}else{
			
			if(startTime !=  null){
				
				if(endTime != null){
				
					result = smsRepository.findSmsRecordByStartEndTime(startTime, endTime, pageable);
					
				}else{
					result=  smsRepository.findSmsRecordByStartTime(startTime, pageable);	
				}
				
			}else{
				
				result = smsRepository.findAll(pageable);
				
			}
		}
		
		return result.getContent();
	
	}

	
	/**发送消息*/
	@Override
	public void sendSmsMessage(String phone,String tpl_id, Map<String, String> params,String ipAddress) {
		
		SMSRoute smsRoute = smsRouteRepository.getCurActiveSmsChannel();
	
		SMSRecord  record = new SMSRecord();
		String smsChannel = smsRoute.getCurSMSChannel();
		
		if(smsChannel.equalsIgnoreCase(NoticeConstants.SMS_CHANNEL_1)){
			
			JuHeAPISmsService.sendSms(phone,tpl_id,propertiesConfig.getSmsUrl(),propertiesConfig.getSmsKey(),params);
		}
		
		record.setContent(params.get("code"));
		record.setPhone(phone);
		record.setVeriCode(params.get("code"));
		record.setIpAddress(ipAddress);
		record.setCreateTime(new Date());
		/**存短信进入数据库**/
		smsRepository.save(record);
		
//		//短信存入redis缓存
		redisTemplate.opsForValue().set(record.getPhone()+"", record.getVeriCode());
		redisTemplate.expire(record.getPhone(),60,TimeUnit.SECONDS);
		String s=redisTemplate.opsForValue().get(record.getPhone()+"");
		LOG.info("key="+record.getPhone()+"value="+s);
		//JSONObject json = null;
		//JSONObject result=json.fromObject(s);
		//System.out.println(result.get(record.getPhone()));
		
	}
	
	
	/**发送通知消息*/
	@Override
	public void sendSmsInformMessage(String phone,String template_type, Map<String, String> params,String ipAddress,String brandId) {
		SMSRoute smsRoute = smsRouteRepository.getCurActiveSmsChannel();
		
		SMSInform  inform = new SMSInform();
		
		SMSTemplate SMSTemplate=smsTemplateRepository.findSMSTemplate(template_type);
		
		String smsChannel = smsRoute.getCurSMSChannel();
		
		if(smsChannel.equalsIgnoreCase(NoticeConstants.SMS_CHANNEL_1)){
			JuHeAPISmsService.sendSms(phone,SMSTemplate.getTplId(),propertiesConfig.getSmsUrl(),propertiesConfig.getSmsKey(),params);
		}
		
		inform.setContent(gettemplateContent(params, SMSTemplate.getTemplate()));
		inform.setBrandId(brandId);
		inform.setPhone(phone);
		inform.setVeriCode(SMSTemplate.getTemplateType());
		inform.setIpAddress(ipAddress);
		inform.setCreateTime(new Date());
		/**存短信进入数据库**/
		smsInformRepository.save(inform);
	}

	public String gettemplateContent(Map<String, String> params, String template){
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			String val = params.get(key);
			String tKey="#"+key+"#";
			System.out.println("key:"+tKey+"--val"+val);
			template=template.replace(tKey,val);
		}
		return template;
	}

	@Override
	public String querySmscodeByPhone(String phone) {
		SMSRecord lastestSmsRecord = smsRepository.findLastestSmsRecord(phone);
		if(lastestSmsRecord != null ) {
			return lastestSmsRecord.getVeriCode();
		}else {
			return null; 
		}
	}


	@Override
	public int findCountByIpAddress(String ipAddress) {
		return smsRepository.findCountByIpAddress(ipAddress,new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}


	@Override
	public int findCountByPhoneAndDate(String phone) {
		return smsRepository.findCountByPhoneAndDate(phone,DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd"));
	}

	/**发送消息*/
	public void sendSmsNotice(String phone,String tpl_id, Map<String, String> params,String ipAddress) {
		String s = JuHeAPISmsService.sendSms(phone, tpl_id, propertiesConfig.getSmsUrl(), propertiesConfig.getSmsKey(), params);
	}
}
