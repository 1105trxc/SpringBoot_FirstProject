package com.sboot.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.sboot.website.service.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SpringBootTuan5Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTuan5Application.class, args);
	}
}