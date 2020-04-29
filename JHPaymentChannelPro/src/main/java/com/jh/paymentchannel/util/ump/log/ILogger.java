
package com.jh.paymentchannel.util.ump.log;

/**
 * ***********************************************************************
 * <br>description : 日志处理器接口
 * @author      umpay
 * @date        2014-8-1 上午09:22:04
 * @version     1.0  
 ************************************************************************
 */
public interface ILogger {
	/**
	 * 
	 * @param msg
	 */
	public void info(String msg);
	/**
	 * 
	 * @param msg
	 */
	public void debug(String msg);
	/**
	 * 
	 * @param msg
	 * @param ex
	 */
	public void info(String msg,Throwable ex);
	/**
	 * 
	 * @param msg
	 * @param ex
	 */
	public void debug(String msg,Throwable ex);
}
