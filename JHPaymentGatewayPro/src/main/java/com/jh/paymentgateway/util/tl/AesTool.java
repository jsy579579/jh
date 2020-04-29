package com.jh.paymentgateway.util.tl;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Linwei on 2018/3/19.
 */
public class AesTool {


    private static int length=128;
    /**
     * 加密
     *
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static String encrypt(String content, String password) {
        return StringUtils.trim(Base64.encodeBase64String(AESUtil.encrypt(content, password)));
    }

    /**
     * 解密
     *
     * @param content
     *            待解密内容
     * @param password
     *            解密密钥
     * @return
     */
    public static String decrypt(String content, String password){
        try {
			return new String(AESUtil.decrypt(Base64.decodeBase64(content.getBytes()), password),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return null;
    }

//  /**
//   * 将二进制转换成16进制
//   *
//   * @param buf
//   * @return
//   */
//  public static String parseByte2HexStr(byte buf[]) {
//      StringBuffer sb = new StringBuffer();
//      for (int i = 0; i < buf.length; i++) {
//          String hex = Integer.toHexString(buf[i] & 0xFF);
//          if (hex.length() == 1) {
//              hex = '0' + hex;
//          }
//          sb.append(hex.toUpperCase());
//      }
//      return sb.toString();
//  }
//
//  /**
//   * 将16进制转换为二进制
//   *
//   * @param hexStr
//   * @return
//   */
//  public static byte[] parseHexStr2Byte(String hexStr) {
//      if (hexStr.length() < 1)
//          return null;
//      byte[] result = new byte[hexStr.length() / 2];
//      for (int i = 0; i < hexStr.length() / 2; i++) {
//          int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
//          int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
//                  16);
//          result[i] = (byte) (high * 16 + low);
//      }
//      return result;
//  }

    /**
     * 加密
     *
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static byte[] encrypt2(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/Pkcs5Padding");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}