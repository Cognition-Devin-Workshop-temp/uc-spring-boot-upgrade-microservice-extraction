package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  @InjectMocks private DuplicatedArticleValidator validator;

  @Test
  void should_return_true_when_title_not_duplicated() {
    when(articleQueryService.findBySlug("new-title", null)).thenReturn(Optional.empty());

    boolean result = validator.isValid("New Title", null);

    assertTrue(result);
  }

  @Test
  void should_return_false_when_title_duplicated() {
    ArticleData existing = mock(ArticleData.class);
    when(articleQueryService.findBySlug("existing-title", null)).thenReturn(Optional.of(existing));

    boolean result = validator.isValid("Existing Title", null);

    assertFalse(result);
  }
}
