package com.jh.paymentchannel.util.ump.log;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * ***********************************************************************
 * <br>description : log4j日志
 * @author      umpay
 * @date        2014-8-1 上午09:22:22
 * @version     1.0  
 ************************************************************************
 */
public class Log4jLogger implements ILogger{

	private final static Logger log = LogManager.getLogger("umpSign");

	public void debug(String msg) {
		log.debug(msg);
		
	}

	public void debug(String msg, Throwable ex) {
		log.debug(msg,ex);
		
	}

	public void info(String msg) {
		log.info(msg);
		
	}

	public void info(String msg, Throwable ex) {
		log.info(msg,ex);
	}
	
	
}
