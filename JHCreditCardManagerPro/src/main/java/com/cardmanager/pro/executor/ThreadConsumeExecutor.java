package com.cardmanager.pro.executor;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;

@Component
@Scope("prototype")
public class ThreadConsumeExecutor extends BaseExecutor implements Runnable {

	private ConsumeTaskPOJO consumeTaskPOJO;
	
	@Override
	public void run() {
		this.channelFactory.getChannelRoot(consumeTaskPOJO.getVersion()).executeConsumeTask(consumeTaskPOJO);
	}

	public ConsumeTaskPOJO getConsumeTaskPOJO() {
		return consumeTaskPOJO;
	}

	public void setConsumeTaskPOJO(ConsumeTaskPOJO consumeTaskPOJO) {
		this.consumeTaskPOJO = consumeTaskPOJO;
	}
}
