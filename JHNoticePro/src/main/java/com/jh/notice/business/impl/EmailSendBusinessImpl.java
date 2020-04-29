package com.jh.notice.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.notice.business.EmailSendBusiness;
import com.jh.notice.pojo.EmailRecord;
import com.jh.notice.repository.EmailRepository;


@Service
public class EmailSendBusinessImpl implements EmailSendBusiness{

	@Autowired
    private EmailRepository repository;
	
	
	@Override
	public List<EmailRecord> findEmailRecord(Pageable pageable) {
		
		Page<EmailRecord> result= repository.findAll(pageable);
		
		return result.getContent();
	}


	@Override
	public EmailRecord saveEmailRecord(EmailRecord record) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void sendEmail() {
		// TODO Auto-generated method stub
		
	}

	
	
}
