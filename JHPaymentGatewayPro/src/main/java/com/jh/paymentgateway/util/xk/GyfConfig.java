package com.jh.paymentgateway.util.xk;

public class GyfConfig {

    private static final String url="http://47.97.157.247:8082";

    public static final String registerUrl = url+"/app-api/kftBigPay/bindCardInit";

    public static final String bindCardVerifyUrl = url+"/app-api/kftBigPay/bindCardVerify";

    public static final String bindCardPayUrl = url+"/app-api/kftBigPay/bindCardPay";

    public static final String queryCustBalance = url+"/app-api/kftBigPay/queryCustBalance";

    public static final String repay = url+"/app-api/kftBigPay/repay";

    public static final String payOrderQueryURl = url+"/app-api/orderQuery/payOrderQuery";

    public static final String repayOrderQueryUrl = url+"/app-api/orderQuery/repayOrderQuery";

    public static final String repayOrderQuery = url+"/orderQuery/repayOrderQuery";

    public static final String payOrderQuery = url+"/orderQuery/payOrderQuery";

}
