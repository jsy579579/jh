package com.jh.paymentgateway.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sailfish
 * @create 2017-08-24-上午11:19
 */
public interface Conts {
	String mainCustomerNumber = null; //子商编号
	
    String customerNumber = "10015703385";  //商编

    String hmacKey = "oF34lTpB9x9v05D2B0eP1r18EDX71THlT4Go5X0s6V7T85gh2J63j30iPh38";  //商编秘钥

    String baseRequestUrl = "https://skb.yeepay.com/skb-app";  //基础请求路径
	
   }
