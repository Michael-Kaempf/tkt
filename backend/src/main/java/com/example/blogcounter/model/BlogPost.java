package com.example.blogcounter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    private Long id;
    @JsonProperty("content")
    private Content content;
    private String date;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String rendered;
    }
}
