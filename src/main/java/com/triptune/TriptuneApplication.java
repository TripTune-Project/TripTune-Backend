package com.triptune;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TriptuneApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriptuneApplication.class, args);
	}

}
