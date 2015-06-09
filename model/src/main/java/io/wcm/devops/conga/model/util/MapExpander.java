/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Expands map with shortcut keys like
 * {jvm.heapspace.max: 4096m}
 * to
 * {jvm: {heapspace: {max: 4096m}}}
 */
public final class MapExpander {

  private MapExpander() {
    // static methods only
  }

  /**
   * Expands shortcut keys
   * @param map Unexpanded map
   * @return Expanded amp
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> expand(Map<String, Object> map) {
    if (map == null) {
      return null;
    }

    Map<String, Object> expanded = new HashMap<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      Map.Entry<String, Object> expandedEntry = expandEntry(entry);
      Object value = expandedEntry.getValue();
      if (value instanceof Map) {
        Object existingValue = expanded.get(expandedEntry.getKey());
        if (existingValue instanceof Map) {
          value = MapMerger.merge((Map<String, Object>)existingValue, (Map<String, Object>)value);
        }
      }
      expanded.put(expandedEntry.getKey(), value);
    }

    return expanded;
  }

  private static Map.Entry<String, Object> expandEntry(Map.Entry<String, Object> entry) {
    if (!StringUtils.contains(entry.getKey(), ".")) {
      return new MapEntry<String, Object>(entry.getKey(), expandDeep(entry.getValue()));
    }

    String key = StringUtils.substringBefore(entry.getKey(), ".");
    String remaining = StringUtils.substringAfter(entry.getKey(), ".");

    Map<String, Object> map = new HashMap<>();
    map.put(remaining, expandDeep(entry.getValue()));
    Map<String, Object> expandedMap = expand(map);

    return new MapEntry<String, Object>(key, expandedMap);
  }

  @SuppressWarnings("unchecked")
  private static Object expandDeep(Object object) {
    if (object instanceof Map) {
      return expand((Map<String, Object>)object);
    }
    if (object instanceof List) {
      List<Object> expandedList = new ArrayList<>();
      for (Object item : (List)object) {
        expandedList.add(expandDeep(item));
      }
      return expandedList;
    }
    return object;
  }

}