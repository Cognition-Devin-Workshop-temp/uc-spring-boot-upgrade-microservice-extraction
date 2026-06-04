package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("Updated Title", null, null);

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_body() {
    Article article =
        new Article("Title", "desc", "original body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam(null, "updated body", null);

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("updated body", result.getBody());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_description() {
    Article article =
        new Article("Title", "original desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam(null, null, "updated desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("updated desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_all_fields() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_not_change_article_when_all_params_null() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam(null, null, null);

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("Title", result.getTitle());
    assertEquals("body", result.getBody());
    assertEquals("desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }
}
