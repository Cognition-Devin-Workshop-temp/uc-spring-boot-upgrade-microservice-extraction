package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_find_article_by_slug() {
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
    assertEquals("Test Title", result.getData().getTitle());
  }

  @Test
  void should_throw_not_found_for_bad_slug() {
    when(articleQueryService.findBySlug(eq("bad-slug"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("bad-slug"));
  }

  @Test
  void should_get_feed_with_first() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_first_and_last_null_on_feed() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_feed_with_last() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_first() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_first_and_last_null_on_articles() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }
}
