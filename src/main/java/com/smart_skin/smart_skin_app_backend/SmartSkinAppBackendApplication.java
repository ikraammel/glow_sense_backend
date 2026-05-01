package com.smart_skin.smart_skin_app_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartSkinAppBackendApplication {

    public static void main(String[] args) {
        System.out.println("DB_URL = " + System.getenv("DB_URL"));
        SpringApplication.run(SmartSkinAppBackendApplication.class, args);
        System.out.println("DB_URL = " + System.getenv("DB_URL"));
    }

}