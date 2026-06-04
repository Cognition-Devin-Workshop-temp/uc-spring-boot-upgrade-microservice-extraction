# Mutation Testing Report — Articles Domain

## Overview

PIT (Pitest) mutation testing was configured for the Articles domain packages:
- `io.spring.core.article` — `Article`, `Tag`, `ArticleRepository`
- `io.spring.application.article` — `ArticleCommandService`, `NewArticleParam`, `UpdateArticleParam`, `DuplicatedArticleValidator`

## Configuration

```groovy
pitest {
    targetClasses = ['io.spring.core.article.*', 'io.spring.application.article.*']
    targetTests = ['io.spring.*']
    excludedTestClasses = ['io.spring.selenium.*',
                           'io.spring.infrastructure.article.ArticleRepositoryTransactionTest']
    mutators = ['DEFAULTS']
    outputFormats = ['HTML', 'XML']
    timestampedReports = false
    threads = 4
    junit5PluginVersion = '0.15'
}
```

Run with: `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew pitest`

## Before / After Scores

| Metric                | Before | After  |
|-----------------------|--------|--------|
| Total mutations       | 69     | 69     |
| Killed                | 18     | 65     |
| Survived              | 14     | 2      |
| No coverage           | 37     | 2      |
| **Mutation score**    | **26%**| **94%**|
| Line coverage         | 65%    | 100%   |
| Test strength         | 56%    | 97%    |
| Tests run per mutation| 1.25   | 1.52   |

## Surviving Mutants Identified and Killed

The following 10+ surviving mutants were identified from the baseline run and killed by new/improved tests:

### 1. `Article::getDescription` — EmptyObjectReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Replaced return value with `""` for `Article::getDescription`.
**Fix:** Added `should_return_correct_description` test asserting `assertEquals("my description", article.getDescription())`.

### 2. `Article::getBody` — EmptyObjectReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Replaced return value with `""` for `Article::getBody`.
**Fix:** Added `should_return_correct_body` test asserting the actual body content.

### 3. `Article::getTitle` — EmptyObjectReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Replaced return value with `""` for `Article::getTitle`.
**Fix:** Added `should_return_correct_title` test asserting the actual title content.

### 4. `Article::update` (line 57, description check) — RemoveConditionalMutator (SURVIVED → KILLED)
**Mutation:** Removed the `!Util.isEmpty(description)` conditional, allowing description to update unconditionally.
**Fix:** Added `should_update_description_only` and `should_not_update_with_empty_strings` tests that verify description is only updated when non-empty.

### 5. `Article::update` (line 61, body check) — RemoveConditionalMutator (SURVIVED → KILLED)
**Mutation:** Removed the `!Util.isEmpty(body)` conditional, allowing body to update unconditionally.
**Fix:** Added `should_update_body_only` and `should_not_update_with_empty_strings` tests verifying body is only updated when non-empty.

### 6. `Article::equals` — multiple RemoveConditionalMutator mutations (4 SURVIVED → 3 KILLED)
**Mutations:** Removed various conditional checks in Lombok-generated `equals` method (canEqual, id null check, id equality).
**Fix:** Added tests using `@NoArgsConstructor` to create null-id articles, reflection-based subclass tests with matching ids, and explicit `assertNotEquals`/`assertEquals` for all equality scenarios.

### 7. `Article::hashCode` — MathMutator + PrimitiveReturnsMutator (4 NO_COVERAGE → 4 KILLED)
**Mutations:** Replaced `*` with `/`, `+` with `-`, conditional removal, and return 0.
**Fix:** Added `should_have_expected_hashcode_for_null_id` (asserts exact value `102`), `should_have_non_zero_hashcode`, and `should_have_different_hashcodes_for_different_ids`.

### 8. `Article::canEqual` — BooleanTrueReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Always return `true` from `canEqual`.
**Fix:** Added `should_verify_canEqual_returns_false_for_non_article` test directly asserting `canEqual("string")` returns false.

### 9. `Tag::equals/hashCode/canEqual` — multiple mutators (6 SURVIVED → 5 KILLED)
**Mutations:** Same patterns as Article: conditionals removed in equals, hashCode math mutations, canEqual always true.
**Fix:** Created `TagTest.java` with comprehensive tests: equality for same/different names, null-name tags via `@NoArgsConstructor`, exact hashCode assertions, subclass canEqual rejection, `toString` verification.

### 10. `NewArticleParam::getTagList` — EmptyObjectReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Replaced return value with `Collections.emptyList()`.
**Fix:** Added `should_return_tag_list_values` test asserting `assertEquals(tags, param.getTagList())`.

### 11. `NewArticleParam$Builder::toString` — EmptyObjectReturnValsMutator (SURVIVED → KILLED)
**Mutation:** Replaced builder `toString` return with `""`.
**Fix:** Added `should_have_builder_toString` test asserting the string is non-empty and contains expected content.

### 12. `ArticleCommandService::createArticle/updateArticle` — VoidMethodCallMutator + NullReturnValsMutator (5 NO_COVERAGE → 5 KILLED)
**Mutation:** Removed `articleRepository.save()` call; replaced return values with null.
**Fix:** Created `ArticleCommandServiceTest.java` with Mockito-based unit tests verifying both create and update paths, including `verify(articleRepository).save(any())`.

## Remaining Mutants (4)

The 4 remaining mutants are in Lombok-generated `equals` bytecode at deeply nested branches:

| Class     | Mutation | Status      | Reason |
|-----------|----------|-------------|--------|
| `Article` | `equals` block 2 conditional removal | SURVIVED | Bytecode-level canEqual bypass in Lombok-generated equals; equivalent mutation when both objects are same type |
| `Article` | `equals` block 3 boolean return true | NO_COVERAGE | Unreachable branch in Lombok-generated instanceof short-circuit |
| `Tag`     | `equals` block 2 conditional removal | SURVIVED | Same as Article — canEqual bypass equivalent mutation |
| `Tag`     | `equals` block 3 boolean return true | NO_COVERAGE | Same as Article — unreachable instanceof branch |

These are effectively **equivalent mutants** — the mutations don't change observable behavior because the `canEqual` bypass only matters in inheritance hierarchies that don't exist in this codebase.

## New Test Files

| File | Tests | Purpose |
|------|-------|---------|
| `src/test/java/io/spring/core/article/ArticleTest.java` | 23 tests (was 5) | Article field access, update logic, equals/hashCode, canEqual |
| `src/test/java/io/spring/core/article/TagTest.java` | 15 tests (new) | Tag equality, hashCode, toString, canEqual, setters |
| `src/test/java/io/spring/application/article/ArticleCommandServiceTest.java` | 3 tests (new) | ArticleCommandService create/update with Mockito |
| `src/test/java/io/spring/application/article/NewArticleParamTest.java` | 3 tests (new) | NewArticleParam builder, getters, toString |
| `src/test/java/io/spring/application/article/UpdateArticleParamTest.java` | 4 tests (new) | UpdateArticleParam getters and defaults |

## How to Run

```bash
# Run mutation testing
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew pitest

# View HTML report
open build/reports/pitest/index.html
```
