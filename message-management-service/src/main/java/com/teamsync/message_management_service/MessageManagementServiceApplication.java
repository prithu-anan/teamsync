package com.teamsync.message_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MessageManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageManagementServiceApplication.class, args);
	}

}
