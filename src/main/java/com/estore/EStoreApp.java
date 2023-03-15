package com.estore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class EStoreApp {

    public static void main(String[] args) {
        SpringApplication.run(EStoreApp.class, args);
    }

}
