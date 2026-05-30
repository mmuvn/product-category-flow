package swp391.group6.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "swp391.group6")
@EntityScan(basePackages = "swp391.group6.entity")
@EnableJpaRepositories(basePackages = "swp391.group6.repository")
public class TreeshopmanagingsystemApplication {

	public static void main(String[] args) {
		// Windows uses "Asia/Saigon", which PostgreSQL rejects on connect.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(TreeshopmanagingsystemApplication.class, args);
	}

}
