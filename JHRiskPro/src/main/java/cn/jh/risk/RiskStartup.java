package cn.jh.risk;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

//import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;

import com.alibaba.druid.pool.DruidDataSourceFactory;

@SpringBootApplication
@EnableDiscoveryClient
public class RiskStartup extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws Exception {
		List<String> params = new ArrayList<>();
		StringBuffer argsSB = new StringBuffer();
		String argsS = "";
		for (String arg : args) {
			params.add(arg);
			argsSB.append(arg+";");
		}
		argsS = argsSB.toString();
		if (!argsS.contains("--spring.profiles.active")) {
			params.add("--spring.profiles.active=dev");
		}
		if (!argsS.contains("--server.port")) {
			params.add("--server.port=0");
		}
		args = params.toArray(new String[params.size()]);
		SpringApplication.run(RiskStartup.class, args);

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
