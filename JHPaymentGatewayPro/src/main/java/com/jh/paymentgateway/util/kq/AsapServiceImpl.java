package com.jh.paymentgateway.util.kq;

import java.util.HashMap;
import java.util.Map;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import com.alibaba.fastjson.JSON;
import com.bill99.asap.service.ICryptoService;
import com.bill99.asap.service.impl.CryptoServiceFactory;
import com.bill99.schema.asap.commons.Mpf;
import com.bill99.schema.asap.data.SealedData;
import com.bill99.schema.asap.data.UnsealedData;


public class AsapServiceImpl implements AsapService {

    /**
     * 加密并签名
     * 
     * @param memberCode 商户会员号
     * @param originalData 原始报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制
     * @return 要发送给快钱报文示例，其中二进制的原文和签名已经用Base64编码
     */
    public String signAndEncrypt(String memberCode, byte[] originalData) throws Exception {
        // 建商户功能标识对象
        Mpf mpf = new Mpf();
        mpf.setMemberCode(memberCode);
        mpf.setFeatureCode("F1001_1");
        // 从CryptoServiceFactory中获取CryptoService实例
        ICryptoService service = CryptoServiceFactory.createCryptoService();
        // 调用ICryptoService的seal方法对数据加密并签名
        SealedData sealedData = service.seal(mpf, originalData);
        Map<String, String> result = new HashMap<String, String>();
        BASE64Encoder encoder = new BASE64Encoder();
        result.put("envelope", encoder.encodeBuffer(sealedData.getDigitalEnvelope())); // 加密后的数字信封
        result.put("encryptedData", encoder.encodeBuffer(sealedData.getEncryptedData())); // 加密后的数据
        result.put("signature", encoder.encodeBuffer(sealedData.getSignedData())); // 签名后的数据
        return JSON.toJSONString(result);
    }

    /**
     * 解密商户报文并验签
     * 
     * @param memberCode * 商户会员号 *
     * @param encryptedData * 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制 *
     * @param encryptedData * 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制 *
     * @param encryptedData * 加密过的报文，字节数组形式。如果你的报文是String，请先getBytes转为二进制 *
     * @return 解密过的报文
     */
    public String decryptMerchantMsgAndVerifySignature(String memberCode, String encryptedData, String envelope,
        String signature) throws Exception {
        // 构建一个MPF
        Mpf mpf = new Mpf();
        mpf.setMemberCode(memberCode);
        mpf.setFeatureCode("F1001_1");
        // 构建SealedData对象
        BASE64Decoder decoder = new BASE64Decoder();
        SealedData sealedData = new SealedData();
        sealedData.setDigitalEnvelope(decoder.decodeBuffer(envelope));
        sealedData.setEncryptedData(decoder.decodeBuffer(encryptedData));
        sealedData.setSignedData(decoder.decodeBuffer(signature));
        // 获取CryptoService的实例，调用unseal方法进行解密/验签
        ICryptoService service = CryptoServiceFactory.createCryptoService();
        UnsealedData unsealedData = service.unseal(mpf, sealedData);
        // 解密后的数据和验签结果
        boolean verifySignResult = unsealedData.getVerifySignResult();
        byte[] decryptedData = unsealedData.getDecryptedData();
        System.out.println("verifySignResult=" + verifySignResult + ",decryptedData=" + new String(decryptedData));
        return new String(decryptedData);
    }

}
