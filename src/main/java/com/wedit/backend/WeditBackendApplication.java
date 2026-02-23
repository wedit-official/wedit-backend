package com.wedit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WeditBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeditBackendApplication.class, args);
	}

}
