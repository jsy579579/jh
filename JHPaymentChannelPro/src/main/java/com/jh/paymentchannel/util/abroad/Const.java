package com.jh.paymentchannel.util.abroad;

public class Const {
	
	public static final String DIRECT_PAY_TRADE="com.order.Unified.Pay";				//统一下单接口
	public static final String OPERATEORDER_VIEW="com.order.Trade.Query";				//统一下单查询接口
	public static final String PROXY_PAY="com.proxy.Pay.OrderPay";						//余额代付下单接口
	public static final String PROXY_QUERY="com.proxy.Pay.OrderQuery";					//余额代付查询接口
	public static final String BALANCE_QUERY="com.proxy.Pay.BalanceQuery";				//可用金额查询接口
	public static final String RATE_QUERY="com.order.Trade.RateQuery";
	
	public static final String PMT_TAG_WECHAT="weixin_qrcode";							//微信支付方式，暂未用
	public static final String PMT_TAG_QQ="qq_qrcode";									//qq支付方式
	public static final String PMT_TAG_ALIPAY="alipay_qrcode";							//支付宝支付
	public static final String PAY_TYPE_SWEPT="swept";								//支付场景，暂未用
	public static final String PAY_WAP_ALIPAY="alipay_wap";
	
	
}
