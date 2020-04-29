package com.jh.paymentchannel.util.ump.common;

/**
 * ***********************************************************************
 * <br>description : 请求数据规则校验
 * @author      umpay
 * @date        2014-7-24 下午07:41:36
 * @version     1.0  
 ************************************************************************
 */
public class ReqRule {
	/**是否为空*/
	private boolean empty;
	/**匹配正则表达式*/
	private String regex;
	/**字符长度*/
	private int length;
	/**是否需要URLEncoding*/
	private boolean encode;
	public boolean isEmpty() {
		return empty;
	}
	public String getRegex() {
		return regex;
	}
	public int getLength() {
		return length;
	}
	
	public ReqRule(boolean empty,String regex,int length,boolean encode){
		this.empty = empty;
		this.regex = regex;
		this.length = length;
		this.encode = encode;
	}
	public boolean isEncode() {
		return encode;
	}
}
