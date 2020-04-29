package com.jh.paymentgateway.util.hqb;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * 签名工具类
 * 
 * @className SignUtil
 * @Description
 * @author xuguiyi
 * @contact
 * @date 2016-6-7 下午11:11:00
 */
public class SignUtil {
	
	/**
	 * 原生微信公众号支付查询
	 *  @param ps
	 *  @return  
	 *  @return String
	 */
	public static String getPrimordialSign(Map<String, String> ps ){
		StringBuffer buff = new StringBuffer() ;
		Set<String> keys = ps.keySet() ;
		for(String key : keys ){
			String value = ps.get(key) ;
			if(SignUtil.isBlank(value) == false ){
				if( buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value) ;
			}
		}
		return buff.toString() ;
	}
	
	/**
	 * 判断字符串是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s) {
		if (s == null || s.trim().length() == 0) {
			return true;
		}
		return false;
	}
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getTeleSign(Map<String, String> sortedParams,String signkey) throws Exception {
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			String value = String.valueOf(sortedParams.get(key));
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				signSrc.append(value);
			}
		}
		
		signSrc.append(DigestUtils.md5Hex(signkey)).append("@!@#@#DDSD323dsds");
		String sign = DigestUtils.md5Hex(DigestUtils.md5Hex(signSrc.toString())).toLowerCase();

		System.out.println("商户signSrc="+signSrc);
		return sign;
		
	}

	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getZFBSign(Map<String, String> sortedParams,String signkey) throws Exception {
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			String value = String.valueOf(sortedParams.get(key));
		if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
			if (i < keys.size()-1) {
				signSrc.append(key + "=" + value+"&");
			}else {
				signSrc.append(key + "=" + value+"&key=");
			}
		}
	}
		signSrc.append(signkey);
		System.out.println("商户signSrc="+signSrc);
		String sign = sha1(signSrc.toString()).toUpperCase();
		System.out.println(sign);
		return sign; 
		
	}
	
	
	/**
     * 将字符串进行sha1加密
     *
     * @param str 需要加密的字符串
     * @return 加密后的内容
     */
    public static String sha1(String str) { 
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     *原生公众号验签
     * @param sortedParams
     * @return
     */
    public static String getPublicSign(Map<String, String> sortedParams,String signkey) throws Exception {
    	StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
				Object key = keys.get(i);
				String value = String.valueOf(sortedParams.get(key));
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				if (i < keys.size()-1) {
					signSrc.append(key + "=" + value+"&");
				}else {
					signSrc.append(key + "=" + value+"&key=" + signkey);
				}
			}
		}
		String sign = DigestUtils.md5Hex(signSrc.toString()).toUpperCase();
		
		return sign;
    }
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getSign(Map<String, String> sortedParams,String signkey) throws Exception {
		
		StringBuffer signSrc = new StringBuffer(); 
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		System.out.println(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i); 
			String value = String.valueOf(sortedParams.get(key));
			if (key != null && !"".equals(key) && value != null && !"".equals(value) && !"sign".equals(key)) {
				signSrc.append(key + "=" + value);
			}
		}
		System.out.println("signSrc="+signSrc);
		String sign = DigestUtils.md5Hex(signSrc.toString() + signkey).toUpperCase();
		
		return sign;
		
	}
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return 
	 */
	public static String getNewCloudSign(Map<String, String> sortedParams,String signkey) throws Exception {
		
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = String.valueOf(sortedParams.get(key));
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				signSrc.append(key + "=" + value);
			}
		} 
		  
		signSrc = signSrc.append(signkey);
		String sign = DigestUtils.md5Hex(signSrc.toString());
		
		return sign;
		
	}
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getCloudSign(Map<String, String> sortedParams,String signkey) throws Exception {
		
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = String.valueOf(sortedParams.get(key));
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				signSrc.append(key + "=" + value +"&");
			}
		}
		signSrc = signSrc.append("key="+ signkey);
		String sign = DigestUtils.md5Hex(signSrc.toString());
		
		return sign;
		
	}
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String createLinkString(Map<String, String> params, boolean encode) {
        List keys = new ArrayList(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = (String)keys.get(i); 
            if (!key.equals("sign"))
            {
                String value = (String)params.get(key); 
                if (StringUtils.isBlank(value)) {
                    System.err.println("请求参数为空或者空字符串不参与验签:" + key);
                }
                else {
                    if (encode) {
                        try {
                            value = URLEncoder.encode(value, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }
        return prestr;
    }
	 
	
	/*public static void main(String[] args) throws Exception {
		//{"transcode":"001","merchno":"001","dsorderid":"2016060700","regno":"222222","compayname":"阿里巴巴","frname":"马云","version":"0100","ordersn":"201511130000003"}
	
//		String json = "{\"transcode\":\"001\",\"merchno\":\"000000000000000\",\"dsorderid\":\"2016060700\",\"regno\":\"222222\",\"compayname\":\"阿里巴巴\",\"frname\":\"马云\",\"version\":\"0100\",\"ordersn\":\"201511130000003\"}";
//	
//		Map<String,String> map = mapper.readValue(json, Map.class);
//		
//		System.out.println(getSign(map,"c26c44"));
		 
		System.out.println(DigestUtils.md5Hex("bindId=180616000379231765dsorderid=18061911041249510003489434fixAmount=100futureRateValue=0.75merchno=2018042211481yxmethodname=CreateRepayPlanMutilnotifyUrl=https://www.yxzngj.online/shuangtongdao/notify.aspxordersn=2018061911041385922repayItemList=[{trade_time=2018-06-19 11:02:39,transfer_time=2018-06-19 11:02:39,trade_amount=568532,transfer_amount=564300,fee=4232,cooperator_item_id=XF18061913493900019736968,cooperator_transfer_item_id=HK18061915023900019736633,repay_item_type=1},{trade_time=2018-06-19 12:06:39,transfer_time=2018-06-19 12:06:39,trade_amount=0,transfer_amount=0,fee=0,cooperator_item_id=HK18061915023900019736633,cooperator_transfer_item_id=HK18061915023900019736633,repay_item_type=2},{trade_time=2018-06-19 13:12:39,transfer_time=2018-06-19 13:12:39,trade_amount=438968,transfer_amount=435700,fee=3268,cooperator_item_id=XF18062013274300019736110,cooperator_transfer_item_id=HK18062014284300019736677,repay_item_type=1},{trade_time=2018-06-19 14:32:39,transfer_time=2018-06-19 14:32:39,trade_amount=0,transfer_amount=0,fee=0,cooperator_item_id=HK18062014284300019736677,cooperator_transfer_item_id=HK18062014284300019736677,repay_item_type=2}]repayMode=2transcode=031userId=1000348version=010086c7dc3c"));
//		10dfb52480dac9a7f1caa971d705fa74
//		c7650823be78a835593a6c15068b8ee1
//		System.out.println(DigestUtils.md5Hex("46541631631 sdf")); 
	}*/
}
