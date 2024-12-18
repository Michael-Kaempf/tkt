package com.example.blogcounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlogWordCounterApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogWordCounterApplication.class, args);
    }
}