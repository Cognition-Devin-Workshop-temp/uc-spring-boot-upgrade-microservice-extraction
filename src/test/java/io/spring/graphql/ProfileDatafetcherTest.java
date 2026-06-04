package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
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
import java.util.Arrays;
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

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void should_get_article_author() {
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList(),
            profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, profileData);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);

    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("comment-id").build();
    when(dfe.getSource()).thenReturn(comment);

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_when_profile_not_found() {
    when(profileQueryService.findByUsername("nonexist", user)).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("nonexist");

    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("nonexist", dfe));
  }
}
