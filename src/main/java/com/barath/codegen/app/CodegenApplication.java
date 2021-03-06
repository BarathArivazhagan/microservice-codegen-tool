package com.barath.codegen.app;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.spring.initializr.web.autoconfigure.InitializrAutoConfiguration;


@SpringBootApplication(exclude={InitializrAutoConfiguration.class})
@EnableCaching
public class CodegenApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodegenApplication.class, args);
	}
	
	

	@Configuration
	@EnableAsync
	static class AsyncConfiguration extends AsyncConfigurerSupport {

		@Override
		public Executor getAsyncExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(1);
			executor.setMaxPoolSize(5);
			executor.setThreadNamePrefix("initializr-");
			executor.initialize();
			return executor;
		}

	}
}
