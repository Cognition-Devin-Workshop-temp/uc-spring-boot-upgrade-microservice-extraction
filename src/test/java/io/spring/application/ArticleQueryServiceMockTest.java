package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceMockTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  @InjectMocks private ArticleQueryService articleQueryService;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList("java"),
            new ProfileData("author-id", "author", "bio", "img", false));
  }

  @Test
  void should_find_by_id_success_with_user() {
    when(articleReadService.findById(eq("article-1"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq("article-1")))
        .thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(eq("article-1"))).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findById("article-1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_find_by_id_success_without_user() {
    when(articleReadService.findById(eq("article-1"))).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("article-1", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_find_by_id_not_found() {
    when(articleReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_slug_success_with_user() {
    when(articleReadService.findBySlug(eq("test-slug"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq("article-1")))
        .thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(eq("article-1"))).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFavorited());
  }

  @Test
  void should_find_by_slug_without_user() {
    when(articleReadService.findBySlug(eq("test-slug"))).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_find_by_slug_not_found() {
    when(articleReadService.findBySlug(eq("nonexistent"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor() {
    List<String> articleIds = new ArrayList<>(Arrays.asList("article-1"));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(eq("java"), eq(null), eq(null), any()))
        .thenReturn(articleIds);
    when(articleReadService.findArticles(eq(articleIds))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 3)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user)))
        .thenReturn(new HashSet<>(Arrays.asList("article-1")));
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor("java", null, null, page, user);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).isFavorited());
    assertEquals(3, result.getData().get(0).getFavoritesCount());
  }

  @Test
  void should_return_empty_when_no_articles_with_cursor() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_handle_has_extra_articles_with_cursor() {
    ArticleData article2 =
        new ArticleData(
            "article-2",
            "slug-2",
            "title2",
            "desc2",
            "body2",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList(),
            new ProfileData("author-id", "author", "bio", "img", false));
    List<String> articleIds = new ArrayList<>(Arrays.asList("article-1", "article-2"));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(articleIds);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertNotNull(result);
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_recent_articles_with_cursor_prev_direction() {
    List<String> articleIds = new ArrayList<>(Arrays.asList("article-1"));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(articleIds);
    when(articleReadService.findArticles(eq(articleIds))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_user_feed_with_cursor() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("followed-user"));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_return_empty_feed_when_no_followed_users() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_prev_direction() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("followed-user"));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertNotNull(result);
  }

  @Test
  void should_find_recent_articles_page() {
    List<String> articleIds = Arrays.asList("article-1");
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(eq("java"), eq(null), eq(null), any()))
        .thenReturn(articleIds);
    when(articleReadService.countArticle(eq("java"), eq(null), eq(null))).thenReturn(1);
    when(articleReadService.findArticles(eq(articleIds))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findRecentArticles("java", null, null, page, user);

    assertNotNull(result);
    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }

  @Test
  void should_return_empty_recent_articles_when_no_ids() {
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertNotNull(result);
    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_user_feed_page() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("followed-user"));
    Page page = new Page(0, 10);
    when(articleReadService.findArticlesOfAuthors(any(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(any())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertNotNull(result);
    assertEquals(1, result.getArticleDatas().size());
  }

  @Test
  void should_return_empty_user_feed_when_no_followed_users() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());
    Page page = new Page(0, 10);

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertNotNull(result);
    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_recent_articles_without_user() {
    List<String> articleIds = Arrays.asList("article-1");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(articleIds);
    when(articleReadService.findArticles(eq(articleIds))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }
}
