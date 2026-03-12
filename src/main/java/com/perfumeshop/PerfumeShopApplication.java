package com.perfumeshop;

import com.perfumeshop.config.TelegramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramProperties.class)
public class PerfumeShopApplication {

	public static void main(String[] args) {

		SpringApplication.run(PerfumeShopApplication.class, args);
	}
}