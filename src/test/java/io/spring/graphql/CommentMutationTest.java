package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList(), user.getId());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "commentId",
            "body",
            "articleId",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    var result = commentMutation.createComment("test-title", "body");
    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_create_comment_throw_when_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_create_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_delete_comment_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), anyString()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-title", "comment-id");
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_delete_comment_throw_when_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> commentMutation.removeComment("slug", "id"));
  }

  @Test
  void should_delete_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("nonexistent", "id"));
  }

  @Test
  void should_delete_comment_throw_when_comment_not_found() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(anyString(), anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "nonexistent"));
  }

  @Test
  void should_delete_comment_throw_no_authorization() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle = new Article("Title", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(otherArticle));
    Comment comment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(commentRepository.findById(eq(otherArticle.getId()), anyString()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class, () -> commentMutation.removeComment("title", "comment-id"));
  }
}
