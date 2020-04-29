package com.jh.paymentgateway.util.ryt;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;


/**
 * @author:Ivan
 * @version Revision 1.0.0
 * @see:
 * @创建日期：2016-12-11
 * @功能说明：密钥工具类
 * @begin
 * @修改记录:
 * @修改后版本          修改人      	修改内容
 * @2016-12-11  	         Ivan        	创建
 * @end
 */
public class PlatKeyGenerator {

	 /** *//** 
     * 加密算法RSA 
     */  
    public static final String KEY_ALGORITHM = "RSA";  
//    private static final String RSANOPADDING = "RSA/ECB/NoPadding";
    
//    public static final String KEY_ALGORITHM = "RSA/None/PKCS1Padding";
      
    /** *//** 
     * 签名算法 
     */  
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";  
  
    /** *//** 
     * 获取公钥的key 
     */  
    private static final String PUBLIC_KEY = "RSAPublicKey";  
      
    /** *//** 
     * RSA最大加密明文大小 
     */  
    private static final int MAX_ENCRYPT_BLOCK = 117;  
      
    /** *//** 
     * RSA最大解密密文大小 
     */  
    private static final int MAX_DECRYPT_BLOCK = 128;  
  
  
    /** *//** 
     * <p> 
     * 校验数字签名 
     * </p> 
     * @param data 已加密数据 
     * @param publicKey 公钥(BASE64编码) 
     * @param sign 数字签名 
     *  
     * @return 
     * @throws Exception 
     *  
     */  
    public static boolean verify(byte[] data, String publicKey, String sign)  
            throws Exception {  
        byte[] keyBytes = PlatBase64Utils.decode(publicKey);  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        PublicKey publicK = keyFactory.generatePublic(keySpec);  
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initVerify(publicK);  
        signature.update(data);  
        return signature.verify(PlatBase64Utils.decode(sign));  
    }  
  
    /** *//** 
     * <p> 
     * 公钥解密 
     * </p> 
     *  
     * @param encryptedData 已加密数据 
     * @param publicKey 公钥(BASE64编码) 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)  
            throws Exception {  
        byte[] keyBytes = PlatBase64Utils.decode(publicKey);  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicK = keyFactory.generatePublic(x509KeySpec);  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, publicK);  
        int inputLen = encryptedData.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段解密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {  
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_DECRYPT_BLOCK;  
        }  
        byte[] decryptedData = out.toByteArray();  
        out.close();  
        return decryptedData;  
    }  
  
    /** *//** 
     * <p> 
     * 公钥加密 
     * </p> 
     *  
     * @param data 源数据 
     * @param publicKey 公钥(BASE64编码) 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPublicKey(byte[] data, String publicKey)  
            throws Exception {  
        byte[] keyBytes = PlatBase64Utils.decode(publicKey);  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicK = keyFactory.generatePublic(x509KeySpec);  
        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, publicK);  
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {  
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_ENCRYPT_BLOCK;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
    }  
  
    /** *//** 
     * <p> 
     * 获取公钥 
     * </p> 
     *  
     * @param keyMap 密钥对 
     * @return 
     * @throws Exception 
     */  
    public static String getPublicKey(Map<String, Object> keyMap)  
            throws Exception {  
        Key key = (Key) keyMap.get(PUBLIC_KEY);  
        return PlatBase64Utils.encode(key.getEncoded());  
    } 
    
    /*public static void main(String[] args) {
    	  try {
    	        
    	        String pub_key=SampleConstant.T0_PUB_KEY;
    	        
//    	        pub_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOwHy3T2GiD04+O+l8lfKkYYBAMUs2cWFsa2eJ3HyR2cyggJqmjXQuXCRO+/JXkvwrWNdqKou3shC78JCTx6bmQzgGEOxYmn3W0+Fx8KWpUVdm1hp0naNTgvWM0JH7I258DxZukBHQHju+e8J325kWRkIJGLnX4bwC4Aa8/FQykwIDAQAB";
//    	       
//    	        System.out.println("pub_key-->"+pub_key);
//    	        String encryptedData="HOH0DmU8f/uHBQYD7gj+Eg2pHIz5V6BYuNpqovaR0t36C9UXqgNxc7Tbd96h0rkTSF3vE7EvL4IxvtDfdEperVrqW2U/K1EHykPJE1rXWQgHk1dadMmtZTkF12jiv8wnMjkZOyWyhTT+Dv8doPq+L78IFaLty4aK5f19hIYZfiFiN34O+6/Jgg5zgfHS1BC6XuvPrYcu2V4JqS6nsGRwRJTPXMgzRRonsXnKJAdZRJBn/rjyRXUybNFTt2Ks5NtfETvfKczh9iA023ewdWFQKgK6rbbr20BdDjvoX866qZJP6NJ2wEagXx/JQph/T8ANsYbNrwFwArxITVn0EQY/rXdv5YQg2R69GGi8R+C/9pRQ1TCifTiXwrEGNVy2SarnMqpnCFtusNO5YCzrFYI62RIeQvPM0nq/B7yjerxaN0GiIz5cDruAyxzT9x3HcSioPIBuGMPaAUct4mmWbvWYLJ31iRIgOklvXdqLij8kUG8S+5v7p/cMF0lZdpnalOWPYwHNyZCnRzyngWoDs/JDbFLA+1SONFifK78zRDryKptyELERZSaAs3LqEvdgaaCNV+nk1dyHrLjnhx1zyoQqErBHre3523cgMmffVkfpwtQdHCc6fiUEkE/u35LPrzMobkkIf9AQg8HsygOP4e0xp7KzbP/AcvKCpWzKE+xkJUQL9LyabxVqGBmrqun7T9Qu8kvIZu9wMo7zTfhOZ3H2C9vnsmrXLDnjVptQHrfwd+0E8fIEgdRRg/eZFM1T+nBTEEbP88Ai2tclGF33l0RHafHbeWJKnm1VPbs0nCjwKGjw5+kl3me1xlSTYKEliB+ZOMecDc2n38nmXUFFDe5zaCs1nVywEKClmdyM1ef7tVIbBdxEFRXmLWHILqrIoL1pilTd98Zsh/kVVwji9Mch4igCh4vnw5toTU6EZ6A748t3/3qweSMw/yEwM/x8f9C10fu2PIqdZ20TUZlVZtHTurQMNg2u+aabD6d/zEkZHnRSZ20D9pitADc7Hk3j3aW4UAMLatXOGow+g8KwyNSbz+sqjkqo3h9efyQ6fZMdOgjL5ZLCGYiv/jUBc7laHyoKiMjE8hiQ3Sl0n0WJroAY1w/kN2VcbLyjWFuEJZV/krKhwHYTLs2+Gh/aRmRUfjVJMCqqY6/gCE9ZwFwPvZ0z1kQcVNKByB1eSDhe2rs2IrM=";
    	       
    	        
    	        String encryptedData= "AHnikrLjcUeG8vISetHXtkQ85q3edIDfVynsm/B91ZA44hUjf1nA8u6uh2vG0UwHtDBdLwIZyXsXHmLldLw5uLa16Jj0Sw1exxX7pRF3ZHEI63XAQLimu3LyWTKELaEG5+Vgmo+ci8CHF0nJP+mDrLPfC1TOvyLrRwMhRaeNJsIt+wQACqggJE4v4gWfpVXPY8qrrQQSCEaW/RMI9PG6nKKVGkl5ZPEnNZQIyVpDIBvmm0sQVoDZqfA4UsugKBmTYE6509zvMmKxwj/xS++OL5btxrRjNt4GpmokLqk51w/yQSWVnSCkBlOX6DppdqwbTcbX7+p+bZtWOGYOce2OawoHtvE7gnpyvs78DMhyZCDK1jc9bGyMd3BK4BRHC4fuz15hU5g7vMBmYSbv5tdFrMtJS77spFsMHuUQx5TNT6jMr2FmvKmgr/oOQ00QnJ2bXjx99t5NfbjpopxM0DuipLkJrXAS6RHJgg7zDC+G6U1kQWBzw2adEuNVdfkRpBhtUpHOJaGPhMqDhiakSnpsCkcTDaaRMSBoC+8ZZVhWH0JkXTe2WC7meClsKJ9ked6c+WfoLX0CkPJFwZqIjuH0LaaJRWBNEEZKtudtvrF4QFJWofDTZ5KiwE1cEpUEtCoUO4m540K0DBZxI/UJgV8qaE3NsJH+dTTMFjATBZHMkiVSW1KWgEfmj2r1vLMXS71ZR1pJAt5JxvtV3DPnlrEktTMApW6MQPMRL8BiIE4J9yugvosa+z9BScqvcYTyAtUL8H7gMwsOGh+y1jgnca9aIAPJlUeRiOduxqB3rhwDVyMZuYKfiVXyuLrn4k3r479i8pElS8F8mj9AgP1+5JP5AkIjet41FDqIrQDAIlfS5OsmYhqPnO2PYHKSwZ1ubFuhO4AtMaC81Cryv/wkeZTQFBBZA+hHVuxSJsfgK/yKQyMMi3saBl7EYjOFqJ1HhtjJDuVlOeLg1MjasNu1ITAdOoAD2Ow3stPAXvc6glGbAZZgDB8p0sGrBr8i0VRZwbiOHQdSSF8boTNzcXz96GMzLQnJrXEZvhGFAnoQlO5sFVKhPRm+OCKzFSWiNA51vdEGWKYPSNgv/DvxXjR6G7xOonKWg1+QgRZsuNAhRc0Q+S+zWgOqQGOLpsRoEW/potOsISAwMmPmOVhRJVei8FppVWbR94auywRDNk1twOG7huAQwlWBWI+R++z7tPxKMrTTIh2rBKFMsM1a1r8Cdkr1Yz8mkOnvKK+Rw+oNpv6oiqO3ofxQ9mjb5G/l+O5WEsexHE1+FplN2jS/0ZDVuFywvKCrf3iZWybA6nlNKifIHe4C0WxszUssFA79BaD+Y1n5F97I2h7Ox1/G9cpKZaHxTw==";
    	      
    	        
//    	        String encryptedData = "ZwXduTvfiJYVmtGGdQGpKIUN9tWJEB5NzkC/hIoKeiQBcd989gAthmDKxwQWFwuYyUF2iSyaY2ZFzRIWrJhSUoFnXnfrj7ZXmBXhwXPtpCfpixja2MPLZLDusoVAiqHeAVuUG/7+oucmIazFMmc8f+ZQXAVJebWLn+GNDRAdpDhM6Jm2eFE6WrOXg/LsO39xjVZ0BKNdh2c6pxETmxfAXxxj6f4JwIiZ7SSNjCi5rCAUavHnqdUvm15eJ4BKGS11iFzerZVeT572aw2kqftxGK+WvmNKJUvsZ2E7DKAx1g3PE7BbcR5sFIEEMXtPveFsYqImM+EqY7Efi0Y7mEK05ht8FEmYbZzFXLrMBTa9NlVxG8updc7eH2YvtD08ZQjUqgfMM17ERIK/Z+wevnMD87cwZ7IrIFaTpaYfqC7OWqqAaYe+IA9sESmThkRHZ0A5cjr81K8phtssDfIUcrCtHG8N8jLK6ko36fvpcVXNYDiHtq0gygfsp4QruhGMG1BZjpVo+0zzEnKqayDLbU6VfTCAMYu6dCtQp5dmIbJkHvLYCqxmJ+uJntfvY4b28CN8Zt3QC/uocv+Dn6QnubKUx/FXxqCQK0OHogdE5e3b0Al25+wpF7hMqkRoQULrEhRTibi8Ae9lEorWh4+nAv6zEtfZvEmDOcONMhmzEbJ8J28LKOP/okC8pvDxs3cDujG55nHLJnovK8DjkZ2c7zDVGgQROJqo63Ocyfyau/QesAd/n8Kzk4RwghFehHIsPPWAgYLbDHv7sBWFb/fcRXrGdMhgVgzPgw1gR2TV+qpg4uU5Lnq6wieFq3nEviatkj45SQxhgpYhF5igGCT/+KKkDTNS8EjzRq1wBaWg3XN/rzsejAfi6/LvhyAhPIGwCS45DkVS3oMi+XYhVAv/EsNZSRscbj87c/szg4CSM0GZie+91ezMdrje9gz6++31uEyWC5LRICWDN+6T31mjdCuyoRezao8lsKakrHcfwMcZ0gh7ihWTGAa19oCqkzvzcOzTbyIDEpz3xSkApPVwwx9i46j9yvHtYDysfcu0uUmsjQPEE13Dk416zgODmqlAaBCUywFArfhsSdPtqpFcw2VdgSypNly3b2tbiTDyRdQQu8XcZsPPx4h2Dlid7sWPnllJq2S+XomZNWl7MZwgfAgLtTJ542StomM17Cr+S/wo9wI=";
//    	        System.out.println("encryptedData-->"+encryptedData);
    	        
    	        //解密
    	        byte[] decryptData = decryptByPublicKey(PlatBase64Utils.decode(encryptedData), pub_key);
//    	        System.out.println(new String(decryptData));
    	        
    	        
    	        String str = new String(decryptData);
    	        
    	        System.out.println(str);
    	        
    	        String sign = "O7gT60BUw7aKg6B0/EFUT5bDEzALbzr93+LkEydaAsGXXoiC6mDj3MWhg2mqHBQKjtdTpjTpMhCwrKPU581Xq6CkDfISeqp7uYJtDbFbC22hEUfAoTA9pfVxG8tEFayV+XgHISUCv+fNnhFI4mdM0OSTWrGNO8casNlD3qA3tHI=";
    	        
//    	        String sign = "EmRnDbS+TECp0wUzlvYpJHAMZodpE31LQB3rGYxEQpoJ+q9okL3nDMUUtJGDPZYc+mntrzea3esEcACUdsdMcfd98yMyMnMBdQnkUsAogFYtpU2Z1oxvRar2/OoXVWtu3okbnlYuava7SPKOkFVxnW4n6Aq3kQEHzfPxkdwlnbI=";
//    	        System.out.println("sign-->signature-->"+sign);
    	        
    	        boolean result = PlatKeyGenerator.verify(decryptData, pub_key, sign);
//    	        System.out.println(result);
    	        
    	        String returnMsg = "failed";
    	        if(result){
    				returnMsg = "success";
    				
    				Map<String,String> resultMap = JSON.parseObject(new String(decryptData), Map.class);
    				
    				 System.out.println(resultMap.get("respCode"));
    				for(Entry<String, String> entry : resultMap.entrySet()){
    					System.out.println(entry.getKey() + "|" + entry.getValue());
    				}
    			}else{
    				System.out.println("验签失败");
    			}
    	        
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    	}*/
}
