package com.jh.paymentchannel.util.ump.log;

/**
 * ***********************************************************************
 * <br>description : 日志管理器
 * @author      umpay
 * @date        2014-8-1 上午09:23:10
 * @version     1.0  
 ************************************************************************
 */
public class LogManager {

	private static ILogger logger ;
	
	/**
	 * 获取日志处理器
	 * @return
	 */
	public static ILogger getLogger(){
		if( null != logger)return logger;
		try{
			Class.forName("org.apache.log4j.Logger");
			logger = new Log4jLogger();
			logger.info("使用Log4j进行日志输出");
		}catch(ClassNotFoundException ex){
			logger = new SysOutLogger();
			logger.info("没有发现Log4j,使用System.out进行日志输出");
		}
		return logger;
	}
	
	/**
	 * 注入logger
	 * @param logger_
	 */
	public static void setLogger(ILogger logger_){
		logger = logger_;
	}
}
