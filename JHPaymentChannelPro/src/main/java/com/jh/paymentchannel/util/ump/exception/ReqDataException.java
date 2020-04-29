
package com.jh.paymentchannel.util.ump.exception;
/**
 * ***********************************************************************
 * <br>description : 请求数据异常
 * @author      umpay
 * @date        2014-7-24 下午07:42:26
 * @version     1.0  
 ************************************************************************
 */
public class ReqDataException extends Exception{

	private static final long serialVersionUID = 7793118467616878809L;
	
	public ReqDataException(){
		super();
	}
	public ReqDataException(String msg){
		super(msg);
	}
	public ReqDataException(String msg,Throwable ex){
		super(msg,ex);
	}
	public ReqDataException(Throwable ex){
		super(ex);
	}

}
