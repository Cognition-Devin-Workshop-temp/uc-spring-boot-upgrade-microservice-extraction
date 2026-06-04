package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
  }

  @Test
  public void should_generate_id_on_creation() {
    Tag tag = new Tag("java");
    assertNotNull(tag.getId());
  }

  @Test
  public void should_be_equal_when_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
  }

  @Test
  public void should_not_be_equal_when_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }

  @Test
  public void should_be_equal_to_self() {
    Tag tag = new Tag("java");
    assertEquals(tag, tag);
  }

  @Test
  public void should_not_equal_null() {
    Tag tag = new Tag("java");
    assertNotEquals(null, tag);
  }

  @Test
  public void should_not_equal_different_type() {
    Tag tag = new Tag("java");
    assertNotEquals("java", tag);
  }

  @Test
  public void should_have_same_hashcode_for_equal_tags() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_have_different_hashcode_for_different_tags() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_have_toString() {
    Tag tag = new Tag("java");
    String str = tag.toString();
    assertNotNull(str);
    assertEquals(true, str.contains("java"));
  }

  @Test
  public void should_equal_null_name_tags() {
    Tag t1 = new Tag();
    Tag t2 = new Tag();
    assertEquals(t1, t2);
    assertEquals(t1.hashCode(), t2.hashCode());
  }

  @Test
  public void should_not_equal_null_name_and_non_null_name() {
    Tag nullName = new Tag();
    Tag withName = new Tag("java");
    assertNotEquals(nullName, withName);
    assertNotEquals(withName, nullName);
  }

  @Test
  public void should_have_expected_hashcode_for_null_name() {
    Tag tag = new Tag();
    int expected = 59 + 43;
    assertEquals(expected, tag.hashCode());
  }

  @Test
  public void should_have_non_zero_hashcode() {
    Tag tag = new Tag("java");
    assertNotEquals(0, tag.hashCode());
  }

  @Test
  public void should_verify_canEqual_returns_true_for_tag_instance() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("spring");
    assertTrue(t1.canEqual(t2));
  }

  @Test
  public void should_verify_canEqual_returns_false_for_non_tag() {
    Tag tag = new Tag("java");
    assertFalse(tag.canEqual("not a tag"));
  }

  @Test
  public void should_not_equal_subclass_that_rejects_canEqual() {
    Tag tag = new Tag("java");
    Tag fake =
        new Tag("java") {
          @Override
          protected boolean canEqual(Object other) {
            return false;
          }
        };
    assertFalse(tag.equals(fake));
  }

  @Test
  public void should_set_name_via_setter() {
    Tag tag = new Tag();
    tag.setName("python");
    assertEquals("python", tag.getName());
  }

  @Test
  public void should_set_id_via_setter() {
    Tag tag = new Tag();
    tag.setId("custom-id");
    assertEquals("custom-id", tag.getId());
  }
}
