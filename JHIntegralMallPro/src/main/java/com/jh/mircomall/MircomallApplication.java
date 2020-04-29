package com.jh.mircomall;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;

@SpringBootApplication
@EnableDiscoveryClient
public class MircomallApplication {

	public static void main(String[] args) throws Exception {
		String port = "5558";
		String logPath = "logs/integralmall.log";
		if (args.length > 1) {
			port = args[1];
			if (args.length > 2) {
				logPath = args[2];
			} else {
				logPath = "logs/integralmall.log";
			}
		} else {
			port = "5558";
		}
		new SpringApplicationBuilder(MircomallApplication.class).properties("server.port=" + port).run(args);
		Properties logProp = new Properties();
		logProp.load(MircomallApplication.class.getClassLoader().getResourceAsStream("log4j.properties"));
		logProp.setProperty("log4j.appender.file.file", logPath);
		PropertyConfigurator.configure(logProp);
		/* SpringApplication.run(MircomallApplication.class, args); */
	}

	@Autowired
	private Environment env;

	@Bean
	public DataSource getDataSource() throws Exception {
		String dataBaseName = env.getProperty("mysql.dataBaseName");
		Properties props = new Properties();
		props.put("driverClassName", env.getProperty("spring.datasource.driver-class-name"));
		props.put("url", env.getProperty("spring.datasource.url")+"/"+dataBaseName+"?useUnicode=true&characterEncoding=UTF-8");
		props.put("username", env.getProperty("spring.datasource.username"));
		props.put("password", env.getProperty("spring.datasource.password"));
		return DruidDataSourceFactory.createDataSource(props);
	}
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthenticationHeaderInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public ApiExceptionHandler getApiExceptionHander() {
		return new ApiExceptionHandler();
	}
	
}
