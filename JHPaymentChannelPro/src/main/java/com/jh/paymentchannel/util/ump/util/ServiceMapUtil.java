package com.jh.paymentchannel.util.ump.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.jh.paymentchannel.util.ump.common.Const;
import com.jh.paymentchannel.util.ump.common.ReqRule;

/**
 * ***********************************************************************
 * <br>description : 缓存请求的servic接口、正则、加解密字段等
 * @author      umpay
 * @date        2014-8-1 上午09:40:46
 * @version     1.0  
 ************************************************************************
 */
public  class ServiceMapUtil {
	private static Map serviceRule = new HashMap();
	private static Map reqRule = new HashMap();
	private static HashSet encryptSet = new HashSet();
	private static HashSet encryptId = new HashSet();
	
	static{
		serviceRule.put("pay_req", Const.PAY_REQ_RULE);
		serviceRule.put("pay_req_ivr_call", Const.PAY_REQ_IVR_CALL_RULE);
		serviceRule.put("pay_req_ivr_tcall", Const.PAY_REQ_IVR_TCALL_RULE);
		serviceRule.put("query_order", Const.QUERY_ORDER_RULE);
		serviceRule.put("mer_cancel", Const.MER_CANCEL_RULE);
		serviceRule.put("mer_refund", Const.MER_REFUND_RULE);
		serviceRule.put("download_settle_file", Const.DOWNLOAD_SETTLE_FILE_RULE);
		serviceRule.put("pay_req_split_front", Const.PAY_REQ_SPLIT_FRONT_RULE);
		serviceRule.put("pay_req_split_back", Const.PAY_REQ_SPLIT_BACK_RULE);
		serviceRule.put("pay_req_split_direct", Const.PAY_REQ_SPLIT_DIRECT_RULE);
		serviceRule.put("split_refund_req", Const.SPLIT_REFUND_REQ_RULE);
		serviceRule.put("pay_result_notify",Const.PAY_RESULT_NOTIFY_RULE);
		serviceRule.put("split_req_result", Const.SPLIT_REFUND_REQ_RULE);
		serviceRule.put("split_refund_result", Const.SPLIT_REFUND_RESULT_RULE);
		serviceRule.put("credit_direct_pay", Const.CREDIT_DIRECT_PAY_RULE);
		serviceRule.put("debit_direct_pay", Const.DEBIT_DIRECT_PAY_RULE);
		serviceRule.put("pre_auth_direct_req", Const.PRE_AUTH_DIRECT_REQ);
		serviceRule.put("pre_auth_direct_pay", Const.PRE_AUTH_DIRECT_PAY);
		serviceRule.put("pre_auth_direct_cancel", Const.PRE_AUTH_DIRECT_CANCEL);
		serviceRule.put("pre_auth_direct_query", Const.PRE_AUTH_DIRECT_QUERY);
		serviceRule.put("pre_auth_direct_refund", Const.PRE_AUTH_DIRECT_REFUND);
		serviceRule.put("pre_auth_direct_settle", Const.PRE_AUTH_DIRECT_SETTLE);
		serviceRule.put("pay_transfer_register", Const.PAY_TRANSFER_REGISTER);
		serviceRule.put("pay_transfer_req", Const.PAY_TRANSFER_REQ);
		serviceRule.put("pay_transfer_order_query", Const.PAY_TRANSFER_ORDER_QUERY);
		serviceRule.put("pay_transfer_mer_refund", Const.PAY_TRANSFER_MER_REFUND);
		serviceRule.put("card_auth", Const.CARD_AUTH);
		serviceRule.put("req_sms_verifycode", Const.REQ_SMS_VERIFYCODE);
		serviceRule.put("pay_confirm", Const.PAY_CONFIRM);
		serviceRule.put("pay_req_shortcut_front", Const.PAY_REQ_SHORTCUT_FRONT);
		serviceRule.put("pay_req_shortcut", Const.PAY_REQ_SHORTCUT);
		serviceRule.put("first_pay_confirm_shortcut", Const.FIRST_PAY_CONFIRM_SHORTCUT);
		serviceRule.put("agreement_pay_confirm_shortcut", Const.AGREEMENT_PAY_CONFIRM_SHORTCUT);
		serviceRule.put("req_smsverify_shortcut", Const.REQ_SMSVERIFY_SHORTCUT);
		serviceRule.put("query_mer_bank_shortcut", Const.QUERY_MER_BANK_SHORTCUT);
		serviceRule.put("query_mercust_bank_shortcut", Const.QUERY_MERCUST_BANK_SHORTCUT);
		serviceRule.put("unbind_mercust_protocol_shortcut", Const.UNBIND_MERCUST_PROTOCOL_SHORTCUT);
		serviceRule.put("split_req", Const.SPLIT_REQ_RULE);
		serviceRule.put("query_split_order", Const.QUERY_SPLIT_ORDER_RULE);
		serviceRule.put("transfer_direct_req", Const.TRANSFER_DIRECT_REQ_RULE);
		serviceRule.put("transfer_query", Const.TRANSFER_QUERY_RULE);
		serviceRule.put("mer_order_info_query", Const.MER_ORDER_INFO_QUERY);
		serviceRule.put("mer_refund_query", Const.MER_REFUND_QUERY);
	}
	
	/**
	 * ReqRule 第一个参数true、false 标识是否允许为空
	 *         第二个参数标识，需要匹配的正则表达式
	 *         第三个参数标识，字符对应值允许的最大长度 0标识未设定
	 *         第四个参数表示，是否进行urlEncoder
	 */
	static{
		reqRule.put("service", new ReqRule(false,"[a-zA-Z0-9_]*",32,false));
		reqRule.put("charset", new ReqRule(false,"UTF-8|GBK|GB2312|GB18030",0,false));
		reqRule.put("mer_id", new ReqRule(false,"^[0-9]*$",8,false));
		reqRule.put("sign_type", new ReqRule(false,"RSA",0,false));
		reqRule.put("sign", new ReqRule(false,"",0,true));        //如商户请求POST方式,无需对sign字段编码
		reqRule.put("ret_url", new ReqRule(true,"",0,true));
		reqRule.put("notify_url", new ReqRule(true,"",0,true));
		reqRule.put("res_format", new ReqRule(false,"",0,false));
		reqRule.put("version", new ReqRule(false,"4.0|1.0",3,true));
		reqRule.put("goods_id", new ReqRule(true,"",0,false));
		reqRule.put("goods_inf", new ReqRule(true,"",0,true));
		reqRule.put("media_id", new ReqRule(true,"",0,true));
		reqRule.put("mobile_id", new ReqRule(true,"",11,false));
		reqRule.put("media_type", new ReqRule(true,"MOBILE|EMAIL|MERUSERID",0,false));       //
		reqRule.put("order_id", new ReqRule(false,"",32,true));       //
		reqRule.put("mer_date", new ReqRule(false,"[12][0-9]{7}",32,false));        //
		reqRule.put("amount", new ReqRule(false,"^[1-9][0-9]*$",0,false));
		reqRule.put("amt_type", new ReqRule(false,"RMB",0,false));        
		reqRule.put("pay_type", new ReqRule(true,"",0,false));
		reqRule.put("gate_id", new ReqRule(true,"",0,false));
		reqRule.put("mer_priv", new ReqRule(true,"",0,true));
		reqRule.put("user_ip", new ReqRule(true,"",0,false));
		reqRule.put("expand", new ReqRule(true,"",0,true));
		reqRule.put("expire_time", new ReqRule(true,"[0-9]*",32,false));
		reqRule.put("token", new ReqRule(false,"",0,false));
		reqRule.put("trade_state", new ReqRule(false,"",32,false));
		reqRule.put("ret_code", new ReqRule(false,"^[0-9]*$",0,false));
		reqRule.put("ret_msg", new ReqRule(true,"",0,true));
		reqRule.put("trade_no", new ReqRule(false,"",16,false));
		reqRule.put("pay_date", new ReqRule(false,"[12][0-9]{7}",8,false));
		reqRule.put("settle_date", new ReqRule(false,"[12][0-9]{7}",8,false));
		reqRule.put("pay_seq", new ReqRule(true,"",0,false));
		reqRule.put("error_code", new ReqRule(true,"^[0-9]*$",0,false));
		reqRule.put("mer_check_date", new ReqRule(true,"[12][0-9]{7}",0,false));
		reqRule.put("mer_trace", new ReqRule(true,"",0,false));
		reqRule.put("bank_check_state", new ReqRule(true,"",0,false));
		reqRule.put("product_id", new ReqRule(true,"",0,false));
		reqRule.put("refund_amt", new ReqRule(true,"^[1-9][0-9]*$",0,false));             //
		reqRule.put("refund_no", new ReqRule(false,"",16,false));
		reqRule.put("refund_amount", new ReqRule(false,"^[1-9][0-9]*$",0,false));     //
		reqRule.put("org_amount", new ReqRule(false,"^[1-9][0-9]*$",0,false));
		reqRule.put("refund_state", new ReqRule(false,"",0,false));
		reqRule.put("split_data", new ReqRule(true,"",0,true));
		reqRule.put("split_type", new ReqRule(true,"11|21|[1-2]{0,2}",0,false));
		reqRule.put("is_success", new ReqRule(false,"Y|N",1,false));
		reqRule.put("sub_mer_id", new ReqRule(false,"^[0-9]*$",8,false));
		reqRule.put("sub_order_id",new ReqRule(false,"",32,false));
		reqRule.put("refund_desc", new ReqRule(false,"",128,true));
		reqRule.put("card_id", new ReqRule(true,"",256,true));
		reqRule.put("valid_date", new ReqRule(false,"",256,true));
		reqRule.put("cvv2", new ReqRule(false,"",256,true));
		reqRule.put("pass_wd", new ReqRule(true,"",256,true));
		reqRule.put("identity_type",new ReqRule(true,"",256,false));
		reqRule.put("identity_code",new ReqRule(true,"",256,true));
		reqRule.put("card_holder",new ReqRule(true,"",256,true));
		reqRule.put("req_date", new ReqRule(true,"[12][0-9]{7}",8,false));
		reqRule.put("req_time", new ReqRule(true,"[0-9]{6}",6,false));
		reqRule.put("cust_name", new ReqRule(true,"",32,true));
		reqRule.put("mail_addr", new ReqRule(false,"",64,true));
		reqRule.put("birthday", new ReqRule(false,"[12][0-9]{7}",8,false));
		reqRule.put("sex", new ReqRule(false,"M|F",1,false));
		reqRule.put("contact_phone", new ReqRule(false,"",0,false));
		reqRule.put("contact_mobile", new ReqRule(false,"[0-9]{11}",11,false));
		reqRule.put("fee_amount", new ReqRule(false,"0|^[1-9][0-9]*$",0,false));
		reqRule.put("finance_vou_no", new ReqRule(false,"",32,false));
		reqRule.put("recv_account_type", new ReqRule(true,"[0-1]{2}",2,false));
		reqRule.put("recv_bank_acc_pro", new ReqRule(true,"[0-1]{1}",1,false));
		reqRule.put("recv_account", new ReqRule(true,"",0,true));
		reqRule.put("recv_user_name", new ReqRule(true,"",0,true));
		reqRule.put("recv_gate_id", new ReqRule(true,"",0,false));
		reqRule.put("recv_type", new ReqRule(true,"[0-1]",1,false));
		reqRule.put("purpose", new ReqRule(false,"",0,true));
		reqRule.put("prov_name", new ReqRule(false,"",0,true));
		reqRule.put("city_name", new ReqRule(false,"",0,true));
		reqRule.put("bank_brhname", new ReqRule(false,"",0,true));
		reqRule.put("debit_pay_type", new ReqRule(true,"1|2",1,false));
		reqRule.put("pay_category", new ReqRule(false,"01",2,false));
		reqRule.put("verify_code",new ReqRule(true,"",8,false));
		reqRule.put("mer_cust_id", new ReqRule(true,"",32,false));
		reqRule.put("usr_busi_agreement_id", new ReqRule(true,"",64,false));
		reqRule.put("usr_pay_agreement_id", new ReqRule(true,"",64,false));
		reqRule.put("split_category", new ReqRule(true,"1|2|3",1,false));
		reqRule.put("identity_holder", new ReqRule(true,"",256,true));
		reqRule.put("split_refund_list", new ReqRule(true,"",0,true));
		reqRule.put("split_cmd", new ReqRule(true,"",0,true));
		reqRule.put("settle_type", new ReqRule(true,"",0,false));
		reqRule.put("push_type", new ReqRule(true,"0|1|2|3",1,false));
		reqRule.put("order_type", new ReqRule(true,"1|2",1,false));
	}
	
	/**默认需要进行加密的字段名称*/
	static{
		 encryptId.add("card_id");
		 encryptId.add("cvv2");
		 encryptId.add("valid_date");
		 encryptId.add("card_holder");
		 encryptId.add("identity_code");
		 encryptId.add("pass_wd");
		 encryptId.add("recv_account");
		 encryptId.add("recv_user_name");
		 encryptId.add("identity_holder");
		 encryptId.add("cardId");
		 encryptId.add("validDate");
		 encryptId.add("cardHolder");
		 encryptId.add("identityCode");
		 encryptId.add("passWd");
		 encryptId.add("mer_cust_name");
		 encryptId.add("account_name");
	}
	public static Map getServiceRule() {
		return serviceRule;
	}

	public static Map getReqRule() {
		return reqRule;
	}

	public static HashSet getEncryptSetw() {
		return encryptSet;
	}

	public static HashSet getEncryptId() {
		return encryptId;
	}
}
