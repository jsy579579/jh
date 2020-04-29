package com.cardmanager.pro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class PropertiesConfig{
	
	@Value("${schedule-task.on-off}")
	private String scanOnOff;
	
	@Value("${paymentgateway.ip}")
	private String paymentgatewayIp;

	public String getPaymentgatewayIp() {
		return paymentgatewayIp;
	}

	public String getScanOnOff() {
		return scanOnOff;
	}
	
}
