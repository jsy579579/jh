package com.jh.paymentchannel.util.abroad;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

    /**
     * ���� md5ժҪ�ֽ�
     *
     * @param input
     * @return
     */
    private static byte[] digest(String input) {
        return digest(input, Charset.forName("UTF-8"));
    }

    private static byte[] digest(String input, Charset charset) {
        try {
            // �õ�һ��MD5ת�����������ҪSHA1����ɡ�SHA1����
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // ������ַ�ת�����ֽ�����
            byte[] inputByteArray = input.getBytes(charset);
            // inputByteArray�������ַ�ת���õ����ֽ�����
            messageDigest.update(inputByteArray);
            // ת�������ؽ��Ҳ���ֽ����飬��16��Ԫ��
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * @param input
     * @return
     * @Description: 32λСдMD5
     */
    public static String digest32Lower(String input) {
        String digestVal = null;
        byte[] bytes = digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int bt = b & 0xff;
            if (bt < 16) {
                sb.append(0);
            }
            sb.append(Integer.toHexString(bt));
        }
        digestVal = sb.toString();

        return digestVal;
    }

    /**
     * @param input
     * @return
     * @Description: 32λ��дMD5
     */
    public static String digest32Upper(String input) {
        String reStr = digest32Lower(input);
        if (reStr != null) {
            reStr = reStr.toUpperCase();
        }
        return reStr;
    }

    /**
     * @param input
     * @return
     * @Description: 16λСдMD5
     */
    public static String digest16Lower(String input) {
        String reStr = digest32Lower(input);
        if (reStr != null) {
            reStr = reStr.toUpperCase().substring(8, 24);
        }
        return reStr;
    }

    /**
     * @param input
     * @return
     * @Description: 16λ��дMD5
     */
    public static String digest16Upper(String input) {
        String reStr = digest32Upper(input);
        if (reStr != null) {
            reStr = reStr.substring(8, 24);
        }
        return reStr;
    }
}
