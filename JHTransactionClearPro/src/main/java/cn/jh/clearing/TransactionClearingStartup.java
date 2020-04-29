package cn.jh.clearing;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.apache.log4j.PropertyConfigurator;
//import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class TransactionClearingStartup extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws Exception {
		List<String> params = new ArrayList<>();
		StringBuffer argsSB = new StringBuffer();
		String argsS = "";
		String logPath = "logs/transactionclear.log";;
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
//		new SpringApplicationBuilder(TransactionClearingStartup.class).properties(propertiesMap).run(args);
		SpringApplication.run(TransactionClearingStartup.class, args);
		Properties logProp = new Properties();
		logProp.load(TransactionClearingStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
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
	
    @Bean
    public Executor myAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(8192);
        executor.setThreadNamePrefix("MyExecutor-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
