package com.jh.paymentchannel.util.ump.common;

/**
 * ***********************************************************************
 * <br>description : 常量类
 * @author      umpay
 * @date        2014-7-24 下午07:30:47
 * @version     1.0  
 ************************************************************************
 */
public class Const {

	
	/**u付请求地址*/
	public static final String UMPAYSTIE_SERVICE = "/pay/payservice.do";
	/**merId*/
	public static final String MERID = "merId";
	public static final String MER_ID = "mer_id";
	/**goodsId*/
	public static final String GOODSID = "goodsId";
	public static final String GOODS_ID = "goods_id";
	/**goodsInf*/
	public static final String GOODSINF = "goodsInf";
	public static final String GOODS_INF = "goods_inf";
	/**mobileId*/
	public static final String MOBILEID = "mobileId";
	/**orderId*/
	public static final String ORDERID = "orderId";
	public static final String ORDER_ID = "order_id";
	/**merDate*/
	public static final String MERDATE = "merDate";
	public static final String MER_DATE = "mer_date";
	/**payDate*/
	public static final String PAYDATE = "payDate";
	public static final String PAY_DATE = "pay_date";
	/**settleDate*/
	public static final String SETTLEDATE = "settleDate";
	public static final String SETTLE_DATE = "settle_date";
	/**amount*/
	public static final String AMOUNT = "amount";
	/**amtType*/
	public static final String AMTTYPE = "amtType";
	public static final String AMT_TYPE = "amt_type";
	/**bankType*/
	public static final String BANKTYPE = "bankType";
	public static final String BANK_TYPE = "bank_type";
	/**gateId*/
	public static final String GATEID = "gateId";
	public static final String GATE_ID = "gate_id";
	/**refundNo*/
	public static final String REFUNDNO = "refundNo";
	public static final String REFUND_NO = "refund_no";
	/**payAmount*/
	public static final String PAYAMOUNT = "payAmount";
	public static final String PAY_AMOUNT = "pay_amount";
	/**retUrl*/
	public static final String RETURL = "retUrl";
	public static final String RET_URL = "ret_url";
	/**notifyUrl*/
	public static final String NOTIFYURL = "notifyUrl";
	public static final String NOTIFY_URL = "notify_url";
	/**merPriv*/
	public static final String MERPRIV = "merPriv";
	public static final String MER_PRIV = "mer_priv";
	/**expand*/
	public static final String EXPAND = "expand";
	/**version*/
	public static final String VERSION = "version";
	/**version*/
	public static final String CARDINFO = "cardInfo";
	/**sign*/
	public static final String SIGN = "sign";
	/**plain*/
	public static final String PLAIN = "plain";
	/**retCode*/
	public static final String RETCODE = "retCode";
	public static final String RET_CODE = "ret_code";
	/**retMsg*/
	public static final String RETMSG = "retMsg";
	public static final String RET_MSG = "ret_msg";
	/**transType*/
	public static final String TRANSTYPE = "transType";
	public static final String TRANS_TYPE = "trans_type";
	
	
	/**统一支付平台应用名*/
	public static final String PLAT_APP_NAME_PAY = "spay";
	
	public static final String METHOD_GET = "get";
	public static final String METHOD_POST = "post";
	
	public static final String SUCCESS = "0000";
	public static final String SERVICE = "service";
	public static final String PAYSERVICE = "payservice";
	public static final String SIGN_TYPE = "sign_type";
	public static final String CHARSET = "charset";
	public static final String USERIP = "userIp";
	
	/**订单查询数据字段*/
	public static final String PLATTOMER_QUERYTRANS_FIELD = "merId,goodsId,orderId,merDate,payDate,amount,amtType,bankType,mobileId,gateId,transType,transState,settleDate,bankCheck,merPriv,retCode,version,sign";
	/**商户撤销交易数据字段*/
	public static final String PLATTOMER_REVOKE_FIELD = "merId,amount,retCode,retMsg,version,sign";
	/**商户退费交易数据字段*/
	public static final String PLATTOMER_REFUND_FIELD = "merId,refundNo,amount,retCode,retMsg,version,sign";
	/**后台直连数据字段*/
	public static final String PLATTOMER_DIRECTREQPAY_FIELD = "merId,goodsId,orderId,merDate,retCode,retMsg,version,sign";
	/**2011-10-14 add by xiajiajia*/
	/** 一般支付请求*/
	public static final String PAY_REQ_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** IVR支付方式下单*/
	public static final String PAY_REQ_IVR_CALL_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** IVR转呼方式下单*/
	public static final String PAY_REQ_IVR_TCALL_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** 商户查询订单状态*/
	public static final String QUERY_ORDER_RULE="service,charset,sign_type,mer_id,version,order_id,mer_date";
	/** 商户撤销交易*/
	public static final String MER_CANCEL_RULE ="service,charset,sign_type,mer_id,version,order_id,mer_date,amount";
	/** 商户退费*/
	public static final String MER_REFUND_RULE ="service,charset,sign_type,mer_id,version,refund_no,order_id,mer_date,org_amount";
	/** 下载对账文件*/
	public static final String DOWNLOAD_SETTLE_FILE_RULE ="service,sign_type,mer_id,version,settle_date";
	/** 分账前端支付请求*/
	public static final String PAY_REQ_SPLIT_FRONT_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** 分账后端支付请求*/
	public static final String PAY_REQ_SPLIT_BACK_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** 分账退费*/
	public static final String SPLIT_REFUND_REQ_RULE = "service,charset,mer_id,sign_type,version,refund_no,order_id,mer_date,refund_amount,org_amount,sub_mer_id,sub_order_id";
	/** 直连网银*/
	public static final String PAY_REQ_SPLIT_DIRECT_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
	/** 交易结果通知*/
	public static final String PAY_RESULT_NOTIFY_RULE = "service,mer_id,sign_type,version,trade_no,order_id,mer_date,pay_date,amount,amt_type,pay_type,settle_date,trade_state";
	/** 分账结果通知*/
	public static final String SPLIT_REQ_RESULT_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,is_success";
	/** 分账退费结果通知*/
	public static final String SPLIT_REFUND_RESULT_RULE = "service,charset,sign_type,mer_id,version,refund_no,order_id,mer_date,refund_amount,org_amount,refund_amt,sub_mer_id,sub_order_id,sub_refund_amt,is_success";
	/** 信用卡直连*/
	public static final String CREDIT_DIRECT_PAY_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type,pay_type,card_id,valid_date,cvv2";
	/** 借记卡直连*/
	public static final String DEBIT_DIRECT_PAY_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type,pay_type,card_id";
	/**预授权直连申请*/
	public static final String PRE_AUTH_DIRECT_REQ = "service,charset,mer_id,sign_type,version,order_id,mer_date,media_id,media_type,amount,amt_type,pay_type,card_id,valid_date,cvv2";
	/**预授权完成*/
	public static final String PRE_AUTH_DIRECT_PAY = "service,charset,mer_id,sign_type,version,order_id,trade_no,mer_date,amount,amt_type,pay_type";
	/**预授权撤销*/
	public static final String PRE_AUTH_DIRECT_CANCEL = "service,charset,mer_id,sign_type,version,order_id,trade_no,mer_date";
	/**银行卡转账注册*/
	public static final String PAY_TRANSFER_REGISTER = "service,charset,mer_id,res_format,version,sign_type,req_date,req_time,media_type,media_id,identity_type,identity_code,cust_name";
	/**银行卡转账申请*/
	public static final String PAY_TRANSFER_REQ = "service,charset,mer_id,ret_url,notify_url,res_format,version,sign_type,order_id,mer_date,req_time,media_id,media_type,amount,fee_amount,recv_account_type,recv_bank_acc_pro,recv_account,recv_user_name,recv_gate_id,recv_type,purpose";
	/**银行卡转账订单查询*/
	public static final String PAY_TRANSFER_ORDER_QUERY = "service,charset,mer_id,res_format,version,sign_type,order_id,mer_date";
	/**银行卡转账退费*/
	public static final String PAY_TRANSFER_MER_REFUND = "service,charset,mer_id,res_format,version,sign_type,refund_no,order_id,mer_date";
	/**预授权查询*/
    public static final String PRE_AUTH_DIRECT_QUERY = "service,charset,mer_id,sign_type,version,order_id,mer_date";
    /**预授权退费*/
    public static final String PRE_AUTH_DIRECT_REFUND = "service,charset,sign_type,mer_id,version,order_id,mer_date,refund_no,refund_amount,org_amount";
    /**预授权下载对账文件*/
    public static final String PRE_AUTH_DIRECT_SETTLE = "service,sign_type,mer_id,version,settle_date";
    /**实名认证*/
    public static final String CARD_AUTH = "service,charset,mer_id,sign_type,version,mer_date,card_id";
    /**信用卡API快捷---获取短信验证码*/
    public static final String REQ_SMS_VERIFYCODE = "service,mer_id,charset,sign_type,version,trade_no,media_id,media_type";
    /**信用卡API快捷---确认支付*/
    public static final String PAY_CONFIRM = "service,mer_id,charset,sign_type,version,trade_no,pay_category,card_id";
    /**一键快捷--前端请求*/
    public static final String PAY_REQ_SHORTCUT_FRONT = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type,pay_type,gate_id";
    /**一键快捷--API下单*/
    public static final String PAY_REQ_SHORTCUT = "service,charset,mer_id,sign_type,version,order_id,mer_date,amount,amt_type";
    /**一键快捷--(首次支付)确认支付*/
    public static final String FIRST_PAY_CONFIRM_SHORTCUT = "service,mer_id,charset,sign_type,version,trade_no,media_id,media_type,card_id";
    /**一键快捷--（协议支付)确认支付*/
    public static final String AGREEMENT_PAY_CONFIRM_SHORTCUT = "service,mer_id,charset,sign_type,version,trade_no,usr_pay_agreement_id";
    /**一键快捷--获取短信验证码*/
    public static final String REQ_SMSVERIFY_SHORTCUT = "service,mer_id,sign_type,version,trade_no";
    /**一键快捷--查询商户支持的银行列表*/
    public static final String QUERY_MER_BANK_SHORTCUT = "service,sign_type,charset,mer_id,version,pay_type";
    /**一键快捷--查询用户签约的银行列表*/
    public static final String QUERY_MERCUST_BANK_SHORTCUT = "service,sign_type,charset,mer_id,version,pay_type";
    /**一键快捷--商户解除用户关联*/
    public static final String UNBIND_MERCUST_PROTOCOL_SHORTCUT = "service,sign_type,charset,mer_id,version";
    /**分账项目--分账指令*/
    public static final String SPLIT_REQ_RULE = "service,charset,mer_id,sign_type,version,order_id,mer_date";
    /**分账项目--分账状态查询*/
    public static final String QUERY_SPLIT_ORDER_RULE = "service,sign_type,charset,mer_id,version,order_id,mer_date";
    /**付款API直连--付款请求*/
    public static final String TRANSFER_DIRECT_REQ_RULE = "service,charset,mer_id,version,sign_type,order_id,mer_date,amount,recv_account_type,recv_bank_acc_pro,recv_account,recv_user_name,recv_gate_id,purpose,prov_name,city_name,bank_brhname";
    /**付款API直连--付款查询*/
    public static final String TRANSFER_QUERY_RULE = "service,charset,mer_id,version,sign_type,order_id,mer_date";
    /**历史订单查询*/
    public static final String MER_ORDER_INFO_QUERY = "service,sign_type,charset,mer_id,version,order_id,mer_date";
    /**退费订单状态查询*/
    public static final String MER_REFUND_QUERY = "service,sign_type,charset,mer_id,version,refund_no";
    
}
