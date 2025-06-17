package com.petsave.petsave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class PetsaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetsaveApplication.class, args);
	}

}
