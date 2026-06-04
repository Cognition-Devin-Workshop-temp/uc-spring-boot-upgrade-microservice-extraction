package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
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
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
  }

  @Test
  public void should_get_feed_with_first_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(1, result.getData().getEdges().size());
      assertEquals("test-slug", result.getData().getEdges().get(0).getNode().getSlug());
    }
  }

  @Test
  public void should_get_feed_with_last_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(null, null, 10, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_throw_when_feed_missing_first_and_last() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(
          IllegalArgumentException.class,
          () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
    }
  }

  @Test
  public void should_get_feed_with_anonymous_user() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findUserFeedWithCursor(isNull(), any(CursorPageParameter.class)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(0, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_get_user_feed_with_first_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_get_user_feed_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFeed(null, null, 5, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_throw_when_user_feed_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("unknown").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFavorites(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_get_user_favorites_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFavorites(null, null, 5, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_throw_when_user_favorites_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), eq("testuser"), isNull(), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userArticles(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  public void should_get_user_articles_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), eq("testuser"), isNull(), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userArticles(null, null, 5, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  public void should_throw_when_user_articles_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  public void should_get_articles_with_first_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              eq("java"), eq("testuser"), eq("fav"), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(10, null, null, null, "testuser", "fav", "java", dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
      assertTrue(result.getData().getPageInfo().isHasNextPage());
    }
  }

  @Test
  public void should_get_articles_with_last_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), isNull(), any(CursorPageParameter.class), eq(user)))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  public void should_throw_when_articles_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_article_from_article_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("test-slug", result.getData().getSlug());
    }
  }

  @Test
  public void should_throw_when_article_not_found_in_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
    }
  }

  @Test
  public void should_get_comment_article() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq("article-id"), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result =
          articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("test-slug", result.getData().getSlug());
    }
  }

  @Test
  public void should_throw_when_comment_article_not_found() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findById(eq("article-id"), isNull())).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.getCommentArticle(dataFetchingEnvironment));
    }
  }

  @Test
  public void should_find_article_by_slug() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findBySlug(eq("test-slug"), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

      assertNotNull(result);
      assertEquals("test-slug", result.getData().getSlug());
      assertEquals("Test Title", result.getData().getTitle());
      assertEquals("body", result.getData().getBody());
    }
  }

  @Test
  public void should_throw_when_slug_not_found() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findBySlug(eq("no-such-slug"), eq(user)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.findArticleBySlug("no-such-slug"));
    }
  }

  @Test
  public void should_build_page_info_with_empty_pager() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), isNull(), any(CursorPageParameter.class), isNull()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(0, result.getData().getEdges().size());
      assertFalse(result.getData().getPageInfo().isHasNextPage());
      assertFalse(result.getData().getPageInfo().isHasPreviousPage());
    }
  }
}
