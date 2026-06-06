# Refactoring Log — Articles Domain

This document details the top 5 code quality issues identified in the Articles domain and the refactoring applied to each.

---

## 1. Duplicated Connection-Building Logic in `ArticleDatafetcher`

**Problem:**  
`ArticleDatafetcher.java` (384 lines) contained 5 methods (`getFeed`, `userFeed`, `userFavorites`, `userArticles`, `getArticles`) that each repeated the same 15-line block: building `ArticlesConnection` from a `CursorPager<ArticleData>`, constructing `ArticleEdge` objects, creating `PageInfo`, and assembling the `DataFetcherResult` with `localContext`. This inflated the class by ~75 duplicated lines and made maintenance error-prone.

Additionally, 3 single-article resolvers (`getArticle`, `getCommentArticle`, `findArticleBySlug`) duplicated the same anonymous `HashMap` pattern for local context.

**Refactoring Approach:**  
- Extracted `buildConnectionResult(CursorPager<ArticleData>)` — a single private method that builds the `ArticlesConnection`, maps edges, constructs page info, and returns the `DataFetcherResult`.
- Extracted `buildSingleArticleContext(ArticleData)` for the repeated single-article HashMap pattern.
- All 5 paginated methods now delegate to `buildConnectionResult`, and the 3 single-article resolvers use `buildSingleArticleContext`.

**Result:** Class reduced from 384 → 275 lines (~28% reduction). All existing tests pass.

---

## 2. Duplicated Response Envelope Pattern in REST Controllers

**Problem:**  
Three controllers (`ArticleApi`, `ArticlesApi`, `ArticleFavoriteApi`) each created anonymous `HashMap<String, Object>` instances with a double-brace initializer to wrap article data under an `"article"` key. This pattern:
- Was scattered across 4 separate locations
- Used mutable `HashMap` unnecessarily
- Made the response envelope format implicit and hard to change globally

**Refactoring Approach:**  
- Created `ArticleResponseHelper` utility class with a single static method `withArticleEnvelope(ArticleData)` that returns `ResponseEntity<Map<String, Object>>` using `Collections.singletonMap` (immutable).
- Replaced all inline HashMap constructions in the 3 controllers with calls to `ArticleResponseHelper.withArticleEnvelope(...)`.
- Enabled use of method references (e.g., `ArticleResponseHelper::withArticleEnvelope`) for cleaner lambda expressions.

**Result:** Eliminated 4 instances of duplicated envelope construction. Response format is now defined in one place. All existing tests pass.

---

## 3. Missing Null Safety in `Article.toSlug()` and Redundant `updatedAt` Assignments

**Problem:**  
- `Article.toSlug(String title)` called `title.toLowerCase()` without a null check, causing `NullPointerException` when invoked with a null title.
- `Article.update(...)` set `this.updatedAt = new DateTime()` independently in each `if` block, creating up to 3 separate `DateTime` objects per call. This was wasteful and meant the final `updatedAt` timestamp could differ from the earlier assignments within the same method invocation.

**Refactoring Approach:**  
- Added a null/empty guard at the top of `toSlug()` that returns `""` for null or empty titles.
- Replaced per-field `updatedAt` assignments with a single `boolean modified` flag. The timestamp is set once at the end of the method, only if at least one field was actually modified.

**Result:** Eliminates NPE risk. `updatedAt` is now set atomically and only when needed. All existing tests pass.

---

## 4. Code Quality Cleanup in `ArticleQueryService`

**Problem:**  
- Typo in variable name: `followdUsers` (missing 'e') appeared in two methods, reducing readability.
- Used `list.size() == 0` instead of idiomatic `list.isEmpty()` across 6 locations.
- Unnecessary `else` blocks after early-return `if` statements added indentation depth without value.

**Refactoring Approach:**  
- Renamed `followdUsers` → `followedUsers` throughout.
- Replaced all `list.size() == 0` checks with `list.isEmpty()`.
- Removed unnecessary `else` blocks after early returns, flattening the control flow.

**Result:** Improved readability and consistency. All existing tests pass.

---

## 5. Duplicated Authorization + Fetch Pattern

**Problem:**  
The pattern "fetch article by slug → check if user is author → throw 403 if not" was repeated in:
- `ArticleApi.updateArticle()` and `ArticleApi.deleteArticle()` (REST layer)
- `ArticleMutation.updateArticle()` and `ArticleMutation.deleteArticle()` (GraphQL layer)

This tight coupling of lookup + authorization in each handler made the authorization logic scattered and easy to implement inconsistently.

**Refactoring Approach:**  
- In `ArticleApi`: extracted a private `findArticleAndCheckAuthorization(slug, user)` method that encapsulates the repository lookup + authorization check. Both `updateArticle` and `deleteArticle` now call this single method.
- In `ArticleCommandService`: added `findArticleBySlugAndVerifyAuthor(slug, user)` as a reusable service method. The `ArticleMutation` GraphQL handlers now use this instead of duplicating the pattern inline.
- Also simplified `ArticleFavoriteApi.unfavoriteArticle()` by replacing verbose lambda with method reference: `.ifPresent(articleFavoriteRepository::remove)`.

**Result:** Authorization logic is centralized per layer. Adding new article-mutating endpoints only requires calling one method. All existing tests pass (68/68).

---

## Summary

| # | Issue | Type | Files Changed |
|---|-------|------|---------------|
| 1 | Duplicated connection-building | Duplication | `ArticleDatafetcher.java` |
| 2 | Duplicated response envelope | Duplication | `ArticleApi.java`, `ArticlesApi.java`, `ArticleFavoriteApi.java`, new `ArticleResponseHelper.java` |
| 3 | Null-unsafe `toSlug()` + redundant timestamps | Missing error handling | `Article.java` |
| 4 | Typos + non-idiomatic checks | Unclear naming | `ArticleQueryService.java` |
| 5 | Duplicated auth+fetch pattern | Tight coupling | `ArticleApi.java`, `ArticleMutation.java`, `ArticleCommandService.java` |

**All 68 existing tests pass after these refactorings.**
