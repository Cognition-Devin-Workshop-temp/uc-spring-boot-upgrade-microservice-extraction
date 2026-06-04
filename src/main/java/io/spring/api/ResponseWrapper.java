package io.spring.api;

import java.util.Collections;
import java.util.Map;

public final class ResponseWrapper {

  private ResponseWrapper() {}

  public static Map<String, Object> wrap(String key, Object value) {
    return Collections.singletonMap(key, value);
  }
}
