package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article =
        new Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId(), new DateTime());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_taglist() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_fail_create_article_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    String slug = article.getSlug();
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .description("updated desc")
            .body("updated body")
            .build();
    when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    var result = articleMutation.updateArticle(slug, params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_fail_update_article_when_not_found() {
    when(articleRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () ->
            articleMutation.updateArticle(
                "non-existent", UpdateArticleInput.newBuilder().title("x").build()));
  }

  @Test
  void should_fail_update_article_when_not_author() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other", "desc", "body", Arrays.asList(), otherUser.getId(), new DateTime());
    when(articleRepository.findBySlug(otherArticle.getSlug()))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () ->
            articleMutation.updateArticle(
                otherArticle.getSlug(), UpdateArticleInput.newBuilder().title("x").build()));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));

    var result = articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_fail_favorite_article_when_not_found() {
    when(articleRepository.findBySlug("no-slug")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("no-slug"));
  }

  @Test
  void should_unfavorite_article_success() {
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    var result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_article_when_no_favorite_exists() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    var result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_fail_delete_article_when_not_author() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other", "desc", "body", Arrays.asList(), otherUser.getId(), new DateTime());
    when(articleRepository.findBySlug(otherArticle.getSlug()))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(otherArticle.getSlug()));
  }

  @Test
  void should_fail_delete_article_when_not_found() {
    when(articleRepository.findBySlug("no-slug")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("no-slug"));
  }
}
