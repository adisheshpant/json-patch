package com;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public interface JsonPatch {

   List<Operation> getDiff(String json1, String json2) throws IOException;

   List<Operation> getDiff(JsonNode json1, JsonNode json2);

}
