package com.example.LiveChattingApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LiveChattingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveChattingAppApplication.class, args);
	}

}
