package com.jh.paymentchannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.PropertyConfigurator;
//import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentChannelStartup extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws Exception {
		List<String> params = new ArrayList<>();
		StringBuffer argsSB = new StringBuffer();
		String argsS = "";
		String logPath = "logs/paymentchannel.log";;
		for (String arg : args) {
			argsSB.append(arg+";");
			params.add(arg);
			if (arg.contains("logPath=")) {
				logPath = arg.substring(arg.indexOf("=")+1,arg.length()).trim();
			}
		}
		argsS = argsSB.toString();
		if (argsS.contains("activeScan")) {
			params.add("--schedule-task.on-off=true");
		}else {
			params.add("--schedule-task.on-off=false");
		}
		
		if (!argsS.contains("--spring.profiles.active")) {
			params.add("--spring.profiles.active=dev");
		}
		if (!argsS.contains("--server.port")) {
			params.add("--server.port=0");
		}
//		new SpringApplicationBuilder(PaymentChannelStartup.class).properties(propertiesMap).run(args);
		args = params.toArray(new String[params.size()]);
		SpringApplication.run(PaymentChannelStartup.class, args);
		Properties logProp = new Properties();
		logProp.load(PaymentChannelStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
		logProp.setProperty("log4j.appender.file.file", logPath);
		PropertyConfigurator.configure(logProp);
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

	/*
	 * @Bean public DataSource
	 * druidDataSource(@Value("{spring.datasource.driver-class-name}") String
	 * driver,
	 * 
	 * @Value("{spring.datasource.url}") String url,
	 * 
	 * @Value("{spring.datasource.username}") String username,
	 * 
	 * @Value("{spring.datasource.password}") String password){
	 * 
	 * DruidDataSource druidDataSource = new DruidDataSource();
	 * 
	 * druidDataSource.setDriverClassName(driver); druidDataSource.setUrl(url);
	 * druidDataSource.setUsername(username);
	 * druidDataSource.setPassword(password);
	 * 
	 * try{ druidDataSource.setFilters("stat, wall"); } catch(SQLException e){
	 * e.printStackTrace(); }
	 * 
	 * return druidDataSource; }
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthenticationHeaderInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public ApiExceptionHandler getApiExceptionHander() {
		return new ApiExceptionHandler();
	}

}
