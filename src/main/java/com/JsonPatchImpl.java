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
    compare(json1, json2, "/", diff);

    return diff;
  }

  private static void compare(JsonNode j1, JsonNode j2, String path, List<Operation> operations) {
    if (j1.equals(j2)) return;

    JsonNodeType n1 = j1.getNodeType();
    JsonNodeType n2 = j2.getNodeType();

    if (n1 == OBJECT && n2 == OBJECT) {
      compareJObjects(j1, j2, path, operations);
    } else if (n1 == ARRAY && n2 == ARRAY) {
      compareArrays((ArrayNode) j1, (ArrayNode) j2, path, operations);
    } else {
      operations.add(Operation.replace(path, j2.toString()));
    }
  }

  private static void compareJObjects(JsonNode json1, JsonNode json2, String path, List<Operation> operations) {
    Iterator<Entry<String, JsonNode>> fields1 = json1.fields();

    // Compare L to R
    while (fields1.hasNext()) {
      Entry<String, JsonNode> entry = fields1.next();
      JsonNode field1Node = entry.getValue();
      String field1Name = entry.getKey();

      // Check if second json is missing the field
      if (!json2.has(field1Name)) {
        operations.add(Operation.add(path + field1Name, field1Node.toString()));
      } else {
        compare(field1Node, json2.get(field1Name), path + field1Name + "/", operations);
      }
    }

    // Compare R to L, only the missing ones
    Iterator<Entry<String, JsonNode>> fields2 = json2.fields();
    while (fields2.hasNext()) {
      Entry<String, JsonNode> entry = fields2.next();
      if (!json1.has(entry.getKey())) {
        operations.add(Operation.remove(path + entry.getKey()));
      }
    }
  }

  private static void compareArrays(ArrayNode a1, ArrayNode a2, String path, List<Operation> operations) {
    if (a1.equals(a2)) return;

    for (int i = 0; i < a1.size(); i++) {
      if (i >= a2.size()) {
        operations.add(Operation.add(path + i, a1.get(i).toString()));
      } else {
        compare(a1.get(i), a2.get(i), path + i + "/", operations);
      }
    }

    if (a2.size() > a1.size()) {
      for (int i = a1.size(); i < a2.size(); i++) {
        operations.add(Operation.remove(path + i));
      }
    }
  }
}
