package io.spring.infrastructure.repository;

import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import io.spring.infrastructure.mybatis.mapper.TagMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisTagRepository implements TagRepository {
  private TagMapper tagMapper;

  public MyBatisTagRepository(TagMapper tagMapper) {
    this.tagMapper = tagMapper;
  }

  @Override
  public void save(Tag tag) {
    tagMapper.insert(tag);
  }

  @Override
  public Optional<Tag> findById(String id) {
    return Optional.ofNullable(tagMapper.findById(id));
  }

  @Override
  public Optional<Tag> findByName(String name) {
    return Optional.ofNullable(tagMapper.findByName(name));
  }

  @Override
  public List<Tag> findAll() {
    return tagMapper.findAll();
  }

  @Override
  public void update(Tag tag) {
    tagMapper.update(tag);
  }

  @Override
  public void remove(String id) {
    tagMapper.delete(id);
  }
}
