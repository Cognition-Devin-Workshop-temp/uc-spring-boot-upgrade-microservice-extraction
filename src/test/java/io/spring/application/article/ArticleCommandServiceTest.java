package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  @Test
  void should_create_article_success() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test description")
            .body("Article body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertEquals("test-article", result.getSlug());
    assertEquals("A test description", result.getDescription());
    assertEquals("Article body content", result.getBody());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertEquals("no-tags-article", result.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_success() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param =
        new UpdateArticleParam("Updated Title", "updated body", "updated desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("updated-title", result.getSlug());
    assertEquals("updated body", result.getBody());
    assertEquals("updated desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_partial_fields() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "only description update");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    verify(articleRepository).save(article);
  }
}
