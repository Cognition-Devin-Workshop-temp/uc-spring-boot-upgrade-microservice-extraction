package io.spring.api;

import io.spring.application.data.ArticleData;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public final class ArticleResponseHelper {

  private ArticleResponseHelper() {}

  public static ResponseEntity<Map<String, Object>> withArticleEnvelope(ArticleData articleData) {
    return ResponseEntity.ok(Collections.singletonMap("article", articleData));
  }
}
