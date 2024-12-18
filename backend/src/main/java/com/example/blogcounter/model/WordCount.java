package com.example.blogcounter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class WordCount {
    private Map<String, Integer> wordFrequencies;
}
