package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
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
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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
    user = new User("test@test.com", "testuser", "password", "", "");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());

    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
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
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_create_article_fail_unauthenticated() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);
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
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("new body")
            .description("new desc")
            .build();
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
  }

  @Test
  void should_update_article_fail_not_found() {
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("non-existent", changes));
  }

  @Test
  void should_update_article_fail_unauthorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();
    when(articleRepository.findBySlug(eq("other-article"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("other-article", changes));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_favorite_article_fail_not_found() {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("non-existent"));
  }

  @Test
  void should_favorite_article_fail_unauthenticated() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);

    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle("test-title"));
  }

  @Test
  void should_unfavorite_article_success() {
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  void should_unfavorite_article_when_no_favorite_exists() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void should_delete_article_fail_not_found() {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("non-existent"));
  }

  @Test
  void should_delete_article_fail_unauthorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("other-article"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("other-article"));
  }
}
