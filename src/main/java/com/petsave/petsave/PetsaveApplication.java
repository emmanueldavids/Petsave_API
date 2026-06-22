// package com.petsave.petsave;

// import io.github.cdimascio.dotenv.Dotenv;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// @SpringBootApplication
// @EnableWebSecurity
// public class PetsaveApplication {

// 	public static void main(String[] args) {
// 		Dotenv dotenv = Dotenv.configure().load();
// 		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
// 		SpringApplication.run(PetsaveApplication.class, args);
// 	}

// }


package com.petsave.petsave;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
@EnableAsync
public class PetsaveApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.ignoreIfMalformed()
				.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);

		SpringApplication.run(PetsaveApplication.class, args);
	}

}