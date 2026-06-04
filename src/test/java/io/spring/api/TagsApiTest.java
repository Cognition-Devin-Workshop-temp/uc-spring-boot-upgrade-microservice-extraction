package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TagsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TagsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private TagsQueryService tagsQueryService;

  @MockBean private TagRepository tagRepository;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_all_tags() throws Exception {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring"));

    RestAssuredMockMvc.when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags[0]", equalTo("java"))
        .body("tags[1]", equalTo("spring"));
  }

  @Test
  public void should_get_tag_by_id() throws Exception {
    Tag tag = new Tag("java");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    RestAssuredMockMvc.when()
        .get("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("java"))
        .body("tag.id", equalTo(tag.getId()));
  }

  @Test
  public void should_404_when_tag_not_found() throws Exception {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/tags/{id}", "nonexistent").then().statusCode(404);
  }

  @Test
  public void should_create_tag() throws Exception {
    doNothing().when(tagRepository).save(any(Tag.class));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\":{\"name\":\"new-tag\"}}")
        .when()
        .post("/tags")
        .then()
        .statusCode(201)
        .body("tag.name", equalTo("new-tag"));

    verify(tagRepository).save(any(Tag.class));
  }

  @Test
  public void should_require_auth_for_create() throws Exception {
    given()
        .contentType("application/json")
        .body("{\"tag\":{\"name\":\"new-tag\"}}")
        .when()
        .post("/tags")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_update_tag() throws Exception {
    Tag tag = new Tag("old-name");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));
    doNothing().when(tagRepository).update(any(Tag.class));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\":{\"name\":\"new-name\"}}")
        .when()
        .put("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("new-name"));

    verify(tagRepository).update(any(Tag.class));
  }

  @Test
  public void should_404_when_updating_nonexistent_tag() throws Exception {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\":{\"name\":\"new-name\"}}")
        .when()
        .put("/tags/{id}", "nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_delete_tag() throws Exception {
    Tag tag = new Tag("to-delete");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));
    doNothing().when(tagRepository).remove(eq(tag.getId()));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", tag.getId())
        .then()
        .statusCode(204);

    verify(tagRepository).remove(eq(tag.getId()));
  }

  @Test
  public void should_404_when_deleting_nonexistent_tag() throws Exception {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", "nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_require_auth_for_delete() throws Exception {
    given().when().delete("/tags/{id}", "some-id").then().statusCode(401);
  }
}
