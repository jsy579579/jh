package com.cardmanager.pro.executor;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

@Component
@Scope("prototype")
public class ThreadRepaymentExecutor extends BaseExecutor implements Runnable {

	private RepaymentTaskPOJO repaymentTaskPOJO;
	
	@Override
	public void run() {
		this.channelFactory.getChannelRoot(repaymentTaskPOJO.getVersion()).executeRepaymentTask(repaymentTaskPOJO);
	}

	public RepaymentTaskPOJO getRepaymentTaskPOJO() {
		return repaymentTaskPOJO;
	}

	public void setRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTaskPOJO) {
		this.repaymentTaskPOJO = repaymentTaskPOJO;
	}
	
}
