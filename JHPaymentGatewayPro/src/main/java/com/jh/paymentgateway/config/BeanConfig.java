
package com.jh.paymentgateway.config;

import com.lycheepay.gateway.client.GBPService;
import com.lycheepay.gateway.client.KftService;
import com.lycheepay.gateway.client.security.KeystoreSignProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description
 */

@Configuration
public class BeanConfig {
    @Value("${gbp.clientIp}")
    private String GBPClientIp;

    @Value("${kft.clientIp}")
    private String KFTClientIp;

    @Value("${gbp.language}")
    private String language;

    @Value("${gbp.tempZipFilePath}")
    private String tempZipFilePath;

    @Value("${gbp.severDomain}")
    private String gbpSeverDomain;

    @Value("${gbp.severPort}")
    private String gbpSeverPort;

    @Value("${kft.connectionTimeoutSeconds}")
    private String connectionTimeoutSeconds;

    @Value("${kft.responseTimeoutSeconds}")
    private String responseTimeoutSeconds;

    @Value("${kft.kftSeverDomain}")
    private String kftSeverDomain;

    @Value("${kft.kftSeverPort}")
    private String kftSeverPort;

    @Value("${gbp.keyStoreType}")
    private String keyStoreType;

    @Value("${gbp.keyStorePath}")
    private String keyStorePath;

    @Value("${gbp.keyStorePassword}")
    private String keyStorePassword;

    @Value("${gbp.alias}")
    private String alias;

    @Value("${gbp.keyPassword}")
    private String keyPassword;

    @Bean
    public GBPService GBPService() throws Exception{
        GBPService service = new GBPService(keystoreSignProvider(),GBPClientIp,language,tempZipFilePath);
        service.setHttpDomain(gbpSeverDomain);// 测试服务器地址
        service.setHttpPort(Integer.valueOf(gbpSeverPort));// 测试环境端口6443,生产环境端口8443,不设置默认8443
        service.setConnectionTimeoutSeconds(Integer.valueOf(connectionTimeoutSeconds));// 设置的交易连接超时时间要大于0小于2分钟,单位:秒.0表示不超时,一直等待,默认30秒
        service.setResponseTimeoutSeconds(Integer.valueOf(responseTimeoutSeconds));// 设置的交易响应超时时间,要大于0小于10分钟,单位:秒.0表示不超时,一直等待,默认5分钟,ps：对应获取对账文件这个应该设长一点时间
        return service;
    }

    @Bean
    public KftService kftService() throws Exception{
        KftService kftService=new KftService(keystoreSignProvider(), KFTClientIp, language, tempZipFilePath);
        kftService.setHttpDomain(kftSeverDomain);// 测试服务器地址
        kftService.setHttpPort(Integer.valueOf(kftSeverPort));// 测试环境端口6443,生产环境端口8443,不设置默认8443
        kftService.setConnectionTimeoutSeconds(Integer.valueOf(connectionTimeoutSeconds));// 设置的交易连接超时时间要大于0小于2分钟,单位:秒.0表示不超时,一直等待,默认30秒
        kftService.setResponseTimeoutSeconds(Integer.valueOf(responseTimeoutSeconds));// 设置的交易响应超时时间,要大于0小于10分钟,单位:秒.0表示不超时,一直等待,默认5分钟,ps：对应获取对账文件这个应该设长一点时间
        return kftService;
    }

    // 证书类型、证书路径、证书密码、别名、证书容器密码
    @Bean
    public KeystoreSignProvider keystoreSignProvider() throws Exception {
        String os = System.getProperty("os.name");
        String keyStorePath = "/product/deploy/kft/pfx.pfx";
        if(os.toLowerCase().startsWith("win")) {
            keyStorePath = "D:/pfx.pfx";
        }
        return new KeystoreSignProvider(
                "PKCS12", keyStorePath, "shbyt0729A".toCharArray(), null,
                "shbyt0729A".toCharArray());
        // D:/pfx.pfx   测试地址
        //  /product/deploy/kft/pfx.pfx   生产地址
    }

}

