package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;
  private TagRepository tagRepository;

  @GetMapping
  public ResponseEntity getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTags());
          }
        });
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity getTag(@PathVariable("id") String id) {
    return tagRepository
        .findById(id)
        .map(tag -> ResponseEntity.ok(tagResponse(tag)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping
  public ResponseEntity createTag(@Valid @RequestBody NewTagParam newTagParam) {
    Tag tag = new Tag(newTagParam.getName());
    tagRepository.save(tag);
    return ResponseEntity.status(201).body(tagResponse(tag));
  }

  @PutMapping(path = "/{id}")
  public ResponseEntity updateTag(
      @PathVariable("id") String id, @Valid @RequestBody UpdateTagParam updateTagParam) {
    Tag tag = tagRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    tag.setName(updateTagParam.getName());
    tagRepository.update(tag);
    return ResponseEntity.ok(tagResponse(tag));
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity deleteTag(@PathVariable("id") String id) {
    tagRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    tagRepository.remove(id);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> tagResponse(Tag tag) {
    return new HashMap<String, Object>() {
      {
        put("tag", tag);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("tag")
class NewTagParam {
  @NotBlank(message = "can't be empty")
  private String name;
}

@Getter
@NoArgsConstructor
@JsonRootName("tag")
class UpdateTagParam {
  @NotBlank(message = "can't be empty")
  private String name;
}
