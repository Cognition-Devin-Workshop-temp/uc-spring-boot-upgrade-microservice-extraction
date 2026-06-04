package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  public void should_build_with_all_fields() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("My Title")
            .description("My Description")
            .body("My Body")
            .tagList(tags)
            .build();

    assertEquals("My Title", param.getTitle());
    assertEquals("My Description", param.getDescription());
    assertEquals("My Body", param.getBody());
    assertNotNull(param.getTagList());
    assertEquals(2, param.getTagList().size());
    assertTrue(param.getTagList().contains("java"));
    assertTrue(param.getTagList().contains("spring"));
  }

  @Test
  public void should_return_tag_list_values() {
    List<String> tags = Arrays.asList("react", "typescript");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(tags)
            .build();

    assertEquals(tags, param.getTagList());
  }

  @Test
  public void should_have_builder_toString() {
    String str = NewArticleParam.builder().title("t").description("d").body("b").toString();
    assertNotNull(str);
    assertFalse(str.isEmpty());
    assertTrue(str.contains("t"));
  }
}
