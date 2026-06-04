package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "", "");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
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

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  private void setAnonymousAuth() {
    AnonymousAuthenticationToken anonymous =
        new AnonymousAuthenticationToken(
            "key", "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymous);
  }

  @Test
  void should_throw_when_unauthenticated_create() {
    setAnonymousAuth();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("t").description("d").body("b").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").body("new body").description("new desc").build();
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), params);

    assertNotNull(result);
  }

  @Test
  void should_throw_not_found_on_update() {
    when(articleRepository.findBySlug("bad-slug")).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("t").build();

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("bad-slug", params));
  }

  @Test
  void should_throw_no_authorization_on_update() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug(otherArticle.getSlug()))
        .thenReturn(Optional.of(otherArticle));
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("t").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(otherArticle.getSlug(), params));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_unauthenticated_favorite() {
    setAnonymousAuth();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_throw_not_found_on_favorite() {
    when(articleRepository.findBySlug("bad")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("bad"));
  }

  @Test
  void should_unfavorite_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_throw_when_unauthenticated_unfavorite() {
    setAnonymousAuth();
    assertThrows(AuthenticationException.class, () -> articleMutation.unfavoriteArticle("slug"));
  }

  @Test
  void should_throw_not_found_on_unfavorite() {
    when(articleRepository.findBySlug("bad")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> articleMutation.unfavoriteArticle("bad"));
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_unauthenticated_delete() {
    setAnonymousAuth();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }

  @Test
  void should_throw_not_found_on_delete() {
    when(articleRepository.findBySlug("bad")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("bad"));
  }

  @Test
  void should_throw_no_authorization_on_delete() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug(otherArticle.getSlug()))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(otherArticle.getSlug()));
  }
}
