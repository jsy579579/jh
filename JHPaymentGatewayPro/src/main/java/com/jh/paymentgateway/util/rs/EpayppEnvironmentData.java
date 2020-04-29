/**
 * Epaygg.com Inc.
 * Copyright (c) 2016-2017 All Rights Reserved.
 */
package com.jh.paymentgateway.util.rs;

/**
 * 
 * @author huliang
 * @version $Id: EpayppTestData.java, v 0.1 2017年6月22日 下午1:24:52 huliang Exp $
 */
public class EpayppEnvironmentData {

    // 模拟生产(prod)/模拟测试(test)
    private static final String ENV                       = "prod";

    /**
     * 测试环境参数
     */
    private static final String TEST_SERVER_URL           = "https://stable.epaygg.cn/mapi/gateway.htm";
    private static final String TEST_PARTNER_ID           = "1818001000000818";
    private static final String TEST_NOTIFY_URL           = "https://stable.epaygg.cn/acquire-server-java/demo/trade/notify.htm";
    private static final String TEST_RETURN_URL           = "https://stable.epaygg.cn/acquire-server-java/demo/trade/returnUrl.htm";

    private static final String TEST_MERCHANT_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
                                                            + "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIjjB/XVwEkvGv1I\n"
                                                            + "swB6cl1OglVWi8w8zmnp/DY7vidIBCpLVR6cAaAvDQ9RltqgOgjk1dIXOseXKcT/\n"
                                                            + "RwyBKsNnzVIko7PnVjzMliQeP64TQcF+ZgwA0zDCtVv5F/KaV1Tto1vQa8G6eWO4\n"
                                                            + "jZOfEeXk/Mxz7r2172xU/sX+gMfRAgMBAAECgYB/5JY4YnhgKAmwYa8UoFKfyM1O\n"
                                                            + "zCibLqiwHjRNu23DJ670kSO7EIkYTz2FdXZbV15BlwQLYTbC6gHVk6ifD0OSoiox\n"
                                                            + "f8VSuMiDfqUaDrzTUJGDstPNsrgER/p5LvKt84Jj2ldmXvssPQcqjHEtOrqJqWpz\n"
                                                            + "EcJ8jrO9ADPGk09LXQJBANq4UfdRwm8eInvkoipV87WnGSCPIHvbwCtGu2oJF4/m\n"
                                                            + "/9TXTSn0qhzrdUqJ2/VsHH3AN545nMQmxx/g1SSJVXcCQQCgN/vMvxmZ/LmCn41M\n"
                                                            + "5kdOUcvU5HWu3Dn1upsVkxOfx1oXCB3s9gkguVOazjdJ9NZcK1u4AzWrGKO4JrAs\n"
                                                            + "N773AkA1laK/LNCt2HwqTkFMjfI/Nsj5KReUQKo78ABKYh/bqYRT6MuG0+I5Y5ZA\n"
                                                            + "xLWW0v3H8SevI/48MCPos3SElwDRAkEAgNODC+a81yx2naldHHAs9bSfiNcKKUe6\n"
                                                            + "8vLQsFBKfzb0IYDNTzOls0JRDzCxFC5iDkpuvU8XYDO3sOKq1WMdkQJAT0gtUpco\n"
                                                            + "gNAjHJ/mA4VxTiA74SbWA8K5s4jp7s7wt0+a/HzdXiCtbXWES3wIJxNqoMDeUCT+\n"
                                                            + "dyIwXVjnhZn8wA==\n"
                                                            + "-----END PRIVATE KEY-----\n";

    private static final String TEST_EPAYXX_PUBLIC_KEY    = "-----BEGIN PUBLIC KEY-----\n"
                                                            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2NGBNIWhgPmUD2tz7i6eeb3ga\n"
                                                            + "si4gd3ObJbF8ISE2kN1kZN2XIoPpEsTSnovUf4Eb1jOBuefhzs+KORSTt/pyIzYv1\n"
                                                            + "4U9c3QiM6iz6LHOVc+sI+xhNqP+5tS6qGzk/FV8cDhVuoEQmhQGC9M6kkWKrXRdv7\n"
                                                            + "ASCMSyOGHk5NIFywIDAQAB\n"
                                                            + "-----END PUBLIC KEY-----\n";

    /**
     * 生产环境参数
     */
    private static final String PROD_SERVER_URL           = "https://gw.epayxx.net/mapi/gateway.htm";
    private static final String PROD_PARTNER_ID           = "1818001000003822";
    private static final String PROD_NOTIFY_URL           = "https://stable.epaygg.cn/wc-server-java/demo/trade/notify.htm";
    private static final String PROD_RETURN_URL           = "https://stable.epaygg.cn/wc-server-java/demo/trade/returnUrl.htm";
    private static final String PROD_MERCHANT_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCSGfFw8I+kSnz2hJEiJcVQOpTsOLR2tT+0fvl1YM1duDRsDxppBA88QpM4RH7YYameuBbqxp6Ht2zzJXoZ6EEsVqhskjV4X8/VQYi1dCcqEXI7Tq78L2fy1GcD0fDX1CAKP2bL8KJuu3R5Vd9K6WQLzleQEstMXNxqnwxom1zbY5rKE+ZfbMyPOylsehsk8Lob10w8HvLUSMGIeip23+pQ9GRCeo55Oq6DDmX9NQS0fteb60qW/3zlYVK7fbJTZLbHXBAS2TdoPrKKE3c1tdvxosmu6vfmzTI8AIExvf5s7OSvv9qM49j9XE7nPszIZajRY+jcoCm/8N3XGrMYX67/AgMBAAECggEASu7ta3ymX6AouZNCkN4Idl6ldQacYGoTs3KQZYhxrEjG8klIxWXknoaS1YAkAr0MbzCB6IZYVslYItks5868Zo5Hse/HZubVRM5o3JAnaicqjIqNqyBxUxVnhIkP2tKcYEUmZyETXnHcikLl1JkhzABX3rgU9ySFlFXg2mIc3RRQvWw4SefoF1DH+cGjKh2iHT8eAB0aEot+7vDVt4gaaqyhengP0P3rTQwxS+VKLtldRGCcgvhu586eeZSSMllYrfEoYHI5FINVMFLdoXjNdZhj2QVlXgL7h8Wofs60Z4Q11UPg/+83pt/QGTte9DKOTmR0pCqOVTAA2XMvhG2+YQKBgQDVcPGCHCg+CSF3Wb2aLosRrqcZCNNfea5Mh9j7n5eei/T2fXjTmd/FKxog79K1m7BysIXlOGZcJRJOOSF+yCFbRl326Ar67RqwsmLXt7hWkR2IuvVGQjNTpU8UthKWWqaLcOZVTwxsaN8zLymMzM4RuaxxxLgYBbWY5iCpm5D0kQKBgQCvO6cYx1RaQLnttAKeQYI1fDh/6mSjho0xmrCbsfEpGkw4hFtwFu6MIV+Zz6qR537T8vHOqF49vm5dA7BWjkpJsfsGzsxtXut7lqnKrjRddUCwsXoYzMneoOuIVtgcho2A0K1n760Bb5+MageV0cx4+p4K2zJzsA3JIWozGtbyjwKBgQDAqauGi44TuUA5MItCImMr+eAha+MImpinwjQtpXhCCAl9efLX5lyj6G00b+ZeQgO68vZZ21giMuBcNZuzikj50AG/fuNybxYZi1xHZjICCgmDw2blHZqhFWXVxyfuCjOtSKLRPIJ1VRCsbhTuYGxeeaBcLXsTTAwI0SmIj8D/0QKBgQCPI61FIl4XM1QthaO13lEcm5ITe0YmBd0ELhYhuGMEbkTgzc1bbIAD26caH3Z3pKAHRiab5xDEYvAH7uF2ctjgBhDF6Ns4ZBb7Z4De3RpNVWA4dWEFLROhVdXQExCJjKe+F7fudOvfhmzP6DS1/yCFmkLLH27A7Yj1SORVRpFapQKBgBuSrZhAtqenQd4qG+oTxWdwUSGIVo/WUAJE+T2dLID5pcmw1ER8yUDPNAb4v/bZA6tow+vyrBdslFyXT2+yaCS+KlYoEEdtisGiaUfHKpvQDGlmTRuInV7megvoqeOMD4k8suT0434aU2D3lEPqdQ6jxqiyUZGGFwyoVHWxgPOA";
    private static final String PROD_EPAYXX_PUBLIC_KEY    = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOxOjh/1dxibWJumJThn8OkrKgTWMsCpy/5tLQ52oDyahvbLu2e7eNOj4+06clOKJReE7touHsTpNxh7ZCNCUEhRxQbsBF0KELjhaRHs2QGVtI4KDofsFhHG/6zHnNo1RP6jsfBFnZENo3PCbT6O0wdOyS1Yg6vYJJM7LIaiT5gQIDAQAB";

    /**
     * 易支付开放平台网关地址
     * 
     * @return
     */
    public static String getServerUrl() {
        if (ENV.equals("test")) {
            return TEST_SERVER_URL;
        } else if (ENV.equals("prod")) {
            return PROD_SERVER_URL;
        }
        return "";
    }

    /**
     * 易支付分配的合作伙伴编号
     * 
     * @return
     */
    public static String getPartnerId() {
        if (ENV.equals("test")) {
            return TEST_PARTNER_ID;
        } else if (ENV.equals("prod")) {
            return PROD_PARTNER_ID;
        }
        return "";
    }

    /**
     * 商户私钥
     * 
     * @return
     */
    public static String getPKCS8PrivateKey() {
        if (ENV.equals("test")) {
            return TEST_MERCHANT_PRIVATE_KEY;
        } else if (ENV.equals("prod")) {
            return PROD_MERCHANT_PRIVATE_KEY;
        }
        return "";
    }

    /**
     * 易支付公钥
     * 
     * @return
     */
    public static String getPKCS8PublicKey() {
        if (ENV.equals("test")) {
            return TEST_EPAYXX_PUBLIC_KEY;
        } else if (ENV.equals("prod")) {
            return PROD_EPAYXX_PUBLIC_KEY;
        }
        return "";
    }

    public static String getNotifyUrl() {
        if (ENV.equals("test")) {
            return TEST_NOTIFY_URL;
        } else if (ENV.equals("prod")) {
            return PROD_NOTIFY_URL;
        }
        return "";
    }

    public static String getReturnUrl() {
        if (ENV.equals("test")) {
            return TEST_RETURN_URL;
        } else if (ENV.equals("prod")) {
            return PROD_RETURN_URL;
        }
        return "";
    }

}
