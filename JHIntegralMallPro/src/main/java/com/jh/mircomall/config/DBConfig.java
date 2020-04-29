package com.jh.mircomall.config;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DBConfig {
    @Autowired
    private Environment env;

    @Bean
	public DataSource getDataSource() throws Exception {

		Properties props = new Properties();
		props.put("driverClassName", env.getProperty("spring.datasource.driver-class-name"));
		props.put("url", env.getProperty("spring.datasource.url"));
		props.put("username", env.getProperty("spring.datasource.username"));
		props.put("password", env.getProperty("spring.datasource.password"));
		return DruidDataSourceFactory.createDataSource(props);

	}

}
