package com.jh.good;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@SpringBootApplication
@EnableDiscoveryClient
public class GoodStartup extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws Exception {
		List<String> params = new ArrayList<>();
		StringBuffer argsSB = new StringBuffer();
		String argsS = "";
		String logPath = "logs/good.log";
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
		args = params.toArray(new String[params.size()]);
//		new SpringApplicationBuilder(UserStartup.class).properties(propertiesMap).run(args);
		SpringApplication.run(GoodStartup.class, args);
		Properties logProp = new Properties();
		logProp.load(GoodStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
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

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthenticationHeaderInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public ApiExceptionHandler getApiExceptionHander() {
		return new ApiExceptionHandler();
	}

	@Bean
	public Executor myAsync() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(6);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(8192);
		executor.setThreadNamePrefix("MyExecutor-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}



}
