package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
  @Mock private DataFetchingEnvironment dfe;

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
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void should_get_article_author() {
    ArticleData articleData =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            java.util.Collections.emptyList(),
            profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("slug").build();
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    when(dfe.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("comment-id").build();
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    when(dfe.getArgument("username")).thenReturn("testuser");
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_when_profile_not_found() {
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), any())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> profileDatafetcher.getUserProfile(dfe));
  }
}
