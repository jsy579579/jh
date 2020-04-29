package com.jh.paymentgateway.util.ryt;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jh.paymentgateway.controller.RYTpageRequest;

public class Test {

    public static final String pub_key = "";
  /*  public static void main(String[] args) throws Exception {
    	String orderNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	String merchantNo = "B101180811";
    	String txnAmt = "55.00";
    	String txnAmt1 = "53.00";
    	String certNo = "370983199302183717";
    	String phoneNo = "13166382981";
    	String realName = "钟守韩";
    	String  accNo="6225768681617732";
    	String rate ="0.0055";
    	String backUrl ="http://www.baidu.com";
    	String frontUrl ="http://www.baidu.com";
    	String province="上海市";
    	String city="上海市";
    	String advanceFee="0.5";
    	String bankName="招商银行";
    	String orderIp="116.235.56.89";
    	
    	RYTpageRequest RYTpageRequest =new RYTpageRequest();
    	
    	//注册
    	RYTpageRequest.CreditRHRegist(accNo, phoneNo, realName, certNo);
    	//开户
    	RYTpageRequest.CreditRHOpenProduct(merchantNo, rate);
    	//Ryt平台信用卡绑卡开通
    	RYTpageRequest.CreditRHBinding(orderNo, merchantNo, accNo, phoneNo, realName, certNo, backUrl,frontUrl);
    	//Ryt平台信用卡消费计划
    	RYTpageRequest.CreditRHConsume(orderNo, merchantNo, txnAmt, accNo, phoneNo, realName, certNo, rate, province, city, backUrl,orderIp);
    	//Ryt平台信用卡代付计划
    	RYTpageRequest.CreditRHAdvance(orderNo, merchantNo, txnAmt1, advanceFee, accNo, bankName, realName, certNo, backUrl);
    	//Ryt平台计划查询
    	RYTpageRequest.CreditRHQuery(orderNo, merchantNo, "2");
    	//Ryt平台商户余额查询
    	RYTpageRequest.CreditRHBalanceQuery(merchantNo);
    	//Ryt平台商户费率修改
    	RYTpageRequest.CreditRHChangeRate(merchantNo, rate);
    	//Ryt平台商户银行卡解绑
    	RYTpageRequest.CreditRHunbind(merchantNo, accNo);
    	
    }*/
}
