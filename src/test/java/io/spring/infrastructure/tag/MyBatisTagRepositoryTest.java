package io.spring.infrastructure.tag;

import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisTagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisTagRepository.class})
public class MyBatisTagRepositoryTest extends DbTestBase {
  @Autowired private TagRepository tagRepository;

  @Test
  public void should_create_and_fetch_tag() {
    Tag tag = new Tag("test-tag");
    tagRepository.save(tag);
    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("test-tag", found.get().getName());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_find_tag_by_name() {
    Tag tag = new Tag("unique-tag");
    tagRepository.save(tag);
    Optional<Tag> found = tagRepository.findByName("unique-tag");
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_return_empty_when_tag_not_found() {
    Assertions.assertFalse(tagRepository.findById("nonexistent-id").isPresent());
    Assertions.assertFalse(tagRepository.findByName("nonexistent-name").isPresent());
  }

  @Test
  public void should_find_all_tags() {
    Tag tag1 = new Tag("alpha");
    Tag tag2 = new Tag("beta");
    tagRepository.save(tag1);
    tagRepository.save(tag2);
    List<Tag> tags = tagRepository.findAll();
    Assertions.assertTrue(tags.size() >= 2);
    Assertions.assertTrue(tags.stream().anyMatch(t -> t.getName().equals("alpha")));
    Assertions.assertTrue(tags.stream().anyMatch(t -> t.getName().equals("beta")));
  }

  @Test
  public void should_update_tag() {
    Tag tag = new Tag("old-name");
    tagRepository.save(tag);
    tag.setName("new-name");
    tagRepository.update(tag);
    Optional<Tag> updated = tagRepository.findById(tag.getId());
    Assertions.assertTrue(updated.isPresent());
    Assertions.assertEquals("new-name", updated.get().getName());
  }

  @Test
  public void should_delete_tag() {
    Tag tag = new Tag("to-delete");
    tagRepository.save(tag);
    Assertions.assertTrue(tagRepository.findById(tag.getId()).isPresent());
    tagRepository.remove(tag.getId());
    Assertions.assertFalse(tagRepository.findById(tag.getId()).isPresent());
  }
}
