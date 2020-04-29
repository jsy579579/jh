

/**

 * Alipay.com Inc.

 * Copyright (c) 2004-2014 All Rights Reserved.

 */

package com.jh.paymentgateway.util;


/**
 * 支付宝服务窗环境常量（demo中常量只是参考，需要修改成自己的常量值）
 * 
 * @author taixu.zqq
 * @version $Id: AlipayServiceConstants.java, v 0.1 2014年7月24日 下午4:33:49 taixu.zqq Exp $
 */
public class AlipayServiceEnvConstants {

	  /**支付宝公钥-从支付宝生活号详情页面获取*/
		public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApHc7hsclcz0R0/i42dO91bP5ThEBnzjbOYva9iT9ul96bLeVkwzapiOT5DZR0eyME/Fl6iG9HhJ0hx3DzTTUccEvL7DECnAx7ICcNK6LjNFKHwv6niPOjNc17lolwJYAKOIIaE583ndo4jpl09+BanaqfLWgwkeUiTjBsl5HgBu2IzoUU001jj4QoQsEaZtLtl3yMs67Ylrh1IRbgbPnrlZd0uUzK4g9BcQnAcStpXC6Kxe8DT2ABRl6VHyXUZM7hh+v0d8D7iDBznPN7yAj2wHTUBgs5huoC6XK1UY7QChevsJyHSPaNMXO8mUu1WtjyzS2qV/x+Klwa4QWLvRWgwIDAQAB";
		//public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApHc7hsclcz0R0/i42dO91bP5ThEBnzjbOYva9iT9ul96bLeVkwzapiOT5DZR0eyME/Fl6iG9HhJ0hx3DzTTUccEvL7DECnAx7ICcNK6LjNFKHwv6niPOjNc17lolwJYAKOIIaE583ndo4jpl09+BanaqfLWgwkeUiTjBsl5HgBu2IzoUU001jj4QoQsEaZtLtl3yMs67Ylrh1IRbgbPnrlZd0uUzK4g9BcQnAcStpXC6Kxe8DT2ABRl6VHyXUZM7hh+v0d8D7iDBznPN7yAj2wHTUBgs5huoC6XK1UY7QChevsJyHSPaNMXO8mUu1WtjyzS2qV/x+Klwa4QWLvRWgwIDAQAB";
		
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
	    public static final String APP_ID            = "2017070807685815";
	    //public static final String APP_ID            = "2017070807685815";

	    
	    //TODO !!!! 注：该私钥为测试账号私钥  开发者必须设置自己的私钥 , 否则会存在安全隐患 
	    public static final String PRIVATE_KEY        = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCGXEb9Glz2M4M3QBJf9OyEM4tToU7aDgGq/rEsmQ+2BtA+NiD8mcjLUjYe9Nd1vrki/HiEeLnxUdMtU7ZdJIf4pdJMSLJlHy+iR4lleAfusChGaOhk1UCydapjkQB3RFwmZ3f16xpzcNepBuJAcM7+PPeUN4oL6Y7/Jr1Y2ViNdmzgts8WZGCO1aUKsi6XHD7ZWTEFvgHUoJrvW9U7tWNiSTMWFbrehKekNACYpcHFEpCX4YNukW0h1lyS72MfC66VaBkwo53BC1EEZHM7IrzMFS9K4HG3qHR0p9H+4J4wYAf2BtGTrqhnxLI3Cxzpb3xPlQzu7GhFCUtejG+w2ynVAgMBAAECggEAaZhIKk731H6zl06BomxpHZUs4LMbt3/ei7bTI8u/bz13mA0Oa7eo+TJJWpbuZplYA0kMpoADEsdOAoeSAS3etuoLluxjV9nko9M5BkgJY+uKG5wDWIvI5vhWyx2AkjtleMFEpD77cNK0iyDf2+lMpL7hjNkBY09RlPGrShK256WRWomueP6z9BIcMsbb1F0AQasy/X1mQUIiC/hBrteGt8BrrOa2CIyc1iN7sfzU2zkRulkWJ2BD/uTQrEItoWn/yc17LASRnctc1+i2gOOsb/At06ymb5C8NTx2gRcjWzZNfyqZlcOafIwJmtH8eFwMIomkWYYuJxzOtb92jFEcAQKBgQC+Wp05WZbnXmHVjG/e3mCcl0ii/mKlKC+5ljMWVtw6ZORC7LEiAEet76LsUMta02wjwbRtwbxqLphspEHmYF+LkEeSGBpWzbp5avC/1rbIg775Ruzil6gMWhaNEy0eAk6DZYKJiDyt3FsrcgHB+zZG/8zOTsm593aMJve91axxgQKBgQC0skfc/4w/nAoAZYBv5KqNhMdWziqvKEdkRngnICDBIlZ6OE/WJ9CyFGR07PvMXhUZVCEYBNBsDmK6wTmezaUQJNJALBc18Ldn8L4S/i6N90xKnPHUMO89ic8rqfS1cJzILtwh81fkscU1LtihRLlR8pSRrGEwW/jtWT+ubYJ6VQKBgFws1stplYguDOPyoE1kk5XgL/01f6EIB6wvjkd4AiaIqnu5z8BuMEeKrfMO4Jn3nPrnlq47TTiBna+mDn83Fa0oTW/yGfcEohKfdTsABvyrxzNAY/8JfweULC1eJs/MQCaiZgahIPjzpnYwO6eqNMtGnO8d+dMCgLY3PGoBaQKBAoGBAJB3nUcOtWXB7qkkfcx4CPf+eZVoXe4OiQ2d3cCCSk2/k15CGWqCCLPn4+0r76EUc0/92Duo1a4ezka758KUbez7U3HlDmWc4DXk30K2ICh22HMrCZl5yjzsKkuhpTlPILpX5L3sHZCFpMMBgkdld7kR9kCFjmqojdvNqEFf4prFAoGBAIHYqEAy6EvkcWmg4/I1HFDpaOiGAu+uRS3WBRalYhjbqqMmGOtKaQLBmmDXKRDK+qN43ELZZPn0XITtZfxOEGMQqrKtwQdSZLWoqBFyecU259BSACDakZPJTz3XkudAoXBy8YZpEC5HtlEYpJmvXpRxyX94iwokw09VBmpdNIqz";
	    //public static final String PRIVATE_KEY       = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCIdJA9bZVWA2CduBw41NEdwDe6hsq6bwnoOqo+HKcqNG0MI0ebUhJEdBf6mdobwZsbBGedGcapBKni4uaLYgWw5XBMQ8+dRNso90kKSlXvuN3g/Nuj/cdOzinBKHPsxA5TEr3qGmbu5wyLtdVvQEkCrlR5l9l686yovwHW0sGQO3CpNvisUUEpYefmLnDHZxedIMHl4wFpU69s3iyD63qfaUlKyLkvCvYa+ug4DYMlQbKpw/P+pofZlv7dk1/d9LU2thEAYyzV+SFNekr8o1OU1bkxYgufMUnOMjGAnIrGc9sI1uVOKpT9FcBQ6xjCGXh1Ba/Bh9vHs3lQhjqUhow/AgMBAAECggEAaacO83EAmpt82EFwIJSu4UEuJQ8uspE884FQxUy8JdJ3yFkcnWc7W6Mj2dX76VfLN2z+qMYHamCBBXo3lR4tV90eSP6MJrHkmuB0vjH2Pek+anB9w4jAJAU+kR84Os9GZA3z2+nbXwioTxhYDI6SuS+vmoH3UtqPCgzyaJnAY2Jt1LvIG8JITPWQaY7wweof3K3S9rg0WJ8B0Fh8ELncpC+QtcG3BIbOnSzK470R0C9GAjwEc/9bRuvH0q+22xqi8b+2VGRmXSlakqtxBcowhBMEb840TGsv5LmKbf+fatt536vg3XGl5wpClFbgI/VMoiR091KJP+EJE9GVGuGZyQKBgQDZJr9HQ0csx/85bxekhu0SPVygg/we1WqceFubjp1bmT04OAZ1ErMXl1dgNYm5jAJ9340MuqIKSprOM17K5xBlbDmdb+q3QkR5Jc8667FGeWK2sZIxNFqbSBiaNawMR+MIft4TC5zYhz12BctDX8dl5t2Drb2ieIUDOEKyFGy8mwKBgQCg3gq3UgMKn0IngUSlP9ybaMSfHz21sB7Xf2XxlvVaHcbb3ePL5YEv8ja29hQ5VGMHrgHv6vf6Nv1JQc0CfOfSocOui5acEAwEmE2yKCWDlqQkB4kAELToogg4XNqkWZIvmU4YQ2DiVHS/pd8/6nA39kQGdnFnONc1LRI7ydH/LQKBgQDEoaFzFijNFAy4JTBzevoRGh9V+i3Cfd4b8aCCK5Gx8ADPd2vhdx1Ur2YfaFtd+LoI4PUIIbe5OfUT4tBjSvg93tINDdqsNwVp7iLIZ8QO3LvWUtTeWVnyYkZk3VT9idq7RqYw+ML+DvhIdtaoG4Kjc3oCUsD07c0ELV23g2czIQKBgEQGQN/ORUz7lVzhM+bw/1eUUPi8nDq1NAWKcNBdNnkZ+FpHFSnGbf+ZW/u4SUsI3SuFMHqEkMH0+Nw+f/OaX+lY0EeB7Xkm6/4RbWF1yo8/Zz95FDy709Q/mFxFH4u5+LA6EqT98P0kG45jFOmROEzhzVgyQ7gTeCWbXod7y1otAoGAcTnAD6Gl2M/1GhpPAd1ySLwdqEqJ16H6ifV2kFMzuJtcxd28EIowkePRE1RRyaFL4K4VHzIIOAETYAhxBV3bBjPjkfQtWejMYvdzLWWqvUmZar8tCUs1RWgtizYJT9Mh0hnMAD/wuIE4sxwDb7qcXRaDT5W+1RV78udIMnDRnpo=";
	    
	    //TODO !!!! 注：该公钥为测试账号公钥  开发者必须设置自己的公钥 ,否则会存在安全隐患
	    public static final String PUBLIC_KEY        = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhlxG/Rpc9jODN0ASX/TshDOLU6FO2g4Bqv6xLJkPtgbQPjYg/JnIy1I2HvTXdb65Ivx4hHi58VHTLVO2XSSH+KXSTEiyZR8vokeJZXgH7rAoRmjoZNVAsnWqY5EAd0RcJmd39esac3DXqQbiQHDO/jz3lDeKC+mO/ya9WNlYjXZs4LbPFmRgjtWlCrIulxw+2VkxBb4B1KCa71vVO7VjYkkzFhW63oSnpDQAmKXBxRKQl+GDbpFtIdZcku9jHwuulWgZMKOdwQtRBGRzOyK8zBUvSuBxt6h0dKfR/uCeMGAH9gbRk66oZ8SyNwsc6W98T5UM7uxoRQlLXoxvsNsp1QIDAQAB";
	    //public static final String PUBLIC_KEY        = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxF/sKac6RXiwHDUsTiw1G3wA0BqfY+nDODn74EqHS5gOyONCfg6ifXUBksTp40NXrgmNeXTSwKjXVm7fWQ1Gb+74DiOrfZoqnl6xzFTvQ4pf+UTWcqxem9u0BAPeWOPYYvqsvlmwYVuXvuQ6f0y5jDI76VUL1N580ThRrvotei99XcH2jjP5Wrl2pF6Ky28TouFZ6qZqOk1I66M5pVVAwyM0JvBSLNNbSRpDM2FfuPxJLtwMJJ8tDBGyaPMjTe+HzpkKmcGg3l7Fr/U3OlTxhlHUuN9PWTDJEC6fcGWSkTAwxqKNT3ns8kS6aNgPi8Lh6buS/7JSwoL9dk9gzGDwtwIDAQAB";
	   
	    /**支付宝网关*/
        public static final String ALIPAY_GATEWAY    = "https://openapi.alipay.com/gateway.do";

         /**授权访问令牌的授权类型*/
         public static final String GRANT_TYPE        = "authorization_code";
}