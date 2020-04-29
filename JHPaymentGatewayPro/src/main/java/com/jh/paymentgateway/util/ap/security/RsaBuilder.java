package com.jh.paymentgateway.util.ap.security;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RsaBuilder {

    public enum Country {
                         /** 中国 */
        CN
    }

    private static final String SUBJECT_TEMPLE      = "C=%s,ST=%s,L=%s,O=%s,OU=%s,CN=%s,E=%s";
    private static final String ALGORITHM           = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    /** 证书发布机构 */
    private static String       ISSUER              = "C=CN,ST=GuangDong,L=Shenzhen,O=万融,OU=万融,CN=万融 License CA,E=service@colotnet.com";
    /** 证书默认有效期:单位月 */
    public static Integer       VALID_MONTH         = 12;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成证书
     * 
     * @param alias 证书别名
     * @param keySize 证书长度
     * @param country 证书国家
     * @param province 证书省
     * @param city 证书市
     * @param owner 证书拥有者
     * @param organization 证书机构
     * @param cerDescribe 证书描述
     * @param email 证书邮件
     * @param serial 证书序列号
     * @param validStart 证书有效期 开始时间
     * @param validEnd 证书有效期 结束时间
     * @param privateKeyPassword 私钥密码
     * @throws Exception
     */
    public static String createRsaCertificate(String alias, int keySize, Country country, String province, String city,
                                              String owner, String organization, String cerDescribe, String email,
                                              BigInteger serial, Date validStart, Date validEnd,
                                              String privateKeyPassword) throws Exception {
        String subject = String.format(SUBJECT_TEMPLE, country, province, city, owner, organization, cerDescribe,
                                       email);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Certificate cert = generateV3(ISSUER, subject, serial, validStart, validEnd, keyPair.getPublic(),
                                      keyPair.getPrivate(), null);
        return getPFXKeyStr(KeyStore.getInstance("PKCS12"), alias, privateKeyPassword, keyPair, cert);
    }

    /**
     * @param alias 证书别名
     * @param keySize 证书长度
     * @param country 证书国家
     * @param province 证书省
     * @param city 证书市
     * @param owner 证书拥有者
     * @param organization 证书机构
     * @param cerDescribe 证书描述
     * @param email 证书邮件
     * @param serial 证书序列号
     * @param month 有效期月 (1 为 1个月)
     * <p>
     * 证书有效期为：开始日期(当天) 结束日期(1为1个月 以此类推)
     * </p>
     * @param privateKeyPassword 私钥密码
     * @throws Exception
     */
    public static String createRsaCertificateByMonth(String alias, int keySize, Country country, String province,
                                                     String city, String owner, String organization, String cerDescribe,
                                                     String email, BigInteger serial, int month,
                                                     String privateKeyPassword) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + month);
        return createRsaCertificate(alias, keySize, country, province, city, owner, organization, cerDescribe, email,
                                    serial, new Date(), calendar.getTime(), privateKeyPassword);
    }

    public static String createRsaCertificateByMonth(String alias, Country country, String province, String city,
                                                     String owner, String organization, String cerDescribe,
                                                     String email, BigInteger serial, int month,
                                                     String privateKeyPassword) throws Exception {
        return createRsaCertificateByMonth(alias, 2048, country, province, city, owner, organization, cerDescribe,
                                           email, serial, month, privateKeyPassword);
    }

    public static String createRsaCertificateByMonth(Country country, String province, String city, String owner,
                                                     String organization, String cerDescribe, String email,
                                                     BigInteger serial, int month,
                                                     String privateKeyPassword) throws Exception {
        return createRsaCertificateByMonth("alias", country, province, city, owner, organization, cerDescribe, email,
                                           serial, month, privateKeyPassword);
    }

    public static String createRsaCertificate(Country country, String province, String city, String owner,
                                              String organization, String cerDescribe, String email, BigInteger serial,
                                              Date validStart, Date validEnd,
                                              String privateKeyPassword) throws Exception {
        return createRsaCertificate("alias", 2048, country, province, city, owner, organization, cerDescribe, email,
                                    serial, validStart, validEnd, privateKeyPassword);
    }

    public static Certificate generateV3(String issuer, String subject, BigInteger serial, Date notBefore,
                                         Date notAfter, PublicKey publicKey, PrivateKey privKey,
                                         List<Extension> extensions) throws Exception {
        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuer), serial, notBefore,
                                                                           notAfter, new X500Name(subject), publicKey);
        ContentSigner sigGen = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider("BC").build(privKey);
        if (extensions != null) for (Extension ext : extensions) {
            builder.addExtension(new ASN1ObjectIdentifier(ext.getOid()), ext.isCritical(),
                                 ASN1Primitive.fromByteArray(ext.getValue()));
        }
        X509CertificateHolder holder = builder.build(sigGen);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is1 = new ByteArrayInputStream(holder.toASN1Structure().getEncoded());
        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
        is1.close();
        return theCert;
    }

    private static String getPFXKeyStr(KeyStore store, String alias, String privateKeyPassword, KeyPair keyPair,
                                       Certificate cert) throws Exception {
        store.load(null, null);
        store.setKeyEntry(alias, keyPair.getPrivate(), privateKeyPassword.toCharArray(), new Certificate[] { cert });
        cert.verify(keyPair.getPublic());
        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        store.store(byteArrayOutput, privateKeyPassword.toCharArray());
        try {
            byteArrayOutput.flush();
            byteArrayOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encode(byteArrayOutput.toByteArray());
    }

    public static void main(String[] args) throws Exception {
        String str = createRsaCertificateByMonth(Country.CN, "GuangDong", "Shenzhen", "test", "测试", "test",
                                                 "123456789@qq.com", BigInteger.ZERO, 6, "123456");
        System.out.println("key file:" + str);
        PrivateKey privateKey = (PrivateKey)RsaUtil.getPrivateKey(str, "123456");
        System.out.println(privateKey.toString());



    }

    public class Extension {

        private String  oid;
        private boolean critical;
        private byte[]  value;

        public String getOid() {
            return oid;
        }

        public byte[] getValue() {
            return value;
        }

        public boolean isCritical() {
            return critical;
        }
    }

}
