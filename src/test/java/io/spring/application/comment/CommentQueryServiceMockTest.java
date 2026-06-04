package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceMockTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_find_by_id_returns_comment_data() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findById("comment-id")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_find_by_id_returns_empty_when_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_by_article_id_with_user() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData comment2 =
        new CommentData("c2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleId("article-id"))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_with_null_user() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(comment1));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
  }

  @Test
  void should_find_by_article_id_returns_empty_list() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_cursor_returns_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_find_by_article_id_with_cursor_next_direction() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData comment2 =
        new CommentData("c2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertEquals(2, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_by_article_id_with_cursor_has_extra() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    List<CommentData> comments = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      comments.add(
          new CommentData(
              "c" + i, "body" + i, "article-id", new DateTime(), new DateTime(), profileData));
    }
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertEquals(2, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_by_article_id_with_cursor_prev_direction() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData comment2 =
        new CommentData("c2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertEquals(2, result.getData().size());
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_find_by_article_id_with_cursor_null_user() {
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(comment1));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertEquals(1, result.getData().size());
  }
}
