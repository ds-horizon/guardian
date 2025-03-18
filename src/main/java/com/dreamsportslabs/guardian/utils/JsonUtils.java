package com.dreamsportslabs.guardian.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

  private JsonUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  private static final ObjectMapper snakeCaseObjectMapper =
      JsonMapper.builder()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
          .serializationInclusion(JsonInclude.Include.NON_NULL)
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .build();

  public static JsonObject getJsonObjectFromNestedJson(JsonObject json, String flattenedKey) {
    JsonObject cur = json;
    String[] keys = flattenedKey.split("\\.");
    for (String key : keys) {
      if (!cur.containsKey(key)) {
        return new JsonObject();
      }
      cur = cur.getJsonObject(key);
    }
    return cur;
  }

  @SneakyThrows
  public static <T> List<T> rowSetToList(RowSet<Row> rows, Class<T> clazz) {
    List<T> list = new ArrayList<>();

    if (rows.columnsNames().size() == 1) {
      String columnName = rows.columnsNames().get(0);
      for (Row row : rows) {
        list.add((T) row.getValue(columnName));
      }
      return list;
    }

    for (Row row : rows) {
      list.add(snakeCaseObjectMapper.readValue(row.toJson().toString(), clazz));
    }
    return list;
  }
}
