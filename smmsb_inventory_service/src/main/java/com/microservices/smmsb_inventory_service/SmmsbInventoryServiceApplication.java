package com.microservices.smmsb_inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmmsbInventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmmsbInventoryServiceApplication.class, args);
	}

}
