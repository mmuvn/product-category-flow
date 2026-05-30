package swp391.group6.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "swp391.group6")
@EntityScan("swp391.group6.model")
@EnableJpaRepositories("swp391.group6.repository")
public class TreeshopmanagingsystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(TreeshopmanagingsystemApplication.class, args);
	}
}