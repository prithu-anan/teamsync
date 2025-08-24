package com.teamsync.projectmanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProjectManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectManagementServiceApplication.class, args);
	}

}
