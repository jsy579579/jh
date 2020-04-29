package cjhkchannel.utils;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午3:38:33 类说明
 */
public class RequestURL {
	/**
	 * 注册接口
	 * 
	 */
	public static final String IS_Register = "/v2/merchant/merchantReg";
	/**
	 * 银行卡签约短信发送
	 * 
	 */
	public static final String IS_Send_SMS = "/v2/sign/merchantSignSms";
	/**
	 * 银行卡签约
	 * 
	 */
	public static final String IS_Confirm_SMS = "/v2/sign/merchantSign";
	/**
	 * 签约状态查询
	 * 
	 */
	public static final String IS_Query_Bank = "/v2/sign/signQuery";
	/**
	 * 消费
	 * 
	 */
	public static final String IS_Consume = "/v2/trans/merchantConsume";
	/**
	 * 消费回调
	 * 
	 */
	public static final String IS_Consume_CallBack = "/v1.0/paymentgateway/topup/cjquick/fastpay/notify_call";
	/**
	 * 消费查询
	 * 
	 */
	public static final String IS_Quick_Consume = "/v2/trans/consumeQuery";
	/**
	 * 商户变更(1:交易费率变更    2：交易费率新增)
	 * 
	 */
	public static final String IS_Change_Merchant = "/v2/merchant/merchantChange";
	/**
	 * 提现
	 * 
	 */
	public static final String IS_Withdraw = "/v2/trans/withdraw";
	/**
	 * 提现结果查询
	 * 
	 */
	public static final String IS_Quick_Withdraw = "/v2/trans/withdrawQuery";
	/**
	 * 商户钱包查询
	 * 
	 */
	public static final String IS_Query_wallet = "/v2/wallet/walletQuery";

}
