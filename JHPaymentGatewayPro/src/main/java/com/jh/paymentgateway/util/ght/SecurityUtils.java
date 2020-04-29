package com.jh.paymentgateway.util.ght;


import java.util.HashMap;
import java.util.Map;



/**
 * @author zhangly
 * 
 *         加密，验签类
 */
public class SecurityUtils
{
	/**
	 * Logger for this class
	 */
//	private static final Logger logger = Logger.getLogger(SecurityUtils.class);

	public static final String inputCharset = "UTF-8";

	/**
	 * 获取sign
	 * 
	 * @param sPara
	 *            参数
	 * @param privateKey
	 *            私钥
	 * @return
	 * @throws Exception
	 */

	/**
	 * 验签
	 * 
	 * @param sPara
	 *            参数
	 * @param sign
	 *            sign值
	 * @param pubKey
	 *            公钥
	 * @return
	 * @throws Exception
	 */

	/**
	 * 解密
	 * 
	 * @param msg
	 *            接收到信息
	 * @param aesKey
	 *            aesKey
	 * @return
	 * @throws Exception
	 */
//	public static String decryptMsg(final String msg, final String aesKey) throws Exception
//	{
//		// base64 转码
//		byte[] decodeStr = Base64.decode(msg);
//		// String a = new String(decodeStr);
//		// System.out.println("--------------a-------------"+a);
//		// 解密
//		byte[] decryptStr = AES.decrypt(decodeStr, Base64.decode(aesKey));
//
//		return new String(decryptStr, inputCharset);
//	}

	/**
	 * 加密
	 * 
	 * @param msg
	 *            请求明文信息
	 * @param aesKey
	 *            aesKey
	 * @return
	 * @throws Exception
	 */
//	public static String encryptMsg(final String msg, final String aesKey) throws Exception
//	{
//		// 加密
//		byte[] encryptStr = AES.encrypt(msg.getBytes(inputCharset), Base64.decode(aesKey));
//		// base64 转码
//		return Base64.encode(encryptStr);
//	}

	/**
	 * 解密
	 * 
	 * @param reqStr
	 * @param dataKey
	 * @return
	 */
//	public static String decrypt(final String reqStr, final String dataKey)
//	{
//		try
//		{
//			String decryptReqMsg = decryptMsg(reqStr, dataKey);
//			logger.info("certify security >>> 【密文信息解密为：】:" + decryptReqMsg); //$NON-NLS-1$
//			return decryptReqMsg;
//		} catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error("certify security >>> decrypt error !!!"); //$NON-NLS-1$
//		}
//		return null;
//	}

	/**
	 * 加密
	 * 
	 * @param respStr
	 * @param dataKey
	 * @return
	 */
//	public static String encrypt(final String respStr, final String dataKey)
//	{
//		try
//		{
//			String decryptRespStr = encryptMsg(respStr, dataKey);
//			logger.error("certify security >>> respStr:" + respStr); //$NON-NLS-1$
//			return decryptRespStr;
//		} catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error("certify security >>> encrypt error !!!"); //$NON-NLS-1$
//		}
//		return null;
//	}

}
