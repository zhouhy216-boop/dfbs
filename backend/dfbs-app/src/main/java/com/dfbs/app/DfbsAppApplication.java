package com.dfbs.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DfbsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DfbsAppApplication.class, args);
	}

}
