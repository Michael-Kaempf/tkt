package com.example.blogcounter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogPost {
    private Long id;
    @JsonProperty("content")
    private Content content;
    private String date;

    @Builder
    @Data
    public static class Content {
        private String rendered;
    }
}
