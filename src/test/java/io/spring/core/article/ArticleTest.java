package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_return_correct_description() {
    Article article = new Article("title", "my description", "body", Arrays.asList("java"), "123");
    assertEquals("my description", article.getDescription());
  }

  @Test
  public void should_return_correct_body() {
    Article article = new Article("title", "desc", "my body", Arrays.asList("java"), "123");
    assertEquals("my body", article.getBody());
  }

  @Test
  public void should_return_correct_title() {
    Article article = new Article("my title", "desc", "body", Arrays.asList("java"), "123");
    assertEquals("my title", article.getTitle());
  }

  @Test
  public void should_have_equal_articles_with_same_id() {
    Article article1 = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertEquals(article1, article1);
  }

  @Test
  public void should_not_equal_different_article() {
    Article article1 = new Article("title1", "desc", "body", Arrays.asList("java"), "123");
    Article article2 = new Article("title2", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals(article1, article2);
  }

  @Test
  public void should_not_equal_null() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals(null, article);
  }

  @Test
  public void should_not_equal_different_type() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals("a string", article);
  }

  @Test
  public void should_have_consistent_hashcode() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    int hash1 = article.hashCode();
    int hash2 = article.hashCode();
    assertEquals(hash1, hash2);
  }

  @Test
  public void should_update_description_only() {
    Article article = new Article("title", "old desc", "old body", Arrays.asList("java"), "123");
    article.update(null, "new desc", null);
    assertEquals("title", article.getTitle());
    assertEquals("new desc", article.getDescription());
    assertEquals("old body", article.getBody());
  }

  @Test
  public void should_update_body_only() {
    Article article = new Article("title", "old desc", "old body", Arrays.asList("java"), "123");
    article.update(null, null, "new body");
    assertEquals("title", article.getTitle());
    assertEquals("old desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_update_title_and_slug() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");
    article.update("new title", null, null);
    assertEquals("new title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("old desc", article.getDescription());
    assertEquals("old body", article.getBody());
  }

  @Test
  public void should_not_update_with_empty_strings() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    article.update("", "", "");
    assertEquals("title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  public void should_update_all_fields() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");
    article.update("new title", "new desc", "new body");
    assertEquals("new title", article.getTitle());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_set_updatedAt_on_update() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");
    assertNotNull(article.getUpdatedAt());
    article.update("new title", null, null);
    assertNotNull(article.getUpdatedAt());
  }

  @Test
  public void should_create_tags_from_tag_list() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java", "spring"), "123");
    assertEquals(2, article.getTags().size());
  }

  @Test
  public void should_create_article_with_empty_tags() {
    Article article = new Article("title", "desc", "body", Collections.emptyList(), "123");
    assertNotNull(article.getTags());
    assertEquals(0, article.getTags().size());
  }

  @Test
  public void should_set_userId() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "user-42");
    assertEquals("user-42", article.getUserId());
  }

  @Test
  public void should_generate_unique_id() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotNull(article.getId());
  }

  @Test
  public void should_set_createdAt() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotNull(article.getCreatedAt());
  }

  @Test
  public void should_equal_null_id_articles() {
    Article a1 = new Article();
    Article a2 = new Article();
    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  public void should_not_equal_null_id_and_non_null_id() {
    Article nullId = new Article();
    Article withId = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals(nullId, withId);
    assertNotEquals(withId, nullId);
  }

  @Test
  public void should_have_non_zero_hashcode() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals(0, article.hashCode());
  }

  @Test
  public void should_have_expected_hashcode_for_null_id() {
    Article article = new Article();
    int expected = 59 + 43;
    assertEquals(expected, article.hashCode());
  }

  @Test
  public void should_have_different_hashcodes_for_different_ids() {
    Article a1 = new Article("title1", "desc", "body", Arrays.asList("java"), "123");
    Article a2 = new Article("title2", "desc", "body", Arrays.asList("java"), "123");
    assertNotEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  public void should_not_equal_subclass_that_rejects_canEqual() throws Exception {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    Article fake =
        new Article() {
          @Override
          protected boolean canEqual(Object other) {
            return false;
          }
        };
    Field idField = Article.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(fake, article.getId());
    assertFalse(article.equals(fake));
  }

  @Test
  public void should_verify_canEqual_returns_true_for_article_instance() {
    Article a1 = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    Article a2 = new Article("title2", "desc", "body", Arrays.asList("java"), "123");
    assertTrue(a1.canEqual(a2));
  }

  @Test
  public void should_verify_canEqual_returns_false_for_non_article() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertFalse(article.canEqual("not an article"));
  }
}
