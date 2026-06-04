package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private CommentData commentData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    DateTime now = new DateTime();
    commentData =
        new CommentData(
            "comment-id",
            "comment body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
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
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_payload() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    var result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("comment-id", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), CursorPager.Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    var result = commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    var result = commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_article_comments_both_first_and_last_null() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
