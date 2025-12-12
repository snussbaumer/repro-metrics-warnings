package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RestController
@EnableFeignClients
@RequiredArgsConstructor
public class TestApplication {

    private final TestFeignClient testFeignClient;

    @GetMapping("/test")
    public String test() {
        return testFeignClient.test();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
