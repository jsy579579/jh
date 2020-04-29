package com.jh.mircomall.utils;

import java.util.Date;

public  class OrderUtil {
	/**
	 * 生成订单号
	 *@Author ChenFan
	 *@Date 2018年5月9日
	 * @param userId
	 * @param businessId
	 * @return
	 */
	public final static String orderNumber(int userId,int businessId){
		
		StringBuilder builder=new StringBuilder();
		builder.append("T");
		builder.append(new Date().getTime());
		builder.append("U");
		builder.append(userId);
		builder.append("G");
		builder.append(businessId);
		return builder.toString();
	}
}
