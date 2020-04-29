package cn.jh.clearing.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jh.user.pojo.User;

import cn.jh.clearing.pojo.PaymentOrder;

public class DataEncrypt {

	private static final Logger LOG = LoggerFactory.getLogger(DataEncrypt.class);

	//将手机号及银行卡号进行脱敏处理
	public static List<PaymentOrder> paymentOrderdataEncrypt(List<PaymentOrder> data) {
		for(PaymentOrder data1:data) {
			try {
				String phone=data1.getPhone();
				if(phone!=null) {
					String phoneDate=phone.replace(phone.substring(3,7), "****");
					data1.setPhone(phoneDate);
				}
			} catch (Exception e) {
				
			}
		}
		return data;
	}
	
	
}
