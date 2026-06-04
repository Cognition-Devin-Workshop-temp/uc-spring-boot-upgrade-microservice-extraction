package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
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
    article =
        new Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId(), new DateTime());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_success() {
    String slug = article.getSlug();
    when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    var result = commentMutation.createComment(slug, "comment body");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_fail_create_comment_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment(article.getSlug(), "body"));
  }

  @Test
  void should_fail_create_comment_when_article_not_found() {
    when(articleRepository.findBySlug("no-slug")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("no-slug", "body"));
  }

  @Test
  void should_delete_comment_success() {
    String slug = article.getSlug();
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(slug, comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_fail_delete_comment_when_not_authorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other", "desc", "body", Arrays.asList(), otherUser.getId(), new DateTime());
    Comment comment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(articleRepository.findBySlug(otherArticle.getSlug()))
        .thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(otherArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(otherArticle.getSlug(), comment.getId()));
  }

  @Test
  void should_fail_delete_comment_when_comment_not_found() {
    when(articleRepository.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexist")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "nonexist"));
  }

  @Test
  void should_fail_delete_comment_when_article_not_found() {
    when(articleRepository.findBySlug("no-slug")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("no-slug", "comment-id"));
  }
}
