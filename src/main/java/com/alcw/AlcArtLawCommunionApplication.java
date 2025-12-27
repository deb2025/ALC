package com.alcw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.alcw")

public class AlcArtLawCommunionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlcArtLawCommunionApplication.class, args);
	}
}
