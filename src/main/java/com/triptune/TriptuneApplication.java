package com.triptune;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TriptuneApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriptuneApplication.class, args);
	}

}
