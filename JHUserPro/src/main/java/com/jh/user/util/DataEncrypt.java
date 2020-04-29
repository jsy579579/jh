package com.jh.user.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jh.user.pojo.User;


public class DataEncrypt {

	private static final Logger LOG = LoggerFactory.getLogger(DataEncrypt.class);

	//将手机号及银行卡号进行脱敏处理
	public static List<User> userDataEncrypt(List<User> data) {
		for(User data1:data) {
			try {
				String phone=data1.getPhone();
				if(phone!=null) {
					String phoneDate=phone.replace(phone.substring(3,7), "****");
					data1.setPhone(phoneDate);
				}
			} catch (Exception e) {
				
			}
		}
		LOG.info("脱敏后数据============="+data);
		return data;
	}
}
