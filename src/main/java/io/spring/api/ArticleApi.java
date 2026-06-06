package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(ArticleResponseHelper::withArticleEnvelope)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    Article article = findArticleAndCheckAuthorization(slug, user);
    Article updatedArticle = articleCommandService.updateArticle(article, updateArticleParam);
    return ArticleResponseHelper.withArticleEnvelope(
        articleQueryService.findBySlug(updatedArticle.getSlug(), user).get());
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article = findArticleAndCheckAuthorization(slug, user);
    articleRepository.remove(article);
    return ResponseEntity.<Void>noContent().build();
  }

  private Article findArticleAndCheckAuthorization(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }
    return article;
  }
}
