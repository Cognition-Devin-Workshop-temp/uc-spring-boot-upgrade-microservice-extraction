package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  private DuplicatedArticleValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedArticleValidator();
    ReflectionTestUtils.setField(validator, "articleQueryService", articleQueryService);
  }

  @Test
  void should_return_true_when_title_is_unique() {
    when(articleQueryService.findBySlug(eq("unique-title"), any())).thenReturn(Optional.empty());

    boolean result = validator.isValid("Unique Title", null);

    assertTrue(result);
  }

  @Test
  void should_return_false_when_title_is_duplicated() {
    ArticleData existing = mock(ArticleData.class);
    when(articleQueryService.findBySlug(eq("existing-title"), any()))
        .thenReturn(Optional.of(existing));

    boolean result = validator.isValid("Existing Title", null);

    assertFalse(result);
  }

  @Test
  void should_handle_title_with_special_characters() {
    when(articleQueryService.findBySlug(any(), any())).thenReturn(Optional.empty());

    boolean result = validator.isValid("Title With Special Chars!@#", null);

    assertTrue(result);
  }
}
