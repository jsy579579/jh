package com.jh.paymentgateway.util.df.ybgjdf;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import java.util.Enumeration;


public class RSAKey {
	
	
	private static final String keyAlgorithm ="RSA";

	private final static byte[] hex = "0123456789ABCDEF".getBytes();
	
    public static PublicKey getRSAPublicKeyByRelativeFileSuffix(String relativePath, String fileSuffix)
	{
		return getRSAPublicKeyByRelativeFileSuffix(relativePath, fileSuffix, keyAlgorithm);
	}
    
	public static PublicKey getRSAPublicKeyByRelativeFileSuffix(String relativePath, String fileSuffix,
			String keyAlgorithm) {

		Resource r = new ClassPathResource(relativePath);
		File f;
		String filePath = "";
		try {
			f = r.getFile();
			filePath = f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return getRSAPublicKeyByAbsoluteFileSuffix(filePath, fileSuffix, keyAlgorithm);

	}
	
	public static PublicKey getRSAPublicKeyByAbsoluteFileSuffix(String filePath, String fileSuffix)
	{
		return getRSAPublicKeyByAbsoluteFileSuffix( filePath,  fileSuffix,  keyAlgorithm);
	}
	
	

	
	/**
	 * 获取RSA公钥对象
	 * 
	 * @param filePath
	 *            RSA公钥路径
	 * @param fileSuffix
	 *            RSA公钥名称，决定编码类型
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA公钥对象
	 * @throws SecurityRteException
	 */
	public static PublicKey getRSAPublicKeyByAbsoluteFileSuffix(String filePath, String fileSuffix, String keyAlgorithm)  {
		InputStream in = null;
		String keyType = "";
		if ("crt".equalsIgnoreCase(fileSuffix) || "txt".equalsIgnoreCase(fileSuffix) ||"cer".equalsIgnoreCase(fileSuffix)) {
			keyType = "X.509";
		} else if ("pem".equalsIgnoreCase(fileSuffix)) {
			keyType = "PKCS12";
		} else if(("yljf").equalsIgnoreCase(fileSuffix)){
			keyType = "yljf";
		} else{
			keyType = "PKCS12";
		}

		try {
			in = new FileInputStream(filePath);
			PublicKey pubKey = null;
			if ("X.509".equals(keyType)) {
				CertificateFactory factory = CertificateFactory.getInstance(keyType);
				Certificate cert = factory.generateCertificate(in);
				pubKey = cert.getPublicKey();
			} else if ("PKCS12".equals(keyType)) {
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					if (readLine.charAt(0) == '-') {
						continue;
					} else {
						sb.append(readLine);
						sb.append('\r');
					}
				}
				X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(sb.toString()));
				KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
				pubKey = keyFactory.generatePublic(pubX509);
			}else if("yljf".equals(keyType)){
				BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
				String s = br.readLine();
				ASN1InputStream ain = new ASN1InputStream(hexString2ByteArr(s));
				RSAPublicKeyStructure pStruct = 	RSAPublicKeyStructure.getInstance(ain.readObject());
				RSAPublicKeySpec spec = new 	RSAPublicKeySpec(pStruct.getModulus(), 			pStruct.getPublicExponent());
				KeyFactory kf = KeyFactory.getInstance("util.RSA");
				if (in != null)
				{
					in.close();
				}

				return kf.generatePublic(spec);
			}

			return pubKey;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("公钥路径文件不存在",e);
		} catch (CertificateException e) {
			throw new RuntimeException("公钥路径文件不存在",e);
		} catch (IOException e) {
			throw new RuntimeException("读取公钥异常",e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("生成密钥工厂时没有[%s]此类算法",e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("生成公钥对象异常",e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static PrivateKey getRSAPrivateKeyByRelativePathFileSuffix(String relativePath, String fileSuffix)
	{
		return getRSAPrivateKeyByRelativePathFileSuffix( relativePath,  fileSuffix,  null);
	}
	
	
	public static PrivateKey getRSAPrivateKeyByRelativePathFileSuffix(String relativePath, String fileSuffix, String password)
	{
		return getRSAPrivateKeyByRelativePathFileSuffix( relativePath,  fileSuffix,  password,  keyAlgorithm);
	}
	
	public static PrivateKey getRSAPrivateKeyByRelativePathFileSuffix(String relativePath, String fileSuffix, String password, String keyAlgorithm){
		Resource r = new ClassPathResource(relativePath);
		File f;
		String filePath = "";
		try {
			f = r.getFile();
			filePath = f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return  getRSAPrivateKeyByAbsoluteFileSuffix( filePath,  fileSuffix,  password,  keyAlgorithm);
	}
	
	
	
	public static PrivateKey getRSAPrivateKeyByAbsoluteFileSuffix(String filePath, String fileSuffix){
		return getRSAPrivateKeyByAbsoluteFileSuffix( filePath,  fileSuffix,  null);
	}
	
	public static PrivateKey getRSAPrivateKeyByAbsoluteFileSuffix(String filePath, String fileSuffix, String password){
		return getRSAPrivateKeyByAbsoluteFileSuffix( filePath,  fileSuffix,  password,  keyAlgorithm);
	}
	
	/**
	 * 获取RSA私钥对象
	 * 
	 * @param filePath
	 *            RSA私钥路径
	 * @param fileSuffix
	 *            RSA私钥名称，决定编码类型
	 * @param password
	 *            RSA私钥保护密钥
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA私钥对象
	 * @throws SecurityRteException
	 */
	public static PrivateKey getRSAPrivateKeyByAbsoluteFileSuffix(String filePath, String fileSuffix, String password, String keyAlgorithm)
			 {
		String keyType = "";
		if ("keystore".equalsIgnoreCase(fileSuffix)) {
			keyType = "JKS";
		} else if ("pfx".equalsIgnoreCase(fileSuffix) || "p12".equalsIgnoreCase(fileSuffix)) {
			keyType = "PKCS12";
		} else if ("jck".equalsIgnoreCase(fileSuffix)) {
			keyType = "JCEKS";
		} else if ("pem".equalsIgnoreCase(fileSuffix) || "pkcs8".equalsIgnoreCase(fileSuffix)) {
			keyType = "PKCS8";
		} else if ("pkcs1".equalsIgnoreCase(fileSuffix)) {
			keyType = "PKCS1";
		} else if ("yljf".equalsIgnoreCase(fileSuffix)) {
			keyType = "yljf";
		} else if ("ldys".equalsIgnoreCase(fileSuffix)) {
			keyType = "ldys";
		} else{
			keyType = "JKS";
		}

		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			PrivateKey priKey = null;
			if ("JKS".equals(keyType) || "PKCS12".equals(keyType) || "JCEKS".equals(keyType)) {
				KeyStore ks = KeyStore.getInstance(keyType);
				if (password != null) {
					char[] cPasswd = password.toCharArray();
					ks.load(in, cPasswd);
					Enumeration<String> aliasenum = ks.aliases();
					String keyAlias = null;
					while (aliasenum.hasMoreElements()) {
						keyAlias = (String) aliasenum.nextElement();
						priKey = (PrivateKey) ks.getKey(keyAlias, cPasswd);
						if (priKey != null){
							break;
						}

					}
				}
			}else if("yljf".equals(keyType)){
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String s = br.readLine();
				PKCS8EncodedKeySpec priPKCS8=new PKCS8EncodedKeySpec(hexStrToBytes(s));
				KeyFactory keyf=KeyFactory.getInstance("util.RSA");
				PrivateKey myprikey=keyf.generatePrivate(priPKCS8);
				return myprikey;
			}else if("ldys".equals(keyType)){
				byte[] b = new byte[20480];
				in.read(b);
				PKCS8EncodedKeySpec priPKCS8=new PKCS8EncodedKeySpec(b);
				KeyFactory keyf=KeyFactory.getInstance("util.RSA");
				PrivateKey myprikey=keyf.generatePrivate(priPKCS8);
				return myprikey;
			}else {
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					if (readLine.charAt(0) == '-') {
						continue;
					} else {
						sb.append(readLine);
						sb.append('\r');
					}
				}
				if ("PKCS8".equals(keyType)) {
					PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(sb.toString()));
					KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
					priKey = keyFactory.generatePrivate(priPKCS8);
				} else if ("PKCS1".equals(keyType)) {
//					RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(sb.toString().getBytes()));
					RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(Base64.decodeBase64(sb.toString())));
					KeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
					KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
					priKey = keyFactory.generatePrivate(rsaPrivKeySpec);
				}
			}

			return priKey;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("私钥路径文件不存在",e);
		} catch (KeyStoreException e) {
			throw new RuntimeException("获取KeyStore对象异常",e);
		} catch (IOException e) {
			throw new RuntimeException("读取私钥异常",e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("生成私钥对象异常",e);
		} catch (CertificateException e) {
			throw new RuntimeException("加载私钥密码异常",e);
		} catch (UnrecoverableKeyException e) {
			throw new RuntimeException("生成私钥对象异常",e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("生成私钥对象异常",e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	
	public static  byte[] hexString2ByteArr(String hexStr) {
		return new BigInteger(hexStr, 16).toByteArray();
	}
	public static final byte[] hexStrToBytes(String s) {
		byte[] bytes; 
		bytes = new byte[s.length() / 2];
		for (int i = 0; i < bytes.length; i++) { 
			bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * 		i + 2), 16);
		} 
		return bytes;
	}
	/**
	 * 字符数组16进制字符
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytes2Str(byte[] bytes, int radix) {
		int size = 2;
		if (radix == 2) {
			size = 8;
		}
		StringBuilder sb = new StringBuilder(bytes.length * size);
		for (int i = 0; i < bytes.length; i++) {
			int integer = bytes[i];
			while (integer < 0) {
				integer = integer + 256;
			}
			String str = Integer.toString(integer, radix);
			sb.append(StringUtils.leftPad(str.toUpperCase(), size, "0"));
		}
		return sb.toString();
	}

	public static String bytes2HexString(byte[] b) {
		byte[] buff = new byte[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
			buff[2 * i + 1] = hex[b[i] & 0x0f];
		}
		return new String(buff);
	}
}
