package com.jh.paymentgateway.util.kq;

public interface AsapService {
    /**
     * @Title: signAndEncrypt
     * @Description: 加密并签名
     * @param memberCode 商户会员号
     * @param data 原始报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @param data 原始报文的签名，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @return 签名是否正确
     **/
    String signAndEncrypt(String memberCode, byte[] originalData) throws Exception;

    /**
     * @Title: decryptMerchantMsgAndVerifySignature
     * @Description: 解密商户报文并验签
     * @param memberCode * 商户会员号
     * @param encryptedData 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @param encryptedData * 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @param encryptedData 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @return 解密过的报文
     */
    String decryptMerchantMsgAndVerifySignature(String memberCode, String encryptedData, String envelope,
            String signature) throws Exception;
}
