package com.example.blogcounter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class WordCounterServiceTest {
    private final WordCounterService wordCounterService = new WordCounterService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
                arguments(
                        "<p>Hello world</p>",
                        Map.of("hello", 1, "world", 1)
                ),
                arguments(
                        "<p>Hello, hello HELLO!</p>",
                        Map.of("hello", 3)
                ),
                arguments(
                        "<p>Special-chars!@#$ should be removed</p>",
                        Map.of("special", 1, "chars", 1, "should", 1, "be", 1, "removed", 1)
                )
        );
    }

//    @ParameterizedTest
//    @MethodSource("provideTestCases")
//    @DisplayName("Should process different HTML content correctly")
//    void processContent_ValidCases(String htmlContent, Map<String, Integer> expected) throws Exception {
//        // Given
//        String json = String.format("""
//            {
//                "content": {
//                    "rendered": "%s"
//                }
//            }
//        """, htmlContent);
//        JsonNode node = objectMapper.readTree(json);
//
//        // When
//        Map<String, Integer> result = wordCounterService.processContent(List.of(node));
//
//        // Then
//        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
//    }
//
//    @Test
//    @DisplayName("Should handle empty content")
//    void processContent_EmptyContent() throws Exception {
//        // Given
//        String json = """
//            {
//                "content": {
//                    "rendered": ""
//                }
//            }
//        """;
//        JsonNode node = objectMapper.readTree(json);
//
//        // When
//        Map<String, Integer> result = wordCounterService.processContent(List.of(node));
//
//        // Then
//        assertThat(result).isEmpty();
//    }
}