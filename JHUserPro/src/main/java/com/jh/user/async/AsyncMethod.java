package com.jh.user.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.jh.user.service.UserJpushService;

@Component
@Lazy(true)
public class AsyncMethod{
	
	@Autowired
	private UserJpushService userJpushService;

	@Async
	public void JpushTest(long userId,String alert,String content, String btype,String btypeval){
		userJpushService.JpushTest(userId, alert, content, btype, btypeval);
	}

}
