# Refactoring Log — Articles Domain

## Summary

Five high-impact code quality issues were identified and refactored in the Articles domain.
All existing tests pass after each change (`./gradlew test -x jacocoTestCoverageVerification`).

---

## Refactoring 1: Extract Duplicated Cursor-Pagination Logic in `ArticleDatafetcher`

**File:** `src/main/java/io/spring/graphql/ArticleDatafetcher.java`

### Problem
Five methods (`getFeed`, `userFeed`, `userFavorites`, `userArticles`, `getArticles`) each contained 30+ near-identical lines to:
1. Validate pagination arguments
2. Build a `CursorPageParameter`
3. Build an `ArticlesConnection` with edges
4. Wrap the result in a `DataFetcherResult` with `localContext`

This resulted in ~150 lines of duplicated code in a 384-line file.

### Refactoring Approach
- Extracted `validatePaginationArgs(Integer first, Integer last)` for the repeated null-check.
- Extracted `resolveArticlesCursor(...)` to encapsulate the first/last branching and query-service delegation.
- Extracted `buildConnectionResult(CursorPager<ArticleData>)` to build the `ArticlesConnection`, edges, and `DataFetcherResult` in one place.
- Replaced anonymous `HashMap` inner classes with `Map.of(...)` in single-article data fetchers.

### Result
File reduced from 384 lines to 247 lines (36% reduction). Each public method is now a concise delegation to the shared helpers.

### Tests
All tests pass — no behavioral change.

---

## Refactoring 2: Eliminate Double-Brace HashMap Initialization (Response Envelope)

**Files:**
- `src/main/java/io/spring/api/ArticleApi.java`
- `src/main/java/io/spring/api/ArticlesApi.java`
- `src/main/java/io/spring/api/ArticleFavoriteApi.java`
- `src/main/java/io/spring/api/ResponseWrapper.java` *(new)*

### Problem
Three controllers used anonymous `HashMap` subclass initialization (`new HashMap<>() {{ put(...); }}`) to wrap domain objects in a JSON envelope like `{"article": ...}`. This anti-pattern:
- Creates a new anonymous inner class per call-site (class-loading overhead).
- Holds a reference to the enclosing instance (potential memory leak).
- Obscures intent behind boilerplate.

### Refactoring Approach
- Introduced `ResponseWrapper.wrap(String key, Object value)` — a one-liner utility returning an immutable `Collections.singletonMap`.
- Replaced all three controllers' envelope creation with `ResponseWrapper.wrap("article", ...)`.
- Removed now-unused `articleResponse()` and `responseArticleData()` private methods.

### Result
Each controller lost 5–8 lines of boilerplate. The response structure is now consistent and memory-safe.

### Tests
All tests pass — response JSON is identical.

---

## Refactoring 3: Fix Unclear Naming in `ArticleQueryService`

**File:** `src/main/java/io/spring/application/ArticleQueryService.java`

### Problem
- `fillExtraInfo` — ambiguous; does not describe *what* information is filled.
- `followdUsers` — typo (`d` instead of `ed`).
- `articleData1` — meaningless lambda parameter name in `setIsFollowingAuthor`.

### Refactoring Approach
- Renamed `fillExtraInfo(...)` → `populateFavoritesAndFollowing(...)` (both overloads).
- Renamed `followdUsers` → `followedUsers` in `findUserFeedWithCursor` and `findUserFeed`.
- Renamed `articleData1` → `article` in the stream lambda.

### Result
Method and variable names now communicate intent without requiring readers to inspect the method body.

### Tests
All tests pass — rename only, no logic change.

---

## Refactoring 4: Fix `Article.update()` Redundant Timestamp Creation and Add Null-Safety to `toSlug()`

**File:** `src/main/java/io/spring/core/article/Article.java`

### Problem
1. `update(...)` created up to three separate `DateTime` objects (one per non-empty field), even though only the last one survives. This is wasteful and semantically misleading — an update should have a single timestamp.
2. `toSlug(String title)` had no null guard. Passing a null title (e.g., from a partial update where title is not provided) would throw a `NullPointerException`.

### Refactoring Approach
1. Replaced per-field `this.updatedAt = new DateTime()` with a single `boolean updated` flag. A single `DateTime` is created only if at least one field was actually changed.
2. Added an early-return in `toSlug`: if title is null or empty, return `""`.

### Result
- Exactly one `DateTime` allocation per update (or zero if nothing changed).
- `toSlug` is now defensive against null/empty input.

### Tests
All tests pass — `ArticleTest` and `ArticleApiTest` cover update scenarios.

---

## Refactoring 5: Extract Duplicate Authorization Pattern in `ArticleApi`

**File:** `src/main/java/io/spring/api/ArticleApi.java`

### Problem
Both `updateArticle` and `deleteArticle` duplicated the same three-step pattern:
1. Fetch article by slug (or throw 404).
2. Check if the user can write (or throw 403).
3. Perform the action.

This tight coupling of lookup + authorization + action inside nested lambdas made the methods hard to read and violated DRY.

### Refactoring Approach
- Extracted `findArticleAndCheckAuthorization(String slug, User user)` which encapsulates steps 1–2 and returns the authorized `Article`.
- `updateArticle` and `deleteArticle` now call this helper and focus solely on their action.

### Result
Each endpoint method dropped from ~12 lines (with nested lambdas) to 3–4 flat lines. Authorization logic is defined once, reducing the risk of inconsistent checks.

### Tests
All tests pass — `ArticleApiTest` covers both authorized and unauthorized scenarios.
