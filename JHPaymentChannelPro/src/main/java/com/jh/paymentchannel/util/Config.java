package com.jh.paymentchannel.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ������������ļ��ļ��غͻ�ȡ
 * 
 * @author Administrator
 */
public class Config {

	private static final String propsPath = "/config.properties";
	private static Properties props = new Properties();
	private static boolean initFlag = false;

	/**
	 * ��ʼ�������ļ�
	 */
	private static synchronized void init() {
		if (!initFlag) {
			InputStream in = null;
			try {
				in = Config.class.getResourceAsStream(propsPath);
				props.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
			initFlag = true;
		}
	}

	/**
	 * ��ȡ��ʼ������
	 * 
	 * @param propName
	 * @return
	 */
	public static String getProperty(String propName) {
		init();
		return props.getProperty(propName);
	}
}
