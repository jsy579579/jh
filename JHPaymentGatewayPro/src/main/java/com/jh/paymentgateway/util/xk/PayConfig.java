package com.jh.paymentgateway.util.xk;

public class PayConfig {

	public static final String MERNO = "99000029"; //商户号
	public static final String SIGNTYPE = "MD5";
	
	//public static final String KEY = "123123";
	public static final String KEY = "79fex6cpm43jdck8";
	public static final String SIGNATURE = "signature";
	
	public static final String ORDER_QUERY_UEL = "http://localhost:8080/orderQuery/payOrderQuery";
	
	public static final String REPAY_ORDER_QUERY_UEL = "http://localhost:8080/orderQuery/repayOrderQuery";
	
	public static final String CASH_ORDER_QUERY_UEL = "http://localhost:8080/orderQuery/cashOrderQuery";
	
	public static final String MER_BALANCE_QUERY_UEL = "http://localhost:8080/merInfo/merBalanceQuery";
	
	public static final String MER_CASH_UEL = "http://localhost:8080/merInfo/merCash";
}
