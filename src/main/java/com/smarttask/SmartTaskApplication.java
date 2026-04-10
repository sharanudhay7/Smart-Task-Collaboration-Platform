package com.smarttask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartTaskApplication.class, args);
    }
}