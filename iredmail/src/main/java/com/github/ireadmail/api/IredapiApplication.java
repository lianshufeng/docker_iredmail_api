package com.github.ireadmail.api;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.github.ireadmail.api.core")
@SpringBootApplication
public class IredapiApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(IredapiApplication.class, args);
    }


}
