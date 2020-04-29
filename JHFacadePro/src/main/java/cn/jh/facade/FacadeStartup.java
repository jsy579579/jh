package cn.jh.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.sql.DataSource;

import org.apache.catalina.connector.Connector;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import cn.jh.common.exception.ApiExceptionHandler;
import cn.jh.common.interceptor.AuthenticationHeaderInterceptor;


@SpringBootApplication
@EnableDiscoveryClient
public class FacadeStartup extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws Exception {
		List<String> params = new ArrayList<>();
		StringBuffer argsSB = new StringBuffer();
		String argsS = "";
		String logPath = "logs/facade.log";;
		for (String arg : args) {
			argsSB.append(arg+";");
			params.add(arg);
			if (arg.contains("logPath=")) {
				logPath = arg.substring(arg.indexOf("=")+1,arg.length()).trim();
			}
		}
		argsS = argsSB.toString();
		if (!argsS.contains("--spring.profiles.active")) {
			params.add("--spring.profiles.active=dev");
		}
		if (!argsS.contains("--server.port")) {
			params.add("--server.port=0");
		}
		args = params.toArray(new String[params.size()]);
//		new SpringApplicationBuilder(FacadeStartup.class).properties("server.port="+port).run(args);
		SpringApplication.run(FacadeStartup.class, args);
		Properties logProp = new Properties();
		logProp.load(FacadeStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
		logProp.setProperty("log4j.appender.file.file",logPath);
		PropertyConfigurator.configure(logProp);
		
	}

	@Bean
	public Filter characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter =new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		
		return characterEncodingFilter;
	}
	
	
	@Bean  
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("12800000KB"); 
        factory.setMaxRequestSize("12800000KB"); 
        return factory.createMultipartConfig();  
    }
    
	
	@Bean
	public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
	    return new EmbeddedServletContainerCustomizer() {
	        @Override 
	        public void customize(ConfigurableEmbeddedServletContainer container) {
	            TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
	            tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {

					@Override
					public void customize(Connector connector) {
						connector.setMaxPostSize(100000000);
					}
	            	
	            });
	        
	        }
	    };
	}
	
	
	@Autowired
	private Environment env;

	@Bean
	public DataSource getDataSource() throws Exception {
		Properties props = new Properties();

		props.put("driverClassName",
				env.getProperty("spring.datasource.driver-class-name"));
		props.put("url", env.getProperty("spring.datasource.url"));
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

}
