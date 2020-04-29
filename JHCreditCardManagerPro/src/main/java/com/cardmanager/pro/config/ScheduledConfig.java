package com.cardmanager.pro.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
@Configuration
public class ScheduledConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(setTaskExecutors());
	}

	@Bean(destroyMethod="shutdown")
	public Executor setTaskExecutors(){
	    return Executors.newScheduledThreadPool(3); // 3个线程来处理。
	}
}
