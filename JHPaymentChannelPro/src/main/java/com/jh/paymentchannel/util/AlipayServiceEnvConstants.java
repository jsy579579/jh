

/**

 * Alipay.com Inc.

 * Copyright (c) 2004-2014 All Rights Reserved.

 */

package com.jh.paymentchannel.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 支付宝服务窗环境常量（demo中常量只是参考，需要修改成自己的常量值）
 *
 * @author taixu.zqq
 * @version $Id: AlipayServiceConstants.java, v 0.1 2014年7月24日 下午4:33:49 taixu.zqq Exp $
 */
@Component
public class AlipayServiceEnvConstants {

	/**支付宝公钥-从支付宝生活号详情页面获取*/
	//易百管家
	//public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAltqWokoiPsrqHuO/+mZa0TyEkBZ/hEUmLipPgFEvAN1QJjmxrbSHaXl1vERYCIvelCHDZOlmbXXXqWlxmwf0/CXv0sCQqNkmtR/V4jQM45eC6J8Y3RSgw6c0dpRh5w1YrmXfaJYWjzXXal77Zl0qh/Rqowa4Jf2yGVI4sr1dUJ+V11POgIN30cKA06mRxTc0gcp/z8PS4i8tH78lc7c/6XutjN8vB1FnpPETasNNEN9041YduZVnYiCheV/b4dSBjpnG4wuj+WXvJSzkCpADh8PDEglpn/1sK2xxapDoluHy+BFxmmohOcY0JTB1Kwa74XA/Ol5UIbYjPZMU035b/wIDAQAB";
	//百也特
	//public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhPwOVu9+qg3FPttexqlRdbeFeSpG7YtpJOuumfblLBE37t6C9PmVZNMcmlMIAX7M06BYjsqFXqlK+F2SIZkv+TnTpYr2sPUUAOippVq+F6zZI4stvNr3YpaTEvVinQbuwcIB32ObuR0JOZhvFUpqyyRyt0aByyunZdjFZM5zVVUZ38lF8ZQMQ9IFS38Vf2plS4lO3xj2yI7ELpdbOT/qvC2bcGocuR2QPAHt4XHs0Sm8h0JiRrCM9180P4/FKV4BEsjbGs8PJQyVuBnYql2uyXXIk4D9/vVSS8kgEDLbmGvreo6W3jAO01DXBpUYX0dP/e6ep4HfG6PktcM6nDJjtQIDAQAB";
	//TODO !!!! 注：该公钥为测试账号公钥  开发者必须设置自己的公钥 ,否则会存在安全隐患

	public static String ALIPAY_PUBLIC_KEY;

	public static String APP_ID;

	public static String PRIVATE_KEY;

	@Value("${alipay.privateKey}")
	public void setPrivateKey(String privateKey){
		PRIVATE_KEY=privateKey;
	}

	@Value("${alipay.publicKey}")
	public void setpublicKey(String publicKey){
		ALIPAY_PUBLIC_KEY=publicKey;
	}

	@Value("${alipay.appId}")
	public void setappId(String appId){
		APP_ID=appId;
	}
	/**签名编码-视支付宝服务窗要求*/
	public static final String SIGN_CHARSET      = "UTF-8";

	/**字符编码-传递给支付宝的数据编码*/
	public static final String CHARSET           = "UTF-8";

	/**签名类型-视支付宝服务窗要求*/
	public static final String SIGN_TYPE         = "RSA2";

	/**开发者账号PID*/
	public static final String PARTNER           = "2088521064490881";
	//public static final String PARTNER           = "2088521048698360";


	/** 服务窗appId  */
	//TODO !!!! 注：该appId必须设为开发者自己的生活号id
	//public static final String APP_ID            = "2019060765451656";  //易百管家
	//public static final String APP_ID            = "2019072765949901";	//百也特





	//TODO !!!! 注：该私钥为测试账号私钥  开发者必须设置自己的私钥 , 否则会存在安全隐患
	//易百管家
	//public static final String PRIVATE_KEY        = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC+b64hQXoFW3ec3TksYad8vRWfYFRAOUltKCijQLNJPxE+XsrIfSdjjVrhN9TTrwFCaqlmOw0UhPHb78oHK2/lS+cqs0H8W+WNysMS4/KOaKEZThrZkm3EBfYTMz0eumLUbB2gysj9zLhEkO75Bgg/+yG/3KAfeyYzuX6H8k1R38V/hzZaJe7wZ3wAlEMpDacQiOaMD8kEnIlfdMPava1Sk04ZgFoQJ12IGJEAZrSmiX0o50Fvogdyo/zH5diD4TsglkET4fdLm1SEWFcY9/R25dH/H7BqO4vPofhN5hZWtprrYS8JYZFr8zwyFpXTQR8YVnhwJJUzhFmI+NOojnr1AgMBAAECggEAUm5lIWAGQWtmSzA3H07mAMiYI3S/A03IyscnUAMhmAMEo8rEXCRQeHcnb3WLWlXWZblluiq1brxdNKaG/LOAq/A/D+yoE2Rz6PxrlDG3PI1trlZwqN0JRmCOM6FqQ3LKdMiXypH8jl+fI47Y4lIKICfEZBhL91SHJLyWWKI5dcZ3XUf4XwTot2K5wfiBJeMPru5D+JSNWqlXyjgX+gvwgKGmh0AYlX5uAOFgmGSvQIZFgRM2wQ2VBF5JfK978cuXZiLJgqL6vI5/EBqfb4DUrxZZ6pIU9yaq8ZeJW80+Q3noL23ivvgHpYe6qHA2+WXxps0JmthDcujoi7ZiFhKkAQKBgQDrT8VwKizLcU2swLa2ooTeqjpLs3rQ+3E08HiF7e05Sv3INHZREX/2cOB4Pt0qxOJ6QH+n8dQdzTqbVnF6h3v+UlhCpuc5gmzMr8JyRwXUkzVb69nCW37i251Pyc5vmoDNemllaVH3pBuAb7prT0PVANj6Mkfwx8+DWKcDHcLXUQKBgQDPLeLJu+jlu1oNS0BtNTSy3irqMppm7tSHzbR2EpvNAOw8UiMECPyPYZWlly8VKz2Nviop/D/HsoVpSRbexLzxRmzsVAgxceNYDPQu8bVxnSDbt1WHK4Jz42Qk8sdH3aJr5SVADUP0MIeYq+G69hVyZ8gjWbZAHVYovKynkWUIZQKBgBx/d7ji0rP8z9xpJnet7QuOcCr03J3spcQ0j1QhAYbbxyp0+BNPXED7YKQPcPeaCpelshAj4H2PelaPvISLXf/p9xhzYDN90TFk49a1NUaOYXr1cFTygcZ1tHKSTTcE7i50cck/XRbxwAbpARm36s4kISMD7eSCPmZ42RkuhIdRAoGAcW6kHuO9ThrX4TN0YNaitdQiFvwBwZ29y2T7CzKBDysULKqj9U+eSDqbnoA6DRMRrouRtk8f9IxvsweLeAirkkaG2JXhroC2gr0E7Lb2KvBmzyV/tHREBoNXj0tFhLl6diyNypX4wLfZZeOlwaF+eJcN4xz5Rf1J8R/sQArBEGkCgYBOwPqs39uuo7PitcTvHIr5RsigVUow0Pa8EcR/NSbYYXUejbhL4WQOD5HBVjZsY3BAkB+tjjMD1J7k9EtFThQxtA30i9WCvQeGUUTvctYcvuO2SV3v1jlobryemjDNfvvYDJXch9iq1EtBeSz3YERb9C9LVl94Zn/1+50ACNnUWA==";
	//百也特
	//public static final String PRIVATE_KEY       = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCIdJA9bZVWA2CduBw41NEdwDe6hsq6bwnoOqo+HKcqNG0MI0ebUhJEdBf6mdobwZsbBGedGcapBKni4uaLYgWw5XBMQ8+dRNso90kKSlXvuN3g/Nuj/cdOzinBKHPsxA5TEr3qGmbu5wyLtdVvQEkCrlR5l9l686yovwHW0sGQO3CpNvisUUEpYefmLnDHZxedIMHl4wFpU69s3iyD63qfaUlKyLkvCvYa+ug4DYMlQbKpw/P+pofZlv7dk1/d9LU2thEAYyzV+SFNekr8o1OU1bkxYgufMUnOMjGAnIrGc9sI1uVOKpT9FcBQ6xjCGXh1Ba/Bh9vHs3lQhjqUhow/AgMBAAECggEAaacO83EAmpt82EFwIJSu4UEuJQ8uspE884FQxUy8JdJ3yFkcnWc7W6Mj2dX76VfLN2z+qMYHamCBBXo3lR4tV90eSP6MJrHkmuB0vjH2Pek+anB9w4jAJAU+kR84Os9GZA3z2+nbXwioTxhYDI6SuS+vmoH3UtqPCgzyaJnAY2Jt1LvIG8JITPWQaY7wweof3K3S9rg0WJ8B0Fh8ELncpC+QtcG3BIbOnSzK470R0C9GAjwEc/9bRuvH0q+22xqi8b+2VGRmXSlakqtxBcowhBMEb840TGsv5LmKbf+fatt536vg3XGl5wpClFbgI/VMoiR091KJP+EJE9GVGuGZyQKBgQDZJr9HQ0csx/85bxekhu0SPVygg/we1WqceFubjp1bmT04OAZ1ErMXl1dgNYm5jAJ9340MuqIKSprOM17K5xBlbDmdb+q3QkR5Jc8667FGeWK2sZIxNFqbSBiaNawMR+MIft4TC5zYhz12BctDX8dl5t2Drb2ieIUDOEKyFGy8mwKBgQCg3gq3UgMKn0IngUSlP9ybaMSfHz21sB7Xf2XxlvVaHcbb3ePL5YEv8ja29hQ5VGMHrgHv6vf6Nv1JQc0CfOfSocOui5acEAwEmE2yKCWDlqQkB4kAELToogg4XNqkWZIvmU4YQ2DiVHS/pd8/6nA39kQGdnFnONc1LRI7ydH/LQKBgQDEoaFzFijNFAy4JTBzevoRGh9V+i3Cfd4b8aCCK5Gx8ADPd2vhdx1Ur2YfaFtd+LoI4PUIIbe5OfUT4tBjSvg93tINDdqsNwVp7iLIZ8QO3LvWUtTeWVnyYkZk3VT9idq7RqYw+ML+DvhIdtaoG4Kjc3oCUsD07c0ELV23g2czIQKBgEQGQN/ORUz7lVzhM+bw/1eUUPi8nDq1NAWKcNBdNnkZ+FpHFSnGbf+ZW/u4SUsI3SuFMHqEkMH0+Nw+f/OaX+lY0EeB7Xkm6/4RbWF1yo8/Zz95FDy709Q/mFxFH4u5+LA6EqT98P0kG45jFOmROEzhzVgyQ7gTeCWbXod7y1otAoGAcTnAD6Gl2M/1GhpPAd1ySLwdqEqJ16H6ifV2kFMzuJtcxd28EIowkePRE1RRyaFL4K4VHzIIOAETYAhxBV3bBjPjkfQtWejMYvdzLWWqvUmZar8tCUs1RWgtizYJT9Mh0hnMAD/wuIE4sxwDb7qcXRaDT5W+1RV78udIMnDRnpo=";



	/**支付宝网关*/
	public static final String ALIPAY_GATEWAY    = "https://openapi.alipay.com/gateway.do";

	/**授权访问令牌的授权类型*/
	public static final String GRANT_TYPE        = "authorization_code";
}