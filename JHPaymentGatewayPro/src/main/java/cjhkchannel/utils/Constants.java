package cjhkchannel.utils;

/**
 * 密钥常量
 *
 */
public class Constants {
	public final static int CONNECT_TIMEOUT = 60; // 设置连接超时时间，单位秒
	public final static int READ_TIMEOUT = 120; // 设置读取超时时间，单位秒
	public final static int WRITE_TIMEOUT = 60; // 设置写的超时时间，单位秒

	public static final String DEVELOP_MODE = "0";
	public static final String CLIENT_NO = "3000";

	public static final String TEST_SERVER_URL = "https://dev-api.chanpay.co";
	public static final String TEST_KEY = "0123456789ABCDEFFEDCBA9876543210";
	public static final String TEST_ACCOUNT_UUID = "4bf0db69-a2a4-4a4f-b9f4-3d555e987115";
	
	public static final String ONLINE_SERVER_URL = "https://qkapi.chanpay.com";
	public static final String ONLINE_KEY = "AF3C14B0B32F6AF9624CE00598C7178CBCE67FF65E91EFC6";
	public static final String ONLINE_ACCOUNT_UUID = "a5762297-4049-4aeb-99bd-9b37ec37686f";

	public static String getClientNo() {
		return CLIENT_NO;
	}

	public static String getServerUrl() {
		// 生产模式
		if ("0".equals(DEVELOP_MODE)) {
			return ONLINE_SERVER_URL;
		}
		// 开发模式
		else {
			return TEST_SERVER_URL;
		}
	}

	public static String getKey() {
		// 生产模式
		if ("0".equals(DEVELOP_MODE)) {
			return ONLINE_KEY;
		}
		// 开发模式
		else {
			return TEST_KEY;
		}
	}

	public static String getAccountUuid() {
		// 生产模式
		if ("0".equals(DEVELOP_MODE)) {
			return ONLINE_ACCOUNT_UUID;
		}
		// 开发模式
		else {
			return TEST_ACCOUNT_UUID;
		}
	}
}
