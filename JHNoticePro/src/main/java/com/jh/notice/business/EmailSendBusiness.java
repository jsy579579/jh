package com.jh.notice.business;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jh.notice.pojo.EmailRecord;



public interface  EmailSendBusiness {

	
	public List<EmailRecord> findEmailRecord(Pageable pageable);
	
	
	public EmailRecord  saveEmailRecord(EmailRecord record);
	
	
	public void sendEmail();
	
}
