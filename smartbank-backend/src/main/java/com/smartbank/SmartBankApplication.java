package com.smartbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBankApplication.class, args);
    }
}
