

/**

 * Alipay.com Inc.

 * Copyright (c) 2004-2014 All Rights Reserved.

 */

package cn.jh.facade.util;


/**
 * 支付宝服务窗环境常量（demo中常量只是参考，需要修改成自己的常量值）
 * 
 * @author taixu.zqq
 * @version $Id: AlipayServiceConstants.java, v 0.1 2014年7月24日 下午4:33:49 taixu.zqq Exp $
 */
public class AlipayServiceEnvConstants {

	  /**支付宝公钥-从支付宝生活号详情页面获取*/
		public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiHSQPW2VVgNgnbgcONTRHcA3uobKum8J6DqqPhynKjRtDCNHm1ISRHQX+pnaG8GbGwRnnRnGqQSp4uLmi2IFsOVwTEPPnUTbKPdJCkpV77jd4Pzbo/3HTs4pwShz7MQOUxK96hpm7ucMi7XVb0BJAq5UeZfZevOsqL8B1tLBkDtwqTb4rFFBKWHn5i5wx2cXnSDB5eMBaVOvbN4sg+t6n2lJSsi5Lwr2GvroOA2DJUGyqcPz/qaH2Zb+3ZNf3fS1NrYRAGMs1fkhTXpK/KNTlNW5MWILnzFJzjIxgJyKxnPbCNblTiqU/RXAUOsYwhl4dQWvwYfbx7N5UIY6lIaMPwIDAQAB";
		//public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApHc7hsclcz0R0/i42dO91bP5ThEBnzjbOYva9iT9ul96bLeVkwzapiOT5DZR0eyME/Fl6iG9HhJ0hx3DzTTUccEvL7DECnAx7ICcNK6LjNFKHwv6niPOjNc17lolwJYAKOIIaE583ndo4jpl09+BanaqfLWgwkeUiTjBsl5HgBu2IzoUU001jj4QoQsEaZtLtl3yMs67Ylrh1IRbgbPnrlZd0uUzK4g9BcQnAcStpXC6Kxe8DT2ABRl6VHyXUZM7hh+v0d8D7iDBznPN7yAj2wHTUBgs5huoC6XK1UY7QChevsJyHSPaNMXO8mUu1WtjyzS2qV/x+Klwa4QWLvRWgwIDAQAB";
		
	    /**签名编码-视支付宝服务窗要求*/
	    public static final String SIGN_CHARSET      = "GBK";

	    /**字符编码-传递给支付宝的数据编码*/
	    public static final String CHARSET           = "GBK";

	    /**签名类型-视支付宝服务窗要求*/
	    public static final String SIGN_TYPE         = "RSA2";
	    
	    /**开发者账号PID*/
	    public static final String PARTNER           = "2088521064490881";
	    //public static final String PARTNER           = "2088521048698360";
	    
	    
	    /** 服务窗appId  */
	    //TODO !!!! 注：该appId必须设为开发者自己的生活号id  
	    public static final String APP_ID            = "2019072765949901";
	    //public static final String APP_ID            = "2017070807685815";

	    
	    //TODO !!!! 注：该私钥为测试账号私钥  开发者必须设置自己的私钥 , 否则会存在安全隐患 
	    public static final String PRIVATE_KEY       = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCIdJA9bZVWA2CduBw41NEdwDe6hsq6bwnoOqo+HKcqNG0MI0ebUhJEdBf6mdobwZsbBGedGcapBKni4uaLYgWw5XBMQ8+dRNso90kKSlXvuN3g/Nuj/cdOzinBKHPsxA5TEr3qGmbu5wyLtdVvQEkCrlR5l9l686yovwHW0sGQO3CpNvisUUEpYefmLnDHZxedIMHl4wFpU69s3iyD63qfaUlKyLkvCvYa+ug4DYMlQbKpw/P+pofZlv7dk1/d9LU2thEAYyzV+SFNekr8o1OU1bkxYgufMUnOMjGAnIrGc9sI1uVOKpT9FcBQ6xjCGXh1Ba/Bh9vHs3lQhjqUhow/AgMBAAECggEAaacO83EAmpt82EFwIJSu4UEuJQ8uspE884FQxUy8JdJ3yFkcnWc7W6Mj2dX76VfLN2z+qMYHamCBBXo3lR4tV90eSP6MJrHkmuB0vjH2Pek+anB9w4jAJAU+kR84Os9GZA3z2+nbXwioTxhYDI6SuS+vmoH3UtqPCgzyaJnAY2Jt1LvIG8JITPWQaY7wweof3K3S9rg0WJ8B0Fh8ELncpC+QtcG3BIbOnSzK470R0C9GAjwEc/9bRuvH0q+22xqi8b+2VGRmXSlakqtxBcowhBMEb840TGsv5LmKbf+fatt536vg3XGl5wpClFbgI/VMoiR091KJP+EJE9GVGuGZyQKBgQDZJr9HQ0csx/85bxekhu0SPVygg/we1WqceFubjp1bmT04OAZ1ErMXl1dgNYm5jAJ9340MuqIKSprOM17K5xBlbDmdb+q3QkR5Jc8667FGeWK2sZIxNFqbSBiaNawMR+MIft4TC5zYhz12BctDX8dl5t2Drb2ieIUDOEKyFGy8mwKBgQCg3gq3UgMKn0IngUSlP9ybaMSfHz21sB7Xf2XxlvVaHcbb3ePL5YEv8ja29hQ5VGMHrgHv6vf6Nv1JQc0CfOfSocOui5acEAwEmE2yKCWDlqQkB4kAELToogg4XNqkWZIvmU4YQ2DiVHS/pd8/6nA39kQGdnFnONc1LRI7ydH/LQKBgQDEoaFzFijNFAy4JTBzevoRGh9V+i3Cfd4b8aCCK5Gx8ADPd2vhdx1Ur2YfaFtd+LoI4PUIIbe5OfUT4tBjSvg93tINDdqsNwVp7iLIZ8QO3LvWUtTeWVnyYkZk3VT9idq7RqYw+ML+DvhIdtaoG4Kjc3oCUsD07c0ELV23g2czIQKBgEQGQN/ORUz7lVzhM+bw/1eUUPi8nDq1NAWKcNBdNnkZ+FpHFSnGbf+ZW/u4SUsI3SuFMHqEkMH0+Nw+f/OaX+lY0EeB7Xkm6/4RbWF1yo8/Zz95FDy709Q/mFxFH4u5+LA6EqT98P0kG45jFOmROEzhzVgyQ7gTeCWbXod7y1otAoGAcTnAD6Gl2M/1GhpPAd1ySLwdqEqJ16H6ifV2kFMzuJtcxd28EIowkePRE1RRyaFL4K4VHzIIOAETYAhxBV3bBjPjkfQtWejMYvdzLWWqvUmZar8tCUs1RWgtizYJT9Mh0hnMAD/wuIE4sxwDb7qcXRaDT5W+1RV78udIMnDRnpo=";
	    //public static final String PRIVATE_KEY       = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDEX+wppzpFeLAcNSxOLDUbfADQGp9j6cM4OfvgSodLmA7I40J+DqJ9dQGSxOnjQ1euCY15dNLAqNdWbt9ZDUZv7vgOI6t9miqeXrHMVO9Dil/5RNZyrF6b27QEA95Y49hi+qy+WbBhW5e+5Dp/TLmMMjvpVQvU3nzROFGu+i16L31dwfaOM/lauXakXorLbxOi4Vnqpmo6TUjrozmlVUDDIzQm8FIs01tJGkMzYV+4/Eku3Awkny0MEbJo8yNN74fOmQqZwaDeXsWv9Tc6VPGGUdS4309ZMMkQLp9wZZKRMDDGoo1PeezyRLpo2A+LwuHpu5L/slLCgv12T2DMYPC3AgMBAAECggEANJJdfhU342tY8mRF1TO+j/oVmaDW9P+a9M2JIdWx/Mnbg2pb3bV7SMzvO+SdnOrlaUFDb7WXyl4pMAmEn6mPJt456EciXQDN0hIV1VcTqug7o1cLdi2xMlaSDdUfTHrK7kyfvGH0d0v3BvLAul3G+sJkCaZsAXQCxFwoegid7iyafJ+XAto/keGAjmlesZBgp9HdbAhV5NvGK1xmXjH+CboPynQER+ze818l834xp5T0lYrJSMd+agYfEJNBmzVgKa3NhlNc+YHJOGC9uHWQp+eHLaIJmBF9xpDnT9BfuFqgBlxLqRItdzPWL3SB+qBvLpe98QRBfj1N4e86vgUKkQKBgQDhtuRqF3WTukpIq6uJvdXp7gcSiezVfGckV1IwmbjSWKYgpEjVcGDejxTKna8fAw/Q6MmdOl6qyxnuMVjTNBHYdWDYZR3FqQo8H86YqnjcYI1G9kwZBYw0MwdP2n2EHVbfCKtfyXWw/TK0iFacXzyl1Z4Qe9rt1TmnlYrn3kUfOQKBgQDeuTwEOIJVhCud219O7TDGfANOKgbQAgASHqVgJS8VIy4/b78+0AL4OMW7jF7ZIc0RRKp1v//h2MeoklNEM9pWk2UAlF+jhCPWHrS8GxzOjJaXOjB4pDpgIYgrBkbYwu1n2QM+i8DQ0wKunkRK3xkj4t8gEpyoZz3OhyXI6iSfbwKBgH+PyzsiEvLwP3cefg2UWcahHSIfDNgCPJBPQUXyOn+2Vfzwm7D0l+pv1oJD52qRYmjghsDy8fl4WylAfkCulr9MZvUlAFRrGjushifFgBScBHvsSkmB1yDRDfrVMkZuBmB6F9BEnNzv264ntzscn7vhJDmQpcPv02ugmiY8ebgRAoGBAKsDTdrrzN9+wcLOm2rN4uK0fBE7Ycm7j3RaRZ8CThmLzsn0ssK3E2CmruTnzjgopP26XKxXBVHTktKTsA2xRtetCyObyaeTIbUaFKpOoxrRhhuOGYO4jf4cISLwCdwMKB/R2JTg1+c/7lJHwaY/kV0lqS9GOzVvOFzpFpEjf4VPAoGBAMT/+SjcZQP46UdkI+JS2TDPhsUStVnzE/0qrRvekfmETADX/Tw0hGZQOHTJoPlltG/3CO+rEsFboYOYo1xYbEaoiOrk1/Yt5+c2A3/8/RL5a/Cxzz8RX1CPuTWsEcgufKx6hjOCGC/nPeaeK3ErUSSrEDkfI45TPBJXfxlv7Pmi";
	    
	    //TODO !!!! 注：该公钥为测试账号公钥  开发者必须设置自己的公钥 ,否则会存在安全隐患
	    public static final String PUBLIC_KEY        = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2C+znNgji7GDMqwCvcKXsrPyuUp0DCBabP9xWTyjS73xaFiOJYD85f9NTZUAtU9qsLy49KX3KKjt5jcTMmbW1mGGQrQkZcH5xQI5Gf5cFpFUtCyyzxbydVRsegqE19CAyFRnr4uuknh9WCtLUAvULD0LUFmKr7yofRQSToyyRPNLwpGXfcxbNCweIC5LnjJ1gMhD7QLKdSDfManpMVlvxCWGM7Bz+RfcRrnUTnvDOS8x4DaHUV2+DLy9Mz39uWKC57P/lmwnlMI+knHsY2OcW0RRqUwP6FoVhYz3jObc8C5Dwo9GzxpG/H/QIfT8QuIpa2AKDmqnKD1zeOiZytFAwIDAQAB";
	    //public static final String PUBLIC_KEY        = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxF/sKac6RXiwHDUsTiw1G3wA0BqfY+nDODn74EqHS5gOyONCfg6ifXUBksTp40NXrgmNeXTSwKjXVm7fWQ1Gb+74DiOrfZoqnl6xzFTvQ4pf+UTWcqxem9u0BAPeWOPYYvqsvlmwYVuXvuQ6f0y5jDI76VUL1N580ThRrvotei99XcH2jjP5Wrl2pF6Ky28TouFZ6qZqOk1I66M5pVVAwyM0JvBSLNNbSRpDM2FfuPxJLtwMJJ8tDBGyaPMjTe+HzpkKmcGg3l7Fr/U3OlTxhlHUuN9PWTDJEC6fcGWSkTAwxqKNT3ns8kS6aNgPi8Lh6buS/7JSwoL9dk9gzGDwtwIDAQAB";
	   
	    /**支付宝网关*/
        public static final String ALIPAY_GATEWAY    = "https://openapi.alipay.com/gateway.do";

         /**授权访问令牌的授权类型*/
         public static final String GRANT_TYPE        = "authorization_code";
}