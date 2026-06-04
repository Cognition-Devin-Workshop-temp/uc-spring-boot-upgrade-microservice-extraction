package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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

  private User creator;

  @BeforeEach
  public void setUp() {
    creator = new User("test@test.com", "testuser", "password", "", "");
  }

  @Test
  public void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals(creator.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("New Desc", updated.getDescription());
    assertEquals("New Body", updated.getBody());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_return_article_on_update() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("Updated Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
  }
}
