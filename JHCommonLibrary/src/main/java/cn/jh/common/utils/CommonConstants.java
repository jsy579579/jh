package cn.jh.common.utils;

/**
 * 常用常量
 */
public class CommonConstants {
	/*参数不完整**/
	public static final String ERROR_VERI_CODE = "000002";
	/*参数错误**/
	public static final String ERROR_PARAM  = "000001";
	/**通道错误*/
	public static final String ERROR_PASS_ERROR = "000003";
	/**支付错误*/
	public static final String ERROR_PAY_PASS   = "000004";
	/**密令错误*/
	public static final String ERROR_TOKEN      = "000005";
	/**卡教研失败***/
	public static final String ERROR_CARD_FAILED = "000006";
	/**用户不存在***/
	public static final String ERROR_USER_NOT_EXISTED = "000007";
	/***用户已存在**/
	public static final String ERROR_USER_HAS_REGISTER = "000008";
	/**卡失败***/
	public static final String ERROR_CARD_ERROR = "000009";
	
	/**支付请求错误*/
	/**金额有误**/
	public static final String ERROR_AMOUNT_ERROR  = "000010";
	
	public static final String ERRRO_ORDER_HAS_CHECKED = "000011";
	/***用户加入黑名单***/
	public static final String ERROR_USER_BLACK = "000012";
	/**用户未注册***/
	public static final String ERROR_USER_NO_REGISTER = "000013";
	
	/**提现下单错误*/
	public static final String ERROR_WITHDRAW_ORDER_FAIL  = "000014";
	/**默认卡有误**/
	public static final String ERROR_USER_NO_DEFAULT_CARD = "000015";
	/***提现失败**/
	public static final String ERROR_WITHDRAW_REQ_FAILD  = "000016";
	/***提现余额不足**/
	public static final String ERROR_WITHDRAW_BALANCE_NO_ENOUGH = "000017";
	/***不存在**/
	public static final String ERROR_PAYMENT_NOT_EXIST   = "000019";
	/**该笔订单已经提交*/
	public static final String ERROR_WITHDRAW_ORDER_HASREQ = "000020";
	
	
	
	/**签名无效*/
	public static final String ERROR_SIGN_NOVALID = "000018";
	
	/**成功*/
	public static final String SUCCESS = "000000";
	
	public static final String BINDTASKCARD = "888888";
	/***失败*/
	public static final String FALIED  = "999999";
	/**等待处理**/
	public static final String WAIT_CHECK   = "666666";
	
	/***秘密密钥**/
	public static final String SECRETKEY = "juhe-20170328";
	/**结果**/
	public static final String RESULT = "result";
	/**有效**/
	public static final String STATUS_VALID = "0";
	/**无效***/
	public static final String STATUS_INVALID = "1";
	/***返回码**/
	public static final String RESP_CODE  = "resp_code";
	/***返回描述**/
	public static final String RESP_MESSAGE = "resp_message";
	
	
	
	
	
	
	/**品牌类型*/
	/***主品牌**/
	public static final String BRAND_MAIN  = "0";
	/***其他品牌**/
	public static final String BRAND_OTHER = "1";
	
	
	
	/**银行卡的默认状态*/
	/**银行卡默认*/
	public static final String CARD_DEFAULT = "1";
	/**银行卡非默认*/
	public static final String CARD_NOT_DEFAULT  = "0";
	
	
	
	/**充值/支付/代付的订单状态*/
	/***等待回执**/
	public static final String ORDER_READY = "0";
	/**成功**/
	public static final String ORDER_SUCCESS = "1";
	/**取消订单***/
	public static final String ORDER_CANCEL  = "2";
	
	
	/**充值/支付/提现/退款的类型标识*/
	/**充值**/
	public static final String ORDER_TYPE_TOPUP = "0";
	/**支付购买产品**/
	public static final String ORDER_TYPE_PAY   = "1";
	/**提现***/
	public static final String ORDER_TYPE_WITHDRAW = "2";
	public static final String ORDER_TYPE_REFUND     = "3";
	/***待结算***/
	public static final String ORDER_TYPE_BILL     = "4";
	public static final String ORDER_TYPE_WZDJ    = "5";
	public static final String ORDER_TYPE_WZDJ_PAY    = "6";
	public static final String ORDER_TYPE_PAYMENT    = "7";//7信用卡还款和提现
	public static final String ORDER_TYPE_BALANCE    = "8";//8信用卡提现（未使用）
	
	public static final String ORDER_TYPE_CONSUME = "10";//10新信用卡管家消费订单
	
	public static final String ORDER_TYPE_REPAYMENT = "11";//11新信用卡管家还款订单
	
	public static final String ORDER_TYPE_AUTHCOUNT = "12";//12银行卡校验次数充值类型
	
	public static final String ORDER_TYPE_COIN = "13";//13积分抽奖订单
	
	public static final String ORDER_TYPE_EXCHANGE_COIN = "exchange_coin";//JF积分兑换类型
	
	/**积分类型*/
	/***添加分**/
	public static final String COIN_TYPE_ADD = "0";
	/***减少积分**/
	public static final String COIN_TYPE_SUB = "1";
	
	
	/**订单的结算类型*/
	/**工作日结算**/
	public static final String CLEARING_T_0  = "0";
	/**下个工作日结算**/
	public static final String CLEARING_T_1  = "1";
	/***当天结算**/
	public static final String CLEARING_D_0  = "2";
	/**第二天结算**/
	public static final String CLEARING_D_1  = "3";
}
