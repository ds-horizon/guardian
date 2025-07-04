package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.tuple.Pair;

public class SqlUtils {
  public static Pair<String, Tuple> prepareUpdateQuery(Object object) {
    List<Object> values = new ArrayList<>();
    StringJoiner insertSetFields = new StringJoiner(", ");

    JsonObject jsonObject = JsonObject.mapFrom(object);

    for (String key : jsonObject.fieldNames()) {
      Object value = jsonObject.getValue(key);

      if (key == null) {
        continue;
      } else {
        key = convertCamelToSnake(key);
      }

      if (value == null) {
        continue;
      }

      insertSetFields.add(key + " = ?"); // MySQL uses ? placeholders
      values.add(value);
    }

    if (values.isEmpty()) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    return Pair.of(insertSetFields.toString(), Tuple.wrap(values));
  }

  public static String convertCamelToSnake(String camelCaseString) {
    PropertyNamingStrategies.SnakeCaseStrategy snakeCaseStrategy =
        new PropertyNamingStrategies.SnakeCaseStrategy();

    return snakeCaseStrategy.translate(camelCaseString);
  }
}
