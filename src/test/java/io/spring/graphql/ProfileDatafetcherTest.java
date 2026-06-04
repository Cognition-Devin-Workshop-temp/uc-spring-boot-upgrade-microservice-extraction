package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.HashMap;
import java.util.Map;
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
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "http://img.com/pic.png");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "http://img.com/pic.png", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("http://img.com/pic.png", result.getImage());
  }

  @Test
  void should_get_author_from_article() {
    ProfileData articleAuthor =
        new ProfileData("author-id", "author", "author bio", "http://img.com/author.png", false);
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            java.util.Arrays.asList("java"),
            articleAuthor);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article graphqlArticle = Article.newBuilder().slug("test-slug").build();
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(graphqlArticle);
    when(profileQueryService.findByUsername(eq("author"), eq(user)))
        .thenReturn(Optional.of(articleAuthor));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("author", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    ProfileData commentAuthor =
        new ProfileData("commenter-id", "commenter", "bio", "http://img.com/c.png", false);
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), commentAuthor);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);

    Comment graphqlComment = Comment.newBuilder().id("comment-id").build();
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(graphqlComment);
    when(profileQueryService.findByUsername(eq("commenter"), eq(user)))
        .thenReturn(Optional.of(commentAuthor));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("commenter", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    ProfileData profileData =
        new ProfileData("other-id", "otheruser", "bio", "http://img.com/other.png", true);
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("otheruser");
    when(profileQueryService.findByUsername(eq("otheruser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("otheruser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("otheruser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }

  @Test
  void should_throw_not_found_when_profile_missing() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.getUserProfile(dataFetchingEnvironment));
  }
}
