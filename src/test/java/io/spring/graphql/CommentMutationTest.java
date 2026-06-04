package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
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
import io.spring.graphql.types.CommentPayload;
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
    user = new User("test@test.com", "testuser", "password", "", "");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());

    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_success() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "", "", false));
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-title", "Great article!");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
    verify(commentQueryService).findById(any(), eq(user));
  }

  @Test
  void should_create_comment_fail_article_not_found() {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("non-existent", "body"));
  }

  @Test
  void should_create_comment_fail_unauthenticated() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);

    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("test-title", "body"));
  }

  @Test
  void should_delete_comment_success() {
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), any())).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(eq(comment));
  }

  @Test
  void should_delete_comment_fail_article_not_found() {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("non-existent", "comment-id"));
  }

  @Test
  void should_delete_comment_fail_comment_not_found() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("comment-id")))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "comment-id"));
  }

  @Test
  void should_delete_comment_fail_unauthorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());
    Comment otherComment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(articleRepository.findBySlug(eq("other-article"))).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(eq(otherArticle.getId()), any()))
        .thenReturn(Optional.of(otherComment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other-article", otherComment.getId()));
  }

  @Test
  void should_delete_comment_fail_unauthenticated() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-title", "comment-id"));
  }
}
