package com.cardmanager.pro.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;

@Component
@Scope("prototype")
public class ThreadConsumeCheckor extends BaseExecutor implements Runnable {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private ConsumeTaskPOJO consumeTaskPOJO;
	
	@Override
	public void run() {
		this.channelFactory.getChannelRoot(consumeTaskPOJO.getVersion()).checkConsumeTask(consumeTaskPOJO);
	}
	
	public void setConsumeTaskPOJO(ConsumeTaskPOJO consumeTaskPOJO) {
		this.consumeTaskPOJO = consumeTaskPOJO;
	}

}
