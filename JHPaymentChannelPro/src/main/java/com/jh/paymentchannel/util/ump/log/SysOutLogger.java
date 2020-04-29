
package com.jh.paymentchannel.util.ump.log;
/**
 * ***********************************************************************
 * <br>description : 控制台日志
 * @author      umpay
 * @date        2014-8-1 上午09:23:36
 * @version     1.0  
 ************************************************************************
 */
public class SysOutLogger implements ILogger {

	public void debug(String msg) {
		System.out.println(msg);

	}

	public void debug(String msg, Throwable ex) {
		System.out.println(msg);
		ex.printStackTrace();

	}

	public void info(String msg) {
		System.out.println(msg);

	}

	public void info(String msg, Throwable ex) {
		System.out.println(msg);
		ex.printStackTrace();

	}

}
