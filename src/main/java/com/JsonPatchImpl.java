package com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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

  private void compare(JsonNode json1, JsonNode json2, String path, List<Operation> operations) {

    if (json1.equals(json2)) return;

    Iterator<Entry<String, JsonNode>> fields1 = json1.fields();

    while (fields1.hasNext()) {

      Entry<String, JsonNode> entry = fields1.next();
      JsonNode field1Node = entry.getValue();
      String field1Name = entry.getKey();

      // Check if second json is missing the field
      if (!json2.has(field1Name)) {
        operations.add(new Operation("add", path + field1Name, field1Node.toString()));
        continue;
      }

      JsonNode field2Node = json2.get(field1Name);
      if (field1Node.getNodeType() == JsonNodeType.OBJECT && field2Node.getNodeType() == JsonNodeType.OBJECT) {
        compare(field1Node, field2Node, path + field1Name + "/", operations);
        continue;
      }

      if (!field1Node.equals(field2Node)) {
        operations.add(new Operation("replace", path + field1Name, field1Node.toString()));
      }

    }

    Iterator<Entry<String, JsonNode>> fields2 = json2.fields();
    while (fields2.hasNext()) {
      Entry<String, JsonNode> entry = fields2.next();

      if (!json1.has(entry.getKey())) {
        operations.add(new Operation("remove", path + entry.getKey(), null));
      }
    }
  }
}
