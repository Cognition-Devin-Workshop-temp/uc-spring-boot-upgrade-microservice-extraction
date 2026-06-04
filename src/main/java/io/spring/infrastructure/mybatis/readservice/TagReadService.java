package io.spring.infrastructure.mybatis.readservice;

import io.spring.core.article.Tag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagReadService {
  List<String> all();

  List<Tag> allTags();

  Tag findById(String id);
}
