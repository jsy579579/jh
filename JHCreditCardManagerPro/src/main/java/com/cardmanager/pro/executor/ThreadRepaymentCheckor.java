package com.cardmanager.pro.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

@Component
@Scope("prototype")
public class ThreadRepaymentCheckor extends BaseExecutor implements Runnable {

	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private RepaymentTaskPOJO repaymentTaskPOJO;

	@Override
	public void run() {
		this.channelFactory.getChannelRoot(repaymentTaskPOJO.getVersion()).checkRepaymentTask(repaymentTaskPOJO);
	}

	public void setRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTaskPOJO) {
		this.repaymentTaskPOJO = repaymentTaskPOJO;
	}

}
