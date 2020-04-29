/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * www.hnapay.com
 */

package com.jh.paymentgateway.util.xskj;

/**
 * com.hnapay.expconsumedemo.constant
 * Created by weiyajun on 2017-03-02  11:08
 */
public class ExpConstant {

    //新生签名使用的算法
    public static String ALGORITHM = "RSA";

    //快捷类新生公钥
    public static String EXP_HANPAY_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4Ybi8UscW3Cq4yFoLqZAmTv+3dtzBvc6mOKg/Ec75OJm+BfOpR8wM9eKa/rhBXnudSgXsoDEaTO7wmRtSHL+aLpdHQfVTwPjzkJjKx7rMHwTqgCu5ASDabz4vY6QCSJ9KoYET5lsRU/qB7/XQxNnSDA7Q8I7jEGXpEfLmTrOZrQIDAQAB";


    public static String VERSION = "version";
    public static String TRANCODE = "tranCode";
    public static String MERID = "merId";
    public static String MERORDERID = "merOrderId";
    public static String SUBMITTIME = "submitTime";
    public static String MSGCIPHERTEXT = "msgCiphertext";
    public static String SIGNTYPE = "signType";
    public static String SIGNVALUE = "signValue";
    public static String MERATTACH = "merAttach";
    public static String CHARSET = "charset";

    public static String TRANAMT = "tranAmt";
    public static String PAYTYPE = "payType";
    public static String CARDTYPE = "cardType";
    public static String BANKCODE = "bankCode";
    public static String CARDNO = "cardNo";
    public static String BIZPROTOCOLNO = "bizProtocolNo";
    public static String PAYPROTOCOLNO = "payProtocolNo";
    public static String FRONTURL = "frontUrl";
    public static String NOTIFYURL = "notifyUrl";
    public static String ORDEREXPIRETIME = "orderExpireTime";
    public static String MERUSERIP = "merUserIp";
    public static String RISKEXPAND = "riskExpand";
    public static String GOODSINFO = "goodsInfo";
    public static String HNAPAYORDERID = "hnapayOrderId";//新生订单号
    public static String HOLDNAME = "holderName";//持卡人姓名
    public static String CARDAVAILABLEDATE = "cardAvailableDate";//信用卡有效期
    public static String CVV2 = "cvv2";//信用卡CVV2
    public static String MOBILENO = "mobileNo";//银行签约手机号
    public static String IDENTITYTYPE = "identityType";//证件类型
    public static String IDENTITYCODE = "identityCode";//证件号码
    public static String MERUSERID = "merUserId";//商户用户ID
    public static String PAYFACTORS = "payFactors";//支付要素
    public static String RESULTCODE="resultCode";//处理结果码
    public static String ERRORCODE="errorCode";//异常代码
    public static String ERRORMSG="errorMsg";//异常描述
    
    public static String ORGMERORDERID = "orgMerOrderId";  // 原商户支付订单号
    public static String ORGSUBMITTIME = "orgSubmitTime";  // 原订单支付下单请求时间
    public static String ORDERAMT = "orderAmt";            // 原订单金额
    public static String REFUNDORDERAMT = "refundOrderAmt";// 退款金额
    public static String SMSCODE = "smsCode";//签约短信验证码

    public static String MOBILE = "mobile";//手机号
    public static String PAYEEACCOUNT = "payeeAccount";//结算卡号


    /** 信用卡消费参数常量 **/
    /** 商户ID【请如实填写】. */
    public static final String MER_ID = "11000000205";
    /** 新生信用卡消费出款接口请求地址. */
    public static final String URL_PAY = "http://localhost:8080/gateway/exp/payment.do";
    /** 交易码 信用卡消费出款-EXP20 */
    public static final String TRAN_CODE = "EXP20";
    /** 版本号 2.0. */
    public static final String TRAN_VERSION = "2.0";
    /** 签名类型 RSA-1. */
    public static final String SIGN_TYPE_RSA = "1";
    /** 编码方式 UTF8-1. */
    public static final String CHARSET_UTF8 = "1";

}
