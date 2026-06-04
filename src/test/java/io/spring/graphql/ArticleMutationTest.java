package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    article =
        new Article("Test Title", "desc", "body", Arrays.asList("java", "spring"), user.getId());
  }

  @Test
  public void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(article, result.getLocalContext());
      verify(articleCommandService).createArticle(any(), eq(user));
    }
  }

  @Test
  public void should_create_article_with_null_tags() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

      assertNotNull(result);
    }
  }

  @Test
  public void should_throw_when_creating_article_unauthenticated() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
    }
  }

  @Test
  public void should_update_article_success() {
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("new body")
            .description("new desc")
            .build();

    Article updatedArticle =
        new Article("New Title", "new desc", "new body", Collections.emptyList(), user.getId());

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

      DataFetcherResult<ArticlePayload> result =
          articleMutation.updateArticle(article.getSlug(), changes);

      assertNotNull(result);
      assertEquals(updatedArticle, result.getLocalContext());
    }
  }

  @Test
  public void should_throw_when_updating_article_not_found() {
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq("no-slug"))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleMutation.updateArticle("no-slug", changes));
    }
  }

  @Test
  public void should_throw_when_updating_article_unauthenticated() {
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      assertThrows(
          AuthenticationException.class,
          () -> articleMutation.updateArticle(article.getSlug(), changes));
    }
  }

  @Test
  public void should_throw_when_updating_article_unauthorized() {
    User otherUser = new User("other@test.com", "other", "123", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Collections.emptyList(), otherUser.getId());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
          .thenReturn(Optional.of(otherArticle));

      assertThrows(
          NoAuthorizationException.class,
          () -> articleMutation.updateArticle(otherArticle.getSlug(), changes));
    }
  }

  @Test
  public void should_favorite_article_success() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

      assertNotNull(result);
      assertEquals(article, result.getLocalContext());
      verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
    }
  }

  @Test
  public void should_throw_when_favorite_article_unauthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.favoriteArticle("some-slug"));
    }
  }

  @Test
  public void should_throw_when_favorite_article_not_found() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq("no-slug"))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("no-slug"));
    }
  }

  @Test
  public void should_unfavorite_article_success() {
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
          .thenReturn(Optional.of(fav));

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository).remove(fav);
    }
  }

  @Test
  public void should_unfavorite_article_when_no_favorite_exists() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
          .thenReturn(Optional.empty());

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository, never()).remove(any());
    }
  }

  @Test
  public void should_throw_when_unfavorite_article_unauthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.unfavoriteArticle("some-slug"));
    }
  }

  @Test
  public void should_delete_article_success() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

      assertNotNull(result);
      assertTrue(result.getSuccess());
      verify(articleRepository).remove(article);
    }
  }

  @Test
  public void should_throw_when_deleting_article_unauthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("some-slug"));
    }
  }

  @Test
  public void should_throw_when_deleting_article_not_found() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq("no-slug"))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("no-slug"));
    }
  }

  @Test
  public void should_throw_when_deleting_article_unauthorized() {
    User otherUser = new User("other@test.com", "other", "123", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Collections.emptyList(), otherUser.getId());

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
          .thenReturn(Optional.of(otherArticle));

      assertThrows(
          NoAuthorizationException.class,
          () -> articleMutation.deleteArticle(otherArticle.getSlug()));
    }
  }
}
