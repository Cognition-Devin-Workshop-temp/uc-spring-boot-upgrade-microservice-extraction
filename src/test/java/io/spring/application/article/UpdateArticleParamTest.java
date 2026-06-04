package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  public void should_return_title() {
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");
    assertEquals("New Title", param.getTitle());
  }

  @Test
  public void should_return_body() {
    UpdateArticleParam param = new UpdateArticleParam("", "New Body", "");
    assertEquals("New Body", param.getBody());
  }

  @Test
  public void should_return_description() {
    UpdateArticleParam param = new UpdateArticleParam("", "", "New Description");
    assertEquals("New Description", param.getDescription());
  }

  @Test
  public void should_have_empty_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
