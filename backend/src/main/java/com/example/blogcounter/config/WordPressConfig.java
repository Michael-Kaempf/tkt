package com.example.blogcounter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wordpress.api")
@Data
public class WordPressConfig {
    private String baseUrl;
    private int fetchInterval;
    private int postsPerPage;
}
