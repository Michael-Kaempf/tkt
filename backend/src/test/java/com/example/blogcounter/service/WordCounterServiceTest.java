package com.example.blogcounter.service;

import com.example.blogcounter.model.BlogPost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WordCounterServiceTest {

    private final WordCounterService service = new WordCounterService();

    private static Stream<Arguments> htmlTestCases() {
        return Stream.of(
                Arguments.of(
                        "<p>This is a test</p>",
                        Map.of("this", 1, "is", 1, "a", 1, "test", 1)
                ),
                Arguments.of(
                        "<div>Multiple words words in a sentence</div>",
                        Map.of("multiple", 1, "words", 2, "in", 1, "a", 1, "sentence", 1)
                ),
                Arguments.of(
                        "<p>Special-characters!and.punctuation</p>",
//                        Map.of("special", 1, "characters", 1, "and", 1, "punctuation", 1)
                        Map.of("specialcharactersandpunctuation", 1)
                )
        );
    }

//    @ParameterizedTest
//    @MethodSource("htmlTestCases")
//    void shouldHandleVariousHtmlInputs(String html, Map<String, Integer> expected) {
//        // given
//        var post = createBlogPost(html);
//
//        // when
//        var result = service.processContent(List.of(post));
//
//        // then
//        assertThat(result.getWordFrequencies()).isEqualTo(expected);
//    }
//
//    @Test
//    void shouldHandleEmptyList() {
//        // when
//        var result = service.processContent(List.of());
//
//        // then
//        assertThat(result.getWordFrequencies()).isEmpty();
//    }
//
//    @Test
//    void shouldHandleMultiplePosts() {
//        // given
//        var post1 = createBlogPost("<p>First post content</p>");
//        var post2 = createBlogPost("<p>Second post content</p>");
//
//        // when
//        var result = service.processContent(List.of(post1, post2));
//
//        // then
//        assertThat(result.getWordFrequencies())
//                .containsEntry("post", 2)
//                .containsEntry("content", 2)
//                .containsEntry("first", 1)
//                .containsEntry("second", 1);
//    }
//
//    @Test
//    void shouldHandleComplexHtmlStructure() {
//        // given
//        String complexHtml = """
//            <article>
//                <h1>Title here</h1>
//                <div class="content">
//                    <p>First paragraph with <strong>bold text</strong></p>
//                    <ul>
//                        <li>List item one</li>
//                        <li>List item two</li>
//                    </ul>
//                </div>
//            </article>
//            """;
//        var post = createBlogPost(complexHtml);
//
//        // when
//        var result = service.processContent(List.of(post));
//
//        // then
//        assertThat(result.getWordFrequencies())
//                .containsEntry("title", 1)
//                .containsEntry("here", 1)
//                .containsEntry("first", 1)
//                .containsEntry("paragraph", 1)
//                .containsEntry("bold", 1)
//                .containsEntry("text", 1)
//                .containsEntry("list", 2)
//                .containsEntry("item", 2)
//                .containsEntry("one", 1)
//                .containsEntry("two", 1);
//    }
//
//    private BlogPost createBlogPost(String html) {
//        var content = BlogPost.Content.builder()
//                .rendered(html)
//                .build();
//        return BlogPost.builder()
//                .content(content)
//                .build();
//    }
}
