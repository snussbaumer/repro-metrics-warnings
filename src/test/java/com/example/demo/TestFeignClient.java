package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "TestFeignClient", name = "TestFeignClient", url = "http://localhost:9004")
public interface TestFeignClient {

    @GetMapping("/outside/test")
    String test();

}
