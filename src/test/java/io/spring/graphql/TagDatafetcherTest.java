package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  @InjectMocks private TagDatafetcher tagDatafetcher;

  @Test
  void should_return_all_tags() {
    List<String> tags = Arrays.asList("java", "spring", "graphql");
    when(tagsQueryService.allTags()).thenReturn(tags);

    List<String> result = tagDatafetcher.getTags();

    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
    assertTrue(result.contains("graphql"));
  }

  @Test
  void should_return_empty_list_when_no_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> result = tagDatafetcher.getTags();

    assertTrue(result.isEmpty());
  }
}
