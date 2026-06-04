package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
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
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
  }

  private CommentData createCommentData(String id, String authorId) {
    return new CommentData(
        id,
        "comment body",
        "article-id",
        new DateTime(),
        new DateTime(),
        new ProfileData(authorId, "author", "bio", "image", false));
  }

  @Test
  public void should_find_comment_by_id() {
    CommentData commentData = createCommentData("c1", "author-id");
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("not-exist")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("not-exist", user);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_comments_by_article_id_with_following() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    List<CommentData> comments = Arrays.asList(c1, c2);

    when(commentReadService.findByArticleId("article-id")).thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_with_null_user() {
    CommentData c1 = createCommentData("c1", "author1");
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(c1));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
    verify(userRelationshipQueryService, never()).followingAuthors(any(), anyList());
  }

  @Test
  public void should_return_empty_list_when_no_comments() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_next() {
    CommentData c1 = createCommentData("c1", "author1");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(c1));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_by_article_id_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_null_user() {
    CommentData c1 = createCommentData("c1", "author1");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(c1));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    verify(userRelationshipQueryService, never()).followingAuthors(any(), anyList());
  }

  @Test
  public void should_handle_has_extra_for_cursor_next() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  public void should_reverse_results_for_cursor_prev() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertEquals("c2", result.getData().get(0).getId());
    assertEquals("c1", result.getData().get(1).getId());
  }

  @Test
  public void should_handle_has_extra_for_cursor_prev() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.PREV);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasPrevious());
  }
}
