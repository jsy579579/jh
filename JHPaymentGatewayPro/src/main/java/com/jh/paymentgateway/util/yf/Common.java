package com.jh.paymentgateway.util.yf;

import java.util.Map;

/**
 * 全局配置参数
 *
 */
public class Common {

	/**
	 * 下游戏公钥
	 */
	public static final String PUBLICKKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC7NxR6Is8svVX/mqHh/F8p849vPSkjXlUrdZeSxEoaZXRmrRctad50fVJ7/OykoRCnzFztlnEIpq/NEt7waqh1P2Ob/FdsbPxAlOXVf3tMdzw8tDGkLbEC4P4s+QGMeRAijx16OEuLkcTe+C4hQJw1r62dFrMChr8+ZiaRAuj/DwIDAQAB";
	
	/**
	 * 下游私钥
	 */
	public static final String PRIVATEKEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALs3FHoizyy9Vf+aoeH8Xynzj289KSNeVSt1l5LEShpldGatFy1p3nR9Unv87KShEKfMXO2WcQimr80S3vBqqHU/Y5v8V2xs/ECU5dV/e0x3PDy0MaQtsQLg/iz5AYx5ECKPHXo4S4uRxN74LiFAnDWvrZ0WswKGvz5mJpEC6P8PAgMBAAECgYBhaIg0ADaRTbYYNymxfnE5T6u6sjTcXTGtWhLSX2U7FLyay8a6b1I6hBZNwsyR3+fdlrxnNhZ77NadugrbRnN9ITlvr9EXOfh9G9gJlQHgOn6HZWBElMk1dnEJ8OyylDq84TqMqnzBnN0Ht539XkO1WV/nEhXj6sxLpvNs+xbJ+QJBAO2U0IjG6iImLaMQOPSChhXtBpGNZczZz5l0ANpGVV3MWfKAd023pE+4Tzvxu2J80tcVfrmPxi46P6OSesg+gzUCQQDJuqrrV3VwXl2RNgzgxatT32rWy5k7+P1sQ05B1PKGi5mdYDk5xZCnW9B30mJG8zErALIJlh3DKaW70jKDhV2zAkEAhdvL5tSURGtYWUChpnoIDECA6+9MBTBPfHlUpabtIC0sHKrvDTXD+TqyQecAGgyUSqUMwoZUNeWRx5qXXU2DZQJAD5QHwPbXDd9rsFwRMIZzTZ3SPVYptjTrNSIZeAH+3J8JNNsKcEiufA4eEjK//iSnpl9+YDkkgT7FGrUHy5pP+wJAYvzVNCYKB3XdInVfc6RmcZ5bEZAHAXjx+nt5EGfEWmeJH6GcetqFluqh78ezh8yL/uFr3rHlIdJB1suH1mLELQ==";
	
	/**
	 * 我方接口地址
	 */
	public static final String URL = "http://47.106.111.38:8001/QRCodeSys/online.action";
//	public static final String URL = "http://localhost:8080/QRCodeSys/online.action";
	
	public static final String myPayUrl="http://39.108.127.129:8001/up_account/main.action";
//	public static final String myPayUrl="http://localhost:8080/up_account/main.action";

	/**
	 * 编码方式
	 */
	public static final String CHARSET = "UTF-8";

	/**
	 * 向我方发送参数名称
	 */
	public static final String PARAM = "reqParam";
	
	/**
	 * 获取响应参数
	 * @param signMap
	 * @return
	 */
	public static String getParString(Map<String, Object> signMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("respCode=" + signMap.get("respCode") + ", ");
		sb.append("url=" + signMap.get("url") + ", ");
		sb.append("respInfo=" + signMap.get("respInfo"));
		sb.append("}");
		return sb.toString();
	}
}
