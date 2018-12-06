package com.example.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RemoteService {

    @RestController
    public static class ServiceController {

        @GetMapping("/service1")
        public String hello1(String req) throws InterruptedException {
            Thread.sleep(2000L);
            return req + "/service1";
        }

        @GetMapping("/service2")
        public String hello2(String req) throws InterruptedException {
            Thread.sleep(2000L);
            return req + "/service2";
        }


    }


    public static void main(String[] args) {
        System.setProperty("SERVER_PORT", "8081");
        System.setProperty("server.tomcat.max-threads", "1000");
        SpringApplication.run(RemoteService.class, args);
    }
}
