package cn.jh.clearing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import cn.jh.clearing.business.WithDrawDealBusiness;


@Configuration
@Controller
@EnableScheduling
public class WithDrawWaitDealTask {

		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		@Autowired
		private WithDrawDealBusiness withdrawBusiness;
		
		@Value("${schedule-task.on-off}")
		private String scheduleTaskOnOff;
		/**
		 * 每3分钟执行一次
		 */
		@Scheduled(cron = "0 0/3 * * * ?") 
	    public void scheduler() {        
			if("true".equals(scheduleTaskOnOff)){
				withdrawBusiness.deal();
			}
	    }
	
}
