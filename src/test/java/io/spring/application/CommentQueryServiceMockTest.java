package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
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
public class CommentQueryServiceMockTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    commentData =
        new CommentData(
            "comment-1",
            "body",
            "article-1",
            DateTime.now(),
            DateTime.now(),
            new ProfileData("author-id", "author", "bio", "img", false));
  }

  @Test
  void should_find_comment_by_id_success() {
    when(commentReadService.findById(eq("comment-1"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_by_article_id_with_user() {
    CommentData comment2 =
        new CommentData(
            "comment-2",
            "body2",
            "article-1",
            DateTime.now(),
            DateTime.now(),
            new ProfileData("author2-id", "author2", "bio2", "img2", false));
    when(commentReadService.findByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(commentData, comment2));
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    List<CommentData> result = commentQueryService.findByArticleId("article-1", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    when(commentReadService.findByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-1", null);

    assertEquals(1, result.size());
    verify(userRelationshipQueryService, never()).followingAuthors(any(), any());
  }

  @Test
  void should_find_comments_by_article_id_with_empty_list() {
    when(commentReadService.findByArticleId(eq("article-1"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_with_cursor_and_next_direction() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-1"), any())).thenReturn(comments);
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_find_comments_with_cursor_and_prev_direction() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("article-1"), any())).thenReturn(comments);
    Set<String> following = new HashSet<>();
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_handle_has_extra_in_cursor_pager() {
    CommentData comment2 =
        new CommentData(
            "comment-2",
            "body2",
            "article-1",
            DateTime.now(),
            DateTime.now(),
            new ProfileData("author2-id", "author2", "bio2", "img2", false));
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData, comment2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-1"), any())).thenReturn(comments);
    Set<String> following = new HashSet<>();
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_comments_with_cursor_without_user() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-1"), any())).thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    verify(userRelationshipQueryService, never()).followingAuthors(any(), any());
  }
}
