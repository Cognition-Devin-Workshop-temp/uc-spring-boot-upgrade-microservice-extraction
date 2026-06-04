package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
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
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_feed_with_first() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    var result = articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_feed_with_last() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, true);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    var result = articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_throw_when_feed_both_first_and_last_null() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    var result = articleDatafetcher.getArticles(10, null, null, null, "author", null, "java", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    var result = articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_articles_both_first_and_last_null() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_find_article_by_slug() {
    when(articleQueryService.findBySlug("test-title", user)).thenReturn(Optional.of(articleData));

    var result = articleDatafetcher.findArticleBySlug("test-title");

    assertNotNull(result);
    assertEquals("test-title", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_slug_not_found() {
    when(articleQueryService.findBySlug("no-slug", user)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("no-slug"));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId(), new DateTime());
    when(articleQueryService.findById(coreArticle.getId(), user))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    var result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertEquals("test-title", result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            articleData.getId(),
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(articleQueryService.findById(articleData.getId(), user))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    var result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertEquals("test-title", result.getData().getSlug());
  }

  @Test
  void should_get_user_favorites_with_first() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_favorites_both_null() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_articles_both_null() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_feed_with_first() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last() throws Exception {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    var result = articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_feed_both_null() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("nonexist").build();
    when(userRepository.findByUsername("nonexist")).thenReturn(Optional.empty());

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }
}
