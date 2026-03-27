package com.alcw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.alcw")
@EnableScheduling
public class AlcArtLawCommunionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlcArtLawCommunionApplication.class, args);
	}
}
