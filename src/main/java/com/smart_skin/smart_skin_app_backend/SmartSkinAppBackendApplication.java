package com.smart_skin.smart_skin_app_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartSkinAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSkinAppBackendApplication.class, args);
    }

}