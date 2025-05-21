package com.ftn.uns.ac.rs.smarthomesimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SmartHomeSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHomeSimulatorApplication.class, args);
	}
}
