package com.jh.paymentgateway.util.kb;

import java.lang.ref.WeakReference;

import org.apache.commons.codec.binary.Base64;

/**
 * TODO Add comments.
 *
 * @author songx
 * @date 2018/10/22.
 */
public class AllinpayUtils {
    String s = "";
    String pk = "";
    String prk = "";

    public AllinpayUtils() {
    }

    public String getKeyByOrganId(String organId) {
        WeakReference<Md5Util> wfBean = new WeakReference(new Md5Util());
        Md5Util md5Util = (Md5Util)wfBean.get();
        return md5Util.getkeyBeanofStr(organId);
    }

    public String encryptByPrivateKey(String params, String privatekey) throws Exception {
        byte[] data = params.getBytes();
        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privatekey);
        Base64 base64 = new Base64();
        String cipherTextBase64 = base64.encodeToString(encodedData);
        return cipherTextBase64;
    }

    public String decryptByPublicKey(String encodedData, String publickey) throws Exception {
        Base64 base64 = new Base64();
        byte[] decodedData = RSAUtils.decryptByPublicKey(base64.decode(encodedData), publickey);
        String target = new String(decodedData);
        return target;
    }

    public String sign(String params, String prk) throws Exception {
        String sign = RSAUtils.sign(params.getBytes(), prk);
        return sign;
    }

    public boolean verify(String params, String publickey, String sign) throws Exception {
        boolean b = RSAUtils.verify(params.getBytes(), publickey, sign);
        return b;
    }
}
