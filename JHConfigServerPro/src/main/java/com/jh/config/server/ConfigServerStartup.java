package com.jh.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerStartup {

	public static void main(String[] args) {
        if (args.length == 0) args = new String[] { "--spring.profiles.active=dev,native" };
		SpringApplication.run(ConfigServerStartup.class, args);
	}

}
