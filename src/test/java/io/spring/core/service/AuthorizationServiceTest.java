package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User articleOwner;
  private User otherUser;
  private Article article;

  @BeforeEach
  void setUp() {
    articleOwner = new User("owner@test.com", "owner", "password", "", "");
    otherUser = new User("other@test.com", "other", "password", "", "");
    article =
        new Article("Test Article", "desc", "body", Arrays.asList("java"), articleOwner.getId());
  }

  @Test
  void should_be_instantiable() {
    AuthorizationService service = new AuthorizationService();
    assertNotNull(service);
  }

  @Test
  void should_allow_article_owner_to_write() {
    assertTrue(AuthorizationService.canWriteArticle(articleOwner, article));
  }

  @Test
  void should_deny_non_owner_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  void should_allow_article_owner_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleOwner, article, comment));
  }

  @Test
  void should_allow_comment_owner_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(otherUser, article, comment));
  }

  @Test
  void should_deny_non_owner_to_write_comment() {
    User thirdUser = new User("third@test.com", "third", "password", "", "");
    Comment comment = new Comment("body", otherUser.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(thirdUser, article, comment));
  }
}
