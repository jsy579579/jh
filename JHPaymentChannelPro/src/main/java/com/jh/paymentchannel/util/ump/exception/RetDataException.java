package com.jh.paymentchannel.util.ump.exception;

/**
 * ***********************************************************************
 * <br>description : 获取平台响应数据异常
 * @author      umpay
 * @date        2014-7-24 下午07:42:26
 * @version     1.0  
 ************************************************************************
 */
public class RetDataException extends Exception{

	private static final long serialVersionUID = 7793118467616878809L;
	
	public RetDataException(){
		super();
	}
	public RetDataException(String msg){
		super(msg);
	}
	public RetDataException(String msg,Throwable ex){
		super(msg,ex);
	}
	public RetDataException(Throwable ex){
		super(ex);
	}

}
