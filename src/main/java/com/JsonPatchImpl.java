package com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static java.util.Collections.emptyList;

public class JsonPatchImpl implements JsonPatch {

  private ObjectMapper objectMapper;

  public JsonPatchImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public List<Operation> getDiff(String json1, String json2) throws IOException {
    JsonNode jsonNode1 = objectMapper.readTree(json1);
    JsonNode jsonNode2 = objectMapper.readTree(json2);
    return getDiff(jsonNode1, jsonNode2);
  }

  @Override
  public List<Operation> getDiff(JsonNode json1, JsonNode json2) {
    if (json1.equals(json2)) return emptyList();

    final List<Operation> diff = new ArrayList<>();
    compare(json1, json2, "", diff);

    return diff;
  }

  /**
   * Compare two JSON nodes recursively.
   *
   * @param json1
   * @param json2
   * @param path
   * @param operations
   */
  private static void compare(JsonNode json1, JsonNode json2, String path, List<Operation> operations) {

    if (json1 != null && json2 != null) {

      if (json1.equals(json2)) {
        return;
      }

      JsonNodeType n1 = json1.getNodeType();
      JsonNodeType n2 = json2.getNodeType();

      if (n1 == OBJECT && n2 == OBJECT) {
        compareObjects(json1, json2, path + "/", operations);
      } else if (n1 == ARRAY && n2 == ARRAY) {
        compareArrays((ArrayNode) json1, (ArrayNode) json2, path + "/", operations);
      } else {
        operations.add(Operation.replace(path, json2.toString()));
      }

    } else if (json1 != null) {
      operations.add(Operation.remove(path));
    } else if (json2 != null) {
      operations.add(Operation.add(path, json2.toString()));
    }
  }

  /**
   * Compare two JSON objects. Object comparison involves field by field comparisons. If a field is
   * missing
   *
   * @param json1
   * @param json2
   * @param path
   * @param operations
   */
  private static void compareObjects(JsonNode json1, JsonNode json2, String path, List<Operation> operations) {

    // Compare L to R
    Iterator<Entry<String, JsonNode>> json1Fields = json1.fields();
    while (json1Fields.hasNext()) {
      Entry<String, JsonNode> entry = json1Fields.next();
      JsonNode json1Field = entry.getValue();
      String json1FieldName = entry.getKey();
      compare(json1Field, json2.get(json1FieldName), path + json1FieldName, operations);
    }

    // Compare R to L, only the missing ones
    Iterator<Entry<String, JsonNode>> json2Fields = json2.fields();
    while (json2Fields.hasNext()) {
      Entry<String, JsonNode> entry = json2Fields.next();
      String json2FieldName = entry.getKey();
      if (!json1.has(json2FieldName)) {
        operations.add(Operation.remove(path + json2FieldName));
      }
    }
  }

  /**
   * @param a1
   * @param a2
   * @param path
   * @param operations
   */
  private static void compareArrays(ArrayNode a1, ArrayNode a2, String path, List<Operation> operations) {

    for (int i = 0; i < a1.size(); i++) {
      if (i >= a2.size()) {
        operations.add(Operation.add(path + i, a1.get(i).toString()));
      } else {
        compare(a1.get(i), a2.get(i), path + i, operations);
      }
    }

    if (a2.size() > a1.size()) {
      for (int i = a1.size(); i < a2.size(); i++) {
        operations.add(Operation.remove(path + i));
      }
    }
  }
}
